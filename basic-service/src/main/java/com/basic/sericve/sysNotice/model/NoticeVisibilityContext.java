package com.basic.sericve.sysNotice.model;

import java.util.List;

/**
 * 公告可见性查询上下文，保存当前用户有效角色和所属部门祖先 ID。
 *
 * @param roleIds 用户当前启用的角色 ID
 * @param deptIds 用户直属部门及其全部有效祖先部门 ID
 */
public record NoticeVisibilityContext(List<Long> roleIds, List<Long> deptIds) {

    public NoticeVisibilityContext {
        roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
        deptIds = deptIds == null ? List.of() : List.copyOf(deptIds);
    }
}
