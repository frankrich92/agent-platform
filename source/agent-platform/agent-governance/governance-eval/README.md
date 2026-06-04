# governance-eval

## 模块作用

评估治理模块，定义评估样本、运行器和结果对象。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 3 个 Java 源文件。
- 主要包：
  - `com.htam.agent.governance.eval`
- 阅读入口：
  - `EvalResult`
  - `EvalRunner`
  - `EvalSample`

## 依赖边界

治理模块应输出决策、审批、审计和鉴权结果，避免直接耦合具体运行时实现。

## 阅读建议

先看 `com.htam.agent.governance.eval` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
