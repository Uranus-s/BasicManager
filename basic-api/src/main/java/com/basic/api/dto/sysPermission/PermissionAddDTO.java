package com.basic.api.dto.sysPermission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 菜单权限新增DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "菜单权限新增请求")
public class PermissionAddDTO {

    /**
     * 父ID
     */
    @Schema(description = "父权限ID，根节点使用0", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "父权限ID不能为空")
    private Long parentId;

    /**
     * 名称
     */
    @Schema(description = "权限名称", example = "用户管理", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50")
    private String name;

    /**
     * 类型 MENU/BUTTON/API
     */
    @Schema(description = "权限类型：MENU菜单，BUTTON按钮，API接口", example = "MENU", allowableValues = {"MENU", "BUTTON", "API"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "权限类型不能为空")
    private String type;

    /**
     * 路由路径或接口路径
     */
    @Schema(description = "前端路由路径或后端接口路径", example = "/system/user")
    @Size(max = 200, message = "路径长度不能超过200")
    private String path;

    /**
     * 前端组件路径
     */
    @Schema(description = "前端组件路径", example = "system/user/index")
    @Size(max = 200, message = "组件路径长度不能超过200")
    private String component;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识", example = "system:user:list")
    @Size(max = 100, message = "权限标识长度不能超过100")
    private String permission;

    /**
     * 图标
     */
    @Schema(description = "菜单图标名称", example = "user")
    private String icon;

    /**
     * 排序
     */
    @Schema(description = "显示排序，数值越小越靠前", example = "1")
    private Integer sort;

    /**
     * 是否显示 0=隐藏 1=显示
     */
    @Schema(description = "是否显示：0隐藏，1显示", example = "1", allowableValues = {"0", "1"})
    private Byte visible;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "状态：0禁用，1正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;
}
