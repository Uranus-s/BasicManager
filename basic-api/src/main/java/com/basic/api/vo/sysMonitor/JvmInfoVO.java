package com.basic.api.vo.sysMonitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * JVM信息VO
 *
 * @author Gas
 */
@Data
@Schema(description = "JVM 运行信息")
public class JvmInfoVO {

    /**
     * Java版本
     */
    @Schema(description = "Java 版本", example = "23")
    private String javaVersion;

    /**
     * JVM名称
     */
    @Schema(description = "JVM 名称", example = "OpenJDK 64-Bit Server VM")
    private String jvmName;

    /**
     * JVM供应商
     */
    @Schema(description = "JVM 供应商", example = "Oracle Corporation")
    private String jvmVendor;

    /**
     * JVM启动时间
     */
    @Schema(description = "JVM 启动时间戳，单位：毫秒", example = "1753236000000")
    private Long startTime;

    /**
     * JVM运行时长，毫秒
     */
    @Schema(description = "JVM 运行时长，单位：毫秒", example = "3600000")
    private Long uptimeMs;

    /**
     * 已用堆内存，字节
     */
    @Schema(description = "已用堆内存，单位：字节", example = "268435456")
    private Long heapUsedBytes;

    /**
     * 最大堆内存，字节
     */
    @Schema(description = "最大堆内存，单位：字节", example = "1073741824")
    private Long heapMaxBytes;

    /**
     * 已用非堆内存，字节
     */
    @Schema(description = "已用非堆内存，单位：字节", example = "134217728")
    private Long nonHeapUsedBytes;

    /**
     * 当前线程数
     */
    @Schema(description = "线程总数", example = "48")
    private Integer threadCount;

    /**
     * 守护线程数
     */
    @Schema(description = "守护线程数", example = "40")
    private Integer daemonThreadCount;
}
