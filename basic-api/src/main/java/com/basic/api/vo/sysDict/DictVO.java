package com.basic.api.vo.sysDict;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典详情VO
 *
 * @author Gas
 */
@Data
@Schema(description = "字典信息")
public class DictVO {

    /**
     * 字典ID
     */
    @Schema(description = "字典 ID", example = "1")
    private Long id;

    /**
     * 字典编码
     */
    @Schema(description = "字典编码", example = "user_status")
    private String dictCode;

    /**
     * 字典名称
     */
    @Schema(description = "字典名称", example = "用户状态")
    private String dictName;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "状态：0-禁用，1-正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-07-23T10:30:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2026-07-23T10:30:00")
    private LocalDateTime updateTime;
}
