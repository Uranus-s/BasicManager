package com.basic.api.vo.sysMonitor;

import lombok.Data;

/**
 * 磁盘信息VO
 *
 * @author Gas
 */
@Data
public class DiskInfoVO {

    /**
     * 文件存储名称
     */
    private String name;

    /**
     * 文件存储类型
     */
    private String type;

    /**
     * 总空间，字节
     */
    private Long totalSpaceBytes;

    /**
     * 可用空间，字节
     */
    private Long usableSpaceBytes;

    /**
     * 已用空间，字节
     */
    private Long usedSpaceBytes;

    /**
     * 使用率
     */
    private Double usedPercent;
}
