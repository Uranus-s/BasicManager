package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysNoticeApi;
import com.basic.api.dto.sysNotice.NoticeAddDTO;
import com.basic.api.dto.sysNotice.NoticeQueryDTO;
import com.basic.api.dto.sysNotice.NoticeUpdateDTO;
import com.basic.api.vo.sysNotice.NoticeListVO;
import com.basic.api.vo.sysNotice.NoticeTargetOptionsVO;
import com.basic.api.vo.sysNotice.NoticeVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.core.log.annotation.OperateLog;
import com.basic.sericve.sysNotice.service.ISysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知公告管理入口，仅负责权限校验、参数接收和结果包装。
 */
@Validated
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
public class SysNoticeController implements SysNoticeApi {

    private final ISysNoticeService noticeService;

    @Override
    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping("/list")
    public Result<PageResult<NoticeListVO>> getNoticeList(NoticeQueryDTO dto) {
        return Result.success(noticeService.getNoticeList(dto));
    }

    @Override
    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping("/{id}")
    public Result<NoticeVO> getNoticeById(@PathVariable("id") Long id) {
        return Result.success(noticeService.getNoticeById(id));
    }

    @Override
    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping("/target-options")
    public Result<NoticeTargetOptionsVO> getTargetOptions() {
        return Result.success(noticeService.getTargetOptions());
    }

    @Override
    @PreAuthorize("hasAuthority('system:notice:add')")
    @PostMapping
    @OperateLog(module = "通知公告", method = "新增公告")
    public Result<?> addNotice(@RequestBody NoticeAddDTO dto) {
        return Result.success(noticeService.addNotice(dto));
    }

    @Override
    @PreAuthorize("hasAuthority('system:notice:edit')")
    @PutMapping
    @OperateLog(module = "通知公告", method = "修改公告")
    public Result<?> updateNotice(@RequestBody NoticeUpdateDTO dto) {
        noticeService.updateNotice(dto);
        return Result.success();
    }

    @Override
    @PreAuthorize("hasAuthority('system:notice:delete')")
    @DeleteMapping("/{id}")
    @OperateLog(module = "通知公告", method = "删除公告")
    public Result<?> deleteNotice(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return Result.success();
    }

    @Override
    @PreAuthorize("hasAuthority('system:notice:publish')")
    @PostMapping("/{id}/publish")
    @OperateLog(module = "通知公告", method = "发布公告")
    public Result<?> publishNotice(@PathVariable("id") Long id) {
        noticeService.publishNotice(id);
        return Result.success();
    }

    @Override
    @PreAuthorize("hasAuthority('system:notice:withdraw')")
    @PostMapping("/{id}/withdraw")
    @OperateLog(module = "通知公告", method = "撤回公告")
    public Result<?> withdrawNotice(@PathVariable("id") Long id) {
        noticeService.withdrawNotice(id);
        return Result.success();
    }
}
