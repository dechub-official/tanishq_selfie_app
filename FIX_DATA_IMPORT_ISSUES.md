# 🔧 Data Import Issue - Diagnosis & Fix Guide

**Problem:** Old data imported from Excel (Nov/Oct) has multiple issues:
- Missing event names (showing as "-")
- Date mismatches
- Old data not viewable
- Data doesn't match Excel sheet

---

## 🔍 STEP 1: Diagnose the Issues

### Run This SQL Query First to Identify Problems:

```sql
USE selfie_preprod;

-- 1. Check events with missing names
SELECT 'Events with Missing Names' AS Issue,
       COUNT(*) AS Count
FROM events 
WHERE event_name IS NULL OR event_name = '' OR event_name = '-';

-- Show sample of problematic events
SELECT 'Sample Events with Missing Names:' AS '';
SELECT id, store_code, start_date, event_name, created_at
FROM events 
WHERE event_name IS NULL OR event_name = '' OR event_name = '-'
ORDER BY created_at DESC
LIMIT 10;

-- 2. Check events with NULL or empty critical fields
SELECT 'Events with NULL Fields' AS '';
SELECT 
    'Missing event_name' AS Field,
    COUNT(*) AS Count
FROM events WHERE event_name IS NULL OR event_name = ''
UNION ALL
SELECT 'Missing store_code', COUNT(*) FROM events WHERE store_code IS NULL
UNION ALL
SELECT 'Missing start_date', COUNT(*) FROM events WHERE start_date IS NULL
UNION ALL
SELECT 'Missing event_type', COUNT(*) FROM events WHERE event_type IS NULL;

-- 3. Check events imported from Excel (likely have is_uploaded_from_excel flag or recent created_at)
SELECT 'Old Imported Events (Nov-Dec 2025)' AS '';
SELECT 
    DATE_FORMAT(STR_TO_DATE(start_date, '%d/%m/%Y'), '%Y-%m') AS Month,
    COUNT(*) AS Events,
    SUM(CASE WHEN event_name IS NULL OR event_name = '' OR event_name = '-' THEN 1 ELSE 0 END) AS Missing_Names,
    SUM(CASE WHEN attendees = 0 THEN 1 ELSE 0 END) AS Zero_Attendees
FROM events
WHERE start_date LIKE '%/11/2025' OR start_date LIKE '%/10/2025'
GROUP BY Month;

-- 4. Check store codes that don't exist in stores table
SELECT 'Events with Invalid Store Codes' AS '';
SELECT e.id, e.store_code, e.event_name, e.start_date
FROM events e
WHERE e.store_code NOT IN (SELECT store_code FROM stores)
LIMIT 10;

-- 5. Check date format issues
SELECT 'Date Format Analysis' AS '';
SELECT 
    start_date,
    LENGTH(start_date) AS date_length,
    COUNT(*) AS count
FROM events
GROUP BY start_date, LENGTH(start_date)
HAVING COUNT(*) > 1
ORDER BY count DESC
LIMIT 10;
```

---

## 🛠️ STEP 2: Fix the Issues

### Option A: Fix Existing Data (If data is partially correct)

```sql
USE selfie_preprod;

-- Backup first!
-- Run this before making changes
CREATE TABLE events_backup_20260124 AS SELECT * FROM events;

-- Fix 1: Update events with missing names to have a default name based on event_type
UPDATE events 
SET event_name = CONCAT(COALESCE(event_type, 'Event'), ' - ', store_code, ' - ', start_date)
WHERE event_name IS NULL OR event_name = '' OR event_name = '-';

-- Fix 2: Fix date format if needed (if dates are in wrong format)
-- This assumes dates are in dd/mm/yyyy or dd-mm-yyyy format
UPDATE events
SET start_date = DATE_FORMAT(STR_TO_DATE(start_date, '%d/%m/%Y'), '%d-%m-%Y')
WHERE start_date LIKE '%/%';

-- Fix 3: Set default values for NULL fields
UPDATE events SET event_type = 'General Event' WHERE event_type IS NULL OR event_type = '';
UPDATE events SET region = 'Unknown' WHERE region IS NULL OR region = '';
UPDATE events SET attendees = 0 WHERE attendees IS NULL;
UPDATE events SET invitees = 0 WHERE invitees IS NULL;
UPDATE events SET sale = 0 WHERE sale IS NULL;

-- Fix 4: Remove events with invalid store codes (if any)
-- First, let's see which ones will be affected
SELECT COUNT(*) FROM events WHERE store_code NOT IN (SELECT store_code FROM stores);

-- If you want to delete them (BE CAREFUL):
-- DELETE FROM events WHERE store_code NOT IN (SELECT store_code FROM stores);

-- Or update them to a valid store code if you know the correct mapping
-- UPDATE events SET store_code = 'CORRECT_CODE' WHERE store_code = 'WRONG_CODE';
```

### Option B: Clean Slate - Delete Old Imported Data and Re-import Correctly

```sql
USE selfie_preprod;

-- Backup first!
CREATE TABLE events_backup_20260124 AS SELECT * FROM events;
CREATE TABLE attendees_backup_20260124 AS SELECT * FROM attendees;
CREATE TABLE invitees_backup_20260124 AS SELECT * FROM invitees;

-- Delete old problematic data (Nov-Dec 2025)
-- BE VERY CAREFUL WITH THIS!

-- 1. Delete attendees for these events first (foreign key constraint)
DELETE FROM attendees 
WHERE event_id IN (
    SELECT id FROM events 
    WHERE start_date LIKE '%/11/2025' OR start_date LIKE '%/10/2025'
);

-- 2. Delete invitees for these events
DELETE FROM invitees 
WHERE event_id IN (
    SELECT id FROM events 
    WHERE start_date LIKE '%/11/2025' OR start_date LIKE '%/10/2025'
);

-- 3. Delete the events themselves
DELETE FROM events 
WHERE start_date LIKE '%/11/2025' OR start_date LIKE '%/10/2025';

-- Now you can re-import the corrected Excel data through the application
```

---

## 📊 STEP 3: Verify the Fix

```sql
USE selfie_preprod;

-- Check if issues are resolved
SELECT 'After Fix - Events with Missing Names' AS Status,
       COUNT(*) AS Count
FROM events 
WHERE event_name IS NULL OR event_name = '' OR event_name = '-';

-- Check recent events
SELECT id, event_name, store_code, start_date, attendees, invitees
FROM events
ORDER BY created_at DESC
LIMIT 20;

-- Check data integrity
SELECT 
    COUNT(*) AS total_events,
    SUM(CASE WHEN event_name IS NOT NULL AND event_name != '' AND event_name != '-' THEN 1 ELSE 0 END) AS events_with_names,
    SUM(attendees) AS total_attendees,
    SUM(invitees) AS total_invitees
FROM events;
```

---

## 🎯 RECOMMENDED SOLUTION FOR YOU:

Based on your screenshots, I recommend **Option B (Clean Slate)** because:

1. ✓ Too many corrupted records
2. ✓ Missing critical data (event names)
3. ✓ Old data not viewable (likely ID/structure mismatch)
4. ✓ Easier than fixing hundreds of records manually

### Here's the Exact Steps:

#### Step 1: Backup Current Database
```bash
mysqldump -u root -pDechub#2025 selfie_preprod > backup_before_cleanup_20260124.sql
```

#### Step 2: Run Diagnostic Queries (STEP 1 above)
- This will show you exactly what's wrong
- Save the output to compare after fix

#### Step 3: Delete Problematic Old Data
- Run the DELETE queries from Option B
- This removes Nov/Oct imported data

#### Step 4: Prepare Clean Excel Data
- Review your Excel sheet
- Ensure these columns are present and filled:
  * Event Name (REQUIRED - no blanks)
  * Store Code (REQUIRED - must match stores table)
  * Event Date (format: DD-MM-YYYY or DD/MM/YYYY)
  * Event Type
  * Region
  * Invitees Count
  * Attendees Count (if known)

#### Step 5: Re-import Through Application
- Use the application's CSV upload feature
- Follow the format from "Download sample format"
- Import store by store or in batches

#### Step 6: Verify
- Run verification queries (STEP 3)
- Check dashboard - events should show properly
- Test "View Data" function

---

## 📝 Excel to Database Column Mapping

Make sure your Excel has these exact columns:

| Excel Column | Database Column | Required | Notes |
|--------------|----------------|----------|-------|
| Event Name | event_name | ✓ | Must not be blank |
| Store Code | store_code | ✓ | Must exist in stores table |
| Event Date | start_date | ✓ | Format: DD-MM-YYYY |
| Event Type | event_type | ✓ | e.g., "Wedding", "Festival" |
| Region | region | - | North1, South2, etc. |
| No. of Invitees | invitees | - | Number |
| No. of Attendees | attendees | - | Number |
| RSO Name | rso | - | Text |
| Location | location | - | Text |
| Community | community | - | Text |

---

## ⚠️ IMPORTANT: Before You Start

1. **BACKUP FIRST!** Always backup before deleting data
2. **Test on one store** - Try with one store's data first
3. **Verify store codes** - Ensure all store codes in Excel exist in database
4. **Check date formats** - Use consistent date format (DD-MM-YYYY recommended)
5. **Don't use "-" or blank** - Fill all required fields properly

---

## 🚨 Common Mistakes in Excel Import

❌ **Avoid These:**
- Blank event names
- Using "-" as placeholder
- Wrong date formats (YYYY-MM-DD vs DD-MM-YYYY)
- Store codes that don't exist
- Mixing date formats (some DD/MM, some MM/DD)
- Special characters in names
- Extra spaces in columns

✅ **Do This Instead:**
- Fill all event names properly
- Use consistent date format
- Verify store codes beforehand
- Clean data in Excel first
- Test with small batch first

---

## 📞 Need Help Running These?

If you need help executing these SQL queries:

1. Open MySQL Workbench
2. Connect to: localhost:3306 / selfie_preprod
3. Copy-paste the queries
4. Execute one section at a time
5. Review results before proceeding

---

**Created:** January 24, 2026  
**For:** Tanishq Selfie App - Data Import Issues  
**Status:** Ready to Execute

