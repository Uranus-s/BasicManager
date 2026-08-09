package com.basic.api.controller.ai;

import com.basic.api.dto.aiChat.AiChatMessageQueryDTO;
import com.basic.api.dto.aiChat.AiChatSendDTO;
import com.basic.api.vo.aiChat.AiChatHistoryVO;
import com.basic.api.vo.aiChat.AiChatStreamVO;
import com.basic.common.result.Result;
import com.basic.common.web.annotation.IgnoreResponseAdvice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

/**
 * 登录用户的 AI 聊天接口契约。
 */
@Tag(name = "AI 聊天", description = "DeepSeek 流式聊天与历史管理")
public interface AiChatApi {

    /**
     * 流式生成模型回答。SSE 响应保持原始事件格式，不参与统一 JSON 包装。
     */
    @Operation(summary = "发送 AI 聊天消息")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @IgnoreResponseAdvice
    Flux<ServerSentEvent<AiChatStreamVO>> stream(@Valid @RequestBody AiChatSendDTO dto);

    /**
     * 按游标读取当前登录用户的聊天历史。
     */
    @Operation(summary = "获取 AI 聊天历史")
    @GetMapping("/messages")
    Result<AiChatHistoryVO> getMessages(@Valid AiChatMessageQueryDTO dto);

    /**
     * 逻辑清空当前登录用户的全部聊天消息。
     */
    @Operation(summary = "清空 AI 聊天历史")
    @DeleteMapping("/memory")
    Result<?> clearMemory();
}
