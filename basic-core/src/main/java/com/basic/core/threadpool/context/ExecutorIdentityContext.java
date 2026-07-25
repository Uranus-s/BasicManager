package com.basic.core.threadpool.context;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 保存当前任务所属的受管执行器名称，为 CallerRuns 和嵌套执行提供稳定归属。
 */
public final class ExecutorIdentityContext {

    private static final ThreadLocal<String> CURRENT_EXECUTOR = new ThreadLocal<>();

    private ExecutorIdentityContext() {
    }

    /**
     * @return 当前任务所属执行器名称；不在受管任务中时返回 {@code null}
     */
    public static String currentExecutorName() {
        return CURRENT_EXECUTOR.get();
    }

    /**
     * 包装 Runnable，并在执行结束后恢复调用线程原有身份。
     *
     * @param executorName 执行器 Bean 名称
     * @param task 原始任务
     * @return 带执行器身份边界的任务
     */
    public static Runnable wrap(String executorName, Runnable task) {
        Objects.requireNonNull(executorName, "executorName 不能为空");
        Objects.requireNonNull(task, "task 不能为空");
        return () -> {
            String previousExecutor = install(executorName);
            try {
                task.run();
            } finally {
                restore(previousExecutor);
            }
        };
    }

    /**
     * 包装 Callable，并在正常返回或异常退出后恢复调用线程原有身份。
     *
     * @param executorName 执行器 Bean 名称
     * @param task 原始任务
     * @param <T> 任务返回类型
     * @return 带执行器身份边界的任务
     */
    public static <T> Callable<T> wrap(String executorName, Callable<T> task) {
        Objects.requireNonNull(executorName, "executorName 不能为空");
        Objects.requireNonNull(task, "task 不能为空");
        return () -> {
            String previousExecutor = install(executorName);
            try {
                return task.call();
            } finally {
                restore(previousExecutor);
            }
        };
    }

    private static String install(String executorName) {
        String previousExecutor = CURRENT_EXECUTOR.get();
        CURRENT_EXECUTOR.set(executorName);
        return previousExecutor;
    }

    private static void restore(String previousExecutor) {
        if (previousExecutor == null) {
            CURRENT_EXECUTOR.remove();
        } else {
            CURRENT_EXECUTOR.set(previousExecutor);
        }
    }
}
