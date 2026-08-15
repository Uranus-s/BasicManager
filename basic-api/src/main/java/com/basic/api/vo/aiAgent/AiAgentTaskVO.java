package com.basic.api.vo.aiAgent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 网页代理任务状态视图，不包含原始敏感页面数据。
 */
@Data
public class AiAgentTaskVO {
    /**
     * 任务追踪主键，事件订阅、控制操作和动作结果上报均以此关联。
     */
    private Long id;
    /**
     * 面向列表和审计展示的脱敏目标摘要，不返回用户原始输入。
     */
    private String goalSummary;
    /**
     * 服务端任务状态机当前状态，客户端不得通过本对象直接改变状态。
     */
    private String status;
    /**
     * 当前可信页面快照对应的业务路由，页面跳转后需以新快照更新。
     */
    private String routeName;
    /**
     * 当前页面快照版本，用于拒绝基于旧页面状态下发或执行动作。
     */
    private String pageVersion;
    /**
     * 已完成规划步骤数，表示任务进度而非可由客户端指定的执行序号。
     */
    private Integer currentStep;
    /**
     * 当前尚未终结的动作幂等标识，客户端仅可对该动作进行结果上报或确认。
     */
    private String activeActionId;
    /**
     * 稳定失败代码，供客户端展示与恢复策略判断，避免依赖易变的错误描述。
     */
    private String failureCode;
    /**
     * 服务端创建任务时记录的起始时间，用于展示任务生命周期的开始点。
     */
    private LocalDateTime startedAt;
    /**
     * 任务进入完成、失败或取消等终态的服务端时间，非终态任务允许为空。
     */
    private LocalDateTime finishedAt;
    /**
     * 当前待执行或待确认动作的详情；任务无活动动作或处于终态时为空。
     */
    private AiAgentActionVO activeAction;
}
