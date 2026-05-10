# Synaptiq Seed Data

> Realistic seed data for all Synaptiq collections — tenants, catalogs, users, and analytics.

[![Part of Synaptiq](https://img.shields.io/badge/part%20of-Synaptiq-7C4DFF?style=flat-square)](../README.md)

## Overview

This directory contains scripts and data files for populating a fresh Synaptiq MongoDB instance with realistic demo data. The seed data covers all major bounded contexts and is designed to showcase the full feature set of the platform.

## Collections Seeded

| Collection | Records | Description |
|------------|---------|-------------|
| `tenants` | 3 | Demo tenants with different plans and configurations |
| `users` | 6 | Admin and viewer users across tenants |
| `catalog_schemas` | 3 | Product schemas (electronics, furniture, SaaS tools) |
| `catalog_items` | 50+ | Sample products with rich metadata |
| `chat_sessions` | 10 | Example conversation histories |
| `analytics_events` | 100+ | Usage metrics and billing data |
| `branding_configs` | 3 | Per-tenant theme and branding configurations |

## Quick Start

### Using the batch script (recommended)

```bash
# Windows
scripts\seed-data.bat

# macOS / Linux
python seed-data/seed_all.py
```

### Manual

```bash
# Prerequisites
pip install pymongo

# Run the seed script
python seed-data/seed_all.py

# Or seed individual collections
python seed-data/seed_tenants.py
python seed-data/seed_catalog.py
```

## Configuration

The seed scripts connect to MongoDB using these defaults:

| Variable | Default |
|----------|---------|
| `MONGODB_URI` | `mongodb://localhost:27017` |
| `MONGODB_DB` | `synaptiq` |

Override via environment variables:

```bash
MONGODB_URI=mongodb://myhost:27017 python seed-data/seed_all.py
```

## Default Credentials

After seeding, you can log in with:

| Email | Password | Role |
|-------|----------|------|
| `admin@synaptiq.local` | `admin` | Platform Admin |
| `tenant@demo.com` | `demo123` | Tenant Admin |
| `viewer@demo.com` | `demo123` | Tenant Viewer |
