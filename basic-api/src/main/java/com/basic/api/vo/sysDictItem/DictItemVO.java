package com.basic.api.vo.sysDictItem;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典项VO
 *
 * @author Gas
 */
@Data
public class DictItemVO {

    /**
     * 字典项ID
     */
    private Long id;

    /**
     * 字典ID
     */
    private Long dictId;

    /**
     * 字典项值
     */
    private String itemValue;

    /**
     * 字典项标签
     */
    private String itemLabel;

    /**
     * 排序
     */
    private Integer sort;

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
