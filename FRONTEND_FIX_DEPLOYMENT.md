# 🔥 CRITICAL FIX: Frontend URL Redirection Issue

**Date:** December 8, 2025  
**Issue:** "Create Event" button redirects to PRODUCTION instead of PRE-PROD  
**Root Cause:** Backend is serving OLD frontend build with hardcoded production URLs  

---

## 🎯 THE PROBLEM

### What's Happening:
1. ✅ You built NEW frontend with correct pre-prod URL
2. ✅ New frontend is in: `C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist`
3. ❌ Backend is still serving OLD frontend from: `src/main/resources/static/`
4. ❌ Old frontend has hardcoded production URL in JavaScript

### Evidence:
```javascript
// OLD frontend (in backend WAR file):
const we="https://celebrations.tanishq.co.in/events";  // ❌ PRODUCTION URL

// NEW frontend (in dist folder):
const we="https://celebrationsite-preprod.tanishq.co.in/events";  // ✅ PRE-PROD URL
```

---

## ✅ THE SOLUTION (3 Steps)

### Step 1: Replace Old Frontend with New Frontend
### Step 2: Rebuild Backend WAR File
### Step 3: Redeploy to Server

---

## 📋 DETAILED STEPS

### STEP 1: Copy New Frontend to Backend Project

#### Option A: Manual Copy (Recommended)

```cmd
REM 1. Backup old frontend
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources
move static static_backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%

REM 2. Create new static folder
mkdir static

REM 3. Copy NEW frontend files
xcopy /E /I /Y C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist\* C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\static\
```

#### Option B: Using Script (See below)

**What this does:**
- Backs up old frontend (in case we need to rollback)
- Copies your NEW pre-prod frontend build into backend project
- Backend will now serve the correct frontend

---

### STEP 2: Rebuild Backend WAR File

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

REM Clean old builds
mvn clean

REM Build new WAR with updated frontend
mvn package -DskipTests

REM Verify build
dir target\tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 2-3 minutes

WAR file created: target\tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war
Size: ~50-80 MB (larger than before because it includes new frontend)
```

---

### STEP 3: Deploy to Server

#### A. Transfer WAR File
```cmd
scp target\tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war jewdev-test@10.160.128.94:/opt/tanishq/applications_preprod/
```

#### B. Deploy on Server
```bash
# SSH to server
ssh jewdev-test@10.160.128.94

# Go to deployment directory
cd /opt/tanishq/applications_preprod

# Backup old deployment
mkdir -p backups/backup_$(date +%Y%m%d_%H%M%S)
cp *.war backups/backup_$(date +%Y%m%d_%H%M%S)/ 2>/dev/null

# Stop application
sudo kill -15 $(ps aux | grep "[j]ava.*tanishq" | awk '{print $2}' | head -1)
sleep 10

# Start new application
nohup java -jar tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

# Wait for startup
sleep 30

# Verify
curl -I http://localhost:3000
tail -50 application.log
```

---

## 🧪 VERIFICATION STEPS

### 1. Check Frontend Files in WAR
```bash
# On server, extract and check
cd /opt/tanishq/applications_preprod
unzip -l tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war | grep "index.html"

# Check JavaScript for URL
unzip -p tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war WEB-INF/classes/static/assets/*.js | grep -o "https://[^\"]*tanishq.co.in/events" | head -1
```

**Expected Output:**
```
https://celebrationsite-preprod.tanishq.co.in/events  ✅ CORRECT
```

**NOT:**
```
https://celebrations.tanishq.co.in/events  ❌ WRONG (old build)
```

---

### 2. Test in Browser

1. **Open:** `http://celebrationsite-preprod.tanishq.co.in` (or `http://10.160.128.94:3000`)
2. **Login** with your credentials
3. **Click "Create Event" button**
4. **Verify URL stays:** `https://celebrationsite-preprod.tanishq.co.in/events` ✅

**If it redirects to production:**
- ❌ Old frontend still being served
- Need to check if new frontend was copied correctly
- May need to clear browser cache

---

### 3. Check Browser Developer Console

```
Press F12 → Console Tab

Look for API calls:
✅ CORRECT: https://celebrationsite-preprod.tanishq.co.in/events/login
❌ WRONG:   https://celebrations.tanishq.co.in/events/login
```

---

## 🤖 AUTOMATED SCRIPT

Save this as `deploy-new-frontend.bat`:

```batch
@echo off
echo ========================================
echo TANISHQ PREPROD - FRONTEND UPDATE
echo ========================================
echo.

set FRONTEND_DIST=C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist
set BACKEND_STATIC=C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\static
set BACKEND_ROOT=C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

echo [Step 1/4] Checking frontend build...
if not exist "%FRONTEND_DIST%\index.html" (
    echo ERROR: Frontend build not found!
    echo Please run: npm run build:preprod
    echo In folder: C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
    pause
    exit /b 1
)
echo ✓ Frontend build found
echo.

echo [Step 2/4] Backing up old frontend...
cd %BACKEND_ROOT%\src\main\resources
if exist static (
    ren static static_backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%
    echo ✓ Old frontend backed up
) else (
    echo ! No old frontend to backup
)
echo.

echo [Step 3/4] Copying new frontend...
mkdir static
xcopy /E /I /Y "%FRONTEND_DIST%\*" "%BACKEND_STATIC%\"
if %errorlevel% neq 0 (
    echo ERROR: Failed to copy frontend files
    pause
    exit /b 1
)
echo ✓ New frontend copied
echo.

echo [Step 4/4] Rebuilding WAR file...
cd %BACKEND_ROOT%
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)
echo ✓ WAR file built successfully
echo.

echo ========================================
echo SUCCESS! Next Steps:
echo ========================================
echo.
echo 1. Transfer WAR to server:
echo    scp target\tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war jewdev-test@10.160.128.94:/opt/tanishq/applications_preprod/
echo.
echo 2. SSH to server and restart application
echo.
echo WAR Location: %BACKEND_ROOT%\target\tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war
echo.
pause
```

**Usage:**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
deploy-new-frontend.bat
```

---

## 🔍 TROUBLESHOOTING

### Issue: Frontend files not copied

**Check:**
```cmd
dir C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\static
```

**Should see:**
```
index.html
assets\
  index-[hash].js
  index-[hash].css
  [images]
```

**Fix:**
```cmd
REM Manually copy again
xcopy /E /I /Y C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist\* C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\static\
```

---

### Issue: WAR file too small (< 50 MB)

**Problem:** Frontend not included in WAR

**Check pom.xml has:**
```xml
<build>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <includes>
                <include>**/*</include>
            </includes>
        </resource>
    </resources>
</build>
```

---

### Issue: Still redirecting to production after deployment

**Possible causes:**

1. **Browser cache:**
   ```
   Press Ctrl+Shift+Delete
   Clear cache and hard reload (Ctrl+F5)
   ```

2. **Old WAR deployed:**
   ```bash
   # On server, verify WAR timestamp
   ls -lh /opt/tanishq/applications_preprod/*.war
   
   # Should see recent timestamp
   ```

3. **Wrong environment build:**
   ```cmd
   REM Rebuild frontend with preprod config
   cd C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
   npm run build:preprod
   
   REM Then copy to backend again
   ```

---

## 📊 VERIFICATION CHECKLIST

### Before Deployment
- [ ] Frontend built with `npm run build:preprod`
- [ ] Frontend `dist` folder exists and has files
- [ ] Old backend static folder backed up
- [ ] New frontend copied to `src/main/resources/static/`
- [ ] WAR file rebuilt successfully
- [ ] WAR file size > 50 MB (includes frontend)

### After Deployment
- [ ] Application starts on port 3000
- [ ] Can access via browser
- [ ] Login works
- [ ] Dashboard loads
- [ ] Click "Create Event" stays on pre-prod URL ✅
- [ ] Browser console shows pre-prod API calls
- [ ] No production URLs in browser console

### Final Validation
- [ ] Create test event
- [ ] Download QR code
- [ ] QR code URL points to pre-prod
- [ ] Scan QR code (or open URL)
- [ ] Customer form loads correctly

---

## 🎯 SUMMARY

### The Issue:
Backend serving OLD frontend with hardcoded production URLs

### The Fix:
1. Copy NEW pre-prod frontend → backend static folder
2. Rebuild backend WAR (includes new frontend)
3. Deploy new WAR to server

### After Fix:
- ✅ "Create Event" button stays on pre-prod
- ✅ All URLs point to pre-prod
- ✅ No production redirection

### Timeline:
- Copy frontend: 2 minutes
- Rebuild WAR: 3 minutes
- Deploy to server: 5 minutes
- **Total: ~10 minutes**

---

## 🚀 QUICK START (Copy-Paste Commands)

```cmd
REM === ON YOUR WINDOWS MACHINE ===

REM 1. Backup and copy frontend
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources
move static static_backup_old
mkdir static
xcopy /E /I /Y C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist\* static\

REM 2. Rebuild WAR
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -DskipTests

REM 3. Transfer to server
scp target\tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war jewdev-test@10.160.128.94:/opt/tanishq/applications_preprod/
```

```bash
# === ON SERVER ===

cd /opt/tanishq/applications_preprod

# Backup
mkdir -p backups/backup_$(date +%Y%m%d_%H%M%S)
cp *.war backups/backup_$(date +%Y%m%d_%H%M%S)/

# Stop app
sudo kill -15 $(ps aux | grep "[j]ava.*tanishq" | awk '{print $2}' | head -1)
sleep 10

# Start new app
nohup java -jar tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

# Verify
sleep 30
curl -I http://localhost:3000
tail -50 application.log | grep -i "started\|error"
```

---

**Created:** December 8, 2025  
**Status:** Ready to execute  
**Estimated Time:** 10-15 minutes  
**Impact:** Fixes "Create Event" redirection issue ✅

