package com.basic.api.vo.aiAgent;

import lombok.Data;

/**
 * 当前用户的网页代理启用状态和活动任务摘要。
 */
@Data
public class AiAgentStatusVO {
    /**
     * 当前用户是否允许使用代理能力；关闭时客户端不得创建或恢复任务。
     */
    private Boolean enabled;
    /**
     * 当前用户的活动任务摘要；不存在活动任务时为空，不包含原始页面快照。
     */
    private AiAgentTaskVO activeTask;
    /**
     * 当前浏览器实例是否为活动任务绑定页面，用于决定能否继续接收或控制任务。
     */
    private Boolean currentClient;
}
