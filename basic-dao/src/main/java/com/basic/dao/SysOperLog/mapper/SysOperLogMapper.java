package com.basic.dao.SysOperLog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basic.dao.SysOperLog.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 操作日志表 Mapper 接口
 * </p>
 *
 * @author aber
 * @since 2026-01-31
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

}
