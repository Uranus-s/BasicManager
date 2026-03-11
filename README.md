# Basic Project（README）

## 一、项目简介

本项目是一个 **标准的 Spring Boot 多模块架构项目**，通过模块拆分实现 **职责清晰、解耦良好、便于扩展和维护**。项目同时包含 `api` 与 `web` 模块，适用于 **单体演进型项目或微服务架构**。

---

## 二、整体模块结构

```text
basic-parent (pom)
│
├─ basic-common-core            公共工具模块
├─ basic-common-web              Web层公共模块
├─ basic-core                    核心能力模块（MyBatis-Plus / Redis / Security）
├─ basic-api                     接口定义模块（Controller 接口）
├─ basic-service                 业务逻辑模块
├─ basic-dao                     数据访问模块（Entity / Mapper）
└─ basic-web                     Web入口模块（启动类）
```

**模块数量**：7 个（无 basic-job 模块）

---

## 三、模块职责说明（重点）

### 1. parent（父工程）

**模块类型：** `pom`

**职责：**

* 统一管理 Spring Boot / Spring Cloud 版本
* 统一第三方依赖版本
* Maven 插件统一配置

📌 **不包含任何业务代码**

---

### 2. basic-common-core（公共工具模块）

**职责：** 提供最底层的通用能力，无业务逻辑

**包含内容：**
```text
basic-common-core
└─ com.basic.common
   ├─ result/         统一返回封装（Result、ResultEnum）
   ├─ utils/          工具类（BeanConvertUtils）
   ├─ validate/       参数校验注解（@Mobile）
   └─ exception/      业务异常（BusinessException）
```

**依赖原则：**
* ❌ 不依赖任何业务模块
* ✅ 可被所有模块依赖

---

### 3. basic-common-web（Web层公共模块）

**职责：** 提供 Web 层通用能力

**包含内容：**
```text
basic-common-web
└─ com.basic.common.web
   ├─ advice/         全局响应封装、异常处理
   ├─ annotation/     忽略响应包装注解
   └─ exception/       Web 层异常
```

**依赖：** basic-common-core

---

### 4. basic-core（核心模块）

**职责：** 提供项目级通用基础能力

**包含内容：**

```text
basic-core
└─ mybatis/          MyBatis-Plus 配置（分页、乐观锁、逻辑删除、数据权限）
   ├─ base/          BaseEntity、BaseMapperPlus
   ├─ config/        MybatisPlusConfig
   ├─ handler/       自动填充处理器
   └─ interceptor/  数据权限拦截器
└─ redis/            Redis 缓存（配置、工具类、缓存注解）
   ├─ config/
   ├─ utils/
   └─ aspect/
└─ security/         Spring Security + JWT
   ├─ config/        SecurityConfig
   ├─ filter/        JwtAuthenticationFilter
   ├─ handler/       认证/授权异常处理
   ├─ model/         LoginUser
   ├─ service/       SecurityUserDetailsService
   ├─ spi/           SecurityUserQueryService（SPI 解耦）
   └─ util/          JwtUtil
```

📌 业务无关，但项目强相关

---

### 5. basic-api（接口定义模块）

**职责：** 定义系统对外提供的能力（接口契约），实现 Controller 与 Service 解耦

**包含内容：**
```text
basic-api
└─ com.basic.api
   └── controller/    （对外接口定义，仅接口，无实现）
```

**示例：**

```java
@RequestMapping("/user")
public interface UserApi {

    @GetMapping("/{id}")
    Result<UserVO> getUser(@PathVariable Long id);

    @PostMapping
    Result<Long> saveUser(@RequestBody @Valid UserDTO dto);
}
```

**特点：**

* ❌ 不包含 `@RestController`（仅接口定义）
* ❌ 不包含业务实现
* ❌ 不可独立启动

📌 **api 是接口说明书，不是执行者**

---

### 6. basic-service（业务逻辑模块）

**职责：** 核心业务逻辑实现

**包含内容：**
```text
basic-service
└─ com.basic.sericve (注意：包名 sericve)
    ├── sysUser/           用户服务
    ├── sysRole/           角色服务
    ├── sysPermission/     权限服务
    ├── sysDept/           部门服务
    ├── sysDict/           字典服务
    ├── sysDictItem/       字典项服务
    ├── sysConfig/         配置服务
    ├── sysFile/           文件服务
    ├── sysLoginLog/       登录日志服务
    ├── sysOperLog/        操作日志服务
    ├── sysUserRole/       用户角色关联
    ├── sysUserDept/       用户部门关联
    ├── sysRolePermission/ 角色权限关联
    └── security/          Security SPI 实现
```

* Service 接口与实现类（IService + ServiceImpl）
* 业务规则、校验逻辑
* 事务控制
* SPI 实现（如 SecurityUserQueryService）

**依赖关系：**

```text
basic-service → basic-api → basic-core → basic-dao
                    ↓
              basic-common-core
```

---

### 7. basic-dao（数据访问模块）

**职责：** 数据持久化

**包含内容：**

```text
basic-dao
└─ com.basic.dao
    ├── sysUser/           用户表
    │   ├── entity/        SysUser
    │   └── mapper/        SysUserMapper
    ├── sysRole/           角色表
    ├── sysPermission/     权限表
    ├── sysDept/           部门表
    ├── sysDict/           字典表
    ├── sysDictItem/       字典项表
    ├── sysConfig/         配置表
    ├── sysFile/           文件表
    ├── sysLoginLog/       登录日志表
    ├── sysOperLog/        操作日志表
    ├── sysUserRole/       用户角色关联表
    ├── sysUserDept/       用户部门关联表
    └── sysRolePermission/ 角色权限关联表
```

* Entity 实体类（继承 BaseEntity）
* Mapper 接口（继承 BaseMapperPlus）

**依赖关系：**

```text
basic-dao → basic-core
```

📌 只负责数据持久化，无业务逻辑

---

### 8. basic-web（Web入口模块）

**职责：** 对外 HTTP 接口 & 应用启动

**包含内容：**

* `@SpringBootApplication` 启动类
* `@RestController` Controller 实现
* Controller 接口实现（实现 api 模块）
* application.yml 配置

**示例：**

```java
@SpringBootApplication(scanBasePackages = "com.basic")
@MapperScan("com.basic.dao")
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
```

```java
@RestController
public class TestController implements TestApi {

    @Autowired
    private ISysUserService sysUserService;

    @Override
    public String getTest(Long id) {
        return sysUserService.getById(id).toString();
    }
}
```

📌 **Controller 只做参数接收与结果返回，不写业务**

---

### 9. 技术栈

| 技术 | 版本 |
|------|------|
| Java | 23 |
| Spring Boot | 4.0.0 |
| MyBatis-Plus | 3.5.x |
| MySQL | 8.0 |
| Redis | Cluster (Lettuce) |
| JWT | jjwt 0.12.5 |
| Spring Security | - |
| Spring AOP | - |

---

## 四、模块依赖关系（非常重要）

**依赖方向（单向）：**

```text
web → api → service → dao → core
                    ↓
              common-core
                   ↑
              common-web
```

📌 **禁止反向依赖**（如 common 依赖 service）

---

## 五、请求处理流程

```text
客户端请求
   ↓
web.Controller（实现 api 接口）
   ↓
service（业务处理）
   ↓
dao（数据访问）
```

`api` 在整个过程中仅作为 **接口契约存在**。

---

## 六、适用场景

### ✔ 适合

* 单体项目长期演进
* 微服务架构
* 多系统 / 多端调用
* 对外接口稳定性要求高

### ❌ 不强制

* 小型 Demo
* 临时项目

---

## 七、设计原则总结

* 高内聚、低耦合
* 接口定义与实现分离
* 上层依赖下层，禁止反向依赖
* Controller 不写业务
* api 稳定，service / web 可重构

推荐分层模型：
```text
api:
UserCreateDTO   （接口入参）
UserVO          （接口出参）

service:
UserBO          （业务对象，可选）

dao:
UserEntity / PO （数据库对象）
```
转换关系：
```text
DTO → BO → Entity
Entity → BO → VO
```

---

## 十、启动说明

### 启动模块

```text
basic-web
```

### 启动类

```java
@SpringBootApplication(scanBasePackages = "com.basic")
@MapperScan("com.basic.dao")
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
```

### Maven 命令

```bash
# 构建所有模块
mvn clean install -DskipTests

# 运行应用
mvn spring-boot:run -pl basic-web

# 运行单个测试
mvn test -Dtest=TestClassName
```

### 配置信息

| 配置项 | 值 |
|--------|-----|
| 服务端口 | 8080 |
| 数据库 | MySQL 192.168.0.110:3306 (basic_project) |
| Redis | Cluster 192.168.0.110:6379-6384 |

---

## 十一、各模块文档

详细说明请参考各模块目录下的 README.md：

| 模块 | 文档 |
|------|------|
| basic-common-core | [README.md](./basic-common-core/README.md) |
| basic-common-web | [README.md](./basic-common-web/README.md) |
| basic-core | [README.md](./basic-core/README.md) |
| basic-api | [README.md](./basic-api/README.md) |
| basic-service | [README.md](./basic-service/README.md) |
| basic-dao | [README.md](./basic-dao/README.md) |
| basic-web | [README.md](./basic-web/README.md) |

---

## 十二、备注

该结构可平滑演进为微服务架构，`api` 模块可直接作为 Feign Client 或 SDK 使用。

---
