package com.basic.sericve.sysOperLog.service;

import com.basic.api.dto.sysOperLog.OperLogQueryDTO;
import com.basic.api.vo.sysOperLog.OperLogVO;
import com.basic.common.result.PageResult;
import com.basic.dao.sysOperLog.entity.SysOperLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 操作日志表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysOperLogService extends IService<SysOperLog> {

    /**
     * 保存操作日志
     *
     * @param module         模块名
     * @param method         方法名
     * @param requestUrl     请求URL
     * @param requestMethod  请求方式
     * @param requestParams  请求参数
     * @param responseResult 返回结果
     * @param status         状态 0=失败 1=成功
     * @param costTime       耗时(ms)
     * @return 日志ID
     */
    Long saveOperLog(String module, String method, String requestUrl, String requestMethod,
                     String requestParams, String responseResult, Byte status, Long costTime);

    /**
     * 获取操作日志详情
     *
     * @param id 日志ID
     * @return 操作日志详情
     */
    OperLogVO getOperLogById(Long id);

    /**
     * 分页查询操作日志列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult<OperLogVO> getOperLogList(OperLogQueryDTO dto);

    /**
     * 删除操作日志
     *
     * @param id 日志ID
     */
    void deleteOperLog(Long id);

    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     */
    void deleteOperLogs(java.util.List<Long> ids);

    /**
     * 清空操作日志
     */
    void clearOperLog();
}
