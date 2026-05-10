package com.basic.dao.sysDictItem.entity;

import com.baomidou.mybatisplus.annotation.*;
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 逻辑删除
     */
    @TableLogic
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
