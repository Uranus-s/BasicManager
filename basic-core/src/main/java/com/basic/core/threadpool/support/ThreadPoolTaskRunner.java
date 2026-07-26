package com.basic.core.threadpool.support;

import com.basic.core.threadpool.context.TaskNamingContext;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.Map;
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
    /** 业务虚拟线程执行器，键为 Spring Bean 名称，创建后不可变。 */
    private final Map<String, ? extends AsyncTaskExecutor> businessVirtualExecutors;

    /**
     * 构造一个 {@link ThreadPoolTaskRunner} 实例。
     *
     * @param cpuExecutor     CPU 密集型任务的异步执行器（不可为 {@code null}）
     * @param virtualExecutor  虚拟线程异步执行器（不可为 {@code null}）
     */
    public ThreadPoolTaskRunner(
            AsyncTaskExecutor cpuExecutor,
            AsyncTaskExecutor virtualExecutor) {
        this(cpuExecutor, virtualExecutor, Map.of());
    }

    /**
     * 构造一个支持业务虚拟线程池路由的 {@link ThreadPoolTaskRunner} 实例。
     *
     * @param cpuExecutor              CPU 密集型任务的异步执行器（不可为 {@code null}）
     * @param virtualExecutor          默认虚拟线程异步执行器（不可为 {@code null}）
     * @param businessVirtualExecutors 以 Bean 名称索引的业务虚拟线程执行器（不可为 {@code null}）
     */
    public ThreadPoolTaskRunner(
            AsyncTaskExecutor cpuExecutor,
            AsyncTaskExecutor virtualExecutor,
            Map<String, ? extends AsyncTaskExecutor> businessVirtualExecutors) {
        this.cpuExecutor = Objects.requireNonNull(cpuExecutor);
        this.virtualExecutor = Objects.requireNonNull(virtualExecutor);
        this.businessVirtualExecutors =
                Map.copyOf(Objects.requireNonNull(businessVirtualExecutors));
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

    /**
     * 以"发射后不管"模式向指定业务的虚拟线程执行器提交具名任务。
     *
     * @param businessName 业务标识，用于定位已配置的业务虚拟线程池
     * @param taskName     任务名称，用于日志与诊断上下文
     * @param task         待执行的 {@link Runnable} 任务
     */
    public void executeVirtual(String businessName, String taskName, Runnable task) {
        resolveBusinessVirtualExecutor(businessName)
                .execute(TaskNamingContext.wrap(taskName, task));
    }

    /**
     * 向指定业务的虚拟线程执行器提交具名 {@link Runnable} 任务，并返回 {@link Future}。
     *
     * @param businessName 业务标识，用于定位已配置的业务虚拟线程池
     * @param taskName     任务名称，用于日志与诊断上下文
     * @param task         待执行的 {@link Runnable} 任务
     * @return 用于跟踪任务执行结果的 {@link Future}
     */
    public Future<?> submitVirtual(String businessName, String taskName, Runnable task) {
        return resolveBusinessVirtualExecutor(businessName)
                .submit(TaskNamingContext.wrap(taskName, task));
    }

    /**
     * 向指定业务的虚拟线程执行器提交具名 {@link Callable} 任务，并返回带返回值的 {@link Future}。
     *
     * @param <T>          任务返回值类型
     * @param businessName 业务标识，用于定位已配置的业务虚拟线程池
     * @param taskName     任务名称，用于日志与诊断上下文
     * @param task         待执行的 {@link Callable} 任务
     * @return 用于跟踪任务执行结果并获取返回值的 {@link Future}
     */
    public <T> Future<T> submitVirtual(
            String businessName, String taskName, Callable<T> task) {
        return resolveBusinessVirtualExecutor(businessName)
                .submit(TaskNamingContext.wrap(taskName, task));
    }

    /**
     * 先校验业务标识并定位执行器，确保未配置或非法业务不会触发任务包装和指标变更。
     *
     * @param businessName 业务标识
     * @return 对应的业务虚拟线程执行器
     */
    private AsyncTaskExecutor resolveBusinessVirtualExecutor(String businessName) {
        String beanName;
        try {
            beanName = BusinessVirtualThreadPoolNames.beanName(businessName);
        } catch (IllegalArgumentException exception) {
            // 保留调用方原始标识，便于定位空白、大小写或 null 等配置与调用错误。
            throw new IllegalArgumentException(
                    "业务虚拟线程池标识不合法: '" + String.valueOf(businessName) + "'",
                    exception);
        }
        AsyncTaskExecutor executor = businessVirtualExecutors.get(beanName);
        if (executor == null) {
            throw new IllegalArgumentException("未配置业务虚拟线程池: " + businessName);
        }
        return executor;
    }
}
