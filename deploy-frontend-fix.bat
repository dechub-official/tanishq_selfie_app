@echo off
REM ========================================
REM TANISHQ PREPROD - FRONTEND FIX DEPLOYMENT
REM ========================================
REM This script:
REM 1. Copies new pre-prod frontend to backend
REM 2. Rebuilds WAR file with updated frontend
REM 3. Prepares for server deployment
REM ========================================

setlocal EnableDelayedExpansion

set FRONTEND_DIST=C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist
set BACKEND_ROOT=C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
set BACKEND_STATIC=%BACKEND_ROOT%\src\main\resources\static

echo.
echo ========================================
echo TANISHQ PREPROD - FRONTEND UPDATE
echo ========================================
echo.
echo This will:
echo  1. Backup old frontend
echo  2. Copy new pre-prod frontend
echo  3. Rebuild WAR file
echo.
echo Press Ctrl+C to cancel, or
pause
echo.

REM ========================================
REM STEP 1: Verify Frontend Build Exists
REM ========================================
echo [1/5] Checking frontend build...
if not exist "%FRONTEND_DIST%\index.html" (
    echo.
    echo ❌ ERROR: Frontend build not found!
    echo.
    echo Expected location: %FRONTEND_DIST%
    echo.
    echo Please build frontend first:
    echo   cd C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
    echo   npm run build:preprod
    echo.
    pause
    exit /b 1
)
echo ✅ Frontend build found
echo    Location: %FRONTEND_DIST%
echo.

REM ========================================
REM STEP 2: Backup Old Frontend
REM ========================================
echo [2/5] Backing up old frontend...
cd "%BACKEND_ROOT%\src\main\resources"

if exist static (
    REM Create backup folder name with timestamp
    set TIMESTAMP=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
    set TIMESTAMP=!TIMESTAMP: =0!
    set BACKUP_NAME=static_backup_!TIMESTAMP!

    echo    Creating backup: !BACKUP_NAME!
    ren static "!BACKUP_NAME!"

    if exist "!BACKUP_NAME!" (
        echo ✅ Old frontend backed up
        echo    Location: src\main\resources\!BACKUP_NAME!
    ) else (
        echo ❌ ERROR: Failed to backup old frontend
        pause
        exit /b 1
    )
) else (
    echo ℹ️  No old frontend to backup (fresh installation)
)
echo.

REM ========================================
REM STEP 3: Copy New Frontend
REM ========================================
echo [3/5] Copying new pre-prod frontend...
mkdir "%BACKEND_STATIC%"

echo    Copying files from: %FRONTEND_DIST%
echo    To: %BACKEND_STATIC%
echo.

xcopy /E /I /Y "%FRONTEND_DIST%\*" "%BACKEND_STATIC%\" >nul 2>&1

if %errorlevel% neq 0 (
    echo ❌ ERROR: Failed to copy frontend files
    echo.
    echo Troubleshooting:
    echo  1. Check frontend build exists
    echo  2. Check write permissions
    echo  3. Try manual copy
    echo.
    pause
    exit /b 1
)

REM Verify copy
if exist "%BACKEND_STATIC%\index.html" (
    echo ✅ New frontend copied successfully

    REM Count files
    for /f %%i in ('dir /b /s "%BACKEND_STATIC%\*" ^| find /c /v ""') do set FILE_COUNT=%%i
    echo    Total files copied: !FILE_COUNT!
) else (
    echo ❌ ERROR: Frontend copy verification failed
    echo    index.html not found in static folder
    pause
    exit /b 1
)
echo.

REM ========================================
REM STEP 4: Rebuild Backend WAR
REM ========================================
echo [4/5] Rebuilding backend WAR file...
echo    This may take 2-3 minutes...
echo.

cd "%BACKEND_ROOT%"

REM Clean old builds
echo    Cleaning old builds...
call mvn clean >nul 2>&1

REM Build new WAR
echo    Building WAR file (please wait)...
call mvn package -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo ❌ ERROR: Maven build failed!
    echo.
    echo Please check:
    echo  1. Maven is installed: mvn --version
    echo  2. Java is installed: java -version
    echo  3. Check pom.xml for errors
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ WAR file built successfully
echo.

REM ========================================
REM STEP 5: Verify Build
REM ========================================
echo [5/5] Verifying build...

set WAR_FILE=%BACKEND_ROOT%\target\tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war

if exist "%WAR_FILE%" (
    echo ✅ WAR file verified

    REM Get file size
    for %%F in ("%WAR_FILE%") do set WAR_SIZE=%%~zF
    set /a WAR_SIZE_MB=!WAR_SIZE! / 1048576

    echo    Location: %WAR_FILE%
    echo    Size: !WAR_SIZE_MB! MB
    echo.

    REM Check if size is reasonable (should be > 50MB with frontend)
    if !WAR_SIZE_MB! LSS 50 (
        echo ⚠️  WARNING: WAR file seems small (expected ^> 50 MB)
        echo    Frontend may not be included
        echo    Please verify manually
        echo.
    )
) else (
    echo ❌ ERROR: WAR file not found!
    echo    Expected: %WAR_FILE%
    pause
    exit /b 1
)

REM ========================================
REM SUCCESS SUMMARY
REM ========================================
echo.
echo ========================================
echo ✅ BUILD COMPLETE!
echo ========================================
echo.
echo Summary:
echo  ✅ Old frontend backed up
echo  ✅ New pre-prod frontend copied
echo  ✅ WAR file rebuilt
echo  ✅ Ready for deployment
echo.
echo WAR File:
echo  %WAR_FILE%
echo  Size: !WAR_SIZE_MB! MB
echo.
echo ========================================
echo NEXT STEPS:
echo ========================================
echo.
echo 1. Transfer WAR to server:
echo.
echo    scp target\tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war jewdev-test@10.160.128.94:/opt/tanishq/applications_preprod/
echo.
echo 2. SSH to server:
echo.
echo    ssh jewdev-test@10.160.128.94
echo.
echo 3. Deploy on server:
echo.
echo    cd /opt/tanishq/applications_preprod
echo    sudo kill -15 $(ps aux ^| grep "[j]ava.*tanishq" ^| awk '{print $2}' ^| head -1)
echo    sleep 10
echo.
echo    nohup java -jar tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war \
echo      --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod \
echo      --spring.datasource.username=root \
echo      --spring.datasource.password=Dechub#2025 \
echo      --server.port=3000 \
echo      --spring.profiles.active=preprod \
echo      ^> application.log 2^>^&1 ^&
echo.
echo 4. Verify deployment:
echo.
echo    - Open: http://celebrationsite-preprod.tanishq.co.in
echo    - Login and click "Create Event"
echo    - URL should stay on pre-prod (NOT redirect to production)
echo.
echo ========================================
echo.
echo See FRONTEND_FIX_DEPLOYMENT.md for detailed instructions
echo.
pause

