# Tanishq Selfie App - Essential Deployment Information

**Last Updated:** January 24, 2026  
**Project:** Tanishq Selfie Application  
**Status:** Production Ready

---

## 📋 Quick Overview

This document contains all essential deployment information. All unnecessary documentation files have been removed.

---

## 🖥️ Server Information

### Production Server
- **IP:** 10.10.63.97
- **OS:** Linux
- **MySQL Version:** 8.4.7 Enterprise
- **Application Port:** 8080 (Backend)
- **Database:** selfie_prod

### Pre-Production Server
- **Database:** tanishq_preprod
- **Application Port:** 3002

---

## 🚀 Deployment Steps

### Option 1: Production Deployment (Linux Server)

**Prerequisites:**
- SSH access to 10.10.63.97
- Root credentials
- WAR file built using Maven

**Steps:**

1. **Build the WAR file (on Windows):**
   ```batch
   cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
   mvn clean package -P production
   ```

2. **Upload WAR to server:**
   ```batch
   scp target\tanishq-preprod-21-01-2026-1-0.0.1-SNAPSHOT.war root@10.10.63.97:/opt/tanishq/
   ```

3. **Connect to server:**
   ```batch
   ssh root@10.10.63.97
   ```

4. **Run deployment script:**
   ```bash
   cd /opt/tanishq
   ./deploy_production.sh
   ```

5. **Verify deployment:**
   - Application should start on port 8080
   - Access via: http://10.10.63.97:8080
   - Check logs: `tail -f /opt/tanishq/logs/application.log`

---

### Option 2: Using Nohup (Background Process)

```bash
# Stop existing application
pkill -f tanishq-preprod

# Start in background
cd /opt/tanishq
nohup java -jar tanishq-preprod-21-01-2026-1-0.0.1-SNAPSHOT.war \
  --server.port=8080 \
  --spring.profiles.active=production \
  > logs/application.log 2>&1 &

# Check if running
ps aux | grep tanishq
```

---

## 🗄️ Database Information

### Production Database
- **Name:** selfie_prod
- **User:** root
- **Password:** Nagaraj@07
- **Host:** localhost
- **Port:** 3306

### Important Tables
- `events` - Event information
- `stores` - Store details
- `users` - User accounts
- `selfies` - Selfie uploads

### Backup Command
```bash
mysqldump -u root -pNagaraj@07 selfie_prod > /opt/tanishq/backups/backup_$(date +%Y%m%d_%H%M%S).sql
```

### Restore Command
```bash
mysql -u root -pNagaraj@07 selfie_prod < /opt/tanishq/backups/backup_YYYYMMDD_HHMMSS.sql
```

---

## 📦 Project Structure

```
tanishq_selfie_app/
├── src/
│   └── main/
│       ├── java/              # Java source code
│       ├── resources/
│       │   ├── application.properties
│       │   ├── application-production.properties
│       │   └── static/        # Frontend files
│       └── webapp/
├── target/                    # Build output (WAR file)
├── storage/                   # Uploaded files
├── database_backup/           # Database backups
├── pom.xml                    # Maven configuration
├── build-preprod.bat          # Build script (Windows)
├── deploy_production.sh       # Deployment script (Linux)
└── README.md                  # This file

```

---

## 🔧 Build Profiles

### Production Profile
```bash
mvn clean package -P production
```
- Uses production database
- Optimized build
- Security enabled

### Pre-Production Profile
```bash
mvn clean package -P preprod
```
- Uses preprod database
- Debug enabled
- Testing features enabled

---

## 🔐 Security Notes

### Database Security
- MySQL runs on localhost only (not exposed externally)
- Use strong passwords
- Regular backups recommended

### Application Security
- CORS configured for allowed origins
- File upload restrictions in place
- SQL injection protection via JPA

---

## 📊 Data Import (CSV)

### Import Events from CSV

1. **Upload CSV to server:**
   ```batch
   scp events.csv root@10.10.63.97:/opt/tanishq/csv_import/
   ```

2. **Import using MySQL:**
   ```bash
   mysql -u root -pNagaraj@07 selfie_prod
   ```
   
3. **Load data:**
   ```sql
   LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv'
   INTO TABLE events
   FIELDS TERMINATED BY ','
   ENCLOSED BY '"'
   LINES TERMINATED BY '\n'
   IGNORE 1 ROWS;
   ```

---

## 🔍 Troubleshooting

### Application Not Starting
1. Check if port is already in use: `netstat -tuln | grep 8080`
2. Check logs: `tail -f /opt/tanishq/logs/application.log`
3. Verify MySQL is running: `systemctl status mysqld`

### Database Connection Issues
1. Test connection: `mysql -u root -pNagaraj@07 -e "SELECT 1;"`
2. Check MySQL logs: `tail -f /var/log/mysqld.log`
3. Verify credentials in `application-production.properties`

### 502 Bad Gateway
1. Check if application is running: `ps aux | grep tanishq`
2. Check Nginx config: `nginx -t`
3. Restart Nginx: `systemctl restart nginx`

---

## 📞 Support Contacts

### Database Admin
- **User:** root
- **Access:** SSH to 10.10.63.97

### Application Logs Location
- `/opt/tanishq/logs/application.log`
- `/opt/tanishq/logs/error.log`

---

## ✅ Deployment Checklist

Before deploying to production:

- [ ] Build WAR file with production profile
- [ ] Backup current database
- [ ] Stop existing application
- [ ] Upload new WAR file
- [ ] Deploy using script
- [ ] Verify application starts
- [ ] Test core functionality
- [ ] Check logs for errors
- [ ] Verify database connections
- [ ] Test file uploads

---

## 🎯 Quick Commands Reference

### Build
```batch
mvn clean package -P production
```

### Deploy
```bash
./deploy_production.sh
```

### Check Status
```bash
ps aux | grep tanishq
systemctl status mysqld
```

### View Logs
```bash
tail -f /opt/tanishq/logs/application.log
```

### Restart Application
```bash
pkill -f tanishq-preprod
cd /opt/tanishq
nohup java -jar tanishq-preprod-21-01-2026-1-0.0.1-SNAPSHOT.war --server.port=8080 --spring.profiles.active=production > logs/application.log 2>&1 &
```

---

## 📝 Notes

- All unnecessary documentation and temporary script files have been removed
- Only essential deployment files are retained
- Database and server functionality remain unaffected
- This README contains all critical deployment information

---

**End of Documentation**

