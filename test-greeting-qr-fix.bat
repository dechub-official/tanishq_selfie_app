@echo off
REM ============================================
REM Greeting Feature - Quick Test Script
REM ============================================

echo ============================================
echo Greeting Feature QR Code Fix - Test Script
echo ============================================
echo.

set BASE_URL=http://localhost:8080

echo Step 1: Creating a new greeting...
curl -X POST %BASE_URL%/greetings/generate > temp_greeting.txt
set /p GREETING_ID=<temp_greeting.txt
echo Created Greeting ID: %GREETING_ID%
echo.

echo Step 2: Generating QR Code...
curl -X GET %BASE_URL%/greetings/%GREETING_ID%/qr --output qr_code.png
echo QR Code saved to: qr_code.png
echo.

echo Step 3: Checking greeting status (should be pending)...
curl -X GET %BASE_URL%/greetings/%GREETING_ID%/view
echo.
echo.

echo Step 4: To test video upload, run this command manually:
echo curl -X POST %BASE_URL%/greetings/%GREETING_ID%/upload ^
echo   -F "video=@YOUR_VIDEO.mp4" ^
echo   -F "name=Test User" ^
echo   -F "message=Test Message"
echo.

echo ============================================
echo IMPORTANT: Verify QR Code Content
echo ============================================
echo The QR code should encode ONLY: %GREETING_ID%
echo NOT a full URL like: https://celebrationsite-preprod.tanishq.co.in/greetings/%GREETING_ID%/upload
echo.
echo Use any QR scanner app to verify the content.
echo ============================================

pause

