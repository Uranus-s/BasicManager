package com.basic.api.controller.sys;

import com.basic.api.dto.sysDict.DictAddDTO;
import com.basic.api.dto.sysDict.DictQueryDTO;
import com.basic.api.dto.sysDict.DictUpdateDTO;
import com.basic.api.vo.sysDict.DictVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理API接口
 *
 * @author Gas
 */
@Tag(name = "字典管理", description = "系统字典的新增、修改、查询和删除接口")
public interface SysDictApi {

    /**
     * 新增字典
     *
     * @param dto 字典新增DTO
     * @return 操作结果
     */
    @Operation(summary = "新增字典", description = "创建新的系统字典")
    @PostMapping
    Result<?> addDict(@Valid @RequestBody DictAddDTO dto);

    /**
     * 更新字典
     *
     * @param dto 字典更新DTO
     * @return 操作结果
     */
    @Operation(summary = "更新字典", description = "更新指定系统字典的名称和状态")
    @PutMapping
    Result<?> updateDict(@Valid @RequestBody DictUpdateDTO dto);

    /**
     * 删除字典
     *
     * @param id 字典ID
     * @return 操作结果
     */
    @Operation(summary = "删除字典", description = "根据字典ID删除系统字典")
    @DeleteMapping("/{id}")
    Result<?> deleteDict(
            @Parameter(description = "字典ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 获取字典详情
     *
     * @param id 字典ID
     * @return 字典详情
     */
    @Operation(summary = "获取字典详情", description = "根据字典ID查询字典详情")
    @GetMapping("/{id}")
    Result<DictVO> getDictById(
            @Parameter(description = "字典ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 分页查询字典列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询字典", description = "根据字典编码、名称和状态分页查询字典")
    @GetMapping("/list")
    Result<PageResult<DictVO>> getDictList(DictQueryDTO dto);

    /**
     * 获取所有字典列表
     *
     * @return 字典列表
     */
    @Operation(summary = "查询全部字典", description = "返回全部系统字典，适用于下拉选择")
    @GetMapping("/all")
    Result<List<DictVO>> getAllDicts();
}
