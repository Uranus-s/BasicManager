package com.basic.dao.aiAgent.mapper;

import com.basic.core.mybatis.base.BaseMapperPlus;
import com.basic.dao.aiAgent.entity.AiAgentAction;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AI 网页代理动作 Mapper，提供动作持久化与基础查询能力。
 */
public interface AiAgentActionMapper extends BaseMapperPlus<AiAgentAction> {

    /**
     * 锁定任务内指定动作，保证重复结果上报或确认只能由一个事务完成状态迁移。
     *
     * @param taskId 动作所属任务，用于避免仅凭全局动作标识跨任务读取
     * @param actionId 动作幂等标识
     * @return 锁定后的动作；不存在或已逻辑删除时返回空
     */
    @Select("SELECT * FROM ai_agent_action "
            + "WHERE task_id = #{taskId} AND action_id = #{actionId} AND deleted = 0 "
            + "LIMIT 1 FOR UPDATE")
    AiAgentAction selectByTaskAndActionIdForUpdate(
            @Param("taskId") Long taskId, @Param("actionId") String actionId);
}
