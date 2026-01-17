@echo off
REM =====================================================
REM SETUP SCRIPTS ON PRODUCTION SERVER
REM =====================================================
REM Uploads the import and validation scripts to server
REM =====================================================

setlocal

set "GREEN=[92m"
set "BLUE=[94m"
set "YELLOW=[93m"
set "NC=[0m"

set PROD_SERVER=10.10.63.97
set PROD_USER=root
set LOCAL_DIR=%~dp0

echo.
echo %BLUE%========================================%NC%
echo %BLUE%UPLOAD SCRIPTS TO PRODUCTION SERVER%NC%
echo %BLUE%========================================%NC%
echo.

echo %BLUE%Uploading scripts to %PROD_SERVER%...%NC%
echo.

REM Upload import script
echo Uploading import_csv_to_mysql.sh...
scp "%LOCAL_DIR%import_csv_to_mysql.sh" %PROD_USER%@%PROD_SERVER%:/opt/tanishq/
if %ERRORLEVEL% EQU 0 (
    echo %GREEN%✓ import_csv_to_mysql.sh uploaded%NC%
) else (
    echo %YELLOW%⚠ Failed to upload import_csv_to_mysql.sh%NC%
)
echo.

REM Upload validation script
echo Uploading validate_csv_files.sh...
scp "%LOCAL_DIR%validate_csv_files.sh" %PROD_USER%@%PROD_SERVER%:/opt/tanishq/
if %ERRORLEVEL% EQU 0 (
    echo %GREEN%✓ validate_csv_files.sh uploaded%NC%
) else (
    echo %YELLOW%⚠ Failed to upload validate_csv_files.sh%NC%
)
echo.

REM Make scripts executable
echo %BLUE%Making scripts executable...%NC%
ssh %PROD_USER%@%PROD_SERVER% "chmod +x /opt/tanishq/import_csv_to_mysql.sh /opt/tanishq/validate_csv_files.sh"
if %ERRORLEVEL% EQU 0 (
    echo %GREEN%✓ Scripts are now executable%NC%
) else (
    echo %YELLOW%⚠ Could not set executable permissions%NC%
)
echo.

echo %GREEN%========================================%NC%
echo %GREEN%SCRIPTS UPLOADED SUCCESSFULLY!%NC%
echo %GREEN%========================================%NC%
echo.

echo %BLUE%Next steps:%NC%
echo.
echo 1. Upload your CSV files using: upload_csv_files.bat
echo.
echo 2. SSH to server and run import:
echo    %YELLOW%ssh %PROD_USER%@%PROD_SERVER%%NC%
echo    %YELLOW%cd /opt/tanishq%NC%
echo    %YELLOW%./import_csv_to_mysql.sh%NC%
echo.

pause

