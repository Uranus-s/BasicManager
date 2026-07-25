package com.basic.sericve.sysThreadPool.impl;

import com.basic.api.vo.sysThreadPool.ThreadPoolMonitorVO;
import com.basic.api.vo.sysThreadPool.ThreadPoolSnapshotVO;
import com.basic.core.threadpool.model.ThreadPoolSnapshot;
import com.basic.core.threadpool.monitor.ThreadPoolMonitorRegistry;
import com.basic.sericve.sysThreadPool.service.ISysThreadPoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 统一线程池监控服务实现，将核心监控模型转换为对外接口模型。
 *
 * @author Gas
 */
@Service
@RequiredArgsConstructor
public class SysThreadPoolServiceImpl implements ISysThreadPoolService {

    private final ThreadPoolMonitorRegistry threadPoolMonitorRegistry;

    /**
     * 每次查询均重新采集快照，避免将线程池运行指标缓存为过期数据。
     *
     * @return 当前线程池实时监控信息
     */
    @Override
    public ThreadPoolMonitorVO getStatus() {
        ThreadPoolMonitorVO monitor = new ThreadPoolMonitorVO();
        monitor.setCollectTime(LocalDateTime.now());
        monitor.setExecutors(threadPoolMonitorRegistry.snapshots().stream()
                .map(this::toSnapshotVO)
                .toList());
        return monitor;
    }

    /**
     * 逐字段复制核心快照，隔离核心模型并将枚举转换为稳定的接口字符串。
     *
     * @param snapshot 核心线程池快照
     * @return 对外线程池快照
     */
    private ThreadPoolSnapshotVO toSnapshotVO(ThreadPoolSnapshot snapshot) {
        ThreadPoolSnapshotVO view = new ThreadPoolSnapshotVO();
        view.setName(snapshot.getName());
        view.setType(snapshot.getType() == null ? null : snapshot.getType().name());
        view.setStatus(snapshot.getStatus() == null ? null : snapshot.getStatus().name());
        view.setThreadNamePrefix(snapshot.getThreadNamePrefix());
        view.setCorePoolSize(snapshot.getCorePoolSize());
        view.setMaximumPoolSize(snapshot.getMaximumPoolSize());
        view.setPoolSize(snapshot.getPoolSize());
        view.setLargestPoolSize(snapshot.getLargestPoolSize());
        view.setActiveCount(snapshot.getActiveCount());
        view.setQueueSize(snapshot.getQueueSize());
        view.setQueueCapacity(snapshot.getQueueCapacity());
        view.setQueueRemainingCapacity(snapshot.getQueueRemainingCapacity());
        view.setConcurrencyLimit(snapshot.getConcurrencyLimit());
        view.setSubmittedTaskCount(snapshot.getSubmittedTaskCount());
        view.setCompletedTaskCount(snapshot.getCompletedTaskCount());
        view.setFailedTaskCount(snapshot.getFailedTaskCount());
        view.setRejectedTaskCount(snapshot.getRejectedTaskCount());
        return view;
    }
}
