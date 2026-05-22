from __future__ import annotations

import hashlib
import json
import os
import time
from typing import Any

from common import BlockedByRepoPolicy, E2EError, Module


class FilesRagModule(Module):
    def steps(self):
        return [
            ("knowledge/config", self.knowledge_chain),
            ("files/attachment upload and download", self.attach_chain),
            ("rag/document real processing", self.rag_chain),
        ]

    def knowledge_chain(self) -> None:
        self.require_auth()
        payload = self.knowledge_payload(f"{self.state.prefix}_kb_crud")
        self.client.json("POST", "/knowledge/config", payload)
        item_id = self.find_page_id("/knowledge/config/page", {"name": payload["name"]})
        self.state.created["knowledge_id"] = item_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/knowledge/config", [item_id], expect_success=False))
        self.client.json("GET", f"/knowledge/config/{item_id}")
        payload["id"] = item_id
        payload["description"] = "updated by real-chain e2e"
        self.client.json("PUT", "/knowledge/config", payload)
        self.client.json("POST", "/knowledge/config/used-with-agent", [item_id])

    def attach_chain(self) -> None:
        self.require_auth()
        self.ensure_local_storage()
        content = b"agent-platform e2e attachment\n"
        uploaded = self.client.upload(
            "/attach/upload",
            fields={},
            files={"file": (f"{self.state.prefix}.txt", content, "text/plain")},
        )
        attach_id = str(self.require_data(uploaded, "attach-upload"))
        self.state.created["attach_id"] = attach_id
        self.add_cleanup(lambda: self.client.json("POST", "/attach/delete", [attach_id], expect_success=False))
        self.client.json("GET", "/attach/page", params={"page": 1, "size": 10})
        self.client.json("GET", "/attach/log/page", params={"page": 1, "size": 10})
        self.client.json("GET", "/attach/selectOne", params={"id": attach_id})
        self.client.json("POST", "/attach/list", [attach_id])
        if not self.client.download("GET", f"/attach/download/{attach_id}"):
            raise E2EError("downloaded attachment is empty")
        if not self.client.download("POST", "/attach/batchDownload", [attach_id]):
            raise E2EError("batch download is empty")

        chunk_content = b"chunked upload e2e\n"
        digest = hashlib.md5(chunk_content).hexdigest()
        chunked = self.client.upload(
            "/attach/chunk-upload",
            fields={
                "hash": digest,
                "totalSize": len(chunk_content),
                "index": 0,
                "totalChunks": 1,
                "uniqueKey": f"{self.state.prefix}_{digest}",
                "fileName": f"{self.state.prefix}_chunk.txt",
            },
            files={"file": (f"{self.state.prefix}_chunk.txt", chunk_content, "text/plain")},
        )
        chunk_attach_id = str(self.require_data(chunked, "chunk-upload"))
        self.add_cleanup(lambda: self.client.json("POST", "/attach/delete", [chunk_attach_id], expect_success=False))

    def ensure_local_storage(self) -> None:
        payload = {
            "name": f"{self.state.prefix}_attach_storage",
            "protocol": "LOCAL",
            "protocolConfig": json.dumps({"localDir": str(self.state.temp_dir / "attach-storage")}),
            "remark": "created by real-chain e2e attachment fixture",
            "valid": 0,
        }
        self.client.json("POST", "/storage/add", payload)
        storage_id = self.find_page_id("/storage/page", {"name": payload["name"]})
        self.state.created["attach_storage_id"] = storage_id
        self.add_cleanup(lambda: self.client.json("POST", "/storage/delete", [storage_id], expect_success=False))
        self.client.json("GET", "/storage/validSuccess", params={"id": storage_id})

    def rag_chain(self) -> None:
        self.require_auth()
        if self.config.skip_external:
            return
        if "E2E_RAG_CONNECTION_CONFIG" not in os.environ:
            raise BlockedByRepoPolicy(
                "E2E_RAG_CONNECTION_CONFIG is required for real RAG document processing; no mock embedding service is used"
            )
        payload = self.knowledge_payload(f"{self.state.prefix}_rag")
        self.client.json("POST", "/knowledge/config", payload)
        kb_id = self.find_page_id("/knowledge/config/page", {"name": payload["name"]})
        self.state.created["rag_kb_id"] = kb_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/knowledge/config", [kb_id], expect_success=False))
        text = (
            "Agent platform E2E RAG document.\n"
            "This document verifies real parsing, chunking, embedding, vector storage, and retrieval.\n"
        ).encode()
        uploaded = self.client.upload(
            "/rag/document/upload",
            fields={"knowledgeBaseConfigId": kb_id},
            files={"file": (f"{self.state.prefix}_rag.txt", text, "text/plain")},
        )
        doc_id = str(self.require_data(uploaded, "rag-upload"))
        self.state.created["rag_doc_id"] = doc_id
        self.add_cleanup(lambda: self.client.json("DELETE", "/rag/document", [doc_id], expect_success=False))
        self.wait_for_rag_document(kb_id, doc_id)
        chunks = self.require_data(
            self.client.json("GET", "/rag/document/chunks", params={"documentId": doc_id}),
            "rag-chunks",
        )
        if not chunks:
            raise E2EError("RAG document completed but no chunks were created")
        chunk_id = str(chunks[0]["id"])
        self.client.json("PUT", f"/rag/document/chunk/{chunk_id}", {"content": chunks[0]["content"] + "\nupdated"})
        self.client.json(
            "POST",
            "/rag/document/search",
            {"knowledgeBaseConfigId": kb_id, "query": "real parsing vector retrieval", "limit": 3, "scoreThreshold": 0.0},
        )
        if not self.client.download("GET", f"/rag/document/download/{doc_id}"):
            raise E2EError("RAG document download is empty")
        reuploaded = self.client.upload(
            f"/rag/document/re-upload/{doc_id}",
            fields={},
            files={"file": (f"{self.state.prefix}_rag_reupload.txt", text + b"reupload\n", "text/plain")},
        )
        self.require_data(reuploaded, "rag-reupload")
        self.wait_for_rag_document(kb_id, doc_id)
        self.client.json("POST", f"/rag/document/re-chunk/{doc_id}")
        self.wait_for_rag_document(kb_id, doc_id)

    def knowledge_payload(self, name: str) -> dict[str, Any]:
        default_connection = {
            "providerType": "ollama",
            "baseUrl": "http://localhost:11434/api/embed",
            "embeddingModel": "nomic-embed-text",
            "batchSize": 10,
            "bufferSizeMb": 50,
        }
        connection_config = json.loads(os.getenv("E2E_RAG_CONNECTION_CONFIG", json.dumps(default_connection)))
        return {
            "name": name,
            "kbType": "LOCAL",
            "ragMode": "GENERIC",
            "description": "created by real-chain e2e",
            "connectionConfig": connection_config,
            "endpointConfig": {},
            "retrievalConfig": {"chunkSize": 128, "chunkOverlap": 0, "chunkDelimiters": "\\n"},
            "rerankingConfig": {},
            "queryRewriteConfig": {},
            "metadataFilters": {},
            "httpConfig": {},
            "healthStatus": "UNKNOWN",
            "lastSyncTime": None,
            "enabled": True,
        }

    def wait_for_rag_document(self, kb_id: str, doc_id: str) -> None:
        deadline = time.time() + int(os.getenv("E2E_RAG_TIMEOUT", "120"))
        last_doc = None
        while time.time() < deadline:
            docs = self.require_data(
                self.client.json("GET", "/rag/document/list", params={"knowledgeBaseConfigId": kb_id}),
                "rag-list",
            )
            for doc in docs:
                if str(doc["id"]) == str(doc_id):
                    last_doc = doc
                    if doc.get("status") == "COMPLETED":
                        return
                    if doc.get("status") == "FAILED":
                        raise E2EError(f"RAG document failed: {doc.get('errorMessage')}")
            time.sleep(2)
        raise E2EError(f"RAG document did not complete before timeout: {last_doc}")
