@echo off
REM ==========================================
REM QUICK BUILD AND DEPLOY - HARDCODED URL FIX
REM ==========================================
REM Date: December 5, 2025
REM Purpose: Build WAR with fixed URLs and prepare for deployment
REM ==========================================

echo.
echo ════════════════════════════════════════════════════════
echo  BUILDING WAR FILE WITH HARDCODED URL FIXES
echo ════════════════════════════════════════════════════════
echo.
echo Changes Applied:
echo  [x] Port changed from 3002 to 3000
echo  [x] QR Code URL changed to IP-based
echo  [x] Old: http://celebrationsite-preprod.tanishq.co.in/events/customer/
echo  [x] New: http://10.160.128.94:3000/events/customer/
echo.
echo ════════════════════════════════════════════════════════
echo.

cd /d "%~dp0"

REM Step 1: Clean previous build
echo [1/3] Cleaning previous build...
call mvn clean
if errorlevel 1 (
    echo ERROR: Maven clean failed!
    pause
    exit /b 1
)
echo.

REM Step 2: Build WAR file (skip tests for faster build)
echo [2/3] Building WAR file (skipping tests)...
call mvn package -DskipTests
if errorlevel 1 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)
echo.

REM Step 3: Verify WAR file
echo [3/3] Verifying WAR file...
if exist "target\tanishq-preprod-05-12-2025-1-0.0.1-SNAPSHOT.war" (
    for %%A in ("target\tanishq-preprod-05-12-2025-1-0.0.1-SNAPSHOT.war") do (
        echo.
        echo ════════════════════════════════════════════════════════
        echo  BUILD SUCCESSFUL!
        echo ════════════════════════════════════════════════════════
        echo.
        echo WAR File: %%~nxA
        echo Size: %%~zA bytes
        echo Location: %%~fA
        echo.
        echo ════════════════════════════════════════════════════════
        echo  NEXT STEPS
        echo ════════════════════════════════════════════════════════
        echo.
        echo 1. Transfer WAR to server:
        echo    scp target\tanishq-preprod-05-12-2025-1-0.0.1-SNAPSHOT.war ^
        echo        jewdev-test@10.160.128.94:/opt/tanishq/applications_preprod/
        echo.
        echo 2. SSH to server:
        echo    ssh jewdev-test@10.160.128.94
        echo.
        echo 3. Deploy using:
        echo    bash deploy_new_build.sh
        echo.
        echo    OR manually:
        echo    cd /opt/tanishq/applications_preprod
        echo    sudo kill -15 $(ps aux ^| grep "[j]ava.*tanishq" ^| awk '{print $2}' ^| head -1)
        echo    sleep 10
        echo    nohup java -jar tanishq-preprod-05-12-2025-1-0.0.1-SNAPSHOT.war ^
        echo      --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false^&serverTimezone=UTC ^
        echo      --spring.datasource.username=root ^
        echo      --spring.datasource.password=Dechub#2025 ^
        echo      --server.port=3000 ^
        echo      --spring.profiles.active=preprod ^
        echo      ^> application.log 2^>^&1 ^&
        echo.
        echo 4. Test the fix:
        echo    - Create an event
        echo    - Download QR code
        echo    - Verify QR URL contains: http://10.160.128.94:3000/events/customer/
        echo.
        echo ════════════════════════════════════════════════════════
    )
) else (
    echo.
    echo ERROR: WAR file not found!
    echo Expected: target\tanishq-preprod-05-12-2025-1-0.0.1-SNAPSHOT.war
    echo.
    pause
    exit /b 1
)

echo.
pause

