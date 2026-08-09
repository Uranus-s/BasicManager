package com.basic.api.controller.notice;

import com.basic.api.dto.sysNotice.NoticeVisibleQueryDTO;
import com.basic.api.vo.sysNotice.NoticeDetailVO;
import com.basic.api.vo.sysNotice.NoticeListVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 登录用户公告接口契约，仅返回当前用户有权查看的已发布公告。
 */
@Tag(name = "公告中心", description = "当前登录用户的可见公告查询接口")
public interface NoticeApi {

    /**
     * 查询当前用户最新可见公告。
     *
     * @param limit 返回数量，默认 5 条且最多 10 条
     * @return 最新公告列表
     */
    @Operation(summary = "获取最新公告", description = "按发布时间倒序返回当前用户最新可见公告")
    @GetMapping("/latest")
    Result<List<NoticeListVO>> getLatestNotices(
            @Parameter(description = "返回数量", example = "5")
            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "返回数量必须大于0")
            @Max(value = 10, message = "返回数量不能超过10") Integer limit);

    /**
     * 分页查询当前用户可见公告。
     *
     * @param dto 可见公告查询条件
     * @return 可见公告分页结果
     */
    @Operation(summary = "分页查询可见公告", description = "按标题和类型分页查询当前用户可见的已发布公告")
    @GetMapping("/list")
    Result<PageResult<NoticeListVO>> getVisibleNoticeList(@Valid NoticeVisibleQueryDTO dto);

    /**
     * 查询当前用户可见公告详情。
     *
     * @param id 公告 ID
     * @return 公告详情
     */
    @Operation(summary = "获取可见公告详情", description = "根据公告 ID 查询当前用户有权查看的已发布公告")
    @GetMapping("/{id}")
    Result<NoticeDetailVO> getVisibleNoticeById(
            @Parameter(description = "公告 ID", example = "1", required = true)
            @PathVariable Long id);
}
