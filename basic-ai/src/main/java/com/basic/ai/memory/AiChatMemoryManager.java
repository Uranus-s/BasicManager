package com.basic.ai.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

/**
 * 向业务层提供模型记忆管理能力，避免业务代码依赖 Spring AI 的具体实现。
 */
@Component
@RequiredArgsConstructor
public class AiChatMemoryManager {

    private final ChatMemory chatMemory;

    /**
     * 清除指定会话的模型上下文，不影响其他用户会话。
     */
    public void clear(String conversationId) {
        chatMemory.clear(conversationId);
    }
}
