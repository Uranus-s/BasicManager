package com.basic.dao.SysDictItem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 字典项表
 * </p>
 *
 * @author aber
 * @since 2026-01-31
 */
@Getter
@Setter
@ToString
@TableName("sys_dict_item")
public class SysDictItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long updateBy;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 逻辑删除
     */
    private Byte deleted;

    /**
     * 字典ID
     */
    private Long dictId;

    /**
     * 值
     */
    private String itemValue;

    /**
     * 标签
     */
    private String itemLabel;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private Byte status;
}
