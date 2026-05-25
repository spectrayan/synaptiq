@echo off
REM ============================================================================
REM  Synaptiq — Local Development Startup Script (Windows)
REM
REM  Starts the full local stack:
REM    1. Docker services (MongoDB, Redis, Firebase Auth Emulator)
REM    2. Spring Boot backend (dev profile)
REM    3. Angular frontend (dev server)
REM
REM  Prerequisites:
REM    - Docker Desktop running
REM    - Node.js + pnpm installed
REM    - Java 21+ with Maven installed
REM
REM  Usage: scripts\start-dev.bat
REM ============================================================================

setlocal enabledelayedexpansion

REM -- Colors (ANSI escape codes) --
set "GREEN=[32m"
set "YELLOW=[33m"
set "CYAN=[36m"
set "RED=[31m"
set "RESET=[0m"

set "ROOT_DIR=%~dp0.."
cd /d "%ROOT_DIR%"

echo.
echo %CYAN%============================================================%RESET%
echo %CYAN%  Synaptiq — Local Development Environment%RESET%
echo %CYAN%============================================================%RESET%
echo.

REM ── Step 1: Check prerequisites ─────────────────────────────────────────────

echo %YELLOW%[1/5]%RESET% Checking prerequisites...

where docker >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo %RED%ERROR: Docker is not installed or not in PATH.%RESET%
    echo        Install Docker Desktop from https://www.docker.com/products/docker-desktop/
    exit /b 1
)

docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo %RED%ERROR: Docker daemon is not running.%RESET%
    echo        Start Docker Desktop and try again.
    exit /b 1
)

where pnpm >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo %RED%ERROR: pnpm is not installed.%RESET%
    echo        Install via: npm install -g pnpm
    exit /b 1
)

where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo %RED%ERROR: Java is not installed.%RESET%
    echo        Install Temurin JDK 21 from https://adoptium.net/
    exit /b 1
)

where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo %RED%ERROR: Maven is not installed.%RESET%
    echo        Install Maven from https://maven.apache.org/install.html
    exit /b 1
)

echo %GREEN%  ✓ All prerequisites met%RESET%
echo.

REM ── Step 2: Check Java version ──────────────────────────────────────────────

echo %YELLOW%[2/5]%RESET% Checking Java version...
java -version 2>&1 | findstr /r "version" >nul
echo %GREEN%  ✓ Java detected%RESET%
echo.

REM ── Step 3: Start Docker services ───────────────────────────────────────────

echo %YELLOW%[3/5]%RESET% Starting Docker services (MongoDB, Redis, Firebase Auth)...

docker compose up -d mongodb redis firebase-auth
if %ERRORLEVEL% neq 0 (
    echo %RED%ERROR: Failed to start Docker services.%RESET%
    exit /b 1
)

echo %GREEN%  ✓ Docker services started%RESET%
echo.

REM ── Wait for services to be healthy ─────────────────────────────────────────

echo %YELLOW%     Waiting for services to be healthy...%RESET%

set /a RETRIES=0
:wait_loop
if %RETRIES% gtr 30 (
    echo %RED%ERROR: Docker services did not become healthy in time.%RESET%
    echo        Run 'docker compose logs' to check for errors.
    exit /b 1
)

docker compose ps --format "{{.Health}}" 2>nul | findstr /i "starting" >nul
if %ERRORLEVEL% equ 0 (
    set /a RETRIES+=1
    timeout /t 2 /nobreak >nul
    goto wait_loop
)

echo %GREEN%  ✓ All Docker services healthy%RESET%
echo.

REM ── Step 4: Start Spring Boot backend ───────────────────────────────────────

echo %YELLOW%[4/5]%RESET% Starting Spring Boot backend (dev profile on port 8080)...

REM Start backend in a new window
start "Synaptiq API" cmd /k "cd /d %ROOT_DIR% && mvn spring-boot:run -f apps/backend/spring-apis/pom.xml -Dspring-boot.run.profiles=dev"

echo %GREEN%  ✓ Backend starting on http://localhost:8080%RESET%
echo %GREEN%    Swagger UI: http://localhost:8080/swagger-ui.html%RESET%
echo.

REM ── Step 5: Start Angular frontend ──────────────────────────────────────────

echo %YELLOW%[5/5]%RESET% Starting Angular frontend (dev server on port 4200)...

REM Start frontend in a new window
start "Synaptiq Shell" cmd /k "cd /d %ROOT_DIR% && pnpm nx serve synaptiq"

echo %GREEN%  ✓ Frontend starting on http://localhost:4200%RESET%
echo.

REM ── Done ────────────────────────────────────────────────────────────────────

echo %CYAN%============================================================%RESET%
echo %CYAN%  Synaptiq is starting up!%RESET%
echo %CYAN%============================================================%RESET%
echo.
echo   %GREEN%Frontend:%RESET%    http://localhost:4200
echo   %GREEN%Backend:%RESET%     http://localhost:8080
echo   %GREEN%Swagger UI:%RESET%  http://localhost:8080/swagger-ui.html
echo   %GREEN%MongoDB:%RESET%     mongodb://localhost:27017
echo   %GREEN%Redis:%RESET%       redis://localhost:6379
echo   %GREEN%Firebase UI:%RESET% http://localhost:4000
echo.
echo   Backend and frontend are running in separate terminal windows.
echo   Press Ctrl+C in those windows to stop individual services.
echo.
echo   To stop Docker services: docker compose down
echo   To stop everything:      scripts\stop-dev.bat
echo.

endlocal
