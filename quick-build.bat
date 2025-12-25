@echo off
echo ========================================
echo Building Tanishq Celebration App
echo With URL Fixes Applied
echo ========================================
echo.

cd /d c:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

echo Cleaning previous build...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven clean failed
    pause
    exit /b 1
)

echo.
echo Building application (skip tests for speed)...
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven build failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.
echo WAR file location:
dir /b target\*.war
echo.
echo Full path:
echo %CD%\target\
echo.
echo Next: Deploy this WAR file to your pre-prod server
pause

