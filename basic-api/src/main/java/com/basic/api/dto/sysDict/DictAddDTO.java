package com.basic.api.dto.sysDict;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典新增DTO
 *
 * @author Gas
 */
@Data
public class DictAddDTO {

    /**
     * 字典编码
     */
    @NotBlank(message = "字典编码不能为空")
    @Size(max = 100, message = "字典编码长度不能超过100")
    private String dictCode;

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
