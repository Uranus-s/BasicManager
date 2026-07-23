package com.basic.api.dto.sysConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 参数配置查询DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "参数配置分页查询条件")
public class ConfigQueryDTO {

    /**
     * 参数键（模糊查询）
     */
    @Schema(description = "参数键，支持模糊匹配", example = "system.user")
    private String configKey;

    /**
     * 当前页码（从1开始）
     */
    @Schema(description = "当前页码，从1开始", example = "1", defaultValue = "1", minimum = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页记录数", example = "10", defaultValue = "10", minimum = "1")
    private Integer pageSize = 10;
}
