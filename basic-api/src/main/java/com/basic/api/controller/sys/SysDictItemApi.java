package com.basic.api.controller.sys;

import com.basic.api.dto.sysDictItem.DictItemAddDTO;
import com.basic.api.dto.sysDictItem.DictItemQueryDTO;
import com.basic.api.dto.sysDictItem.DictItemUpdateDTO;
import com.basic.api.vo.sysDictItem.DictItemVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典项管理API接口
 *
 * @author Gas
 */
@Tag(name = "字典项管理", description = "系统字典项的新增、修改、查询和删除接口")
public interface SysDictItemApi {

    /**
     * 新增字典项
     *
     * @param dto 字典项新增DTO
     * @return 操作结果
     */
    @Operation(summary = "新增字典项", description = "在指定字典下创建新的字典项")
    @PostMapping
    Result<?> addDictItem(@Valid @RequestBody DictItemAddDTO dto);

    /**
     * 更新字典项
     *
     * @param dto 字典项更新DTO
     * @return 操作结果
     */
    @Operation(summary = "更新字典项", description = "更新指定字典项的标签、值、排序和状态")
    @PutMapping
    Result<?> updateDictItem(@Valid @RequestBody DictItemUpdateDTO dto);

    /**
     * 删除字典项
     *
     * @param id 字典项ID
     * @return 操作结果
     */
    @Operation(summary = "删除字典项", description = "根据字典项ID删除字典项")
    @DeleteMapping("/{id}")
    Result<?> deleteDictItem(
            @Parameter(description = "字典项ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 获取字典项详情
     *
     * @param id 字典项ID
     * @return 字典项详情
     */
    @Operation(summary = "获取字典项详情", description = "根据字典项ID查询字典项详情")
    @GetMapping("/{id}")
    Result<DictItemVO> getDictItemById(
            @Parameter(description = "字典项ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 分页查询字典项列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询字典项", description = "根据字典、标签、值和状态分页查询字典项")
    @GetMapping("/list")
    Result<PageResult<DictItemVO>> getDictItemList(DictItemQueryDTO dto);

    /**
     * 根据字典ID获取字典项列表
     *
     * @param dictId 字典ID
     * @return 字典项列表
     */
    @Operation(summary = "根据字典ID查询字典项", description = "返回指定字典下的全部可用字典项")
    @GetMapping("/dict/{dictId}")
    Result<List<DictItemVO>> getDictItemsByDictId(
            @Parameter(description = "字典ID", example = "1", required = true)
            @PathVariable Long dictId);

    /**
     * 根据字典编码获取字典项列表
     *
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    @Operation(summary = "根据字典编码查询字典项", description = "返回指定字典编码对应的全部可用字典项")
    @GetMapping("/code/{dictCode}")
    Result<List<DictItemVO>> getDictItemsByDictCode(
            @Parameter(description = "字典编码", example = "sys_user_status", required = true)
            @PathVariable String dictCode);
}
