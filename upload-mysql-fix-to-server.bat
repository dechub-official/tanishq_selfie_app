@echo off
REM Upload MySQL Display Fix to Server and Execute
REM This will fix the messy table display issue on the server

echo ========================================
echo MySQL Display Fix - Upload to Server
echo ========================================
echo.

set SERVER_USER=jewdev-test
set SERVER_IP=10-160-128-94
set SERVER=%SERVER_USER%@%SERVER_IP%

echo Server: %SERVER%
echo.
echo This script will:
echo   1. Upload fix-mysql-display-server.sh to the server
echo   2. Make it executable
echo   3. Run it to fix MySQL display
echo.

REM Check if WinSCP or pscp is available
where winscp.com >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Using WinSCP...
    winscp.com /command "open scp://%SERVER%" "put fix-mysql-display-server.sh /home/%SERVER_USER%/" "exit"
    goto RUN_SCRIPT
)

where pscp >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Using PSCP...
    pscp fix-mysql-display-server.sh %SERVER%:/home/%SERVER_USER%/
    goto RUN_SCRIPT
)

echo.
echo ========================================
echo ERROR: No SCP tool found!
echo ========================================
echo.
echo Please install one of these:
echo   - WinSCP: https://winscp.net/
echo   - PuTTY (includes pscp): https://www.putty.org/
echo.
echo OR manually upload fix-mysql-display-server.sh to the server
echo.
pause
goto END

:RUN_SCRIPT
echo.
echo ========================================
echo File uploaded successfully!
echo ========================================
echo.
echo Now connecting to server to run the fix...
echo.

REM Check if plink is available
where plink >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Running fix script on server...
    plink %SERVER% "chmod +x ~/fix-mysql-display-server.sh && bash ~/fix-mysql-display-server.sh"
    echo.
    echo ========================================
    echo FIX APPLIED!
    echo ========================================
    echo.
    echo Now test by connecting to MySQL:
    echo   ssh %SERVER%
    echo   mysql -u root -p applications_preprod
    echo.
) else (
    echo.
    echo Plink not found. Please manually run on server:
    echo.
    echo   ssh %SERVER%
    echo   chmod +x ~/fix-mysql-display-server.sh
    echo   bash ~/fix-mysql-display-server.sh
    echo.
)

:END
echo.
echo ========================================
echo ALTERNATIVE: Quick Manual Fix
echo ========================================
echo.
echo If the above didn't work, connect to your server and run:
echo.
echo   ssh %SERVER%
echo.
echo Then copy-paste this command:
echo.
echo cat ^> ~/.my.cnf ^<^< 'EOF'
echo [mysql]
echo table
echo column-names
echo pager=less -S -n -i -F -X
echo auto-rehash
echo show-warnings
echo prompt='mysql [\d]^> '
echo EOF
echo.
echo Then reconnect to MySQL:
echo   mysql -u root -p applications_preprod
echo.
echo ========================================
echo.
pause

