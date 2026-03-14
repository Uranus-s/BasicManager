package com.basic.sericve.sysConfig.impl;

import com.basic.api.dto.sysConfig.ConfigAddDTO;
import com.basic.api.dto.sysConfig.ConfigQueryDTO;
import com.basic.api.dto.sysConfig.ConfigUpdateDTO;
import com.basic.api.vo.sysConfig.ConfigVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysConfig.entity.SysConfig;
import com.basic.dao.sysConfig.mapper.SysConfigMapper;
import com.basic.sericve.sysConfig.service.ISysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 系统参数表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addConfig(ConfigAddDTO dto) {
        // 检查参数键是否已存在
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, dto.getConfigKey());
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultEnum.DATA_ALREADY_EXIST);
        }

        // 创建参数配置
        SysConfig config = new SysConfig();
        BeanUtils.copyProperties(dto, config);
        save(config);
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(ConfigUpdateDTO dto) {
        SysConfig config = getById(dto.getId());
        if (config == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        BeanUtils.copyProperties(dto, config);
        updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        SysConfig config = getById(id);
        if (config == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }
        // 逻辑删除
        removeById(id);
    }

    @Override
    public ConfigVO getConfigById(Long id) {
        SysConfig config = getById(id);
        if (config == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        ConfigVO vo = new ConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }

    @Override
    public PageResult<ConfigVO> getConfigList(ConfigQueryDTO dto) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getConfigKey())) {
            wrapper.like(SysConfig::getConfigKey, dto.getConfigKey());
        }
        wrapper.orderByDesc(SysConfig::getCreateTime);

        IPage<SysConfig> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<ConfigVO> voList = new ArrayList<>();
        for (SysConfig config : page.getRecords()) {
            ConfigVO vo = new ConfigVO();
            BeanUtils.copyProperties(config, vo);
            voList.add(vo);
        }

        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), voList);
    }

    @Override
    public String getConfigByKey(String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig config = getOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }
}
