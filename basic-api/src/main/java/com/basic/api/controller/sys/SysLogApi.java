package com.basic.api.controller.sys;

import com.basic.api.dto.sysLog.SysLogQueryDTO;
import com.basic.api.vo.sysLog.SysLogVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 统一日志管理API接口
 *
 * @author Gas
 */
@Tag(name = "系统日志", description = "统一查询系统运行日志接口")
public interface SysLogApi {

    /**
     * 分页查询统一日志列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询系统日志", description = "按日志类型、级别、用户和时间范围分页查询")
    @GetMapping("/list")
    Result<PageResult<SysLogVO>> getLogList(SysLogQueryDTO dto);
}
