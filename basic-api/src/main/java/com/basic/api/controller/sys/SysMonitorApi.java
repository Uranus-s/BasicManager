package com.basic.api.controller.sys;

import com.basic.api.vo.sysMonitor.MonitorVO;
import com.basic.api.vo.sysThreadPool.ThreadPoolMonitorVO;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 服务监控API接口
 *
 * @author Gas
 */
@Tag(name = "服务监控", description = "查询应用、JVM、基础设施和线程池运行状态接口")
public interface SysMonitorApi {

    /**
     * 获取服务监控状态
     *
     * @return 服务监控状态
     */
    @Operation(summary = "获取服务监控状态", description = "汇总返回系统、JVM、磁盘、数据库和 Redis 状态")
    @GetMapping("/status")
    Result<MonitorVO> getStatus();

    /**
     * 获取统一管理线程池的实时快照。
     *
     * @return 线程池实时监控信息
     */
    @Operation(summary = "获取线程池实时状态", description = "返回所有统一管理执行器的当前运行指标")
    @GetMapping("/thread-pools")
    Result<ThreadPoolMonitorVO> getThreadPoolStatus();
}
