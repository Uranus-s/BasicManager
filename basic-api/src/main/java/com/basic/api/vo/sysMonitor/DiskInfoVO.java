package com.basic.api.vo.sysMonitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 磁盘信息VO
 *
 * @author Gas
 */
@Data
@Schema(description = "磁盘使用信息")
public class DiskInfoVO {

    /**
     * 文件存储名称
     */
    @Schema(description = "磁盘名称", example = "C:\\")
    private String name;

    /**
     * 文件存储类型
     */
    @Schema(description = "文件系统类型", example = "NTFS")
    private String type;

    /**
     * 总空间，字节
     */
    @Schema(description = "总空间，单位：字节", example = "512000000000")
    private Long totalSpaceBytes;

    /**
     * 可用空间，字节
     */
    @Schema(description = "可用空间，单位：字节", example = "256000000000")
    private Long usableSpaceBytes;

    /**
     * 已用空间，字节
     */
    @Schema(description = "已用空间，单位：字节", example = "256000000000")
    private Long usedSpaceBytes;

    /**
     * 使用率
     */
    @Schema(description = "空间使用率，单位：百分比", example = "50.0")
    private Double usedPercent;
}
