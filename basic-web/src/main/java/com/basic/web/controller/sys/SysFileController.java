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

    /**
     * 上传文件记录
     *
     * @param fileName 文件名
     * @param filePath 文件路径
     * @param fileSize 文件大小
     * @param fileType 文件类型
     * @param bizType  业务类型
     * @return 文件ID
     */
    @Override
    @PostMapping
    public Result<Long> uploadFile(@RequestParam String fileName, @RequestParam String filePath,
                                    @RequestParam Long fileSize, @RequestParam String fileType,
                                    @RequestParam String bizType) {
        Long fileId = sysFileService.uploadFile(fileName, filePath, fileSize, fileType, bizType);
        return Result.success(fileId);
    }

    /**
     * 删除文件
     *
     * @param id 文件ID
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteFile(@PathVariable Long id) {
        sysFileService.deleteFile(id);
        return Result.success();
    }

    /**
     * 批量删除文件
     *
     * @param ids 文件ID列表
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/batch")
    public Result<?> deleteFiles(@RequestBody List<Long> ids) {
        sysFileService.deleteFiles(ids);
        return Result.success();
    }

    /**
     * 根据ID获取文件详情
     *
     * @param id 文件ID
     * @return 文件信息
     */
    @Override
    @GetMapping("/{id}")
    public Result<FileVO> getFileById(@PathVariable Long id) {
        return Result.success(sysFileService.getFileById(id));
    }

    /**
     * 获取文件列表（分页）
     *
     * @param dto 查询条件
     * @return 文件列表（分页）
     */
    @Override
    @GetMapping("/list")
    public Result<PageResult<FileVO>> getFileList(FileQueryDTO dto) {
        return Result.success(sysFileService.getFileList(dto));
    }
}
