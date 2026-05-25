package com.basic.sericve.sysFile.impl;

import com.basic.api.dto.sysFile.FileQueryDTO;
import com.basic.api.vo.sysFile.FileVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysFile.entity.SysFile;
import com.basic.dao.sysFile.mapper.SysFileMapper;
import com.basic.sericve.sysFile.config.FileStorageProperties;
import com.basic.sericve.sysFile.service.ISysFileService;
import com.basic.sericve.sysFile.storage.FileStorageService;
import com.basic.sericve.sysFile.storage.StoredFile;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * 文件表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {

    /**
     * 与存储层保持一致的业务类型校验规则，保证不同存储实现的输入约束一致。
     */
    private static final Pattern BIZ_TYPE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    /**
     * 按 storageType 建立策略映射，便于通过配置选择 local、oss、minio 等实现。
     */
    private final Map<String, FileStorageService> storageServiceMap;

    private final FileStorageProperties fileStorageProperties;

    public SysFileServiceImpl(List<FileStorageService> storageServices, FileStorageProperties fileStorageProperties) {
        this.storageServiceMap = storageServices.stream()
                .collect(Collectors.toMap(FileStorageService::storageType, Function.identity()));
        this.fileStorageProperties = fileStorageProperties;
    }

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
    public FileVO uploadFile(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty() || !StringUtils.hasText(bizType)
                || !BIZ_TYPE_PATTERN.matcher(bizType).matches()) {
            throw new BusinessException(ResultEnum.PARAM_INVALID);
        }

        // 业务层只负责选择策略和保存元数据，具体文件落盘由存储策略处理。
        FileStorageService storageService = storageServiceMap.get(fileStorageProperties.getStorageType());
        if (storageService == null) {
            throw new BusinessException(ResultEnum.PARAM_INVALID);
        }

        StoredFile storedFile;
        try {
            storedFile = storageService.store(file, bizType);
        } catch (RuntimeException ex) {
            throw new BusinessException(ResultEnum.IO_ERROR);
        }

        SysFile sysFile = new SysFile();
        sysFile.setFileName(storedFile.getFileName());
        sysFile.setFilePath(storedFile.getFilePath());
        sysFile.setFileSize(storedFile.getFileSize());
        sysFile.setFileType(storedFile.getFileType());
        sysFile.setBizType(bizType);

        try {
            save(sysFile);
        } catch (RuntimeException ex) {
            // 数据库记录失败时尽量清理已保存文件，避免产生孤立文件。
            storageService.delete(storedFile.getFilePath());
            throw ex;
        }

        FileVO vo = new FileVO();
        BeanUtils.copyProperties(sysFile, vo);
        return vo;
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
