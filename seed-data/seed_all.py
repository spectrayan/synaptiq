"""
Synaptiq Master Seed Script
============================
Runs all seed scripts in order to fully populate the database.

Usage:
    python seed-data/seed_all.py
"""
import asyncio
import logging
import sys
import os

# Ensure seed-data directory is on the path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


async def main():
    logger.info("=" * 60)
    logger.info("🌱 Synaptiq Full Database Seed")
    logger.info("=" * 60)

    # 0. Default users (admin@synaptiq.dev / admin)
    logger.info("\n👤 Phase 0: Users...")
    from seed_users import seed as seed_users
    await seed_users()

    # 1. E2E business data (tenant, catalog, orders, sales, tasks, etc.)
    logger.info("\n📦 Phase 1: Business data...")
    from seed_e2e_data import seed as seed_e2e
    await seed_e2e()

    # 2. Observability data (API metrics, infra, LLM, errors, SLOs, analytics)
    logger.info("\n🔭 Phase 2: Observability data...")
    from seed_observability import seed_observability
    await seed_observability()

    # 3. Schema registry (must run LAST — overwrites registry with complete list)
    logger.info("\n📋 Phase 3: Schema registry...")
    from seed_schema_registry import seed_schema_registry
    await seed_schema_registry()

    logger.info("\n" + "=" * 60)
    logger.info("🎉 All seeds complete!")
    logger.info("=" * 60)


if __name__ == "__main__":
    asyncio.run(main())
