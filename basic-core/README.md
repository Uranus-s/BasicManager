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
