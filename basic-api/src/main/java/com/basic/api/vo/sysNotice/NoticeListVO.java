package com.basic.api.vo.sysNotice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告列表信息，用于管理端列表和用户端可见公告摘要。
 */
@Data
@Schema(description = "公告列表信息")
public class NoticeListVO {

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
     * 公告接收范围。
     */
    @Schema(description = "公告接收范围", example = "ALL", allowableValues = {"ALL", "TARGETED"})
    private String scopeType;

    /**
     * 公告状态。
     */
    @Schema(description = "公告状态", example = "PUBLISHED", allowableValues = {"DRAFT", "PUBLISHED", "WITHDRAWN"})
    private String status;

    /**
     * 公告最近一次发布时间，未发布时为空。
     */
    @Schema(description = "最近一次发布时间", example = "2026-08-09T10:30:00")
    private LocalDateTime publishTime;

    /**
     * 公告最后更新时间。
     */
    @Schema(description = "最后更新时间", example = "2026-08-09T11:00:00")
    private LocalDateTime updateTime;
}
