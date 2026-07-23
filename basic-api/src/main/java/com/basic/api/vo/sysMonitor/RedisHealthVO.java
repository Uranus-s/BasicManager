package com.basic.api.vo.sysMonitor;

import lombok.Data;

/**
 * Redis健康状态VO
 *
 * @author Gas
 */
@Data
public class RedisHealthVO {

    /**
     * 状态：UP/DOWN
     */
    private String status;

    /**
     * Redis模式：single/cluster
     */
    private String mode;

    /**
     * PING响应
     */
    private String ping;

    /**
     * 响应耗时，毫秒
     */
    private Long responseTimeMs;

    /**
     * 错误信息
     */
    private String errorMessage;
}
