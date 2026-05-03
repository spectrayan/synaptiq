#!/usr/bin/env bash
# ============================================================================
#  Synaptiq — Local Development (No Docker)
#
#  Starts the full stack without Docker:
#    1. FastAPI backend (hot-reload on port 8000)
#    2. Angular frontend (dev server on port 4200)
#
#  Auth mode (set AUTH_PROVIDER env var):
#    - "builtin" (default): MongoDB users + JWT — no Firebase needed
#    - "firebase": Starts Firebase Auth Emulator on port 9099
#
#  Expects:
#    - MongoDB already running on localhost:27017
#    - Node.js + pnpm installed
#    - Python 3.12+ with uv installed
#
#  Usage: ./scripts/start-local.sh
#         AUTH_PROVIDER=firebase ./scripts/start-local.sh
# ============================================================================

set -euo pipefail

# -- Colors --
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
RED='\033[0;31m'
NC='\033[0m'

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

AUTH_PROVIDER="${AUTH_PROVIDER:-builtin}"

# Track child PIDs for cleanup
PIDS=()

cleanup() {
    echo ""
    echo -e "${YELLOW}Shutting down...${NC}"
    for pid in "${PIDS[@]}"; do
        kill "$pid" 2>/dev/null || true
    done
    # Kill any stragglers
    if [ "$AUTH_PROVIDER" = "firebase" ]; then
        pkill -f "firebase emulators:start" 2>/dev/null || true
    fi
    pkill -f "uvicorn synaptiq_api.main:app" 2>/dev/null || true
    pkill -f "nx serve shell" 2>/dev/null || true
    rm -f /tmp/synaptiq-api.pid /tmp/synaptiq-shell.pid /tmp/synaptiq-firebase.pid
    echo -e "${GREEN}Done.${NC}"
}
trap cleanup SIGINT SIGTERM EXIT

echo ""
echo -e "${CYAN}============================================================${NC}"
echo -e "${CYAN}  Synaptiq — Local Development (No Docker)${NC}"
echo -e "${CYAN}  Auth Provider: ${AUTH_PROVIDER}${NC}"
echo -e "${CYAN}============================================================${NC}"
echo ""

# ── Step 1: Check prerequisites ──────────────────────────────────────────────

TOTAL_STEPS=4
if [ "$AUTH_PROVIDER" = "firebase" ]; then
    TOTAL_STEPS=5
fi

echo -e "${YELLOW}[1/${TOTAL_STEPS}]${NC} Checking prerequisites..."

check_cmd() {
    if ! command -v "$1" &> /dev/null; then
        echo -e "${RED}ERROR: $1 is not installed.${NC}"
        echo "       $2"
        exit 1
    fi
}

check_cmd "pnpm"     "Install via: npm install -g pnpm"
check_cmd "uv"       "Install via: pip install uv  OR  curl -LsSf https://astral.sh/uv/install.sh | sh"
if [ "$AUTH_PROVIDER" = "firebase" ]; then
    check_cmd "firebase" "Install via: npm install -g firebase-tools"
fi

echo -e "${GREEN}  ✓ All prerequisites met${NC}"
echo ""

# ── Step 2: Check MongoDB is reachable ───────────────────────────────────────

echo -e "${YELLOW}[2/${TOTAL_STEPS}]${NC} Checking MongoDB connectivity..."

if command -v mongosh &> /dev/null; then
    if mongosh --quiet --eval "db.adminCommand('ping')" mongodb://localhost:27017 &> /dev/null; then
        echo -e "${GREEN}  ✓ MongoDB is reachable on localhost:27017${NC}"
    else
        echo -e "${RED}ERROR: MongoDB is not responding on localhost:27017${NC}"
        echo "       Make sure MongoDB is running locally."
        exit 1
    fi
else
    # No mongosh — just check if the port is open
    if (echo > /dev/tcp/localhost/27017) 2>/dev/null; then
        echo -e "${GREEN}  ✓ MongoDB port 27017 is open${NC}"
    else
        echo -e "${RED}ERROR: Nothing listening on localhost:27017${NC}"
        echo "       Make sure MongoDB is running locally."
        exit 1
    fi
fi
echo ""

# ── Step 3: Firebase Auth Emulator (only if AUTH_PROVIDER=firebase) ──────────

STEP_NEXT=3

if [ "$AUTH_PROVIDER" = "firebase" ]; then
    echo -e "${YELLOW}[${STEP_NEXT}/${TOTAL_STEPS}]${NC} Starting Firebase Auth Emulator (port 9099)..."
    STEP_NEXT=$((STEP_NEXT + 1))

    # Kill any existing Firebase emulator
    pkill -f "firebase emulators:start" 2>/dev/null || true
    sleep 1

    export FIREBASE_AUTH_EMULATOR_HOST=localhost:9099

    firebase emulators:start --only auth --project synaptiq-dev \
        &> /tmp/synaptiq-firebase.log &
    FIREBASE_PID=$!
    PIDS+=("$FIREBASE_PID")
    echo "$FIREBASE_PID" > /tmp/synaptiq-firebase.pid

    # Wait for emulator to be ready
    RETRIES=0
    MAX_RETRIES=30
    while [ $RETRIES -lt $MAX_RETRIES ]; do
        if curl -s http://localhost:9099/ &> /dev/null; then
            break
        fi
        RETRIES=$((RETRIES + 1))
        sleep 1
    done

    if [ $RETRIES -ge $MAX_RETRIES ]; then
        echo -e "${RED}ERROR: Firebase Auth Emulator did not start in time.${NC}"
        echo "       Check /tmp/synaptiq-firebase.log for details."
        exit 1
    fi

    echo -e "${GREEN}  ✓ Firebase Auth Emulator running on http://localhost:9099${NC}"
    echo ""
else
    echo -e "${YELLOW}[${STEP_NEXT}/${TOTAL_STEPS}]${NC} Skipping Firebase (using built-in JWT auth)"
    echo -e "${GREEN}  ✓ Built-in auth enabled — no external dependencies${NC}"
    echo ""
    STEP_NEXT=$((STEP_NEXT + 1))
fi

# ── Step N: Start FastAPI backend ────────────────────────────────────────────

echo -e "${YELLOW}[${STEP_NEXT}/${TOTAL_STEPS}]${NC} Starting FastAPI backend + Angular frontend..."
STEP_NEXT=$((STEP_NEXT + 1))

pushd "apps/backend/api" > /dev/null

# Install Python deps if venv doesn't exist
if [ ! -d ".venv" ]; then
    echo -e "${YELLOW}     Installing Python dependencies...${NC}"
    uv sync
fi

# Export auth provider setting
export AUTH_PROVIDER="$AUTH_PROVIDER"

# Firebase env vars (only relevant when AUTH_PROVIDER=firebase)
if [ "$AUTH_PROVIDER" = "firebase" ]; then
    export FIREBASE_AUTH_EMULATOR_HOST=localhost:9099
    export FIREBASE_PROJECT_ID=synaptiq-dev
fi

uv run uvicorn synaptiq_api.main:app \
    --host 0.0.0.0 \
    --port 8000 \
    --reload \
    &> /tmp/synaptiq-api.log &
API_PID=$!
PIDS+=("$API_PID")
echo "$API_PID" > /tmp/synaptiq-api.pid

popd > /dev/null

echo -e "${GREEN}  ✓ Backend starting on http://localhost:8000 (PID: $API_PID)${NC}"

# ── Start Angular frontend ───────────────────────────────────────────────────

pnpm nx serve shell &> /tmp/synaptiq-shell.log &
SHELL_PID=$!
PIDS+=("$SHELL_PID")
echo "$SHELL_PID" > /tmp/synaptiq-shell.pid

echo -e "${GREEN}  ✓ Frontend starting on http://localhost:4200 (PID: $SHELL_PID)${NC}"
echo ""

# ── Done ─────────────────────────────────────────────────────────────────────

echo -e "${CYAN}============================================================${NC}"
echo -e "${CYAN}  Synaptiq is starting up!${NC}"
echo -e "${CYAN}============================================================${NC}"
echo ""
echo -e "  ${GREEN}Frontend:${NC}    http://localhost:4200"
echo -e "  ${GREEN}Backend:${NC}     http://localhost:8000"
echo -e "  ${GREEN}API Docs:${NC}    http://localhost:8000/docs"
echo -e "  ${GREEN}MongoDB:${NC}     mongodb://localhost:27017"
if [ "$AUTH_PROVIDER" = "firebase" ]; then
    echo -e "  ${GREEN}Firebase:${NC}    http://localhost:9099 (Auth Emulator)"
else
    echo -e "  ${GREEN}Auth:${NC}        Built-in (admin@synaptiq.local / admin)"
fi
echo ""
echo "  Logs:"
echo "    API:      tail -f /tmp/synaptiq-api.log"
echo "    Shell:    tail -f /tmp/synaptiq-shell.log"
if [ "$AUTH_PROVIDER" = "firebase" ]; then
    echo "    Firebase: tail -f /tmp/synaptiq-firebase.log"
fi
echo ""
echo "  Press Ctrl+C to stop everything."
echo ""

# Wait for any child to exit
wait
