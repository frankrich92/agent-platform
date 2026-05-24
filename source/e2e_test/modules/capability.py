from __future__ import annotations

import json
import os
import shutil
import subprocess
import zipfile
from pathlib import Path

from common import BlockedByRepoPolicy, E2EError, Module


class CapabilityModule(Module):
    def steps(self):
        return [
            ("capability/prompt template", self.prompt_chain),
            ("capability/sensitive config", self.sensitive_chain),
            ("capability/hook config", self.hook_chain),
            ("capability/tool config and execution", self.tool_chain),
            ("capability/skill package", self.skill_chain),
            ("capability/skill import", self.skill_import_chain),
            ("capability/MCP real activation", self.mcp_chain),
        ]

    def prompt_chain(self) -> None:
        self.require_auth()
        payload = {
            "category": self.state.prefix,
            "name": f"{self.state.prefix}_prompt",
            "description": "created by real-chain e2e",
            "content": "You are an E2E test agent.",
            "usageCount": 0,
            "enabled": True,
        }
        self.client.json("POST", "/prompt/template", payload)
        item_id = self.find_page_id("/prompt/template/page", {"name": payload["name"]})
        self.state.created["prompt_id"] = item_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/prompt/template", [item_id], expect_success=False))
        self.client.json("GET", f"/prompt/template/{item_id}")
        payload["id"] = item_id
        payload["content"] += " Updated."
        self.client.json("PUT", "/prompt/template", payload)
        self.client.json("POST", "/prompt/template/used-with-agent", [item_id])
        self.client.json("GET", "/prompt/template/get/categories")

    def sensitive_chain(self) -> None:
        self.require_auth()
        payload = {
            "category": self.state.prefix,
            "name": f"{self.state.prefix}_sensitive",
            "description": "created by real-chain e2e",
            "words": ["blocked-e2e-word"],
            "action": "WARN",
            "replacement": "***",
            "enabled": True,
        }
        self.client.json("POST", "/sensitive/config", payload)
        item_id = self.find_page_id("/sensitive/config/page", {"name": payload["name"]})
        self.state.created["sensitive_id"] = item_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/sensitive/config", [item_id], expect_success=False))
        self.client.json("GET", f"/sensitive/config/{item_id}")
        payload["id"] = item_id
        payload["description"] = "updated by real-chain e2e"
        self.client.json("PUT", "/sensitive/config", payload)
        self.client.json("POST", "/sensitive/config/used-with-agent", [item_id])
        self.client.json("GET", "/sensitive/config/get/categories")

    def hook_chain(self) -> None:
        self.require_auth()
        payload = {
            "name": f"{self.state.prefix}_hook",
            "hookType": "CUSTOM",
            "description": "created by real-chain e2e",
            "classPath": "e2e.E2EHook",
            "code": "class E2EHook {}\n",
            "priority": 100,
            "enabled": True,
        }
        self.client.json("POST", "/hook-config", payload)
        item_id = self.find_page_id("/hook-config/page", {"name": payload["name"]})
        self.state.created["hook_id"] = item_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/hook-config", [item_id], expect_success=False))
        self.client.json("GET", f"/hook-config/{item_id}")
        payload["id"] = item_id
        payload["priority"] = 101
        self.client.json("PUT", "/hook-config", payload)
        self.client.json("POST", "/hook-config/used-with-agent", [item_id])

    def tool_chain(self) -> None:
        self.require_auth()
        tool_id = f"{self.state.prefix}_echo_tool"
        code = (
            "import com.htam.agent.core.tool.dynamices.IDynamicAgentTool\n"
            "class E2EEchoTool implements IDynamicAgentTool {\n"
            "  Object execute(Object... args) {\n"
            "    return 'e2e:' + (args == null || args.length == 0 ? '' : args[0].toString())\n"
            "  }\n"
            "}\n"
        )
        payload = {
            "name": f"{self.state.prefix}_tool",
            "toolId": tool_id,
            "description": "created by real-chain e2e",
            "category": self.state.prefix,
            "toolType": "CUSTOM",
            "needConfirm": False,
            "inputSchema": [{"name": "text", "type": "string", "required": True, "description": "input"}],
            "outputSchema": [{"name": "result", "type": "string", "required": False, "description": "output"}],
            "classPath": None,
            "language": "JAVA",
            "code": code,
            "version": "1.0.0",
            "enabled": True,
        }
        self.client.json("POST", "/tool", payload)
        item_id = self.find_page_id("/tool/page", {"name": payload["name"]})
        self.state.created["tool_id"] = item_id
        self.state.created["tool_key"] = tool_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/tool", [item_id], expect_success=False))
        self.client.json("GET", f"/tool/{item_id}")
        payload["id"] = item_id
        payload["description"] = "updated by real-chain e2e"
        self.client.json("PUT", "/tool", payload)
        self.client.json("POST", "/tool/used-with-agent", [item_id])
        self.client.json("GET", "/tool/get/categories")
        result = self.client.json("POST", f"/agent/endpoint/do/{tool_id}/tool", {"text": "ok"})
        if "e2e:" not in str(result.get("data")):
            raise E2EError(f"unexpected tool execution result: {result}")

    def skill_chain(self) -> None:
        self.require_auth()
        self.require_created("tool_id")
        payload = {
            "name": f"{self.state.prefix}_skill",
            "description": "created by real-chain e2e",
            "skillContent": "Use this skill only for E2E verification.",
            "category": self.state.prefix,
            "references": [],
            "examples": [],
            "scripts": [],
            "tools": [self.state.created["tool_id"]],
            "enabled": True,
        }
        self.client.json("POST", "/skill", payload)
        item_id = self.find_page_id("/skill/page", {"name": payload["name"]})
        self.state.created["skill_id"] = item_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/skill", [item_id], expect_success=False))
        self.client.json("GET", f"/skill/{item_id}")
        payload["id"] = item_id
        payload["description"] = "updated by real-chain e2e"
        self.client.json("PUT", "/skill", payload)
        self.client.json("POST", "/skill/used-with-agent", [item_id])
        self.client.json("GET", "/skill/get/categories")

    def skill_import_chain(self) -> None:
        self.require_auth()
        local_name = f"{self.state.prefix}_local_import_skill"
        upload_name = f"{self.state.prefix}_upload_import_skill"
        git_name = f"{self.state.prefix}_git_import_skill"

        local_root = self._create_skill_root("local-import", local_name)
        self.client.json(
            "POST",
            "/skill/import/local",
            {"category": self.state.prefix, "path": str(local_root), "cover": True},
        )
        self._track_imported_skill(local_name, "local_import_skill_id")

        upload_zip = self._create_skill_zip("upload-import.zip", upload_name)
        self.client.upload(
            "/skill/import/upload",
            {"category": self.state.prefix, "cover": "true"},
            {"file": ("upload-import.zip", upload_zip.read_bytes(), "application/zip")},
        )
        self._track_imported_skill(upload_name, "upload_import_skill_id")

        git_repo = self._create_skill_git_repo(git_name)
        self.client.json(
            "POST",
            "/skill/import/git",
            {"category": self.state.prefix, "repoUrl": str(git_repo), "cover": True},
        )
        self._track_imported_skill(git_name, "git_import_skill_id")

    def _track_imported_skill(self, skill_name: str, key: str) -> None:
        item_id = self.find_page_id("/skill/page", {"name": skill_name})
        self.state.created[key] = item_id
        self.add_cleanup(lambda item_id=item_id: self.client.json("DELETE", "/skill", [item_id], expect_success=False))

    def _create_skill_root(self, dirname: str, skill_name: str) -> Path:
        root = self.state.temp_dir / dirname / "skills"
        skill_dir = root / skill_name
        skill_dir.mkdir(parents=True, exist_ok=True)
        (skill_dir / "SKILL.md").write_text(
            "\n".join(
                [
                    "---",
                    f"name: {skill_name}",
                    "description: E2E imported skill",
                    "---",
                    "",
                    "Use this skill only for migrated source E2E verification.",
                    "",
                ]
            ),
            encoding="utf-8",
        )
        return root

    def _create_skill_zip(self, filename: str, skill_name: str) -> Path:
        skill_root = self._create_skill_root(f"{skill_name}-zip-src", skill_name)
        zip_path = self.state.temp_dir / filename
        with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as archive:
            for path in skill_root.parent.rglob("*"):
                if path.is_file():
                    archive.write(path, path.relative_to(skill_root.parent))
        return zip_path

    def _create_skill_git_repo(self, skill_name: str) -> Path:
        repo = self.state.temp_dir / f"{skill_name}-repo"
        skills_root = repo / "skills"
        shutil.copytree(self._create_skill_root(f"{skill_name}-git-src", skill_name), skills_root)
        subprocess.run(["git", "init"], cwd=repo, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        subprocess.run(["git", "add", "."], cwd=repo, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        subprocess.run(
            ["git", "-c", "user.name=e2e", "-c", "user.email=e2e@example.com", "commit", "-m", "e2e skill"],
            cwd=repo,
            check=True,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
        return repo

    def mcp_chain(self) -> None:
        self.require_auth()
        if self.config.skip_external:
            return
        raw_config = os.getenv("E2E_MCP_CONFIG")
        if not raw_config:
            raise BlockedByRepoPolicy("E2E_MCP_CONFIG is required for real MCP activation; no mock server is used")
        payload = {
            "name": f"{self.state.prefix}_mcp",
            "protocol": os.getenv("E2E_MCP_PROTOCOL", "STDIO"),
            "mode": os.getenv("E2E_MCP_MODE", "SYNC"),
            "timeout": int(os.getenv("E2E_MCP_TIMEOUT", "30")),
            "protocolConfig": json.loads(raw_config),
            "description": "created by real-chain e2e",
            "healthStatus": "UNKNOWN",
            "activationStatus": "NOT_ACTIVATED",
            "activationMessage": "",
            "lastActivationTime": None,
            "lastToolSyncTime": None,
            "toolCount": 0,
            "runtimeFailThreshold": 3,
            "needsSync": True,
            "enabled": True,
        }
        self.client.json("POST", "/mcp/server", payload)
        item_id = self.find_page_id("/mcp/server/page", {"name": payload["name"]})
        self.state.created["mcp_id"] = item_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/mcp/server", [item_id], expect_success=False))
        self.client.json("GET", f"/mcp/server/{item_id}")
        activated = self.client.json("POST", f"/mcp/server/{item_id}/activate")
        data = self.require_data(activated, "mcp-activate")
        if data.get("activationStatus") != "ACTIVE":
            raise E2EError(f"MCP activation did not become ACTIVE: {data}")
        self.client.json("POST", f"/mcp/server/{item_id}/sync-tools")
        tools = self.require_data(self.client.json("GET", f"/mcp/server/{item_id}/tools"), "mcp-tools")
        if tools:
            self.client.json(
                "PUT",
                f"/mcp/server/{item_id}/tools/global-enabled",
                {"toolIds": [tools[0]["id"]], "enabled": True},
            )
        self.client.json("POST", "/mcp/server/used-with-agent", [item_id])
