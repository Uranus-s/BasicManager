package com.basic.sericve.sysFile.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储策略接口。
 * <p>
 * 业务层只依赖该接口进行文件存储、删除和存储类型识别，后续接入 OSS、MinIO
 * 时新增实现类即可，不需要改 Controller 的上传入口。
 * </p>
 *
 * @author Gas
 */
public interface FileStorageService {

    /**
     * 存储类型标识，需与配置项 basic.file.storage-type 保持一致。
     *
     * @return 存储类型，如 local、oss、minio
     */
    String storageType();

    /**
     * 保存文件并返回可持久化的文件信息。
     *
     * @param file    上传文件
     * @param bizType 业务类型，用于文件归类
     * @return 已存储文件信息
     */
    StoredFile store(MultipartFile file, String bizType);

    /**
     * 删除指定路径对应的文件。
     *
     * @param filePath 文件访问路径或存储路径
     */
    void delete(String filePath);
}
