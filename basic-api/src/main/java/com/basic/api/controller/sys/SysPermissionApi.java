package com.basic.api.controller.sys;

import com.basic.api.dto.sysPermission.PermissionAddDTO;
import com.basic.api.dto.sysPermission.PermissionQueryDTO;
import com.basic.api.dto.sysPermission.PermissionUpdateDTO;
import com.basic.api.vo.sysPermission.PermissionTreeVO;
import com.basic.api.vo.sysPermission.PermissionVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单权限管理API接口
 *
 * @author Gas
 */
@Tag(name = "权限管理", description = "菜单权限、权限树和用户权限查询接口")
public interface SysPermissionApi {

    /**
     * 新增权限
     *
     * @param dto 权限新增DTO
     * @return 操作结果
     */
    @Operation(summary = "新增权限", description = "创建目录、菜单或按钮权限")
    @PostMapping
    Result<?> addPermission(@Valid @RequestBody PermissionAddDTO dto);

    /**
     * 更新权限
     *
     * @param dto 权限更新DTO
     * @return 操作结果
     */
    @Operation(summary = "更新权限", description = "更新指定权限的路由、标识和展示信息")
    @PutMapping
    Result<?> updatePermission(@Valid @RequestBody PermissionUpdateDTO dto);

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @return 操作结果
     */
    @Operation(summary = "删除权限", description = "根据权限ID删除菜单权限")
    @DeleteMapping("/{id}")
    Result<?> deletePermission(
            @Parameter(description = "权限ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 获取权限详情
     *
     * @param id 权限ID
     * @return 权限详情
     */
    @Operation(summary = "获取权限详情", description = "根据权限ID查询权限详情")
    @GetMapping("/{id}")
    Result<PermissionVO> getPermissionById(
            @Parameter(description = "权限ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 分页查询权限列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询权限", description = "根据权限名称、类型和状态分页查询")
    @GetMapping("/list")
    Result<PageResult<PermissionVO>> getPermissionList(PermissionQueryDTO dto);

    /**
     * 获取权限树
     *
     * @return 权限树
     */
    @Operation(summary = "获取权限树", description = "返回完整的菜单权限层级树")
    @GetMapping("/tree")
    Result<List<PermissionTreeVO>> getPermissionTree();

    /**
     * 获取所有权限列表（下拉选择用）
     *
     * @return 权限列表
     */
    @Operation(summary = "查询全部权限", description = "返回全部权限，适用于角色授权选择")
    @GetMapping("/all")
    Result<List<PermissionVO>> getAllPermissions();

    /**
     * 获取用户权限（用于权限认证）
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    @Operation(summary = "查询用户权限", description = "返回指定用户拥有的权限标识列表")
    @GetMapping("/user/{userId}")
    Result<List<String>> getUserPermissions(
            @Parameter(description = "用户ID", example = "1", required = true)
            @PathVariable Long userId);
}
