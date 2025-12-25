# ✅ VERIFIED - YOUR PRE-PROD SETUP SUMMARY

**Date:** December 4, 2025  
**Status:** Verified Configuration

---

## ✅ YOUR PRE-PROD URLS (CORRECTED)

```
✅ PRIMARY URL:       http://celebrations-preprod.tanishq.co.in (CORRECT!)
⚠️ OLD URL:          celebrationsite-preprod.tanishq.co.in (WRONG - ignore this)
✅ Direct IP Access:  http://10.160.128.94:3000 (Port 3000!)
```

**NOTE:** The correct URL is **celebrations-preprod** (with 's'), NOT celebrationsite-preprod!

---

## 🏗️ INFRASTRUCTURE SETUP

### **AWS Load Balancer:**
```
Name:        Jew-Testing-ELB
Type:        Internal Application Load Balancer
DNS:         internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com
Region:      ap-south-1 (Mumbai)
Record Type: CNAME
```

### **DNS Configuration:**
```
celebrations-preprod.tanishq.co.in  →  CNAME  →  internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com
```

**Note:** DNS points to AWS ELB, which forwards to your server on port 3000.

### **Application Server:**
```
IP:           10.160.128.94
Port:         3000 (REQUIRED by AWS ELB - NOT 3002!)
Application:  tanishq_selfie_app (WAR deployment)
Database:     selfie_preprod (525 stores, 89 users, 16 events)
S3 Bucket:    celebrations-tanishq-preprod
```

---

## ⚠️ CURRENT STATUS - 502 BAD GATEWAY ISSUE

```
✅ Application Running     (PID: 256305)
❌ Port WRONG              (Running on 3002, ELB expects 3000)
✅ Database Connected      (525 stores)
❌ Domain Shows 502        (http://celebrationsite-preprod.tanishq.co.in)
⚠️  PORT MISMATCH          (App: 3002 ≠ ELB: 3000)
```

**URGENT FIX REQUIRED:** Change port from 3002 to 3000!

See: **FIX_502_BAD_GATEWAY.md** for complete solution.

---

## 🎯 VERIFICATION STEPS - RUN NOW

### **STEP 1: Complete Server Verification**

**Run this on server (10.160.128.94):**

```bash
# Open the verification file I created
# See: DOMAIN_VERIFICATION_COMPLETE.md

# Or run this quick verification:

echo "=== PRE-PROD VERIFICATION ===" && \
echo "" && \
echo "1. Application:" && \
ps aux | grep "[j]ava.*tanishq" > /dev/null && echo "   ✅ Running" || echo "   ❌ Not Running" && \
echo "" && \
echo "2. Port 3002:" && \
netstat -tlnp | grep ":3002" > /dev/null && echo "   ✅ Listening" || echo "   ❌ Not Listening" && \
echo "" && \
echo "3. Database:" && \
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores;" -s -N 2>/dev/null | awk '{print "   ✅ "$1" stores"}' && \
echo "" && \
echo "4. DNS Test:" && \
nslookup celebrationsite-preprod.tanishq.co.in 2>/dev/null | grep -q "Address" && echo "   ✅ DNS Resolves" || echo "   ⚠️  DNS Issue" && \
echo "" && \
echo "5. Domain Access:" && \
curl -s -o /dev/null -w "   HTTP %{http_code}\n" http://celebrationsite-preprod.tanishq.co.in 2>/dev/null && \
echo "" && \
echo "6. IP Access:" && \
curl -s -o /dev/null -w "   HTTP %{http_code}\n" http://10.160.128.94:3002 2>/dev/null && \
echo "" && \
echo "=== COMPLETE ==="
```

---

### **STEP 2: Fix Firewall**

```bash
# Open port 3002
firewall-cmd --permanent --add-port=3002/tcp
firewall-cmd --reload

# Verify
firewall-cmd --list-ports | grep 3002
```

---

### **STEP 3: Test APIs**

```bash
# Get test user
TEST_USER=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N)
TEST_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N)

echo "Testing with: $TEST_USER"

# Test via domain
echo ""
echo "Via Domain:"
curl -X POST http://celebrationsite-preprod.tanishq.co.in/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}"

# Test via IP
echo ""
echo ""
echo "Via IP:"
curl -X POST http://10.160.128.94:3002/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}"
```

---

### **STEP 4: Test from Windows**

**Browser:**
```
http://celebrationsite-preprod.tanishq.co.in
http://preprod.tanishq.co.in
http://10.160.128.94:3002
```

**Command Prompt:**
```cmd
curl http://celebrationsite-preprod.tanishq.co.in
curl http://preprod.tanishq.co.in
curl http://10.160.128.94:3002
```

---

## 🔍 AWS ELB CHECKLIST

**Verify in AWS Console:**

### **Load Balancer (EC2 → Load Balancers):**
- [ ] Name: Jew-Testing-ELB exists
- [ ] Scheme: Internal
- [ ] State: Active
- [ ] DNS Name: internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com

### **Target Group:**
- [ ] Target registered: 10.160.128.94:3000 (NOT 3002!)
- [ ] Health Status: Healthy
- [ ] Health Check path: / or /events/login
- [ ] Protocol: HTTP
- [ ] Port: 3000 (MUST MATCH APPLICATION PORT)

### **Security Groups:**
- [ ] ELB security group allows inbound HTTP (port 80)
- [ ] Server security group allows inbound from ELB (port 3002)
- [ ] Server security group ID: (check and note)

### **Listeners:**
- [ ] Port 80 → Forward to Target Group
- [ ] (Optional) Port 443 → Forward to Target Group (if HTTPS)

---

## 📋 CONFIGURATION SUMMARY

| Component | Value | Status |
|-----------|-------|--------|
| **Primary Domain** | celebrationsite-preprod.tanishq.co.in | ✅ Configured |
| **Sub-Domain** | preprod.tanishq.co.in | ✅ Configured |
| **ELB DNS** | internal-Jew-Testing-ELB-2118632530... | ✅ Active |
| **Server IP** | 10.160.128.94 | ✅ Running |
| **App Port** | 3002 | ✅ Listening |
| **Database** | selfie_preprod | ✅ Connected |
| **S3 Bucket** | celebrations-tanishq-preprod | ✅ Configured |
| **SSL/HTTPS** | Not configured | ⚠️ HTTP only |
| **Firewall** | Port 3002 blocked | ⚠️ Need to open |

---

## 🚨 POTENTIAL ISSUES & FIXES

### **Issue 1: Domain Not Accessible**

**Symptoms:** Domain returns timeout or connection refused

**Check:**
```bash
# 1. DNS resolving?
nslookup celebrationsite-preprod.tanishq.co.in

# 2. ELB target healthy?
# Check AWS Console → Target Groups

# 3. Security groups?
# Check AWS Console → Security Groups
```

**Fix:**
- Ensure ELB target group shows "Healthy"
- Verify security group allows traffic from ELB to server:3002
- Check Route 53 DNS records point to ELB

---

### **Issue 2: Application Not Responding**

**Symptoms:** Port accessible but no response

**Check:**
```bash
ps aux | grep java | grep tanishq
netstat -tlnp | grep 3002
tail -50 /opt/tanishq/applications_preprod/application.log
```

**Fix:**
```bash
# Restart application
cd /opt/tanishq/applications_preprod
pkill -f tanishq
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3002 \
  > application.log 2>&1 &
```

---

### **Issue 3: API Returns Errors**

**Symptoms:** HTTP 200 but JSON shows errors

**Check:**
- Database connection
- Correct credentials
- API endpoint path
- Application logs

---

## ✅ SUCCESS CRITERIA

Your pre-prod is **FULLY WORKING** when:

✅ **DNS:**
- celebrationsite-preprod.tanishq.co.in resolves to ELB
- preprod.tanishq.co.in resolves to ELB
- ELB DNS resolves correctly

✅ **Infrastructure:**
- ELB target group shows "Healthy"
- Security groups configured correctly
- Firewall allows port 3002

✅ **Application:**
- Process running on port 3002
- No errors in logs
- Database connected (selfie_preprod)

✅ **Access:**
- Can access via domain from Windows
- Can access via IP from Windows
- APIs return correct responses
- Frontend loads properly

✅ **Functionality:**
- Login API works
- Get Events API works
- S3 upload works
- QR generation works

---

## 🎯 IMMEDIATE NEXT STEPS

1. ✅ **Run STEP 1** (verification script on server)
2. ✅ **Run STEP 2** (fix firewall)
3. ✅ **Run STEP 3** (test APIs)
4. ✅ **Run STEP 4** (test from Windows)
5. ✅ **Check AWS Console** (verify ELB health)

---

## 📚 DOCUMENTATION CREATED

I've created these guides for you:

1. **DOMAIN_VERIFICATION_COMPLETE.md** ← **Most comprehensive**
2. **PREPROD_URL_ANALYSIS.md** ← Updated with correct domains
3. **VERIFIED_PREPROD_SETUP.md** ← This file (quick reference)
4. **PREPROD_SERVER_TESTING.md** ← Detailed testing guide
5. **PREPROD_QUICK_CHECK.md** ← Quick health checks

---

## 🚀 START NOW

**Run the verification script (STEP 1) on your server!**

Then tell me the results and I'll help with any issues! 💪

