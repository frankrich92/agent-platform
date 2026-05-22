# Agent Platform E2E Tests

This directory contains Python real-chain E2E scripts. The runners call the
running platform services and real dependencies; they do not mock backend
responses, databases, Redis, vector stores, model providers, or MCP servers.

```sh
python3 source/e2e_test/run_all.py
python3 source/e2e_test/run_all.py --stop-on-fail
python3 source/e2e_test/run_all.py --skip-external
python3 source/e2e_test/run_all.py --show-response
```

Module split:

- `modules/auth_iam.py`: frontend smoke, auth, account management.
- `modules/provider.py`: model provider and model config.
- `modules/capability.py`: prompt, sensitive words, hook, tool, skill,
  local/upload/git skill import, MCP.
- `modules/agent_chat.py`: code execution config, studio, agent, A2A, chat,
  workspace files.
- `modules/files_rag.py`: knowledge config, attachments, RAG documents.
- `modules/platform_ops.py`: secret keys, storage protocol, params, jobs, cleanup.

`e2e_real_chain.py` is only a compatibility wrapper for `run_all.py`.

Useful environment variables:

```sh
export E2E_BASE_URL=http://127.0.0.1:3060
export E2E_UI_URL=http://127.0.0.1:3001
export E2E_USERNAME=admin
export E2E_PASSWORD='Admin@123.com'
export E2E_MCP_CONFIG='{"command":"python3","args":["/absolute/path/to/real_mcp_server.py"],"env":{}}'
export E2E_RAG_CONNECTION_CONFIG='{"providerType":"ollama","baseUrl":"http://localhost:11434/api/embed","embeddingModel":"nomic-embed-text"}'
export E2E_SHOW_RESPONSE=1
export E2E_VERBOSE_MAX_CHARS=12000
export AGENT_RUNTIME_ROOT=/opt/project/agent-platform/source/.agent-platform-e2e
# Localhost E2E disables HTTP_PROXY/HTTPS_PROXY by default.
# Set this only when intentionally testing through a proxy.
export E2E_USE_PROXY=1
```

Response output is disabled by default. Use `--show-response` or
`E2E_SHOW_RESPONSE=1` to print JSON responses for every module, including chat
session detail and message-chain responses. Access tokens, refresh tokens,
passwords, and API keys are redacted before printing. `--verbose` is still
supported as a backward-compatible command-line alias. Use `--hide-response` or
`E2E_HIDE_RESPONSE=1` to force response output off.

Login uses the same password format as the frontend: the raw `E2E_PASSWORD`
value is MD5-hashed before calling `/api/auth/login`.

The client bypasses system proxy settings by default. This matters when
`HTTP_PROXY` is set globally: local backend calls such as
`http://127.0.0.1:3060/api/auth/login` should hit the Spring Boot process
directly, not a proxy.

The migrated backend runtime root is configurable through the Java system
property `agent.runtime.root` or the environment variable `AGENT_RUNTIME_ROOT`.
Set it to a directory under `source/` when starting the backend for E2E runs so
skill import and workspace tests validate the migrated source implementation
without touching the read-only `.apboa/` reference tree.

MCP activation and RAG document processing are real external dependency checks.
When `E2E_MCP_CONFIG` or `E2E_RAG_CONNECTION_CONFIG` is not provided, those
steps report `BLOCKED` instead of using mocks.
