"""
Seed Knowledge Base categories and documents via the backend API.

Creates categories, then uploads sample .md files from the kb/ folder through the
POST /api/v1/kb/documents endpoint, triggering the full ingestion pipeline
(Tika → TokenTextSplitter → Ollama → VectorStore).

Usage:
    python seed-data/seed_knowledge_base.py

Prerequisites:
    pip install requests
    Backend must be running (mvn spring-boot:run -Dspring-boot.run.profiles=dev)
    Ollama must be running with the configured embedding model
"""
import logging
import os
from pathlib import Path

import requests

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

API_BASE = os.environ.get("SYNAPTIQ_API_BASE", "http://localhost:8080")
TENANT_ID = os.environ.get("TENANT_ID", "default")

# Categories to create
SEED_CATEGORIES = [
    {
        "id": "cat-hr-policy",
        "name": "HR Policy",
        "description": "Human resources policies, employee handbook, and benefits information",
    },
    {
        "id": "cat-eng-docs",
        "name": "Engineering Docs",
        "description": "Technical documentation, architecture decisions, and coding standards",
    },
    {
        "id": "cat-product-specs",
        "name": "Product Specs",
        "description": "Product requirements, feature specs, and roadmap documents",
    },
]

# Map each file to its category and tags
SEED_DOCUMENTS = [
    {
        "file": "PTO_Policy_2026.md",
        "categoryId": "cat-hr-policy",
        "tags": ["pto", "leave", "vacation"],
    },
    {
        "file": "Employee_Handbook_v3.md",
        "categoryId": "cat-hr-policy",
        "tags": ["handbook", "onboarding"],
    },
    {
        "file": "Architecture_Decision_Records.md",
        "categoryId": "cat-eng-docs",
        "tags": ["adr", "architecture"],
    },
    {
        "file": "Coding_Standards.md",
        "categoryId": "cat-eng-docs",
        "tags": ["standards", "java", "style"],
    },
    {
        "file": "Q3_2026_Roadmap.md",
        "categoryId": "cat-product-specs",
        "tags": ["roadmap", "q3", "planning"],
    },
]


def seed_sync():
    """Create categories and upload sample KB documents via the backend API."""
    logger.info("📚 Seeding Knowledge Base (categories + documents)...")

    kb_dir = Path(__file__).parent / "kb"
    if not kb_dir.exists():
        logger.error("❌ KB seed directory not found: %s", kb_dir)
        return

    headers = {"X-Tenant-ID": TENANT_ID}

    session = requests.Session()
    session.headers.update(headers)

    # 1. Check backend is reachable
    try:
        status_resp = session.get(f"{API_BASE}/api/v1/kb/status", timeout=10)
        status_resp.raise_for_status()
        status = status_resp.json()
        existing_cats = status.get("totalCategories", 0)
        existing_docs = status.get("totalDocuments", 0)
        logger.info(
            "  📊 Current KB status: %d docs, %d categories",
            existing_docs, existing_cats,
        )
        if existing_docs > 0:
            logger.info("  ⏭ KB already has documents — skipping seed (idempotent)")
            return
    except requests.RequestException as e:
        logger.error("❌ Backend not reachable at %s: %s", API_BASE, e)
        logger.info("   Make sure the backend is running with dev profile")
        return

    # 2. Create categories (if none exist)
    if existing_cats == 0:
        logger.info("  📁 Creating %d categories...", len(SEED_CATEGORIES))
        for cat in SEED_CATEGORIES:
            try:
                resp = session.post(
                    f"{API_BASE}/api/v1/kb/categories",
                    json={"name": cat["name"], "description": cat["description"]},
                    timeout=10,
                )
                if resp.status_code == 200:
                    result = resp.json()
                    logger.info("    ✅ Category: %s (id=%s)", cat["name"], result.get("id", "?"))
                else:
                    logger.warning(
                        "    ⚠️  Category '%s' failed (%d): %s",
                        cat["name"], resp.status_code, resp.text[:200],
                    )
            except requests.RequestException as e:
                logger.warning("    ⚠️  Category error for '%s': %s", cat["name"], e)
    else:
        logger.info("  ⏭ %d categories already exist — skipping category creation", existing_cats)

    # 3. Upload each document
    upload_url = f"{API_BASE}/api/v1/kb/documents"
    uploaded = 0

    # Re-fetch categories to get their actual IDs
    status_resp = session.get(f"{API_BASE}/api/v1/kb/status", timeout=10)
    cats = {c["name"]: c["id"] for c in status_resp.json().get("categories", [])}

    for doc_spec in SEED_DOCUMENTS:
        file_path = kb_dir / doc_spec["file"]
        if not file_path.exists():
            logger.warning("  ⏭ File not found: %s — skipping", file_path)
            continue

        # Resolve category ID from name or use configured ID
        category_id = doc_spec["categoryId"]
        logger.info("  📄 Uploading: %s → %s", doc_spec["file"], category_id)

        try:
            with open(file_path, "rb") as f:
                files_data = {"file": (doc_spec["file"], f, "text/markdown")}
                form_data = {"categoryId": category_id}

                url = upload_url
                if doc_spec["tags"]:
                    qs = "&".join(f"tags={t}" for t in doc_spec["tags"])
                    url = f"{upload_url}?{qs}"

                resp = session.post(url, files=files_data, data=form_data, timeout=120)

            if resp.status_code == 200:
                result = resp.json()
                logger.info(
                    "    ✅ Uploaded: id=%s, status=%s",
                    result.get("id", "?"),
                    result.get("status", "?"),
                )
                uploaded += 1
            else:
                logger.warning(
                    "    ⚠️  Upload failed (%d): %s",
                    resp.status_code,
                    resp.text[:200],
                )
        except requests.RequestException as e:
            logger.warning("    ⚠️  Upload error for %s: %s", doc_spec["file"], e)

    logger.info("📚 KB seed complete: %d/%d documents uploaded", uploaded, len(SEED_DOCUMENTS))


# Async wrapper for compatibility with seed_all.py
async def seed():
    """Async wrapper — delegates to synchronous implementation."""
    seed_sync()


if __name__ == "__main__":
    seed_sync()
