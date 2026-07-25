# Basic-Core 模块说明

## 1. 模块定位

`core` 模块是整个项目的 **基础设施模块**，提供项目级通用能力，不依赖具体业务。  
主要职责：

- 集成 MyBatis-Plus，提供自动填充、乐观锁、分页、逻辑删除等基础能力
- 提供通用 Mapper 基类和实体基类
- 配置 MyBatis-Plus 插件链（分页、乐观锁、防全表操作、数据权限）
- 可扩展数据权限、Redis、MQ 等基础设施（后续可增加）

> 📌 业务无关，但项目强相关

---

## 2. Maven 依赖

核心依赖如下：

```xml
<dependencies>
    <!-- Spring JDBC -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>

    <!-- MyBatis 官方 Starter（Boot 4 兼容） -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>4.0.0</version>
    </dependency>

    <!-- MyBatis-Plus 核心 -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-core</artifactId>
        <version>3.5.15</version>
    </dependency>

    <!-- MyBatis-Plus 扩展插件 -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-extension</artifactId>
        <version>3.5.15</version>
    </dependency>

    <!-- SQL 解析器（分页、防全表依赖） -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-jsqlparser</artifactId>
        <version>3.5.15</version>
    </dependency>
</dependencies>
```

---

## 3. 模块结构

```text
basic-core
 └── mybatis
     ├── base
     │   └── BaseEntity              # 所有表的基础字段、乐观锁、逻辑删除
     │   └── BaseMapperPlus<T>       # 通用 Mapper 基类
     ├── config
     │   ├── MybatisConfig           # Mapper 扫描配置
     │   └── MybatisPlusConfig       # 插件链 + 自动填充注册
     ├── handler
     │   └── CommonMetaObjectHandler # 自动填充实现
     └── interceptor
         └── DataScopeInterceptor    # 数据权限拦截器
 └── redis
     ├── config
     │   └── RedisConfig                 # RedisTemplate + 序列化配置
     ├── utils
     │   └── RedisUtils                  # Redis 常用方法封装
     ├── annotation
     │   └── RedisCache.java             # 可选：自动缓存注解
     └── aspect
         └── RedisCacheAspect.java       # 可选：注解切面实现
 └── security
     ├── config
     │   └── SecurityConfig.java                    # Security 配置
     ├── filter
     │   └── JwtAuthenticationFilter.java           # JWT 认证过滤器
     ├── handler
     │   ├── AuthenticationEntryPointImpl.java      # 认证失败处理
     │   └── AccessDeniedHandlerImpl.java           # 权限不足处理
     ├── model
     │   └── LoginUser.java                         # 用户信息
     ├── service
     │   └── SecurityUserDetailsService.java        # 用户信息查询服务
     ├── spi
     │   └── SecurityUserQueryService.java          # 用户信息查询 SPI （解耦 core 与业务模块）
     └── util
         └── JwtUtil.java                           # JWT 工具类
```

---

## 4. 核心功能说明

### 4.1 MyBatis-Plus 集成

#### 基础实体 BaseEntity
```java
public class BaseEntity {
    private Long id;              // 主键
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间
    private String createBy;      // 创建人
    private String updateBy;      // 更新人
    private Integer version;      // 乐观锁版本号
    private Integer deleted;      // 逻辑删除标志
}
```

#### 通用 Mapper
```java
public interface BaseMapperPlus<T> extends BaseMapper<T> {
    // 逻辑删除查询
    default List<T> selectByIds(List<Long> ids) {
        return selectBatchIds(ids.stream()
            .filter(id -> selectById(id) != null)
            .collect(Collectors.toList()));
    }
}
```

#### 配置类 MybatisPlusConfig
- 分页插件：`PaginationInnerInterceptor`
- 乐观锁插件：`OptimisticLockerInnerInterceptor`
- 防全表删除/更新：`BlockAttackInnerInterceptor`
- 数据权限拦截器：`DataScopeInterceptor`

#### 自动填充
`CommonMetaObjectHandler` 实现元数据自动填充：
- 创建时间、更新时间：自动写入
- 创建人、更新人：从 SecurityContext 获取

---

### 4.2 Redis 缓存

#### RedisConfig
- 支持单机和集群模式
- Key 序列化：StringRedisSerializer
- Value 序列化：Jackson2JsonRedisSerializer

#### RedisUtils 工具类
```java
// 常见操作
redisUtils.set(key, value, expireTime, TimeUnit);
redisUtils.get(key);
redisUtils.delete(key);
redisUtils.hasKey(key);
redisUtils.expire(key, timeout, unit);
```

#### 缓存注解（可选）
- `@RedisCache` - 方法级缓存注解
- `@RedisCacheAspect` - 切面实现

---

### 4.3 Spring Security 安全

#### SecurityConfig
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // 无状态 Session 管理
    // JWT 认证过滤器链
    // 公开接口白名单配置
}
```

#### 核心组件

| 组件 | 职责 |
|------|------|
| JwtAuthenticationFilter | 拦截请求，解析 JWT Token |
| JwtUtil | 生成/解析 JWT Token |
| LoginUser | 当前登录用户信息 |
| SecurityUserDetailsService | 加载用户详情（SPI 机制） |
| AuthenticationEntryPointImpl | 认证失败处理 |
| AccessDeniedHandlerImpl | 权限不足处理 |

#### SPI 机制
`SecurityUserQueryService` 解耦 core 与业务：
- 接口定义在 core 模块
- 实现放在 service 模块
- 通过 SPI 自动加载

---

### 4.4 统一线程池

项目统一管理异步任务、阻塞 I/O 任务和定时任务，业务模块应按任务特性选择执行器，避免自行创建无监控、不可统一关闭的线程池。

#### 执行器选择

| 固定 Bean 名称 | 适用场景 | 说明 |
|---|---|---|
| `cpuTaskExecutor` | CPU 密集任务、需要有界队列削峰的任务 | 无名称 `@Async` 默认使用此执行器。 |
| `virtualTaskExecutor` | 数据库、Redis、HTTP、文件等阻塞 I/O 任务 | 使用受并发上限约束的虚拟线程执行器。 |
| `scheduledTaskScheduler` | `@Scheduled` 的触发和调度 | 仅负责调度，不应用于执行耗时业务逻辑。 |

使用命名执行器时，通过 `ThreadPoolNames` 常量指定名称：

```java
@Async(ThreadPoolNames.CPU)
public void calculateReport() {
    // CPU 密集任务或需要有界队列削峰的任务
}

@Async(ThreadPoolNames.VIRTUAL)
public void loadRemoteData() {
    // 数据库、Redis、HTTP、文件等阻塞 I/O 任务
}
```

#### 定时任务约束

耗时 `@Scheduled` 方法只负责触发，实际工作必须转交给业务执行器（`cpuTaskExecutor` 或 `virtualTaskExecutor`）。这样可以避免调度线程被长任务占用，影响后续触发。

CPU 和虚拟线程执行器会传播任务提交方的 `SecurityContext` 与 MDC；`scheduledTaskScheduler` 为保留调度任务的取消和移除策略，不传播这两类上下文。因此，定时任务不应依赖提交线程的登录态或 MDC；如需身份、租户或链路信息，应在任务业务参数或任务内部显式建立。

禁止在业务模块自行调用 `Executors.newFixedThreadPool`、`Executors.newCachedThreadPool` 等方式创建线程池，统一使用上述受监控执行器。

#### 实时监控

通过 `GET /system/monitor/thread-pools` 查询平台线程池、虚拟线程执行器和调度器的实时快照。该接口统一归入系统监控 Controller；指标仅代表当前 JVM 进程，应用重启后会清零，多实例部署时需要分别采集每个实例的数据。

线程池参数以 `basic.thread-pool` 为前缀配置，包括 CPU 线程数与队列容量、虚拟线程并发上限、调度器线程数和应用关闭等待时间。

---

## 5. 模块依赖

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot4-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-jsqlparser</artifactId>
    </dependency>

    <!-- 其他 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>jakarta.servlet</groupId>
        <artifactId>jakarta.servlet-api</artifactId>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
</dependencies>
```

### 被依赖

| 模块 | 说明 |
|------|------|
| basic-common-core | 无依赖 |
| basic-common-web | 无依赖 |
| basic-api | 无依赖 |
| basic-service | 依赖 |
| basic-dao | 依赖 |
| basic-web | 依赖 |
