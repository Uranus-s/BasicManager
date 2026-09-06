package com.basic.sericve.ai.assistant.capability.permission;

import com.basic.api.dto.sysPermission.PermissionQueryDTO;
import com.basic.api.vo.sysPermission.PermissionTreeVO;
import com.basic.api.vo.sysPermission.PermissionVO;
import com.basic.common.result.PageResult;
import com.basic.sericve.ai.assistant.capability.AssistantCapability;
import com.basic.sericve.ai.assistant.operation.AssistantOperationSpec;
import com.basic.sericve.sysPermission.service.ISysPermissionService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.basic.sericve.ai.assistant.operation.AssistantOperations.query;

/** 权限只读 AI 能力；权限树以扁平摘要返回并限制节点数量。 */
@Component
@RequiredArgsConstructor
public class PermissionAssistantCapability implements AssistantCapability {

    private static final String PERMISSION_QUERY = "system:permission:query";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 20;
    private static final int MAX_TREE_NODES = 20;

    private final ISysPermissionService permissionService;

    @Override
    public String capabilityId() { return "permission"; }

    @Override
    public List<AssistantOperationSpec<?, ?>> operations() {
        List<AssistantOperationSpec<?, ?>> operations = new ArrayList<>();
        operations.add(query("permission_search", PermissionSearchInput.class, PermissionSearchResult.class)
                .description("按名称、类型、父级或状态查询权限，分页默认10条且最多20条")
                .permissions(PERMISSION_QUERY).execute(this::search).build());
        operations.add(query("permission_get", PermissionIdInput.class, PermissionDetailResult.class)
                .description("按ID读取权限详情").permissions(PERMISSION_QUERY)
                .execute(input -> toDetail(permissionService.getPermissionById(input.id()))).build());
        operations.add(query("permission_tree", PermissionTreeResult.class)
                .description("读取权限树并按深度优先扁平化，最多返回20个节点")
                .permissions(PERMISSION_QUERY).execute(this::tree).build());
        return List.copyOf(operations);
    }

    private PermissionSearchResult search(PermissionSearchInput input) {
        PermissionQueryDTO query = new PermissionQueryDTO();
        query.setName(trimToNull(input.name()));
        query.setType(trimToNull(input.type()));
        query.setParentId(input.parentId());
        query.setStatus(input.status());
        query.setPageNum(pageNum(input.pageNum()));
        query.setPageSize(pageSize(input.pageSize()));
        PageResult<PermissionVO> page = permissionService.getPermissionList(query);
        List<PermissionSummary> items = page.getList() == null ? List.of() : page.getList().stream()
                .limit(MAX_PAGE_SIZE).map(PermissionAssistantCapability::toSummary).toList();
        return new PermissionSearchResult(page.getTotal(), items);
    }

    private PermissionTreeResult tree() {
        List<PermissionNode> nodes = new ArrayList<>();
        long total = flatten(permissionService.getPermissionTree(), nodes, 0);
        return new PermissionTreeResult(total, List.copyOf(nodes), total > MAX_TREE_NODES);
    }

    private static PermissionSummary toSummary(PermissionVO permission) {
        return new PermissionSummary(permission.getId(), permission.getParentId(), permission.getParentName(), permission.getName(),
                permission.getType(), permission.getPath(), permission.getComponent(), permission.getPermission(), permission.getIcon(),
                permission.getSort(), permission.getVisible(), permission.getStatus(), permission.getCreateTime(), permission.getUpdateTime());
    }
    private static PermissionDetailResult toDetail(PermissionVO permission) {
        return new PermissionDetailResult(permission.getId(), permission.getParentId(), permission.getParentName(), permission.getName(),
                permission.getType(), permission.getPath(), permission.getComponent(), permission.getPermission(), permission.getIcon(),
                permission.getSort(), permission.getVisible(), permission.getStatus(), permission.getCreateTime(), permission.getUpdateTime());
    }
    private static long flatten(List<PermissionTreeVO> source, List<PermissionNode> output, int level) {
        if (source == null) return 0;
        long total = 0;
        for (PermissionTreeVO node : source) {
            total++;
            if (output.size() < MAX_TREE_NODES) output.add(new PermissionNode(node.getId(), node.getParentId(), level, node.getName(),
                    node.getType(), node.getPath(), node.getComponent(), node.getPermission(), node.getIcon(), node.getSort(),
                    node.getVisible(), node.getStatus()));
            total += flatten(node.getChildren(), output, level + 1);
        }
        return total;
    }
    private static int pageNum(Integer value) { return value == null || value < 1 ? 1 : value; }
    private static int pageSize(Integer value) { return value == null ? DEFAULT_PAGE_SIZE : Math.max(1, Math.min(value, MAX_PAGE_SIZE)); }
    private static String trimToNull(String value) { return StringUtils.hasText(value) ? value.trim() : null; }

    public record PermissionSearchInput(String name, String type, Long parentId, Byte status, Integer pageNum, Integer pageSize) { }
    public record PermissionIdInput(@NotNull(message = "权限ID不能为空") Long id) { }
    public record PermissionSearchResult(Long total, List<PermissionSummary> items) { }
    public record PermissionSummary(Long id, Long parentId, String parentName, String name, String type, String path,
                                    String component, String permission, String icon, Integer sort, Byte visible, Byte status,
                                    LocalDateTime createTime, LocalDateTime updateTime) { }
    public record PermissionDetailResult(Long id, Long parentId, String parentName, String name, String type, String path,
                                         String component, String permission, String icon, Integer sort, Byte visible, Byte status,
                                         LocalDateTime createTime, LocalDateTime updateTime) { }
    public record PermissionNode(Long id, Long parentId, Integer level, String name, String type, String path, String component,
                                 String permission, String icon, Integer sort, Byte visible, Byte status) { }
    public record PermissionTreeResult(Long total, List<PermissionNode> items, boolean truncated) { }
}
