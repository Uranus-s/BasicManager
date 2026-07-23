package com.basic.api.controller.sys;

import com.basic.api.vo.sysMonitor.MonitorVO;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 服务监控API接口
 *
 * @author Gas
 */
@Tag(name = "服务监控", description = "查询应用、JVM、磁盘、数据库和 Redis 运行状态接口")
public interface SysMonitorApi {

    /**
     * 获取服务监控状态
     *
     * @return 服务监控状态
     */
    @Operation(summary = "获取服务监控状态", description = "汇总返回系统、JVM、磁盘、数据库和 Redis 状态")
    @GetMapping("/status")
    Result<MonitorVO> getStatus();
}
