package com.basic.api.vo.sysConfig;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参数配置VO
 *
 * @author Gas
 */
@Data
public class ConfigVO {

    /**
     * 参数ID
     */
    private Long id;

    /**
     * 参数键
     */
    private String configKey;

    /**
     * 参数值
     */
    private String configValue;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
