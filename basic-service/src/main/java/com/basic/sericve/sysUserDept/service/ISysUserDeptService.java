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
     * 移除用户所有部门
     *
     * @param userId 用户ID
     */
    void removeAllByUserId(Long userId);
}
