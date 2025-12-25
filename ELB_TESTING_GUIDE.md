# 🧪 ELB CONFIGURATION TESTING GUIDE

**Date:** December 3, 2025  
**Purpose:** Test AWS ELB configuration for celebrationsite-preprod.tanishq.co.in

---

## 🎯 WHAT IS THE ELB ISSUE?

Your domain `celebrationsite-preprod.tanishq.co.in` is pointing to an **Elastic Load Balancer (ELB)** that routes traffic to the **WRONG servers**.

### Current Configuration (WRONG):

```
Domain: celebrationsite-preprod.tanishq.co.in
    ↓
DNS resolves to: internal-jew-testing-elb-2118632530.ap-south-1.elb.amazonaws.com
    ↓
ELB routes to: 10.160.128.79 or 10.160.128.117 ❌ WRONG SERVERS!
    ↓
Result: Connection fails (servers not running your app)
```

### Required Configuration (CORRECT):

```
Domain: celebrationsite-preprod.tanishq.co.in
    ↓
DNS resolves to: internal-jew-testing-elb-2118632530.ap-south-1.elb.amazonaws.com
    ↓
ELB routes to: 10.160.128.94 ✅ YOUR SERVER
    ↓
Result: Application accessible!
```

---

## 🔍 STEP 1: VERIFY DNS RESOLUTION

### **On Your Windows Machine:**

```cmd
nslookup celebrationsite-preprod.tanishq.co.in
```

**Expected Output:**
```
Server:  TCLBLRCORPDC02.titan.com
Address: 172.25.6.22

Non-authoritative answer:
Name:    internal-jew-testing-elb-2118632530.ap-south-1.elb.amazonaws.com
Addresses:  10.160.128.117  ← WRONG!
            10.160.128.79   ← WRONG!
Aliases:  celebrationsite-preprod.tanishq.co.in
```

**What this shows:**
- ✅ DNS is configured (domain exists)
- ✅ Points to ELB
- ❌ But ELB routes to WRONG IPs (not your server 10.160.128.94)

---

## 🔍 STEP 2: TEST ELB TARGET SERVERS

### **On Server (10.160.128.94):**

```bash
# Test if YOUR server is in the target group
curl -I http://10.160.128.94

# Test if WRONG servers are running anything
curl -I http://10.160.128.79
curl -I http://10.160.128.117
```

**Expected Results:**

**Your Server (10.160.128.94):**
```bash
curl -I http://10.160.128.94

# Should return:
HTTP/1.1 200 OK
Server: nginx/1.26.3
Content-Type: text/html;charset=UTF-8
Content-Length: 34455  ← Your app!
```

**Wrong Servers (79 and 117):**
```bash
curl -I http://10.160.128.79

# Will likely return:
curl: (7) Failed to connect to 10.160.128.79 port 80: Connection refused
# OR
curl: (28) Connection timed out
```

**This proves:**
- ✅ Your server (94) is working
- ❌ Wrong servers (79, 117) are NOT working
- ❌ ELB is routing to dead servers!

---

## 🔍 STEP 3: TEST DIRECT ELB ACCESS

### **From Server:**

```bash
# Get ELB DNS name
ELB_DNS="internal-jew-testing-elb-2118632530.ap-south-1.elb.amazonaws.com"

# Try to access ELB directly
curl -I http://$ELB_DNS

# Or with the actual name
curl -I http://internal-jew-testing-elb-2118632530.ap-south-1.elb.amazonaws.com
```

**Expected Result (Currently):**
```
curl: (7) Failed to connect
# OR
HTTP/1.1 502 Bad Gateway
# OR
Connection timeout
```

**Why?** ELB is routing to dead servers (79, 117) so it can't reach your app.

---

## 🔍 STEP 4: CHECK ELB CONFIGURATION (AWS CLI)

### **On Server (as root):**

```bash
# Set AWS CLI path
export PATH=/usr/local/aws-cli/v2/current/bin:$PATH

# Verify AWS credentials
aws sts get-caller-identity

# List all load balancers
aws elbv2 describe-load-balancers --region ap-south-1

# Get specific ELB details (Classic LB)
aws elb describe-load-balancers --region ap-south-1 \
  --load-balancer-names jew-testing-elb

# Get target groups (Application/Network LB)
aws elbv2 describe-target-groups --region ap-south-1

# Get targets in target group (replace with actual ARN)
aws elbv2 describe-target-health \
  --target-group-arn arn:aws:elasticloadbalancing:ap-south-1:099296757009:targetgroup/xxx/xxx \
  --region ap-south-1
```

**What to look for:**
```json
{
  "TargetHealthDescriptions": [
    {
      "Target": {
        "Id": "i-xxxxx",  // Instance ID
        "Port": 80
      },
      "TargetHealth": {
        "State": "healthy" or "unhealthy"
      }
    }
  ]
}
```

**Check if:**
- ✅ Target group exists
- ❌ Contains instances 10.160.128.79 or 10.160.128.117 (wrong!)
- ❌ Does NOT contain 10.160.128.94 (your server)

---

## 🔍 STEP 5: IDENTIFY YOUR INSTANCE ID

### **On Server:**

```bash
# Get your instance ID
curl http://169.254.169.254/latest/meta-data/instance-id

# Example output: i-06c4809f608a9dc09
```

**Save this!** You'll need it to add to the target group.

---

## 🔍 STEP 6: COMPREHENSIVE ELB DIAGNOSTIC

### **Complete Test Script (Run on Server):**

```bash
#!/bin/bash

echo "=================================="
echo "ELB CONFIGURATION DIAGNOSTIC"
echo "=================================="
echo ""

# Set AWS CLI path
export PATH=/usr/local/aws-cli/v2/current/bin:$PATH

# Your details
YOUR_IP="10.160.128.94"
WRONG_IP1="10.160.128.79"
WRONG_IP2="10.160.128.117"
DOMAIN="celebrationsite-preprod.tanishq.co.in"

echo "1. Testing YOUR server ($YOUR_IP):"
curl -I http://$YOUR_IP 2>&1 | head -5
echo ""

echo "2. Testing WRONG server 1 ($WRONG_IP1):"
curl -I http://$WRONG_IP1 2>&1 | head -5
echo ""

echo "3. Testing WRONG server 2 ($WRONG_IP2):"
curl -I http://$WRONG_IP2 2>&1 | head -5
echo ""

echo "4. DNS Resolution for domain:"
nslookup $DOMAIN | grep -A 5 "Name:"
echo ""

echo "5. Your Instance ID:"
curl -s http://169.254.169.254/latest/meta-data/instance-id
echo ""
echo ""

echo "6. Your Instance Private IP:"
curl -s http://169.254.169.254/latest/meta-data/local-ipv4
echo ""
echo ""

echo "7. List all Load Balancers:"
aws elb describe-load-balancers --region ap-south-1 --query 'LoadBalancerDescriptions[*].[LoadBalancerName,DNSName]' --output table
echo ""

echo "8. List all Target Groups:"
aws elbv2 describe-target-groups --region ap-south-1 --query 'TargetGroups[*].[TargetGroupName,TargetGroupArn]' --output table
echo ""

echo "=================================="
echo "DIAGNOSTIC COMPLETE"
echo "=================================="
```

**Save this as `elb-diagnostic.sh` and run:**

```bash
chmod +x elb-diagnostic.sh
./elb-diagnostic.sh > elb-diagnostic-output.txt
cat elb-diagnostic-output.txt
```

---

## 🔍 STEP 7: PING TEST FROM SERVER

### **Test Network Connectivity:**

```bash
# Ping wrong servers
ping -c 4 10.160.128.79
ping -c 4 10.160.128.117

# Ping your own IP (loopback test)
ping -c 4 10.160.128.94

# Check if servers are in same subnet
ip route get 10.160.128.79
ip route get 10.160.128.117
ip route get 10.160.128.94
```

**Expected:**
```
# Wrong servers - likely timeout or unreachable
ping: 10.160.128.79: No route to host
# OR
100% packet loss

# Your server - should work
4 packets transmitted, 4 received, 0% packet loss
```

---

## 🔍 STEP 8: CHECK FROM BROWSER (Windows)

### **Test Domain Access:**

1. **Open browser**
2. **Try to access:** `http://celebrationsite-preprod.tanishq.co.in`

**Expected Result (Before Fix):**
```
This site can't be reached
celebrationsite-preprod.tanishq.co.in took too long to respond
ERR_CONNECTION_TIMED_OUT
```

**Why?** ELB is routing to dead servers (79, 117).

---

## 📊 EVIDENCE COLLECTION

### **Create Evidence Report:**

```bash
# On server, create diagnostic report
cat > elb-evidence-report.txt << 'EOF'
ELB CONFIGURATION EVIDENCE REPORT
Date: $(date)
Server: 10.160.128.94

=== 1. YOUR SERVER STATUS ===
EOF

curl -I http://10.160.128.94 >> elb-evidence-report.txt 2>&1

cat >> elb-evidence-report.txt << 'EOF'

=== 2. WRONG SERVER 1 (10.160.128.79) STATUS ===
EOF

curl -I http://10.160.128.79 >> elb-evidence-report.txt 2>&1

cat >> elb-evidence-report.txt << 'EOF'

=== 3. WRONG SERVER 2 (10.160.128.117) STATUS ===
EOF

curl -I http://10.160.128.117 >> elb-evidence-report.txt 2>&1

cat >> elb-evidence-report.txt << 'EOF'

=== 4. DNS RESOLUTION ===
EOF

nslookup celebrationsite-preprod.tanishq.co.in >> elb-evidence-report.txt 2>&1

cat >> elb-evidence-report.txt << 'EOF'

=== 5. INSTANCE DETAILS ===
EOF

echo "Instance ID: $(curl -s http://169.254.169.254/latest/meta-data/instance-id)" >> elb-evidence-report.txt
echo "Private IP: $(curl -s http://169.254.169.254/latest/meta-data/local-ipv4)" >> elb-evidence-report.txt

cat elb-evidence-report.txt
```

**Send this report to network team!**

---

## ✅ VERIFICATION CHECKLIST

Use this to verify ELB configuration:

### **Before Network Team Fixes:**

- [ ] DNS resolves to ELB ✅ (working)
- [ ] ELB targets 10.160.128.79 ❌ (wrong)
- [ ] ELB targets 10.160.128.117 ❌ (wrong)
- [ ] ELB targets 10.160.128.94 ❌ (missing!)
- [ ] Your server accessible via direct IP ✅ (working)
- [ ] Domain accessible via ELB ❌ (broken)

### **After Network Team Fixes:**

- [ ] DNS resolves to ELB ✅
- [ ] ELB targets 10.160.128.94 ✅
- [ ] Your server healthy in target group ✅
- [ ] Domain accessible via ELB ✅
- [ ] Application loads correctly ✅

---

## 🔧 WHAT NETWORK TEAM NEEDS TO DO

### **Required Changes:**

**Option 1: Classic Load Balancer**
```bash
# Remove old instances
aws elb deregister-instances-from-load-balancer \
  --load-balancer-name jew-testing-elb \
  --instances i-OLDINSTANCE1 i-OLDINSTANCE2 \
  --region ap-south-1

# Add your instance
aws elb register-instances-with-load-balancer \
  --load-balancer-name jew-testing-elb \
  --instances i-06c4809f608a9dc09 \
  --region ap-south-1
```

**Option 2: Application/Network Load Balancer**
```bash
# Get target group ARN
aws elbv2 describe-target-groups --region ap-south-1

# Register your instance
aws elbv2 register-targets \
  --target-group-arn arn:aws:elasticloadbalancing:ap-south-1:099296757009:targetgroup/xxx/xxx \
  --targets Id=i-06c4809f608a9dc09,Port=80 \
  --region ap-south-1

# Deregister old instances
aws elbv2 deregister-targets \
  --target-group-arn arn:aws:elasticloadbalancing:ap-south-1:099296757009:targetgroup/xxx/xxx \
  --targets Id=i-OLDINSTANCE1 Id=i-OLDINSTANCE2 \
  --region ap-south-1
```

---

## 📧 EMAIL TO NETWORK TEAM

**Subject:** URGENT: ELB Target Group Configuration - Wrong Servers

**Body:**

Hi Network Team,

The ELB for `celebrationsite-preprod.tanishq.co.in` is configured with incorrect target servers.

**Issue:**
- ELB: internal-jew-testing-elb-2118632530.ap-south-1.elb.amazonaws.com
- Current Targets: 10.160.128.79, 10.160.128.117 (NOT working - dead servers)
- Required Target: 10.160.128.94 (my pre-prod server - WORKING)

**Instance Details:**
- Instance ID: i-06c4809f608a9dc09
- Private IP: 10.160.128.94
- Port: 80
- Health Check Path: / (root)

**Evidence:**
```
✅ My server (10.160.128.94) - WORKING
$ curl -I http://10.160.128.94
HTTP/1.1 200 OK (returns application)

❌ Current targets - NOT WORKING
$ curl -I http://10.160.128.79
Connection timeout (server not running)

$ curl -I http://10.160.128.117
Connection refused (server not running)
```

**Required Action:**
Please update the ELB target group to:
1. Remove targets: 10.160.128.79, 10.160.128.117
2. Add target: 10.160.128.94:80 (Instance: i-06c4809f608a9dc09)

**Diagnostic Report:** (attached elb-evidence-report.txt)

Please confirm once updated so I can verify domain access.

Thanks!

---

## 🧪 POST-FIX VERIFICATION

### **After Network Team Updates:**

```bash
# Wait 2-3 minutes for changes to propagate

# 1. Test domain
curl -I http://celebrationsite-preprod.tanishq.co.in

# Expected: HTTP/1.1 200 OK with your app content

# 2. Verify DNS still resolves correctly
nslookup celebrationsite-preprod.tanishq.co.in

# Should show your IP: 10.160.128.94

# 3. Test in browser
# Open: http://celebrationsite-preprod.tanishq.co.in
# Should load your Tanishq Celebrations app!

# 4. Verify target health
aws elbv2 describe-target-health \
  --target-group-arn YOUR_TARGET_GROUP_ARN \
  --region ap-south-1

# Should show:
# "State": "healthy"
# "Target": { "Id": "i-06c4809f608a9dc09" }
```

---

## 🎯 QUICK TEST COMMANDS

**Run these commands in sequence:**

```bash
# Test 1: Your server works
curl -I http://10.160.128.94
# Expected: 200 OK

# Test 2: Wrong servers don't work
curl -I http://10.160.128.79
# Expected: Timeout/Connection refused

# Test 3: DNS resolves to ELB
nslookup celebrationsite-preprod.tanishq.co.in
# Expected: Points to ELB (but wrong targets)

# Test 4: Domain doesn't work (yet)
curl -I http://celebrationsite-preprod.tanishq.co.in
# Expected: Timeout (because ELB routes to wrong servers)

# Test 5: Get your instance ID
curl http://169.254.169.254/latest/meta-data/instance-id
# Expected: i-06c4809f608a9dc09
```

---

## 🎊 SUMMARY

### **Current State:**

```
✅ DNS configured correctly (points to ELB)
✅ Your server (10.160.128.94) working perfectly
❌ ELB has wrong targets (79, 117 - dead servers)
❌ Domain not accessible (ELB routing fails)
```

### **What You Need:**

Network team to update ELB target group:
- **Remove:** 10.160.128.79, 10.160.128.117
- **Add:** 10.160.128.94 (Instance: i-06c4809f608a9dc09)

### **How to Test:**

1. Run diagnostic script above
2. Collect evidence report
3. Send to network team with email template
4. Wait for confirmation
5. Test domain access again
6. ✅ Done!

---

**Run the diagnostic commands above and send the evidence report to your network team!** 🚀


