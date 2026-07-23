package com.basic.sericve.sysMonitor.service;

import com.basic.api.vo.sysMonitor.MonitorVO;

/**
 * 服务监控服务接口
 *
 * @author Gas
 */
public interface ISysMonitorService {

    /**
     * 获取服务监控状态
     *
     * @return 服务监控状态
     */
    MonitorVO getStatus();
}
