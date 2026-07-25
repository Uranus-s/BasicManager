package com.basic.sericve.sysThreadPool.service;

import com.basic.api.vo.sysThreadPool.ThreadPoolMonitorVO;

/**
 * 统一线程池监控服务接口。
 *
 * @author Gas
 */
public interface ISysThreadPoolService {

    /**
     * 获取所有受管执行器的实时监控快照。
     *
     * @return 线程池监控信息
     */
    ThreadPoolMonitorVO getStatus();
}
