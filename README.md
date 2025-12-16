# Basic Project（README）

## 一、项目简介

本项目是一个 **标准的 Spring Boot 多模块架构项目**，通过模块拆分实现 **职责清晰、解耦良好、便于扩展和维护**。项目同时包含 `api` 与 `web` 模块，适用于 **单体演进型项目或微服务架构**。

---

## 二、整体模块结构

```text
basic-parent
│
├─ basic-common      公共基础模块
├─ basic-core        核心能力模块（配置 / AOP / 安全等）
├─ basic-api         接口定义模块（接口契约）
├─ basic-service     业务逻辑模块
├─ basic-dao         数据访问模块（Mapper / Repository）
├─ basic-web         Web 接入层 & 启动模块
└─ basic-job         定时任务模块（未实现）
```

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

### 2. common（公共模块）

**职责：** 提供全项目通用能力

**包含内容：**
```text
common
└─ com.basic.common
   ├─ constant       常量
   ├─ enums          枚举
   ├─ exception      通用异常
   ├─ result         统一返回对象
   ├─ utils          工具类
   ├─ annotation     自定义注解
   ├─ config         通用配置
   ├─ model          通用模型 / DTO
   ├─ validate       校验相关
   ├─ aspect         通用切面（可选）
   └─ cache          通用缓存封装（可选）
```

**依赖原则：**

* ❌ 不依赖任何业务模块
* ✅ 可被所有模块依赖

---

### 3. core（核心模块）

**职责：** 提供项目级通用基础能力

**包含内容：**

* MyBatis / Redis / MQ 等通用配置
* 拦截器、过滤器
* AOP（日志、鉴权、限流）
* 安全相关组件

📌 业务无关，但项目强相关

---

### 4. api（接口定义模块）

**职责：** 定义系统对外提供的能力（接口契约）

**包含内容：**

* Controller 接口定义（仅接口，无实现）
* DTO / VO
* Feign 接口（如有微服务）

**示例：**

```java
@RequestMapping("/user")
public interface UserApi {

    @GetMapping("/{id}")
    UserDTO getById(@PathVariable Long id);
}
```

**特点：**

* ❌ 不包含 `@RestController`
* ❌ 不包含业务实现
* ❌ 不可独立启动

📌 **api 是接口说明书，不是执行者**

---

### 5. service（业务服务模块）

**职责：** 核心业务逻辑实现

**包含内容：**

* Service 接口与实现类
* 业务规则、校验逻辑
* 事务控制
* 领域模型

**依赖关系：**

```text
service → api → common
```

---

### 6. dao（数据访问模块）

**职责：** 数据持久化

**包含内容：**

* Mapper 接口
* MyBatis XML
* Repository

📌 小项目可合并至 `service` 模块

---

### 7. web（Web 接入层 & 启动模块）

**职责：** 对外 HTTP 接口 & 应用启动

**包含内容：**

* `@SpringBootApplication`
* `@RestController`
* Controller 接口实现（实现 api 模块）
* Web 配置（MVC、CORS、异常处理）

**示例：**

```java
@RestController
public class UserController implements UserApi {

    @Autowired
    private UserService userService;

    @Override
    public UserDTO getById(Long id) {
        return userService.getById(id);
    }
}
```

📌 **Controller 只做参数接收与结果返回，不写业务**

---

### 8. job（定时任务模块，未实现）

**职责：** 定时任务调度

**包含内容：**

* Spring `@Scheduled`
* Quartz / XXL-Job 任务

---

## 四、模块依赖关系（非常重要）

```text
web
 ↓
service
 ↓
dao
 ↓
common
```

或（推荐模式）：

```text
web → api → service → dao → common
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

---

## 八、启动说明

启动模块：

```text
basic-web
```

启动类：

```java
@SpringBootApplication
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
```

---

## 九、备注

该结构可平滑演进为微服务架构，`api` 模块可直接作为 Feign Client 或 SDK 使用。

---
