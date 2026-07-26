package com.basic.core.threadpool.executor;

import com.basic.core.threadpool.context.ExecutorIdentityContext;
import com.basic.core.threadpool.model.ThreadPoolSnapshot;
import com.basic.core.threadpool.model.ThreadPoolStatus;
import com.basic.core.threadpool.model.ThreadPoolType;
import com.basic.core.threadpool.monitor.ManagedExecutorMonitor;
import com.basic.core.threadpool.monitor.TaskExecutionMetrics;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskRejectedException;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * 以受限虚拟线程执行 I/O 密集任务，并统一暴露任务指标和执行器生命周期。
 */
public final class MonitoredVirtualTaskExecutor
        implements AsyncTaskExecutor, DisposableBean, ManagedExecutorMonitor {

    private static final String DEFAULT_THREAD_NAME_PREFIX = "basic-virtual-";

    private final String name;
    private final int concurrencyLimit;
    private final SimpleAsyncTaskExecutor delegate;
    private final TaskDecorator taskDecorator;
    private final String threadNamePrefix;
    private final Pattern threadNamePattern;
    private final TaskExecutionMetrics metrics = new TaskExecutionMetrics();
    private final ReentrantLock admissionLock = new ReentrantLock();
    private final Condition admissionAvailable = admissionLock.newCondition();
    private volatile ThreadPoolStatus status = ThreadPoolStatus.RUNNING;
    private int acceptedTaskCount;
    private boolean delegateCloseFinished;

    /**
     * 构造一个受限虚拟线程执行器，以虚拟线程执行 I/O 密集任务并通过准入信号量控制并发数。
     *
     * @param name             执行器名称，用于标识和监控
     * @param concurrencyLimit 最大并发虚拟线程数，超出时提交者将被阻塞等待
     * @param awaitTermination 优雅关闭时等待任务完成的超时时间
     * @param taskDecorator    任务装饰器，用于在任务执行前后添加自定义逻辑
     */
    public MonitoredVirtualTaskExecutor(String name, int concurrencyLimit,
                                        Duration awaitTermination, TaskDecorator taskDecorator) {
        this(name, concurrencyLimit, awaitTermination, taskDecorator,
                DEFAULT_THREAD_NAME_PREFIX);
    }

    /**
     * 构造一个具有独立线程名前缀的受限虚拟线程执行器，便于按业务识别线程归属。
     *
     * @param name             执行器名称，用于标识和监控
     * @param concurrencyLimit 最大并发虚拟线程数，超出时提交者将被阻塞等待
     * @param awaitTermination 优雅关闭时等待任务完成的超时时间
     * @param taskDecorator    任务装饰器，用于在任务执行前后添加自定义逻辑
     * @param threadNamePrefix 当前执行器创建的虚拟线程名称前缀，不允许为空白
     */
    public MonitoredVirtualTaskExecutor(
            String name,
            int concurrencyLimit,
            Duration awaitTermination,
            TaskDecorator taskDecorator,
            String threadNamePrefix) {
        this.name = Objects.requireNonNull(name, "name 不能为空");
        this.concurrencyLimit = concurrencyLimit;
        this.taskDecorator = Objects.requireNonNull(taskDecorator, "taskDecorator 不能为空");
        if (threadNamePrefix == null || threadNamePrefix.isBlank()) {
            throw new IllegalArgumentException("线程名前缀不能为空");
        }
        this.threadNamePrefix = threadNamePrefix;
        this.threadNamePattern = Pattern.compile(
                Pattern.quote(threadNamePrefix)
                        + "\\d+(?:\\[[^\\[\\]]*\\])*");
        this.delegate = new SimpleAsyncTaskExecutor(threadNamePrefix);
        delegate.setVirtualThreads(true);
        delegate.setTaskTerminationTimeout(
                Objects.requireNonNull(awaitTermination, "awaitTermination 不能为空").toMillis());
    }

    /**
     * 先在准入锁内切换关闭状态并唤醒提交者，再等待已经接受的任务结束。
     */
    @Override
    public void destroy() {
        admissionLock.lock();
        try {
            if (status != ThreadPoolStatus.RUNNING) {
                return;
            }
            status = ThreadPoolStatus.SHUTTING_DOWN;
            admissionAvailable.signalAll();
        } finally {
            admissionLock.unlock();
        }

        try {
            delegate.close();
        } finally {
            admissionLock.lock();
            try {
                delegateCloseFinished = true;
                if (acceptedTaskCount == 0) {
                    status = ThreadPoolStatus.TERMINATED;
                }
            } finally {
                admissionLock.unlock();
            }
        }
    }

    /**
     * 提交任务到虚拟线程执行，自动记录提交指标、包装上下文并执行并发准入控制。
     *
     * @param task 待执行的任务，不允许为 {@code null}
     */
    @Override
    public void execute(Runnable task) {
        metrics.recordSubmitted();
        executeAccepted(taskDecorator.decorate(
                metrics.wrap(ExecutorIdentityContext.wrap(name, task))));
    }

    /**
     * 提交一个 Runnable 任务并返回 Future，自动记录提交指标、包装上下文并执行并发准入控制。
     *
     * @param task 待提交的任务，不允许为 {@code null}
     * @return 表示任务异步执行结果的 Future
     */
    @Override
    public Future<?> submit(Runnable task) {
        metrics.recordSubmitted();
        FutureTask<Void> future = new FutureTask<>(
                metrics.wrap(ExecutorIdentityContext.wrap(name, task)), null);
        executeAccepted(taskDecorator.decorate(future));
        return future;
    }

    /**
     * 提交一个 Callable 任务并返回 Future，自动记录提交指标、包装上下文并执行并发准入控制。
     *
     * @param task 待提交的带返回值任务，不允许为 {@code null}
     * @param <T>  任务返回值的类型
     * @return 表示任务异步执行结果的 Future
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        metrics.recordSubmitted();
        FutureTask<T> future = new FutureTask<>(
                metrics.wrap(ExecutorIdentityContext.wrap(name, task)));
        executeAccepted(taskDecorator.decorate(future));
        return future;
    }

    /**
     * 以可观测的方式提交 {@link CompletableFuture} 异步任务。
     * Spring {@code @Async} 方法声明为 {@code CompletableFuture} 时会走该入口；
     * 若使用接口默认实现，异常会被 CompletableFuture 内部捕获，导致执行器指标无法记录失败。
     * 此处在完成返回 Future 前记录失败，确保 Future 完成时失败指标已经可见，
     * 并避免异常被 CompletableFuture 消费后再泄漏为虚拟线程未捕获异常。
     *
     * @param task 待提交的任务，不允许为 {@code null}
     * @param <T> 任务结果类型
     * @return 表示任务异步执行结果的 CompletableFuture
     */
    @Override
    public <T> CompletableFuture<T> submitCompletable(Callable<T> task) {
        Objects.requireNonNull(task, "task 不能为空");
        CompletableFuture<T> future = new CompletableFuture<>();
        execute(() -> {
            if (future.isDone()) {
                return;
            }
            try {
                future.complete(task.call());
            } catch (RuntimeException | Error exception) {
                metrics.recordExternalFailure();
                future.completeExceptionally(exception);
            } catch (Exception exception) {
                metrics.recordExternalFailure();
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    /**
     * 以可观测的方式提交无返回值的 {@link CompletableFuture} 异步任务。
     *
     * @param task 待提交的任务，不允许为 {@code null}
     * @return 表示任务异步执行结果的 CompletableFuture
     */
    @Override
    public CompletableFuture<Void> submitCompletable(Runnable task) {
        Objects.requireNonNull(task, "task 不能为空");
        CompletableFuture<Void> future = new CompletableFuture<>();
        execute(() -> {
            if (future.isDone()) {
                return;
            }
            try {
                task.run();
                future.complete(null);
            } catch (RuntimeException | Error exception) {
                metrics.recordExternalFailure();
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 采集当前虚拟线程执行器的快照数据，包含并发限制、活跃任务数及任务统计等指标。
     *
     * @return 虚拟线程执行器快照，包含名称、类型、状态、并发限制及任务计数等完整信息
     */
    @Override
    public ThreadPoolSnapshot snapshot() {
        return ThreadPoolSnapshot.builder()
                .name(name)
                .type(ThreadPoolType.VIRTUAL)
                .status(status)
                .threadNamePrefix(threadNamePrefix)
                .corePoolSize(null)
                .maximumPoolSize(null)
                .poolSize(null)
                .largestPoolSize(null)
                .activeCount(metrics.activeCount())
                .queueSize(null)
                .queueCapacity(null)
                .queueRemainingCapacity(null)
                .concurrencyLimit(concurrencyLimit)
                .submittedTaskCount(metrics.submittedTaskCount())
                .completedTaskCount(metrics.completedTaskCount())
                .failedTaskCount(metrics.failedTaskCount())
                .rejectedTaskCount(metrics.rejectedTaskCount())
                .build();
    }

    /**
     * 判断指定线程是否属于当前虚拟线程执行器管理。
     *
     * @param threadName 线程名称，可能为 {@code null}
     * @return 若线程名符合当前执行器的完整命名格式则返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public boolean ownsThread(String threadName) {
        return threadName != null && threadNamePattern.matcher(threadName).matches();
    }

    /**
     * 记录一次外部提交的虚拟任务执行失败，用于链路追踪中透传失败计数。
     */
    @Override
    public void recordExternalFailure() {
        metrics.recordExternalFailure();
    }

    /**
     * 在同一把锁内等待名额、复检关闭状态并把任务交给 delegate，消除关闭穿透窗口。
     *
     * @param task 已完成指标与上下文装饰的任务
     */
    private void executeAccepted(Runnable task) {
        admissionLock.lock();
        try {
            while (status == ThreadPoolStatus.RUNNING
                    && acceptedTaskCount >= concurrencyLimit) {
                try {
                    admissionAvailable.await();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw reject("等待虚拟线程执行名额时被中断", exception);
                }
            }
            if (status != ThreadPoolStatus.RUNNING) {
                throw reject("虚拟线程执行器正在关闭，拒绝新任务", null);
            }

            acceptedTaskCount++;
            try {
                delegate.execute(releaseAdmissionAfter(task));
            } catch (RejectedExecutionException exception) {
                releaseReservedAdmission();
                metrics.recordRejected();
                throw exception;
            } catch (RuntimeException | Error exception) {
                releaseReservedAdmission();
                throw exception;
            }
        } finally {
            admissionLock.unlock();
        }
    }

    /**
     * 任务完成后归还准入名额；关闭已返回且活动任务归零时才能进入终止态。
     */
    private Runnable releaseAdmissionAfter(Runnable task) {
        return () -> {
            try {
                task.run();
            } finally {
                admissionLock.lock();
                try {
                    releaseReservedAdmission();
                    if (status == ThreadPoolStatus.SHUTTING_DOWN
                            && delegateCloseFinished
                            && acceptedTaskCount == 0) {
                        status = ThreadPoolStatus.TERMINATED;
                    }
                } finally {
                    admissionLock.unlock();
                }
            }
        };
    }

    /**
     * 调用方必须持有准入锁，以保证名额计数和条件通知原子更新。
     */
    private void releaseReservedAdmission() {
        acceptedTaskCount--;
        admissionAvailable.signal();
    }

    /**
     * 统一记录包装层拒绝，并返回 Spring/JDK 均可识别的拒绝异常。
     */
    private TaskRejectedException reject(String message, Throwable cause) {
        metrics.recordRejected();
        return cause == null
                ? new TaskRejectedException(message)
                : new TaskRejectedException(message, cause);
    }
}
