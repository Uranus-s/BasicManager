package com.basic.sericve.ai.assistant.capability.user;

import com.basic.api.dto.sysUser.UserQueryDTO;
import com.basic.api.vo.sysUser.UserListVO;
import com.basic.api.vo.sysUser.UserVO;
import com.basic.common.result.PageResult;
import com.basic.sericve.ai.assistant.capability.AssistantCapability;
import com.basic.sericve.ai.assistant.operation.AssistantOperationSpec;
import com.basic.sericve.sysUser.service.ISysUserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.basic.sericve.ai.assistant.operation.AssistantOperations.query;

/** 用户只读 AI 能力，仅暴露安全摘要和关联 ID/权限标识。 */
@Component
@RequiredArgsConstructor
public class UserAssistantCapability implements AssistantCapability {

    private static final String USER_QUERY = "system:user:query";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 20;
    private static final int MAX_ASSOCIATED_ITEMS = 20;

    private final ISysUserService userService;

    @Override
    public String capabilityId() {
        return "user";
    }

    @Override
    public List<AssistantOperationSpec<?, ?>> operations() {
        List<AssistantOperationSpec<?, ?>> operations = new ArrayList<>();
        operations.add(query("user_search", UserSearchInput.class, UserSearchResult.class)
                .description("按账号、昵称、联系方式或状态查询用户，分页默认10条且最多20条")
                .permissions(USER_QUERY)
                .execute(this::search)
                .build());
        operations.add(query("user_get", UserIdInput.class, UserDetailResult.class)
                .description("按ID读取用户安全详情，不返回密码、令牌或头像内容")
                .permissions(USER_QUERY)
                .execute(input -> toDetail(userService.getUserById(input.id())))
                .build());
        operations.add(query("user_roles", UserIdInput.class, UserRolesResult.class)
                .description("按用户ID读取其角色ID列表")
                .permissions(USER_QUERY)
                .execute(input -> {
                    List<Long> roleIds = userService.getUserRoles(input.id());
                    int total = roleIds == null ? 0 : roleIds.size();
                    return new UserRolesResult(input.id(), (long) total, limit(roleIds), total > MAX_ASSOCIATED_ITEMS);
                })
                .build());
        operations.add(query("user_permissions", UserIdInput.class, UserPermissionsResult.class)
                .description("按用户ID读取其权限标识列表")
                .permissions(USER_QUERY)
                .execute(input -> {
                    List<String> permissions = userService.getUserPermissions(input.id());
                    int total = permissions == null ? 0 : permissions.size();
                    return new UserPermissionsResult(input.id(), (long) total, limit(permissions),
                            total > MAX_ASSOCIATED_ITEMS);
                })
                .build());
        return List.copyOf(operations);
    }

    private UserSearchResult search(UserSearchInput input) {
        UserQueryDTO query = new UserQueryDTO();
        query.setUsername(trimToNull(input.username()));
        query.setNickname(trimToNull(input.nickname()));
        query.setPhone(trimToNull(input.phone()));
        query.setEmail(trimToNull(input.email()));
        query.setStatus(input.status());
        query.setPageNum(pageNum(input.pageNum()));
        query.setPageSize(pageSize(input.pageSize()));
        PageResult<UserListVO> page = userService.getUserList(query);
        List<UserSummary> items = page.getList() == null ? List.of() : page.getList().stream()
                .limit(MAX_PAGE_SIZE).map(UserAssistantCapability::toSummary).toList();
        return new UserSearchResult(page.getTotal(), items);
    }

    private static UserSummary toSummary(UserListVO user) {
        return new UserSummary(user.getId(), user.getUsername(), user.getNickname(), user.getPhone(),
                user.getEmail(), user.getStatus(), safeNames(user.getDeptNames()), safeNames(user.getRoleNames()),
                user.getCreateTime());
    }

    private static UserDetailResult toDetail(UserVO user) {
        int rolesTotal = user.getRoles() == null ? 0 : user.getRoles().size();
        List<RoleSummary> roles = user.getRoles() == null ? List.of() : user.getRoles().stream()
                .limit(MAX_ASSOCIATED_ITEMS)
                .map(role -> new RoleSummary(role.getId(), role.getRoleCode(), role.getRoleName())).toList();
        return new UserDetailResult(user.getId(), user.getUsername(), user.getNickname(), user.getPhone(),
                user.getEmail(), user.getStatus(), safeNames(user.getDeptNames()), roles, (long) rolesTotal,
                rolesTotal > MAX_ASSOCIATED_ITEMS,
                user.getCreateTime(), user.getUpdateTime());
    }

    private static List<String> safeNames(List<String> values) {
        return values == null ? List.of() : values.stream().filter(StringUtils::hasText)
                .limit(MAX_ASSOCIATED_ITEMS).toList();
    }

    private static <T> List<T> limit(List<T> values) {
        return values == null ? List.of() : values.stream().limit(MAX_ASSOCIATED_ITEMS).toList();
    }

    private static int pageNum(Integer value) { return value == null || value < 1 ? 1 : value; }
    private static int pageSize(Integer value) { return value == null ? DEFAULT_PAGE_SIZE : Math.max(1, Math.min(value, MAX_PAGE_SIZE)); }
    private static String trimToNull(String value) { return StringUtils.hasText(value) ? value.trim() : null; }

    public record UserSearchInput(String username, String nickname, String phone, String email, Byte status,
                                  Integer pageNum, Integer pageSize) { }
    public record UserIdInput(@NotNull(message = "用户ID不能为空") Long id) { }
    public record UserSearchResult(Long total, List<UserSummary> items) { }
    public record UserSummary(Long id, String username, String nickname, String phone, String email, Byte status,
                              List<String> deptNames, List<String> roleNames, LocalDateTime createTime) { }
    public record UserDetailResult(Long id, String username, String nickname, String phone, String email, Byte status,
                                   List<String> deptNames, List<RoleSummary> roles, Long rolesTotal, boolean rolesTruncated,
                                   LocalDateTime createTime,
                                   LocalDateTime updateTime) { }
    public record RoleSummary(Long id, String roleCode, String roleName) { }
    public record UserRolesResult(Long userId, Long total, List<Long> roleIds, boolean truncated) { }
    public record UserPermissionsResult(Long userId, Long total, List<String> permissions, boolean truncated) { }
}
