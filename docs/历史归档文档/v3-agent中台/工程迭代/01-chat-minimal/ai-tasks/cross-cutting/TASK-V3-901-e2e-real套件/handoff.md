# TASK-V3-901 交接记录

## 状态

Review。

## 交接内容

已完成：

- 新增 `source/test/v3/chat_minimal_db_e2e.py`。
- 脚本通过 HTTP API 发起一次真实对话，消费 SSE 至终态，并校验底层数据库记录。
- 默认清理测试数据；支持 `--keep-data` 保留记录给人工复核。
- 支持通过 `HTAM_E2E_*` 环境变量或命令行参数覆盖 base URL、用户、Agent、消息和超时时间。

修改范围：

```text
source/test/v3/
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-901-e2e-real套件/
```

未完成：

- 未覆盖重启恢复、多节点恢复、取消、失败和 provider 超时分支。
- 未接入 CI；后续 `TASK-V3-903` 决定是否把该脚本作为可选 L5 门禁。

风险：

- 依赖真实 `.env`、PostgreSQL 和 LLM provider 可用性。
- `--keep-data` 会保留测试数据，人工复核后应手动清理或再次运行默认清理路径。

已知问题 / workaround：

- 本地没有 Python PostgreSQL driver 时，脚本会尝试使用本机 PostgreSQL JDBC jar 走 Java 探针查询；可通过 `POSTGRES_JDBC_JAR` 显式指定 jar 路径。

是否触碰公共契约：否。

公共契约处理：

- 已合入主契约：不适用。
- 仍停留在任务内局部约定：不适用。
- 待人工确认：后续是否纳入 CI L5 门禁。

未授权或共享文件变更：

- 是否发现未授权文件变更：否。
- 涉及文件：不适用。
- 处理建议：不适用。

验证结果：

```bash
python3 source/test/v3/chat_minimal_db_e2e.py --base-url http://127.0.0.1:18080 --timeout-seconds 120
python3 source/test/v3/chat_minimal_db_e2e.py --base-url http://127.0.0.1:18080 --timeout-seconds 120 --keep-data
```

结果：通过；默认清理路径和人工保留路径均可用。
