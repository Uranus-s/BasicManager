package com.basic.sericve.sysFile.service;

import com.basic.api.dto.sysFile.FileQueryDTO;
import com.basic.api.vo.sysFile.FileVO;
import com.basic.common.result.PageResult;
import com.basic.dao.sysFile.entity.SysFile;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 文件表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysFileService extends IService<SysFile> {

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
    Long uploadFile(String fileName, String filePath, Long fileSize, String fileType, String bizType);

    /**
     * 删除文件
     *
     * @param id 文件ID
     */
    void deleteFile(Long id);

    /**
     * 批量删除文件
     *
     * @param ids 文件ID列表
     */
    void deleteFiles(List<Long> ids);

    /**
     * 获取文件详情
     *
     * @param id 文件ID
     * @return 文件详情
     */
    FileVO getFileById(Long id);

    /**
     * 分页查询文件列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult<FileVO> getFileList(FileQueryDTO dto);
}
