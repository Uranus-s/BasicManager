package com.basic.api.vo.sysMonitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Redis健康状态VO
 *
 * @author Gas
 */
@Data
@Schema(description = "Redis 健康信息")
public class RedisHealthVO {

    /**
     * 状态：UP/DOWN
     */
    @Schema(description = "健康状态", example = "UP", allowableValues = {"UP", "DOWN"})
    private String status;

    /**
     * Redis模式：single/cluster
     */
    @Schema(description = "运行模式", example = "cluster")
    private String mode;

    /**
     * PING响应
     */
    @Schema(description = "PING 响应", example = "PONG")
    private String ping;

    /**
     * 响应耗时，毫秒
     */
    @Schema(description = "响应耗时，单位：毫秒", example = "5")
    private Long responseTimeMs;

    /**
     * 错误信息
     */
    @Schema(description = "异常信息，健康时为空")
    private String errorMessage;
}
