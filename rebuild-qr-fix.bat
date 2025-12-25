@echo off
REM ============================================
REM REBUILD AND DEPLOY - QR FORM FIX
REM ============================================
echo.
echo ========================================
echo QR Code Form Fix - Rebuild Script
echo ========================================
echo.
echo This script will rebuild the application with the fixed events.html file
echo.

REM Check if we're in the right directory
if not exist "pom.xml" (
    echo [ERROR] pom.xml not found!
    echo Please run this script from the project root directory
    pause
    exit /b 1
)

echo [1/5] Checking if events.html was updated...
findstr /C:"index-Bl1_SFlI.js" "src\main\resources\static\events.html" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] events.html not updated yet!
    echo Please make sure events.html references index-Bl1_SFlI.js
    pause
    exit /b 1
)
echo [OK] events.html has correct JavaScript reference
echo.

echo [2/5] Checking for Maven...
where mvn >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Maven not found in PATH
    echo.
    echo Please use ONE of these options:
    echo.
    echo OPTION 1: Install Maven
    echo   1. Download from: https://maven.apache.org/download.cgi
    echo   2. Extract to C:\apache-maven-3.x.x
    echo   3. Add to PATH: C:\apache-maven-3.x.x\bin
    echo   4. Restart this script
    echo.
    echo OPTION 2: Use IntelliJ IDEA
    echo   1. Open this project in IntelliJ IDEA
    echo   2. Right-click pom.xml ^> Maven ^> Reload Project
    echo   3. Open Maven sidebar ^(View ^> Tool Windows ^> Maven^)
    echo   4. Expand Lifecycle folder
    echo   5. Double-click: clean
    echo   6. Double-click: package
    echo   7. Wait for BUILD SUCCESS
    echo   8. New WAR file will be in: target\ folder
    echo.
    echo OPTION 3: Use existing WAR and manual fix on server
    echo   See QR_FORM_FIX_IMMEDIATE.md for instructions
    echo.
    pause
    exit /b 1
)

echo [OK] Maven found
mvn --version
echo.

echo [3/5] Cleaning previous build...
call mvn clean
if errorlevel 1 (
    echo [ERROR] Maven clean failed!
    pause
    exit /b 1
)
echo [OK] Clean completed
echo.

echo [4/5] Building WAR file...
echo This may take 2-5 minutes...
call mvn package -DskipTests
if errorlevel 1 (
    echo [ERROR] Maven build failed!
    echo Check the error messages above
    pause
    exit /b 1
)
echo [OK] Build completed
echo.

echo [5/5] Verifying WAR file...
if not exist "target\*.war" (
    echo [ERROR] WAR file not found in target folder!
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD SUCCESS!
echo ========================================
echo.

REM Find the WAR file
for %%f in (target\*.war) do (
    echo WAR File: %%f
    echo Size: %%~zf bytes
    set WAR_FILE=%%f
)

echo.
echo ========================================
echo NEXT STEPS - DEPLOYMENT
echo ========================================
echo.
echo 1. Upload WAR file to server using WinSCP:
echo    - Server: 10.160.128.94
echo    - Username: root
echo    - Path: /opt/tanishq/applications_preprod/
echo    - File: %WAR_FILE%
echo.
echo 2. SSH to server and deploy:
echo    ssh root@10.160.128.94
echo    cd /opt/tanishq/applications_preprod
echo    sudo bash deploy-preprod.sh
echo.
echo 3. Test the fix:
echo    - Create/open an event in Events Dashboard
echo    - Download QR code
echo    - Scan QR code with mobile phone
echo    - Verify form appears with all fields
echo.
echo ========================================
echo.
pause

