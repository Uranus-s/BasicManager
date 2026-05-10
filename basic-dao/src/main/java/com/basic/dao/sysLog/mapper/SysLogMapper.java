package com.basic.dao.sysLog.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basic.dao.sysLog.model.SysLogQuery;
import com.basic.dao.sysLog.model.SysLogRecord;
import org.apache.ibatis.annotations.Param;

/**
 * 统一日志查询Mapper
 *
 * @author Gas
 */
public interface SysLogMapper {

    /**
     * 分页查询统一日志列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SysLogRecord> selectLogPage(Page<SysLogRecord> page, @Param("query") SysLogQuery query);
}
