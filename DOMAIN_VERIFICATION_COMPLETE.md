# ✅ PRE-PROD DOMAIN VERIFICATION

## 🌐 YOUR ACTUAL PRE-PROD DOMAIN DETAILS

**Based on your information:**

```
Primary URL:     celebrationsite-preprod.tanishq.co.in
Sub-Domain:      preprod.tanishq.co.in
DNS Name:        internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com
Record Type:     CNAME
Region:          ap-south-1 (Mumbai)
Load Balancer:   AWS ELB (Elastic Load Balancer)
```

---

## 🔍 COMPLETE VERIFICATION CHECKLIST

### **STEP 1: Verify DNS Configuration**

**On Server, run:**

```bash
echo "================================================"
echo "DNS VERIFICATION"
echo "================================================"

# Check primary domain
echo "1. Primary Domain DNS:"
nslookup celebrationsite-preprod.tanishq.co.in

echo ""
echo "2. Sub-domain DNS:"
nslookup preprod.tanishq.co.in

echo ""
echo "3. Load Balancer DNS:"
nslookup internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com

echo ""
echo "4. Checking if domains resolve to ELB:"
dig celebrationsite-preprod.tanishq.co.in CNAME +short
dig preprod.tanishq.co.in CNAME +short

echo ""
echo "================================================"
```

---

### **STEP 2: Verify Load Balancer Configuration**

**Check ELB targets:**

```bash
echo "================================================"
echo "LOAD BALANCER VERIFICATION"
echo "================================================"

# Check if this server is registered with ELB
echo "Server IP:"
hostname -I

echo ""
echo "ELB should be configured to route to:"
echo "  - Target: 10.160.128.94:3002"
echo "  - Protocol: HTTP"
echo "  - Health Check: /events/login or /"

echo ""
echo "Verify in AWS Console:"
echo "  EC2 > Load Balancers > Jew-Testing-ELB"
echo "  Check Target Groups and Health Status"

echo ""
echo "================================================"
```

---

### **STEP 3: Test Domain Access**

**From Server:**

```bash
echo "================================================"
echo "DOMAIN ACCESS TEST"
echo "================================================"

# Test 1: Primary domain
echo "1. Testing celebrationsite-preprod.tanishq.co.in:"
curl -I http://celebrationsite-preprod.tanishq.co.in 2>/dev/null | head -5

echo ""
echo "2. Testing preprod.tanishq.co.in:"
curl -I http://preprod.tanishq.co.in 2>/dev/null | head -5

echo ""
echo "3. Testing direct to ELB:"
curl -I http://internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com 2>/dev/null | head -5

echo ""
echo "4. Testing direct IP:"
curl -I http://10.160.128.94:3002 2>/dev/null | head -5

echo ""
echo "================================================"
```

---

### **STEP 4: Test APIs via Domain**

```bash
echo "================================================"
echo "API TEST VIA DOMAIN"
echo "================================================"

# Get test credentials
TEST_USER=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N)
TEST_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N)

echo "Test User: $TEST_USER"
echo ""

# Test via primary domain
echo "1. Testing via celebrationsite-preprod.tanishq.co.in:"
curl -X POST http://celebrationsite-preprod.tanishq.co.in/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" \
  2>/dev/null | python3 -m json.tool 2>/dev/null || echo "Failed"

echo ""
echo "2. Testing via preprod.tanishq.co.in:"
curl -X POST http://preprod.tanishq.co.in/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" \
  2>/dev/null | python3 -m json.tool 2>/dev/null || echo "Failed"

echo ""
echo "3. Testing via direct IP:"
curl -X POST http://10.160.128.94:3002/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" \
  2>/dev/null | python3 -m json.tool 2>/dev/null || echo "Failed"

echo ""
echo "================================================"
```

---

### **STEP 5: Check Application Configuration**

```bash
echo "================================================"
echo "APPLICATION CONFIGURATION CHECK"
echo "================================================"

# Check if app knows about the domain
echo "1. Application Process:"
ps aux | grep "[j]ava.*tanishq"

echo ""
echo "2. Application Logs (checking for domain references):"
tail -100 /opt/tanishq/applications_preprod/application.log 2>/dev/null | grep -i "domain\|url\|host" | tail -10

echo ""
echo "3. Check for server configuration:"
# If WAR is deployed to Tomcat, check server.xml
find /opt -name "server.xml" -type f 2>/dev/null | head -1 | xargs cat 2>/dev/null | grep -i "Host\|port" | head -10

echo ""
echo "================================================"
```

---

## 🎯 COMPLETE VERIFICATION SCRIPT - RUN THIS

**Copy this entire block:**

```bash
echo "========================================================"
echo "COMPLETE PRE-PROD DOMAIN VERIFICATION"
echo "Date: $(date)"
echo "========================================================"
echo ""

# 1. DNS Verification
echo "=== 1. DNS VERIFICATION ==="
echo ""
echo "Primary Domain (celebrationsite-preprod.tanishq.co.in):"
nslookup celebrationsite-preprod.tanishq.co.in 2>/dev/null | grep -A2 "Name:" || echo "DNS lookup failed"

echo ""
echo "Sub-domain (preprod.tanishq.co.in):"
nslookup preprod.tanishq.co.in 2>/dev/null | grep -A2 "Name:" || echo "DNS lookup failed"

echo ""
echo "ELB DNS:"
nslookup internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com 2>/dev/null | grep -A5 "Name:" || echo "DNS lookup failed"

# 2. Application Status
echo ""
echo "=== 2. APPLICATION STATUS ==="
ps aux | grep "[j]ava.*tanishq" > /dev/null && echo "✅ Application Running (PID: $(ps aux | grep '[j]ava.*tanishq' | awk '{print $2}' | head -1))" || echo "❌ Application Not Running"

netstat -tlnp 2>/dev/null | grep ":3002" > /dev/null && echo "✅ Port 3002 Listening" || echo "❌ Port 3002 Not Listening"

DB_COUNT=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores;" -s -N 2>/dev/null)
[ -n "$DB_COUNT" ] && echo "✅ Database Connected ($DB_COUNT stores)" || echo "❌ Database Error"

# 3. Firewall Check
echo ""
echo "=== 3. FIREWALL STATUS ==="
firewall-cmd --list-ports 2>/dev/null | grep -q "3002" && echo "✅ Port 3002 Open in Firewall" || echo "⚠️  Port 3002 NOT in Firewall (Need to add)"

firewall-cmd --list-services 2>/dev/null | grep -q "http" && echo "✅ HTTP Service Allowed" || echo "⚠️  HTTP Not Allowed (Need to add)"

# 4. Domain Access Test
echo ""
echo "=== 4. DOMAIN ACCESS TEST ==="

echo "Testing celebrationsite-preprod.tanishq.co.in:"
DOMAIN_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://celebrationsite-preprod.tanishq.co.in 2>/dev/null)
if [ "$DOMAIN_STATUS" = "200" ] || [ "$DOMAIN_STATUS" = "302" ]; then
    echo "✅ Domain accessible (HTTP $DOMAIN_STATUS)"
else
    echo "❌ Domain not accessible (HTTP $DOMAIN_STATUS)"
fi

echo ""
echo "Testing preprod.tanishq.co.in:"
SUBDOMAIN_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://preprod.tanishq.co.in 2>/dev/null)
if [ "$SUBDOMAIN_STATUS" = "200" ] || [ "$SUBDOMAIN_STATUS" = "302" ]; then
    echo "✅ Sub-domain accessible (HTTP $SUBDOMAIN_STATUS)"
else
    echo "❌ Sub-domain not accessible (HTTP $SUBDOMAIN_STATUS)"
fi

echo ""
echo "Testing direct IP (10.160.128.94:3002):"
IP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://10.160.128.94:3002 2>/dev/null)
if [ "$IP_STATUS" = "200" ] || [ "$IP_STATUS" = "302" ]; then
    echo "✅ Direct IP accessible (HTTP $IP_STATUS)"
else
    echo "❌ Direct IP not accessible (HTTP $IP_STATUS)"
fi

# 5. API Test
echo ""
echo "=== 5. API FUNCTIONALITY TEST ==="

TEST_USER=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N 2>/dev/null)
TEST_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N 2>/dev/null)

if [ -n "$TEST_USER" ]; then
    echo "Test User: $TEST_USER"
    echo ""
    
    # Test via domain
    echo "Testing Login API via domain:"
    API_RESULT=$(curl -s -X POST http://celebrationsite-preprod.tanishq.co.in/events/login \
      -H "Content-Type: application/json" \
      -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" 2>/dev/null)
    
    if echo "$API_RESULT" | grep -q '"status":true'; then
        echo "✅ Login API works via domain!"
    else
        echo "⚠️  Login API response: $API_RESULT"
    fi
    
    # Test via IP
    echo ""
    echo "Testing Login API via IP:"
    API_IP_RESULT=$(curl -s -X POST http://10.160.128.94:3002/events/login \
      -H "Content-Type: application/json" \
      -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" 2>/dev/null)
    
    if echo "$API_IP_RESULT" | grep -q '"status":true'; then
        echo "✅ Login API works via IP!"
    else
        echo "⚠️  Login API response: $API_IP_RESULT"
    fi
fi

# 6. SSL/HTTPS Check
echo ""
echo "=== 6. SSL/HTTPS STATUS ==="
netstat -tlnp 2>/dev/null | grep ":443" > /dev/null && echo "✅ HTTPS Port 443 Listening" || echo "ℹ️  HTTPS Not configured (using HTTP only)"

# 7. Load Balancer Check
echo ""
echo "=== 7. LOAD BALANCER CONFIGURATION ==="
echo "ELB Name: Jew-Testing-ELB"
echo "DNS: internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com"
echo "Region: ap-south-1"
echo ""
echo "⚠️  Verify in AWS Console:"
echo "   1. ELB Target Group contains: 10.160.128.94:3002"
echo "   2. Health Check Status: Healthy"
echo "   3. Security Group allows: Port 3002 from ELB"

# Summary
echo ""
echo "========================================================"
echo "SUMMARY"
echo "========================================================"
echo ""
echo "Your Pre-Prod URLs:"
echo "  Primary:    http://celebrationsite-preprod.tanishq.co.in"
echo "  Sub-domain: http://preprod.tanishq.co.in"
echo "  Direct IP:  http://10.160.128.94:3002"
echo ""
echo "Status:"
ps aux | grep -q "[j]ava.*tanishq" && echo "  ✅ Application: Running" || echo "  ❌ Application: Not Running"
[ "$DOMAIN_STATUS" = "200" ] && echo "  ✅ Domain: Accessible" || echo "  ⚠️  Domain: Check ELB/DNS"
[ -n "$DB_COUNT" ] && echo "  ✅ Database: Connected" || echo "  ❌ Database: Error"

echo ""
echo "Next Steps:"
if [ "$DOMAIN_STATUS" != "200" ]; then
    echo "  1. Check AWS ELB target group health status"
    echo "  2. Verify ELB security group allows traffic"
    echo "  3. Verify target group has 10.160.128.94:3002"
fi

if ! firewall-cmd --list-ports 2>/dev/null | grep -q "3002"; then
    echo "  1. Open firewall: firewall-cmd --permanent --add-port=3002/tcp"
    echo "  2. Reload firewall: firewall-cmd --reload"
fi

echo ""
echo "========================================================"
```

---

## 🌐 TEST FROM YOUR WINDOWS PC

**After running the verification above, test from Windows:**

### **Browser Tests:**

```
1. http://celebrationsite-preprod.tanishq.co.in
2. http://preprod.tanishq.co.in
3. http://10.160.128.94:3002
```

### **API Tests (Command Prompt):**

```cmd
REM Test domain
curl http://celebrationsite-preprod.tanishq.co.in/events/login

REM Test sub-domain
curl http://preprod.tanishq.co.in/events/login

REM Test direct IP
curl http://10.160.128.94:3002/events/login
```

---

## 📊 EXPECTED CONFIGURATION

### **DNS Records (should be configured in Route 53 or DNS):**

```
celebrationsite-preprod.tanishq.co.in  →  CNAME  →  internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com
preprod.tanishq.co.in                  →  CNAME  →  internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com
```

### **ELB Configuration (in AWS):**

```
Name: Jew-Testing-ELB
Type: Application Load Balancer (internal)
Scheme: Internal
Region: ap-south-1

Target Group:
  - Target: 10.160.128.94:3002
  - Protocol: HTTP
  - Health Check: /events/login or /

Listeners:
  - Port 80 → Forward to Target Group
```

### **Application Server:**

```
IP: 10.160.128.94
Port: 3002
App: tanishq_selfie_app WAR
Database: selfie_preprod (525 stores)
```

---

## ✅ VERIFICATION CHECKLIST

- [ ] DNS resolves celebrationsite-preprod.tanishq.co.in to ELB
- [ ] DNS resolves preprod.tanishq.co.in to ELB
- [ ] ELB DNS resolves correctly
- [ ] Application running on port 3002
- [ ] Firewall allows port 3002
- [ ] Can access http://10.160.128.94:3002 directly
- [ ] Can access http://celebrationsite-preprod.tanishq.co.in via ELB
- [ ] Can access http://preprod.tanishq.co.in via ELB
- [ ] Login API works via domain
- [ ] Database connected (selfie_preprod)
- [ ] ELB target group shows healthy
- [ ] Security groups configured correctly

---

## 🚨 IF DOMAIN DOESN'T WORK

**Common issues:**

1. **ELB Target Not Healthy:**
   - Check AWS Console → EC2 → Target Groups
   - Verify 10.160.128.94:3002 is registered and healthy
   - Check health check path

2. **Security Group Blocking:**
   - ELB security group must allow traffic from internet
   - Server security group must allow traffic from ELB

3. **Firewall Blocking:**
   ```bash
   firewall-cmd --permanent --add-port=3002/tcp
   firewall-cmd --reload
   ```

4. **Application Not Running:**
   - Check: `ps aux | grep tanishq`
   - Restart if needed

---

## 🎯 RUN THE COMPLETE VERIFICATION NOW

**Copy the big script above (starts with "COMPLETE PRE-PROD DOMAIN VERIFICATION") and run it on the server!**

It will check:
1. ✅ DNS configuration
2. ✅ Application status
3. ✅ Firewall settings
4. ✅ Domain accessibility
5. ✅ API functionality
6. ✅ Load balancer setup

**Then tell me the results!** 🚀

