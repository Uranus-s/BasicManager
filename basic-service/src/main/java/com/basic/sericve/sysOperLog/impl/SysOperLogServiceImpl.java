package com.basic.sericve.sysOperLog.impl;

import com.basic.api.dto.sysOperLog.OperLogQueryDTO;
import com.basic.api.vo.sysOperLog.OperLogVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysOperLog.entity.SysOperLog;
import com.basic.dao.sysOperLog.mapper.SysOperLogMapper;
import com.basic.sericve.sysOperLog.service.ISysOperLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * 操作日志表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOperLog(String module, String method, String requestUrl, String requestMethod,
                            String requestParams, String responseResult, Byte status, Long costTime) {
        SysOperLog operLog = new SysOperLog();
        operLog.setCreateTime(LocalDateTime.now());
        operLog.setModule(module);
        operLog.setMethod(method);
        operLog.setRequestUrl(requestUrl);
        operLog.setRequestMethod(requestMethod);
        operLog.setRequestParams(requestParams);
        operLog.setResponseResult(responseResult);
        operLog.setStatus(status);
        operLog.setCostTime(costTime);
        save(operLog);
        return operLog.getId();
    }

    /** 保存 Agent 写操作最小审计字段，调用方负责只传入核准后的安全内容。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAgentActionLog(String module, String method, Long userId, Long actionId,
                                   Map<String, Object> safeRequest,
                                   Map<String, Object> safeResponse) {
        Map<String, Object> request = new LinkedHashMap<>();
        if (safeRequest != null) {
            request.putAll(safeRequest);
        }
        request.put("userId", userId);
        request.put("actionId", actionId);
        Map<String, Object> response = new LinkedHashMap<>();
        if (safeResponse != null) {
            response.putAll(safeResponse);
        }
        response.put("result", "SUCCESS");
        try {
            // 复用统一操作日志入口，使 Agent 写操作与后台接口采用同一存储格式。
            return saveOperLog(module, method, "/ai/chat/stream", "POST",
                    objectMapper.writeValueAsString(request),
                    objectMapper.writeValueAsString(response),
                    (byte) 1, 0L);
        }
        catch (JacksonException exception) {
            throw new BusinessException(ResultEnum.SERIALIZE_ERROR);
        }
    }

    @Override
    public OperLogVO getOperLogById(Long id) {
        SysOperLog operLog = getById(id);
        if (operLog == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        OperLogVO vo = new OperLogVO();
        BeanUtils.copyProperties(operLog, vo);
        return vo;
    }

    @Override
    public PageResult<OperLogVO> getOperLogList(OperLogQueryDTO dto) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getModule())) {
            wrapper.like(SysOperLog::getModule, dto.getModule());
        }
        if (StringUtils.hasText(dto.getRequestMethod())) {
            wrapper.eq(SysOperLog::getRequestMethod, dto.getRequestMethod());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(SysOperLog::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(SysOperLog::getCreateTime);

        IPage<SysOperLog> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<OperLogVO> voList = new ArrayList<>();
        for (SysOperLog operLog : page.getRecords()) {
            OperLogVO vo = new OperLogVO();
            BeanUtils.copyProperties(operLog, vo);
            voList.add(vo);
        }

        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOperLog(Long id) {
        SysOperLog operLog = getById(id);
        if (operLog == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOperLogs(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            removeByIds(ids);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearOperLog() {
        remove(null);
    }
}
