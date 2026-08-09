package com.basic.api.vo.sysNotice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户端公告详情，仅暴露当前用户阅读公告所需的信息。
 */
@Data
@Schema(description = "用户端公告详情")
public class NoticeDetailVO {

    /**
     * 公告 ID。
     */
    @Schema(description = "公告 ID", example = "1")
    private Long id;

    /**
     * 公告标题。
     */
    @Schema(description = "公告标题", example = "系统维护通知")
    private String title;

    /**
     * 公告类型字典项值。
     */
    @Schema(description = "公告类型字典项值", example = "system")
    private String noticeType;

    /**
     * Markdown 格式的公告正文。
     */
    @Schema(description = "Markdown 格式的公告正文", example = "## 维护安排")
    private String content;

    /**
     * 公告最近一次发布时间。
     */
    @Schema(description = "发布时间", example = "2026-08-09T10:30:00")
    private LocalDateTime publishTime;
}
