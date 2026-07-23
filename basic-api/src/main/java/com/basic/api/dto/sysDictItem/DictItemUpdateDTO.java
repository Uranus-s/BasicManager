package com.basic.api.dto.sysDictItem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典项更新DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "字典项更新请求")
public class DictItemUpdateDTO {

    /**
     * 字典项ID
     */
    @Schema(description = "字典项ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "字典项ID不能为空")
    private Long id;

    /**
     * 值
     */
    @Schema(description = "字典项存储值", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典项值不能为空")
    @Size(max = 100, message = "字典项值长度不能超过100")
    private String itemValue;

    /**
     * 标签
     */
    @Schema(description = "字典项显示标签", example = "正常", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典项标签不能为空")
    @Size(max = 100, message = "字典项标签长度不能超过100")
    private String itemLabel;

    /**
     * 排序
     */
    @Schema(description = "显示排序，数值越小越靠前", example = "1")
    private Integer sort;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "状态：0禁用，1正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;
}
