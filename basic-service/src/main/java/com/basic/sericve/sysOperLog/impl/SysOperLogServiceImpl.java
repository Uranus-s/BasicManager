package com.basic.sericve.sysOperLog.impl;

import com.basic.dao.sysOperLog.entity.SysOperLog;
import com.basic.dao.sysOperLog.mapper.SysOperLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.basic.sericve.sysOperLog.service.ISysOperLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志表 服务实现类
 * </p>
 *
 * @author aber
 * @since 2026-01-31
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {

}
