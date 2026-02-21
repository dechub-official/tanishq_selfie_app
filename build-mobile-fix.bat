@echo off
REM ========================================
REM BUILD SCRIPT FOR MOBILE GREETING FIX
REM ========================================

echo.
echo ================================================
echo Building Tanishq Application (PREPROD Profile)
echo ================================================
echo.

cd /d C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

echo Cleaning previous build...
call mvn clean

echo.
echo Building with PREPROD profile...
call mvn package -P preprod -DskipTests

echo.
echo ================================================
echo Build Complete!
echo ================================================
echo.
echo WAR file location:
echo target\tanishq-preprod-07-02-2026-5-0.0.1-SNAPSHOT.war
echo.
echo Next steps:
echo 1. Copy WAR file to server
echo 2. Deploy to Tomcat
echo 3. Test on mobile device
echo.
echo For detailed testing instructions, see:
echo MOBILE_GREETING_UPLOAD_COMPLETE_FIX.md
echo.
pause

