@echo off
REM =========================================================================
REM Events QR Code - Build and Deploy Script (Separated Implementation)
REM =========================================================================
REM This script builds the application with the new separated QR services
REM Date: December 18, 2025
REM Status: Ready for deployment
REM =========================================================================

echo.
echo =========================================================================
echo   BUILDING EVENTS QR CODE - SEPARATED IMPLEMENTATION
echo =========================================================================
echo.
echo This build includes:
echo   [x] EventQrCodeService - Dedicated for Events ONLY
echo   [x] QrCodeService - Dedicated for Greetings ONLY
echo   [x] Complete separation - NO MIXING
echo   [x] Attendee count auto-increment
echo.

REM Check if we're in the correct directory
if not exist "pom.xml" (
    echo ERROR: pom.xml not found!
    echo Please run this script from the project root directory.
    pause
    exit /b 1
)

echo Step 1: Cleaning previous builds...
echo =========================================================================
call mvn clean
if errorlevel 1 (
    echo ERROR: Maven clean failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Compiling with preprod profile...
echo =========================================================================
call mvn compile -Ppreprod
if errorlevel 1 (
    echo ERROR: Maven compile failed!
    echo Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo Step 3: Packaging WAR file...
echo =========================================================================
call mvn package -Ppreprod
if errorlevel 1 (
    echo ERROR: Maven package failed!
    pause
    exit /b 1
)

echo.
echo =========================================================================
echo   BUILD SUCCESSFUL!
echo =========================================================================
echo.
echo WAR file created: target\tanishq-preprod-*.war
echo.
echo Next Steps:
echo   1. Deploy WAR to Tomcat webapps folder
echo   2. Restart Tomcat server
echo   3. Test QR code generation
echo   4. Test attendee registration flow
echo.
echo Configuration:
echo   Events QR URL:    https://celebrationsite-preprod.tanishq.co.in/events/customer/
echo   Greetings QR URL: https://celebrationsite-preprod.tanishq.co.in/greetings/
echo.
echo Services:
echo   EventQrCodeService - Events ONLY (SEPARATED)
echo   QrCodeService      - Greetings ONLY (SEPARATED)
echo.
echo =========================================================================
pause

