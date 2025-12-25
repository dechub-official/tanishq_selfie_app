## 📝 PERMANENT FIX - CREATE STARTUP SCRIPT

**Create restart script for future:**

```bash
cat > /opt/tanishq/applications_preprod/start_app.sh << 'EOF'
#!/bin/bash

# Stop any existing process
pkill -f tanishq-preprod
sleep 3

# Start application on port 3000
cd /opt/tanishq/applications_preprod

nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

echo "Application started on port 3000"
echo "PID: $!"
EOF

# Make executable
chmod +x /opt/tanishq/applications_preprod/start_app.sh

echo "Startup script created: /opt/tanishq/applications_preprod/start_app.sh"
```

**Future usage:**
```bash
/opt/tanishq/applications_preprod/start_app.sh
```

---

## 🚨 IF STILL SHOWS 502 AFTER FIX

**Wait 1-2 minutes** for ELB health check to pass, then:

```bash
# Check if app is really on 3000
netstat -tlnp | grep 3000

# Check app logs
tail -50 /opt/tanishq/applications_preprod/application.log

# Test local
curl http://localhost:3000

# Check ELB target health in AWS Console
# EC2 → Target Groups → Check if target shows "Healthy"
```

---

## ✅ SUMMARY

**Problem:** 502 Bad Gateway  
**Cause:** App on port 3002, ELB expects port 3000  
**Solution:** Restart app on port 3000  
**Result:** Domain should work!

**Run the COMPLETE ONE-COMMAND FIX now!**

---

**COPY THE BIG COMMAND BLOCK AND RUN IT ON SERVER!** 🚀

It will:
1. ✅ Stop app on port 3002
2. ✅ Open firewall for port 3000
3. ✅ Start app on port 3000
4. ✅ Wait for startup
5. ✅ Test everything
6. ✅ Show you if it's working

**Paste it and hit enter!** 💪
# 🚨 502 BAD GATEWAY FIX - PORT MISMATCH

## ⚠️ THE PROBLEM

**Error:** `502 Bad Gateway` when accessing `http://celebrationsite-preprod.tanishq.co.in`

**Root Cause:**
```
❌ Your app runs on:        Port 3002
❌ AWS ELB expects:          Port 3000
❌ Result:                   Load Balancer can't connect to your app
```

**Current Status:**
```
✅ Application Running:  PID 256305
✅ Port Listening:       3002 (WRONG PORT)
❌ ELB Connection:       Failed (expects port 3000)
```

---

## ✅ SOLUTION: CHANGE PORT TO 3000

### **STEP 1: Stop Current Application**

```bash
# Kill the process running on port 3002
pkill -f tanishq-preprod

# Verify it stopped
ps aux | grep java | grep tanishq
```

---

### **STEP 2: Start Application on Port 3000**

```bash
cd /opt/tanishq/applications_preprod

# Start with correct port (3000 instead of 3002)
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

echo "Started on port 3000. PID: $!"
```

---

### **STEP 3: Open Firewall for Port 3000**

```bash
# Open port 3000 in firewall
firewall-cmd --permanent --add-port=3000/tcp
firewall-cmd --reload

# Verify
firewall-cmd --list-ports | grep 3000
```

---

### **STEP 4: Verify Application**

```bash
# Wait for startup
sleep 15

# Check process
ps aux | grep java | grep tanishq

# Check port
netstat -tlnp | grep 3000

# Test locally
curl -I http://localhost:3000
```

---

### **STEP 5: Test Domain (Should Work Now!)**

```bash
# Wait a bit more for ELB health check
sleep 30

# Test domain
curl -I http://celebrationsite-preprod.tanishq.co.in

# Should see HTTP 200 now!
```

---

## 🚀 COMPLETE ONE-COMMAND FIX

**Copy this entire block:**

```bash
echo "========================================================"
echo "FIXING 502 BAD GATEWAY - CHANGING PORT TO 3000"
echo "========================================================"
echo ""

# Step 1: Stop current application
echo "1. Stopping application on port 3002..."
pkill -f tanishq-preprod
sleep 3

# Step 2: Open firewall
echo ""
echo "2. Opening port 3000 in firewall..."
firewall-cmd --permanent --add-port=3000/tcp 2>/dev/null
firewall-cmd --reload 2>/dev/null
firewall-cmd --list-ports 2>/dev/null | grep 3000 && echo "   ✅ Port 3000 open" || echo "   ⚠️  Firewall command not available"

# Step 3: Start on correct port
echo ""
echo "3. Starting application on port 3000..."
cd /opt/tanishq/applications_preprod

nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

APP_PID=$!
echo "   Started with PID: $APP_PID"

# Step 4: Wait for startup
echo ""
echo "4. Waiting for application to start (15 seconds)..."
sleep 15

# Step 5: Verify
echo ""
echo "5. Verification:"
ps aux | grep "[j]ava.*tanishq" > /dev/null && echo "   ✅ Process running" || echo "   ❌ Process not running"
netstat -tlnp 2>/dev/null | grep ":3000" > /dev/null && echo "   ✅ Port 3000 listening" || echo "   ❌ Port not listening"

echo ""
echo "6. Testing local access:"
LOCAL_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 2>/dev/null)
echo "   Local (http://localhost:3000): HTTP $LOCAL_STATUS"

# Step 6: Wait for ELB health check
echo ""
echo "7. Waiting for ELB health check (30 seconds)..."
sleep 30

echo ""
echo "8. Testing domain:"
DOMAIN_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://celebrationsite-preprod.tanishq.co.in 2>/dev/null)
echo "   Domain: HTTP $DOMAIN_STATUS"

if [ "$DOMAIN_STATUS" = "200" ] || [ "$DOMAIN_STATUS" = "302" ]; then
    echo ""
    echo "========================================================"
    echo "✅✅✅ SUCCESS! 502 BAD GATEWAY FIXED! ✅✅✅"
    echo "========================================================"
    echo ""
    echo "Your URLs are now working:"
    echo "  ✅ http://celebrationsite-preprod.tanishq.co.in"
    echo "  ✅ http://preprod.tanishq.co.in"
    echo ""
else
    echo ""
    echo "⚠️  Domain still showing HTTP $DOMAIN_STATUS"
    echo "Wait 1-2 minutes for ELB health check to pass."
    echo ""
    echo "Check logs: tail -50 /opt/tanishq/applications_preprod/application.log"
fi

echo ""
echo "========================================================"
```

---

## 📊 WHAT CHANGED

| Item | Before (WRONG) | After (CORRECT) |
|------|----------------|-----------------|
| **Port** | 3002 | 3000 |
| **Command** | `--server.port=3002` | `--server.port=3000` |
| **Firewall** | 3002/tcp | 3000/tcp |
| **ELB Target** | Expects 3000 | ✅ Matches now |
| **Domain Access** | ❌ 502 Bad Gateway | ✅ Should work |

---

## 🔍 WHY 502 BAD GATEWAY HAPPENED

**502 Bad Gateway means:**
> "The load balancer received a request but couldn't forward it to your application"

**Reason:**
```
1. Browser/User → http://celebrationsite-preprod.tanishq.co.in
2. DNS resolves → ELB (internal-Jew-Testing-ELB-2118632530...)
3. ELB tries to forward → 10.160.128.94:3000
4. But your app is on → 10.160.128.94:3002 ❌
5. ELB can't connect → Returns 502 Bad Gateway
```

**Now with port 3000:**
```
1. Browser/User → http://celebrationsite-preprod.tanishq.co.in
2. DNS resolves → ELB
3. ELB forwards → 10.160.128.94:3000
4. Your app responds → ✅ Success!
5. User gets → 200 OK
```

---

## ✅ VERIFICATION CHECKLIST

After running the fix, verify:

- [ ] Application stopped (old process on 3002)
- [ ] New process started on port 3000
- [ ] Firewall allows port 3000
- [ ] `curl http://localhost:3000` returns HTTP 200
- [ ] `curl http://10.160.128.94:3000` returns HTTP 200
- [ ] Wait 1-2 minutes for ELB health check
- [ ] `curl http://celebrationsite-preprod.tanishq.co.in` returns HTTP 200
- [ ] Browser shows website (not 502)

---

## 🧪 TEST AFTER FIX

### **On Server:**

```bash
# Check process
ps aux | grep java | grep tanishq

# Should show:
# root  <PID>  ... java -jar tanishq-preprod... --server.port=3000

# Check port
netstat -tlnp | grep 3000

# Should show:
# tcp6  0  0 :::3000  :::*  LISTEN  <PID>/java

# Test local
curl http://localhost:3000

# Should see HTML response
```

---

### **From Windows:**

**Browser:**
```
http://celebrationsite-preprod.tanishq.co.in
http://preprod.tanishq.co.in
```

**Should see:** Your "Celebrate with Tanishq" landing page (NOT 502 error)

**Command Prompt:**
```cmd
curl http://celebrationsite-preprod.tanishq.co.in
```

**Should see:** HTML content

---

## 🎯 AWS ELB CONFIGURATION (CONFIRMED)

**The AWS team has ELB configured for:**

```
Target Group:
  Target: 10.160.128.94:3000  ← Port 3000!
  Protocol: HTTP
  Health Check: / (port 3000)
  
Listener:
  Port 80 → Forward to Target Group
```

**So you MUST run on port 3000!**

---


