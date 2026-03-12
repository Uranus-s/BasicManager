package com.basic.api.controller.sys;

import com.basic.api.dto.sysDictItem.DictItemAddDTO;
import com.basic.api.dto.sysDictItem.DictItemQueryDTO;
import com.basic.api.dto.sysDictItem.DictItemUpdateDTO;
import com.basic.api.vo.sysDictItem.DictItemVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典项管理API接口
 *
 * @author Gas
 */
public interface SysDictItemApi {

    /**
     * 新增字典项
     *
     * @param dto 字典项新增DTO
     * @return 操作结果
     */
    @PostMapping
    Result<?> addDictItem(@Valid @RequestBody DictItemAddDTO dto);

    /**
     * 更新字典项
     *
     * @param dto 字典项更新DTO
     * @return 操作结果
     */
    @PutMapping
    Result<?> updateDictItem(@Valid @RequestBody DictItemUpdateDTO dto);

    /**
     * 删除字典项
     *
     * @param id 字典项ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    Result<?> deleteDictItem(@PathVariable Long id);

    /**
     * 获取字典项详情
     *
     * @param id 字典项ID
     * @return 字典项详情
     */
    @GetMapping("/{id}")
    Result<DictItemVO> getDictItemById(@PathVariable Long id);

    /**
     * 分页查询字典项列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    Result<PageResult<DictItemVO>> getDictItemList(DictItemQueryDTO dto);

    /**
     * 根据字典ID获取字典项列表
     *
     * @param dictId 字典ID
     * @return 字典项列表
     */
    @GetMapping("/dict/{dictId}")
    Result<List<DictItemVO>> getDictItemsByDictId(@PathVariable Long dictId);

    /**
     * 根据字典编码获取字典项列表
     *
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    @GetMapping("/code/{dictCode}")
    Result<List<DictItemVO>> getDictItemsByDictCode(@PathVariable String dictCode);
}
