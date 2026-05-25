package com.basic.web.sys;

import com.basic.sericve.sysFile.config.FileStorageProperties;
import com.basic.sericve.sysFile.storage.LocalFileStorageServiceImpl;
import com.basic.sericve.sysFile.storage.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void storeWritesFileUnderBizTypeDateDirectoryWithoutUsingOriginalNameAsDiskName() throws Exception {
        FileStorageProperties properties = new FileStorageProperties();
        properties.getLocal().setBasePath(tempDir.toString());
        properties.getLocal().setUrlPrefix("/uploads");
        LocalFileStorageServiceImpl storageService = new LocalFileStorageServiceImpl(properties);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "file-content".getBytes()
        );

        StoredFile storedFile = storageService.store(file, "avatar");

        assertThat(storedFile.getFileName()).isEqualTo("avatar.png");
        assertThat(storedFile.getFileSize()).isEqualTo(12L);
        assertThat(storedFile.getFileType()).isEqualTo("image/png");
        assertThat(storedFile.getFilePath()).startsWith("/uploads/avatar/");
        assertThat(storedFile.getFilePath()).endsWith(".png");
        assertThat(storedFile.getFilePath()).doesNotEndWith("/avatar.png");
        Path savedPath = tempDir.resolve(storedFile.getFilePath().replaceFirst("^/uploads/", ""));
        assertThat(Files.readString(savedPath)).isEqualTo("file-content");
    }

    @Test
    void storeRejectsInvalidBizType() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.getLocal().setBasePath(tempDir.toString());
        LocalFileStorageServiceImpl storageService = new LocalFileStorageServiceImpl(properties);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "content".getBytes());

        assertThatThrownBy(() -> storageService.store(file, "../avatar"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bizType");
    }
}
