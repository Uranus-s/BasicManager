package com.basic.web.controller.ai;

import com.basic.api.controller.ai.AiChatApi;
import com.basic.api.dto.aiChat.AiChatMessageQueryDTO;
import com.basic.api.dto.aiChat.AiChatSendDTO;
import com.basic.api.vo.aiChat.AiChatHistoryVO;
import com.basic.api.vo.aiChat.AiChatStreamVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.Result;
import com.basic.common.result.ResultEnum;
import com.basic.common.web.annotation.IgnoreResponseAdvice;
import com.basic.core.log.annotation.OperateLog;
import com.basic.core.security.model.LoginUser;
import com.basic.sericve.ai.chat.service.IAiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 登录用户 AI 聊天入口。用户 ID 只从认证上下文获取，避免跨用户读取或清空消息。
 */
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class AiChatController implements AiChatApi {

    private final IAiChatService aiChatService;

    @Override
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @IgnoreResponseAdvice
    public Flux<ServerSentEvent<AiChatStreamVO>> stream(@Valid @RequestBody AiChatSendDTO dto) {
        return aiChatService.stream(currentLoginUser(), dto.getMessage());
    }

    @Override
    @GetMapping("/messages")
    public Result<AiChatHistoryVO> getMessages(@Valid AiChatMessageQueryDTO dto) {
        Long userId = currentUserId();
        return Result.success(aiChatService.getMessages(userId, dto.getBeforeId(), dto.getLimit()));
    }

    @Override
    @DeleteMapping("/memory")
    @OperateLog(module = "AI聊天", method = "清空聊天记录")
    public Result<?> clearMemory() {
        aiChatService.clearMemory(currentUserId());
        return Result.success();
    }

    private Long currentUserId() {
        return currentLoginUser().getUserId();
    }

    /**
     * 从 Security 上下文读取完整登录用户，用户 ID 和权限均不接受请求参数覆盖。
     */
    private LoginUser currentLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoginUser loginUser)
                || loginUser.getUserId() == null) {
            throw new BusinessException(ResultEnum.UNAUTHORIZED);
        }
        return loginUser;
    }
}
