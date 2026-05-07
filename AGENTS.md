# AGENTS.md

本文档为 Codex 在本仓库中工作时提供指导。

## 项目概览

这是一个标准的 **Spring Boot 多模块架构** 项目，设计目标是支持 **单体演进** 或 **微服务架构**。项目使用 Java 23、Spring Boot 4.0.0、MyBatis-Plus 和 JWT 认证。

## 构建命令

```bash
# 构建所有模块
mvn clean install -DskipTests

# 运行应用（启动模块：basic-web）
mvn spring-boot:run -pl basic-web

# 运行单个测试类
mvn test -Dtest=TestClassName

# 运行单个测试方法
mvn test -Dtest=TestClassName#testMethodName

# 初始化数据库（先执行 initSql.sql）
mysql -u root -p basic_project < initSql.sql
```

## 架构

### 模块结构

```
basic-parent
├─ basic-common-core      # 通用工具（utils、result、enums、annotations）
├─ basic-common-web       # Web 层通用能力（advice、config、filters）
├─ basic-core             # 项目级能力（MyBatis、Redis、AOP、Security）
├─ basic-api              # 接口定义（Controller 接口、DTO/VO）
├─ basic-service          # 业务逻辑（Service 接口与实现）
├─ basic-dao              # 数据访问（Mapper 接口、Entity 类）
└─ basic-web              # Web 入口与应用启动
```

### 依赖流向（单向）

```
web → api → service → dao → core
                    ↓
              common-core
```

### 请求流向

```
客户端请求 → web.Controller（实现 api 接口）→ service → dao
```

**核心原则：**
- Controller 只负责接收参数和返回响应，不编写业务逻辑。
- api 模块定义接口，web 模块实现接口。
- Entity 类继承 `BaseEntity`，Mapper 接口继承 `BaseMapperPlus`。

### 数据模型转换

```
DTO → BO → Entity（持久化）
Entity → BO → VO（响应）
```

## 技术栈

- Java 23 与虚拟线程
- Spring Boot 4.0.0
- MyBatis-Plus 3.5.x
- MySQL 8.0
- Redis Cluster（Lettuce）
- JWT（jjwt 0.12.5）
- Spring Security
- Spring AOP

## 配置

主配置文件：`basic-web/src/main/resources/application.yml`

- 服务端口：8080
- 数据库：MySQL，地址 `192.168.0.110:3306`，库名 `basic_project`
- Redis：集群模式，地址 `192.168.0.110:6379-6384`
- 数据库初始化文件：项目根目录下的 `initSql.sql`

## 启动

运行 `basic-web` 模块，其中包含：
- `@SpringBootApplication` 启动类：`basic-web/src/main/java/com/basic/web/WebApplication.java`
- 实现 basic-api 接口的 `@RestController`

## 测试

测试类位于 `basic-web/src/test/java/`：
- `MybatisTest.java`：数据库测试
- `RedisTest.java`：Redis 测试

## 重要注意事项

- **包名拼写问题：** Service 包名是 `com.basic.sericve`，不是 `service`。
- **Entity 类：** 继承 basic-core 中的 `BaseEntity`。
- **Mapper 接口：** 继承 basic-core 中的 `BaseMapperPlus`。
- **Redis 缓存：** 使用 `@RedisCache` 注解进行缓存。
- **后续文档输出：** 本仓库后续新增或修改的文档内容默认使用中文。

## 设计原则

- **高内聚、低耦合**
- **接口定义与实现分离**
- **上层依赖下层，不允许反向依赖**
- **api 模块保持稳定，service/web 可按需重构**
- **Controller 不编写业务逻辑**
