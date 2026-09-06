package com.basic.ai.assistant.gateway;

import com.basic.ai.assistant.model.AssistantSignal;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 统一 AI 助手模型调用边界，业务层只提供当前 Run 可见的工具集合。
 */
public interface AiAssistantGateway {

    /**
     * 启动一次统一 AI 助手流式调用。工具回调列表可以为空；为空时模型执行纯聊天，
     * 仍然沿用同一调用链路，不具备读取或变更系统数据的能力。
     *
     * @param runId 本次运行的日志关联标识，当前使用触发消息 ID
     * @param conversationId 聊天记忆隔离键，当前按用户维度隔离
     * @param message 已完成业务侧规范化的用户消息
     * @param toolCallbacks 根据当前用户权限动态生成的工具白名单
     * @return 文本增量、工具进度和待审批操作组成的有序信号流
     */
    Flux<AssistantSignal> stream(Long runId,
                             String conversationId,
                             String message,
                             List<ToolCallback> toolCallbacks);

    /**
     * 使用无工具的最小请求验证当前 AI 助手模型配置是否可用。
     */
    void testConnection();
}
