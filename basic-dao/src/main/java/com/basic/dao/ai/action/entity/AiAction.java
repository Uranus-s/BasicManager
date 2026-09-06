package com.basic.dao.ai.action.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.basic.core.mybatis.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * AI 助手待审批操作，payloadJson 是确认时唯一允许执行的不可变业务快照。
 */
@Getter
@Setter
@ToString(exclude = "payloadJson")
@TableName("ai_agent_action")
public class AiAction extends BaseEntity {

    /** 生成该操作的用户消息，用于关联完整会话日志。 */
    private Long triggerMessageId;
    /** 操作归属用户；所有读取和状态变更都必须校验该字段。 */
    private Long userId;
    /** 稳定动作类型，决定确认阶段选择哪个确定性执行器。 */
    private String actionType;
    /** 不可变业务快照 JSON，确认时禁止重新解析用户原始回复。 */
    private String payloadJson;
    /** PENDING、CANCELLED 或 EXECUTED。 */
    private String status;
    /** 允许用户确认的截止时间。 */
    private LocalDateTime expiresAt;
    /** 用户明确确认的时间。 */
    private LocalDateTime confirmedAt;
    /** 确定性业务写入完成的时间。 */
    private LocalDateTime executedAt;
    /** 已执行操作产生的业务主键，用于幂等返回。 */
    private Long resultId;
}
