# capability-skill

## 模块作用

Skill 能力模块，负责 Skill 包导入、文件系统、市场策略、签名、发布和工具关联。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 38 个 Java 源文件。
- 主要包：
  - `com.htam.agent.capability.skill`
  - `com.htam.agent.capability.skill.cluster`
  - `com.htam.agent.capability.skill.imports`
  - `com.htam.agent.capability.skill.imports.config`
  - `com.htam.agent.capability.skill.imports.source`
  - `com.htam.agent.capability.skill.service`
  - `com.htam.agent.capability.skill.service.impl`
- 阅读入口：
  - `InitLoadSkillScript`
  - `SkillContentProvider`
  - `SkillDependency`
  - `SkillFileSystemService`
  - `SkillFileSystemServiceInitializer`
  - `SkillIndexEntry`
  - `SkillMarketPolicy`
  - `SkillPackageSignature`
  - `SkillReleasePlanner`
  - `SkillReleaseStatus`
  - 其余 28 项见源码目录。
- `src/test/java`：包含 2 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

能力模块维护配置态和能力计划，运行时执行细节通过 runtime/worker/repo 边界协作。

## 阅读建议

先看 `com.htam.agent.capability.skill` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
