package com.basic.api.dto.sysConfig;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 参数配置新增DTO
 *
 * @author Gas
 */
@Data
public class ConfigAddDTO {

    /**
     * 参数键
     */
    @NotBlank(message = "参数键不能为空")
    @Size(max = 100, message = "参数键长度不能超过100")
    private String configKey;

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
