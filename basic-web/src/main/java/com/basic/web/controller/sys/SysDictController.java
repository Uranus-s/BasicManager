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

    /**
     * 新增字典
     *
     * @param dto 字典信息
     * @return 操作结果
     */
    @Override
    @PostMapping
    public Result<?> addDict(@Valid @RequestBody DictAddDTO dto) {
        sysDictService.addDict(dto);
        return Result.success();
    }

    /**
     * 更新字典信息
     *
     * @param dto 字典信息
     * @return 操作结果
     */
    @Override
    @PutMapping
    public Result<?> updateDict(@Valid @RequestBody DictUpdateDTO dto) {
        sysDictService.updateDict(dto);
        return Result.success();
    }

    /**
     * 删除字典
     *
     * @param id 字典ID
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteDict(@PathVariable Long id) {
        sysDictService.deleteDict(id);
        return Result.success();
    }

    /**
     * 根据ID获取字典详情
     *
     * @param id 字典ID
     * @return 字典信息
     */
    @Override
    @GetMapping("/{id}")
    public Result<DictVO> getDictById(@PathVariable Long id) {
        return Result.success(sysDictService.getDictById(id));
    }

    /**
     * 获取字典列表（分页）
     *
     * @param dto 查询条件
     * @return 字典列表（分页）
     */
    @Override
    @GetMapping("/list")
    public Result<PageResult<DictVO>> getDictList(DictQueryDTO dto) {
        return Result.success(sysDictService.getDictList(dto));
    }

    /**
     * 获取所有字典列表（不分页）
     *
     * @return 所有字典列表
     */
    @Override
    @GetMapping("/all")
    public Result<List<DictVO>> getAllDicts() {
        return Result.success(sysDictService.getAllDicts());
    }
}
