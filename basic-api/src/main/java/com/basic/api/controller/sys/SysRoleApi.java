package com.basic.api.controller.sys;

import com.basic.api.dto.sysRole.RoleAddDTO;
import com.basic.api.dto.sysRole.RoleQueryDTO;
import com.basic.api.dto.sysRole.RoleUpdateDTO;
import com.basic.api.dto.sysRole.RoleUserManageDTO;
import com.basic.api.vo.sysRole.RoleListVO;
import com.basic.api.vo.sysRole.RoleVO;
import com.basic.api.vo.sysUser.UserListVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理API接口
 *
 * @author Gas
 */
@Tag(name = "角色管理", description = "角色、角色权限和角色用户关联管理接口")
public interface SysRoleApi {

    /**
     * 新增角色
     *
     * @param dto 角色新增DTO
     * @return 操作结果
     */
    @Operation(summary = "新增角色", description = "创建系统角色并配置初始权限")
    @PostMapping
    Result<?> addRole(@Valid @RequestBody RoleAddDTO dto);

    /**
     * 更新角色
     *
     * @param dto 角色更新DTO
     * @return 操作结果
     */
    @Operation(summary = "更新角色", description = "更新角色名称、状态、备注和权限")
    @PutMapping
    Result<?> updateRole(@Valid @RequestBody RoleUpdateDTO dto);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @Operation(summary = "删除角色", description = "根据角色ID删除系统角色")
    @DeleteMapping("/{id}")
    Result<?> deleteRole(
            @Parameter(description = "角色ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @Operation(summary = "获取角色详情", description = "根据角色ID查询角色及其权限详情")
    @GetMapping("/{id}")
    Result<RoleVO> getRoleById(
            @Parameter(description = "角色ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 分页查询角色列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询角色", description = "根据角色编码、名称和状态分页查询")
    @GetMapping("/list")
    Result<PageResult<RoleListVO>> getRoleList(RoleQueryDTO dto);

    /**
     * 获取所有角色列表（下拉选择用）
     *
     * @return 角色列表
     */
    @Operation(summary = "查询全部角色", description = "返回全部角色，适用于用户角色分配")
    @GetMapping("/all")
    Result<List<RoleListVO>> getAllRoles();

    /**
     * 分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    @Operation(summary = "分配角色权限", description = "使用权限ID列表覆盖指定角色的权限")
    @PostMapping("/assignPermissions/{roleId}")
    Result<?> assignPermissions(
            @Parameter(description = "角色ID", example = "1", required = true)
            @PathVariable Long roleId,
            @Parameter(description = "权限ID列表", example = "[1, 2]", required = true)
            @RequestBody List<Long> permissionIds);

    /**
     * 获取角色权限
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Operation(summary = "查询角色权限", description = "返回指定角色关联的权限ID列表")
    @GetMapping("/permissions/{roleId}")
    Result<List<Long>> getRolePermissions(
            @Parameter(description = "角色ID", example = "1", required = true)
            @PathVariable Long roleId);

    /**
     * 获取角色关联的用户列表
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Operation(summary = "查询角色用户", description = "返回指定角色关联的用户列表")
    @GetMapping("/users/{roleId}")
    Result<List<UserListVO>> getUsersByRoleId(
            @Parameter(description = "角色ID", example = "1", required = true)
            @PathVariable Long roleId);

    /**
     * 批量管理角色关联用户
     *
     * @param roleId 角色ID
     * @param dto    用户增量变更信息
     * @return 操作结果
     */
    @Operation(summary = "管理角色用户", description = "批量新增或移除指定角色关联的用户")
    @PostMapping("/users/{roleId}")
    Result<?> manageRoleUsers(
            @Parameter(description = "角色ID", example = "1", required = true)
            @PathVariable Long roleId,
            @Valid @RequestBody RoleUserManageDTO dto);
}
