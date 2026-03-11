package com.basic.sericve.sysUserRole.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.basic.dao.sysUserRole.entity.SysUserRole;

import java.util.List;

/**
 * <p>
 * 用户角色关联表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysUserRoleService extends IService<SysUserRole> {

    /**
     * 分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getRoleIdsByUserId(Long userId);

    /**
     * 移除用户所有角色
     *
     * @param userId 用户ID
     */
    void removeAllByUserId(Long userId);
}
