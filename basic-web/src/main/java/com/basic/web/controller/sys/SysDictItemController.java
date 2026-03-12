package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysDictItemApi;
import com.basic.api.dto.sysDictItem.DictItemAddDTO;
import com.basic.api.dto.sysDictItem.DictItemQueryDTO;
import com.basic.api.dto.sysDictItem.DictItemUpdateDTO;
import com.basic.api.vo.sysDictItem.DictItemVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.sericve.sysDictItem.service.ISysDictItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典项管理 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/dict/item")
@RequiredArgsConstructor
public class SysDictItemController implements SysDictItemApi {

    private final ISysDictItemService sysDictItemService;

    /**
     * 新增字典项
     *
     * @param dto 字典项信息
     * @return 操作结果
     */
    @Override
    @PostMapping
    public Result<?> addDictItem(@Valid @RequestBody DictItemAddDTO dto) {
        sysDictItemService.addDictItem(dto);
        return Result.success();
    }

    /**
     * 更新字典项信息
     *
     * @param dto 字典项信息
     * @return 操作结果
     */
    @Override
    @PutMapping
    public Result<?> updateDictItem(@Valid @RequestBody DictItemUpdateDTO dto) {
        sysDictItemService.updateDictItem(dto);
        return Result.success();
    }

    /**
     * 删除字典项
     *
     * @param id 字典项ID
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteDictItem(@PathVariable Long id) {
        sysDictItemService.deleteDictItem(id);
        return Result.success();
    }

    /**
     * 根据ID获取字典项详情
     *
     * @param id 字典项ID
     * @return 字典项信息
     */
    @Override
    @GetMapping("/{id}")
    public Result<DictItemVO> getDictItemById(@PathVariable Long id) {
        return Result.success(sysDictItemService.getDictItemById(id));
    }

    /**
     * 获取字典项列表（分页）
     *
     * @param dto 查询条件
     * @return 字典项列表（分页）
     */
    @Override
    @GetMapping("/list")
    public Result<PageResult<DictItemVO>> getDictItemList(DictItemQueryDTO dto) {
        return Result.success(sysDictItemService.getDictItemList(dto));
    }

    /**
     * 根据字典ID获取字典项列表
     *
     * @param dictId 字典ID
     * @return 字典项列表
     */
    @Override
    @GetMapping("/dict/{dictId}")
    public Result<List<DictItemVO>> getDictItemsByDictId(@PathVariable Long dictId) {
        return Result.success(sysDictItemService.getDictItemsByDictId(dictId));
    }

    /**
     * 根据字典编码获取字典项列表
     *
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    @Override
    @GetMapping("/code/{dictCode}")
    public Result<List<DictItemVO>> getDictItemsByDictCode(@PathVariable String dictCode) {
        return Result.success(sysDictItemService.getDictItemsByDictCode(dictCode));
    }
}
