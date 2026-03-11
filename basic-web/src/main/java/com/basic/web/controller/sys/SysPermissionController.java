package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysPermissionApi;
import com.basic.api.dto.sysPermission.PermissionAddDTO;
import com.basic.api.dto.sysPermission.PermissionQueryDTO;
import com.basic.api.dto.sysPermission.PermissionUpdateDTO;
import com.basic.api.vo.sysPermission.PermissionTreeVO;
import com.basic.api.vo.sysPermission.PermissionVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.sericve.sysPermission.service.ISysPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单权限管理 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/permission")
@RequiredArgsConstructor
public class SysPermissionController implements SysPermissionApi {

    private final ISysPermissionService sysPermissionService;

    @Override
    @PostMapping
    public Result<?> addPermission(@Valid @RequestBody PermissionAddDTO dto) {
        sysPermissionService.addPermission(dto);
        return Result.success();
    }

    @Override
    @PutMapping
    public Result<?> updatePermission(@Valid @RequestBody PermissionUpdateDTO dto) {
        sysPermissionService.updatePermission(dto);
        return Result.success();
    }

    @Override
    @DeleteMapping("/{id}")
    public Result<?> deletePermission(@PathVariable Long id) {
        sysPermissionService.deletePermission(id);
        return Result.success();
    }

    @Override
    @GetMapping("/{id}")
    public Result<PermissionVO> getPermissionById(@PathVariable Long id) {
        return Result.success(sysPermissionService.getPermissionById(id));
    }

    @Override
    @GetMapping("/list")
    public Result<PageResult<PermissionVO>> getPermissionList(PermissionQueryDTO dto) {
        return Result.success(sysPermissionService.getPermissionList(dto));
    }

    @Override
    @GetMapping("/tree")
    public Result<List<PermissionTreeVO>> getPermissionTree() {
        return Result.success(sysPermissionService.getPermissionTree());
    }

    @Override
    @GetMapping("/all")
    public Result<List<PermissionVO>> getAllPermissions() {
        return Result.success(sysPermissionService.getAllPermissions());
    }

    @Override
    @GetMapping("/user/{userId}")
    public Result<List<String>> getUserPermissions(@PathVariable Long userId) {
        return Result.success(sysPermissionService.getUserPermissions(userId));
    }
}
