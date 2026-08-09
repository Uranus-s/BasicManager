package com.basic.api.controller.sys;

import com.basic.api.dto.sysNotice.NoticeAddDTO;
import com.basic.api.dto.sysNotice.NoticeQueryDTO;
import com.basic.api.dto.sysNotice.NoticeUpdateDTO;
import com.basic.api.vo.sysNotice.NoticeListVO;
import com.basic.api.vo.sysNotice.NoticeTargetOptionsVO;
import com.basic.api.vo.sysNotice.NoticeVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 管理端公告接口契约，提供公告维护、发布和撤回能力。
 */
@Tag(name = "公告管理", description = "管理端公告维护、目标选择和状态流转接口")
public interface SysNoticeApi {

    /**
     * 分页查询管理端公告列表。
     *
     * @param dto 公告查询条件
     * @return 公告分页结果
     */
    @Operation(summary = "分页查询公告", description = "按标题、类型和状态分页查询公告")
    @GetMapping("/list")
    Result<PageResult<NoticeListVO>> getNoticeList(@Valid NoticeQueryDTO dto);

    /**
     * 查询管理端公告详情。
     *
     * @param id 公告 ID
     * @return 公告详情
     */
    @Operation(summary = "获取公告详情", description = "根据公告 ID 查询正文、状态和定向接收目标")
    @GetMapping("/{id}")
    Result<NoticeVO> getNoticeById(
            @Parameter(description = "公告 ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 查询公告定向接收目标选项。
     *
     * @return 有效角色和部门树
     */
    @Operation(summary = "获取公告接收目标", description = "返回可用于定向公告的有效角色和部门树")
    @GetMapping("/target-options")
    Result<NoticeTargetOptionsVO> getTargetOptions();

    /**
     * 新增公告草稿。
     *
     * @param dto 公告新增参数
     * @return 操作结果
     */
    @Operation(summary = "新增公告", description = "创建一条草稿状态的公告")
    @PostMapping
    Result<?> addNotice(@Valid @RequestBody NoticeAddDTO dto);

    /**
     * 修改公告内容和接收范围。
     *
     * @param dto 公告修改参数
     * @return 操作结果
     */
    @Operation(summary = "修改公告", description = "按数据版本修改公告内容和定向接收目标")
    @PutMapping
    Result<?> updateNotice(@Valid @RequestBody NoticeUpdateDTO dto);

    /**
     * 删除草稿或已撤回公告。
     *
     * @param id 公告 ID
     * @return 操作结果
     */
    @Operation(summary = "删除公告", description = "删除草稿状态或已撤回状态的公告")
    @DeleteMapping("/{id}")
    Result<?> deleteNotice(
            @Parameter(description = "公告 ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 发布公告。
     *
     * @param id 公告 ID
     * @return 操作结果
     */
    @Operation(summary = "发布公告", description = "发布草稿或重新发布已撤回公告")
    @PostMapping("/{id}/publish")
    Result<?> publishNotice(
            @Parameter(description = "公告 ID", example = "1", required = true)
            @PathVariable Long id);

    /**
     * 撤回已发布公告。
     *
     * @param id 公告 ID
     * @return 操作结果
     */
    @Operation(summary = "撤回公告", description = "将已发布公告撤回并停止对用户展示")
    @PostMapping("/{id}/withdraw")
    Result<?> withdrawNotice(
            @Parameter(description = "公告 ID", example = "1", required = true)
            @PathVariable Long id);
}
