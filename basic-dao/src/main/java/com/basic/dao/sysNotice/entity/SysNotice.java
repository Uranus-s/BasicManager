package com.basic.dao.sysNotice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.basic.core.mybatis.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 通知公告实体，保存公告正文、接收范围和发布状态。
 */
@Getter
@Setter
@ToString
@TableName("sys_notice")
public class SysNotice extends BaseEntity {

    /**
     * 公告标题。
     */
    private String title;

    /**
     * 公告类型，对应 sys_notice_type 字典项值。
     */
    private String noticeType;

    /**
     * Markdown 格式的公告正文。
     */
    private String content;

    /**
     * 接收范围，取值为 ALL 或 TARGETED。
     */
    private String scopeType;

    /**
     * 生命周期状态，取值为 DRAFT、PUBLISHED 或 WITHDRAWN。
     */
    private String status;

    /**
     * 最近一次发布时间，再次发布时会刷新。
     */
    private LocalDateTime publishTime;
}
