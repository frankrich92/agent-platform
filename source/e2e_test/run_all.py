from __future__ import annotations

from common import ApiClient, BlockedByRepoPolicy, Reporter, RunState, cleanup_temp, parse_args
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
    reporter = Reporter(config.stop_on_fail)
    modules = [
        AuthIamModule(config, state, client),
        ProviderModule(config, state, client),
        CapabilityModule(config, state, client),
        FilesRagModule(config, state, client),
        AgentChatModule(config, state, client),
        PlatformOpsModule(config, state, client),
    ]
    try:
        for module in modules:
            for name, step in module.steps():
                if name not in ("auth/frontend smoke", "auth/login and refresh", "cleanup/created data") and not state.token:
                    reporter.step(name, lambda: (_ for _ in ()).throw(
                        BlockedByRepoPolicy("auth/login failed; dependent API chain skipped")
                    ))
                    continue
                reporter.step(name, step)
        return reporter.summary()
    finally:
        cleanup_temp(state)


if __name__ == "__main__":
    raise SystemExit(main())
