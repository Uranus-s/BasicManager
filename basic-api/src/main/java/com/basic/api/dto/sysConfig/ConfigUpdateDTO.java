package com.basic.api.dto.sysConfig;

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
public class ConfigUpdateDTO {

    /**
     * 参数ID
     */
    @NotNull(message = "参数ID不能为空")
    private Long id;

    /**
     * 参数值
     */
    @NotBlank(message = "参数值不能为空")
    @Size(max = 255, message = "参数值长度不能超过255")
    private String configValue;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}
