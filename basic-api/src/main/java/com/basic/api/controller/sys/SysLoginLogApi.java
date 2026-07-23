package com.basic.api.controller.sys;

import com.basic.api.dto.sysLoginLog.LoginLogQueryDTO;
import com.basic.api.vo.sysLoginLog.LoginLogVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录日志管理API接口
 *
 * @author Gas
 */
@Tag(name = "登录日志管理", description = "登录日志查询、删除和清理接口")
public interface SysLoginLogApi {

    /**
     * 获取登录日志详情
     *
     * @param id 日志ID
     * @return 登录日志详情
     */
    @Operation(summary = "获取登录日志详情", description = "根据日志ID查询登录日志详情")
    @GetMapping("/{id}")
    Result<LoginLogVO> getLoginLogById(
            @Parameter(description = "登录日志ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 分页查询登录日志列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询登录日志", description = "根据用户名、IP、状态和时间范围分页查询")
    @GetMapping("/list")
    Result<PageResult<LoginLogVO>> getLoginLogList(LoginLogQueryDTO dto);

    /**
     * 删除登录日志
     *
     * @param id 日志ID
     * @return 操作结果
     */
    @Operation(summary = "删除登录日志", description = "根据日志ID删除登录日志")
    @DeleteMapping("/{id}")
    Result<?> deleteLoginLog(
            @Parameter(description = "登录日志ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 批量删除登录日志
     *
     * @param ids 日志ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除登录日志", description = "根据日志ID列表批量删除登录日志")
    @DeleteMapping("/batch")
    Result<?> deleteLoginLogs(
            @Parameter(description = "登录日志ID列表", example = "[1, 2]", required = true)
            @RequestBody List<Long> ids);

    /**
     * 清空登录日志
     *
     * @return 操作结果
     */
    @Operation(summary = "清空登录日志", description = "删除全部登录日志记录")
    @DeleteMapping("/clear")
    Result<?> clearLoginLog();
}
