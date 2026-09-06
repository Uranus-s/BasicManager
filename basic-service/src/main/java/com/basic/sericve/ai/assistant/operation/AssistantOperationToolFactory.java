package com.basic.sericve.ai.assistant.operation;

import com.basic.ai.assistant.runtime.AssistantRunContext;
import com.basic.ai.assistant.runtime.ObservableToolCallback;
import com.basic.common.exception.BusinessException;
import com.basic.sericve.ai.assistant.AssistantRequestContext;
import com.basic.sericve.ai.assistant.action.AssistantActionService;
import com.basic.sericve.ai.assistant.action.model.AssistantProposalResult;
import com.basic.sericve.ai.assistant.action.model.PendingAssistantAction;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 根据当前运行上下文，把稳定的 Operation Spec 转换为 Spring AI 可调用的工具。
 *
 * <p>工厂只注册当前用户具备全部所需权限的操作，以工具白名单限制模型的能力边界。
 * 查询工具可以直接读取数据；写工具只能创建待审批快照，实际业务写入仍需用户确认后
 * 由 {@link AssistantActionService} 执行。</p>
 *
 * <p>所有工具都会使用 {@link ObservableToolCallback} 包装，统一向当前运行发送脱敏的
 * 工具开始、结果和待审批信号。</p>
 */
@Component
@RequiredArgsConstructor
public class AssistantOperationToolFactory {

    private final AssistantOperationRegistry operationRegistry;
    private final AssistantActionService actionService;
    private final Validator validator;

    /**
     * 为一次模型运行创建权限受限的工具列表。
     *
     * @param context 本次运行冻结的用户、权限和触发消息上下文
     * @return 不可变的工具白名单
     */
    public List<ToolCallback> createTools(AssistantRequestContext context) {
        List<ToolCallback> tools = new ArrayList<>();
        for (AssistantOperationSpec<?, ?> operation
                : operationRegistry.permittedOperations(context.permissions())) {
            tools.add(toToolCaptured(operation, context));
        }
        return List.copyOf(tools);
    }

    /**
     * 按 Spec 的实际类型分派转换逻辑，并捕获通配符中的输入、输出泛型。
     *
     * <p>Operation Spec 是密封层次；保留兜底异常可以在层次扩展后未同步工厂时尽早失败。</p>
     */
    private ToolCallback toToolCaptured(AssistantOperationSpec<?, ?> operation,
                                        AssistantRequestContext context) {
        if (operation instanceof AssistantQuerySpec<?, ?> query) {
            return queryToolCaptured(query, context);
        }
        if (operation instanceof AssistantActionSpec<?, ?> action) {
            return actionToolCaptured(action, context);
        }
        throw new IllegalStateException("未知 AI Operation 类型：" + operation.getClass().getName());
    }

    /**
     * 将只读 Spec 转换为模型工具。
     *
     * <p>无输入查询使用无参回调，避免向模型暴露无意义的输入结构；有输入查询先执行
     * Bean Validation，校验失败时抛出参数异常并交由统一工具运行链路处理。</p>
     */
    private <I, O> ToolCallback queryToolCaptured(AssistantQuerySpec<I, O> spec,
                                                  AssistantRequestContext context) {
        ToolCallback callback;
        if (spec.noInput()) {
            callback = FunctionToolCallback.<O>builder(spec.toolName(), () ->
                            spec.contextRequired()
                                    ? spec.executeWithContext(context)
                                    : spec.execute())
                    .description(spec.description())
                    .build();
        }
        else {
            callback = FunctionToolCallback.<I, O>builder(spec.toolName(), input -> {
                        validateInput(input);
                        return spec.contextRequired()
                                ? spec.executeWithContext(input, context)
                                : spec.execute(input);
                    })
                    .description(spec.description())
                    .inputType(spec.inputType())
                    .build();
        }
        return new ObservableToolCallback(callback, spec.description());
    }

    /**
     * 将写操作 Spec 转换为只负责“提议”的模型工具。
     *
     * <p>回调不会调用 Spec 的最终执行函数，而是持久化待审批快照；{@code returnDirect=false}
     * 让提议结果继续返回模型，由运行上下文同时向客户端发送审批事件。</p>
     */
    private <I, P> ToolCallback actionToolCaptured(AssistantActionSpec<I, P> spec,
                                                   AssistantRequestContext context) {
        ToolCallback callback = FunctionToolCallback.<I, AssistantProposalResult>builder(
                        spec.toolName(), (input, toolContext) ->
                                propose(spec, input, context, toolContext))
                .description(spec.description())
                .inputType(spec.inputType())
                .toolMetadata(ToolMetadata.builder().returnDirect(false).build())
                .build();
        return new ObservableToolCallback(callback, spec.description());
    }

    /**
     * 校验写工具输入并创建待审批操作。
     *
     * <p>参数或可预期业务校验失败会转换为结构化失败结果，使模型能够基于结果继续回复；
     * 创建成功后先把审批信息写入运行上下文，包装器会在工具结果信号之后按固定顺序发送。</p>
     */
    private <I, P> AssistantProposalResult propose(AssistantActionSpec<I, P> spec,
                                                    I input,
                                                    AssistantRequestContext context,
                                                    ToolContext toolContext) {
        String validationMessage = validationMessage(input);
        if (validationMessage != null) {
            return AssistantProposalResult.failed(validationMessage);
        }
        try {
            PendingAssistantAction pending = actionService.propose(
                    spec.actionType(), input, context.triggerMessageId(),
                    context.userId(), context.permissions());
            AssistantRunContext.from(toolContext).markWaitingApproval(
                    pending.preview().title(), pending.preview().summary(), pending);
            return AssistantProposalResult.pending(pending.id(), pending.expiresAt());
        }
        catch (BusinessException exception) {
            return AssistantProposalResult.failed(exception.getMessage());
        }
    }

    /** 校验查询工具输入；返回消息不为空时终止本次工具调用。 */
    private void validateInput(Object input) {
        String message = validationMessage(input);
        if (message != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 返回稳定的首条输入校验消息，无错误时返回 {@code null}。
     *
     * <p>约束消息先排序再选择，避免校验器返回集合的迭代顺序影响模型看到的错误内容。</p>
     */
    private String validationMessage(Object input) {
        if (input == null) {
            return "工具输入不能为空";
        }
        Set<ConstraintViolation<Object>> violations = validator.validate(input);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted(Comparator.naturalOrder())
                .findFirst()
                .orElse(null);
    }
}
