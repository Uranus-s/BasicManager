package com.basic.api.vo.sysRole;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色列表VO
 *
 * @author Gas
 */
@Data
public class RoleListVO {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 状态 0=禁用 1=正常
     */
    private Byte status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
