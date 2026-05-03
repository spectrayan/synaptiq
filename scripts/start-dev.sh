#!/usr/bin/env bash
# ============================================================================
#  Synaptiq — Local Development Startup Script (macOS / Linux)
#
#  Starts the full local stack:
#    1. Docker services (MongoDB, Redis, Firebase Auth Emulator)
#    2. FastAPI backend (hot-reload)
#    3. Angular frontend (dev server)
#
#  Prerequisites:
#    - Docker / Docker Compose installed
#    - Node.js + pnpm installed
#    - Python 3.12+ with uv installed
#    - Copy apps/backend/api/.env.example -> apps/backend/api/.env
#
#  Usage: ./scripts/start-dev.sh
# ============================================================================

set -euo pipefail

# -- Colors --
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
RED='\033[0;31m'
NC='\033[0m' # No Color

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo ""
echo -e "${CYAN}============================================================${NC}"
echo -e "${CYAN}  Synaptiq — Local Development Environment${NC}"
echo -e "${CYAN}============================================================${NC}"
echo ""

# ── Step 1: Check prerequisites ──────────────────────────────────────────────

echo -e "${YELLOW}[1/5]${NC} Checking prerequisites..."

check_cmd() {
    if ! command -v "$1" &> /dev/null; then
        echo -e "${RED}ERROR: $1 is not installed.${NC}"
        echo "       $2"
        exit 1
    fi
}

check_cmd "docker"  "Install Docker from https://docs.docker.com/get-docker/"
check_cmd "pnpm"    "Install via: npm install -g pnpm"
check_cmd "uv"      "Install via: pip install uv  OR  curl -LsSf https://astral.sh/uv/install.sh | sh"

# Check Docker daemon is running
if ! docker info &> /dev/null; then
    echo -e "${RED}ERROR: Docker daemon is not running.${NC}"
    echo "       Start Docker and try again."
    exit 1
fi

echo -e "${GREEN}  ✓ All prerequisites met${NC}"
echo ""

# ── Step 2: Check .env file ──────────────────────────────────────────────────

echo -e "${YELLOW}[2/5]${NC} Checking backend .env configuration..."

if [ ! -f "apps/backend/api/.env" ]; then
    echo -e "${YELLOW}  ! No .env file found — copying from .env.example${NC}"
    cp "apps/backend/api/.env.example" "apps/backend/api/.env"
    echo -e "${YELLOW}  ! Edit apps/backend/api/.env to add your API keys${NC}"
else
    echo -e "${GREEN}  ✓ .env file exists${NC}"
fi
echo ""

# ── Step 3: Start Docker services ────────────────────────────────────────────

echo -e "${YELLOW}[3/5]${NC} Starting Docker services (MongoDB, Redis, Firebase Auth)..."

docker compose up -d mongodb redis firebase-auth

echo -e "${GREEN}  ✓ Docker services started${NC}"
echo ""

# ── Wait for services to be healthy ──────────────────────────────────────────

echo -e "${YELLOW}     Waiting for services to be healthy...${NC}"

RETRIES=0
MAX_RETRIES=30

while [ $RETRIES -lt $MAX_RETRIES ]; do
    HEALTH=$(docker compose ps --format "{{.Health}}" 2>/dev/null || echo "")
    if echo "$HEALTH" | grep -qi "starting"; then
        RETRIES=$((RETRIES + 1))
        sleep 2
    else
        break
    fi
done

if [ $RETRIES -ge $MAX_RETRIES ]; then
    echo -e "${RED}ERROR: Docker services did not become healthy in time.${NC}"
    echo "       Run 'docker compose logs' to check for errors."
    exit 1
fi

echo -e "${GREEN}  ✓ All Docker services healthy${NC}"
echo ""

# ── Step 4: Start FastAPI backend ────────────────────────────────────────────

echo -e "${YELLOW}[4/5]${NC} Starting FastAPI backend (hot-reload on port 8000)..."

pushd "apps/backend/api" > /dev/null

# Install Python deps if venv doesn't exist
if [ ! -d ".venv" ]; then
    echo -e "${YELLOW}     Installing Python dependencies...${NC}"
    uv sync
fi

# Start backend in the background
uv run uvicorn synaptiq_api.main:app \
    --host 0.0.0.0 \
    --port 8000 \
    --reload \
    &> /tmp/synaptiq-api.log &
API_PID=$!

popd > /dev/null

echo -e "${GREEN}  ✓ Backend starting on http://localhost:8000 (PID: $API_PID)${NC}"
echo -e "${GREEN}    API docs: http://localhost:8000/docs${NC}"
echo ""

# ── Step 5: Start Angular frontend ───────────────────────────────────────────

echo -e "${YELLOW}[5/5]${NC} Starting Angular frontend (dev server on port 4200)..."

# Start frontend in the background
pnpm nx serve shell &> /tmp/synaptiq-shell.log &
SHELL_PID=$!

echo -e "${GREEN}  ✓ Frontend starting on http://localhost:4200 (PID: $SHELL_PID)${NC}"
echo ""

# ── Save PIDs for stop script ────────────────────────────────────────────────

echo "$API_PID" > /tmp/synaptiq-api.pid
echo "$SHELL_PID" > /tmp/synaptiq-shell.pid

# ── Done ─────────────────────────────────────────────────────────────────────

echo -e "${CYAN}============================================================${NC}"
echo -e "${CYAN}  Synaptiq is starting up!${NC}"
echo -e "${CYAN}============================================================${NC}"
echo ""
echo -e "  ${GREEN}Frontend:${NC}    http://localhost:4200"
echo -e "  ${GREEN}Backend:${NC}     http://localhost:8000"
echo -e "  ${GREEN}API Docs:${NC}    http://localhost:8000/docs"
echo -e "  ${GREEN}MongoDB:${NC}     mongodb://localhost:27017"
echo -e "  ${GREEN}Redis:${NC}       redis://localhost:6379"
echo -e "  ${GREEN}Firebase UI:${NC} http://localhost:4000"
echo ""
echo "  Logs:"
echo "    API:   tail -f /tmp/synaptiq-api.log"
echo "    Shell: tail -f /tmp/synaptiq-shell.log"
echo ""
echo "  To stop everything: ./scripts/stop-dev.sh"
echo ""

# ── Trap cleanup on exit ─────────────────────────────────────────────────────

cleanup() {
    echo ""
    echo -e "${YELLOW}Shutting down...${NC}"
    kill $API_PID 2>/dev/null || true
    kill $SHELL_PID 2>/dev/null || true
    docker compose stop mongodb redis firebase-auth 2>/dev/null || true
    rm -f /tmp/synaptiq-api.pid /tmp/synaptiq-shell.pid
    echo -e "${GREEN}Done.${NC}"
}

trap cleanup SIGINT SIGTERM

# Wait for either process to exit
wait
