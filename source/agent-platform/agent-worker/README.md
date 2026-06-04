# agent-worker

## 模块作用

Worker 聚合层，抽象代码执行、文件、沙箱、工作区和多种 CLI 编码 Agent。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `worker-spi`
  - `worker-core`
  - `worker-file`
  - `worker-sandbox`
  - `worker-workspace`
  - `worker-shell`
  - `worker-build`
  - `worker-coding-cli`
  - `worker-codex`
  - `worker-qwen-code`
  - `worker-opencode`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

Worker 通过 worker-spi 暴露任务协议，具体 CLI、文件和沙箱实现留在各适配子模块。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
