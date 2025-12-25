@echo off
REM ========================================
REM TANISHQ CELEBRATIONS - MULTI-ENVIRONMENT BUILD SYSTEM
REM ========================================
REM This script helps you build for different environments
REM Automatically copies the correct frontend build
REM ========================================

setlocal EnableDelayedExpansion

echo.
echo ========================================
echo TANISHQ CELEBRATIONS - BUILD SYSTEM
echo ========================================
echo.
echo Select Environment to Build:
echo.
echo  [1] Pre-Production (celebrationsite-preprod.tanishq.co.in)
echo  [2] UAT (uat.tanishq.co.in)
echo  [3] Production (celebrations.tanishq.co.in)
echo  [4] Exit
echo.
set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" goto preprod
if "%choice%"=="2" goto uat
if "%choice%"=="3" goto prod
if "%choice%"=="4" goto end
echo Invalid choice!
pause
exit /b 1

:preprod
set ENV_NAME=Pre-Production
set PROFILE=preprod
set FRONTEND_BUILD=npm run build:preprod
goto confirm

:uat
set ENV_NAME=UAT
set PROFILE=uat
set FRONTEND_BUILD=npm run build:uat
goto confirm

:prod
set ENV_NAME=Production
set PROFILE=prod
set FRONTEND_BUILD=npm run build:prod
goto confirm

:confirm
echo.
echo ========================================
echo BUILD CONFIRMATION
echo ========================================
echo.
echo Environment: %ENV_NAME%
echo Maven Profile: -P%PROFILE%
echo Frontend Build: %FRONTEND_BUILD%
echo.
echo This will:
echo  1. Build frontend for %ENV_NAME%
echo  2. Copy frontend to backend
echo  3. Build WAR file
echo.
set /p confirm="Continue? (y/n): "
if /i not "%confirm%"=="y" goto end

echo.
echo ========================================
echo STEP 1: Building Frontend
echo ========================================
echo.
echo Frontend location: C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
echo Command: %FRONTEND_BUILD%
echo.

cd /d C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events

if not exist "package.json" (
    echo.
    echo ❌ ERROR: Frontend project not found!
    echo.
    echo Expected location:
    echo   C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
    echo.
    echo Please update the path in this script if your frontend is elsewhere.
    echo.
    pause
    exit /b 1
)

echo Building frontend...
call %FRONTEND_BUILD%

if %errorlevel% neq 0 (
    echo.
    echo ❌ ERROR: Frontend build failed!
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ Frontend build complete!
echo.

REM Verify frontend build
if not exist "dist\index.html" (
    echo.
    echo ❌ ERROR: Frontend dist folder not found!
    echo   Expected: dist\index.html
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo STEP 2: Building Backend WAR
echo ========================================
echo.
echo Backend location: C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
echo Maven profile: %PROFILE%
echo.

cd /d C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

echo Cleaning previous builds...
call mvn clean -q

if %errorlevel% neq 0 (
    echo.
    echo ❌ ERROR: Maven clean failed!
    echo.
    pause
    exit /b 1
)

echo.
echo Building WAR file with profile: %PROFILE%
echo This will automatically:
echo  - Clean old frontend from backend
echo  - Copy new frontend to backend
echo  - Build WAR file
echo.
echo Please wait (2-3 minutes)...
echo.

call mvn package -P%PROFILE% -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo ❌ ERROR: Maven build failed!
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ BUILD SUCCESSFUL!
echo ========================================
echo.

REM Find the WAR file
for %%f in (target\tanishq-%PROFILE%-*.war) do set WAR_FILE=%%f

if not defined WAR_FILE (
    echo ❌ ERROR: WAR file not found in target folder!
    pause
    exit /b 1
)

REM Get file size
for %%F in ("%WAR_FILE%") do set WAR_SIZE=%%~zF
set /a WAR_SIZE_MB=!WAR_SIZE! / 1048576

echo Environment: %ENV_NAME%
echo WAR File: %WAR_FILE%
echo Size: !WAR_SIZE_MB! MB
echo.

if !WAR_SIZE_MB! LSS 50 (
    echo ⚠️  WARNING: WAR file seems small (expected ^> 50 MB)
    echo Frontend may not be included properly
    echo.
)

echo ========================================
echo NEXT STEPS - DEPLOYMENT
echo ========================================
echo.

if "%PROFILE%"=="preprod" (
    echo Transfer to Pre-Prod Server:
    echo   scp %WAR_FILE% jewdev-test@10.160.128.94:/opt/tanishq/applications_preprod/
    echo.
    echo Deploy on Pre-Prod Server:
    echo   ssh jewdev-test@10.160.128.94
    echo   cd /opt/tanishq/applications_preprod
    echo   sudo kill -15 $(ps aux ^| grep "[j]ava.*tanishq" ^| awk '{print $2}' ^| head -1)
    echo   nohup java -jar %WAR_FILE:target\=% --spring.profiles.active=preprod ^> application.log 2^>^&1 ^&
    echo.
    echo Verify:
    echo   http://celebrationsite-preprod.tanishq.co.in
)

if "%PROFILE%"=="uat" (
    echo Transfer to UAT Server:
    echo   scp %WAR_FILE% user@uat-server:/path/to/deployment/
    echo.
    echo Deploy on UAT Server:
    echo   ssh user@uat-server
    echo   cd /path/to/deployment
    echo   sudo kill -15 $(ps aux ^| grep "[j]ava.*tanishq" ^| awk '{print $2}' ^| head -1)
    echo   nohup java -jar %WAR_FILE:target\=% --spring.profiles.active=uat ^> application.log 2^>^&1 ^&
    echo.
    echo Verify:
    echo   http://uat.tanishq.co.in
)

if "%PROFILE%"=="prod" (
    echo.
    echo ⚠️  PRODUCTION BUILD
    echo ========================================
    echo.
    echo ⚠️  IMPORTANT: This is a PRODUCTION build!
    echo.
    echo Before deploying to production:
    echo   1. Test thoroughly in pre-prod/UAT
    echo   2. Get approval from stakeholders
    echo   3. Schedule maintenance window
    echo   4. Backup current production
    echo   5. Have rollback plan ready
    echo.
    echo Transfer to Production Server:
    echo   scp %WAR_FILE% user@prod-server:/path/to/deployment/
    echo.
    echo Deploy on Production Server:
    echo   [Follow your production deployment procedure]
    echo.
    echo Verify:
    echo   http://celebrations.tanishq.co.in
)

echo.
echo ========================================
echo BUILD SUMMARY
echo ========================================
echo.
echo ✅ Frontend built: %ENV_NAME%
echo ✅ Frontend copied to backend
echo ✅ WAR file created: %WAR_FILE%
echo ✅ Size: !WAR_SIZE_MB! MB
echo.
echo The WAR file is ready for deployment!
echo.
pause
goto end

:end
echo.
echo Exiting build system...
exit /b 0

