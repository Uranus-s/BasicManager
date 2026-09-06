package com.basic.api.vo.aiChat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * AI 聊天历史游标分页结果。
 */
@Data
@Schema(description = "AI 聊天历史")
public class AiChatHistoryVO {
    private List<AiChatMessageVO> messages;
    private Long nextBeforeId;
    private Boolean hasMore;
    /** 当前用户唯一有效的待审批操作；不存在时为 null。 */
    private AiPendingActionVO pendingAction;
}
