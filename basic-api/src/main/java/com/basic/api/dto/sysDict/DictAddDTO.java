package com.basic.api.dto.sysDict;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典新增DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "字典新增请求")
public class DictAddDTO {

    /**
     * 字典编码
     */
    @Schema(description = "唯一字典编码", example = "sys_user_status", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典编码不能为空")
    @Size(max = 100, message = "字典编码长度不能超过100")
    private String dictCode;

    /**
     * 字典名称
     */
    @Schema(description = "字典名称", example = "用户状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称长度不能超过100")
    private String dictName;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "状态：0禁用，1正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;
}
