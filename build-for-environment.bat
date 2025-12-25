@echo off
REM ============================================
REM LONG-TERM BUILD SCRIPT FOR ANY ENVIRONMENT
REM ============================================
REM Usage:
REM   build-for-environment.bat preprod
REM   build-for-environment.bat uat
REM   build-for-environment.bat prod
REM ============================================

setlocal EnableDelayedExpansion

set ENV=%1

if "%ENV%"=="" (
    echo.
    echo Usage: build-for-environment.bat [preprod^|uat^|prod]
    echo.
    echo Example:
    echo   build-for-environment.bat preprod
    echo.
    pause
    exit /b 1
)

if not "%ENV%"=="preprod" if not "%ENV%"=="uat" if not "%ENV%"=="prod" (
    echo.
    echo ERROR: Invalid environment. Must be: preprod, uat, or prod
    echo.
    pause
    exit /b 1
)

echo.
echo ============================================
echo BUILDING FOR: %ENV%
echo ============================================
echo.

REM Step 1: Build Frontend
echo [Step 1/4] Building frontend for %ENV%...
cd C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events

if not exist "package.json" (
    echo ERROR: Frontend project not found!
    echo Expected: C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
    pause
    exit /b 1
)

call npm run build:%ENV%
if %errorlevel% neq 0 (
    echo ERROR: Frontend build failed!
    pause
    exit /b 1
)

echo ✓ Frontend built successfully
echo.

REM Step 2: Backup old frontend
echo [Step 2/4] Backing up old frontend...
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources

if exist static (
    set TIMESTAMP=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
    set TIMESTAMP=!TIMESTAMP: =0!
    ren static static_backup_!TIMESTAMP!
    echo ✓ Old frontend backed up
) else (
    echo ℹ No old frontend to backup
)
echo.

REM Step 3: Copy new frontend
echo [Step 3/4] Copying new frontend to backend...
mkdir static
xcopy /E /I /Y C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist\* static\ >nul

if exist static\index.html (
    echo ✓ Frontend copied successfully
) else (
    echo ERROR: Frontend copy failed!
    pause
    exit /b 1
)
echo.

REM Step 4: Build backend WAR
echo [Step 4/4] Building backend WAR file...
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

call mvn clean package -DskipTests

if %errorlevel% neq 0 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)

echo.
echo ============================================
echo ✓ BUILD COMPLETE!
echo ============================================
echo.
echo Environment: %ENV%
echo WAR File: target\selfie-29-10-2025-1-0.0.1-SNAPSHOT.war
echo.

if "%ENV%"=="preprod" (
    echo Next: Deploy to pre-prod server
    echo   scp target\selfie-29-10-2025-1-0.0.1-SNAPSHOT.war jewdev-test@10.160.128.94:/opt/tanishq/applications_preprod/
)

if "%ENV%"=="uat" (
    echo Next: Deploy to UAT server
    echo   [Add your UAT server deployment command]
)

if "%ENV%"=="prod" (
    echo.
    echo ⚠️ WARNING: PRODUCTION BUILD
    echo Please test thoroughly before deploying!
    echo.
)

echo.
pause

