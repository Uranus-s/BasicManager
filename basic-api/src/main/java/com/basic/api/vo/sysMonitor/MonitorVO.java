package com.basic.api.vo.sysMonitor;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务监控信息VO
 *
 * @author Gas
 */
@Data
public class MonitorVO {

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;

    /**
     * 系统信息
     */
    private SystemInfoVO system;

    /**
     * JVM信息
     */
    private JvmInfoVO jvm;

    /**
     * 磁盘信息
     */
    private List<DiskInfoVO> disks;

    /**
     * 数据库状态
     */
    private DatabaseHealthVO database;

    /**
     * Redis状态
     */
    private RedisHealthVO redis;
}
