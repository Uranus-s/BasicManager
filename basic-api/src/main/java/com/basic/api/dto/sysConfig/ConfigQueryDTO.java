package com.basic.api.dto.sysConfig;

import lombok.Data;

/**
 * 参数配置查询DTO
 *
 * @author Gas
 */
@Data
public class ConfigQueryDTO {

    /**
     * 参数键（模糊查询）
     */
    private String configKey;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
