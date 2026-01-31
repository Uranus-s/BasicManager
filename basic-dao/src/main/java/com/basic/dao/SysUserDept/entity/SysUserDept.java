package com.basic.dao.SysUserDept.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 * 用户部门关联表
 * </p>
 *
 * @author aber
 * @since 2026-01-31
 */
@Getter
@Setter
@ToString
@TableName("sys_user_dept")
public class SysUserDept implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId("user_id")
    private Long userId;

    /**
     * 部门ID
     */
    @TableId("dept_id")
    private Long deptId;
}
