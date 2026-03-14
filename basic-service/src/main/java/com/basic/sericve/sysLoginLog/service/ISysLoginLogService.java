package com.basic.sericve.sysLoginLog.service;

import com.basic.api.dto.sysLoginLog.LoginLogQueryDTO;
import com.basic.api.vo.sysLoginLog.LoginLogVO;
import com.basic.common.result.PageResult;
import com.basic.dao.sysLoginLog.entity.SysLoginLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 登录日志表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysLoginLogService extends IService<SysLoginLog> {

    /**
     * 记录登录日志
     *
     * @param username 用户名
     * @param ip       IP地址
     * @param browser  浏览器
     * @param os       操作系统
     * @param status   状态 0=失败 1=成功
     * @param msg      提示消息
     * @return 日志ID
     */
    Long saveLoginLog(String username, String ip, String browser, String os, Byte status, String msg);

    /**
     * 获取登录日志详情
     *
     * @param id 日志ID
     * @return 登录日志详情
     */
    LoginLogVO getLoginLogById(Long id);

    /**
     * 分页查询登录日志列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult<LoginLogVO> getLoginLogList(LoginLogQueryDTO dto);

    /**
     * 删除登录日志
     *
     * @param id 日志ID
     */
    void deleteLoginLog(Long id);

    /**
     * 批量删除登录日志
     *
     * @param ids 日志ID列表
     */
    void deleteLoginLogs(java.util.List<Long> ids);

    /**
     * 清空登录日志
     */
    void clearLoginLog();
}
