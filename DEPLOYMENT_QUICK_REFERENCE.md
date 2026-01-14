# 🚀 PRODUCTION DEPLOYMENT - QUICK REFERENCE CARD
## Tanishq Celebrations - MySQL Migration

---

## 📍 ENVIRONMENT DETAILS

| Environment | Server IP | URL | Database | Port |
|-------------|-----------|-----|----------|------|
| **Pre-Production** | 10.160.128.94 | https://celebrationsite-preprod.tanishq.co.in/ | selfie_preprod | 3000 |
| **Production** | 10.10.63.97 | https://celebrations.tanishq.co.in/ | selfie_prod | 3001 |

---

## ⚡ QUICK DEPLOYMENT STEPS

### 1️⃣ BEFORE DEPLOYMENT (On Your Windows Machine)

```powershell
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Build for production
mvn clean install -Pprod
```

### 2️⃣ EXPORT DATA FROM PRE-PROD (On 10.160.128.94)

```bash
# SSH to Pre-Production server
ssh root@10.160.128.94

# Run export script
chmod +x /opt/tanishq/export_preprod_data.sh
/opt/tanishq/export_preprod_data.sh

# Copy to production
scp /tmp/tanishq_migration/data_for_production_*.sql.gz root@10.10.63.97:/tmp/
```

### 3️⃣ SETUP PRODUCTION (On 10.10.63.97)

```bash
# SSH to Production server
ssh root@10.10.63.97

# Create directories
mkdir -p /opt/tanishq/{storage/{selfie_images,bride_uploads,bride_images},logs,backups}
chmod -R 755 /opt/tanishq

# Create database
mysql -u root -p'Nagaraj@07' -e "CREATE DATABASE IF NOT EXISTS selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Import data
gunzip /tmp/data_for_production_*.sql.gz
mysql -u root -p'Nagaraj@07' selfie_prod < /tmp/data_for_production_*.sql
```

### 4️⃣ UPLOAD FILES (From Windows)

```powershell
# Upload WAR file
pscp target\tanishq-prod-*.war root@10.10.63.97:/opt/tanishq/tanishq-prod.war

# Upload service account keys
pscp src\main\resources\*.p12 root@10.10.63.97:/opt/tanishq/
```

### 5️⃣ START APPLICATION (On 10.10.63.97)

```bash
cd /opt/tanishq
nohup java -jar tanishq-prod.war --spring.profiles.active=prod > logs/app.log 2>&1 &

# Verify
tail -f logs/app.log
```

---

## 🔧 USEFUL COMMANDS

### Check Application Status
```bash
ps -ef | grep tanishq
netstat -tulpn | grep 3001
tail -100 /opt/tanishq/logs/app.log
```

### Database Commands
```bash
# Connect to MySQL
mysql -u root -p'Nagaraj@07' selfie_prod

# Check tables
SHOW TABLES;

# Check record counts
SELECT 'events' as t, COUNT(*) FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees;

# Manual backup
mysqldump -u root -p'Nagaraj@07' selfie_prod > /opt/tanishq/backups/backup_$(date +%Y%m%d).sql
```

### Stop Application
```bash
# Find PID
PID=$(pgrep -f tanishq-prod.war)

# Graceful stop
kill -15 $PID

# Force stop (if needed)
kill -9 $PID
```

---

## 🔴 EMERGENCY ROLLBACK

```bash
# Stop new application
kill -15 $(pgrep -f tanishq-prod.war)

# Restore old WAR (if backed up)
mv /opt/tanishq/tanishq-prod.war /opt/tanishq/tanishq-prod.war.failed
mv /opt/tanishq/tanishq-prod.war.backup /opt/tanishq/tanishq-prod.war

# Restart
cd /opt/tanishq
nohup java -jar tanishq-prod.war --spring.profiles.active=prod > logs/app.log 2>&1 &
```

---

## ✅ VERIFICATION CHECKLIST

After deployment, verify:

- [ ] Application started (check logs)
- [ ] Port 3001 is listening
- [ ] Homepage loads: https://celebrations.tanishq.co.in/
- [ ] Login works for store users
- [ ] Events page shows data
- [ ] Can create new event
- [ ] Attendee registration works
- [ ] QR codes generate correctly
- [ ] Email sending works
- [ ] File uploads work

---

## 📞 CRITICAL CONTACTS

| Role | Action |
|------|--------|
| Stop for users | Take down maintenance page |
| Rollback needed | Follow emergency rollback |
| Database issue | Check MySQL logs: `/var/log/mysqld.log` |

---

## 📁 FILE LOCATIONS

| File | Location on Production |
|------|----------------------|
| Application WAR | `/opt/tanishq/tanishq-prod.war` |
| Logs | `/opt/tanishq/logs/app.log` |
| Image uploads | `/opt/tanishq/storage/selfie_images/` |
| Bride uploads | `/opt/tanishq/storage/bride_uploads/` |
| Database backups | `/opt/tanishq/backups/` |
| Service keys | `/opt/tanishq/*.p12` |

---

**Last Updated**: January 12, 2026

