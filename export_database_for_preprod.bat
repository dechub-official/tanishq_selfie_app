@echo off
REM ================================================
REM Database Export for Pre-Production Migration
REM ================================================
REM This script exports the local tanishq database
REM for migration to pre-production server

echo ================================================
echo Database Export for Pre-Production Migration
echo ================================================
echo.

REM Configuration
set DB_USER=nagaraj_jadar
set DB_PASS=Nagaraj07
set DB_NAME=tanishq
set BACKUP_DIR=database_backup
set DATE_STAMP=%date:~-4,4%%date:~-7,2%%date:~-10,2%
set TIME_STAMP=%time:~0,2%%time:~3,2%%time:~6,2%
set TIME_STAMP=%TIME_STAMP: =0%
set BACKUP_FILE=tanishq_backup_%DATE_STAMP%_%TIME_STAMP%.sql

echo Database: %DB_NAME%
echo User: %DB_USER%
echo Backup Directory: %BACKUP_DIR%
echo Backup File: %BACKUP_FILE%
echo.

REM Create backup directory if it doesn't exist
echo [Step 1/4] Creating backup directory...
if not exist "%BACKUP_DIR%" (
    mkdir "%BACKUP_DIR%"
    echo Backup directory created: %BACKUP_DIR%
) else (
    echo Backup directory already exists: %BACKUP_DIR%
)
echo.

REM Check if mysqldump is available
echo [Step 2/4] Checking for mysqldump...
where mysqldump >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: mysqldump not found in PATH
    echo Trying common MySQL installation paths...

    if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" (
        set "MYSQLDUMP=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe"
        echo Found: !MYSQLDUMP!
    ) else if exist "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysqldump.exe" (
        set "MYSQLDUMP=C:\Program Files\MySQL\MySQL Server 5.7\bin\mysqldump.exe"
        echo Found: !MYSQLDUMP!
    ) else (
        echo.
        echo ERROR: mysqldump.exe not found!
        echo Please install MySQL or add it to PATH
        echo.
        pause
        exit /b 1
    )
) else (
    set MYSQLDUMP=mysqldump
    echo mysqldump found in PATH
)
echo.

REM Export the database
echo [Step 3/4] Exporting database...
echo This may take a few minutes depending on database size...
echo.

"%MYSQLDUMP%" -u %DB_USER% -p%DB_PASS% ^
    --databases %DB_NAME% ^
    --add-drop-database ^
    --routines ^
    --triggers ^
    --events ^
    --single-transaction ^
    --quick ^
    --lock-tables=false ^
    --result-file="%BACKUP_DIR%\%BACKUP_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo Export completed successfully!
) else (
    echo.
    echo ================================================
    echo ERROR: Database export failed!
    echo ================================================
    echo.
    echo Please check:
    echo - MySQL service is running
    echo - Username and password are correct: %DB_USER%
    echo - Database exists: %DB_NAME%
    echo.
    pause
    exit /b 1
)
echo.

REM Verify the file was created
echo [Step 4/4] Verifying export file...
if exist "%BACKUP_DIR%\%BACKUP_FILE%" (
    for %%A in ("%BACKUP_DIR%\%BACKUP_FILE%") do (
        set FILE_SIZE=%%~zA
    )
    echo.
    echo ================================================
    echo SUCCESS: Database exported successfully!
    echo ================================================
    echo.
    echo File Details:
    echo - Location: %CD%\%BACKUP_DIR%\%BACKUP_FILE%
    echo - Size: %FILE_SIZE% bytes
    echo.
    echo ================================================
    echo NEXT STEPS - Pre-Production Migration
    echo ================================================
    echo.
    echo 1. Connect to FortiClient VPN
    echo    - Use your VPN credentials
    echo.
    echo 2. Transfer SQL file to Pre-Prod Server
    echo    - Open WinSCP
    echo    - Hostname: 10.160.128.94
    echo    - Username: nishal
    echo    - Auth: Private key from downloads
    echo    - Upload file to: /tmp/
    echo.
    echo 3. Import on Pre-Prod Server
    echo    - Open PuTTY
    echo    - Connect to: 10.160.128.94
    echo    - Username: nishal
    echo    - Run these commands:
    echo.
    echo    cd /tmp
    echo    mysql -u tanishq_preprod -p tanishq_preprod ^< %BACKUP_FILE%
    echo.
    echo 4. Verify Import
    echo    - Login to MySQL: mysql -u tanishq_preprod -p tanishq_preprod
    echo    - Check tables: SHOW TABLES;
    echo    - Check data: SELECT COUNT(*) FROM [table_name];
    echo.
    echo 5. Cleanup
    echo    - Remove file from server: rm /tmp/%BACKUP_FILE%
    echo.
    echo ================================================
    echo.
) else (
    echo.
    echo ERROR: Backup file was not created!
    echo Expected file: %BACKUP_DIR%\%BACKUP_FILE%
    echo.
)

REM List all backup files
echo Available backup files in %BACKUP_DIR%:
echo.
dir /B "%BACKUP_DIR%\*.sql"
echo.

echo Press any key to exit...
pause >nul

