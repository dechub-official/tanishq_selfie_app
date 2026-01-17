@echo off
REM =====================================================
REM DATA MIGRATION HELPER - LOCAL WINDOWS MACHINE
REM =====================================================
REM This script helps you migrate data from pre-prod to
REM production by orchestrating the process from your
REM local Windows machine
REM =====================================================

setlocal enabledelayedexpansion

REM Colors (for Windows 10+)
set "GREEN=[92m"
set "RED=[91m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

REM Configuration - UPDATE THESE VALUES!
set PREPROD_SERVER=<PREPROD_SERVER_IP>
set PROD_SERVER=10.10.63.97
set MYSQL_USER=root
set MYSQL_PASS=Nagaraj@07
set PREPROD_DB=selfie_preprod
set PROD_DB=selfie_prod

echo.
echo %BLUE%========================================%NC%
echo %BLUE%DATA MIGRATION: PRE-PROD TO PRODUCTION%NC%
echo %BLUE%========================================%NC%
echo.

REM =====================================================
REM STEP 1: Export data from pre-prod
REM =====================================================

echo %BLUE%STEP 1: Exporting data from Pre-Prod Server...%NC%
echo.

REM Create export command
set EXPORT_CMD=mysqldump -u %MYSQL_USER% -p%MYSQL_PASS% %PREPROD_DB% --no-create-info --complete-insert --skip-triggers events attendees invitees bride_details users stores product_details greetings rivaah rivaah_users abm_login cee_login rbm_login user_details password_history > /tmp/preprod_data_export.sql

echo Connecting to Pre-Prod server: %PREPROD_SERVER%
echo.

REM Execute export on pre-prod server
ssh root@%PREPROD_SERVER% "%EXPORT_CMD%"

if %ERRORLEVEL% NEQ 0 (
    echo %RED%ERROR: Failed to export data from pre-prod server!%NC%
    echo %YELLOW%Make sure you can SSH to the pre-prod server%NC%
    pause
    exit /b 1
)

echo %GREEN%SUCCESS: Data exported from pre-prod%NC%
echo.

REM =====================================================
REM STEP 2: Transfer file to production
REM =====================================================

echo %BLUE%STEP 2: Transferring data to Production Server...%NC%
echo.

REM Transfer via pre-prod to prod (or download then upload)
ssh root@%PREPROD_SERVER% "scp /tmp/preprod_data_export.sql root@%PROD_SERVER%:/opt/tanishq/preprod_data_export.sql"

if %ERRORLEVEL% NEQ 0 (
    echo %RED%ERROR: Failed to transfer data to production server!%NC%
    echo %YELLOW%Trying alternative method: Download to local then upload...%NC%

    REM Download to local
    scp root@%PREPROD_SERVER%:/tmp/preprod_data_export.sql "%TEMP%\preprod_data_export.sql"

    REM Upload to production
    scp "%TEMP%\preprod_data_export.sql" root@%PROD_SERVER%:/opt/tanishq/preprod_data_export.sql

    if !ERRORLEVEL! NEQ 0 (
        echo %RED%ERROR: Failed to transfer file!%NC%
        pause
        exit /b 1
    )
)

echo %GREEN%SUCCESS: Data transferred to production server%NC%
echo.

REM =====================================================
REM STEP 3: Stop production application
REM =====================================================

echo %BLUE%STEP 3: Stopping Production Application...%NC%
echo.

ssh root@%PROD_SERVER% "kill $(cat /opt/tanishq/tanishq-prod.pid 2>/dev/null) 2>/dev/null || true"

echo %GREEN%Production application stopped%NC%
echo.

REM Wait a few seconds
timeout /t 5 /nobreak >nul

REM =====================================================
REM STEP 4: Backup current production database
REM =====================================================

echo %BLUE%STEP 4: Backing up current Production Database...%NC%
echo.

set BACKUP_FILE=backup_before_import_%date:~10,4%%date:~4,2%%date:~7,2%_%time:~0,2%%time:~3,2%%time:~6,2%.sql
set BACKUP_FILE=%BACKUP_FILE: =0%

ssh root@%PROD_SERVER% "mysqldump -u %MYSQL_USER% -p%MYSQL_PASS% %PROD_DB% > /opt/tanishq/%BACKUP_FILE%"

echo %GREEN%Backup created: %BACKUP_FILE%%NC%
echo.

REM =====================================================
REM STEP 5: Import data to production
REM =====================================================

echo %BLUE%STEP 5: Importing data to Production Database...%NC%
echo.
echo %YELLOW%This may take a few minutes depending on data size...%NC%
echo.

ssh root@%PROD_SERVER% "mysql -u %MYSQL_USER% -p%MYSQL_PASS% %PROD_DB% < /opt/tanishq/preprod_data_export.sql"

if %ERRORLEVEL% NEQ 0 (
    echo %RED%ERROR: Failed to import data!%NC%
    echo %YELLOW%You can restore from backup: %BACKUP_FILE%%NC%
    pause
    exit /b 1
)

echo %GREEN%SUCCESS: Data imported to production database%NC%
echo.

REM =====================================================
REM STEP 6: Verify imported data
REM =====================================================

echo %BLUE%STEP 6: Verifying imported data...%NC%
echo.

ssh root@%PROD_SERVER% "mysql -u %MYSQL_USER% -p%MYSQL_PASS% -e \"USE %PROD_DB%; SELECT 'events' as table_name, COUNT(*) as count FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees UNION ALL SELECT 'invitees', COUNT(*) FROM invitees UNION ALL SELECT 'bride_details', COUNT(*) FROM bride_details UNION ALL SELECT 'users', COUNT(*) FROM users UNION ALL SELECT 'stores', COUNT(*) FROM stores UNION ALL SELECT 'product_details', COUNT(*) FROM product_details;\""

echo.
echo %GREEN%Data verification complete!%NC%
echo.

REM =====================================================
REM STEP 7: Restart production application
REM =====================================================

echo %BLUE%STEP 7: Restarting Production Application...%NC%
echo.

REM Clear logs
ssh root@%PROD_SERVER% "echo '' > /opt/tanishq/logs/application.log"

REM Start application
ssh root@%PROD_SERVER% "cd /opt/tanishq && nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid"

echo %GREEN%Application started!%NC%
echo.

REM Wait for application to start
echo Waiting for application to start (30 seconds)...
timeout /t 30 /nobreak >nul

REM =====================================================
REM STEP 8: Verify application is running
REM =====================================================

echo %BLUE%STEP 8: Verifying Application Status...%NC%
echo.

ssh root@%PROD_SERVER% "ps -p $(cat /opt/tanishq/tanishq-prod.pid) && curl -I http://localhost:3000/ | head -1"

echo.

REM =====================================================
REM COMPLETION
REM =====================================================

echo.
echo %GREEN%========================================%NC%
echo %GREEN%MIGRATION COMPLETED SUCCESSFULLY!%NC%
echo %GREEN%========================================%NC%
echo.
echo %BLUE%Summary:%NC%
echo   - Data exported from Pre-Prod
echo   - Data transferred to Production
echo   - Backup created: %BACKUP_FILE%
echo   - Data imported to Production
echo   - Application restarted on port 3000
echo.
echo %YELLOW%Next Steps:%NC%
echo   1. Test the production website
echo   2. Verify user can see their 2 months of data
echo   3. Check application logs for any errors
echo      Command: ssh root@%PROD_SERVER% "tail -f /opt/tanishq/logs/application.log"
echo.
echo %GREEN%Migration script completed!%NC%
echo.

pause

