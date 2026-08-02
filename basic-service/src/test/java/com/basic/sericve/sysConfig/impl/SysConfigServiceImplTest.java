package com.basic.sericve.sysConfig.impl;

import com.basic.api.dto.sysConfig.ConfigBasicUpdateDTO;
import com.basic.api.dto.sysConfig.ConfigSecurityUpdateDTO;
import com.basic.api.vo.sysConfig.ConfigSettingsVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysConfig.entity.SysConfig;
import com.basic.dao.sysConfig.mapper.SysConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 系统设置服务测试，验证固定配置键、默认值降级和更新边界。
 */
@ExtendWith(MockitoExtension.class)
class SysConfigServiceImplTest {

    @Mock
    private SysConfigMapper mapper;

    private SysConfigServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SysConfigServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
    }

    @Test
    void getSettingsShouldReturnStoredValues() {
        when(mapper.selectOne(any())).thenReturn(
                config(1L, "sys.title", "业务平台"),
                config(2L, "sys.login.tokenExpireHours", "48"));

        ConfigSettingsVO result = service.getSettings();

        assertThat(result.getSystemName()).isEqualTo("业务平台");
        assertThat(result.getTokenExpireHours()).isEqualTo(48);
    }

    @Test
    void getSettingsShouldFallbackWhenValuesAreMissingOrInvalid() {
        when(mapper.selectOne(any())).thenReturn(
                null,
                config(2L, "sys.login.tokenExpireHours", "abc"));

        ConfigSettingsVO result = service.getSettings();

        assertThat(result.getSystemName()).isEqualTo("基础管理系统");
        assertThat(result.getTokenExpireHours()).isEqualTo(24);
    }

    @Test
    void getTokenExpireMillisShouldUseConfiguredHours() {
        when(mapper.selectOne(any())).thenReturn(
                config(2L, "sys.login.tokenExpireHours", "6"));

        assertThat(service.getTokenExpireMillis()).isEqualTo(TimeUnit.HOURS.toMillis(6));
    }

    @Test
    void updateSecuritySettingsShouldInsertMissingFixedKey() {
        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.insert(any(SysConfig.class))).thenReturn(1);
        ConfigSecurityUpdateDTO dto = new ConfigSecurityUpdateDTO();
        dto.setTokenExpireHours(72);

        service.updateSecuritySettings(dto);

        ArgumentCaptor<SysConfig> captor = ArgumentCaptor.forClass(SysConfig.class);
        verify(mapper).insert(captor.capture());
        assertThat(captor.getValue().getConfigKey()).isEqualTo("sys.login.tokenExpireHours");
        assertThat(captor.getValue().getConfigValue()).isEqualTo("72");
    }

    @Test
    void updateBasicSettingsShouldTrimAndUpdateExistingConfig() {
        SysConfig existing = config(1L, "sys.title", "旧名称");
        when(mapper.selectOne(any())).thenReturn(existing);
        when(mapper.updateById(any(SysConfig.class))).thenReturn(1);
        ConfigBasicUpdateDTO dto = new ConfigBasicUpdateDTO();
        dto.setSystemName("  新名称  ");

        service.updateBasicSettings(dto);

        assertThat(existing.getConfigValue()).isEqualTo("新名称");
        verify(mapper).updateById(existing);
        verify(mapper, never()).insert(any(SysConfig.class));
    }

    @Test
    void updateBasicSettingsShouldRejectExpiredDataVersion() {
        SysConfig existing = config(1L, "sys.title", "旧名称");
        when(mapper.selectOne(any())).thenReturn(existing);
        when(mapper.updateById(any(SysConfig.class))).thenReturn(0);
        ConfigBasicUpdateDTO dto = new ConfigBasicUpdateDTO();
        dto.setSystemName("新名称");

        assertThatThrownBy(() -> service.updateBasicSettings(dto))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getError()).isEqualTo(ResultEnum.DATA_VERSION_EXPIRED));
    }

    @Test
    void updateBasicSettingsShouldRejectBlankName() {
        ConfigBasicUpdateDTO dto = new ConfigBasicUpdateDTO();
        dto.setSystemName("   ");

        assertThatThrownBy(() -> service.updateBasicSettings(dto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updateSecuritySettingsShouldRejectOutOfRangeHours() {
        ConfigSecurityUpdateDTO dto = new ConfigSecurityUpdateDTO();
        dto.setTokenExpireHours(169);

        assertThatThrownBy(() -> service.updateSecuritySettings(dto))
                .isInstanceOf(BusinessException.class);
    }

    private static SysConfig config(Long id, String key, String value) {
        SysConfig config = new SysConfig();
        config.setId(id);
        config.setConfigKey(key);
        config.setConfigValue(value);
        return config;
    }
}
