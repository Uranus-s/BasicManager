package com.basic.ai.assistant.runtime;

import com.basic.ai.assistant.model.AssistantSignal;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

import java.util.Objects;

/**
 * 为工具调用补充可观察信号，同时避免把参数、原始结果或异常详情暴露给客户端。
 *
 * <p>包装器只发送预定义展示名、成败摘要和稳定错误码，工具输入与返回值
 * 仍只在 Spring AI 内部流转。</p>
 */
public final class ObservableToolCallback implements ToolCallback {

    private static final int MAX_SUMMARY_LENGTH = 200;
    private final ToolCallback delegate;
    private final String displayName;

    public ObservableToolCallback(ToolCallback delegate, String displayName) {
        this.delegate = Objects.requireNonNull(delegate, "delegate 不能为空");
        this.displayName = Objects.requireNonNull(displayName, "displayName 不能为空");
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String input) {
        // 兼容 ToolCallback 接口；AI 助手路径必须调用带 ToolContext 的重载才能受控。
        return delegate.call(input);
    }

    @Override
    public String call(String input, ToolContext toolContext) {
        AssistantRunContext context = AssistantRunContext.from(toolContext);
        context.emit(AssistantSignal.toolStart(context.getRunId(), displayName,
                summary(displayName + "开始"), null));
        try {
            String result = delegate.call(input, toolContext);
            context.emit(AssistantSignal.toolResult(context.getRunId(), displayName,
                    summary(displayName + "成功"), null));
            context.emitPendingApprovalIfPresent();
            return result;
        }
        catch (RuntimeException exception) {
            context.emit(AssistantSignal.toolResult(context.getRunId(), displayName,
                    summary(displayName + "失败"), stableErrorCode(exception)));
            throw exception;
        }
    }

    private static String summary(String value) {
        return value.length() <= MAX_SUMMARY_LENGTH ? value : value.substring(0, MAX_SUMMARY_LENGTH);
    }

    private static Integer stableErrorCode(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof BusinessException businessException) {
                return businessException.getError().getCode();
            }
            current = current.getCause();
        }
        return ResultEnum.REMOTE_SERVICE_ERROR.getCode();
    }
}
