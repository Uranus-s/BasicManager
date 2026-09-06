package com.basic.sericve.ai.assistant.capability.role;

import com.basic.api.dto.sysRole.RoleQueryDTO;
import com.basic.api.vo.sysRole.RoleListVO;
import com.basic.api.vo.sysRole.RoleVO;
import com.basic.api.vo.sysUser.UserListVO;
import com.basic.common.result.PageResult;
import com.basic.sericve.ai.assistant.capability.AssistantCapability;
import com.basic.sericve.ai.assistant.operation.AssistantOperationSpec;
import com.basic.sericve.sysRole.service.ISysRoleService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.basic.sericve.ai.assistant.operation.AssistantOperations.query;

/** 角色只读 AI 能力，不包含角色创建、修改或关联分配操作。 */
@Component
@RequiredArgsConstructor
public class RoleAssistantCapability implements AssistantCapability {

    private static final String ROLE_QUERY = "system:role:query";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 20;
    private static final int MAX_ASSOCIATED_ITEMS = 20;

    private final ISysRoleService roleService;

    @Override
    public String capabilityId() { return "role"; }

    @Override
    public List<AssistantOperationSpec<?, ?>> operations() {
        List<AssistantOperationSpec<?, ?>> operations = new ArrayList<>();
        operations.add(query("role_search", RoleSearchInput.class, RoleSearchResult.class)
                .description("按角色编码、名称或状态查询角色，分页默认10条且最多20条")
                .permissions(ROLE_QUERY).execute(this::search).build());
        operations.add(query("role_get", RoleIdInput.class, RoleDetailResult.class)
                .description("按ID读取角色安全详情").permissions(ROLE_QUERY)
                .execute(input -> toDetail(roleService.getRoleById(input.id()))).build());
        operations.add(query("role_permissions", RoleIdInput.class, RolePermissionsResult.class)
                .description("按角色ID读取其权限ID列表").permissions(ROLE_QUERY)
                .execute(input -> {
                    List<Long> permissionIds = roleService.getRolePermissions(input.id());
                    int total = permissionIds == null ? 0 : permissionIds.size();
                    return new RolePermissionsResult(input.id(), (long) total, limit(permissionIds),
                            total > MAX_ASSOCIATED_ITEMS);
                }).build());
        operations.add(query("role_users", RoleIdInput.class, RoleUsersResult.class)
                .description("按角色ID读取用户安全摘要，最多返回20条").permissions(ROLE_QUERY)
                .execute(input -> {
                    List<UserListVO> users = roleService.getUsersByRoleId(input.id());
                    int total = users == null ? 0 : users.size();
                    List<UserSummary> items = users == null ? List.of() : users.stream().limit(MAX_ASSOCIATED_ITEMS)
                            .map(RoleAssistantCapability::toUserSummary).toList();
                    return new RoleUsersResult(input.id(), (long) total, items, total > MAX_ASSOCIATED_ITEMS);
                }).build());
        return List.copyOf(operations);
    }

    private RoleSearchResult search(RoleSearchInput input) {
        RoleQueryDTO query = new RoleQueryDTO();
        query.setRoleCode(trimToNull(input.roleCode()));
        query.setRoleName(trimToNull(input.roleName()));
        query.setStatus(input.status());
        query.setPageNum(pageNum(input.pageNum()));
        query.setPageSize(pageSize(input.pageSize()));
        PageResult<RoleListVO> page = roleService.getRoleList(query);
        List<RoleSummary> items = page.getList() == null ? List.of() : page.getList().stream()
                .limit(MAX_PAGE_SIZE).map(role -> new RoleSummary(role.getId(), role.getRoleCode(), role.getRoleName(),
                        role.getStatus(), role.getRemark(), role.getCreateTime())).toList();
        return new RoleSearchResult(page.getTotal(), items);
    }

    private static RoleDetailResult toDetail(RoleVO role) {
        int permissionsTotal = role.getPermissions() == null ? 0 : role.getPermissions().size();
        List<PermissionSummary> permissions = role.getPermissions() == null ? List.of() : role.getPermissions().stream()
                .limit(MAX_ASSOCIATED_ITEMS).map(permission -> new PermissionSummary(permission.getId(), permission.getName(),
                        permission.getPermission())).toList();
        return new RoleDetailResult(role.getId(), role.getRoleCode(), role.getRoleName(), role.getStatus(), role.getRemark(),
                permissions, (long) permissionsTotal, permissionsTotal > MAX_ASSOCIATED_ITEMS,
                role.getCreateTime(), role.getUpdateTime());
    }

    private static UserSummary toUserSummary(UserListVO user) {
        return new UserSummary(user.getId(), user.getUsername(), user.getNickname(), user.getPhone(), user.getEmail(),
                user.getStatus(), safeNames(user.getDeptNames()), safeNames(user.getRoleNames()), user.getCreateTime());
    }
    private static List<String> safeNames(List<String> values) { return values == null ? List.of() : values.stream()
            .filter(StringUtils::hasText).limit(MAX_ASSOCIATED_ITEMS).toList(); }
    private static <T> List<T> limit(List<T> values) { return values == null ? List.of() : values.stream().limit(MAX_ASSOCIATED_ITEMS).toList(); }
    private static int pageNum(Integer value) { return value == null || value < 1 ? 1 : value; }
    private static int pageSize(Integer value) { return value == null ? DEFAULT_PAGE_SIZE : Math.max(1, Math.min(value, MAX_PAGE_SIZE)); }
    private static String trimToNull(String value) { return StringUtils.hasText(value) ? value.trim() : null; }

    public record RoleSearchInput(String roleCode, String roleName, Byte status, Integer pageNum, Integer pageSize) { }
    public record RoleIdInput(@NotNull(message = "角色ID不能为空") Long id) { }
    public record RoleSearchResult(Long total, List<RoleSummary> items) { }
    public record RoleSummary(Long id, String roleCode, String roleName, Byte status, String remark, LocalDateTime createTime) { }
    public record RoleDetailResult(Long id, String roleCode, String roleName, Byte status, String remark,
                                   List<PermissionSummary> permissions, Long permissionsTotal, boolean permissionsTruncated,
                                   LocalDateTime createTime, LocalDateTime updateTime) { }
    public record PermissionSummary(Long id, String name, String permission) { }
    public record RolePermissionsResult(Long roleId, Long total, List<Long> permissionIds, boolean truncated) { }
    public record RoleUsersResult(Long roleId, Long total, List<UserSummary> items, boolean truncated) { }
    public record UserSummary(Long id, String username, String nickname, String phone, String email, Byte status,
                              List<String> deptNames, List<String> roleNames, LocalDateTime createTime) { }
}
