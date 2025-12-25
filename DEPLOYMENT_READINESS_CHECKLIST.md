# Pre-Production Deployment - Complete Verification Checklist

## ✅ What You ALREADY HAVE (Ready on Your Machine)

### 1. Application Files ✅
- ✅ **Source Code:** Complete Spring Boot application
- ✅ **Frontend Files:** Static files in `src/main/resources/static/`
- ✅ **pom.xml:** Configured with `tanishq-preprod-02-12-2025-1`
- ✅ **Profile:** Set to `preprod` in `application.properties`

### 2. Configuration Files ✅
- ✅ **application-preprod.properties:** Created and configured
  - Port: 3002
  - Database: tanishq_preprod
  - Server: 10.160.128.94
  - ⚠️ **ACTION NEEDED:** Update database password

### 3. Google Service Account Key ✅
- ✅ **tanishqgmb-5437243a8085.p12:** Found in `src/main/resources/`
- ✅ This file exists and will be packaged in WAR
- 📋 **NEED TO COPY TO SERVER:** `/opt/tanishq/tanishqgmb-5437243a8085.p12`

### 4. SSL Certificates ✅
- ✅ **event-images-469618-32e65f6d62b3.p12:** Found
- ✅ **local-ssl.p12:** Found
- ✅ Packaged in WAR file

### 5. Documentation & Scripts ✅
- ✅ **DATABASE_MIGRATION_STEP_BY_STEP.md:** Created
- ✅ **DATABASE_MIGRATION_QUICK_REF.md:** Created
- ✅ **DATABASE_MIGRATION_GUIDE.md:** Created
- ✅ **PREPROD_DEPLOYMENT_GUIDE.md:** Created
- ✅ **PREPROD_DEPLOYMENT_CHECKLIST.md:** Created
- ✅ **README_PREPROD.md:** Created
- ✅ **export_database_for_preprod.bat:** Created
- ✅ **build-preprod.bat:** Created
- ✅ **deploy-preprod.sh:** Created
- ✅ **import_database_preprod.sh:** Created

### 6. Build Tools ✅
- ✅ **Maven:** Installed (you're using it)
- ✅ **Java 11:** Need to verify (use `java -version`)
- ✅ **MySQL:** Installed locally with database `tanishq`

### 7. Excel Files ✅
- ✅ **attendees_sample_format.xlsx:** Found
- ✅ **contact.xlsx:** Found
- ✅ **EventAttendedRepository.xlsx:** Found
- ✅ Packaged in WAR file

---

## ⚠️ What You NEED TO DO (Before First Deployment)

### 1. Local Machine Setup ⚠️

#### A. Verify Java Version
```cmd
java -version
```
**Must show Java 11.** If not:
- Download and install JDK 11
- Set JAVA_HOME environment variable
- Update PATH

#### B. Update Database Password ⚠️ **CRITICAL**
**File:** `src/main/resources/application-preprod.properties`
**Line 7:** Change `CHANGE_THIS_PASSWORD` to actual password
```properties
spring.datasource.password=YOUR_ACTUAL_PREPROD_DB_PASSWORD
```

#### C. Verify Local Database Connection ✅
- MySQL should be running on localhost
- Database `tanishq` should exist with data
- User `nagaraj_jadar` should have access

---

### 2. Server Setup (10.160.128.94) ⚠️

You mentioned you've **already created the database** on the server. Let me verify what else is needed:

#### A. Database Setup ✅ (You said this is done)
- ✅ Database created: `tanishq_preprod`
- ✅ User created: `tanishq_preprod`
- ⚠️ **VERIFY:** User has proper permissions

**To verify on server:**
```bash
mysql -u tanishq_preprod -p
# Enter password
SHOW DATABASES;
USE tanishq_preprod;
SHOW TABLES;
EXIT;
```

If no tables shown, you need to **migrate data** (see next section).

#### B. Directory Structure ⚠️ **NEEDS CREATION**
These directories must exist on the server:

```bash
# Connect to server via PuTTY as nishal
sudo su root

# Create application directory
mkdir -p /opt/tanishq/applications_preprod

# Create storage directory for images
mkdir -p /opt/tanishq/storage/selfie_images

# Set permissions
chmod -R 755 /opt/tanishq/storage
chown -R nishal:nishal /opt/tanishq
```

**Status:** ❓ **NEED TO CHECK** if these exist

#### C. Required Files on Server ⚠️ **NEED TO UPLOAD**

These files must be uploaded to `/opt/tanishq/`:

1. **tanishqgmb-5437243a8085.p12** ⚠️
   - Location on your machine: `src/main/resources/tanishqgmb-5437243a8085.p12`
   - Upload to server: `/opt/tanishq/tanishqgmb-5437243a8085.p12`
   - Used for Google Sheets integration

2. **tanishq_selfie_app_store_data.xlsx** ❓
   - Need to verify if this file exists on your machine
   - If exists, upload to: `/opt/tanishq/tanishq_selfie_app_store_data.xlsx`
   - Used for store details

**Status:** ❓ **NEED TO UPLOAD**

#### D. Java Installation ⚠️ **NEED TO VERIFY**
```bash
# On server, check Java version
java -version
```
**Must be Java 11.** If not installed:
```bash
# For CentOS/RHEL
sudo yum install java-11-openjdk

# For Ubuntu/Debian
sudo apt-get install openjdk-11-jdk
```

**Status:** ❓ **NEED TO VERIFY**

#### E. Web Server Configuration ⚠️ **NEED TO CONFIGURE**

**Nginx or Apache** must be configured to proxy requests from domain to application.

**Domain:** http://celebrations-preprod.tanishq.co.in/  
**Proxy to:** localhost:3002

**Example Nginx configuration:**
```nginx
server {
    listen 80;
    server_name celebrations-preprod.tanishq.co.in;
    
    location / {
        proxy_pass http://localhost:3002;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Status:** ❓ **NEED TO CONFIGURE**

#### F. DNS Configuration ❓
- Domain `celebrations-preprod.tanishq.co.in` must point to `10.160.128.94`
- This is usually handled by your network/IT team

**Status:** ❓ **NEED TO VERIFY**

---

### 3. Database Migration ⚠️ **MUST DO**

You have the database created but **NO DATA** yet. You need to migrate:

#### What to migrate:
- All tables structure
- All data from localhost
- Stored procedures (if any)
- Triggers (if any)

#### How to migrate:
**Use the automated script I created:**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
export_database_for_preprod.bat
```

Then follow: `DATABASE_MIGRATION_STEP_BY_STEP.md`

**Status:** ⚠️ **REQUIRED BEFORE DEPLOYMENT**

---

### 4. Access Requirements ✅

You should have these:
- ✅ **FortiClient VPN:** Access credentials
- ✅ **WinSCP:** Software installed
- ✅ **PuTTY:** Software installed
- ✅ **Server Credentials:**
  - Hostname: 10.160.128.94
  - Username: nishal
  - Private key file
- ✅ **Database Credentials:**
  - Username: tanishq_preprod
  - Password: [You should have this]

---

## 📋 Complete Pre-Deployment Checklist

### On Your Local Machine:

- [ ] **Java 11 verified:** `java -version` shows Java 11
- [ ] **Database password updated** in `application-preprod.properties`
- [ ] **Local database** has all your data
- [ ] **Maven installed:** `mvn -version` works
- [ ] **VPN credentials** available
- [ ] **WinSCP installed** and configured
- [ ] **PuTTY installed** and configured
- [ ] **Private key file** accessible

### On Pre-Prod Server (10.160.128.94):

- [ ] **Can connect via VPN**
- [ ] **Can SSH/PuTTY** to server as nishal
- [ ] **Database exists:** `tanishq_preprod`
- [ ] **Database user exists:** `tanishq_preprod` with password
- [ ] **Database user has permissions** on tanishq_preprod
- [ ] **Directories created:**
  - [ ] `/opt/tanishq/applications_preprod`
  - [ ] `/opt/tanishq/storage/selfie_images`
- [ ] **Java 11 installed** on server
- [ ] **Files uploaded to /opt/tanishq/:**
  - [ ] `tanishqgmb-5437243a8085.p12`
  - [ ] `tanishq_selfie_app_store_data.xlsx` (if exists)
- [ ] **Nginx/Apache configured** for domain
- [ ] **Domain resolves** to 10.160.128.94
- [ ] **Database migrated** from localhost

---

## ❌ What You DON'T Need (Already Handled)

### You DON'T need to:
- ❌ Install MySQL on server (already done - database exists)
- ❌ Create separate frontend build (packaged in WAR)
- ❌ Install Tomcat (Spring Boot embedded)
- ❌ Configure SSL certificates (handled by Nginx/Apache)
- ❌ Set up load balancer (single server)
- ❌ Configure firewall rules (handled by IT team)
- ❌ Create system services (using nohup)

---

## 🎯 Quick Verification Script

I'll create a script to help you verify everything:

### For Windows (Check Local Setup):
```cmd
@echo off
echo ========================================
echo Pre-Production Deployment Verification
echo ========================================
echo.

echo [1/6] Checking Java version...
java -version
echo.

echo [2/6] Checking Maven...
mvn -version
echo.

echo [3/6] Checking if database password is updated...
findstr /C:"CHANGE_THIS_PASSWORD" src\main\resources\application-preprod.properties
if %ERRORLEVEL% EQU 0 (
    echo WARNING: Database password not updated!
) else (
    echo OK: Database password appears to be updated
)
echo.

echo [4/6] Checking if profile is set to preprod...
findstr /C:"spring.profiles.active=preprod" src\main\resources\application.properties
if %ERRORLEVEL% EQU 0 (
    echo OK: Profile set to preprod
) else (
    echo WARNING: Profile not set to preprod
)
echo.

echo [5/6] Checking if required files exist...
if exist "src\main\resources\tanishqgmb-5437243a8085.p12" (
    echo OK: tanishqgmb-5437243a8085.p12 found
) else (
    echo ERROR: tanishqgmb-5437243a8085.p12 not found
)
echo.

echo [6/6] Checking documentation...
if exist "DATABASE_MIGRATION_STEP_BY_STEP.md" (
    echo OK: Migration guide found
) else (
    echo WARNING: Migration guide not found
)
echo.

echo ========================================
echo Verification Complete
echo ========================================
pause
```

### For Server (Check Server Setup):
```bash
#!/bin/bash
echo "========================================"
echo "Pre-Production Server Verification"
echo "========================================"
echo ""

echo "[1/8] Checking Java version..."
java -version
echo ""

echo "[2/8] Checking MySQL installation..."
mysql --version
echo ""

echo "[3/8] Checking if database exists..."
mysql -u tanishq_preprod -p -e "SHOW DATABASES LIKE 'tanishq_preprod';"
echo ""

echo "[4/8] Checking application directory..."
if [ -d "/opt/tanishq/applications_preprod" ]; then
    echo "OK: Application directory exists"
else
    echo "ERROR: Application directory not found"
fi
echo ""

echo "[5/8] Checking storage directory..."
if [ -d "/opt/tanishq/storage/selfie_images" ]; then
    echo "OK: Storage directory exists"
else
    echo "ERROR: Storage directory not found"
fi
echo ""

echo "[6/8] Checking required files..."
if [ -f "/opt/tanishq/tanishqgmb-5437243a8085.p12" ]; then
    echo "OK: Google service key found"
else
    echo "ERROR: Google service key not found"
fi
echo ""

echo "[7/8] Checking Nginx/Apache..."
if systemctl status nginx >/dev/null 2>&1; then
    echo "OK: Nginx is running"
elif systemctl status httpd >/dev/null 2>&1; then
    echo "OK: Apache is running"
else
    echo "WARNING: No web server detected"
fi
echo ""

echo "[8/8] Checking port 3002..."
if netstat -tlnp 2>/dev/null | grep -q ":3002"; then
    echo "WARNING: Port 3002 is already in use"
else
    echo "OK: Port 3002 is available"
fi
echo ""

echo "========================================"
echo "Verification Complete"
echo "========================================"
```

---

## 🚀 What You Should Do NOW

### Step 1: Verify Local Setup (5 minutes)
```cmd
# Check Java version
java -version

# Check if password updated
notepad src\main\resources\application-preprod.properties
# Look for line 7, make sure it's not "CHANGE_THIS_PASSWORD"
```

### Step 2: Check Server Access (5 minutes)
```
1. Connect FortiClient VPN
2. Open PuTTY → Connect to 10.160.128.94
3. Login as nishal
4. Check if you can access: sudo su root
```

### Step 3: Verify Server Setup (10 minutes)
```bash
# On server via PuTTY

# Check Java
java -version

# Check database
mysql -u tanishq_preprod -p
# Enter password, then:
SHOW DATABASES;
USE tanishq_preprod;
SHOW TABLES;
EXIT;

# Check directories
ls -la /opt/tanishq/
ls -la /opt/tanishq/applications_preprod/
ls -la /opt/tanishq/storage/

# Check if files exist
ls -la /opt/tanishq/*.p12
ls -la /opt/tanishq/*.xlsx
```

### Step 4: Create What's Missing (15 minutes)

**If directories don't exist:**
```bash
sudo su root
mkdir -p /opt/tanishq/applications_preprod
mkdir -p /opt/tanishq/storage/selfie_images
chmod -R 755 /opt/tanishq/storage
chown -R nishal:nishal /opt/tanishq
```

**If files don't exist on server:**
1. Open WinSCP
2. Connect to 10.160.128.94
3. Upload from local `src/main/resources/`:
   - `tanishqgmb-5437243a8085.p12` → `/opt/tanishq/`

### Step 5: Migrate Database (20 minutes)
```cmd
# On your Windows machine
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
export_database_for_preprod.bat

# Then follow DATABASE_MIGRATION_STEP_BY_STEP.md
```

### Step 6: First Deployment (20 minutes)
```cmd
# Build application
build-preprod.bat

# Then follow PREPROD_DEPLOYMENT_CHECKLIST.md
```

---

## 📊 Summary: What You Have vs What You Need

| Item | Status | Action Required |
|------|--------|-----------------|
| **Application Code** | ✅ Ready | None |
| **Configuration Files** | ✅ Ready | Update DB password |
| **Build Scripts** | ✅ Ready | None |
| **Documentation** | ✅ Ready | Read guides |
| **Java 11 (Local)** | ❓ Unknown | Verify with `java -version` |
| **Java 11 (Server)** | ❓ Unknown | Verify on server |
| **Database (Server)** | ✅ Created | Migrate data |
| **Directories (Server)** | ❓ Unknown | Create if missing |
| **Files (Server)** | ❓ Unknown | Upload .p12 file |
| **Nginx Configuration** | ❓ Unknown | Configure or verify |
| **DNS Configuration** | ❓ Unknown | Verify with IT team |

---

## ✅ You're Almost Ready!

### What You HAVE:
✅ Complete application code  
✅ All configuration files  
✅ All documentation and guides  
✅ All build and deployment scripts  
✅ Database created on server  
✅ Required .p12 files locally  

### What You NEED:
⚠️ Update database password in config  
⚠️ Verify/install Java 11 on both machines  
⚠️ Create directories on server  
⚠️ Upload .p12 file to server  
⚠️ Migrate database from localhost  
⚠️ Verify/configure Nginx  
⚠️ Verify DNS  

### Estimated Time to Complete:
- **If server is fully set up:** 30-45 minutes
- **If server needs setup:** 1-2 hours

---

## 📞 Next Steps

1. **Read this document completely** ✅ You're doing it now
2. **Run verification steps** (Step 1-3 above)
3. **Create missing items** (Step 4 above)
4. **Migrate database** (Step 5 above)
5. **Deploy application** (Step 6 above)

---

**Document Created:** December 2, 2025  
**Status:** Ready for verification  
**Next Action:** Run Step 1-3 verification steps

