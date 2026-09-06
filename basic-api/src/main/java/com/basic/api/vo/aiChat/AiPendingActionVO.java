package com.basic.api.vo.aiChat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 待用户自然语言确认的 AI 助手操作预览，不暴露内部序列化内容。
 */
@Data
@Schema(description = "AI 助手待确认操作")
public class AiPendingActionVO {
    /** 待审批操作 ID，前端仅用于标识当前预览。 */
    private Long id;
    /** 稳定 Action Type，前端不根据该字段执行任何业务分支。 */
    private String actionType;
    /** Handler 生成的通用预览标题。 */
    private String title;
    /** 用户核对的短字段列表。 */
    private List<AiActionPreviewFieldVO> fields;
    /** 可选长文本正文，例如待发布公告内容。 */
    private String content;
    /** 超过该时间后后端拒绝确认旧预览。 */
    private LocalDateTime expiresAt;
}
