# 🚨 PRODUCTION DATABASE ISSUES - ACTION PLAN

**Date:** January 29, 2026  
**Database:** selfie_prod  
**Status:** CRITICAL ISSUES FOUND

---

## 📊 Issues Summary

Based on your MySQL verification output, here are the critical issues:

### Issue #1: Stores Without Region ⚠️ CRITICAL
- **Found:** 88 stores out of 529 (16.64%)
- **Impact:** These stores cannot use regional manager login
- **Status:** NEEDS FIX

### Issue #2: Events Without Region ⚠️ CRITICAL  
- **Found:** 30,857 events have NULL region
- **Impact:** Downloaded reports show "null" in region column
- **Status:** NEEDS FIX

### Issue #3: Region Name Inconsistencies ⚠️ MEDIUM
- **Found:** Multiple variations of same region (NORTH 1, North 1, north1)
- **Impact:** Confusion and potential login issues
- **Status:** NEEDS STANDARDIZATION

### Issue #4: Login Table Structure Issues ⚠️ HIGH
- **Found:** Queries failing with "Unknown column 'username'"
- **Impact:** Cannot verify ABM/RBM/CEE accounts
- **Status:** NEEDS INVESTIGATION

---

## 🔧 Fix Strategy

### Step 1: BACKUP FIRST (MANDATORY!)
```sql
-- Run this BEFORE any fixes:
CREATE TABLE stores_backup_20260129 AS SELECT * FROM stores;
CREATE TABLE events_backup_20260129 AS SELECT * FROM events;
```

### Step 2: Check Login Tables Structure
```bash
# Run this file first:
mysql -u root -p selfie_prod < CHECK_LOGIN_TABLES_STRUCTURE.sql > login_tables_check.txt
```

**What this does:**
- Shows actual column names in login tables
- Lists all managers referenced in stores
- Identifies missing accounts

### Step 3: Fix All Database Issues
```bash
# After reviewing login tables, run:
mysql -u root -p selfie_prod < FIX_PRODUCTION_DATABASE_ISSUES.sql > fix_results.txt
```

**What this does:**
- Standardizes region names (NORTH 1 → north1)
- Assigns regions to 88 stores without region
- Updates 30,857 events with proper regions
- Cleans up empty strings vs NULL

---

## 📋 Detailed Fix Plan

### Fix #1: Standardize Region Names

**Problem:**
```
NORTH 1, North 1, north1 → should all be: north1
SOUTH 1, South 1, south1, South1 → should all be: south1
```

**Fix:**
```sql
UPDATE stores SET region = 'north1' WHERE region IN ('NORTH 1', 'North 1');
UPDATE stores SET region = 'north2' WHERE region IN ('NORTH 2', 'North 2');
-- etc...
```

**Expected Result After regions:**
```
north1, north2, north3, north4
south1, south2, south3
east1, east2
west1, west2, west3
```

---

### Fix #2: Assign Regions to 88 Stores

**Stores Needing Region Assignment:**

**Andhra Pradesh → south1:**
- GUT (Guntur), SRK (Srikakulam)

**Telangana → south3:**
- HKL, HKK, HKJ, HIM, HBV, HBB (Hyderabad stores)

**Karnataka → south2:**
- BMA, BKM, BFF, BHN (Bangalore), MYK (Mysore)

**Kerala → south2:**
- THR (Thrissur)

**West Bengal → east1:**
- CHD, CSK, BRU, BRK, KSM, SGU, SUI, TML (Kolkata & West Bengal)

**Odisha → east1:**
- BAR, BHK, BRH, CTK, DNK (Odisha stores)
- BWP (Orrisa)

**Bihar → east2:**
- AUR, JHN, KTH, KSN, MDB, MUN, NWD, PTB, RXL

**Jharkhand → east2:**
- KDR (Koderma), RAH (Ranchi)

**Assam → east1:**
- ITN (Itanagar), GUS (Guwahati), SCL (Silchar)

**Delhi → north1:**
- DGR, SEP

**Gujarat → west1:**
- AHA (Ahmedabad), GND (Gandhinagar), RJK (Rajkot), SUV (Surat), VSD (Valsad)

**Haryana → north1:**
- BNW (Bhiwani), KAI (Kaithal)

**Jammu & Kashmir → north3:**
- ANT (Anantnag), JMC (Jammu)

**Madhya Pradesh → west1:**
- KTN (Katni), RTL (Ratlam)

**Maharashtra → west2:**
- BOI (Boisar), ABH (Mumbai - Ambarnath), PCC, PBB (Pune), NGM (Nagpur)

**Punjab → north3:**
- AGT (Amritsar), BRN (Barnala), LDN (Ludhiana)

**Rajasthan → north1:**
- JPM (Jaipur)

**Tamil Nadu → south1:**
- CGL, TBM, TRL, CAD, CAN, COW, CPO, NMK, RPM, VLK, MAI, TJY

**Uttar Pradesh → north2:**
- NDS (Noida), AGF (Agra), BIJ (Bijnor), ETW (Etawah), GZR, GON, GPR, LVY, MGH, ALB

**Special/Test Stores → test:**
- KRN, UNK, HKL (with NULL state)

---

### Fix #3: Update Events with Region

**Problem:**
30,857 events have NULL region

**Fix:**
```sql
UPDATE events e
JOIN stores s ON e.store_code = s.store_code
SET e.region = s.region
WHERE (e.region IS NULL OR e.region = '')
  AND s.region IS NOT NULL;
```

**Expected Result:**
Events should inherit region from their store

---

### Fix #4: Login Tables

**Need to first check structure:**
```sql
DESCRIBE abm_login;
DESCRIBE rbm_login;
DESCRIBE cee_login;
```

**Possible column names:**
- `username` / `user_name` / `login` / `name`
- `password` / `user_password` / `pass`

**After knowing column names, add missing accounts:**
```sql
-- Example (adjust column names):
INSERT INTO abm_login (username, password) VALUES ('NORTH1-ABM-01', 'password123');
INSERT INTO rbm_login (username, password) VALUES ('NORTH1', 'password123');
INSERT INTO cee_login (username, password) VALUES ('NORTH1-CEE-01', 'password123');
```

---

## 🎯 Execution Steps

### Step 1: Connect to Production Database
```bash
mysql -u root -p
# Enter password when prompted
USE selfie_prod;
```

### Step 2: Run Login Tables Check
```bash
# In MySQL:
source C:/JAVA/celebration-preprod-latest/celeb/tanishq_selfie_app/CHECK_LOGIN_TABLES_STRUCTURE.sql;
```

### Step 3: Review Output
- Note the actual column names in login tables
- Note which managers are missing accounts
- Save this information

### Step 4: Run Main Fix Script
```bash
# In MySQL:
source C:/JAVA/celebration-preprod-latest/celeb/tanishq_selfie_app/FIX_PRODUCTION_DATABASE_ISSUES.sql;
```

### Step 5: Add Missing Login Accounts
Based on output from Step 2, manually add accounts:
```sql
-- Example:
INSERT INTO abm_login (actual_username_column, actual_password_column) 
VALUES ('NORTH1-ABM-01', 'password');
```

### Step 6: Verify Fixes
```sql
-- Check stores without region (should be 0)
SELECT COUNT(*) FROM stores WHERE region IS NULL;

-- Check events without region (should be close to 0)
SELECT COUNT(*) FROM events WHERE region IS NULL;

-- Check region distribution
SELECT region, COUNT(*) FROM stores GROUP BY region;
```

---

## ✅ Expected Results After Fix

### Stores:
```
Before: 88 stores without region (16.64%)
After:  0 stores without region (0%)
```

### Events:
```
Before: 30,857 events without region
After:  0 events without region (or very close to 0)
```

### Region Distribution (Standardized):
```
north1: ~52 stores
north2: ~46 stores
north3: ~22 stores
north4: ~1 store
south1: ~54 stores (50 + 4 fixed)
south2: ~42 stores (41 + 1 fixed)
south3: ~30 stores
east1: ~52 stores (50 + 2 fixed)
east2: ~39 stores
west1: ~38 stores
west2: ~29 stores (27 + 2 fixed)
west3: ~33 stores (32 + 1 fixed)
test: ~4 stores
```

### Login Tables:
- All ABM/RBM/CEE accounts exist
- No orphaned manager references
- All regional codes have corresponding accounts

---

## 🧪 Testing After Fix

### Test 1: Regional Manager Login
```
Try logging in with:
- north1 (should work)
- south2 (should work)
- east1 (should work)
- west1 (should work)
```

### Test 2: ABM/RBM/CEE Login
```
Try logging in with each manager username
All should work without errors
```

### Test 3: Event Reports
```
1. Go to Events section
2. Download event report
3. Check "Region" column
4. Should show actual region names (not null)
```

### Test 4: Store Filtering by Region
```
1. Select a region (e.g., North1)
2. Should show all stores for that region
3. No errors about missing data
```

---

## 🔄 Rollback Plan (If Needed)

If something goes wrong, restore from backup:

```sql
-- Rollback stores
DROP TABLE stores;
CREATE TABLE stores AS SELECT * FROM stores_backup_20260129;

-- Rollback events
DROP TABLE events;
CREATE TABLE events AS SELECT * FROM events_backup_20260129;

-- Verify rollback
SELECT COUNT(*) FROM stores;
SELECT COUNT(*) FROM events;
```

---

## 📊 Verification Queries

Run these after the fix to confirm everything is correct:

```sql
-- 1. Stores by region
SELECT 
    COALESCE(region, 'NULL') as region,
    COUNT(*) as stores
FROM stores
GROUP BY region
ORDER BY region;

-- 2. Stores without region (should be 0)
SELECT COUNT(*) as stores_without_region
FROM stores 
WHERE region IS NULL;

-- 3. Events without region (should be 0 or minimal)
SELECT COUNT(*) as events_without_region
FROM events 
WHERE region IS NULL;

-- 4. Events by region
SELECT 
    COALESCE(region, 'NULL') as region,
    COUNT(*) as events
FROM events
GROUP BY region
ORDER BY region;

-- 5. Verify region standardization
SELECT DISTINCT region 
FROM stores 
ORDER BY region;
-- Should only see: east1, east2, north1, north2, north3, north4, 
--                  south1, south2, south3, west1, west2, west3, test

-- 6. Manager accounts summary
SELECT 
    'ABM Accounts' as type,
    COUNT(*) as count
FROM abm_login
UNION ALL
SELECT 'RBM Accounts', COUNT(*) FROM rbm_login
UNION ALL
SELECT 'CEE Accounts', COUNT(*) FROM cee_login;
```

---

## 📝 Manual Steps Required

After running the automated fixes, you may need to:

1. **Add Missing Login Accounts**
   - Check output of CHECK_LOGIN_TABLES_STRUCTURE.sql
   - Add accounts for managers that don't exist

2. **Verify Special Stores**
   - KRN, UNK, HKL (assigned to 'test' region)
   - May need proper region based on actual location

3. **Update Google Sheets**
   - Ensure Google Sheets has same standardized region names
   - Update any ABM/RBM/CEE usernames

4. **Test Application**
   - Try all regional logins
   - Download reports
   - Check for any errors

---

## 🔐 Security Note

**IMPORTANT:** All fixes will be logged. The backup tables will remain in the database for reference:
- `stores_backup_20260129`
- `events_backup_20260129`

Keep these backups for at least 30 days before dropping them.

---

## 📞 Quick Reference Commands

```bash
# Connect to production
mysql -u root -p selfie_prod

# Run login check
source CHECK_LOGIN_TABLES_STRUCTURE.sql;

# Run main fix
source FIX_PRODUCTION_DATABASE_ISSUES.sql;

# Quick verification
SELECT COUNT(*) FROM stores WHERE region IS NULL;
SELECT COUNT(*) FROM events WHERE region IS NULL;
```

---

## 📊 Progress Tracking

Use this checklist:

```
PRE-FIX VERIFICATION:
[ ] Connected to selfie_prod database
[ ] Confirmed 88 stores without region
[ ] Confirmed 30,857 events without region
[ ] Checked login table structure

BACKUP:
[ ] Created stores_backup_20260129
[ ] Created events_backup_20260129
[ ] Verified backup row counts match

FIX EXECUTION:
[ ] Ran CHECK_LOGIN_TABLES_STRUCTURE.sql
[ ] Reviewed login table column names
[ ] Ran FIX_PRODUCTION_DATABASE_ISSUES.sql
[ ] Added missing login accounts
[ ] No SQL errors during execution

POST-FIX VERIFICATION:
[ ] 0 stores without region
[ ] 0 (or minimal) events without region
[ ] All regions standardized
[ ] Can login with regional codes
[ ] Event reports show regions (not null)

TESTING:
[ ] Tested north1 login - WORKS
[ ] Tested south2 login - WORKS
[ ] Tested east1 login - WORKS
[ ] Tested west1 login - WORKS
[ ] Downloaded event report - shows regions
[ ] All ABM/RBM/CEE logins work

CLEANUP:
[ ] Updated documentation
[ ] Informed team of changes
[ ] Scheduled backup deletion (30 days)
```

---

**🚀 Ready to Execute?**

1. Start with: `CHECK_LOGIN_TABLES_STRUCTURE.sql`
2. Then run: `FIX_PRODUCTION_DATABASE_ISSUES.sql`
3. Verify results
4. Test application

---

**Created:** January 29, 2026  
**Database:** selfie_prod  
**Status:** Ready to Execute  
**Priority:** HIGH - Fix as soon as possible

