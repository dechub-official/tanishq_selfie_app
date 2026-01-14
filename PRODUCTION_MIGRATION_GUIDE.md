# PRODUCTION DATABASE MIGRATION GUIDE
## From Google Sheets to MySQL - Live Production Environment

**Date**: January 12, 2026  
**Application**: Tanishq Celebrations  
**Production URL**: https://celebrations.tanishq.co.in/  
**Production Server IP**: 10.10.63.97  
**Pre-Production Server IP**: 10.160.128.94  

---

## ⚠️ CRITICAL WARNING - READ FIRST

**This is a LIVE production system.** Any mistake can cause:
- Data loss for active events and attendees
- Downtime affecting real customers
- Loss of user registrations and login credentials

**NEVER proceed without:**
1. Complete backup of Google Sheets data
2. Maintenance window scheduled
3. Rollback plan ready
4. Communication to stakeholders

---

## 📋 PRE-DEPLOYMENT CHECKLIST

### Step 1: Pre-Migration Data Backup (MANDATORY)

Before ANY changes to production, backup ALL Google Sheets data:

| Sheet | Sheet ID | Backup Required |
|-------|----------|-----------------|
| User Details | `1vSG8T8rRm5jge_j-exRRvglRO6DEVBXH8UjqMaRQ_5w` | ✅ YES |
| Bride Details | `13C0M-v8tZQpDCXg09pufQ1f6kz2sQYE0wEIRB1-yIpk` | ✅ YES |
| Store Details | `1Y3ieu2Fz0ELcixqNaJa1KTvBOCP65B0lbbmP_edF_oQ` | ✅ YES |
| Events Details | `1ZKb4rqIon5HSdXNnwnYPNZA75Rh1vSBQoup7GCmaWcQ` | ✅ YES |
| Events Attendees | `1rXq_zS0dj0pofs_wzlDfpl5rXVYDIN0fs9Qb9TgMXYU` | ✅ YES |
| Events Invitees | `1D4R7minvW2rke4LQfO70PemRDQZYI92x63EYFf1p9b0` | ✅ YES |
| Checklist Products | `1ZM3YEDlRI-Kbbt1CVN3qZlnMx2QizXJWTMiUO6Rgd-Y` | ✅ YES |
| Rivaah Details | `1tjb2cF6Ye0uIj51jtVHUKgNITJRey5i13Ew0GSwncVY` | ✅ YES |
| Rivaah User Details | `186XwrPKGhaaFMmN5q7doT5lXxbdKVrk8zI_Bs45N0sg` | ✅ YES |
| Events Credentials | `16auf7HZxT6RZmi3pShvds_-FttAg2vyKLjb9esnrLRw` | ✅ YES |
| Greeting Sheet | `1EbbvXLIY6rVylXvlbfEgbZQUXPUuHoVlM9tuL-tFBDs` | ✅ YES |

**How to backup:**
1. Open each Google Sheet
2. Go to File → Download → Microsoft Excel (.xlsx)
3. Save with date: `backup_YYYYMMDD_sheetname.xlsx`
4. Store backups in multiple locations (local + cloud)

### Step 2: Verify Pre-Production is Stable

Before migrating production, confirm Pre-Production works correctly:

```bash
# On Pre-Production Server (10.160.128.94)
# Check application is running
curl -s http://localhost:3000/actuator/health

# Check database connection
mysql -u root -p'Dechub#2025' -e "USE selfie_preprod; SELECT COUNT(*) FROM events;"

# Verify all tables exist
mysql -u root -p'Dechub#2025' -e "USE selfie_preprod; SHOW TABLES;"
```

**Expected Tables (15 tables):**
- `abm_login`
- `attendees`
- `bride_details`
- `cee_login`
- `events`
- `greetings` (or `greeting`)
- `invitees`
- `password_history`
- `product_details`
- `rbm_login`
- `rivaah`
- `rivaah_user`
- `stores`
- `users`
- `user_details`

### Step 3: Document Current Production State

Before deployment, document what's currently running:

```bash
# On Production Server (10.10.63.97)
# SSH as root

# Check current application
ps -ef | grep java
netstat -tulpn | grep java

# Check current directory structure
ls -la /applications_one/ 2>/dev/null || echo "Not found"
ls -la /opt/tanishq/ 2>/dev/null || echo "Not found"

# Note the current WAR file name and deployment location
find / -name "*.war" -path "*tanishq*" 2>/dev/null
```

---

## 🔧 PRODUCTION SETUP STEPS

### Step 4: Verify MySQL Installation on Production

```bash
# On Production Server (10.10.63.97)
# Check MySQL status
systemctl status mysqld

# Check MySQL version
mysql --version

# Verify you can connect
mysql -u root -p'Nagaraj@07' -e "SELECT VERSION();"
```

### Step 5: Create Production Database

```sql
-- Connect to MySQL on Production
mysql -u root -p'Nagaraj@07'

-- Create database (if not already done)
CREATE DATABASE IF NOT EXISTS selfie_prod
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Verify
SHOW DATABASES;

-- Select database
USE selfie_prod;

-- Check tables (should be empty initially)
SHOW TABLES;

EXIT;
```

### Step 6: Create Directory Structure on Production

```bash
# On Production Server (10.10.63.97) as root

# Create application directories
mkdir -p /opt/tanishq/storage/selfie_images
mkdir -p /opt/tanishq/storage/bride_uploads
mkdir -p /opt/tanishq/storage/bride_images
mkdir -p /opt/tanishq/logs
mkdir -p /opt/tanishq/backups

# Set permissions
chmod -R 755 /opt/tanishq
chown -R root:root /opt/tanishq

# Verify
ls -laR /opt/tanishq/
```

### Step 7: Upload Required Files to Production

**Files needed on Production Server:**

| File | Local Path | Production Path |
|------|------------|-----------------|
| WAR File | `target\tanishq-prod-*.war` | `/opt/tanishq/tanishq-prod.war` |
| Service Account Key 1 | `src\main\resources\tanishqgmb-5437243a8085.p12` | `/opt/tanishq/tanishqgmb-5437243a8085.p12` |
| Service Account Key 2 | `src\main\resources\event-images-469618-32e65f6d62b3.p12` | `/opt/tanishq/event-images-469618-32e65f6d62b3.p12` |
| Excel Store Data | `tanishq_selfie_app_store_data.xlsx` | `/opt/tanishq/tanishq_selfie_app_store_data.xlsx` |
| Base Image | `storage\base.jpg` (if exists) | `/opt/tanishq/storage/base.jpg` |

**Upload using WinSCP or pscp:**

```powershell
# From Windows PowerShell
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Upload WAR file
pscp target\tanishq-prod-*.war root@10.10.63.97:/opt/tanishq/tanishq-prod.war

# Upload service account keys
pscp src\main\resources\tanishqgmb-5437243a8085.p12 root@10.10.63.97:/opt/tanishq/
pscp src\main\resources\event-images-469618-32e65f6d62b3.p12 root@10.10.63.97:/opt/tanishq/
```

---

## 🚀 DEPLOYMENT STRATEGY OPTIONS

### Option A: Big Bang Migration (Higher Risk, Faster)
Deploy new MySQL-based application and migrate all data at once.

**Pros:** Simple, one-time effort
**Cons:** Longer downtime, higher risk

### Option B: Parallel Running (Recommended for Production)
Run both systems temporarily, verify MySQL system works, then switch.

**Pros:** Lower risk, can rollback easily
**Cons:** More complex, needs temporary parallel setup

### ✅ RECOMMENDED: Option B - Parallel Running

---

## 📦 STEP-BY-STEP DEPLOYMENT PROCEDURE

### Phase 1: Preparation (Do This Now - No Downtime)

#### 1.1 Build Production WAR

```powershell
# On your Windows machine
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Update pom.xml artifactId for production
# Change: tanishq-preprod-07-01-2026-2
# To:     tanishq-prod-12-01-2026-1

# Build with prod profile
mvn clean install -Pprod

# WAR will be at: target\tanishq-prod-12-01-2026-1-0.0.1-SNAPSHOT.war
```

#### 1.2 Verify application-prod.properties

Ensure these settings are correct:

```properties
# CRITICAL SETTINGS TO VERIFY

# Port - Use 3001 (different from preprod's 3000)
server.port=3001

# Database - Production database name
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_prod
spring.datasource.username=root
spring.datasource.password=Nagaraj@07

# DDL Mode - UPDATE will create tables automatically
spring.jpa.hibernate.ddl-auto=update

# SQL Logging - Turn OFF for production
spring.jpa.show-sql=false

# QR Code URLs - Must point to production domain
events.qr.base.url=https://celebrations.tanishq.co.in/events/customer/
greeting.qr.base.url=https://celebrations.tanishq.co.in/greetings/
qr.code.base.url=https://celebrations.tanishq.co.in/events/customer/

# File paths - Production paths
dechub.tanishq.key.filepath=/opt/tanishq/tanishqgmb-5437243a8085.p12
selfie.upload.dir=/opt/tanishq/storage/selfie_images
dechub.bride.upload.dir=/opt/tanishq/storage/bride_uploads
dechub.base.image=/opt/tanishq/storage/base.jpg
store.details.excel.sheet=/opt/tanishq/tanishq_selfie_app_store_data.xlsx

# AWS S3 - Production bucket
aws.s3.bucket.name=celebrations-tanishq-prod
aws.s3.region=ap-south-1
```

### Phase 2: Data Migration (Schedule Maintenance Window)

#### 2.1 Export Data from Pre-Production MySQL

```bash
# On Pre-Production Server (10.160.128.94)
# Export complete database

mysqldump -u root -p'Dechub#2025' selfie_preprod > /tmp/selfie_preprod_backup_$(date +%Y%m%d_%H%M%S).sql

# Download to your local machine
# Then upload to Production server
```

#### 2.2 Import Data to Production MySQL

```bash
# On Production Server (10.10.63.97)

# First, create the database if not exists
mysql -u root -p'Nagaraj@07' -e "CREATE DATABASE IF NOT EXISTS selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Import the data (modify database name in SQL first if needed)
# IMPORTANT: Edit the SQL file to change database name from selfie_preprod to selfie_prod
mysql -u root -p'Nagaraj@07' selfie_prod < /tmp/selfie_preprod_backup.sql

# Verify data
mysql -u root -p'Nagaraj@07' -e "USE selfie_prod; SHOW TABLES; SELECT COUNT(*) FROM events;"
```

### Phase 3: Application Deployment

#### 3.1 Stop Current Production Application (If Running)

```bash
# On Production Server (10.10.63.97)

# Find and stop current application
ps -ef | grep java | grep tanishq
kill -15 <PID>

# Or if using systemd
systemctl stop tanishq
```

#### 3.2 Deploy New WAR File

```bash
# On Production Server

cd /opt/tanishq

# Backup old WAR if exists
cp tanishq-prod.war tanishq-prod.war.backup.$(date +%Y%m%d) 2>/dev/null

# New WAR should already be uploaded
ls -la tanishq-prod.war

# Set permissions
chmod 644 tanishq-prod.war
```

#### 3.3 Start Application

**Option A: Direct Java Command**
```bash
cd /opt/tanishq
nohup java -jar tanishq-prod.war --spring.profiles.active=prod > logs/app.log 2>&1 &

# Check if started
sleep 10
ps -ef | grep java
tail -100 logs/app.log
```

**Option B: Systemd Service (Recommended)**
```bash
# Create service file
cat > /etc/systemd/system/tanishq.service << 'EOF'
[Unit]
Description=Tanishq Celebrations Application
After=network.target mysqld.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/tanishq
ExecStart=/usr/bin/java -jar /opt/tanishq/tanishq-prod.war --spring.profiles.active=prod
ExecStop=/bin/kill -15 $MAINPID
Restart=always
RestartSec=10
StandardOutput=append:/opt/tanishq/logs/app.log
StandardError=append:/opt/tanishq/logs/app-error.log

[Install]
WantedBy=multi-user.target
EOF

# Enable and start
systemctl daemon-reload
systemctl enable tanishq
systemctl start tanishq
systemctl status tanishq
```

### Phase 4: Verification

#### 4.1 Check Application Health

```bash
# On Production Server

# Check process is running
ps -ef | grep java

# Check port is listening
netstat -tulpn | grep 3001

# Check logs for errors
tail -200 /opt/tanishq/logs/app.log | grep -i error

# Test health endpoint (if available)
curl -s http://localhost:3001/actuator/health
```

#### 4.2 Verify Database Tables Created

```bash
mysql -u root -p'Nagaraj@07' -e "USE selfie_prod; SHOW TABLES;"
```

**Expected output:**
```
+------------------------+
| Tables_in_selfie_prod  |
+------------------------+
| abm_login              |
| attendees              |
| bride_details          |
| cee_login              |
| events                 |
| greeting               |
| invitees               |
| password_history       |
| product_details        |
| rbm_login              |
| rivaah                 |
| rivaah_user            |
| stores                 |
| user_details           |
| users                  |
+------------------------+
```

#### 4.3 Functional Testing Checklist

| Test | URL/Action | Expected Result |
|------|------------|-----------------|
| Homepage loads | https://celebrations.tanishq.co.in/ | Page loads without errors |
| Login works | Try admin/store login | Successful authentication |
| Events list | Check events page | Events display from DB |
| Create event | Create test event | Event saved to MySQL |
| Attendee registration | Register test attendee | Data in MySQL `attendees` table |
| QR Code generation | Generate event QR | QR with correct URL |
| Image upload | Upload test image | File saved to `/opt/tanishq/storage/` |
| Email sending | Trigger test email | Email sent successfully |

---

## 🔄 ROLLBACK PROCEDURE

If something goes wrong, follow these steps:

### Immediate Rollback (Within Maintenance Window)

```bash
# On Production Server

# 1. Stop new application
systemctl stop tanishq
# OR
kill -15 $(pgrep -f tanishq-prod.war)

# 2. Restore old WAR
cd /opt/tanishq
mv tanishq-prod.war tanishq-prod.war.failed
mv tanishq-prod.war.backup.YYYYMMDD tanishq-prod.war

# 3. Start old application
systemctl start tanishq
# OR
nohup java -jar tanishq-prod.war --spring.profiles.active=prod > logs/app.log 2>&1 &

# 4. Verify rollback
curl -s http://localhost:3001/
```

### Database Rollback

```bash
# If database migration caused issues

# 1. Drop production database
mysql -u root -p'Nagaraj@07' -e "DROP DATABASE selfie_prod;"

# 2. Recreate empty database
mysql -u root -p'Nagaraj@07' -e "CREATE DATABASE selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 3. Re-import clean backup
mysql -u root -p'Nagaraj@07' selfie_prod < /path/to/clean_backup.sql
```

---

## 📊 POST-DEPLOYMENT MONITORING

### Daily Checks (First Week)

```bash
# Database size and growth
mysql -u root -p'Nagaraj@07' -e "
SELECT 
    table_name AS 'Table',
    ROUND(data_length/1024/1024, 2) AS 'Size (MB)',
    table_rows AS 'Rows'
FROM information_schema.tables 
WHERE table_schema = 'selfie_prod'
ORDER BY data_length DESC;"

# Application logs check
tail -500 /opt/tanishq/logs/app.log | grep -i "error\|exception\|failed"

# Disk space
df -h /opt/tanishq /var/lib/mysql
```

### Set Up Automated Backups

```bash
# Create backup script
cat > /opt/tanishq/backup_database.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/tanishq/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=7

# Create backup
mysqldump -u root -p'Nagaraj@07' selfie_prod | gzip > "$BACKUP_DIR/selfie_prod_$TIMESTAMP.sql.gz"

# Remove old backups
find $BACKUP_DIR -name "*.sql.gz" -mtime +$RETENTION_DAYS -delete

echo "Backup completed: selfie_prod_$TIMESTAMP.sql.gz"
EOF

chmod +x /opt/tanishq/backup_database.sh

# Add to crontab (daily at 2 AM)
echo "0 2 * * * /opt/tanishq/backup_database.sh >> /opt/tanishq/logs/backup.log 2>&1" | crontab -
```

---

## ✅ FINAL CHECKLIST BEFORE GO-LIVE

- [ ] All Google Sheets data backed up
- [ ] Pre-Production verified working
- [ ] Production MySQL installed and running
- [ ] Production database `selfie_prod` created
- [ ] Directory structure created on production
- [ ] All required files uploaded to `/opt/tanishq/`
- [ ] application-prod.properties verified
- [ ] WAR file built with prod profile
- [ ] Maintenance window scheduled
- [ ] Stakeholders notified
- [ ] Rollback plan documented and tested
- [ ] Monitoring set up
- [ ] Automated backups configured

---

## 📞 EMERGENCY CONTACTS

| Role | Contact | Notes |
|------|---------|-------|
| DBA | [Add Contact] | Database issues |
| DevOps | [Add Contact] | Server issues |
| Developer | [Add Contact] | Application issues |

---

## 📝 APPENDIX

### A. Differences Between Pre-Prod and Prod Configuration

| Setting | Pre-Production | Production |
|---------|---------------|------------|
| Port | 3000 | 3001 |
| Database | selfie_preprod | selfie_prod |
| DB Password | Dechub#2025 | Nagaraj@07 |
| SQL Logging | true | false |
| Log Level | DEBUG | INFO |
| QR Base URL | celebrationsite-preprod.tanishq.co.in | celebrations.tanishq.co.in |
| S3 Bucket | celebrations-tanishq-preprod | celebrations-tanishq-prod |

### B. Database Schema (Auto-Created by Hibernate)

The following tables will be automatically created when the application starts:

1. **abm_login** - ABM manager credentials
2. **attendees** - Event attendee registrations
3. **bride_details** - Bride registration data
4. **cee_login** - CEE user credentials
5. **events** - Event master data
6. **greeting** - Video greeting data
7. **invitees** - Event invitee list
8. **password_history** - Password change history
9. **product_details** - Product checklist items
10. **rbm_login** - RBM manager credentials
11. **rivaah** - Rivaah event data
12. **rivaah_user** - Rivaah user data
13. **stores** - Store master data
14. **user_details** - Extended user information
15. **users** - User master data

### C. Quick Commands Reference

```bash
# Start application
systemctl start tanishq

# Stop application
systemctl stop tanishq

# Check status
systemctl status tanishq

# View logs
tail -f /opt/tanishq/logs/app.log

# Check database
mysql -u root -p'Nagaraj@07' selfie_prod

# Manual backup
/opt/tanishq/backup_database.sh
```

---

**Document Version**: 1.0  
**Last Updated**: January 12, 2026  
**Author**: Migration Team

