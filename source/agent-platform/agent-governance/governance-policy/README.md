# governance-policy

## 模块作用

安全策略模块，负责 Shell/Python/Node/HTML 脚本静态安全检查和规则模型。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 18 个 Java 源文件。
- 主要包：
  - `com.htam.agent.governance.policy.security.script`
  - `com.htam.agent.governance.policy.security.script.ast`
  - `com.htam.agent.governance.policy.security.script.checker`
  - `com.htam.agent.governance.policy.security.script.model`
- 阅读入口：
  - `AbstractScriptChecker`
  - `CheckerRegistry`
  - `ScriptSecurityChecker`
  - `ScriptSecurityException`
  - `ScriptSecurityService`
  - `AstAnalyzer`
  - `JavaScriptAstAnalyzer`
  - `PythonAstAnalyzer`
  - `HtmlSecurityChecker`
  - `NodeJsSecurityChecker`
  - 其余 8 项见源码目录。

## 依赖边界

治理模块应输出决策、审批、审计和鉴权结果，避免直接耦合具体运行时实现。

## 阅读建议

先看 `com.htam.agent.governance.policy.security.script` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
