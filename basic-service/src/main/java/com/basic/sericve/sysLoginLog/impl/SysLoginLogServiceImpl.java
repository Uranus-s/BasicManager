package com.basic.sericve.sysLoginLog.impl;

import com.basic.api.dto.sysLoginLog.LoginLogQueryDTO;
import com.basic.api.vo.sysLoginLog.LoginLogVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysLoginLog.entity.SysLoginLog;
import com.basic.dao.sysLoginLog.mapper.SysLoginLogMapper;
import com.basic.sericve.sysLoginLog.service.ISysLoginLogService;
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
 * 登录日志表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements ISysLoginLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveLoginLog(String username, String ip, String browser, String os, Byte status, String msg) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username);
        loginLog.setIp(ip);
        loginLog.setBrowser(browser);
        loginLog.setOs(os);
        loginLog.setStatus(status);
        loginLog.setMsg(msg);
        save(loginLog);
        return loginLog.getId();
    }

    @Override
    public LoginLogVO getLoginLogById(Long id) {
        SysLoginLog loginLog = getById(id);
        if (loginLog == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        LoginLogVO vo = new LoginLogVO();
        BeanUtils.copyProperties(loginLog, vo);
        return vo;
    }

    @Override
    public PageResult<LoginLogVO> getLoginLogList(LoginLogQueryDTO dto) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getUsername())) {
            wrapper.like(SysLoginLog::getUsername, dto.getUsername());
        }
        if (StringUtils.hasText(dto.getIp())) {
            wrapper.eq(SysLoginLog::getIp, dto.getIp());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(SysLoginLog::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(SysLoginLog::getCreateTime);

        IPage<SysLoginLog> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<LoginLogVO> voList = new ArrayList<>();
        for (SysLoginLog loginLog : page.getRecords()) {
            LoginLogVO vo = new LoginLogVO();
            BeanUtils.copyProperties(loginLog, vo);
            voList.add(vo);
        }

        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLoginLog(Long id) {
        SysLoginLog loginLog = getById(id);
        if (loginLog == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLoginLogs(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            removeByIds(ids);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearLoginLog() {
        remove(null);
    }
}
