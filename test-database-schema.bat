@echo off
REM Quick Database Column Check
REM Run this batch file to check if stores table has required columns

echo === CHECKING DATABASE SCHEMA ===
echo.

REM Change these if needed
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=selfie_preprod
set DB_USER=root
set DB_PASSWORD=Dechub#2025

echo Attempting to connect to database: %DB_NAME%
echo.

mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% -D %DB_NAME% -e "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'stores' AND COLUMN_NAME IN ('region', 'level', 'abm_username', 'rbm_username', 'cee_username', 'corporate_username') ORDER BY COLUMN_NAME;"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Could not connect to database or MySQL client not installed
    echo.
    echo If MySQL client is not installed, you need to:
    echo 1. Install MySQL client
    echo 2. OR use MySQL Workbench to run the migration script
    echo 3. OR access the server directly where the database is hosted
    pause
    exit /b 1
)

echo.
echo === CHECK COMPLETE ===
echo.
echo If you see 6 column names above, the schema is correct.
echo If you see fewer or none, you need to run: sql\FIX_MISSING_COLUMNS.sql
echo.
pause

