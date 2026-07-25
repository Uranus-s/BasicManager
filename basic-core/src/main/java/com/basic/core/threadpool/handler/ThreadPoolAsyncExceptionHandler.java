package com.basic.core.threadpool.handler;

import com.basic.core.threadpool.context.ExecutorIdentityContext;
import com.basic.core.threadpool.monitor.ThreadPoolMonitorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

/**
 * 处理 {@code void} 异步方法无法返回给调用方的异常，并路由至所属线程池指标。
 */
@Slf4j
@RequiredArgsConstructor
public final class ThreadPoolAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final ThreadPoolMonitorRegistry registry;

    /**
     * 记录执行器失败指标和方法身份，不输出方法参数，避免敏感信息进入日志。
     *
     * @param exception 异步任务异常
     * @param method 异步方法
     * @param parameters 方法参数，仅由框架传入，不得写入日志
     */
    @Override
    public void handleUncaughtException(Throwable exception, Method method, Object... parameters) {
        String threadName = Thread.currentThread().getName();
        String executorName = ExecutorIdentityContext.currentExecutorName();
        if (executorName == null) {
            executorName = registry.findExecutorNameForThread(threadName).orElse("unmanaged");
        }
        registry.recordFailureForExecutor(executorName);
        log.error("异步任务执行失败，method={}.{}, executor={}, thread={}",
                method.getDeclaringClass().getName(),
                method.getName(),
                executorName,
                threadName,
                exception);
    }
}
