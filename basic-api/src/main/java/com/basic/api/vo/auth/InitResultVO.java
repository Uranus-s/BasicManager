package com.basic.api.vo.auth;

import lombok.Data;

/**
 * 初始化结果VO
 *
 * @author Gas
 */
@Data
public class InitResultVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色编码
     */
    private String roleCode;
}
