package com.basic.sericve.ai.chat.service;

import com.basic.api.vo.aiChat.AiChatHistoryVO;
import com.basic.api.vo.aiChat.AiChatStreamVO;
import com.basic.core.security.model.LoginUser;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * AI 聊天业务接口，负责流式生成、历史读取和聊天记忆管理。
 */
public interface IAiChatService {

    /**
     * 使用完整登录用户启动流式聊天，以便业务层构造权限受限的助手工具集。
     */
    Flux<ServerSentEvent<AiChatStreamVO>> stream(LoginUser loginUser, String message);

    AiChatHistoryVO getMessages(Long userId, Long beforeId, Integer limit);

    void clearMemory(Long userId);

    void testConnection();
}
