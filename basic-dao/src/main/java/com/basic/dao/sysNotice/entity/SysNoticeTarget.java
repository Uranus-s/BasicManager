package com.basic.dao.sysNotice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 公告接收目标关联，同一公告可以同时关联多个角色和部门。
 */
@Getter
@Setter
@ToString
@TableName("sys_notice_target")
public class SysNoticeTarget {

    /**
     * 公告 ID。
     */
    private Long noticeId;

    /**
     * 目标类型，取值为 ROLE 或 DEPT。
     */
    private String targetType;

    /**
     * 角色或部门 ID。
     */
    private Long targetId;
}
