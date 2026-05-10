#!/usr/bin/env bash
# ============================================================================
#  Synaptiq — Local Development Startup Script (macOS / Linux)
#
#  Starts the full local stack:
#    1. Docker services (MongoDB, Redis, Firebase Auth Emulator)
#    2. Spring Boot backend (dev profile)
#    3. Angular frontend (dev server)
#
#  Prerequisites:
#    - Docker / Docker Compose installed
#    - Node.js + pnpm installed
#    - Java 21+ with Maven installed
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
check_cmd "java"    "Install Temurin JDK 21 from https://adoptium.net/"
check_cmd "mvn"     "Install Maven from https://maven.apache.org/install.html"

# Check Docker daemon is running
if ! docker info &> /dev/null; then
    echo -e "${RED}ERROR: Docker daemon is not running.${NC}"
    echo "       Start Docker and try again."
    exit 1
fi

echo -e "${GREEN}  ✓ All prerequisites met${NC}"
echo ""

# ── Step 2: Check Java version ───────────────────────────────────────────────

echo -e "${YELLOW}[2/5]${NC} Checking Java version..."

JAVA_VERSION=$(java -version 2>&1 | head -1 | grep -oP '(?<=")\d+' | head -1)
if [ "$JAVA_VERSION" -lt 21 ] 2>/dev/null; then
    echo -e "${RED}ERROR: Java 21+ required (found Java $JAVA_VERSION).${NC}"
    exit 1
fi
echo -e "${GREEN}  ✓ Java $JAVA_VERSION detected${NC}"
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

# ── Step 4: Start Spring Boot backend ────────────────────────────────────────

echo -e "${YELLOW}[4/5]${NC} Starting Spring Boot backend (dev profile on port 8080)..."

mvn spring-boot:run \
    -f apps/backend/spring-apis/pom.xml \
    -Dspring-boot.run.profiles=dev \
    &> /tmp/synaptiq-api.log &
API_PID=$!

echo -e "${GREEN}  ✓ Backend starting on http://localhost:8080 (PID: $API_PID)${NC}"
echo -e "${GREEN}    Swagger UI: http://localhost:8080/swagger-ui.html${NC}"
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
echo -e "  ${GREEN}Backend:${NC}     http://localhost:8080"
echo -e "  ${GREEN}Swagger UI:${NC}  http://localhost:8080/swagger-ui.html"
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
