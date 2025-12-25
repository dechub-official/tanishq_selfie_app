# 🚨 CRITICAL ISSUE - NGINX NOT PROXYING TO YOUR APP

**Date:** December 3, 2025  
**Status:** ⚠️ URGENT - Nginx serving default page instead of your application

---

## 🔍 WHAT'S WRONG

### ✅ What's Working:
- Your application is running on port 3002 ✅
- Direct access to port 3002 works (`curl localhost:3002`) ✅
- DNS resolves `celebrationsite-preprod.tanishq.co.in` ✅
- Port 80 is open ✅

### ❌ What's Broken:
- **Nginx returns default HTML page** instead of proxying to your app
- `curl localhost` returns 5909 bytes HTML (default Nginx page)
- Your app returns 34455 bytes (actual application)
- **Nginx configuration missing or wrong**

### ❗ CRITICAL DISCOVERY:
**ELB is pointing to 10.160.128.79 and 10.160.128.117**  
**Your server is 10.160.128.94**

This means:
1. The network team configured ELB to route to **WRONG servers**
2. Even if you fix Nginx, the domain won't work until ELB is fixed
3. But direct IP (10.160.128.94) should work once Nginx is fixed

---

## 🎯 IMMEDIATE FIXES NEEDED

### FIX 1: Fix Nginx Configuration (YOU CAN DO THIS NOW)

**Time:** 2 minutes

### FIX 2: Contact Network Team About ELB (URGENT EMAIL)

**Time:** 5 minutes

---

## 🔧 FIX 1: NGINX CONFIGURATION

### Step 1: Check Current Nginx Config

**On server (PuTTY):**

```bash
# Check if config file exists
ls -l /etc/nginx/conf.d/celebrations-preprod.conf

# View the config
cat /etc/nginx/conf.d/celebrations-preprod.conf
```

**Expected:** You should see `proxy_pass http://localhost:3002`

**If file doesn't exist or doesn't have proxy_pass, proceed to Step 2**

---

### Step 2: Create Correct Nginx Configuration

**Execute this command on server:**

```bash
# Backup any existing config
cp /etc/nginx/conf.d/celebrations-preprod.conf /etc/nginx/conf.d/celebrations-preprod.conf.backup 2>/dev/null

# Create correct configuration
cat > /etc/nginx/conf.d/celebrations-preprod.conf << 'EOF'
server {
    listen 80;
    server_name celebrationsite-preprod.tanishq.co.in preprod.tanishq.co.in 10.160.128.94 _;

    client_max_body_size 10M;

    # Logging
    access_log /var/log/nginx/celebrations-preprod-access.log;
    error_log /var/log/nginx/celebrations-preprod-error.log;

    location / {
        proxy_pass http://localhost:3002;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        # Timeouts
        proxy_connect_timeout 600;
        proxy_send_timeout 600;
        proxy_read_timeout 600;
        send_timeout 600;
    }
}
EOF

# Verify the file was created
cat /etc/nginx/conf.d/celebrations-preprod.conf
```

---

### Step 3: Enable SELinux Permission for Nginx

```bash
# Allow Nginx to connect to network
setsebool -P httpd_can_network_connect 1

# Verify
getsebool httpd_can_network_connect
```

**Expected:** `httpd_can_network_connect --> on`

---

### Step 4: Check for Conflicting Default Config

```bash
# Check default.conf
ls -l /etc/nginx/conf.d/default.conf

# If it exists, rename it to disable
mv /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.disabled

# List all configs
ls -l /etc/nginx/conf.d/
```

**Only `celebrations-preprod.conf` should be active (*.conf)**

---

### Step 5: Test and Reload Nginx

```bash
# Test configuration
nginx -t
```

**Expected:** `nginx: configuration file /etc/nginx/nginx.conf test is successful`

```bash
# Reload Nginx
systemctl reload nginx

# Check status
systemctl status nginx --no-pager
```

---

### Step 6: Verify Fix Worked

```bash
# Test local access
curl -I http://localhost

# Should now return:
# HTTP/1.1 200 OK
# Server: nginx/1.26.3
# Content-Type: text/html;charset=UTF-8  <-- From your app
# Content-Length: 34455  <-- Your app size (not 5909)
```

**Compare:**
- **Before:** Content-Length: 5909 (Nginx default page)
- **After:** Content-Length: 34455 (Your application) ✅

```bash
# Test with actual HTML
curl http://localhost | head -50

# Should show your Tanishq application HTML
```

---

## 📧 FIX 2: EMAIL TO NETWORK TEAM (URGENT)

**Send this NOW:**

---

**To:** Anna Mariya (Network Team)  
**CC:** Your Manager  
**Subject:** URGENT: ELB Configuration Error - Wrong Target Servers

---

Hi Anna,

Thank you for configuring the DNS for celebrationsite-preprod.tanishq.co.in.

However, I discovered a critical issue with the ELB configuration:

**Problem:**

The ELB `internal-jew-testing-elb-2118632530.ap-south-1.elb.amazonaws.com` is currently routing to:
- 10.160.128.79
- 10.160.128.117

**But my application is running on:**
- **10.160.128.94** ⬅️ Correct server

**Evidence:**

```
C:\Users\Prasanna>nslookup celebrationsite-preprod.tanishq.co.in
Non-authoritative answer:
Name: internal-jew-testing-elb-2118632530.ap-south-1.elb.amazonaws.com
Addresses: 10.160.128.117
           10.160.128.79    <-- Wrong servers
Aliases: celebrationsite-preprod.tanishq.co.in
```

**Required Action:**

Please update the ELB target group to route to the correct server:

- **Server IP:** 10.160.128.94
- **Port:** 80
- **Health Check:** HTTP:80 / (or /actuator/health)

**Current Status:**

✅ Application running on 10.160.128.94:80  
✅ Direct IP access works: http://10.160.128.94  
❌ Domain access fails because ELB routes to wrong servers

**Urgency:** HIGH - Pre-prod environment is ready but inaccessible via domain

Please confirm once the ELB is updated so I can verify.

Thank you!

Regards,  
Nagaraj

---

**SEND THIS EMAIL NOW!**

---

## 🧪 VERIFICATION CHECKLIST

### After Fixing Nginx:

- [ ] `curl -I http://localhost` returns Content-Length: 34455
- [ ] `curl http://localhost` shows Tanishq application HTML
- [ ] `curl http://10.160.128.94` works from external network
- [ ] Nginx logs show no errors: `tail /var/log/nginx/celebrations-preprod-error.log`

### After Network Team Fixes ELB:

- [ ] `nslookup celebrationsite-preprod.tanishq.co.in` shows 10.160.128.94
- [ ] Browser can access http://celebrationsite-preprod.tanishq.co.in
- [ ] All features work on the domain

---

## 📊 TECHNICAL ANALYSIS

### Why Domain Doesn't Work:

```
Browser
   ↓
celebrationsite-preprod.tanishq.co.in (DNS)
   ↓
internal-jew-testing-elb-2118632530... (CNAME)
   ↓
10.160.128.79 or 10.160.128.117 ← ELB routes here
   ↓
❌ WRONG SERVERS (nothing running)

YOUR APP IS ON: 10.160.128.94 ✅
```

### Why IP Works:

```
Browser
   ↓
http://10.160.128.94 (Direct IP)
   ↓
Nginx on port 80
   ↓
proxy_pass to localhost:3002
   ↓
Your Java application ✅
```

---

## 🎯 QUICK COMMAND SUMMARY

**On Server (Copy/Paste):**

```bash
# Fix Nginx configuration
cat > /etc/nginx/conf.d/celebrations-preprod.conf << 'EOF'
server {
    listen 80;
    server_name celebrationsite-preprod.tanishq.co.in preprod.tanishq.co.in 10.160.128.94 _;
    client_max_body_size 10M;
    access_log /var/log/nginx/celebrations-preprod-access.log;
    error_log /var/log/nginx/celebrations-preprod-error.log;
    location / {
        proxy_pass http://localhost:3002;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_connect_timeout 600;
        proxy_send_timeout 600;
        proxy_read_timeout 600;
        send_timeout 600;
    }
}
EOF

# Disable default config
mv /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.disabled 2>/dev/null

# Enable SELinux
setsebool -P httpd_can_network_connect 1

# Test and reload
nginx -t && systemctl reload nginx

# Verify
curl -I http://localhost
curl http://localhost | head -20
```

---

## ⏱️ TIMELINE

| Task | Time | Who |
|------|------|-----|
| Fix Nginx config | 2 min | You (NOW) |
| Send email to network team | 2 min | You (NOW) |
| Network team fixes ELB | ??? | Network team |
| Test and verify | 5 min | You (after ELB fix) |

---

## 🎯 DO THIS NOW:

### Step 1: Fix Nginx (2 minutes)
1. Open PuTTY to server
2. Copy/paste the quick command summary above
3. Verify with `curl -I http://localhost`
4. Should show Content-Length: 34455

### Step 2: Email Network Team (2 minutes)
1. Copy the email template above
2. Send to Anna Mariya
3. CC your manager
4. Wait for confirmation

### Step 3: Test Direct IP (1 minute)
1. Open browser: http://10.160.128.94
2. Should load your Tanishq application ✅

### Step 4: Wait for ELB Fix
1. Network team will update ELB target
2. They will confirm
3. Then test domain access

---

## ✅ SUCCESS CRITERIA

**Phase 1 - Nginx Fixed (YOU CAN COMPLETE NOW):**
- ✅ http://10.160.128.94 works in browser
- ✅ Shows Tanishq Celebrations application
- ✅ All features functional

**Phase 2 - ELB Fixed (NETWORK TEAM):**
- ✅ http://celebrationsite-preprod.tanishq.co.in works in browser
- ✅ Same application loads
- ✅ Ready for UAT testing

---

## 📞 WHO TO CONTACT

**For Nginx Issues:**
- You can fix yourself (commands above)
- Linux team (if permissions issues)

**For ELB/DNS Issues:**
- Anna Mariya (Network Team)
- AWS Team (ELB configuration)

**For Application Issues:**
- Check application logs
- Check database connectivity
- Restart if needed

---

## 🔍 TROUBLESHOOTING

### Problem: nginx -t fails

```bash
# Check syntax
nginx -t

# View detailed error
cat /var/log/nginx/error.log
```

### Problem: Permission denied

```bash
# Check SELinux
getsebool httpd_can_network_connect

# Should be: on
# If off, run:
setsebool -P httpd_can_network_connect 1
```

### Problem: Nginx won't reload

```bash
# Full restart
systemctl restart nginx

# Check status
systemctl status nginx
```

### Problem: Still returns wrong content

```bash
# Check all config files
ls -la /etc/nginx/conf.d/

# Remove any *.conf except celebrations-preprod.conf
mv /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.disabled

# Reload
systemctl reload nginx
```

---

**START NOW:**

1. ✅ Copy the quick command summary
2. ✅ Paste in PuTTY
3. ✅ Verify with curl
4. ✅ Send email to network team
5. ⏳ Wait for ELB fix confirmation
6. ✅ Test domain
7. 🎉 Success!

---

**Total Time to Fix Nginx:** 2 minutes  
**Total Time to Send Email:** 2 minutes  
**Total Time Waiting for Network Team:** Unknown (could be hours/days)

**BUT - Direct IP will work immediately after Nginx fix!**

---

## 📋 PASTE THIS IN PUTTY NOW:

```bash
cat > /etc/nginx/conf.d/celebrations-preprod.conf << 'EOF'
server {
    listen 80;
    server_name celebrationsite-preprod.tanishq.co.in preprod.tanishq.co.in 10.160.128.94 _;
    client_max_body_size 10M;
    access_log /var/log/nginx/celebrations-preprod-access.log;
    error_log /var/log/nginx/celebrations-preprod-error.log;
    location / {
        proxy_pass http://localhost:3002;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_connect_timeout 600;
        proxy_send_timeout 600;
        proxy_read_timeout 600;
        send_timeout 600;
    }
}
EOF
mv /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.disabled 2>/dev/null
setsebool -P httpd_can_network_connect 1
nginx -t && systemctl reload nginx
echo "=== VERIFICATION ==="
curl -I http://localhost
echo ""
echo "=== IF Content-Length is 34455, SUCCESS! ==="
```

**Copy ↑ this entire block and paste in PuTTY now!**


