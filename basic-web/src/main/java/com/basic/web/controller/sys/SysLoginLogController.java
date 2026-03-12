package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysLoginLogApi;
import com.basic.api.dto.sysLoginLog.LoginLogQueryDTO;
import com.basic.api.vo.sysLoginLog.LoginLogVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.sericve.sysLoginLog.service.ISysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录日志管理 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/loginLog")
@RequiredArgsConstructor
public class SysLoginLogController implements SysLoginLogApi {

    private final ISysLoginLogService sysLoginLogService;

    /**
     * 根据ID获取登录日志详情
     *
     * @param id 日志ID
     * @return 登录日志信息
     */
    @Override
    @GetMapping("/{id}")
    public Result<LoginLogVO> getLoginLogById(@PathVariable Long id) {
        return Result.success(sysLoginLogService.getLoginLogById(id));
    }

    /**
     * 获取登录日志列表（分页）
     *
     * @param dto 查询条件
     * @return 登录日志列表（分页）
     */
    @Override
    @GetMapping("/list")
    public Result<PageResult<LoginLogVO>> getLoginLogList(LoginLogQueryDTO dto) {
        return Result.success(sysLoginLogService.getLoginLogList(dto));
    }

    /**
     * 删除登录日志
     *
     * @param id 日志ID
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteLoginLog(@PathVariable Long id) {
        sysLoginLogService.deleteLoginLog(id);
        return Result.success();
    }

    /**
     * 批量删除登录日志
     *
     * @param ids 日志ID列表
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/batch")
    public Result<?> deleteLoginLogs(@RequestBody List<Long> ids) {
        sysLoginLogService.deleteLoginLogs(ids);
        return Result.success();
    }

    /**
     * 清空所有登录日志
     *
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/clear")
    public Result<?> clearLoginLog() {
        sysLoginLogService.clearLoginLog();
        return Result.success();
    }
}
