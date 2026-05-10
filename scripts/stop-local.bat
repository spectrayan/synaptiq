@echo off
REM ============================================================================
REM  Synaptiq — Stop Local Development (No Docker) (Windows)
REM
REM  Stops processes started by start-local.bat:
REM    - Firebase Auth Emulator
REM    - Spring Boot backend
REM    - Angular frontend
REM
REM  Usage: scripts\stop-local.bat
REM ============================================================================

setlocal

set "GREEN=[OK]"
set "YELLOW=[WAIT]"
set "CYAN=[INFO]"
set "RESET="

echo.
echo %CYAN%Synaptiq — Stopping local development...%RESET%
echo.

REM ── Kill Firebase Auth Emulator window ──────────────────────────────────────

echo %YELLOW%[1/3]%RESET% Stopping Firebase Auth Emulator...
taskkill /FI "WINDOWTITLE eq Synaptiq Firebase*" /F >nul 2>&1
echo %GREEN%  ✓ Firebase emulator stopped%RESET%

REM ── Kill backend window ─────────────────────────────────────────────────────

echo %YELLOW%[2/3]%RESET% Stopping Spring Boot backend...
taskkill /FI "WINDOWTITLE eq Synaptiq API*" /F >nul 2>&1
REM Backup kill if running in same terminal or background process
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080" ^| findstr "LISTENING"') do taskkill /F /PID %%a >nul 2>&1
echo %GREEN%  ✓ Backend stopped%RESET%

REM ── Kill frontend window ────────────────────────────────────────────────────

echo %YELLOW%[3/3]%RESET% Stopping Angular frontend...
taskkill /FI "WINDOWTITLE eq Synaptiq Shell*" /F >nul 2>&1
REM Backup kill if running in same terminal or background process
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":4200" ^| findstr "LISTENING"') do taskkill /F /PID %%a >nul 2>&1
echo %GREEN%  ✓ Frontend stopped%RESET%

echo.
echo %GREEN%All services stopped.%RESET%
echo.

endlocal
