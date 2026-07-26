package com.basic.core.threadpool.support;

import com.basic.core.threadpool.context.TaskNamingContext;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 无需 {@code @Async} 即可向统一 CPU 或虚拟线程执行器提交具名任务。
 */
public final class ThreadPoolTaskRunner {

    /** CPU 密集型任务执行器，依赖平台线程池。 */
    private final AsyncTaskExecutor cpuExecutor;
    /** 虚拟线程执行器，适用于高并发 I/O 密集型任务。 */
    private final AsyncTaskExecutor virtualExecutor;

    /**
     * 构造一个 {@link ThreadPoolTaskRunner} 实例。
     *
     * @param cpuExecutor     CPU 密集型任务的异步执行器（不可为 {@code null}）
     * @param virtualExecutor  虚拟线程异步执行器（不可为 {@code null}）
     */
    public ThreadPoolTaskRunner(
            AsyncTaskExecutor cpuExecutor,
            AsyncTaskExecutor virtualExecutor) {
        this.cpuExecutor = Objects.requireNonNull(cpuExecutor);
        this.virtualExecutor = Objects.requireNonNull(virtualExecutor);
    }

    /**
     * 以"发射后不管"模式向 CPU 执行器提交具名任务。
     *
     * @param taskName 任务名称，用于日志与诊断上下文
     * @param task     待执行的 {@link Runnable} 任务
     */
    public void executeCpu(String taskName, Runnable task) {
        cpuExecutor.execute(TaskNamingContext.wrap(taskName, task));
    }

    /**
     * 以"发射后不管"模式向虚拟线程执行器提交具名任务。
     *
     * @param taskName 任务名称，用于日志与诊断上下文
     * @param task     待执行的 {@link Runnable} 任务
     */
    public void executeVirtual(String taskName, Runnable task) {
        virtualExecutor.execute(TaskNamingContext.wrap(taskName, task));
    }

    /**
     * 向 CPU 执行器提交具名 {@link Runnable} 任务，并返回 {@link Future}。
     *
     * @param taskName 任务名称，用于日志与诊断上下文
     * @param task     待执行的 {@link Runnable} 任务
     * @return 用于跟踪任务执行结果的 {@link Future}
     */
    public Future<?> submitCpu(String taskName, Runnable task) {
        return cpuExecutor.submit(TaskNamingContext.wrap(taskName, task));
    }

    /**
     * 向虚拟线程执行器提交具名 {@link Runnable} 任务，并返回 {@link Future}。
     *
     * @param taskName 任务名称，用于日志与诊断上下文
     * @param task     待执行的 {@link Runnable} 任务
     * @return 用于跟踪任务执行结果的 {@link Future}
     */
    public Future<?> submitVirtual(String taskName, Runnable task) {
        return virtualExecutor.submit(TaskNamingContext.wrap(taskName, task));
    }

    /**
     * 向 CPU 执行器提交具名 {@link Callable} 任务，并返回带返回值的 {@link Future}。
     *
     * @param <T>      任务返回值类型
     * @param taskName 任务名称，用于日志与诊断上下文
     * @param task     待执行的 {@link Callable} 任务
     * @return 用于跟踪任务执行结果并获取返回值的 {@link Future}
     */
    public <T> Future<T> submitCpu(String taskName, Callable<T> task) {
        return cpuExecutor.submit(TaskNamingContext.wrap(taskName, task));
    }

    /**
     * 向虚拟线程执行器提交具名 {@link Callable} 任务，并返回带返回值的 {@link Future}。
     *
     * @param <T>      任务返回值类型
     * @param taskName 任务名称，用于日志与诊断上下文
     * @param task     待执行的 {@link Callable} 任务
     * @return 用于跟踪任务执行结果并获取返回值的 {@link Future}
     */
    public <T> Future<T> submitVirtual(String taskName, Callable<T> task) {
        return virtualExecutor.submit(TaskNamingContext.wrap(taskName, task));
    }
}
