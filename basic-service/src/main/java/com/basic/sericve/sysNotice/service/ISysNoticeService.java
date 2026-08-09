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

    Long addNotice(NoticeAddDTO dto);

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
