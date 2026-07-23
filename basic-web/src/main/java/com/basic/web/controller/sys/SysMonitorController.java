package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysMonitorApi;
import com.basic.api.vo.sysMonitor.MonitorVO;
import com.basic.common.result.Result;
import com.basic.sericve.sysMonitor.service.ISysMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务监控 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/monitor")
@RequiredArgsConstructor
public class SysMonitorController implements SysMonitorApi {

    private final ISysMonitorService sysMonitorService;

    /**
     * 获取服务监控状态
     *
     * @return 服务监控状态
     */
    @Override
    @GetMapping("/status")
//    @PreAuthorize("hasAuthority('system:monitor:view')")
    public Result<MonitorVO> getStatus() {
        return Result.success(sysMonitorService.getStatus());
    }
}
