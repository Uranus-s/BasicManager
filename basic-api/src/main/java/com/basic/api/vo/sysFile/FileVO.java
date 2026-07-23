package com.basic.api.vo.sysFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件VO
 *
 * @author Gas
 */
@Data
@Schema(description = "文件信息")
public class FileVO {

    /**
     * 文件ID
     */
    @Schema(description = "文件 ID", example = "1")
    private Long id;

    /**
     * 文件名
     */
    @Schema(description = "文件名称", example = "avatar.png")
    private String fileName;

    /**
     * 文件路径
     */
    @Schema(description = "文件访问路径", example = "https://example.com/files/avatar.png")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小，单位：字节", example = "102400")
    private Long fileSize;

    /**
     * 文件类型
     */
    @Schema(description = "文件 MIME 类型", example = "image/png")
    private String fileType;

    /**
     * 业务类型
     */
    @Schema(description = "业务类型", example = "avatar")
    private String bizType;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-07-23T10:30:00")
    private LocalDateTime createTime;
}
