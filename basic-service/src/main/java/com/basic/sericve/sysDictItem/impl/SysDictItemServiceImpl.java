package com.basic.sericve.sysDictItem.impl;

import com.basic.api.dto.sysDictItem.DictItemAddDTO;
import com.basic.api.dto.sysDictItem.DictItemQueryDTO;
import com.basic.api.dto.sysDictItem.DictItemUpdateDTO;
import com.basic.api.vo.sysDictItem.DictItemVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysDict.entity.SysDict;
import com.basic.dao.sysDictItem.entity.SysDictItem;
import com.basic.dao.sysDictItem.mapper.SysDictItemMapper;
import com.basic.sericve.sysDict.service.ISysDictService;
import com.basic.sericve.sysDictItem.service.ISysDictItemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典项表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
@RequiredArgsConstructor
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements ISysDictItemService {

    private final ISysDictService sysDictService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addDictItem(DictItemAddDTO dto) {
        // 检查字典是否存在
        SysDict dict = sysDictService.getById(dto.getDictId());
        if (dict == null) {
            throw new BusinessException(ResultEnum.RELATION_DATA_NOT_EXIST);
        }

        // 创建字典项
        SysDictItem item = new SysDictItem();
        BeanUtils.copyProperties(dto, item);
        if (item.getSort() == null) {
            item.setSort(0);
        }
        if (item.getStatus() == null) {
            item.setStatus((byte) 1);
        }
        save(item);
        return item.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictItem(DictItemUpdateDTO dto) {
        SysDictItem item = getById(dto.getId());
        if (item == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        BeanUtils.copyProperties(dto, item);
        updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictItem(Long id) {
        SysDictItem item = getById(id);
        if (item == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }
        // 逻辑删除
        removeById(id);
    }

    @Override
    public DictItemVO getDictItemById(Long id) {
        SysDictItem item = getById(id);
        if (item == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        DictItemVO vo = new DictItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    @Override
    public PageResult<DictItemVO> getDictItemList(DictItemQueryDTO dto) {
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        if (dto.getDictId() != null) {
            wrapper.eq(SysDictItem::getDictId, dto.getDictId());
        }
        if (StringUtils.hasText(dto.getItemValue())) {
            wrapper.like(SysDictItem::getItemValue, dto.getItemValue());
        }
        if (StringUtils.hasText(dto.getItemLabel())) {
            wrapper.like(SysDictItem::getItemLabel, dto.getItemLabel());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(SysDictItem::getStatus, dto.getStatus());
        }
        wrapper.orderByAsc(SysDictItem::getSort).orderByDesc(SysDictItem::getCreateTime);

        IPage<SysDictItem> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<DictItemVO> voList = new ArrayList<>();
        for (SysDictItem item : page.getRecords()) {
            DictItemVO vo = new DictItemVO();
            BeanUtils.copyProperties(item, vo);
            voList.add(vo);
        }

        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), voList);
    }

    @Override
    public List<DictItemVO> getDictItemsByDictId(Long dictId) {
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictItem::getDictId, dictId);
        wrapper.eq(SysDictItem::getStatus, (byte) 1);
        wrapper.orderByAsc(SysDictItem::getSort);
        List<SysDictItem> items = list(wrapper);

        return items.stream()
                .map(item -> {
                    DictItemVO vo = new DictItemVO();
                    BeanUtils.copyProperties(item, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DictItemVO> getDictItemsByDictCode(String dictCode) {
        SysDict dict = sysDictService.getByDictCode(dictCode);
        if (dict == null) {
            return new ArrayList<>();
        }
        return getDictItemsByDictId(dict.getId());
    }
}
