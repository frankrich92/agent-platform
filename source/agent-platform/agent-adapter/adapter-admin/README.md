# adapter-admin

## 模块作用

管理端适配占位模块，描述管理端 endpoint 入口，后续承接后台专用 API。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 1 个 Java 源文件。
- 主要包：
  - `com.htam.agent.adapter.admin`
- 阅读入口：
  - `AdminEndpointDescriptor`

## 依赖边界

适配器负责协议转换和参数校验，业务规则应下沉到 profile/run/capability/governance/worker 等模块。

## 阅读建议

先看 `com.htam.agent.adapter.admin` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
