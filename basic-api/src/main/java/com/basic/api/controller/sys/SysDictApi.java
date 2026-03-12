package com.basic.api.controller.sys;

import com.basic.api.dto.sysDict.DictAddDTO;
import com.basic.api.dto.sysDict.DictQueryDTO;
import com.basic.api.dto.sysDict.DictUpdateDTO;
import com.basic.api.vo.sysDict.DictVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理API接口
 *
 * @author Gas
 */
public interface SysDictApi {

    /**
     * 新增字典
     *
     * @param dto 字典新增DTO
     * @return 操作结果
     */
    @PostMapping
    Result<?> addDict(@Valid @RequestBody DictAddDTO dto);

    /**
     * 更新字典
     *
     * @param dto 字典更新DTO
     * @return 操作结果
     */
    @PutMapping
    Result<?> updateDict(@Valid @RequestBody DictUpdateDTO dto);

    /**
     * 删除字典
     *
     * @param id 字典ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    Result<?> deleteDict(@PathVariable Long id);

    /**
     * 获取字典详情
     *
     * @param id 字典ID
     * @return 字典详情
     */
    @GetMapping("/{id}")
    Result<DictVO> getDictById(@PathVariable Long id);

    /**
     * 分页查询字典列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    Result<PageResult<DictVO>> getDictList(DictQueryDTO dto);

    /**
     * 获取所有字典列表
     *
     * @return 字典列表
     */
    @GetMapping("/all")
    Result<List<DictVO>> getAllDicts();
}
