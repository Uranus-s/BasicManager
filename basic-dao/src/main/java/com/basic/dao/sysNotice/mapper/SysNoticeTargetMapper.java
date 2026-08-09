package com.basic.dao.sysNotice.mapper;

import com.basic.dao.sysNotice.entity.SysNoticeTarget;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告接收目标 Mapper，目标关联采用按公告整体替换的持久化策略。
 */
public interface SysNoticeTargetMapper {

    /**
     * 物理删除指定公告的全部接收目标。
     */
    int deleteByNoticeId(@Param("noticeId") Long noticeId);

    /**
     * 批量写入去重后的角色和部门目标。
     */
    int insertBatch(@Param("targets") List<SysNoticeTarget> targets);

    /**
     * 查询指定公告的全部接收目标。
     */
    List<SysNoticeTarget> selectByNoticeId(@Param("noticeId") Long noticeId);
}
