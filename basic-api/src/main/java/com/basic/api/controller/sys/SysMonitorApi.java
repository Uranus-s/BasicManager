package com.basic.api.controller.sys;

import com.basic.api.vo.sysMonitor.MonitorVO;
import com.basic.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 服务监控API接口
 *
 * @author Gas
 */
public interface SysMonitorApi {

    /**
     * 获取服务监控状态
     *
     * @return 服务监控状态
     */
    @GetMapping("/status")
    Result<MonitorVO> getStatus();
}
