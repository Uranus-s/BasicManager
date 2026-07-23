package com.basic.api.dto.sysDict;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典更新DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "字典更新请求")
public class DictUpdateDTO {

    /**
     * 字典ID
     */
    @Schema(description = "字典ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "字典ID不能为空")
    private Long id;

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
