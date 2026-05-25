package com.basic.sericve.sysFile.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件存储后的结果对象。
 * <p>
 * 该对象用于在存储策略和业务服务之间传递文件元数据，字段会被写入 sys_file 表。
 * </p>
 *
 * @author Gas
 */
@Getter
@AllArgsConstructor
public class StoredFile {

    /**
     * 原始文件名，用于展示和记录，不直接作为磁盘文件名。
     */
    private final String fileName;

    /**
     * 文件访问路径。
     */
    private final String filePath;

    /**
     * 文件大小，单位字节。
     */
    private final Long fileSize;

    /**
     * 文件 MIME 类型。
     */
    private final String fileType;
}
