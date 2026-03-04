package com.basic.sericve.sysConfig.impl;

import com.basic.dao.sysConfig.entity.SysConfig;
import com.basic.dao.sysConfig.mapper.SysConfigMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.basic.sericve.sysConfig.service.ISysConfigService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统参数表 服务实现类
 * </p>
 *
 * @author aber
 * @since 2026-01-31
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

}
