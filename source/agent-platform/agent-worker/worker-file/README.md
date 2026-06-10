# worker-file

## 模块作用

文件 Worker 模块，负责附件、附件日志、存储协议和本地/S3/FTP 文件存储。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 16 个 Java 源文件。
- 主要包：
  - `com.htam.agent.worker.file.enums`
  - `com.htam.agent.worker.file.service`
  - `com.htam.agent.worker.file.storage.config`
  - `com.htam.agent.worker.file.storage.core`
  - `com.htam.agent.worker.file.storage.core.service`
- 阅读入口：
  - `AttachOptType`
  - `ProtocolType`
  - `AttachLogService`
  - `AttachLogServiceImpl`
  - `AttachService`
  - `AttachServiceImpl`
  - `StorageProtocolService`
  - `StorageProtocolServiceImpl`
  - `AmazonS3Config`
  - `FtpStorageConfig`
  - 其余 6 项见源码目录。
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

Worker 通过 worker-spi 暴露任务协议，具体 CLI、文件和沙箱实现留在各适配子模块。

## 阅读建议

先看 `com.htam.agent.worker.file.enums` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
