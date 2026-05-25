# OpenClaw 设计调研

> 调研版本：v1
> 调研模型：GLM-5.1
> 调研日期：2026-05-14
> 调研对象：OpenClaw (v2026.5.x, GitHub 372k stars, TypeScript)
> 对照范围：通用大模型对话 01/02/03/04 文档
> 与同期 Hermes Agent 调研的关系：OpenClaw 是 Hermes 的前身/同源项目，Hermes 从 OpenClaw fork 而来。本调研侧重 OpenClaw 的差异化设计——Gateway 架构、会话模型、沙箱安全、模型 Failover、上下文压缩、以及与 Hermes 的架构分野。

## 1. 背景与定位

OpenClaw 是一个本地优先（local-first）的个人 AI 助手平台，定位为"Your own personal AI assistant. Any OS. Any Platform. The lobster way."。核心差异化：

- **本地优先**：运行在用户自己的设备上，不是云服务
- **单用户焦点**：设计为个人助手，不是企业多人系统
- **22+ 消息平台**：WhatsApp、Telegram、Signal、iMessage、Discord、Slack、微信、QQ、LINE 等
- **Gateway 架构**：单一守护进程作为控制平面，协调所有连接
- **多 Agent 路由**：不同频道/账号/对端可路由到隔离的 Agent
- **TypeScript 技术栈**：pnpm monorepo，Node 24

与当前项目的关系：

| 维度 | OpenClaw | 当前项目 V1 |
|------|---------|------------|
| 语言栈 | TypeScript (pnpm monorepo) | Java (JDK 21 + Spring Boot 4.0.5) |
| 产品形态 | 本地运行的个人 AI 助手（22+ 消息平台） | Web 端通用大模型对话 |
| 核心架构 | Gateway 守护进程 + WebSocket 协议 | Spring Boot + WebFlux + SSE |
| 会话模型 | 多频道路由 + DM 隔离 + 每日重置 | Web 单入口 + 用户隔离 |
| 部署模式 | 本地守护进程（launchd/systemd） | Spring Boot 单实例 |

两者技术栈和产品形态差异很大，但 OpenClaw 的 Gateway 架构、会话管理、沙箱安全、模型 Failover、上下文压缩等设计对当前项目的后续演进有系统性参考价值。

## 2. Gateway 架构——OpenClaw 的核心设计

### 2.1 架构概览

OpenClaw 的 Gateway 是单一、长驻的守护进程，拥有所有消息面和连接协调：

```
┌─────────┐  ┌──────────┐  ┌───────┐  ┌───────┐  ┌──────┐
│WhatsApp  │  │ Telegram │  │Discord│  │ Slack │  │ 微信  │  ...22+
└────┬────┘  └────┬─────┘  └───┬───┘  └───┬───┘  └───┬───┘
     │            │            │          │          │
     └────────────┴────────────┴──────────┴──────────┘
                         │
                    ┌────▼────┐
                    │ Gateway │  单一守护进程（127.0.0.1:18789）
                    │ (WS API) │  WebSocket + JSON
                    └────┬────┘
                         │
              ┌──────────┼──────────┐
              │          │          │
         ┌────▼───┐ ┌───▼────┐ ┌───▼────┐
         │Client  │ │ Node   │ │WebChat │
         │(macOS) │ │(iOS/   │ │        │
         │        │ │Android)│ │        │
         └────────┘ └────────┘ └────────┘
```

### 2.2 核心设计不变量

| 不变量 | 说明 |
|--------|------|
| 单 Gateway / 单 Baileys 会话 | 每台主机恰好一个 Gateway，控制一个 WhatsApp 会话 |
| 强制握手 | 首帧必须是 `connect`，违规硬关闭 |
| 无事件回放 | 客户端在连接断裂时必须刷新状态 |
| 幂等副作用 | `send` 和 `agent` 方法需要幂等键 + 服务端去重缓存 |

### 2.3 线协议（Wire Protocol）

| 类型 | 格式 | 说明 |
|------|------|------|
| Request | `{type:"req", id, method, params}` | 客户端发起 |
| Response | `{type:"res", id, ok, payload\|error}` | 服务端回复（按 id 匹配） |
| Event | `{type:"event", event, payload, seq?}` | 服务端推送 |

Agent 执行采用异步流式模式：

```
Client → Gateway:  req:agent
Gateway → Client:  res {runId, status:"accepted"}     ← 确认
Gateway → Client:  event:agent (streaming)            ← 流式事件
Gateway → Client:  res {runId, status, summary}        ← 最终结果
```

`runId` 将确认、流式事件和最终响应关联在一起。

### 2.4 对当前项目的启示

1. **Gateway 模式 vs Spring Boot 单体**：OpenClaw 的 Gateway 是一个独立的守护进程，而当前项目是 Spring Boot 内嵌的 WebFlux 服务。V1 不需要引入独立 Gateway，但后续如果要支持多入口（消息网关、CLI），可以考虑将 Agent 核心逻辑抽离为独立服务，Web 层作为接入层之一。

2. **幂等设计**：OpenClaw 对副作用操作强制幂等键 + 服务端去重。当前项目的前端排队机制已部分覆盖（同一会话串行发送），但后续如果要支持消息重试，应考虑在 API 层引入幂等键。

3. **异步确认 + 流式推送 + 最终结果**：当前项目的 SSE 流式响应已有类似模式（thinking → message 事件流），但缺少显式的"确认"和"最终结果"分离。后续可以考虑在 SSE 协议中增加 `ack` 事件类型。

4. **WebSocket vs SSE**：OpenClaw 使用 WebSocket 双向通信，当前项目使用 SSE 单向推送 + HTTP POST 发送。V1 的 SSE 方案足够，但后续如果需要服务端主动推送（如 Agent 工具执行进度、定时任务通知），WebSocket 会是更自然的选择。

## 3. 会话模型

### 3.1 消息路由规则

| 来源 | 路由行为 |
|------|---------|
| 直接消息 | 共享会话（默认） |
| 群聊 | 按群隔离 |
| 房间/频道 | 按房间隔离 |
| Cron 任务 | 每次运行新会话 |
| Webhook | 按 hook 隔离 |

### 3.2 DM 隔离（关键安全特性）

默认所有 DM 共享一个会话，适合单用户但对多用户部署有风险。配置选项：

| 值 | 行为 |
|-----|------|
| `main`（默认） | 所有 DM 共享一个会话 |
| `per-peer` | 按发送者跨频道隔离 |
| `per-channel-peer` | 按频道 + 发送者隔离（**推荐**） |
| `per-account-channel-peer` | 按账号 + 频道 + 发送者隔离 |

### 3.3 会话生命周期与重置

| 重置类型 | 触发条件 | 说明 |
|---------|---------|------|
| 每日重置（默认） | 本地时间凌晨 4:00 | 基于 `sessionStartedAt` |
| 空闲重置（可选） | 一段时间无交互 | 心跳、Cron、系统事件**不**延长会话 |
| 手动重置 | `/new` 或 `/reset` | `/new <model>` 还能切换模型 |

关键设计：系统事件（心跳、Cron、exec）的写入**不延长**每日或空闲重置的新鲜度。当重置滚动会话时，旧会话的排队系统事件通知被**丢弃**，避免过时信息进入新会话。

### 3.4 会话存储

| 数据 | 路径 |
|------|------|
| 会话索引 | `~/.openclaw/agents/<agentId>/sessions/sessions.json` |
| 会话转录 | `~/.openclaw/agents/<agentId>/sessions/<sessionId>.jsonl` |

会话维护支持自动修剪（按年龄和条目数量），默认为 `warn` 模式（仅报告），可切换为 `enforce` 模式（自动清理）。

### 3.5 对当前项目的启示

1. **会话生命周期管理**：当前项目 V1 的会话是手动管理（用户新建/删除）。OpenClaw 的每日重置 + 空闲重置是一个好实践——长期不活跃的会话在下次打开时应自动开始新上下文，而非继续旧上下文（可能导致 token 浪费和过时信息）。后续可以考虑在打开旧会话时提示"是否继续上次对话"。

2. **DM 隔离 → 用户隔离**：OpenClaw 的 `per-channel-peer` 隔离模式与当前项目的 `userCode` 隔离本质相同。当前项目已按 `userCode` 隔离会话和消息，这一点已覆盖。

3. **会话修剪**：OpenClaw 的会话修剪策略（按年龄 + 条目数上限）值得参考。后续实现时，应区分"保留"（群会话、用户对话）和"可清理"（Cron、子代理）的会话类型。

4. **JSONL 转录**：OpenClaw 使用 JSONL 存储会话转录，每行一条消息。当前项目使用 PostgreSQL 消息表，查询更灵活但写入更重。两种方案各有利弊，当前项目的 RDBMS 方案更适合 Web 应用的查询需求。

## 4. Agent 运行时与上下文管理

### 4.1 工作空间与上下文注入

Agent 的单一工作空间目录作为所有工具和上下文的根。新会话首轮时，OpenClaw 将以下文件注入系统 prompt 的"项目上下文"：

| 文件 | 用途 |
|------|------|
| `AGENTS.md` | 操作指令 + "记忆" |
| `SOUL.md` | 人设、边界、语气 |
| `TOOLS.md` | 用户维护的工具使用约定（不控制工具存在性） |
| `BOOTSTRAP.md` | 首次运行仪式（完成后删除） |
| `IDENTITY.md` | Agent 名称/风格/emoji |
| `USER.md` | 用户档案 + 称呼偏好 |

**注入规则**：空白文件跳过；大文件截断并标注（Agent 需读取文件获取完整内容）；缺失文件注入单行标记。

### 4.2 队列模式与消息转向（Steering）

| 模式 | 新消息到达时 | 交付时机 |
|------|------------|---------|
| `steer` | 注入当前运行 | 当前工具调用完成后、下一次 LLM 调用前 |
| `followup` | 暂挂 | 当前轮结束后开启新轮 |
| `collect` | 暂挂 | 当前轮结束后开启新轮 |

`steer` 模式的关键行为：转向消息在**工具调用完成后、下一次 LLM 调用前**交付，且**不再跳过**当前助手消息的剩余工具调用。

### 4.3 对当前项目的启示

1. **上下文文件注入**：OpenClaw 的 `AGENTS.md` / `SOUL.md` / `USER.md` 文件注入系统 prompt 的模式与 Hermes 的 MEMORY.md 类似，但更细化——区分了操作指令、人设、工具约定、用户档案。后续实现用户/项目上下文时，可以考虑这种分层注入。

2. **消息转向模式**：`steer` 模式允许用户在 Agent 执行工具的过程中追加指令，工具调用完成后才生效。这是一个优雅的并发控制设计——既不中断正在执行的工具调用，又让用户的后续指令不会被忽略。当前项目的前端排队机制已覆盖基本的串行化需求，但 `steer` 模式的"工具完成后、LLM 调用前"的精确插入点是更细粒度的控制。

3. **BOOTSTRAP.md 首次运行仪式**：这是一个有趣的 UX 设计——新用户首次使用时，Agent 执行一个初始化仪式（如了解用户偏好），完成后删除 BOOTSTRAP.md。后续如果需要用户引导流程，可以参考此模式。

## 5. 记忆系统

### 5.1 三层记忆架构

| 层级 | 文件 | 用途 | 加载时机 |
|------|------|------|---------|
| L1 长期记忆 | `MEMORY.md` | 持久事实、偏好、决策、摘要 | 每个 DM 会话开始时 |
| L2 每日笔记 | `memory/YYYY-MM-DD.md` | 详细日志、观察、会话摘要 | 今天和昨天的笔记自动加载 |
| L3 梦日记 | `DREAMS.md`（可选） | 梦境清扫摘要，供人工审查 | 不自动注入 |

### 5.2 记忆提炼流程

```
每日笔记 (memory/YYYY-MM-DD.md)
        ↓ distill/promote
长期记忆 (MEMORY.md)
```

- Agent 从每日笔记中**提炼**有用信息到 `MEMORY.md`
- 过时的长期条目应随时间移除
- 工作空间指令生成和心跳流程**周期性自动**处理
- 不需要为每个细节手动编辑 `MEMORY.md`

### 5.3 记忆搜索

| 工具 | 功能 |
|------|------|
| `memory_search` | 语义搜索——即使措辞不同也能找到相关笔记 |
| `memory_get` | 读取特定记忆文件或行范围 |

搜索引擎支持混合搜索（向量相似度 + 关键词匹配），自动检测嵌入 Provider（OpenAI / Gemini / Voyage / Mistral）。

### 5.4 记忆后端

| 后端 | 类型 | 关键特性 |
|------|------|---------|
| 内置（默认） | SQLite | 关键词搜索、向量相似度、混合搜索 |
| QMD | 本地优先 | 重排序、查询扩展、可索引工作空间外目录 |
| Honcho | AI-native | 跨会话记忆、用户建模、语义搜索 |
| LanceDB | 内置插件 | OpenAI 兼容嵌入、自动召回、自动捕获 |

### 5.5 梦境系统（Dreaming）

可选的后台整合机制：

- **选择加入**：默认禁用
- **定时调度**：启用后 `memory-core` 自动管理一个循环 Cron 任务
- **阈值门控**：晋升必须通过分数、召回频率和查询多样性门槛
- **可审查**：阶段摘要和日记条目写入 `DREAMS.md` 供人工审查

### 5.6 自动记忆刷新

在上下文压缩（compaction）之前，OpenClaw 运行一个**静默轮次**，提醒 Agent 将重要上下文保存到记忆文件。这是防止信息在压缩中丢失的关键保护。

记忆刷新可以使用本地模型（而非当前会话模型），避免浪费主模型 token：

```json
{
  "agents": {
    "defaults": {
      "compaction": {
        "memoryFlush": {
          "model": "ollama/qwen3:8b"
        }
      }
    }
  }
}
```

### 5.7 对当前项目的启示

1. **三层记忆 vs Hermes 的三层记忆**：OpenClaw 和 Hermes 的记忆架构在概念上相似（L1 长期 / L2 日记 / L3 搜索），但 OpenClaw 增加了"梦境系统"——一个后台整合机制，定期从短期记忆中提炼高信号内容到长期记忆。这个模式在 Java 技术栈中可以通过定时任务实现。

2. **记忆搜索的混合模式**：向量相似度 + 关键词匹配的混合搜索比单一策略更可靠。后续如果实现记忆/知识检索，应同时支持两种模式。当前项目已有 PostgreSQL，`pg_trgm` 覆盖关键词匹配，`pgvector` 可覆盖向量搜索。

3. **自动记忆刷新**：在上下文压缩前提醒 Agent 保存重要信息，是一个简单但关键的设计。当前项目后续实现上下文压缩时，应确保压缩前先执行记忆持久化。

4. **辅助任务用小模型**：记忆刷新、摘要等辅助任务使用本地小模型（如 Qwen3:8B），而非主模型。这是一个重要的成本优化策略。后续实现时，应支持为辅助任务配置独立模型。

## 6. 技能系统

### 6.1 SKILL.md 格式

```markdown
---
name: my-skill
description: Brief description
metadata: { "openclaw": { "requires": { "bins": ["uv"], "env": ["GEMINI_API_KEY"] }, "primaryEnv": "GEMINI_API_KEY" } }
---
# Skill instructions here
```

关键 frontmatter 字段：

| 字段 | 说明 |
|------|------|
| `name` | 技能标识（必须） |
| `description` | 简短描述（必须） |
| `user-invocable` | 是否暴露为斜杠命令（默认 true） |
| `disable-model-invocation` | 不注入 Agent 正常 prompt，仅斜杠命令可用 |
| `command-dispatch: "tool"` | 斜杠命令直接派发到工具，绕过模型 |
| `metadata.openclaw.requires` | 门控条件（bins/env/config） |

### 6.2 门控（Gating）机制

技能在加载时根据 `metadata.openclaw` 条件过滤：

| 条件 | 说明 |
|------|------|
| `requires.bins` | 二进制必须在 PATH 上 |
| `requires.anyBins` | 至少一个二进制存在 |
| `requires.env` | 环境变量必须存在 |
| `requires.config` | openclaw.json 路径必须为真 |
| `os` | 限制到特定操作系统 |

### 6.3 加载优先级（高到低）

| 优先级 | 来源 | 路径 | 可见范围 |
|--------|------|------|---------|
| 1 | 工作空间技能 | `<workspace>/skills` | 仅该 Agent |
| 2 | 项目 Agent 技能 | `<workspace>/.agents/skills` | 仅该工作空间的 Agent |
| 3 | 个人 Agent 技能 | `~/.agents/skills` | 该机器所有 Agent |
| 4 | 托管/本地技能 | `~/.openclaw/skills` | 该机器所有 Agent |
| 5 | 捆绑技能 | 随安装分发 | 全局 |
| 6 | 额外目录 | `skills.load.extraDirs` | 全局 |

同名冲突时，**最高优先级源胜出**。

### 6.4 Agent 技能白名单

```json5
{
  agents: {
    defaults: {
      skills: ["github", "weather"],  // 默认技能白名单
    },
    list: [
      { id: "writer" },                    // 继承 github, weather
      { id: "docs", skills: ["docs-search"] }, // 替换（不合并）默认
      { id: "locked-down", skills: [] },       // 无技能
    ],
  },
}
```

关键规则：非空 `agents.list[].skills` 列表是最终集合——**不与 defaults 合并**。

### 6.5 技能 Token 开销

```
总开销 = 195 + Σ (97 + len(name_escaped) + len(description_escaped) + len(location_escaped))
```

约每个技能 24 tokens + 字段长度。紧凑且可控。

### 6.6 Skill Workshop 插件

可选的实验性插件，可从 Agent 工作中观察到的可复用流程创建或更新技能：

- **默认禁用**——必须显式启用
- 仅写入 `<workspace>/skills`
- 支持**待审批**或**自动安全写入**模式
- 隔离不安全提案
- 写入后刷新技能快照，无需重启 Gateway

### 6.7 对当前项目的启示

1. **门控机制**：OpenClaw 的技能门控（bins/env/config/os）是一个优雅的设计——技能根据运行时环境自动显示/隐藏。后续实现工具/能力系统时，类似的环境门控可以确保只暴露当前环境可用的能力。

2. **技能白名单**：不同 Agent/场景可配置不同的技能集合，非空列表不与默认合并的规则避免了意外暴露。后续多 Agent 场景中，权限控制应同样明确。

3. **Token 开销控制**：OpenClaw 对技能注入的 token 开销有精确计算和限制。当前项目后续引入任何 prompt 注入机制时，都应有类似的 token 预算控制。

4. **command-dispatch: "tool" 模式**：斜杠命令直接派发到工具，绕过模型推理。这是一个重要的效率优化——确定性的命令不需要消耗 LLM 推理。后续如果实现斜杠命令系统，应区分"模型处理"和"直接派发"两种模式。

## 7. 上下文压缩（Compaction）

### 7.1 压缩流程

1. 较旧的对话轮次被**摘要**为一个紧凑条目
2. 摘要保存到会话转录中
3. 最近的消息**保持完整**（未摘要的尾部）

**拆分点处理**：工具调用与其匹配的 `toolResult` 条目**配对保留**，拆分边界不会切断工具块。

### 7.2 自动压缩

- **默认开启**——当会话接近上下文限制时自动触发
- 识别多种 Provider 的上下文溢出错误模式（`request_too_large`、`context length exceeded` 等）
- 压缩前自动提醒 Agent 保存重要信息到记忆文件

### 7.3 手动压缩

```
/compact Focus on the API design decisions
```

可附加指令引导摘要方向。

### 7.4 转录轮换

启用 `truncateAfterCompaction` 时：

- 不就地重写现有转录
- 从摘要 + 保留状态 + 未摘要尾部创建**新的后续转录**
- 之前的 JSONL 作为**归档检查点源**保留
- 后续转录丢弃短期重试窗口内的精确重复长用户轮次（防止频道重试风暴）

### 7.5 可插拔压缩 Provider

插件可注册自定义压缩 Provider：

```json
{
  "agents": {
    "defaults": {
      "compaction": {
        "provider": "my-provider"
      }
    }
  }
}
```

如果自定义 Provider 失败或返回空结果，回退到内置 LLM 摘要。

### 7.6 压缩 vs 修剪

| 特性 | 压缩（Compaction） | 修剪（Pruning） |
|------|------------------|----------------|
| 做什么 | 摘要旧对话 | 修剪旧工具结果 |
| 是否保存 | 是（在会话转录中） | 否（仅内存中，每次请求） |
| 范围 | 整个对话 | 仅工具结果 |

### 7.7 对当前项目的启示

1. **工具调用配对保留**：压缩时不能将工具调用和工具结果拆开——这是基本的上下文完整性约束。后续实现压缩时必须遵守。

2. **转录轮换**：不就地重写，而是创建新转录文件。这是一个安全的设计——原始数据不丢失。当前项目的 `summary_message_id` 字段已预留了类似语义。

3. **可插拔压缩 Provider**：允许不同的压缩策略（如专用小模型、规则化摘要）。后续可以提供默认的 LLM 摘要实现，同时允许插件替换。

4. **修剪作为轻量补充**：修剪工具结果（不保存，仅内存）是一个比完整压缩更轻量的策略。后续可以先实现修剪（简单），再实现完整压缩（复杂）。

5. **压缩前记忆刷新**：与第 5 节记忆系统一致——压缩前先保存，是最重要的保护。

## 8. 模型 Failover 与多模型管理

### 8.1 模型选择级联

| 步骤 | 来源 | 行为 |
|------|------|------|
| 1. 主模型 | `agents.defaults.model.primary` | 正常起点，使用 fallback 链 |
| 2. Fallback 链 | `agents.defaults.model.fallbacks`（有序列表） | 主模型失败后依次尝试 |
| 3. Provider Auth Failover | Provider 内 auth profile 轮换 | 在 Provider 内先尝试不同认证，再跳到下一个 fallback 模型 |

### 8.2 选择来源与行为差异

| 来源 | 行为 |
|------|------|
| 配置默认 | 使用 fallback 链 |
| 自动 Failover | 标记 `modelOverrideSource: "auto"`，后续轮次不重试已知故障的主模型 |
| 用户手动选择（`/model`） | **严格模式**——如果不可达，回复**显式失败**，不 fallback |
| Cron / Payload | 使用配置的 fallback 链，除非 payload 指定 `fallbacks: []`（严格模式） |

### 8.3 多模型表面

| 表面 | 用途 |
|------|------|
| 主模型 | 默认 Agent 交互 |
| 图像模型 | 仅当主模型不能处理图像时使用 |
| PDF 模型 | PDF 工具使用，fallback 到图像模型 |
| 图像生成模型 | `image_generate` 工具 |
| 音乐生成模型 | `music_generate` 工具 |
| 视频生成模型 | `video_generate` 工具 |

每个表面有独立的 primary + fallbacks 配置。

### 8.4 模型白名单

```json5
{
  agents: {
    defaults: {
      models: {
        "anthropic/claude-sonnet-4-6": { alias: "Sonnet" },
        "anthropic/claude-opus-4-6": { alias: "Opus" },
        "openai-codex/*": {},  // provider/* 通配符
      },
    },
  },
}
```

`provider/*` 条目允许动态发现新模型，无需手动更新白名单。

### 8.5 对当前项目的启示

1. **Failover 链设计**：当前项目 V1 是单一模型配置，但后续多模型场景中，有序 fallback 链是关键的高可用设计。建议在 Provider 抽象接口中预留 fallback 链支持。

2. **用户选择 vs 自动 Failover 的行为差异**：用户手动切换模型时应该是严格模式（不可达则报错），自动 Failover 应静默切换并标记。这个区分在 UX 上很重要——用户不会困惑于"为什么回复风格变了"。

3. **多模型表面**：不同任务使用不同模型（如主对话用大模型、摘要用小模型、图像分析用视觉模型）是成本优化的关键。当前项目后续可以在 `LlmProviderAdapter` 接口基础上，为不同任务配置不同模型。

4. **模型白名单**：限制可用模型范围是企业场景的常见需求。后续如果支持用户选择模型，应有白名单机制。

## 9. 沙箱安全

### 9.1 沙箱模式

| 模式 | 行为 |
|------|------|
| `off` | 无沙箱，所有工具在主机执行 |
| `non-main` | 仅非主会话沙箱化 |
| `all` | 所有会话沙箱化 |

### 9.2 沙箱范围

| 范围 | 行为 |
|------|------|
| `agent`（默认） | 每个 Agent 一个容器 |
| `session` | 每个会话一个容器 |
| `shared` | 所有沙箱化会话共享一个容器 |

### 9.3 沙箱后端

| 后端 | 说明 |
|------|------|
| Docker（默认） | 本地容器隔离，支持浏览器沙箱 |
| SSH | 远程机器执行，不支持浏览器沙箱 |
| OpenShell | 托管远程环境，支持 mirror/remote 工作空间模式 |

### 9.4 工作空间访问控制

| 访问级别 | 行为 |
|---------|------|
| `none`（默认） | 工具看到沙箱工作空间 |
| `ro` | 挂载 Agent 工作空间为只读，禁用 write/edit/apply_patch |
| `rw` | 挂载 Agent 工作空间为读写 |

### 9.5 Docker 安全模型

- 默认无网络（`network: "none"`）
- `network: "host"` 被阻止
- `network: "container:<id>"` 默认阻止
- 绑定挂载验证：阻止 `docker.sock`、`/etc`、`/proc`、`/sys`、`/dev`、凭证目录
- 符号链接逃逸防护：解析到真实路径后重新检查

### 9.6 分层安全模型

```
1. 工具策略（允许/拒绝）→ 第一道门
2. 沙箱隔离 → 控制爆炸半径
3. 工作空间访问控制 → 控制可见性
4. 绑定挂载验证 → 防止敏感主机路径暴露
5. 网络隔离 → 防止未授权网络访问
6. 提权执行（Elevated Exec）→ 显式逃逸口
```

### 9.7 对当前项目的启示

1. **V1 不涉及工具执行**，但后续 Agent 平台引入工具能力时，沙箱是基础安全要求。OpenClaw 的分层安全模型是成熟实践，值得直接参考。

2. **`non-main` 模式**：主会话在主机运行（方便开发），非主会话沙箱化（安全隔离）。这个折中策略在单用户场景中很实用——用户自己信任的操作在主机执行，来自外部的操作隔离。

3. **绑定挂载验证**：符号链接逃逸防护是一个常被忽略但重要的安全层。后续实现容器化工具执行时，必须对挂载源路径做规范化 + 祖先解析 + 阻止路径检查。

4. **提权执行（Elevated Exec）**：显式的逃逸口机制——需要主机级操作时，通过 `tools.elevated` 逃出沙箱，而非全局关闭沙箱。后续实现时应提供类似的安全阀。

## 10. 多 Agent 路由

### 10.1 架构

OpenClaw 支持将入站频道/账号/对端路由到隔离的 Agent：

```
WhatsApp Alice → Agent "personal"
WhatsApp Bob   → Agent "work"
Discord #dev   → Agent "coder"
Telegram group → Agent "writer"
```

每个 Agent 有独立的工作空间和每 Agent 会话。非主会话默认在沙箱中运行。

### 10.2 Agent 配置

```json5
{
  agents: {
    defaults: {
      workspace: "~/.openclaw/workspace",
      sandbox: { mode: "non-main" },
      skills: ["github", "weather"],
    },
    list: [
      { id: "writer", skills: ["docs-search"] },
      { id: "coder", skills: ["github", "deploy"] },
      { id: "locked-down", skills: [] },
    ],
  },
}
```

### 10.3 对当前项目的启示

当前项目 V1 是单用户 Web 对话，不涉及多 Agent。但后续如果要支持不同角色/场景的 Agent（如"代码助手"、"文档助手"、"数据分析"），OpenClaw 的 Agent 路由 + 技能白名单 + 独立工作空间模式是成熟的参考架构。

## 11. 与 Hermes Agent 的架构分野

OpenClaw 和 Hermes 同源但走向不同方向：

| 维度 | OpenClaw | Hermes Agent |
|------|---------|-------------|
| 语言栈 | TypeScript (Node.js) | Python |
| 核心定位 | 本地优先的个人助手 | 闭环学习的自主 Agent |
| 差异化能力 | 22+ 消息平台 + 原生 App + Canvas | 闭环学习 + 技能自创建 + FTS5 搜索 |
| 记忆架构 | MEMORY.md + 每日笔记 + 梦境系统 | MEMORY.md + USER.md + FTS5 + Honcho |
| 模型管理 | Failover 链 + 多表面 + 白名单 | 多 Provider + 运行时切换 |
| 安全模型 | 分层沙箱 + 绑定验证 + 提权执行 | 命令审批 + 注入扫描 |
| 技能系统 | 门控 + 白名单 + ClawHub + Workshop | 渐进式披露 + 条件激活 + agentskills.io |
| 压缩 | 可插拔 Provider + 转录轮换 + 修剪 | Preflight 检查 + 保留最后 N 轮 |
| 社区规模 | 372k stars | 148k stars |

**核心分野**：OpenClaw 侧重"个人助手的平台完整性"（多平台、多 Agent、沙箱、App），Hermes 侧重"Agent 的自我进化能力"（闭环学习、技能自创建、自改进）。两者互补而非竞争。

## 12. 综合判断

### 12.1 V1 阶段即可引入的原则

1. **幂等副作用设计**：后续如果支持消息重试，API 层应引入幂等键 + 服务端去重。当前排队机制是前端侧保证，服务端也应有兜底。

2. **会话生命周期管理**：长期不活跃的会话在下次打开时应考虑上下文刷新策略，而非直接继续旧上下文。

3. **工具调用配对保留**：后续实现上下文压缩时，工具调用和结果必须配对保留，不可拆分。

4. **压缩前记忆刷新**：上下文压缩前必须先执行记忆持久化，防止信息丢失。

5. **辅助任务用小模型**：摘要、记忆刷新等辅助任务应支持配置独立的小模型，避免浪费主模型 token。

### 12.2 后续版本的核心参考

6. **Gateway 模式**：如果后续需要支持多入口（消息网关、CLI、App），可以将 Agent 核心逻辑抽离为独立服务，各入口作为接入层。当前 Maven 模块结构（agent-api/agent-core/agent-web）已为此打下基础。

7. **模型 Failover 链**：有序 fallback + Provider 内 auth 轮换 + 用户选择严格模式 vs 自动 Failover 静默模式的行为差异。

8. **分层沙箱安全**：工具策略 → 沙箱隔离 → 工作空间访问控制 → 绑定验证 → 网络隔离 → 提权执行。六层安全模型。

9. **技能门控与白名单**：运行时环境检测（bins/env/config/os）自动决定技能可见性，不同 Agent/场景配置不同技能集合。

10. **可插拔压缩 Provider**：默认 LLM 摘要 + 允许自定义 Provider 替换，失败自动回退。

11. **转录轮换**：压缩时不就地重写，创建新转录文件，原始数据不丢失。`summary_message_id` 字段已预留此语义。

12. **多模型表面**：不同任务（主对话/图像/摘要/生成）使用不同模型，各自独立配置 primary + fallbacks。

### 12.3 不适合引入的方向

- 22+ 消息平台 Gateway（V1 明确只做 Web）
- 本地守护进程 + launchd/systemd 部署模式（与 Spring Boot 模型不匹配）
- Canvas / A2UI 可视化工作空间（Agent 高级能力，V1 不涉及）
- 梦境系统 Dreaming（后台整合机制，V1 阶段过早）
- Node.js/TypeScript 技术栈（与 Java 技术栈不匹配）
- Voice Wake / Talk Mode 语音交互（V1 明确不做）

## 13. 参考来源

- OpenClaw GitHub：https://github.com/openclaw/openclaw
- OpenClaw 架构文档：https://docs.openclaw.ai/concepts/architecture
- OpenClaw Agent 运行时：https://docs.openclaw.ai/concepts/agent
- OpenClaw 会话模型：https://docs.openclaw.ai/concepts/session
- OpenClaw 记忆系统：https://docs.openclaw.ai/concepts/memory
- OpenClaw 技能系统：https://docs.openclaw.ai/tools/skills
- OpenClaw 上下文压缩：https://docs.openclaw.ai/concepts/compaction
- OpenClaw 模型管理：https://docs.openclaw.ai/concepts/models
- OpenClaw 沙箱安全：https://docs.openclaw.ai/gateway/sandboxing
- ClawHub 技能注册中心：https://clawhub.ai
