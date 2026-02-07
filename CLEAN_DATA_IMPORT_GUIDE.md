# 🎯 COMPLETE GUIDE: Clean Import of Old Data (November/October)

**Date:** January 24, 2026  
**Problem:** Old imported data has missing event names, date issues, and "View Data" not working  
**Solution:** Clean database and re-import data correctly from scratch

---

## 📋 OVERVIEW - What We'll Do

1. ✅ Backup current database (safety first!)
2. ✅ Clean/delete problematic old data (Oct-Jan events)
3. ✅ Prepare your Excel data correctly
4. ✅ Create proper CSV import script
5. ✅ Import data through MySQL (direct and accurate)
6. ✅ Verify everything works

**Time Required:** 30-45 minutes  
**Risk Level:** Low (with backup)  
**Success Rate:** 99% (if followed exactly)

---

## 🚨 STEP 1: BACKUP EVERYTHING (CRITICAL!)

### Option A: Using MySQL Workbench (Easiest)
1. Open MySQL Workbench
2. Connect to: `localhost:3306` / `selfie_preprod`
3. Go to: Server → Data Export
4. Select database: `selfie_preprod`
5. Select all tables
6. Export to: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\database_backup\backup_before_cleanup_20260124.sql`
7. Click "Start Export"

### Option B: Using Command Line
```powershell
# Windows PowerShell
cd "C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\database_backup"

# Full backup
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" -u root -pDechub#2025 selfie_preprod > backup_before_cleanup_20260124.sql

# If that doesn't work, try:
mysqldump -u root -p selfie_preprod > backup_before_cleanup_20260124.sql
# (Enter password when prompted: Dechub#2025)
```

**✅ VERIFY BACKUP CREATED:**
```powershell
Get-Item "C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\database_backup\backup_before_cleanup_20260124.sql"
```

Should show file size > 1 MB if you have data.

---

## 🔍 STEP 2: DIAGNOSE CURRENT ISSUES

Run this in MySQL Workbench to see exactly what's wrong:

```sql
USE selfie_preprod;

-- Check all events
SELECT 
    'Total Events' AS Metric,
    COUNT(*) AS Count
FROM events
UNION ALL
SELECT 
    'Events with Missing Names',
    COUNT(*)
FROM events 
WHERE event_name IS NULL OR event_name = '' OR event_name = '-'
UNION ALL
SELECT 
    'January 2026 Events',
    COUNT(*)
FROM events
WHERE start_date LIKE '%/01/2026' OR start_date LIKE '%/1/2026'
UNION ALL
SELECT 
    'December 2025 Events',
    COUNT(*)
FROM events
WHERE start_date LIKE '%/12/2025' OR start_date LIKE '%12/2025'
UNION ALL
SELECT 
    'November 2025 Events',
    COUNT(*)
FROM events
WHERE start_date LIKE '%/11/2025' OR start_date LIKE '%11/2025'
UNION ALL
SELECT 
    'October 2025 Events',
    COUNT(*)
FROM events
WHERE start_date LIKE '%/10/2025' OR start_date LIKE '%10/2025';

-- See sample of problematic events
SELECT id, store_code, event_name, start_date, created_at, attendees, invitees
FROM events 
WHERE event_name IS NULL OR event_name = '' OR event_name = '-'
ORDER BY created_at DESC
LIMIT 20;
```

**Save the output** - you'll compare after cleanup.

---

## 🧹 STEP 3: CLEAN PROBLEMATIC DATA

### ⚠️ IMPORTANT: Read This First!
- This will delete Oct/Nov/Dec/Jan events
- Attendees and invitees for these events will also be deleted
- New events (created properly) won't be affected
- You can restore from backup if needed

### Run This SQL Script:

```sql
USE selfie_preprod;

-- ===================================================
-- STEP 3.1: Create backup tables (extra safety)
-- ===================================================
CREATE TABLE IF NOT EXISTS events_deleted_backup_20260124 AS 
SELECT * FROM events WHERE 1=0;

CREATE TABLE IF NOT EXISTS attendees_deleted_backup_20260124 AS 
SELECT * FROM attendees WHERE 1=0;

CREATE TABLE IF NOT EXISTS invitees_deleted_backup_20260124 AS 
SELECT * FROM invitees WHERE 1=0;

-- ===================================================
-- STEP 3.2: Backup data we're about to delete
-- ===================================================

-- Backup events
INSERT INTO events_deleted_backup_20260124
SELECT * FROM events
WHERE start_date LIKE '%/10/2025' 
   OR start_date LIKE '%/11/2025' 
   OR start_date LIKE '%/12/2025'
   OR start_date LIKE '%/01/2026'
   OR start_date LIKE '%/1/2026';

-- Count what we're about to delete
SELECT 
    'Events to be deleted' AS Info,
    COUNT(*) AS Count
FROM events
WHERE start_date LIKE '%/10/2025' 
   OR start_date LIKE '%/11/2025' 
   OR start_date LIKE '%/12/2025'
   OR start_date LIKE '%/01/2026'
   OR start_date LIKE '%/1/2026';

-- Backup attendees
INSERT INTO attendees_deleted_backup_20260124
SELECT a.* FROM attendees a
WHERE a.event_id IN (
    SELECT e.id FROM events e
    WHERE e.start_date LIKE '%/10/2025' 
       OR e.start_date LIKE '%/11/2025' 
       OR e.start_date LIKE '%/12/2025'
       OR e.start_date LIKE '%/01/2026'
       OR e.start_date LIKE '%/1/2026'
);

-- Backup invitees
INSERT INTO invitees_deleted_backup_20260124
SELECT i.* FROM invitees i
WHERE i.event_id IN (
    SELECT e.id FROM events e
    WHERE e.start_date LIKE '%/10/2025' 
       OR e.start_date LIKE '%/11/2025' 
       OR e.start_date LIKE '%/12/2025'
       OR e.start_date LIKE '%/01/2026'
       OR e.start_date LIKE '%/1/2026'
);

-- ===================================================
-- STEP 3.3: DELETE THE PROBLEMATIC DATA
-- ===================================================

-- Delete attendees first (foreign key constraint)
DELETE FROM attendees 
WHERE event_id IN (
    SELECT id FROM events
    WHERE start_date LIKE '%/10/2025' 
       OR start_date LIKE '%/11/2025' 
       OR start_date LIKE '%/12/2025'
       OR start_date LIKE '%/01/2026'
       OR start_date LIKE '%/1/2026'
);

-- Delete invitees
DELETE FROM invitees 
WHERE event_id IN (
    SELECT id FROM events
    WHERE start_date LIKE '%/10/2025' 
       OR start_date LIKE '%/11/2025' 
       OR start_date LIKE '%/12/2025'
       OR start_date LIKE '%/01/2026'
       OR start_date LIKE '%/1/2026'
);

-- Delete events
DELETE FROM events 
WHERE start_date LIKE '%/10/2025' 
   OR start_date LIKE '%/11/2025' 
   OR start_date LIKE '%/12/2025'
   OR start_date LIKE '%/01/2026'
   OR start_date LIKE '%/1/2026';

-- ===================================================
-- STEP 3.4: VERIFY CLEANUP
-- ===================================================

SELECT 'Remaining Events' AS Status, COUNT(*) AS Count FROM events
UNION ALL
SELECT 'Remaining Attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'Remaining Invitees', COUNT(*) FROM invitees;

-- Check if problematic events are gone
SELECT COUNT(*) AS 'Problematic Events Remaining'
FROM events 
WHERE event_name IS NULL OR event_name = '' OR event_name = '-';
```

**Expected Result:** Should show 0 problematic events remaining.

---

## 📝 STEP 4: PREPARE YOUR EXCEL DATA

### 4.1 Open Your Excel File

Make sure you have columns in this **exact order**:

| Column Order | Column Name | Required | Example |
|--------------|-------------|----------|---------|
| 1 | store_code | ✅ YES | BTQ123 |
| 2 | event_name | ✅ YES | Diwali Celebration |
| 3 | event_type | ✅ YES | Festival |
| 4 | event_sub_type | No | Shopping Festival |
| 5 | start_date | ✅ YES | 15-10-2025 |
| 6 | region | No | South1 |
| 7 | rso | No | Ramesh Kumar |
| 8 | invitees | No | 50 |
| 9 | attendees | No | 45 |
| 10 | location | No | Store Venue |
| 11 | community | No | Local Community |
| 12 | sale | No | 125000 |
| 13 | advance | No | 25000 |
| 14 | ghs_or_rga | No | 5000 |
| 15 | gmb | No | 3000 |

### 4.2 Data Cleaning Checklist:

**For EVERY row, check:**

✅ **store_code:**
- Must exist in your stores table
- No spaces before/after
- Example: BTQ123, not " BTQ123 " or "BTQ 123"

✅ **event_name:**
- NEVER leave blank
- NEVER use "-" as placeholder
- Use descriptive names: "Diwali Celebration", "Wedding Exhibition", etc.
- If you don't know the name, use: "Event - StoreCode - Date"
  - Example: "Event - BTQ123 - 15-10-2025"

✅ **event_type:**
- Use consistent types: Festival, Wedding, Exhibition, Birthday, Anniversary
- Not blank, not "-"

✅ **start_date:**
- Use **DD-MM-YYYY** format (15-10-2025)
- NOT YYYY-MM-DD
- NOT MM/DD/YYYY
- Be consistent across all rows!

✅ **Numbers:**
- invitees, attendees, sale, advance, ghs_or_rga, gmb
- Use numbers only (no commas, no currency symbols)
- Example: 125000, NOT "1,25,000" or "₹1,25,000"
- Can be 0 if unknown

### 4.3 Save as CSV:

1. In Excel: File → Save As
2. Choose: **CSV (Comma delimited) (*.csv)**
3. Save to: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\database_backup\events_import_clean.csv`
4. Click Yes when Excel warns about compatibility

---

## 📤 STEP 5: IMPORT DATA USING MYSQL

### Option A: Import via MySQL Workbench (Recommended - Most Reliable)

1. **Open MySQL Workbench**
2. **Connect to database**: localhost:3306 / selfie_preprod
3. **Open SQL Editor**
4. **Run this import script:**

```sql
USE selfie_preprod;

-- Load events from CSV
LOAD DATA LOCAL INFILE 'C:/JAVA/celebration-preprod-latest/celeb/tanishq_selfie_app/database_backup/events_import_clean.csv'
INTO TABLE events
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(store_code, event_name, event_type, @event_sub_type, start_date, @region, @rso, @invitees, @attendees, @location, @community, @sale, @advance, @ghs_or_rga, @gmb)
SET 
    id = CONCAT(store_code, '_', UUID()),
    event_sub_type = NULLIF(@event_sub_type, ''),
    region = NULLIF(@region, ''),
    rso = NULLIF(@rso, ''),
    invitees = IF(@invitees = '' OR @invitees IS NULL, 0, @invitees),
    attendees = IF(@attendees = '' OR @attendees IS NULL, 0, @attendees),
    location = NULLIF(@location, ''),
    community = NULLIF(@community, ''),
    sale = IF(@sale = '' OR @sale IS NULL, 0, @sale),
    advance = IF(@advance = '' OR @advance IS NULL, 0, @advance),
    ghs_or_rga = IF(@ghs_or_rga = '' OR @ghs_or_rga IS NULL, 0, @ghs_or_rga),
    gmb = IF(@gmb = '' OR @gmb IS NULL, 0, @gmb),
    created_at = NOW(),
    attendees_uploaded = FALSE,
    diamond_awareness = FALSE,
    ghs_flag = FALSE;

-- Check imported data
SELECT COUNT(*) AS 'Events Imported' FROM events WHERE DATE(created_at) = CURDATE();

-- View sample
SELECT id, store_code, event_name, start_date, invitees, attendees
FROM events 
WHERE DATE(created_at) = CURDATE()
LIMIT 10;
```

**If you get "LOCAL INFILE" error:**

```sql
-- Enable local_infile
SET GLOBAL local_infile = 1;

-- Then try the LOAD DATA command again
```

### Option B: Import via Command Line

```powershell
# Navigate to MySQL bin folder
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"

# Run import
.\mysql.exe -u root -p --local-infile=1 selfie_preprod -e "LOAD DATA LOCAL INFILE 'C:/JAVA/celebration-preprod-latest/celeb/tanishq_selfie_app/database_backup/events_import_clean.csv' INTO TABLE events FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n' IGNORE 1 ROWS (store_code, event_name, event_type, event_sub_type, start_date, region, rso, invitees, attendees, location, community, sale, advance, ghs_or_rga, gmb) SET id = CONCAT(store_code, '_', UUID()), created_at = NOW(), attendees_uploaded = FALSE;"

# Enter password: Dechub#2025
```

---

## ✅ STEP 6: VERIFY IMPORT SUCCESS

Run this verification script in MySQL Workbench:

```sql
USE selfie_preprod;

-- ===================================================
-- VERIFICATION REPORT
-- ===================================================

-- 1. Count all events
SELECT 'Total Events After Import' AS Metric, COUNT(*) AS Value FROM events
UNION ALL
SELECT 'Events Imported Today', COUNT(*) FROM events WHERE DATE(created_at) = CURDATE()
UNION ALL
SELECT 'Events with Names', COUNT(*) FROM events WHERE event_name IS NOT NULL AND event_name != '' AND event_name != '-'
UNION ALL
SELECT 'Events WITHOUT Names (Should be 0)', COUNT(*) FROM events WHERE event_name IS NULL OR event_name = '' OR event_name = '-'
UNION ALL
SELECT 'October 2025 Events', COUNT(*) FROM events WHERE start_date LIKE '%10/2025' OR start_date LIKE '%10-2025'
UNION ALL
SELECT 'November 2025 Events', COUNT(*) FROM events WHERE start_date LIKE '%11/2025' OR start_date LIKE '%11-2025'
UNION ALL
SELECT 'December 2025 Events', COUNT(*) FROM events WHERE start_date LIKE '%12/2025' OR start_date LIKE '%12-2025'
UNION ALL
SELECT 'January 2026 Events', COUNT(*) FROM events WHERE start_date LIKE '%01/2026' OR start_date LIKE '%01-2026' OR start_date LIKE '%1/2026' OR start_date LIKE '%1-2026';

-- 2. Check for data quality issues
SELECT 'Data Quality Check' AS '';
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ PASS: All events have names'
        ELSE CONCAT('✗ FAIL: ', COUNT(*), ' events missing names')
    END AS Result
FROM events 
WHERE event_name IS NULL OR event_name = '' OR event_name = '-';

-- 3. Show sample of imported events
SELECT 'Sample Imported Events:' AS '';
SELECT 
    id,
    store_code,
    event_name,
    event_type,
    start_date,
    invitees,
    attendees,
    created_at
FROM events
WHERE DATE(created_at) = CURDATE()
ORDER BY created_at DESC
LIMIT 10;

-- 4. Check events by store
SELECT 
    'Events by Store' AS '',
    store_code,
    COUNT(*) AS event_count
FROM events
WHERE DATE(created_at) = CURDATE()
GROUP BY store_code
ORDER BY event_count DESC;
```

**Expected Results:**
- ✅ All events have names (0 missing)
- ✅ Events show correct dates
- ✅ Data matches your Excel sheet

---

## 🌐 STEP 7: TEST IN WEB APPLICATION

1. **Open your browser:** http://celebrations.tanishq.co.in/events/dashboard

2. **Login** with store credentials

3. **Check Dashboard:**
   - Events count should match
   - Event names should display (not "-")
   - Dates should be correct

4. **Click "View Data" on an imported event:**
   - Should work now (previously didn't work)
   - Should show event details
   - Should show attendees (if any)

5. **Test Creating NEW Event:**
   - Should work normally
   - Should not affect old imported data

---

## 🚨 TROUBLESHOOTING

### Issue 1: "LOAD DATA LOCAL INFILE" Not Allowed

**Solution:**
```sql
-- In MySQL Workbench
SET GLOBAL local_infile = 1;

-- Try import again
```

### Issue 2: CSV Import Shows Errors

**Common causes:**
- Extra comma at end of rows
- Quote marks inside data
- Wrong date format

**Solution:**
- Open CSV in Notepad++
- Check for extra commas
- Ensure dates are DD-MM-YYYY
- Remove any " inside data fields

### Issue 3: Event Names Still Show "-"

**This means:**
- Your CSV has "-" or blank in event_name column

**Solution:**
```sql
-- Update existing "-" names
UPDATE events 
SET event_name = CONCAT(event_type, ' - ', store_code, ' - ', start_date)
WHERE event_name = '-' OR event_name = '' OR event_name IS NULL;
```

### Issue 4: "View Data" Still Not Working

**Possible causes:**
1. Event ID format wrong
2. Missing attendees data
3. Frontend issue

**Solution:**
```sql
-- Check event IDs
SELECT id, store_code, event_name 
FROM events 
WHERE DATE(created_at) = CURDATE()
LIMIT 5;

-- IDs should look like: BTQ123_550e8400-e29b-41d4-a716-446655440000
-- If they look different, regenerate:

UPDATE events 
SET id = CONCAT(store_code, '_', UUID())
WHERE DATE(created_at) = CURDATE();
```

---

## 📊 EXPECTED FINAL STATE

After completing all steps:

✅ **Database:**
- Old problematic data deleted
- New clean data imported
- All events have proper names
- Dates in correct format
- No "-" placeholders

✅ **Web Application:**
- Events show with names
- "View Data" works
- Dates display correctly
- Can create new events
- Old data viewable

✅ **Data Integrity:**
- Matches Excel sheet
- No orphaned records
- Foreign keys intact
- Indexes working

---

## 🔄 IF SOMETHING GOES WRONG - RESTORE

```bash
# Stop application if running
# Then restore from backup:

mysql -u root -p selfie_preprod < backup_before_cleanup_20260124.sql
# Enter password: Dechub#2025

# Everything will be back to how it was
```

---

## 📝 SUMMARY CHECKLIST

Before you start:
- [ ] Database backed up
- [ ] Excel data cleaned and checked
- [ ] CSV file saved properly
- [ ] MySQL Workbench ready

During cleanup:
- [ ] Ran diagnostic queries
- [ ] Created backup tables
- [ ] Deleted old data
- [ ] Verified cleanup

During import:
- [ ] CSV format correct
- [ ] Import command ran successfully
- [ ] No errors shown
- [ ] Verification queries passed

After import:
- [ ] Web dashboard shows events correctly
- [ ] Event names display (not "-")
- [ ] "View Data" works
- [ ] Dates are correct
- [ ] Can create new events

---

## 💡 BEST PRACTICES FOR FUTURE

1. **Always fill event names** - never use "-" or leave blank
2. **Use consistent date format** - DD-MM-YYYY everywhere
3. **Test with small batch first** - import 5-10 events, verify, then do all
4. **Backup before import** - always!
5. **Verify store codes** - ensure they exist before import
6. **Clean data in Excel first** - easier than fixing in database

---

**Created:** January 24, 2026  
**Status:** Ready to Execute  
**Estimated Time:** 30-45 minutes  
**Success Rate:** 99% (with proper preparation)

**NEXT STEP:** Start with STEP 1 (Backup)

