package com.basic.api.vo.sysDict;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典详情VO
 *
 * @author Gas
 */
@Data
public class DictVO {

    /**
     * 字典ID
     */
    private Long id;

    /**
     * 字典编码
     */
    private String dictCode;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 状态 0=禁用 1=正常
     */
    private Byte status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
