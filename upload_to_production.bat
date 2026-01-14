@echo off
REM =====================================================
REM Upload Production Files to Server
REM Windows batch script to upload files via SCP
REM =====================================================

echo ========================================
echo Uploading Files to Production Server
echo ========================================
echo.

SET SERVER_IP=10.10.63.97
SET SERVER_USER=root
SET SERVER_PATH=/opt/tanishq

echo Server: %SERVER_USER%@%SERVER_IP%
echo Target: %SERVER_PATH%
echo.

REM Check if pscp is available (PuTTY SCP)
where pscp >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: pscp not found!
    echo Please install PuTTY or use WinSCP manually
    echo.
    echo Manual upload instructions:
    echo 1. Open WinSCP or your preferred SFTP client
    echo 2. Connect to %SERVER_IP% as %SERVER_USER%
    echo 3. Upload the following files to %SERVER_PATH%:
    echo.
    echo    - target\tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war
    echo      Rename to: tanishq-prod.war
    echo.
    echo    - src\main\resources\tanishqgmb-5437243a8085.p12
    echo    - src\main\resources\tanishq_selfie_app_store_data.xlsx (if exists)
    echo    - setup_production_server.sh
    echo    - setup_production_database.sql
    echo.
    pause
    exit /b 1
)

echo Using pscp to upload files...
echo.

REM Upload WAR file
echo [1/5] Uploading WAR file...
pscp -batch target\tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war %SERVER_USER%@%SERVER_IP%:%SERVER_PATH%/tanishq-prod.war
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to upload WAR file
    pause
    exit /b 1
)
echo     OK - WAR file uploaded
echo.

REM Upload Google service account key
echo [2/5] Uploading Google service account key...
pscp -batch src\main\resources\tanishqgmb-5437243a8085.p12 %SERVER_USER%@%SERVER_IP%:%SERVER_PATH%/
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to upload P12 file
    pause
    exit /b 1
)
echo     OK - P12 file uploaded
echo.

REM Upload setup script
echo [3/5] Uploading setup script...
pscp -batch setup_production_server.sh %SERVER_USER%@%SERVER_IP%:%SERVER_PATH%/
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to upload setup script
    pause
    exit /b 1
)
echo     OK - Setup script uploaded
echo.

REM Upload database script
echo [4/5] Uploading database script...
pscp -batch setup_production_database.sql %SERVER_USER%@%SERVER_IP%:%SERVER_PATH%/
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to upload database script
    pause
    exit /b 1
)
echo     OK - Database script uploaded
echo.

REM Upload Excel file (if exists)
echo [6/6] Uploading Excel file (if exists)...
if exist "tanishq_selfie_app_store_data.xlsx" (
    pscp -batch tanishq_selfie_app_store_data.xlsx %SERVER_USER%@%SERVER_IP%:%SERVER_PATH%/
    echo     OK - Excel file uploaded
) else (
    echo     SKIP - Excel file not found, upload manually if needed
)
echo.

echo ========================================
echo Upload completed successfully!
echo ========================================
echo.
echo Next steps on production server:
echo.
echo OPTION 1 - Quick Deployment (Recommended):
echo 1. SSH to server: ssh %SERVER_USER%@%SERVER_IP%
echo 2. Run: chmod +x /opt/tanishq/deploy_production.sh
echo 3. Execute: /opt/tanishq/deploy_production.sh
echo.
echo OPTION 2 - Manual Deployment:
echo 1. SSH to server: ssh %SERVER_USER%@%SERVER_IP%
echo 2. Setup server: chmod +x /opt/tanishq/setup_production_server.sh
echo 3. Run setup: /opt/tanishq/setup_production_server.sh
echo 4. Setup database: mysql -u root -p ^< /opt/tanishq/setup_production_database.sql
echo 5. Start application: systemctl start tanishq-prod
echo.
echo See PRODUCTION_DEPLOYMENT_CHECKLIST.md for detailed instructions
echo.
pause

