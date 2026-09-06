package com.basic.ai.chat.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;

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

    /**
     * 追加一轮已由确定性业务代码处理的对话，避免后续模型仍认为操作处于待确认状态。
     *
     * <p>仅用于确认、取消等绕过主模型的确定性回复；用户消息和助手回复作为一个列表
     * 一次写入，以保持同一会话中的角色顺序。</p>
     */
    public void addExchange(String conversationId, String userMessage, String assistantMessage) {
        chatMemory.add(conversationId, List.of(
                new UserMessage(userMessage),
                new AssistantMessage(assistantMessage)));
    }
}
