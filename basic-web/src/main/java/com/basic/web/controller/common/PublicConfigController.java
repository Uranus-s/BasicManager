package com.basic.web.controller.common;

import com.basic.api.controller.sys.PublicConfigApi;
import com.basic.api.vo.sysConfig.PublicConfigVO;
import com.basic.common.result.Result;
import com.basic.sericve.sysConfig.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开系统设置入口，通过专用 VO 严格限制未登录用户可读取的字段。
 *
 * @author Gas
 */
@RestController
@RequiredArgsConstructor
public class PublicConfigController implements PublicConfigApi {

    private final ISysConfigService sysConfigService;

    @Override
    @GetMapping("/public/system/settings")
    public Result<PublicConfigVO> getPublicSettings() {
        return Result.success(sysConfigService.getPublicSettings());
    }
}
