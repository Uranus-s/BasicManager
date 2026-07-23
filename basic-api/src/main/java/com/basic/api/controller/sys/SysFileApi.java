package com.basic.api.controller.sys;

import com.basic.api.dto.sysFile.FileQueryDTO;
import com.basic.api.vo.sysFile.FileVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件管理API接口
 *
 * @author Gas
 */
@Tag(name = "文件管理", description = "文件上传、文件记录查询和删除接口")
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
    @Operation(summary = "新增文件记录", description = "根据文件元数据创建文件记录并返回文件ID")
    @PostMapping
    Result<Long> uploadFile(
            @Parameter(description = "文件名", example = "avatar.png", required = true)
            @RequestParam String fileName,
            @Parameter(description = "文件访问路径", example = "/uploads/avatar/avatar.png", required = true)
            @RequestParam String filePath,
            @Parameter(description = "文件大小，单位为字节", example = "102400", required = true)
            @RequestParam Long fileSize,
            @Parameter(description = "文件 MIME 类型", example = "image/png", required = true)
            @RequestParam String fileType,
            @Parameter(description = "文件业务类型", example = "avatar", required = true)
            @RequestParam String bizType);

    /**
     * 上传文件
     *
     * @param file    文件
     * @param bizType 业务类型
     * @return 文件信息
     */
    @Operation(summary = "上传文件", description = "上传文件到当前存储服务并返回文件信息")
    @PostMapping("/upload")
    Result<FileVO> upload(
            @Parameter(description = "待上传文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件业务类型", example = "avatar", required = true)
            @RequestParam("bizType") String bizType);

    /**
     * 删除文件
     *
     * @param id 文件ID
     * @return 操作结果
     */
    @Operation(summary = "删除文件", description = "删除指定文件记录及其存储对象")
    @DeleteMapping("/{id}")
    Result<?> deleteFile(
            @Parameter(description = "文件ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 批量删除文件
     *
     * @param ids 文件ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除文件", description = "批量删除文件记录及其存储对象")
    @DeleteMapping("/batch")
    Result<?> deleteFiles(
            @Parameter(description = "文件ID列表", example = "[1, 2]", required = true)
            @RequestBody List<Long> ids);

    /**
     * 获取文件详情
     *
     * @param id 文件ID
     * @return 文件详情
     */
    @Operation(summary = "获取文件详情", description = "根据文件ID查询文件详情")
    @GetMapping("/{id}")
    Result<FileVO> getFileById(
            @Parameter(description = "文件ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 分页查询文件列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询文件", description = "根据文件名、类型和业务类型分页查询")
    @GetMapping("/list")
    Result<PageResult<FileVO>> getFileList(FileQueryDTO dto);
}
