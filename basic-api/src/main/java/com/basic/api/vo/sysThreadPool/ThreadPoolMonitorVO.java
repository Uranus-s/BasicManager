package com.basic.api.vo.sysThreadPool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 线程池实时监控信息。
 *
 * @author Gas
 */
@Data
@Schema(description = "线程池实时监控信息")
public class ThreadPoolMonitorVO {

    /**
     * 本次接口采集线程池状态的时间。
     */
    @Schema(description = "数据采集时间", example = "2026-07-25T12:00:00")
    private LocalDateTime collectTime;

    /**
     * 当前受统一监控管理的执行器快照。
     */
    @Schema(description = "受管执行器实时快照")
    private List<ThreadPoolSnapshotVO> executors;
}
