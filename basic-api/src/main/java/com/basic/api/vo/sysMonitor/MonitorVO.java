package com.basic.api.vo.sysMonitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务监控信息VO
 *
 * @author Gas
 */
@Data
@Schema(description = "系统监控汇总信息")
public class MonitorVO {

    /**
     * 采集时间
     */
    @Schema(description = "数据采集时间", example = "2026-07-23T10:30:00")
    private LocalDateTime collectTime;

    /**
     * 系统信息
     */
    @Schema(description = "操作系统及应用信息")
    private SystemInfoVO system;

    /**
     * JVM信息
     */
    @Schema(description = "JVM 运行信息")
    private JvmInfoVO jvm;

    /**
     * 磁盘信息
     */
    @Schema(description = "磁盘使用信息列表")
    private List<DiskInfoVO> disks;

    /**
     * 数据库状态
     */
    @Schema(description = "数据库健康信息")
    private DatabaseHealthVO database;

    /**
     * Redis状态
     */
    @Schema(description = "Redis 健康信息")
    private RedisHealthVO redis;
}
