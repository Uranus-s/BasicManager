package com.basic.core.threadpool.executor;

import com.basic.core.threadpool.model.ThreadPoolSnapshot;
import com.basic.core.threadpool.model.ThreadPoolStatus;
import com.basic.core.threadpool.model.ThreadPoolType;
import com.basic.core.threadpool.monitor.ManagedExecutorMonitor;
import com.basic.core.threadpool.monitor.TaskExecutionMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Spring 调度器的受监控实现，统一暴露定时任务的原生线程池指标。
 */
public final class MonitoredThreadPoolTaskScheduler extends ThreadPoolTaskScheduler
        implements ManagedExecutorMonitor {

    private static final Logger log = LoggerFactory.getLogger(MonitoredThreadPoolTaskScheduler.class);
    private static final String SCHEDULED_THREAD_NAME_PREFIX = "basic-scheduled-";

    private final String name;
    private final long awaitTerminationMillis;
    private final TaskExecutionMetrics metrics = new TaskExecutionMetrics();

    /**
     * 创建调度执行器，并约束关闭时不再保留周期或延迟任务。
     *
     * @param name 执行器名称
     * @param poolSize 调度线程数
     * @param awaitTermination 关闭时等待任务结束的时长
     */
    public MonitoredThreadPoolTaskScheduler(String name, int poolSize, Duration awaitTermination) {
        this.name = name;
        this.awaitTerminationMillis = awaitTermination.toMillis();
        setPoolSize(poolSize);
        setThreadNamePrefix(SCHEDULED_THREAD_NAME_PREFIX);
        setRemoveOnCancelPolicy(true);
        setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        setRejectedExecutionHandler(new CountingCallerRunsPolicy(metrics));
        setErrorHandler(exception -> {
            metrics.recordExternalFailure();
            log.error("定时任务执行失败", exception);
        });
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ThreadPoolSnapshot snapshot() {
        ScheduledThreadPoolExecutor executor = getScheduledThreadPoolExecutor();
        return ThreadPoolSnapshot.builder()
                .name(name)
                .type(ThreadPoolType.SCHEDULED)
                .status(resolveStatus(executor))
                .threadNamePrefix(SCHEDULED_THREAD_NAME_PREFIX)
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .poolSize(executor.getPoolSize())
                .largestPoolSize(executor.getLargestPoolSize())
                .activeCount(executor.getActiveCount())
                .queueSize(executor.getQueue().size())
                .queueCapacity(null)
                .queueRemainingCapacity(null)
                .concurrencyLimit(null)
                .submittedTaskCount(executor.getTaskCount())
                .completedTaskCount(executor.getCompletedTaskCount())
                .failedTaskCount(metrics.failedTaskCount())
                .rejectedTaskCount(metrics.rejectedTaskCount())
                .build();
    }

    @Override
    public boolean ownsThread(String threadName) {
        return threadName != null && threadName.startsWith(SCHEDULED_THREAD_NAME_PREFIX);
    }

    @Override
    public void recordExternalFailure() {
        metrics.recordExternalFailure();
    }

    /**
     * 上下文关闭事件已提前关闭底层执行器时直接结束生命周期阶段，避免与销毁等待重复计时。
     * 普通 context.stop() 不会关闭执行器，仍委托父类实现以保留后续 start() 恢复语义。
     *
     * @param callback 生命周期停止完成回调
     */
    @Override
    public void stop(Runnable callback) {
        ScheduledThreadPoolExecutor executor;
        try {
            executor = getScheduledThreadPoolExecutor();
        } catch (IllegalStateException exception) {
            // 尚未初始化时沿用 Spring 原有行为，由父类立即完成回调。
            super.stop(callback);
            return;
        }

        if (executor.isShutdown()) {
            callback.run();
            return;
        }
        super.stop(callback);
    }

    /**
     * 调度器关闭超时后强制中断运行任务，确保平台线程不会无限阻塞 JVM 退出。
     */
    @Override
    public void shutdown() {
        ScheduledThreadPoolExecutor executor;
        try {
            executor = getScheduledThreadPoolExecutor();
        } catch (IllegalStateException exception) {
            // 初始化失败时底层调度执行器尚不存在，关闭操作无需继续。
            return;
        }

        executor.shutdown();
        boolean interrupted = false;
        try {
            if (!executor.awaitTermination(
                    awaitTerminationMillis, TimeUnit.MILLISECONDS)) {
                forceShutdown(executor);
            }
        } catch (InterruptedException exception) {
            forceShutdown(executor);
            interrupted = true;
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 取消强制关闭时仍在队列中的 Future，避免调用方永久等待。
     */
    private void forceShutdown(ScheduledThreadPoolExecutor executor) {
        executor.shutdownNow().stream()
                .filter(Future.class::isInstance)
                .map(Future.class::cast)
                .forEach(future -> future.cancel(true));
    }

    /**
     * 优先报告终止状态，确保关闭等待结束后的快照不会长期停留在关闭中。
     */
    private ThreadPoolStatus resolveStatus(ScheduledThreadPoolExecutor executor) {
        if (executor.isTerminated()) {
            return ThreadPoolStatus.TERMINATED;
        }
        if (executor.isShutdown()) {
            return ThreadPoolStatus.SHUTTING_DOWN;
        }
        return ThreadPoolStatus.RUNNING;
    }
}
