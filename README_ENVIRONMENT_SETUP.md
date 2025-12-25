# 🎉 TANISHQ CELEBRATION APP - MULTI-ENVIRONMENT SETUP COMPLETE! 

## ✅ What Has Been Set Up

Your application now supports **TWO COMPLETELY SEPARATE ENVIRONMENTS**:

### 🔵 Pre-Production Environment
- **Database:** `selfie_preprod` ✓ (Already exists)
- **Port:** 3000
- **URL:** https://celebrationsite-preprod.tanishq.co.in
- **Purpose:** Testing and development

### 🔴 Production Environment  
- **Database:** `selfie_prod` ⚠️ (Needs to be created)
- **Port:** 3001
- **URL:** https://celebrationsite.tanishq.co.in
- **Purpose:** Live production system

---

## 📦 NEW FILES CREATED

| File | Purpose |
|------|---------|
| **BUILD_PROD.bat** | Build for production environment |
| **BUILD_ENVIRONMENT_SWITCHER.bat** | Interactive menu to switch environments |
| **SETUP_PRODUCTION_DATABASE.bat** | One-click database setup |
| **setup_production_database.sql** | SQL script for database creation |
| **MULTI_ENVIRONMENT_SETUP_GUIDE.md** | Complete documentation |
| **QUICK_REFERENCE.txt** | Quick reference guide |
| **README_ENVIRONMENT_SETUP.md** | This file |

---

## 🚀 HOW TO USE - STEP BY STEP

### STEP 1: Set Up Production Database (One-Time Setup)

**Option A: Automatic Setup (RECOMMENDED)**
```batch
Double-click: SETUP_PRODUCTION_DATABASE.bat
```

**Option B: Manual Setup**
```sql
mysql -u root -p
CREATE DATABASE selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### STEP 2: Build for Your Environment

**🎯 EASY WAY - Use the Environment Switcher:**
```batch
Double-click: BUILD_ENVIRONMENT_SWITCHER.bat
```
This will show you an interactive menu where you can:
- Build for Pre-Production
- Build for Production  
- View current settings
- Test database connections

**📝 DIRECT WAY - Use Individual Scripts:**

For Pre-Production:
```batch
BUILD_PREPROD.bat
```

For Production:
```batch
BUILD_PROD.bat
```

---

## 🎯 TYPICAL WORKFLOW

### Scenario 1: Testing New Features
```
1. Make code changes
2. Run: BUILD_PREPROD.bat
3. Deploy to pre-prod server
4. Test at: https://celebrationsite-preprod.tanishq.co.in
5. If everything works → Go to Scenario 2
```

### Scenario 2: Going Live to Production
```
1. After thorough testing in pre-prod
2. Run: BUILD_PROD.bat
3. Deploy to production server
4. Go live at: https://celebrationsite.tanishq.co.in
```

### Scenario 3: Both Running Simultaneously
```
✅ Pre-prod running on port 3000 → selfie_preprod database
✅ Production running on port 3001 → selfie_prod database
✅ NO CONFLICTS! Both work independently!
```

---

## 🗄️ DATABASE CONFIGURATION

### Connection Details (Same for Both)
```
Host: localhost
Port: 3306
Username: root
Password: Dechub#2025
```

### Databases
```
selfie_preprod  ← Pre-Production
selfie_prod     ← Production
```

### Tables
Tables are **automatically created** by Spring Boot on first run!
No manual table creation needed!

---

## 📋 ENVIRONMENT COMPARISON

| Feature | Pre-Production | Production |
|---------|---------------|------------|
| Database | selfie_preprod | selfie_prod |
| Port | 3000 | 3001 |
| URL | celebrationsite-preprod | celebrationsite |
| Build Command | BUILD_PREPROD.bat | BUILD_PROD.bat |
| Maven Profile | `-Ppreprod` | `-Pprod` |
| SQL Logging | ON (verbose) | OFF (minimal) |
| Debug Level | DEBUG | INFO |
| S3 Bucket | celebrations-tanishq-preprod | celebrations-tanishq-prod |

---

## 🔧 CONFIGURATION FILES

### Modified Files
✓ **application-prod.properties** - Updated with complete MySQL configuration

### New Files  
✓ **BUILD_PROD.bat** - Production build script
✓ **BUILD_ENVIRONMENT_SWITCHER.bat** - Environment switcher
✓ **SETUP_PRODUCTION_DATABASE.bat** - Database setup
✓ **setup_production_database.sql** - SQL setup script

### Existing Files (No Changes Needed)
✓ **application.properties** - Main config (already set to preprod)
✓ **application-preprod.properties** - Pre-prod config (already working)
✓ **BUILD_PREPROD.bat** - Pre-prod build (already working)
✓ **pom.xml** - Maven config (already has profiles)

---

## 🎯 WHAT THE BUILD SCRIPTS DO

### BUILD_PREPROD.bat
```
1. Checks Maven & Java installation
2. Cleans previous builds
3. Runs: mvn clean package -Ppreprod -DskipTests
4. Creates WAR with pre-prod configuration
5. Shows deployment instructions
```

### BUILD_PROD.bat  
```
1. Checks Maven & Java installation
2. Cleans previous builds
3. Runs: mvn clean package -Pprod -DskipTests
4. Creates WAR with production configuration
5. Shows deployment instructions with warnings
```

### BUILD_ENVIRONMENT_SWITCHER.bat
```
Interactive menu with options:
1. Build for Pre-Production
2. Build for Production
3. View Current Settings
4. Test Pre-Prod Database Connection
5. Test Production Database Connection
0. Exit
```

---

## ⚙️ HOW IT WORKS BEHIND THE SCENES

### Maven Profiles (in pom.xml)
```xml
<profiles>
    <profile>
        <id>preprod</id>
        <properties>
            <spring.profiles.active>preprod</spring.profiles.active>
        </properties>
    </profile>
    
    <profile>
        <id>prod</id>
        <properties>
            <spring.profiles.active>prod</spring.profiles.active>
        </properties>
    </profile>
</profiles>
```

### When you run BUILD_PREPROD.bat:
```
→ Maven uses -Ppreprod flag
→ Spring Boot uses application-preprod.properties
→ Connects to selfie_preprod database
→ Uses preprod URLs for QR codes
→ Runs on port 3000
```

### When you run BUILD_PROD.bat:
```
→ Maven uses -Pprod flag
→ Spring Boot uses application-prod.properties
→ Connects to selfie_prod database
→ Uses production URLs for QR codes
→ Runs on port 3001
```

---

## 📤 DEPLOYMENT TO SERVER

### For Pre-Production Server:
```bash
# 1. Build locally
BUILD_PREPROD.bat

# 2. Connect to server (WinSCP/FileZilla)
# Upload WAR file to: /opt/tanishq/

# 3. Deploy
sudo systemctl stop tomcat
sudo cp [war-file].war /opt/tomcat/webapps/ROOT.war
sudo systemctl start tomcat
tail -f /opt/tomcat/logs/catalina.out
```

### For Production Server:
```bash
# 1. Build locally
BUILD_PROD.bat

# 2. Connect to server (WinSCP/FileZilla)
# Upload WAR file to: /opt/tanishq/

# 3. Deploy
sudo systemctl stop tomcat
sudo cp [war-file].war /opt/tomcat/webapps/ROOT.war
sudo systemctl start tomcat
tail -f /opt/tomcat/logs/catalina.out
```

---

## ✅ PRE-DEPLOYMENT CHECKLIST

### Before Deploying to Production:

Database Setup:
- [ ] MySQL 8.0 installed on production server
- [ ] Database `selfie_prod` created
- [ ] Root password is `Dechub#2025`
- [ ] Can connect: `mysql -u root -p`

Server Directories:
- [ ] Created: `/opt/tanishq/storage/selfie_images`
- [ ] Created: `/opt/tanishq/storage/bride_uploads`
- [ ] Permissions: `sudo chmod -R 755 /opt/tanishq`

Key Files:
- [ ] Uploaded: `/opt/tanishq/tanishqgmb-5437243a8085.p12`
- [ ] Uploaded: `/opt/tanishq/event-images-469618-32e65f6d62b3.p12`

Server Configuration:
- [ ] Tomcat is installed and running
- [ ] Port 3001 is open in firewall
- [ ] DNS points to production server
- [ ] SSL certificate installed (for HTTPS)

Testing:
- [ ] Thoroughly tested in pre-production
- [ ] All features working correctly
- [ ] Data migration plan ready (if needed)

---

## ✅ POST-DEPLOYMENT VERIFICATION

### Pre-Production Working:
- [ ] URL accessible: https://celebrationsite-preprod.tanishq.co.in
- [ ] Can login to admin panel
- [ ] Database has tables: `SELECT * FROM stores;`
- [ ] Events can be created
- [ ] QR codes generated with preprod URL
- [ ] File uploads work
- [ ] Google Sheets sync works

### Production Working:
- [ ] URL accessible: https://celebrationsite.tanishq.co.in
- [ ] Can login to admin panel
- [ ] Database has tables: `SELECT * FROM stores;`
- [ ] Events can be created
- [ ] QR codes generated with production URL
- [ ] File uploads work
- [ ] Google Sheets sync works
- [ ] Email notifications work

---

## 🆘 TROUBLESHOOTING

### Problem: "Database not found"
```sql
CREATE DATABASE selfie_prod CHARACTER SET utf8mb4;
```

### Problem: "Access denied for user"
```sql
GRANT ALL PRIVILEGES ON selfie_prod.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

### Problem: "Port already in use"
```
Pre-prod uses port 3000
Production uses port 3001
They don't conflict!
Check: netstat -ano | findstr "3000"
Check: netstat -ano | findstr "3001"
```

### Problem: "Build failed"
```batch
# Check Maven
mvn -version

# Check Java
java -version

# Clear Maven cache
mvn clean
```

### Problem: "Cannot connect to MySQL"
```sql
# Check MySQL service
services.msc → Look for MySQL

# Check connection
mysql -u root -p
# Password: Dechub#2025
```

### Problem: "File upload fails on server"
```bash
sudo mkdir -p /opt/tanishq/storage/selfie_images
sudo mkdir -p /opt/tanishq/storage/bride_uploads
sudo chmod -R 755 /opt/tanishq
```

---

## 📞 GETTING HELP

### Quick Reference
→ Read: **QUICK_REFERENCE.txt**

### Complete Documentation
→ Read: **MULTI_ENVIRONMENT_SETUP_GUIDE.md**

### Check Logs

**On Server:**
```bash
tail -f /opt/tomcat/logs/catalina.out
```

**Build Logs:**
Check console output during build

**Database:**
```sql
mysql -u root -p
SHOW DATABASES;
USE selfie_prod;
SHOW TABLES;
```

---

## 🎉 SUCCESS! YOU'RE READY TO GO!

### You now have:
✅ Two completely separate environments
✅ Easy build scripts for each environment
✅ Interactive environment switcher
✅ Database setup automation
✅ Complete isolation - no conflicts!

### Next Steps:

1. **Setup Production Database** (if not done yet)
   ```batch
   SETUP_PRODUCTION_DATABASE.bat
   ```

2. **Build for Pre-Production** (test first!)
   ```batch
   BUILD_PREPROD.bat
   ```

3. **Build for Production** (after testing)
   ```batch
   BUILD_PROD.bat
   ```

---

## 📝 SUMMARY

### To Build for Pre-Prod:
```batch
BUILD_PREPROD.bat
```

### To Build for Production:
```batch
BUILD_PROD.bat
```

### To Use Interactive Menu:
```batch
BUILD_ENVIRONMENT_SWITCHER.bat
```

### That's it! You're all set! 🚀

---

**Last Updated:** December 18, 2025  
**Version:** 1.0  
**Status:** ✅ Multi-Environment Setup Complete

