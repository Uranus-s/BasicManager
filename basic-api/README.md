# Basic-API 模块说明

## 1. 模块定位

`basic-api` 是项目的 **接口定义模块**，定义 Controller 接口规范，实现 Controller 与 Service 的解耦。

主要职责：
- 定义 Controller 接口（仅接口定义，无实现）
- 定义 DTO/VO 数据传输对象
- 提供接口规范文档

> 📌 稳定层：api 模块接口应保持稳定，service/web 可重构

---

## 2. 模块结构

```text
basic-api
 └── com.basic.api
     └── controller/
         └── TestApi.java       # 测试接口定义
```

---

## 3. 接口分层原则

### 3.1 接口定义规范

**接口类命名**：`{业务}Api`，如 `UserApi`、`OrderApi`

**接口方法命名**：
- `get{Entity}` - 获取单个
- `list{Entity}` - 列表查询
- `save{Entity}` - 保存
- `update{Entity}` - 更新
- `delete{Entity}` - 删除

```java
@RequestMapping("/user")
public interface UserApi {
    @GetMapping("/{id}")
    UserVO getUser(@PathVariable Long id);

    @PostMapping
    Result<Void> saveUser(@RequestBody UserDTO user);

    @PutMapping
    Result<Void> updateUser(@RequestBody UserDTO user);

    @DeleteMapping("/{id}")
    Result<Void> deleteUser(@PathVariable Long id);
}
```

### 3.2 数据传输对象 DTO/VO

| 类型 | 用途 | 命名规范 |
|------|------|----------|
| DTO | 请求参数 | {业务}DTO |
| VO | 响应数据 | {业务}VO |
| Query | 查询参数 | {业务}Query |

---

## 4. Maven 依赖

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <scope>provided</scope>
    </dependency>

    <!-- 参数校验 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## 5. 模块依赖

### 依赖关系

本模块为接口定义层，**不依赖**项目内其他业务模块。

```
basic-api (无业务依赖)
```

### 被依赖

| 模块 | 说明 |
|------|------|
| basic-common-core | 无依赖 |
| basic-common-web | 无依赖 |
| basic-core | 无依赖 |
| basic-service | 依赖（实现接口） |
| basic-dao | 无依赖 |
| basic-web | 依赖（实现接口） |

---

## 6. 使用示例

### 接口定义（api 模块）

```java
public interface UserApi {
    @GetMapping("/list")
    Result<PageResult<UserVO>> listUser(UserQuery query);

    @PostMapping
    Result<Long> saveUser(@RequestBody @Valid UserDTO dto);
}
```

### 接口实现（web 模块）

```java
@RestController
public class UserController implements UserApi {
    @Autowired
    private IUserService userService;

    @Override
    public Result<PageResult<UserVO>> listUser(UserQuery query) {
        return Result.success(userService.listUser(query));
    }
}
```
