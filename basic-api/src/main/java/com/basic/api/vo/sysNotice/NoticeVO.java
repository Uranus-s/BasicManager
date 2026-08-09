package com.basic.api.vo.sysNotice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端公告详情，包含正文、可见目标和并发更新所需版本。
 */
@Data
@Schema(description = "管理端公告详情")
public class NoticeVO {

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
    @Schema(description = "公告接收范围", example = "TARGETED", allowableValues = {"ALL", "TARGETED"})
    private String scopeType;

    /**
     * 公告状态。
     */
    @Schema(description = "公告状态", example = "DRAFT", allowableValues = {"DRAFT", "PUBLISHED", "WITHDRAWN"})
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

    /**
     * Markdown 格式的公告正文。
     */
    @Schema(description = "Markdown 格式的公告正文", example = "## 维护安排")
    private String content;

    /**
     * 数据版本，用于公告更新时的乐观锁校验。
     */
    @Schema(description = "数据版本", example = "1")
    private Integer version;

    /**
     * 定向接收公告的角色 ID 列表。
     */
    @Schema(description = "定向接收公告的角色 ID 列表", example = "[1, 2]")
    private List<Long> roleIds;

    /**
     * 定向接收公告的部门 ID 列表。
     */
    @Schema(description = "定向接收公告的部门 ID 列表", example = "[3, 4]")
    private List<Long> deptIds;

    /**
     * 公告创建时间。
     */
    @Schema(description = "创建时间", example = "2026-08-09T09:00:00")
    private LocalDateTime createTime;
}
