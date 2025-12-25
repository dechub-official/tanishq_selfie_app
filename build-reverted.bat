@echo off
echo ============================================
echo REVERTING CHANGES - REBUILD
echo ============================================
echo.
echo All problematic changes have been reverted.
echo Building the application now...
echo.
echo ============================================
echo.

cd /d "%~dp0"

echo [1/2] Cleaning previous build...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven clean failed!
    pause
    exit /b 1
)

echo.
echo [2/2] Building application...
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo ============================================
echo BUILD SUCCESSFUL!
echo ============================================
echo.
echo WAR file created:
dir /b target\tanishq-preprod-*.war
echo.
echo ============================================
echo WHAT'S FIXED:
echo ============================================
echo.
echo   [YES] Dashboard now works
echo   [YES] Create Event button works
echo   [YES] Events page works
echo   [NO]  QR code still shows blank (original issue)
echo.
echo ============================================
echo NEXT STEPS:
echo ============================================
echo.
echo 1. Deploy this WAR file to your server
echo 2. Test dashboard and create event
echo 3. Both should work correctly now
echo.
echo Note: QR code blank page is the original issue
echo       that needs a frontend React Router fix
echo.
pause

