package com.basic.sericve.sysConfig.service;

import com.basic.api.dto.sysConfig.ConfigAddDTO;
import com.basic.api.dto.sysConfig.ConfigQueryDTO;
import com.basic.api.dto.sysConfig.ConfigUpdateDTO;
import com.basic.api.vo.sysConfig.ConfigVO;
import com.basic.common.result.PageResult;
import com.basic.dao.sysConfig.entity.SysConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 系统参数表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysConfigService extends IService<SysConfig> {

    /**
     * 新增参数配置
     *
     * @param dto 参数配置新增DTO
     * @return 参数ID
     */
    Long addConfig(ConfigAddDTO dto);

    /**
     * 更新参数配置
     *
     * @param dto 参数配置更新DTO
     */
    void updateConfig(ConfigUpdateDTO dto);

    /**
     * 删除参数配置
     *
     * @param id 参数ID
     */
    void deleteConfig(Long id);

    /**
     * 获取参数配置详情
     *
     * @param id 参数ID
     * @return 参数配置详情
     */
    ConfigVO getConfigById(Long id);

    /**
     * 分页查询参数配置列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult<ConfigVO> getConfigList(ConfigQueryDTO dto);

    /**
     * 根据参数键获取参数值
     *
     * @param configKey 参数键
     * @return 参数值
     */
    String getConfigByKey(String configKey);
}
