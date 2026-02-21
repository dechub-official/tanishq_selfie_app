@echo off
echo ========================================
echo Building Tanishq Preprod WAR
echo ========================================
echo.

echo Cleaning previous build...
call mvnw.cmd clean

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven clean failed
    exit /b 1
)

echo.
echo Building WAR file with preprod profile...
call mvnw.cmd package -DskipTests -P preprod

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven build failed
    exit /b 1
)

echo.
echo ========================================
echo Build completed successfully!
echo WAR file location: target\tanishq-preprod-30-01-2026-2-0.0.1-SNAPSHOT.war
echo ========================================

