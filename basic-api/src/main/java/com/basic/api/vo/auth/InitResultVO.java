package com.basic.api.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 初始化结果VO
 *
 * @author Gas
 */
@Data
@Schema(description = "系统初始化结果")
public class InitResultVO {

    /**
     * 用户ID
     */
    @Schema(description = "管理员用户 ID", example = "1")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "管理员登录账号", example = "admin")
    private String username;

    /**
     * 角色ID
     */
    @Schema(description = "管理员角色 ID", example = "1")
    private Long roleId;

    /**
     * 角色编码
     */
    @Schema(description = "管理员角色编码", example = "system_admin")
    private String roleCode;
}
