package com.basic.sericve.ai.service;

import com.basic.api.vo.aiChat.AiChatHistoryVO;
import com.basic.api.vo.aiChat.AiChatStreamVO;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * AI 聊天业务接口，负责流式生成、历史读取和聊天记忆管理。
 */
public interface IAiChatService {

    Flux<ServerSentEvent<AiChatStreamVO>> stream(Long userId, String message);

    AiChatHistoryVO getMessages(Long userId, Long beforeId, Integer limit);

    void clearMemory(Long userId);

    void testConnection();
}
