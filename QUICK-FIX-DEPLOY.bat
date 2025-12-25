@echo off
REM Quick Fix Deployment for Static Files Path Issue
REM This rebuilds the WAR with the corrected events.html

echo ==========================================
echo QUICK FIX: Static Files Path Correction
echo ==========================================
echo.

cd /d "c:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app"

echo 1. Building the application (this will take a moment)...
echo    Command: mvn clean package -DskipTests
echo.

call mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo BUILD SUCCESSFUL!
    echo.
    echo ==========================================
    echo DEPLOYMENT INSTRUCTIONS:
    echo ==========================================
    echo.
    echo The fixed WAR file is in: target\
    echo.
    echo 2. Copy the new WAR to your server:
    echo    Use WinSCP or scp to upload:
    echo    target\tanishq-preprod-18-12-2025-7-0.0.1-SNAPSHOT.war
    echo    TO: /opt/tanishq/applications_preprod/
    echo.
    echo 3. On the server, stop the current application:
    echo    kill 355542
    echo.
    echo 4. Start the new version:
    echo    cd /opt/tanishq/applications_preprod
    echo    nohup java -jar tanishq-preprod-18-12-2025-7-0.0.1-SNAPSHOT.war \
    echo      --spring.profiles.active=preprod \
    echo      --server.port=3000 \
    echo      ^> application.log 2^>^&1 ^&
    echo.
    echo 5. Test in browser:
    echo    https://celebrationsite-preprod.tanishq.co.in/events/customer/TEST_25302712-2ea6-4706-bb30-d8c118693608
    echo    Should now show the attendee form!
    echo.
    echo ==========================================
    pause
) else (
    echo.
    echo BUILD FAILED!
    echo Check the error messages above
    pause
    exit /b 1
)

