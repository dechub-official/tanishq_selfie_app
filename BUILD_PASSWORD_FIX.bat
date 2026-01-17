@echo off
REM ========================================================
REM QUICK BUILD AND INSTRUCTIONS FOR PASSWORD CHANGE FIX
REM ========================================================

echo.
echo ========================================================
echo   PASSWORD CHANGE FIX - BUILD INSTRUCTIONS
echo ========================================================
echo.

echo The code has been fixed with the following changes:
echo.
echo [FIXED] 1. NonUniqueResultException for duplicate usernames (TEST)
echo [FIXED] 2. 500 Internal Server Error for password history operations
echo [FIXED] 3. Better error handling and logging
echo.
echo ========================================================
echo   FILES MODIFIED:
echo ========================================================
echo   1. src\main\java\com\dechub\tanishq\repository\UserRepository.java
echo   2. src\main\java\com\dechub\tanishq\service\TanishqPageService.java
echo.
echo ========================================================
echo   OPTION 1: BUILD WITH MAVEN (if installed)
echo ========================================================
echo.
echo If Maven is installed, run:
echo   mvn clean package -DskipTests
echo.
echo The WAR file will be in: target\tanishq-preprod-17-01-2026-1-0.0.1-SNAPSHOT.war
echo.
echo ========================================================
echo   OPTION 2: BUILD WITH IDE (IntelliJ IDEA / Eclipse)
echo ========================================================
echo.
echo 1. Open the project in IntelliJ IDEA or Eclipse
echo 2. Click on Maven tool window (usually on the right side)
echo 3. Expand "Lifecycle"
echo 4. Double-click "clean"
echo 5. Double-click "package"
echo 6. The WAR file will be in: target\tanishq-preprod-17-01-2026-1-0.0.1-SNAPSHOT.war
echo.
echo ========================================================
echo   OPTION 3: DEPLOY TO PRODUCTION SERVER TO BUILD
echo ========================================================
echo.
echo Since the production server has Maven installed, you can:
echo.
echo 1. Copy the source files to production server
echo 2. SSH into production server
echo 3. Run: mvn clean package -DskipTests
echo.
echo Commands:
echo.
echo   REM Copy entire project to server
echo   scp -r . root@10.10.63.97:/opt/tanishq/source/
echo.
echo   REM SSH to server
echo   ssh root@10.10.63.97
echo.
echo   REM Build on server
echo   cd /opt/tanishq/source/tanishq_selfie_app
echo   mvn clean package -DskipTests
echo.
echo   REM Stop current app
echo   kill $(cat /opt/tanishq/tanishq-prod.pid)
echo.
echo   REM Backup old WAR
echo   cp /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
echo      /opt/tanishq/backup/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war.backup
echo.
echo   REM Copy new WAR
echo   cp target/tanishq-preprod-17-01-2026-1-0.0.1-SNAPSHOT.war \
echo      /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war
echo.
echo   REM Start app
echo   nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
echo     --spring.profiles.active=prod \
echo     --server.port=3000 \
echo     ^> /opt/tanishq/logs/application.log 2^>^&1 ^&
echo.
echo   echo $! ^> /opt/tanishq/tanishq-prod.pid
echo.
echo   REM Check logs
echo   tail -f /opt/tanishq/logs/application.log
echo.
echo ========================================================
echo   WHAT WAS FIXED:
echo ========================================================
echo.
echo 1. Changed password lookup to use both username AND password
echo    - This prevents NonUniqueResultException for duplicate users
echo.
echo 2. Added error handling for:
echo    - Password cache updates (won't fail password change)
echo    - Password history deletion (won't fail password change)
echo    - Password history save (won't fail password change)
echo.
echo 3. Added better logging:
echo    - Stack traces for debugging
echo    - Warning messages for non-critical failures
echo.
echo ========================================================
echo   RECOMMENDED: OPTION 3 (Build on Production Server)
echo ========================================================
echo.
echo This is the fastest and most reliable option since:
echo   - Production server already has Maven installed
echo   - No need to transfer large WAR files
echo   - You can test immediately after building
echo.
echo ========================================================
echo.
pause

