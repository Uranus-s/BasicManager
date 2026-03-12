package com.basic.api.controller.sys;

import com.basic.api.dto.sysConfig.ConfigAddDTO;
import com.basic.api.dto.sysConfig.ConfigQueryDTO;
import com.basic.api.dto.sysConfig.ConfigUpdateDTO;
import com.basic.api.vo.sysConfig.ConfigVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 参数配置管理API接口
 *
 * @author Gas
 */
public interface SysConfigApi {

    /**
     * 新增参数配置
     *
     * @param dto 参数配置新增DTO
     * @return 操作结果
     */
    @PostMapping
    Result<?> addConfig(@Valid @RequestBody ConfigAddDTO dto);

    /**
     * 更新参数配置
     *
     * @param dto 参数配置更新DTO
     * @return 操作结果
     */
    @PutMapping
    Result<?> updateConfig(@Valid @RequestBody ConfigUpdateDTO dto);

    /**
     * 删除参数配置
     *
     * @param id 参数ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    Result<?> deleteConfig(@PathVariable Long id);

    /**
     * 获取参数配置详情
     *
     * @param id 参数ID
     * @return 参数配置详情
     */
    @GetMapping("/{id}")
    Result<ConfigVO> getConfigById(@PathVariable Long id);

    /**
     * 分页查询参数配置列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    Result<PageResult<ConfigVO>> getConfigList(ConfigQueryDTO dto);

    /**
     * 根据参数键获取参数值
     *
     * @param configKey 参数键
     * @return 参数值
     */
    @GetMapping("/key/{configKey}")
    Result<String> getConfigByKey(@PathVariable String configKey);
}
