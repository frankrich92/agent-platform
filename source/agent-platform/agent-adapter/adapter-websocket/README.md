# adapter-websocket

## 模块作用

WebSocket 适配模块，负责连接鉴权、会话管理、客户端/服务端消息处理和 Redis 集群广播。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 19 个 Java 源文件。
- 主要包：
  - `com.htam.agent.adapter.websocket.cluster`
  - `com.htam.agent.adapter.websocket.config`
  - `com.htam.agent.adapter.websocket.context`
  - `com.htam.agent.adapter.websocket.handler`
  - `com.htam.agent.adapter.websocket.handler.client`
  - `com.htam.agent.adapter.websocket.handler.server`
  - `com.htam.agent.adapter.websocket.interceptor`
  - `com.htam.agent.adapter.websocket.model`
  - 其余 1 项见源码目录。
- 阅读入口：
  - `ClusterMessage`
  - `ClusterMessageSubscriber`
  - `RedisSessionManager`
  - `AgentRedisConfig`
  - `AgentWebSocketConfig`
  - `AgentWebSocketHandler`
  - `AgentWebSocketSessionManager`
  - `AgentWebSocketSession`
  - `ClientMessageHandlerAdapter`
  - `ServiceMessageHandlerAdapter`
  - 其余 9 项见源码目录。

## 依赖边界

适配器负责协议转换和参数校验，业务规则应下沉到 profile/run/capability/governance/worker 等模块。

## 阅读建议

先看 `com.htam.agent.adapter.websocket.cluster` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
