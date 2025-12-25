@echo off
echo ============================================
echo FINAL FIX - Dashboard and QR Code Issues
echo ============================================
echo.
echo This build includes fixes for:
echo   1. Dashboard blank page after login
echo   2. QR code showing blank page
echo.
echo ============================================
echo.

cd /d "%~dp0"

echo [1/3] Cleaning previous build...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven clean failed!
    pause
    exit /b 1
)

echo.
echo [2/3] Compiling application...
call mvn compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Packaging application...
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Package failed!
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
echo NEXT STEPS:
echo ============================================
echo.
echo 1. TEST LOCALLY (Optional):
echo    java -jar target\tanishq-preprod-*.war --spring.profiles.active=preprod
echo.
echo 2. DEPLOY TO SERVER:
echo    - Copy WAR file to server
echo    - Restart application
echo    - Test dashboard: https://celebrationsite-preprod.tanishq.co.in/events
echo    - Test QR code: Scan and verify attendee form shows
echo.
echo 3. VERIFY:
echo    - Login works
echo    - Dashboard shows (not blank)
echo    - Create event works
echo    - QR code download works
echo    - Scanning QR shows form (not blank)
echo    - Form submission works
echo.
echo See FINAL_FIX_DASHBOARD_AND_QR.md for detailed instructions
echo.
pause

