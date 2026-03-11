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

    @Override
    @PostMapping
    public Result<?> addConfig(@Valid @RequestBody ConfigAddDTO dto) {
        sysConfigService.addConfig(dto);
        return Result.success();
    }

    @Override
    @PutMapping
    public Result<?> updateConfig(@Valid @RequestBody ConfigUpdateDTO dto) {
        sysConfigService.updateConfig(dto);
        return Result.success();
    }

    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteConfig(@PathVariable Long id) {
        sysConfigService.deleteConfig(id);
        return Result.success();
    }

    @Override
    @GetMapping("/{id}")
    public Result<ConfigVO> getConfigById(@PathVariable Long id) {
        return Result.success(sysConfigService.getConfigById(id));
    }

    @Override
    @GetMapping("/list")
    public Result<PageResult<ConfigVO>> getConfigList(ConfigQueryDTO dto) {
        return Result.success(sysConfigService.getConfigList(dto));
    }

    @Override
    @GetMapping("/key/{configKey}")
    public Result<String> getConfigByKey(@PathVariable String configKey) {
        return Result.success(sysConfigService.getConfigByKey(configKey));
    }
}
