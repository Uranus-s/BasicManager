package com.basic.api.dto.sysDict;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字典查询DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "字典分页查询条件")
public class DictQueryDTO {

    /**
     * 字典编码（模糊查询）
     */
    @Schema(description = "字典编码，支持模糊匹配", example = "sys_user")
    private String dictCode;

    /**
     * 字典名称（模糊查询）
     */
    @Schema(description = "字典名称，支持模糊匹配", example = "用户")
    private String dictName;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "状态：0禁用，1正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;

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
