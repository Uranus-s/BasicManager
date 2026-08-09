package com.basic.ai.deepseek;

import com.basic.ai.config.AiModelConfigProvider;
import com.basic.ai.event.AiConfigChangedEvent;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

/**
 * DeepSeek 客户端运行时工厂。
 *
 * <p>API Key 由上层配置端口按需提供。客户端会缓存复用，配置事务提交后
 * 清除缓存；任何日志和异常都不得携带 Key。</p>
 */
@Component
@RequiredArgsConstructor
public class DeepSeekChatClientFactory {

    private final AiModelConfigProvider configProvider;

    private volatile ChatClient cachedClient;

    /**
     * 获取当前配置对应的客户端，使用双重检查保证并发下只创建一次。
     */
    public ChatClient current() {
        ChatClient client = cachedClient;
        if (client != null) {
            return client;
        }
        synchronized (this) {
            client = cachedClient;
            if (client == null) {
                client = buildClient();
                cachedClient = client;
            }
            return client;
        }
    }

    /**
     * 配置事务提交后丢弃旧客户端，下一次调用按新配置重建。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAiConfigChanged(AiConfigChangedEvent event) {
        cachedClient = null;
    }

    private ChatClient buildClient() {
        String apiKey = configProvider.getDeepSeekApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(ResultEnum.AI_CONFIG_MISSING);
        }
        DeepSeekApi api = DeepSeekApi.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(apiKey)
                .build();
        DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                .model(DeepSeekApi.ChatModel.DEEPSEEK_V4_FLASH)
                .maxTokens(4096)
                .build();
        DeepSeekChatModel model = DeepSeekChatModel.builder()
                .deepSeekApi(api)
                .options(options)
                .build();
        return ChatClient.create(model);
    }
}
