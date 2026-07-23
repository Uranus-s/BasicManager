package com.basic.api.dto.sysConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 参数配置更新DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "参数配置更新请求")
public class ConfigUpdateDTO {

    /**
     * 参数ID
     */
    @Schema(description = "参数配置ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "参数ID不能为空")
    private Long id;

    /**
     * 参数值
     */
    @Schema(description = "新的参数值", example = "ChangeMe123!", requiredMode = Schema.RequiredMode.REQUIRED)
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
