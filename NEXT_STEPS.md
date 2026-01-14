# What to Do Next - Production Setup Summary

## Current Status
✅ MySQL 8.4.7 Commercial installed on production server (ip-10-10-63-97)
✅ MySQL service is running
✅ You are connected to MySQL as root

## What You Need to Do Next (In Order)

### STEP 1: Create the Production Database (RIGHT NOW in MySQL)
You are already in MySQL prompt. Run these commands:

```sql
CREATE DATABASE IF NOT EXISTS selfie_prod 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

SHOW DATABASES;

USE selfie_prod;

SHOW TABLES;

EXIT;
```

### STEP 2: Create Application Directories
Exit MySQL and run in Linux terminal:

```bash
mkdir -p /opt/tanishq/storage/selfie_images
mkdir -p /opt/tanishq/storage/bride_uploads
mkdir -p /opt/tanishq/storage/bride_images
mkdir -p /opt/tanishq/logs
mkdir -p /opt/tanishq/backups
chmod -R 755 /opt/tanishq
ls -la /opt/tanishq/
```

### STEP 3: Upload Application Files from Windows

You need to upload these files from your Windows machine to the production server:

#### Files to Upload:
1. **WAR File**
   - Source: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\target\tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war`
   - Destination: `/opt/tanishq/tanishq-prod.war` (rename it!)

2. **Google Service Account Key**
   - Source: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\tanishqgmb-5437243a8085.p12`
   - Destination: `/opt/tanishq/tanishqgmb-5437243a8085.p12`

3. **Setup Scripts** (I just created these for you)
   - Source: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\setup_production_server.sh`
   - Destination: `/opt/tanishq/setup_production_server.sh`
   
   - Source: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\setup_production_database.sql`
   - Destination: `/opt/tanishq/setup_production_database.sql`

4. **Additional Files** (if you have them):
   - `base.jpg` → `/opt/tanishq/storage/base.jpg`
   - `tanishq_selfie_app_store_data.xlsx` → `/opt/tanishq/tanishq_selfie_app_store_data.xlsx`

#### How to Upload:

**Option A: Using WinSCP (Recommended for Windows)**
1. Download WinSCP from https://winscp.net/
2. Connect to 10.10.63.97 as root
3. Navigate to /opt/tanishq/
4. Drag and drop the files
5. Rename the WAR file to `tanishq-prod.war`

**Option B: Using Command Line (if you have PuTTY/PSCP)**
```powershell
# Run this in Windows PowerShell from your project directory
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Upload files
pscp target\tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war root@10.10.63.97:/opt/tanishq/tanishq-prod.war
pscp src\main\resources\tanishqgmb-5437243a8085.p12 root@10.10.63.97:/opt/tanishq/
pscp setup_production_server.sh root@10.10.63.97:/opt/tanishq/
pscp setup_production_database.sql root@10.10.63.97:/opt/tanishq/
```

**Option C: Use the batch script I created**
```powershell
# Run this from your project directory
.\upload_to_production.bat
```

### STEP 4: Configure MySQL for Production (On Linux Server)

```bash
# Edit MySQL config
vi /etc/my.cnf

# Add these settings under [mysqld] section:
# max_connections=200
# max_allowed_packet=100M
# innodb_buffer_pool_size=2G
# character-set-server=utf8mb4
# collation-server=utf8mb4_unicode_ci

# Restart MySQL
systemctl restart mysqld
systemctl status mysqld
```

### STEP 5: Create Systemd Service for Application

```bash
vi /etc/systemd/system/tanishq-prod.service
```

Paste this content:
```ini
[Unit]
Description=Tanishq Production Application
After=network.target mysqld.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/tanishq
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/tanishq/tanishq-prod.war
Restart=always
RestartSec=10
StandardOutput=append:/opt/tanishq/logs/application.log
StandardError=append:/opt/tanishq/logs/error.log

[Install]
WantedBy=multi-user.target
```

### STEP 6: Configure Firewall

```bash
firewall-cmd --permanent --add-port=3001/tcp
firewall-cmd --reload
firewall-cmd --list-ports
```

### STEP 7: Start the Application

```bash
# Reload systemd
systemctl daemon-reload

# Enable service to start on boot
systemctl enable tanishq-prod

# Start the application
systemctl start tanishq-prod

# Check status
systemctl status tanishq-prod
```

### STEP 8: Verify Everything is Working

```bash
# Watch application logs
tail -f /opt/tanishq/logs/application.log

# In another terminal, check if port is listening
ss -tulpn | grep 3001

# Test the endpoint
curl http://localhost:3001/

# Check database tables (wait a minute for app to start)
mysql -u root -p selfie_prod -e "SHOW TABLES;"
```

---

## Summary of Files I Created for You

I've created several helpful files in your project:

1. **PRODUCTION_SETUP_GUIDE.md** - Complete detailed guide with all steps
2. **PRODUCTION_QUICK_START.md** - Quick reference commands
3. **setup_production_server.sh** - Automated setup script for Linux
4. **setup_production_database.sql** - SQL commands to create database
5. **upload_to_production.bat** - Windows batch script to upload files
6. **NEXT_STEPS.md** - This file!

---

## Expected Timeline

- Step 1 (Database creation): 2 minutes
- Step 2 (Create directories): 2 minutes  
- Step 3 (Upload files): 5-10 minutes
- Step 4 (Configure MySQL): 5 minutes
- Step 5 (Create service): 3 minutes
- Step 6 (Firewall): 2 minutes
- Step 7 (Start app): 2 minutes
- Step 8 (Verify): 5 minutes

**Total: About 30 minutes**

---

## What's Different from Pre-Prod?

| Setting | Pre-Prod | Production |
|---------|----------|------------|
| Database Name | selfie_preprod | selfie_prod |
| Port | 3000 | 3001 |
| SQL Logging | ON (debug) | OFF (performance) |
| Log Level | DEBUG | INFO |
| Profile | preprod | prod |

---

## Current Status - Checklist

- [x] MySQL installed
- [x] MySQL running
- [ ] Database created (DO THIS NOW!)
- [ ] Directories created
- [ ] Files uploaded
- [ ] MySQL configured
- [ ] Service created
- [ ] Firewall configured
- [ ] Application running
- [ ] Verified working

---

## START HERE - Your Very Next Command

You are currently in MySQL prompt on production server. Type this now:

```sql
CREATE DATABASE IF NOT EXISTS selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
EXIT;
```

Then follow the steps above in order!

---

## Need Help?

Refer to these files:
- **Quick commands**: PRODUCTION_QUICK_START.md
- **Detailed guide**: PRODUCTION_SETUP_GUIDE.md
- **Troubleshooting**: See "Troubleshooting" section in PRODUCTION_SETUP_GUIDE.md

