@echo off
REM ========================================================
REM TANISHQ CELEBRATION APP - BUILD SCRIPT FOR PRE-PROD
REM ========================================================

echo.
echo ========================================================
echo   TANISHQ CELEBRATION APP - PRE-PROD BUILD
echo ========================================================
echo   Date: %DATE% %TIME%
echo   Target: celebrationsite-preprod.tanishq.co.in
echo ========================================================
echo.

REM Check if Maven is installed
echo [1/6] Checking Maven installation...
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo    [ERROR] Maven is not installed or not in PATH
    echo    Please install Maven from: https://maven.apache.org/download.cgi
    echo    And add it to your system PATH
    pause
    exit /b 1
)
mvn -version | findstr "Apache Maven"
echo    [OK] Maven is installed
echo.

REM Check if Java is installed
echo [2/6] Checking Java installation...
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo    [ERROR] Java is not installed or not in PATH
    echo    Please install JDK 11 or higher
    pause
    exit /b 1
)
java -version 2>&1 | findstr "version"
echo    [OK] Java is installed
echo.

REM Clean previous build
echo [3/6] Cleaning previous build...
echo    Running: mvn clean
mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo    [ERROR] Maven clean failed
    pause
    exit /b 1
)
echo    [OK] Previous build cleaned
echo.

REM Build the application
echo [4/6] Building application (this may take 2-3 minutes)...
echo    Running: mvn install -DskipTests
mvn install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo    [ERROR] Build failed! Check errors above.
    pause
    exit /b 1
)
echo    [OK] Build completed successfully
echo.

REM Verify WAR file
echo [5/6] Verifying WAR file...
set WAR_FILE=target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
if not exist "%WAR_FILE%" (
    echo    [ERROR] WAR file not found: %WAR_FILE%
    echo    Build may have failed
    pause
    exit /b 1
)
echo    [OK] WAR file created: %WAR_FILE%

REM Show file size
for %%A in ("%WAR_FILE%") do (
    set size=%%~zA
    set /A size_mb=%%~zA/1024/1024
)
echo    File size: %size_mb% MB
echo.

REM Final instructions
echo [6/6] Build Summary
echo ========================================================
echo.
echo    BUILD SUCCESSFUL!
echo.
echo    WAR File Location:
echo    %CD%\%WAR_FILE%
echo.
echo    WAR File Size: %size_mb% MB
echo.
echo ========================================================
echo    NEXT STEPS:
echo ========================================================
echo.
echo    1. Open WinSCP or FileZilla
echo.
echo    2. Connect to server:
echo       Host: 10.160.128.94
echo       User: root
echo       Port: 22
echo.
echo    3. Upload WAR file to:
echo       /opt/tanishq/applications_preprod/
echo.
echo    4. SSH to server and run deployment script
echo.
echo    5. See COMPLETE_BUILD_AND_DEPLOY_GUIDE.md for details
echo.
echo ========================================================
echo.

REM Ask if user wants to open the target folder
echo Would you like to open the target folder? (Y/N)
set /p OPEN_FOLDER=
if /i "%OPEN_FOLDER%"=="Y" (
    explorer target
)

echo.
echo Build process complete!
echo.
pause

