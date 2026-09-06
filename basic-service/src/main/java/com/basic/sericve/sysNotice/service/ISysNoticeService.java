package com.basic.sericve.sysNotice.service;

import com.basic.api.dto.sysNotice.NoticeAddDTO;
import com.basic.api.dto.sysNotice.NoticeQueryDTO;
import com.basic.api.dto.sysNotice.NoticeUpdateDTO;
import com.basic.api.dto.sysNotice.NoticeVisibleQueryDTO;
import com.basic.api.vo.sysNotice.NoticeDetailVO;
import com.basic.api.vo.sysNotice.NoticeListVO;
import com.basic.api.vo.sysNotice.NoticeTargetOptionsVO;
import com.basic.api.vo.sysNotice.NoticeVO;
import com.basic.common.result.PageResult;

import java.util.List;

/**
 * 通知公告业务服务，统一承载管理操作和用户端可见性查询。
 */
public interface ISysNoticeService {

    /**
     * 只校验公告草稿的类型、范围和目标，不产生任何数据库写入。
     */
    void validateNoticeDraft(NoticeAddDTO dto);

    /**
     * 校验公告修改内容及客户端版本，不产生数据库写入。
     */
    void validateNoticeUpdate(NoticeUpdateDTO dto);

    /** 校验指定版本的公告当前允许删除。 */
    void validateNoticeDelete(Long id, Integer version);

    /** 校验指定版本的公告当前允许发布。 */
    void validateNoticePublish(Long id, Integer version);

    /** 校验指定版本的公告当前允许撤回。 */
    void validateNoticeWithdraw(Long id, Integer version);

    Long addNotice(NoticeAddDTO dto);

    /**
     * 在同一事务中创建并发布公告，供审批执行器从外部 Bean 调用。
     */
    Long addAndPublishNotice(NoticeAddDTO dto);

    void updateNotice(NoticeUpdateDTO dto);

    void deleteNotice(Long id);

    void publishNotice(Long id);

    void withdrawNotice(Long id);

    NoticeVO getNoticeById(Long id);

    PageResult<NoticeListVO> getNoticeList(NoticeQueryDTO dto);

    NoticeTargetOptionsVO getTargetOptions();

    List<NoticeListVO> getLatestVisibleNotices(Long userId, Integer limit);

    PageResult<NoticeListVO> getVisibleNoticeList(Long userId, NoticeVisibleQueryDTO dto);

    NoticeDetailVO getVisibleNoticeById(Long userId, Long id);
}
