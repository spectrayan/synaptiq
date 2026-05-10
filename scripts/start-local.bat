@echo off
REM ============================================================================
REM  Synaptiq — Local Development (No Docker) (Windows)
REM
REM  Starts the full stack without Docker:
REM    1. Spring Boot backend (dev profile on port 8080)
REM    2. Angular frontend (dev server on port 4200)
REM
REM  Auth mode (set AUTH_PROVIDER env var):
REM    - "builtin" (default): MongoDB users + JWT — no Firebase needed
REM    - "firebase": Starts Firebase Auth Emulator on port 9099
REM
REM  Expects:
REM    - MongoDB already running on localhost:27017
REM    - Node.js + pnpm installed
REM    - Java 21+ with Maven installed
REM
REM  Usage: scripts\start-local.bat
REM         set AUTH_PROVIDER=firebase && scripts\start-local.bat
REM ============================================================================

setlocal enabledelayedexpansion

REM -- Console Prefixes --
set "GREEN=[OK]"
set "YELLOW=[WAIT]"
set "CYAN=[INFO]"
set "RED=[ERROR]"

set "ROOT_DIR=%~dp0.."
cd /d "%ROOT_DIR%"

if "%AUTH_PROVIDER%"=="" set "AUTH_PROVIDER=builtin"

echo.
echo %CYAN% ============================================================
echo %CYAN%   Synaptiq - Local Development (No Docker) (Windows)
echo %CYAN%   Auth Provider: %AUTH_PROVIDER%
echo %CYAN% ============================================================
echo.

REM ── Step 1: Check prerequisites ─────────────────────────────────────────────

set "TOTAL_STEPS=4"
if "%AUTH_PROVIDER%"=="firebase" set "TOTAL_STEPS=5"

echo %YELLOW% [1/%TOTAL_STEPS%] Checking prerequisites...

where pnpm >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo %RED% ERROR: pnpm is not installed.
    echo        Install via: npm install -g pnpm
    exit /b 1
)

where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo %RED% ERROR: Java is not installed.
    echo        Install Temurin JDK 21 from https://adoptium.net/
    exit /b 1
)

where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo %RED% ERROR: Maven is not installed.
    echo        Install Maven from https://maven.apache.org/install.html
    exit /b 1
)

if "%AUTH_PROVIDER%"=="firebase" (
    where firebase >nul 2>&1
    if !ERRORLEVEL! neq 0 (
        echo %RED% ERROR: firebase is not installed.
        echo        Install via: npm install -g firebase-tools
        exit /b 1
    )
)

echo %GREEN%   v All prerequisites met
echo.

REM ── Step 2: Check MongoDB is reachable ──────────────────────────────────────

echo %YELLOW% [2/%TOTAL_STEPS%] Checking MongoDB connectivity...

REM Using python to check if port is open since netcat is not standard on Windows
python -c "import socket; s=socket.socket(); s.settimeout(2); s.connect(('localhost', 27017)); s.close()" >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo %GREEN%   v MongoDB port 27017 is open
) else (
    echo %RED% ERROR: Nothing listening on localhost:27017
    echo        Make sure MongoDB is running locally.
    exit /b 1
)
echo.

REM ── Step 3: Firebase Auth Emulator (only if AUTH_PROVIDER=firebase) ─────────

set "STEP_NEXT=3"

if "%AUTH_PROVIDER%"=="firebase" (
    echo %YELLOW% [!STEP_NEXT!/%TOTAL_STEPS%] Starting Firebase Auth Emulator ^(port 9099^)...
    set /a STEP_NEXT+=1

    set "FIREBASE_AUTH_EMULATOR_HOST=localhost:9099"
    
    start "Synaptiq Firebase" cmd /k "cd /d %ROOT_DIR% && firebase emulators:start --only auth --project synaptiq-dev"

    echo %GREEN%   v Firebase Auth Emulator starting on http://localhost:9099
    echo.
) else (
    echo %YELLOW% [!STEP_NEXT!/%TOTAL_STEPS%] Skipping Firebase ^(using built-in JWT auth^)
    echo %GREEN%   v Built-in auth enabled - no external dependencies
    echo.
    set /a STEP_NEXT+=1
)

REM ── Step N: Start Spring Boot backend + Angular frontend ────────────────────

echo %YELLOW% [!STEP_NEXT!/%TOTAL_STEPS%] Starting Spring Boot backend + Angular frontend...

REM Setup Firebase env var for backend window if needed
set "FIREBASE_ENV_CMD="
if "%AUTH_PROVIDER%"=="firebase" set "FIREBASE_ENV_CMD=set FIREBASE_AUTH_EMULATOR_HOST=localhost:9099 && "

start "Synaptiq API" cmd /k "cd /d %ROOT_DIR% && set AUTH_PROVIDER=%AUTH_PROVIDER% && !FIREBASE_ENV_CMD!mvn spring-boot:run -f apps/backend/spring-apis/pom.xml -Dspring-boot.run.profiles=dev"
echo %GREEN%   v Backend starting on http://localhost:8080

start "Synaptiq Shell" cmd /k "cd /d %ROOT_DIR% && pnpm nx serve shell"
echo %GREEN%   v Frontend starting on http://localhost:4200
echo.

REM ── Done ────────────────────────────────────────────────────────────────────

echo %CYAN% ============================================================
echo %CYAN%   Synaptiq is starting up!
echo %CYAN% ============================================================
echo.
echo   %GREEN% Frontend:    http://localhost:4200
echo   %GREEN% Backend:     http://localhost:8080
echo   %GREEN% Swagger UI:  http://localhost:8080/swagger-ui.html
echo   %GREEN% MongoDB:     mongodb://localhost:27017
if "%AUTH_PROVIDER%"=="firebase" (
    echo   %GREEN% Firebase:    http://localhost:9099 ^(Auth Emulator^)
) else (
    echo   %GREEN% Auth:        Built-in ^(admin@synaptiq.local / admin^)
)
echo.
echo   Services are running in separate terminal windows.
echo   Press Ctrl+C in those windows to stop individual services.
echo.
echo   To stop everything:      scripts\stop-local.bat
echo.

endlocal
