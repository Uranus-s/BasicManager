# basic-ai 模块说明

## 一、模块定位

`basic-ai` 是项目的 AI 基础设施模块，负责隔离 Spring AI、DeepSeek 客户端、模型网关和 ChatMemory 的具体实现。业务层只依赖模块提供的网关、配置端口和记忆管理能力，不直接创建第三方模型客户端。

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

桌面端右下角浮动助手同时提供“聊天”和“操作”模式。聊天模式支持拖动、缩放、最小化、停止生成、重试、清空记录和加载更早历史；操作模式只执行公共适配器从当前 Element Plus 页面扫描出的语义动作。移动端不会加载助手组件。

## 三、目录结构

```text
basic-ai
└─ src/main/java/com/basic/ai
   ├─ config/       模型配置读取端口
   ├─ agent/        网页代理单步决策网关、提示词隔离与内部模型
   ├─ deepseek/     DeepSeek 客户端工厂和网关实现
   ├─ event/        AI 配置变更事件
   ├─ gateway/      模型调用网关
   ├─ memory/       ChatMemory 配置、管理和业务历史存储
   └─ model/        AI 内部存储与分页模型
```

## 四、调用流程

```text
桌面端浮窗
  → POST /ai/chat/stream
  → AiChatServiceImpl
  → AiChatGateway
  → MessageChatMemoryAdvisor
  → DeepSeek V4 Flash
  → SSE start / delta / done / error
```

`DeepSeekChatClientFactory` 按当前系统配置创建并缓存 `ChatClient`。管理员更新 API Key 且事务提交后，`AiConfigChangedEvent` 会使旧客户端缓存失效，下一次调用再按新配置创建客户端。

会话 ID 直接使用当前登录用户 ID，因此不同用户的模型上下文相互隔离。同一用户同一时间只允许一个生成订阅，跨标签页并发请求会返回“AI 正在生成回答”的业务错误。

## 五、聊天记忆与业务历史

AI 聊天使用两类存储，二者用途不同，不能互相替代：

| 表 | 用途 | 保留范围 |
|----|------|----------|
| `SPRING_AI_CHAT_MEMORY` | 供 Spring AI Advisor 自动读取和写入模型上下文 | 每个会话最近 20 条消息 |
| `ai_chat_message` | 面向用户的完整聊天历史、游标分页和部分回答状态 | 全部未删除业务消息 |

正常生成完成后，一轮用户问题和助手回答会写入业务历史。客户端停止生成或上游发生异常时，已经产生的助手内容会以 `partial = 1` 保存；若首个 token 前失败，只保存用户问题，不写入空助手消息。

清空聊天记录时，service 会在事务中同时清理当前用户的完整业务历史和 Spring AI 模型记忆。清空操作不可恢复。

## 六、配置说明

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

## 七、HTTP 接口

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
| `done` | 回答生成和持久化完成 |
| `error` | 业务或上游错误，包含稳定错误码与用户可读消息 |

前端明确禁用 SSE 自动重连，避免网络异常后重复提交同一问题和重复保存聊天记录。

## 八、初始化与验证

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

## 九、扩展约束

新增模型提供商时，应实现或扩展 `AiChatGateway` 和配置端口，不要让业务 Service 直接依赖第三方客户端。需要调整上下文策略时，优先修改 `AiChatMemoryConfiguration`，保持 service 层不感知 Spring AI 的具体记忆实现。

日志不得输出 DeepSeek API Key、用户完整消息正文或第三方响应正文。修改模型、记忆、Security 或数据表结构后，需要同步检查配置页面、DTO/VO、`initSql.sql` 和本文档。

## 十、站内网页代理

### 10.1 分层边界

网页代理使用严格的单步协议，不把浏览器控制权直接交给模型：

```text
 basic-ui 自动扫描 Element Plus 页面并生成固定动作能力
  → basic-service 通用动作校验、状态机、风险策略与持久化
  → basic-ai 生成一个结构化单步决策
  → basic-service 再次校验通用动作协议
  → basic-ui 在当前路由执行临时语义目标并上报结果
```

- `basic-ai` 只把脱敏页面上下文转换为一个结构化动作或完成结论，不读取登录态，不持久化任务，也不执行 DOM。
- `basic-service` 负责用户绑定、客户端实例绑定、通用动作校验、风险确认、状态机、动作幂等、任务/动作落库和 SSE 事件；具体业务权限仍由原业务接口校验。
- `basic-ui` 通过公共适配器扫描当前可见且可用的 Element Plus 控件，为本次页面快照生成临时 target；不接受模型生成的 CSS 选择器、脚本、`eval`、`new Function` 或任意 URL。
- `basic-web` 从 Spring Security 上下文读取 `LoginUser`，客户端不能传入用户 ID 或权限集合。

遵循项目 Element Plus 规范的登录后后台页面不需要注册专属 capability 或执行器。公共适配器支持按钮、菜单、文本输入、文本域、下拉选择、单选、多选、开关、分页、树节点和可见表格摘要。密码、API Key、Token、文件上传、隐藏输入和 AI 面板自身始终排除；自定义非 Element Plus 组件需要在公共适配器中统一扩展，不在业务页面编写 AI 代码。

### 10.2 开关与风险控制

系统配置键 `ai.agent.enabled` 默认值为 `false`。管理员显式开启前，创建任务接口会拒绝执行。查询、查看、刷新、翻页、打开和关闭等点击为低风险；保存、提交、删除、发布、撤回、分配、移除等点击以及用途不明确的按钮按高风险处理。高风险动作先进入 `WAITING_CONFIRMATION`，只有当前用户确认后才会向原标签页下发；业务操作仍经过原页面事件、后端权限和事务。

前端使用 `sessionStorage` 保存当前标签页的 `clientInstanceId`。动作执行期间只在内存中设置任务/动作追踪上下文，Axios 为实际业务请求增加候选关联头并关闭自动重试：

```text
X-AI-Agent-Task-Id
X-AI-Agent-Action-Id
```

后端验证任务所有者、客户端、任务状态和活动动作后才把请求标记为 AI 来源。伪造或过期头不会写入可信日志关联。

### 10.3 数据表与接口

| 表 | 用途 |
|----|------|
| `ai_agent_task` | 用户任务、当前状态、页面版本、活动动作和失败码 |
| `ai_agent_action` | 单步动作、风险级别、确认人、结果摘要和执行时间 |
| `sys_oper_log` 的 AI 关联字段 | 记录实际业务操作对应的代理任务与动作 |

REST 与 SSE 接口统一位于 `/ai/agent`：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/status` | 查询开关和当前用户活动任务 |
| POST | `/tasks` | 使用当前页面快照创建任务 |
| GET | `/tasks/{taskId}` | 查询当前用户任务 |
| GET | `/tasks/{taskId}/events` | 订阅当前标签页的任务事件 |
| POST | `/tasks/{taskId}/actions/{actionId}/result` | 上报执行前路由/版本、执行结果及动作后的新快照；前者用于拒绝旧动作，后者用于下一步规划 |
| POST | `/tasks/{taskId}/pause` | 暂停任务 |
| POST | `/tasks/{taskId}/resume` | 携带新快照恢复任务 |
| POST | `/tasks/{taskId}/cancel` | 终止任务 |
| POST | `/tasks/{taskId}/actions/{actionId}/confirm` | 确认当前高风险动作 |

SSE 事件包括 `action`、`confirmation`、`paused` 和 `task`。事件中心只重放最近 20 条事件；前端不自动重连，断线时先请求服务端暂停，防止重复执行非幂等动作。

### 10.4 验证

```bash
# 后端模块和测试
mvn -pl basic-web -am test

# 前端通用 Element Plus 适配器和既有公告契约
cd basic-ui
pnpm run check:ai-agent
pnpm run check:notice
pnpm run build

# 真实环境端到端验收
E2E_USERNAME=<隔离账号> E2E_PASSWORD=<密码> pnpm run test:e2e:ai-agent
```

端到端测试只使用唯一后缀数据，并通过页面清理本次创建记录。缺少 MySQL、Redis、DeepSeek 配置、已开启的 `ai.agent.enabled` 或隔离测试账号时，应明确记录未运行条件，不能将跳过表述为通过。
