# Basic-Service 模块说明

## 1. 模块定位

`basic-service` 是项目的 **业务逻辑模块**，实现业务逻辑处理，是系统的核心业务层。

主要职责：
- 业务逻辑实现（Service 接口及实现）
- 事务管理
- 业务规则处理
- SPI 实现（如 SecurityUserQueryService）

> 📌 核心业务层：所有业务逻辑都应在此模块实现

---

## 2. 模块结构

```text
basic-service
 └── com.basic.sericve (注意：包名 sericve 而非 service)
     ├── sysUser/           # 用户服务
     │   ├── service/
     │   │   └── ISysUserService.java
     │   └── impl/
     │       └── SysUserServiceImpl.java
     ├── sysRole/           # 角色服务
     ├── sysPermission/     # 权限服务
     ├── sysDept/           # 部门服务
     ├── sysDict/           # 字典服务
     ├── sysDictItem/      # 字典项服务
     ├── sysConfig/        # 配置服务
     ├── sysFile/          # 文件服务
     ├── sysLoginLog/      # 登录日志服务
     ├── sysOperLog/       # 操作日志服务
     ├── sysUserRole/      # 用户角色关联服务
     ├── sysUserDept/      # 用户部门关联服务
     ├── sysRolePermission/ # 角色权限关联服务
     └── security/         # 安全相关服务
         └── SecurityUserQueryServiceImpl.java
```

---

## 3. Service 层规范

### 3.1 接口定义规范

```java
public interface I{Entity}Service extends IService<Entity> {
    // 业务方法定义
}
```

### 3.2 实现类规范

```java
@Service
public class {Entity}ServiceImpl implements I{Entity}Service {
    @Autowired
    private {Entity}Mapper {entity}Mapper;

    // 业务方法实现
}
```

### 3.3 Service 层职责

| 职责 | 说明 |
|------|------|
| 参数校验 | 业务规则校验（非空、唯一性等） |
| 事务管理 | @Transactional 事务控制 |
| 业务处理 | 核心业务逻辑实现 |
| 持久化 | 调用 dao 层完成数据持久化 |
| 异常抛出 | 业务异常 BusinessException |

### 3.4 Controller 与 Service 区别

| Controller | Service |
|------------|---------|
| 参数接收和校验 | 业务逻辑处理 |
| 响应封装 | 事务管理 |
| 异常映射 | 数据持久化 |
| 无业务逻辑 | 纯业务实现 |

---

## 4. Maven 依赖

```xml
<dependencies>
    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot4-starter</artifactId>
    </dependency>

    <!-- 项目模块 -->
    <dependency>
        <groupId>com.basic</groupId>
        <artifactId>basic-api</artifactId>
    </dependency>
    <dependency>
        <groupId>com.basic</groupId>
        <artifactId>basic-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.basic</groupId>
        <artifactId>basic-common-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.basic</groupId>
        <artifactId>basic-dao</artifactId>
    </dependency>
</dependencies>
```

---

## 5. 模块依赖

### 依赖关系

```
basic-service
    ├── basic-api          # 接口定义
    ├── basic-core        # 核心能力（Security SPI 实现）
    ├── basic-common-core # 公共工具
    └── basic-dao         # 数据访问
```

### 被依赖

| 模块 | 说明 |
|------|------|
| basic-common-core | 无依赖 |
| basic-common-web | 无依赖 |
| basic-core | 无依赖 |
| basic-api | 无依赖 |
| basic-dao | 无依赖 |
| basic-web | 依赖 |

---

## 6. SPI 机制

### SecurityUserQueryService

在 basic-core 中定义 SPI 接口，basic-service 提供实现：

```java
// basic-core 定义接口
public interface SecurityUserQueryService {
    LoginUser getUserDetails(String username);
}

// basic-service 提供实现
@Service
public class SecurityUserQueryServiceImpl implements SecurityUserQueryService {
    @Override
    public LoginUser getUserDetails(String username) {
        // 查询用户信息
    }
}
```
