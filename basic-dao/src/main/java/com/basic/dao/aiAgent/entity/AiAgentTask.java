package com.basic.dao.aiAgent.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basic.core.mybatis.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * AI 网页代理任务实体，用于持久化用户目标、页面绑定关系和任务执行状态。
 */
@Getter
@Setter
@ToString
@TableName("ai_agent_task")
public class AiAgentTask extends BaseEntity {

    private Long userId;

    /**
     * 浏览器标签页实例 ID，用于确保任务只在绑定的页面中执行。
     */
    private String clientInstanceId;

    /**
     * 活动任务唯一键，用于限制同一用户与标签页执行范围内只能存在一个运行中任务。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String activeKey;

    /**
     * 脱敏后的用户目标摘要，用于模型决策和审计展示。
     */
    private String goalSummary;

    private String status;

    /**
     * 当前前端路由名称，用于校验任务所处页面。
     */
    private String routeName;

    /**
     * 当前页面版本摘要，用于识别页面上下文是否发生变化。
     */
    private String pageVersion;

    private Integer currentStep;

    /**
     * 当前等待执行或确认的动作 ID。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String activeActionId;

    /**
     * 当前异步规划租约令牌，用于拒绝暂停、终止或重新规划前启动的旧模型结果，防止迟到响应覆盖新状态。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String planningToken;

    /**
     * 稳定失败代码，用于向客户端返回可判定的失败原因，不存放面向用户的可变错误文本。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String failureCode;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;
}
