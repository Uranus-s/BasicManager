package com.basic.api.dto.sysNotice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 修改公告请求参数，携带数据版本以防止并发覆盖。
 */
@Data
@Schema(description = "修改公告请求参数")
public class NoticeUpdateDTO {

    /**
     * 待修改的公告 ID。
     */
    @Schema(description = "公告 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "公告ID不能为空")
    private Long id;

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
    @Schema(description = "公告接收范围", example = "TARGETED", allowableValues = {"ALL", "TARGETED"}, requiredMode = Schema.RequiredMode.REQUIRED)
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

    /**
     * 客户端读取公告时获得的数据版本，用于乐观锁校验。
     */
    @Schema(description = "数据版本", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数据版本不能为空")
    private Integer version;
}
