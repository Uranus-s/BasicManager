package com.basic.web.controller;

import com.basic.api.vo.sysFile.FileVO;
import com.basic.common.result.Result;
import com.basic.sericve.sysFile.service.ISysFileService;
import com.basic.web.controller.sys.SysFileController;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SysFileControllerTest {

    @Test
    void uploadDelegatesMultipartFileToServiceAndReturnsFileVO() {
        ISysFileService sysFileService = mock(ISysFileService.class);
        SysFileController controller = new SysFileController(sysFileService);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "content".getBytes());
        FileVO fileVO = new FileVO();
        fileVO.setId(10L);
        fileVO.setFileName("avatar.png");
        when(sysFileService.uploadFile(file, "avatar")).thenReturn(fileVO);

        Result<FileVO> result = controller.upload(file, "avatar");

        verify(sysFileService).uploadFile(file, "avatar");
        assertThat(result.getData().getId()).isEqualTo(10L);
        assertThat(result.getData().getFileName()).isEqualTo("avatar.png");
    }
}
