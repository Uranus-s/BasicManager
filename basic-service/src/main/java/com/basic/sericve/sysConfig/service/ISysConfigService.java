package com.basic.sericve.sysConfig.service;

import com.basic.api.dto.sysConfig.ConfigBasicUpdateDTO;
import com.basic.api.dto.sysConfig.ConfigSecurityUpdateDTO;
import com.basic.api.vo.sysConfig.ConfigSettingsVO;
import com.basic.api.vo.sysConfig.PublicConfigVO;
import com.basic.dao.sysConfig.entity.SysConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统设置服务，只暴露具有明确业务语义的预定义设置。
 *
 * @author Gas
 */
public interface ISysConfigService extends IService<SysConfig> {

    /**
     * 获取管理端完整系统设置。
     *
     * @return 当前实际生效的系统设置
     */
    ConfigSettingsVO getSettings();

    /**
     * 获取无需登录即可展示的基础设置。
     *
     * @return 公开系统设置
     */
    PublicConfigVO getPublicSettings();

    /**
     * 更新基础设置。
     *
     * @param dto 基础设置
     */
    void updateBasicSettings(ConfigBasicUpdateDTO dto);

    /**
     * 更新账号安全设置。
     *
     * @param dto 账号安全设置
     */
    void updateSecuritySettings(ConfigSecurityUpdateDTO dto);

    /**
     * 获取 Token 有效期毫秒数，异常配置自动使用安全默认值。
     *
     * @return Token 有效期毫秒数
     */
    long getTokenExpireMillis();
}
