package com.basic.api.dto.sysDictItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典项新增DTO
 *
 * @author Gas
 */
@Data
public class DictItemAddDTO {

    /**
     * 字典ID
     */
    @NotNull(message = "字典ID不能为空")
    private Long dictId;

    /**
     * 值
     */
    @NotBlank(message = "字典项值不能为空")
    @Size(max = 100, message = "字典项值长度不能超过100")
    private String itemValue;

    /**
     * 标签
     */
    @NotBlank(message = "字典项标签不能为空")
    @Size(max = 100, message = "字典项标签长度不能超过100")
    private String itemLabel;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 0=禁用 1=正常
     */
    private Byte status;
}
