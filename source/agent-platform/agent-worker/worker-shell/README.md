# worker-shell

## 模块作用

Shell Worker 模块，描述 shell 命令请求和执行器。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 2 个 Java 源文件。
- 主要包：
  - `com.htam.agent.worker.shell`
- 阅读入口：
  - `ShellCommandExecutor`
  - `ShellCommandRequest`

## 依赖边界

Worker 通过 worker-spi 暴露任务协议，具体 CLI、文件和沙箱实现留在各适配子模块。

## 阅读建议

先看 `com.htam.agent.worker.shell` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
