package com.basic.sericve.ai.config;

import com.basic.ai.config.AiModelConfigProvider;
import com.basic.sericve.sysConfig.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 从系统配置业务读取模型密钥，隔离 basic-ai 与 basic-service 的依赖方向。
 */
@Component
@RequiredArgsConstructor
public class SysConfigAiModelConfigProvider implements AiModelConfigProvider {

    private final ISysConfigService configService;

    @Override
    public String getDeepSeekApiKey() {
        return configService.getDeepSeekApiKey();
    }
}
