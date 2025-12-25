@echo off
echo ========================================
echo PRE-DEPLOYMENT VERIFICATION
echo ========================================
echo.

echo Checking if fix is applied...
echo.

REM Check if events.html has the correct reference
findstr /C:"index-Bl1_SFlI.js" "src\main\resources\static\events.html" >nul 2>&1
if errorlevel 1 (
    echo [FAIL] events.html still has wrong JavaScript reference
    echo Expected: index-Bl1_SFlI.js
    findstr /C:"index-" "src\main\resources\static\events.html"
    echo.
    echo FIX NEEDED: Update src\main\resources\static\events.html
    pause
    exit /b 1
) else (
    echo [PASS] events.html has correct JavaScript reference
)

REM Check if the actual JS file exists
if not exist "src\main\resources\static\assets\index-Bl1_SFlI.js" (
    echo [WARNING] JavaScript file not found: index-Bl1_SFlI.js
    echo.
    echo Available JavaScript files:
    dir /B "src\main\resources\static\assets\index-*.js"
    echo.
    echo You may need to update events.html to match one of these files
    pause
    exit /b 1
) else (
    echo [PASS] JavaScript file exists: index-Bl1_SFlI.js
)

REM Check if CSS file exists
if not exist "src\main\resources\static\assets\index-DRK0HUpC.css" (
    echo [WARNING] CSS file not found: index-DRK0HUpC.css
    echo.
    echo Available CSS files:
    dir /B "src\main\resources\static\assets\index-*.css"
    echo.
    echo You may need to update events.html to match one of these files
    pause
    exit /b 1
) else (
    echo [PASS] CSS file exists: index-DRK0HUpC.css
)

echo.
echo ========================================
echo ALL CHECKS PASSED!
echo ========================================
echo.
echo Your local code is ready to be built and deployed.
echo.
echo Next step: Run one of these:
echo   1. rebuild-qr-fix.bat
echo   2. Or use IntelliJ: Maven ^> Lifecycle ^> clean + package
echo.
pause

