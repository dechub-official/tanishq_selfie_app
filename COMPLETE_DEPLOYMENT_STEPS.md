# 🚀 COMPLETE DEPLOYMENT GUIDE - PREPROD
**Date:** December 5, 2025  
**Current Port:** 3000 (Running)  
**Target:** Deploy new changes to preprod server

---

## ✅ CURRENT STATUS (VERIFIED)

```
Application Port:    3000 ✅
Database:           selfie_preprod on localhost ✅
Application PID:    263255
Status:             Running and responding
Domain:             http://celebrationsite-preprod.tanishq.co.in
```

---

## 📋 STEP-BY-STEP DEPLOYMENT PROCESS

### **STEP 1: BUILD THE PROJECT (On Windows - Your Local Machine)**

#### 1.1 Clean Previous Build
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean
```

#### 1.2 Build with Maven (Skip Tests for Speed)
```cmd
mvn clean package -DskipTests
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 2-3 minutes
[INFO] Final Memory: XX MB
```

#### 1.3 Verify WAR File Created
```cmd
dir target\*.war
```

**Expected File:**
```
tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
```

**Location:**
```
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
```

---

### **STEP 2: TRANSFER WAR FILE TO SERVER**

#### Option A: Using SCP (Recommended)
```cmd
scp target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war jewdev-test@10.160.128.94:/opt/tanishq/applications_preprod/
```

#### Option B: Using WinSCP (GUI Tool)
1. Open WinSCP
2. Connect to: `10.160.128.94`
3. Username: `jewdev-test`
4. Navigate to: `/opt/tanishq/applications_preprod/`
5. Upload: `tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war`

#### Option C: Using FileZilla
1. Host: `sftp://10.160.128.94`
2. Username: `jewdev-test`
3. Upload to: `/opt/tanishq/applications_preprod/`

---

### **STEP 3: BACKUP CURRENT APPLICATION (On Server)**

SSH to your server first:
```cmd
ssh jewdev-test@10.160.128.94
```

Then run:
```bash
# 3.1 Go to application directory
cd /opt/tanishq/applications_preprod

# 3.2 Create backup directory
mkdir -p backups/backup_$(date +%Y%m%d_%H%M%S)

# 3.3 Backup current WAR file
cp tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war backups/backup_$(date +%Y%m%d_%H%M%S)/

# 3.4 Backup current logs
cp application.log backups/backup_$(date +%Y%m%d_%H%M%S)/ 2>/dev/null || echo "No log to backup"

# 3.5 Verify backup
ls -lh backups/
```

---

### **STEP 4: STOP CURRENT APPLICATION (On Server)**

```bash
# 4.1 Find the process ID
ps aux | grep java | grep tanishq

# 4.2 Stop the application (using PID from above)
sudo kill -15 263255

# Wait 10 seconds
sleep 10

# 4.3 Verify it stopped
ps aux | grep java | grep tanishq

# If still running, force kill:
sudo kill -9 263255

# 4.4 Confirm port is free
sudo netstat -tlnp | grep 3000
```

**Expected:** No output (port is free)

---

### **STEP 5: REPLACE WAR FILE (On Server)**

```bash
# 5.1 Go to deployment directory
cd /opt/tanishq/applications_preprod

# 5.2 Check the new file exists and size
ls -lh tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war

# 5.3 Optional: Rename old file (if you didn't upload new one yet)
# mv tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war.old

# 5.4 Verify new file
ls -lh *.war
```

---

### **STEP 6: START NEW APPLICATION (On Server)**

```bash
# 6.1 Go to deployment directory
cd /opt/tanishq/applications_preprod

# 6.2 Clear old logs
rm -f application.log nohup.out

# 6.3 Start application with correct configuration
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

# 6.4 Note the PID
echo "Application started with PID: $!"

# 6.5 Wait for startup (30 seconds)
echo "Waiting for application to start..."
sleep 30
```

---

### **STEP 7: VERIFY DEPLOYMENT (On Server)**

```bash
# 7.1 Check if process is running
ps aux | grep java | grep tanishq

# Expected: You should see the java process

# 7.2 Check if port 3000 is listening
sudo netstat -tlnp | grep 3000

# Expected: tcp6  0  0 :::3000  :::*  LISTEN  <NEW_PID>/java

# 7.3 Check application logs for startup
tail -50 application.log

# Look for: "Tomcat started on port(s): 3000 (http)"

# 7.4 Check for errors
grep -i "error\|exception" application.log | tail -20

# 7.5 Test HTTP response
curl -I http://localhost:3000

# Expected: HTTP/1.1 200 OK (or 302 Found)
```

---

### **STEP 8: TEST DATABASE CONNECTION (On Server)**

```bash
# 8.1 Verify database connection from logs
grep -i "HikariPool\|datasource" application.log | tail -5

# Expected: HikariPool-1 - Starting...
#          HikariPool-1 - Start completed.

# 8.2 Check database connectivity
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM events;" 2>/dev/null

# 8.3 Test login API
TEST_USER=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N 2>/dev/null)
TEST_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N 2>/dev/null)

curl -X POST http://localhost:3000/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}"

# Expected: JSON response with "status":true
```

---

### **STEP 9: TEST DOMAIN ACCESS (From Your Browser)**

```bash
# 9.1 Wait for ELB health check (on server)
echo "Waiting for AWS ELB health check (60 seconds)..."
sleep 60

# 9.2 Test domain from server
curl -I http://celebrationsite-preprod.tanishq.co.in

# Expected: HTTP/1.1 301 (redirect to HTTPS)
# or HTTP/1.1 200 OK
```

**From Your Browser:**
1. Open: `http://celebrationsite-preprod.tanishq.co.in`
2. You should see the homepage
3. Try to login with test credentials
4. Verify your new changes are visible

---

### **STEP 10: MONITOR AND VERIFY (On Server)**

```bash
# 10.1 Monitor logs in real-time (Ctrl+C to stop)
tail -f application.log

# 10.2 Check for errors while testing
grep -i "error\|exception" application.log | tail -20

# 10.3 Monitor database queries
tail -100 application.log | grep -i "select\|update\|insert" | tail -10

# 10.4 Check application health
curl http://localhost:3000/actuator/health 2>/dev/null || echo "No actuator endpoint"
```

---

## 📊 COMPLETE ONE-COMMAND DEPLOYMENT SCRIPT

**Save this as:** `deploy_new_build.sh`

```bash
#!/bin/bash

echo "════════════════════════════════════════════════════════"
echo "PREPROD DEPLOYMENT - NEW BUILD"
echo "Date: $(date)"
echo "════════════════════════════════════════════════════════"
echo ""

# Configuration
APP_DIR="/opt/tanishq/applications_preprod"
WAR_FILE="tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war"
DB_USER="root"
DB_PASS="Dechub#2025"
DB_NAME="selfie_preprod"
APP_PORT=3000

cd $APP_DIR

# Step 1: Backup
echo "1️⃣  Creating backup..."
BACKUP_DIR="backups/backup_$(date +%Y%m%d_%H%M%S)"
mkdir -p $BACKUP_DIR
cp $WAR_FILE $BACKUP_DIR/ 2>/dev/null || echo "No old WAR to backup"
cp application.log $BACKUP_DIR/ 2>/dev/null || echo "No old log to backup"
echo "✅ Backup created: $BACKUP_DIR"
echo ""

# Step 2: Stop old application
echo "2️⃣  Stopping old application..."
OLD_PID=$(ps aux | grep "[j]ava.*tanishq" | awk '{print $2}' | head -1)
if [ -n "$OLD_PID" ]; then
    echo "Found process: $OLD_PID"
    sudo kill -15 $OLD_PID
    sleep 10
    
    # Force kill if still running
    if ps -p $OLD_PID > /dev/null 2>&1; then
        echo "Force killing..."
        sudo kill -9 $OLD_PID
        sleep 5
    fi
    echo "✅ Old application stopped"
else
    echo "✅ No running application found"
fi
echo ""

# Step 3: Clean logs
echo "3️⃣  Cleaning old logs..."
rm -f application.log nohup.out
echo "✅ Logs cleaned"
echo ""

# Step 4: Verify WAR file
echo "4️⃣  Verifying WAR file..."
if [ -f "$WAR_FILE" ]; then
    WAR_SIZE=$(ls -lh $WAR_FILE | awk '{print $5}')
    echo "✅ WAR file found: $WAR_FILE ($WAR_SIZE)"
else
    echo "❌ WAR file not found: $WAR_FILE"
    exit 1
fi
echo ""

# Step 5: Start new application
echo "5️⃣  Starting new application..."
nohup java -jar $WAR_FILE \
  --spring.datasource.url=jdbc:mysql://localhost:3306/$DB_NAME?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=$DB_USER \
  --spring.datasource.password=$DB_PASS \
  --server.port=$APP_PORT \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

NEW_PID=$!
echo "✅ Application started with PID: $NEW_PID"
echo ""

# Step 6: Wait for startup
echo "6️⃣  Waiting for application startup (30 seconds)..."
for i in {30..1}; do
    if [ $((i % 10)) -eq 0 ]; then
        echo "  $i seconds remaining..."
    fi
    sleep 1
done
echo ""

# Step 7: Verify deployment
echo "7️⃣  Verifying deployment..."
echo ""

# Check process
if ps aux | grep "[j]ava.*tanishq" > /dev/null; then
    RUNNING_PID=$(ps aux | grep "[j]ava.*tanishq" | awk '{print $2}' | head -1)
    echo "✅ Process running (PID: $RUNNING_PID)"
else
    echo "❌ Process not running!"
    echo "Check logs:"
    tail -50 application.log
    exit 1
fi

# Check port
if sudo netstat -tlnp 2>/dev/null | grep ":$APP_PORT" | grep -q "java"; then
    echo "✅ Port $APP_PORT is listening"
else
    echo "❌ Port $APP_PORT not listening!"
fi

# Check HTTP response
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$APP_PORT 2>/dev/null)
if [ "$HTTP_STATUS" = "200" ] || [ "$HTTP_STATUS" = "302" ]; then
    echo "✅ HTTP Response: $HTTP_STATUS"
else
    echo "⚠️  HTTP Response: $HTTP_STATUS"
fi

# Check database
DB_COUNT=$(mysql -u $DB_USER -p$DB_PASS $DB_NAME -e "SELECT COUNT(*) FROM events;" -s -N 2>/dev/null)
if [ -n "$DB_COUNT" ]; then
    echo "✅ Database connected: $DB_COUNT events"
else
    echo "⚠️  Database connection issue"
fi

echo ""

# Step 8: Show recent logs
echo "8️⃣  Recent startup logs:"
echo "────────────────────────────────────────────────────────"
tail -20 application.log | grep -i "started\|tomcat\|error" | tail -10
echo "────────────────────────────────────────────────────────"
echo ""

# Step 9: Summary
echo "════════════════════════════════════════════════════════"
echo "DEPLOYMENT SUMMARY"
echo "════════════════════════════════════════════════════════"
echo ""
echo "Application:   Running on port $APP_PORT"
echo "Database:      $DB_NAME ($DB_COUNT events)"
echo "Process ID:    $RUNNING_PID"
echo "HTTP Status:   $HTTP_STATUS"
echo ""
echo "Access URLs:"
echo "  Local:   http://localhost:$APP_PORT"
echo "  IP:      http://10.160.128.94:$APP_PORT"
echo "  Domain:  http://celebrationsite-preprod.tanishq.co.in"
echo ""
echo "Monitor logs:"
echo "  tail -f $APP_DIR/application.log"
echo ""
echo "════════════════════════════════════════════════════════"
echo "DEPLOYMENT COMPLETE!"
echo "════════════════════════════════════════════════════════"
```

---

## 🎯 QUICK DEPLOYMENT (3 COMMANDS)

### On Windows (Your Local Machine):
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -DskipTests
scp target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war jewdev-test@10.160.128.94:/opt/tanishq/applications_preprod/
```

### On Server:
```bash
# Option 1: Manual deployment
cd /opt/tanishq/applications_preprod
sudo kill -15 $(ps aux | grep "[j]ava.*tanishq" | awk '{print $2}' | head -1)
sleep 10
rm -f application.log
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &
sleep 30
tail -50 application.log

# Option 2: Use deployment script
bash deploy_new_build.sh
```

---

## ✅ POST-DEPLOYMENT CHECKLIST

- [ ] WAR file built successfully (mvn package)
- [ ] WAR file uploaded to server
- [ ] Old application stopped
- [ ] Backup created
- [ ] New application started
- [ ] Port 3000 is listening
- [ ] HTTP response is 200/302
- [ ] Database connection verified
- [ ] Domain accessible in browser
- [ ] Login works with test user
- [ ] New changes are visible
- [ ] No errors in logs

---

## 🆘 TROUBLESHOOTING

### Issue: Application won't start
```bash
# Check logs for errors
tail -100 application.log | grep -i "error\|exception"

# Check if port is already in use
sudo netstat -tlnp | grep 3000

# Check database connectivity
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT 1;"
```

### Issue: 502 Bad Gateway on domain
```bash
# Check if application is running
ps aux | grep java | grep tanishq

# Check if port 3000 is listening
sudo netstat -tlnp | grep 3000

# Check ELB health (wait 2 minutes for health check)
sleep 120
curl -I http://celebrationsite-preprod.tanishq.co.in
```

### Issue: Changes not visible
```bash
# Verify WAR file timestamp
ls -lh /opt/tanishq/applications_preprod/*.war

# Check if correct WAR is running
ps aux | grep java | grep tanishq

# Clear browser cache and try again
```

---

## 📝 IMPORTANT NOTES

1. **Port:** Application runs on port 3000 (not 3002)
2. **Database:** selfie_preprod on localhost
3. **Startup Time:** Wait at least 30 seconds after starting
4. **ELB Health Check:** Wait 60-120 seconds for domain to work
5. **Always backup** before deploying

---

**Created:** December 5, 2025  
**Port:** 3000  
**Database:** selfie_preprod  
**Domain:** http://celebrationsite-preprod.tanishq.co.in

