package com.basic.dao.sysUser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basic.dao.sysUser.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 系统用户表 Mapper 接口
 * </p>
 *
 * @author aber
 * @since 2026-01-28
 */
//@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser getUserByName1(@Param("username") String username);

    SysUser getUserByName2(@Param("username") String username);
}
