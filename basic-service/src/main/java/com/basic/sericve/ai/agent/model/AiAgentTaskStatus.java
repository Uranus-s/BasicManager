package com.basic.sericve.ai.agent.model;

/**
 * AI 网页代理任务状态，状态流转必须由服务端状态机统一校验。
 */
public enum AiAgentTaskStatus {
    CREATED,
    PLANNING,
    EXECUTING,
    WAITING_CONFIRMATION,
    PAUSED,
    SUCCEEDED,
    FAILED,
    CANCELED;

    /**
     * 判断任务是否已经进入不可恢复的终态。
     *
     * @return 终态返回 {@code true}
     */
    public boolean terminal() {
        return this == SUCCEEDED || this == FAILED || this == CANCELED;
    }
}
