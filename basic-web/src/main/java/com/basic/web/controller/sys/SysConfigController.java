package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysConfigApi;
import com.basic.api.dto.sysConfig.ConfigAddDTO;
import com.basic.api.dto.sysConfig.ConfigQueryDTO;
import com.basic.api.dto.sysConfig.ConfigUpdateDTO;
import com.basic.api.vo.sysConfig.ConfigVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.sericve.sysConfig.service.ISysConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 参数配置管理 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SysConfigController implements SysConfigApi {

    private final ISysConfigService sysConfigService;

    /**
     * 新增配置
     *
     * @param dto 配置信息
     * @return 操作结果
     */
    @Override
    @PostMapping
    public Result<?> addConfig(@Valid @RequestBody ConfigAddDTO dto) {
        sysConfigService.addConfig(dto);
        return Result.success();
    }

    /**
     * 更新配置信息
     *
     * @param dto 配置信息
     * @return 操作结果
     */
    @Override
    @PutMapping
    public Result<?> updateConfig(@Valid @RequestBody ConfigUpdateDTO dto) {
        sysConfigService.updateConfig(dto);
        return Result.success();
    }

    /**
     * 删除配置
     *
     * @param id 配置ID
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteConfig(@PathVariable Long id) {
        sysConfigService.deleteConfig(id);
        return Result.success();
    }

    /**
     * 根据ID获取配置详情
     *
     * @param id 配置ID
     * @return 配置信息
     */
    @Override
    @GetMapping("/{id}")
    public Result<ConfigVO> getConfigById(@PathVariable Long id) {
        return Result.success(sysConfigService.getConfigById(id));
    }

    /**
     * 获取配置列表（分页）
     *
     * @param dto 查询条件
     * @return 配置列表（分页）
     */
    @Override
    @GetMapping("/list")
    public Result<PageResult<ConfigVO>> getConfigList(ConfigQueryDTO dto) {
        return Result.success(sysConfigService.getConfigList(dto));
    }

    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    @Override
    @GetMapping("/key/{configKey}")
    public Result<String> getConfigByKey(@PathVariable String configKey) {
        return Result.success(sysConfigService.getConfigByKey(configKey));
    }
}
