package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysLogApi;
import com.basic.api.dto.sysLog.SysLogQueryDTO;
import com.basic.api.vo.sysLog.SysLogVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.sericve.sysLog.service.ISysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一日志管理 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/log")
@RequiredArgsConstructor
public class SysLogController implements SysLogApi {

    private final ISysLogService sysLogService;

    /**
     * 获取统一日志列表（分页）
     *
     * @param dto 查询条件
     * @return 统一日志列表（分页）
     */
    @Override
    @GetMapping("/list")
    public Result<PageResult<SysLogVO>> getLogList(SysLogQueryDTO dto) {
        return Result.success(sysLogService.getLogList(dto));
    }
}
