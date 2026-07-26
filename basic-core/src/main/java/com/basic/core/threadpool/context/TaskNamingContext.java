package com.basic.core.threadpool.context;

import org.slf4j.MDC;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * 在任务执行期间临时设置可读线程名和 MDC 任务名，并在结束后恢复原状态。
 */
public final class TaskNamingContext {

    /** MDC 键名，用于存储当前任务名称。 */
    public static final String MDC_TASK_NAME = "taskName";

    /** 任务名称码点长度上限，超出部分将被截断。 */
    private static final int MAX_TASK_NAME_CODE_POINTS = 80;
    /** 需替换的控制字符、格式字符、行/段分隔符及方括号的正则。 */
    private static final Pattern UNSAFE_CHARACTERS =
            Pattern.compile("[\\p{Cc}\\p{Cf}\\p{Zl}\\p{Zp}\\[\\]]+");

    /** 工具类，禁止实例化。 */
    private TaskNamingContext() {
    }

    /**
     * 打开一个任务命名上下文，设置当前线程名和 MDC 上下文。
     * <p>线程名将追加 {@code [任务名]} 后缀，MDC 中存入标准化后的任务名。
     * 返回的 {@link Scope} 需通过 try-with-resources 关闭以恢复原状态。
     *
     * @param taskName 任务名称（不能为空或空白）
     * @return 用于恢复线程名和 MDC 状态的 {@link Scope}
     */
    public static Scope open(String taskName) {
        String normalizedTaskName = normalize(taskName);
        Thread thread = Thread.currentThread();
        String previousThreadName = thread.getName();
        String previousTaskName = MDC.get(MDC_TASK_NAME);
        thread.setName(previousThreadName + "[" + normalizedTaskName + "]");
        MDC.put(MDC_TASK_NAME, normalizedTaskName);
        return new Scope(thread, previousThreadName, previousTaskName);
    }

    /**
     * 将 {@link Runnable} 包装为带任务命名上下文的执行体。
     * <p>包装后的任务在运行时自动打开并关闭命名上下文，
     * 将任务名反映到当前线程名和 MDC 中。
     *
     * @param taskName 任务名称（不能为空或空白）
     * @param task     待执行的 {@link Runnable} 任务（不能为 {@code null}）
     * @return 带命名上下文的 {@link Runnable} 包装
     */
    public static Runnable wrap(String taskName, Runnable task) {
        Objects.requireNonNull(task, "task 不能为空");
        String normalizedTaskName = normalize(taskName);
        return () -> {
            try (Scope ignored = open(normalizedTaskName)) {
                task.run();
            }
        };
    }

    /**
     * 将 {@link Callable} 包装为带任务命名上下文的执行体。
     * <p>包装后的任务在运行时自动打开并关闭命名上下文，
     * 将任务名反映到当前线程名和 MDC 中。
     *
     * @param <T>      任务返回值类型
     * @param taskName 任务名称（不能为空或空白）
     * @param task     待执行的 {@link Callable} 任务（不能为 {@code null}）
     * @return 带命名上下文的 {@link Callable} 包装
     */
    public static <T> Callable<T> wrap(String taskName, Callable<T> task) {
        Objects.requireNonNull(task, "task 不能为空");
        String normalizedTaskName = normalize(taskName);
        return () -> {
            try (Scope ignored = open(normalizedTaskName)) {
                return task.call();
            }
        };
    }

    /**
     * 标准化任务名称：去除首尾空白、替换非法字符（控制字符、格式字符、
     * 行/段分隔符、方括号为下划线）、截断超出长度限制的部分。
     *
     * @param taskName 原始任务名称
     * @return 标准化后的任务名称
     * @throws IllegalArgumentException 如果 taskName 为空、空白或清理后为空
     */
    private static String normalize(String taskName) {
        if (taskName == null || taskName.isBlank()) {
            throw new IllegalArgumentException("任务名不能为空");
        }
        String normalized = UNSAFE_CHARACTERS.matcher(taskName.strip()).replaceAll("_");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("任务名清理后不能为空");
        }
        int codePointCount = normalized.codePointCount(0, normalized.length());
        if (codePointCount <= MAX_TASK_NAME_CODE_POINTS) {
            return normalized;
        }
        int endIndex = normalized.offsetByCodePoints(0, MAX_TASK_NAME_CODE_POINTS);
        return normalized.substring(0, endIndex);
    }

    /**
     * 保存任务执行前的线程名和 MDC 状态，关闭时恢复原状态。
     */
    public static final class Scope implements AutoCloseable {

        private final Thread thread;
        private final String previousThreadName;
        private final String previousTaskName;
        private boolean closed;

        private Scope(Thread thread, String previousThreadName, String previousTaskName) {
            this.thread = thread;
            this.previousThreadName = previousThreadName;
            this.previousTaskName = previousTaskName;
        }

        /**
         * 恢复线程名和 MDC 任务名到打开前的状态。
         * <p>如果当前线程不是创建该 Scope 的线程则抛出异常；
         * 多次调用仅首次生效，后续调用直接返回。
         *
         * @throws IllegalStateException 如果由非创建线程调用
         */
        @Override
        public void close() {
            if (Thread.currentThread() != thread) {
                throw new IllegalStateException("任务命名上下文只能由创建线程关闭");
            }
            if (closed) {
                return;
            }
            closed = true;
            thread.setName(previousThreadName);
            if (previousTaskName == null) {
                MDC.remove(MDC_TASK_NAME);
            } else {
                MDC.put(MDC_TASK_NAME, previousTaskName);
            }
        }
    }
}
