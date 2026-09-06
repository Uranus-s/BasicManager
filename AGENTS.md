# AGENTS.md

本文档为 Codex 在本仓库中工作时提供指导。后续新增或修改的仓库文档默认使用中文。

## 项目概览

这是一个前后端一体的管理系统项目。后端采用 Spring Boot 多模块架构，设计目标是支持单体演进或后续拆分为微服务，使用 Java 23、Spring Boot 4.0.0、MyBatis-Plus、Spring Security、JWT、Redis 和 MySQL；配套前端位于 `basic-ui`，使用 Vue 3、Rspack、Element Plus、Vuex、Vue Router 和 Axios。

当前工程以 `basic-web` 作为启动模块，业务能力按 `api`、`service`、`dao`、`core`、`common` 分层组织。

## 常用命令

```bash
# 构建所有模块（跳过测试）
mvn clean install -DskipTests

# 编译所有模块
mvn clean compile

# 运行全部测试
mvn test

# 运行单个测试类
mvn test -Dtest=TestClassName

# 运行单个测试方法
mvn test -Dtest=TestClassName#testMethodName

# 启动 Web 应用
mvn spring-boot:run -pl basic-web

# 只编译指定模块及其依赖
mvn -pl basic-web -am compile

# 安装前端依赖
cd basic-ui && pnpm install

# 启动前端开发服务器（默认端口 8091）
cd basic-ui && pnpm run serve:rspack

# 构建前端
cd basic-ui && pnpm run build

# 初始化数据库结构及演示数据（仅适用于全新环境）
mysql -u root -p < initSql.sql
```

## 模块结构

```text
basic-parent
├─ basic-common           # 公共模块分组目录（非 Maven 模块）
│  ├─ basic-common-core   # 通用能力：result、exception、utils、validate 等
│  └─ basic-common-web    # Web 通用能力：advice、web exception、Jackson 配置等
├─ basic-core             # 项目核心能力：MyBatis、Redis、Security、AOP、日志等
├─ basic-ai               # AI 基础设施：统一助手运行时、模型提供商、ChatMemory 和聊天历史
├─ basic-api              # 对外接口契约：Controller 接口、DTO、VO
├─ basic-service          # 业务逻辑：Service 接口与实现、领域编排、存储策略
├─ basic-dao              # 数据访问：Entity、Mapper、Mapper XML
├─ basic-web              # 后端应用入口：启动类、Controller 实现、Web 配置
└─ basic-ui               # 配套前端：Vue 3、Rspack、Element Plus
```

## 依赖与调用方向

优先保持单向依赖，避免下层反向依赖上层。

```text
basic-web → basic-api → basic-common-core
basic-web → basic-service → basic-ai → basic-dao → basic-core
basic-web → basic-common-web
basic-service → basic-api / basic-ai / basic-dao / basic-core / basic-common-core
```

请求调用链：

```text
客户端请求 → web.Controller（实现 api 接口）→ service → dao/mapper → database
```

数据转换方向：

```text
DTO → Entity/业务对象 → 持久化
Entity → VO → 响应
```

当前代码未严格引入独立 BO 层。新增代码如需 BO，可以在 service 内部或清晰的业务包下引入，避免为了形式增加无意义转换。

## 分层职责

### basic-api

- 定义 Controller 接口、请求 DTO、响应 VO。
- Controller 接口放在 `com.basic.api.controller` 下，使用 Spring MVC 注解声明路由、参数和返回类型。
- DTO 放在 `com.basic.api.dto.{业务名}` 下，用于接收请求参数。
- VO 放在 `com.basic.api.vo.{业务名}` 下，用于响应数据。
- DTO/VO 不写业务逻辑，不直接依赖 dao/entity。

### basic-web

- 只放应用启动类、Controller 实现、Web 入口相关配置。
- Controller 实现 `basic-api` 中的接口，负责参数接收、登录态读取、调用 service、包装 `Result`。
- Controller 不写业务规则、数据库查询、复杂数据转换。
- 有状态变更的接口优先添加 `@OperateLog`。
- 路由注解需要和 api 接口保持一致；修改接口路径时同时检查实现类，避免出现接口和实现路径不一致。

### basic-service

- 包名当前是 `com.basic.sericve`，注意拼写是 `sericve`，不是 `service`。不要擅自整体重命名。
- Service 接口放在 `{业务名}.service`，实现类放在 `{业务名}.impl`。
- 业务校验、事务、跨表编排、缓存使用、文件存储选择等逻辑放在 service。
- 需要事务的方法使用 `@Transactional(rollbackFor = Exception.class)`。
- 业务异常优先使用 `BusinessException` 和 `ResultEnum`，不要直接向 Controller 抛裸 RuntimeException。

### basic-ai

- 放统一助手运行时、模型提供商、ChatMemory 和聊天历史基础设施。
- 可以依赖 `basic-dao`、`basic-core` 和 `basic-common-core`，不得依赖 `basic-service`、`basic-api` 或 `basic-web`。
- 聊天业务编排、登录用户权限、SSE 事件包装和 API VO 转换仍放在 `basic-service`。
- 新增模型提供商时通过网关或配置端口扩展，不要让业务 Service 直接构建第三方模型客户端。

### basic-dao

- Entity 放在 `com.basic.dao.{业务名}.entity`。
- Mapper 接口放在 `com.basic.dao.{业务名}.mapper`。
- Mapper XML 放在 `basic-dao/src/main/resources/mapper`。
- Mapper XML 的 namespace 必须和 Mapper 接口全限定名一致。
- 新增表字段时同步检查 Entity、Mapper XML、DTO/VO、SQL 初始化脚本。
- 当前部分 Entity 直接声明审计字段、逻辑删除字段和乐观锁字段，并未统一继承 `BaseEntity`。新增 Entity 优先复用 `BaseEntity` 和 `BaseMapperPlus`；修改旧 Entity 时先遵循现有实现，避免大范围无关重构。

### basic-core

- 放项目级基础设施：MyBatis 配置、分页、数据权限、Redis、Security、JWT、日志 AOP、SPI。
- 不放具体业务逻辑，不依赖 service/web。
- 修改 Security、JWT、Redis、MyBatis 插件时要扩大验证范围，因为会影响多个模块。

### basic-common-core / basic-common-web

- `basic-common-core` 放与 Web 无关的通用类型、异常、返回结果、工具类、校验注解。
- `basic-common-web` 放 Web 层通用能力，如全局响应封装、全局异常处理、Jackson 配置。
- 通用模块 API 要谨慎修改，优先保持向后兼容。

## 代码约定

- Java 版本为 23，编译使用 Maven Compiler Plugin 的 `<release>23</release>`。
- 项目使用 Lombok，已有代码常用 `@Getter`、`@Setter`、`@ToString`、`@RequiredArgsConstructor`。
- 构造器注入优先于字段注入，已有 `@RequiredArgsConstructor` 的类继续沿用。
- MyBatis-Plus 查询优先使用 `LambdaQueryWrapper`，避免硬编码字段名。
- 密码必须通过 `PasswordEncoder` 处理，不允许明文保存或比较。
- 登录用户信息从 Spring Security 上下文中获取，已有模型为 `LoginUser`。
- 返回值统一使用 `Result<T>`；分页响应使用 `PageResult<T>`。
- 对外错误码和消息优先沉淀到 `ResultEnum`。
- 文件上传相关逻辑优先通过 `ISysFileService` 和 `FileStorageService` 体系扩展。
- 缓存使用 `@RedisCache` 前先确认 key、过期时间和失效策略，避免缓存用户权限、角色等强一致数据时产生脏读。
- 新增代码必须写好中文注释，重点说明业务意图、关键流程、边界条件和非显然约束；避免只复述代码表面含义的无效注释。

## 命名约定

- Controller 接口：`{Biz}Api`
- Controller 实现：`{Biz}Controller`
- Service 接口：`I{Biz}Service`
- Service 实现：`{Biz}ServiceImpl`
- Mapper：`{Biz}Mapper`
- Entity：`{Biz}`
- 新增 DTO：`{Biz}AddDTO`、`{Biz}UpdateDTO`、`{Biz}QueryDTO`
- 新增 VO：`{Biz}VO`、`{Biz}ListVO`、`{Biz}TreeVO`
- 系统功能包通常使用 `sysXxx` 命名，新增同类功能时保持现有大小写风格。

## 新增接口流程

1. 在 `basic-api` 新增或修改 DTO、VO、`{Biz}Api`。
2. 在 `basic-dao` 新增或修改 Entity、Mapper、Mapper XML。
3. 在 `basic-service` 新增或修改 Service 接口和实现，处理业务校验、事务、转换。
4. 在 `basic-web` 新增或修改 Controller，实现 api 接口并调用 service。
5. 如涉及数据库结构或初始化业务数据，统一更新完整初始化脚本 `initSql.sql`。
6. 如接口会改变认证、权限、菜单或日志行为，同步检查 Security、权限初始化和 `@OperateLog`。
7. 运行最小必要验证命令，至少编译受影响模块。

## 数据库与配置

主配置文件：

- `basic-web/src/main/resources/application.yml`
- `basic-web/src/main/resources/application-dev.yml`
- `basic-web/src/main/resources/application-prod.yml`

默认配置关注点：

- 服务端口：`8080`
- 数据库：MySQL，库名 `basic_project`
- Redis：集群模式
- 完整初始化脚本：`initSql.sql`，会创建数据库、重建项目表并写入演示数据，仅适用于全新环境

不要在代码或文档中新增真实密码、生产密钥、私有 token。示例配置使用占位符。

## 安全与权限

- JWT 工具位于 `basic-core/src/main/java/com/basic/core/security/util/JwtUtil.java`。
- Security 配置位于 `basic-core/src/main/java/com/basic/core/security/config/SecurityConfig.java`。
- 认证业务主要位于 `basic-service/src/main/java/com/basic/sericve/auth`。
- 当前登录用户模型为 `com.basic.core.security.model.LoginUser`。
- 权限、角色、菜单相关改动需要同时检查初始化权限、用户路由、角色权限关联和前端所需字段。
- 不要绕过 `PasswordEncoder`、JWT 校验或 Security 上下文。

## 日志与审计

- 操作日志注解：`@OperateLog`。
- 登录日志和操作日志的 AOP 位于 `basic-core`。
- 数据库日志落库实现位于 `basic-service/src/main/java/com/basic/sericve/log`。
- 新增会修改数据的后台管理接口时，优先补充操作日志。
- 不要在日志中记录明文密码、token、身份证号等敏感信息。

## 测试与验证

测试主要位于 `basic-web/src/test/java`，当前包含数据库和 Redis 相关测试。涉及外部 MySQL/Redis 的测试可能依赖本地或局域网环境，运行前确认配置可用。

验证范围应与改动风险匹配，默认只执行能够覆盖本次改动的最小必要检查，不为了形式完整而运行无关测试、全量构建或依赖外部环境的测试。

推荐验证策略：

```bash
# 文档或小范围纯代码改动后，至少检查 Git diff
git diff -- AGENTS.md

# Java 代码改动后，按需编译直接受影响的模块及其必要依赖
mvn -pl basic-web -am compile

# 只有改动涉及共享基础设施、跨模块契约或明确存在较大回归风险时，才考虑运行全量测试
mvn test
```

优先运行与变更行为直接相关的测试。全量测试、重复测试和依赖 MySQL、Redis 等外部环境的集成测试不作为默认步骤；只有任务明确要求、发布前验证或风险足以证明其必要性时才执行。如果必要测试因环境缺失无法运行，需要在交付说明中明确说明。

测试相关代码不得纳入 Git，包括测试用例、测试夹具、Mock、测试专用脚本和测试专用配置。确需编写临时验证代码时，应放在仓库外，或在验证完成后删除，不得暂存或提交。

## 工程取舍原则

- 遵循 YAGNI 和最小改动原则，以当前明确需求和已知风险为依据，不为假设场景预先增加复杂度。
- 测试、备份、容灾、安全加固等措施按改动范围、数据价值、故障影响和用户明确要求决定；没有现实需求或可说明的风险时，不主动扩大范围。
- 默认选择满足当前需求的简单实现。只有存在不可逆数据风险、明确合规要求、已知故障场景或确定的扩展计划时，才引入额外机制，并说明必要性与维护成本。
- 不以“最佳实践”为由擅自增加冗余架构、通用抽象、降级链路、重复校验、全量回归测试、备份脚本或运维设施。
- 保留必要安全底线，例如不泄露密钥和敏感信息、密码使用 `PasswordEncoder`、不绕过 JWT 校验和 Security 上下文；不要将“避免过度设计”理解为取消这些已有约束。

## Codex 工作规则

- 修改前先快速阅读相关模块和已有实现，优先复用现有模式。
- 只改与任务相关的文件，不做无关格式化、重命名或架构调整。
- 遇到用户已有未提交改动时，不要回滚；需要基于现状继续工作。
- 禁止擅自执行 `git commit`；所有代码和文档改动必须由用户审核后亲自提交。
- 搜索文件和文本优先使用 `rg`。
- 手工编辑文件优先使用补丁方式，避免生成无关文件。
- 后续新增或修改文档默认使用中文。
- 新增代码时同步补充必要的中文类注释、方法注释或关键逻辑注释，方便后续维护者理解。
- 若发现文档与代码不一致，优先以代码现状为准，并在必要时同步修正文档。
- 开始任务时先判断实际风险和交付目标；除非用户明确要求或有直接证据表明必要，不主动扩展测试、备份、容灾、安全加固及其他非当前需求范围的工作。
- 禁止暂存或提交任何测试相关代码；交付前只需确认本次改动未将此类文件加入 Git 变更范围，不要擅自处理仓库中原有的测试文件或用户已有改动。

## 已知注意事项

- `basic-service` 的 Java 包名拼写为 `com.basic.sericve`，这是当前代码事实。
- `SysUserApi#getCurrentUserAvatar` 标注为 `GET /avatar`，当前 `SysUserController#getCurrentUserAvatar` 实现为 `GET /getAvatar`；修改相关接口时需要特别核对路径一致性。
- 根 POM 中声明的 `mybatis-plus.version` 为 `3.5.15`，但实际管理依赖使用 `mybatis-plus-spring-boot4-starter` `3.5.14`；升级依赖时要统一检查。
- 根 POM 中 `spring-boot-starter-aop` 当前显式版本为 `4.0.0-M2`，与 Spring Boot BOM 版本不同；调整依赖时不要只改一处。
- README 与模块 README 中可能仍保留示例性写法，例如 Entity 继承 `BaseEntity`、Mapper 继承 `BaseMapperPlus`。实际改代码时以当前源码为准。
