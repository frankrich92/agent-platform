# Apboa 同步追踪记录

本文记录 `.apboa/` 只读上游源码同步到新工程的进度，便于后续继续迁移和核对。

## 当前同步点

- 上游目录：`.apboa/`
- 已核对起点：`a43c392958a115365d1dde9c6ac19f0404b54d51`
- 已同步到：`b1afcf0a65ee48fc43b81d63c384aa63e7ac9849`
- 最近已核对到：`b1afcf0a65ee48fc43b81d63c384aa63e7ac9849`
- 最近同步时间：2026-06-01
- 最近核对时间：2026-06-01
- 已同步提交说明：`Merge remote-tracking branch 'origin/master'`
- 最近核对提交说明：`Merge remote-tracking branch 'origin/master'`

说明：`已同步到` 表示已经迁移落地到新工程的上游基线；`最近已核对到` 表示最近一次完成源码差异核对的上游位置。

## 2026-05-22 已同步的上游提交

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
| 数据库初始化脚本与注释乱码修复 | `docs/once_db_init/db_init.sql` | 已同步，并按新工程包名替换为 `com.htam.agent` |
| 应用启动数据库迁移 | `source/agent-platform/agent-boot/src/main/resources/db/migration/V1__init_schema.sql` | 已接入 Flyway |

## 未同步或暂不迁移内容

| 上游变更 | 处理结论 |
| --- | --- |
| README 交流群图片和展示文案 | 新工程当前无根 `README.md` 承载该内容，且不影响平台功能，暂不迁移 |
| `.apboa/` 内历史源码路径和包名 | 新工程按 `com.htam.agent` 和 `source/agent-platform/` 模块边界落地，不保留旧路径 |

## 2026-06-01 新增核对的上游提交

核对范围：`12ff55fc045bc3ce97ea785c1218604228b322e8..b1afcf0a65ee48fc43b81d63c384aa63e7ac9849`。

上游日志中包含 `!xxx` 合并请求包装提交；下列按实际功能提交归并记录：

```text
54f0166 refactor(cluster): 优化Redis消息发布与订阅机制
24c3aca feat(model): 新增模型连接性检测功能
748fd4a fix(ui): 修复模型配置弹窗宽度属性错误
ea9b848 feat(ui): 优化模态框和模型配置组件样式与功能
c90e207 feat(chat): 添加消息内容复制功能
89b5903 feat(skill): 重构技能包文件存储方式，支持文件粒度管理
051252a feat(ui): 扩展并更新Ant Design Vue全局组件声明
06c14b4 feat(db): 初始化参数表新增默认参数数据
e2a49dc fix(db_init): 优化技能包文件表中的技能描述内容
71996ca fix(readme): 优化README.md，优化使用手册
d1d7c34 fix(skill): 修复 SKILL.md 元数据缺失的问题
5d15845 feat(params): 添加参数变更广播及技能模块缓存联动
8c93f2f fix(docs): 修正skill_file插入数据的id生成逻辑
c7ddfff feat: support elasticsearch vector store
ddda622 chore: use elasticsearch 8.15.0 docker image
125dc50 fix: gate vector store docker services by profile
ff66eee fix: accept default login password formats
8016e74 fix: tie compose vector profile to selected store
60e614c fix: allow external elasticsearch vector store
c35daee fix: 优化agentSession数据库名称获取方式，遵循JDBC规范
dc0e990 fix: 优化agentSession数据库名称获取方式，遵循JDBC规范
6cf31f2 feat(session): 实现自动探测数据库名的 MysqlSession 配置
f2f1222 feat: 支持weaviate rag
3f895bb fix(resource): 修正存储配置异常提示信息
8837368 feat(core): 修复skill注册过程，前置配置代码执行环境时机
d5169b9 更新二维码
bb59989 更新二维码
```

## 2026-06-01 迁移落地状态

| 上游变更 | 当前项目状态 | 处理结论 | 落点 |
| --- | --- | --- | --- |
| Redis 消息发布改为事务提交后异步发布、发布重试、订阅分发线程池 | 已在新工程实现 `publishAfterCommit`、异步重试和订阅线程池，事务内配置变更调用方已切换 | 已迁移 | `source/agent-platform/agent-run/run-event`，以及相关配置服务调用方 |
| 默认登录密码兼容：支持服务端加盐 MD5、客户端 MD5 后再加盐、历史纯客户端 MD5 | 已新增无盐 MD5 工具方法和兼容匹配逻辑 | 已迁移 | `source/agent-platform/agent-governance/governance-iam`、`source/agent-platform/agent-domain/domain-common` |
| 模型连通性检测：新增检测接口、检测状态字段、前端模型卡片状态展示 | 已新增 `connectivity_*` 字段、Flyway 脚本、检测接口、简单模型构建链路和前端检测入口 | 已迁移 | `capability-provider`、`runtime-agentscope`、`adapter-rest`、`agent-platform-ui`、`boot-app/db/migration` |
| 技能包文件粒度管理：新增 `skill_file` 表、文件树 API、技能编辑器、按扩展名白名单入库 | 已新增 `skill_file` 领域/仓储/服务/API/Flyway、启动同步、导入写入、运行时读取和前端文件树编辑页；保留旧 JSON 列兼容第一阶段数据结构 | 已迁移 | `domain-capability`、`repo-mybatis`、`capability-skill`、`runtime-agentscope`、`adapter-rest`、`agent-platform-ui`、`boot-app/db/migration` |
| 参数变更广播与技能模块缓存联动 | 已新增参数变更消息、Redis topic、参数保存后广播和技能扩展名缓存清理 | 已迁移 | `governance-auth`、`capability-skill`、`run-event` |
| 技能注册时前置配置代码执行环境 | 已调整 `ReActAgentHelper` 和 `SkillBoxFactory`，构建 SkillBox 前复用 toolkit 并前置代码执行环境配置 | 已迁移 | `source/agent-platform/agent-runtime/runtime-agentscope` |
| 存储配置异常提示修正与聊天消息复制按钮 | 后端提示文案和聊天消息整条复制按钮已迁移 | 已迁移 | `worker-file`、`agent-platform-ui/src/components/chat/MessageItem.vue` |
| Elasticsearch / Weaviate RAG 向量存储与 Docker 配置 | 当前已支持 `pgvector`、`milvus`、`qdrant`，未支持 `elasticsearch` / `weaviate` | 暂不迁移；仅在部署目标需要这两类向量库时纳入 | `capability-rag`、`boot-app/application*.yml`、部署脚本 |
| AgentScope `MysqlSession` 自动探测数据库名 | 当前新工程使用自研 `PostgresSession` 和 `conversationDataSource`，不依赖上游 `MysqlSession` | 暂不迁移；只保留设计参考 | 无 |
| FileIcon 配色扩展、Ant Design Vue 组件声明、模型弹窗部分样式、README/二维码/图片 | 与核心平台能力无强关联，且部分为上游项目文档/展示资源 | 暂不迁移或按 UI/文档需要单独处理 | `agent-platform-ui`、`docs/` |

## 后续同步检查命令

后续如需继续检查 `.apboa` 新增上游提交，以“最近已核对到”作为起点：

```sh
git -C .apboa log --oneline --reverse b1afcf0a65ee48fc43b81d63c384aa63e7ac9849..HEAD
git -C .apboa diff --name-status b1afcf0a65ee48fc43b81d63c384aa63e7ac9849..HEAD
```

如果发现新增上游变更，应先按功能归类，再同步到 `.apboa/` 之外的新工程目录。`.apboa/` 始终只读，不作为变更落点。
