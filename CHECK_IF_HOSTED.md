# ✅ HOW TO CHECK IF YOUR PROJECT IS HOSTED

## 🎯 QUICK HEALTH CHECK - RUN THIS NOW

**Copy and paste this on the server:**

```bash
echo "================================================"
echo "PROJECT HEALTH CHECK"
echo "================================================"

# 1. Check if application is running
echo ""
echo "1. Application Process:"
if ps aux | grep -q "[j]ava.*tanishq"; then
    echo "   ✅ RUNNING"
    ps aux | grep "[j]ava.*tanishq" | awk '{print "   PID: "$2", User: "$1}'
else
    echo "   ❌ NOT RUNNING"
fi

# 2. Check port 3002
echo ""
echo "2. Port 3002 (Application):"
if netstat -tlnp 2>/dev/null | grep -q ":3002"; then
    echo "   ✅ LISTENING"
    netstat -tlnp 2>/dev/null | grep ":3002"
else
    echo "   ❌ NOT LISTENING"
fi

# 3. Check database connection
echo ""
echo "3. Database (selfie_preprod):"
DB_CHECK=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores;" -s -N 2>/dev/null)
if [ -n "$DB_CHECK" ] && [ "$DB_CHECK" -gt "0" ]; then
    echo "   ✅ CONNECTED - $DB_CHECK stores"
else
    echo "   ❌ NOT CONNECTED or NO DATA"
fi

# 4. Check API endpoint
echo ""
echo "4. API Health (Login endpoint):"
API_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d '{"code":"TEST","password":"test"}' 2>/dev/null)

if [ "$API_RESPONSE" = "200" ]; then
    echo "   ✅ RESPONDING - HTTP 200"
elif [ "$API_RESPONSE" = "401" ] || [ "$API_RESPONSE" = "403" ]; then
    echo "   ✅ RESPONDING - HTTP $API_RESPONSE (auth error is OK, means API works)"
else
    echo "   ❌ NOT RESPONDING - HTTP $API_RESPONSE"
fi

# 5. Test with real credentials
echo ""
echo "5. Real Login Test:"
TEST_USER=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N 2>/dev/null)
TEST_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N 2>/dev/null)

if [ -n "$TEST_USER" ] && [ -n "$TEST_PASS" ]; then
    echo "   Testing with user: $TEST_USER"
    LOGIN_RESULT=$(curl -s -X POST http://localhost:3002/events/login \
      -H "Content-Type: application/json" \
      -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}")
    
    if echo "$LOGIN_RESULT" | grep -q '"status":true'; then
        echo "   ✅ LOGIN SUCCESSFUL"
        echo "   Response: $(echo "$LOGIN_RESULT" | head -c 80)..."
    else
        echo "   ❌ LOGIN FAILED"
        echo "   Response: $LOGIN_RESULT"
    fi
else
    echo "   ⚠️  Could not get test credentials"
fi

# 6. Check logs for errors
echo ""
echo "6. Recent Logs:"
if [ -f "/opt/tanishq/applications_preprod/application.log" ]; then
    ERROR_COUNT=$(tail -100 /opt/tanishq/applications_preprod/application.log | grep -i "error" | wc -l)
    if [ "$ERROR_COUNT" -eq "0" ]; then
        echo "   ✅ NO ERRORS in last 100 lines"
    else
        echo "   ⚠️  $ERROR_COUNT errors found in last 100 lines"
        echo "   Last 3 errors:"
        tail -100 /opt/tanishq/applications_preprod/application.log | grep -i "error" | tail -3
    fi
else
    echo "   ⚠️  Log file not found"
fi

# 7. External access check
echo ""
echo "7. External Access:"
SERVER_IP=$(hostname -I | awk '{print $1}')
echo "   Server IP: $SERVER_IP"
echo "   Internal URL: http://localhost:3002"
echo "   External URL: http://$SERVER_IP:3002"
echo ""
echo "   To test from Windows, open browser:"
echo "   http://$SERVER_IP:3002/events/login"

echo ""
echo "================================================"
echo "SUMMARY"
echo "================================================"

ISSUES=0

ps aux | grep -q "[j]ava.*tanishq" || { echo "❌ Application not running"; ((ISSUES++)); }
netstat -tlnp 2>/dev/null | grep -q ":3002" || { echo "❌ Port not listening"; ((ISSUES++)); }
[ -n "$DB_CHECK" ] && [ "$DB_CHECK" -gt "0" ] || { echo "❌ Database issue"; ((ISSUES++)); }

if [ "$ISSUES" -eq "0" ]; then
    echo ""
    echo "✅✅✅ PROJECT IS HOSTED AND WORKING! ✅✅✅"
    echo ""
    echo "Access URLs:"
    echo "  - From server: http://localhost:3002"
    echo "  - From network: http://$SERVER_IP:3002"
    echo ""
    echo "Test endpoints:"
    echo "  - Login: http://$SERVER_IP:3002/events/login"
    echo "  - Health: http://$SERVER_IP:3002/actuator/health"
else
    echo ""
    echo "⚠️  PROJECT HAS $ISSUES ISSUE(S)"
    echo ""
    echo "Run this to see detailed logs:"
    echo "tail -50 /opt/tanishq/applications_preprod/application.log"
fi

echo "================================================"
```

---

## 🌐 CHECK FROM YOUR WINDOWS MACHINE

**After running the health check above, test from Windows:**

### **Method 1: Using curl (from your local PC)**

```cmd
REM Get the server IP from the health check output above
curl -X POST http://10.160.128.94:3002/events/login ^
  -H "Content-Type: application/json" ^
  -d "{\"code\":\"TEST\",\"password\":\"test\"}"
```

### **Method 2: Using Browser (from your local PC)**

1. Open Chrome/Firefox
2. Go to: `http://10.160.128.94:3002`
3. You should see a response (might be error page, but proves it's hosted)

### **Method 3: Using Postman (from your local PC)**

1. Open Postman
2. Create POST request to: `http://10.160.128.94:3002/events/login`
3. Headers: `Content-Type: application/json`
4. Body (raw JSON):
   ```json
   {
     "code": "TEST",
     "password": "Titan@123"
   }
   ```
5. Click Send

---

## 📊 WHAT "HOSTED" MEANS

Your project is **SUCCESSFULLY HOSTED** if:

✅ **Application running** - Java process is active  
✅ **Port listening** - Port 3002 is open  
✅ **Database connected** - Can query selfie_preprod  
✅ **API responding** - HTTP requests return responses  
✅ **Login works** - Can authenticate users  

---

## 🔍 DETAILED CHECKS

### **Check 1: Process Running**
```bash
ps aux | grep java
```
**Expected:** See line with `tanishq-preprod...war`

### **Check 2: Port Active**
```bash
netstat -tlnp | grep 3002
```
**Expected:** `tcp ... LISTEN ... java`

### **Check 3: Application Logs**
```bash
tail -50 /opt/tanishq/applications_preprod/application.log
```
**Expected:** See "Started Application" or "Tomcat started on port(s): 3002"

### **Check 4: Test API**
```bash
curl http://localhost:3002/events/login
```
**Expected:** Some JSON response (even error is OK - means API is up)

### **Check 5: Test with Real Data**
```bash
# Get real user
TEST_USER=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N)
TEST_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N)

# Test login
curl -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}"
```
**Expected:** `{"status":true,"storeData":{...}}`

---

## 🚨 IF NOT HOSTED

**Start the application:**

```bash
cd /opt/tanishq/applications_preprod

nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3002 \
  > application.log 2>&1 &

echo "Started! PID: $!"
echo "Wait 15 seconds for startup..."
sleep 15

# Check
ps aux | grep java | grep tanishq
```

---

## 📱 ACCESS FROM ANYWHERE

**If hosted successfully, you can access from:**

1. **From Server itself:**
   ```bash
   curl http://localhost:3002/events/login
   ```

2. **From Windows PC on same network:**
   ```
   http://10.160.128.94:3002
   ```

3. **From Internet (if firewall allows):**
   ```
   http://YOUR_PUBLIC_IP:3002
   ```

---

## 🎯 QUICK YES/NO CHECK

**Run this single command:**

```bash
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:3002/events/login && echo "✅ HOSTED!" || echo "❌ NOT HOSTED"
```

**If you see:** `HTTP Status: 200` or `HTTP Status: 401` → **✅ HOSTED!**  
**If you see:** `HTTP Status: 000` or connection error → **❌ NOT HOSTED**

---

**RUN THE COMPLETE HEALTH CHECK (first script) NOW!** 🚀

It will tell you everything about your deployment status!

