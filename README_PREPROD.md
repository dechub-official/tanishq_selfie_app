# Pre-Production Environment - Complete Setup Package

## 📋 Overview

This package contains all the necessary files and documentation to deploy the Tanishq Celebrations application to the pre-production environment.

**Pre-Production Server:** 10.160.128.94  
**Domain:** http://celebrations-preprod.tanishq.co.in/  
**Status:** ✅ Configured and Ready for Deployment

---

## 📁 Files in This Package

### 1. Configuration Files

#### `application-preprod.properties`
**Location:** `src/main/resources/application-preprod.properties`  
**Purpose:** Pre-production environment configuration  
**Action Required:** ⚠️ Update database password before first deployment

**Key Settings:**
- Server Port: 3002
- Database: tanishq_preprod
- File paths configured for Linux server
- Email configuration included

### 2. Documentation Files

#### `PREPROD_DEPLOYMENT_GUIDE.md` 📖
**Purpose:** Comprehensive step-by-step deployment guide  
**Use When:** First deployment or need detailed instructions  
**Contains:**
- One-time server setup procedures
- Database setup instructions
- Regular deployment procedure
- Nginx/Apache configuration
- Troubleshooting guide

#### `PREPROD_DEPLOYMENT_CHECKLIST.md` ✅
**Purpose:** Quick reference checklist for deployments  
**Use When:** Regular deployments  
**Contains:**
- Pre-deployment checklist
- Deployment steps
- Verification steps
- Quick command reference
- Troubleshooting quick fixes

#### `PREPROD_SETUP_SUMMARY.md` 📊
**Purpose:** Complete overview of the pre-prod setup  
**Use When:** Understanding the overall setup or reference  
**Contains:**
- Environment comparison (prod vs preprod)
- File locations and configurations
- Access credentials
- Maintenance procedures

#### `DATABASE_MIGRATION_GUIDE.md` 📚 🆕
**Purpose:** Complete database migration guide  
**Use When:** Migrating database from localhost to pre-prod  
**Contains:**
- 4 different migration methods
- Step-by-step instructions
- Database name mismatch solutions
- Troubleshooting guide
- MySQL Workbench GUI method

#### `DATABASE_MIGRATION_QUICK_REF.md` ⚡ 🆕
**Purpose:** Quick reference for database migration  
**Use When:** Quick migration steps  
**Contains:**
- 4-step migration process
- Common commands
- Troubleshooting quick fixes
- Verification checklist

### 3. Helper Scripts

#### `build-preprod.bat` (Windows)
**Purpose:** Automated build process for Windows  
**Usage:**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
build-preprod.bat
```
**Features:**
- Java version verification
- Profile verification
- Maven clean install
- Build status reporting

#### `deploy-preprod.sh` (Linux)
**Purpose:** Server-side deployment helper  
**Usage:**
```bash
cd /opt/tanishq/applications_preprod
chmod +x deploy-preprod.sh
./deploy-preprod.sh
```
**Features:**
- Process identification
- Guided deployment steps
- Log monitoring commands

#### `export_database_for_preprod.bat` (Windows) 🆕
**Purpose:** Export local database for migration to pre-prod  
**Usage:**
```cmd
export_database_for_preprod.bat
```
**Features:**
- Exports localhost tanishq database
- Creates timestamped backup
- Provides next steps

#### `import_database_preprod.sh` (Linux) 🆕
**Purpose:** Import database on pre-prod server  
**Usage:**
```bash
cd /tmp
./import_database_preprod.sh
```
**Features:**
- Automated import process
- Handles database name mismatch
- Verification and cleanup

---

## 🚀 Quick Start Guide

### First Time Deployment

1. **Review Documentation**
   - Read: `PREPROD_SETUP_SUMMARY.md`
   - Review: `PREPROD_DEPLOYMENT_GUIDE.md`

2. **Update Configuration**
   - Edit: `application-preprod.properties`
   - Set database password (replace `CHANGE_THIS_PASSWORD`)

3. **Verify Application Settings**
   - Check: `application.properties` → `spring.profiles.active=preprod` ✅
   - Check: `pom.xml` → `artifactId=tanishq-preprod-02-12-2025-1` ✅

4. **Build Application**
   - Run: `build-preprod.bat`
   - Or manually: `mvn clean install`

5. **Deploy to Server**
   - Follow: `PREPROD_DEPLOYMENT_CHECKLIST.md`
   - Use WinSCP to upload WAR file
   - Use PuTTY to deploy on server

### Regular Deployments

1. **Use the Checklist**
   - Open: `PREPROD_DEPLOYMENT_CHECKLIST.md`
   - Follow each checkbox

2. **Update Before Build**
   - Frontend files copied ✅
   - Profile set to preprod ✅
   - artifactId updated with new date ✅

3. **Build and Deploy**
   - Run: `build-preprod.bat`
   - Upload via WinSCP
   - Deploy using checklist steps

---

## ⚙️ Current Configuration Status

### Application Configuration ✅
```properties
# application.properties
spring.profiles.active=preprod
```

### Build Configuration ✅
```xml
<!-- pom.xml -->
<artifactId>tanishq-preprod-02-12-2025-1</artifactId>
```

### Pre-Prod Environment ✅
```properties
# application-preprod.properties
server.port=3002
spring.datasource.url=jdbc:mysql://10.160.128.94:3306/tanishq_preprod
spring.datasource.username=tanishq_preprod
spring.datasource.password=CHANGE_THIS_PASSWORD ⚠️
```

---

## ⚠️ Important Actions Required

### Before First Deployment:

1. **Database Password** ⚠️ REQUIRED
   ```properties
   # In application-preprod.properties
   spring.datasource.password=CHANGE_THIS_PASSWORD
   ```
   Change this to the actual password set during database creation.

2. **Migrate Database Data** ⚠️ REQUIRED
   You need to migrate your database from localhost to the pre-prod server:
   
   **Quick Steps:**
   ```cmd
   REM Step 1: Export from localhost
   export_database_for_preprod.bat
   
   REM Step 2: Transfer via WinSCP to server /tmp/
   
   REM Step 3: Import on server (via PuTTY)
   mysql -u tanishq_preprod -p tanishq_preprod < /tmp/tanishq_backup_*.sql
   ```
   
   **Detailed Instructions:** See `DATABASE_MIGRATION_QUICK_REF.md`
   **Complete Guide:** See `DATABASE_MIGRATION_GUIDE.md`

2. **Server Setup Verification** ⚠️ REQUIRED
   Ensure the following are complete on server 10.160.128.94:
   - [ ] Directories created: `/opt/tanishq/applications_preprod`
   - [ ] Storage created: `/opt/tanishq/storage/selfie_images`
   - [ ] Java 11 installed
   - [ ] Database created: `tanishq_preprod`
   - [ ] Database user created with permissions
   - [ ] Required files uploaded (p12, xlsx)
   - [ ] Nginx/Apache configured for domain

3. **Update artifactId for Each Deployment**
   ```xml
   <!-- Before each deployment, update in pom.xml -->
   <artifactId>tanishq-preprod-DD-MM-YYYY-N</artifactId>
   ```
   Example: `tanishq-preprod-03-12-2025-1` (for December 3, 2025, first deployment)

---

## 📊 Deployment Workflow

```
┌─────────────────────────────────────┐
│  LOCAL MACHINE (Windows)            │
├─────────────────────────────────────┤
│ 1. Copy frontend static files       │
│ 2. Update index.html (if needed)    │
│ 3. Set profile: preprod              │
│ 4. Update artifactId in pom.xml     │
│ 5. Run: build-preprod.bat           │
│ 6. Verify BUILD SUCCESS             │
└──────────────┬──────────────────────┘
               │
               │ WAR File Transfer
               ▼
┌─────────────────────────────────────┐
│  VPN CONNECTION                     │
├─────────────────────────────────────┤
│ • Connect FortiClient VPN           │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  FILE TRANSFER (WinSCP)             │
├─────────────────────────────────────┤
│ • Hostname: 10.160.128.94           │
│ • Upload WAR to:                    │
│   /opt/tanishq/applications_preprod │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  SERVER (PuTTY)                     │
├─────────────────────────────────────┤
│ 1. sudo su root                     │
│ 2. cd /opt/tanishq/applications_... │
│ 3. ps -ef | grep tanishq-preprod    │
│ 4. kill [process_id]                │
│ 5. nohup java -jar [war] > [log] &  │
│ 6. tail -f [log]                    │
│ 7. Verify startup                   │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  VERIFICATION                       │
├─────────────────────────────────────┤
│ • Process running                   │
│ • Website accessible                │
│ • Features working                  │
└─────────────────────────────────────┘
```

---

## 🎯 Success Criteria

Your deployment is successful when:

✅ WAR file builds without errors  
✅ File uploaded to server  
✅ Application process starts  
✅ Logs show "Started TanishqSelfieApplication"  
✅ Process visible in `ps -ef | grep tanishq-preprod`  
✅ Port 3002 is listening  
✅ Website loads at http://celebrations-preprod.tanishq.co.in/  
✅ Login functionality works  
✅ Key features are operational  

---

## 📞 Support & Troubleshooting

### Documentation References
1. **Detailed Issues:** See `PREPROD_DEPLOYMENT_GUIDE.md` - Troubleshooting section
2. **Quick Fixes:** See `PREPROD_DEPLOYMENT_CHECKLIST.md` - Troubleshooting section
3. **Configuration Issues:** Review `PREPROD_SETUP_SUMMARY.md`

### Common Issues

#### Build Fails
- **Check:** Java version is 11
- **Check:** All dependencies downloaded
- **Check:** No compilation errors
- **See:** `PREPROD_DEPLOYMENT_GUIDE.md` - Troubleshooting

#### Application Won't Start
- **Check:** Database password is correct
- **Check:** Port 3002 is available
- **Check:** Java 11 is on server
- **See:** `PREPROD_DEPLOYMENT_CHECKLIST.md` - Troubleshooting Quick Fix

#### Cannot Access Website
- **Check:** Application process is running
- **Check:** Nginx/Apache is running
- **Check:** VPN is connected
- **See:** `PREPROD_DEPLOYMENT_GUIDE.md` - Cannot Access Website

---

## 📝 Deployment Checklist Summary

### Pre-Deployment
- [ ] Frontend files copied
- [ ] Profile = preprod
- [ ] artifactId updated
- [ ] Build successful

### Deployment
- [ ] VPN connected
- [ ] WAR uploaded
- [ ] Old process killed
- [ ] New process started

### Verification
- [ ] Process running
- [ ] Logs clean
- [ ] Website accessible
- [ ] Features tested

---

## 🔐 Access Information

### VPN
- **Tool:** FortiClient VPN
- **Credentials:** Use provided credentials

### Server Access
- **IP:** 10.160.128.94
- **Username:** nishal
- **Auth:** Private key from downloads
- **Root:** `sudo su root`

### Database
- **Host:** 10.160.128.94:3306
- **Database:** tanishq_preprod
- **Username:** tanishq_preprod
- **Password:** [Set during setup]

---

## 🔄 Differences from Production

| Aspect | Production | Pre-Production |
|--------|-----------|----------------|
| Server IP | 10.10.63.97 | 10.160.128.94 |
| Domain | celebrations.tanishq.co.in | celebrations-preprod.tanishq.co.in |
| Port | 3001 | 3002 |
| Profile | prod | preprod |
| Database | tanishq | tanishq_preprod |
| Deploy Dir | applications_one | applications_preprod |
| War Prefix | tanishq-DD-MM-YYYY-N | tanishq-preprod-DD-MM-YYYY-N |

---

## 📅 Maintenance

### Regular Tasks
- Clean old log files (>30 days)
- Remove old WAR files (>30 days)
- Backup database regularly
- Monitor application health

### Log Cleanup Commands
```bash
# Find old logs
find /opt/tanishq/applications_preprod -name "*.log" -mtime +30

# Delete old logs
find /opt/tanishq/applications_preprod -name "*.log" -mtime +30 -delete
```

---

## 📚 Documentation Map

```
PREPROD_SETUP_SUMMARY.md
└── Complete overview and reference
    ├── Environment details
    ├── File locations
    ├── Configuration comparison
    └── Maintenance procedures

PREPROD_DEPLOYMENT_GUIDE.md
└── Comprehensive deployment instructions
    ├── One-time server setup
    ├── Database setup
    ├── Nginx configuration
    ├── Regular deployment steps
    └── Detailed troubleshooting

PREPROD_DEPLOYMENT_CHECKLIST.md
└── Quick reference for deployments
    ├── Step-by-step checklist
    ├── Quick commands
    ├── Verification steps
    └── Quick troubleshooting

DATABASE_MIGRATION_GUIDE.md
└── Complete database migration guide
    ├── 4 migration methods
    ├── Detailed instructions
    ├── Database name handling
    └── Full troubleshooting

DATABASE_MIGRATION_QUICK_REF.md
└── Quick database migration reference
    ├── 4-step process
    ├── Common commands
    └── Quick fixes

README_PREPROD.md (this file)
└── Getting started and overview
    ├── File descriptions
    ├── Quick start guide
    ├── Current status
    └── Action items
    └── Action items
```

---

## ✨ Next Steps

1. **Read the Documentation**
   - Start with this README
   - Review PREPROD_SETUP_SUMMARY.md
   - Study PREPROD_DEPLOYMENT_GUIDE.md

2. **Complete Setup**
   - Update database password in application-preprod.properties
   - Verify server setup is complete
   - Test build locally

3. **First Deployment**
   - Follow PREPROD_DEPLOYMENT_GUIDE.md completely
   - Use PREPROD_DEPLOYMENT_CHECKLIST.md for verification
   - Test all features thoroughly

4. **Regular Deployments**
   - Use PREPROD_DEPLOYMENT_CHECKLIST.md
   - Keep artifactId updated
   - Maintain deployment logs

---

## 📝 Notes

- All configuration files are ready and in place
- Profile is set to `preprod` ✅
- artifactId is configured with today's date ✅
- Only database password needs to be updated before first deployment ⚠️
- All documentation is comprehensive and detailed
- Helper scripts are provided for both Windows and Linux

---

## ✅ Checklist - Before First Deployment

- [ ] Read all documentation
- [ ] Update database password in application-preprod.properties
- [ ] Verify server setup is complete
- [ ] Test build with build-preprod.bat
- [ ] Have VPN credentials ready
- [ ] Have WinSCP and PuTTY installed
- [ ] Know how to access private key
- [ ] Understand the deployment workflow
- [ ] Ready to follow the checklist

---

**Package Version:** 1.0  
**Created:** December 2, 2025  
**Status:** Ready for Deployment  
**Last Updated:** December 2, 2025

---

**🎉 You're all set! Follow the guides and you'll have the pre-production environment up and running smoothly.**

