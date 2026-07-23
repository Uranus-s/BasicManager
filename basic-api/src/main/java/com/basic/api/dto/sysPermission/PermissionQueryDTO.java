package com.basic.api.dto.sysPermission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜单权限查询DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "菜单权限分页查询条件")
public class PermissionQueryDTO {

    /**
     * 名称（模糊查询）
     */
    @Schema(description = "权限名称，支持模糊匹配", example = "用户")
    private String name;

    /**
     * 类型 MENU/BUTTON/API
     */
    @Schema(description = "权限类型：MENU菜单，BUTTON按钮，API接口", example = "MENU", allowableValues = {"MENU", "BUTTON", "API"})
    private String type;

    /**
     * 父ID
     */
    @Schema(description = "父权限ID", example = "0")
    private Long parentId;

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
