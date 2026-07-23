package com.basic.api.controller.sys;

import com.basic.api.dto.sysOperLog.OperLogQueryDTO;
import com.basic.api.vo.sysOperLog.OperLogVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志管理API接口
 *
 * @author Gas
 */
@Tag(name = "操作日志管理", description = "操作日志查询、删除和清理接口")
public interface SysOperLogApi {

    /**
     * 获取操作日志详情
     *
     * @param id 日志ID
     * @return 操作日志详情
     */
    @Operation(summary = "获取操作日志详情", description = "根据日志ID查询操作日志详情")
    @GetMapping("/{id}")
    Result<OperLogVO> getOperLogById(
            @Parameter(description = "操作日志ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 分页查询操作日志列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询操作日志", description = "根据模块、请求方式、状态和时间范围分页查询")
    @GetMapping("/list")
    Result<PageResult<OperLogVO>> getOperLogList(OperLogQueryDTO dto);

    /**
     * 删除操作日志
     *
     * @param id 日志ID
     * @return 操作结果
     */
    @Operation(summary = "删除操作日志", description = "根据日志ID删除操作日志")
    @DeleteMapping("/{id}")
    Result<?> deleteOperLog(
            @Parameter(description = "操作日志ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除操作日志", description = "根据日志ID列表批量删除操作日志")
    @DeleteMapping("/batch")
    Result<?> deleteOperLogs(
            @Parameter(description = "操作日志ID列表", example = "[1, 2]", required = true)
            @RequestBody List<Long> ids);

    /**
     * 清空操作日志
     *
     * @return 操作结果
     */
    @Operation(summary = "清空操作日志", description = "删除全部操作日志记录")
    @DeleteMapping("/clear")
    Result<?> clearOperLog();
}
