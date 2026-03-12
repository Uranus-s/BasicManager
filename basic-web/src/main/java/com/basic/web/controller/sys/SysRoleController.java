package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysRoleApi;
import com.basic.api.dto.sysRole.RoleAddDTO;
import com.basic.api.dto.sysRole.RoleQueryDTO;
import com.basic.api.dto.sysRole.RoleUpdateDTO;
import com.basic.api.vo.sysRole.RoleListVO;
import com.basic.api.vo.sysRole.RoleVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.sericve.sysRole.service.ISysRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController implements SysRoleApi {

    private final ISysRoleService sysRoleService;

    /**
     * 新增角色
     *
     * @param dto 角色信息
     * @return 操作结果
     */
    @Override
    @PostMapping
    public Result<?> addRole(@Valid @RequestBody RoleAddDTO dto) {
        sysRoleService.addRole(dto);
        return Result.success();
    }

    /**
     * 更新角色信息
     *
     * @param dto 角色信息
     * @return 操作结果
     */
    @Override
    @PutMapping
    public Result<?> updateRole(@Valid @RequestBody RoleUpdateDTO dto) {
        sysRoleService.updateRole(dto);
        return Result.success();
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteRole(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 根据ID获取角色详情
     *
     * @param id 角色ID
     * @return 角色信息
     */
    @Override
    @GetMapping("/{id}")
    public Result<RoleVO> getRoleById(@PathVariable Long id) {
        return Result.success(sysRoleService.getRoleById(id));
    }

    /**
     * 获取角色列表（分页）
     *
     * @param dto 查询条件
     * @return 角色列表（分页）
     */
    @Override
    @GetMapping("/list")
    public Result<PageResult<RoleListVO>> getRoleList(RoleQueryDTO dto) {
        return Result.success(sysRoleService.getRoleList(dto));
    }

    /**
     * 获取所有角色列表（不分页）
     *
     * @return 所有角色列表
     */
    @Override
    @GetMapping("/all")
    public Result<List<RoleListVO>> getAllRoles() {
        return Result.success(sysRoleService.getAllRoles());
    }

    /**
     * 分配角色权限
     *
     * @param roleId       角色ID
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    @Override
    @PostMapping("/assignPermissions/{roleId}")
    public Result<?> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        sysRoleService.assignPermissions(roleId, permissionIds);
        return Result.success();
    }

    /**
     * 获取角色权限列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Override
    @GetMapping("/permissions/{roleId}")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        return Result.success(sysRoleService.getRolePermissions(roleId));
    }
}
