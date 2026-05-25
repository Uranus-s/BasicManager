package com.basic.sericve.sysFile.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoredFile {

    private final String fileName;

    private final String filePath;

    private final Long fileSize;

    private final String fileType;
}
