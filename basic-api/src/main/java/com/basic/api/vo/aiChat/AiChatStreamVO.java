package com.basic.api.vo.aiChat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 流式事件数据，兼容增量内容、停止状态和业务错误。
 */
@Data
@Schema(description = "AI 聊天流式事件")
public class AiChatStreamVO {
    /** 仅 delta 事件使用的助手文本增量。 */
    private String content;
    /** tool_start、tool_result 和 approval_required 使用的展示名称。 */
    private String toolName;
    /** 可对外展示的工具状态摘要，不包含原始参数和结果。 */
    private String summary;
    /** approval_required 事件携带的业务预览。 */
    private AiPendingActionVO pendingAction;
    /** stop 事件标识当前流是否已由用户主动停止。 */
    private Boolean stopped;
    /** error 事件携带的业务错误码。 */
    private Integer code;
    /** stop 或 error 事件携带的提示信息。 */
    private String message;
}
