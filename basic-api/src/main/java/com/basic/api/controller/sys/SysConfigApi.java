package com.basic.api.controller.sys;

import com.basic.api.dto.sysConfig.ConfigBasicUpdateDTO;
import com.basic.api.dto.sysConfig.ConfigAiUpdateDTO;
import com.basic.api.dto.sysConfig.ConfigSecurityUpdateDTO;
import com.basic.api.vo.sysConfig.ConfigSettingsVO;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 系统设置管理 API，仅允许读写预定义业务设置。
 *
 * @author Gas
 */
@Tag(name = "系统设置", description = "系统基础信息和账号安全设置")
public interface SysConfigApi {

    @Operation(summary = "获取系统设置")
    @GetMapping("/settings")
    Result<ConfigSettingsVO> getSettings();

    @Operation(summary = "更新基础设置")
    @PutMapping("/settings/basic")
    Result<?> updateBasicSettings(@Valid @RequestBody ConfigBasicUpdateDTO dto);

    @Operation(summary = "更新账号安全设置")
    @PutMapping("/settings/security")
    Result<?> updateSecuritySettings(@Valid @RequestBody ConfigSecurityUpdateDTO dto);

    @Operation(summary = "更新 AI 设置")
    @PutMapping("/settings/ai")
    Result<?> updateAiSettings(@Valid @RequestBody ConfigAiUpdateDTO dto);

    @Operation(summary = "测试 DeepSeek 连接")
    @PostMapping("/settings/ai/test")
    Result<?> testAiConnection();
}
