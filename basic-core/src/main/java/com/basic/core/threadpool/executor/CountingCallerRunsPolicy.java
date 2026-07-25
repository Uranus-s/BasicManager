package com.basic.core.threadpool.executor;

import com.basic.core.threadpool.monitor.TaskExecutionMetrics;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 在线程池和队列均饱和时由提交线程执行任务，并记录过载次数。
 */
public final class CountingCallerRunsPolicy implements RejectedExecutionHandler {

    private final TaskExecutionMetrics metrics;

    public CountingCallerRunsPolicy(TaskExecutionMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        metrics.recordRejected();
        if (executor.isShutdown()) {
            throw new RejectedExecutionException("线程池已关闭，拒绝新任务");
        }
        runnable.run();
    }
}
