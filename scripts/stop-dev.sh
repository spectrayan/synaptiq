#!/usr/bin/env bash
# ============================================================================
#  Synaptiq — Stop Local Development Environment (macOS / Linux)
#
#  Stops all running Synaptiq processes:
#    - Kills Spring Boot backend and Angular frontend
#    - Stops Docker services (MongoDB, Redis, Firebase Auth)
#
#  Usage: ./scripts/stop-dev.sh
# ============================================================================

set -uo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo ""
echo -e "${CYAN}Synaptiq — Stopping development environment...${NC}"
echo ""

# ── Kill backend ─────────────────────────────────────────────────────────────

echo -e "${YELLOW}[1/3]${NC} Stopping Spring Boot backend..."
if [ -f /tmp/synaptiq-api.pid ]; then
    PID=$(cat /tmp/synaptiq-api.pid)
    kill "$PID" 2>/dev/null || true
    rm -f /tmp/synaptiq-api.pid
fi
# Also kill any Spring Boot processes for this project
pkill -f "spring-boot:run.*spring-apis" 2>/dev/null || true
pkill -f "spring-apis.*SNAPSHOT.jar" 2>/dev/null || true
echo -e "${GREEN}  ✓ Backend stopped${NC}"

# ── Kill frontend ────────────────────────────────────────────────────────────

echo -e "${YELLOW}[2/3]${NC} Stopping Angular frontend..."
if [ -f /tmp/synaptiq-shell.pid ]; then
    PID=$(cat /tmp/synaptiq-shell.pid)
    kill "$PID" 2>/dev/null || true
    rm -f /tmp/synaptiq-shell.pid
fi
# Also kill any nx serve processes
pkill -f "nx serve shell" 2>/dev/null || true
echo -e "${GREEN}  ✓ Frontend stopped${NC}"

# ── Stop Docker services ─────────────────────────────────────────────────────

echo -e "${YELLOW}[3/3]${NC} Stopping Docker services..."
docker compose stop mongodb redis firebase-auth 2>/dev/null || true
echo -e "${GREEN}  ✓ Docker services stopped${NC}"

echo ""
echo -e "${GREEN}All services stopped.${NC}"
echo "To remove Docker volumes: docker compose down -v"
echo ""
