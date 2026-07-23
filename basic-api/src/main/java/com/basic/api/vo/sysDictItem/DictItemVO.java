package com.basic.api.vo.sysDictItem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典项VO
 *
 * @author Gas
 */
@Data
@Schema(description = "字典项信息")
public class DictItemVO {

    /**
     * 字典项ID
     */
    @Schema(description = "字典项 ID", example = "1")
    private Long id;

    /**
     * 字典ID
     */
    @Schema(description = "所属字典 ID", example = "1")
    private Long dictId;

    /**
     * 字典项值
     */
    @Schema(description = "字典项值", example = "1")
    private String itemValue;

    /**
     * 字典项标签
     */
    @Schema(description = "字典项标签", example = "正常")
    private String itemLabel;

    /**
     * 排序
     */
    @Schema(description = "显示顺序", example = "1")
    private Integer sort;

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
