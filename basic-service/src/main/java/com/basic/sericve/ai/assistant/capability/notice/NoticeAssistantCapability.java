package com.basic.sericve.ai.assistant.capability.notice;

import com.basic.api.dto.sysNotice.NoticeAddDTO;
import com.basic.api.dto.sysNotice.NoticeQueryDTO;
import com.basic.api.dto.sysNotice.NoticeUpdateDTO;
import com.basic.api.dto.sysNotice.NoticeVisibleQueryDTO;
import com.basic.api.vo.sysDictItem.DictItemVO;
import com.basic.api.vo.sysNotice.NoticeListVO;
import com.basic.api.vo.sysNotice.NoticeTargetOptionsVO;
import com.basic.api.vo.sysNotice.NoticeVO;
import com.basic.api.vo.sysNotice.NoticeDetailVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.sericve.ai.assistant.action.model.ActionPreviewField;
import com.basic.sericve.ai.assistant.capability.AssistantCapability;
import com.basic.sericve.ai.assistant.AssistantRequestContext;
import com.basic.sericve.ai.assistant.operation.AssistantOperationSpec;
import com.basic.sericve.ai.assistant.operation.AssistantOperations;
import com.basic.sericve.sysDictItem.service.ISysDictItemService;
import com.basic.sericve.sysNotice.service.ISysNoticeService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.basic.sericve.ai.assistant.operation.AssistantOperations.action;
import static com.basic.sericve.ai.assistant.operation.AssistantOperations.query;
import static com.basic.sericve.ai.assistant.operation.AssistantOperations.result;

/** 公告 AI 能力，以 Operation Builder 声明查询和单操作写入。 */
@Component
@RequiredArgsConstructor
public class NoticeAssistantCapability implements AssistantCapability {

    private static final String NOTICE_QUERY = "system:notice:query";
    private static final String NOTICE_TYPE_DICT = "sys_notice_type";
    private static final String SCOPE_ALL = "ALL";
    private static final int MAX_SEARCH_RESULTS = 10;
    private static final int MAX_VISIBLE_PAGE_SIZE = 20;
    private static final int MAX_DETAIL_CONTENT_LENGTH = 500;
    private static final int MAX_TARGET_COUNT = 100;

    private final ISysNoticeService noticeService;
    private final ISysDictItemService dictItemService;

    @Override
    public String capabilityId() {
        return "notice";
    }

    @Override
    public List<AssistantOperationSpec<?, ?>> operations() {
        List<AssistantOperationSpec<?, ?>> operations = new ArrayList<>();
        operations.add(query("notice_search", NoticeSearchInput.class, NoticeSearchResult.class)
                .description("按标题、类型或状态查询公告，最多返回10条摘要")
                .permissions(NOTICE_QUERY)
                .execute(this::search)
                .build());
        operations.add(query("notice_get", NoticeIdInput.class, NoticeDetailResult.class)
                .description("按ID读取公告详情和版本，正文最多返回500字")
                .permissions(NOTICE_QUERY)
                .execute(input -> toDetail(noticeService.getNoticeById(input.id())))
                .build());
        operations.add(query("notice_visible_search", NoticeVisibleSearchInput.class, NoticeVisibleSearchResult.class)
                .description("查询当前用户可见的已发布公告，分页默认10条且最多20条")
                .executeWithContext(this::visibleSearch)
                .build());
        operations.add(query("notice_visible_get", NoticeIdInput.class, NoticeVisibleDetailResult.class)
                .description("读取当前用户可见的公告详情，不暴露接收目标")
                .executeWithContext((input, context) -> visibleDetail(input, context))
                .build());
        operations.add(AssistantOperations.<List<NoticeTypeOption>>query(
                        "notice_type_list", List.class)
                .description("查询当前可用的公告类型")
                .permissions(NOTICE_QUERY)
                .execute(this::noticeTypes)
                .build());
        operations.add(query("notice_target_list", NoticeTargetOptionsVO.class)
                .description("查询公告可用的角色和部门接收目标")
                .permissions(NOTICE_QUERY)
                .execute(noticeService::getTargetOptions)
                .build());
        operations.add(action("notice_create_propose", NoticeCreateActionPayload.class)
                .actionType("NOTICE_CREATE")
                .description("生成新增单个公告草稿的预览并等待用户确认；不会直接新增")
                .permissions(NOTICE_QUERY, "system:notice:add")
                .preview("新增公告待确认", payload -> contentFields(
                        null, payload.title(), payload.noticeType(), payload.scopeType(),
                        payload.roleIds(), payload.deptIds()))
                .content(NoticeCreateActionPayload::content)
                .validate(this::validateDraft)
                .execute(payload -> {
                    Long noticeId = noticeService.addNotice(toAddDto(payload));
                    return result(noticeId, "公告草稿新增成功，公告ID：" + noticeId);
                })
                .build());
        operations.add(action(
                        "notice_update_propose",
                        NoticeUpdateInput.class,
                        NoticeUpdateActionPayload.class)
                .actionType("NOTICE_UPDATE")
                .description("生成修改单个公告的预览；省略字段保持当前值")
                .permissions(NOTICE_QUERY, "system:notice:edit")
                .prepare(this::prepareUpdate)
                .preview("修改公告待确认", payload -> contentFields(
                        payload.id(), payload.title(), payload.noticeType(), payload.scopeType(),
                        payload.roleIds(), payload.deptIds()))
                .content(NoticeUpdateActionPayload::content)
                .validate(this::validateUpdate)
                .execute(payload -> {
                    noticeService.updateNotice(toUpdateDto(payload));
                    return result(payload.id(), "公告修改成功，公告ID：" + payload.id());
                })
                .build());
        operations.add(action(
                        "notice_delete_propose",
                        NoticeIdInput.class,
                        NoticeExistingActionPayload.class)
                .actionType("NOTICE_DELETE")
                .description("生成删除单个草稿或已撤回公告的预览；不会直接删除")
                .permissions(NOTICE_QUERY, "system:notice:delete")
                .prepare(this::prepareExisting)
                .preview("删除公告待确认", NoticeAssistantCapability::lifecycleFields)
                .validate(payload -> noticeService.validateNoticeDelete(payload.id(), payload.version()))
                .execute(payload -> {
                    noticeService.deleteNotice(payload.id());
                    return result(payload.id(), "公告删除成功，公告ID：" + payload.id());
                })
                .build());
        operations.add(action("notice_create_and_publish_propose", NoticeCreateActionPayload.class)
                .actionType("NOTICE_PUBLISH")
                .description("生成新建并立即发布单个公告的预览；不会直接发布")
                .permissions(NOTICE_QUERY, "system:notice:add", "system:notice:publish")
                .preview("新建并发布公告待确认", payload -> contentFields(
                        null, payload.title(), payload.noticeType(), payload.scopeType(),
                        payload.roleIds(), payload.deptIds()))
                .content(NoticeCreateActionPayload::content)
                .validate(this::validateDraft)
                .execute(payload -> {
                    Long noticeId = noticeService.addAndPublishNotice(toAddDto(payload));
                    return result(noticeId, "公告新建并发布成功，公告ID：" + noticeId);
                })
                .build());
        operations.add(action(
                        "notice_publish_existing_propose",
                        NoticeIdInput.class,
                        NoticeExistingActionPayload.class)
                .actionType("NOTICE_PUBLISH_EXISTING")
                .description("生成发布单个已有草稿或已撤回公告的预览；不会直接发布")
                .permissions(NOTICE_QUERY, "system:notice:publish")
                .prepare(this::prepareExisting)
                .preview("发布已有公告待确认", NoticeAssistantCapability::existingContentFields)
                .content(NoticeExistingActionPayload::content)
                .validate(payload -> noticeService.validateNoticePublish(payload.id(), payload.version()))
                .execute(payload -> {
                    noticeService.publishNotice(payload.id());
                    return result(payload.id(), "公告发布成功，公告ID：" + payload.id());
                })
                .build());
        operations.add(action(
                        "notice_withdraw_propose",
                        NoticeIdInput.class,
                        NoticeExistingActionPayload.class)
                .actionType("NOTICE_WITHDRAW")
                .description("生成撤回单个已发布公告的预览；不会直接撤回")
                .permissions(NOTICE_QUERY, "system:notice:withdraw")
                .prepare(this::prepareExisting)
                .preview("撤回公告待确认", NoticeAssistantCapability::lifecycleFields)
                .validate(payload -> noticeService.validateNoticeWithdraw(payload.id(), payload.version()))
                .execute(payload -> {
                    noticeService.withdrawNotice(payload.id());
                    return result(payload.id(), "公告撤回成功，公告ID：" + payload.id());
                })
                .build());
        return List.copyOf(operations);
    }

    private NoticeSearchResult search(NoticeSearchInput input) {
        NoticeQueryDTO query = new NoticeQueryDTO();
        query.setTitle(trimToNull(input.title()));
        query.setNoticeType(trimToNull(input.noticeType()));
        query.setStatus(trimToNull(input.status()));
        query.setPageNum(1);
        query.setPageSize(MAX_SEARCH_RESULTS);
        PageResult<NoticeListVO> page = noticeService.getNoticeList(query);
        List<NoticeSummary> items = page.getList() == null ? List.of() : page.getList().stream()
                .limit(MAX_SEARCH_RESULTS)
                .map(NoticeAssistantCapability::toSummary)
                .toList();
        return new NoticeSearchResult(page.getTotal(), items);
    }

    private NoticeVisibleSearchResult visibleSearch(NoticeVisibleSearchInput input, AssistantRequestContext context) {
        NoticeVisibleQueryDTO query = new NoticeVisibleQueryDTO();
        query.setTitle(trimToNull(input.title()));
        query.setNoticeType(trimToNull(input.noticeType()));
        query.setPageNum(input.pageNum() == null || input.pageNum() < 1 ? 1 : input.pageNum());
        query.setPageSize(input.pageSize() == null ? 10 : Math.max(1, Math.min(input.pageSize(), MAX_VISIBLE_PAGE_SIZE)));
        PageResult<NoticeListVO> page = noticeService.getVisibleNoticeList(context.userId(), query);
        List<NoticeVisibleSummary> items = page.getList() == null ? List.of() : page.getList().stream()
                .limit(MAX_VISIBLE_PAGE_SIZE)
                .map(NoticeAssistantCapability::toVisibleSummary)
                .toList();
        return new NoticeVisibleSearchResult(page.getTotal(), items);
    }

    private NoticeVisibleDetailResult visibleDetail(NoticeIdInput input, AssistantRequestContext context) {
        NoticeDetailVO notice = noticeService.getVisibleNoticeById(context.userId(), input.id());
        return new NoticeVisibleDetailResult(notice.getId(), notice.getTitle(), notice.getNoticeType(),
                truncate(notice.getContent(), MAX_DETAIL_CONTENT_LENGTH), notice.getPublishTime());
    }

    private List<NoticeTypeOption> noticeTypes() {
        return dictItemService.getDictItemsByDictCode(NOTICE_TYPE_DICT).stream()
                .filter(item -> Objects.equals(item.getStatus(), (byte) 1))
                .map(NoticeAssistantCapability::toTypeOption)
                .toList();
    }

    /** 将增量输入和数据库当前值合并为确认阶段使用的不可变全量快照。 */
    private NoticeUpdateActionPayload prepareUpdate(NoticeUpdateInput input) {
        NoticeVO current = noticeService.getNoticeById(input.id());
        String scopeType = input.scopeType() == null ? current.getScopeType() : input.scopeType();
        List<Long> roleIds = SCOPE_ALL.equals(scopeType)
                ? List.of() : chooseTargets(input.roleIds(), current.getRoleIds());
        List<Long> deptIds = SCOPE_ALL.equals(scopeType)
                ? List.of() : chooseTargets(input.deptIds(), current.getDeptIds());
        return new NoticeUpdateActionPayload(
                current.getId(),
                choose(input.title(), current.getTitle()),
                choose(input.noticeType(), current.getNoticeType()),
                choose(input.content(), current.getContent()),
                scopeType,
                roleIds,
                deptIds,
                current.getVersion());
    }

    /** 已有公告状态操作只信任模型给出的 ID，其余字段全部读取服务端事实。 */
    private NoticeExistingActionPayload prepareExisting(NoticeIdInput input) {
        NoticeVO notice = noticeService.getNoticeById(input.id());
        return new NoticeExistingActionPayload(
                notice.getId(), notice.getTitle(), notice.getNoticeType(), notice.getContent(),
                notice.getScopeType(), notice.getStatus(), safeList(notice.getRoleIds()),
                safeList(notice.getDeptIds()), notice.getVersion());
    }

    private void validateDraft(NoticeCreateActionPayload payload) {
        validateTargetCount(payload.roleIds(), payload.deptIds());
        noticeService.validateNoticeDraft(toAddDto(payload));
    }

    private void validateUpdate(NoticeUpdateActionPayload payload) {
        validateTargetCount(payload.roleIds(), payload.deptIds());
        noticeService.validateNoticeUpdate(toUpdateDto(payload));
    }

    private static void validateTargetCount(List<Long> roleIds, List<Long> deptIds) {
        if (roleIds.size() + deptIds.size() > MAX_TARGET_COUNT) {
            throw new BusinessException(ResultEnum.PARAM_INVALID);
        }
    }

    private static NoticeAddDTO toAddDto(NoticeCreateActionPayload payload) {
        NoticeAddDTO dto = new NoticeAddDTO();
        dto.setTitle(payload.title());
        dto.setNoticeType(payload.noticeType());
        dto.setContent(payload.content());
        dto.setScopeType(payload.scopeType());
        dto.setRoleIds(payload.roleIds());
        dto.setDeptIds(payload.deptIds());
        return dto;
    }

    private static NoticeUpdateDTO toUpdateDto(NoticeUpdateActionPayload payload) {
        NoticeUpdateDTO dto = new NoticeUpdateDTO();
        dto.setId(payload.id());
        dto.setTitle(payload.title());
        dto.setNoticeType(payload.noticeType());
        dto.setContent(payload.content());
        dto.setScopeType(payload.scopeType());
        dto.setRoleIds(payload.roleIds());
        dto.setDeptIds(payload.deptIds());
        dto.setVersion(payload.version());
        return dto;
    }

    private static List<ActionPreviewField> contentFields(
            Long id, String title, String noticeType, String scopeType,
            List<Long> roleIds, List<Long> deptIds) {
        List<ActionPreviewField> fields = new ArrayList<>();
        if (id != null) {
            fields.add(field("公告ID", id));
        }
        fields.add(field("标题", title));
        fields.add(field("类型", noticeType));
        fields.add(field("范围", scopeLabel(scopeType)));
        if (!roleIds.isEmpty()) {
            fields.add(field("接收角色ID", joinIds(roleIds)));
        }
        if (!deptIds.isEmpty()) {
            fields.add(field("接收部门ID", joinIds(deptIds)));
        }
        return List.copyOf(fields);
    }

    private static List<ActionPreviewField> lifecycleFields(NoticeExistingActionPayload payload) {
        return List.of(
                field("公告ID", payload.id()),
                field("标题", payload.title()),
                field("当前状态", payload.status()));
    }

    private static List<ActionPreviewField> existingContentFields(NoticeExistingActionPayload payload) {
        List<ActionPreviewField> fields = new ArrayList<>(contentFields(
                payload.id(), payload.title(), payload.noticeType(), payload.scopeType(),
                payload.roleIds(), payload.deptIds()));
        fields.add(field("当前状态", payload.status()));
        return List.copyOf(fields);
    }

    private static ActionPreviewField field(String label, Object value) {
        return new ActionPreviewField(label, value == null ? "" : String.valueOf(value));
    }

    private static NoticeSummary toSummary(NoticeListVO notice) {
        return new NoticeSummary(notice.getId(), notice.getTitle(), notice.getNoticeType(),
                notice.getScopeType(), notice.getStatus(), notice.getPublishTime(), notice.getUpdateTime());
    }

    private static NoticeVisibleSummary toVisibleSummary(NoticeListVO notice) {
        return new NoticeVisibleSummary(notice.getId(), notice.getTitle(), notice.getNoticeType(),
                notice.getPublishTime());
    }

    private static NoticeDetailResult toDetail(NoticeVO notice) {
        return new NoticeDetailResult(
                notice.getId(), notice.getTitle(), notice.getNoticeType(), notice.getScopeType(),
                notice.getStatus(), truncate(notice.getContent(), MAX_DETAIL_CONTENT_LENGTH),
                notice.getVersion(), safeList(notice.getRoleIds()), safeList(notice.getDeptIds()),
                notice.getPublishTime(), notice.getUpdateTime());
    }

    private static NoticeTypeOption toTypeOption(DictItemVO item) {
        return new NoticeTypeOption(item.getItemValue(), item.getItemLabel());
    }

    private static String choose(String requested, String current) {
        return requested == null ? current : requested;
    }

    private static List<Long> chooseTargets(List<Long> requested, List<Long> current) {
        return requested == null ? safeList(current) : List.copyOf(requested);
    }

    private static List<Long> safeList(List<Long> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private static String scopeLabel(String scopeType) {
        return SCOPE_ALL.equals(scopeType) ? "全员" : "定向";
    }

    private static String joinIds(List<Long> ids) {
        return ids.stream().map(String::valueOf)
                .collect(java.util.stream.Collectors.joining("、"));
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /** 新建公告的不可变待审批快照，可用于保存草稿或新建后立即发布。 */
    public record NoticeCreateActionPayload(
            @NotBlank(message = "公告标题不能为空")
            @Size(max = 200, message = "公告标题长度不能超过200") String title,
            @NotBlank(message = "公告类型不能为空")
            @Size(max = 64, message = "公告类型长度不能超过64") String noticeType,
            @NotBlank(message = "公告正文不能为空")
            @Size(max = 100000, message = "公告正文长度不能超过100000") String content,
            @NotBlank(message = "接收范围不能为空")
            @Pattern(regexp = "ALL|TARGETED", message = "接收范围不正确") String scopeType,
            List<Long> roleIds,
            List<Long> deptIds) {

        public NoticeCreateActionPayload {
            roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
            deptIds = deptIds == null ? List.of() : List.copyOf(deptIds);
        }
    }

    /** 已有公告状态操作的服务端快照，模型只提供公告 ID。 */
    public record NoticeExistingActionPayload(
            @NotNull(message = "公告ID不能为空") Long id,
            @NotBlank(message = "公告标题不能为空") String title,
            @NotBlank(message = "公告类型不能为空") String noticeType,
            @NotBlank(message = "公告正文不能为空") String content,
            @NotBlank(message = "接收范围不能为空") String scopeType,
            @NotBlank(message = "公告状态不能为空") String status,
            List<Long> roleIds,
            List<Long> deptIds,
            @NotNull(message = "数据版本不能为空") Integer version) {

        public NoticeExistingActionPayload {
            roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
            deptIds = deptIds == null ? List.of() : List.copyOf(deptIds);
        }
    }

    /** 修改公告的不可变待审批快照，version 用于防止确认时覆盖后续变更。 */
    public record NoticeUpdateActionPayload(
            @NotNull(message = "公告ID不能为空") Long id,
            @NotBlank(message = "公告标题不能为空")
            @Size(max = 200, message = "公告标题长度不能超过200") String title,
            @NotBlank(message = "公告类型不能为空")
            @Size(max = 64, message = "公告类型长度不能超过64") String noticeType,
            @NotBlank(message = "公告正文不能为空")
            @Size(max = 100000, message = "公告正文长度不能超过100000") String content,
            @NotBlank(message = "接收范围不能为空")
            @Pattern(regexp = "ALL|TARGETED", message = "接收范围不正确") String scopeType,
            List<Long> roleIds,
            List<Long> deptIds,
            @NotNull(message = "数据版本不能为空") Integer version) {

        public NoticeUpdateActionPayload {
            roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
            deptIds = deptIds == null ? List.of() : List.copyOf(deptIds);
        }
    }

    public record NoticeSearchInput(String title, String noticeType, String status) {
    }

    public record NoticeVisibleSearchInput(String title, String noticeType, Integer pageNum, Integer pageSize) {
    }

    public record NoticeIdInput(@NotNull(message = "公告ID不能为空") Long id) {
    }

    /** null 表示保持当前值，空列表表示清空对应的定向接收目标。 */
    public record NoticeUpdateInput(
            @NotNull(message = "公告ID不能为空") Long id,
            String title,
            String noticeType,
            String content,
            String scopeType,
            List<Long> roleIds,
            List<Long> deptIds) {
    }

    public record NoticeSearchResult(Long total, List<NoticeSummary> items) {
    }

    public record NoticeVisibleSearchResult(Long total, List<NoticeVisibleSummary> items) {
    }

    public record NoticeVisibleSummary(Long id, String title, String noticeType, LocalDateTime publishTime) {
    }

    public record NoticeVisibleDetailResult(Long id, String title, String noticeType, String content,
                                            LocalDateTime publishTime) {
    }

    public record NoticeSummary(
            Long id,
            String title,
            String noticeType,
            String scopeType,
            String status,
            LocalDateTime publishTime,
            LocalDateTime updateTime) {
    }

    public record NoticeDetailResult(
            Long id,
            String title,
            String noticeType,
            String scopeType,
            String status,
            String content,
            Integer version,
            List<Long> roleIds,
            List<Long> deptIds,
            LocalDateTime publishTime,
            LocalDateTime updateTime) {
    }

    public record NoticeTypeOption(String value, String label) {
    }
}
