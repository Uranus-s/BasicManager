# Admin 用户初始化接口实施计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 Admin 用户初始化接口，支持创建管理员用户、角色和完整权限菜单

**Architecture:** 在 AuthController 添加初始化接口，ISysUserService 添加初始化方法，依次创建用户、角色、权限及其关联关系

**Tech Stack:** Spring Boot, MyBatis-Plus, BCrypt 密码加密

---

## 文件结构

| 操作 | 文件路径 |
|------|----------|
| 创建 | `basic-api/src/main/java/com/basic/api/dto/auth/InitAdminDTO.java` |
| 创建 | `basic-api/src/main/java/com/basic/api/vo/auth/InitResultVO.java` |
| 修改 | `basic-api/src/main/java/com/basic/api/controller/AuthApi.java` |
| 修改 | `basic-web/src/main/java/com/basic/web/controller/AuthController.java` |
| 修改 | `basic-service/src/main/java/com/basic/sericve/sysUser/service/ISysUserService.java` |
| 修改 | `basic-service/src/main/java/com/basic/sericve/sysUser/impl/SysUserServiceImpl.java` |
| 修改 | `basic-web/src/main/resources/application.yml` |

---

## Chunk 1: DTO/VO 定义

### Task 1: 创建 InitAdminDTO

**Files:**
- 创建: `basic-api/src/main/java/com/basic/api/dto/auth/InitAdminDTO.java`

- [ ] **Step 1: 创建 InitAdminDTO**

```java
package com.basic.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 初始化管理员请求DTO
 *
 * @author Gas
 */
@Data
public class InitAdminDTO {

    /**
     * 初始化密钥
     */
    @NotBlank(message = "初始化密钥不能为空")
    private String initKey;

    /**
     * 管理员密码
     */
    @NotBlank(message = "管理员密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String adminPassword;
}
```

- [ ] **Step 2: 提交**

```bash
git add basic-api/src/main/java/com/basic/api/dto/auth/InitAdminDTO.java
git commit -m "feat(auth): 添加初始化管理员请求DTO"
```

### Task 2: 创建 InitResultVO

**Files:**
- 创建: `basic-api/src/main/java/com/basic/api/vo/auth/InitResultVO.java`

- [ ] **Step 1: 创建 InitResultVO**

```java
package com.basic.api.vo.auth;

import lombok.Data;

/**
 * 初始化结果VO
 *
 * @author Gas
 */
@Data
public class InitResultVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色编码
     */
    private String roleCode;
}
```

- [ ] **Step 2: 提交**

```bash
git add basic-api/src/main/java/com/basic/api/vo/auth/InitResultVO.java
git commit -m "feat(auth): 添加初始化结果VO"
```

---

## Chunk 2: API 接口定义

### Task 3: 添加 AuthApi 接口方法

**Files:**
- 修改: `basic-api/src/main/java/com/basic/api/controller/AuthApi.java:34`

- [ ] **Step 1: 添加 initAdmin 方法声明**

在 AuthApi 接口末尾添加:

```java
/**
 * 初始化管理员
 *
 * @param initAdminDTO 初始化请求
 * @return 初始化结果
 */
Result<InitResultVO> initAdmin(InitAdminDTO initAdminDTO);
```

- [ ] **Step 2: 提交**

```bash
git add basic-api/src/main/java/com/basic/api/controller/AuthApi.java
git commit -m "feat(auth): 添加初始化管理员接口声明"
```

---

## Chunk 3: Service 实现

### Task 4: 添加 ISysUserService 方法声明

**Files:**
- 修改: `basic-service/src/main/java/com/basic/sericve/sysUser/service/ISysUserService.java:116`

- [ ] **Step 1: 添加 initAdmin 方法声明**

在 ISysUserService 接口末尾添加:

```java
/**
 * 初始化管理员（用户、角色、权限）
 *
 * @param adminPassword 管理员密码
 * @return 初始化结果
 */
InitResultVO initAdmin(String adminPassword);
```

- [ ] **Step 2: 提交**

```bash
git add basic-service/src/main/java/com/basic/sericve/sysUser/service/ISysUserService.java
git commit -m "feat(user): 添加初始化管理员方法声明"
```

### Task 5: 实现 SysUserServiceImpl.initAdmin

**Files:**
- 修改: `basic-service/src/main/java/com/basic/sericve/sysUser/impl/SysUserServiceImpl.java`

- [ ] **Step 1: 添加依赖注入**

在 SysUserServiceImpl 类中添加:

```java
private final ISysRoleService sysRoleService;
private final ISysPermissionService sysPermissionService;
private final SysUserRoleMapper sysUserRoleMapper;
private final SysRolePermissionMapper sysRolePermissionMapper;
```

- [ ] **Step 2: 添加 initAdmin 方法实现**

在 SysUserServiceImpl 类中添加:

```java
@Override
public InitResultVO initAdmin(String adminPassword) {
    // 1. 检查是否已存在用户
    if (count() > 0) {
        throw new BusinessException("系统已存在用户，无需初始化");
    }

    // 2. 创建管理员角色
    SysRole adminRole = new SysRole();
    adminRole.setRoleCode("admin");
    adminRole.setRoleName("管理员");
    adminRole.setStatus((byte) 1);
    adminRole.setRemark("系统超级管理员");
    sysRoleService.save(adminRole);

    // 3. 创建管理员用户
    SysUser adminUser = new SysUser();
    adminUser.setUsername("admin");
    adminUser.setPassword(passwordEncoder.encode(adminPassword));
    adminUser.setNickname("管理员");
    adminUser.setStatus((byte) 1);
    save(adminUser);

    // 4. 关联用户角色
    SysUserRole userRole = new SysUserRole();
    userRole.setUserId(adminUser.getId());
    userRole.setRoleId(adminRole.getId());
    sysUserRoleMapper.insert(userRole);

    // 5. 创建权限菜单并关联角色
    List<Long> permissionIds = createInitPermissions();
    if (!permissionIds.isEmpty()) {
        for (Long permId : permissionIds) {
            SysRolePermission rolePerm = new SysRolePermission();
            rolePerm.setRoleId(adminRole.getId());
            rolePerm.setPermissionId(permId);
            sysRolePermissionMapper.insert(rolePerm);
        }
    }

    // 6. 返回结果
    InitResultVO result = new InitResultVO();
    result.setUserId(adminUser.getId());
    result.setUsername(adminUser.getUsername());
    result.setRoleId(adminRole.getId());
    result.setRoleCode(adminRole.getRoleCode());
    return result;
}

/**
 * 创建初始化权限菜单
 *
 * @return 权限ID列表
 */
private List<Long> createInitPermissions() {
    List<Long> permissionIds = new ArrayList<>();

    // 系统管理菜单
    SysPermission systemMenu = new SysPermission();
    systemMenu.setParentId(0L);
    systemMenu.setName("系统管理");
    systemMenu.setType("MENU");
    systemMenu.setPath("/system");
    systemMenu.setIcon("Setting");
    systemMenu.setSort(1);
    systemMenu.setVisible((byte) 1);
    systemMenu.setStatus((byte) 1);
    sysPermissionService.save(systemMenu);
    permissionIds.add(systemMenu.getId());

    // 子菜单定义: 名称, 路径, 权限标识, 图标, 排序
    String[][] childMenus = {
        {"用户管理", "/system/user", "system:user:list", "User", "1"},
        {"角色管理", "/system/role", "system:role:list", "peoples", "2"},
        {"菜单管理", "/system/permission", "system:permission:list", "menu", "3"},
        {"部门管理", "/system/dept", "system:dept:list", "office", "4"},
        {"字典管理", "/system/dict", "system:dict:list", "dict", "5"},
        {"参数配置", "/system/config", "system:config:list", "config", "6"},
    };

    // 日志管理菜单
    SysPermission logMenu = new SysPermission();
    logMenu.setParentId(systemMenu.getId());
    logMenu.setName("日志管理");
    logMenu.setType("MENU");
    logMenu.setPath("/system/log");
    logMenu.setIcon("Log");
    logMenu.setSort(7);
    logMenu.setVisible((byte) 1);
    logMenu.setStatus((byte) 1);
    sysPermissionService.save(logMenu);
    permissionIds.add(logMenu.getId());

    // 日志子菜单
    String[][] logChildMenus = {
        {"登录日志", "/system/loginLog", "system:loginLog:list", "login", "1"},
        {"操作日志", "/system/operLog", "system:operLog:list", "operation", "2"},
    };

    // 保存子菜单
    int sort = 1;
    for (String[] menu : childMenus) {
        SysPermission perm = new SysPermission();
        perm.setParentId(systemMenu.getId());
        perm.setName(menu[0]);
        perm.setType("MENU");
        perm.setPath(menu[1]);
        perm.setPermission(menu[2]);
        perm.setIcon(menu[3]);
        perm.setSort(sort++);
        perm.setVisible((byte) 1);
        perm.setStatus((byte) 1);
        sysPermissionService.save(perm);
        permissionIds.add(perm.getId());
    }

    // 保存日志子菜单
    sort = 1;
    for (String[] menu : logChildMenus) {
        SysPermission perm = new SysPermission();
        perm.setParentId(logMenu.getId());
        perm.setName(menu[0]);
        perm.setType("MENU");
        perm.setPath(menu[1]);
        perm.setPermission(menu[2]);
        perm.setIcon(menu[3]);
        perm.setSort(sort++);
        perm.setVisible((byte) 1);
        perm.setStatus((byte) 1);
        sysPermissionService.save(perm);
        permissionIds.add(perm.getId());
    }

    return permissionIds;
}
```

- [ ] **Step 3: 添加必要的 import**

```java
import com.basic.api.vo.auth.InitResultVO;
import com.basic.common.exception.BusinessException;
import com.basic.dao.sysRolePermission.entity.SysRolePermission;
import com.basic.dao.sysRolePermission.mapper.SysRolePermissionMapper;
import com.basic.dao.sysUserRole.entity.SysUserRole;
import com.basic.dao.sysUserRole.mapper.SysUserRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
```

- [ ] **Step 4: 注入 PasswordEncoder**

确保构造函数注入了 PasswordEncoder:

```java
private final PasswordEncoder passwordEncoder;
```

- [ ] **Step 5: 提交**

```bash
git add basic-service/src/main/java/com/basic/sericve/sysUser/impl/SysUserServiceImpl.java
git commit -m "feat(user): 实现初始化管理员方法"
```

---

## Chunk 4: Controller 实现

### Task 6: 实现 AuthController.initAdmin

**Files:**
- 修改: `basic-web/src/main/java/com/basic/web/controller/AuthController.java`

- [ ] **Step 1: 添加配置属性类**

在 AuthController 类中添加:

```java
@Value("${system.init-key:admin-init-key}")
private String initKey;
```

- [ ] **Step 2: 实现 initAdmin 方法**

在 AuthController 类中添加:

```java
/**
 * 初始化管理员
 *
 * @param initAdminDTO 初始化请求
 * @return 初始化结果
 */
@Override
public Result<InitResultVO> initAdmin(@Valid @RequestBody InitAdminDTO initAdminDTO) {
    // 验证密钥
    if (!initKey.equals(initAdminDTO.getInitKey())) {
        return Result.build(false, 401, "初始化密钥错误", null);
    }

    // 执行初始化
    InitResultVO result = sysUserService.initAdmin(initAdminDTO.getAdminPassword());
    return Result.success(result);
}
```

- [ ] **Step 3: 添加 import**

```java
import com.basic.api.dto.auth.InitAdminDTO;
import com.basic.api.vo.auth.InitResultVO;
import org.springframework.beans.factory.annotation.Value;
```

- [ ] **Step 4: 提交**

```bash
git add basic-web/src/main/java/com/basic/web/controller/AuthController.java
git commit -m "feat(auth): 实现初始化管理员接口"
```

---

## Chunk 5: 配置

### Task 7: 添加系统配置项

**Files:**
- 修改: `basic-web/src/main/resources/application.yml`

- [ ] **Step 1: 添加配置项**

在 application.yml 末尾添加:

```yaml
# 系统配置
system:
  init-key: admin-init-key
```

- [ ] **Step 2: 提交**

```bash
git add basic-web/src/main/resources/application.yml
git commit -m "chore(config): 添加系统初始化密钥配置"
```

---

## Chunk 6: 验证

### Task 8: 编译验证

- [ ] **Step 1: 执行编译**

```bash
mvn clean compile -DskipTests
```

预期: BUILD SUCCESS

- [ ] **Step 2: 最终提交**

```bash
git add .
git commit -m "feat(auth): 实现Admin用户初始化接口

- 添加初始化管理员请求DTO和响应VO
- 在AuthApi添加初始化接口声明
- 在ISysUserService添加初始化方法
- 实现完整的初始化逻辑（用户、角色、权限菜单）
- 添加系统配置项"
```

---

## 测试验证

启动服务后，使用以下命令测试:

```bash
# 成功场景
curl -X POST http://localhost:8080/auth/init \
  -H "Content-Type: application/json" \
  -d '{"initKey":"admin-init-key","adminPassword":"123456"}'

# 密钥错误场景
curl -X POST http://localhost:8080/auth/init \
  -H "Content-Type: application/json" \
  -d '{"initKey":"wrong-key","adminPassword":"123456"}'

# 重复初始化场景（第二次调用）
curl -X POST http://localhost:8080/auth/init \
  -H "Content-Type: application/json" \
  -d '{"initKey":"admin-init-key","adminPassword":"123456"}'
```
