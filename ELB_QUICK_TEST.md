# ⚡ ELB TESTING - QUICK COMMANDS

**Copy and paste these commands on your server to test ELB configuration**

---

## 🔥 IMMEDIATE TESTS (Run in PuTTY)

### **Test 1: Check Your Server**
```bash
curl -I http://10.160.128.94
```
**Expected:** `HTTP/1.1 200 OK` with `Content-Length: 34455` ✅

---

### **Test 2: Check Wrong Servers**
```bash
# Test first wrong server
curl -I http://10.160.128.79

# Test second wrong server  
curl -I http://10.160.128.117
```
**Expected:** `Connection timeout` or `Connection refused` ❌

---

### **Test 3: Get Your Instance ID**
```bash
curl http://169.254.169.254/latest/meta-data/instance-id
```
**Expected:** `i-06c4809f608a9dc09` ✅

---

### **Test 4: Check DNS Resolution**
```bash
nslookup celebrationsite-preprod.tanishq.co.in
```
**Expected:** Shows IPs `10.160.128.79` and `10.160.128.117` ❌ (wrong!)

---

### **Test 5: Try to Access Domain**
```bash
curl -I http://celebrationsite-preprod.tanishq.co.in
```
**Expected:** Timeout or error ❌ (because ELB routes to wrong servers)

---

## 📊 GENERATE EVIDENCE REPORT

### **Copy/Paste This Entire Block:**

```bash
echo "=== ELB DIAGNOSTIC REPORT ===" > elb-test.txt
echo "Date: $(date)" >> elb-test.txt
echo "" >> elb-test.txt

echo "1. YOUR SERVER (10.160.128.94):" >> elb-test.txt
curl -I http://10.160.128.94 >> elb-test.txt 2>&1
echo "" >> elb-test.txt

echo "2. WRONG SERVER 1 (10.160.128.79):" >> elb-test.txt
timeout 5 curl -I http://10.160.128.79 >> elb-test.txt 2>&1
echo "" >> elb-test.txt

echo "3. WRONG SERVER 2 (10.160.128.117):" >> elb-test.txt
timeout 5 curl -I http://10.160.128.117 >> elb-test.txt 2>&1
echo "" >> elb-test.txt

echo "4. DNS RESOLUTION:" >> elb-test.txt
nslookup celebrationsite-preprod.tanishq.co.in >> elb-test.txt 2>&1
echo "" >> elb-test.txt

echo "5. INSTANCE ID:" >> elb-test.txt
curl -s http://169.254.169.254/latest/meta-data/instance-id >> elb-test.txt
echo "" >> elb-test.txt

echo "6. PRIVATE IP:" >> elb-test.txt
curl -s http://169.254.169.254/latest/meta-data/local-ipv4 >> elb-test.txt
echo "" >> elb-test.txt

echo "=== END REPORT ===" >> elb-test.txt

# Display the report
cat elb-test.txt
```

**This creates file:** `elb-test.txt`

---

## 📤 VIEW THE REPORT

```bash
cat elb-test.txt
```

**Send this output to your network team!**

---

## 🔍 AWS CLI TESTS (If you have permissions)

### **Set AWS CLI Path:**
```bash
export PATH=/usr/local/aws-cli/v2/current/bin:$PATH
```

### **Verify AWS Access:**
```bash
aws sts get-caller-identity
```
**Expected:** Shows your AWS account info ✅

### **List Load Balancers:**
```bash
aws elb describe-load-balancers --region ap-south-1 | grep -A 20 "jew-testing"
```

### **List Target Groups:**
```bash
aws elbv2 describe-target-groups --region ap-south-1
```

---

## ✅ EXPECTED RESULTS

### **What You'll Find:**

| Test | Current Result | After Fix |
|------|---------------|-----------|
| Your server (94) | ✅ 200 OK | ✅ 200 OK |
| Wrong server (79) | ❌ Timeout | N/A |
| Wrong server (117) | ❌ Timeout | N/A |
| DNS resolution | Shows 79, 117 ❌ | Shows 94 ✅ |
| Domain access | ❌ Timeout | ✅ 200 OK |

---

## 📧 EMAIL TEMPLATE

**After running tests, send this email:**

---

**To:** network.team@titan.co.in, anna.mariya@titan.co.in  
**Subject:** URGENT: ELB Misconfiguration - celebrationsite-preprod.tanishq.co.in

**Body:**

Hi Team,

The ELB for celebrationsite-preprod.tanishq.co.in is routing to incorrect servers.

**Problem:**
- ELB: internal-jew-testing-elb-2118632530.ap-south-1.elb.amazonaws.com
- Current targets: 10.160.128.79, 10.160.128.117 (both OFFLINE/NOT WORKING)
- Required target: 10.160.128.94 (my pre-prod server - ONLINE and WORKING)

**Evidence:**

✅ **My Server Works:**
```
$ curl -I http://10.160.128.94
HTTP/1.1 200 OK
Server: nginx/1.26.3
Content-Length: 34455 (application running)
```

❌ **Current ELB Targets Don't Work:**
```
$ curl -I http://10.160.128.79
curl: (7) Failed to connect (server offline)

$ curl -I http://10.160.128.117  
curl: (7) Failed to connect (server offline)
```

**My Instance Details:**
- Instance ID: i-06c4809f608a9dc09
- Private IP: 10.160.128.94
- Port: 80
- Application: Running and healthy

**Required Action:**
Please update the ELB target group to:
1. **Remove:** 10.160.128.79, 10.160.128.117 (dead servers)
2. **Add:** 10.160.128.94:80 (Instance: i-06c4809f608a9dc09)

**Attached:** elb-test.txt (diagnostic report)

Please confirm once updated.

Thanks!

---

## 🎯 ONE-LINE TESTS

```bash
# All tests in one command
echo "Your server:"; curl -I http://10.160.128.94 2>&1 | head -3; echo ""; echo "Wrong server 1:"; timeout 3 curl -I http://10.160.128.79 2>&1 | head -3; echo ""; echo "Wrong server 2:"; timeout 3 curl -I http://10.160.128.117 2>&1 | head -3
```

---

## 🔄 AFTER NETWORK TEAM FIXES

### **Verification Commands:**

```bash
# Test 1: Domain should work now
curl -I http://celebrationsite-preprod.tanishq.co.in

# Expected: HTTP/1.1 200 OK with your app

# Test 2: DNS should show your IP
nslookup celebrationsite-preprod.tanishq.co.in

# Expected: Should show 10.160.128.94

# Test 3: Browser test
# Open: http://celebrationsite-preprod.tanishq.co.in
# Should load your Tanishq app!
```

---

## ⏱️ TIMELINE

1. **Run tests:** 2 minutes
2. **Generate report:** 1 minute  
3. **Send email:** 2 minutes
4. **Network team fix:** 10-30 minutes
5. **Verification:** 2 minutes

**Total:** ~20-40 minutes from start to finish

---

**RUN THE TESTS NOW AND SEND THE EMAIL!** 🚀


