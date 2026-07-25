package com.basic.api.vo.sysThreadPool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 单个线程池或调度器的实时运行快照。
 *
 * @author Gas
 */
@Data
@Schema(description = "单个受管执行器实时快照")
public class ThreadPoolSnapshotVO {

    @Schema(description = "Spring 容器中的执行器名称", example = "cpuTaskExecutor")
    private String name;

    @Schema(description = "执行器类型枚举名称", example = "PLATFORM")
    private String type;

    @Schema(description = "执行器运行状态枚举名称", example = "RUNNING")
    private String status;

    @Schema(description = "工作线程名称前缀", example = "basic-cpu-")
    private String threadNamePrefix;

    @Schema(description = "核心线程数；不适用时为空", example = "8")
    private Integer corePoolSize;

    @Schema(description = "最大线程数；不适用时为空", example = "16")
    private Integer maximumPoolSize;

    @Schema(description = "当前线程数；不适用时为空", example = "8")
    private Integer poolSize;

    @Schema(description = "历史最大线程数；不适用时为空", example = "10")
    private Integer largestPoolSize;

    @Schema(description = "当前活跃任务数；不适用时为空", example = "3")
    private Integer activeCount;

    @Schema(description = "当前队列任务数；不适用时为空", example = "12")
    private Integer queueSize;

    @Schema(description = "队列容量；不适用时为空", example = "500")
    private Integer queueCapacity;

    @Schema(description = "队列剩余容量；不适用时为空", example = "488")
    private Integer queueRemainingCapacity;

    @Schema(description = "并发限制；不适用时为空", example = "100")
    private Integer concurrencyLimit;

    @Schema(description = "已提交任务累计数", example = "1200")
    private long submittedTaskCount;

    @Schema(description = "已完成任务累计数", example = "1180")
    private long completedTaskCount;

    @Schema(description = "失败任务累计数", example = "5")
    private long failedTaskCount;

    @Schema(description = "拒绝任务累计数", example = "3")
    private long rejectedTaskCount;
}
