"""
Synaptiq Seed: Observability Data
=================================
Generates realistic observability data across 6 collections:
  - api_metrics       (5-min intervals, 7 days)
  - infra_metrics     (1-min intervals, 24 hours)
  - llm_metrics       (5-min intervals, 7 days)
  - error_logs        (~200 entries, 7 days)
  - slo_metrics       (daily, 90 days)
  - user_analytics    (hourly, 7 days)

Data follows realistic patterns:
  - Diurnal traffic cycles (peak 10am-2pm)
  - Weekend dip in traffic
  - Log-normal latency distributions
  - Correlated metrics (high traffic → higher latency)
  - 2-3 anomaly spikes per week

Usage:
    python seed-data/seed_observability.py
"""
import asyncio
import logging
import math
import os
import random
from datetime import datetime, timedelta, timezone

from motor.motor_asyncio import AsyncIOMotorClient

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

MONGODB_URI = os.environ.get("MONGODB_URI", "mongodb://localhost:27017/?directConnection=true")
DB_NAME = "synaptiq"
TENANT_ID = "demo-tenant"


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _diurnal_factor(hour: int) -> float:
    """Simulate realistic traffic pattern: peaks at 10am and 2pm, low at night."""
    return 0.15 + 0.85 * max(0, math.sin(math.pi * (hour - 6) / 12)) if 6 <= hour <= 22 else 0.08 + 0.07 * random.random()


def _weekend_factor(weekday: int) -> float:
    """Lower traffic on weekends."""
    return 0.45 if weekday >= 5 else 1.0


def _jitter(base: float, pct: float = 0.15) -> float:
    """Add random jitter ±pct%."""
    return base * (1 + random.uniform(-pct, pct))


def _lognormal_latency(median: float, sigma: float = 0.5) -> float:
    """Log-normal latency distribution."""
    return max(1, random.lognormvariate(math.log(median), sigma))


def _should_spike(ts: datetime) -> bool:
    """Create 2-3 anomaly spikes per week (deterministic from timestamp)."""
    seed = int(ts.timestamp()) // 3600
    random.seed(seed)
    result = random.random() < 0.018  # ~3 spikes per week at hourly granularity
    random.seed()  # Reset
    return result


# ---------------------------------------------------------------------------
# Generators
# ---------------------------------------------------------------------------

ENDPOINTS = [
    ("/api/v1/chat/message", "POST", 280, 0.7),         # (base_rps, base_err_rate%)
    ("/api/v1/catalog/items", "GET", 150, 0.3),
    ("/api/v1/actions/execute", "POST", 40, 0.5),
    ("/api/v1/config/branding/public", "GET", 200, 0.1),
    ("/api/v1/chat/sessions", "GET", 80, 0.2),
]


async def seed_api_metrics(db) -> int:
    """5-minute interval API endpoint metrics over 7 days."""
    coll = db["api_metrics"]
    await coll.delete_many({"tenant_id": TENANT_ID})

    now = datetime.now(timezone.utc)
    docs = []

    for minutes_ago in range(0, 7 * 24 * 60, 5):  # 7 days, 5-min intervals
        ts = now - timedelta(minutes=minutes_ago)
        hour = ts.hour
        weekday = ts.weekday()
        traffic_mult = _diurnal_factor(hour) * _weekend_factor(weekday)
        is_spike = _should_spike(ts)

        for endpoint, method, base_rps, base_err in ENDPOINTS:
            rps_5min = max(1, int(_jitter(base_rps * traffic_mult * 5, 0.2)))
            if is_spike and endpoint == "/api/v1/chat/message":
                rps_5min = int(rps_5min * random.uniform(2.5, 4.0))

            load_factor = rps_5min / (base_rps * 5) if base_rps > 0 else 1
            base_latency = 120 if "GET" in method else 350
            p50 = max(10, int(_lognormal_latency(base_latency * max(1, load_factor * 0.8))))
            p95 = max(p50 + 50, int(p50 * random.uniform(2.5, 4.5)))
            p99 = max(p95 + 100, int(p95 * random.uniform(1.5, 3.0)))
            avg_lat = int(p50 * random.uniform(1.1, 1.4))

            err_rate = base_err * random.uniform(0.5, 1.5)
            if is_spike:
                err_rate = min(15, err_rate * random.uniform(3, 8))
            errors = max(0, int(rps_5min * err_rate / 100))
            s5xx = max(0, int(errors * random.uniform(0.2, 0.5)))
            s4xx = errors - s5xx
            s2xx = rps_5min - errors

            docs.append({
                "tenant_id": TENANT_ID,
                "timestamp": ts,
                "endpoint": endpoint,
                "method": method,
                "requests": rps_5min,
                "errors": errors,
                "error_rate": round(errors / max(1, rps_5min) * 100, 2),
                "p50_latency_ms": p50,
                "p95_latency_ms": p95,
                "p99_latency_ms": p99,
                "avg_latency_ms": avg_lat,
                "status_2xx": s2xx,
                "status_4xx": s4xx,
                "status_5xx": s5xx,
                "bytes_in": rps_5min * random.randint(200, 800),
                "bytes_out": rps_5min * random.randint(1000, 5000),
            })

    if docs:
        # Insert in batches to avoid oversized writes
        for i in range(0, len(docs), 500):
            await coll.insert_many(docs[i:i + 500])
    logger.info("  ✅ Inserted %d api_metrics records", len(docs))
    return len(docs)


SERVICES = [
    ("api-server", 35, 480, 82),   # (base_cpu%, base_mem_mb, base_mem%)
    ("mongodb", 18, 1200, 45),
    ("redis", 5, 256, 32),
    ("llm-proxy", 22, 384, 48),
]


async def seed_infra_metrics(db) -> int:
    """1-minute interval infrastructure metrics over 24 hours."""
    coll = db["infra_metrics"]
    await coll.delete_many({"tenant_id": TENANT_ID})

    now = datetime.now(timezone.utc)
    docs = []

    for minutes_ago in range(0, 24 * 60):  # 24 hours, 1-min intervals
        ts = now - timedelta(minutes=minutes_ago)
        hour = ts.hour
        traffic = _diurnal_factor(hour)

        for svc, base_cpu, base_mem, base_mem_pct in SERVICES:
            cpu = min(98, max(1, _jitter(base_cpu * max(0.3, traffic), 0.2)))
            mem = max(64, _jitter(base_mem * max(0.6, traffic * 0.4 + 0.6), 0.05))
            mem_pct = min(95, max(10, _jitter(base_mem_pct * max(0.7, traffic * 0.3 + 0.7), 0.05)))

            docs.append({
                "tenant_id": TENANT_ID,
                "timestamp": ts,
                "service": svc,
                "cpu_percent": round(cpu, 1),
                "memory_mb": round(mem, 0),
                "memory_percent": round(mem_pct, 1),
                "disk_read_mb": round(random.uniform(0.5, 30) * traffic, 1),
                "disk_write_mb": round(random.uniform(0.2, 15) * traffic, 1),
                "network_in_mb": round(random.uniform(1, 20) * traffic, 1),
                "network_out_mb": round(random.uniform(5, 50) * traffic, 1),
                "active_connections": max(1, int(_jitter(25 * traffic, 0.3))) if svc == "api-server" else max(1, int(random.uniform(3, 15))),
                "gc_pause_ms": round(random.uniform(2, 25), 1) if svc in ("api-server", "llm-proxy") else 0,
                "thread_count": random.randint(8, 48) if svc == "api-server" else random.randint(4, 16),
            })

    if docs:
        for i in range(0, len(docs), 500):
            await coll.insert_many(docs[i:i + 500])
    logger.info("  ✅ Inserted %d infra_metrics records", len(docs))
    return len(docs)


async def seed_llm_metrics(db) -> int:
    """5-minute interval LLM usage metrics over 7 days."""
    coll = db["llm_metrics"]
    await coll.delete_many({"tenant_id": TENANT_ID})

    now = datetime.now(timezone.utc)
    docs = []
    providers = [
        ("gemini", "gemini-2.0-flash", 0.00015, 0.0006),  # (input_cost_per_1k, output_cost_per_1k)
        ("openai", "gpt-4o-mini", 0.00015, 0.0006),
    ]

    for minutes_ago in range(0, 7 * 24 * 60, 5):
        ts = now - timedelta(minutes=minutes_ago)
        traffic = _diurnal_factor(ts.hour) * _weekend_factor(ts.weekday())
        is_spike = _should_spike(ts)

        # Primary provider gets 80% of traffic
        provider, model, in_cost, out_cost = providers[0]
        requests = max(1, int(_jitter(45 * traffic, 0.25)))
        if is_spike:
            requests = int(requests * random.uniform(2, 3.5))

        tokens_in = int(requests * random.uniform(200, 400))
        tokens_out = int(requests * random.uniform(150, 300))
        cache_hits = int(requests * random.uniform(0.2, 0.4))
        tool_calls = int(requests * random.uniform(0.3, 0.6))
        errors = max(0, int(requests * random.uniform(0, 0.03)))
        cost = round((tokens_in * in_cost + tokens_out * out_cost) / 1000, 4)

        docs.append({
            "tenant_id": TENANT_ID,
            "timestamp": ts,
            "provider": provider,
            "model": model,
            "requests": requests,
            "tokens_in": tokens_in,
            "tokens_out": tokens_out,
            "avg_ttft_ms": int(_lognormal_latency(350, 0.4)),
            "avg_generation_ms": int(_lognormal_latency(1800, 0.3)),
            "cache_hits": cache_hits,
            "cache_misses": requests - cache_hits,
            "tool_calls": tool_calls,
            "errors": errors,
            "cost_usd": cost,
        })

    if docs:
        for i in range(0, len(docs), 500):
            await coll.insert_many(docs[i:i + 500])
    logger.info("  ✅ Inserted %d llm_metrics records", len(docs))
    return len(docs)


ERROR_TYPES = [
    ("LLMTimeoutError", "error", "llm-proxy", "/api/v1/chat/message", "LLM API timeout after {ms}ms"),
    ("RateLimitError", "warning", "api-server", "/api/v1/chat/message", "Rate limit exceeded for tenant {tid}"),
    ("MongoConnectionError", "error", "api-server", "/api/v1/catalog/items", "MongoDB connection pool exhausted"),
    ("ValidationError", "warning", "api-server", "/api/v1/actions/execute", "Schema validation failed: {field}"),
    ("EmbeddingError", "error", "llm-proxy", "/api/v1/chat/message", "Embedding API returned 503"),
    ("CircuitBreakerOpen", "error", "api-server", "/api/v1/chat/message", "Circuit breaker open for tenant {tid}"),
    ("RedisConnectionError", "warning", "redis", "/api/v1/config/branding/public", "Redis READONLY — failover in progress"),
    ("OOMKilled", "critical", "api-server", None, "Container killed: OOM (memory limit 2Gi)"),
]


async def seed_error_logs(db) -> int:
    """~200 error log entries over 7 days."""
    coll = db["error_logs"]
    await coll.delete_many({"tenant_id": TENANT_ID})

    now = datetime.now(timezone.utc)
    docs = []

    for i in range(200):
        hours_ago = random.uniform(0, 7 * 24)
        ts = now - timedelta(hours=hours_ago)

        etype, level, service, endpoint, msg_template = random.choice(ERROR_TYPES)
        msg = msg_template.format(
            ms=random.randint(15000, 60000),
            tid=TENANT_ID,
            field=random.choice(["price", "name", "category", "email"]),
        )

        docs.append({
            "tenant_id": TENANT_ID,
            "timestamp": ts,
            "level": level,
            "service": service,
            "error_type": etype,
            "message": msg,
            "endpoint": endpoint,
            "session_id": f"sess-{random.randint(1000, 9999)}",
            "resolved": random.random() < 0.6,
            "count": random.randint(1, 15),
        })

    docs.sort(key=lambda d: d["timestamp"])
    if docs:
        await coll.insert_many(docs)
    logger.info("  ✅ Inserted %d error_logs records", len(docs))
    return len(docs)


SLO_DEFS = [
    ("API Availability", 99.9, 99.85, 99.99),
    ("Chat Latency P95 < 2s", 95.0, 90.0, 99.5),
    ("LLM Error Rate < 5%", 95.0, 91.0, 99.8),
    ("Search Latency P95 < 500ms", 95.0, 92.0, 99.9),
]


async def seed_slo_metrics(db) -> int:
    """Daily SLO compliance over 90 days."""
    coll = db["slo_metrics"]
    await coll.delete_many({"tenant_id": TENANT_ID})

    now = datetime.now(timezone.utc)
    docs = []

    for days_ago in range(90):
        day = now - timedelta(days=days_ago)

        for slo_name, target, low_bound, high_bound in SLO_DEFS:
            # Generally above target, with occasional dips
            actual = random.uniform(low_bound, high_bound)
            if random.random() < 0.08:  # ~8% chance of being below target
                actual = random.uniform(low_bound, target - 0.1)

            budget_remaining = max(0, min(100, ((actual - target) / (100 - target)) * 100 + 50))
            violations = 0 if actual >= target else random.randint(1, 5)

            docs.append({
                "tenant_id": TENANT_ID,
                "date": day.strftime("%Y-%m-%d"),
                "slo_name": slo_name,
                "target": target,
                "actual": round(actual, 2),
                "budget_remaining_pct": round(budget_remaining, 1),
                "error_budget_minutes": round(max(0, (100 - actual) * 14.4), 1),  # 1440 min/day
                "violations": violations,
            })

    if docs:
        await coll.insert_many(docs)
    logger.info("  ✅ Inserted %d slo_metrics records", len(docs))
    return len(docs)


async def seed_user_analytics(db) -> int:
    """Hourly user engagement over 7 days."""
    coll = db["user_analytics"]
    await coll.delete_many({"tenant_id": TENANT_ID})

    now = datetime.now(timezone.utc)
    docs = []
    intents_pool = ["search", "compare", "dashboard", "support", "analytics", "admin", "catalog", "save_item"]

    for hours_ago in range(7 * 24):
        ts = now - timedelta(hours=hours_ago)
        traffic = _diurnal_factor(ts.hour) * _weekend_factor(ts.weekday())

        active = max(1, int(_jitter(30 * traffic, 0.3)))
        new_sess = max(0, int(active * random.uniform(0.25, 0.45)))
        msgs = max(1, int(active * random.uniform(4, 8)))
        comps = max(0, int(msgs * random.uniform(0.4, 0.7)))
        actions = max(0, int(msgs * random.uniform(0.05, 0.15)))

        # Pick 3 top intents weighted by traffic
        top = random.sample(intents_pool, k=min(3, len(intents_pool)))

        docs.append({
            "tenant_id": TENANT_ID,
            "timestamp": ts,
            "active_sessions": active,
            "new_sessions": new_sess,
            "messages_sent": msgs,
            "components_rendered": comps,
            "actions_triggered": actions,
            "avg_session_duration_min": round(random.uniform(3, 18) * max(0.5, traffic), 1),
            "bounce_rate": round(random.uniform(5, 25) * max(0.3, 1.3 - traffic), 1),
            "top_intents": top,
        })

    if docs:
        await coll.insert_many(docs)
    logger.info("  ✅ Inserted %d user_analytics records", len(docs))
    return len(docs)


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

async def seed_observability():
    """Seed all observability collections."""
    client = AsyncIOMotorClient(MONGODB_URI)
    db = client[DB_NAME]

    logger.info("🔭 Seeding observability data for tenant '%s'...", TENANT_ID)

    total = 0
    total += await seed_api_metrics(db)
    total += await seed_infra_metrics(db)
    total += await seed_llm_metrics(db)
    total += await seed_error_logs(db)
    total += await seed_slo_metrics(db)
    total += await seed_user_analytics(db)

    logger.info("🎉 Observability seed complete! %d total documents", total)
    client.close()


if __name__ == "__main__":
    asyncio.run(seed_observability())
