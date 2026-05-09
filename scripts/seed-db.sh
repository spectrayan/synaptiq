#!/usr/bin/env bash
# ============================================================================
#  Synaptiq — Seed Database
#
#  Populates MongoDB with demo tenant, catalog, business data, observability
#  metrics, and schema registry entries.
#
#  Expects:
#    - MongoDB running on localhost:27017
#    - Python 3.11+ with uv installed
#
#  Usage:
#    ./scripts/seed-db.sh          # Seed everything
#    ./scripts/seed-db.sh --reset  # Drop the database first, then seed
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

SEED_DIR="$ROOT_DIR/seed-data"
BACKEND_DIR="$ROOT_DIR/apps/backend/api"
RESET=false

# Parse args
for arg in "$@"; do
    case "$arg" in
        --reset) RESET=true ;;
        *) echo -e "${RED}Unknown option: $arg${NC}"; exit 1 ;;
    esac
done

echo ""
echo -e "${CYAN}============================================================${NC}"
echo -e "${CYAN}  Synaptiq — Database Seeder${NC}"
echo -e "${CYAN}============================================================${NC}"
echo ""

# ── Check prerequisites ──────────────────────────────────────────────────────

echo -e "${YELLOW}[1/3]${NC} Checking prerequisites..."

if ! command -v uv &> /dev/null; then
    echo -e "${RED}ERROR: uv is not installed.${NC}"
    echo "       Install via: pip install uv  OR  curl -LsSf https://astral.sh/uv/install.sh | sh"
    exit 1
fi

echo -e "${GREEN}  ✓ uv available${NC}"

# ── Check MongoDB connectivity ───────────────────────────────────────────────

echo -e "${YELLOW}[2/3]${NC} Checking MongoDB connectivity..."

# ── Auto-detect MongoDB URI ──────────────────────────────────────────────────
if [ -z "${MONGODB_URI:-}" ]; then
    # Try to detect credentials from a running Docker mongo container
    MONGO_USER=""
    MONGO_PASS=""
    if command -v docker &> /dev/null && docker ps --format '{{.Names}}' 2>/dev/null | grep -qi mongo; then
        CONTAINER=$(docker ps --format '{{.Names}}' 2>/dev/null | grep -i mongo | head -1)
        MONGO_USER=$(docker inspect "$CONTAINER" --format '{{range .Config.Env}}{{println .}}{{end}}' 2>/dev/null | grep MONGO_INITDB_ROOT_USERNAME | cut -d= -f2 || true)
        MONGO_PASS=$(docker inspect "$CONTAINER" --format '{{range .Config.Env}}{{println .}}{{end}}' 2>/dev/null | grep MONGO_INITDB_ROOT_PASSWORD | cut -d= -f2 || true)
    fi

    if [ -n "$MONGO_USER" ] && [ -n "$MONGO_PASS" ]; then
        export MONGODB_URI="mongodb://${MONGO_USER}:${MONGO_PASS}@localhost:27017/?directConnection=true&authSource=admin"
        echo -e "${GREEN}  ✓ Detected MongoDB credentials from Docker container '${CONTAINER}'${NC}"
    else
        export MONGODB_URI="mongodb://localhost:27017/?directConnection=true"
        echo -e "${YELLOW}  ℹ Using unauthenticated MongoDB connection${NC}"
    fi
fi

echo -e "${GREEN}  ✓ MONGODB_URI=${MONGODB_URI%%@*}@...${NC}"

# Verify connectivity
if (echo > /dev/tcp/localhost/27017) 2>/dev/null; then
    echo -e "${GREEN}  ✓ MongoDB port 27017 is open${NC}"
else
    echo -e "${RED}ERROR: Nothing listening on localhost:27017${NC}"
    echo "       Make sure MongoDB is running."
    exit 1
fi
echo ""

# ── Optional: Reset database ─────────────────────────────────────────────────

if [ "$RESET" = true ]; then
    echo -e "${YELLOW}  ⚠ --reset flag detected: dropping synaptiq database...${NC}"
    if command -v mongosh &> /dev/null; then
        mongosh --quiet --eval 'db.getSiblingDB("synaptiq").dropDatabase()' "$MONGODB_URI"
        echo -e "${GREEN}  ✓ Database dropped${NC}"
    else
        echo -e "${RED}  ! mongosh not found — cannot reset. Seed will upsert over existing data.${NC}"
    fi
    echo ""
fi

# ── Run seed scripts ─────────────────────────────────────────────────────────

echo -e "${YELLOW}[3/3]${NC} Seeding database..."
echo ""

# Use the backend venv which already has motor/pymongo installed
if [ ! -d "$BACKEND_DIR/.venv" ]; then
    echo -e "${YELLOW}     Backend venv not found — installing dependencies...${NC}"
    pushd "$BACKEND_DIR" > /dev/null
    uv sync
    popd > /dev/null
fi

# Run the master seed script using the backend's Python (which has motor)
pushd "$BACKEND_DIR" > /dev/null
uv run python "$SEED_DIR/seed_all.py"
popd > /dev/null

echo ""
echo -e "${CYAN}============================================================${NC}"
echo -e "${GREEN}  ✅ Database seeded successfully!${NC}"
echo -e "${CYAN}============================================================${NC}"
echo ""
echo -e "  ${GREEN}Database:${NC}  synaptiq (mongodb://localhost:27017)"
echo -e "  ${GREEN}Tenant:${NC}    demo-tenant"
echo ""
echo "  Collections seeded:"
echo "    Business:       products, orders, sales_metrics, monthly_sales,"
echo "                    category_sales, regional_sales, tasks, events,"
echo "                    support_tickets"
echo "    Observability:  api_metrics, infra_metrics, llm_metrics,"
echo "                    error_logs, slo_metrics, user_analytics"
echo "    Registry:       schema_registry (15 schemas)"
echo ""
