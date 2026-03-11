package com.basic.api.dto.sysFile;

import lombok.Data;

/**
 * 文件查询DTO
 *
 * @author Gas
 */
@Data
public class FileQueryDTO {

    /**
     * 文件名（模糊查询）
     */
    private String fileName;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
