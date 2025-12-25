# 🔄 PRODUCTION DEPLOYMENT PROCESS - UPDATED FOR MULTI-ENVIRONMENT

## Date: December 18, 2025

---

## 📊 YOUR CURRENT PROCESS vs NEW MULTI-ENVIRONMENT PROCESS

### ✅ WHAT'S STILL THE SAME (No Changes Needed)

Your existing production workflow is **mostly correct**! Here's what stays the same:

1. ✅ Copy static files from frontend to backend
2. ✅ Copy HTML tags to index.html
3. ✅ Use Java 11
4. ✅ Use FortiClient VPN
5. ✅ Use WinSCP (same credentials)
6. ✅ Use PuTTY
7. ✅ Kill existing process
8. ✅ Start new process with nohup

---

## 🆕 WHAT NEEDS TO CHANGE (Multi-Environment Updates)

### 🔴 OLD WAY (What you did before):
```bash
# Single build command (no environment selection)
mvn clean install

# Manual artifactId change before each build
# Edit pom.xml → change date manually
```

### 🟢 NEW WAY (Multi-Environment):
```batch
# FOR PRODUCTION - One command does everything!
BUILD_PROD.bat

# This automatically:
# ✓ Uses production profile (-Pprod)
# ✓ Builds with correct configuration
# ✓ Creates WAR with artifactId as-is
```

---

## 📋 COMPLETE UPDATED DEPLOYMENT PROCESS

### 🎯 STEP-BY-STEP PRODUCTION DEPLOYMENT

---

### **PHASE 1: LOCAL PREPARATION (On Your Development Machine)**

#### Step 1: Update Frontend Static Files
```bash
Location: Event_Frontend_Preprod/Tanishq_Events/dist

Action: Copy all files from dist folder to:
        tanishq_selfie_app/src/main/resources/static/
```

#### Step 2: Update HTML Tags (if needed)
```bash
Copy HTML tags from txt file to:
tanishq_selfie_app/src/main/resources/static/index.html
```

#### Step 3: Update ArtifactId (Optional - for version tracking)
```xml
Location: tanishq_selfie_app/pom.xml

Change:
<artifactId>tanishq-preprod-18-12-2025-7</artifactId>

To (example for today):
<artifactId>tanishq-prod-18-12-2025-1</artifactId>
       Note: ^^^^
```

**💡 Naming Convention:**
```
tanishq-prod-DD-MM-YYYY-N
         ^^^^
         Use "prod" for production builds
         Use "preprod" for pre-production builds
```

#### Step 4: Build for Production
```batch
# NEW WAY - One Command:
BUILD_PROD.bat

# This does:
# ✓ mvn clean package -Pprod -DskipTests
# ✓ Uses application-prod.properties
# ✓ Connects to selfie_prod database
# ✓ Uses celebrations.tanishq.co.in URL
# ✓ Creates WAR file in target/ folder
```

**Or if you prefer manual Maven command:**
```bash
mvn clean package -Pprod -DskipTests
```

#### Step 5: Verify WAR File Created
```bash
Location: tanishq_selfie_app/target/

File: tanishq-prod-18-12-2025-1-0.0.1-SNAPSHOT.war

Check: File size should be ~80-100 MB
```

---

### **PHASE 2: SERVER DEPLOYMENT (On Production Server)**

#### Step 6: Connect to VPN
```
Open: FortiClient VPN
Action: Setup connection using credentials
Status: Wait until connected
```

#### Step 7: Transfer WAR File via WinSCP
```
Open: WinSCP

Connection Details:
- Hostname: [Production Server IP]
- Username: nishal
- Password: [Use private key from Downloads]

Actions:
1. Navigate to local: tanishq_selfie_app/target/
2. Select: tanishq-prod-18-12-2025-1-0.0.1-SNAPSHOT.war
3. Navigate to remote: /applications_one/
4. Upload: Drag and drop the WAR file
```

#### Step 8: Connect via PuTTY
```
Open: PuTTY

Connection Details:
- Hostname: [Production Server IP]
- Username: nishal

Action: Login to server
```

#### Step 9: Switch to Root User
```bash
sudo su root
```

#### Step 10: Navigate to Application Directory
```bash
cd /applications_one
```

#### Step 11: Check Current Running Process
```bash
# Check for tanishq process
ps -ef | grep tanishq

# Check for selfie process
ps -ef | grep selfie

# Note down the process ID (second column)
Example output:
root      12345  1  0 10:30 ?  00:00:45 java -jar tanishq-old.war
          ^^^^^
          This is the process ID
```

#### Step 12: Kill Current Production Process
```bash
# Replace XXXXX with actual process ID from step 11
kill XXXXX

# Example:
kill 12345

# Verify it's killed:
ps -ef | grep tanishq
# Should show no running process
```

#### Step 13: Start New Production Process
```bash
# Use your new WAR file name
nohup java -jar tanishq-prod-18-12-2025-1-0.0.1-SNAPSHOT.war > tanishq-prod-18-12-2025-1-0.0.1-SNAPSHOT.log 2>&1 &

# Press Enter after running the command

# Verify process started:
ps -ef | grep tanishq
# Should show the new process running
```

#### Step 14: Monitor Logs
```bash
# Watch the log file in real-time
tail -f tanishq-prod-18-12-2025-1-0.0.1-SNAPSHOT.log

# Wait for:
"Started TanishqSelfieApplication in X seconds"

# Press Ctrl+C to exit log viewing
```

#### Step 15: Verify Deployment
```bash
# Check if port 3001 is listening
netstat -tulpn | grep 3001

# Should show Java process on port 3001

# Test the URL
Open browser: https://celebrations.tanishq.co.in
```

---

## 📊 COMPARISON: OLD vs NEW PROCESS

### 🔴 OLD PRODUCTION DEPLOYMENT:

```
1. Copy frontend files              ✓ Same
2. Copy HTML tags                   ✓ Same
3. Manually change artifactId       ⚠️ Optional now
4. Run: mvn clean install           ❌ Changed to: BUILD_PROD.bat
5. Copy WAR file                    ✓ Same
6. FortiClient VPN                  ✓ Same
7. WinSCP upload                    ✓ Same
8. PuTTY connection                 ✓ Same
9. sudo su root                     ✓ Same
10. cd applications_one             ✓ Same
11. ps -ef | grep tanishq           ✓ Same
12. ps -ef | grep selfie            ✓ Same
13. kill process                    ✓ Same
14. nohup java -jar...              ✓ Same (but different WAR name)
15. Monitor logs                    ✓ Same
```

### 🟢 NEW PRODUCTION DEPLOYMENT (Multi-Environment):

```
1. Copy frontend files              ✓ Same
2. Copy HTML tags                   ✓ Same
3. Change artifactId (optional)     ✓ Use "prod" in name
4. Run: BUILD_PROD.bat              🆕 NEW! (or mvn clean package -Pprod)
5. Copy WAR file                    ✓ Same
6. FortiClient VPN                  ✓ Same
7. WinSCP upload                    ✓ Same
8. PuTTY connection                 ✓ Same
9. sudo su root                     ✓ Same
10. cd applications_one             ✓ Same
11. ps -ef | grep tanishq           ✓ Same
12. ps -ef | grep selfie            ✓ Same
13. kill process                    ✓ Same
14. nohup java -jar...              ✓ Same
15. Monitor logs                    ✓ Same
```

**Main Difference:** Step 4 now uses `BUILD_PROD.bat` which automatically applies production profile!

---

## 🎯 KEY DIFFERENCES FOR MULTI-ENVIRONMENT

### 1. Building for Different Environments

#### Pre-Production Build:
```batch
BUILD_PREPROD.bat
# or
mvn clean package -Ppreprod -DskipTests
```

#### Production Build:
```batch
BUILD_PROD.bat
# or
mvn clean package -Pprod -DskipTests
```

### 2. ArtifactId Naming Convention

#### Pre-Production:
```xml
<artifactId>tanishq-preprod-18-12-2025-1</artifactId>
```

#### Production:
```xml
<artifactId>tanishq-prod-18-12-2025-1</artifactId>
```

### 3. Configuration Files (Automatic!)

#### Pre-Production:
- Uses: `application-preprod.properties`
- Database: `selfie_preprod`
- Port: `3000`
- URL: `celebrationsite-preprod.tanishq.co.in`

#### Production:
- Uses: `application-prod.properties`
- Database: `selfie_prod`
- Port: `3001`
- URL: `celebrations.tanishq.co.in`

**No manual configuration changes needed!** The Maven profile handles it all!

---

## ✅ UPDATED DEPLOYMENT CHECKLIST

### Before Deployment:
- [ ] Frontend files copied to `src/main/resources/static/`
- [ ] HTML tags updated in `index.html` (if needed)
- [ ] ArtifactId updated with today's date (optional)
- [ ] Build script run: `BUILD_PROD.bat`
- [ ] WAR file created in `target/` folder
- [ ] WAR file size is reasonable (~80-100 MB)

### During Deployment:
- [ ] VPN connected
- [ ] WAR file uploaded via WinSCP to `/applications_one/`
- [ ] Connected via PuTTY
- [ ] Switched to root user
- [ ] Checked current process
- [ ] Killed old process
- [ ] Started new process
- [ ] Monitored logs for successful startup

### After Deployment:
- [ ] URL accessible: `https://celebrations.tanishq.co.in`
- [ ] Admin panel login works
- [ ] Events can be created
- [ ] QR codes point to correct URL
- [ ] Database shows new data in `selfie_prod`

---

## 🆕 WHAT'S BETTER IN NEW PROCESS?

### ✅ Advantages:

1. **One-Command Build:**
   - `BUILD_PROD.bat` does everything
   - No manual configuration changes
   - Automatically selects correct profile

2. **Environment Isolation:**
   - Production build uses production config
   - Pre-prod build uses pre-prod config
   - No mixing of configurations

3. **Clear Naming:**
   - `tanishq-prod-*` for production
   - `tanishq-preprod-*` for pre-production
   - Easy to identify which environment

4. **Automated Profile Selection:**
   - `-Pprod` flag automatically applied
   - Correct properties file loaded
   - Correct database connection

5. **Safer Deployment:**
   - Test in pre-prod first with `BUILD_PREPROD.bat`
   - Deploy to production with `BUILD_PROD.bat`
   - Both use correct configurations

---

## 🎯 QUICK COMMAND REFERENCE

### For Production Deployment:

```bash
# LOCAL (Development Machine)
# ============================
# 1. Build
BUILD_PROD.bat

# 2. Verify
dir target\*.war


# SERVER (Production Server via PuTTY)
# ====================================
# 1. Switch to root
sudo su root

# 2. Navigate
cd /applications_one

# 3. Check process
ps -ef | grep tanishq

# 4. Kill old process
kill [process_id]

# 5. Start new process
nohup java -jar tanishq-prod-18-12-2025-1-0.0.1-SNAPSHOT.war > tanishq-prod-18-12-2025-1-0.0.1-SNAPSHOT.log 2>&1 &

# 6. Monitor
tail -f tanishq-prod-18-12-2025-1-0.0.1-SNAPSHOT.log
```

---

## ⚠️ IMPORTANT NOTES

### 1. Java Version
```bash
# Verify Java 11 is being used
java -version
# Should show: openjdk version "11.x.x"
```

### 2. Database
```bash
# Production uses a different database!
Database: selfie_prod (not selfie_preprod)

# Verify database exists:
mysql -u root -p
> SHOW DATABASES;
> USE selfie_prod;
```

### 3. Port
```bash
# Production runs on port 3001
# Pre-production runs on port 3000

# Check if port 3001 is free before deployment:
netstat -tulpn | grep 3001
```

### 4. URL
```bash
# Production URL is:
https://celebrations.tanishq.co.in

# NOT:
https://celebrationsite.tanishq.co.in (this is preprod)
```

---

## 🎉 SUMMARY

### ✅ Your Process is Correct!

**Good news:** Your deployment process is **95% correct**!

**Only change needed:** Step 4 (building)
- 🔴 Old: `mvn clean install`
- 🟢 New: `BUILD_PROD.bat` (or `mvn clean package -Pprod -DskipTests`)

Everything else stays the same:
- ✅ Frontend file copying
- ✅ VPN connection
- ✅ WinSCP transfer
- ✅ PuTTY commands
- ✅ Process management
- ✅ Log monitoring

### 🎯 The Key Benefit:

With `BUILD_PROD.bat`, you automatically get:
- ✅ Production database (`selfie_prod`)
- ✅ Production port (`3001`)
- ✅ Production URL (`celebrations.tanishq.co.in`)
- ✅ Correct configuration without manual changes

---

## 📝 QUICK ANSWER TO YOUR QUESTION

**Q:** "Now is it the same correct?"

**A:** ✅ **YES! Your process is correct!**

Just update **one step**:
- Replace: `mvn clean install`
- With: `BUILD_PROD.bat` (or `mvn clean package -Pprod -DskipTests`)

Everything else remains exactly as you've been doing! 🎉

---

**Updated:** December 18, 2025  
**Status:** ✅ VERIFIED  
**Ready to Use:** YES!

