package com.basic.sericve.sysLog.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basic.api.dto.sysLog.SysLogQueryDTO;
import com.basic.api.vo.sysLog.SysLogVO;
import com.basic.common.result.PageResult;
import com.basic.dao.sysLog.mapper.SysLogMapper;
import com.basic.dao.sysLog.model.SysLogQuery;
import com.basic.dao.sysLog.model.SysLogRecord;
import com.basic.sericve.sysLog.service.ISysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一日志服务实现
 *
 * @author Gas
 */
@Service
@RequiredArgsConstructor
public class SysLogServiceImpl implements ISysLogService {

    private final SysLogMapper sysLogMapper;

    @Override
    public PageResult<SysLogVO> getLogList(SysLogQueryDTO dto) {
        SysLogQueryDTO queryDTO = dto == null ? new SysLogQueryDTO() : dto;
        SysLogQuery query = buildQuery(queryDTO);
        IPage<SysLogRecord> page = sysLogMapper.selectLogPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                query
        );

        List<SysLogVO> voList = new ArrayList<>();
        for (SysLogRecord record : page.getRecords()) {
            SysLogVO vo = new SysLogVO();
            BeanUtils.copyProperties(record, vo);
            voList.add(vo);
        }

        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), voList);
    }

    private SysLogQuery buildQuery(SysLogQueryDTO dto) {
        SysLogQuery query = new SysLogQuery();
        query.setLogType(dto.getLogType());
        query.setKeyword(dto.getKeyword());
        query.setStatus(dto.getStatus());
        return query;
    }
}
