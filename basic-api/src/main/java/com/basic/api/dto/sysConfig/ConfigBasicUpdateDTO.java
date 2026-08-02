package com.basic.api.dto.sysConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 基础设置更新请求，用于维护系统对外展示的基础信息。
 *
 * @author Gas
 */
@Data
@Schema(description = "基础设置更新请求")
public class ConfigBasicUpdateDTO {

    /**
     * 系统名称，用于页面标题等需要展示系统身份的场景。
     */
    @Schema(description = "系统名称", example = "基础管理系统", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "系统名称不能为空")
    private String systemName;
}
