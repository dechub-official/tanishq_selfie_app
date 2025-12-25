# 🚨 URGENT: Application Not Accessible - Troubleshooting Guide

## ❌ Current Problem

**URL:** https://celebrationsite-preprod.tanishq.co.in/events/customer/TEST_25302712-2ea6-4706-bb30-d8c118693608

**Error:** "This site can't be reached" - Blank page

**Root Cause:** The application server is either:
1. Not running
2. Not accessible from the internet
3. DNS/Proxy not configured correctly
4. Port not exposed

---

## 🔍 Step-by-Step Diagnosis

### Step 1: Check if Application is Running

**On the server, run:**

```bash
# Check if Java process is running
ps aux | grep java | grep tanishq

# OR check if port 3000 is listening
netstat -tulpn | grep 3000

# OR using lsof
lsof -i :3000
```

**Expected:** Should show a Java process running on port 3000

**If NOT running:**
```bash
# Start the application
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-preprod-*.war > application.log 2>&1 &

# OR if using systemd
sudo systemctl start tanishq-preprod

# Check startup logs
tail -f application.log
# OR
tail -f /opt/tanishq/logs/application.log
```

---

### Step 2: Check Application Responds Locally

**On the server:**

```bash
# Test if app responds on localhost
curl http://localhost:3000/events.html

# Should return HTML content (not error)

# Test the specific endpoint
curl http://localhost:3000/events/customer/TEST_25302712-2ea6-4706-bb30-d8c118693608

# Should return HTML with forward to events.html
```

**If this works:** Application is running, issue is with external access

**If this fails:** Application has startup issues

---

### Step 3: Check External Access

**From your local machine or another server:**

```bash
# Test direct IP access (replace with actual server IP)
curl http://10.160.128.94:3000/events.html

# Test domain access
curl https://celebrationsite-preprod.tanishq.co.in/events.html
```

**If IP works but domain doesn't:** DNS or reverse proxy issue

---

### Step 4: Check Reverse Proxy (Nginx/Apache)

The domain `celebrationsite-preprod.tanishq.co.in` likely goes through a reverse proxy.

**Check Nginx configuration:**

```bash
# Find nginx config
sudo grep -r "celebrationsite-preprod" /etc/nginx/

# Check if site is enabled
ls -la /etc/nginx/sites-enabled/ | grep celebration

# View the config
sudo cat /etc/nginx/sites-available/celebrationsite-preprod.conf
# OR
sudo cat /etc/nginx/conf.d/celebrationsite-preprod.conf
```

**Expected Configuration:**
```nginx
server {
    listen 80;
    listen 443 ssl;
    server_name celebrationsite-preprod.tanishq.co.in;

    # SSL configuration
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**If config is missing or wrong:**
```bash
# Reload nginx after fixing
sudo nginx -t  # Test config
sudo systemctl reload nginx
```

---

### Step 5: Check Firewall

```bash
# Check if port 3000 is allowed
sudo iptables -L -n | grep 3000

# OR check firewalld
sudo firewall-cmd --list-all

# If needed, open port
sudo firewall-cmd --add-port=3000/tcp --permanent
sudo firewall-cmd --reload
```

---

### Step 6: Check DNS Resolution

**From your local machine:**

```bash
# Check DNS resolution
nslookup celebrationsite-preprod.tanishq.co.in

# OR
dig celebrationsite-preprod.tanishq.co.in
```

**Expected:** Should resolve to server IP (e.g., 10.160.128.94)

**If not resolving:** DNS not configured or propagating

---

## 🔧 Quick Fixes

### Fix 1: If Application Not Running

```bash
# Go to deployment directory
cd /opt/tanishq/applications_preprod

# Find the latest WAR
ls -lt tanishq-preprod-*.war | head -1

# Start it
nohup java -jar tanishq-preprod-18-12-2025-7-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

# Get process ID
echo $!

# Watch logs
tail -f application.log
```

**Look for in logs:**
```
Started TanishqApplication in X seconds
Tomcat started on port(s): 3000
```

---

### Fix 2: If Nginx Not Configured

**Create Nginx config:**

```bash
sudo nano /etc/nginx/sites-available/celebrationsite-preprod.conf
```

**Add this configuration:**

```nginx
server {
    listen 80;
    server_name celebrationsite-preprod.tanishq.co.in;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name celebrationsite-preprod.tanishq.co.in;

    # SSL Configuration (update paths)
    ssl_certificate /etc/ssl/certs/tanishq-preprod.crt;
    ssl_certificate_key /etc/ssl/private/tanishq-preprod.key;
    
    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Proxy to Spring Boot app
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_cache_bypass $http_upgrade;
        
        # Timeout settings
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Static files
    location /static/ {
        proxy_pass http://localhost:3000/static/;
        proxy_cache_valid 200 1d;
        expires 1d;
        add_header Cache-Control "public, immutable";
    }
}
```

**Enable and reload:**

```bash
# Create symlink
sudo ln -s /etc/nginx/sites-available/celebrationsite-preprod.conf /etc/nginx/sites-enabled/

# Test config
sudo nginx -t

# Reload
sudo systemctl reload nginx
```

---

### Fix 3: If DNS Not Configured

**Contact your DNS admin to add:**

```
Type: A Record
Name: celebrationsite-preprod
Value: <SERVER_IP>  (e.g., 10.160.128.94)
TTL: 300
```

**OR if using /etc/hosts for testing:**

```bash
# On your local machine
sudo nano /etc/hosts

# Add:
<SERVER_IP> celebrationsite-preprod.tanishq.co.in
```

---

## 🧪 Testing After Fixes

### Test 1: Local Access
```bash
# On server
curl http://localhost:3000/events.html

# Should return HTML
```

### Test 2: Direct IP Access
```bash
# From anywhere
curl http://<SERVER_IP>:3000/events.html
```

### Test 3: Domain Access
```bash
# From anywhere
curl https://celebrationsite-preprod.tanishq.co.in/events.html

# Should return HTML
```

### Test 4: Browser Test
Open in browser:
```
https://celebrationsite-preprod.tanishq.co.in/events/customer/TEST_25302712-2ea6-4706-bb30-d8c118693608
```

**Expected:** Attendee form should load (not blank page!)

---

## 📊 Diagnostic Checklist

Run these commands and share results:

```bash
# 1. Check if app is running
ps aux | grep java | grep tanishq

# 2. Check port listening
netstat -tulpn | grep 3000

# 3. Check local access
curl -I http://localhost:3000/events.html

# 4. Check external access (from another machine)
curl -I http://<SERVER_IP>:3000/events.html

# 5. Check domain access
curl -I https://celebrationsite-preprod.tanishq.co.in/events.html

# 6. Check nginx status
sudo systemctl status nginx

# 7. Check nginx config
sudo nginx -t

# 8. Check application logs
tail -20 /opt/tanishq/applications_preprod/application.log
# OR wherever your logs are

# 9. Check DNS
nslookup celebrationsite-preprod.tanishq.co.in

# 10. Check firewall
sudo iptables -L -n | grep 3000
```

---

## 🚨 Most Likely Issues

Based on the error "This site can't be reached":

1. **Application Not Running** (70% probability)
   - Process died
   - Failed to start
   - Port conflict

2. **Reverse Proxy Not Configured** (20% probability)
   - Nginx not proxying to port 3000
   - Wrong domain config
   - SSL certificate issue

3. **DNS Not Configured** (10% probability)
   - Domain not pointing to server
   - Not propagated yet

---

## 🔥 Emergency Quick Start

**If everything is broken, start fresh:**

```bash
# 1. Stop any existing process
pkill -f tanishq

# 2. Go to app directory
cd /opt/tanishq/applications_preprod

# 3. Start application
nohup java -jar tanishq-preprod-18-12-2025-7-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=preprod \
  --server.port=3000 \
  > application.log 2>&1 &

# 4. Wait 30 seconds for startup

# 5. Test locally
curl http://localhost:3000/events.html

# 6. If that works, check external access
# Issue is with nginx/firewall/DNS

# 7. Check nginx
sudo systemctl status nginx
sudo nginx -t
```

---

## 📞 Need to Share?

**Run this diagnostic script and share output:**

```bash
#!/bin/bash
echo "=== Application Status ==="
ps aux | grep java | grep tanishq

echo -e "\n=== Port Status ==="
netstat -tulpn | grep 3000

echo -e "\n=== Local Access Test ==="
curl -I http://localhost:3000/events.html 2>&1

echo -e "\n=== Nginx Status ==="
sudo systemctl status nginx | head -10

echo -e "\n=== Nginx Config Test ==="
sudo nginx -t 2>&1

echo -e "\n=== DNS Resolution ==="
nslookup celebrationsite-preprod.tanishq.co.in

echo -e "\n=== Recent Logs ==="
tail -20 /opt/tanishq/applications_preprod/application.log 2>&1 || echo "Log file not found"
```

Save as `diagnose.sh`, run with `bash diagnose.sh` and share the output.

---

**NEXT STEP: Run the diagnostic commands above and share the results!**

