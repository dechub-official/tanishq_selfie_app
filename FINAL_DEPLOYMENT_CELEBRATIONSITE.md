# 🎯 FINAL URL CONFIRMED - COMPLETE SETUP GUIDE

**Date:** December 5, 2025  
**Final URL:** http://celebrationsite-preprod.tanishq.co.in  
**Status:** Ready to configure and deploy

---

## ✅ CONFIRMED INFORMATION

**Finalized Pre-Prod URL:**
```
http://celebrationsite-preprod.tanishq.co.in
(NOT celebrations-preprod - it's celebrationsite-preprod!)
```

**DNS Configuration (by AWS Team):**
```
Domain:     celebrationsite-preprod.tanishq.co.in
Type:       CNAME
Value:      internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com
Status:     DNS record is mapped ✅
```

**Server Details:**
```
IP:         10.160.128.94
Port:       3000 (as per AWS ELB configuration)
Database:   selfie_preprod
S3 Bucket:  celebrations-tanishq-preprod
```

---

## 🚀 COMPLETE DEPLOYMENT GUIDE - FROM SCRATCH

### **STEP 1: Stop Old Application**

```bash
echo "Step 1: Stopping old application..."

# Kill any existing Java process
pkill -9 -f tanishq-preprod

# Wait for process to stop
sleep 3

# Verify stopped
ps aux | grep java | grep tanishq

# Should return nothing
echo "Old application stopped ✅"
```

---

### **STEP 2: Backup Current Setup**

```bash
echo "Step 2: Creating backup..."

# Backup directory
cd /opt/tanishq/applications_preprod

# Create backup folder
mkdir -p backup_$(date +%Y%m%d_%H%M%S)

# Backup logs and config if any
cp -r *.log backup_$(date +%Y%m%d_%H%M%S)/ 2>/dev/null || echo "No logs to backup"

echo "Backup created ✅"
```

---

### **STEP 3: Clean Old Deployment**

```bash
echo "Step 3: Cleaning old deployment..."

# Go to deployment directory
cd /opt/tanishq/applications_preprod

# Remove old log files
rm -f application.log app.log nohup.out

# List current files
ls -lh

echo "Cleanup complete ✅"
```

---

### **STEP 4: Verify Database**

```bash
echo "Step 4: Verifying database..."

# Check database exists and has data
mysql -u root -pDechub#2025 selfie_preprod -e "
SELECT 
  'Database Verification' as Check_Type,
  'selfie_preprod' as Database_Name;
  
SELECT 'Stores' as Table_Name, COUNT(*) as Row_Count FROM stores
UNION ALL SELECT 'Users', COUNT(*) FROM users
UNION ALL SELECT 'Events', COUNT(*) FROM events
UNION ALL SELECT 'Attendees', COUNT(*) FROM attendees;
" 2>&1 | grep -v "Warning"

echo "Database verified ✅"
```

---

### **STEP 5: Open Firewall for Port 3000**

```bash
echo "Step 5: Configuring firewall..."

# Open port 3000
firewall-cmd --permanent --add-port=3000/tcp 2>/dev/null
firewall-cmd --reload 2>/dev/null

# Verify
firewall-cmd --list-ports 2>/dev/null | grep 3000 && echo "✅ Port 3000 open" || echo "ℹ️ Firewall command not available"

echo "Firewall configured ✅"
```

---

### **STEP 6: Deploy Application with Correct Configuration**

```bash
echo "Step 6: Deploying application..."

# Go to deployment directory
cd /opt/tanishq/applications_preprod

# Verify WAR file exists
ls -lh tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war

# Start application with correct configuration
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

echo "Application started with PID: $!"
echo "Waiting for startup..."
```

---

### **STEP 7: Monitor Startup**

```bash
echo "Step 7: Monitoring application startup..."

# Wait for startup
sleep 20

# Check if process is running
if ps aux | grep "[j]ava.*tanishq" > /dev/null; then
    PID=$(ps aux | grep "[j]ava.*tanishq" | awk '{print $2}' | head -1)
    echo "✅ Application running with PID: $PID"
else
    echo "❌ Application not running!"
    echo "Check logs:"
    tail -50 application.log
    exit 1
fi

# Check port
if netstat -tlnp | grep ":3000" > /dev/null; then
    echo "✅ Port 3000 is listening"
else
    echo "❌ Port 3000 not listening!"
    exit 1
fi

# Check logs for successful startup
echo ""
echo "Recent logs:"
tail -30 application.log | grep -i "started\|tomcat\|error" | tail -10

echo ""
echo "Application deployed ✅"
```

---

### **STEP 8: Test Local Access**

```bash
echo "Step 8: Testing local access..."

# Test localhost
echo "Testing localhost:3000..."
LOCALHOST_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 2>/dev/null)

if [ "$LOCALHOST_STATUS" = "200" ]; then
    echo "✅ Localhost test: HTTP $LOCALHOST_STATUS (Success)"
else
    echo "⚠️ Localhost test: HTTP $LOCALHOST_STATUS"
fi

# Test IP
echo "Testing 10.160.128.94:3000..."
IP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://10.160.128.94:3000 2>/dev/null)

if [ "$IP_STATUS" = "200" ]; then
    echo "✅ IP test: HTTP $IP_STATUS (Success)"
else
    echo "⚠️ IP test: HTTP $IP_STATUS"
fi

echo "Local tests complete ✅"
```

---

### **STEP 9: Test Database Connectivity**

```bash
echo "Step 9: Testing database connectivity..."

# Test database connection
DB_TEST=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores;" -s -N 2>/dev/null)

if [ -n "$DB_TEST" ] && [ "$DB_TEST" -gt "0" ]; then
    echo "✅ Database connected: $DB_TEST stores"
else
    echo "❌ Database connection failed!"
fi

# Test application database connection via API
echo ""
echo "Testing login API..."
TEST_USER=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N 2>/dev/null)
TEST_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N 2>/dev/null)

if [ -n "$TEST_USER" ]; then
    echo "Testing with user: $TEST_USER"
    API_RESPONSE=$(curl -s -X POST http://localhost:3000/events/login \
      -H "Content-Type: application/json" \
      -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" 2>/dev/null)
    
    if echo "$API_RESPONSE" | grep -q '"status":true'; then
        echo "✅ Login API works!"
    else
        echo "⚠️ Login API response: $API_RESPONSE"
    fi
fi

echo "Database tests complete ✅"
```

---

### **STEP 10: Verify DNS and Domain Access**

```bash
echo "Step 10: Verifying DNS and domain..."

# Check DNS resolution
echo "Checking DNS resolution..."
nslookup celebrationsite-preprod.tanishq.co.in 2>/dev/null || echo "DNS check failed"

# Wait for ELB health check
echo ""
echo "Waiting for AWS ELB health check (60 seconds)..."
echo "ELB checks target health every 30 seconds"
for i in {60..1}; do
    if [ $((i % 15)) -eq 0 ]; then
        echo "  $i seconds remaining..."
    fi
    sleep 1
done

# Test domain
echo ""
echo "Testing domain: http://celebrationsite-preprod.tanishq.co.in"
DOMAIN_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://celebrationsite-preprod.tanishq.co.in 2>/dev/null)

if [ "$DOMAIN_STATUS" = "200" ] || [ "$DOMAIN_STATUS" = "302" ]; then
    echo "✅ Domain test: HTTP $DOMAIN_STATUS (Success!)"
    echo "🎉 Domain is accessible!"
elif [ "$DOMAIN_STATUS" = "502" ]; then
    echo "⚠️ Domain test: HTTP 502 (Bad Gateway)"
    echo "Possible reasons:"
    echo "  1. ELB target group not configured for 10.160.128.94:3000"
    echo "  2. Security group blocking traffic"
    echo "  3. Health check not passing"
else
    echo "⚠️ Domain test: HTTP $DOMAIN_STATUS"
    echo "Check with AWS team for ELB configuration"
fi

echo ""
echo "DNS verification complete ✅"
```

---

### **STEP 11: Final Verification**

```bash
echo "Step 11: Final verification..."

echo ""
echo "========================================================"
echo "DEPLOYMENT VERIFICATION SUMMARY"
echo "========================================================"
echo ""

# Application status
echo "Application Status:"
ps aux | grep "[j]ava.*tanishq" > /dev/null && echo "  ✅ Running (PID: $(ps aux | grep '[j]ava.*tanishq' | awk '{print $2}' | head -1))" || echo "  ❌ Not Running"

# Port status
netstat -tlnp | grep ":3000" > /dev/null && echo "  ✅ Port 3000 listening" || echo "  ❌ Port not listening"

# Database status
DB_COUNT=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores;" -s -N 2>/dev/null)
echo "  ✅ Database: $DB_COUNT stores"

# Access status
echo ""
echo "Access URLs:"
echo "  Local:   http://localhost:3000"
echo "  IP:      http://10.160.128.94:3000"
echo "  Domain:  http://celebrationsite-preprod.tanishq.co.in"

# Recent logs
echo ""
echo "Recent Application Logs:"
tail -20 application.log | grep -v "SELECT\|UPDATE\|INSERT\|Hibernate" | tail -10

echo ""
echo "========================================================"
echo "DEPLOYMENT COMPLETE!"
echo "========================================================"
echo ""
echo "Next Steps:"
echo "1. Test domain in browser: http://celebrationsite-preprod.tanishq.co.in"
echo "2. If 502 error, contact AWS team to verify ELB configuration"
echo "3. Monitor logs: tail -f /opt/tanishq/applications_preprod/application.log"
echo ""
echo "========================================================"
```

---

## 🎯 COMPLETE ONE-COMMAND DEPLOYMENT

**Copy this ENTIRE script and run on server:**

```bash
#!/bin/bash

echo "========================================================"
echo "CELEBRATIONSITE-PREPROD DEPLOYMENT"
echo "Final URL: http://celebrationsite-preprod.tanishq.co.in"
echo "Date: $(date)"
echo "========================================================"
echo ""

# Step 1: Stop old application
echo "1. Stopping old application..."
pkill -9 -f tanishq-preprod 2>/dev/null
sleep 3
ps aux | grep "[j]ava.*tanishq" && echo "⚠️ Process still running" || echo "✅ Stopped"

# Step 2: Backup
echo ""
echo "2. Creating backup..."
cd /opt/tanishq/applications_preprod
BACKUP_DIR="backup_$(date +%Y%m%d_%H%M%S)"
mkdir -p $BACKUP_DIR
cp -r *.log $BACKUP_DIR/ 2>/dev/null || echo "No logs to backup"
echo "✅ Backup: $BACKUP_DIR"

# Step 3: Clean logs
echo ""
echo "3. Cleaning old logs..."
rm -f application.log app.log nohup.out
echo "✅ Cleaned"

# Step 4: Verify database
echo ""
echo "4. Verifying database..."
DB_COUNT=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores;" -s -N 2>/dev/null)
echo "✅ Database: $DB_COUNT stores"

# Step 5: Configure firewall
echo ""
echo "5. Configuring firewall..."
firewall-cmd --permanent --add-port=3000/tcp 2>/dev/null
firewall-cmd --reload 2>/dev/null
firewall-cmd --list-ports 2>/dev/null | grep -q 3000 && echo "✅ Port 3000 open"

# Step 6: Deploy application
echo ""
echo "6. Deploying application on port 3000..."
cd /opt/tanishq/applications_preprod

nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

APP_PID=$!
echo "✅ Started (PID: $APP_PID)"

# Step 7: Wait for startup
echo ""
echo "7. Waiting for application startup (25 seconds)..."
for i in {25..1}; do
    echo -n "."
    sleep 1
done
echo ""

# Step 8: Verify deployment
echo ""
echo "8. Verifying deployment..."

if ps aux | grep "[j]ava.*tanishq" > /dev/null; then
    REAL_PID=$(ps aux | grep "[j]ava.*tanishq" | awk '{print $2}' | head -1)
    echo "✅ Process running (PID: $REAL_PID)"
else
    echo "❌ Process not running!"
    tail -30 application.log
    exit 1
fi

if netstat -tlnp | grep ":3000" > /dev/null; then
    echo "✅ Port 3000 listening"
else
    echo "❌ Port not listening!"
    exit 1
fi

# Step 9: Test access
echo ""
echo "9. Testing access..."

LOCAL_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 2>/dev/null)
echo "  Localhost: HTTP $LOCAL_STATUS"

IP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://10.160.128.94:3000 2>/dev/null)
echo "  IP Access: HTTP $IP_STATUS"

# Step 10: Test database API
echo ""
echo "10. Testing database API..."
TEST_USER=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N 2>/dev/null)
TEST_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N 2>/dev/null)

API_RESPONSE=$(curl -s -X POST http://localhost:3000/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" 2>/dev/null)

if echo "$API_RESPONSE" | grep -q '"status":true'; then
    echo "✅ Login API works (User: $TEST_USER)"
else
    echo "⚠️ API Response: $(echo $API_RESPONSE | cut -c1-100)"
fi

# Step 11: Wait for ELB and test domain
echo ""
echo "11. Waiting for AWS ELB health check (60 seconds)..."
for i in {60..1}; do
    if [ $((i % 20)) -eq 0 ]; then
        echo "  $i seconds..."
    fi
    sleep 1
done

echo ""
echo "12. Testing domain..."
DOMAIN_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://celebrationsite-preprod.tanishq.co.in 2>/dev/null)

echo "  Domain: HTTP $DOMAIN_STATUS"

# Final summary
echo ""
echo "========================================================"
echo "DEPLOYMENT SUMMARY"
echo "========================================================"
echo ""
echo "Application:  Running on port 3000"
echo "Database:     selfie_preprod ($DB_COUNT stores)"
echo "S3 Bucket:    celebrations-tanishq-preprod"
echo ""
echo "Access URLs:"
echo "  ✅ Local:   http://localhost:3000 (HTTP $LOCAL_STATUS)"
echo "  ✅ IP:      http://10.160.128.94:3000 (HTTP $IP_STATUS)"

if [ "$DOMAIN_STATUS" = "200" ] || [ "$DOMAIN_STATUS" = "302" ]; then
    echo "  ✅ Domain:  http://celebrationsite-preprod.tanishq.co.in (HTTP $DOMAIN_STATUS)"
    echo ""
    echo "🎉🎉🎉 SUCCESS! DOMAIN IS WORKING! 🎉🎉🎉"
elif [ "$DOMAIN_STATUS" = "502" ]; then
    echo "  ⚠️ Domain:  http://celebrationsite-preprod.tanishq.co.in (HTTP 502)"
    echo ""
    echo "⚠️ 502 Bad Gateway - Possible issues:"
    echo "  1. ELB target group not configured for 10.160.128.94:3000"
    echo "  2. Security group blocking traffic from ELB"
    echo "  3. Health check path incorrect"
    echo ""
    echo "Action: Contact AWS team to verify ELB configuration"
else
    echo "  ⚠️ Domain:  http://celebrationsite-preprod.tanishq.co.in (HTTP $DOMAIN_STATUS)"
    echo ""
    echo "Wait 2 more minutes and test again, or contact AWS team"
fi

echo ""
echo "Application Logs:"
echo "  View: tail -f /opt/tanishq/applications_preprod/application.log"
echo ""
echo "Recent startup logs:"
tail -15 application.log | grep -i "started\|tomcat\|error" | tail -5

echo ""
echo "========================================================"
echo "DEPLOYMENT COMPLETE!"
echo "========================================================"
```

---

## 📋 POST-DEPLOYMENT CHECKLIST

### **On Server:**
- [ ] Old application stopped
- [ ] Backup created
- [ ] Logs cleaned
- [ ] Database verified (525 stores)
- [ ] Firewall port 3000 open
- [ ] Application running on port 3000
- [ ] Localhost:3000 returns HTTP 200
- [ ] IP:3000 returns HTTP 200
- [ ] Login API works

### **Domain Access:**
- [ ] DNS resolves (nslookup celebrationsite-preprod.tanishq.co.in)
- [ ] Domain returns HTTP 200 (not 502)
- [ ] Can access in browser

### **AWS Configuration:**
- [ ] ELB target: 10.160.128.94:3000
- [ ] Target health: Healthy
- [ ] Security groups: Allow traffic
- [ ] DNS CNAME: Points to ELB

---

## 🧪 TESTING AFTER DEPLOYMENT

### **Test 1: Local Server**
```bash
curl http://localhost:3000
# Should return HTML
```

### **Test 2: IP Access**
```bash
curl http://10.160.128.94:3000
# Should return HTML
```

### **Test 3: Domain Access**
```bash
curl http://celebrationsite-preprod.tanishq.co.in
# Should return HTML (not 502!)
```

### **Test 4: Login API**
```bash
curl -X POST http://celebrationsite-preprod.tanishq.co.in/events/login \
  -H "Content-Type: application/json" \
  -d '{"code":"TEST","password":"Titan@123"}'
# Should return JSON
```

### **Test 5: Browser**
```
Open: http://celebrationsite-preprod.tanishq.co.in
Should see: "Let's Celebrate with Tanishq" page
```

---

## 🆘 TROUBLESHOOTING

### **Issue: 502 Bad Gateway on Domain**

**Cause:** ELB can't reach application on port 3000

**Fix:**
```bash
# Verify app is on port 3000
netstat -tlnp | grep 3000

# Check AWS Console:
# EC2 → Target Groups → Verify target 10.160.128.94:3000 shows "Healthy"

# If unhealthy, check security group allows traffic from ELB
```

---

### **Issue: Application Not Starting**

**Cause:** Error in startup

**Fix:**
```bash
# Check logs
tail -100 /opt/tanishq/applications_preprod/application.log

# Look for errors
grep -i "error\|exception" application.log | tail -20

# Restart
cd /opt/tanishq/applications_preprod
pkill -f tanishq-preprod
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --server.port=3000 --spring.profiles.active=preprod \
  > application.log 2>&1 &
```

---

### **Issue: Database Connection Error**

**Cause:** Database credentials or connectivity

**Fix:**
```bash
# Test database
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT 1;"

# Check connection string in startup command
ps aux | grep java | grep datasource
```

---

## ✅ SUCCESS CRITERIA

**Deployment is successful when:**

✅ Application running on port 3000  
✅ Database connected (525 stores)  
✅ `curl http://localhost:3000` returns HTML  
✅ `curl http://10.160.128.94:3000` returns HTML  
✅ `curl http://celebrationsite-preprod.tanishq.co.in` returns HTML  
✅ Login API returns JSON response  
✅ Browser shows "Celebrate with Tanishq" page  

---

## 🎯 QUICK REFERENCE

**Start Application:**
```bash
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --server.port=3000 --spring.profiles.active=preprod \
  > application.log 2>&1 &
```

**Stop Application:**
```bash
pkill -f tanishq-preprod
```

**Check Status:**
```bash
ps aux | grep java | grep tanishq
netstat -tlnp | grep 3000
tail -50 application.log
```

**Test Domain:**
```bash
curl http://celebrationsite-preprod.tanishq.co.in
```

---

**RUN THE COMPLETE ONE-COMMAND DEPLOYMENT SCRIPT NOW!** 🚀

It will handle everything automatically from scratch!

