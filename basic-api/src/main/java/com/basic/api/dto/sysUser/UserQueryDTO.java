package com.basic.api.dto.sysUser;

import lombok.Data;

/**
 * 用户查询DTO
 *
 * @author Gas
 */
@Data
public class UserQueryDTO {

    /**
     * 登录账号（模糊查询）
     */
    private String username;

    /**
     * 昵称（模糊查询）
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0=禁用 1=正常
     */
    private Byte status;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
