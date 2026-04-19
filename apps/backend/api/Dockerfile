# ── Synaptiq API — Production Dockerfile ─────────────────────────────────────
# Multi-stage build: install deps → slim runtime image
# Target: Cloud Run (port 8080 via $PORT)
# ─────────────────────────────────────────────────────────────────────────────

# Stage 1 — Install dependencies
FROM python:3.12-slim AS builder

WORKDIR /app

# Install uv for fast, deterministic installs
RUN pip install --no-cache-dir uv

COPY pyproject.toml uv.lock* ./

# Install production deps only (no dev)
RUN uv sync --frozen --no-dev

# Stage 2 — Runtime image
FROM python:3.12-slim

WORKDIR /app

# Copy installed packages from builder
COPY --from=builder /app/.venv /app/.venv
COPY --from=builder /usr/local/bin/uv /usr/local/bin/uv

# Copy application source
COPY pyproject.toml uv.lock* ./
COPY src/ ./src/

# Set environment
ENV PYTHONPATH=/app/src \
    PYTHONUNBUFFERED=1 \
    PORT=8080

EXPOSE 8080

# Health check for Cloud Run
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8080/health')" || exit 1

# Run with gunicorn + uvicorn workers for production concurrency
CMD ["uv", "run", "uvicorn", "synaptiq_api.main:app", \
     "--host", "0.0.0.0", \
     "--port", "8080", \
     "--workers", "2", \
     "--timeout-keep-alive", "30", \
     "--access-log"]
