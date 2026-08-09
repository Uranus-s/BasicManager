package com.basic.sericve.sysNotice.support;

import com.basic.dao.sysDept.entity.SysDept;
import com.basic.dao.sysRole.entity.SysRole;
import com.basic.sericve.sysDept.service.ISysDeptService;
import com.basic.sericve.sysNotice.model.NoticeVisibilityContext;
import com.basic.sericve.sysRole.service.ISysRoleService;
import com.basic.sericve.sysUserDept.service.ISysUserDeptService;
import com.basic.sericve.sysUserRole.service.ISysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 根据当前用户的实时角色和部门关系构建公告可见性查询条件。
 */
@Component
@RequiredArgsConstructor
public class NoticeVisibilityContextResolver {

    private final ISysUserRoleService userRoleService;
    private final ISysUserDeptService userDeptService;
    private final ISysDeptService deptService;
    private final ISysRoleService roleService;

    /**
     * 解析用户可用于匹配定向公告的角色和部门集合。
     *
     * @param userId 当前登录用户 ID
     * @return 可见性查询上下文
     */
    public NoticeVisibilityContext resolve(Long userId) {
        List<Long> roleIds = resolveActiveRoleIds(userId);
        List<Long> deptIds = resolveDepartmentIds(userId);
        return new NoticeVisibilityContext(roleIds, deptIds);
    }

    private List<Long> resolveActiveRoleIds(Long userId) {
        List<Long> rawRoleIds = distinct(userRoleService.getRoleIdsByUserId(userId));
        if (rawRoleIds.isEmpty()) {
            return List.of();
        }

        Set<Long> activeRoleIds = roleService.listByIds(rawRoleIds).stream()
                .filter(role -> role.getStatus() != null && role.getStatus() == 1)
                .map(SysRole::getId)
                .collect(Collectors.toSet());
        return rawRoleIds.stream()
                .filter(activeRoleIds::contains)
                .toList();
    }

    private List<Long> resolveDepartmentIds(Long userId) {
        List<Long> directDeptIds = distinct(userDeptService.getDeptIdsByUserId(userId));
        if (directDeptIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> parentById = deptService.list().stream()
                .collect(Collectors.toMap(
                        SysDept::getId,
                        dept -> dept.getParentId() == null ? 0L : dept.getParentId(),
                        (first, ignored) -> first
                ));

        Set<Long> visibleDeptIds = new LinkedHashSet<>();
        for (Long deptId : directDeptIds) {
            if (!parentById.containsKey(deptId)) {
                continue;
            }

            // 向上收集祖先；共享集合同时承担去重和脏数据循环保护。
            Long current = deptId;
            while (current != null && current != 0L && visibleDeptIds.add(current)) {
                current = parentById.get(current);
            }
        }
        return List.copyOf(visibleDeptIds);
    }

    private List<Long> distinct(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }
}
