package com.basic.core.threadpool.model;

import lombok.Builder;
import lombok.Getter;

/**
 * 线程池运行指标的不可变快照，供不同类型执行器统一对外暴露状态。
 */
@Getter
@Builder
public class ThreadPoolSnapshot {

    private final String name;
    private final ThreadPoolType type;
    private final ThreadPoolStatus status;
    private final String threadNamePrefix;
    private final Integer corePoolSize;
    private final Integer maximumPoolSize;
    private final Integer poolSize;
    private final Integer largestPoolSize;
    private final Integer activeCount;
    private final Integer queueSize;
    private final Integer queueCapacity;
    private final Integer queueRemainingCapacity;
    private final Integer concurrencyLimit;
    private final long submittedTaskCount;
    private final long completedTaskCount;
    private final long failedTaskCount;
    private final long rejectedTaskCount;
}
