# Event Creation Fix - Quick Command Reference

## 🔨 Build Commands

### Windows (PowerShell)
```powershell
# Navigate to project
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app_clean

# Build with script
.\build-and-package.ps1

# OR build manually
mvn clean package -DskipTests

# Find WAR file
Get-ChildItem -Path target -Filter "*.war"
```

### Linux/Mac (Bash)
```bash
# Navigate to project
cd /path/to/tanishq_selfie_app_clean

# Build
mvn clean package -DskipTests

# Find WAR file
ls -lh target/*.war
```

---

## 📤 Upload Commands

### Using SCP
```powershell
# Windows PowerShell
scp C:\JAVA\...\target\tanishq-preprod-*.war root@your-server:/tmp/

# Linux/Mac
scp target/tanishq-preprod-*.war root@your-server:/tmp/
```

### Using rsync
```bash
rsync -avz target/tanishq-preprod-*.war root@your-server:/tmp/
```

---

## 🚀 Deployment Commands (Server)

### Basic Deployment
```bash
# SSH to server
ssh root@your-server-ip

# Backup current WAR
cd /opt/tanishq/applications_preprod
cp tanishq-preprod.war tanishq-preprod.war.backup_$(date +%Y%m%d_%H%M%S)

# Stop server
systemctl stop tomcat

# Deploy new WAR
cp /tmp/tanishq-preprod-*.war /opt/tanishq/applications_preprod/tanishq-preprod.war

# Start server
systemctl start tomcat

# Watch logs
tail -f /opt/tanishq/applications_preprod/application.log
```

### Quick Deployment (One-liner)
```bash
systemctl stop tomcat && \
cp /tmp/tanishq-preprod-*.war /opt/tanishq/applications_preprod/tanishq-preprod.war && \
systemctl start tomcat && \
tail -f /opt/tanishq/applications_preprod/application.log
```

---

## 📊 Monitoring Commands

### View Logs
```bash
# Last 100 lines
tail -100 /opt/tanishq/applications_preprod/application.log

# Follow live
tail -f /opt/tanishq/applications_preprod/application.log

# Search for errors
grep -i error /opt/tanishq/applications_preprod/application.log | tail -20

# Search for specific user
grep "Using authenticated user" /opt/tanishq/applications_preprod/application.log
```

### Check Application Status
```bash
# Service status
systemctl status tomcat

# Check if port is listening
netstat -tlnp | grep 3000

# Check Java process
ps aux | grep java

# Check resources
free -h
df -h
```

---

## 🔍 Verification Commands

### Verify Fix Deployed
```bash
# Check WAR file
ls -lh /opt/tanishq/applications_preprod/tanishq-preprod.war

# Search for fix in WAR
strings /opt/tanishq/applications_preprod/tanishq-preprod.war | grep "Using authenticated user"

# Should output: "Using authenticated user '%s' as store code for event creation"
```

### Test Application
```bash
# Check if responding
curl -I http://localhost:3000/events/

# Check specific endpoint (with auth)
curl -X GET http://localhost:3000/events/getevents \
  -H "Cookie: JSESSIONID=your-session-id"
```

---

## 🗃️ Database Commands

### Check Events
```bash
# Connect to database
mysql -u tanishq_user -p tanishq_db

# View recent events
SELECT id, store_code, event_name, created_at 
FROM events 
ORDER BY created_at DESC 
LIMIT 10;

# Count events by store
SELECT store_code, COUNT(*) as event_count 
FROM events 
GROUP BY store_code 
ORDER BY event_count DESC;

# Exit
exit;
```

### Quick Database Checks
```bash
# Check user exists
mysql -u tanishq_user -p tanishq_db -e "SELECT * FROM users WHERE store_code = 'TEST';"

# Count total events
mysql -u tanishq_user -p tanishq_db -e "SELECT COUNT(*) FROM events;"

# View stores
mysql -u tanishq_user -p tanishq_db -e "SELECT store_code, store_name FROM stores LIMIT 10;"
```

---

## 🔄 Restart Commands

### Tomcat
```bash
# Stop
systemctl stop tomcat

# Start
systemctl start tomcat

# Restart
systemctl restart tomcat

# Status
systemctl status tomcat
```

### Clear Cache
```bash
# Clear Tomcat work directory
rm -rf /opt/tomcat/work/*
rm -rf /opt/tomcat/temp/*

# Restart after clearing
systemctl restart tomcat
```

---

## ↩️ Rollback Commands

### Quick Rollback
```bash
# Stop server
systemctl stop tomcat

# List backups
ls -lh /opt/tanishq/applications_preprod/tanishq-preprod.war.backup_*

# Restore specific backup
cp /opt/tanishq/applications_preprod/tanishq-preprod.war.backup_YYYYMMDD_HHMMSS \
   /opt/tanishq/applications_preprod/tanishq-preprod.war

# Start server
systemctl start tomcat
```

### Rollback One-liner
```bash
systemctl stop tomcat && \
cp /opt/tanishq/applications_preprod/tanishq-preprod.war.backup_LATEST \
   /opt/tanishq/applications_preprod/tanishq-preprod.war && \
systemctl start tomcat
```

---

## 🧪 Testing Commands

### Test Event Creation (CLI)
```bash
# Create test event (requires valid session)
curl -X POST http://localhost:3000/events/upload \
  -H "Cookie: JSESSIONID=your-session-id" \
  -F "eventName=Test Event" \
  -F "eventType=HOME VISITS AND REACH OUTS" \
  -F "date=2026-03-06" \
  -F "time=10:00" \
  -F "customerName=Test User" \
  -F "customerContact=9876543210"
```

### Check Session
```bash
# View recent authentication logs
grep "authenticated" /opt/tanishq/applications_preprod/application.log | tail -10

# Check for TEST user
grep "TEST" /opt/tanishq/applications_preprod/application.log | tail -20
```

---

## 🔧 Troubleshooting Commands

### Application Won't Start
```bash
# Check port conflict
netstat -tlnp | grep 3000

# Check Java version
java -version

# Check memory
free -h

# Check disk space
df -h

# View full error logs
tail -200 /opt/tanishq/applications_preprod/application.log | grep -i "error\|exception"
```

### Database Connection Issues
```bash
# Test database connection
mysql -u tanishq_user -p tanishq_db -e "SELECT 1;"

# Check database status
systemctl status mysql

# View database error logs
tail -100 /var/log/mysql/error.log
```

### Permission Issues
```bash
# Check file permissions
ls -l /opt/tanishq/applications_preprod/tanishq-preprod.war

# Fix permissions if needed
chown tomcat:tomcat /opt/tanishq/applications_preprod/tanishq-preprod.war
chmod 644 /opt/tanishq/applications_preprod/tanishq-preprod.war

# Check directory permissions
ls -ld /opt/tanishq/applications_preprod/
```

---

## 📊 Performance Monitoring

### Check System Resources
```bash
# CPU and Memory
top -bn1 | head -20

# Disk I/O
iostat -x 1 5

# Network
netstat -s

# Process details
ps aux | grep java
```

### Check Log File Size
```bash
# Check log size
ls -lh /opt/tanishq/applications_preprod/application.log

# Rotate logs if too large (backup first!)
cp /opt/tanishq/applications_preprod/application.log \
   /opt/tanishq/applications_preprod/application.log.$(date +%Y%m%d)
> /opt/tanishq/applications_preprod/application.log
```

---

## 🔐 Security Checks

### Check User Sessions
```bash
# View active sessions
grep "authenticated with type" /opt/tanishq/applications_preprod/application.log | tail -20

# Check for unauthorized access attempts
grep "SECURITY ALERT" /opt/tanishq/applications_preprod/application.log
```

### Check Store Access
```bash
# View store access logs
grep "Unauthorized store access" /opt/tanishq/applications_preprod/application.log

# View successful event creations
grep "Using authenticated user" /opt/tanishq/applications_preprod/application.log | tail -20
```

---

## 📝 Backup Commands

### Create Backup
```bash
# Full backup
tar -czf tanishq_backup_$(date +%Y%m%d_%H%M%S).tar.gz \
  /opt/tanishq/applications_preprod/

# WAR file only
cp /opt/tanishq/applications_preprod/tanishq-preprod.war \
   /backup/tanishq-preprod.war.$(date +%Y%m%d_%H%M%S)
```

### Database Backup
```bash
# Backup database
mysqldump -u tanishq_user -p tanishq_db > tanishq_db_backup_$(date +%Y%m%d).sql

# Restore database
mysql -u tanishq_user -p tanishq_db < tanishq_db_backup_YYYYMMDD.sql
```

---

## 🎯 Quick Reference

| Task | Command |
|------|---------|
| Build WAR | `mvn clean package -DskipTests` |
| Deploy WAR | `cp /tmp/*.war /opt/tanishq/applications_preprod/tanishq-preprod.war` |
| Restart Server | `systemctl restart tomcat` |
| View Logs | `tail -f /opt/tanishq/applications_preprod/application.log` |
| Check Status | `systemctl status tomcat` |
| Verify Fix | `strings tanishq-preprod.war \| grep "Using authenticated user"` |
| Rollback | `cp tanishq-preprod.war.backup_* tanishq-preprod.war` |

---

## 📞 Emergency Contacts

**If critical issue occurs:**

1. **Stop application**: `systemctl stop tomcat`
2. **Rollback**: Use commands in "Rollback" section above
3. **Check logs**: `tail -200 /opt/tanishq/applications_preprod/application.log`
4. **Contact team**: Provide logs and error details

---

**Last Updated**: March 6, 2026  
**Version**: 1.0

