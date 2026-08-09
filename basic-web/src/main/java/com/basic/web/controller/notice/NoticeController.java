package com.basic.web.controller.notice;

import com.basic.api.controller.notice.NoticeApi;
import com.basic.api.dto.sysNotice.NoticeVisibleQueryDTO;
import com.basic.api.vo.sysNotice.NoticeDetailVO;
import com.basic.api.vo.sysNotice.NoticeListVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.common.result.ResultEnum;
import com.basic.core.security.model.LoginUser;
import com.basic.sericve.sysNotice.service.ISysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 登录用户公告入口，所有查询都使用认证上下文中的用户 ID 校验可见性。
 */
@Validated
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController implements NoticeApi {

    private final ISysNoticeService noticeService;

    @Override
    @GetMapping("/latest")
    public Result<List<NoticeListVO>> getLatestNotices(@RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(noticeService.getLatestVisibleNotices(currentUserId(), limit));
    }

    @Override
    @GetMapping("/list")
    public Result<PageResult<NoticeListVO>> getVisibleNoticeList(NoticeVisibleQueryDTO dto) {
        return Result.success(noticeService.getVisibleNoticeList(currentUserId(), dto));
    }

    @Override
    @GetMapping("/{id}")
    public Result<NoticeDetailVO> getVisibleNoticeById(@PathVariable("id") Long id) {
        return Result.success(noticeService.getVisibleNoticeById(currentUserId(), id));
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof LoginUser loginUser)
                || loginUser.getUserId() == null) {
            throw new BusinessException(ResultEnum.UNAUTHORIZED);
        }
        return loginUser.getUserId();
    }
}
