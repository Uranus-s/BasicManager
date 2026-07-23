package com.basic.api.vo.sysMonitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统信息VO
 *
 * @author Gas
 */
@Data
@Schema(description = "操作系统及应用信息")
public class SystemInfoVO {

    /**
     * 应用名称
     */
    @Schema(description = "应用名称", example = "basic-web")
    private String applicationName;

    /**
     * 当前环境
     */
    @Schema(description = "激活的运行环境", example = "[\"dev\"]")
    private String[] activeProfiles;

    /**
     * 操作系统名称
     */
    @Schema(description = "操作系统名称", example = "Windows 11")
    private String osName;

    /**
     * 操作系统版本
     */
    @Schema(description = "操作系统版本", example = "10.0")
    private String osVersion;

    /**
     * 系统架构
     */
    @Schema(description = "操作系统架构", example = "amd64")
    private String osArch;

    /**
     * 可用处理器数量
     */
    @Schema(description = "可用处理器数量", example = "16")
    private Integer availableProcessors;

    /**
     * 系统平均负载
     */
    @Schema(description = "系统平均负载", example = "1.25")
    private Double systemLoadAverage;

    /**
     * 用户目录
     */
    @Schema(description = "应用工作目录", example = "G:\\Java\\BasicProject")
    private String userDir;
}
