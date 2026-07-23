package com.basic.api.controller.sys;

import com.basic.api.dto.sysUser.UserAddDTO;
import com.basic.api.dto.sysUser.UserQueryDTO;
import com.basic.api.dto.sysUser.UserUpdateDTO;
import com.basic.api.vo.sysUser.UserListVO;
import com.basic.api.vo.sysUser.UserVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户管理API接口
 *
 * @author Gas
 */
@Tag(name = "用户管理", description = "系统用户、角色分配、密码和个人头像管理接口")
public interface SysUserApi {

    /**
     * 新增用户
     *
     * @param dto 用户新增DTO
     * @return 操作结果
     */
    @Operation(summary = "新增用户", description = "创建系统用户并关联指定部门和角色")
    @PostMapping
    Result<?> addUser(@Valid @RequestBody UserAddDTO dto);

    /**
     * 更新用户
     *
     * @param dto 用户更新DTO
     * @return 操作结果
     */
    @Operation(summary = "更新用户", description = "更新用户资料、状态、部门和角色")
    @PutMapping
    Result<?> updateUser(@Valid @RequestBody UserUpdateDTO dto);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除用户", description = "根据用户ID删除系统用户")
    @DeleteMapping("/{id}")
    Result<?> deleteUser(
            @Parameter(description = "用户ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @Operation(summary = "获取用户详情", description = "根据用户ID查询用户、部门和角色详情")
    @GetMapping("/{id}")
    Result<UserVO> getUserById(
            @Parameter(description = "用户ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 分页查询用户列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询用户", description = "根据用户名、昵称、手机号和状态分页查询")
    @GetMapping("/list")
    Result<PageResult<UserListVO>> getUserList(UserQueryDTO dto);

    /**
     * 重置密码
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @Operation(summary = "重置用户密码", description = "将指定用户密码重置为系统默认密码")
    @PostMapping("/resetPwd/{id}")
    Result<?> resetPassword(
            @Parameter(description = "用户ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 操作结果
     */
    @Operation(summary = "分配用户角色", description = "使用角色ID列表覆盖指定用户的角色")
    @PostMapping("/assignRoles/{userId}")
    Result<?> assignRoles(
            @Parameter(description = "用户ID", example = "1", required = true)
            @PathVariable Long userId,
            @Parameter(description = "角色ID列表", example = "[1, 2]", required = true)
            @RequestBody List<Long> roleIds);

    /**
     * 获取用户角色
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Operation(summary = "查询用户角色", description = "返回指定用户关联的角色ID列表")
    @GetMapping("/roles/{userId}")
    Result<List<Long>> getUserRoles(
            @Parameter(description = "用户ID", example = "1", required = true)
            @PathVariable Long userId);

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 操作结果
     */
    @Operation(summary = "修改当前用户密码", description = "校验旧密码后设置新的登录密码")
    @PostMapping("/updatePwd")
    Result<?> updatePassword(
            @Parameter(description = "旧密码", example = "OldP@ssw0rd!", required = true)
            @RequestParam String oldPassword,
            @Parameter(description = "新密码", example = "NewP@ssw0rd!", required = true)
            @RequestParam String newPassword);

    /**
     * 修改当前用户头像
     *
     * @param file 头像文件
     * @return 头像访问路径
     */
    @Operation(summary = "修改当前用户头像", description = "上传头像文件并更新当前用户头像地址")
    @PostMapping("/avatar")
    Result<String> updateCurrentUserAvatar(
            @Parameter(description = "头像文件", required = true)
            @RequestParam("file") MultipartFile file);

    /**
     * 获取当前用户头像
     *
     * @return 头像访问路径
     */
    @Operation(summary = "获取当前用户头像", description = "返回当前登录用户的头像访问地址")
    @GetMapping("/avatar")
    Result<String> getCurrentUserAvatar();
}
