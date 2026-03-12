package com.basic.api.controller.sys;

import com.basic.api.dto.sysFile.FileQueryDTO;
import com.basic.api.vo.sysFile.FileVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件管理API接口
 *
 * @author Gas
 */
public interface SysFileApi {

    /**
     * 上传文件记录
     *
     * @param fileName  文件名
     * @param filePath  文件路径
     * @param fileSize  文件大小
     * @param fileType 文件类型
     * @param bizType  业务类型
     * @return 文件ID
     */
    @PostMapping
    Result<Long> uploadFile(@RequestParam String fileName, @RequestParam String filePath,
                            @RequestParam Long fileSize, @RequestParam String fileType,
                            @RequestParam String bizType);

    /**
     * 删除文件
     *
     * @param id 文件ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    Result<?> deleteFile(@PathVariable Long id);

    /**
     * 批量删除文件
     *
     * @param ids 文件ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    Result<?> deleteFiles(@RequestBody List<Long> ids);

    /**
     * 获取文件详情
     *
     * @param id 文件ID
     * @return 文件详情
     */
    @GetMapping("/{id}")
    Result<FileVO> getFileById(@PathVariable Long id);

    /**
     * 分页查询文件列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    Result<PageResult<FileVO>> getFileList(FileQueryDTO dto);
}
