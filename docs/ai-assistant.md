# AI 助手端到端说明

## 一、整体定位

AI 助手由前端浮窗、Web 接口、service 业务编排、`basic-ai` 基础设施和 dao 持久化共同组成。`basic-ai` 负责统一助手运行时、模型提供商、ChatMemory 和聊天历史基础设施；业务层只依赖其网关、配置端口和记忆管理能力，不直接创建第三方模型客户端。

```text
basic-service → basic-ai → basic-dao → basic-core
```

模块不得依赖 `basic-service`、`basic-api` 或 `basic-web`。登录态读取、用户级并发控制、SSE 事件包装、API VO 转换和业务事务仍由 service/web 层负责。

## 二、当前能力

| 能力 | 当前实现 |
|------|----------|
| AI 框架 | Spring AI 2.0.0 |
| 模型提供商 | DeepSeek |
| 模型 | `DEEPSEEK_V4_FLASH` |
| 单次最大输出 | 4096 tokens |
| 上下文 Advisor | `MessageChatMemoryAdvisor` |
| 上下文窗口 | `MessageWindowChatMemory`，每个会话最多 20 条消息 |
| 模型记忆存储 | `JdbcChatMemoryRepository` + MySQL |
| 业务历史存储 | `ai_chat_message` |
| 响应方式 | POST SSE 流式输出 |
| 统一助手 | 空工具时进行纯聊天，有工具时按需调用当前 Run 注册的能力 |
| 安全限制 | 默认最多 16 次工具调用，单次 AI 模型流最长 90 秒 |
| 写操作 | 只生成待审批 Action，用户自然语言确认后由 service 事务执行 |

第一版仅在桌面端提供右下角浮动聊天窗口，支持拖动、缩放、最小化、停止生成、重试、清空记录和加载更早历史。移动端不会加载聊天组件。

## 三、目录结构

```text
basic-ai
└─ src/main/java/com/basic/ai
   ├─ assistant/            统一助手网关、信号、运行时硬限制和会话日志
   ├─ chat/
   │  ├─ memory/            ChatMemory 配置与管理
   │  └─ history/           业务聊天历史存储与分页模型
   ├─ config/               模型配置读取端口
   ├─ event/                AI 配置变更事件
   └─ provider/deepseek/    DeepSeek 客户端工厂和助手网关实现

basic-service/src/main/java/com/basic/sericve/ai
├─ assistant/
│  ├─ action/               待审批操作生命周期与预览模型
│  ├─ approval/             自然语言审批意图路由
│  ├─ capability/           能力扩展点及部门、公告能力
│  └─ operation/            Operation 声明、注册和工具转换
├─ chat/                    聊天 Service 接口与实现
└─ config/                  系统配置到模型配置端口的适配

basic-dao/src/main/java/com/basic/dao/ai
├─ action/                  待审批 Action Entity 与 Mapper
└─ chat/                    聊天消息 Entity 与 Mapper
```

## 四、调用流程

```text
桌面端浮窗
  → POST /ai/chat/stream
  → AiChatServiceImpl
  → AiAssistantOrchestrator
  → AssistantOperationToolFactory
  → AssistantOperationRegistry
  → AiAssistantGateway
  → 当前用户有权使用的工具集合
  → MessageChatMemoryAdvisor
  → DeepSeekAssistantGateway
  → DeepSeek V4 Flash
  → SSE start / delta / tool_start / tool_result / approval_required / done / error
```

`DeepSeekChatClientFactory` 按当前系统配置创建并缓存 `ChatClient`。管理员更新 API Key 且事务提交后，`AiConfigChangedEvent` 会使旧客户端缓存失效，下一次调用再按新配置创建客户端。

会话 ID 直接使用当前登录用户 ID，因此不同用户的模型上下文相互隔离。同一用户同一时间只允许一个生成订阅，跨标签页并发请求会返回“AI 正在生成回答”的业务错误。

所有用户统一进入 `AiAssistantOrchestrator`。`AssistantOperationRegistry` 自动收集 service 层业务能力声明的 Operation，`AssistantOperationToolFactory` 再根据当前登录权限生成本次运行的工具白名单：普通用户获得当前用户可见公告查询工具；后台用户按现有权限获得对应业务工具。用户 ID、权限和触发消息 ID 均由服务端冻结，模型不能通过输入覆盖。用户消息会先写入 `ai_chat_message`，消息 ID 同时作为本次完整会话日志的关联标识；确认、取消、修改和含糊回复都使用各自的触发消息 ID。

统一助手使用开放规划，但不具备任意代码、HTTP 或数据库访问能力。模型只能看到本次请求注册的 Spring AI `ToolCallback`；`GuardedToolCallingManager` 在实际执行前统一检查工具调用总数，整个 AI 模型流使用不可续期的绝对总时限。普通运行时异常会转换为稳定业务错误码，原始异常消息不会进入模型或 SSE。工具结果视为不可信业务数据，系统提示禁止把工具内容当作指令，也禁止输出内部思维过程。

公告和部门能力复用以下现有权限。工具集合按当前请求的登录用户权限组装；最终确认时会使用确认请求中的权限再次校验，预览本身不能绕过最终写权限：

| 权限 | 可用能力 |
|------|----------|
| `system:notice:query` | 查询公告、详情、公告类型和接收目标 |
| `system:notice:add` | 在同时具有查询权限时生成新增公告草稿预览 |
| `system:notice:edit` | 在同时具有查询权限时生成修改公告预览 |
| `system:notice:delete` | 在同时具有查询权限时生成删除草稿或已撤回公告预览 |
| `system:notice:publish` | 在同时具有查询权限时生成发布已有公告预览 |
| `system:notice:add` + `system:notice:publish` | 在同时具有查询权限时生成新建并立即发布公告预览 |
| `system:notice:withdraw` | 在同时具有查询权限时生成撤回已发布公告预览 |
| `system:dept:query` | 查询部门摘要、详情和部门树 |
| `system:dept:add` | 生成新增单个部门的预览 |
| `system:dept:edit` | 生成修改单个部门的预览 |
| `system:dept:delete` | 生成删除单个部门的预览 |

字典和字典项工具复用 `system:dict:*` 与 `system:dict:item:*` 权限。用户、角色、权限菜单、系统日志、登录日志、操作日志、在线用户和监控仅提供受限查询；系统配置、文件、认证、密码、授权关系变更以及日志删除均不注册 AI 工具。

普通用户公告查询使用 `AssistantRequestContext.userId` 调用可见公告 Service，不接受模型传入用户 ID。所有分页默认 10 条、最大 20 条；非分页列表返回 `total`/`truncated`，公告和日志正文按固定长度裁剪。

所有 `*_propose` 写工具只创建 `PENDING` Action 和审批预览，不直接修改业务表；参数校验失败时结果返回模型继续追问，只有成功创建 Action 后才由运行时动态设置 `returnDirect` 并结束本次模型循环。用户直接回复自然语言即可确认、修改或取消，不使用确认按钮。修改表达优先于确认表达，含糊回复只会继续追问。

公告能力提供以下写工具，每个工具只对应一个业务操作：

| 工具 | Action Type | 操作 |
|------|-------------|------|
| `notice_create_propose` | `NOTICE_CREATE` | 新增公告草稿 |
| `notice_update_propose` | `NOTICE_UPDATE` | 修改一个已有公告 |
| `notice_delete_propose` | `NOTICE_DELETE` | 删除一个草稿或已撤回公告 |
| `notice_create_and_publish_propose` | `NOTICE_PUBLISH` | 新建一个公告并立即发布 |
| `notice_publish_existing_propose` | `NOTICE_PUBLISH_EXISTING` | 发布一个已有草稿或已撤回公告 |
| `notice_withdraw_propose` | `NOTICE_WITHDRAW` | 撤回一个已发布公告 |

`NOTICE_PUBLISH` 保留原 Action Type，确保升级前已保存的待确认记录仍能反序列化和执行。修改工具接收增量字段，服务端读取当前完整公告后生成带版本号的全量快照；删除、发布已有公告和撤回也保存服务端公告版本，确认前若公告内容或状态已变化则拒绝执行，要求重新生成预览。

## 五、聊天记忆与业务历史

AI 聊天使用两类存储，二者用途不同，不能互相替代：

| 表 | 用途 | 保留范围 |
|----|------|----------|
| `SPRING_AI_CHAT_MEMORY` | 供 Spring AI Advisor 自动读取和写入模型上下文 | 每个会话最近 20 条消息 |
| `ai_chat_message` | 面向用户的完整聊天历史、游标分页和部分回答状态 | 全部未删除业务消息 |

用户问题会在模型调用或审批分类前先写入业务历史，并将生成的消息 ID 作为完整会话日志关联标识；助手回答在运行结束后单独保存。客户端停止生成或上游发生异常时，已经产生的助手内容会以 `partial = 1` 保存；若首个 token 前失败，只保留已写入的用户问题，不写入空助手消息。错误和取消链路共用一次性保存保护，避免重复写入部分回答。

清空聊天记录时，service 会在事务中同时清理当前用户的完整业务历史、Spring AI 模型记忆，并取消尚未执行的待审批 Action。清空操作不可恢复。

## 六、AI 运行日志与审批状态

AI 助手运行过程和写操作采用不同记录方式：

| 记录 | 内容边界 |
|------|----------|
| `com.basic.ai.assistant.conversation` DEBUG 日志 | 完整模型请求与响应、工具对话历史，写出前执行结构化与文本脱敏 |
| `ai_agent_action` | 类型化写操作快照、触发消息 ID、有效期和执行结果 ID |
| `sys_oper_log` | 确认写操作后的用户、Action、业务结果 ID 和脱敏操作轮廓 |

完整会话日志只在启用对应 DEBUG 级别时输出，密码、Token、API Key、身份证号等敏感数据会被遮蔽。Action 状态只保存 `PENDING`、`EXECUTED` 和 `CANCELLED`；过期由 `expires_at` 实时判断，不额外写入审计状态。创建、取消 Action 时先锁定稳定的 `sys_user` 用户行，使同一用户跨业务能力最多只有一个有效 Pending；确认执行时再锁定 Action 行，通过同一个 `AssistantActionSpec` 复验归属、有效期、当前权限和业务对象。重复确认已执行 Action 不再次执行 Spec，业务写入、Action 状态和脱敏操作日志在同一事务中提交或回滚。

## 七、配置说明

DeepSeek API Key 由管理员在“系统设置 → AI 设置”中维护，对应数据库配置键：

```text
ai.deepseek.apiKey
```

API Key 不应写入源码、README、YAML、日志或异常信息。系统设置接口仅返回“是否已配置”和脱敏后的值；模型客户端通过 `AiModelConfigProvider` 在运行时读取真实配置。

Spring AI JDBC ChatMemory 的建表策略位于 `basic-web` 环境配置：

```yaml
spring:
  ai:
    chat:
      memory:
        repository:
          jdbc:
            initialize-schema: always # 开发环境
            platform: mysql
```

生产环境使用 `initialize-schema: never`，必须先执行 `initSql.sql` 创建 `SPRING_AI_CHAT_MEMORY` 和 `ai_chat_message` 表，禁止依赖应用启动时自动变更生产数据库结构。

AI 助手运行限制位于 `basic-web/src/main/resources/application.yml`：

```yaml
basic:
  ai:
    agent:
      max-tool-calls: 16
      timeout: 90s
      approval-ttl: 15m
```

## 八、HTTP 接口

所有接口都位于 `/ai/chat`，必须携带有效 JWT。用户 ID 只从 Spring Security 上下文读取，不接受客户端传入。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/chat/stream` | 发送消息并以 SSE 返回模型增量，消息最长 4000 个字符 |
| GET | `/ai/chat/messages` | 按消息 ID 游标查询历史，默认 50 条，范围 1～100 条 |
| DELETE | `/ai/chat/memory` | 清空当前用户的模型记忆和完整聊天历史 |

历史首次查询示例：

```http
GET /ai/chat/messages?limit=50
Authorization: Bearer <token>
```

继续向前翻页时，将上一次响应中的 `nextBeforeId` 作为 `beforeId`：

```http
GET /ai/chat/messages?beforeId=123&limit=50
Authorization: Bearer <token>
```

流式接口会发送以下 SSE 事件：

| 事件 | 含义 |
|------|------|
| `start` | 后端已接受本次生成请求 |
| `delta` | 模型回答增量，正文位于 `data.content` |
| `tool_start` | 工具开始执行，只包含展示名称和脱敏摘要 |
| `tool_result` | 工具执行完成，只包含脱敏结果摘要 |
| `approval_required` | 写操作预览已生成，结构化数据位于 `data.pendingAction` |
| `done` | 回答生成和持久化完成 |
| `error` | 业务或上游错误，包含稳定错误码与用户可读消息 |

前端明确禁用 SSE 自动重连，避免网络异常后重复提交同一问题和重复保存聊天记录。

## 九、初始化与验证

全新环境先初始化数据库：

```bash
mysql -u root -p < initSql.sql
```

至少编译受影响模块及其依赖：

```bash
mvn -pl basic-web -am compile
```

启动后应依次验证：

1. 在系统设置中保存 DeepSeek API Key，并执行连接测试。
2. 桌面端右下角打开 AI 助手，确认可以收到增量回答。
3. 刷新页面，确认完整聊天历史仍可读取。
4. 停止一次生成，确认已产生的回答显示“回答未完成”。
5. 清空记录，确认模型上下文和历史列表同时清除。
6. 请求新增公告但不指定范围，确认 AI 助手必须追问。
7. 分别请求新增草稿、修改、删除、发布已有公告、撤回和新建并发布，确认每次只生成对应单操作预览。
8. 对任一公告预览使用自然语言分别验证确认、修改和取消。
9. 刷新页面和切换标签页，确认待审批预览能够恢复且不会重复执行。
10. 使用部门查询、新增、修改和删除工具，确认三个写操作都只生成单操作预览并等待自然语言确认。
11. 使用用户、角色、权限、日志、在线用户和监控查询，确认结果分页、截断和敏感字段边界生效。
12. 使用字典和字典项新增、修改、删除，确认 Action 版本冲突不会执行旧快照。

## 十、扩展约束

新增模型提供商时，应实现或扩展 `AiAssistantGateway` 和配置端口，并将实现放入 `provider` 对应子包，不要让业务 Service 直接依赖第三方客户端。需要调整上下文策略时，优先修改 `AiChatMemoryConfiguration`，保持 service 层不感知 Spring AI 的具体记忆实现。

新增 AI 助手工具时必须遵守以下约束：

1. `basic-ai` 只提供业务无关运行时，不依赖 service、api 或 web。
2. 每个业务能力在 service 实现 `AssistantCapability`，放入 `ai/assistant/capability/{业务名}`，通过 `operations()` 返回稳定且不可变的 Operation 列表；需要当前用户范围时只能使用运行时传入的 `AssistantRequestContext`。
3. 普通查询使用 `AssistantOperations.query(...)` 声明名称、描述、权限和查询函数，并限制返回数量和正文长度。
4. 普通写操作使用 `AssistantOperations.action(...)` 声明 Action Type、权限、预览、领域校验和执行函数，不再新增独立 Handler 或 Proposal Tool 方法。
5. 模型输入与最终快照不同时使用 `.prepare(...)`，例如公告修改先读取当前完整数据、合并增量字段并固化版本。
6. 写工具只创建类型安全且有有效期的 Pending Action；通用 Action Service 在确认事务中使用同一个 Action Spec 重新检查权限、Bean Validation 和业务状态后执行。

典型查询只需要一个 Builder 声明：

```java
query("dept_get", DeptGetInput.class, DeptDetailResult.class)
        .description("按ID读取部门详情")
        .permissions("system:dept:query")
        .execute(input -> toDetail(deptService.getDeptById(input.id())))
        .build();
```

典型写操作把真正的业务差异留在预览、校验和执行函数中：

```java
action("dept_delete_propose", DeptDeleteActionPayload.class)
        .actionType("DEPT_DELETE")
        .description("生成删除单个部门的预览；不会直接删除")
        .permissions("system:dept:delete")
        .preview("删除部门待确认", payload -> fields("部门ID", payload.id()))
        .validate(payload -> deptService.validateDeptDelete(payload.id()))
        .execute(payload -> {
            deptService.deleteDept(payload.id());
            return result(payload.id(), "部门删除成功，部门ID：" + payload.id());
        })
        .build();
```

完整会话日志写出前必须遮蔽 DeepSeek API Key、密码、Token 和身份证号等敏感数据。修改模型、记忆、Security 或数据表结构后，需要同步检查配置页面、DTO/VO、`initSql.sql` 和本文档。
