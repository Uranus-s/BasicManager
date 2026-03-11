package com.basic.sericve.sysFile.impl;

import com.basic.api.dto.sysFile.FileQueryDTO;
import com.basic.api.vo.sysFile.FileVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysFile.entity.SysFile;
import com.basic.dao.sysFile.mapper.SysFileMapper;
import com.basic.sericve.sysFile.service.ISysFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 文件表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadFile(String fileName, String filePath, Long fileSize, String fileType, String bizType) {
        // 创建文件记录
        SysFile file = new SysFile();
        file.setFileName(fileName);
        file.setFilePath(filePath);
        file.setFileSize(fileSize);
        file.setFileType(fileType);
        file.setBizType(bizType);
        save(file);
        return file.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long id) {
        SysFile file = getById(id);
        if (file == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }
        // 物理删除（文件记录通常直接删除）
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFiles(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            removeByIds(ids);
        }
    }

    @Override
    public FileVO getFileById(Long id) {
        SysFile file = getById(id);
        if (file == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        FileVO vo = new FileVO();
        BeanUtils.copyProperties(file, vo);
        return vo;
    }

    @Override
    public PageResult<FileVO> getFileList(FileQueryDTO dto) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getFileName())) {
            wrapper.like(SysFile::getFileName, dto.getFileName());
        }
        if (StringUtils.hasText(dto.getBizType())) {
            wrapper.eq(SysFile::getBizType, dto.getBizType());
        }
        if (StringUtils.hasText(dto.getFileType())) {
            wrapper.eq(SysFile::getFileType, dto.getFileType());
        }
        wrapper.orderByDesc(SysFile::getCreateTime);

        IPage<SysFile> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<FileVO> voList = new ArrayList<>();
        for (SysFile file : page.getRecords()) {
            FileVO vo = new FileVO();
            BeanUtils.copyProperties(file, vo);
            voList.add(vo);
        }

        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), voList);
    }
}
