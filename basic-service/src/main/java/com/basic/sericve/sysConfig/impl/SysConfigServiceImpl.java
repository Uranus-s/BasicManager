package com.basic.sericve.sysConfig.impl;

import com.basic.api.dto.sysConfig.ConfigBasicUpdateDTO;
import com.basic.api.dto.sysConfig.ConfigAiUpdateDTO;
import com.basic.api.dto.sysConfig.ConfigSecurityUpdateDTO;
import com.basic.api.vo.sysConfig.ConfigSettingsVO;
import com.basic.api.vo.sysConfig.PublicConfigVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysConfig.entity.SysConfig;
import com.basic.dao.sysConfig.mapper.SysConfigMapper;
import com.basic.sericve.sysConfig.service.ISysConfigService;
import com.basic.ai.event.AiConfigChangedEvent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 系统设置服务实现，负责固定配置键映射、类型转换和异常降级。
 *
 * @author Gas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    private static final String SYSTEM_NAME_KEY = "sys.title";
    private static final String TOKEN_EXPIRE_HOURS_KEY = "sys.login.tokenExpireHours";
    private static final String AI_DEEPSEEK_API_KEY = "ai.deepseek.apiKey";
    private static final String DEFAULT_SYSTEM_NAME = "基础管理系统";
    private static final int DEFAULT_TOKEN_EXPIRE_HOURS = 24;
    private static final int MAX_TOKEN_EXPIRE_HOURS = 168;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ConfigSettingsVO getSettings() {
        ConfigSettingsVO settings = new ConfigSettingsVO();
        settings.setSystemName(resolveSystemName(readValue(SYSTEM_NAME_KEY)));
        settings.setTokenExpireHours(resolveTokenExpireHours(readValue(TOKEN_EXPIRE_HOURS_KEY)));
        String apiKey = getDeepSeekApiKey();
        settings.setDeepSeekApiKeyConfigured(StringUtils.hasText(apiKey));
        settings.setDeepSeekApiKeyMasked(maskApiKey(apiKey));
        return settings;
    }

    @Override
    public PublicConfigVO getPublicSettings() {
        PublicConfigVO settings = new PublicConfigVO();
        settings.setSystemName(resolveSystemName(readValue(SYSTEM_NAME_KEY)));
        return settings;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBasicSettings(ConfigBasicUpdateDTO dto) {
        String systemName = dto == null ? null : dto.getSystemName();
        if (!StringUtils.hasText(systemName)) {
            throw new BusinessException(ResultEnum.PARAM_ILLEGAL);
        }
        String normalizedName = systemName.trim();
        if (normalizedName.length() > 50) {
            throw new BusinessException(ResultEnum.PARAM_ILLEGAL);
        }
        upsert(SYSTEM_NAME_KEY, normalizedName, "系统名称");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSecuritySettings(ConfigSecurityUpdateDTO dto) {
        Integer hours = dto == null ? null : dto.getTokenExpireHours();
        if (hours == null || hours < 1 || hours > MAX_TOKEN_EXPIRE_HOURS) {
            throw new BusinessException(ResultEnum.PARAM_ILLEGAL);
        }
        upsert(TOKEN_EXPIRE_HOURS_KEY, String.valueOf(hours), "Token 有效期，单位小时");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAiSettings(ConfigAiUpdateDTO dto) {
        String input = dto == null ? null : dto.getApiKey();
        String current = readValue(AI_DEEPSEEK_API_KEY);
        if (!StringUtils.hasText(input)) {
            if (!StringUtils.hasText(current)) {
                throw new BusinessException(ResultEnum.AI_CONFIG_MISSING);
            }
            return;
        }
        String normalized = input.trim();
        if (normalized.length() > 255) {
            throw new BusinessException(ResultEnum.PARAM_OUT_OF_RANGE);
        }
        upsert(AI_DEEPSEEK_API_KEY, normalized, "DeepSeek API Key，运行时由管理员配置");
        applicationEventPublisher.publishEvent(new AiConfigChangedEvent(this));
    }

    @Override
    public String getDeepSeekApiKey() {
        String value = readValue(AI_DEEPSEEK_API_KEY);
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    @Override
    public long getTokenExpireMillis() {
        int hours = resolveTokenExpireHours(readValue(TOKEN_EXPIRE_HOURS_KEY));
        return TimeUnit.HOURS.toMillis(hours);
    }

    private String readValue(String key) {
        SysConfig config = getBaseMapper().selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key));
        return config == null ? null : config.getConfigValue();
    }

    private String resolveSystemName(String value) {
        if (StringUtils.hasText(value)) {
            String normalized = value.trim();
            if (normalized.length() <= 50) {
                return normalized;
            }
        }
        log.warn("系统名称配置缺失或非法，已使用默认值");
        return DEFAULT_SYSTEM_NAME;
    }

    private int resolveTokenExpireHours(String value) {
        if (StringUtils.hasText(value)) {
            try {
                int hours = Integer.parseInt(value.trim());
                if (hours >= 1 && hours <= MAX_TOKEN_EXPIRE_HOURS) {
                    return hours;
                }
            } catch (NumberFormatException ignored) {
                // 存量配置格式异常时继续使用默认值，不能中断登录流程。
            }
        }
        log.warn("Token有效期配置缺失或非法，已使用默认值{}小时", DEFAULT_TOKEN_EXPIRE_HOURS);
        return DEFAULT_TOKEN_EXPIRE_HOURS;
    }

    /**
     * 仅保留密钥首尾各四个字符；短密钥全部遮盖，避免掩码本身泄露有效凭据。
     */
    private String maskApiKey(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.length() <= 8) {
            return "******";
        }
        return normalized.substring(0, 4) + "******"
                + normalized.substring(normalized.length() - 4);
    }

    private void upsert(String key, String value, String remark) {
        SysConfig config = getBaseMapper().selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key));
        if (config == null) {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setRemark(remark);
            save(config);
            return;
        }
        config.setConfigValue(value);
        config.setRemark(remark);
        if (!updateById(config)) {
            // 配置记录启用了乐观锁，版本冲突时不能向管理端返回保存成功。
            throw new BusinessException(ResultEnum.DATA_VERSION_EXPIRED);
        }
    }
}
