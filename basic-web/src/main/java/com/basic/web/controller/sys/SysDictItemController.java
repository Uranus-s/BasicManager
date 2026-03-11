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

    @Override
    @PostMapping
    public Result<?> addDictItem(@Valid @RequestBody DictItemAddDTO dto) {
        sysDictItemService.addDictItem(dto);
        return Result.success();
    }

    @Override
    @PutMapping
    public Result<?> updateDictItem(@Valid @RequestBody DictItemUpdateDTO dto) {
        sysDictItemService.updateDictItem(dto);
        return Result.success();
    }

    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteDictItem(@PathVariable Long id) {
        sysDictItemService.deleteDictItem(id);
        return Result.success();
    }

    @Override
    @GetMapping("/{id}")
    public Result<DictItemVO> getDictItemById(@PathVariable Long id) {
        return Result.success(sysDictItemService.getDictItemById(id));
    }

    @Override
    @GetMapping("/list")
    public Result<PageResult<DictItemVO>> getDictItemList(DictItemQueryDTO dto) {
        return Result.success(sysDictItemService.getDictItemList(dto));
    }

    @Override
    @GetMapping("/dict/{dictId}")
    public Result<List<DictItemVO>> getDictItemsByDictId(@PathVariable Long dictId) {
        return Result.success(sysDictItemService.getDictItemsByDictId(dictId));
    }

    @Override
    @GetMapping("/code/{dictCode}")
    public Result<List<DictItemVO>> getDictItemsByDictCode(@PathVariable String dictCode) {
        return Result.success(sysDictItemService.getDictItemsByDictCode(dictCode));
    }
}
