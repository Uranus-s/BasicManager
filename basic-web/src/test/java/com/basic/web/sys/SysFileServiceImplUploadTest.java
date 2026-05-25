package com.basic.web.sys;

import com.basic.api.vo.sysFile.FileVO;
import com.basic.common.exception.BusinessException;
import com.basic.dao.sysFile.entity.SysFile;
import com.basic.dao.sysFile.mapper.SysFileMapper;
import com.basic.sericve.sysFile.config.FileStorageProperties;
import com.basic.sericve.sysFile.impl.SysFileServiceImpl;
import com.basic.sericve.sysFile.storage.FileStorageService;
import com.basic.sericve.sysFile.storage.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysFileServiceImplUploadTest {

    @Mock
    private SysFileMapper sysFileMapper;

    @Mock
    private FileStorageService storageService;

    @Test
    void uploadFileStoresFileSavesMetadataAndReturnsFileVO() {
        FileStorageProperties properties = new FileStorageProperties();
        when(storageService.storageType()).thenReturn("local");
        SysFileServiceImpl service = new SysFileServiceImpl(List.of(storageService), properties);
        ReflectionTestUtils.setField(service, "baseMapper", sysFileMapper);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "content".getBytes());
        when(storageService.store(file, "avatar")).thenReturn(
                new StoredFile("avatar.png", "/uploads/avatar/2026/05/25/file.png", 7L, "image/png")
        );
        when(sysFileMapper.insert(any(SysFile.class))).thenAnswer(invocation -> {
            SysFile sysFile = invocation.getArgument(0);
            sysFile.setId(99L);
            return 1;
        });

        FileVO result = service.uploadFile(file, "avatar");

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getFileName()).isEqualTo("avatar.png");
        assertThat(result.getFilePath()).isEqualTo("/uploads/avatar/2026/05/25/file.png");
        assertThat(result.getFileSize()).isEqualTo(7L);
        assertThat(result.getFileType()).isEqualTo("image/png");
        assertThat(result.getBizType()).isEqualTo("avatar");
        ArgumentCaptor<SysFile> fileCaptor = ArgumentCaptor.forClass(SysFile.class);
        verify(sysFileMapper).insert(fileCaptor.capture());
        assertThat(fileCaptor.getValue().getFileName()).isEqualTo("avatar.png");
        assertThat(fileCaptor.getValue().getFilePath()).isEqualTo("/uploads/avatar/2026/05/25/file.png");
    }

    @Test
    void uploadFileRejectsEmptyFile() {
        FileStorageProperties properties = new FileStorageProperties();
        when(storageService.storageType()).thenReturn("local");
        SysFileServiceImpl service = new SysFileServiceImpl(List.of(storageService), properties);
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> service.uploadFile(file, "docs"))
                .isInstanceOf(BusinessException.class);
    }
}
