package com.basic.api.controller.sys;

import com.basic.api.dto.sysUser.UserAddDTO;
import com.basic.api.dto.sysUser.UserQueryDTO;
import com.basic.api.dto.sysUser.UserUpdateDTO;
import com.basic.api.vo.sysUser.UserListVO;
import com.basic.api.vo.sysUser.UserVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理API接口
 *
 * @author Gas
 */
public interface SysUserApi {

    /**
     * 新增用户
     *
     * @param dto 用户新增DTO
     * @return 操作结果
     */
    @PostMapping
    Result<?> addUser(@Valid @RequestBody UserAddDTO dto);

    /**
     * 更新用户
     *
     * @param dto 用户更新DTO
     * @return 操作结果
     */
    @PutMapping
    Result<?> updateUser(@Valid @RequestBody UserUpdateDTO dto);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    Result<?> deleteUser(@PathVariable Long id);

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    Result<UserVO> getUserById(@PathVariable Long id);

    /**
     * 分页查询用户列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    Result<PageResult<UserListVO>> getUserList(UserQueryDTO dto);

    /**
     * 重置密码
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PostMapping("/resetPwd/{id}")
    Result<?> resetPassword(@PathVariable Long id);

    /**
     * 分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 操作结果
     */
    @PostMapping("/assignRoles/{userId}")
    Result<?> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds);

    /**
     * 获取用户角色
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @GetMapping("/roles/{userId}")
    Result<List<Long>> getUserRoles(@PathVariable Long userId);

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 操作结果
     */
    @PostMapping("/updatePwd")
    Result<?> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword);
}
