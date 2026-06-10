FROM node:24-alpine AS build

WORKDIR /workspace
RUN corepack enable

COPY source/agent-platform-ui/package.json source/agent-platform-ui/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY source/agent-platform-ui ./
RUN pnpm build:main

FROM nginx:1.27-alpine

COPY docker/frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist-main /usr/share/nginx/html

EXPOSE 80
