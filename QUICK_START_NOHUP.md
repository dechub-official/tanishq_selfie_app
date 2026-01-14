# ⚡ QUICK START - Production Deployment with NOHUP

## You Already Have:
✅ WAR file at: `/opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war`

---

## 🚀 DEPLOY IN 3 STEPS (5 Minutes)

### Step 1: Create Database & Directories
```bash
# SSH to server
ssh root@10.10.63.97

# Create database
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Create directories
cd /opt/tanishq
mkdir -p storage/selfie_images storage/bride_uploads storage/bride_images logs
chmod -R 755 storage logs
```

### Step 2: Start Application
```bash
cd /opt/tanishq

# Start with nohup
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3001 \
  > logs/application.log 2>&1 &

# Save PID
echo $! > tanishq-prod.pid
```

### Step 3: Monitor & Verify
```bash
# Watch logs
tail -f logs/application.log

# Wait for: "Started TanishqSelfieApplication in X seconds"
# Press Ctrl+C to stop watching (app keeps running)

# Verify running
ps -p $(cat tanishq-prod.pid)
ss -tlnp | grep 3001
```

---

## ✅ DONE! Application is Running!

---

## 📋 MANAGEMENT COMMANDS

### Check Status:
```bash
ps -p $(cat /opt/tanishq/tanishq-prod.pid)
ss -tlnp | grep 3001
```

### View Logs:
```bash
tail -f /opt/tanishq/logs/application.log
```

### Stop Application:
```bash
kill $(cat /opt/tanishq/tanishq-prod.pid)
```

### Restart Application:
```bash
# Stop
kill $(cat /opt/tanishq/tanishq-prod.pid)
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

## 🔧 OPTIONAL: Create Management Scripts

### Create Restart Script:
```bash
cat > /opt/tanishq/restart-prod.sh << 'EOF'
#!/bin/bash
cd /opt/tanishq
[ -f tanishq-prod.pid ] && kill $(cat tanishq-prod.pid) 2>/dev/null
lsof -ti:3001 | xargs kill -9 2>/dev/null
sleep 3
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3001 \
  > logs/application.log 2>&1 &
echo $! > tanishq-prod.pid
echo "Started with PID: $(cat tanishq-prod.pid)"
EOF

chmod +x /opt/tanishq/restart-prod.sh
```

**Usage:** `/opt/tanishq/restart-prod.sh`

### Create Stop Script:
```bash
cat > /opt/tanishq/stop-prod.sh << 'EOF'
#!/bin/bash
cd /opt/tanishq
[ -f tanishq-prod.pid ] && kill $(cat tanishq-prod.pid) 2>/dev/null
lsof -ti:3001 | xargs kill -9 2>/dev/null
echo "Production stopped"
EOF

chmod +x /opt/tanishq/stop-prod.sh
```

**Usage:** `/opt/tanishq/stop-prod.sh`

---

## 🐛 TROUBLESHOOTING

### Port already in use?
```bash
lsof -ti:3001 | xargs kill -9
```

### Check MySQL connection?
```bash
mysql -u root -p -e "USE selfie_prod; SHOW TABLES;"
```

### View errors in logs?
```bash
grep -i error /opt/tanishq/logs/application.log | tail -20
```

---

## 📊 COMPARISON: Pre-Prod vs Production

| Setting | Pre-Prod | Production |
|---------|----------|------------|
| **WAR File** | tanishq-preprod-*.war | tanishq-preprod-07-01-2026-2-*.war |
| **Profile** | `--spring.profiles.active=preprod` | `--spring.profiles.active=prod` |
| **Port** | 3000 | 3001 |
| **Database** | selfie_preprod | selfie_prod |
| **Logs** | logs/application.log | logs/application.log |
| **PID File** | tanishq-preprod.pid | tanishq-prod.pid |

---

## 🎯 FULL START COMMAND (Copy-Paste Ready)

```bash
cd /opt/tanishq && nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3001 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid && echo "Started with PID: $(cat tanishq-prod.pid)"
```

---

**That's it! Simple deployment with nohup, just like pre-prod!** 🚀

**For detailed guide, see:** `DEPLOY_PRODUCTION_NOHUP.md`

