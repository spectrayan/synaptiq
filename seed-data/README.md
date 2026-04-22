# Synaptiq Seed Data

Realistic seed data for all Synaptiq collections — business, catalog, and observability.

## Usage

```bash
# Seed everything (requires MongoDB running on localhost:27017)
python seed-data/seed_all.py

# Or seed individual collections:
python seed-data/seed_tenant.py
python seed-data/seed_catalog.py
python seed-data/seed_business_data.py
python seed-data/seed_observability.py
python seed-data/seed_schema_registry.py
```

## Requirements

- Python 3.11+
- MongoDB running (default: `mongodb://localhost:27017`)
- `motor` and `pymongo` packages (included in backend deps)

## Collections

### Business Data
| Collection | Records | Description |
|-----------|---------|-------------|
| `products` | 8 | Product catalog with pricing and sales data |
| `orders` | 30 | Customer orders with status tracking |
| `sales_metrics` | 90 | Daily aggregated sales data (90 days) |
| `monthly_sales` | 4 | Monthly summaries |
| `category_sales` | 6 | Revenue by category |
| `regional_sales` | 5 | Revenue by region |
| `tasks` | 8 | Project management tasks |
| `events` | 6 | Audit/event timeline |
| `support_tickets` | 3 | Customer support tickets |

### Observability Data
| Collection | Records | Description |
|-----------|---------|-------------|
| `api_metrics` | ~2,016 | 5-min interval API endpoint metrics (7 days) |
| `infra_metrics` | ~1,440 | 1-min interval infrastructure metrics (24 hours) |
| `llm_metrics` | ~2,016 | 5-min interval LLM usage/cost metrics (7 days) |
| `error_logs` | ~200 | Application error logs (7 days) |
| `slo_metrics` | ~360 | Daily SLO compliance (90 days, 4 SLOs) |
| `user_analytics` | ~168 | Hourly user engagement (7 days) |
