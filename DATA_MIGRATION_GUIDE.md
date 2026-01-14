# 🔄 DATA MIGRATION: Pre-Prod → Production

## ✅ Current Status
- **Pre-Prod:** Running with data (selfie_preprod database)
- **Production:** Running but empty (selfie_prod database)
- **Goal:** Copy all data from pre-prod to production safely

---

## ⚠️ IMPORTANT SAFETY NOTES

1. **Backup First:** Always backup before migration
2. **Test Data:** Pre-prod data might contain test data
3. **IDs:** Auto-increment IDs will be preserved
4. **Timestamps:** Created/updated dates will be preserved
5. **Users:** All user accounts will be copied

---

## 📋 MIGRATION APPROACH

### Option 1: Full Database Migration (Recommended)
**Copy ALL data from pre-prod to production**

### Option 2: Selective Migration
**Copy only specific tables (e.g., skip test data)**

### Option 3: Start Fresh
**Keep production empty, let real users create data**

---

## 🚀 METHOD 1: FULL MIGRATION (Copy Everything)

This will copy ALL data from pre-prod to production.

### Step 1: Backup Production Database (Safety First!)
```bash
# Create backup directory
mkdir -p /opt/tanishq/backups

# Backup current production database (even if empty)
mysqldump -u root -p selfie_prod > /opt/tanishq/backups/selfie_prod_before_migration_$(date +%Y%m%d_%H%M%S).sql

echo "Production backup completed"
```

### Step 2: Export Pre-Prod Data
```bash
# Export all data from pre-prod database
mysqldump -u root -p selfie_preprod > /opt/tanishq/backups/preprod_export_$(date +%Y%m%d_%H%M%S).sql

echo "Pre-prod data exported"
```

### Step 3: Clear Production Database (Optional - if has test data)
```bash
# This will drop all tables and recreate empty database
mysql -u root -p -e "DROP DATABASE IF EXISTS selfie_prod; CREATE DATABASE selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo "Production database cleared"
```

### Step 4: Import Pre-Prod Data to Production
```bash
# Find the exported file
EXPORT_FILE=$(ls -t /opt/tanishq/backups/preprod_export_*.sql | head -1)

# Import to production
mysql -u root -p selfie_prod < $EXPORT_FILE

echo "Data imported to production"
```

### Step 5: Verify Data Migrated
```bash
# Count records in each table
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
SELECT 'invitees', COUNT(*) FROM invitees;
"
```

### Step 6: Restart Production Application (Optional)
```bash
# Restart to clear any caches
kill $(cat /opt/tanishq/tanishq-prod.pid)
sleep 3
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > logs/application.log 2>&1 &
echo $! > tanishq-prod.pid
```

---

## 🎯 COMPLETE MIGRATION SCRIPT (Copy-Paste)

This single script does everything:

```bash
#!/bin/bash

echo "=========================================="
echo "🔄 DATA MIGRATION: Pre-Prod → Production"
echo "=========================================="
echo ""

# Configuration
BACKUP_DIR="/opt/tanishq/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Create backup directory
mkdir -p $BACKUP_DIR

echo "📦 Step 1: Backing up production database..."
mysqldump -u root -p selfie_prod > $BACKUP_DIR/selfie_prod_before_migration_$TIMESTAMP.sql
if [ $? -eq 0 ]; then
    echo "✅ Production backup saved to: $BACKUP_DIR/selfie_prod_before_migration_$TIMESTAMP.sql"
else
    echo "❌ Production backup failed! Exiting."
    exit 1
fi
echo ""

echo "📤 Step 2: Exporting pre-prod data..."
mysqldump -u root -p selfie_preprod > $BACKUP_DIR/preprod_export_$TIMESTAMP.sql
if [ $? -eq 0 ]; then
    echo "✅ Pre-prod data exported to: $BACKUP_DIR/preprod_export_$TIMESTAMP.sql"
else
    echo "❌ Pre-prod export failed! Exiting."
    exit 1
fi
echo ""

echo "🗑️  Step 3: Clearing production database..."
mysql -u root -p -e "DROP DATABASE IF EXISTS selfie_prod; CREATE DATABASE selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if [ $? -eq 0 ]; then
    echo "✅ Production database cleared and recreated"
else
    echo "❌ Failed to clear production database! Exiting."
    exit 1
fi
echo ""

echo "📥 Step 4: Importing pre-prod data to production..."
mysql -u root -p selfie_prod < $BACKUP_DIR/preprod_export_$TIMESTAMP.sql
if [ $? -eq 0 ]; then
    echo "✅ Data imported successfully"
else
    echo "❌ Import failed! Exiting."
    exit 1
fi
echo ""

echo "🔍 Step 5: Verifying data..."
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
SELECT 'invitees', COUNT(*) FROM invitees;
"
echo ""

echo "🔄 Step 6: Restarting production application..."
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

sleep 5
if ps -p $(cat tanishq-prod.pid) > /dev/null; then
    echo "✅ Production application restarted (PID: $(cat tanishq-prod.pid))"
else
    echo "⚠️  Application may not have started. Check logs."
fi
echo ""

echo "=========================================="
echo "✅ MIGRATION COMPLETE!"
echo "=========================================="
echo ""
echo "📊 Summary:"
echo "  - Pre-prod backup: $BACKUP_DIR/selfie_prod_before_migration_$TIMESTAMP.sql"
echo "  - Pre-prod export: $BACKUP_DIR/preprod_export_$TIMESTAMP.sql"
echo "  - Production PID: $(cat /opt/tanishq/tanishq-prod.pid)"
echo ""
echo "🌐 Test your production site now:"
echo "  https://celebrations.tanishq.co.in/"
echo ""
echo "📝 Check logs if needed:"
echo "  tail -f /opt/tanishq/logs/application.log"
echo ""
```

---

## ⚡ QUICK MIGRATION (Manual Commands)

If you want to run commands manually:

### 1. Create Backup Directory
```bash
mkdir -p /opt/tanishq/backups
```

### 2. Backup Production (Safety)
```bash
mysqldump -u root -p selfie_prod > /opt/tanishq/backups/prod_backup_$(date +%Y%m%d_%H%M%S).sql
```

### 3. Export Pre-Prod
```bash
mysqldump -u root -p selfie_preprod > /opt/tanishq/backups/preprod_export_$(date +%Y%m%d_%H%M%S).sql
```

### 4. Import to Production
```bash
# Find the exported file
ls -lt /opt/tanishq/backups/preprod_export_*.sql | head -1

# Import (use the actual filename)
mysql -u root -p selfie_prod < /opt/tanishq/backups/preprod_export_XXXXXX.sql
```

### 5. Verify
```bash
mysql -u root -p -e "USE selfie_prod; SELECT COUNT(*) FROM events; SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM attendees;"
```

### 6. Restart App
```bash
kill $(cat /opt/tanishq/tanishq-prod.pid)
sleep 3
cd /opt/tanishq && nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid
```

---

## 🔍 METHOD 2: SELECTIVE MIGRATION (Advanced)

If you want to copy only specific data:

### Export Specific Tables Only
```bash
# Export only specific tables
mysqldump -u root -p selfie_preprod events attendees bride_details users > /opt/tanishq/backups/selective_export.sql
```

### Import Specific Tables
```bash
mysql -u root -p selfie_prod < /opt/tanishq/backups/selective_export.sql
```

---

## 🔄 METHOD 3: INCREMENTAL SYNC (If Both DBs Have Data)

If you want to merge data without losing production data:

### Export with INSERT IGNORE
```bash
mysqldump -u root -p --insert-ignore --no-create-info selfie_preprod > /opt/tanishq/backups/merge_export.sql
```

This will insert records that don't exist, skip duplicates.

---

## 📊 TABLES TO MIGRATE

Based on your database, these tables will be copied:

```
✅ events               - All events
✅ attendees            - Event attendees
✅ invitees             - Event invitees
✅ bride_details        - Bride information
✅ greetings            - Video greetings
✅ users                - User accounts
✅ user_details         - User profiles
✅ stores               - Store information
✅ product_details      - Product catalog
✅ rbm_login            - RBM user credentials
✅ cee_login            - CEE user credentials
✅ abm_login            - ABM user credentials
✅ rivaah               - Rivaah data
✅ rivaah_users         - Rivaah users
✅ password_history     - Password history
```

---

## ⚠️ IMPORTANT CONSIDERATIONS

### 1. Storage Files (Images, Uploads)
Database migration only copies database records, NOT files!

You also need to copy storage files:

```bash
# Copy storage files from pre-prod to production
# (Adjust paths if pre-prod is on different server)

# If on same server:
rsync -av /opt/preprod/storage/ /opt/tanishq/storage/

# If on different servers:
# On pre-prod server:
tar -czf /tmp/preprod_storage.tar.gz /opt/preprod/storage/
# Copy to production server
# On production server:
tar -xzf /tmp/preprod_storage.tar.gz -C /opt/tanishq/
```

### 2. S3 Bucket
If images are in S3, you need to copy between buckets:

```bash
# Using AWS CLI
aws s3 sync s3://celebrations-tanishq-preprod s3://celebrations-tanishq-prod
```

### 3. Google Sheets References
If pre-prod data references Google Sheets, those references will work in production (same sheets).

### 4. Test Data vs Real Data
Pre-prod might have test events, test users, etc. Consider:
- Do you want test data in production?
- Should you clean test data first?
- Do you need to update any configuration?

---

## 📝 POST-MIGRATION CHECKLIST

After migration:

- [ ] ✅ Verify record counts match
- [ ] ✅ Test user login works
- [ ] ✅ Test event creation
- [ ] ✅ Test image uploads
- [ ] ✅ Test QR code generation
- [ ] ✅ Check all APIs work
- [ ] ✅ Verify email sending
- [ ] ✅ Check Google Sheets sync
- [ ] ✅ Test complete user flow

---

## 🐛 TROUBLESHOOTING

### If Migration Fails:

**Restore Production Backup:**
```bash
mysql -u root -p -e "DROP DATABASE selfie_prod; CREATE DATABASE selfie_prod;"
mysql -u root -p selfie_prod < /opt/tanishq/backups/selfie_prod_before_migration_XXXXXX.sql
```

### If App Won't Start After Migration:

**Check Logs:**
```bash
tail -100 /opt/tanishq/logs/application.log
```

**Restart App:**
```bash
kill $(cat /opt/tanishq/tanishq-prod.pid)
sleep 3
cd /opt/tanishq && nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid
```

---

## 🎯 RECOMMENDATION

**For Production Launch:**

1. **If pre-prod has real data:** Use full migration
2. **If pre-prod has test data:** Clean test data first, then migrate
3. **If starting fresh:** Skip migration, let real users create data

**Safest Approach:**
1. Backup everything first
2. Migrate data
3. Test thoroughly
4. Keep backups for 30 days

---

## 📞 QUICK COMMANDS

### One-Command Full Migration:
```bash
mkdir -p /opt/tanishq/backups && mysqldump -u root -p selfie_preprod > /opt/tanishq/backups/preprod_export_$(date +%Y%m%d_%H%M%S).sql && mysql -u root -p selfie_prod < $(ls -t /opt/tanishq/backups/preprod_export_*.sql | head -1) && echo "Migration complete!"
```

### Verify After Migration:
```bash
mysql -u root -p -e "USE selfie_prod; SELECT 'events' as tbl, COUNT(*) as cnt FROM events UNION SELECT 'users', COUNT(*) FROM users UNION SELECT 'attendees', COUNT(*) FROM attendees;"
```

---

**Ready to migrate? Follow the script above for safe data migration!** 🚀

