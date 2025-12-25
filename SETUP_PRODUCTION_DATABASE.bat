@echo off
REM ========================================================
REM PRODUCTION DATABASE SETUP - FOR PRODUCTION SERVER
REM ========================================================

echo.
echo ========================================================
echo   TANISHQ CELEBRATION - PRODUCTION DATABASE SETUP
echo ========================================================
echo.
echo   This script will create the production database
echo   Database Name: selfie_prod
echo   MySQL User: root
echo   Password: Dechub#2025
echo.
echo ========================================================
echo.

echo Checking MySQL installation...
where mysql >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] MySQL is not installed or not in PATH
    echo.
    echo Please install MySQL 8.0 first:
    echo https://dev.mysql.com/downloads/mysql/
    echo.
    pause
    exit /b 1
)
echo [OK] MySQL is installed
echo.

echo.
echo ========================================================
echo   STEP 1: Creating Production Database
echo ========================================================
echo.

REM Create database using SQL script
mysql -u root -pDechub#2025 < setup_production_database.sql

if %ERRORLEVEL% EQU 0 (
    echo [OK] Production database created successfully!
    echo.
    echo ========================================================
    echo   STEP 2: Verifying Database
    echo ========================================================
    echo.

    mysql -u root -pDechub#2025 -e "SHOW DATABASES LIKE 'selfie_prod'; USE selfie_prod; SELECT 'Production Database Ready!' as Status;"

    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ========================================================
        echo   SUCCESS! Production Database is Ready!
        echo ========================================================
        echo.
        echo   Database: selfie_prod
        echo   Host: localhost
        echo   Port: 3306
        echo   User: root
        echo   Charset: utf8mb4
        echo.
        echo   Tables will be created automatically by Spring Boot
        echo   when you first run the application.
        echo.
        echo ========================================================
        echo   NEXT STEP: Build Production WAR
        echo ========================================================
        echo.
        echo   Run: BUILD_PROD.bat
        echo.
        echo ========================================================
        echo.
    )
) else (
    echo.
    echo [ERROR] Failed to create database!
    echo.
    echo Please check:
    echo 1. MySQL service is running
    echo 2. Root password is correct: Dechub#2025
    echo 3. You have admin privileges
    echo.
    echo To manually create:
    echo   mysql -u root -p
    echo   CREATE DATABASE selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    echo.
)

echo.
pause

