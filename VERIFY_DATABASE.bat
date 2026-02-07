@echo off
REM =====================================================
REM MySQL Database Verification Launcher
REM =====================================================

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║         MySQL Database Verification Tool                      ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

echo Starting verification...
echo.

REM Run PowerShell script
powershell -ExecutionPolicy Bypass -File "%~dp0verify_database.ps1"

pause

