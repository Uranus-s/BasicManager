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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 以受限虚拟线程执行 I/O 密集任务，并统一暴露任务指标和执行器生命周期。
 */
public final class MonitoredVirtualTaskExecutor
        implements AsyncTaskExecutor, DisposableBean, ManagedExecutorMonitor {

    private static final String VIRTUAL_THREAD_NAME_PREFIX = "basic-virtual-";

    private final String name;
    private final int concurrencyLimit;
    private final SimpleAsyncTaskExecutor delegate;
    private final TaskDecorator taskDecorator;
    private final TaskExecutionMetrics metrics = new TaskExecutionMetrics();
    private final ReentrantLock admissionLock = new ReentrantLock();
    private final Condition admissionAvailable = admissionLock.newCondition();
    private volatile ThreadPoolStatus status = ThreadPoolStatus.RUNNING;
    private int acceptedTaskCount;
    private boolean delegateCloseFinished;

    public MonitoredVirtualTaskExecutor(String name, int concurrencyLimit,
                                        Duration awaitTermination, TaskDecorator taskDecorator) {
        this.name = name;
        this.concurrencyLimit = concurrencyLimit;
        this.taskDecorator = taskDecorator;
        this.delegate = new SimpleAsyncTaskExecutor(VIRTUAL_THREAD_NAME_PREFIX);
        delegate.setVirtualThreads(true);
        delegate.setTaskTerminationTimeout(awaitTermination.toMillis());
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

    @Override
    public void execute(Runnable task) {
        metrics.recordSubmitted();
        executeAccepted(taskDecorator.decorate(
                metrics.wrap(ExecutorIdentityContext.wrap(name, task))));
    }

    @Override
    public Future<?> submit(Runnable task) {
        metrics.recordSubmitted();
        FutureTask<Void> future = new FutureTask<>(
                metrics.wrap(ExecutorIdentityContext.wrap(name, task)), null);
        executeAccepted(taskDecorator.decorate(future));
        return future;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        metrics.recordSubmitted();
        FutureTask<T> future = new FutureTask<>(
                metrics.wrap(ExecutorIdentityContext.wrap(name, task)));
        executeAccepted(taskDecorator.decorate(future));
        return future;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ThreadPoolSnapshot snapshot() {
        return ThreadPoolSnapshot.builder()
                .name(name)
                .type(ThreadPoolType.VIRTUAL)
                .status(status)
                .threadNamePrefix(VIRTUAL_THREAD_NAME_PREFIX)
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

    @Override
    public boolean ownsThread(String threadName) {
        return threadName != null && threadName.startsWith(VIRTUAL_THREAD_NAME_PREFIX);
    }

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
