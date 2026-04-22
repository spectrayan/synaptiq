@echo off
REM ============================================================================
REM  Synaptiq — Stop Local Development Environment (Windows)
REM
REM  Stops all running Synaptiq processes:
REM    - Kills FastAPI backend and Angular frontend windows
REM    - Stops Docker services (MongoDB, Redis, Firebase Auth)
REM
REM  Usage: scripts\stop-dev.bat
REM ============================================================================

setlocal

set "GREEN=[32m"
set "YELLOW=[33m"
set "CYAN=[36m"
set "RESET=[0m"

set "ROOT_DIR=%~dp0.."
cd /d "%ROOT_DIR%"

echo.
echo %CYAN%Synaptiq — Stopping development environment...%RESET%
echo.

REM ── Kill backend window ─────────────────────────────────────────────────────

echo %YELLOW%[1/3]%RESET% Stopping FastAPI backend...
taskkill /FI "WINDOWTITLE eq Synaptiq API*" /F >nul 2>&1
echo %GREEN%  ✓ Backend stopped%RESET%

REM ── Kill frontend window ────────────────────────────────────────────────────

echo %YELLOW%[2/3]%RESET% Stopping Angular frontend...
taskkill /FI "WINDOWTITLE eq Synaptiq Shell*" /F >nul 2>&1
echo %GREEN%  ✓ Frontend stopped%RESET%

REM ── Stop Docker services ────────────────────────────────────────────────────

echo %YELLOW%[3/3]%RESET% Stopping Docker services...
docker compose stop mongodb redis firebase-auth >nul 2>&1
echo %GREEN%  ✓ Docker services stopped%RESET%

echo.
echo %GREEN%All services stopped.%RESET%
echo To remove Docker volumes: docker compose down -v
echo.

endlocal
