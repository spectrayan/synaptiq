"""
Synaptiq E2E Seed Script — WRAPPER
===================================
This script has been moved to seed-data/seed_e2e_data.py.
This wrapper preserves backward compatibility for existing imports.

Usage:
    python -m synaptiq_api.scripts.seed_e2e_data
    # OR (preferred):
    python seed-data/seed_e2e_data.py
"""
import asyncio
import sys
import os

# Add seed-data directory to path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "..", "..", "..", "..", "..", "seed-data"))

from seed_e2e_data import seed  # noqa: E402

if __name__ == "__main__":
    asyncio.run(seed())
