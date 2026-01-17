@echo off
REM ========================================================
REM UPLOAD SOURCE FILES TO PRODUCTION SERVER
REM ========================================================

echo.
echo ========================================================
echo   UPLOADING SOURCE FILES TO PRODUCTION SERVER
echo ========================================================
echo.

set SERVER=root@10.10.63.97
set LOCAL_DIR=%~dp0
set REMOTE_DIR=/opt/tanishq/source/

echo Local Directory: %LOCAL_DIR%
echo Remote Server: %SERVER%
echo Remote Directory: %REMOTE_DIR%
echo.
echo ========================================================
echo.

echo [1/3] Checking if SCP is available...
where scp >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo    [ERROR] SCP is not available
    echo    Please install OpenSSH Client or use WinSCP
    echo.
    echo    Alternative methods:
    echo    1. Use WinSCP GUI: https://winscp.net/
    echo    2. Use FileZilla
    echo    3. Enable OpenSSH Client in Windows Settings
    pause
    exit /b 1
)
echo    [OK] SCP is available
echo.

echo [2/3] Uploading source files to production server...
echo    This may take a few minutes...
echo.

REM Upload only source files (exclude target, logs, etc.)
scp -r ^
    src ^
    pom.xml ^
    deploy_password_fix.sh ^
    FIX_PASSWORD_CHANGE_ERROR.md ^
    %SERVER%:%REMOTE_DIR%tanishq_selfie_app/

if %ERRORLEVEL% NEQ 0 (
    echo    [ERROR] Upload failed
    echo.
    echo    Troubleshooting:
    echo    1. Check if you can SSH to the server: ssh %SERVER%
    echo    2. Check if the remote directory exists
    echo    3. Check your network connection
    pause
    exit /b 1
)

echo.
echo    [OK] Files uploaded successfully
echo.

echo [3/3] Making deploy script executable on server...
ssh %SERVER% "chmod +x %REMOTE_DIR%tanishq_selfie_app/deploy_password_fix.sh"

if %ERRORLEVEL% NEQ 0 (
    echo    [WARNING] Could not make script executable
    echo    You may need to run: chmod +x deploy_password_fix.sh on server
) else (
    echo    [OK] Deploy script is executable
)

echo.
echo ========================================================
echo   UPLOAD COMPLETE!
echo ========================================================
echo.
echo Next steps:
echo.
echo 1. SSH to production server:
echo    ssh %SERVER%
echo.
echo 2. Run the deployment script:
echo    cd %REMOTE_DIR%tanishq_selfie_app
echo    ./deploy_password_fix.sh
echo.
echo 3. Wait for deployment to complete
echo.
echo 4. Test the password change functionality
echo.
echo Alternative: Run deployment directly from here:
echo    ssh %SERVER% "cd %REMOTE_DIR%tanishq_selfie_app && ./deploy_password_fix.sh"
echo.
echo ========================================================
echo.

set /p DEPLOY="Do you want to deploy now? (Y/N): "
if /i "%DEPLOY%"=="Y" (
    echo.
    echo Deploying...
    ssh %SERVER% "cd %REMOTE_DIR%tanishq_selfie_app && ./deploy_password_fix.sh"

    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ========================================================
        echo   DEPLOYMENT SUCCESSFUL!
        echo ========================================================
        echo.
        echo The password change fix has been deployed.
        echo.
        echo Test it now:
        echo   1. Go to: https://celebrations.tanishq.co.in/events/reset-password
        echo   2. Try changing password for store "TEST" or "ABH"
        echo   3. Should see "Password changed successfully"
        echo.
    ) else (
        echo.
        echo [ERROR] Deployment failed
        echo Please check the error messages above
    )
) else (
    echo.
    echo Deployment skipped. You can deploy later using:
    echo    ssh %SERVER% "cd %REMOTE_DIR%tanishq_selfie_app && ./deploy_password_fix.sh"
)

echo.
pause

