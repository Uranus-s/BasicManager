package com.basic.api.controller.sys;

import com.basic.api.dto.sysLog.SysLogQueryDTO;
import com.basic.api.vo.sysLog.SysLogVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 统一日志管理API接口
 *
 * @author Gas
 */
public interface SysLogApi {

    /**
     * 分页查询统一日志列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    Result<PageResult<SysLogVO>> getLogList(SysLogQueryDTO dto);
}
