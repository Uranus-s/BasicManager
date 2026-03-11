package com.basic.api.controller.sys;

import com.basic.api.dto.sysOperLog.OperLogQueryDTO;
import com.basic.api.vo.sysOperLog.OperLogVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志管理API接口
 *
 * @author Gas
 */
public interface SysOperLogApi {

    /**
     * 获取操作日志详情
     *
     * @param id 日志ID
     * @return 操作日志详情
     */
    @GetMapping("/{id}")
    Result<OperLogVO> getOperLogById(@PathVariable Long id);

    /**
     * 分页查询操作日志列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    Result<PageResult<OperLogVO>> getOperLogList(OperLogQueryDTO dto);

    /**
     * 删除操作日志
     *
     * @param id 日志ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    Result<?> deleteOperLog(@PathVariable Long id);

    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    Result<?> deleteOperLogs(@RequestBody List<Long> ids);

    /**
     * 清空操作日志
     *
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    Result<?> clearOperLog();
}
