package com.basic.web.controller;

import com.basic.api.dto.sysConfig.ConfigBasicUpdateDTO;
import com.basic.web.controller.sys.SysConfigController;
import com.basic.web.controller.common.PublicConfigController;
import com.basic.web.controller.sys.SysDeptController;
import com.basic.web.controller.sys.SysDictController;
import com.basic.web.controller.sys.SysDictItemController;
import com.basic.web.controller.sys.SysFileController;
import com.basic.web.controller.sys.SysLoginLogController;
import com.basic.web.controller.sys.SysLogController;
import com.basic.web.controller.sys.SysMonitorController;
import com.basic.web.controller.sys.SysOperLogController;
import com.basic.web.controller.sys.SysPermissionController;
import com.basic.web.controller.sys.SysRoleController;
import com.basic.web.controller.sys.SysUserController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 后台接口权限契约测试，防止 Controller 注解与初始化权限数据发生漂移。
 */
class ControllerPermissionContractTest {

    private static final Map<Class<?>, Map<String, String>> MANAGEMENT_PERMISSIONS = Map.ofEntries(
            entry(SysUserController.class, Map.ofEntries(
                    entry("addUser", "system:user:add"),
                    entry("updateUser", "system:user:edit"),
                    entry("deleteUser", "system:user:delete"),
                    entry("getUserById", "system:user:query"),
                    entry("getUserList", "system:user:query"),
                    entry("resetPassword", "system:user:resetPwd"),
                    entry("assignRoles", "system:user:assignRole"),
                    entry("getUserRoles", "system:user:query"))),
            entry(SysRoleController.class, Map.ofEntries(
                    entry("addRole", "system:role:add"),
                    entry("updateRole", "system:role:edit"),
                    entry("deleteRole", "system:role:delete"),
                    entry("getRoleById", "system:role:query"),
                    entry("getRoleList", "system:role:query"),
                    entry("getAllRoles", "system:role:query"),
                    entry("assignPermissions", "system:role:assignPermission"),
                    entry("getRolePermissions", "system:role:query"),
                    entry("getUsersByRoleId", "system:role:query"),
                    entry("manageRoleUsers", "system:role:assignUser"))),
            entry(SysPermissionController.class, Map.of(
                    "addPermission", "system:permission:add",
                    "updatePermission", "system:permission:edit",
                    "deletePermission", "system:permission:delete",
                    "getPermissionById", "system:permission:query",
                    "getPermissionList", "system:permission:query",
                    "getPermissionTree", "system:permission:query",
                    "getAllPermissions", "system:permission:query",
                    "getUserPermissions", "system:permission:query")),
            entry(SysDeptController.class, Map.ofEntries(
                    entry("addDept", "system:dept:add"),
                    entry("updateDept", "system:dept:edit"),
                    entry("deleteDept", "system:dept:delete"),
                    entry("getDeptById", "system:dept:query"),
                    entry("getDeptList", "system:dept:query"),
                    entry("getDeptTree", "system:dept:query"),
                    entry("getAllDepts", "system:dept:query"),
                    entry("getUsersByDeptId", "system:dept:query"),
                    entry("addUsersToDept", "system:dept:assignUser"),
                    entry("removeUsersFromDept", "system:dept:assignUser"))),
            entry(SysDictController.class, Map.of(
                    "addDict", "system:dict:add",
                    "updateDict", "system:dict:edit",
                    "deleteDict", "system:dict:delete",
                    "getDictById", "system:dict:query",
                    "getDictList", "system:dict:query",
                    "getAllDicts", "system:dict:query")),
            entry(SysDictItemController.class, Map.of(
                    "addDictItem", "system:dict:item:add",
                    "updateDictItem", "system:dict:item:edit",
                    "deleteDictItem", "system:dict:item:delete",
                    "getDictItemById", "system:dict:item:query",
                    "getDictItemList", "system:dict:item:query",
                    "getDictItemsByDictId", "system:dict:item:query",
                    "getDictItemsByDictCode", "system:dict:item:query")),
            entry(SysConfigController.class, Map.of(
                    "getSettings", "system:config:query",
                    "updateBasicSettings", "system:config:edit",
                    "updateSecuritySettings", "system:config:edit")),
            entry(SysFileController.class, Map.of(
                    "uploadFile", "system:file:upload",
                    "upload", "system:file:upload",
                    "deleteFile", "system:file:delete",
                    "deleteFiles", "system:file:delete",
                    "getFileById", "system:file:query",
                    "getFileList", "system:file:query")),
            entry(SysLogController.class, Map.of("getLogList", "system:log:query")),
            entry(SysLoginLogController.class, Map.of(
                    "getLoginLogById", "system:loginLog:query",
                    "getLoginLogList", "system:loginLog:query",
                    "deleteLoginLog", "system:loginLog:delete",
                    "deleteLoginLogs", "system:loginLog:batchDelete",
                    "clearLoginLog", "system:loginLog:clear")),
            entry(SysOperLogController.class, Map.of(
                    "getOperLogById", "system:operLog:query",
                    "getOperLogList", "system:operLog:query",
                    "deleteOperLog", "system:operLog:delete",
                    "deleteOperLogs", "system:operLog:batchDelete",
                    "clearOperLog", "system:operLog:clear")),
            entry(SysMonitorController.class, Map.of(
                    "getStatus", "system:monitor:view",
                    "getThreadPoolStatus", "system:monitor:view")),
            entry(AuthController.class, Map.of(
                    "getOnlineUsers", "auth:online:list",
                    "forceLogout", "auth:online:forceLogout")));

    @Test
    void managementMethodsShouldDeclareExpectedPermission() {
        MANAGEMENT_PERMISSIONS.forEach((controllerClass, permissions) ->
                permissions.forEach((methodName, permission) -> {
                    Method method = findMethod(controllerClass, methodName);
                    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

                    assertThat(annotation)
                            .as("%s#%s 缺少 @PreAuthorize", controllerClass.getSimpleName(), methodName)
                            .isNotNull();
                    assertThat(annotation.value())
                            .as("%s#%s 权限标识不正确", controllerClass.getSimpleName(), methodName)
                            .isEqualTo("hasAuthority('" + permission + "')");
                }));
    }

    @Test
    void currentUserSelfServiceMethodsShouldOnlyRequireLogin() {
        assertThat(findMethod(SysUserController.class, "updatePassword").getAnnotation(PreAuthorize.class)).isNull();
        assertThat(findMethod(SysUserController.class, "updateCurrentUserAvatar").getAnnotation(PreAuthorize.class)).isNull();
        assertThat(findMethod(SysUserController.class, "getCurrentUserAvatar").getAnnotation(PreAuthorize.class)).isNull();
    }

    @Test
    void publicConfigShouldNotRequireManagementPermission() {
        assertThat(findMethod(PublicConfigController.class, "getPublicSettings")
                .getAnnotation(PreAuthorize.class)).isNull();
    }

    @Test
    void initializationSqlShouldContainEveryControllerPermission() throws IOException {
        String sql = Files.readString(resolveInitSql(), StandardCharsets.UTF_8);

        MANAGEMENT_PERMISSIONS.values().stream()
                .flatMap(permissionMap -> permissionMap.values().stream())
                .distinct()
                .forEach(permission -> assertThat(sql)
                        .as("初始化 SQL 缺少权限 %s", permission)
                        .contains("'" + permission + "'"));
    }

    @Test
    void initializationSqlShouldDefineTypedSystemSettings() throws IOException {
        String sql = Files.readString(resolveInitSql(), StandardCharsets.UTF_8);

        assertThat(sql)
                .contains("'系统设置', 'MENU', '/system/config'")
                .contains("'system/config/index.vue'")
                .contains("'system:config:query'")
                .contains("'system:config:edit'")
                .contains("'sys.login.tokenExpireHours', '24'")
                .contains("UNIQUE KEY uk_sys_config_key_deleted")
                .doesNotContain("'system:config:add'")
                .doesNotContain("'system:config:delete'")
                .doesNotContain("'sys.user.initPassword'")
                .doesNotContain("'sys.login.tokenExpire'");
    }

    @Test
    void constantHomeRouteShouldBeVisibleInNavigation() throws IOException {
        String routerConfig = Files.readString(resolveRouterConfig(), StandardCharsets.UTF_8);

        assertThat(routerConfig)
                .as("固定首页路由的父节点必须显示，否则首页不会出现在侧栏")
                .containsPattern("redirect:\\s*[\"']/index[\"'],\\s*hidden:\\s*false");
    }

    @Test
    void basicSettingsValidationShouldMeasureTrimmedSystemName() {
        ConfigBasicUpdateDTO dto = new ConfigBasicUpdateDTO();
        dto.setSystemName("  " + "系".repeat(50) + "  ");
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        assertThat(validator.validate(dto)).isEmpty();
    }

    private static Method findMethod(Class<?> controllerClass, String methodName) {
        return Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(controllerClass.getSimpleName() + " 不存在方法 " + methodName));
    }

    private static Path resolveInitSql() {
        Path moduleRelativePath = Path.of("..", "initSql.sql").normalize();
        return Files.exists(moduleRelativePath) ? moduleRelativePath : Path.of("initSql.sql");
    }

    private static Path resolveRouterConfig() {
        Path moduleRelativePath = Path.of("..", "basic-ui", "src", "router", "index.js").normalize();
        return Files.exists(moduleRelativePath)
                ? moduleRelativePath
                : Path.of("basic-ui", "src", "router", "index.js");
    }
}
