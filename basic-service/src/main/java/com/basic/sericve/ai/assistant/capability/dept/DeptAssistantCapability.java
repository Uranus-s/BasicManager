package com.basic.sericve.ai.assistant.capability.dept;

import com.basic.api.dto.sysDept.DeptAddDTO;
import com.basic.api.dto.sysDept.DeptQueryDTO;
import com.basic.api.dto.sysDept.DeptUpdateDTO;
import com.basic.api.vo.sysDept.DeptTreeVO;
import com.basic.api.vo.sysDept.DeptVO;
import com.basic.api.vo.sysUser.UserListVO;
import com.basic.common.result.PageResult;
import com.basic.sericve.ai.assistant.AssistantRequestContext;
import com.basic.sericve.ai.assistant.capability.AssistantCapability;
import com.basic.sericve.ai.assistant.operation.AssistantOperationSpec;
import com.basic.sericve.ai.assistant.operation.AssistantOperations;
import com.basic.sericve.sysDept.service.ISysDeptService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.basic.sericve.ai.assistant.operation.AssistantOperations.action;
import static com.basic.sericve.ai.assistant.operation.AssistantOperations.fields;
import static com.basic.sericve.ai.assistant.operation.AssistantOperations.query;
import static com.basic.sericve.ai.assistant.operation.AssistantOperations.result;

/** 部门 AI 能力，以 Operation Builder 声明查询和单操作写入。 */
@Component
@RequiredArgsConstructor
public class DeptAssistantCapability implements AssistantCapability {

    private static final String DEPT_QUERY = "system:dept:query";
    private static final int MAX_SEARCH_RESULTS = 10;
    private static final int MAX_TREE_RESULTS = 200;
    private static final int MAX_USER_RESULTS = 20;

    private final ISysDeptService deptService;

    @Override
    public String capabilityId() {
        return "dept";
    }

    @Override
    public List<AssistantOperationSpec<?, ?>> operations() {
        List<AssistantOperationSpec<?, ?>> operations = new ArrayList<>();
        operations.add(query("dept_search", DeptSearchInput.class, DeptSearchResult.class)
                .description("按部门名称、负责人或父部门ID查询，最多返回10条摘要")
                .permissions(DEPT_QUERY)
                .execute(this::search)
                .build());
        operations.add(query("dept_get", DeptGetInput.class, DeptDetailResult.class)
                .description("按ID读取部门详情；修改部门前应先读取当前完整字段")
                .permissions(DEPT_QUERY)
                .execute(input -> toDetail(deptService.getDeptById(input.id())))
                .build());
        operations.add(AssistantOperations.<List<DeptNodeResult>>query(
                        "dept_tree", List.class)
                .description("查询部门树，按深度优先展开且最多返回200个节点")
                .permissions(DEPT_QUERY)
                .execute(this::deptTree)
                .build());
        operations.add(query("dept_users", DeptUsersInput.class, DeptUsersResult.class)
                .description("查询指定部门下的用户摘要，最多返回20条")
                .permissions(DEPT_QUERY)
                .executeWithContext((input, context) -> deptUsers(input))
                .build());
        operations.add(action("dept_create_propose", DeptCreateActionPayload.class)
                .actionType("DEPT_CREATE")
                .description("生成新增单个部门的预览并等待用户确认；不会直接新增")
                .permissions("system:dept:add")
                .preview("新增部门待确认", payload -> fields(
                        "部门名称", payload.deptName(),
                        "父部门ID", payload.parentId(),
                        "负责人", payload.leader(),
                        "联系电话", payload.phone(),
                        "排序", payload.sort()))
                .validate(payload -> deptService.validateDeptParent(null, payload.parentId()))
                .execute(payload -> {
                    Long deptId = deptService.addDept(toAddDto(payload));
                    return result(deptId, "部门新增成功，部门ID：" + deptId);
                })
                .build());
        operations.add(action("dept_update_propose", DeptUpdateActionPayload.class)
                .actionType("DEPT_UPDATE")
                .description("生成修改单个部门的全量预览并等待用户确认；不会直接修改")
                .permissions("system:dept:edit")
                .preview("修改部门待确认", payload -> fields(
                        "部门ID", payload.id(),
                        "部门名称", payload.deptName(),
                        "父部门ID", payload.parentId(),
                        "负责人", payload.leader(),
                        "联系电话", payload.phone(),
                        "排序", payload.sort()))
                .validate(payload -> {
                    deptService.getDeptById(payload.id());
                    deptService.validateDeptParent(payload.id(), payload.parentId());
                })
                .execute(payload -> {
                    deptService.updateDept(toUpdateDto(payload));
                    return result(payload.id(), "部门修改成功，部门ID：" + payload.id());
                })
                .build());
        operations.add(action("dept_delete_propose", DeptDeleteActionPayload.class)
                .actionType("DEPT_DELETE")
                .description("生成删除单个部门的预览并等待用户确认；不会直接删除")
                .permissions("system:dept:delete")
                .preview("删除部门待确认", payload -> fields("部门ID", payload.id()))
                .validate(payload -> deptService.validateDeptDelete(payload.id()))
                .execute(payload -> {
                    deptService.deleteDept(payload.id());
                    return result(payload.id(), "部门删除成功，部门ID：" + payload.id());
                })
                .build());
        return List.copyOf(operations);
    }

    private DeptSearchResult search(DeptSearchInput input) {
        DeptQueryDTO query = new DeptQueryDTO();
        query.setDeptName(trimToNull(input.deptName()));
        query.setLeader(trimToNull(input.leader()));
        query.setParentId(input.parentId());
        query.setPageNum(1);
        query.setPageSize(MAX_SEARCH_RESULTS);
        PageResult<DeptVO> page = deptService.getDeptList(query);
        List<DeptSummary> items = page.getList() == null ? List.of() : page.getList().stream()
                .limit(MAX_SEARCH_RESULTS)
                .map(DeptAssistantCapability::toSummary)
                .toList();
        return new DeptSearchResult(page.getTotal(), items);
    }

    private List<DeptNodeResult> deptTree() {
        List<DeptNodeResult> nodes = new ArrayList<>();
        flattenTree(deptService.getDeptTree(), nodes);
        return List.copyOf(nodes);
    }

    private DeptUsersResult deptUsers(DeptUsersInput input) {
        List<UserListVO> users = deptService.getUsersByDeptId(input.deptId());
        int total = users == null ? 0 : users.size();
        List<UserSummary> items = users == null ? List.of() : users.stream()
                .limit(MAX_USER_RESULTS)
                .map(DeptAssistantCapability::toUserSummary)
                .toList();
        return new DeptUsersResult((long) total, items, total > MAX_USER_RESULTS);
    }

    private static DeptAddDTO toAddDto(DeptCreateActionPayload payload) {
        DeptAddDTO dto = new DeptAddDTO();
        dto.setParentId(payload.parentId());
        dto.setDeptName(payload.deptName());
        dto.setLeader(payload.leader());
        dto.setPhone(payload.phone());
        dto.setSort(payload.sort());
        return dto;
    }

    private static DeptUpdateDTO toUpdateDto(DeptUpdateActionPayload payload) {
        DeptUpdateDTO dto = new DeptUpdateDTO();
        dto.setId(payload.id());
        dto.setParentId(payload.parentId());
        dto.setDeptName(payload.deptName());
        dto.setLeader(payload.leader());
        dto.setPhone(payload.phone());
        dto.setSort(payload.sort());
        return dto;
    }

    private static DeptSummary toSummary(DeptVO dept) {
        return new DeptSummary(dept.getId(), dept.getParentId(), dept.getParentName(),
                dept.getDeptName(), dept.getLeader(), dept.getSort());
    }

    private static DeptDetailResult toDetail(DeptVO dept) {
        return new DeptDetailResult(dept.getId(), dept.getParentId(), dept.getParentName(),
                dept.getDeptName(), dept.getLeader(), dept.getPhone(), dept.getSort());
    }

    private static UserSummary toUserSummary(UserListVO user) {
        List<String> roleNames = user.getRoleNames() == null ? List.of()
                : user.getRoleNames().stream().filter(StringUtils::hasText).limit(20).toList();
        return new UserSummary(user.getId(), user.getUsername(), user.getNickname(), user.getStatus(), roleNames);
    }

    private static void flattenTree(List<DeptTreeVO> tree, List<DeptNodeResult> output) {
        if (tree == null || output.size() >= MAX_TREE_RESULTS) {
            return;
        }
        for (DeptTreeVO node : tree) {
            if (output.size() >= MAX_TREE_RESULTS) {
                return;
            }
            output.add(new DeptNodeResult(node.getId(), node.getParentId(),
                    node.getDeptName(), node.getLeader(), node.getSort()));
            flattenTree(node.getChildren(), output);
        }
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    /** 新增部门待审批快照，字段与现有新增 DTO 保持一致。 */
    public record DeptCreateActionPayload(
            @NotNull(message = "父部门ID不能为空") Long parentId,
            @NotBlank(message = "部门名称不能为空")
            @Size(max = 50, message = "部门名称长度不能超过50") String deptName,
            @Size(max = 50, message = "负责人长度不能超过50") String leader,
            @Size(max = 20, message = "联系电话长度不能超过20") String phone,
            Integer sort) {
    }

    /** 修改部门待审批快照，保存确认时所需的完整字段。 */
    public record DeptUpdateActionPayload(
            @NotNull(message = "部门ID不能为空") Long id,
            @NotNull(message = "父部门ID不能为空") Long parentId,
            @NotBlank(message = "部门名称不能为空")
            @Size(max = 50, message = "部门名称长度不能超过50") String deptName,
            @Size(max = 50, message = "负责人长度不能超过50") String leader,
            @Size(max = 20, message = "联系电话长度不能超过20") String phone,
            Integer sort) {
    }

    /** 删除部门待审批快照，仅保存目标部门 ID。 */
    public record DeptDeleteActionPayload(
            @NotNull(message = "部门ID不能为空") Long id) {
    }

    public record DeptSearchInput(String deptName, String leader, Long parentId) {
    }

    public record DeptGetInput(@NotNull(message = "部门ID不能为空") Long id) {
    }

    public record DeptUsersInput(@NotNull(message = "部门ID不能为空") Long deptId) {
    }

    public record DeptSearchResult(Long total, List<DeptSummary> items) {
    }

    public record DeptSummary(Long id, Long parentId, String parentName,
                              String deptName, String leader, Integer sort) {
    }

    public record DeptDetailResult(Long id, Long parentId, String parentName,
                                   String deptName, String leader, String phone, Integer sort) {
    }

    public record DeptNodeResult(Long id, Long parentId, String deptName,
                                 String leader, Integer sort) {
    }

    public record DeptUsersResult(Long total, List<UserSummary> items, boolean truncated) {
    }

    public record UserSummary(Long id, String username, String nickname, Byte status, List<String> roleNames) {
    }
}
