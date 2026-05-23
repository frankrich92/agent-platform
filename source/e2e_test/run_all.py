from __future__ import annotations

from common import ApiClient, DagRunner, Fixtures, Reporter, RunState, Step, cleanup_temp, parse_args
from modules.agent_chat import AgentChatModule
from modules.auth_iam import AuthIamModule
from modules.capability import CapabilityModule
from modules.files_rag import FilesRagModule
from modules.platform_ops import PlatformOpsModule
from modules.provider import ProviderModule


def main() -> int:
    config = parse_args()
    state = RunState()
    client = ApiClient(config, state)
    reporter = Reporter(config.stop_on_fail, config.allow_blocked)
    fixtures = Fixtures(state)
    modules = [
        AuthIamModule(config, state, client),
        ProviderModule(config, state, client),
        CapabilityModule(config, state, client),
        FilesRagModule(config, state, client),
        AgentChatModule(config, state, client),
        PlatformOpsModule(config, state, client),
    ]
    try:
        DagRunner(reporter, fixtures).run(build_steps(modules))
        return reporter.summary()
    finally:
        cleanup_temp(state)


def build_steps(modules) -> list[Step]:
    by_name = {name: fn for module in modules for name, fn in module.steps()}
    return [
        Step("auth/frontend smoke", by_name["auth/frontend smoke"]),
        Step("auth/login and refresh", by_name["auth/login and refresh"], provides=["auth"]),
        Step("iam/account management", by_name["iam/account management"], requires=["auth"], provides=["account_id"]),
        Step(
            "provider/model provider and config",
            by_name["provider/model provider and config"],
            requires=["auth"],
            provides=["provider_id", "model_config_id"],
        ),
        Step("capability/prompt template", by_name["capability/prompt template"], requires=["auth"], provides=["prompt_id"]),
        Step("capability/sensitive config", by_name["capability/sensitive config"], requires=["auth"], provides=["sensitive_id"]),
        Step("capability/hook config", by_name["capability/hook config"], requires=["auth"], provides=["hook_id"]),
        Step(
            "capability/tool config and execution",
            by_name["capability/tool config and execution"],
            requires=["auth"],
            provides=["tool_id", "tool_key"],
        ),
        Step(
            "capability/skill package",
            by_name["capability/skill package"],
            requires=["auth", "tool_id"],
            provides=["skill_id"],
        ),
        Step("capability/skill import", by_name["capability/skill import"], requires=["auth"], provides=[
            "local_import_skill_id",
            "upload_import_skill_id",
            "git_import_skill_id",
        ]),
        Step("capability/MCP real activation", by_name["capability/MCP real activation"], requires=["auth"], provides=["mcp_id"]),
        Step("knowledge/config", by_name["knowledge/config"], requires=["auth"], provides=["knowledge_id"]),
        Step("files/attachment upload and download", by_name["files/attachment upload and download"], requires=["auth"]),
        Step("rag/document real processing", by_name["rag/document real processing"], requires=["auth"], provides=["rag_kb_id", "rag_doc_id"]),
        Step("agent/code execution config", by_name["agent/code execution config"], requires=["auth"], provides=["code_execution_id"]),
        Step("agent/studio config", by_name["agent/studio config"], requires=["auth"], provides=["studio_id"]),
        Step(
            "agent/definition and A2A",
            by_name["agent/definition and A2A"],
            requires=[
                "auth",
                "model_config_id",
                "skill_id",
                "tool_id",
                "hook_id",
                "knowledge_id",
                "prompt_id",
                "sensitive_id",
                "studio_id",
                "code_execution_id",
            ],
            provides=["agent_id"],
        ),
        Step("agent/chat key and session", by_name["agent/chat key and session"], requires=["auth", "agent_id"], provides=["session_id"]),
        Step("agent/workspace files", by_name["agent/workspace files"], requires=["auth", "session_id"]),
        Step("iam/secret key", by_name["iam/secret key"], requires=["auth"], provides=["secret_key_id"]),
        Step("files/storage protocol", by_name["files/storage protocol"], requires=["auth"], provides=["storage_id"]),
        Step("system/params", by_name["system/params"], requires=["auth"], provides=["param_id"]),
        Step("task/job scheduler", by_name["task/job scheduler"], requires=["auth", "agent_id"], provides=["job_id"]),
        Step("cleanup/created data", by_name["cleanup/created data"]),
    ]


if __name__ == "__main__":
    raise SystemExit(main())
