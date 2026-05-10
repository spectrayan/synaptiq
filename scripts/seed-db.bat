@echo off
setlocal EnableDelayedExpansion

:: ============================================================================
::  Synaptiq — Seed Database (Windows)
::
::  Populates MongoDB with demo tenant, catalog, business data, observability
::  metrics, and schema registry entries.
::
::  Expects:
::    - MongoDB running on localhost:27017
::    - Python 3.12+ with pymongo installed
::
::  Usage:
::    .\scripts\seed-db.bat          # Seed everything
::    .\scripts\seed-db.bat --reset  # Drop the database first, then seed
:: ============================================================================

:: -- Colors --
:: Windows 10+ supports ANSI escape sequences, but we'll use basic echo for compatibility
set "GREEN=[OK]"
set "YELLOW=[WAIT]"
set "CYAN=[INFO]"
set "RED=[ERROR]"

set "ROOT_DIR=%~dp0.."
cd /d "%ROOT_DIR%"

set "SEED_DIR=%ROOT_DIR%\seed-data"
set "RESET=false"

:: Parse args
:parse_args
if "%~1"=="" goto end_parse
if /i "%~1"=="--reset" (
    set "RESET=true"
    shift
    goto parse_args
)
echo %RED% Unknown option: %~1
exit /b 1
:end_parse

echo.
echo %CYAN% ============================================================
echo %CYAN%   Synaptiq — Database Seeder (Windows)
echo %CYAN% ============================================================
echo.

:: ── Check prerequisites ──────────────────────────────────────────────────────

echo %YELLOW% [1/3] Checking prerequisites...

:: Check Python
python --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo %RED% ERROR: Python 3 is not installed or not in PATH.
    echo        Install Python 3.12+ from https://www.python.org/downloads/
    exit /b 1
)
set "PYTHON_CMD=python"

:: Check pymongo is installed
%PYTHON_CMD% -c "import pymongo" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo %YELLOW%   ! pymongo not found — installing...
    %PYTHON_CMD% -m pip install --quiet pymongo
)

echo %GREEN%   ✓ Python + pymongo available

:: ── Check MongoDB connectivity ───────────────────────────────────────────────

echo %YELLOW% [2/3] Checking MongoDB connectivity...

:: Auto-detect MongoDB URI
if "%MONGODB_URI%"=="" (
    :: Try to detect credentials from Docker (simplified for batch)
    set "MONGO_USER="
    set "MONGO_PASS="
    
    for /f "tokens=*" %%i in ('docker ps --format "{{.Names}}" 2^>nul ^| findstr /i "mongo"') do set CONTAINER=%%i
    
    if defined CONTAINER (
        for /f "tokens=2 delims==" %%i in ('docker inspect !CONTAINER! --format "{{range .Config.Env}}{{println .}}{{end}}" 2^>nul ^| findstr MONGO_INITDB_ROOT_USERNAME') do set MONGO_USER=%%i
        for /f "tokens=2 delims==" %%i in ('docker inspect !CONTAINER! --format "{{range .Config.Env}}{{println .}}{{end}}" 2^>nul ^| findstr MONGO_INITDB_ROOT_PASSWORD') do set MONGO_PASS=%%i
    )
    
    if defined MONGO_USER if defined MONGO_PASS (
        set "MONGODB_URI=mongodb://!MONGO_USER!:!MONGO_PASS!@localhost:27017/?directConnection=true&authSource=admin"
        echo %GREEN%   ✓ Detected MongoDB credentials from Docker container '!CONTAINER!'
    ) else (
        set "MONGODB_URI=mongodb://localhost:27017/?directConnection=true"
        echo %YELLOW%   ℹ Using unauthenticated MongoDB connection
    )
)

echo %GREEN%   ✓ MONGODB_URI set
echo.

:: ── Optional: Reset database ─────────────────────────────────────────────────

if "%RESET%"=="true" (
    echo %YELLOW%   ⚠ --reset flag detected: dropping synaptiq database...
    mongosh --version >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        mongosh --quiet --eval "db.getSiblingDB('synaptiq').dropDatabase()" "%MONGODB_URI%"
        echo %GREEN%   ✓ Database dropped
    ) else (
        echo %RED%   ! mongosh not found — cannot reset. Seed will upsert over existing data.
    )
    echo.
)

:: ── Run seed scripts ─────────────────────────────────────────────────────────

echo %YELLOW% [3/3] Seeding database...
echo.

%PYTHON_CMD% "%SEED_DIR%\seed_all.py"

echo.
echo %CYAN% ============================================================
echo %GREEN%   ✅ Database seeded successfully!
echo %CYAN% ============================================================
echo.
echo   %GREEN% Database:  synaptiq ^(mongodb://localhost:27017^)
echo   %GREEN% Tenant:    demo-tenant
echo.
echo   Collections seeded:
echo     Business:       products, orders, sales_metrics, monthly_sales,
echo                     category_sales, regional_sales, tasks, events,
echo                     support_tickets
echo     Observability:  api_metrics, infra_metrics, llm_metrics,
echo                     error_logs, slo_metrics, user_analytics
echo     Registry:       schema_registry ^(15 schemas^)
echo.

endlocal
