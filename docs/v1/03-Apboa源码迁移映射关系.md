# Apboa 源码迁移映射关系

## 1. 文档目的

本文用于记录 `.apboa/` 上游源码到当前新工程的迁移映射关系。后续如果 Apboa 有新的提交，应优先依据本文判断新增或变更代码应该落到哪个新模块，避免重新分析全量工程。

`.apboa/` 是只读参考目录，任何同步动作都不能修改 `.apboa/` 下的文件。

## 2. 全局迁移规则

| 规则项 | 固定规则 |
| --- | --- |
| 后端新工程根目录 | `source/agent-platform/` |
| 前端新工程根目录 | `source/agent-platform-ui/` |
| Maven `groupId` | `com.htam.agent` |
| Java 包名前缀 | `com.htam.agent` |
| 原包名替换 | `com.hxh.apboa` -> `com.htam.agent` |
| 数据库表结构 | 保持 Apboa 原表结构不变 |
| REST/AGUI/WebSocket 外部路径 | 第一阶段保持兼容 |
| `.apboa/` | 只能读取，不能修改、格式化、生成或提交 |

同步新增 Java 源码时，需要同时完成：

1. 根据本文映射复制到目标模块。
2. 将 `package` 与 `import com.hxh.apboa...` 替换为 `com.htam.agent...`。
3. 将物理目录从 `com/hxh/apboa` 调整为 `com/htam/agent`。
4. 保持业务逻辑不重写，优先只做模块落位、包名和依赖调整。
5. 若新增 XML mapper，路径应落在 `com/htam/agent/**/mapper/**/*.xml` 下。

## 3. 原模块到新模块映射

| Apboa 原路径 | 新工程目标路径 | 新模块 | 迁移说明 |
| --- | --- | --- | --- |
| `.apboa/common` | `source/agent-platform/agent-infra/infra-common` | `infra-common` | 公共 DTO、VO、entity、枚举、响应结构、异常、工具类、MyBatis/Jackson/Redis/JWT 等通用能力。 |
| `.apboa/cluster` | `source/agent-platform/agent-infra/infra-stream` | `infra-stream` | Redis 发布订阅、集群消息发布/订阅基础能力。 |
| `.apboa/security/script-security` | `source/agent-platform/agent-infra/infra-security` | `infra-security` | 脚本安全检查与执行安全相关代码。 |
| `.apboa/biz/resource` | `source/agent-platform/agent-infra/infra-files` | `infra-files` | 附件、文件资源、上传下载、资源协议等文件平台能力。 |
| `.apboa/biz/params` | `source/agent-platform/agent-admin/admin-system` | `admin-system` | 系统参数配置、参数适配器及参数管理接口。 |
| `.apboa/biz/model` | `source/agent-platform/agent-admin/admin-provider` | `admin-provider` | 模型供应商与模型配置管理。 |
| `.apboa/biz/account` | `source/agent-platform/agent-admin/admin-iam` | `admin-iam` | 账号、认证、用户资料、角色管理。 |
| `.apboa/biz/sk` | `source/agent-platform/agent-admin/admin-iam` | `admin-iam` | API Key/Secret Key 管理，与 IAM 同域合并。 |
| `.apboa/biz/studio` | `source/agent-platform/agent-admin/admin-agent` | `admin-agent` | Studio 配置与 Studio 管理能力。 |
| `.apboa/biz/tool` | `source/agent-platform/agent-admin/admin-capability` | `admin-capability` | 工具配置、Agent 工具关联、工具管理。 |
| `.apboa/biz/mcp` | `source/agent-platform/agent-admin/admin-capability` | `admin-capability` | MCP Server、MCP Tool、运行时降级相关管理。 |
| `.apboa/biz/skill` | `source/agent-platform/agent-admin/admin-capability` | `admin-capability` | Skill 包、Skill Tool、Agent Skill 关联。 |
| `.apboa/biz/hook` | `source/agent-platform/agent-admin/admin-capability` | `admin-capability` | Hook 配置、Agent Hook 关联。 |
| `.apboa/biz/prompt` | `source/agent-platform/agent-admin/admin-capability` | `admin-capability` | 系统 Prompt 模板管理。 |
| `.apboa/biz/sensitive` | `source/agent-platform/agent-admin/admin-capability` | `admin-capability` | 敏感词配置与过滤管理。 |
| `.apboa/biz/agent` | `source/agent-platform/agent-biz/biz-agent` | `biz-agent` | Agent 定义、会话、消息、统计、工作区、执行配置、子 Agent 关联等核心业务。 |
| `.apboa/biz/a2a` | `source/agent-platform/agent-biz/biz-agent` | `biz-agent` | A2A 配置、A2A Agent 管理，与 Agent 核心业务合并。 |
| `.apboa/biz/knowledge` | `source/agent-platform/agent-biz/biz-knowledge` | `biz-knowledge` | 知识库配置、Agent 知识库关联。 |
| `.apboa/biz/rag` | `source/agent-platform/agent-biz/biz-knowledge` | `biz-knowledge` | RAG 文档接口入口。 |
| `.apboa/core/rag` | `source/agent-platform/agent-biz/biz-knowledge` | `biz-knowledge` | 本地 RAG、解析、Embedding、VectorStore、RAG mapper。为避免 runtime 与 knowledge 循环依赖，机械迁入知识库业务模块。 |
| `.apboa/core/knowledge` | `source/agent-platform/agent-biz/biz-knowledge` | `biz-knowledge` | 知识库运行时适配，如 Local/Bailian/Dify/RagFlow 知识实现。 |
| `.apboa/core` 除 `rag`、`knowledge` 外 | `source/agent-platform/agent-runtimes/runtime-agentscope` | `runtime-agentscope` | AgentScope 装配、Agent 工厂、模型工厂、工具箱、MCP 客户端、Hook、Prompt、Memory、工作区运行时等。 |
| `.apboa/job` | `source/agent-platform/agent-biz/biz-task` | `biz-task` | Quartz 任务、任务控制、调度器、任务集群消息。 |
| `.apboa/websocket` | `source/agent-platform/agent-adapter/adapter-websocket` | `adapter-websocket` | WebSocket endpoint、会话管理、客户端/服务端消息处理、推送服务。 |
| `.apboa/console` | `source/agent-platform/agent-boot` | `agent-boot` | Spring Boot 启动类、配置文件、启动初始化以及少量 AgentScope 覆盖类。 |
| `.apboa/ui` | `source/agent-platform-ui` | 独立前端工程 | Vue/Vite 前端独立迁移，不进入后端 Maven reactor。 |

## 4. 新模块职责边界

| 新模块 | 主要承载内容 | 当前来源 |
| --- | --- | --- |
| `agent-domain` | 预留领域核心模型。第一阶段未承载 Apboa 代码主体。 | 新建骨架 |
| `agent-api` | 预留 API/SPI 契约。第一阶段未承载 Apboa 代码主体。 | 新建骨架 |
| `infra-common` | 公共对象、响应、异常、工具类、数据访问基础配置。 | `.apboa/common` |
| `infra-stream` | Redis 集群消息通道。 | `.apboa/cluster` |
| `infra-security` | 脚本安全。 | `.apboa/security/script-security` |
| `infra-files` | 文件/附件资源。 | `.apboa/biz/resource` |
| `admin-system` | 系统参数。 | `.apboa/biz/params` |
| `admin-provider` | 模型供应商和模型配置。 | `.apboa/biz/model` |
| `admin-iam` | 账号、认证、角色、Secret Key。 | `.apboa/biz/account`、`.apboa/biz/sk` |
| `admin-agent` | Studio 管理。 | `.apboa/biz/studio` |
| `admin-capability` | Tool、MCP、Skill、Hook、Prompt、Sensitive 管理。 | `.apboa/biz/tool`、`mcp`、`skill`、`hook`、`prompt`、`sensitive` |
| `biz-agent` | Agent 定义、会话、消息、工作区、A2A。 | `.apboa/biz/agent`、`.apboa/biz/a2a` |
| `biz-knowledge` | 知识库、RAG、向量检索相关业务。 | `.apboa/biz/knowledge`、`.apboa/biz/rag`、`.apboa/core/rag`、`.apboa/core/knowledge` |
| `biz-task` | Quartz 任务与调度。 | `.apboa/job` |
| `runtime-agentscope` | AgentScope 运行时装配。 | `.apboa/core` 剩余部分 |
| `adapter-websocket` | WebSocket 接入适配。 | `.apboa/websocket` |
| `agent-boot` | 启动入口和运行配置。 | `.apboa/console` |
| `source/agent-platform-ui` | 前端工程。 | `.apboa/ui` |

## 5. 配置和资源迁移规则

| 原资源 | 新位置 | 处理规则 |
| --- | --- | --- |
| `.apboa/console/src/main/resources/application.yml` | `source/agent-platform/agent-boot/src/main/resources/application.yml` | 可直接同步，应用名和 profile 可按新工程策略调整。 |
| `.apboa/console/src/main/resources/application-dev.yml` | `source/agent-platform/agent-boot/src/main/resources/application-dev.yml` | 数据库、Redis、端口等连接信息保持不变；包路径类配置需改为 `com.htam.agent`。 |
| `.apboa/console/src/main/resources/logback-spring.xml` | `source/agent-platform/agent-boot/src/main/resources/logback-spring.xml` | 可直接同步；日志文件名后续可按内部规范调整。 |
| `src/main/java/**/mapper/*.xml` | 对应新模块 `src/main/java/com/htam/agent/**/mapper/*.xml` | Mapper XML 随 Java mapper 所属模块迁移。 |
| `.apboa/ui` 静态/配置文件 | `source/agent-platform-ui` | 排除 `pom.xml`、`node_modules`、`dist*`、`target` 等构建/依赖文件。 |

当前后端配置中特别需要保持：

```yaml
mybatis-plus:
  mapper-locations: classpath*:com/htam/agent/**/mapper/**/*.xml
  type-aliases-package: com.htam.agent.**.entity
```

## 6. 后续同步 Apboa 新提交的流程

### 6.1 识别变更

在不修改 `.apboa/` 的前提下查看上游变更。常用判断维度：

1. 新增/修改 Java 文件所在的 Apboa 原模块。
2. 新增/修改资源文件类型，如 yml、xml、前端文件。
3. 是否新增 Maven 依赖。
4. 是否涉及数据库表结构。第一阶段原则上不迁移表结构变更。
5. 是否改变外部 HTTP、AGUI、WebSocket 契约。

### 6.2 按映射落位

按照第 3 节映射复制到目标模块。新增目录和包名应遵循：

```text
source/agent-platform/<目标模块>/src/main/java/com/htam/agent/...
```

前端新增文件落到：

```text
source/agent-platform-ui/...
```

### 6.3 修改包名和路径

最小必要替换：

```text
com.hxh.apboa -> com.htam.agent
com/hxh/apboa -> com/htam/agent
```

不要顺手重构业务逻辑。除非编译或运行必须，不改方法签名、不改表名字段名、不重写实现。

### 6.4 补 Maven 依赖

如果 Apboa 新提交增加了依赖：

1. 先在原模块 `pom.xml` 找依赖来源和版本。
2. 版本优先放到 `source/agent-platform/pom.xml` 的 properties 或 dependencyManagement。
3. 具体使用模块再添加 dependency。
4. 避免引入前端到后端 Maven reactor。

### 6.5 验证

后端：

```sh
export JAVA_HOME=/opt/jdk-21.0.7
export MAVEN_HOME=/opt/apache-maven-3.9.12
export NODE_HOME=/root/.nvm/versions/node/v24.15.0
export PYTHON_HOME=/app/miniforge3/envs/agent
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$NODE_HOME/bin:$PATH"
conda activate agent

cd source/agent-platform
mvn -q -DskipTests package
```

前端：

```sh
export NODE_HOME=/root/.nvm/versions/node/v24.15.0
export PATH="$NODE_HOME/bin:$PATH"
conda activate agent

cd source/agent-platform-ui
pnpm build
```

静态检查：

```sh
rg -n "com\.hxh\.apboa" source
find source/agent-platform -path '*com/hxh/apboa*' -print
rg -n "agent-platform-ui|agent-ui|<module>.*ui" source/agent-platform -g 'pom.xml'
git status --short
```

预期结果：

- `source/` 下不应残留 `com.hxh.apboa`。
- 不应残留 `com/hxh/apboa` 物理路径。
- 后端 Maven reactor 不应包含 `source/agent-platform-ui`。
- `git status` 不应出现 `.apboa/` 变更。

## 7. 当前已知特殊点

### 7.1 `io.agentscope` 包保留

`agent-boot` 中存在少量 `io.agentscope...` 包路径源码，这是原 Apboa 对 AgentScope 类的覆盖/适配代码。当前保留该包名以维持运行时兼容，不纳入 `com.htam.agent` 强制替换范围。

### 7.2 RAG 代码从 `core` 拆入 `biz-knowledge`

原 `.apboa/core/rag` 和 `.apboa/core/knowledge` 没有继续放在 `runtime-agentscope`，而是迁入 `biz-knowledge`。原因是 `biz-knowledge` 的 RAG controller 直接依赖这些类，若放在 runtime 中会造成更强的业务/运行时耦合或循环依赖。

后续如果 Apboa 在 `.apboa/core/rag` 或 `.apboa/core/knowledge` 下新增代码，应继续迁到 `biz-knowledge`。

### 7.3 当前未按原 Apboa Maven 模块保留的内容

以下内容不做 1:1 迁移：

- `.apboa/ui/pom.xml`：前端已独立，不进入 Maven reactor。
- `.apboa` 原父子 POM 结构：已替换为新多模块 POM。
- `node_modules`、`dist*`、`target` 等依赖和构建产物。

## 8. 快速查表

| 如果 Apboa 变更在... | 优先迁到... |
| --- | --- |
| `common` | `infra-common` |
| `cluster` | `infra-stream` |
| `security/script-security` | `infra-security` |
| `biz/resource` | `infra-files` |
| `biz/params` | `admin-system` |
| `biz/model` | `admin-provider` |
| `biz/account`、`biz/sk` | `admin-iam` |
| `biz/studio` | `admin-agent` |
| `biz/tool`、`biz/mcp`、`biz/skill`、`biz/hook`、`biz/prompt`、`biz/sensitive` | `admin-capability` |
| `biz/agent`、`biz/a2a` | `biz-agent` |
| `biz/knowledge`、`biz/rag`、`core/rag`、`core/knowledge` | `biz-knowledge` |
| `core` 其他目录 | `runtime-agentscope` |
| `job` | `biz-task` |
| `websocket` | `adapter-websocket` |
| `console` | `agent-boot` |
| `ui` | `source/agent-platform-ui` |
