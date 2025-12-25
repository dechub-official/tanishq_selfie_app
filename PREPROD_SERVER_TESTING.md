  -F "files=@test.jpg"
```

**Expected:**
```json
{
  "status": true,
  "message": "All 1 files uploaded successfully to S3",
  "result": ["https://celebrations-tanishq-preprod.s3.ap-south-1.amazonaws.com/..."]
}
```

---

## 🔍 STEP 3: CHECK PRE-PROD URL/DOMAIN

### **If you have a domain configured:**

**Common pre-prod domain formats:**
- `https://preprod-celebrations.tanishq.com`
- `https://celebrations-preprod.tanishq.com`
- `https://preprod.celebrations.tanishq.com`

**Test each:**

```cmd
curl -I https://preprod-celebrations.tanishq.com
curl -I https://celebrations-preprod.tanishq.com
curl -I http://10.160.128.94:3002
```

---

### **Check DNS Configuration (on server):**

```bash
# Check if domain is configured
cat /etc/nginx/sites-enabled/* 2>/dev/null | grep server_name
cat /etc/httpd/conf.d/*.conf 2>/dev/null | grep ServerName

# Check if reverse proxy is running
ps aux | grep nginx
ps aux | grep httpd
```

---

### **Check SSL/HTTPS:**

```bash
# Check for SSL certificates
ls -la /etc/letsencrypt/live/ 2>/dev/null
ls -la /etc/ssl/certs/ 2>/dev/null | grep tanishq

# Check HTTPS port
netstat -tlnp | grep ":443"
```

---

## 📊 STEP 4: COMPLETE INTEGRATION TEST

**Run this from Windows to test all features:**

```cmd
@echo off
echo ================================================
echo PRE-PROD COMPLETE INTEGRATION TEST
echo ================================================
echo.

echo 1. Testing server connectivity...
curl -s -o nul -w "HTTP %%{http_code}" http://10.160.128.94:3002
echo.
echo.

echo 2. Testing Login API...
curl -X POST http://10.160.128.94:3002/events/login ^
  -H "Content-Type: application/json" ^
  -d "{\"code\":\"TEST\",\"password\":\"test\"}"
echo.
echo.

echo 3. Testing Get Events API...
curl -X POST http://10.160.128.94:3002/events/getevents ^
  -H "Content-Type: application/json" ^
  -d "{\"storeCode\":\"TEST\",\"startDate\":\"2025-01-01\",\"endDate\":\"2025-12-31\"}"
echo.
echo.

echo 4. Testing Health Endpoint...
curl http://10.160.128.94:3002/actuator/health
echo.
echo.

echo ================================================
echo TEST COMPLETE!
echo ================================================
```

**Save as:** `test_preprod.bat` and run it

---

## ✅ VERIFICATION CHECKLIST

### **On Server:**
- [ ] Application process running
- [ ] Port 3002 listening
- [ ] Database connected (selfie_preprod)
- [ ] No errors in logs
- [ ] S3 configured

### **From Network (Windows PC):**
- [ ] Can ping 10.160.128.94
- [ ] Can access http://10.160.128.94:3002
- [ ] Login API responds
- [ ] Get Events API responds
- [ ] S3 upload works

### **Domain (if configured):**
- [ ] DNS resolves to server IP
- [ ] Domain URL accessible
- [ ] SSL certificate valid
- [ ] HTTPS redirects working

---

## 🚨 COMMON ISSUES & FIXES

### **Issue 1: Connection Refused from Windows**

**Fix - Open firewall on server:**
```bash
firewall-cmd --permanent --add-port=3002/tcp
firewall-cmd --reload
```

### **Issue 2: Application Not Running**

**Fix - Start application:**
```bash
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3002 \
  > application.log 2>&1 &
```

### **Issue 3: Domain Not Working**

**Check if Nginx/Apache is configured:**
```bash
# Check web server
systemctl status nginx
systemctl status httpd

# Check configuration
cat /etc/nginx/nginx.conf
cat /etc/httpd/conf/httpd.conf
```

---

## 📱 QUICK STATUS CHECK - ONE COMMAND

**On Server:**
```bash
echo "App: $(ps aux | grep -c '[j]ava.*tanishq') | Port: $(netstat -tlnp 2>/dev/null | grep -c ':3002') | DB: $(mysql -u root -pDechub#2025 selfie_preprod -e 'SELECT COUNT(*) FROM stores;' -s -N 2>/dev/null || echo 0) stores"
```

**Expected:** `App: 1 | Port: 1 | DB: 450 stores`

**From Windows:**
```cmd
curl -s -o nul -w "Server HTTP: %%{http_code}\n" http://10.160.128.94:3002
```

**Expected:** `Server HTTP: 200` or `Server HTTP: 404`

---

## 🎯 WHAT YOU NEED TO DO NOW:

1. ✅ **SSH to server** (10.160.128.94)
2. ✅ **Run STEP 1** (server status check script)
3. ✅ **Run STEP 2** from your Windows PC (browser/curl tests)
4. ✅ **Check STEP 3** if you have a domain
5. ✅ **Verify all checkboxes** in the checklist

---

**START WITH STEP 1 ON THE SERVER NOW!** 🚀

Copy the first big script and run it on your server!
# 🌐 PRE-PROD SERVER TESTING GUIDE

**Server IP:** 10.160.128.94  
**Port:** 3002  
**Date:** December 4, 2025

---

## ✅ STEP 1: CHECK SERVER IS RUNNING (On Server)

**SSH to server and run this:**

```bash
echo "================================================"
echo "PRE-PROD SERVER STATUS CHECK"
echo "================================================"
echo ""

# 1. Check application process
echo "1. Application Process:"
if ps aux | grep -q "[j]ava.*tanishq"; then
    echo "   ✅ RUNNING"
    APP_PID=$(ps aux | grep "[j]ava.*tanishq" | awk '{print $2}' | head -1)
    echo "   PID: $APP_PID"
    echo "   Uptime: $(ps -p $APP_PID -o etime= 2>/dev/null || echo 'Unknown')"
else
    echo "   ❌ NOT RUNNING - Need to start!"
fi

# 2. Check port 3002
echo ""
echo "2. Port 3002 Status:"
if netstat -tlnp 2>/dev/null | grep -q ":3002"; then
    echo "   ✅ LISTENING"
    netstat -tlnp 2>/dev/null | grep ":3002"
else
    echo "   ❌ NOT LISTENING"
fi

# 3. Check database
echo ""
echo "3. Database Connection:"
DB_STORES=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores;" -s -N 2>/dev/null)
DB_USERS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM users;" -s -N 2>/dev/null)
DB_EVENTS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM events;" -s -N 2>/dev/null)

if [ -n "$DB_STORES" ]; then
    echo "   ✅ CONNECTED"
    echo "   Stores: $DB_STORES"
    echo "   Users: $DB_USERS"
    echo "   Events: $DB_EVENTS"
else
    echo "   ❌ DATABASE ERROR"
fi

# 4. Check local API
echo ""
echo "4. Local API Test (localhost):"
API_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d '{"code":"TEST","password":"test"}' 2>/dev/null)

if [ "$API_STATUS" = "200" ] || [ "$API_STATUS" = "401" ]; then
    echo "   ✅ API RESPONDING - HTTP $API_STATUS"
else
    echo "   ❌ API NOT RESPONDING - HTTP $API_STATUS"
fi

# 5. Test real login
echo ""
echo "5. Real Login Test:"
TEST_USER=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N 2>/dev/null)
TEST_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N 2>/dev/null)

if [ -n "$TEST_USER" ]; then
    echo "   Testing with user: $TEST_USER"
    LOGIN_RESULT=$(curl -s -X POST http://localhost:3002/events/login \
      -H "Content-Type: application/json" \
      -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" 2>/dev/null)
    
    if echo "$LOGIN_RESULT" | grep -q '"status":true'; then
        echo "   ✅ LOGIN WORKS!"
        echo "   Response: $(echo "$LOGIN_RESULT" | cut -c1-100)..."
    else
        echo "   ⚠️  LOGIN FAILED"
        echo "   Response: $LOGIN_RESULT"
    fi
fi

# 6. Check S3 configuration
echo ""
echo "6. S3 Configuration:"
if [ -f "/opt/tanishq/applications_preprod/application.log" ]; then
    if grep -q "celebrations-tanishq-preprod" /opt/tanishq/applications_preprod/application.log 2>/dev/null; then
        echo "   ✅ S3 CONFIGURED (celebrations-tanishq-preprod)"
    else
        echo "   ⚠️  S3 status unknown"
    fi
fi

# 7. Check firewall
echo ""
echo "7. Firewall Status (Port 3002):"
if command -v firewall-cmd &> /dev/null; then
    if firewall-cmd --list-ports 2>/dev/null | grep -q "3002"; then
        echo "   ✅ FIREWALL ALLOWS PORT 3002"
    else
        echo "   ⚠️  PORT 3002 NOT IN FIREWALL"
        echo "   Run: firewall-cmd --permanent --add-port=3002/tcp"
        echo "   Then: firewall-cmd --reload"
    fi
else
    echo "   ℹ️  Firewall command not available"
fi

# 8. Server URLs
echo ""
echo "8. Access URLs:"
SERVER_IP=$(hostname -I | awk '{print $1}')
echo "   Internal: http://localhost:3002"
echo "   Network:  http://$SERVER_IP:3002"
echo "   Public:   http://$(curl -s ifconfig.me 2>/dev/null || echo 'UNKNOWN'):3002"

# 9. Recent logs
echo ""
echo "9. Recent Application Logs:"
if [ -f "/opt/tanishq/applications_preprod/application.log" ]; then
    ERROR_COUNT=$(tail -50 /opt/tanishq/applications_preprod/application.log | grep -i "error" | wc -l)
    if [ "$ERROR_COUNT" -eq "0" ]; then
        echo "   ✅ NO ERRORS in last 50 lines"
    else
        echo "   ⚠️  $ERROR_COUNT errors in last 50 lines"
    fi
    
    echo ""
    echo "   Last startup message:"
    grep -i "started.*application\|tomcat.*started" /opt/tanishq/applications_preprod/application.log | tail -1 | sed 's/^/   /'
fi

echo ""
echo "================================================"
echo "SUMMARY"
echo "================================================"

# Count issues
ISSUES=0
ps aux | grep -q "[j]ava.*tanishq" || ((ISSUES++))
netstat -tlnp 2>/dev/null | grep -q ":3002" || ((ISSUES++))
[ -n "$DB_STORES" ] || ((ISSUES++))

if [ "$ISSUES" -eq "0" ]; then
    echo ""
    echo "✅✅✅ PRE-PROD SERVER IS WORKING! ✅✅✅"
    echo ""
    echo "Server is ready for testing!"
    echo "Network URL: http://$SERVER_IP:3002"
else
    echo ""
    echo "⚠️  FOUND $ISSUES ISSUE(S)"
    echo ""
    echo "Check logs: tail -50 /opt/tanishq/applications_preprod/application.log"
fi

echo "================================================"
```

---

## 🌐 STEP 2: CHECK FROM YOUR WINDOWS PC

### **A. Quick Browser Test**

**Open your browser and try these URLs:**

```
1. http://10.160.128.94:3002
2. http://10.160.128.94:3002/events/login
3. http://10.160.128.94:3002/actuator/health
```

**Expected:** 
- See some response (even 404 or error page means server is reachable)
- NOT "Connection refused" or "Timeout"

---

### **B. Test Login API from Windows**

**Using curl (in Command Prompt):**

```cmd
curl -X POST http://10.160.128.94:3002/events/login ^
  -H "Content-Type: application/json" ^
  -d "{\"code\":\"TEST\",\"password\":\"Titan@123\"}"
```

**Expected Response:**
```json
{"status":false,"storeData":null,"message":"Invalid credentials"}
```
*(This is OK - means API is working, just wrong password)*

---

### **C. Test with Postman**

1. **Open Postman**
2. **Create new POST request**
3. **URL:** `http://10.160.128.94:3002/events/login`
4. **Headers:**
   - Key: `Content-Type`
   - Value: `application/json`
5. **Body** (raw, JSON):
   ```json
   {
     "code": "TEST",
     "password": "Titan@123"
   }
   ```
6. **Click Send**

**Expected:** JSON response (status true or false)

---

### **D. Test Get Events API**

```cmd
curl -X POST http://10.160.128.94:3002/events/getevents ^
  -H "Content-Type: application/json" ^
  -d "{\"storeCode\":\"TEST\",\"startDate\":\"2025-01-01\",\"endDate\":\"2025-12-31\"}"
```

---

### **E. Test S3 Upload**

**First, get a real event ID from server:**

```bash
# On server
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT id FROM events LIMIT 1;" -s -N
```

**Then from Windows:**

```cmd
REM Create a test file first
echo Test image > test.jpg

REM Upload (replace EVENT_ID with actual event ID)
curl -X POST http://10.160.128.94:3002/events/uploadCompletedEvents ^
  -F "eventId=EVENT_ID" ^

