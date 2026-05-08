package com.basic.sericve.sysUserDept.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.basic.dao.sysUserDept.entity.SysUserDept;

import java.util.List;

/**
 * <p>
 * 用户部门关联表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysUserDeptService extends IService<SysUserDept> {

    /**
     * 分配部门
     *
     * @param userId 用户ID
     * @param deptIds 部门ID列表
     */
    void assignDepts(Long userId, List<Long> deptIds);

    /**
     * 获取用户部门ID列表
     *
     * @param userId 用户ID
     * @return 部门ID列表
     */
    List<Long> getDeptIdsByUserId(Long userId);

    /**
     * 获取部门用户ID列表
     *
     * @param deptId 部门ID
     * @return 用户ID列表
     */
    List<Long> getUserIdsByDeptId(Long deptId);

    /**
     * 给部门新增用户关联
     *
     * @param deptId  部门ID
     * @param userIds 用户ID列表
     */
    void addUsersToDept(Long deptId, List<Long> userIds);

    /**
     * 删除部门用户关联
     *
     * @param deptId  部门ID
     * @param userIds 用户ID列表
     */
    void removeUsersFromDept(Long deptId, List<Long> userIds);

    /**
     * 移除用户所有部门
     *
     * @param userId 用户ID
     */
    void removeAllByUserId(Long userId);
}
