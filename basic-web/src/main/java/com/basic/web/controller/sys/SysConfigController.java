package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysConfigApi;
import com.basic.api.dto.sysConfig.ConfigBasicUpdateDTO;
import com.basic.api.dto.sysConfig.ConfigSecurityUpdateDTO;
import com.basic.api.vo.sysConfig.ConfigSettingsVO;
import com.basic.common.result.Result;
import com.basic.core.log.annotation.OperateLog;
import com.basic.sericve.sysConfig.service.ISysConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统设置管理入口。
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SysConfigController implements SysConfigApi {

    private final ISysConfigService sysConfigService;

    @Override
    @PreAuthorize("hasAuthority('system:config:query')")
    @GetMapping("/settings")
    public Result<ConfigSettingsVO> getSettings() {
        return Result.success(sysConfigService.getSettings());
    }

    @Override
    @PreAuthorize("hasAuthority('system:config:edit')")
    @PutMapping("/settings/basic")
    @OperateLog(module = "系统设置", method = "更新基础设置")
    public Result<?> updateBasicSettings(@Valid @RequestBody ConfigBasicUpdateDTO dto) {
        sysConfigService.updateBasicSettings(dto);
        return Result.success();
    }

    @Override
    @PreAuthorize("hasAuthority('system:config:edit')")
    @PutMapping("/settings/security")
    @OperateLog(module = "系统设置", method = "更新账号安全设置")
    public Result<?> updateSecuritySettings(@Valid @RequestBody ConfigSecurityUpdateDTO dto) {
        sysConfigService.updateSecuritySettings(dto);
        return Result.success();
    }
}
