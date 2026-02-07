@echo off
REM =====================================================
REM Quick Database Verification Launcher
REM =====================================================

echo.
echo ================================================================
echo     DATABASE VERIFICATION TOOL
echo ================================================================
echo.
echo This will verify your database and export data to CSV files
echo for comparison with Google Sheets.
echo.
echo You will need:
echo   - MySQL root password
echo   - About 2-3 minutes
echo.
pause

REM Run the PowerShell script
powershell.exe -ExecutionPolicy Bypass -File ".\verify_and_export_database.ps1"

echo.
echo ================================================================
echo Done!
echo ================================================================
echo.
pause

