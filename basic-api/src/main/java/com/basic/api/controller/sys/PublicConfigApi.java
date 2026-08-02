package com.basic.api.controller.sys;

import com.basic.api.vo.sysConfig.PublicConfigVO;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 公开系统设置 API，只返回登录前允许展示的非敏感信息。
 *
 * @author Gas
 */
@Tag(name = "公开系统设置")
public interface PublicConfigApi {

    @Operation(summary = "获取公开系统设置")
    @GetMapping("/public/system/settings")
    Result<PublicConfigVO> getPublicSettings();
}
