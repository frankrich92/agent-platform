#!/usr/bin/env python3
from __future__ import annotations

import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
SKIP_DIRS = {
    ".git",
    ".apboa",
    "node_modules",
    "target",
    "dist",
    "dist-main",
    "dist-doc",
    "dist-zip",
    "__pycache__",
}


@dataclass(frozen=True)
class Rule:
    name: str
    roots: tuple[Path, ...]
    pattern: re.Pattern[str]
    include_suffixes: tuple[str, ...] = ()


RULES = [
    Rule(
        name="old Java package",
        roots=(ROOT / "source", ROOT / "docs" / "once_db_init"),
        pattern=re.compile(r"com\.hxh\.apboa"),
    ),
    Rule(
        name="old runtime default path",
        roots=(ROOT / "source", ROOT / "docs" / "once_db_init"),
        pattern=re.compile(r"\.apboa/(skills|temp|workspace|workspaces)"),
    ),
    Rule(
        name="old platform artifact id",
        roots=(ROOT / "source" / "agent-platform",),
        pattern=re.compile(r"<artifactId>platform-(common|security|stream|capability|provider|files|observability|orchestration)</artifactId>"),
        include_suffixes=(".xml",),
    ),
    Rule(
        name="old platform module path",
        roots=(ROOT / "source" / "agent-platform",),
        pattern=re.compile(r"<module>agent-platform</module>"),
        include_suffixes=(".xml",),
    ),
]


def main() -> int:
    findings: list[str] = []
    for rule in RULES:
        for root in rule.roots:
            if not root.exists():
                continue
            for path in iter_files(root, rule.include_suffixes):
                findings.extend(scan_file(rule, path))

    apboa_status = subprocess.run(
        ["git", "-C", str(ROOT / ".apboa"), "status", "--short"],
        check=False,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
    )
    if apboa_status.returncode != 0:
        findings.append(f".apboa status check failed: {apboa_status.stdout.strip()}")
    elif apboa_status.stdout.strip():
        findings.append(".apboa working tree is not clean:\n" + apboa_status.stdout.rstrip())

    if findings:
        print("Migration residue check failed:")
        for finding in findings:
            print(f"  - {finding}")
        return 1

    print("Migration residue check passed.")
    return 0


def iter_files(root: Path, suffixes: tuple[str, ...]):
    if root.is_file():
        if should_include(root, suffixes):
            yield root
        return
    for path in root.rglob("*"):
        if any(part in SKIP_DIRS for part in path.relative_to(ROOT).parts):
            continue
        if path.is_file() and should_include(path, suffixes):
            yield path


def should_include(path: Path, suffixes: tuple[str, ...]) -> bool:
    if suffixes and path.suffix not in suffixes:
        return False
    return path.suffix.lower() in {
        ".java",
        ".xml",
        ".yml",
        ".yaml",
        ".ts",
        ".tsx",
        ".vue",
        ".scss",
        ".md",
        ".py",
        ".sql",
    }


def scan_file(rule: Rule, path: Path) -> list[str]:
    try:
        text = path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return []
    hits = []
    for lineno, line in enumerate(text.splitlines(), 1):
        if rule.pattern.search(line):
            hits.append(f"{rule.name}: {path.relative_to(ROOT)}:{lineno}")
    return hits


if __name__ == "__main__":
    raise SystemExit(main())
