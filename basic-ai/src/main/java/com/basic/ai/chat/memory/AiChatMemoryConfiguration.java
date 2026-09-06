package com.basic.ai.chat.memory;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 聊天记忆配置，使用持久化 Repository 保存每个会话最近 20 条模型上下文。
 */
@Configuration(proxyBeanMethods = false)
public class AiChatMemoryConfiguration {

    static final int MAX_MESSAGES = 20;

    /**
     * 创建固定消息数量的滑动窗口，避免上下文随聊天轮次无限增长。
     */
    @Bean
    public ChatMemory aiChatMemory(ChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(MAX_MESSAGES)
                .build();
    }

    /**
     * 由 Advisor 在模型调用前后自动读取并更新当前会话记忆。
     */
    @Bean
    public MessageChatMemoryAdvisor aiChatMemoryAdvisor(ChatMemory aiChatMemory) {
        return MessageChatMemoryAdvisor.builder(aiChatMemory).build();
    }
}
