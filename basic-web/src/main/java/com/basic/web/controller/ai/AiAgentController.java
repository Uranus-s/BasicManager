package com.basic.web.controller.ai;

import com.basic.api.controller.ai.AiAgentApi;
import com.basic.api.dto.aiAgent.AiAgentActionResultDTO;
import com.basic.api.dto.aiAgent.AiAgentControlDTO;
import com.basic.api.dto.aiAgent.AiAgentResumeDTO;
import com.basic.api.dto.aiAgent.AiAgentTaskCreateDTO;
import com.basic.api.vo.aiAgent.AiAgentEventVO;
import com.basic.api.vo.aiAgent.AiAgentStatusVO;
import com.basic.api.vo.aiAgent.AiAgentTaskVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.Result;
import com.basic.common.result.ResultEnum;
import com.basic.common.web.annotation.IgnoreResponseAdvice;
import com.basic.core.log.annotation.OperateLog;
import com.basic.core.security.model.LoginUser;
import com.basic.sericve.ai.agent.model.AiAgentUserContext;
import com.basic.sericve.ai.agent.service.IAiAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Set;

/**
 * AI 网页代理 Web 入口，用户身份和权限只从 Spring Security 上下文读取。
 * Controller 仅完成参数接收、登录态转换和统一响应包装，任务归属、状态和风险规则由 Service 校验。
 */
@RestController
@RequestMapping("/ai/agent")
@RequiredArgsConstructor
public class AiAgentController implements AiAgentApi {

    private final IAiAgentService aiAgentService;

    @Override
    @GetMapping("/status")
    public Result<AiAgentStatusVO> getStatus(@RequestParam String clientInstanceId) {
        return Result.success(aiAgentService.getStatus(currentUser(), clientInstanceId));
    }

    @Override
    @PostMapping("/tasks")
    public Result<AiAgentTaskVO> createTask(@Valid @RequestBody AiAgentTaskCreateDTO dto) {
        return Result.success(aiAgentService.createTask(currentUser(), dto));
    }

    @Override
    @GetMapping("/tasks/{taskId}")
    public Result<AiAgentTaskVO> getTask(@PathVariable Long taskId) {
        return Result.success(aiAgentService.getTask(currentUser(), taskId));
    }

    @Override
    @GetMapping(value = "/tasks/{taskId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @IgnoreResponseAdvice
    public Flux<ServerSentEvent<AiAgentEventVO>> events(
            @PathVariable Long taskId, @RequestParam String clientInstanceId) {
        // SSE 必须跳过普通 Result 包装；用户上下文在建立流时读取，后续事件不再依赖请求线程的 SecurityContext。
        return aiAgentService.events(currentUser(), taskId, clientInstanceId);
    }

    @Override
    @PostMapping("/tasks/{taskId}/actions/{actionId}/result")
    public Result<?> reportResult(
            @PathVariable Long taskId,
            @PathVariable String actionId,
            @Valid @RequestBody AiAgentActionResultDTO dto) {
        aiAgentService.reportResult(currentUser(), taskId, actionId, dto);
        return Result.success();
    }

    @Override
    @PostMapping("/tasks/{taskId}/pause")
    @OperateLog(module = "AI网页代理", method = "暂停代理任务")
    public Result<?> pause(
            @PathVariable Long taskId, @Valid @RequestBody AiAgentControlDTO dto) {
        aiAgentService.pause(currentUser(), taskId, dto.getClientInstanceId());
        return Result.success();
    }

    @Override
    @PostMapping("/tasks/{taskId}/resume")
    public Result<?> resume(
            @PathVariable Long taskId, @Valid @RequestBody AiAgentResumeDTO dto) {
        aiAgentService.resume(
                currentUser(), taskId, dto.getClientInstanceId(), dto.getSnapshot());
        return Result.success();
    }

    @Override
    @PostMapping("/tasks/{taskId}/cancel")
    @OperateLog(module = "AI网页代理", method = "终止代理任务")
    public Result<?> cancel(
            @PathVariable Long taskId, @Valid @RequestBody AiAgentControlDTO dto) {
        aiAgentService.cancel(currentUser(), taskId, dto.getClientInstanceId());
        return Result.success();
    }

    @Override
    @PostMapping("/tasks/{taskId}/claim")
    @OperateLog(module = "AI网页代理", method = "接管已暂停代理任务")
    public Result<AiAgentTaskVO> claim(
            @PathVariable Long taskId, @Valid @RequestBody AiAgentControlDTO dto) {
        return Result.success(aiAgentService.claim(
                currentUser(), taskId, dto.getClientInstanceId()));
    }

    @Override
    @PostMapping("/tasks/{taskId}/actions/{actionId}/confirm")
    @OperateLog(module = "AI网页代理", method = "确认高风险动作")
    public Result<?> confirm(
            @PathVariable Long taskId,
            @PathVariable String actionId,
            @Valid @RequestBody AiAgentControlDTO dto) {
        aiAgentService.confirm(currentUser(), taskId, actionId, dto.getClientInstanceId());
        return Result.success();
    }

    private AiAgentUserContext currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoginUser loginUser)
                || loginUser.getUserId() == null) {
            throw new BusinessException(ResultEnum.UNAUTHORIZED);
        }
        // 复制为不可变集合，避免异步规划期间底层认证对象变化导致权限判断前后不一致。
        Set<String> permissions = loginUser.getPermissions() == null
                ? Set.of() : Set.copyOf(loginUser.getPermissions());
        return new AiAgentUserContext(loginUser.getUserId(), permissions);
    }
}
