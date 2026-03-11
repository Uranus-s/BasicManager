package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysDictApi;
import com.basic.api.dto.sysDict.DictAddDTO;
import com.basic.api.dto.sysDict.DictQueryDTO;
import com.basic.api.dto.sysDict.DictUpdateDTO;
import com.basic.api.vo.sysDict.DictVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.sericve.sysDict.service.ISysDictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class SysDictController implements SysDictApi {

    private final ISysDictService sysDictService;

    @Override
    @PostMapping
    public Result<?> addDict(@Valid @RequestBody DictAddDTO dto) {
        sysDictService.addDict(dto);
        return Result.success();
    }

    @Override
    @PutMapping
    public Result<?> updateDict(@Valid @RequestBody DictUpdateDTO dto) {
        sysDictService.updateDict(dto);
        return Result.success();
    }

    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteDict(@PathVariable Long id) {
        sysDictService.deleteDict(id);
        return Result.success();
    }

    @Override
    @GetMapping("/{id}")
    public Result<DictVO> getDictById(@PathVariable Long id) {
        return Result.success(sysDictService.getDictById(id));
    }

    @Override
    @GetMapping("/list")
    public Result<PageResult<DictVO>> getDictList(DictQueryDTO dto) {
        return Result.success(sysDictService.getDictList(dto));
    }

    @Override
    @GetMapping("/all")
    public Result<List<DictVO>> getAllDicts() {
        return Result.success(sysDictService.getAllDicts());
    }
}
