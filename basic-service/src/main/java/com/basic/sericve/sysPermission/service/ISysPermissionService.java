package com.basic.sericve.sysPermission.service;

import com.basic.api.dto.sysPermission.PermissionAddDTO;
import com.basic.api.dto.sysPermission.PermissionQueryDTO;
import com.basic.api.dto.sysPermission.PermissionUpdateDTO;
import com.basic.api.vo.sysPermission.PermissionTreeVO;
import com.basic.api.vo.sysPermission.PermissionVO;
import com.basic.common.result.PageResult;
import com.basic.dao.sysPermission.entity.SysPermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 菜单权限表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysPermissionService extends IService<SysPermission> {

    /**
     * 新增权限
     *
     * @param dto 权限新增DTO
     * @return 权限ID
     */
    Long addPermission(PermissionAddDTO dto);

    /**
     * 更新权限
     *
     * @param dto 权限更新DTO
     */
    void updatePermission(PermissionUpdateDTO dto);

    /**
     * 删除权限
     *
     * @param id 权限ID
     */
    void deletePermission(Long id);

    /**
     * 获取权限详情
     *
     * @param id 权限ID
     * @return 权限详情
     */
    PermissionVO getPermissionById(Long id);

    /**
     * 分页查询权限列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult<PermissionVO> getPermissionList(PermissionQueryDTO dto);

    /**
     * 获取权限树
     *
     * @return 权限树
     */
    List<PermissionTreeVO> getPermissionTree();

    /**
     * 获取所有权限列表
     *
     * @return 权限列表
     */
    List<PermissionVO> getAllPermissions();

    /**
     * 获取用户权限（用于权限认证）
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户权限实体列表
     *
     * @param userId 用户ID
     * @return 权限实体列表
     */
    List<SysPermission> getPermissionsByUserId(Long userId);
}
