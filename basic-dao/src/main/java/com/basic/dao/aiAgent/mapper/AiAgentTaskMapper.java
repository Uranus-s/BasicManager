package com.basic.dao.aiAgent.mapper;

import com.basic.core.mybatis.base.BaseMapperPlus;
import com.basic.dao.aiAgent.entity.AiAgentTask;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AI 网页代理任务 Mapper，提供任务持久化与基础查询能力。
 */
public interface AiAgentTaskMapper extends BaseMapperPlus<AiAgentTask> {

    /**
     * 在当前事务内锁定任务行，串行化状态变更和异步规划结果落库。
     *
     * @param id 任务主键
     * @return 锁定后的任务；不存在或已逻辑删除时返回空
     */
    @Select("SELECT * FROM ai_agent_task WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    AiAgentTask selectByIdForUpdate(@Param("id") Long id);
}
