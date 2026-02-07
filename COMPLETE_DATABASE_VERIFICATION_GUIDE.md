# 🔍 Complete Database Verification Guide

**Date:** January 29, 2026  
**Purpose:** Verify all stores, regions, and login accounts match Google Sheets data  
**Status:** Ready to Execute

---

## 📋 Overview

This guide helps you verify:
- ✅ All stores from Google Sheets are in the database
- ✅ Region assignments are correct
- ✅ ABM/RBM/CEE login accounts exist and work
- ✅ Events have proper region data
- ✅ No data integrity issues

---

## 🚀 Quick Start

### Option 1: Run Verification Script (Recommended - Exports to Excel)

```powershell
# Navigate to project directory
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Run the PowerShell export script
.\verify_and_export_database.ps1
```

**What this does:**
- Connects to your database
- Exports all data to CSV files (can open in Excel)
- Creates organized folder with 12+ reports
- Easy to compare with Google Sheets

**Output Files:**
- `00_summary_statistics.csv` - Overall stats
- `01_all_stores.csv` - Complete store list
- `02_region_summary.csv` - Stores per region
- `03_stores_without_region.csv` - ⚠️ Missing data
- `04_abm_logins.csv` - ABM accounts
- `05_rbm_logins.csv` - RBM accounts
- `06_cee_logins.csv` - CEE accounts
- `07_events_region_check.csv` - Event verification
- `08-11_*_region_stores.csv` - Region-wise lists
- `12_manager_assignment_issues.csv` - ⚠️ Login issues

### Option 2: Run SQL Verification (MySQL Workbench)

```sql
-- Open MySQL Workbench
-- Open file: COMPLETE_DATABASE_VERIFICATION.sql
-- Click Execute (⚡)
```

**What this does:**
- Shows comprehensive report in MySQL Workbench
- 10 sections of verification
- Color-coded results
- Immediate feedback

---

## 📊 What to Verify

### 1. Region-wise Store Distribution

**Question:** How many stores in each region?

**Steps:**
1. Run the verification script OR
2. Check file: `02_region_summary.csv`
3. Compare with your Google Sheets

**Expected Regions:**
- North1, North1a, North1b, North2, North3, North4
- South1, South2, South2a, South3
- East1, East2
- West1, West1a, West1b, West2, West3

**SQL Query:**
```sql
SELECT 
    region,
    COUNT(*) as total_stores
FROM stores
GROUP BY region
ORDER BY region;
```

---

### 2. Missing Store Data

**Question:** Are any stores missing from the database?

**Steps:**
1. Open `01_all_stores.csv` (from verification script)
2. Compare store codes with your Google Sheets
3. Check for missing stores

**Find Missing Stores:**
```sql
-- You'll need to check this manually by comparing
-- Store codes in database vs Google Sheets
SELECT store_code, store_name, region
FROM stores
ORDER BY region, store_code;
```

---

### 3. Stores Without Region

**Question:** Which stores don't have region assigned?

**Steps:**
1. Check file: `03_stores_without_region.csv`
2. These stores need region assignment

**SQL Query:**
```sql
SELECT 
    store_code,
    store_name,
    store_city,
    store_state
FROM stores
WHERE region IS NULL OR region = '' OR region = 'NULL';
```

**Fix Missing Regions:**
```sql
-- Example: Update specific store
UPDATE stores 
SET region = 'North1' 
WHERE store_code = 'BTQXXX';

-- Batch update by state (adjust as needed)
UPDATE stores 
SET region = 'North1' 
WHERE store_state = 'Delhi' AND (region IS NULL OR region = '');
```

---

### 4. ABM Login Verification

**Question:** Do all ABM accounts exist and work?

**Steps:**
1. Check file: `04_abm_logins.csv`
2. Check file: `12_manager_assignment_issues.csv`
3. Test each login in the application

**SQL Query - List All ABMs:**
```sql
SELECT 
    a.username,
    a.password,
    COUNT(s.store_code) as stores_assigned
FROM abm_login a
LEFT JOIN stores s ON a.username = s.abm_username
GROUP BY a.username, a.password;
```

**Find ABMs in Stores but No Account:**
```sql
SELECT DISTINCT 
    s.abm_username,
    COUNT(s.store_code) as store_count
FROM stores s
LEFT JOIN abm_login a ON s.abm_username = a.username
WHERE s.abm_username IS NOT NULL 
  AND s.abm_username != ''
  AND a.username IS NULL
GROUP BY s.abm_username;
```

**Add Missing ABM Account:**
```sql
INSERT INTO abm_login (username, password) 
VALUES ('abm_username', 'password123');
```

---

### 5. RBM Login Verification

**Question:** Do all RBM accounts exist and work?

**Steps:**
1. Check file: `05_rbm_logins.csv`
2. Check file: `12_manager_assignment_issues.csv`
3. Test each login in the application

**SQL Query - List All RBMs:**
```sql
SELECT 
    r.username,
    r.password,
    COUNT(s.store_code) as stores_assigned
FROM rbm_login r
LEFT JOIN stores s ON r.username = s.rbm_username
GROUP BY r.username, r.password;
```

**Find RBMs in Stores but No Account:**
```sql
SELECT DISTINCT 
    s.rbm_username,
    COUNT(s.store_code) as store_count
FROM stores s
LEFT JOIN rbm_login r ON s.rbm_username = r.username
WHERE s.rbm_username IS NOT NULL 
  AND s.rbm_username != ''
  AND r.username IS NULL
GROUP BY s.rbm_username;
```

**Add Missing RBM Account:**
```sql
INSERT INTO rbm_login (username, password) 
VALUES ('rbm_username', 'password123');
```

---

### 6. CEE Login Verification

**Question:** Do all CEE accounts exist and work?

**Steps:**
1. Check file: `06_cee_logins.csv`
2. Check file: `12_manager_assignment_issues.csv`
3. Test each login in the application

**SQL Query - List All CEEs:**
```sql
SELECT 
    c.username,
    c.password,
    COUNT(s.store_code) as stores_assigned
FROM cee_login c
LEFT JOIN stores s ON c.username = s.cee_username
GROUP BY c.username, c.password;
```

**Find CEEs in Stores but No Account:**
```sql
SELECT DISTINCT 
    s.cee_username,
    COUNT(s.store_code) as store_count
FROM stores s
LEFT JOIN cee_login c ON s.cee_username = c.username
WHERE s.cee_username IS NOT NULL 
  AND s.cee_username != ''
  AND c.username IS NULL
GROUP BY s.cee_username;
```

**Add Missing CEE Account:**
```sql
INSERT INTO cee_login (username, password) 
VALUES ('cee_username', 'password123');
```

---

### 7. Region Manager Login Test

**Question:** Can I login with region codes (North1, South2, etc.)?

**Expected Regional Codes that Should Work:**
```
East:   east1, east2
North:  north1, north1a, north1b, north2, north3, north4
South:  south1, south2, south2a, south3
West:   west1, west1a, west1b, west2, west3
Test:   test
```

**Check Which Regions Have Stores:**
```sql
SELECT 
    region,
    COUNT(*) as store_count,
    CASE 
        WHEN LOWER(region) IN ('east1','east2','north1','north1a','north1b',
                               'north2','north3','north4','south1','south2',
                               'south2a','south3','west1','west1a','west1b',
                               'west2','west3','test')
        THEN '✅ Login Enabled'
        ELSE '⚠️ Need to Add'
    END as login_status
FROM stores
WHERE region IS NOT NULL AND region != ''
GROUP BY region
ORDER BY region;
```

**Code Location for Regional Login:**
File: `src/main/java/com/dechub/tanishq/service/TanishqPageService.java`

Around line 202-207, verify this list includes all your regions:
```java
Set<String> regionalManagerCodes = new HashSet<>(Arrays.asList(
    "east1","east2",
    "north1","north1a","north1b","north2","north3","north4",
    "south1","south2","south2a","south3",
    "west1","west1a","west1b","west2","west3",
    "test"
));
```

---

### 8. Events Region Verification

**Question:** Do all events have proper region data?

**Steps:**
1. Check file: `07_events_region_check.csv`
2. Look for "MISSING" or "MISMATCH" status

**SQL Query:**
```sql
SELECT 
    COUNT(*) as events_without_region,
    (SELECT COUNT(*) FROM events) as total_events,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM events), 2) as percentage
FROM events
WHERE region IS NULL OR region = '';
```

**Fix Events Without Region:**
```sql
-- Update events with region from their stores
UPDATE events e
JOIN stores s ON e.store_code = s.store_code
SET e.region = s.region
WHERE (e.region IS NULL OR e.region = '')
  AND s.region IS NOT NULL 
  AND s.region != '';
```

**Verify Fix:**
```sql
SELECT 
    e.id,
    e.event_name,
    e.store_code,
    e.region as event_region,
    s.region as store_region
FROM events e
JOIN stores s ON e.store_code = s.store_code
WHERE e.region IS NULL OR e.region = ''
LIMIT 10;
```

---

## 🔧 Common Issues & Fixes

### Issue 1: Store Missing from Database

**Symptom:** Store code in Google Sheets not found in database

**Fix:**
```sql
-- Check if really missing
SELECT * FROM stores WHERE store_code = 'BTQXXX';

-- If missing, you need to import it
-- Usually done via Excel import feature in the application
```

---

### Issue 2: Store Has Wrong Region

**Symptom:** Region doesn't match Google Sheets

**Fix:**
```sql
UPDATE stores 
SET region = 'North1'  -- Correct region
WHERE store_code = 'BTQXXX';

-- Verify
SELECT store_code, store_name, region 
FROM stores 
WHERE store_code = 'BTQXXX';
```

---

### Issue 3: Manager Cannot Login

**Symptom:** ABM/RBM/CEE username exists in stores but cannot login

**Fix:**
```sql
-- Check if account exists
SELECT * FROM abm_login WHERE username = 'manager_username';
SELECT * FROM rbm_login WHERE username = 'manager_username';
SELECT * FROM cee_login WHERE username = 'manager_username';

-- If missing, add it
INSERT INTO abm_login (username, password) 
VALUES ('manager_username', 'default_password');

-- Update password if needed
UPDATE abm_login 
SET password = 'new_password' 
WHERE username = 'manager_username';
```

---

### Issue 4: Regional Manager Code Not Working

**Symptom:** Cannot login with "North1", "South2", etc.

**Fix:**
1. Check if region code is in the Java code
2. File: `TanishqPageService.java` line ~202
3. Add missing region code to the Set

Example:
```java
Set<String> regionalManagerCodes = new HashSet<>(Arrays.asList(
    "east1","east2",
    "north1","north1a","north1b","north2","north3","north4",
    "south1","south2","south2a","south3",
    "west1","west1a","west1b","west2","west3",
    "north5",  // <- Add new region here
    "test"
));
```

Then rebuild and redeploy.

---

### Issue 5: Events Showing Null Region in Reports

**Symptom:** Downloaded CSV shows "null" in Region column

**Fix:**
```sql
-- Fix all events at once
UPDATE events e
JOIN stores s ON e.store_code = s.store_code
SET e.region = s.region
WHERE (e.region IS NULL OR e.region = '')
  AND s.region IS NOT NULL 
  AND s.region != '';

-- Check how many were fixed
SELECT COUNT(*) FROM events WHERE region IS NOT NULL AND region != '';
```

---

## 📝 Step-by-Step Verification Process

### Step 1: Export Database Data (5 minutes)

```powershell
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
.\verify_and_export_database.ps1
```

Enter database password when prompted.

### Step 2: Compare with Google Sheets (15 minutes)

1. Open your Google Sheets with store data
2. Open `01_all_stores.csv` from the export
3. Compare store codes - note any differences
4. Check region assignments match

### Step 3: Check for Missing Data (10 minutes)

1. Open `03_stores_without_region.csv`
2. If any stores listed, they need region assignment
3. Update using SQL or through application

### Step 4: Verify Login Accounts (10 minutes)

1. Open `04_abm_logins.csv`, `05_rbm_logins.csv`, `06_cee_logins.csv`
2. Compare with your Google Sheets manager list
3. Open `12_manager_assignment_issues.csv`
4. Fix any accounts marked as "NO_ACCOUNT"

### Step 5: Test Regional Logins (5 minutes)

Try logging in with each region code:
- North1, North2, North3, etc.
- South1, South2, South3
- East1, East2
- West1, West2, West3

### Step 6: Verify Events (5 minutes)

1. Open `07_events_region_check.csv`
2. Check "Region_Status" column
3. If any show "MISSING" or "MISMATCH", run the fix SQL

---

## 📊 Complete Verification Checklist

### Database Structure
- [ ] All expected tables exist (stores, events, abm_login, rbm_login, cee_login)
- [ ] Tables have proper indexes
- [ ] No duplicate store codes

### Store Data
- [ ] All stores from Google Sheets are in database
- [ ] Every store has a region assigned
- [ ] Store codes match exactly
- [ ] ABM/RBM/CEE assignments are correct

### Login Accounts
- [ ] All ABM accounts exist in abm_login table
- [ ] All RBM accounts exist in rbm_login table
- [ ] All CEE accounts exist in cee_login table
- [ ] No orphaned assignments (store references non-existent account)
- [ ] Regional manager codes work for login

### Events Data
- [ ] Events have region populated
- [ ] Event regions match store regions
- [ ] No null regions in event reports

### Application Code
- [ ] Regional manager codes list is complete (TanishqPageService.java)
- [ ] Region field population logic exists (lines ~337-346)
- [ ] No compilation errors

---

## 🛠️ Tools & Files

### Created Files:
1. ✅ `COMPLETE_DATABASE_VERIFICATION.sql` - Comprehensive SQL verification
2. ✅ `verify_and_export_database.ps1` - PowerShell export script
3. ✅ `COMPLETE_DATABASE_VERIFICATION_GUIDE.md` - This guide

### Existing Files:
- `verify_database.sql` - Basic verification
- `fix_region_data.sql` - Fix null regions
- `REGION_FIX_SUMMARY.md` - Previous region fix documentation

---

## 📞 Quick Reference Commands

### MySQL Command Line Login:
```bash
mysql -u root -p selfie_preprod
```

### Quick Region Count:
```sql
SELECT region, COUNT(*) as stores 
FROM stores 
GROUP BY region;
```

### Quick Login Check:
```sql
SELECT 'ABM' as type, COUNT(*) as count FROM abm_login
UNION ALL
SELECT 'RBM', COUNT(*) FROM rbm_login
UNION ALL
SELECT 'CEE', COUNT(*) FROM cee_login;
```

### Quick Event Region Status:
```sql
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN region IS NOT NULL AND region != '' THEN 1 ELSE 0 END) as with_region,
    SUM(CASE WHEN region IS NULL OR region = '' THEN 1 ELSE 0 END) as without_region
FROM events;
```

---

## ✅ Success Criteria

Your database is verified and correct when:

1. ✅ Export script runs without errors
2. ✅ All stores from Google Sheets exist in database
3. ✅ Zero stores in `03_stores_without_region.csv`
4. ✅ Zero issues in `12_manager_assignment_issues.csv`
5. ✅ All regional logins work (North1, South2, etc.)
6. ✅ All ABM/RBM/CEE accounts can login
7. ✅ Events show proper region in reports (not null)
8. ✅ Event regions match their store's region

---

## 🚨 If Something Goes Wrong

### Database Connection Failed
```powershell
# Check if MySQL is running
Get-Service MySQL*

# Start MySQL if stopped
Start-Service MySQL80  # Or your MySQL service name
```

### Export Script Fails
- Verify MySQL is installed and accessible
- Check database name and credentials
- Try running SQL queries directly in MySQL Workbench

### Still Have Issues?
1. Check application logs
2. Review `REGION_FIX_SUMMARY.md` for previous fixes
3. Run `COMPLETE_DATABASE_VERIFICATION.sql` in MySQL Workbench
4. Take screenshots of specific errors

---

**Last Updated:** January 29, 2026  
**Version:** 1.0  
**Status:** ✅ Ready to Use

