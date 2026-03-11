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

    @Override
    @GetMapping("/{id}")
    public Result<LoginLogVO> getLoginLogById(@PathVariable Long id) {
        return Result.success(sysLoginLogService.getLoginLogById(id));
    }

    @Override
    @GetMapping("/list")
    public Result<PageResult<LoginLogVO>> getLoginLogList(LoginLogQueryDTO dto) {
        return Result.success(sysLoginLogService.getLoginLogList(dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteLoginLog(@PathVariable Long id) {
        sysLoginLogService.deleteLoginLog(id);
        return Result.success();
    }

    @Override
    @DeleteMapping("/batch")
    public Result<?> deleteLoginLogs(@RequestBody List<Long> ids) {
        sysLoginLogService.deleteLoginLogs(ids);
        return Result.success();
    }

    @Override
    @DeleteMapping("/clear")
    public Result<?> clearLoginLog() {
        sysLoginLogService.clearLoginLog();
        return Result.success();
    }
}
