# Hermes Agent 设计调研

> 调研版本：v1
> 调研模型：GLM-5.1
> 调研日期：2026-05-14
> 调研对象：NousResearch Hermes Agent (v0.13.0, "The Tenacity Release", 2026-05-07)
> 对照范围：通用大模型对话 01/02/03/04 文档
> 与同期调研的关系：本调研在 GPT-5.5 版和 DeepSeek-v4-pro 版基础上，侧重于 Hermes v0.13.0 新增能力、记忆系统内部实现细节、技能系统的工程实践，以及面向 Java/Spring Boot 技术栈的差异化落地建议。

## 1. 背景与定位

Hermes Agent 是 NousResearch 开源的自主 Agent runtime，核心定位为"the agent that grows with you"——一个通过闭环学习持续自我改进的长期运行 Agent。当前版本 v0.13.0，GitHub 148k stars，Python 为主（87.9%），TypeScript 辅助（8.9%）。

与当前项目的核心差异：

| 维度 | Hermes Agent | 当前项目 V1 |
|------|-------------|------------|
| 语言栈 | Python + TypeScript | Java (JDK 21) + Spring Boot 4.0.5 |
| 产品形态 | 长期运行自主 Agent（CLI + 多平台 Gateway） | Web 端通用大模型对话 |
| 核心能力 | 闭环学习、技能自创建、多 Provider、多平台 | 流式对话、thinking 展示、会话管理 |
| 记忆架构 | 三层记忆 + FTS5 + 外部 Provider | 无持久记忆（仅会话内消息历史） |
| Agent 能力 | 70+ 工具、子代理委派、Cron 调度 | 无（纯对话） |
| 部署模式 | $5 VPS 到 Serverless（Modal/Daytona） | Spring Boot 单实例 |

两者技术栈和产品阶段差异很大，但 Hermes 的架构设计原则、闭环学习范式、记忆系统实现和技能管理策略对当前项目的后续演进有系统性参考价值。

## 2. 闭环学习范式——Hermes 的核心差异化

### 2.1 闭环学习全景

Hermes 闭环学习的核心是让 Agent 从使用经验中持续积累和改进，形成"使用 → 学习 → 改进"的正向循环：

```
用户交互 → Agent 执行任务 → 完成复杂任务后自动创建技能（Procedural Memory）
                                      ↓
                        使用技能时遇到问题 → 自动修补技能（Self-improvement）
                                      ↓
                        交互中获得关键信息 → 自主持久化到 MEMORY.md（Episodic Memory）
                                      ↓
                        周期性自我提示 → "是否有什么值得记住的？"（Periodic Nudge）
                                      ↓
                        历史会话可通过 FTS5 检索 → 跨会话上下文回溯（Session Search）
```

### 2.2 各组件的实现机制

**自主记忆持久化**：

- Agent 通过 `memory` 工具管理自己的记忆，提供 `add`/`replace`/`remove` 三种操作
- 替换和删除使用短子串匹配（不需要完整文本），降低 token 消耗
- 记忆写入磁盘即时生效，但当前会话的 system prompt 中的快照不变——新内容在下一会话才生效
- 这种"冻结快照"设计保护了 LLM 的 prefix cache，避免每次对话支付全量输入费用

**周期性自我提示（Periodic Nudge）**：

- Agent 在对话过程中周期性地自我提示"是否有什么值得记住的？"
- 这是一个独特的架构决策：不是被动等待用户要求记忆，而是主动触发记忆编码
- 避免了关键信息在长对话中丢失

**技能自动创建**：

- 触发条件：完成 5+ 工具调用的复杂任务、遇到错误后找到正确路径、被用户纠正方法、发现非平凡工作流
- 通过 `skill_manage` 工具提供 `create`/`patch`/`edit`/`delete`/`write_file`/`remove_file` 六种操作
- `patch` 操作优先于 `edit`——只传递变更部分，更 token 高效
- 技能以 Markdown 文件存储，遵循 agentskills.io 开放标准

**技能自改进**：

- 技能在使用过程中会被自动修补（patch），而非静态文档
- 每次使用技能后，如果发现了新的陷阱或更优步骤，Agent 会自动更新技能文档

### 2.3 对当前项目的启示

闭环学习是 Agent 平台的高级能力，V1 不需要也不应实现。但其架构思路对后续演进有指导意义：

1. **记忆持久化的触发机制**：Hermes 的"周期性自我提示"是一个优雅的设计——不需要用户显式要求"记住这个"，Agent 自己决定什么值得记忆。后续实现用户偏好、项目上下文等持久化能力时，可以考虑由对话模型主动建议持久化，而非全部由用户手动触发。

2. **冻结快照 + 延迟生效**：这个模式的核心价值在于保护 prompt caching 收益。后续如果对接 Anthropic 模型或任何支持 prompt caching 的 provider，此约束至关重要。应在 V1 的技术方案中作为显式设计原则写入。

3. **渐进式披露的技能加载**：Hermes 技能系统采用三级加载（元数据 → 正文 → 引用文件），避免一次性注入所有技能内容。后续如果实现工具/能力模板，应采用同样策略。

## 3. 记忆系统内部实现

### 3.1 双文件存储模型

Hermes 的持久记忆存储在 `~/.hermes/memories/` 下的两个文件中：

| 文件 | 用途 | 字符上限 | 约 token 数 | 典型条目数 |
|------|------|---------|------------|-----------|
| MEMORY.md | Agent 个人笔记——环境配置、项目约定、教训 | 2,200 chars | ~800 tokens | 8–15 条 |
| USER.md | 用户画像——身份、偏好、沟通风格 | 1,375 chars | ~500 tokens | 5–10 条 |

两个文件在会话启动时加载到 system prompt 中作为冻结快照，会话内不更新。容量信息以百分比形式显示（如 `[67% — 1,474/2,200 chars]`），让 Agent 感知剩余空间。条目间用 `§` 分隔符分隔。

### 3.2 记忆管理工具

| 操作 | 参数 | 说明 |
|------|------|------|
| `add` | `content` | 添加新条目，自动拒绝精确重复 |
| `replace` | `old_text` + `content` | 短子串匹配替换，匹配多个时报错要求更具体 |
| `remove` | `old_text` | 短子串匹配删除 |

没有 `read` 操作——记忆内容已在 system prompt 中，Agent 始终可见。

### 3.3 容量管理与合并策略

当添加新条目超出字符上限时，工具返回结构化错误，包含当前条目列表和用量。Agent 的处理流程：

1. 阅读当前条目（错误响应中提供）
2. 识别可删除或可合并的条目
3. 使用 `replace` 将相关条目合并为更紧凑的版本
4. 再 `add` 新条目

**最佳实践**：当记忆使用超过 80% 时，主动合并后再添加。

### 3.4 安全扫描

所有记忆条目在写入前经过安全扫描，检测：
- Prompt 注入模式
- 凭证外泄模式
- SSH 后门模式
- 隐形 Unicode 字符

这是必要的防护——因为记忆条目会被注入到 system prompt 中，如果被污染，可能被利用。

### 3.5 FTS5 会话检索

| 特性 | 说明 |
|------|------|
| 存储位置 | `~/.hermes/state.db`（SQLite） |
| 检索引擎 | FTS5 全文搜索 |
| 摘要处理 | 搜索结果经 Gemini Flash 摘要后注入上下文 |
| 用途 | "上周我们讨论了什么？"类跨会话回溯 |

持久记忆 vs 会话检索的定位差异：

| 维度 | 持久记忆 (MEMORY.md/USER.md) | 会话检索 (FTS5) |
|------|---------------------------|----------------|
| 容量 | ~1,300 tokens 总量 | 无限制（所有会话） |
| 速度 | 即时（在 system prompt 中） | 需搜索 + LLM 摘要 |
| 用途 | 关键事实，始终可用 | 查找特定历史对话 |
| 管理 | Agent 手动策划 | 自动（所有会话存储） |
| Token 成本 | 每会话固定 ~1,300 tokens | 按需（搜索时消耗） |

### 3.6 外部记忆 Provider

Hermes 支持 8 个外部记忆 Provider 插件：Honcho、OpenViking、Mem0、Hindsight、Holographic、RetainDB、ByteRover、Supermemory。关键约束：**至多同时启用一个外部 Provider**，防止工具 schema 膨胀导致模型混淆。

### 3.7 对当前项目的启示

V1 不实现持久记忆，但以下设计决策值得记录到后续演进路径：

1. **双文件模型 → 双表模型**：在 PostgreSQL 中，`user_context` 表（环境配置、项目约定）和 `user_profile` 表（用户偏好、沟通风格）对应 Hermes 的 MEMORY.md 和 USER.md。V1 可只预留表结构。

2. **容量感知**：Hermes 在 system prompt 中显示记忆用量百分比。后续实现时，可以在上下文组装时附加容量提示，让模型自行决定是否合并/精简。

3. **安全扫描**：后续任何注入 system prompt 的用户可控内容（包括记忆、上下文文件、技能文档）都需要经过注入扫描。这是一个常被忽略但至关重要的安全层。

4. **PostgreSQL FTS 替代 SQLite FTS5**：当前项目已有 PostgreSQL，其内置 `tsvector` + `pg_trgm` 可覆盖中英文关键词检索和模糊匹配，不需要引入额外搜索引擎。后续语义检索可增加 `message_embedding` 表。

5. **至多一个外部记忆 Provider**：后续如果引入多种检索后端，应在配置层面控制并发激活数量，避免 prompt 中充斥大量相似功能的工具描述。

## 4. 技能系统的工程实践

### 4.1 技能存储结构

```
~/.hermes/skills/                  # 单一数据源（Single source of truth）
├── mlops/                         # 分类目录
│   ├── axolotl/
│   │   ├── SKILL.md               # 主指令文件（必须）
│   │   ├── references/            # 参考文档
│   │   ├── templates/             # 输出格式模板
│   │   ├── scripts/               # 可调用辅助脚本
│   │   └── assets/                # 附属文件
│   └── vllm/
│       └── SKILL.md
├── devops/
│   └── deploy-k8s/                # Agent 创建的技能
│       ├── SKILL.md
│       └── references/
└── .hub/                          # Skills Hub 状态
    ├── lock.json
    ├── quarantine/
    └── audit.log
```

### 4.2 SKILL.md 格式规范

```markdown
---
name: my-skill
description: Brief description
version: 1.0.0
platforms: [macos, linux]           # 可选——限制到特定 OS
metadata:
  hermes:
    tags: [python, automation]
    category: devops
    fallback_for_toolsets: [web]    # 可选——条件激活
    requires_toolsets: [terminal]   # 可选——条件激活
    config:                          # 可选——非秘密配置
      - key: my.setting
        description: "What this controls"
        default: "value"
        prompt: "Prompt for setup"
---

# Skill Title

## When to Use
触发条件

## Procedure
1. 步骤一
2. 步骤二

## Pitfalls
已知失败模式和修复方法

## Verification
如何确认成功
```

### 4.3 渐进式披露（Token 高效加载）

| 级别 | 函数 | 返回内容 | 大约 Token 成本 |
|------|------|---------|---------------|
| 0 | `skills_list()` | 名称 + 描述 + 分类 | ~3k tokens |
| 1 | `skill_view(name)` | 完整内容 + 元数据 | 不定 |
| 2 | `skill_view(name, path)` | 特定引用文件 | 不定 |

Agent 只在实际需要时加载完整技能内容。这是一个关键的工程决策——一次性注入所有技能文档会导致 prompt 膨胀，而按需加载可以在 70+ 技能的情况下保持系统 prompt 可控。

### 4.4 条件激活机制

技能可以根据当前 session 的工具可用性自动显示/隐藏：

| 字段 | 行为 |
|------|------|
| `fallback_for_toolsets` | 当列出的 toolset 可用时隐藏；缺失时显示 |
| `requires_toolsets` | 当列出的 toolset 不可用时隐藏；存在时显示 |

示例：DuckDuckGo 搜索技能设置 `fallback_for_toolsets: [web]`。当 Firecrawl API key 配置时，web toolset 可用，使用 `web_search`；当 API key 缺失时，DuckDuckGo 自动出现作为 fallback。

### 4.5 技能 Hub 生态

Hermes 的技能共享生态包括：

| 来源 | 示例 | 信任级别 |
|------|------|---------|
| `official` | `official/security/1password` | 内置信任 |
| `skills-sh` | Vercel 公共技能目录 | 社区 |
| `well-known` | URL 发现（`/.well-known/skills/index.json`） | 社区 |
| `github` | `openai/skills/k8s` | 可变 |
| 直接 URL | 任意 `SKILL.md` 链接 | 社区 |

所有 Hub 安装的技能经过安全扫描，检查：数据外泄、prompt 注入、破坏性命令、供应链信号。`--force` 可覆盖警告级发现，但不能覆盖 `dangerous` 级别。

### 4.6 对当前项目的启示

1. **技能 = Procedural Memory**：Hermes 的技能系统本质上是"可复用的操作步骤"。在 Agent 平台中，这类能力可以理解为"Agent 的工具使用知识"。当前项目不涉及，但后续 Agent 编排中应参考渐进式披露策略。

2. **条件激活**：根据运行时环境自动调整可用能力是一个优雅的设计。后续如果支持多种工具运行时（如部分用户有 Docker、部分用户只有本地环境），技能/工具的条件激活可以保证体验一致性。

3. **SKILL.md 格式**：如果后续实现能力模板，Markdown + YAML frontmatter 是一个轻量且可扩展的格式选择，优于自定义 JSON schema。

4. **安全扫描**：技能内容注入 prompt 前的安全扫描与记忆系统一致——所有用户可控的 prompt 内容都需要防护。

## 5. 工具系统与执行后端

### 5.1 工具与工具集架构

Hermes 提供 70+ 内置工具，组织为 25+ 个工具集：

| 工具集 | 示例工具 | 说明 |
|--------|---------|------|
| `web` | `web_search`, `web_extract` | 搜索和内容提取 |
| `terminal` | `terminal`, `process` | 命令执行和进程管理 |
| `browser` | `browser_navigate`, `browser_snapshot` | 浏览器自动化 |
| `vision` | `vision_analyze` | 图像分析 |
| `image_gen` | `image_generate` | 图像生成 |
| `skills` | `skills_list`, `skill_view`, `skill_manage` | 技能管理 |
| `memory` | `memory` | 持久记忆管理 |
| `cronjob` | `cronjob` | 定时任务 |
| `delegation` | `delegate_task` | 子代理委派 |
| `code_execution` | `execute_code` | 程序化工具调用 |
| `mcp-*` | 动态 | MCP 服务器工具 |

### 5.2 七种终端执行后端

| 后端 | 类型 | 关键特性 |
|------|------|---------|
| Local | 本地执行 | 默认，零配置 |
| Docker | 容器化 | 单一持久容器，所有调用通过 `docker exec` 路由 |
| SSH | 远程执行 | 推荐用于安全隔离 |
| Singularity | HPC 容器 | 科研计算场景 |
| Modal | Serverless | 空闲时休眠，近零成本 |
| Daytona | Serverless | 开发环境即服务 |
| Vercel Sandbox | Serverless | 快照文件系统持久化 |

配置示例：

```yaml
terminal:
  backend: docker
  docker_image: python:3.11-slim
  timeout: 180
  container_cpu: 1
  container_memory: 5120   # MB
  container_disk: 51200     # MB
  container_persistent: true
```

### 5.3 进程管理工具

```python
terminal(command="pytest -v tests/", background=true)
# 返回: {"session_id": "proc_abc123", "pid": 12345}

process(action="list")                                    # 列出运行中进程
process(action="poll", session_id="proc_abc123")          # 检查状态
process(action="wait", session_id="proc_abc123")          # 阻塞等待完成
process(action="log", session_id="proc_abc123")           # 完整输出
process(action="kill", session_id="proc_abc123")          # 终止
process(action="write", session_id="proc_abc123", data="y")  # 发送输入
```

PTY 模式支持交互式 CLI 工具。

### 5.4 容器安全模型

所有容器后端强制：
- 只读根文件系统
- 丢弃所有 Linux capabilities
- 禁止特权升级
- PID 限制（256 进程）
- 完整命名空间隔离
- 通过卷（非可写根层）实现持久化

### 5.5 对当前项目的启示

1. **V1 不涉及工具执行**，但后续 Agent 平台引入工具能力时，Docker 隔离是基础安全要求。

2. **进程管理模型**：`background=true` + `process` 工具的异步执行模式值得参考。后续如果支持长时间运行的任务（如数据分析、代码执行），需要类似的异步任务管理能力。

3. **工具集分组**：按功能域组织工具为工具集，可按平台/场景配置激活，是一个清晰的模块化策略。

4. **MCP 集成**：Hermes 支持 MCP（Model Context Protocol）服务器作为工具来源，动态生成 `mcp-<server>` 工具集。后续如果要扩展工具生态，MCP 是一个值得关注的开放协议。

## 6. 多平台 Gateway 与统一核心

### 6.1 架构模式

Hermes 的核心设计原则之一是"平台无关核心"——一个 `AIAgent` 类服务所有入口（CLI、Gateway、ACP、Cron、Batch），平台差异通过回调注入，不在核心逻辑中条件分支。

```
┌─────────┐  ┌──────────┐  ┌───────┐  ┌───────┐  ┌───────┐
│  CLI    │  │ Telegram │  │Discord│  │ Slack │  │ Email │
└────┬────┘  └────┬─────┘  └───┬───┘  └───┬───┘  └───┬───┘
     │            │            │          │          │
     └────────────┴────────────┴──────────┴──────────┘
                         │
                    ┌────▼────┐
                    │ Gateway │  单一进程，多平台桥接
                    └────┬────┘
                         │
                    ┌────▼────┐
                    │ AIAgent │  平台无关核心
                    └─────────┘
```

### 6.2 消息网关特性

- 语音备忘录转录
- 跨平台对话连续性（在 Telegram 开始的对话可在 Slack 继续）
- 平台安全：DM 配对验证身份、允许用户列表
- 每个平台独立配置工具集

### 6.3 对当前项目的启示

当前 V1 只有 Web 入口，Maven 多模块设计（`agent-api` / `agent-core` / `agent-web`）已体现入口与核心分离。建议在详细设计中明确：

- `agent-core` 不引入 `spring-boot-starter-webflux` 依赖
- 领域服务通过接口接收上下文，由 `agent-web` 的 Filter/Controller 负责从请求中提取并传入
- 后续新增入口（如消息网关、CLI）时，只需新增模块依赖 `agent-core`，核心逻辑不变

## 7. 上下文压缩的工程细节

### 7.1 Hermes 的压缩算法

当对话历史接近模型 context limit 的 50% 时，`ContextCompressor` 自动触发：

1. 先 flush memory 到磁盘（防止数据丢失）
2. 对中间轮次的对话进行摘要（lossy summarization）
3. 保留最后 N 条消息完整（`compression.protect_last_n`，默认 20）
4. 生成 tool_call_id 标记压缩边界

**关键点**：压缩发生在请求发送之前（preflight 检查），而非响应返回之后。这避免了在 token 已经超标后才处理的情况。

### 7.2 对我们项目 `summary_message_id` 字段的语义建议

`summary_message_id` 指向一条"摘要消息"——该消息之前的历史消息在上下文组装时已被摘要替代。当前会话的上下文 = `summary_message_id` 对应的摘要文本 + 后续完整消息。

建议在 `04-详细设计` 的消息表字段说明中补充此语义，即使 V1 不做压缩，也要让字段用途明确，避免后续使用偏差。

### 7.3 压缩时机的关键教训

> "Memory that quietly defeats prompt caching is a common and expensive mistake." — Hermes 生产实践

压缩必须发生在 prompt assembly 阶段（请求前），而非响应后。原因：

- 响应后压缩无法挽回已消耗的 token
- Preflight 检查可以在发送请求前就降低 token 消耗
- Anthropic prompt caching 按前缀匹配，前缀一旦变化整个缓存失效

## 8. 子代理委派模式

### 8.1 Hermes 的子代理约束

| 约束 | 值 | 原因 |
|------|-----|------|
| 最大委派深度 | 2 | 防止递归爆炸 |
| 子代理工具白名单 | 不能递归 delegate、不能 clarify、不能 memory、不能 send_message、不能 execute_code | 防止子代理越权 |
| 子代理迭代预算 | 独立，默认 50 轮 | 子代理循环不计入父代理总预算 |
| 父代理阻塞 | 阻塞等待子代理返回摘要 | 保证结果确定性 |

### 8.2 对当前项目的启示

V1 不涉及子代理。但后续平台 Agent 编排中，委派是核心能力。Hermes 的深度限制和工具白名单策略值得直接参考——在 Agent 编排模块中，子任务必须受限执行，不能获取完整代理权限。

具体建议：后续设计 Agent 编排时，子代理的权限范围应通过白名单严格控制，默认不继承父代理的所有能力。

## 9. 安全设计

### 9.1 双层 Prompt 注入扫描

Hermes 对所有注入 system prompt 的用户可控内容进行扫描：

- **Context files**（AGENTS.md、SOUL.md 等）——用户可能编辑的 markdown 文件
- **Memory writes**——Agent 写入 memory 的内容可能被污染

扫描内容包括：注入模式、隐藏 Unicode、数据外泄模式、SSH 后门模式。

### 9.2 命令审批与容器隔离

- 危险命令（rm -rf、DROP TABLE 等）检测和审批
- 工具执行在隔离的终端后端中运行
- 命令白名单模式

### 9.3 对当前项目的启示

V1 的鉴权极简（仅校验 token 非空），对话内容无注入风险传导。但后续引入 Agent 工具执行时，安全是前置条件：

1. 所有可能进入 system prompt 或触发工具调用的用户输入需经过注入扫描
2. 工具执行需要沙箱隔离
3. 危险操作需要审批机制

建议在技术方案中记录此安全设计点，即使 V1 不实现。

## 10. Agent Loop 执行模型

### 10.1 核心流程

```
run_conversation() {
  1. 生成 task_id
  2. 追加用户消息到对话历史
  3. 组装/复用系统 prompt（prompt_builder.py）
  4. 检查是否需要预压缩（>50% context）
  5. 构建 API 消息（统一转换为 OpenAI 格式）
  6. 注入临时 prompt 层（预算警告、上下文压力）
  7. 应用 prompt 缓存标记（Anthropic only）
  8. 可中断 API 调用（_interruptible_api_call）
  9. 解析响应：
     - 有 tool_calls → 执行 → append 结果 → 回到步骤 5
     - 纯文本响应 → 持久化 session → flush memory → 返回
}
```

### 10.2 关键设计决策

| 决策 | 说明 | 对当前项目的启示 |
|------|------|---------------|
| 迭代预算 | 父代理默认 90 轮，子代理 50 轮 | 后续引入工具循环时必须设置最大迭代上限 |
| 可中断调用 | 中断信号传播到 API 层 | 当前 AbortController + doOnCancel 已覆盖 |
| 上下文预压缩 | 请求前检查，>50% 时压缩 | 压缩时机必须是 preflight 而非 post-response |
| Fallback 模型 | 主模型失败 → fallback 模型链 | 后续多模型场景参考 |
| 冻结 Prompt | 会话内不变，修改下会话生效 | 显式设计原则，写入技术方案 |

## 11. 与同期调研的差异化分析

### 11.1 本调研新增内容

| 维度 | GPT-5.5 版 | DeepSeek-v4-pro 版 | 本版（GLM-5.1）新增 |
|------|-----------|-------------------|-------------------|
| 版本覆盖 | 未注明 | v0.10+ | **v0.13.0 最新版** |
| 闭环学习 | 未涉及 | 简述 | **详细分析闭环学习范式 + 触发机制** |
| 记忆系统 | 概述 FTS5 | 三层架构 | **双文件存储模型 + 容量管理 + 安全扫描 + 容量感知** |
| 技能系统 | 未涉及 | 简述 | **SKILL.md 格式规范 + 渐进式披露 + 条件激活 + Hub 生态** |
| 工具系统 | 简述注册模型 | 未涉及 | **25+ 工具集 + 七种执行后端 + 进程管理 + 容器安全** |
| 多平台 Gateway | 未涉及 | 未涉及 | **统一核心架构 + 跨平台连续性** |
| Agent Loop | 未涉及 | 核心流程分析 | **补充迭代预算和 fallback 等关键决策** |
| 安全设计 | 未涉及 | 双层扫描 + 审批 | **与记忆/技能安全扫描对齐分析** |

### 11.2 三版调研共识

所有版本一致认为：

1. **V1 不应引入**：闭环学习、技能自创建、多平台 Gateway、Cron 调度、子代理委派
2. **V1 应预留**：Provider 抽象接口、上下文压缩字段语义、可中断执行
3. **后续演进方向**：分层记忆、工具注册表、跨会话检索、安全扫描

## 12. 综合判断

### 12.1 V1 阶段即可引入的原则

1. **Prompt 稳定性约束**：会话启动后系统级 prompt 不修改，变更新会话生效。写入技术方案作为显式设计原则。
2. **Core 层与 Web 层解耦**：`agent-core` 不依赖 Spring WebFlux，由 `agent-web` 负责 HTTP 协议适配。在详细设计中明确依赖方向。
3. **消息格式归一化**：内部统一使用标准格式，外部适配器完成不同 provider 的格式转换。
4. **可中断执行设计**：当前 AbortController + doOnCancel 已覆盖，后续扩展到显式"停止生成"按钮。

### 12.2 后续版本的核心参考

5. **分层记忆架构**：L1 用户上下文表（PostgreSQL，对应 MEMORY.md）→ L2 能力模板（Markdown，对应 Skills）→ L3 消息全文检索（PostgreSQL FTS + pg_trgm，对应 Session Search）
6. **上下文压缩策略**：preflight 检查 + 保留最后 N 轮 + 中间摘要替代。字段已预留，实现路径清晰。
7. **Provider 抽象接口**：`LlmProviderAdapter` 接口 + V1 单实现 → 后续多实现零改动。
8. **子代理委派约束**：深度限制 + 工具白名单 + 独立迭代预算 → Agent 编排的核心安全模式。
9. **渐进式披露加载**：技能/能力模板按需加载，避免 prompt 膨胀。
10. **安全扫描层**：所有注入 prompt 的用户可控内容需经过注入扫描，包括记忆、上下文文件、技能文档。
11. **容量感知与自动合并**：记忆/上下文容量有限时，模型自行决定合并/精简，而非硬性截断。

### 12.3 不适合引入的方向

- 自动技能创建与自改进（审核/安全/稳定性风险）
- 多平台 Gateway（V1 明确只做 Web）
- Cron 调度、批量轨迹生成、RL 训练环境（MLOps 专用能力）
- Python 单体架构（与技术栈不匹配）
- 文件级 Checkpoint/Rollback（纯对话系统不需要）
- 8 个外部记忆 Provider 并存（工具 schema 膨胀导致模型混淆）

## 13. 参考来源

- Hermes Agent GitHub：https://github.com/NousResearch/hermes-agent
- Hermes 官网：https://hermes-agent.nousresearch.com/
- Hermes 记忆系统文档：https://hermes-agent.nousresearch.com/docs/user-guide/features/memory
- Hermes 技能系统文档：https://hermes-agent.nousresearch.com/docs/user-guide/features/skills
- Hermes 工具系统文档：https://hermes-agent.nousresearch.com/docs/user-guide/features/tools
- agentskills.io 开放标准：https://agentskills.io/specification
- 同期调研 GPT-5.5 版：`docs/v1-通用大模型对话/调研/HermesAgent-v1-GPT5.5.md`
- 同期调研 DeepSeek-v4-pro 版：`docs/v1-通用大模型对话/调研/HermesAgent-v1-dpsk-v4-pro.md`
