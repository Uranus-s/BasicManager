package com.basic.api.dto.sysConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 参数配置新增DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "参数配置新增请求")
public class ConfigAddDTO {

    /**
     * 参数键
     */
    @Schema(description = "唯一参数键", example = "system.user.defaultPassword", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数键不能为空")
    @Size(max = 100, message = "参数键长度不能超过100")
    private String configKey;

    /**
     * 参数值
     */
    @Schema(description = "参数值", example = "ChangeMe123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数值不能为空")
    @Size(max = 255, message = "参数值长度不能超过255")
    private String configValue;

    /**
     * 备注
     */
    @Schema(description = "参数说明", example = "新用户默认密码")
    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}
