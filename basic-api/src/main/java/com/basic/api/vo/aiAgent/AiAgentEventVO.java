package com.basic.api.vo.aiAgent;

import lombok.Data;

/**
 * SSE 代理事件载荷，类型决定客户端读取任务、动作或提示信息。
 */
@Data
public class AiAgentEventVO {
    /**
     * 事件类别，决定客户端应将载荷解释为状态变化、动作下发还是失败提示。
     */
    private String type;
    /**
     * 事件所属任务的追踪键，客户端据此忽略重连后收到的其他任务事件。
     */
    private Long taskId;
    /**
     * 与事件类型组合解释的任务或动作状态；未提供状态的提示类事件允许为空。
     */
    private String status;
    /**
     * 仅在动作下发或动作状态变化事件中携带的动作详情，其他事件允许为空。
     */
    private AiAgentActionVO action;
    /**
     * 面向客户端展示的脱敏事件摘要，不能用作状态机判断或泄露页面原文。
     */
    private String summary;
    /**
     * 失败的稳定业务代码，客户端据此选择可恢复提示而非解析自然语言摘要。
     */
    private String failureCode;
}
