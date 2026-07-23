package com.basic.api.vo.sysConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参数配置VO
 *
 * @author Gas
 */
@Data
@Schema(description = "系统配置信息")
public class ConfigVO {

    /**
     * 参数ID
     */
    @Schema(description = "配置 ID", example = "1")
    private Long id;

    /**
     * 参数键
     */
    @Schema(description = "配置键", example = "system.site.name")
    private String configKey;

    /**
     * 参数值
     */
    @Schema(description = "配置值", example = "基础管理系统")
    private String configValue;

    /**
     * 备注
     */
    @Schema(description = "配置说明", example = "系统显示名称")
    private String remark;

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
