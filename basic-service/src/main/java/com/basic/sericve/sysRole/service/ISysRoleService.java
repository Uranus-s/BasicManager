package com.basic.sericve.sysRole.service;

import com.basic.api.dto.sysRole.RoleAddDTO;
import com.basic.api.dto.sysRole.RoleQueryDTO;
import com.basic.api.dto.sysRole.RoleUpdateDTO;
import com.basic.api.vo.sysRole.RoleListVO;
import com.basic.api.vo.sysRole.RoleVO;
import com.basic.common.result.PageResult;
import com.basic.dao.sysRole.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 系统角色表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 新增角色
     *
     * @param dto 角色新增DTO
     * @return 角色ID
     */
    Long addRole(RoleAddDTO dto);

    /**
     * 更新角色
     *
     * @param dto 角色更新DTO
     */
    void updateRole(RoleUpdateDTO dto);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    RoleVO getRoleById(Long id);

    /**
     * 分页查询角色列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult<RoleListVO> getRoleList(RoleQueryDTO dto);

    /**
     * 获取所有角色列表
     *
     * @return 角色列表
     */
    List<RoleListVO> getAllRoles();

    /**
     * 分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色权限
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getRolePermissions(Long roleId);

    /**
     * 获取用户角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getRoleCodes(Long userId);
}
