# Basic-Common-Core 模块说明

## 1. 模块定位

`basic-common-core` 是项目的 **公共工具模块**，提供最底层的通用能力，不包含任何业务逻辑。

主要职责：
- 统一结果返回封装（Result）
- 工具类（BeanConvertUtils）
- 参数校验注解（@Mobile）
- 业务异常定义（BusinessException）
- 结果枚举定义（ResultEnum）

> 📌 项目最底层的公共模块，无其他项目依赖

---

## 2. 模块结构

```text
basic-common-core
 └── com.basic.common
     ├── result/
     │   ├── Result.java           # 统一响应封装类
     │   ├── IResult.java          # 结果接口
     │   └── ResultEnum.java       # 结果枚举（状态码+消息）
     ├── utils/
     │   └── BeanConvertUtils.java # Bean 对象转换工具
     ├── validate/
     │   └── annotation/
     │       └── Mobile.java       # 手机号校验注解
     └── exception/
         └── BusinessException.java # 业务异常类
```

---

## 3. 核心功能说明

### 3.1 统一响应封装 Result

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private boolean success;    // 请求是否成功
    private Integer code;       // 状态码
    private String message;     // 返回消息
    private T data;            // 返回数据
}
```

**使用示例：**
```java
// 成功响应
return Result.success(data);
return Result.success("操作成功");

// 失败响应
return Result.fail(ResultEnum.PARAM_INVALID);
return Result.fail(10001, "自定义错误消息");
```

### 3.2 结果枚举 ResultEnum

| 分类 | 状态码范围 | 说明 |
|------|-----------|------|
| 成功 | 0 | 成功 |
| 参数校验 | 1xxxx | 参数缺失、格式错误、校验失败等 |
| 认证权限 | 2xxxx | 未登录、Token无效、权限不足等 |
| 用户状态 | 3xxxx | 用户不存在、密码错误等 |
| 业务冲突 | 4xxxx | 状态不允许、重复提交等 |
| 数据资源 | 5xxxx | 数据不存在、关联数据异常等 |
| 第三方 | 6xxxx | 远程服务异常、超时等 |
| 系统异常 | 9xxxx | 系统错误、数据库异常等 |

### 3.3 Bean 转换工具 BeanConvertUtils

```java
// 对象转换
UserVO vo = BeanConvertUtils.convert(source, UserVO.class);

// 列表转换
List<UserVO> voList = BeanConvertUtils.convertToList(sourceList, UserVO.class);
```

### 3.4 业务异常 BusinessException

```java
// 抛出业务异常
throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
throw new BusinessException(30001, "用户不存在");
```

### 3.5 参数校验注解 @Mobile

```java
@Mobile
private String phone;  // 中国大陆手机号校验
```

---

## 4. Maven 依赖

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- 参数校验 -->
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## 5. 模块依赖

### 依赖关系

本模块为最底层模块，**不依赖**项目内其他模块。

### 被依赖

| 模块 | 说明 |
|------|------|
| basic-common-web | 依赖 |
| basic-core | 无依赖 |
| basic-api | 无依赖 |
| basic-service | 依赖 |
| basic-dao | 无依赖 |
| basic-web | 依赖 |
