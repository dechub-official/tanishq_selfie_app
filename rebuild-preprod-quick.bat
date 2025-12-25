@echo off
echo ========================================
echo Quick Rebuild for Pre-Prod
echo ========================================
echo.

echo Step 1: Copy updated events.html to target...
xcopy /Y "src\main\resources\static\events.html" "target\classes\static\" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Failed to copy events.html
    goto :end
)
echo [OK] events.html copied

echo.
echo Step 2: Update WAR file with new events.html...
cd target\classes\static
if not exist events.html (
    echo [ERROR] events.html not found in target
    cd ..\..\..
    goto :end
)

cd ..\..\..
echo [INFO] Rebuilding WAR file...

REM Check if 7-Zip is available
where 7z >nul 2>&1
if errorlevel 1 (
    echo [WARNING] 7-Zip not found, trying to use Maven...
    echo [INFO] Please install Maven or 7-Zip to rebuild WAR
    echo.
    echo Alternative: Use IntelliJ IDEA to rebuild the project
    echo   1. Right-click on pom.xml
    echo   2. Maven -^> Reimport
    echo   3. Maven -^> Lifecycle -^> clean
    echo   4. Maven -^> Lifecycle -^> package
    goto :end
)

REM Create new WAR with updated files
cd target
set WAR_NAME=tanishq-preprod-18-12-2025-fixed.war
echo [INFO] Creating %WAR_NAME%...

7z a -tzip "%WAR_NAME%" * -r >nul
if errorlevel 1 (
    echo [ERROR] Failed to create WAR file
    cd ..
    goto :end
)

echo [OK] WAR file created: target\%WAR_NAME%
cd ..

echo.
echo ========================================
echo SUCCESS! WAR file ready for deployment
echo ========================================
echo.
echo Next steps:
echo 1. Upload target\%WAR_NAME% to server using WinSCP
echo 2. SSH to server and run: sudo bash deploy-preprod.sh
echo 3. Test: https://celebrationsite-preprod.tanishq.co.in/events/customer/TEST_...
echo.

:end
pause

