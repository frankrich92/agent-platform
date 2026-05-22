from __future__ import annotations

import argparse
import json
import os
import shutil
import sys
import tempfile
import time
import traceback
import urllib.error
import urllib.parse
import urllib.request
import uuid
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Callable


class E2EError(RuntimeError):
    pass


class BlockedByRepoPolicy(RuntimeError):
    pass


@dataclass
class Config:
    base_url: str
    ui_url: str
    username: str
    password: str
    api_prefix: str
    timeout: int
    skip_external: bool = False
    stop_on_fail: bool = False
    show_response: bool = False
    use_proxy: bool = False


@dataclass
class RunState:
    token: str | None = None
    refresh_token: str | None = None
    user_id: str | None = None
    prefix: str = field(default_factory=lambda: f"e2e_{int(time.time())}_{uuid.uuid4().hex[:6]}")
    temp_dir: Path = field(default_factory=lambda: Path(tempfile.mkdtemp(prefix="agent-platform-e2e-")))
    created: dict[str, str] = field(default_factory=dict)
    cleanups: list[Callable[[], None]] = field(default_factory=list)


class Reporter:
    def __init__(self, stop_on_fail: bool) -> None:
        self.stop_on_fail = stop_on_fail
        self.passed: list[str] = []
        self.failed: list[tuple[str, str]] = []
        self.blocked: list[tuple[str, str]] = []

    def step(self, name: str, fn: Callable[[], None]) -> str:
        started = time.time()
        try:
            fn()
            self.passed.append(name)
            print(f"PASS {name} ({time.time() - started:.2f}s)")
            return "passed"
        except BlockedByRepoPolicy as exc:
            self.blocked.append((name, str(exc)))
            print(f"BLOCKED {name}: {exc}")
            return "blocked"
        except Exception as exc:
            message = f"{type(exc).__name__}: {exc}"
            self.failed.append((name, message))
            print(f"FAIL {name}: {message}")
            if os.getenv("E2E_DEBUG"):
                traceback.print_exc()
            if self.stop_on_fail:
                raise
            return "failed"

    def summary(self) -> int:
        print()
        print("E2E summary")
        print(f"  passed : {len(self.passed)}")
        print(f"  failed : {len(self.failed)}")
        print(f"  blocked: {len(self.blocked)}")
        if self.failed:
            print()
            print("Failures:")
            for name, message in self.failed:
                print(f"  - {name}: {message}")
        if self.blocked:
            print()
            print("Blocked/skipped:")
            for name, message in self.blocked:
                print(f"  - {name}: {message}")
        return 1 if self.failed else 0


class ApiClient:
    def __init__(self, config: Config, state: RunState) -> None:
        self.config = config
        self.state = state
        if config.use_proxy:
            self.opener = urllib.request.build_opener()
        else:
            self.opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))

    def set_token(self, token: str | None) -> None:
        self.state.token = token

    def json(
        self,
        method: str,
        path: str,
        body: Any | None = None,
        params: dict[str, Any] | None = None,
        *,
        expect_success: bool = True,
        api: bool = True,
        headers: dict[str, str] | None = None,
    ) -> dict[str, Any]:
        data = None if body is None else json.dumps(body).encode("utf-8")
        request_headers = {"Accept": "application/json"}
        if body is not None:
            request_headers["Content-Type"] = "application/json"
        if self.state.token:
            request_headers["Authorization"] = f"Bearer {self.state.token}"
        if headers:
            request_headers.update(headers)
        req = urllib.request.Request(
            self._url(path, params, api=api),
            data=data,
            headers=request_headers,
            method=method.upper(),
        )
        raw = self._open(req)
        try:
            payload = json.loads(raw.decode("utf-8"))
        except json.JSONDecodeError as exc:
            detail = f": {raw[:300]!r}" if self.config.show_response else ""
            raise E2EError(f"response is not JSON for {method} {path}{detail}") from exc
        self._verbose_json(f"{method.upper()} {path}", payload)
        if expect_success and not self.is_success(payload):
            raise E2EError(self._failure_message(method, path, payload))
        return payload

    def upload(
        self,
        path: str,
        fields: dict[str, Any],
        files: dict[str, tuple[str, bytes, str]],
        *,
        expect_success: bool = True,
    ) -> dict[str, Any]:
        boundary = "----agentPlatformE2E" + uuid.uuid4().hex
        body = bytearray()
        for name, value in fields.items():
            body.extend(f"--{boundary}\r\n".encode())
            body.extend(f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode())
            body.extend(str(value).encode())
            body.extend(b"\r\n")
        for name, (filename, content, content_type) in files.items():
            body.extend(f"--{boundary}\r\n".encode())
            body.extend(f'Content-Disposition: form-data; name="{name}"; filename="{filename}"\r\n'.encode())
            body.extend(f"Content-Type: {content_type}\r\n\r\n".encode())
            body.extend(content)
            body.extend(b"\r\n")
        body.extend(f"--{boundary}--\r\n".encode())
        headers = {
            "Accept": "application/json",
            "Content-Type": f"multipart/form-data; boundary={boundary}",
        }
        if self.state.token:
            headers["Authorization"] = f"Bearer {self.state.token}"
        req = urllib.request.Request(self._url(path), data=bytes(body), headers=headers, method="POST")
        payload = json.loads(self._open(req).decode("utf-8"))
        self._verbose_json(f"POST {path}", payload)
        if expect_success and not self.is_success(payload):
            raise E2EError(self._failure_message("POST", path, payload))
        return payload

    def download(
        self,
        method: str,
        path: str,
        body: Any | None = None,
        params: dict[str, Any] | None = None,
    ) -> bytes:
        data = None if body is None else json.dumps(body).encode("utf-8")
        headers = {"Accept": "*/*"}
        if body is not None:
            headers["Content-Type"] = "application/json"
        if self.state.token:
            headers["Authorization"] = f"Bearer {self.state.token}"
        req = urllib.request.Request(self._url(path, params), data=data, headers=headers, method=method.upper())
        raw = self._open(req)
        self._verbose_text(f"{method.upper()} {path}", f"<binary {len(raw)} bytes>")
        return raw

    def smoke_get(self, url: str) -> bytes:
        raw = self._open(urllib.request.Request(url, headers={"Accept": "text/html,*/*"}, method="GET"))
        self._verbose_text(f"GET {url}", f"<html/text {len(raw)} bytes>")
        return raw

    def _url(self, path: str, params: dict[str, Any] | None = None, *, api: bool = True) -> str:
        if path.startswith("http://") or path.startswith("https://"):
            url = path
        else:
            prefix = self.config.api_prefix if api else ""
            url = self.config.base_url.rstrip("/") + prefix.rstrip("/") + "/" + path.lstrip("/")
        if params:
            clean = {k: v for k, v in params.items() if v is not None}
            if clean:
                url += "?" + urllib.parse.urlencode(clean, doseq=True)
        return url

    def _open(self, req: urllib.request.Request) -> bytes:
        try:
            with self.opener.open(req, timeout=self.config.timeout) as resp:
                status = resp.getcode()
                raw = resp.read()
        except urllib.error.HTTPError as exc:
            raw = exc.read()
            detail = f": {raw[:500]!r}" if self.config.show_response else ""
            raise E2EError(f"HTTP {exc.code} {req.full_url}{detail}") from exc
        except urllib.error.URLError as exc:
            raise E2EError(f"cannot connect to {req.full_url}: {exc}") from exc
        if status < 200 or status >= 300:
            raise E2EError(f"HTTP {status} {req.full_url}")
        return raw

    @staticmethod
    def is_success(payload: dict[str, Any]) -> bool:
        return payload.get("success") is True and int(payload.get("code", 0)) == 200

    def _verbose_json(self, label: str, payload: Any) -> None:
        if not self.config.show_response:
            return
        redacted = redact_secrets(payload)
        text = json.dumps(redacted, ensure_ascii=False, indent=2, default=str)
        max_chars = int(os.getenv("E2E_VERBOSE_MAX_CHARS", "12000"))
        if len(text) > max_chars:
            text = text[:max_chars] + "\n... <truncated>"
        print()
        print(f"[E2E_RESPONSE] {label}")
        print(text)

    def _verbose_text(self, label: str, text: str) -> None:
        if not self.config.show_response:
            return
        print()
        print(f"[E2E_RESPONSE] {label}")
        print(text)

    def _failure_message(self, method: str, path: str, payload: dict[str, Any]) -> str:
        if self.config.show_response:
            return f"{method} {path} failed: {json.dumps(redact_secrets(payload), ensure_ascii=False)}"
        code = payload.get("code", "unknown")
        msg = payload.get("msg", "request failed")
        return f"{method} {path} failed: code={code}, msg={msg}; use --show-response to print response body"


class Module:
    def __init__(self, config: Config, state: RunState, client: ApiClient) -> None:
        self.config = config
        self.state = state
        self.client = client

    def steps(self) -> list[tuple[str, Callable[[], None]]]:
        return []

    def add_cleanup(self, fn: Callable[[], None]) -> None:
        self.state.cleanups.append(fn)

    def require_auth(self) -> None:
        if not self.state.token:
            raise BlockedByRepoPolicy("auth/login did not complete; dependent API chain skipped")

    def require_created(self, *keys: str) -> None:
        missing = [key for key in keys if key not in self.state.created]
        if missing:
            raise BlockedByRepoPolicy(f"missing prerequisite fixture(s): {', '.join(missing)}")

    def require_data(self, response: dict[str, Any], label: str) -> Any:
        if not ApiClient.is_success(response):
            if self.config.show_response:
                detail = json.dumps(redact_secrets(response), ensure_ascii=False)
                raise E2EError(f"{label} failed: {detail}")
            raise E2EError(f"{label} failed; use --show-response to print response body")
        return response.get("data")

    def find_page_id(self, path: str, params: dict[str, Any]) -> str:
        page_params = {"page": 1, "size": 20}
        page_params.update(params)
        data = self.require_data(self.client.json("GET", path, params=page_params), path)
        records = data.get("records", data if isinstance(data, list) else [])
        key, value = next(iter(params.items()))
        return self.find_in_records(records, key, value, show_response=self.config.show_response)

    @staticmethod
    def find_in_records(records: list[dict[str, Any]], key: str, value: Any, *, show_response: bool = False) -> str:
        for record in records:
            if str(record.get(key)) == str(value):
                return str(record["id"])
        if show_response:
            raise E2EError(f"cannot find record where {key}={value!r}; records={redact_secrets(records)}")
        raise E2EError(
            f"cannot find record where {key}={value!r}; matched 0 of {len(records)} record(s). "
            "Use --show-response to print records."
        )


def parse_args() -> Config:
    parser = argparse.ArgumentParser(description="Run real-chain E2E checks against agent-platform.")
    parser.add_argument("--base-url", default=os.getenv("E2E_BASE_URL", "http://127.0.0.1:3060"))
    parser.add_argument("--ui-url", default=os.getenv("E2E_UI_URL", "http://127.0.0.1:3001"))
    parser.add_argument("--username", default=os.getenv("E2E_USERNAME", "admin"))
    parser.add_argument("--password", default=os.getenv("E2E_PASSWORD", "Admin@123.com"))
    parser.add_argument("--api-prefix", default=os.getenv("E2E_API_PREFIX", "/api"))
    parser.add_argument("--timeout", type=int, default=int(os.getenv("E2E_TIMEOUT", "30")))
    parser.add_argument("--skip-external", action="store_true", help="Skip real external MCP and RAG dependency checks.")
    parser.add_argument("--stop-on-fail", action="store_true")
    parser.add_argument(
        "--show-response",
        action="store_true",
        help="Print redacted API response content. Disabled by default.",
    )
    parser.add_argument(
        "--hide-response",
        action="store_true",
        default=os.getenv("E2E_HIDE_RESPONSE") == "1",
        help="Force response content output off, even when E2E_SHOW_RESPONSE=1 is set.",
    )
    parser.add_argument(
        "--verbose",
        action="store_true",
        help="Backward-compatible alias for --show-response.",
    )
    parser.add_argument("--use-proxy", action="store_true", default=os.getenv("E2E_USE_PROXY") == "1")
    parser.add_argument("--print-env", action="store_true")
    args = parser.parse_args()
    show_response = os.getenv("E2E_SHOW_RESPONSE") == "1" or args.show_response or args.verbose
    if args.hide_response:
        show_response = False
    config = Config(
        base_url=args.base_url,
        ui_url=args.ui_url,
        username=args.username,
        password=args.password,
        api_prefix=args.api_prefix,
        timeout=args.timeout,
        skip_external=args.skip_external,
        stop_on_fail=args.stop_on_fail,
        show_response=show_response,
        use_proxy=args.use_proxy,
    )
    if args.print_env:
        print(json.dumps(config.__dict__, indent=2, ensure_ascii=False))
        sys.exit(0)
    return config


def cleanup_temp(state: RunState) -> None:
    shutil.rmtree(state.temp_dir, ignore_errors=True)


def redact_secrets(value: Any) -> Any:
    secret_keys = {
        "accessToken",
        "refreshToken",
        "password",
        "oldPassword",
        "newPassword",
        "apiKey",
        "authorization",
        "Authorization",
        "token",
    }
    if isinstance(value, dict):
        redacted: dict[str, Any] = {}
        for key, item in value.items():
            if key in secret_keys:
                redacted[key] = "<redacted>"
            elif key == "value" and any(secret_hint in str(value.get("name", "")).lower() for secret_hint in ("sk", "key", "token")):
                redacted[key] = "<redacted>"
            else:
                redacted[key] = redact_secrets(item)
        return redacted
    if isinstance(value, list):
        return [redact_secrets(item) for item in value]
    return value
