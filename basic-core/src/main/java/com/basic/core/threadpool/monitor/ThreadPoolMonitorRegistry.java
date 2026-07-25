package com.basic.core.threadpool.monitor;

import com.basic.core.threadpool.model.ThreadPoolSnapshot;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 聚合所有受监控执行器，并按线程归属将外部异常路由至对应指标。
 */
public final class ThreadPoolMonitorRegistry {

    private final List<ManagedExecutorMonitor> monitors;

    public ThreadPoolMonitorRegistry(List<ManagedExecutorMonitor> monitors) {
        this.monitors = List.copyOf(monitors);
    }

    /**
     * 按执行器名称稳定排序后返回各线程池的实时快照。
     *
     * @return 有序线程池快照
     */
    public List<ThreadPoolSnapshot> snapshots() {
        return monitors.stream()
                .sorted(Comparator.comparing(ManagedExecutorMonitor::getName))
                .map(ManagedExecutorMonitor::snapshot)
                .toList();
    }

    /**
     * 根据线程名称前缀定位执行器并记录一次未在任务包装器内捕获的失败。
     *
     * @param threadName 发生异常的线程名称
     * @return 找到所属执行器时为 {@code true}
     */
    public boolean recordFailureForThread(String threadName) {
        return findMonitorForThread(threadName)
                .map(monitor -> {
                    monitor.recordExternalFailure();
                    return true;
                })
                .orElse(false);
    }

    /**
     * 根据明确的执行器名称记录失败，避免 CallerRuns 被物理线程名称误导。
     *
     * @param executorName 受管执行器 Bean 名称
     * @return 找到对应执行器时为 {@code true}
     */
    public boolean recordFailureForExecutor(String executorName) {
        return monitors.stream()
                .filter(monitor -> monitor.getName().equals(executorName))
                .findFirst()
                .map(monitor -> {
                    monitor.recordExternalFailure();
                    return true;
                })
                .orElse(false);
    }

    /**
     * @param threadName 发生异常的线程名称
     * @return 线程名前缀对应的执行器名称
     */
    public Optional<String> findExecutorNameForThread(String threadName) {
        return findMonitorForThread(threadName).map(ManagedExecutorMonitor::getName);
    }

    private Optional<ManagedExecutorMonitor> findMonitorForThread(String threadName) {
        return monitors.stream()
                .filter(monitor -> monitor.ownsThread(threadName))
                .findFirst();
    }
}
