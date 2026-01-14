# 🚀 Production Deployment Checklist
## Tanishq Celebrations Application - Production Server Setup

---

## 📋 Server Information

| Item | Details |
|------|---------|
| **Production Server IP** | `10.10.63.97` |
| **MySQL Version** | MySQL 8.4.7 Enterprise (Commercial) |
| **MySQL Port** | 3306 |
| **MySQL Data Directory** | /var/lib/mysql |
| **MySQL Service** | mysqld (RUNNING) |
| **Database Name** | selfie_prod |
| **Application Port** | 3001 |
| **Domain** | https://celebrations.tanishq.co.in/ |

---

## ⚠️ IMPORTANT: Before You Start

1. **Different Servers Confirmed**: Pre-prod and Production are on DIFFERENT servers
2. **MySQL Password**: You need the production MySQL root password
3. **Backup First**: Always backup before deploying
4. **Network Access**: Ensure you can SSH to 10.10.63.97

---

## 📝 Pre-Deployment Checklist

### ✅ Step 1: Configure Production Properties

**File**: `src/main/resources/application-prod.properties`

**Action Required**: Update MySQL password in line 7:

```properties
spring.datasource.password=YOUR_PRODUCTION_MYSQL_PASSWORD_HERE
```

Replace `YOUR_PRODUCTION_MYSQL_PASSWORD_HERE` with your actual production MySQL root password.

---

### ✅ Step 2: Build Production WAR File

On your Windows development machine:

```powershell
# Navigate to project directory
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Clean and build
mvn clean package -DskipTests

# Verify WAR file created
dir target\*.war
```

**Expected Output**: `target\tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war`

---

### ✅ Step 3: Verify Required Files Exist

Check these files exist before uploading:

```powershell
# WAR file
ls target\tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war

# Google Service Account Keys
ls src\main\resources\tanishqgmb-5437243a8085.p12
ls src\main\resources\event-images-469618-32e65f6d62b3.p12

# Setup Scripts
ls setup_production_server.sh
ls setup_production_database.sql
```

---

## 🚀 Deployment Steps

### Step 1: Upload Files to Production Server

Use the provided batch script:

```powershell
.\upload_to_production.bat
```

Or manually upload using WinSCP/pscp:

```powershell
# Upload WAR file (rename to tanishq-prod.war)
pscp target\tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war root@10.10.63.97:/opt/tanishq/tanishq-prod.war

# Upload Google Service Account Keys
pscp src\main\resources\tanishqgmb-5437243a8085.p12 root@10.10.63.97:/opt/tanishq/
pscp src\main\resources\event-images-469618-32e65f6d62b3.p12 root@10.10.63.97:/opt/tanishq/

# Upload setup scripts
pscp setup_production_server.sh root@10.10.63.97:/opt/tanishq/
pscp setup_production_database.sql root@10.10.63.97:/opt/tanishq/
```

---

### Step 2: SSH to Production Server

```bash
ssh root@10.10.63.97
```

---

### Step 3: Run Server Setup Script

This creates directories and systemd service:

```bash
cd /opt/tanishq
chmod +x setup_production_server.sh
./setup_production_server.sh
```

**What it does**:
- Creates `/opt/tanishq` directory structure
- Creates storage directories for uploads
- Sets up systemd service file
- Creates backup script

---

### Step 4: Setup Production Database

```bash
# Connect to MySQL and run setup script
mysql -u root -p < /opt/tanishq/setup_production_database.sql

# Verify database created
mysql -u root -p -e "SHOW DATABASES LIKE 'selfie_prod';"
mysql -u root -p -e "USE selfie_prod; SHOW TABLES;"
```

**Expected**: Database `selfie_prod` created (empty - tables will be auto-created by Spring Boot)

---

### Step 5: Verify File Permissions

```bash
# Check all files are in place
ls -lh /opt/tanishq/

# Expected files:
# - tanishq-prod.war
# - tanishqgmb-5437243a8085.p12
# - event-images-469618-32e65f6d62b3.p12
# - setup_production_server.sh
# - setup_production_database.sql

# Check permissions
chmod 644 /opt/tanishq/*.p12
chmod 644 /opt/tanishq/tanishq-prod.war
```

---

### Step 6: Start the Application

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

---

### Step 7: Monitor Logs

```bash
# Watch application logs in real-time
tail -f /opt/tanishq/logs/application.log

# Check for errors
tail -f /opt/tanishq/logs/error.log

# Check if application started successfully
grep -i "started" /opt/tanishq/logs/application.log
grep -i "error" /opt/tanishq/logs/application.log
```

**Look for**: `TanishqSelfieApplication started in X seconds`

---

### Step 8: Verify Application is Running

```bash
# Check if port 3001 is listening
ss -tlnp | grep 3001

# Test health endpoint (if exists)
curl http://localhost:3001/actuator/health

# Or test basic endpoint
curl -I http://localhost:3001/
```

---

### Step 9: Verify Database Tables Created

```bash
mysql -u root -p -e "USE selfie_prod; SHOW TABLES;"
```

**Expected**: Spring Boot should have auto-created tables based on JPA entities

---

## 🔍 Troubleshooting

### Application Won't Start

1. **Check logs**:
   ```bash
   journalctl -u tanishq-prod -n 100 --no-pager
   tail -100 /opt/tanishq/logs/error.log
   ```

2. **Common Issues**:
   - Wrong MySQL password → Update `/opt/tanishq/tanishq-prod.war` or rebuild with correct password
   - MySQL not running → `systemctl status mysqld`
   - Database doesn't exist → Run setup_production_database.sql again
   - Port 3001 already in use → `ss -tlnp | grep 3001`

### Database Connection Failed

1. **Verify MySQL is running**:
   ```bash
   systemctl status mysqld
   ```

2. **Test MySQL connection**:
   ```bash
   mysql -u root -p -e "SELECT VERSION();"
   ```

3. **Check database exists**:
   ```bash
   mysql -u root -p -e "SHOW DATABASES LIKE 'selfie_prod';"
   ```

4. **Verify credentials in application**:
   ```bash
   # Extract properties from WAR file
   cd /opt/tanishq
   unzip -p tanishq-prod.war WEB-INF/classes/application-prod.properties | grep datasource
   ```

### Port Issues

```bash
# Check what's using port 3001
ss -tlnp | grep 3001

# Kill process if needed
kill -9 <PID>

# Restart application
systemctl restart tanishq-prod
```

### Google Sheets/Drive Not Working

1. **Verify P12 files exist**:
   ```bash
   ls -lh /opt/tanishq/*.p12
   ```

2. **Check file permissions**:
   ```bash
   chmod 644 /opt/tanishq/*.p12
   ```

3. **Verify paths in logs**:
   ```bash
   grep -i "p12" /opt/tanishq/logs/application.log
   ```

---

## 🔄 Post-Deployment Tasks

### 1. Test Application Endpoints

From your browser or Postman:

- **Home**: https://celebrations.tanishq.co.in/
- **Events**: https://celebrations.tanishq.co.in/events/
- **API Health**: https://celebrations.tanishq.co.in/actuator/health (if enabled)

### 2. Setup Backup Cron Job

```bash
# Edit crontab
crontab -e

# Add daily backup at 2 AM
0 2 * * * /opt/tanishq/backup_production_database.sh >> /opt/tanishq/logs/backup.log 2>&1
```

### 3. Monitor Disk Space

```bash
# Check MySQL data directory size
du -sh /var/lib/mysql/

# Check application storage
du -sh /opt/tanishq/storage/

# Check logs size
du -sh /opt/tanishq/logs/
```

### 4. Setup Log Rotation

Create `/etc/logrotate.d/tanishq-prod`:

```bash
cat > /etc/logrotate.d/tanishq-prod << 'EOF'
/opt/tanishq/logs/*.log {
    daily
    rotate 7
    compress
    delaycompress
    notifempty
    create 0644 root root
    sharedscripts
    postrotate
        systemctl reload tanishq-prod > /dev/null 2>&1 || true
    endscript
}
EOF
```

---

## 📊 Useful Commands

### Application Management

```bash
# Start application
systemctl start tanishq-prod

# Stop application
systemctl stop tanishq-prod

# Restart application
systemctl restart tanishq-prod

# Check status
systemctl status tanishq-prod

# View logs
journalctl -u tanishq-prod -f
```

### Database Management

```bash
# Login to MySQL
mysql -u root -p

# Backup database
mysqldump -u root -p selfie_prod > /opt/tanishq/backups/selfie_prod_manual_backup_$(date +%Y%m%d).sql

# Check database size
mysql -u root -p -e "SELECT table_schema 'Database', ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) 'Size (MB)' FROM information_schema.tables WHERE table_schema = 'selfie_prod';"
```

### System Health

```bash
# Check disk space
df -h

# Check memory
free -h

# Check CPU
top

# Check running Java processes
ps aux | grep java
```

---

## 🎯 Success Criteria

✅ Application starts without errors  
✅ Port 3001 is listening  
✅ Database `selfie_prod` exists with tables  
✅ Can access https://celebrations.tanishq.co.in/  
✅ Logs show no critical errors  
✅ Google Sheets integration works  
✅ File uploads work (test selfie upload)  
✅ Systemd service starts on boot  

---

## 📞 Emergency Rollback

If deployment fails:

```bash
# Stop the application
systemctl stop tanishq-prod

# Restore previous WAR file (if you had backed it up)
cp /opt/tanishq/backups/tanishq-prod.war.backup /opt/tanishq/tanishq-prod.war

# Start application
systemctl start tanishq-prod
```

---

## 📝 Important Notes

1. **Password Security**: Never commit actual passwords to Git
2. **Backup Before Deploy**: Always backup before making changes
3. **Test on Pre-Prod First**: Test on pre-prod before production
4. **Monitor Logs**: Always monitor logs after deployment
5. **Document Changes**: Keep track of what you deploy

---

## 🔗 Related Files

- `application-prod.properties` - Production configuration
- `upload_to_production.bat` - Windows upload script
- `setup_production_server.sh` - Server setup script
- `setup_production_database.sql` - Database setup script
- `DEPLOYMENT_QUICK_REFERENCE.md` - Quick deployment guide

---

**Last Updated**: January 13, 2026  
**Production Server**: 10.10.63.97  
**MySQL Version**: 8.4.7 Enterprise  
**Application**: Tanishq Celebrations Selfie Application

