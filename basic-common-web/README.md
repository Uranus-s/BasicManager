# Basic-Common-Web 模块说明

## 1. 模块定位

`basic-common-web` 是项目的 **Web 层公共模块**，提供控制器相关的通用能力。

主要职责：
- 全局响应体封装（GlobalResponseBodyAdvice）
- 全局异常处理（GlobalExceptionAdvice）
- 忽略响应包装注解（@IgnoreResponseAdvice）
- Web 层异常定义（ForbiddenException）

> 📌 依赖 basic-common-core，提供 Web 层增强

---

## 2. 模块结构

```text
basic-common-web
 └── com.basic.common.web
     ├── advice/
     │   ├── GlobalResponseBodyAdvice.java  # 全局响应体处理切面
     │   └── GlobalExceptionAdvice.java    # 全局异常处理
     ├── annotation/
     │   └── IgnoreResponseAdvice.java     # 忽略响应包装注解
     └── exception/
         └── ForbiddenException.java      # 禁止访问异常
```

---

## 3. 核心功能说明

### 3.1 全局响应体处理 GlobalResponseBodyAdvice

**功能**：自动将 Controller 返回的对象封装为 `Result<T>` 格式

**实现原理**：
```java
@RestControllerAdvice(basePackages = "com.basic")
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    // 判断是否需要处理（排除特定类型）
    @Override
    public boolean supports(MethodParameter returnType, ...) {
        // 排除 Result、ResponseEntity、Resource 等类型
    }

    // 封装响应体
    @Override
    public Object beforeBodyWrite(...) {
        // 将返回对象封装为 Result
    }
}
```

**效果**：
```java
// Controller 直接返回数据，自动封装
@GetMapping("/user")
public User getUser() {
    return user;  // 自动封装为 Result.success(user)
}
```

### 3.2 忽略响应包装 @IgnoreResponseAdvice

**使用场景**：文件下载、第三方回调等不需要统一封装的情况

```java
@IgnoreResponseAdvice
@GetMapping("/download")
public File download() {
    return file;
}
```

### 3.3 全局异常处理 GlobalExceptionAdvice

**统一处理的异常类型**：

| 异常类型 | 处理方式 |
|----------|----------|
| BusinessException | 返回业务错误码和消息 |
| ForbiddenException | 返回 20002 无权限 |
| MethodArgumentNotValidException | 返回参数校验错误 |
| ConstraintViolationException | 返回参数校验错误 |
| BindException | 返回绑定错误 |
| Exception | 返回系统错误（兜底） |

### 3.4 禁止访问异常 ForbiddenException

```java
throw new ForbiddenException();
```

---

## 4. Maven 依赖

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- 项目依赖 -->
    <dependency>
        <groupId>com.basic</groupId>
        <artifactId>basic-common-core</artifactId>
    </dependency>
</dependencies>
```

---

## 5. 模块依赖

### 依赖关系

```
basic-common-web
    └── basic-common-core
```

### 被依赖

| 模块 | 说明 |
|------|------|
| basic-common-core | 无依赖 |
| basic-core | 无依赖 |
| basic-api | 无依赖 |
| basic-service | 无依赖 |
| basic-dao | 无依赖 |
| basic-web | 依赖 |
