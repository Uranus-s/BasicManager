package com.basic.api.controller.sys;

import com.basic.api.dto.sysRole.RoleAddDTO;
import com.basic.api.dto.sysRole.RoleQueryDTO;
import com.basic.api.dto.sysRole.RoleUpdateDTO;
import com.basic.api.vo.sysRole.RoleListVO;
import com.basic.api.vo.sysRole.RoleVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理API接口
 *
 * @author Gas
 */
public interface SysRoleApi {

    /**
     * 新增角色
     *
     * @param dto 角色新增DTO
     * @return 操作结果
     */
    @PostMapping
    Result<?> addRole(@Valid @RequestBody RoleAddDTO dto);

    /**
     * 更新角色
     *
     * @param dto 角色更新DTO
     * @return 操作结果
     */
    @PutMapping
    Result<?> updateRole(@Valid @RequestBody RoleUpdateDTO dto);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    Result<?> deleteRole(@PathVariable Long id);

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    Result<RoleVO> getRoleById(@PathVariable Long id);

    /**
     * 分页查询角色列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    Result<PageResult<RoleListVO>> getRoleList(RoleQueryDTO dto);

    /**
     * 获取所有角色列表（下拉选择用）
     *
     * @return 角色列表
     */
    @GetMapping("/all")
    Result<List<RoleListVO>> getAllRoles();

    /**
     * 分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    @PostMapping("/assignPermissions/{roleId}")
    Result<?> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds);

    /**
     * 获取角色权限
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @GetMapping("/permissions/{roleId}")
    Result<List<Long>> getRolePermissions(@PathVariable Long roleId);
}
