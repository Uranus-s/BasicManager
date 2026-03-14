package com.basic.sericve.sysDictItem.service;

import com.basic.api.dto.sysDictItem.DictItemAddDTO;
import com.basic.api.dto.sysDictItem.DictItemQueryDTO;
import com.basic.api.dto.sysDictItem.DictItemUpdateDTO;
import com.basic.api.vo.sysDictItem.DictItemVO;
import com.basic.common.result.PageResult;
import com.basic.dao.sysDictItem.entity.SysDictItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 字典项表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysDictItemService extends IService<SysDictItem> {

    /**
     * 新增字典项
     *
     * @param dto 字典项新增DTO
     * @return 字典项ID
     */
    Long addDictItem(DictItemAddDTO dto);

    /**
     * 更新字典项
     *
     * @param dto 字典项更新DTO
     */
    void updateDictItem(DictItemUpdateDTO dto);

    /**
     * 删除字典项
     *
     * @param id 字典项ID
     */
    void deleteDictItem(Long id);

    /**
     * 获取字典项详情
     *
     * @param id 字典项ID
     * @return 字典项详情
     */
    DictItemVO getDictItemById(Long id);

    /**
     * 分页查询字典项列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult<DictItemVO> getDictItemList(DictItemQueryDTO dto);

    /**
     * 根据字典ID获取字典项列表
     *
     * @param dictId 字典ID
     * @return 字典项列表
     */
    List<DictItemVO> getDictItemsByDictId(Long dictId);

    /**
     * 根据字典编码获取字典项列表
     *
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    List<DictItemVO> getDictItemsByDictCode(String dictCode);
}
