# 🔍 Database Verification Guide

**Complete guide to verify your MySQL database after making changes**

---

## 📋 What Gets Verified

The verification tools check:
- ✓ All 15 required tables exist
- ✓ Table structures are correct
- ✓ Foreign key relationships are intact
- ✓ No orphaned records (data integrity)
- ✓ Indexes are properly configured
- ✓ No duplicate records in unique columns
- ✓ Database size and performance metrics

---

## 🚀 Quick Start - 3 Ways to Verify

### Method 1: Windows BAT File (Easiest)
**For Windows users - Double-click to run**

1. Double-click: `VERIFY_DATABASE.bat`
2. Wait for verification to complete
3. Review the results on screen

**Requirements:**
- MySQL must be installed
- MySQL bin folder must be in system PATH

---

### Method 2: MySQL Workbench or Command Line (Most Reliable)
**Best for detailed analysis**

#### Using MySQL Workbench:
1. Open MySQL Workbench
2. Connect to your database:
   - Host: `localhost`
   - Port: `3306`
   - User: `root`
   - Password: `Dechub#2025`
   - Database: `selfie_preprod`

3. Open file: `verify_database.sql`
4. Click "Execute" (⚡ lightning bolt icon)
5. Review results in tabs below

#### Using MySQL Command Line:
```bash
# Connect to database
mysql -u root -p selfie_preprod

# Run verification script
source verify_database.sql

# Or in one command:
mysql -u root -p selfie_preprod < verify_database.sql
```

---

### Method 3: PowerShell Script (Advanced)
**For automation and scripting**

```powershell
# Run from PowerShell
cd "C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app"
.\verify_database.ps1
```

Or on Linux/Mac:
```bash
chmod +x verify_database.sh
./verify_database.sh
```

---

## 📊 Understanding the Results

### ✅ Good Results Look Like This:

```
✓ Database connection successful
✓ All 15 tables verified successfully!
✓ No orphaned attendees found
✓ No orphaned invitees found
✓ All events have valid store codes
✓ Database Structure: VALID
✓ Data Integrity: GOOD
```

### ⚠️ Warning Signs to Watch For:

```
⚠ Found 5 orphaned attendee records
⚠ Found 2 events with invalid store codes
⚠ Data Integrity: ISSUES FOUND
```

**What to do:** Check the specific issues and clean up data

### ❌ Critical Issues:

```
✗ Table 'events' MISSING
✗ Database connection failed
✗ Database Structure: INCOMPLETE
```

**What to do:** Database needs repair or restore from backup

---

## 🔍 Detailed Verification Checklist

### 1. Table Existence (15 tables required)

**Core Event Management:**
- [ ] events
- [ ] stores
- [ ] attendees
- [ ] invitees
- [ ] greetings

**User Management:**
- [ ] users
- [ ] user_details
- [ ] bride_details
- [ ] password_history

**Authentication:**
- [ ] abm_login
- [ ] rbm_login
- [ ] cee_login

**Product/Rivaah:**
- [ ] rivaah
- [ ] rivaah_users
- [ ] product_details

### 2. Data Integrity Checks

**Orphaned Records:**
- [ ] No attendees without events
- [ ] No invitees without events
- [ ] No events without valid stores
- [ ] No product_details without rivaah
- [ ] No rivaah_users without rivaah

**Unique Constraints:**
- [ ] No duplicate ABM usernames
- [ ] No duplicate RBM usernames
- [ ] No duplicate CEE usernames
- [ ] No duplicate store codes

### 3. Foreign Key Relationships

**Required Relationships:**
- [ ] events.store_code → stores.store_code
- [ ] attendees.event_id → events.id
- [ ] invitees.event_id → events.id
- [ ] product_details.rivaah_id → rivaah.id
- [ ] rivaah_users.rivaah_id → rivaah.id

---

## 🛠️ Fixing Common Issues

### Issue: "Table 'xyz' MISSING"

**Solution:**
1. The application will auto-create tables on next startup
2. Or restore from backup:
   ```bash
   mysql -u root -p selfie_preprod < backup.sql
   ```

### Issue: "Orphaned records found"

**Solution:**
```sql
-- Remove orphaned attendees
DELETE FROM attendees 
WHERE event_id NOT IN (SELECT id FROM events);

-- Remove orphaned invitees
DELETE FROM invitees 
WHERE event_id NOT IN (SELECT id FROM events);
```

### Issue: "Events without valid store codes"

**Solution:**
```sql
-- Find problematic events
SELECT id, event_name, store_code 
FROM events 
WHERE store_code NOT IN (SELECT store_code FROM stores);

-- Either update with correct store code or delete
UPDATE events SET store_code = 'CORRECT_CODE' WHERE id = 'event_id';
-- OR
DELETE FROM events WHERE store_code NOT IN (SELECT store_code FROM stores);
```

### Issue: "Duplicate usernames"

**Solution:**
```sql
-- Find duplicates
SELECT abm_user_id, COUNT(*) 
FROM abm_login 
GROUP BY abm_user_id 
HAVING COUNT(*) > 1;

-- Remove duplicates (keep most recent)
DELETE a1 FROM abm_login a1
INNER JOIN abm_login a2 
WHERE a1.id < a2.id 
AND a1.abm_user_id = a2.abm_user_id;
```

---

## 📝 Manual Verification Queries

If automated tools don't work, run these queries manually:

### Check All Tables Exist
```sql
SHOW TABLES;
```
**Expected:** 15 tables

### Count Records
```sql
SELECT 'events' as table_name, COUNT(*) FROM events
UNION ALL
SELECT 'stores', COUNT(*) FROM stores
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees;
-- ... (add all 15 tables)
```

### Check Foreign Keys
```sql
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'selfie_preprod'
AND REFERENCED_TABLE_NAME IS NOT NULL;
```

### Check for Orphans
```sql
-- Orphaned attendees
SELECT COUNT(*) FROM attendees 
WHERE event_id NOT IN (SELECT id FROM events);

-- Orphaned invitees
SELECT COUNT(*) FROM invitees 
WHERE event_id NOT IN (SELECT id FROM events);
```

---

## 🔄 When to Run Verification

### Always verify after:
- ✓ Manual database changes
- ✓ Importing CSV data
- ✓ Running migration scripts
- ✓ Restoring from backup
- ✓ Upgrading application
- ✓ Schema modifications

### Regular verification:
- Weekly: Quick table check
- Monthly: Full integrity check
- Before deployment: Complete verification

---

## 💾 Backup Before Changes

**Always backup before making changes:**

```bash
# Backup entire database
mysqldump -u root -pDechub#2025 selfie_preprod > backup_$(date +%Y%m%d).sql

# Backup specific table
mysqldump -u root -pDechub#2025 selfie_preprod events > events_backup.sql

# Restore if needed
mysql -u root -pDechub#2025 selfie_preprod < backup_20260124.sql
```

---

## 📊 Expected Results for Healthy Database

### Table Counts (Approximate)
- **stores:** 400-600 (relatively stable)
- **events:** Growing (10,000+)
- **attendees:** Growing rapidly (100,000+)
- **invitees:** Growing (50,000+)
- **users:** 50-200 (staff only)
- **greetings:** Growing (50,000+)
- **abm_login:** 20-50
- **rbm_login:** 10-20
- **cee_login:** 50-100

### Database Size
- **Typical:** 100-500 MB
- **Large:** 500 MB - 2 GB
- **Very Large:** 2+ GB

If significantly different, investigate data issues.

---

## 🚨 Troubleshooting Verification Tools

### PowerShell Script Won't Run

**Error:** "Execution of scripts is disabled"

**Solution:**
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### MySQL Command Not Found

**Error:** "mysql is not recognized"

**Solution:**
1. Find MySQL installation:
   - `C:\Program Files\MySQL\MySQL Server 8.0\bin\`
2. Add to PATH or use full path:
   ```powershell
   & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
   ```

### Connection Refused

**Error:** "Can't connect to MySQL server"

**Solution:**
1. Check MySQL is running:
   ```powershell
   Get-Service MySQL*
   ```
2. Start if stopped:
   ```powershell
   Start-Service MySQL80
   ```

### Access Denied

**Error:** "Access denied for user"

**Solution:**
- Verify password is correct
- Check user has permissions:
  ```sql
  SHOW GRANTS FOR 'root'@'localhost';
  ```

---

## 📞 Support

### For More Help

- **Database Schema:** See `DATABASE_SCHEMA_DOCUMENTATION.md`
- **Quick SQL Queries:** See `DATABASE_QUICK_REFERENCE.md`
- **Visual Diagrams:** See `DATABASE_VISUAL_SCHEMA.md`

---

## ✅ Verification Passed - What Next?

If verification shows all green:
1. ✓ Database structure is correct
2. ✓ Data integrity is good
3. ✓ Safe to proceed with application
4. ✓ Can deploy to production

If issues found:
1. Review specific errors
2. Use "Fixing Common Issues" section
3. Re-run verification after fixes
4. Keep backup until confirmed working

---

**Verification Tools Version:** 1.0  
**Last Updated:** January 24, 2026  
**Compatible With:** MySQL 8.x, selfie_preprod/selfie_prod databases

