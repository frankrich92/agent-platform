# 本地启动命令

本文记录后端和前端的固定本地启动命令，避免每次重新查找。

## 1. 固定环境

涉及 Java、Maven、Node、Python 或构建验证时，先执行：

```sh
export JAVA_HOME=/opt/jdk-21.0.7
export MAVEN_HOME=/opt/apache-maven-3.9.12
export NODE_HOME=/root/.nvm/versions/node/v24.15.0
export PYTHON_HOME=/app/miniforge3/envs/agent
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$NODE_HOME/bin:$PATH"
source /app/miniforge3/etc/profile.d/conda.sh
conda activate agent
```

## 2. 后端启动

后端端口：`3060`

先在 Maven reactor 根目录构建一次：

```sh
cd /opt/project/agent-platform/source/agent-platform
mvn -q -DskipTests package
```

再进入 `agent-boot` 启动 Spring Boot：

```sh
cd /opt/project/agent-platform/source/agent-platform/agent-boot
export AGENT_RUNTIME_ROOT=/opt/project/agent-platform/source/.agent-platform-e2e
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端默认读取 `agent-boot/src/main/resources/application-dev.yml`，当前依赖本地 MySQL、Redis、PgVector、Nacos 等配置。依赖未启动时，Spring Boot 可能启动失败。

## 3. 前端启动

前端端口：`3001`

```sh
cd /opt/project/agent-platform/source/agent-platform-ui
pnpm dev --host 0.0.0.0 --port 3001
```

访问地址：

```text
http://127.0.0.1:3001/
```

Vite 开发代理会将 `/api` 转发到：

```text
http://127.0.0.1:3060
```

## 4. 可达性检查

```sh
ss -ltnp | rg ':3060|:3001'
```

前端检查：

```sh
curl --noproxy '*' -I http://127.0.0.1:3001/
```

后端检查：

```sh
curl --noproxy '*' -I http://127.0.0.1:3060/
```

## 5. E2E 脚本

服务启动后运行：

```sh
cd /opt/project/agent-platform
python source/e2e_test/run_all.py --skip-external
```

如需打印接口响应：

```sh
python source/e2e_test/run_all.py --skip-external --show-response
```
