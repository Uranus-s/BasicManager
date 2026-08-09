package com.basic.api.dto.sysNotice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增公告请求参数，用于提交公告正文及其可见范围。
 */
@Data
@Schema(description = "新增公告请求参数")
public class NoticeAddDTO {

    /**
     * 公告标题。
     */
    @Schema(description = "公告标题", example = "系统维护通知", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题长度不能超过200")
    private String title;

    /**
     * 公告类型，对应公告类型字典项值。
     */
    @Schema(description = "公告类型字典项值", example = "system", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公告类型不能为空")
    @Size(max = 64, message = "公告类型长度不能超过64")
    private String noticeType;

    /**
     * Markdown 格式的公告正文。
     */
    @Schema(description = "Markdown 格式的公告正文", example = "## 维护安排", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公告正文不能为空")
    @Size(max = 100000, message = "公告正文长度不能超过100000")
    private String content;

    /**
     * 公告接收范围，ALL 表示全员，TARGETED 表示按角色或部门定向。
     */
    @Schema(description = "公告接收范围", example = "ALL", allowableValues = {"ALL", "TARGETED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "接收范围不能为空")
    @Pattern(regexp = "ALL|TARGETED", message = "接收范围不正确")
    private String scopeType;

    /**
     * 定向接收公告的角色 ID 列表，仅在 TARGETED 范围下使用。
     */
    @Schema(description = "定向接收公告的角色 ID 列表", example = "[1, 2]")
    private List<Long> roleIds;

    /**
     * 定向接收公告的部门 ID 列表，仅在 TARGETED 范围下使用。
     */
    @Schema(description = "定向接收公告的部门 ID 列表", example = "[3, 4]")
    private List<Long> deptIds;
}
