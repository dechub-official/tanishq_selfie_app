# 🆚 DEPLOYMENT METHODS COMPARISON

## ❌ What You DON'T Want (Systemd Service)

### Setup Process:
```bash
# Create systemd service file
chmod +x setup_production_server.sh
./setup_production_server.sh

# This creates /etc/systemd/system/tanishq-prod.service
systemctl daemon-reload
systemctl enable tanishq-prod
systemctl start tanishq-prod
```

### Management:
```bash
systemctl start tanishq-prod
systemctl stop tanishq-prod
systemctl restart tanishq-prod
systemctl status tanishq-prod
```

**Why you don't want this:**
- ❌ More complex setup
- ❌ Requires systemd service configuration
- ❌ Different from how you run pre-prod
- ❌ More commands to remember

---

## ✅ What You WANT (NOHUP - Simple Way)

### Setup Process:
```bash
# Just start with nohup (ONE COMMAND!)
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3001 \
  > logs/application.log 2>&1 &

echo $! > tanishq-prod.pid
```

### Management:
```bash
# Start
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3001 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid

# Stop
kill $(cat tanishq-prod.pid)

# View logs
tail -f logs/application.log

# Check status
ps -p $(cat tanishq-prod.pid)
```

**Why this is better for you:**
- ✅ Simple - ONE command to start
- ✅ Same as pre-prod approach
- ✅ No systemd setup needed
- ✅ Easy to understand
- ✅ Quick to deploy

---

## 📊 SIDE-BY-SIDE COMPARISON

| Feature | Systemd Service | NOHUP |
|---------|----------------|-------|
| **Setup Time** | ~10 minutes | ~2 minutes |
| **Complexity** | High | Low |
| **Commands** | systemctl start/stop/restart | kill PID / java -jar |
| **Auto-start on boot** | Yes | No (manual start needed) |
| **Setup scripts** | Requires setup_production_server.sh | Not required |
| **Same as pre-prod** | No | Yes ✅ |
| **Easy to manage** | systemctl commands | Simple kill/start |
| **Logs location** | /opt/tanishq/logs/ | /opt/tanishq/logs/ |
| **PID tracking** | systemd manages | You manage (tanishq-prod.pid) |

---

## 🎯 YOUR DEPLOYMENT APPROACH

### Pre-Prod (What you're doing now):
```bash
cd /opt/tanishq
nohup java -jar tanishq-preprod.war \
  --spring.profiles.active=preprod \
  --server.port=3000 \
  > logs/preprod.log 2>&1 &
```

### Production (Same approach, different config):
```bash
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3001 \
  > logs/application.log 2>&1 &
```

**See? Almost identical! Just different:**
- WAR filename
- Profile: `preprod` → `prod`
- Port: `3000` → `3001`
- Log file name

---

## 📝 YOUR EXACT COMMANDS

### 1. Create Database
```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS selfie_prod;"
```

### 2. Create Directories
```bash
cd /opt/tanishq
mkdir -p storage/selfie_images storage/bride_uploads logs
chmod -R 755 storage logs
```

### 3. Start Production
```bash
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3001 \
  > logs/application.log 2>&1 &

echo $! > tanishq-prod.pid
echo "Production started with PID: $(cat tanishq-prod.pid)"
```

### 4. Monitor Logs
```bash
tail -f logs/application.log
```

**Wait for:** `Started TanishqSelfieApplication in X seconds`

**Press Ctrl+C** to stop watching (app continues running)

### 5. Verify
```bash
ps -p $(cat tanishq-prod.pid)
ss -tlnp | grep 3001
```

---

## 🔧 MANAGEMENT COMMANDS

### Check Status
```bash
# Check if process is running
ps -p $(cat /opt/tanishq/tanishq-prod.pid)

# Check port
ss -tlnp | grep 3001

# Check with process name
ps aux | grep tanishq-preprod-07-01-2026-2
```

### Stop Application
```bash
# Graceful stop
kill $(cat /opt/tanishq/tanishq-prod.pid)

# Force stop (if graceful doesn't work)
kill -9 $(cat /opt/tanishq/tanishq-prod.pid)

# Or by port
lsof -ti:3001 | xargs kill -9
```

### View Logs
```bash
# Live logs
tail -f /opt/tanishq/logs/application.log

# Last 100 lines
tail -100 /opt/tanishq/logs/application.log

# Search for errors
grep -i error /opt/tanishq/logs/application.log

# Search for specific text
grep "Started Tanishq" /opt/tanishq/logs/application.log
```

### Restart Application
```bash
# Stop
kill $(cat /opt/tanishq/tanishq-prod.pid)

# Wait
sleep 3

# Start
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3001 \
  > logs/application.log 2>&1 &

echo $! > tanishq-prod.pid
```

---

## 🚀 QUICK START (Copy This)

**One-liner to start production:**
```bash
cd /opt/tanishq && nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3001 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid && echo "Started: $(cat tanishq-prod.pid)"
```

**One-liner to stop production:**
```bash
kill $(cat /opt/tanishq/tanishq-prod.pid) && echo "Stopped production"
```

**One-liner to check status:**
```bash
ps -p $(cat /opt/tanishq/tanishq-prod.pid) && echo "Production is running" || echo "Production is stopped"
```

---

## 💡 PRO TIPS

### 1. Create Alias (Optional)
Add to `~/.bashrc`:
```bash
alias start-prod='cd /opt/tanishq && nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3001 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid'
alias stop-prod='kill $(cat /opt/tanishq/tanishq-prod.pid)'
alias logs-prod='tail -f /opt/tanishq/logs/application.log'
alias status-prod='ps -p $(cat /opt/tanishq/tanishq-prod.pid)'
```

Then use:
```bash
start-prod
stop-prod
logs-prod
status-prod
```

### 2. Create Simple Scripts
I've created restart script commands in `QUICK_START_NOHUP.md`

### 3. Monitor Memory
```bash
# Check memory usage
ps aux | grep tanishq-preprod-07-01-2026-2

# Check detailed info
top -p $(cat /opt/tanishq/tanishq-prod.pid)
```

---

## ✅ SUMMARY

**You want: NOHUP approach** ✅
- Simple
- Same as pre-prod
- No systemd setup
- Easy commands

**You don't want: Systemd service** ❌
- Complex
- Different from pre-prod  
- Requires setup scripts
- More steps

**Your deployment:**
1. Create database ✅
2. Create directories ✅
3. Start with nohup ✅
4. Monitor logs ✅
5. Done! ✅

**Total time: 5 minutes** ⚡

---

## 📞 FILES TO USE

**Main Guide:**
- `DEPLOY_PRODUCTION_NOHUP.md` - Detailed guide with nohup approach

**Quick Reference:**
- `QUICK_START_NOHUP.md` - Quick commands
- `COPY_PASTE_COMMANDS.md` - Copy-paste ready commands

**Ignore These (You Don't Need):**
- ~~`DEPLOY_PRODUCTION_NOW.md`~~ - Uses systemd (not what you want)
- ~~`setup_production_server.sh`~~ - Creates systemd service (not needed)

---

**Follow the NOHUP approach - it's simple and works like pre-prod!** 🚀

