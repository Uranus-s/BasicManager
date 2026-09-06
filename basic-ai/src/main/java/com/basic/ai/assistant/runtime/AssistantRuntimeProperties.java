package com.basic.ai.assistant.runtime;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * AI 助手单次运行的安全边界配置。
 *
 * @param maxToolCalls 单次运行允许执行的工具调用总数
 * @param timeout 整个模型流的最长存活时间，纯聊天同样受此限制
 * @param approvalTtl 写操作快照等待用户确认的有效期
 */
@ConfigurationProperties(prefix = "basic.ai.agent")
public record AssistantRuntimeProperties(
        int maxToolCalls,
        Duration timeout,
        Duration approvalTtl) {

    private static final int DEFAULT_MAX_TOOL_CALLS = 16;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(90);
    private static final Duration DEFAULT_APPROVAL_TTL = Duration.ofMinutes(15);

    public AssistantRuntimeProperties {
        // 配置缺失或非正数时回退到保守默认值，避免关闭运行时边界。
        maxToolCalls = maxToolCalls > 0 ? maxToolCalls : DEFAULT_MAX_TOOL_CALLS;
        timeout = isPositive(timeout) ? timeout : DEFAULT_TIMEOUT;
        approvalTtl = isPositive(approvalTtl) ? approvalTtl : DEFAULT_APPROVAL_TTL;
    }

    private static boolean isPositive(Duration duration) {
        return duration != null && !duration.isZero() && !duration.isNegative();
    }
}
