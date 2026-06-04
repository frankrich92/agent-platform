# Agent Platform Docker

本目录提供新工程的 Docker 启动配置，构建上下文为父仓库根目录，后端来自 `source/agent-platform/`，前端来自 `source/agent-platform-ui/`。

## 快速启动

```sh
cd docker
cp .env.example .env
docker compose up -d --build
```

前端默认暴露在 `http://localhost:3001`，后端默认暴露在 `http://localhost:3060`。

## 向量库

默认使用内置 `pgvector`（复用 `agent-postgres` 服务）。如需使用 Elasticsearch：

```sh
cd docker
VECTOR_STORE_TYPE=elasticsearch docker compose --profile elasticsearch up -d --build
```

如需使用 Weaviate：

```sh
cd docker
VECTOR_STORE_TYPE=weaviate docker compose --profile weaviate up -d --build
```

外部向量库也可以通过 `.env` 覆盖 `ELASTICSEARCH_URIS`、`WEAVIATE_HOST` 等变量，启动时无需启用对应 profile。

## 构建说明

- `docker/backend/Dockerfile` 使用 Maven 构建 `agent-boot/boot-app`，运行时启用 `docker` Spring profile。
- `docker/frontend/Dockerfile` 使用 `pnpm build:main` 构建主前端，并由 Nginx 代理 `/api`、`/api/ws/agent` 和 `/agent/agui` 到后端。
- 根目录 `.dockerignore` 排除 `.apboa`、`node_modules`、`target`、`dist` 和运行数据，避免把参考源码或本地产物带入镜像上下文。
