package com.basic.core.threadpool.monitor;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 记录统一线程池任务从提交到完成期间的运行指标。
 */
public final class TaskExecutionMetrics {

    private final LongAdder submitted = new LongAdder();
    private final LongAdder completed = new LongAdder();
    private final LongAdder failed = new LongAdder();
    private final LongAdder rejected = new LongAdder();
    private final AtomicInteger active = new AtomicInteger();

    /**
     * 记录一次任务提交请求。
     */
    public void recordSubmitted() {
        submitted.increment();
    }

    /**
     * 记录一次线程池过载或关闭造成的拒绝。
     */
    public void recordRejected() {
        rejected.increment();
    }

    /**
     * 记录由执行器外部捕获的任务失败。
     */
    public void recordExternalFailure() {
        failed.increment();
    }

    /**
     * @return 当前正在执行的任务数
     */
    public int activeCount() {
        return active.get();
    }

    /**
     * @return 累计提交的任务数
     */
    public long submittedTaskCount() {
        return submitted.sum();
    }

    /**
     * @return 累计完成的任务数，包含执行失败的任务
     */
    public long completedTaskCount() {
        return completed.sum();
    }

    /**
     * @return 累计失败的任务数
     */
    public long failedTaskCount() {
        return failed.sum();
    }

    /**
     * @return 累计被拒绝的任务数
     */
    public long rejectedTaskCount() {
        return rejected.sum();
    }

    /**
     * 将 Runnable 包装为可追踪任务，确保异常路径也能回收活动计数。
     *
     * @param task 原始任务
     * @return 带指标统计的任务
     */
    public Runnable wrap(Runnable task) {
        return () -> {
            active.incrementAndGet();
            try {
                task.run();
            } catch (RuntimeException | Error exception) {
                failed.increment();
                throw exception;
            } finally {
                completed.increment();
                active.decrementAndGet();
            }
        };
    }

    /**
     * 将 Callable 包装为可追踪任务，确保异常路径也能回收活动计数。
     *
     * @param task 原始任务
     * @param <T> 任务结果类型
     * @return 带指标统计的任务
     */
    public <T> Callable<T> wrap(Callable<T> task) {
        return () -> {
            active.incrementAndGet();
            try {
                return task.call();
            } catch (RuntimeException | Error exception) {
                failed.increment();
                throw exception;
            } catch (Exception exception) {
                failed.increment();
                throw exception;
            } finally {
                completed.increment();
                active.decrementAndGet();
            }
        };
    }
}
