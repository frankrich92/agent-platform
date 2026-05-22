from __future__ import annotations

import zipfile

from common import E2EError, Module


class AgentChatModule(Module):
    def steps(self):
        return [
            ("agent/code execution config", self.code_execution_chain),
            ("agent/studio config", self.studio_chain),
            ("agent/definition and A2A", self.agent_chain),
            ("agent/chat key and session", self.chat_chain),
            ("agent/workspace files", self.workspace_chain),
        ]

    def code_execution_chain(self) -> None:
        self.require_auth()
        payload = {
            "configName": f"{self.state.prefix}_code_exec",
            "workDir": str(self.state.temp_dir / "code-work"),
            "uploadDir": str(self.state.temp_dir / "code-upload"),
            "autoUpload": False,
            "enableShell": False,
            "enableRead": True,
            "enableWrite": False,
            "command": ["python3"],
            "enabled": True,
        }
        self.client.json("POST", "/agent/code-execution", payload)
        items = self.require_data(self.client.json("GET", "/agent/code-execution/list"), "code-execution-list")
        item_id = self.find_in_records(items, "configName", payload["configName"])
        self.state.created["code_execution_id"] = item_id
        self.add_cleanup(
            lambda: self.client.json("DELETE", "/agent/code-execution", [item_id], expect_success=False)
        )
        self.client.json("GET", f"/agent/code-execution/{item_id}")
        payload["id"] = item_id
        payload["enableWrite"] = True
        self.client.json("PUT", "/agent/code-execution", payload)
        self.client.json("POST", "/agent/code-execution/used-with-agent", [item_id])

    def studio_chain(self) -> None:
        self.require_auth()
        payload = {"url": "http://127.0.0.1:7860", "project": f"{self.state.prefix}_studio", "enabled": True}
        self.client.json("POST", "/studio", payload)
        items = self.require_data(self.client.json("GET", "/studio/list"), "studio-list")
        item_id = self.find_in_records(items, "project", payload["project"])
        self.state.created["studio_id"] = item_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/studio", [item_id], expect_success=False))
        self.client.json("GET", f"/studio/{item_id}")
        payload["id"] = item_id
        payload["url"] = "http://127.0.0.1:7861"
        self.client.json("PUT", "/studio", payload)
        self.client.json("POST", "/studio/used-with-agent", [item_id])

    def agent_chain(self) -> None:
        self.require_auth()
        self.require_created(
            "model_config_id",
            "skill_id",
            "tool_id",
            "hook_id",
            "knowledge_id",
            "prompt_id",
            "sensitive_id",
            "studio_id",
            "code_execution_id",
        )
        payload = {
            "agentType": "CUSTOM",
            "name": f"{self.state.prefix}_agent",
            "agentCode": f"{self.state.prefix}_agent".replace("_", "-"),
            "description": "created by real-chain e2e",
            "modelConfigId": self.state.created["model_config_id"],
            "modelParamsOverride": {},
            "skill": [self.state.created["skill_id"]],
            "tool": [self.state.created["tool_id"]],
            "mcp": [self.state.created["mcp_id"]] if "mcp_id" in self.state.created else [],
            "mcpBindings": (
                [{"mcpServerId": self.state.created["mcp_id"], "exposureMode": "ALL_GLOBAL", "mcpToolIds": []}]
                if "mcp_id" in self.state.created
                else []
            ),
            "hook": [self.state.created["hook_id"]],
            "subAgent": [],
            "knowledgeBase": [self.state.created["knowledge_id"]],
            "toolChoiceStrategy": "AUTO",
            "specificToolName": "",
            "systemPromptTemplateId": self.state.created["prompt_id"],
            "followTemplate": True,
            "systemPrompt": "E2E system prompt",
            "sensitiveWordConfigId": self.state.created["sensitive_id"],
            "sensitiveFilterEnabled": False,
            "maxIterations": 3,
            "enablePlanning": False,
            "maxSubtasks": 3,
            "requirePlanConfirmation": False,
            "enableMemory": False,
            "enableMemoryCompression": False,
            "showToolProcess": True,
            "memoryCompressionConfig": {},
            "structuredOutputEnabled": False,
            "structuredOutputSchema": {},
            "structuredOutputReminder": "PROMPT",
            "version": "1.0.0",
            "tag": self.state.prefix,
            "enabled": True,
            "studioConfigId": self.state.created["studio_id"],
            "codeExecutionConfigId": self.state.created["code_execution_id"],
        }
        self.client.json("POST", "/agent/definition", payload)
        agent_id = self.find_page_id("/agent/definition/page", {"name": payload["name"]})
        self.state.created["agent_id"] = agent_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/agent/definition", [agent_id], expect_success=False))
        self.client.json("GET", f"/agent/definition/{agent_id}")
        payload["id"] = agent_id
        payload["description"] = "updated by real-chain e2e"
        self.client.json("PUT", "/agent/definition", payload)
        self.client.json("POST", "/agent/definition/used-with-agent", [agent_id])
        self.client.json("GET", "/agent/definition/get/tags")
        self.client.json("GET", f"/agent/definition/{agent_id}/allow/file-type")
        self.client.json("GET", f"/agent/definition/{agent_id}/enabled/tools")
        self.client.json("GET", f"/agent/definition/{agent_id}/enabled/skills")
        self.client.json("GET", f"/agent/statistics/{agent_id}/trends", params={"days": 7})
        self.client.json(
            "POST",
            "/agentA2a",
            {
                "agentDefinitionId": agent_id,
                "a2aType": "WELLKNOWN",
                "a2aConfig": {
                    "agentName": payload["name"],
                    "baseUrl": self.config.base_url,
                    "relativeCardPath": "/.well-known/agent.json",
                    "authHeaders": [],
                },
            },
        )
        self.client.json("GET", f"/agentA2a/{agent_id}")

    def chat_chain(self) -> None:
        self.require_auth()
        self.require_created("agent_id")
        agent_id = self.state.created["agent_id"]
        chat_key = self.require_data(
            self.client.json("GET", f"/agent/chat-key/{agent_id}", params={"refresh": "true"}),
            "chat-key",
        )
        self.client.json("GET", f"/agent/chat-key/{chat_key}/get-agent-id")
        self.client.json("POST", f"/auth/chat-key-token/{chat_key}", headers={"token": "false"})
        session = self.require_data(
            self.client.json(
                "POST",
                "/agent/chat/session",
                {"agentId": agent_id, "title": f"{self.state.prefix}_session", "initWorkspace": False},
            ),
            "create-session",
        )
        session_id = str(session["id"])
        self.state.created["session_id"] = session_id
        self.add_cleanup(lambda: self.client.json("DELETE", f"/agent/chat/session/{session_id}", expect_success=False))
        msg = self.require_data(
            self.client.json(
                "POST",
                f"/agent/chat/session/{session_id}/message",
                {"role": "user", "content": "hello from e2e"},
            ),
            "append-message",
        )
        self.client.json(
            "POST",
            f"/agent/chat/session/{session_id}/regenerate",
            {"role": "assistant", "content": "hello response from e2e"},
        )
        self.client.json("PUT", f"/agent/chat/session/{session_id}/current", params={"messageId": msg["id"]})
        self.client.json("GET", f"/agent/chat/session/{session_id}/messages/current")
        self.client.json("GET", f"/agent/chat/session/{session_id}/messages/paged", params={"size": 10})
        self.client.json("GET", "/agent/chat/session/list", params={"agentId": agent_id})
        self.client.json("GET", "/agent/chat/session/page", params={"agentId": agent_id, "page": 1, "size": 10})
        self.client.json("GET", f"/agent/chat/session/{session_id}")
        self.client.json("PUT", f"/agent/chat/session/{session_id}/pin")
        self.client.json("PUT", f"/agent/chat/session/{session_id}/unpin")
        self.client.json("PUT", f"/agent/chat/session/{session_id}/title", params={"title": "updated e2e session"})

    def workspace_chain(self) -> None:
        self.require_auth()
        self.require_created("session_id")
        session_id = self.state.created["session_id"]

        single = self.client.upload(
            "/agent/workspace/upload",
            {"sessionId": session_id},
            {"file": ("e2e-workspace.txt", b"hello workspace", "text/plain")},
        )
        single_path = self.require_data(single, "workspace-upload")
        if single_path != "e2e-workspace.txt":
            raise E2EError(f"unexpected workspace path: {single_path}")

        batch = self.client.upload(
            "/agent/workspace/upload/batch",
            {"sessionId": session_id},
            {"files": ("e2e-batch.txt", b"hello batch", "text/plain")},
        )
        batch_paths = self.require_data(batch, "workspace-upload-batch")
        if "e2e-batch.txt" not in batch_paths:
            raise E2EError(f"unexpected workspace batch paths: {batch_paths}")

        archive_path = self.state.temp_dir / "workspace-archive.zip"
        with zipfile.ZipFile(archive_path, "w", zipfile.ZIP_DEFLATED) as archive:
            archive.writestr("nested/e2e-archive.txt", "hello archive")
        archive_result = self.client.upload(
            "/agent/workspace/upload/archive",
            {"sessionId": session_id},
            {"file": ("workspace-archive.zip", archive_path.read_bytes(), "application/zip")},
        )
        archive_paths = self.require_data(archive_result, "workspace-upload-archive")
        if "nested/e2e-archive.txt" not in archive_paths:
            raise E2EError(f"unexpected workspace archive paths: {archive_paths}")

        files = self.require_data(
            self.client.json("GET", "/agent/workspace/files", params={"sessionId": session_id}),
            "workspace-files",
        )
        if not files:
            raise E2EError("workspace file tree is empty after uploads")

        downloaded = self.client.download(
            "GET",
            "/agent/workspace/download",
            params={"sessionId": session_id, "fileName": "e2e-workspace.txt"},
        )
        if b"hello workspace" not in downloaded:
            raise E2EError("workspace single download content mismatch")

        batch_zip = self.client.download(
            "POST",
            "/agent/workspace/download/batch",
            ["e2e-workspace.txt", "e2e-batch.txt"],
            params={"sessionId": session_id},
        )
        if len(batch_zip) == 0:
            raise E2EError("workspace batch download returned empty content")

        all_zip = self.client.download("GET", "/agent/workspace/download/all", params={"sessionId": session_id})
        if len(all_zip) == 0:
            raise E2EError("workspace full download returned empty content")

        self.client.json(
            "DELETE",
            "/agent/workspace/file",
            params={"sessionId": session_id, "filePath": "e2e-batch.txt"},
        )
        self.client.json("DELETE", "/agent/workspace/clear", params={"sessionId": session_id})
