# 🚀 DEPLOY PRODUCTION USING NOHUP (Simple Way)

## Current Status
✅ WAR file uploaded: `/opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war`

---

## Step 1: Create Production Database (2 minutes)

```bash
# SSH to production server
ssh root@10.10.63.97

# Create database
mysql -u root -p

# Then run these SQL commands:
CREATE DATABASE IF NOT EXISTS selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON selfie_prod.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

**Or use the SQL file if you uploaded it:**
```bash
mysql -u root -p < /opt/tanishq/setup_production_database.sql
```

---

## Step 2: Create Required Directories (1 minute)

```bash
cd /opt/tanishq

# Create storage directories
mkdir -p storage/selfie_images
mkdir -p storage/bride_uploads
mkdir -p storage/bride_images
mkdir -p logs

# Set permissions
chmod -R 755 storage
chmod -R 755 logs
```

---

## Step 3: Verify Files Are in Place (1 minute)

```bash
cd /opt/tanishq

# Check WAR file
ls -lh tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war

# Check Google service account keys (if uploaded)
ls -lh *.p12

# Set permissions for .p12 files
chmod 644 *.p12
```

---

## Step 4: Stop Old Application (if running)

```bash
# Find if any Java process is running on port 3001
ps aux | grep tanishq

# If you see a process, note the PID and kill it
kill -9 <PID>

# Or find and kill by port
lsof -ti:3001 | xargs kill -9
```

---

## Step 5: Start Application with NOHUP (Production Mode)

```bash
cd /opt/tanishq

# Start application in background with nohup
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3001 \
  > logs/application.log 2>&1 &

# Note the PID that appears
echo $! > tanishq-prod.pid
```

**Explanation:**
- `nohup` - Runs command immune to hangups
- `--spring.profiles.active=prod` - Uses production configuration
- `--server.port=3001` - Production port
- `> logs/application.log` - Redirect output to log file
- `2>&1` - Redirect errors to same log
- `&` - Run in background
- `echo $! > tanishq-prod.pid` - Save process ID for later

---

## Step 6: Monitor Startup (3 minutes)

```bash
# Watch logs in real-time
tail -f /opt/tanishq/logs/application.log
```

**Look for these success messages:**
- `Started TanishqSelfieApplication in X seconds`
- `Tomcat started on port(s): 3001 (http)`
- `HikariPool-1 - Start completed`

**Press Ctrl+C to stop watching (application keeps running)**

---

## Step 7: Verify Application is Running (2 minutes)

### Check process is running:
```bash
# Using saved PID
ps -p $(cat /opt/tanishq/tanishq-prod.pid)

# Or check by name
ps aux | grep tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT
```

### Check port is listening:
```bash
ss -tlnp | grep 3001
# Or
netstat -tlnp | grep 3001
```

### Check database tables were created:
```bash
mysql -u root -p -e "USE selfie_prod; SHOW TABLES;"
```

Should show tables: `event`, `attendee`, `invitee`, `bride_details`, etc.

### Test from curl:
```bash
curl -I http://localhost:3001/
```

---

## Step 8: Test from Browser

Open: `https://celebrations.tanishq.co.in/`

Or use IP: `http://10.10.63.97:3001/`

---

## ✅ SUCCESS! Application is Running!

Your production application is now running with:
- ✅ MySQL database (selfie_prod)
- ✅ Port 3001
- ✅ Running in background with nohup
- ✅ Logs in `/opt/tanishq/logs/application.log`

---

## 🔧 Management Commands

### Check if running:
```bash
ps -p $(cat /opt/tanishq/tanishq-prod.pid)
# Or
ps aux | grep tanishq
```

### View logs:
```bash
tail -f /opt/tanishq/logs/application.log

# View last 100 lines
tail -100 /opt/tanishq/logs/application.log

# Search for errors
grep -i error /opt/tanishq/logs/application.log
```

### Stop application:
```bash
# Using saved PID
kill $(cat /opt/tanishq/tanishq-prod.pid)

# Or force kill
kill -9 $(cat /opt/tanishq/tanishq-prod.pid)

# Or find and kill by port
lsof -ti:3001 | xargs kill -9
```

### Restart application:
```bash
# Stop first
kill $(cat /opt/tanishq/tanishq-prod.pid)

# Wait a moment
sleep 5

# Start again
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3001 \
  > logs/application.log 2>&1 &

echo $! > tanishq-prod.pid
```

---

## 🔧 Troubleshooting

### Application won't start?

**Check logs:**
```bash
tail -100 /opt/tanishq/logs/application.log
grep -i error /opt/tanishq/logs/application.log
```

**Common issues:**

1. **Port already in use:**
   ```bash
   # Find what's using port 3001
   lsof -i:3001
   
   # Kill it
   lsof -ti:3001 | xargs kill -9
   ```

2. **MySQL connection failed:**
   ```bash
   # Check MySQL is running
   systemctl status mysqld
   
   # Test connection
   mysql -u root -p -e "USE selfie_prod;"
   ```

3. **Database doesn't exist:**
   ```bash
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS selfie_prod;"
   ```

4. **Java not found:**
   ```bash
   # Check Java version
   java -version
   
   # If not found, find Java
   which java
   
   # Or use full path
   /usr/bin/java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war ...
   ```

---

## 📝 Create Restart Script (Optional but Recommended)

Create a simple script to restart application easily:

```bash
# Create script
cat > /opt/tanishq/restart-prod.sh << 'EOF'
#!/bin/bash

echo "Stopping Tanishq Production..."
if [ -f /opt/tanishq/tanishq-prod.pid ]; then
    kill $(cat /opt/tanishq/tanishq-prod.pid) 2>/dev/null
    sleep 3
fi

# Kill any remaining process on port 3001
lsof -ti:3001 | xargs kill -9 2>/dev/null

echo "Starting Tanishq Production..."
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3001 \
  > logs/application.log 2>&1 &

echo $! > tanishq-prod.pid
echo "Started with PID: $(cat tanishq-prod.pid)"
echo "Monitor logs: tail -f /opt/tanishq/logs/application.log"
EOF

# Make executable
chmod +x /opt/tanishq/restart-prod.sh
```

**Usage:**
```bash
# Restart application
/opt/tanishq/restart-prod.sh
```

---

## 📝 Create Stop Script (Optional)

```bash
# Create script
cat > /opt/tanishq/stop-prod.sh << 'EOF'
#!/bin/bash

echo "Stopping Tanishq Production..."
if [ -f /opt/tanishq/tanishq-prod.pid ]; then
    PID=$(cat /opt/tanishq/tanishq-prod.pid)
    kill $PID 2>/dev/null
    echo "Stopped process: $PID"
else
    echo "No PID file found"
fi

# Kill any remaining process on port 3001
lsof -ti:3001 | xargs kill -9 2>/dev/null
echo "Production stopped"
EOF

# Make executable
chmod +x /opt/tanishq/stop-prod.sh
```

**Usage:**
```bash
# Stop application
/opt/tanishq/stop-prod.sh
```

---

## 📝 Create Start Script (Optional)

```bash
# Create script
cat > /opt/tanishq/start-prod.sh << 'EOF'
#!/bin/bash

echo "Starting Tanishq Production..."
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3001 \
  > logs/application.log 2>&1 &

echo $! > tanishq-prod.pid
echo "Started with PID: $(cat tanishq-prod.pid)"
echo "Monitor logs: tail -f /opt/tanishq/logs/application.log"
EOF

# Make executable
chmod +x /opt/tanishq/start-prod.sh
```

**Usage:**
```bash
# Start application
/opt/tanishq/start-prod.sh
```

---

## 🎯 Quick Reference

| Task | Command |
|------|---------|
| **Start** | `cd /opt/tanishq && nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3001 > logs/application.log 2>&1 &` |
| **Stop** | `kill $(cat /opt/tanishq/tanishq-prod.pid)` |
| **Logs** | `tail -f /opt/tanishq/logs/application.log` |
| **Status** | `ps -p $(cat /opt/tanishq/tanishq-prod.pid)` |
| **Port Check** | `ss -tlnp \| grep 3001` |
| **Database** | `mysql -u root -p -e "USE selfie_prod; SHOW TABLES;"` |

---

## 🎉 All Done!

Your production application is running using nohup, just like pre-prod!

**Differences from Pre-Prod:**
- Port: 3001 (instead of 3000)
- Database: selfie_prod (instead of selfie_preprod)
- Profile: prod (instead of preprod)
- S3 Bucket: celebrations-tanishq-prod

**Same as Pre-Prod:**
- ✅ Running with nohup
- ✅ Background process
- ✅ Logs to file
- ✅ Simple start/stop commands

---

## 💡 Tips

1. **Keep PID file safe** - Makes stopping/restarting easier
2. **Monitor logs regularly** - Check for errors
3. **Use scripts** - Create start/stop/restart scripts for convenience
4. **Backup database** - Regular backups of selfie_prod
5. **Check disk space** - Logs can grow large

---

**Total Time: ~10 minutes**

**You're done! Production is running with nohup!** 🚀

