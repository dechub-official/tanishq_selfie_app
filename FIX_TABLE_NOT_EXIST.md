# 🔧 FIX: Table Doesn't Exist Error

## Problem: Tables Not Created in Production

The import failed because tables don't exist in selfie_prod database.

---

## ✅ SOLUTION: Complete Re-Import

Run these commands on **PRODUCTION SERVER (10.10.63.97)**:

### Step 1: Check if Export File Exists
```bash
# List export files
ls -lh /opt/tanishq/backups/preprod_export_*.sql

# If file exists, continue to Step 2
# If NOT, you need to transfer it from pre-prod first!
```

---

### Step 2: Drop and Recreate Production Database
```bash
# Drop existing database completely
mysql -u root -p -e "DROP DATABASE IF EXISTS selfie_prod;"

# Create fresh database
mysql -u root -p -e "CREATE DATABASE selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo "✅ Database recreated"
```

---

### Step 3: Import Pre-Prod Data (Full Import)
```bash
# Find the export file
EXPORT_FILE=$(ls -t /opt/tanishq/backups/preprod_export_*.sql | head -1)

# Show which file will be imported
echo "Importing: $EXPORT_FILE"

# Import (this will create tables AND import data)
mysql -u root -p selfie_prod < $EXPORT_FILE

# This may take 2-5 minutes depending on data size
echo "✅ Import completed"
```

---

### Step 4: Verify Tables Were Created
```bash
# List all tables
mysql -u root -p -e "USE selfie_prod; SHOW TABLES;"
```

**Expected Output:** Should show 15+ tables:
- abm_login
- attendees
- bride_details
- cee_login
- events
- greetings
- invitees
- password_history
- product_details
- rbm_login
- rivaah
- rivaah_users
- stores
- user_details
- users

---

### Step 5: Verify Data Counts
```bash
mysql -u root -p -e "
USE selfie_prod;
SELECT 'events' as table_name, COUNT(*) as count FROM events
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'greetings', COUNT(*) FROM greetings
UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL
SELECT 'stores', COUNT(*) FROM stores
UNION ALL
SELECT 'product_details', COUNT(*) FROM product_details;
"
```

**Expected:** Should show counts without errors

---

### Step 6: Restart Production Application
```bash
# Stop current app
kill $(cat /opt/tanishq/tanishq-prod.pid)
sleep 3

# Start app
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > logs/application.log 2>&1 &

echo $! > tanishq-prod.pid

echo "✅ App restarted with PID: $(cat tanishq-prod.pid)"
```

---

### Step 7: Monitor Startup
```bash
tail -f logs/application.log
```

Wait for: `Started TanishqSelfieApplication`

Press **Ctrl+C** to stop watching

---

## ⚡ COMPLETE FIX (One Script)

Copy-paste this entire block:

```bash
#!/bin/bash

echo "🔧 Fixing Production Database Import..."
echo ""

# Check if export file exists
if [ ! -f /opt/tanishq/backups/preprod_export_*.sql ]; then
    echo "❌ ERROR: No export file found in /opt/tanishq/backups/"
    echo "You need to transfer the file from pre-prod server first!"
    exit 1
fi

# Find export file
EXPORT_FILE=$(ls -t /opt/tanishq/backups/preprod_export_*.sql | head -1)
echo "📁 Using export file: $EXPORT_FILE"
FILE_SIZE=$(ls -lh $EXPORT_FILE | awk '{print $5}')
echo "📊 File size: $FILE_SIZE"
echo ""

# Drop and recreate database
echo "🗑️  Dropping and recreating database..."
mysql -u root -p -e "DROP DATABASE IF EXISTS selfie_prod;" 2>/dev/null
mysql -u root -p -e "CREATE DATABASE selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
echo "✅ Database recreated"
echo ""

# Import data
echo "📥 Importing data (this may take 2-5 minutes)..."
mysql -u root -p selfie_prod < $EXPORT_FILE 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Import completed successfully!"
else
    echo "❌ Import failed! Check MySQL error logs."
    exit 1
fi
echo ""

# Verify tables
echo "🔍 Verifying tables created..."
TABLE_COUNT=$(mysql -u root -p -e "USE selfie_prod; SHOW TABLES;" 2>/dev/null | wc -l)
echo "✅ Tables created: $((TABLE_COUNT - 1))"
echo ""

# Verify data
echo "📊 Data Verification:"
mysql -u root -p -e "
USE selfie_prod;
SELECT 'events' as table_name, COUNT(*) as count FROM events
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'greetings', COUNT(*) FROM greetings
UNION ALL
SELECT 'stores', COUNT(*) FROM stores;
" 2>/dev/null
echo ""

# Restart app
echo "🔄 Restarting production application..."
if [ -f /opt/tanishq/tanishq-prod.pid ]; then
    kill $(cat /opt/tanishq/tanishq-prod.pid) 2>/dev/null
    sleep 3
fi

cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > logs/application.log 2>&1 &

echo $! > tanishq-prod.pid
echo "✅ Application started with PID: $(cat tanishq-prod.pid)"
echo ""

echo "=========================================="
echo "✅ FIX COMPLETE!"
echo "=========================================="
echo ""
echo "📝 Next steps:"
echo "  1. Monitor logs: tail -f /opt/tanishq/logs/application.log"
echo "  2. Test website: https://celebrations.tanishq.co.in/"
echo "  3. Verify data is visible"
echo ""
```

---

## 🎯 IF EXPORT FILE IS MISSING

If you don't have the export file on production server, you need to get it from pre-prod:

### Option 1: Export from Pre-Prod and Transfer

**On Pre-Prod Server:**
```bash
mkdir -p /tmp/migration
mysqldump -u root -p selfie_preprod > /tmp/migration/preprod_export_$(date +%Y%m%d_%H%M%S).sql
```

**Transfer to Production:**
- Use WinSCP to download from pre-prod: `/tmp/migration/preprod_export_*.sql`
- Then upload to production: `/opt/tanishq/backups/`

**OR use SCP directly:**
```bash
# On your Windows PC:
pscp root@<PREPROD_IP>:/tmp/migration/preprod_export_*.sql C:\temp\
pscp C:\temp\preprod_export_*.sql root@10.10.63.97:/opt/tanishq/backups/
```

---

## 📋 MANUAL STEP-BY-STEP (If Script Doesn't Work)

### 1. Drop Database
```bash
mysql -u root -p -e "DROP DATABASE IF EXISTS selfie_prod;"
```

### 2. Create Database
```bash
mysql -u root -p -e "CREATE DATABASE selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 3. Find Export File
```bash
ls -lh /opt/tanishq/backups/preprod_export_*.sql
```

### 4. Import
```bash
# Use actual filename
mysql -u root -p selfie_prod < /opt/tanishq/backups/preprod_export_YYYYMMDD_HHMMSS.sql
```

### 5. Verify
```bash
mysql -u root -p -e "USE selfie_prod; SHOW TABLES;"
```

### 6. Restart App
```bash
kill $(cat /opt/tanishq/tanishq-prod.pid)
sleep 3
cd /opt/tanishq && nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid
```

---

## 🐛 WHY THIS HAPPENED

The original import failed because:
1. The database might have been cleared without proper re-import
2. The import command didn't complete successfully
3. Only partial data was imported without table structures

**Solution:** Complete clean re-import with DROP/CREATE DATABASE first.

---

## ✅ AFTER FIX - VERIFICATION

```bash
# 1. Check tables exist
mysql -u root -p -e "USE selfie_prod; SHOW TABLES;"

# 2. Check data counts
mysql -u root -p -e "USE selfie_prod; SELECT COUNT(*) FROM events; SELECT COUNT(*) FROM users;"

# 3. Check app is running
ps -p $(cat /opt/tanishq/tanishq-prod.pid)

# 4. Test website
curl -I http://localhost:3000/
```

Then test in browser: **https://celebrations.tanishq.co.in/**

---

**Run the fix now! This will properly import all tables and data.** 🚀

