package com.basic.api.controller.sys;

import com.basic.api.dto.sysPermission.PermissionAddDTO;
import com.basic.api.dto.sysPermission.PermissionQueryDTO;
import com.basic.api.dto.sysPermission.PermissionUpdateDTO;
import com.basic.api.vo.sysPermission.PermissionTreeVO;
import com.basic.api.vo.sysPermission.PermissionVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单权限管理API接口
 *
 * @author Gas
 */
public interface SysPermissionApi {

    /**
     * 新增权限
     *
     * @param dto 权限新增DTO
     * @return 操作结果
     */
    @PostMapping
    Result<?> addPermission(@Valid @RequestBody PermissionAddDTO dto);

    /**
     * 更新权限
     *
     * @param dto 权限更新DTO
     * @return 操作结果
     */
    @PutMapping
    Result<?> updatePermission(@Valid @RequestBody PermissionUpdateDTO dto);

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    Result<?> deletePermission(@PathVariable Long id);

    /**
     * 获取权限详情
     *
     * @param id 权限ID
     * @return 权限详情
     */
    @GetMapping("/{id}")
    Result<PermissionVO> getPermissionById(@PathVariable Long id);

    /**
     * 分页查询权限列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    Result<PageResult<PermissionVO>> getPermissionList(PermissionQueryDTO dto);

    /**
     * 获取权限树
     *
     * @return 权限树
     */
    @GetMapping("/tree")
    Result<List<PermissionTreeVO>> getPermissionTree();

    /**
     * 获取所有权限列表（下拉选择用）
     *
     * @return 权限列表
     */
    @GetMapping("/all")
    Result<List<PermissionVO>> getAllPermissions();

    /**
     * 获取用户权限（用于权限认证）
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    @GetMapping("/user/{userId}")
    Result<List<String>> getUserPermissions(@PathVariable Long userId);
}
