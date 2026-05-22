from __future__ import annotations

import hashlib

from common import BlockedByRepoPolicy, E2EError, Module


class AuthIamModule(Module):
    def steps(self):
        return [
            ("auth/frontend smoke", self.frontend_smoke),
            ("auth/login and refresh", self.login_refresh),
            ("iam/account management", self.account_management),
        ]

    def frontend_smoke(self) -> None:
        if not self.config.ui_url:
            return
        try:
            raw = self.client.smoke_get(self.config.ui_url)
        except E2EError as exc:
            if str(exc).startswith("cannot connect to "):
                raise BlockedByRepoPolicy(f"frontend is not reachable at {self.config.ui_url}") from exc
            raise
        if b"<html" not in raw.lower() and b"<!doctype" not in raw.lower():
            raise E2EError("frontend response is not an HTML document")

    def login_refresh(self) -> None:
        try:
            response = self.client.json(
                "POST",
                "/auth/login",
                {"username": self.config.username, "password": md5(self.config.password)},
                headers={"token": "false"},
            )
        except E2EError as exc:
            if str(exc).startswith("cannot connect to "):
                raise BlockedByRepoPolicy(f"backend is not reachable at {self.config.base_url}") from exc
            raise
        data = self.require_data(response, "login")
        self.client.set_token(data["accessToken"])
        self.state.refresh_token = data["refreshToken"]
        self.state.user_id = str(data["userDetail"]["id"])

        refreshed = self.client.json(
            "POST",
            "/auth/refresh-token",
            {"refreshToken": self.state.refresh_token},
            headers={"refreshTokenRequest": "true", "token": "false"},
        )
        self.client.set_token(self.require_data(refreshed, "refresh-token")["accessToken"])

    def account_management(self) -> None:
        self.require_auth()
        suffix = self.state.prefix[-8:]
        payload = {
            "nickname": f"e2e{suffix}"[:10],
            "username": f"{suffix}_user",
            "email": f"{suffix}@example.test",
            "password": md5("E2ePass!123"),
        }
        self.client.json("POST", "/auth/admin/create-account", payload)
        account_id = self.find_account_id(payload["username"])
        self.state.created["account_id"] = account_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/account", [account_id], expect_success=False))

        self.client.json("GET", f"/account/{account_id}")
        self.client.json("PUT", f"/account/{account_id}/change-role", ["READ_ONLY"])
        self.client.json("PUT", f"/account/{account_id}/toggle-enabled", params={"enabled": "false"})
        self.client.json("PUT", f"/account/{account_id}/toggle-enabled", params={"enabled": "true"})
        self.client.json("PUT", f"/account/{account_id}/change-password", params={"newPassword": md5("E2ePass!456")})
        self.client.json(
            "POST",
            "/auth/update-profile",
            {"nickname": "e2eadmin", "email": f"{suffix}_profile@example.test"},
        )
        self.client.json("GET", "/account/list", params={"username": payload["username"]})

    def find_account_id(self, username: str) -> str:
        accounts = self.require_data(
            self.client.json("GET", "/account/list", params={"username": username}),
            "account-list",
        )
        return self.find_in_records(accounts, "username", username)


def md5(value: str) -> str:
    return hashlib.md5(value.encode("utf-8")).hexdigest()
