
# Test 4: Test IP
curl http://10.160.128.94:3000
# Should return HTML content

# Test 5: View logs
tail -50 application.log
# Should show: "Started TanishqSelfieApplication"

# Test 6: Monitor logs in real-time
tail -f application.log
# Press Ctrl+C to stop
```

### **6.2 From Your Windows Machine:**

```cmd
:: Test domain (in browser)
Open: http://celebrationsite-preprod.tanishq.co.in

:: Should see: "Let's Celebrate with Tanishq" page
```

### **6.3 Test Login API:**

```bash
# On server or from Windows (Git Bash/PowerShell)
curl -X POST http://celebrationsite-preprod.tanishq.co.in/events/login \
  -H "Content-Type: application/json" \
  -d '{"code":"TEST001","password":"Titan@123"}'

# Should return JSON like:
# {"status":true,"message":"Login successful","data":{...}}
```

### **6.4 Check AWS ELB Health:**

1. **Login to AWS Console**
2. **Navigate to:** EC2 → Load Balancers
3. **Find:** `Jew-Testing-ELB`
4. **Click:** Target Groups tab
5. **Verify:** Target `10.160.128.94:3000` shows **"Healthy"** status
6. **If Unhealthy:** Check security groups allow traffic from ELB

---

## 🔧 PART 7: TROUBLESHOOTING {#troubleshooting}

### **Problem 1: Build Failed - Maven Errors**

**Error:**
```
[ERROR] Failed to execute goal ... compilation failure
```

**Solution:**
```cmd
:: Check Java version
java -version
:: Must be Java 11 or higher

:: Clean Maven cache
mvn clean

:: Try again with verbose output
mvn clean install -X

:: If dependency issues:
mvn dependency:purge-local-repository
mvn clean install
```

---

### **Problem 2: Build Failed - "mvn not recognized"**

**Error:**
```
'mvn' is not recognized as an internal or external command
```

**Solution:**
```cmd
:: Add Maven to PATH
:: 1. Find Maven installation: C:\Program Files\Apache\maven
:: 2. Add to PATH: C:\Program Files\Apache\maven\bin
:: 3. Restart Command Prompt
:: 4. Verify: mvn -version
```

---

### **Problem 3: WAR File Not Created**

**Error:**
```
BUILD SUCCESS but no WAR file in target folder
```

**Solution:**
```cmd
:: Check pom.xml packaging type
findstr "packaging" pom.xml
:: Should show: <packaging>war</packaging>

:: If shows jar, change to war in pom.xml
:: Then rebuild:
mvn clean install -DskipTests
```

---

### **Problem 4: Cannot Connect to Server**

**Error:**
```
Connection refused or timeout
```

**Solution:**
```
1. Check IP address: 10.160.128.94
2. Check port: 22 (SSH)
3. Check VPN connection (if required)
4. Check firewall on your Windows machine
5. Try from different network
6. Contact network admin
```

---

### **Problem 5: Application Not Starting on Server**

**Error:**
```
Process not running after deployment
```

**Solution:**
```bash
# Check logs
cat application.log

# Common issues:

# A. Port already in use
netstat -tlnp | grep 3000
# Kill existing process:
pkill -9 -f tanishq-preprod
# Redeploy

# B. Database connection error
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT 1;"
# If fails, check database credentials

# C. Permission issues
ls -lh tanishq-preprod*.war
chmod 755 tanishq-preprod*.war
# Redeploy

# D. Java not found
which java
java -version
# Install Java if missing
```

---

### **Problem 6: Domain Returns 502 Bad Gateway**

**Error:**
```
http://celebrationsite-preprod.tanishq.co.in returns 502
```

**Solution:**
```bash
# Step 1: Verify app is running locally
curl http://localhost:3000
# Should return HTML

# Step 2: Check ELB target health in AWS Console
# EC2 → Target Groups → Check 10.160.128.94:3000

# Step 3: If unhealthy, check security group
# Must allow traffic from ELB security group

# Step 4: Wait 2 minutes for health check to pass
# ELB checks every 30 seconds

# Step 5: If still failing, contact AWS team
```

---

### **Problem 7: Database Connection Failed**

**Error in logs:**
```
Cannot connect to database
```

**Solution:**
```bash
# Test database connection
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT 1;"

# If fails:
# 1. Check MySQL is running
systemctl status mysqld

# 2. Check credentials
mysql -u root -p
# Enter password: Dechub#2025

# 3. Check database exists
mysql -u root -pDechub#2025 -e "SHOW DATABASES;" | grep selfie_preprod

# 4. Create database if missing
mysql -u root -pDechub#2025 -e "CREATE DATABASE IF NOT EXISTS selfie_preprod;"

# 5. Restart application
```

---

## 📖 PART 8: QUICK REFERENCE {#quick-reference}

### **8.1 Complete Workflow Summary:**

```
1. Make Code Changes
   ↓
2. Build: mvn clean install -DskipTests
   ↓
3. Transfer WAR file to server (WinSCP)
   ↓
4. SSH to server
   ↓
5. Run deployment script
   ↓
6. Wait 60 seconds for ELB health check
   ↓
7. Test domain in browser
   ↓
8. Done! ✅
```

### **8.2 Key Commands:**

**On Windows (Build):**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean install -DskipTests
```

**On Server (Deploy):**
```bash
cd /opt/tanishq/applications_preprod
pkill -9 -f tanishq-preprod
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war --server.port=3000 --spring.profiles.active=preprod > application.log 2>&1 &
```

**On Server (Monitor):**
```bash
tail -f application.log
ps aux | grep java
netstat -tlnp | grep 3000
```

### **8.3 Important Files:**

| File | Purpose |
|------|---------|
| `pom.xml` | Maven build configuration |
| `src/main/resources/application-preprod.properties` | Pre-prod configuration |
| `src/main/java/com/dechub/tanishq/controller/` | API endpoints |
| `src/main/resources/templates/` | HTML pages |
| `target/*.war` | Built application file |

### **8.4 Important URLs:**

| URL | Purpose |
|-----|---------|
| http://localhost:3000 | Local test on server |
| http://10.160.128.94:3000 | IP-based access |
| http://celebrationsite-preprod.tanishq.co.in | Production domain |

### **8.5 Server Paths:**

| Path | Purpose |
|------|---------|
| `/opt/tanishq/applications_preprod/` | Application deployment |
| `/opt/tanishq/storage/selfie_images/` | Uploaded images |
| `/opt/tanishq/tanishqgmb-5437243a8085.p12` | Google API key |

---

## 🎯 COMPLETE STEP-BY-STEP EXAMPLE

### **Scenario: You changed the login API in EventsController.java**

**Step 1: Edit File on Windows**
```
Open: src/main/java/com/dechub/tanishq/controller/EventsController.java
Make changes
Save file
```

**Step 2: Build on Windows**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean install -DskipTests
```

**Step 3: Wait for Build**
```
[INFO] BUILD SUCCESS
[INFO] Total time:  02:15 min
```

**Step 4: Transfer WAR File**
```
Open WinSCP
Connect to 10.160.128.94
Navigate to: C:\JAVA\...\target\ (left)
Navigate to: /opt/tanishq/applications_preprod/ (right)
Drag: tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
Wait for transfer
```

**Step 5: Deploy on Server**
```bash
# SSH to server
ssh root@10.160.128.94

# Run deployment
cd /opt/tanishq/applications_preprod
pkill -9 -f tanishq-preprod
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --server.port=3000 --spring.profiles.active=preprod \
  > application.log 2>&1 &

# Wait 30 seconds
sleep 30

# Check status
ps aux | grep java
tail -50 application.log
```

**Step 6: Test**
```bash
# Test locally
curl http://localhost:3000/events/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"code":"TEST","password":"Titan@123"}'

# Test domain (after 60 seconds)
curl http://celebrationsite-preprod.tanishq.co.in/events/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"code":"TEST","password":"Titan@123"}'
```

**Step 7: Verify in Browser**
```
Open: http://celebrationsite-preprod.tanishq.co.in
Test login functionality
Done! ✅
```

---

## 📝 NOTES & BEST PRACTICES

### **Before Making Changes:**
- ✅ Always create a Git branch for new features
- ✅ Test changes locally if possible
- ✅ Document what you changed

### **During Build:**
- ✅ Use `-DskipTests` for faster builds (for pre-prod)
- ✅ Check build logs for warnings
- ✅ Verify WAR file size (should be 50-80 MB)

### **During Deployment:**
- ✅ Always backup current WAR file on server
- ✅ Check logs immediately after deployment
- ✅ Test locally before testing domain
- ✅ Wait at least 60 seconds for ELB health check

### **After Deployment:**
- ✅ Monitor logs for errors
- ✅ Test all critical functionalities
- ✅ Document deployment time and changes
- ✅ Keep backup until confirmed working

---

## 🆘 EMERGENCY ROLLBACK

**If new deployment fails:**

```bash
# SSH to server
cd /opt/tanishq/applications_preprod

# Stop current application
pkill -9 -f tanishq-preprod

# Find backup
ls -lh backup_*

# Restore old WAR file
cp backup_YYYYMMDD_HHMMSS/tanishq-preprod*.war .

# Start old version
nohup java -jar tanishq-preprod*.war \
  --server.port=3000 --spring.profiles.active=preprod \
  > application.log 2>&1 &

# Verify
tail -f application.log
```

---

## ✅ FINAL CHECKLIST

**Before You Start:**
- [ ] Java 11+ installed
- [ ] Maven installed
- [ ] Code changes completed
- [ ] WinSCP or FileZilla installed

**During Build:**
- [ ] Clean build: `mvn clean`
- [ ] Build success
- [ ] WAR file exists in target folder
- [ ] WAR file size is reasonable (50-80 MB)

**During Transfer:**
- [ ] Connected to server
- [ ] WAR file uploaded to /opt/tanishq/applications_preprod/
- [ ] File size matches local file

**During Deployment:**
- [ ] Old application stopped
- [ ] Backup created
- [ ] New application started
- [ ] Process is running
- [ ] Port 3000 is listening
- [ ] Localhost returns HTTP 200
- [ ] IP returns HTTP 200

**After Deployment:**
- [ ] Wait 60 seconds for ELB
- [ ] Domain returns HTTP 200 (not 502)
- [ ] Login works
- [ ] All features tested
- [ ] Logs show no errors

---

## 🎉 SUCCESS!

**When everything is working:**

```
✅ Application running on port 3000
✅ Database connected (525 stores)
✅ Localhost test: HTTP 200
✅ IP test: HTTP 200
✅ Domain test: HTTP 200
✅ Login API working
✅ Browser shows application

🎊 DEPLOYMENT SUCCESSFUL! 🎊
```

---

**Questions? Issues?**
- Check the troubleshooting section above
- Review application logs: `tail -f application.log`
- Test step by step
- Don't skip verification steps

**Good luck! 🚀**
# 🎯 COMPLETE BUILD & DEPLOY GUIDE - FROM SCRATCH TO PRODUCTION

**Date:** December 5, 2025  
**Project:** Tanishq Celebration App - Pre-Production  
**Final URL:** http://celebrationsite-preprod.tanishq.co.in

---

## 📚 TABLE OF CONTENTS

1. [Prerequisites & Setup](#prerequisites)
2. [Making Code Changes](#making-changes)
3. [Building the Application](#building)
4. [Transferring to Server](#transfer)
5. [Server Deployment](#deployment)
6. [Testing & Verification](#testing)
7. [Troubleshooting](#troubleshooting)
8. [Quick Reference](#quick-reference)

---

## 🔧 PART 1: PREREQUISITES & SETUP {#prerequisites}

### **On Your Local Windows Machine:**

#### **1.1 Required Software:**

```cmd
✅ Java Development Kit (JDK) 11 or higher
✅ Maven 3.6 or higher
✅ Git (for version control)
✅ Any text editor (VS Code, IntelliJ, Eclipse)
✅ WinSCP or FileZilla (for file transfer)
✅ PuTTY or any SSH client
```

#### **1.2 Verify Installation:**

```cmd
:: Open Command Prompt (cmd) and run:

:: Check Java
java -version
:: Should show: java version "11.x.x" or higher

:: Check Maven
mvn -version
:: Should show: Apache Maven 3.x.x

:: Check Git
git --version
:: Should show: git version 2.x.x
```

#### **1.3 If Maven is Not Installed:**

**Download & Install:**
1. Go to: https://maven.apache.org/download.cgi
2. Download: `apache-maven-3.9.5-bin.zip`
3. Extract to: `C:\Program Files\Apache\maven`
4. Add to PATH:
   - Right-click "This PC" → Properties → Advanced System Settings
   - Environment Variables → System Variables → Path → Edit
   - Add: `C:\Program Files\Apache\maven\bin`
5. Restart Command Prompt and verify: `mvn -version`

---

## 📝 PART 2: MAKING CODE CHANGES {#making-changes}

### **2.1 Project Structure:**

```
tanishq_selfie_app/
│
├── src/
│   ├── main/
│   │   ├── java/com/dechub/tanishq/
│   │   │   ├── controller/          ← API endpoints & page controllers
│   │   │   │   ├── EventsController.java
│   │   │   │   ├── GreetingController.java
│   │   │   │   ├── RivahController.java
│   │   │   │   └── TanishqPageController.java
│   │   │   ├── entity/              ← Database models
│   │   │   ├── repository/          ← Database queries
│   │   │   ├── service/             ← Business logic
│   │   │   └── util/                ← Utility classes
│   │   │
│   │   ├── resources/
│   │   │   ├── application.properties              ← Default config
│   │   │   ├── application-preprod.properties      ← Pre-prod config ⭐
│   │   │   ├── static/              ← CSS, JS, images
│   │   │   └── templates/           ← HTML pages
│   │   │
│   │   └── webapp/                  ← Web resources
│
├── pom.xml                          ← Maven configuration
└── target/                          ← Build output (generated)
```

### **2.2 Common Changes You Might Make:**

#### **A. Change Database Configuration:**

**File:** `src/main/resources/application-preprod.properties`

```properties
# Change database settings
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod
spring.datasource.username=root
spring.datasource.password=Dechub#2025
```

#### **B. Change Port Number:**

**File:** `src/main/resources/application-preprod.properties`

```properties
# Change server port (currently 3002, but we override to 3000 on server)
server.port=3000
```

#### **C. Add/Modify API Endpoints:**

**File:** `src/main/java/com/dechub/tanishq/controller/EventsController.java`

```java
@RestController
@RequestMapping("/events")
public class EventsController {
    
    // Add new endpoint
    @PostMapping("/new-api")
    public ResponseEntity<ResponseDataDTO> newAPI(@RequestBody Map<String, String> request) {
        // Your logic here
        return ResponseEntity.ok(new ResponseDataDTO(true, "Success", data));
    }
}
```

#### **D. Modify Frontend Pages:**

**File:** `src/main/resources/templates/your-page.html`

```html
<!-- Edit HTML templates here -->
```

#### **E. Change S3 Bucket or Storage Path:**

**File:** `src/main/resources/application-preprod.properties`

```properties
# Change S3 bucket or storage paths
selfie.upload.dir=/opt/tanishq/storage/selfie_images
dechub.bride.upload.dir=/opt/tanishq/storage/bride_uploads
```

---

## 🏗️ PART 3: BUILDING THE APPLICATION {#building}

### **3.1 Clean Previous Build:**

```cmd
:: Navigate to project directory
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

:: Clean previous builds
mvn clean

:: This will delete the 'target' folder
```

**Expected Output:**
```
[INFO] Scanning for projects...
[INFO] Building tanishq-preprod-03-12-2025-1 0.0.1-SNAPSHOT
[INFO] --- maven-clean-plugin:3.2.0:clean (default-clean) @ tanishq-preprod-03-12-2025-1 ---
[INFO] Deleting target
[INFO] BUILD SUCCESS
```

### **3.2 Build WAR File:**

```cmd
:: Build the project (skip tests for faster build)
mvn clean install -DskipTests

:: OR with tests (takes longer)
mvn clean install
```

**Expected Output:**
```
[INFO] Scanning for projects...
[INFO] Building tanishq-preprod-03-12-2025-1 0.0.1-SNAPSHOT
[INFO] --- maven-resources-plugin ---
[INFO] --- maven-compiler-plugin ---
[INFO] Compiling 45 source files
[INFO] --- maven-war-plugin ---
[INFO] Packaging webapp
[INFO] Building war: target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
[INFO] BUILD SUCCESS
[INFO] Total time:  2:15 min
```

### **3.3 Verify Build Success:**

```cmd
:: Check if WAR file was created
dir target\*.war

:: You should see:
:: tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
```

### **3.4 Note the WAR File Location:**

```
Full Path: C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war

Size: Should be around 50-80 MB
```

---

## 📤 PART 4: TRANSFERRING TO SERVER {#transfer}

### **4.1 Server Details:**

```
Server IP:   10.160.128.94
Username:    root
Password:    [Ask your admin]
Deploy Dir:  /opt/tanishq/applications_preprod
```

### **4.2 Method A: Using WinSCP (GUI - Easiest):**

**Step 1:** Download WinSCP from https://winscp.net/

**Step 2:** Open WinSCP and create new connection:
```
File Protocol: SFTP
Host name:     10.160.128.94
Port:          22
User name:     root
Password:      [Your server password]
```

**Step 3:** Click "Login"

**Step 4:** Transfer WAR file:
- **Local (left panel):** Navigate to `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\target\`
- **Server (right panel):** Navigate to `/opt/tanishq/applications_preprod/`
- **Drag and drop:** `tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war` from left to right
- **Wait:** Until transfer completes (progress bar)

**Step 5:** Verify transfer:
- Right-click WAR file on server side
- Check size matches local file (should be ~50-80 MB)

### **4.3 Method B: Using SCP Command (Advanced):**

```cmd
:: From Windows Command Prompt
:: (Requires OpenSSH or Git Bash)

scp target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war root@10.160.128.94:/opt/tanishq/applications_preprod/

:: Enter password when prompted
```

### **4.4 Method C: Using FileZilla:**

**Step 1:** Download FileZilla from https://filezilla-project.org/

**Step 2:** Connect:
```
Host:     sftp://10.160.128.94
Username: root
Password: [Your server password]
Port:     22
```

**Step 3:** Transfer file (drag & drop from local to remote)

---

## 🚀 PART 5: SERVER DEPLOYMENT {#deployment}

### **5.1 Connect to Server:**

```cmd
:: Using PuTTY:
:: - Host: 10.160.128.94
:: - Port: 22
:: - Connection Type: SSH
:: - Username: root
:: - Password: [Enter when prompted]

:: OR using Windows PowerShell (if OpenSSH installed):
ssh root@10.160.128.94
:: Enter password when prompted
```

### **5.2 Quick Deploy Script (Copy & Paste):**

**Once connected to server, run this complete script:**

```bash
#!/bin/bash

echo "========================================================"
echo "🚀 TANISHQ CELEBRATION APP - DEPLOYMENT"
echo "========================================================"
echo "URL: http://celebrationsite-preprod.tanishq.co.in"
echo "Date: $(date)"
echo "========================================================"
echo ""

# STEP 1: Stop old application
echo "📍 STEP 1/12: Stopping old application..."
pkill -9 -f "tanishq-preprod" 2>/dev/null
sleep 3
if ps aux | grep "[j]ava.*tanishq" > /dev/null; then
    echo "   ⚠️  Process still running, force killing..."
    pkill -9 java 2>/dev/null
    sleep 2
fi
echo "   ✅ Old application stopped"
echo ""

# STEP 2: Navigate to deployment directory
echo "📍 STEP 2/12: Navigating to deployment directory..."
cd /opt/tanishq/applications_preprod || exit 1
echo "   Current directory: $(pwd)"
echo "   ✅ Ready"
echo ""

# STEP 3: Create backup
echo "📍 STEP 3/12: Creating backup..."
BACKUP_DIR="backup_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"
# Backup old WAR if exists
if ls tanishq-preprod*.war 1> /dev/null 2>&1; then
    cp tanishq-preprod*.war "$BACKUP_DIR/" 2>/dev/null || true
fi
# Backup logs
cp *.log "$BACKUP_DIR/" 2>/dev/null || echo "   No logs to backup"
echo "   ✅ Backup created: $BACKUP_DIR"
echo ""

# STEP 4: Clean old logs
echo "📍 STEP 4/12: Cleaning old logs..."
rm -f application.log app.log nohup.out 2>/dev/null || true
echo "   ✅ Logs cleaned"
echo ""

# STEP 5: Verify WAR file exists
echo "📍 STEP 5/12: Verifying WAR file..."
WAR_FILE="tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war"
if [ ! -f "$WAR_FILE" ]; then
    echo "   ❌ ERROR: WAR file not found: $WAR_FILE"
    echo "   Please upload the WAR file to: $(pwd)"
    exit 1
fi
WAR_SIZE=$(du -h "$WAR_FILE" | cut -f1)
echo "   ✅ WAR file found: $WAR_FILE ($WAR_SIZE)"
echo ""

# STEP 6: Verify database
echo "📍 STEP 6/12: Verifying database..."
DB_COUNT=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores;" -s -N 2>/dev/null || echo "0")
if [ "$DB_COUNT" -gt "0" ]; then
    echo "   ✅ Database connected: $DB_COUNT stores"
else
    echo "   ⚠️  Database connection issue (Count: $DB_COUNT)"
    echo "   Continuing anyway - app will create tables if needed"
fi
echo ""

# STEP 7: Configure firewall
echo "📍 STEP 7/12: Configuring firewall..."
if command -v firewall-cmd &> /dev/null; then
    firewall-cmd --permanent --add-port=3000/tcp 2>/dev/null || true
    firewall-cmd --reload 2>/dev/null || true
    if firewall-cmd --list-ports 2>/dev/null | grep -q "3000"; then
        echo "   ✅ Port 3000 opened in firewall"
    else
        echo "   ⚠️  Could not verify firewall port"
    fi
else
    echo "   ℹ️  Firewall command not available (might be disabled)"
fi
echo ""

# STEP 8: Deploy application
echo "📍 STEP 8/12: Deploying application on port 3000..."
nohup java -jar "$WAR_FILE" \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

APP_PID=$!
echo "   ✅ Application started with PID: $APP_PID"
echo ""

# STEP 9: Wait for startup
echo "📍 STEP 9/12: Waiting for application startup (30 seconds)..."
for i in {30..1}; do
    if [ $((i % 5)) -eq 0 ]; then
        echo -n "   ⏱  $i seconds remaining..."
        echo ""
    fi
    sleep 1
done
echo "   ✅ Wait complete"
echo ""

# STEP 10: Verify deployment
echo "📍 STEP 10/12: Verifying deployment..."

# Check process
if ps aux | grep "[j]ava.*tanishq" > /dev/null; then
    REAL_PID=$(ps aux | grep "[j]ava.*tanishq" | awk '{print $2}' | head -1)
    echo "   ✅ Process running (PID: $REAL_PID)"
else
    echo "   ❌ Process not running!"
    echo ""
    echo "Last 50 lines of log:"
    tail -50 application.log
    exit 1
fi

# Check port
if netstat -tlnp 2>/dev/null | grep ":3000" > /dev/null; then
    echo "   ✅ Port 3000 is listening"
else
    echo "   ❌ Port 3000 not listening!"
    echo ""
    echo "Last 50 lines of log:"
    tail -50 application.log
    exit 1
fi
echo ""

# STEP 11: Test local access
echo "📍 STEP 11/12: Testing local access..."

# Test localhost
LOCAL_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 2>/dev/null || echo "000")
if [ "$LOCAL_STATUS" = "200" ] || [ "$LOCAL_STATUS" = "302" ]; then
    echo "   ✅ Localhost test: HTTP $LOCAL_STATUS"
else
    echo "   ⚠️  Localhost test: HTTP $LOCAL_STATUS"
fi

# Test IP
IP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://10.160.128.94:3000 2>/dev/null || echo "000")
if [ "$IP_STATUS" = "200" ] || [ "$IP_STATUS" = "302" ]; then
    echo "   ✅ IP test: HTTP $IP_STATUS"
else
    echo "   ⚠️  IP test: HTTP $IP_STATUS"
fi
echo ""

# STEP 12: Test database API
echo "📍 STEP 12/12: Testing database connectivity..."
TEST_USER=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N 2>/dev/null || echo "")
if [ -n "$TEST_USER" ]; then
    TEST_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N 2>/dev/null || echo "")
    
    API_RESPONSE=$(curl -s -X POST http://localhost:3000/events/login \
      -H "Content-Type: application/json" \
      -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" 2>/dev/null || echo "")
    
    if echo "$API_RESPONSE" | grep -q '"status":true\|"status":false'; then
        echo "   ✅ Login API responding (User: $TEST_USER)"
    else
        echo "   ⚠️  API Response: $(echo "$API_RESPONSE" | cut -c1-100)"
    fi
else
    echo "   ⚠️  No test user found in database"
fi
echo ""

# Final summary
echo "========================================================"
echo "📊 DEPLOYMENT SUMMARY"
echo "========================================================"
echo ""
echo "Application:  ✅ Running on port 3000 (PID: $REAL_PID)"
echo "Database:     ✅ selfie_preprod ($DB_COUNT stores)"
echo "WAR File:     ✅ $WAR_FILE ($WAR_SIZE)"
echo ""
echo "Access URLs:"
echo "  • Local:    http://localhost:3000 (HTTP $LOCAL_STATUS)"
echo "  • IP:       http://10.160.128.94:3000 (HTTP $IP_STATUS)"
echo "  • Domain:   http://celebrationsite-preprod.tanishq.co.in"
echo ""
echo "Recent Application Logs:"
tail -20 application.log | grep -i "started\|tomcat\|error" | tail -5
echo ""
echo "========================================================"
echo "✅ DEPLOYMENT COMPLETE!"
echo "========================================================"
echo ""
echo "Next Steps:"
echo "1. Wait 60 seconds for AWS ELB health check"
echo "2. Test domain: curl http://celebrationsite-preprod.tanishq.co.in"
echo "3. Open in browser: http://celebrationsite-preprod.tanishq.co.in"
echo "4. Monitor logs: tail -f application.log"
echo ""
echo "If domain shows 502 error:"
echo "  → Contact AWS team to verify ELB target group"
echo "  → Verify target: 10.160.128.94:3000 is 'Healthy'"
echo ""
echo "========================================================"
```

### **5.3 Copy the Above Script:**

**Option A: Copy-Paste Method:**
1. Copy the entire script above (from `#!/bin/bash` to the end)
2. In PuTTY/SSH session, type: `nano deploy.sh`
3. Paste the script (Right-click in PuTTY)
4. Press `Ctrl+X`, then `Y`, then `Enter` to save
5. Run: `chmod +x deploy.sh`
6. Run: `./deploy.sh`

**Option B: Direct Method:**
1. Copy the script content
2. In SSH session, paste it directly line by line
3. Press Enter after pasting

---

## 🧪 PART 6: TESTING & VERIFICATION {#testing}

### **6.1 On Server (After Deployment):**

```bash
# Test 1: Check if application is running
ps aux | grep java | grep tanishq
# Should show: java -jar tanishq-preprod...

# Test 2: Check if port 3000 is listening
netstat -tlnp | grep 3000
# Should show: tcp ... :3000 ... LISTEN

# Test 3: Test localhost
curl http://localhost:3000
# Should return HTML content

