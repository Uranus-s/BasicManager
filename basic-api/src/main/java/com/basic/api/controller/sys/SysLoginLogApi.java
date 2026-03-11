package com.basic.api.controller.sys;

import com.basic.api.dto.sysLoginLog.LoginLogQueryDTO;
import com.basic.api.vo.sysLoginLog.LoginLogVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录日志管理API接口
 *
 * @author Gas
 */
public interface SysLoginLogApi {

    /**
     * 获取登录日志详情
     *
     * @param id 日志ID
     * @return 登录日志详情
     */
    @GetMapping("/{id}")
    Result<LoginLogVO> getLoginLogById(@PathVariable Long id);

    /**
     * 分页查询登录日志列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    Result<PageResult<LoginLogVO>> getLoginLogList(LoginLogQueryDTO dto);

    /**
     * 删除登录日志
     *
     * @param id 日志ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    Result<?> deleteLoginLog(@PathVariable Long id);

    /**
     * 批量删除登录日志
     *
     * @param ids 日志ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    Result<?> deleteLoginLogs(@RequestBody List<Long> ids);

    /**
     * 清空登录日志
     *
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    Result<?> clearLoginLog();
}
