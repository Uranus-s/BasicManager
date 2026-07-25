package com.basic.core.threadpool.executor;

import com.basic.core.threadpool.config.ThreadPoolProperties;
import com.basic.core.threadpool.context.ExecutorIdentityContext;
import com.basic.core.threadpool.model.ThreadPoolSnapshot;
import com.basic.core.threadpool.model.ThreadPoolStatus;
import com.basic.core.threadpool.model.ThreadPoolType;
import com.basic.core.threadpool.monitor.ManagedExecutorMonitor;
import com.basic.core.threadpool.monitor.TaskExecutionMetrics;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 以平台线程执行 CPU 密集任务，并统一提供任务指标和过载背压能力。
 */
public final class MonitoredThreadPoolTaskExecutor
        implements AsyncTaskExecutor, InitializingBean, DisposableBean, ManagedExecutorMonitor {

    private static final String CPU_THREAD_NAME_PREFIX = "basic-cpu-";

    private final String name;
    private final String threadNamePrefix;
    private final int queueCapacity;
    private final HardDeadlineThreadPoolTaskExecutor delegate;
    private final TaskExecutionMetrics metrics = new TaskExecutionMetrics();

    public MonitoredThreadPoolTaskExecutor(String name, ThreadPoolProperties.Cpu properties,
                                           Duration awaitTermination, TaskDecorator taskDecorator) {
        this.name = name;
        this.threadNamePrefix = CPU_THREAD_NAME_PREFIX;
        this.queueCapacity = properties.getQueueCapacity();
        this.delegate = new HardDeadlineThreadPoolTaskExecutor(awaitTermination);
        delegate.setCorePoolSize(properties.getCorePoolSize());
        delegate.setMaxPoolSize(properties.getMaximumPoolSize());
        delegate.setQueueCapacity(queueCapacity);
        delegate.setKeepAliveSeconds(Math.toIntExact(properties.getKeepAlive().toSeconds()));
        delegate.setThreadNamePrefix(threadNamePrefix);
        delegate.setTaskDecorator(taskDecorator);
        delegate.setRejectedExecutionHandler(new CountingCallerRunsPolicy(metrics));
        delegate.setStrictEarlyShutdown(true);
    }

    @Override
    public void afterPropertiesSet() {
        delegate.afterPropertiesSet();
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }

    @Override
    public void execute(Runnable task) {
        metrics.recordSubmitted();
        delegate.execute(metrics.wrap(ExecutorIdentityContext.wrap(name, task)));
    }

    @Override
    public Future<?> submit(Runnable task) {
        metrics.recordSubmitted();
        return delegate.submit(metrics.wrap(ExecutorIdentityContext.wrap(name, task)));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        metrics.recordSubmitted();
        return delegate.submit(metrics.wrap(ExecutorIdentityContext.wrap(name, task)));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ThreadPoolSnapshot snapshot() {
        ThreadPoolExecutor executor = delegate.getThreadPoolExecutor();
        return ThreadPoolSnapshot.builder()
                .name(name)
                .type(ThreadPoolType.PLATFORM)
                .status(resolveStatus(executor))
                .threadNamePrefix(threadNamePrefix)
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .poolSize(executor.getPoolSize())
                .largestPoolSize(executor.getLargestPoolSize())
                .activeCount(metrics.activeCount())
                .queueSize(executor.getQueue().size())
                .queueCapacity(queueCapacity)
                .queueRemainingCapacity(executor.getQueue().remainingCapacity())
                .concurrencyLimit(null)
                .submittedTaskCount(metrics.submittedTaskCount())
                .completedTaskCount(metrics.completedTaskCount())
                .failedTaskCount(metrics.failedTaskCount())
                .rejectedTaskCount(metrics.rejectedTaskCount())
                .build();
    }

    @Override
    public boolean ownsThread(String threadName) {
        return threadName != null && threadName.startsWith(threadNamePrefix);
    }

    @Override
    public void recordExternalFailure() {
        metrics.recordExternalFailure();
    }

    /**
     * 优先返回终止状态，避免终止后的执行器被误报为仍在关闭中。
     */
    private ThreadPoolStatus resolveStatus(ThreadPoolExecutor executor) {
        if (executor.isTerminated()) {
            return ThreadPoolStatus.TERMINATED;
        }
        if (executor.isShutdown()) {
            return ThreadPoolStatus.SHUTTING_DOWN;
        }
        return ThreadPoolStatus.RUNNING;
    }

    /**
     * 在优雅关闭超时后强制中断运行任务，并取消尚未开始的 Future。
     */
    private static final class HardDeadlineThreadPoolTaskExecutor
            extends ThreadPoolTaskExecutor {

        private final long awaitTerminationMillis;

        private HardDeadlineThreadPoolTaskExecutor(Duration awaitTermination) {
            this.awaitTerminationMillis = awaitTermination.toMillis();
        }

        /**
         * 关闭总是先停止接收任务；等待超时或关闭线程被中断时立即升级为强制关闭。
         */
        @Override
        public void shutdown() {
            ThreadPoolExecutor executor;
            try {
                executor = getThreadPoolExecutor();
            } catch (IllegalStateException exception) {
                // 初始化失败时底层执行器尚不存在，关闭操作无需继续。
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
         * 取消队列中尚未开始的原始 Future，避免任务装饰器包装后调用方永久等待。
         */
        private void forceShutdown(ThreadPoolExecutor executor) {
            executor.shutdownNow().forEach(this::cancelRemainingTask);
        }
    }
}
