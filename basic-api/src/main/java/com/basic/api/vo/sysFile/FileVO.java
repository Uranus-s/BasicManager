package com.basic.api.vo.sysFile;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件VO
 *
 * @author Gas
 */
@Data
public class FileVO {

    /**
     * 文件ID
     */
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
