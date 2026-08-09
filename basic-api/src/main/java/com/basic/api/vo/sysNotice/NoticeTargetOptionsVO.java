package com.basic.api.vo.sysNotice;

import com.basic.api.vo.sysDept.DeptTreeVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 公告定向接收目标选项，供管理端选择有效角色和部门。
 */
@Data
@Schema(description = "公告定向接收目标选项")
public class NoticeTargetOptionsVO {

    /**
     * 可选的有效角色列表。
     */
    @Schema(description = "可选的有效角色列表")
    private List<RoleOption> roles;

    /**
     * 可选的有效部门树。
     */
    @Schema(description = "可选的有效部门树")
    private List<DeptTreeVO> departments;

    /**
     * 公告定向接收角色选项。
     */
    @Data
    @Schema(description = "公告定向接收角色选项")
    public static class RoleOption {

        /**
         * 角色 ID。
         */
        @Schema(description = "角色 ID", example = "1")
        private Long id;

        /**
         * 角色编码。
         */
        @Schema(description = "角色编码", example = "system_admin")
        private String roleCode;

        /**
         * 角色名称。
         */
        @Schema(description = "角色名称", example = "系统管理员")
        private String roleName;
    }
}
