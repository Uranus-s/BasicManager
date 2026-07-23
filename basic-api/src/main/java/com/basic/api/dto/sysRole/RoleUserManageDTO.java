package com.basic.api.dto.sysRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.util.List;

/**
 * 角色用户增量管理DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "角色用户关联管理请求参数")
public class RoleUserManageDTO {

    /**
     * 需要新增当前角色的用户ID列表
     */
    @Schema(description = "需要添加到角色的用户 ID 列表", example = "[1, 2]")
    private List<Long> addUserIds;

    /**
     * 需要移除当前角色的用户ID列表
     */
    @Schema(description = "需要从角色移除的用户 ID 列表", example = "[3, 4]")
    private List<Long> removeUserIds;

    @AssertTrue(message = "新增用户和移除用户不能同时为空")
    public boolean isNotEmptyOperation() {
        return (addUserIds != null && !addUserIds.isEmpty())
                || (removeUserIds != null && !removeUserIds.isEmpty());
    }
}
