package com.basic.core.threadpool.config;

import com.basic.core.threadpool.constant.ThreadPoolNames;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;

/**
 * 将 Spring 无名称异步任务和定时任务分别接入 CPU 执行器与独立调度器。
 */
@Configuration(proxyBeanMethods = false)
@EnableAsync
@EnableScheduling
public class ThreadPoolAsyncConfiguration implements AsyncConfigurer, SchedulingConfigurer {

    private final Executor cpuExecutor;
    private final TaskScheduler taskScheduler;
    private final AsyncUncaughtExceptionHandler exceptionHandler;

    public ThreadPoolAsyncConfiguration(
            @Lazy @Qualifier(ThreadPoolNames.CPU) Executor cpuExecutor,
            @Lazy @Qualifier(ThreadPoolNames.SCHEDULED) TaskScheduler taskScheduler,
            @Lazy @Qualifier("threadPoolAsyncExceptionHandler")
            AsyncUncaughtExceptionHandler exceptionHandler) {
        this.cpuExecutor = cpuExecutor;
        this.taskScheduler = taskScheduler;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public Executor getAsyncExecutor() {
        return cpuExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * 显式指定独立调度器，避免 {@code @Scheduled} 与 CPU 异步任务竞争线程。
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler);
    }
}
