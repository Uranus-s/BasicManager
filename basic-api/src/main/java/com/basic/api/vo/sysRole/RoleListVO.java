package com.basic.api.vo.sysRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色列表VO
 *
 * @author Gas
 */
@Data
@Schema(description = "角色列表信息")
public class RoleListVO {

    /**
     * 角色ID
     */
    @Schema(description = "角色 ID", example = "1")
    private Long id;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码", example = "system_admin")
    private String roleCode;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", example = "系统管理员")
    private String roleName;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "状态：0-禁用，1-正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;

    /**
     * 备注
     */
    @Schema(description = "角色备注", example = "负责系统日常管理")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-07-23T10:30:00")
    private LocalDateTime createTime;
}
