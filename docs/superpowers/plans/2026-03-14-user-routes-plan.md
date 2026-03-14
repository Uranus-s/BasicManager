# 用户路由权限接口实施计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为前端提供独立接口 `/auth/routes`，返回当前登录用户拥有的路由权限（树形结构），用于动态生成侧边栏菜单。

**Architecture:** 新建 PermissionTreeVO 响应类，在 ISysUserService 添加 getUserRoutes 方法，通过用户角色获取权限列表并构建树形结构返回。

**Tech Stack:** Spring Boot, MyBatis-Plus, JWT

---

## Chunk 1: 修改 PermissionTreeVO 添加 component 字段

**Files:**
- Modify: `basic-api/src/main/java/com/basic/api/vo/sysPermission/PermissionTreeVO.java`

- [ ] **Step 1: 在 PermissionTreeVO 添加 component 字段**

在 PermissionTreeVO.java 中，在 `path` 字段后添加：

```java
/**
 * 前端组件路径
 */
private String component;
```

- [ ] **Step 2: Commit**

```bash
git add basic-api/src/main/java/com/basic/api/vo/sysPermission/PermissionTreeVO.java
git commit -m "feat(auth): add component field to PermissionTreeVO"
```

---

## Chunk 2: 添加 Service 层方法

**Files:**
- Modify: `basic-service/src/main/java/com/basic/sericve/sysUser/service/ISysUserService.java`
- Modify: `basic-service/src/main/java/com/basic/sericve/sysUser/impl/SysUserServiceImpl.java`

**Notes:**
- `sysPermissionService` 已在 `SysUserServiceImpl` 中注入，无需重复添加
- 使用 `LoginUser` 获取当前登录用户ID，而不是从 `Authentication.getName()`

- [ ] **Step 1: 在 ISysUserService 添加方法声明**

在 `ISysUserService.java` 文件末尾，`initAdmin` 方法后添加：

```java
/**
 * 获取用户路由权限
 *
 * @return 路由权限树形列表
 */
List<PermissionTreeVO> getUserRoutes();
```

- [ ] **Step 2: 在 SysUserServiceImpl 实现 getUserRoutes 方法**

在 `SysUserServiceImpl.java` 的 `getUserPermissions` 方法后添加：

```java
@Override
public List<PermissionTreeVO> getUserRoutes() {
    // 1. 获取当前登录用户ID
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    LoginUser loginUser = (LoginUser) authentication.getPrincipal();
    Long userId = loginUser.getUserId();

    // 2. 获取用户所有权限实体列表
    List<SysPermission> permissions = sysPermissionService.getPermissionsByUserId(userId);

    // 3. 过滤可用权限（status=1 且 visible=1）
    List<SysPermission> activePermissions = permissions.stream()
            .filter(p -> p.getStatus() != null && p.getStatus() == 1)
            .filter(p -> p.getVisible() == null || p.getVisible() == 1)
            .collect(Collectors.toList());

    // 4. 构建树形结构
    return buildPermissionTree(activePermissions);
}
```

- [ ] **Step 3: 添加构建树形结构的辅助方法**

在 `SysUserServiceImpl.java` 中，`getUserRoutes` 方法后添加：

```java
/**
 * 构建权限树形结构
 *
 * @param permissions 权限列表
 * @return 树形结构
 */
private List<PermissionTreeVO> buildPermissionTree(List<SysPermission> permissions) {
    // 按parentId分组
    Map<Long, List<SysPermission>> parentMap = permissions.stream()
            .collect(Collectors.groupingBy(p -> p.getParentId() != null ? p.getParentId() : 0L));

    // 获取所有顶级菜单（parentId = 0）
    List<SysPermission> rootPermissions = parentMap.getOrDefault(0L, Collections.emptyList());

    // 构建树
    return rootPermissions.stream()
            .map(p -> convertToPermissionTreeVO(p, parentMap))
            .sorted(Comparator.comparingInt(PermissionTreeVO::getSort))
            .collect(Collectors.toList());
}

/**
 * 递归转换权限为PermissionTreeVO
 *
 * @param permission 权限实体
 * @param parentMap  按parentId分组的映射
 * @return PermissionTreeVO
 */
private PermissionTreeVO convertToPermissionTreeVO(SysPermission permission, Map<Long, List<SysPermission>> parentMap) {
    PermissionTreeVO routeVO = new PermissionTreeVO();
    routeVO.setId(permission.getId());
    routeVO.setParentId(permission.getParentId());
    routeVO.setName(permission.getName());
    routeVO.setType(permission.getType());
    routeVO.setPath(permission.getPath());
    routeVO.setComponent(permission.getComponent());
    routeVO.setPermission(permission.getPermission());
    routeVO.setIcon(permission.getIcon());
    routeVO.setSort(permission.getSort());
    routeVO.setVisible(permission.getVisible());
    routeVO.setStatus(permission.getStatus());

    // 递归处理子权限
    List<SysPermission> children = parentMap.getOrDefault(permission.getId(), Collections.emptyList());
    if (!children.isEmpty()) {
        routeVO.setChildren(children.stream()
                .map(c -> convertToPermissionTreeVO(c, parentMap))
                .sorted(Comparator.comparingInt(PermissionTreeVO::getSort))
                .collect(Collectors.toList()));
    }

    return routeVO;
}
```

- [ ] **Step 4: 添加必要的 import**

在 `SysUserServiceImpl.java` 顶部添加（如果尚未存在）：
```java
import com.basic.api.vo.sysPermission.PermissionTreeVO;
import com.basic.dao.sysPermission.entity.SysPermission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.basic.core.security.model.LoginUser;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
```

- [ ] **Step 5: 在 ISysPermissionService 添加获取权限实体列表方法**

```java
/**
 * 获取用户权限实体列表
 *
 * @param userId 用户ID
 * @return 权限实体列表
 */
List<SysPermission> getPermissionsByUserId(Long userId);
```

- [ ] **Step 6: 在 SysPermissionServiceImpl 实现方法**

在 `SysPermissionServiceImpl.java` 中添加：

```java
@Override
public List<SysPermission> getPermissionsByUserId(Long userId) {
    // 获取用户角色
    List<Long> roleIds = sysUserRoleService.getRoleIdsByUserId(userId);
    if (roleIds.isEmpty()) {
        return new ArrayList<>();
    }

    // 获取角色权限ID
    Set<Long> permissionIds = new HashSet<>();
    for (Long roleId : roleIds) {
        List<Long> perms = sysRolePermissionService.getPermissionIdsByRoleId(roleId);
        permissionIds.addAll(perms);
    }

    if (permissionIds.isEmpty()) {
        return new ArrayList<>();
    }

    // 获取权限实体列表
    return listByIds(new ArrayList<>(permissionIds));
}
```

添加 import：
```java
import com.basic.dao.sysPermission.entity.SysPermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
```

- [ ] **Step 7: Commit**

```bash
git add basic-service/src/main/java/com/basic/sericve/sysUser/service/ISysUserService.java
git add basic-service/src/main/java/com/basic/sericve/sysUser/impl/SysUserServiceImpl.java
git add basic-service/src/main/java/com/basic/sericve/sysPermission/service/ISysPermissionService.java
git add basic-service/src/main/java/com/basic/sericve/sysPermission/impl/SysPermissionServiceImpl.java
git commit -m "feat(auth): add getUserRoutes method in service layer"
```

---

## Chunk 3: 添加 Controller 层接口

**Files:**
- Modify: `basic-api/src/main/java/com/basic/api/controller/AuthApi.java`
- Modify: `basic-web/src/main/java/com/basic/web/controller/AuthController.java`

- [ ] **Step 1: 在 AuthApi 添加接口声明**

在 `AuthApi.java` 文件中添加：

```java
/**
 * 获取当前用户路由权限
 *
 * @return 路由权限树形列表
 */
Result<List<PermissionTreeVO>> getUserRoutes();
```

添加 import：
```java
import com.basic.api.vo.sysPermission.PermissionTreeVO;
import java.util.List;
```

- [ ] **Step 2: 在 AuthController 实现接口**

在 `AuthController.java` 中添加：

```java
/**
 * 获取当前用户路由权限
 *
 * @return 路由权限树形列表
 */
@Override
@GetMapping("routes")
public Result<List<PermissionTreeVO>> getUserRoutes() {
    List<PermissionTreeVO> routes = sysUserService.getUserRoutes();
    return Result.success(routes);
}
```

添加 import：
```java
import com.basic.api.vo.sysPermission.PermissionTreeVO;
import java.util.List;
```

- [ ] **Step 3: Commit**

```bash
git add basic-api/src/main/java/com/basic/api/controller/AuthApi.java
git add basic-web/src/main/java/com/basic/web/controller/AuthController.java
git commit -m "feat(auth): add /auth/routes endpoint"
```

---

## Chunk 4: 测试验证

**Files:**
- Test: `basic-web/src/main/java/com/basic/web/controller/AuthController.java`

- [ ] **Step 1: 启动应用并测试**

启动应用：
```bash
mvn spring-boot:run -pl basic-web
```

- [ ] **Step 2: 验证接口**

1. 先调用登录接口获取 token：
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

2. 使用 token 调用路由接口：
```bash
curl -X GET http://localhost:8080/auth/routes \
  -H "Authorization: Bearer <token>"
```

预期返回：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "系统管理",
      "path": "/system",
      "type": "MENU",
      "children": [...]
    }
  ]
}
```

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "test(auth): verify /auth/routes endpoint"
```

---

## 实施完成

接口路径：`GET /auth/routes`

返回数据格式：树形结构，包含 MENU、BUTTON、API 三种类型的权限。

