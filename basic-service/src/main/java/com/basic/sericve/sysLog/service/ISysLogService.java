package com.basic.sericve.sysLog.service;

import com.basic.api.dto.sysLog.SysLogQueryDTO;
import com.basic.api.vo.sysLog.SysLogVO;
import com.basic.common.result.PageResult;

/**
 * 统一日志服务接口
 *
 * @author Gas
 */
public interface ISysLogService {

    /**
     * 分页查询统一日志列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult<SysLogVO> getLogList(SysLogQueryDTO dto);
}
