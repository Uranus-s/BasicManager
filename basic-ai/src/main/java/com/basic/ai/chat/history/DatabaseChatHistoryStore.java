package com.basic.ai.chat.history;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.dao.ai.chat.entity.AiChatMessage;
import com.basic.dao.ai.chat.mapper.AiChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI 聊天业务历史存储，负责完整记录、部分回答状态和游标分页。
 */
@Component
@RequiredArgsConstructor
public class DatabaseChatHistoryStore {

    private final AiChatMessageMapper mapper;

    /**
     * 在模型运行前保存用户消息，使助手运行可以关联稳定的触发消息 ID。
     *
     * @return 数据库生成的消息主键
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveUserMessage(Long userId, String userText) {
        return insert(userId, "USER", userText, false);
    }

    /**
     * 保存已产生的助手回答；首个 token 前失败时不创建空消息。
     *
     * @param partial 是否因取消或异常只保存了部分内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAssistantMessage(Long userId, String assistantText, boolean partial) {
        if (StringUtils.hasText(assistantText)) {
            insert(userId, "ASSISTANT", assistantText, partial);
        }
    }

    /**
     * 按消息 ID 向前翻页，返回顺序统一为从旧到新，便于调用方直接追加或前插。
     */
    public AiHistoryPage getHistory(Long userId, Long beforeId, int limit) {
        int pageSize = Math.min(Math.max(limit, 1), 100);
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getUserId, userId)
                .lt(beforeId != null, AiChatMessage::getId, beforeId)
                .orderByDesc(AiChatMessage::getId)
                .last("LIMIT " + (pageSize + 1));
        List<AiChatMessage> rows = mapper.selectList(wrapper);
        boolean hasMore = rows.size() > pageSize;
        List<AiChatMessage> page = new ArrayList<>(rows.subList(0, Math.min(rows.size(), pageSize)));
        Collections.reverse(page);
        List<AiStoredMessage> messages = page.stream().map(this::toStoredMessage).toList();
        Long nextBeforeId = hasMore && !messages.isEmpty() ? messages.getFirst().id() : null;
        return new AiHistoryPage(messages, hasMore, nextBeforeId);
    }

    /**
     * 清除指定用户的完整业务聊天历史。
     */
    public void clear(Long userId) {
        mapper.delete(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getUserId, userId));
    }

    /** 统一写入角色受限的聊天消息，并返回数据库自增主键。 */
    private Long insert(Long userId, String role, String content, boolean partial) {
        if (!("USER".equals(role) || "ASSISTANT".equals(role))) {
            throw new BusinessException(ResultEnum.PARAM_INVALID);
        }
        AiChatMessage entity = new AiChatMessage();
        entity.setUserId(userId);
        entity.setRole(role);
        entity.setContent(content);
        entity.setPartial((byte) (partial ? 1 : 0));
        mapper.insert(entity);
        return entity.getId();
    }

    private AiStoredMessage toStoredMessage(AiChatMessage entity) {
        return new AiStoredMessage(
                entity.getId(),
                entity.getRole(),
                entity.getContent(),
                entity.getPartial() != null && entity.getPartial() == 1,
                entity.getCreateTime());
    }
}
