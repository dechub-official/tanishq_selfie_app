# Fix firewall
firewall-cmd --permanent --add-port=3002/tcp
firewall-cmd --reload

# Test login API with real user
TEST_USER=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N)
TEST_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N)

echo "Testing with user: $TEST_USER"

curl -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}"

echo ""
echo ""
echo "Access URL: http://10.160.128.94:3002"
```

**From Windows:**

```cmd
REM Test after firewall is open
curl http://10.160.128.94:3002

REM Or open browser to:
start http://10.160.128.94:3002
```

---

## 📊 SUMMARY

| Item | Status | URL/Value |
|------|--------|-----------|
| **Application** | ✅ Running | PID 256305 |
| **Port** | ✅ Listening | 3002 |
| **Database** | ✅ Connected | 525 stores |
| **Frontend** | ✅ Working | http://10.160.128.94:3002 |
| **API** | ⚠️ Need to test | /events/login |
| **Firewall** | ❌ Blocked | Need to open port 3002 |
| **Domain** | ❓ Not configured | Use IP for now |
| **Production URL** | ℹ️ Found in HTML | https://celebrations.tanishq.co.in |
| **Pre-Prod URL** | ❓ Ask team | Or use http://10.160.128.94:3002 |

---

## 🎯 IMMEDIATE ACTIONS

1. ✅ **Open firewall** (run the firewall command above)
2. ✅ **Test API** (run the login test above)
3. ✅ **Ask team** for preprod domain name
4. ✅ **Test from Windows** after firewall is open

---

**DO YOU WANT ME TO:**

A. Help you open the firewall and test APIs? ✅  
B. Help you set up a domain with Nginx? 🌐  
C. Help you update the HTML URLs? 📝  
D. Just test with IP address for now? ⚡

**Tell me which option and I'll guide you!**
# ✅ YOUR PRE-PROD SERVER STATUS - ANALYSIS

## 🎉 GOOD NEWS - SERVER IS WORKING!

Based on your output:

```
=== PRE-PROD STATUS ===

1. App Process:
   ✅ Running (PID: 256305)

2. Port 3002:
   ✅ Listening

3. Database:
   ✅ Connected - 525 stores

4. API Test:
   ⚠️  API Issue  <- This needs investigation

5. Firewall:
   ⚠️  Port 3002 Not in Firewall  <- Need to open

6. Access URLs:
   Local:   http://localhost:3002
   Network: http://10.160.128.94:3002
```

---

## 🌐 YOUR PRE-PROD URL ANALYSIS

When you accessed `http://10.160.128.94:3002`, you got:

```html
<!doctype html>
<html lang="en">
  <head>
    <title>Celebrate</title>
```

**This is the FRONTEND HTML page - NOT the API!** ✅

### **What This Means:**

1. ✅ **Your application IS running successfully**
2. ✅ **Port 3002 is accessible**
3. ✅ **Frontend is being served**
4. ✅ **The HTML shows links to:**
   - `https://celebrations.tanishq.co.in/checklist`
   - `https://celebrations.tanishq.co.in/selfie`
   - `https://celebrations.tanishq.co.in/events`

---

## 🎯 YOUR ACTUAL DOMAIN URLS - CONFIRMED

**PRODUCTION domain** (from HTML):

```
https://celebrations.tanishq.co.in
```

**PRE-PROD domains** (CONFIRMED):

```
Primary URL:     http://celebrationsite-preprod.tanishq.co.in
Sub-Domain:      http://preprod.tanishq.co.in
DNS Name:        internal-Jew-Testing-ELB-2118632530.ap-south-1.elb.amazonaws.com
Record Type:     CNAME
Load Balancer:   AWS ELB (Elastic Load Balancer)
Region:          ap-south-1 (Mumbai)
Direct IP:       http://10.160.128.94:3002
```

---

## ⚠️ TWO ISSUES TO FIX

### **Issue 1: API Not Working Properly**

The login API test failed. This might be because:
- API endpoints are at different paths
- Need to test correct endpoint

**Let's test the correct API paths:**

```bash
# Test the actual API endpoints your app uses
# Based on the HTML, the APIs are probably at /events/* paths

# Test 1: Login API
curl -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d '{"code":"TEST","password":"Titan@123"}'

# Test 2: Check what's at root
curl http://localhost:3002/api/health

# Test 3: Check events API
curl http://localhost:3002/events/getevents
```

---

### **Issue 2: Firewall Blocking External Access**

Port 3002 is NOT in the firewall, so you **cannot access from outside** (like from Windows PC on different network).

**Fix:**

```bash
# Open port 3002 in firewall
firewall-cmd --permanent --add-port=3002/tcp
firewall-cmd --reload

# Verify
firewall-cmd --list-ports | grep 3002
```

---

## 📋 PRE-PROD URL SETUP - ANSWER TO YOUR QUESTION

**Q: "Did we set up our pre-prod URL in the project?"**

**A: Based on what I see:**

1. **NO domain configured yet** - You're accessing via IP: `http://10.160.128.94:3002`

2. **HTML hardcodes production URLs:**
   ```html
   href="https://celebrations.tanishq.co.in/checklist"
   href="https://celebrations.tanishq.co.in/selfie"
   href="https://celebrations.tanishq.co.in/events"
   ```

3. **You need to either:**
   - Set up DNS for a preprod domain
   - OR configure a reverse proxy (Nginx/Apache) with domain
   - OR use IP address for testing: `http://10.160.128.94:3002`

---

## 🔍 CHECK IF DOMAIN IS CONFIGURED

**Run these commands on server:**

```bash
# Check for Nginx configuration
cat /etc/nginx/sites-enabled/* 2>/dev/null | grep -i "server_name"
cat /etc/nginx/conf.d/*.conf 2>/dev/null | grep -i "server_name"

# Check for Apache configuration
cat /etc/httpd/conf.d/*.conf 2>/dev/null | grep -i "ServerName"

# Check if Nginx is running
systemctl status nginx

# Check if Apache is running
systemctl status httpd

# Check DNS resolution
nslookup celebrations-preprod.tanishq.co.in
nslookup preprod-celebrations.tanishq.co.in
```

---

## 🎯 WHAT TO DO NOW

### **OPTION 1: Use IP Address (Quickest for Testing)**

**Your pre-prod URL is simply:**
```
http://10.160.128.94:3002
```

**Access from:**
- Server: `http://localhost:3002`
- Same network: `http://10.160.128.94:3002`

**Fix firewall first:**
```bash
firewall-cmd --permanent --add-port=3002/tcp
firewall-cmd --reload
```

---

### **OPTION 2: Set Up Domain (For Production-like Testing)**

**Step 1: Get domain from your team**
- Ask: "What is the pre-prod domain for celebrations app?"
- Common formats:
  - `preprod-celebrations.tanishq.co.in`
  - `celebrations-preprod.tanishq.co.in`
  - `preprod.celebrations.tanishq.co.in`

**Step 2: Configure DNS**
- Point domain to `10.160.128.94`

**Step 3: Set up Nginx reverse proxy**
```bash
# Install Nginx if not installed
yum install nginx -y

# Create configuration
cat > /etc/nginx/conf.d/celebrations-preprod.conf << 'EOF'
server {
    listen 80;
    server_name preprod-celebrations.tanishq.co.in;

    location / {
        proxy_pass http://localhost:3002;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

# Test configuration
nginx -t

# Restart Nginx
systemctl restart nginx
systemctl enable nginx

# Open port 80
firewall-cmd --permanent --add-service=http
firewall-cmd --reload
```

---

### **OPTION 3: Update HTML to Use Pre-Prod URLs**

**The HTML currently has production URLs. You might need to:**

1. **Extract the WAR file**
2. **Update the HTML** to use preprod URLs
3. **Repackage the WAR**

**OR**

**Use environment-based configuration** in your build process.

---

## ✅ QUICK TEST - RUN THIS NOW

**On Server:**

```bash

