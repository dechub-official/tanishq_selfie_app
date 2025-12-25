@echo off
echo ========================================
echo QR CODE FIX - BUILD SCRIPT
echo ========================================
echo.

cd /d "%~dp0"

echo [1/3] Cleaning previous build...
call mvn clean

echo.
echo [2/3] Compiling application...
call mvn compile -DskipTests

echo.
echo [3/3] Packaging application...
call mvn package -DskipTests

echo.
echo ========================================
echo BUILD COMPLETE!
echo ========================================
echo.
echo WAR file location:
dir /b target\*.war
echo.
echo Next Steps:
echo 1. Deploy the WAR file to your server
echo 2. Test QR code scanning
echo 3. Refer to: QR_BLANK_PAGE_COMPLETE_FIX_AND_TEST_GUIDE.md
echo.
pause

