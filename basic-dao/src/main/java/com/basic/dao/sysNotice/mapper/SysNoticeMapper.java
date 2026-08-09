package com.basic.dao.sysNotice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basic.core.mybatis.base.BaseMapperPlus;
import com.basic.dao.sysNotice.entity.SysNotice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知公告 Mapper，用户端查询始终在数据库条件中校验发布状态和接收范围。
 */
public interface SysNoticeMapper extends BaseMapperPlus<SysNotice> {

    /**
     * 按当前版本逻辑删除草稿或已撤回公告，避免并发发布后被误删。
     */
    int deleteDraftOrWithdrawnByIdAndVersion(@Param("id") Long id,
                                              @Param("version") Integer version);

    /**
     * 分页查询当前用户可见的已发布公告。
     */
    Page<SysNotice> selectVisiblePage(Page<SysNotice> page,
                                      @Param("title") String title,
                                      @Param("noticeType") String noticeType,
                                      @Param("roleIds") List<Long> roleIds,
                                      @Param("deptIds") List<Long> deptIds);

    /**
     * 查询当前用户最新可见公告，数量由服务层限制在安全范围内。
     */
    List<SysNotice> selectLatestVisible(@Param("roleIds") List<Long> roleIds,
                                        @Param("deptIds") List<Long> deptIds,
                                        @Param("limit") int limit);

    /**
     * 按 ID 查询当前用户可见公告，避免绕过列表直接访问无权公告。
     */
    SysNotice selectVisibleById(@Param("id") Long id,
                                @Param("roleIds") List<Long> roleIds,
                                @Param("deptIds") List<Long> deptIds);
}
