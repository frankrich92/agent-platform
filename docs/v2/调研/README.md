# v2 调研文档索引

本目录保存面向当前 `agent-platform` 迁移主线的调研与吸收结论。

综合结论：

- `../02-调研内容采纳清单.md`：对本目录全部调研内容的综合取舍，按直接采纳、改写采纳、预留采纳、暂不采纳形成清单。

当前已有调研文档：

- `历史归档文档可吸收内容分析.md`：对 `docs/历史归档文档/` 下自研阶段过程文档的归纳、取舍和当前项目落点建议。
- `Agent平台定位分析.md`：分析通用底座、专家智能体 Profile、Runtime、Workflow、MCP、Worker/Sandbox 的平台定位。
- `gpt-v2.md`：从 Run、Runtime SPI、CapabilityPlan、Skill、Memory、MCP、安全边界等方面提出优化建议。
- `claude4.7-v2.md`：对开源 Agent 实现做横向对比，重点分析 Skill、Run/Span、MCP、Hook、SubAgent、Sandbox、观测等方向。
- `claude-code-architecture-research.md`：对 Claude Code 的记忆、技能、工具、上下文压缩、MCP、会话和子 Agent 模式做深度研究。
- `dpsk-v4.md`：围绕 Skills、Session、Memory、Tools、MCP、多 Agent、Hook 等能力给出横向对比和优先级建议。
- `glm5.1-v2.md`：分析 Skills、Session、Memory、MCP、SubAgent、权限、压缩和回压等优化点。
- `kimi-k2.6-v2.md`：分析开源 Agent 架构，并给出 Skill 元数据、会话、记忆、工具治理和演进建议。
- `gemini3.1-falsh-v2.md`：补充 Skill 自演进、上下文压缩、知识图谱记忆和确定性 Hook 的建议。

使用原则：

- 归档文档只作为历史需求、技术方案、评审意见和过程经验来源，不直接成为当前实现权威。
- 当前工程约束以仓库根 `RTK.md`、`docs/v1/02-模块迁移方案.md`、`docs/v1/03-Apboa源码迁移映射关系.md` 和 `docs/v1/03-模块边界收敛记录.md` 为准。
- 涉及 Apboa 行为迁移时，仍然保持 `.apboa/` 只读，并在 `source/agent-platform/`、`source/agent-platform-ui/` 或 `docs/` 下落地。
