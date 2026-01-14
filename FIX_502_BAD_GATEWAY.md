# 🔧 FIX 502 BAD GATEWAY - Quick Solution

## Problem: Reverse Proxy Configuration Mismatch

Your reverse proxy (Apache/Nginx) is configured for port **3000**, but production is running on port **3001**.

---

## ✅ SOLUTION: Run Production on Port 3000

### Step 1: Stop Current Process
```bash
# Kill the process running on port 3001
kill 3679635

# Verify it's stopped
ss -tlnp | grep 3001
```

### Step 2: Check Port 3000 is Available
```bash
ss -tlnp | grep 3000
```

**If something is running on port 3000:**
```bash
# Find the PID
lsof -ti:3000

# Kill it
lsof -ti:3000 | xargs kill -9
```

### Step 3: Start Production on Port 3000
```bash
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > logs/application.log 2>&1 &

echo $! > tanishq-prod.pid
echo "Production started on port 3000 with PID: $(cat tanishq-prod.pid)"
```

### Step 4: Monitor Startup
```bash
tail -f logs/application.log
```

**Wait for:**
```
Started TanishqSelfieApplication in X seconds
Tomcat started on port(s): 3000 (http)
```

**Press Ctrl+C** to stop watching (app keeps running)

### Step 5: Verify
```bash
# Check process
ps -p $(cat tanishq-prod.pid)

# Check port
ss -tlnp | grep 3000

# Test locally
curl -I http://localhost:3000/
```

### Step 6: Test from Browser
Open: `https://celebrations.tanishq.co.in/`

**Should work now!** ✅

---

## 📋 ONE-LINER (Copy-Paste)

```bash
kill 3679635 && lsof -ti:3000 | xargs kill -9 2>/dev/null; sleep 2 && cd /opt/tanishq && nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid && echo "Started: $(cat tanishq-prod.pid)" && sleep 10 && tail -20 logs/application.log
```

---

## 🔄 IF YOU PREFER PORT 3001 (Alternative)

If you want production on port 3001, you need to update the reverse proxy:

### Find Proxy Config:
```bash
# For Apache
grep -r "ProxyPass.*3000" /etc/httpd/ /etc/apache2/ 2>/dev/null

# For Nginx
grep -r "proxy_pass.*3000" /etc/nginx/ 2>/dev/null
```

### Update Config:
Change `3000` to `3001` in the proxy configuration file.

### Restart Web Server:
```bash
# Apache
systemctl restart httpd
# or
systemctl restart apache2

# Nginx
systemctl restart nginx
```

---

## 📊 SUMMARY

**Issue:** 502 Bad Gateway
**Cause:** Reverse proxy forwards to port 3000, but app runs on 3001
**Solution:** Run production on port 3000 (easiest) OR update proxy config

**Recommended:** Use port 3000 for production (proxy already configured)

---

## ✅ EXPECTED RESULT

After running production on port 3000:
- ✅ `https://celebrations.tanishq.co.in/` will work
- ✅ No 502 Bad Gateway error
- ✅ Website loads correctly
- ✅ All features work

---

## 🎯 VERIFICATION COMMANDS

```bash
# Check running
ps -p $(cat /opt/tanishq/tanishq-prod.pid)

# Check port 3000
ss -tlnp | grep 3000

# Test locally
curl -I http://localhost:3000/

# Check logs
tail -50 /opt/tanishq/logs/application.log
```

---

**Run the fix now and your website will be accessible!** 🚀

