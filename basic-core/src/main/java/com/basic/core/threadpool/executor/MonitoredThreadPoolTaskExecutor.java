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

    /**
     * 构造一个监控型平台线程池执行器。
     *
     * @param name             线程池名称，用于标识和监控
     * @param properties       CPU 线程池配置属性，含核心/最大线程数、队列容量、存活时间等
     * @param awaitTermination 优雅关闭时等待任务完成的超时时间
     * @param taskDecorator    任务装饰器，用于在任务执行前后添加自定义逻辑
     */
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

    /**
     * 提交任务到线程池执行，自动记录提交指标并包装任务上下文。
     *
     * @param task 待执行的任务，不允许为 {@code null}
     */
    @Override
    public void execute(Runnable task) {
        metrics.recordSubmitted();
        delegate.execute(metrics.wrap(ExecutorIdentityContext.wrap(name, task)));
    }

    /**
     * 提交一个 Runnable 任务并返回 Future，自动记录提交指标并包装任务上下文。
     *
     * @param task 待提交的任务，不允许为 {@code null}
     * @return 表示任务异步执行结果的 Future
     */
    @Override
    public Future<?> submit(Runnable task) {
        metrics.recordSubmitted();
        return delegate.submit(metrics.wrap(ExecutorIdentityContext.wrap(name, task)));
    }

    /**
     * 提交一个 Callable 任务并返回 Future，自动记录提交指标并包装任务上下文。
     *
     * @param task 待提交的带返回值任务，不允许为 {@code null}
     * @param <T>  任务返回值的类型
     * @return 表示任务异步执行结果的 Future
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        metrics.recordSubmitted();
        return delegate.submit(metrics.wrap(ExecutorIdentityContext.wrap(name, task)));
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 采集当前线程池的快照数据，包含线程池大小、队列状态、任务统计等指标。
     *
     * @return 线程池快照，包含名称、类型、状态、线程数、队列容量及任务计数等完整信息
     */
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

    /**
     * 判断指定线程是否属于当前线程池管理。
     *
     * @param threadName 线程名称，可能为 {@code null}
     * @return 若线程名以当前线程池的线程名前缀开头则返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public boolean ownsThread(String threadName) {
        return threadName != null && threadName.startsWith(threadNamePrefix);
    }

    /**
     * 记录一次外部提交的失败任务，用于链路追踪中透传失败计数。
     */
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

        /**
         * 构造一个带硬截止时间的线程池任务执行器。
         *
         * @param awaitTermination 优雅关闭时等待任务完成的超时时间
         */
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
