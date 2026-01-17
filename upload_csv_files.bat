@echo off
REM =====================================================
REM UPLOAD CSV FILES TO PRODUCTION SERVER
REM =====================================================
REM This script uploads CSV files from your local machine
REM to the production server for import
REM =====================================================

setlocal enabledelayedexpansion

REM Colors
set "GREEN=[92m"
set "RED=[91m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

REM Configuration
set PROD_SERVER=10.10.63.97
set PROD_USER=root

echo.
echo %BLUE%========================================%NC%
echo %BLUE%UPLOAD CSV FILES TO PRODUCTION%NC%
echo %BLUE%========================================%NC%
echo.

REM =====================================================
REM Get CSV file locations from user
REM =====================================================

echo %YELLOW%Please provide the full path to your CSV files%NC%
echo.

set /p EVENTS_CSV="Path to events.csv: "
set /p ATTENDEES_CSV="Path to attendees.csv: "
set /p INVITEES_CSV="Path to invitees.csv: "

echo.

REM =====================================================
REM Verify files exist
REM =====================================================

echo %BLUE%Verifying files...%NC%
echo.

set FILES_OK=1

if exist "%EVENTS_CSV%" (
    echo %GREEN%✓ Found: events.csv%NC%
) else (
    echo %RED%✗ Not found: %EVENTS_CSV%%NC%
    set FILES_OK=0
)

if exist "%ATTENDEES_CSV%" (
    echo %GREEN%✓ Found: attendees.csv%NC%
) else (
    echo %RED%✗ Not found: %ATTENDEES_CSV%%NC%
    set FILES_OK=0
)

if exist "%INVITEES_CSV%" (
    echo %GREEN%✓ Found: invitees.csv%NC%
) else (
    echo %RED%✗ Not found: %INVITEES_CSV%%NC%
    set FILES_OK=0
)

echo.

if %FILES_OK%==0 (
    echo %RED%Some files are missing! Please check the paths.%NC%
    pause
    exit /b 1
)

REM =====================================================
REM Show file info
REM =====================================================

echo %BLUE%File Information:%NC%
echo.

for %%F in ("%EVENTS_CSV%") do echo   events.csv:    %%~zF bytes
for %%F in ("%ATTENDEES_CSV%") do echo   attendees.csv: %%~zF bytes
for %%F in ("%INVITEES_CSV%") do echo   invitees.csv:  %%~zF bytes

echo.

REM =====================================================
REM Confirmation
REM =====================================================

echo %YELLOW%Ready to upload files to: %PROD_SERVER%%NC%
echo.
set /p CONFIRM="Continue? (yes/no): "

if /i not "%CONFIRM%"=="yes" (
    echo %RED%Upload cancelled%NC%
    pause
    exit /b 0
)

echo.

REM =====================================================
REM Create directory on server
REM =====================================================

echo %BLUE%Step 1: Creating directory on server...%NC%
echo.

ssh %PROD_USER%@%PROD_SERVER% "mkdir -p /opt/tanishq/csv_import && chmod 755 /opt/tanishq/csv_import"

if %ERRORLEVEL% EQU 0 (
    echo %GREEN%✓ Directory created: /opt/tanishq/csv_import%NC%
) else (
    echo %RED%✗ Failed to create directory%NC%
    pause
    exit /b 1
)

echo.

REM =====================================================
REM Upload CSV files
REM =====================================================

echo %BLUE%Step 2: Uploading CSV files...%NC%
echo.

echo Uploading events.csv...
scp "%EVENTS_CSV%" %PROD_USER%@%PROD_SERVER%:/opt/tanishq/csv_import/events.csv

if %ERRORLEVEL% EQU 0 (
    echo %GREEN%✓ events.csv uploaded%NC%
) else (
    echo %RED%✗ Failed to upload events.csv%NC%
    pause
    exit /b 1
)

echo.

echo Uploading attendees.csv...
scp "%ATTENDEES_CSV%" %PROD_USER%@%PROD_SERVER%:/opt/tanishq/csv_import/attendees.csv

if %ERRORLEVEL% EQU 0 (
    echo %GREEN%✓ attendees.csv uploaded%NC%
) else (
    echo %RED%✗ Failed to upload attendees.csv%NC%
    pause
    exit /b 1
)

echo.

echo Uploading invitees.csv...
scp "%INVITEES_CSV%" %PROD_USER%@%PROD_SERVER%:/opt/tanishq/csv_import/invitees.csv

if %ERRORLEVEL% EQU 0 (
    echo %GREEN%✓ invitees.csv uploaded%NC%
) else (
    echo %RED%✗ Failed to upload invitees.csv%NC%
    pause
    exit /b 1
)

echo.

REM =====================================================
REM Verify upload
REM =====================================================

echo %BLUE%Step 3: Verifying upload...%NC%
echo.

ssh %PROD_USER%@%PROD_SERVER% "ls -lh /opt/tanishq/csv_import/*.csv"

echo.

REM =====================================================
REM Success
REM =====================================================

echo %GREEN%========================================%NC%
echo %GREEN%FILES UPLOADED SUCCESSFULLY!%NC%
echo %GREEN%========================================%NC%
echo.

echo %BLUE%Next Steps:%NC%
echo.
echo 1. SSH into production server:
echo    %YELLOW%ssh %PROD_USER%@%PROD_SERVER%%NC%
echo.
echo 2. Run the import script:
echo    %YELLOW%cd /opt/tanishq%NC%
echo    %YELLOW%chmod +x import_csv_to_mysql.sh%NC%
echo    %YELLOW%./import_csv_to_mysql.sh%NC%
echo.
echo    OR manually import (see IMPORT_CSV_TO_MYSQL.md)
echo.

echo %GREEN%Upload completed!%NC%
echo.

pause

