package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysFileApi;
import com.basic.api.dto.sysFile.FileQueryDTO;
import com.basic.api.vo.sysFile.FileVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.sericve.sysFile.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件管理 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/file")
@RequiredArgsConstructor
public class SysFileController implements SysFileApi {

    private final ISysFileService sysFileService;

    @Override
    @PostMapping
    public Result<Long> uploadFile(@RequestParam String fileName, @RequestParam String filePath,
                                    @RequestParam Long fileSize, @RequestParam String fileType,
                                    @RequestParam String bizType) {
        Long fileId = sysFileService.uploadFile(fileName, filePath, fileSize, fileType, bizType);
        return Result.success(fileId);
    }

    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteFile(@PathVariable Long id) {
        sysFileService.deleteFile(id);
        return Result.success();
    }

    @Override
    @DeleteMapping("/batch")
    public Result<?> deleteFiles(@RequestBody List<Long> ids) {
        sysFileService.deleteFiles(ids);
        return Result.success();
    }

    @Override
    @GetMapping("/{id}")
    public Result<FileVO> getFileById(@PathVariable Long id) {
        return Result.success(sysFileService.getFileById(id));
    }

    @Override
    @GetMapping("/list")
    public Result<PageResult<FileVO>> getFileList(FileQueryDTO dto) {
        return Result.success(sysFileService.getFileList(dto));
    }
}
