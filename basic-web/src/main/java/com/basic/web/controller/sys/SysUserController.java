package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysUserApi;
import com.basic.api.dto.sysUser.UserAddDTO;
import com.basic.api.dto.sysUser.UserQueryDTO;
import com.basic.api.dto.sysUser.UserUpdateDTO;
import com.basic.api.vo.sysUser.UserListVO;
import com.basic.api.vo.sysUser.UserVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.sericve.sysUser.service.ISysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @Override
    @PostMapping
    public Result<?> addUser(@Valid @RequestBody UserAddDTO dto) {
        sysUserService.addUser(dto);
        return Result.success();
    }

    @Override
    @PutMapping
    public Result<?> updateUser(@Valid @RequestBody UserUpdateDTO dto) {
        sysUserService.updateUser(dto);
        return Result.success();
    }

    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    @Override
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.success(sysUserService.getUserById(id));
    }

    @Override
    @GetMapping("/list")
    public Result<PageResult<UserListVO>> getUserList(UserQueryDTO dto) {
        return Result.success(sysUserService.getUserList(dto));
    }

    @Override
    @PostMapping("/resetPwd/{id}")
    public Result<?> resetPassword(@PathVariable Long id) {
        sysUserService.resetPassword(id);
        return Result.success();
    }

    @Override
    @PostMapping("/assignRoles/{userId}")
    public Result<?> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        sysUserService.assignRoles(userId, roleIds);
        return Result.success();
    }

    @Override
    @GetMapping("/roles/{userId}")
    public Result<List<Long>> getUserRoles(@PathVariable Long userId) {
        return Result.success(sysUserService.getUserRoles(userId));
    }

    @Override
    @PostMapping("/updatePwd")
    public Result<?> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        // TODO: 从SecurityContext获取当前用户ID
        sysUserService.updatePassword(1L, oldPassword, newPassword);
        return Result.success();
    }
}
