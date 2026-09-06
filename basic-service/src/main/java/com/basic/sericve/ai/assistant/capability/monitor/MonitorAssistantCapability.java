package com.basic.sericve.ai.assistant.capability.monitor;

import com.basic.api.vo.sysMonitor.DatabaseHealthVO;
import com.basic.api.vo.sysMonitor.DiskInfoVO;
import com.basic.api.vo.sysMonitor.JvmInfoVO;
import com.basic.api.vo.sysMonitor.MonitorVO;
import com.basic.api.vo.sysMonitor.RedisHealthVO;
import com.basic.api.vo.sysMonitor.SystemInfoVO;
import com.basic.api.vo.sysThreadPool.ThreadPoolMonitorVO;
import com.basic.api.vo.sysThreadPool.ThreadPoolSnapshotVO;
import com.basic.sericve.ai.assistant.capability.AssistantCapability;
import com.basic.sericve.ai.assistant.operation.AssistantOperationSpec;
import com.basic.sericve.sysMonitor.service.ISysMonitorService;
import com.basic.sericve.sysThreadPool.service.ISysThreadPoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.basic.sericve.ai.assistant.operation.AssistantOperations.query;

/**
 * 系统监控只读 AI 能力。
 *
 * <p>监控服务返回的对象包含工作目录、激活配置和 JDBC 地址等基础设施细节，
 * 因此这里显式投影为诊断摘要，避免把完整内部对象交给模型。</p>
 */
@Component
@RequiredArgsConstructor
public class MonitorAssistantCapability implements AssistantCapability {

    private static final String MONITOR_VIEW = "system:monitor:view";

    private final ISysMonitorService monitorService;
    private final ISysThreadPoolService threadPoolService;

    @Override
    public String capabilityId() {
        return "monitor";
    }

    @Override
    public List<AssistantOperationSpec<?, ?>> operations() {
        return List.of(
                query("monitor_status", MonitorStatusResult.class)
                        .description("读取系统运行监控诊断摘要，不包含环境变量、连接串或配置内容")
                        .permissions(MONITOR_VIEW)
                        .execute(this::monitorStatus)
                        .build(),
                query("thread_pool_status", ThreadPoolStatusResult.class)
                        .description("读取受管线程池运行摘要，不包含线程名称前缀等配置细节")
                        .permissions(MONITOR_VIEW)
                        .execute(this::threadPoolStatus)
                        .build()
        );
    }

    private MonitorStatusResult monitorStatus() {
        MonitorVO monitor = monitorService.getStatus();
        if (monitor == null) {
            return new MonitorStatusResult(null, null, null, null, null, null);
        }
        return new MonitorStatusResult(monitor.getCollectTime(), toSystem(monitor.getSystem()),
                toJvm(monitor.getJvm()), toDiskSummary(monitor.getDisks()),
                toDatabase(monitor.getDatabase()), toRedis(monitor.getRedis()));
    }

    private ThreadPoolStatusResult threadPoolStatus() {
        ThreadPoolMonitorVO monitor = threadPoolService.getStatus();
        if (monitor == null) {
            return new ThreadPoolStatusResult(null, List.of());
        }
        List<ThreadPoolSummary> executors = monitor.getExecutors() == null ? List.of()
                : monitor.getExecutors().stream().map(MonitorAssistantCapability::toThreadPool).toList();
        return new ThreadPoolStatusResult(monitor.getCollectTime(), executors);
    }

    private static SystemSummary toSystem(SystemInfoVO value) {
        if (value == null) return null;
        return new SystemSummary(value.getOsName(), value.getOsArch(), value.getAvailableProcessors(),
                value.getSystemLoadAverage());
    }

    private static JvmSummary toJvm(JvmInfoVO value) {
        if (value == null) return null;
        return new JvmSummary(value.getJavaVersion(), value.getJvmName(), value.getUptimeMs(),
                value.getHeapUsedBytes(), value.getHeapMaxBytes(), value.getNonHeapUsedBytes(),
                value.getThreadCount(), value.getDaemonThreadCount());
    }

    private static DiskSummary toDiskSummary(List<DiskInfoVO> values) {
        if (values == null || values.isEmpty()) return new DiskSummary(0, 0L, 0L, 0L, 0D);
        long total = 0L, usable = 0L, used = 0L;
        for (DiskInfoVO value : values) {
            if (value == null) continue;
            total += nullable(value.getTotalSpaceBytes());
            usable += nullable(value.getUsableSpaceBytes());
            used += nullable(value.getUsedSpaceBytes());
        }
        return new DiskSummary(values.size(), total, usable, used, total <= 0 ? 0D : used * 100D / total);
    }

    private static DatabaseSummary toDatabase(DatabaseHealthVO value) {
        if (value == null) return null;
        return new DatabaseSummary(value.getStatus(), value.getResponseTimeMs(), value.getDatabaseProductName(),
                value.getDatabaseProductVersion(), value.getErrorMessage());
    }

    private static RedisSummary toRedis(RedisHealthVO value) {
        if (value == null) return null;
        return new RedisSummary(value.getStatus(), value.getPing(), value.getResponseTimeMs(), value.getErrorMessage());
    }

    private static ThreadPoolSummary toThreadPool(ThreadPoolSnapshotVO value) {
        if (value == null) return null;
        return new ThreadPoolSummary(value.getName(), value.getType(), value.getStatus(), value.getCorePoolSize(),
                value.getMaximumPoolSize(), value.getPoolSize(), value.getLargestPoolSize(), value.getActiveCount(),
                value.getQueueSize(), value.getQueueCapacity(), value.getQueueRemainingCapacity(),
                value.getConcurrencyLimit(), value.getSubmittedTaskCount(), value.getCompletedTaskCount(),
                value.getFailedTaskCount(), value.getRejectedTaskCount());
    }

    private static long nullable(Long value) { return value == null ? 0L : value; }

    public record MonitorStatusResult(LocalDateTime collectTime, SystemSummary system, JvmSummary jvm,
                                      DiskSummary disks, DatabaseSummary database, RedisSummary redis) { }

    public record SystemSummary(String osName, String osArch, Integer availableProcessors, Double systemLoadAverage) { }

    public record JvmSummary(String javaVersion, String jvmName, Long uptimeMs, Long heapUsedBytes,
                              Long heapMaxBytes, Long nonHeapUsedBytes, Integer threadCount,
                              Integer daemonThreadCount) { }

    public record DiskSummary(int diskCount, long totalSpaceBytes, long usableSpaceBytes,
                              long usedSpaceBytes, double usedPercent) { }

    public record DatabaseSummary(String status, Long responseTimeMs, String productName,
                                  String productVersion, String errorMessage) { }

    public record RedisSummary(String status, String ping, Long responseTimeMs, String errorMessage) { }

    public record ThreadPoolStatusResult(LocalDateTime collectTime, List<ThreadPoolSummary> executors) { }

    public record ThreadPoolSummary(String name, String type, String status, Integer corePoolSize,
                                    Integer maximumPoolSize, Integer poolSize, Integer largestPoolSize,
                                    Integer activeCount, Integer queueSize, Integer queueCapacity,
                                    Integer queueRemainingCapacity, Integer concurrencyLimit,
                                    long submittedTaskCount, long completedTaskCount, long failedTaskCount,
                                    long rejectedTaskCount) { }
}
