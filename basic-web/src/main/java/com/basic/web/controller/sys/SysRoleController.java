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

    @Override
    @PostMapping
    public Result<?> addRole(@Valid @RequestBody RoleAddDTO dto) {
        sysRoleService.addRole(dto);
        return Result.success();
    }

    @Override
    @PutMapping
    public Result<?> updateRole(@Valid @RequestBody RoleUpdateDTO dto) {
        sysRoleService.updateRole(dto);
        return Result.success();
    }

    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteRole(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    @Override
    @GetMapping("/{id}")
    public Result<RoleVO> getRoleById(@PathVariable Long id) {
        return Result.success(sysRoleService.getRoleById(id));
    }

    @Override
    @GetMapping("/list")
    public Result<PageResult<RoleListVO>> getRoleList(RoleQueryDTO dto) {
        return Result.success(sysRoleService.getRoleList(dto));
    }

    @Override
    @GetMapping("/all")
    public Result<List<RoleListVO>> getAllRoles() {
        return Result.success(sysRoleService.getAllRoles());
    }

    @Override
    @PostMapping("/assignPermissions/{roleId}")
    public Result<?> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        sysRoleService.assignPermissions(roleId, permissionIds);
        return Result.success();
    }

    @Override
    @GetMapping("/permissions/{roleId}")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        return Result.success(sysRoleService.getRolePermissions(roleId));
    }
}
