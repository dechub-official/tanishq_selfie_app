# Database Migration Guide - Localhost to Pre-Production

## Overview
This guide will help you migrate your database from localhost to the pre-production server (10.160.128.94).

**Source Database:**
- Host: localhost
- Database: tanishq
- Username: nagaraj_jadar
- Password: Nagaraj07

**Target Database:**
- Host: 10.160.128.94
- Database: tanishq_preprod
- Username: tanishq_preprod
- Password: [Your pre-prod DB password]

---

## Prerequisites

1. **MySQL Tools Installed:**
   - `mysqldump` (for export)
   - `mysql` client (for import)
   - Usually installed with MySQL

2. **Network Access:**
   - FortiClient VPN connected
   - Access to pre-prod server 10.160.128.94

3. **Database Credentials:**
   - Local database credentials (above)
   - Pre-prod database credentials

---

## Method 1: Export and Import via SQL Dump (Recommended)

### Step 1: Export Database from Localhost

Open Command Prompt (cmd) on your Windows machine:

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

REM Create backup directory if not exists
if not exist "database_backup" mkdir database_backup

REM Export the database
mysqldump -u nagaraj_jadar -pNagaraj07 --databases tanishq --add-drop-database --routines --triggers --events > database_backup\tanishq_backup_%date:~-4,4%%date:~-7,2%%date:~-10,2%.sql

echo Database exported successfully!
```

**Note:** This will create a file like `tanishq_backup_20251202.sql` in the `database_backup` folder.

### Step 2: Verify the Export

```cmd
REM Check if file was created
dir database_backup\tanishq_backup_*.sql
```

You should see the SQL file with today's date.

### Step 3: Transfer SQL File to Pre-Prod Server

**Option A: Using WinSCP**
1. Connect FortiClient VPN
2. Open WinSCP
   - Hostname: 10.160.128.94
   - Username: nishal
   - Authentication: Private key
3. Navigate to `/tmp/` on server
4. Upload the SQL backup file from `database_backup\` folder

**Option B: Using Command Line (if you have SSH/SCP)**
```cmd
scp database_backup\tanishq_backup_20251202.sql nishal@10.160.128.94:/tmp/
```

### Step 4: Import Database on Pre-Prod Server

**Connect to server via PuTTY:**

```bash
# Connect to 10.160.128.94 via PuTTY
# Username: nishal

# Switch to root (if needed for MySQL access)
sudo su root

# Navigate to tmp directory
cd /tmp

# Verify SQL file exists
ls -lh tanishq_backup_*.sql

# Import the database
# Replace 'YOUR_PREPROD_PASSWORD' with actual password
mysql -u tanishq_preprod -p tanishq_preprod < tanishq_backup_20251202.sql

# When prompted, enter the pre-prod database password
```

**Alternative: If the SQL file contains CREATE DATABASE:**
```bash
# If your dump includes "CREATE DATABASE tanishq"
# You may need to edit the SQL file first or import differently

# Option 1: Import and let it create the database
mysql -u tanishq_preprod -p < tanishq_backup_20251202.sql

# Then rename the database if needed
mysql -u tanishq_preprod -p
# Inside MySQL:
# CREATE DATABASE tanishq_preprod;
# Then manually copy tables, OR:
```

### Step 5: Verify Import

```bash
# Login to MySQL
mysql -u tanishq_preprod -p tanishq_preprod

# Inside MySQL, run these commands:
SHOW TABLES;

# Check row counts for key tables
SELECT COUNT(*) FROM store_managers;
SELECT COUNT(*) FROM stores;
SELECT COUNT(*) FROM events;
SELECT COUNT(*) FROM attendees;
# ... check other important tables

# Exit MySQL
EXIT;
```

### Step 6: Cleanup

```bash
# Remove the SQL file from /tmp for security
rm /tmp/tanishq_backup_20251202.sql

# Exit root (if you used it)
exit
```

---

## Method 2: Direct Remote Export/Import (Alternative)

This method exports from localhost and imports directly to remote server in one step.

### Prerequisites
- VPN connected
- Remote MySQL allows connections from your IP

```cmd
REM Export from localhost and import to remote in one command
mysqldump -u nagaraj_jadar -pNagaraj07 tanishq | mysql -h 10.160.128.94 -u tanishq_preprod -p[PREPROD_PASSWORD] tanishq_preprod
```

**Note:** This requires that the remote MySQL server allows connections from your machine. If it doesn't work, use Method 1.

---

## Method 3: Using MySQL Workbench (GUI Method)

If you prefer a graphical interface:

### Step 1: Export Using MySQL Workbench

1. Open MySQL Workbench
2. Connect to your localhost MySQL
3. Select your `tanishq` database
4. Go to **Server → Data Export**
5. Select `tanishq` database
6. Choose "Export to Self-Contained File"
7. Select path: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\database_backup\tanishq_backup.sql`
8. Check these options:
   - ✅ Include Create Schema
   - ✅ Dump Stored Procedures and Functions
   - ✅ Dump Events
   - ✅ Dump Triggers
9. Click **Start Export**

### Step 2: Create Connection to Pre-Prod Server

1. In MySQL Workbench, create new connection:
   - Connection Name: Tanishq Pre-Prod
   - Hostname: 10.160.128.94
   - Port: 3306
   - Username: tanishq_preprod
   - Password: [Store in vault or prompt]
2. Test Connection (make sure VPN is connected)

### Step 3: Import to Pre-Prod Server

1. Connect to Pre-Prod server in MySQL Workbench
2. Go to **Server → Data Import**
3. Select "Import from Self-Contained File"
4. Browse to: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\database_backup\tanishq_backup.sql`
5. Under "Default Target Schema", select or create `tanishq_preprod`
6. Click **Start Import**

---

## Method 4: Table-by-Table Export/Import (For Specific Tables Only)

If you only need specific tables:

### Export Specific Tables

```cmd
REM Export only specific tables
mysqldump -u nagaraj_jadar -pNagaraj07 tanishq store_managers stores events attendees event_images qr_codes > database_backup\tanishq_tables_partial.sql
```

### Import to Pre-Prod

Transfer file and import as shown in Method 1, Step 3-4.

---

## Important Notes & Considerations

### 1. Database Name Differences
Your local database is named `tanishq`, but pre-prod is `tanishq_preprod`. You have two options:

**Option A: Keep database name in dump and rename after import**
```sql
-- After import, if database is named 'tanishq', rename it
RENAME DATABASE tanishq TO tanishq_preprod;
-- Note: RENAME DATABASE is deprecated, better to:
-- 1. Create tanishq_preprod
-- 2. Copy tables
-- 3. Drop tanishq
```

**Option B: Edit the SQL dump file before import**
```cmd
REM Open the SQL file in a text editor
REM Find: CREATE DATABASE `tanishq`
REM Replace with: CREATE DATABASE `tanishq_preprod`
REM Find: USE `tanishq`
REM Replace with: USE `tanishq_preprod`
```

### 2. Hibernate Auto-Update
Your configuration has `spring.jpa.hibernate.ddl-auto=update`, which means:
- On first run, Spring Boot will create missing tables automatically
- You might not need to migrate schema, only data
- But it's safer to migrate both schema and data

### 3. File Paths in Database
If your database contains file paths (like image paths), make sure they are updated:
- Local: `C:\Users\...\storage\selfie_images\...`
- Pre-Prod: `/opt/tanishq/storage/selfie_images/...`

### 4. Test vs Production Data
Consider:
- Do you want ALL data in pre-prod?
- Should you sanitize any sensitive data?
- Do you need sample data or real data?

---

## Post-Migration Tasks

### 1. Verify Data Integrity

```sql
-- Connect to pre-prod database
mysql -u tanishq_preprod -p tanishq_preprod

-- Check table counts
SELECT 
    TABLE_NAME,
    TABLE_ROWS
FROM 
    information_schema.TABLES
WHERE 
    TABLE_SCHEMA = 'tanishq_preprod'
ORDER BY 
    TABLE_NAME;

-- Compare with local counts
```

### 2. Update Application Configuration

Make sure `application-preprod.properties` has correct password:
```properties
spring.datasource.password=[YOUR_ACTUAL_PREPROD_PASSWORD]
```

### 3. Test Connection

Run a quick test to verify application can connect:

```cmd
REM Temporarily set profile to preprod and run application locally
REM It will try to connect to remote database
mvn spring-boot:run -Dspring-boot.run.profiles=preprod
```

---

## Automated Migration Script

I'll create a Windows batch script to automate the export process:

**File:** `export_database_for_preprod.bat`

```batch
@echo off
echo ================================================
echo Database Export for Pre-Production Migration
echo ================================================
echo.

REM Configuration
set DB_USER=nagaraj_jadar
set DB_PASS=Nagaraj07
set DB_NAME=tanishq
set BACKUP_DIR=database_backup
set DATE_STAMP=%date:~-4,4%%date:~-7,2%%date:~-10,2%
set TIME_STAMP=%time:~0,2%%time:~3,2%%time:~6,2%
set TIME_STAMP=%TIME_STAMP: =0%

echo Creating backup directory...
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

echo.
echo Exporting database: %DB_NAME%
echo Backup file: %BACKUP_DIR%\tanishq_backup_%DATE_STAMP%_%TIME_STAMP%.sql
echo.

mysqldump -u %DB_USER% -p%DB_PASS% ^
    --databases %DB_NAME% ^
    --add-drop-database ^
    --routines ^
    --triggers ^
    --events ^
    --single-transaction ^
    --quick ^
    --lock-tables=false ^
    > %BACKUP_DIR%\tanishq_backup_%DATE_STAMP%_%TIME_STAMP%.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================================
    echo SUCCESS: Database exported successfully!
    echo ================================================
    echo.
    echo File location: %CD%\%BACKUP_DIR%\tanishq_backup_%DATE_STAMP%_%TIME_STAMP%.sql
    echo.
    echo Next Steps:
    echo 1. Connect to FortiClient VPN
    echo 2. Open WinSCP and connect to 10.160.128.94
    echo 3. Upload the SQL file to /tmp/ directory
    echo 4. Connect via PuTTY and import using:
    echo    mysql -u tanishq_preprod -p tanishq_preprod ^< /tmp/tanishq_backup_%DATE_STAMP%_%TIME_STAMP%.sql
    echo.
) else (
    echo.
    echo ================================================
    echo ERROR: Database export failed!
    echo ================================================
    echo.
    echo Please check:
    echo - MySQL is running
    echo - Username and password are correct
    echo - mysqldump command is in PATH
    echo.
)

pause
```

---

## Troubleshooting

### Problem: mysqldump command not found

**Solution:**
```cmd
REM Add MySQL bin directory to PATH temporarily
set PATH=%PATH%;C:\Program Files\MySQL\MySQL Server 8.0\bin

REM Or use full path
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" -u nagaraj_jadar -pNagaraj07 ...
```

### Problem: Access denied for user on remote server

**Solution:**
```sql
-- On the pre-prod server, check user permissions
SHOW GRANTS FOR 'tanishq_preprod'@'localhost';
SHOW GRANTS FOR 'tanishq_preprod'@'%';

-- Grant all permissions if needed
GRANT ALL PRIVILEGES ON tanishq_preprod.* TO 'tanishq_preprod'@'localhost';
GRANT ALL PRIVILEGES ON tanishq_preprod.* TO 'tanishq_preprod'@'%';
FLUSH PRIVILEGES;
```

### Problem: Database name mismatch

**Solution:** Edit the SQL dump file:
```cmd
REM Use a text editor or PowerShell
powershell -Command "(gc database_backup\tanishq_backup_20251202.sql) -replace 'CREATE DATABASE.*tanishq', 'CREATE DATABASE `tanishq_preprod`' -replace 'USE `tanishq`', 'USE `tanishq_preprod`' | Out-File -encoding ASCII database_backup\tanishq_backup_20251202_modified.sql"
```

### Problem: Import is very slow

**Solution:**
- Disable binary logging temporarily
- Use `--quick` flag in mysqldump
- Import on the server directly (not over network)

---

## Security Checklist

- [ ] Backup file contains sensitive data - handle securely
- [ ] Delete backup files from server /tmp after import
- [ ] Don't commit SQL dumps to git
- [ ] Use strong password for pre-prod database
- [ ] Restrict database access to necessary IPs only

---

## Summary - Quick Steps

1. **Export:** Run `export_database_for_preprod.bat`
2. **Transfer:** Upload SQL file to server via WinSCP
3. **Import:** Run `mysql -u tanishq_preprod -p tanishq_preprod < backup.sql` on server
4. **Verify:** Check tables and data
5. **Cleanup:** Remove SQL file from server
6. **Test:** Deploy application and test

---

**Document Version:** 1.0  
**Created:** December 2, 2025  
**For:** Pre-Production Database Migration

