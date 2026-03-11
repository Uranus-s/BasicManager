package com.basic.sericve.sysDict.impl;

import com.basic.api.dto.sysDict.DictAddDTO;
import com.basic.api.dto.sysDict.DictQueryDTO;
import com.basic.api.dto.sysDict.DictUpdateDTO;
import com.basic.api.vo.sysDict.DictVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysDict.entity.SysDict;
import com.basic.dao.sysDict.mapper.SysDictMapper;
import com.basic.sericve.sysDict.service.ISysDictService;
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
import java.util.stream.Collectors;

/**
 * <p>
 * 系统字典表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addDict(DictAddDTO dto) {
        // 检查字典编码是否已存在
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictCode, dto.getDictCode());
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultEnum.DATA_ALREADY_EXIST);
        }

        // 创建字典
        SysDict dict = new SysDict();
        BeanUtils.copyProperties(dto, dict);
        if (dict.getStatus() == null) {
            dict.setStatus((byte) 1);
        }
        save(dict);
        return dict.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDict(DictUpdateDTO dto) {
        SysDict dict = getById(dto.getId());
        if (dict == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        BeanUtils.copyProperties(dto, dict);
        updateById(dict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDict(Long id) {
        SysDict dict = getById(id);
        if (dict == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }
        // 逻辑删除
        removeById(id);
    }

    @Override
    public DictVO getDictById(Long id) {
        SysDict dict = getById(id);
        if (dict == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        DictVO vo = new DictVO();
        BeanUtils.copyProperties(dict, vo);
        return vo;
    }

    @Override
    public PageResult<DictVO> getDictList(DictQueryDTO dto) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getDictCode())) {
            wrapper.like(SysDict::getDictCode, dto.getDictCode());
        }
        if (StringUtils.hasText(dto.getDictName())) {
            wrapper.like(SysDict::getDictName, dto.getDictName());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(SysDict::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(SysDict::getCreateTime);

        IPage<SysDict> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<DictVO> voList = new ArrayList<>();
        for (SysDict dict : page.getRecords()) {
            DictVO vo = new DictVO();
            BeanUtils.copyProperties(dict, vo);
            voList.add(vo);
        }

        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), voList);
    }

    @Override
    public List<DictVO> getAllDicts() {
        List<SysDict> dicts = list(new LambdaQueryWrapper<SysDict>()
                .orderByDesc(SysDict::getCreateTime));

        return dicts.stream()
                .map(dict -> {
                    DictVO vo = new DictVO();
                    BeanUtils.copyProperties(dict, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public SysDict getByDictCode(String dictCode) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictCode, dictCode);
        return getOne(wrapper);
    }
}
