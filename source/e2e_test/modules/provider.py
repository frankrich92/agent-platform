from __future__ import annotations

import os

from common import Module


class ProviderModule(Module):
    def steps(self):
        return [("provider/model provider and config", self.model_chain)]

    def model_chain(self) -> None:
        self.require_auth()
        provider = {
            "type": os.getenv("E2E_MODEL_PROVIDER_TYPE", "OPEN_AI"),
            "name": f"{self.state.prefix}_provider",
            "description": "created by real-chain e2e",
            "baseUrl": os.getenv("E2E_MODEL_BASE_URL", "https://api.openai.com/v1"),
            "authType": "CONFIG",
            "apiKey": os.getenv("E2E_MODEL_API_KEY", "e2e-placeholder-api-key"),
            "envVarName": "",
            "configMeta": {},
            "enabled": True,
        }
        self.client.json("POST", "/model/provider", provider)
        provider_id = self.find_page_id("/model/provider/page", {"name": provider["name"]})
        self.state.created["provider_id"] = provider_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/model/provider", [provider_id], expect_success=False))
        self.client.json("GET", f"/model/provider/{provider_id}")
        provider["id"] = provider_id
        provider["description"] = "updated by real-chain e2e"
        self.client.json("PUT", "/model/provider", provider)
        self.client.json("POST", "/model/provider/used-with-model", [provider_id])

        config = {
            "providerId": provider_id,
            "name": f"{self.state.prefix}_model",
            "modelId": os.getenv("E2E_MODEL_ID", "gpt-4o-mini"),
            "modelType": ["CHAT"],
            "description": "created by real-chain e2e",
            "streaming": True,
            "thinking": False,
            "contextWindow": 8192,
            "maxTokens": 1024,
            "temperature": 0.2,
            "topP": 0.9,
            "topK": 50,
            "repeatPenalty": 1.0,
            "seed": "",
            "extendConfig": {},
            "enabled": True,
        }
        self.client.json("POST", "/model/config", config)
        config_id = self.find_page_id("/model/config/page", {"name": config["name"]})
        self.state.created["model_config_id"] = config_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/model/config", [config_id], expect_success=False))
        self.client.json("GET", f"/model/config/{config_id}")
        config["id"] = config_id
        config["description"] = "updated by real-chain e2e"
        self.client.json("PUT", "/model/config", config)
        self.client.json("POST", "/model/config/used-with-agent", [config_id])
