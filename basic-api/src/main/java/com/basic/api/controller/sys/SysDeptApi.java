package com.basic.api.controller.sys;

import com.basic.api.dto.sysDept.DeptAddDTO;
import com.basic.api.dto.sysDept.DeptQueryDTO;
import com.basic.api.dto.sysDept.DeptUpdateDTO;
import com.basic.api.vo.sysDept.DeptTreeVO;
import com.basic.api.vo.sysDept.DeptVO;
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
 * 部门管理API接口
 *
 * @author Gas
 */
@Tag(name = "部门管理", description = "组织部门、部门树和部门用户关联管理接口")
public interface SysDeptApi {

    /**
     * 新增部门
     *
     * @param dto 部门新增DTO
     * @return 操作结果
     */
    @Operation(summary = "新增部门", description = "创建组织部门并设置上级部门、负责人和排序")
    @PostMapping
    Result<?> addDept(@Valid @RequestBody DeptAddDTO dto);

    /**
     * 更新部门
     *
     * @param dto 部门更新DTO
     * @return 操作结果
     */
    @Operation(summary = "更新部门", description = "更新指定部门的组织信息")
    @PutMapping
    Result<?> updateDept(@Valid @RequestBody DeptUpdateDTO dto);

    /**
     * 删除部门
     *
     * @param id 部门ID
     * @return 操作结果
     */
    @Operation(summary = "删除部门", description = "根据部门ID删除组织部门")
    @DeleteMapping("/{id}")
    Result<?> deleteDept(
            @Parameter(description = "部门ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    @Operation(summary = "获取部门详情", description = "根据部门ID查询部门详情")
    @GetMapping("/{id}")
    Result<DeptVO> getDeptById(
            @Parameter(description = "部门ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 分页查询部门列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询部门", description = "根据部门名称、负责人和上级部门分页查询")
    @GetMapping("/list")
    Result<PageResult<DeptVO>> getDeptList(DeptQueryDTO dto);

    /**
     * 获取部门树
     *
     * @return 部门树
     */
    @Operation(summary = "获取部门树", description = "返回完整的组织部门层级树")
    @GetMapping("/tree")
    Result<List<DeptTreeVO>> getDeptTree();

    /**
     * 获取所有部门列表（下拉选择用）
     *
     * @return 部门列表
     */
    @Operation(summary = "查询全部部门", description = "返回全部部门，适用于下拉选择")
    @GetMapping("/all")
    Result<List<DeptVO>> getAllDepts();

    /**
     * 获取部门关联的用户列表
     *
     * @param deptId 部门ID
     * @return 用户列表
     */
    @Operation(summary = "查询部门用户", description = "返回指定部门关联的用户列表")
    @GetMapping("/users/{deptId}")
    Result<List<UserListVO>> getUsersByDeptId(
            @Parameter(description = "部门ID", example = "1", required = true)
            @PathVariable Long deptId);

    /**
     * 给部门新增用户
     *
     * @param deptId  部门ID
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "添加部门用户", description = "批量将用户关联到指定部门")
    @PostMapping("/users/{deptId}")
    Result<?> addUsersToDept(
            @Parameter(description = "部门ID", example = "1", required = true)
            @PathVariable Long deptId,
            @Parameter(description = "用户ID列表", example = "[1, 2]", required = true)
            @RequestBody List<Long> userIds);

    /**
     * 删除部门下的用户
     *
     * @param deptId  部门ID
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "移除部门用户", description = "批量解除用户与指定部门的关联")
    @DeleteMapping("/users/{deptId}")
    Result<?> removeUsersFromDept(
            @Parameter(description = "部门ID", example = "1", required = true)
            @PathVariable Long deptId,
            @Parameter(description = "用户ID列表", example = "[1, 2]", required = true)
            @RequestBody List<Long> userIds);
}
