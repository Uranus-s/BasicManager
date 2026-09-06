package com.basic.dao.ai.action.mapper;

import com.basic.core.mybatis.base.BaseMapperPlus;
import com.basic.dao.ai.action.entity.AiAction;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * AI 助手待审批操作 Mapper，涉及状态执行的读取必须使用行锁方法。
 */
public interface AiActionMapper extends BaseMapperPlus<AiAction> {

    /** 锁定用户主表行，将同一用户的待审批槽位变更串行化。 */
    Long lockActionOwner(@Param("userId") Long userId);

    /** 查询用户最新且未过期的待审批操作。 */
    AiAction selectPendingByUserId(@Param("userId") Long userId,
                                   @Param("now") LocalDateTime now);

    /** 按主键加排他锁读取，供确认和取消事务执行状态机校验。 */
    AiAction selectByIdForUpdate(@Param("id") Long id);

    /** 批量取消用户旧操作。 */
    int cancelPendingByUserId(@Param("userId") Long userId,
                              @Param("updateTime") LocalDateTime updateTime);

}
