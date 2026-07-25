package com.basic.core.threadpool.context;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * 在线程池任务间传递 SecurityContext 与 MDC 上下文。
 * <p>
 * 任务执行结束后恢复原值，用于兼容 CallerRunsPolicy 在请求线程直接执行任务的场景，
 * 避免覆盖请求线程已有的安全认证和链路追踪信息。
 * </p>
 */
public class ContextCopyingTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        SecurityContext capturedSecurityContext = SecurityContextHolder.getContext();
        Map<String, String> capturedMdc = MDC.getCopyOfContextMap();
        return () -> {
            SecurityContext previousSecurityContext = SecurityContextHolder.getContext();
            Map<String, String> previousMdc = MDC.getCopyOfContextMap();
            try {
                SecurityContextHolder.setContext(capturedSecurityContext);
                setMdc(capturedMdc);
                runnable.run();
            } finally {
                SecurityContextHolder.setContext(previousSecurityContext);
                setMdc(previousMdc);
            }
        };
    }

    /**
     * 根据上下文副本恢复 MDC；空上下文需要清理当前线程遗留的数据。
     *
     * @param contextMap 待恢复的 MDC 上下文副本
     */
    private void setMdc(Map<String, String> contextMap) {
        if (contextMap == null || contextMap.isEmpty()) {
            MDC.clear();
        } else {
            MDC.setContextMap(contextMap);
        }
    }
}
