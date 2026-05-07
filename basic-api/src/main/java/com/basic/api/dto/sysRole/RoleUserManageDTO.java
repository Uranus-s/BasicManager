package com.basic.api.dto.sysRole;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.util.List;

/**
 * 角色用户增量管理DTO
 *
 * @author Gas
 */
@Data
public class RoleUserManageDTO {

    /**
     * 需要新增当前角色的用户ID列表
     */
    private List<Long> addUserIds;

    /**
     * 需要移除当前角色的用户ID列表
     */
    private List<Long> removeUserIds;

    @AssertTrue(message = "新增用户和移除用户不能同时为空")
    public boolean isNotEmptyOperation() {
        return (addUserIds != null && !addUserIds.isEmpty())
                || (removeUserIds != null && !removeUserIds.isEmpty());
    }
}
