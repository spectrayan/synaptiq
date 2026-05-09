#!/usr/bin/env bash
# ============================================================================
#  Synaptiq — Stop Local Development (No Docker)
#
#  Stops processes started by start-local.sh:
#    - Firebase Auth Emulator
#    - FastAPI backend
#    - Angular frontend
#
#  Usage: ./scripts/stop-local.sh
# ============================================================================

set -uo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo ""
echo -e "${CYAN}Synaptiq — Stopping local development...${NC}"
echo ""

# ── Kill Firebase Auth Emulator ──────────────────────────────────────────────

echo -e "${YELLOW}[1/4]${NC} Stopping Firebase Auth Emulator..."
if [ -f /tmp/synaptiq-firebase.pid ]; then
    PID=$(cat /tmp/synaptiq-firebase.pid)
    kill "$PID" 2>/dev/null || true
    rm -f /tmp/synaptiq-firebase.pid
fi
pkill -f "firebase emulators:start" 2>/dev/null || true
echo -e "${GREEN}  ✓ Firebase emulator stopped${NC}"

# ── Kill backend ─────────────────────────────────────────────────────────────

echo -e "${YELLOW}[2/4]${NC} Stopping FastAPI backend..."
if [ -f /tmp/synaptiq-api.pid ]; then
    PID=$(cat /tmp/synaptiq-api.pid)
    kill "$PID" 2>/dev/null || true
    rm -f /tmp/synaptiq-api.pid
fi
pkill -f "uvicorn synaptiq_api.main:app" 2>/dev/null || true
echo -e "${GREEN}  ✓ Backend stopped${NC}"

# ── Kill frontend ────────────────────────────────────────────────────────────

echo -e "${YELLOW}[3/4]${NC} Stopping Angular frontend..."
if [ -f /tmp/synaptiq-shell.pid ]; then
    PID=$(cat /tmp/synaptiq-shell.pid)
    kill "$PID" 2>/dev/null || true
    rm -f /tmp/synaptiq-shell.pid
fi
pkill -f "nx serve shell" 2>/dev/null || true
echo -e "${GREEN}  ✓ Frontend stopped${NC}"

# ── Kill anything still holding ports 4200 / 8000 ────────────────────────────

echo -e "${YELLOW}[4/4]${NC} Freeing ports 4200 and 8000..."
for PORT in 4200 8000; do
    PIDS=$(lsof -ti :"$PORT" 2>/dev/null || true)
    if [ -n "$PIDS" ]; then
        echo "$PIDS" | xargs kill -9 2>/dev/null || true
        echo -e "${GREEN}  ✓ Killed process(es) on port $PORT${NC}"
    else
        echo -e "${GREEN}  ✓ Port $PORT already free${NC}"
    fi
done

echo ""
echo -e "${GREEN}All services stopped.${NC}"
echo ""
