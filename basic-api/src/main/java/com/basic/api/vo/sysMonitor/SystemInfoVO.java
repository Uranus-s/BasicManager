package com.basic.api.vo.sysMonitor;

import lombok.Data;

/**
 * 系统信息VO
 *
 * @author Gas
 */
@Data
public class SystemInfoVO {

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 当前环境
     */
    private String[] activeProfiles;

    /**
     * 操作系统名称
     */
    private String osName;

    /**
     * 操作系统版本
     */
    private String osVersion;

    /**
     * 系统架构
     */
    private String osArch;

    /**
     * 可用处理器数量
     */
    private Integer availableProcessors;

    /**
     * 系统平均负载
     */
    private Double systemLoadAverage;

    /**
     * 用户目录
     */
    private String userDir;
}
