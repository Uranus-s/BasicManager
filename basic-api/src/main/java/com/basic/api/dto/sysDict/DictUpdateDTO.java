package com.basic.api.dto.sysDict;

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
public class DictUpdateDTO {

    /**
     * 字典ID
     */
    @NotNull(message = "字典ID不能为空")
    private Long id;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称长度不能超过100")
    private String dictName;

    /**
     * 状态 0=禁用 1=正常
     */
    private Byte status;
}
