package com.basic.api.dto.sysNotice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 管理端公告分页查询参数，可按状态检索全部公告。
 */
@Data
@Schema(description = "管理端公告分页查询参数")
public class NoticeQueryDTO {

    /**
     * 公告标题，支持模糊匹配。
     */
    @Schema(description = "公告标题，支持模糊匹配", example = "维护")
    private String title;

    /**
     * 公告类型字典项值。
     */
    @Schema(description = "公告类型字典项值", example = "system")
    private String noticeType;

    /**
     * 公告状态。
     */
    @Schema(description = "公告状态", example = "PUBLISHED", allowableValues = {"DRAFT", "PUBLISHED", "WITHDRAWN"})
    private String status;

    /**
     * 当前页码，从 1 开始。
     */
    @Schema(description = "当前页码，从 1 开始", example = "1", defaultValue = "1", minimum = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页记录数，最大为 50。
     */
    @Schema(description = "每页记录数", example = "10", defaultValue = "10", minimum = "1", maximum = "50")
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 50, message = "每页大小不能超过50")
    private Integer pageSize = 10;
}
