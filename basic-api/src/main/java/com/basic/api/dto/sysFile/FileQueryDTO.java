package com.basic.api.dto.sysFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件查询DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "文件分页查询条件")
public class FileQueryDTO {

    /**
     * 文件名（模糊查询）
     */
    @Schema(description = "文件名，支持模糊匹配", example = "avatar")
    private String fileName;

    /**
     * 业务类型
     */
    @Schema(description = "文件业务类型", example = "avatar")
    private String bizType;

    /**
     * 文件类型
     */
    @Schema(description = "文件 MIME 类型", example = "image/png")
    private String fileType;

    /**
     * 当前页码（从1开始）
     */
    @Schema(description = "当前页码，从1开始", example = "1", defaultValue = "1", minimum = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页记录数", example = "10", defaultValue = "10", minimum = "1")
    private Integer pageSize = 10;
}
