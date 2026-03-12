package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysUserApi;
import com.basic.api.dto.sysUser.UserAddDTO;
import com.basic.api.dto.sysUser.UserQueryDTO;
import com.basic.api.dto.sysUser.UserUpdateDTO;
import com.basic.api.vo.sysUser.UserListVO;
import com.basic.api.vo.sysUser.UserVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.core.security.model.LoginUser;
import com.basic.sericve.sysUser.service.ISysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController implements SysUserApi {

    private final ISysUserService sysUserService;

    /**
     * 新增用户
     *
     * @param dto 用户信息
     * @return 操作结果
     */
    @Override
    @PostMapping
    public Result<?> addUser(@Valid @RequestBody UserAddDTO dto) {
        sysUserService.addUser(dto);
        return Result.success();
    }

    /**
     * 更新用户信息
     *
     * @param dto 用户信息
     * @return 操作结果
     */
    @Override
    @PutMapping
    public Result<?> updateUser(@Valid @RequestBody UserUpdateDTO dto) {
        sysUserService.updateUser(dto);
        return Result.success();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.success(sysUserService.getUserById(id));
    }

    /**
     * 获取用户列表（分页）
     *
     * @param dto 查询条件
     * @return 用户列表（分页）
     */
    @Override
    @GetMapping("/list")
    public Result<PageResult<UserListVO>> getUserList(UserQueryDTO dto) {
        return Result.success(sysUserService.getUserList(dto));
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @Override
    @PostMapping("/resetPwd/{id}")
    public Result<?> resetPassword(@PathVariable Long id) {
        sysUserService.resetPassword(id);
        return Result.success();
    }

    /**
     * 分配用户角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 操作结果
     */
    @Override
    @PostMapping("/assignRoles/{userId}")
    public Result<?> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        sysUserService.assignRoles(userId, roleIds);
        return Result.success();
    }

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Override
    @GetMapping("/roles/{userId}")
    public Result<List<Long>> getUserRoles(@PathVariable Long userId) {
        return Result.success(sysUserService.getUserRoles(userId));
    }

    /**
     * 修改当前用户密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 操作结果
     */
    @Override
    @PostMapping("/updatePwd")
    public Result<?> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUserId();

        sysUserService.updatePassword(userId, oldPassword, newPassword);
        return Result.success();
    }
}
