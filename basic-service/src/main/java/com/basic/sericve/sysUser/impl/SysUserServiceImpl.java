package com.basic.sericve.sysUser.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.basic.dao.sysUser.entity.SysUser;
import com.basic.dao.sysUser.mapper.SysUserMapper;
import com.basic.sericve.sysUser.service.ISysUserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统用户表 服务实现类
 * </p>
 *
 * @author aber
 * @since 2026-01-28
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

}
