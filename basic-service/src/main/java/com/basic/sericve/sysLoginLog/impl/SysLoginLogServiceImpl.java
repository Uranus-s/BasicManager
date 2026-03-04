package com.basic.sericve.sysLoginLog.impl;

import com.basic.dao.sysLoginLog.entity.SysLoginLog;
import com.basic.dao.sysLoginLog.mapper.SysLoginLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.basic.sericve.sysLoginLog.service.ISysLoginLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 登录日志表 服务实现类
 * </p>
 *
 * @author aber
 * @since 2026-01-31
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements ISysLoginLogService {

}
