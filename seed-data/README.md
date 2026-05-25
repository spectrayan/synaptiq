# Synaptiq Seed Data

Seed scripts and structured JSON data for populating the Synaptiq development database.

## Directory Structure

```
seed-data/
├── data/                           # All static data as JSON files
│   ├── tenant.json                 # Tenant config + AI persona
│   ├── application.json            # App config
│   ├── products.json               # Product catalog (8 products)
│   ├── tasks.json                  # Project tasks (8 tasks)
│   ├── events.json                 # Audit event log (6 events)
│   ├── support_tickets.json        # Support tickets (3 tickets)
│   ├── sales/
│   │   ├── monthly.json            # Monthly sales summaries
│   │   ├── category.json           # Sales by category
│   │   └── regional.json           # Regional sales
│   ├── schemas/
│   │   ├── business.json           # Business collection schemas (9)
│   │   └── observability.json      # Observability collection schemas (6)
│   └── workflows/
│       └── spectrayan-health/
│           ├── aba-goal-generation.json  # Multi-agent ABA workflow spec
│           └── client-profiles/         # Test client profiles
│               ├── early-learner.json
│               ├── school-age-social.json
│               ├── adolescent-adaptive.json
│               ├── preschooler-behavior.json
│               └── young-adult-vocational.json
├── results/                        # E2E test results (git-ignored)
├── seed_all.py                     # Master seed script (runs all phases)
├── seed_e2e_data.py                # Business data seeder
├── seed_schema_registry.py         # Schema registry seeder
├── seed_workflows.py               # Workflow + test fixture seeder
├── seed_users.py                   # User seeder
├── seed_observability.py           # Observability metrics seeder
├── seed_knowledge_base.py          # Knowledge base seeder
├── test_e2e_workflow.py            # E2E workflow test runner
└── README.md                       # This file
```

## Usage

### Quick Start: Seed Everything

```bash
# Setup Python venv (first time)
cd seed-data && python3 -m venv .venv && source .venv/bin/activate
pip install motor pymongo requests

# Seed all data
cd /path/to/synaptiq
python seed-data/seed_all.py

# Or use the shell script
./scripts/seed-db.sh
```

### Individual Seeds

```bash
# Business data only
python seed-data/seed_e2e_data.py

# Schema registry only
python seed-data/seed_schema_registry.py

# Workflows + client profiles
python seed-data/seed_workflows.py
```

### E2E Workflow Test

```bash
# Prerequisites: Backend running on localhost:8080
# Set GOOGLE_API_KEY for Gemini API access

# Run with default profile (early-learner)
python seed-data/test_e2e_workflow.py

# Run with a specific client profile
python seed-data/test_e2e_workflow.py --profile school-age-social
python seed-data/test_e2e_workflow.py --profile adolescent-adaptive
python seed-data/test_e2e_workflow.py --profile preschooler-behavior
python seed-data/test_e2e_workflow.py --profile young-adult-vocational
```

## ABA Multi-Agent Workflow

The Spectrayan Health ABA Goal Generation workflow is a **DYNAMIC** multi-agent orchestration:

```
                    ┌─────────────────┐
                    │   Supervisor     │
                    │  (Orchestrator)  │
                    └─────┬───────────┘
                          │ delegates
         ┌────────────────┼────────────────┬──────────────────┐
         ▼                ▼                ▼                  ▼
  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
  │ ABA Therapy  │ │   Speech     │ │ Occupational │ │   CBT        │
  │  Assistant   │ │  Therapy     │ │  Therapy     │ │  Analyst     │
  │              │ │  Assistant   │ │  Assistant   │ │              │
  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
```

- **Supervisor**: Analyzes client profile, delegates to specialists, synthesizes 12-month plan
- **ABA Assistant**: Core ABA therapy goals (communication, social, adaptive, behavior)
- **Speech Therapy**: Speech-language pathology goals
- **OT Assistant**: Occupational therapy goals (motor, sensory, ADLs)
- **CBT Analyst**: Cognitive behavioral therapy goals (emotional regulation, anxiety)

Output: Consolidated JSON with quarterly milestones (Q1-Q4).
