package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysOperLogApi;
import com.basic.api.dto.sysOperLog.OperLogQueryDTO;
import com.basic.api.vo.sysOperLog.OperLogVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.sericve.sysOperLog.service.ISysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志管理 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/operLog")
@RequiredArgsConstructor
public class SysOperLogController implements SysOperLogApi {

    private final ISysOperLogService sysOperLogService;

    /**
     * 根据ID获取操作日志详情
     *
     * @param id 日志ID
     * @return 操作日志信息
     */
    @Override
    @GetMapping("/{id}")
    public Result<OperLogVO> getOperLogById(@PathVariable Long id) {
        return Result.success(sysOperLogService.getOperLogById(id));
    }

    /**
     * 获取操作日志列表（分页）
     *
     * @param dto 查询条件
     * @return 操作日志列表（分页）
     */
    @Override
    @GetMapping("/list")
    public Result<PageResult<OperLogVO>> getOperLogList(OperLogQueryDTO dto) {
        return Result.success(sysOperLogService.getOperLogList(dto));
    }

    /**
     * 删除操作日志
     *
     * @param id 日志ID
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteOperLog(@PathVariable Long id) {
        sysOperLogService.deleteOperLog(id);
        return Result.success();
    }

    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/batch")
    public Result<?> deleteOperLogs(@RequestBody List<Long> ids) {
        sysOperLogService.deleteOperLogs(ids);
        return Result.success();
    }

    /**
     * 清空所有操作日志
     *
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/clear")
    public Result<?> clearOperLog() {
        sysOperLogService.clearOperLog();
        return Result.success();
    }
}
