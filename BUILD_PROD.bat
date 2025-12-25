@echo off
REM ========================================================
REM TANISHQ CELEBRATION APP - BUILD SCRIPT FOR PRODUCTION
REM ========================================================

echo.
echo ========================================================
echo   TANISHQ CELEBRATION APP - PRODUCTION BUILD
echo ========================================================
echo   Date: %DATE% %TIME%
echo   Target: celebrations.tanishq.co.in
echo   Database: selfie_prod
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

REM Build the application with production profile
echo [4/6] Building application with PRODUCTION profile (this may take 2-3 minutes)...
echo    Running: mvn clean package -Pprod -DskipTests
mvn clean package -Pprod -DskipTests
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
for /f "delims=" %%a in ('dir /b /od target\*.war 2^>nul') do set "WAR_FILE=target\%%a"
if not defined WAR_FILE (
    echo    [ERROR] WAR file not found in target directory
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
echo    BUILD SUCCESSFUL - PRODUCTION ENVIRONMENT!
echo.
echo    WAR File Location:
echo    %CD%\%WAR_FILE%
echo.
echo    WAR File Size: %size_mb% MB
echo    Environment: PRODUCTION
echo    Database: selfie_prod (MySQL on Production Server)
echo    Port: 3001
echo    URL: https://celebrations.tanishq.co.in
echo.
echo ========================================================
echo    NEXT STEPS - PRODUCTION DEPLOYMENT:
echo ========================================================
echo.
echo    1. Ensure MySQL is installed on production server:
echo       - Database name: selfie_prod
echo       - User: root
echo       - Password: Dechub#2025
echo.
echo    2. Open WinSCP or FileZilla and connect to PRODUCTION server
echo       Host: [Your Production Server IP]
echo       Username: [Your Username]
echo       Port: 22
echo.
echo    3. Create required directories on production server:
echo       sudo mkdir -p /opt/tanishq/storage/selfie_images
echo       sudo mkdir -p /opt/tanishq/storage/bride_uploads
echo       sudo chmod -R 755 /opt/tanishq
echo.
echo    4. Upload the WAR file to:
echo       /opt/tanishq/
echo.
echo    5. Upload required key files to:
echo       - /opt/tanishq/tanishqgmb-5437243a8085.p12
echo       - /opt/tanishq/event-images-469618-32e65f6d62b3.p12
echo.
echo    6. Stop existing Tomcat (if running):
echo       sudo systemctl stop tomcat
echo.
echo    7. Deploy the WAR file:
echo       sudo cp /opt/tanishq/[war-file-name].war /opt/tomcat/webapps/ROOT.war
echo.
echo    8. Start Tomcat:
echo       sudo systemctl start tomcat
echo.
echo    9. Monitor logs:
echo       tail -f /opt/tomcat/logs/catalina.out
echo.
echo    10. Verify deployment:
echo        https://celebrations.tanishq.co.in
echo.
echo ========================================================
echo    WARNING: THIS IS A PRODUCTION BUILD!
echo    Ensure you have tested in preprod first!
echo ========================================================
echo.
pause

