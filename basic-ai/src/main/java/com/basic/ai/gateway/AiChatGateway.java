package com.basic.ai.gateway;

import reactor.core.publisher.Flux;

/**
 * 聊天模型调用边界，业务层只依赖文本增量，不感知具体提供商客户端。
 */
public interface AiChatGateway {

    /**
     * 使用指定会话的记忆上下文发送当前消息，并返回纯文本增量。
     */
    Flux<String> stream(String conversationId, String message);

    /**
     * 使用最小请求验证当前模型配置是否可用。
     */
    void testConnection();
}
