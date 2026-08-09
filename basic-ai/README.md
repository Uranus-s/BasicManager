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

第一版仅在桌面端提供右下角浮动聊天窗口，支持拖动、缩放、最小化、停止生成、重试、清空记录和加载更早历史。移动端不会加载聊天组件。

## 三、目录结构

```text
basic-ai
└─ src/main/java/com/basic/ai
   ├─ config/       模型配置读取端口
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
