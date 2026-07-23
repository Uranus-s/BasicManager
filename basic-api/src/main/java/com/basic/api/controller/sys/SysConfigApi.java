package com.basic.api.controller.sys;

import com.basic.api.dto.sysConfig.ConfigAddDTO;
import com.basic.api.dto.sysConfig.ConfigQueryDTO;
import com.basic.api.dto.sysConfig.ConfigUpdateDTO;
import com.basic.api.vo.sysConfig.ConfigVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 参数配置管理API接口
 *
 * @author Gas
 */
@Tag(name = "参数配置管理", description = "系统参数配置的新增、修改、查询和删除接口")
public interface SysConfigApi {

    /**
     * 新增参数配置
     *
     * @param dto 参数配置新增DTO
     * @return 操作结果
     */
    @Operation(summary = "新增参数配置", description = "创建新的系统参数键值配置")
    @PostMapping
    Result<?> addConfig(@Valid @RequestBody ConfigAddDTO dto);

    /**
     * 更新参数配置
     *
     * @param dto 参数配置更新DTO
     * @return 操作结果
     */
    @Operation(summary = "更新参数配置", description = "更新指定系统参数的值和备注")
    @PutMapping
    Result<?> updateConfig(@Valid @RequestBody ConfigUpdateDTO dto);

    /**
     * 删除参数配置
     *
     * @param id 参数ID
     * @return 操作结果
     */
    @Operation(summary = "删除参数配置", description = "根据参数ID删除系统参数配置")
    @DeleteMapping("/{id}")
    Result<?> deleteConfig(
            @Parameter(description = "参数ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 获取参数配置详情
     *
     * @param id 参数ID
     * @return 参数配置详情
     */
    @Operation(summary = "获取参数配置详情", description = "根据参数ID查询参数配置详情")
    @GetMapping("/{id}")
    Result<ConfigVO> getConfigById(
            @Parameter(description = "参数ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 分页查询参数配置列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询参数配置", description = "根据参数键等条件分页查询系统参数")
    @GetMapping("/list")
    Result<PageResult<ConfigVO>> getConfigList(ConfigQueryDTO dto);

    /**
     * 根据参数键获取参数值
     *
     * @param configKey 参数键
     * @return 参数值
     */
    @Operation(summary = "根据参数键查询参数值", description = "根据唯一参数键获取对应参数值")
    @GetMapping("/key/{configKey}")
    Result<String> getConfigByKey(
            @Parameter(description = "参数键", example = "system.user.defaultPassword", required = true)
            @PathVariable String configKey);
}
