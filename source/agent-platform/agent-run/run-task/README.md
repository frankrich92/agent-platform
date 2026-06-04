# run-task

## 模块作用

任务调度模块，承接 Quartz 任务、分布式锁、集群消息和任务服务。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 27 个 Java 源文件。
- 主要包：
  - `com.htam.agent.run.task.consts`
  - `com.htam.agent.run.task.core.annotation`
  - `com.htam.agent.run.task.core.aspect`
  - `com.htam.agent.run.task.core.client`
  - `com.htam.agent.run.task.core.cluster`
  - `com.htam.agent.run.task.core.cluster.message`
  - `com.htam.agent.run.task.core.config`
  - `com.htam.agent.run.task.core.enable`
  - 其余 5 项见源码目录。
- 阅读入口：
  - `JobConst`
  - `JobRedisKey`
  - `QuartzCreate`
  - `QuartzRemove`
  - `QuartzAspect`
  - `QuartzCreateAspect`
  - `QuartzRemoveAspect`
  - `QuartzClient`
  - `QuartzScript`
  - `JobDistributedLock`
  - 其余 17 项见源码目录。

## 依赖边界

Run 模块记录运行过程和任务状态，具体执行交给 runtime/worker，持久化交给 repo。

## 阅读建议

先看 `com.htam.agent.run.task.consts` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
