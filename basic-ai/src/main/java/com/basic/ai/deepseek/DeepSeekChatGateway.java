package com.basic.ai.deepseek;

import com.basic.ai.gateway.AiChatGateway;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

/**
 * DeepSeek 模型网关实现，隔离具体客户端和调用协议。
 */
@Component
@RequiredArgsConstructor
public class DeepSeekChatGateway implements AiChatGateway {

    private final DeepSeekChatClientFactory factory;
    private final MessageChatMemoryAdvisor chatMemoryAdvisor;

    @Override
    public Flux<String> stream(String conversationId, String message) {
        return factory.current().prompt()
                .user(message)
                .advisors(spec -> spec
                        .advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    @Override
    public void testConnection() {
        String content = factory.current().prompt()
                .user("请只回复 OK")
                .call()
                .content();
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ResultEnum.REMOTE_RESPONSE_ERROR);
        }
    }
}
