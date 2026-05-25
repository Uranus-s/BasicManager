package com.basic.sericve.sysFile.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storageType();

    StoredFile store(MultipartFile file, String bizType);

    void delete(String filePath);
}
