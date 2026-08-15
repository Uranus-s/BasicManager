package com.basic.sericve.ai.agent.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.basic.ai.agent.gateway.AiAgentGateway;
import com.basic.ai.agent.model.AiAgentCapability;
import com.basic.ai.agent.model.AiAgentDecision;
import com.basic.ai.agent.model.AiAgentPageContext;
import com.basic.ai.agent.model.AiAgentPreviousResult;
import com.basic.api.dto.aiAgent.AiAgentActionResultDTO;
import com.basic.api.dto.aiAgent.AiAgentCapabilityDTO;
import com.basic.api.dto.aiAgent.AiAgentPageSnapshotDTO;
import com.basic.api.dto.aiAgent.AiAgentTaskCreateDTO;
import com.basic.api.vo.aiAgent.AiAgentActionVO;
import com.basic.api.vo.aiAgent.AiAgentEventVO;
import com.basic.api.vo.aiAgent.AiAgentStatusVO;
import com.basic.api.vo.aiAgent.AiAgentTaskVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.dao.aiAgent.entity.AiAgentAction;
import com.basic.dao.aiAgent.entity.AiAgentTask;
import com.basic.dao.aiAgent.mapper.AiAgentActionMapper;
import com.basic.dao.aiAgent.mapper.AiAgentTaskMapper;
import com.basic.sericve.ai.agent.model.AiActionPolicy;
import com.basic.sericve.ai.agent.model.AiAgentRiskLevel;
import com.basic.sericve.ai.agent.model.AiAgentTaskStatus;
import com.basic.sericve.ai.agent.model.AiAgentUserContext;
import com.basic.sericve.ai.agent.policy.AiActionPolicyRegistry;
import com.basic.sericve.ai.agent.service.IAiAgentService;
import com.basic.sericve.ai.agent.support.AiAgentEventHub;
import com.basic.sericve.ai.agent.support.AiAgentSensitiveDataSanitizer;
import com.basic.sericve.ai.agent.support.AiAgentTaskStateMachine;
import com.basic.sericve.sysConfig.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * AI 网页代理服务实现，负责任务状态、风险门禁、客户端绑定和持久化编排。
 */
@Service
@RequiredArgsConstructor
public class AiAgentServiceImpl implements IAiAgentService {

    private static final Set<String> ACTION_TERMINAL_STATES = Set.of("SUCCEEDED", "FAILED");
    private static final Set<String> TASK_TERMINAL_STATES = Set.of(
            AiAgentTaskStatus.SUCCEEDED.name(),
            AiAgentTaskStatus.FAILED.name(),
            AiAgentTaskStatus.CANCELED.name());
    private static final Set<String> DISCONNECT_PAUSABLE_STATES = Set.of(
            AiAgentTaskStatus.PLANNING.name(),
            AiAgentTaskStatus.EXECUTING.name(),
            AiAgentTaskStatus.WAITING_CONFIRMATION.name());

    private final AiAgentTaskMapper taskMapper;
    private final AiAgentActionMapper actionMapper;
    private final AiAgentGateway gateway;
    private final AiActionPolicyRegistry policyRegistry;
    private final ISysConfigService configService;
    private final AiAgentSensitiveDataSanitizer sanitizer;
    private final AiAgentTaskStateMachine stateMachine;
    private final AiAgentEventHub eventHub;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    private record PlanningLease(
            Long taskId,
            String token,
            AiAgentUserContext user,
            AiAgentPageSnapshotDTO snapshot,
            AiAgentPreviousResult previousResult) {
    }

    private record PlanningOutcome(AiAgentEventVO event, boolean terminal) {
    }

    @Override
    public AiAgentStatusVO getStatus(AiAgentUserContext user, String clientInstanceId) {
        AiAgentStatusVO status = new AiAgentStatusVO();
        status.setEnabled(configService.isAiAgentEnabled());
        if (Boolean.TRUE.equals(status.getEnabled())) {
            AiAgentTask task = taskMapper.selectOne(new LambdaQueryWrapper<AiAgentTask>()
                    .eq(AiAgentTask::getUserId, user.userId())
                    .eq(AiAgentTask::getActiveKey, activeKey(user.userId()))
                    .last("LIMIT 1"));
            status.setActiveTask(task == null ? null : toTaskVO(task));
            status.setCurrentClient(task == null
                    || Objects.equals(task.getClientInstanceId(), clientInstanceId));
        } else {
            status.setCurrentClient(true);
        }
        return status;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiAgentTaskVO createTask(AiAgentUserContext user, AiAgentTaskCreateDTO dto) {
        requireEnabled();
        String taskActiveKey = activeKey(user.userId());
        AiAgentTask existingTask = taskMapper.selectOne(new LambdaQueryWrapper<AiAgentTask>()
                .eq(AiAgentTask::getUserId, user.userId())
                .eq(AiAgentTask::getActiveKey, taskActiveKey)
                .last("LIMIT 1 FOR UPDATE"));
        if (existingTask != null) {
            if (!TASK_TERMINAL_STATES.contains(existingTask.getStatus())) {
                throw new BusinessException(ResultEnum.AI_AGENT_BUSY);
            }
            // 兼容历史版本未把 null 写回数据库的终态记录，释放唯一键后再创建新任务。
            existingTask.setActiveKey(null);
            existingTask.setActiveActionId(null);
            existingTask.setPlanningToken(null);
            requireUpdated(taskMapper.updateById(existingTask));
        }
        AiAgentTask task = new AiAgentTask();
        task.setUserId(user.userId());
        task.setClientInstanceId(dto.getClientInstanceId());
        task.setActiveKey(taskActiveKey);
        task.setGoalSummary(sanitizer.sanitizeText(dto.getGoal()));
        task.setStatus(AiAgentTaskStatus.CREATED.name());
        task.setRouteName(dto.getSnapshot().getRouteName());
        task.setPageVersion(dto.getSnapshot().getPageVersion());
        task.setCurrentStep(0);
        task.setStartedAt(LocalDateTime.now());
        try {
            // activeKey 受数据库唯一约束保护，避免同一用户并发请求绕过应用层检查创建多个活动任务。
            taskMapper.insert(task);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ResultEnum.AI_AGENT_BUSY);
        }
        // 规划任务在当前创建事务提交后才启动，模型线程不会读取到尚未提交的任务记录。
        beginPlanning(user, task, dto.getSnapshot(), null);
        return toTaskVO(task);
    }

    @Override
    public AiAgentTaskVO getTask(AiAgentUserContext user, Long taskId) {
        return toTaskVO(requireTask(user, taskId));
    }

    @Override
    public Flux<ServerSentEvent<AiAgentEventVO>> events(
            AiAgentUserContext user, Long taskId, String clientInstanceId) {
        AiAgentTask task = requireTask(user, taskId);
        requireClient(task, clientInstanceId);
        // 订阅结束代表绑定页面失去执行通道；对仍可能下发动作的状态做保守暂停，等待页面重新认领。
        return eventHub.flux(taskId)
                .map(event -> ServerSentEvent.<AiAgentEventVO>builder(event)
                        .event(event.getType())
                        .build())
                .doFinally(ignored -> pauseDisconnectedClient(user, taskId, clientInstanceId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportResult(
            AiAgentUserContext user, Long taskId, String actionId, AiAgentActionResultDTO dto) {
        AiAgentTask task = requireTaskForUpdate(user, taskId);
        requireClient(task, dto.getClientInstanceId());
        AiAgentAction action = requireActionForUpdate(taskId, actionId);
        // 客户端重试可能重复上报，终态动作直接返回，保证结果处理和后续规划幂等。
        if (ACTION_TERMINAL_STATES.contains(action.getStatus())) {
            return;
        }
        // 动作只能在生成它的路由和页面版本执行；上下文漂移时暂停，而不是在旧快照上继续规划。
        if (!executionContextMatches(action, dto)) {
            pauseForPageContextChange(task, action);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        action.setFinishedAt(now);
        action.setResultSummary(sanitizer.sanitizeText(dto.getResultSummary()));
        if ("FAILED".equals(dto.getStatus())) {
            // 动作失败保留任务和快照供用户检查，任务进入可恢复的暂停态而不是直接终结。
            action.setStatus("FAILED");
            requireUpdated(actionMapper.updateById(action));
            task.setFailureCode("ACTION_FAILED");
            if (AiAgentTaskStatus.EXECUTING.name().equals(task.getStatus())) {
                transition(task, AiAgentTaskStatus.PAUSED);
                emitTaskEventAfterCommit(task, "paused", action.getResultSummary());
            }
            return;
        }
        action.setStatus("SUCCEEDED");
        requireUpdated(actionMapper.updateById(action));
        // 只有当前活动动作的成功结果才能触发下一轮规划，迟到结果不能推进已经变化的任务。
        if (AiAgentTaskStatus.EXECUTING.name().equals(task.getStatus())
                && Objects.equals(task.getActiveActionId(), actionId)) {
            beginPlanning(user, task, dto.getSnapshot(), new AiAgentPreviousResult(
                    action.getActionId(), action.getStatus(), action.getResultSummary()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pause(AiAgentUserContext user, Long taskId, String clientInstanceId) {
        AiAgentTask task = requireTaskForUpdate(user, taskId);
        requireClient(task, clientInstanceId);
        if (AiAgentTaskStatus.PAUSED.name().equals(task.getStatus())) {
            return;
        }
        transition(task, AiAgentTaskStatus.PAUSED);
        emitTaskEventAfterCommit(task, "paused", "AI 已暂停");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resume(
            AiAgentUserContext user,
            Long taskId,
            String clientInstanceId,
            AiAgentPageSnapshotDTO snapshot) {
        AiAgentTask task = requireTaskForUpdate(user, taskId);
        requireClient(task, clientInstanceId);
        if (!AiAgentTaskStatus.PAUSED.name().equals(task.getStatus())) {
            throw new BusinessException(ResultEnum.AI_AGENT_STATE_INVALID);
        }
        // 恢复必须携带页面重新采集的新快照，不能沿用断线或上下文变化前的旧页面状态。
        task.setFailureCode(null);
        beginPlanning(user, task, snapshot, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(AiAgentUserContext user, Long taskId, String clientInstanceId) {
        AiAgentTask task = requireTaskForUpdate(user, taskId);
        requireClient(task, clientInstanceId);
        finishTask(task, AiAgentTaskStatus.CANCELED, null, "任务已终止");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiAgentTaskVO claim(
            AiAgentUserContext user, Long taskId, String clientInstanceId) {
        AiAgentTask task = requireTaskForUpdate(user, taskId);
        if (!AiAgentTaskStatus.PAUSED.name().equals(task.getStatus())) {
            throw new BusinessException(ResultEnum.AI_AGENT_STATE_INVALID);
        }
        // 仅暂停任务允许转移标签页归属，防止执行中的动作同时被两个页面消费。
        task.setClientInstanceId(clientInstanceId);
        requireUpdated(taskMapper.updateById(task));
        return toTaskVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(AiAgentUserContext user, Long taskId, String actionId, String clientInstanceId) {
        AiAgentTask task = requireTaskForUpdate(user, taskId);
        requireClient(task, clientInstanceId);
        if (!AiAgentTaskStatus.WAITING_CONFIRMATION.name().equals(task.getStatus())
                || !Objects.equals(task.getActiveActionId(), actionId)) {
            throw new BusinessException(ResultEnum.AI_AGENT_STATE_INVALID);
        }
        AiAgentAction action = requireActionForUpdate(taskId, actionId);
        // 确认人与确认时间由登录态和服务端时钟写入；客户端只能选择是否确认，不能伪造审计信息。
        action.setConfirmedBy(user.userId());
        action.setConfirmedAt(LocalDateTime.now());
        action.setStartedAt(LocalDateTime.now());
        action.setStatus("EXECUTING");
        requireUpdated(actionMapper.updateById(action));
        transition(task, AiAgentTaskStatus.EXECUTING);
        emitAfterCommit(taskId, actionEvent(task, action), false);
    }

    @Override
    public boolean validateTrace(AiAgentUserContext user, Long taskId, String actionId) {
        // Trace 关联只接受当前用户正在执行的活动动作，不能仅凭客户端提供的 ID 建立可信链路。
        AiAgentTask task = taskMapper.selectById(taskId);
        if (task == null || !Objects.equals(task.getUserId(), user.userId())
                || !AiAgentTaskStatus.EXECUTING.name().equals(task.getStatus())
                || !Objects.equals(task.getActiveActionId(), actionId)) {
            return false;
        }
        AiAgentAction action = actionMapper.selectOne(new LambdaQueryWrapper<AiAgentAction>()
                .eq(AiAgentAction::getTaskId, taskId)
                .eq(AiAgentAction::getActionId, actionId));
        return action != null && "EXECUTING".equals(action.getStatus());
    }

    private void beginPlanning(
            AiAgentUserContext user,
            AiAgentTask task,
            AiAgentPageSnapshotDTO snapshot,
            AiAgentPreviousResult previousResult) {
        AiAgentTaskStatus source = AiAgentTaskStatus.valueOf(task.getStatus());
        stateMachine.requireTransition(source, AiAgentTaskStatus.PLANNING);
        // 每轮规划生成独立租约令牌；暂停、取消或新一轮规划会清除/替换令牌，使迟到模型响应自动失效。
        String token = UUID.randomUUID().toString();
        task.setStatus(AiAgentTaskStatus.PLANNING.name());
        task.setPlanningToken(token);
        task.setRouteName(snapshot.getRouteName());
        task.setPageVersion(snapshot.getPageVersion());
        task.setFailureCode(null);
        requireUpdated(taskMapper.updateById(task));
        schedulePlan(new PlanningLease(task.getId(), token, user, snapshot, previousResult));
    }

    private void schedulePlan(PlanningLease lease) {
        Runnable work = () -> Mono.fromRunnable(() -> planSafely(lease))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        // 事务内只登记提交后回调，避免异步线程在数据提交前抢先读取或发布事件。
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    work.run();
                }
            });
        } else {
            work.run();
        }
    }

    private void planSafely(PlanningLease lease) {
        try {
            // 第一个短事务加锁确认租约仍有效；外部模型调用必须在事务之外执行，避免长期占用连接和行锁。
            AiAgentTask task = transactionTemplate.execute(
                    ignored -> requireCurrentPlanningLease(lease));
            if (task == null) {
                return;
            }
            List<AiAgentCapability> capabilities = allowedCapabilities(
                    lease.snapshot(), lease.user().permissions());
            AiAgentPageContext context = new AiAgentPageContext(
                    task.getGoalSummary(),
                    lease.snapshot().getRouteName(),
                    lease.snapshot().getPageVersion(),
                    sanitizer.sanitize(lease.snapshot().getState()),
                    capabilities,
                    lease.previousResult());
            AiAgentDecision decision = gateway.decide(context);
            // 模型返回后在新的短事务中再次校验租约，只有仍拥有租约的响应才能提交状态变化。
            PlanningOutcome outcome = transactionTemplate.execute(
                    ignored -> commitPlanningDecision(lease, decision));
            publishOutcome(lease.taskId(), outcome);
        } catch (Exception exception) {
            // 失败关闭同样校验租约；旧规划的异常不能覆盖用户暂停、取消或更新后的任务状态。
            PlanningOutcome outcome = transactionTemplate.execute(
                    ignored -> failPlanningLease(lease));
            publishOutcome(lease.taskId(), outcome);
        }
    }

    private AiAgentTask requireCurrentPlanningLease(PlanningLease lease) {
        AiAgentTask task = taskMapper.selectByIdForUpdate(lease.taskId());
        return ownsPlanningLease(task, lease) ? task : null;
    }

    private PlanningOutcome commitPlanningDecision(
            PlanningLease lease, AiAgentDecision decision) {
        AiAgentTask task = taskMapper.selectByIdForUpdate(lease.taskId());
        if (!ownsPlanningLease(task, lease)) {
            return null;
        }
        if (decision.completed()) {
            finishTaskState(task, AiAgentTaskStatus.SUCCEEDED, null);
            return new PlanningOutcome(
                    taskEvent(task, "task", decision.completionSummary()), true);
        }
        // 模型动作需再次经过服务端策略校验；高风险动作只持久化并等待确认，不立即进入客户端执行态。
        AiActionPolicy policy = policyRegistry.require(
                decision.actionType(), decision.target(), lease.snapshot().getRouteName(),
                lease.user().permissions());
        AiAgentAction action = createAction(task, lease.snapshot(), decision, policy);
        AiAgentTaskStatus target = policy.riskLevel() == AiAgentRiskLevel.HIGH
                ? AiAgentTaskStatus.WAITING_CONFIRMATION : AiAgentTaskStatus.EXECUTING;
        stateMachine.requireTransition(AiAgentTaskStatus.PLANNING, target);
        task.setStatus(target.name());
        task.setPlanningToken(null);
        requireUpdated(taskMapper.updateById(task));
        AiAgentEventVO event = policy.riskLevel() == AiAgentRiskLevel.HIGH
                ? confirmationEvent(task, action) : actionEvent(task, action);
        return new PlanningOutcome(event, false);
    }

    private PlanningOutcome failPlanningLease(PlanningLease lease) {
        AiAgentTask task = taskMapper.selectByIdForUpdate(lease.taskId());
        if (!ownsPlanningLease(task, lease)) {
            return null;
        }
        finishTaskState(task, AiAgentTaskStatus.FAILED, "PLANNING_FAILED");
        return new PlanningOutcome(taskEvent(task, "task", "代理规划失败"), true);
    }

    private boolean ownsPlanningLease(AiAgentTask task, PlanningLease lease) {
        return task != null
                && Objects.equals(task.getUserId(), lease.user().userId())
                && AiAgentTaskStatus.PLANNING.name().equals(task.getStatus())
                && Objects.equals(task.getPlanningToken(), lease.token());
    }

    private void publishOutcome(Long taskId, PlanningOutcome outcome) {
        if (outcome == null || outcome.event() == null) {
            return;
        }
        if (outcome.terminal()) {
            eventHub.complete(taskId, outcome.event());
        } else {
            eventHub.emit(taskId, outcome.event());
        }
    }

    private void pauseDisconnectedClient(
            AiAgentUserContext user, Long taskId, String clientInstanceId) {
        // 连接终止与用户操作可能并发，必须锁定任务并重新核对用户、标签页和当前状态后再暂停。
        AiAgentEventVO event = transactionTemplate.execute(ignored -> {
            AiAgentTask task = taskMapper.selectByIdForUpdate(taskId);
            if (task == null
                    || !Objects.equals(task.getUserId(), user.userId())
                    || !Objects.equals(task.getClientInstanceId(), clientInstanceId)
                    || !DISCONNECT_PAUSABLE_STATES.contains(task.getStatus())) {
                return null;
            }
            transition(task, AiAgentTaskStatus.PAUSED);
            return taskEvent(task, "paused", "页面连接已断开，AI 已安全暂停");
        });
        if (event != null) {
            eventHub.emit(taskId, event);
        }
    }

    private List<AiAgentCapability> allowedCapabilities(
            AiAgentPageSnapshotDTO snapshot, Set<String> permissions) {
        List<AiAgentCapability> allowed = new ArrayList<>();
        for (AiAgentCapabilityDTO capability : snapshot.getCapabilities()) {
            try {
                policyRegistry.require(
                        capability.getActionType(), capability.getTarget(), snapshot.getRouteName(), permissions);
                allowed.add(new AiAgentCapability(
                        capability.getActionType(),
                        capability.getTarget(),
                        sanitizer.sanitize(capability.getParameterSchema())));
            } catch (BusinessException ignored) {
                // 客户端能力必须与服务端白名单和当前用户权限求交集，未授权项不进入模型上下文。
            }
        }
        return List.copyOf(allowed);
    }

    private AiAgentAction createAction(
            AiAgentTask task,
            AiAgentPageSnapshotDTO snapshot,
            AiAgentDecision decision,
            AiActionPolicy policy) {
        AiAgentAction action = new AiAgentAction();
        action.setTaskId(task.getId());
        action.setActionId(UUID.randomUUID().toString());
        action.setSequenceNo(task.getCurrentStep() + 1);
        action.setRouteName(snapshot.getRouteName());
        action.setPageVersion(snapshot.getPageVersion());
        action.setActionType(decision.actionType());
        action.setTarget(decision.target());
        action.setRiskLevel(policy.riskLevel().name());
        // 动作参数在持久化和下发前统一脱敏，数据库中不保留模型返回的原始参数对象。
        Map<String, Object> arguments = sanitizer.sanitize(
                decision.arguments() == null ? Map.of() : decision.arguments());
        action.setArgumentsJson(writeJson(arguments));
        action.setStatus(policy.riskLevel() == AiAgentRiskLevel.HIGH
                ? "WAITING_CONFIRMATION" : "EXECUTING");
        if (policy.riskLevel() == AiAgentRiskLevel.HIGH) {
            // 高风险确认摘要由可信快照和白名单目标生成，不能直接采用模型提供的解释文本。
            Map<String, Object> state = sanitizer.sanitize(
                    snapshot.getState() == null ? Map.of() : snapshot.getState());
            action.setConfirmationSummary(trustedConfirmationSummary(
                    decision.target(), arguments, state));
        } else {
            action.setStartedAt(LocalDateTime.now());
        }
        requireUpdated(actionMapper.insert(action));
        task.setRouteName(snapshot.getRouteName());
        task.setPageVersion(snapshot.getPageVersion());
        task.setCurrentStep(action.getSequenceNo());
        task.setActiveActionId(action.getActionId());
        return action;
    }

    private void transition(AiAgentTask task, AiAgentTaskStatus target) {
        AiAgentTaskStatus source = AiAgentTaskStatus.valueOf(task.getStatus());
        stateMachine.requireTransition(source, target);
        task.setStatus(target.name());
        if (target != AiAgentTaskStatus.PLANNING) {
            task.setPlanningToken(null);
        }
        requireUpdated(taskMapper.updateById(task));
    }

    private void finishTask(
            AiAgentTask task, AiAgentTaskStatus status, String failureCode, String summary) {
        if (!status.terminal()) {
            throw new BusinessException(ResultEnum.AI_AGENT_STATE_INVALID);
        }
        finishTaskState(task, status, failureCode);
        emitAfterCommit(task.getId(), taskEvent(task, "task", summary), true);
    }

    private void finishTaskState(
            AiAgentTask task, AiAgentTaskStatus status, String failureCode) {
        if (!status.terminal()) {
            throw new BusinessException(ResultEnum.AI_AGENT_STATE_INVALID);
        }
        AiAgentTaskStatus source = AiAgentTaskStatus.valueOf(task.getStatus());
        stateMachine.requireTransition(source, status);
        task.setStatus(status.name());
        task.setPlanningToken(null);
        // 释放 activeKey 后同一用户才能创建下一任务；终态同时清除活动动作，阻止迟到结果继续推进。
        task.setActiveKey(null);
        task.setActiveActionId(null);
        task.setFailureCode(failureCode);
        task.setFinishedAt(LocalDateTime.now());
        requireUpdated(taskMapper.updateById(task));
    }

    private AiAgentEventVO taskEvent(AiAgentTask task, String type, String summary) {
        AiAgentEventVO event = new AiAgentEventVO();
        event.setType(type);
        event.setTaskId(task.getId());
        event.setStatus(task.getStatus());
        event.setSummary(sanitizer.sanitizeText(summary));
        event.setFailureCode(task.getFailureCode());
        return event;
    }

    private void emitTaskEventAfterCommit(AiAgentTask task, String type, String summary) {
        emitAfterCommit(task.getId(), taskEvent(task, type, summary), false);
    }

    private void emitAfterCommit(Long taskId, AiAgentEventVO event, boolean terminal) {
        Runnable publish = () -> {
            if (terminal) {
                eventHub.complete(taskId, event);
            } else {
                eventHub.emit(taskId, event);
            }
        };
        // 事件只能在数据库提交后对外可见，避免客户端收到随后因事务回滚而不存在的状态。
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
        } else {
            publish.run();
        }
    }

    private AiAgentEventVO confirmationEvent(AiAgentTask task, AiAgentAction action) {
        AiAgentEventVO event = actionEvent(task, action);
        event.setType("confirmation");
        event.setSummary(action.getConfirmationSummary());
        return event;
    }

    /**
     * 确认文案只使用已脱敏页面快照中的目标标签，不采用模型生成的动作说明。
     */
    private String trustedConfirmationSummary(
            String target, Map<String, Object> arguments, Map<String, Object> state) {
        Object elementsValue = state.get("elements");
        if (elementsValue instanceof Iterable<?> elements) {
            for (Object elementValue : elements) {
                if (!(elementValue instanceof Map<?, ?> element)
                        || !target.equals(String.valueOf(element.get("target")))) {
                    continue;
                }
                Object labelValue = element.get("label");
                if (labelValue != null && !String.valueOf(labelValue).isBlank()) {
                    String label = sanitizer.sanitizeText(String.valueOf(labelValue));
                    Object contextValue = element.get("context");
                    String context = contextValue == null
                            ? ""
                            : sanitizer.sanitizeText(String.valueOf(contextValue));
                    String object = context.isBlank() ? label : context;
                    return sanitizer.sanitizeText(
                            "对象：" + object + "；动作：点击“" + label + "”；影响：页面将提交该操作");
                }
            }
        }
        throw new BusinessException(ResultEnum.AI_AGENT_ACTION_INVALID);
    }

    private AiAgentEventVO actionEvent(AiAgentTask task, AiAgentAction action) {
        AiAgentEventVO event = new AiAgentEventVO();
        event.setType("action");
        event.setTaskId(task.getId());
        event.setStatus(task.getStatus());
        event.setAction(toActionVO(action));
        return event;
    }

    private AiAgentTask requireTask(AiAgentUserContext user, Long taskId) {
        AiAgentTask task = taskMapper.selectById(taskId);
        if (task == null || !Objects.equals(task.getUserId(), user.userId())) {
            throw new BusinessException(ResultEnum.AI_AGENT_TASK_NOT_FOUND);
        }
        return task;
    }

    private AiAgentTask requireTaskForUpdate(AiAgentUserContext user, Long taskId) {
        AiAgentTask task = taskMapper.selectByIdForUpdate(taskId);
        if (task == null || !Objects.equals(task.getUserId(), user.userId())) {
            throw new BusinessException(ResultEnum.AI_AGENT_TASK_NOT_FOUND);
        }
        return task;
    }

    private AiAgentAction requireActionForUpdate(Long taskId, String actionId) {
        AiAgentAction action = actionMapper.selectByTaskAndActionIdForUpdate(taskId, actionId);
        if (action == null) {
            throw new BusinessException(ResultEnum.AI_AGENT_ACTION_INVALID);
        }
        return action;
    }

    private void requireClient(AiAgentTask task, String clientInstanceId) {
        if (!Objects.equals(task.getClientInstanceId(), clientInstanceId)) {
            throw new BusinessException(ResultEnum.AI_AGENT_CLIENT_MISMATCH);
        }
    }

    private boolean executionContextMatches(AiAgentAction action, AiAgentActionResultDTO dto) {
        return Objects.equals(action.getRouteName(), dto.getExecutionRouteName())
                && Objects.equals(action.getPageVersion(), dto.getExecutionPageVersion());
    }

    /**
     * 页面上下文变化意味着客户端拒绝执行旧动作，需要持久化为可恢复的暂停态，
     * 避免动作长期停留在 EXECUTING 且服务端继续基于过期页面规划。
     */
    private void pauseForPageContextChange(AiAgentTask task, AiAgentAction action) {
        String summary = "AI 动作与当前页面路由或版本不一致";
        action.setStatus("FAILED");
        action.setResultSummary(summary);
        action.setFinishedAt(LocalDateTime.now());
        requireUpdated(actionMapper.updateById(action));
        task.setFailureCode("PAGE_CONTEXT_CHANGED");
        if (AiAgentTaskStatus.EXECUTING.name().equals(task.getStatus())) {
            transition(task, AiAgentTaskStatus.PAUSED);
            emitTaskEventAfterCommit(task, "paused", summary);
        }
    }

    private void requireEnabled() {
        if (!configService.isAiAgentEnabled()) {
            throw new BusinessException(ResultEnum.AI_AGENT_DISABLED);
        }
    }

    private String activeKey(Long userId) {
        return "USER:" + userId;
    }

    private AiAgentTaskVO toTaskVO(AiAgentTask task) {
        AiAgentTaskVO vo = new AiAgentTaskVO();
        vo.setId(task.getId());
        vo.setGoalSummary(task.getGoalSummary());
        vo.setStatus(task.getStatus());
        vo.setRouteName(task.getRouteName());
        vo.setPageVersion(task.getPageVersion());
        vo.setCurrentStep(task.getCurrentStep());
        vo.setActiveActionId(task.getActiveActionId());
        vo.setFailureCode(task.getFailureCode());
        vo.setStartedAt(task.getStartedAt());
        vo.setFinishedAt(task.getFinishedAt());
        // 仅按 activeActionId 读取当前动作，避免把历史动作误当作仍需确认或执行的指令返回。
        if (task.getActiveActionId() != null) {
            AiAgentAction activeAction = actionMapper.selectOne(new LambdaQueryWrapper<AiAgentAction>()
                    .eq(AiAgentAction::getTaskId, task.getId())
                    .eq(AiAgentAction::getActionId, task.getActiveActionId())
                    .last("LIMIT 1"));
            if (activeAction != null) {
                vo.setActiveAction(toActionVO(activeAction));
            }
        }
        return vo;
    }

    private AiAgentActionVO toActionVO(AiAgentAction action) {
        AiAgentActionVO vo = new AiAgentActionVO();
        vo.setActionId(action.getActionId());
        vo.setSequenceNo(action.getSequenceNo());
        vo.setRouteName(action.getRouteName());
        vo.setPageVersion(action.getPageVersion());
        vo.setActionType(action.getActionType());
        vo.setTarget(action.getTarget());
        vo.setRiskLevel(action.getRiskLevel());
        vo.setArguments(readArguments(action.getArgumentsJson()));
        vo.setStatus(action.getStatus());
        vo.setResultSummary(action.getResultSummary());
        vo.setConfirmationSummary(action.getConfirmationSummary());
        return vo;
    }

    private String writeJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new BusinessException(ResultEnum.SERIALIZE_ERROR);
        }
    }

    private Map<String, Object> readArguments(String value) {
        if (value == null) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JacksonException exception) {
            throw new BusinessException(ResultEnum.SERIALIZE_ERROR);
        }
    }

    private void requireUpdated(int affectedRows) {
        if (affectedRows != 1) {
            throw new BusinessException(ResultEnum.AI_AGENT_STATE_INVALID);
        }
    }
}
