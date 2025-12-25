# ⚡ QUICK PRE-PROD CHECK - COPY & PASTE

**Server:** 10.160.128.94:3002  
**Date:** December 4, 2025

---

## 🎯 ON SERVER (SSH to 10.160.128.94)

**Copy this entire block:**

```bash
echo "=== PRE-PROD STATUS ===" && \
echo "" && \
echo "1. App Process:" && \
ps aux | grep "[j]ava.*tanishq" > /dev/null && echo "   ✅ Running (PID: $(ps aux | grep '[j]ava.*tanishq' | awk '{print $2}' | head -1))" || echo "   ❌ Not Running" && \
echo "" && \
echo "2. Port 3002:" && \
netstat -tlnp 2>/dev/null | grep ":3002" > /dev/null && echo "   ✅ Listening" || echo "   ❌ Not Listening" && \
echo "" && \
echo "3. Database:" && \
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT CONCAT('   ✅ Connected - ', COUNT(*), ' stores') FROM stores;" -s -N 2>/dev/null || echo "   ❌ Error" && \
echo "" && \
echo "4. API Test:" && \
TEST_USER=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N 2>/dev/null) && \
TEST_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N 2>/dev/null) && \
curl -s -X POST http://localhost:3002/events/login -H "Content-Type: application/json" -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" | grep -q '"status":true' && echo "   ✅ Login API Works!" || echo "   ⚠️  API Issue" && \
echo "" && \
echo "5. Firewall:" && \
firewall-cmd --list-ports 2>/dev/null | grep -q "3002" && echo "   ✅ Port 3002 Open" || echo "   ⚠️  Port 3002 Not in Firewall" && \
echo "" && \
echo "6. Access URLs:" && \
echo "   Local:   http://localhost:3002" && \
echo "   Network: http://$(hostname -I | awk '{print $1}'):3002" && \
echo "" && \
echo "=== STATUS COMPLETE ==="
```

**Expected if WORKING:**
```
=== PRE-PROD STATUS ===

1. App Process:
   ✅ Running (PID: 12345)

2. Port 3002:
   ✅ Listening

3. Database:
   ✅ Connected - 450 stores

4. API Test:
   ✅ Login API Works!

5. Firewall:
   ✅ Port 3002 Open

6. Access URLs:
   Local:   http://localhost:3002
   Network: http://10.160.128.94:3002

=== STATUS COMPLETE ===
```

---

## 🌐 FROM YOUR WINDOWS PC

**Test 1: Browser**
```
Open: http://10.160.128.94:3002
```

**Test 2: Command Line**
```cmd
curl http://10.160.128.94:3002/events/login
```

**Test 3: Login API**
```cmd
curl -X POST http://10.160.128.94:3002/events/login ^
  -H "Content-Type: application/json" ^
  -d "{\"code\":\"TEST\",\"password\":\"Titan@123\"}"
```

**Test 4: Complete Test (save as test.bat)**
```cmd
@echo off
echo Testing Pre-Prod Server...
echo.
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://10.160.128.94:3002
curl -X POST http://10.160.128.94:3002/events/login -H "Content-Type: application/json" -d "{\"code\":\"TEST\",\"password\":\"test\"}"
echo.
echo Test Complete!
pause
```

---

## 🚨 IF NOT WORKING - START APPLICATION

**On Server:**

```bash
cd /opt/tanishq/applications_preprod && \
pkill -f tanishq && \
sleep 3 && \
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3002 \
  > application.log 2>&1 & \
echo "Started! PID: $!" && \
echo "Waiting 15 seconds..." && \
sleep 15 && \
ps aux | grep "[j]ava.*tanishq" && \
echo "✅ Now check: http://$(hostname -I | awk '{print $1}'):3002"
```

---

## 🔥 IF FIREWALL BLOCKING

**On Server:**

```bash
# Open port 3002
firewall-cmd --permanent --add-port=3002/tcp
firewall-cmd --reload

# Verify
firewall-cmd --list-ports | grep 3002
```

---

## 📋 CHECKLIST

### **Server Status:**
- [ ] Application running
- [ ] Port 3002 listening  
- [ ] Database connected
- [ ] No errors in logs
- [ ] Firewall allows port 3002

### **External Access (from Windows):**
- [ ] Can ping 10.160.128.94
- [ ] Can access http://10.160.128.94:3002
- [ ] Login API responds
- [ ] Get correct JSON response

### **If You Have Domain:**
- [ ] DNS points to 10.160.128.94
- [ ] Domain URL accessible
- [ ] SSL working (https)

---

## 🎯 QUICK ANSWERS

**Q: Is server running?**
```bash
ps aux | grep java | grep tanishq
```

**Q: Is port open?**
```bash
netstat -tlnp | grep 3002
```

**Q: Can I access from Windows?**
```cmd
curl http://10.160.128.94:3002
```

**Q: What's the URL?**
```
IP:   http://10.160.128.94:3002
Domain: (check with your team if configured)
```

---

## 📚 MORE DETAILS

See **PREPROD_SERVER_TESTING.md** for:
- Complete integration tests
- Domain/DNS configuration
- SSL certificate setup
- Troubleshooting guide
- All API endpoint tests

---

**RUN THE SERVER CHECK NOW!** 🚀

Copy the first big bash script and paste it on your server!

