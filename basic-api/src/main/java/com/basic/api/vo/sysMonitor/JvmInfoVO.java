package com.basic.api.vo.sysMonitor;

import lombok.Data;

/**
 * JVM信息VO
 *
 * @author Gas
 */
@Data
public class JvmInfoVO {

    /**
     * Java版本
     */
    private String javaVersion;

    /**
     * JVM名称
     */
    private String jvmName;

    /**
     * JVM供应商
     */
    private String jvmVendor;

    /**
     * JVM启动时间
     */
    private Long startTime;

    /**
     * JVM运行时长，毫秒
     */
    private Long uptimeMs;

    /**
     * 已用堆内存，字节
     */
    private Long heapUsedBytes;

    /**
     * 最大堆内存，字节
     */
    private Long heapMaxBytes;

    /**
     * 已用非堆内存，字节
     */
    private Long nonHeapUsedBytes;

    /**
     * 当前线程数
     */
    private Integer threadCount;

    /**
     * 守护线程数
     */
    private Integer daemonThreadCount;
}
