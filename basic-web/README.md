# Basic-Web 模块说明

## 1. 模块定位

`basic-web` 是项目的 **Web 入口模块**，是整个应用的启动模块，包含 Spring Boot 启动类和 Controller 实现。

主要职责：
- Spring Boot 启动入口
- Controller 实现（实现 api 模块定义的接口）
- 请求参数接收和响应返回
- 路由配置

> 📌 聚合模块：依赖所有其他模块，是应用启动入口

---

## 2. 模块结构

```text
basic-web
 └── com.basic.web
     ├── WebApplication.java      # Spring Boot 启动类
     └── controller/
         └── TestController.java  # 测试控制器
```

---

## 3. 核心类说明

### 3.1 启动类 WebApplication

```java
@SpringBootApplication(scanBasePackages = "com.basic")
@MapperScan("com.basic.dao")
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
```

**关键配置**：
| 注解 | 说明 |
|------|------|
| @SpringBootApplication | Spring Boot 启动注解 |
| scanBasePackages = "com.basic" | 组件扫描包路径 |
| @MapperScan("com.basic.dao") | MyBatis Mapper 扫描 |

### 3.2 Controller 实现

```java
@RestController
@RequestMapping("/test")
@Validated
public class TestController implements TestApi {

    @Autowired
    private ISysUserService sysUserService;

    @GetMapping("/hello/{num}")
    public String hello(@PathVariable("num") @Min(1) @Max(10) Integer num) {
        sysUserService.getById(1);
        return "hello world " + num;
    }
}
```

**Controller 职责**：
| 职责 | 说明 |
|------|------|
| 参数接收 | @RequestParam、@PathVariable、@RequestBody |
| 参数校验 | @Validated + 校验注解 |
| 参数绑定 | 调用 Service 层方法 |
| 响应返回 | 自动封装为 Result（由 basic-common-web 处理） |

---

## 4. 配置文件

### 4.1 application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: basic-web
  datasource:
    url: jdbc:mysql://192.168.0.110:3306/basic_project
    username: root
    password: root
  data:
    redis:
      cluster:
        nodes: 192.168.0.110:6379,192.168.0.110:6380,192.168.0.110:6381,192.168.0.110:6382,192.168.0.110:6383,192.168.0.110:6384
```

---

## 5. Maven 依赖

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- 数据库 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>

    <!-- 项目模块 -->
    <dependency>
        <groupId>com.basic</groupId>
        <artifactId>basic-api</artifactId>
    </dependency>
    <dependency>
        <groupId>com.basic</groupId>
        <artifactId>basic-service</artifactId>
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
        <artifactId>basic-common-web</artifactId>
    </dependency>
</dependencies>
```

---

## 6. 模块依赖

### 依赖关系

```
basic-web (入口模块)
    ├── basic-api          # 接口定义
    ├── basic-service      # 业务实现
    ├── basic-core         # 核心能力
    ├── basic-common-core  # 公共工具
    └── basic-common-web   # Web 公共
```

### 被依赖

本模块为聚合入口模块，**不被**其他项目模块依赖。

---

## 7. 启动方式

### 7.1 Maven 命令

```bash
# 启动应用
mvn spring-boot:run -pl basic-web

# 打包后启动
mvn clean install -DskipTests
java -jar basic-web/target/basic-web.jar
```

### 7.2 IDE 启动

直接运行 `WebApplication.main()` 方法

---

## 8. 请求流程

```
HTTP 请求
    ↓
WebApplication (Tomcat)
    ↓
Filter (Security)
    ↓
Controller (参数接收)
    ↓
Service (业务处理)
    ↓
Mapper (数据持久化)
    ↓
Database
```

---

## 9. 设计原则

> **Controller 只做三件事**：
> 1. 接收参数
> 2. 调用 Service
> 3. 返回结果
>
> **禁止在 Controller 中写业务逻辑**
