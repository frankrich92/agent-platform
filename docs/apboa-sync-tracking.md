# Apboa 同步追踪记录

本文记录 `.apboa/` 只读上游源码同步到新工程的进度，便于后续继续迁移和核对。

## 当前同步点

- 上游目录：`.apboa/`
- 已核对起点：`a43c392958a115365d1dde9c6ac19f0404b54d51`
- 已同步到：`12ff55fc045bc3ce97ea785c1218604228b322e8`
- 同步时间：2026-05-22
- 最新提交说明：`chore: configure local deployment`

## 本轮核对的上游提交

```text
8dc214d fix(skill): 修复 ZIP 导入路径/编码/识别并完善标签与反馈
06d52f9 feat(security): 新增多语言脚本安全检查模块和配置优化
d9aae05 !131 fix(db): improve comments and fix messy code in db_init.sql
c00bd82 !133 feat(security): 新增多语言脚本安全检查模块和配置优化
f35128c !132 fix(skill): 修复 ZIP 导入路径/编码/识别并完善标签与反馈
ef0fa9c docs(readme): 更新交流群信息及图片
753d88c !134 docs(readme): 更新交流群信息及图片
12ff55f chore: configure local deployment
```

## 已同步内容

| 上游变更 | 新工程落点 | 状态 |
| --- | --- | --- |
| 技能导入增强：ZIP 安全解压、上传目录识别、`SKILL.md` 规范化、导入结果提示 | `source/agent-platform/agent-admin/admin-capability`、`source/agent-platform/agent-infra/infra-common`、`source/agent-platform-ui` | 已同步 |
| 多语言脚本安全检查：Python、Node.js、Shell、HTML 检查与 Workspace 校验集成 | `source/agent-platform/agent-infra/infra-security`、`source/agent-platform/agent-runtimes/runtime-agentscope` | 已同步 |
| 代码执行默认命令调整：移除 `npm`，加入 `sh` | `source/agent-platform-ui/src/components/codeExecution/CodeExecutionConfigForm.vue` | 已同步 |
| 本地部署配置：Nacos、Redis、MySQL、PgVector 改为本地配置 | `source/agent-platform/agent-boot/src/main/resources/application-dev.yml` | 已同步 |
| 数据库初始化脚本与注释乱码修复 | `docs/once_db_init/db_init.sql` | 已同步 |

## 未同步或暂不迁移内容

| 上游变更 | 处理结论 |
| --- | --- |
| README 交流群图片和展示文案 | 新工程当前无根 `README.md` 承载该内容，且不影响平台功能，暂不迁移 |
| `.apboa/` 内历史源码路径和包名 | 新工程按 `com.htam.agent` 和 `source/agent-platform/` 模块边界落地，不保留旧路径 |

## 后续同步检查命令

后续如需继续检查 `.apboa` 新增提交，以上面的“已同步到”作为起点：

```sh
git -C .apboa log --oneline --reverse 12ff55fc045bc3ce97ea785c1218604228b322e8..HEAD
git -C .apboa diff --name-status 12ff55fc045bc3ce97ea785c1218604228b322e8..HEAD
```

如果发现新增上游变更，应先按功能归类，再同步到 `.apboa/` 之外的新工程目录。`.apboa/` 始终只读，不作为变更落点。
