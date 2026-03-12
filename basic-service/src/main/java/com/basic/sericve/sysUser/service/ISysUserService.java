package com.basic.sericve.sysUser.service;

import com.basic.api.dto.sysUser.UserAddDTO;
import com.basic.api.dto.sysUser.UserQueryDTO;
import com.basic.api.dto.sysUser.UserUpdateDTO;
import com.basic.api.vo.auth.InitResultVO;
import com.basic.api.vo.sysUser.UserListVO;
import com.basic.api.vo.sysUser.UserVO;
import com.basic.common.result.PageResult;
import com.basic.dao.sysUser.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 系统用户表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果（包含Token和用户信息）
     */
    com.basic.api.vo.auth.LoginVO login(String username, String password);

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 新增用户
     *
     * @param dto 用户新增DTO
     * @return 用户ID
     */
    Long addUser(UserAddDTO dto);

    /**
     * 更新用户
     *
     * @param dto 用户更新DTO
     */
    void updateUser(UserUpdateDTO dto);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserVO getUserById(Long id);

    /**
     * 分页查询用户列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult<UserListVO> getUserList(UserQueryDTO dto);

    /**
     * 重置密码
     *
     * @param id 用户ID
     */
    void resetPassword(Long id);

    /**
     * 分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(Long userId, java.util.List<Long> roleIds);

    /**
     * 获取用户角色
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    java.util.List<Long> getUserRoles(Long userId);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    SysUser getUserByUsername(String username);

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 初始化管理员（用户、角色、权限）
     *
     * @param adminPassword 管理员密码
     * @return 初始化结果
     */
    InitResultVO initAdmin(String adminPassword);
}
