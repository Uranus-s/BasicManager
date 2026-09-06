# basic-ai 模块说明

## 一、模块定位

`basic-ai` 是项目的 AI 基础设施模块，负责统一助手运行时、模型提供商、ChatMemory 和聊天历史存储。业务层通过网关、配置端口和记忆管理能力调用模型，不直接构建第三方模型客户端。

```text
basic-service → basic-ai → basic-dao → basic-core
```

本模块不得依赖 `basic-service`、`basic-api` 或 `basic-web`。登录态读取、用户权限、业务工具声明、审批事务、SSE 包装和 API VO 转换仍由 service/web 层负责。

完整调用流程、权限、审批状态、HTTP 接口和验证步骤见 [AI 助手端到端说明](../docs/ai-assistant.md)。

## 二、基础设施能力

| 能力 | 当前实现 |
|------|----------|
| AI 框架 | Spring AI 2.0.0 |
| 模型提供商 | DeepSeek |
| 模型 | `DEEPSEEK_V4_FLASH` |
| 模型调用 | `AiAssistantGateway` |
| 工具运行限制 | 最多 16 次工具调用，模型流最长 90 秒 |
| 上下文窗口 | `MessageWindowChatMemory`，每个会话最多 20 条消息 |
| 模型记忆存储 | `JdbcChatMemoryRepository` + MySQL |
| 业务历史存储 | `ai_chat_message` |
| 提供商配置 | `AiModelConfigProvider` |

## 三、目录结构

```text
basic-ai
└─ src/main/java/com/basic/ai
   ├─ assistant/
   │  ├─ gateway/             助手与审批意图模型网关
   │  ├─ logging/             模型会话日志与脱敏
   │  ├─ model/               助手信号和审批意图
   │  └─ runtime/             单次运行上下文、工具限制和超时
   ├─ chat/
   │  ├─ memory/              ChatMemory 配置与管理
   │  └─ history/             业务聊天历史存储与分页模型
   ├─ config/                 模型配置读取端口
   ├─ event/                  AI 配置变更事件
   └─ provider/deepseek/      DeepSeek 客户端工厂和网关实现
```

## 四、配置边界

DeepSeek API Key 由 service 层实现的 `AiModelConfigProvider` 在运行时提供。API Key 不得写入源码、README、YAML、日志或异常信息。

ChatMemory 建表策略和助手运行限制位于 `basic-web` 配置；生产环境必须通过 `initSql.sql` 初始化所需表，不由本模块在启动时变更生产数据库结构。

## 五、扩展约束

1. 新增模型提供商时，实现或扩展 `AiAssistantGateway` 和配置端口，并将实现放入 `provider/{提供商}` 子包。
2. 调整上下文策略时，优先修改 `AiChatMemoryConfiguration`，避免 service 层感知 Spring AI 的具体记忆实现。
3. 运行时只负责模型调用、工具执行边界和信号输出，不引入公告、部门、字典、用户、日志等具体业务依赖；所有业务 Capability 和权限声明位于 `basic-service`。
4. 业务工具按当前登录权限动态注册。普通用户可使用当前用户可见公告查询；后台查询和字典/字典项审批写入由 service 层显式声明，`basic-ai` 不提供通用 CRUD 或任意数据库访问。
4. 完整会话日志写出前必须遮蔽 API Key、密码、Token 和身份证号等敏感数据。
