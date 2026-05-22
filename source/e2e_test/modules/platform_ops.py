from __future__ import annotations

import json

from common import Module


class PlatformOpsModule(Module):
    def steps(self):
        return [
            ("iam/secret key", self.secret_key_chain),
            ("files/storage protocol", self.storage_chain),
            ("system/params", self.params_chain),
            ("task/job scheduler", self.job_chain),
            ("cleanup/created data", self.cleanup),
        ]

    def secret_key_chain(self) -> None:
        self.require_auth()
        created = self.client.json(
            "POST",
            "/sk",
            {"name": f"{self.state.prefix}_sk", "remark": "created by real-chain e2e", "enabled": True},
        )
        data = self.require_data(created, "sk-create")
        item_id = str(data["id"])
        self.state.created["secret_key_id"] = item_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/sk", [item_id], expect_success=False))
        self.client.json("GET", "/sk/list")
        self.client.json("PUT", "/sk", {"id": item_id, "name": f"{self.state.prefix}_sk_updated"})

    def storage_chain(self) -> None:
        self.require_auth()
        payload = {
            "name": f"{self.state.prefix}_storage",
            "protocol": "LOCAL",
            "protocolConfig": json.dumps({"localDir": str(self.state.temp_dir / "storage")}),
            "remark": "created by real-chain e2e",
            "valid": 0,
        }
        self.client.json("POST", "/storage/add", payload)
        item_id = self.find_page_id("/storage/page", {"name": payload["name"]})
        self.state.created["storage_id"] = item_id
        self.add_cleanup(lambda: self.client.json("POST", "/storage/delete", [item_id], expect_success=False))
        self.client.json("GET", "/storage/selectOne", params={"id": item_id})
        payload["id"] = item_id
        payload["remark"] = "updated by real-chain e2e"
        self.client.json("POST", "/storage/update", payload)
        self.client.json(
            "POST",
            "/storage/updateProtocol",
            {"id": item_id, "protocolConfig": json.dumps({"localDir": str(self.state.temp_dir / "storage2")})},
        )
        self.client.json("GET", "/storage/validSuccess", params={"id": item_id})

    def params_chain(self) -> None:
        self.require_auth()
        payload = {
            "paramName": f"{self.state.prefix}_param",
            "paramKey": f"{self.state.prefix}.param",
            "paramValue": "value1",
        }
        self.client.json("POST", "/params/add", payload)
        item_id = self.find_page_id("/params/page", {"paramKey": payload["paramKey"]})
        self.state.created["param_id"] = item_id
        self.add_cleanup(lambda: self.client.json("POST", "/params/delete", [item_id], expect_success=False))
        self.client.json("GET", f"/params/{item_id}")
        payload["id"] = item_id
        payload["paramValue"] = "value2"
        self.client.json("POST", "/params/update", payload)
        self.client.json("GET", "/params/fetch-value-by-key", params={"key": payload["paramKey"]})

    def job_chain(self) -> None:
        self.require_auth()
        self.require_created("agent_id")
        agent_id = self.state.created["agent_id"]
        payload = {
            "type": "AGENT",
            "bizId": agent_id,
            "cron": "0 0 0 1 1 ? 2099",
            "jobClass": "com.htam.agent.job.scheduler.AgentScheduler",
            "dataMap": json.dumps({"agentId": agent_id, "input": "hello from e2e"}),
            "enabled": False,
        }
        self.client.json("POST", "/job/add", payload)
        job = self.require_data(self.client.json("GET", "/job/getByBizId", params={"bizId": agent_id}), "job-get")
        item_id = str(job["id"])
        self.state.created["job_id"] = item_id
        self.add_cleanup(lambda: self.client.json("GET", "/job/delete", params={"id": item_id}, expect_success=False))
        self.client.json("GET", "/job/list")
        payload["id"] = item_id
        payload["cron"] = "0 0 1 1 1 ? 2099"
        self.client.json("POST", "/job/update", payload)
        self.client.json("GET", "/job/updateCron", params={"id": item_id, "cron": "0 0 2 1 1 ? 2099"})
        self.client.json("GET", "/job/start", params={"id": item_id})
        self.client.json("GET", "/job/stop", params={"id": item_id})
        self.client.json("GET", "/job/deleteByBizId", params={"bizId": agent_id})

    def cleanup(self) -> None:
        for clean in reversed(self.state.cleanups):
            try:
                clean()
            except Exception as exc:
                print(f"WARN cleanup failed: {exc}")
