package com.basic.dao.aiAgent.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.basic.core.mybatis.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * AI 网页代理动作实体，用于记录任务内每个语义动作的下发、确认和执行结果。
 */
@Getter
@Setter
@ToString
@TableName("ai_agent_action")
public class AiAgentAction extends BaseEntity {

    private Long taskId;

    /**
     * 全局幂等动作 ID，用于将重复下发、确认和结果上报收敛到同一条动作记录，避免重复执行。
     */
    private String actionId;

    private Integer sequenceNo;

    /**
     * 动作生成时所在的前端路由名称。
     */
    private String routeName;

    /**
     * 动作生成时的页面版本摘要，用于阻止在过期页面上执行。
     */
    private String pageVersion;

    /**
     * 语义动作类型，用于选择客户端执行器。
     */
    private String actionType;

    /**
     * 动作对应的页面语义目标。
     */
    private String target;

    /**
     * 动作风险等级，用于判定是否需要用户确认；该值是服务端策略结论，客户端不能自行降低。
     */
    private String riskLevel;

    /**
     * 脱敏后的动作参数 JSON，用于下发和审计；不得保存脚本、凭据或未过滤的页面原始数据。
     */
    private String argumentsJson;

    private String status;

    /**
     * 脱敏后的执行结果摘要。
     */
    private String resultSummary;

    /**
     * 服务端基于可信页面快照生成的高风险确认摘要，用于刷新后恢复确认上下文，不能信任模型自述。
     */
    private String confirmationSummary;

    private Long confirmedBy;

    private LocalDateTime confirmedAt;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;
}
