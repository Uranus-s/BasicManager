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
```
