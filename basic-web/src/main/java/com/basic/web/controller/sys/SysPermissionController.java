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

    /**
     * 新增权限
     *
     * @param dto 权限信息
     * @return 操作结果
     */
    @Override
    @PostMapping
    public Result<?> addPermission(@Valid @RequestBody PermissionAddDTO dto) {
        sysPermissionService.addPermission(dto);
        return Result.success();
    }

    /**
     * 更新权限信息
     *
     * @param dto 权限信息
     * @return 操作结果
     */
    @Override
    @PutMapping
    public Result<?> updatePermission(@Valid @RequestBody PermissionUpdateDTO dto) {
        sysPermissionService.updatePermission(dto);
        return Result.success();
    }

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/{id}")
    public Result<?> deletePermission(@PathVariable Long id) {
        sysPermissionService.deletePermission(id);
        return Result.success();
    }

    /**
     * 根据ID获取权限详情
     *
     * @param id 权限ID
     * @return 权限信息
     */
    @Override
    @GetMapping("/{id}")
    public Result<PermissionVO> getPermissionById(@PathVariable Long id) {
        return Result.success(sysPermissionService.getPermissionById(id));
    }

    /**
     * 获取权限列表（分页）
     *
     * @param dto 查询条件
     * @return 权限列表（分页）
     */
    @Override
    @GetMapping("/list")
    public Result<PageResult<PermissionVO>> getPermissionList(PermissionQueryDTO dto) {
        return Result.success(sysPermissionService.getPermissionList(dto));
    }

    /**
     * 获取权限树形结构
     *
     * @return 权限树形列表
     */
    @Override
    @GetMapping("/tree")
    public Result<List<PermissionTreeVO>> getPermissionTree() {
        return Result.success(sysPermissionService.getPermissionTree());
    }

    /**
     * 获取所有权限列表（不分页）
     *
     * @return 所有权限列表
     */
    @Override
    @GetMapping("/all")
    public Result<List<PermissionVO>> getAllPermissions() {
        return Result.success(sysPermissionService.getAllPermissions());
    }

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    @Override
    @GetMapping("/user/{userId}")
    public Result<List<String>> getUserPermissions(@PathVariable Long userId) {
        return Result.success(sysPermissionService.getUserPermissions(userId));
    }
}
