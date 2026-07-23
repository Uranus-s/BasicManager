package com.basic.sericve.sysMonitor.impl;

import com.basic.api.vo.sysMonitor.DatabaseHealthVO;
import com.basic.api.vo.sysMonitor.DiskInfoVO;
import com.basic.api.vo.sysMonitor.JvmInfoVO;
import com.basic.api.vo.sysMonitor.MonitorVO;
import com.basic.api.vo.sysMonitor.RedisHealthVO;
import com.basic.api.vo.sysMonitor.SystemInfoVO;
import com.basic.sericve.sysMonitor.service.ISysMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务监控服务实现
 *
 * @author Gas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysMonitorServiceImpl implements ISysMonitorService {

    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private static final String DATABASE_CONNECT_FAILED = "数据库连接失败";
    private static final String REDIS_CONNECT_FAILED = "Redis连接失败";

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final Environment environment;

    @Override
    public MonitorVO getStatus() {
        MonitorVO monitor = new MonitorVO();
        monitor.setCollectTime(LocalDateTime.now());
        monitor.setSystem(getSystemInfo());
        monitor.setJvm(getJvmInfo());
        monitor.setDisks(getDiskInfo());
        monitor.setDatabase(getDatabaseHealth());
        monitor.setRedis(getRedisHealth());
        return monitor;
    }

    private SystemInfoVO getSystemInfo() {
        SystemInfoVO info = new SystemInfoVO();
        info.setApplicationName(environment.getProperty("spring.application.name", ""));
        info.setActiveProfiles(environment.getActiveProfiles());
        info.setOsName(System.getProperty("os.name"));
        info.setOsVersion(System.getProperty("os.version"));
        info.setOsArch(System.getProperty("os.arch"));
        info.setAvailableProcessors(Runtime.getRuntime().availableProcessors());
        info.setSystemLoadAverage(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());
        info.setUserDir(System.getProperty("user.dir"));
        return info;
    }

    private JvmInfoVO getJvmInfo() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();

        JvmInfoVO info = new JvmInfoVO();
        info.setJavaVersion(System.getProperty("java.version"));
        info.setJvmName(runtimeBean.getVmName());
        info.setJvmVendor(runtimeBean.getVmVendor());
        info.setStartTime(runtimeBean.getStartTime());
        info.setUptimeMs(runtimeBean.getUptime());
        info.setHeapUsedBytes(heap.getUsed());
        info.setHeapMaxBytes(heap.getMax());
        info.setNonHeapUsedBytes(nonHeap.getUsed());
        info.setThreadCount(threadBean.getThreadCount());
        info.setDaemonThreadCount(threadBean.getDaemonThreadCount());
        return info;
    }

    private List<DiskInfoVO> getDiskInfo() {
        List<DiskInfoVO> disks = new ArrayList<>();
        for (FileStore fileStore : FileSystems.getDefault().getFileStores()) {
            try {
                long totalSpace = fileStore.getTotalSpace();
                long usableSpace = fileStore.getUsableSpace();
                long usedSpace = totalSpace - usableSpace;

                DiskInfoVO disk = new DiskInfoVO();
                disk.setName(fileStore.name());
                disk.setType(fileStore.type());
                disk.setTotalSpaceBytes(totalSpace);
                disk.setUsableSpaceBytes(usableSpace);
                disk.setUsedSpaceBytes(usedSpace);
                disk.setUsedPercent(totalSpace <= 0 ? 0D : usedSpace * 100D / totalSpace);
                disks.add(disk);
            } catch (IOException ignored) {
                // 单个文件存储读取失败不影响整体监控结果。
            }
        }
        return disks;
    }

    private DatabaseHealthVO getDatabaseHealth() {
        DatabaseHealthVO health = new DatabaseHealthVO();
        long start = System.nanoTime();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            DatabaseMetaData metaData = connection.getMetaData();
            health.setStatus(STATUS_UP);
            health.setDatabaseProductName(metaData.getDatabaseProductName());
            health.setDatabaseProductVersion(metaData.getDatabaseProductVersion());
            health.setJdbcUrl(maskJdbcUrl(metaData.getURL()));
        } catch (Exception e) {
            health.setStatus(STATUS_DOWN);
            health.setErrorMessage(DATABASE_CONNECT_FAILED);
            log.warn("数据库健康检查失败", e);
        } finally {
            health.setResponseTimeMs(elapsedMillis(start));
        }
        return health;
    }

    private RedisHealthVO getRedisHealth() {
        RedisHealthVO health = new RedisHealthVO();
        health.setMode(environment.getProperty("spring.data.redis.mode", "single"));
        long start = System.nanoTime();
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            health.setPing(connection.ping());
            health.setStatus(STATUS_UP);
        } catch (Exception e) {
            health.setStatus(STATUS_DOWN);
            health.setErrorMessage(REDIS_CONNECT_FAILED);
            log.warn("Redis健康检查失败", e);
        } finally {
            health.setResponseTimeMs(elapsedMillis(start));
        }
        return health;
    }

    private String maskJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return jdbcUrl;
        }
        int queryIndex = jdbcUrl.indexOf('?');
        return queryIndex >= 0 ? jdbcUrl.substring(0, queryIndex) : jdbcUrl;
    }

    private long elapsedMillis(long startNanoTime) {
        return (System.nanoTime() - startNanoTime) / 1_000_000L;
    }
}
