# 🔍 Database Verification - Quick Start

**Date:** January 29, 2026  
**Purpose:** Verify all stores, regions, and logins match Google Sheets

---

## 🚀 Fastest Way to Verify (Recommended)

### Method 1: Double-click this file
```
VERIFY_DATABASE_EXPORT.bat
```

This will:
- ✅ Export all data to CSV files
- ✅ Create organized reports you can open in Excel
- ✅ Compare easily with Google Sheets
- ⏱️ Takes ~2 minutes

**You'll get files like:**
- `01_all_stores.csv` - All your stores
- `03_stores_without_region.csv` - Problem stores
- `04_abm_logins.csv` - ABM accounts
- `12_manager_assignment_issues.csv` - Login problems

---

## 📋 Other Verification Methods

### Method 2: Quick Summary (30 seconds)

**In MySQL Workbench:**
1. Open file: `QUICK_DATABASE_SUMMARY.sql`
2. Click Execute ⚡
3. See instant summary of issues

### Method 3: Complete Detailed Report

**In MySQL Workbench:**
1. Open file: `COMPLETE_DATABASE_VERIFICATION.sql`
2. Click Execute ⚡
3. Get 10 sections of detailed verification

---

## 📚 Full Documentation

Open this for complete guide: **`COMPLETE_DATABASE_VERIFICATION_GUIDE.md`**

Contains:
- ✅ Step-by-step verification process
- ✅ How to fix each type of issue
- ✅ SQL queries for common tasks
- ✅ Troubleshooting guide

---

## ⚡ Quick Checks

### Check 1: How many stores without region?
```sql
SELECT COUNT(*) FROM stores 
WHERE region IS NULL OR region = '';
```
**Expected:** 0 (zero)

### Check 2: Can I login with region codes?
Try logging in with: `North1`, `South2`, `East1`, `West1`
**Expected:** All should work

### Check 3: Events showing null in reports?
```sql
SELECT COUNT(*) FROM events 
WHERE region IS NULL OR region = '';
```
**Expected:** 0 (zero)

---

## 🔧 Quick Fixes

### Fix: Stores without region
```sql
-- Update manually
UPDATE stores SET region = 'North1' WHERE store_code = 'BTQXXX';
```

### Fix: Events without region
```sql
-- Auto-fix from stores
UPDATE events e
JOIN stores s ON e.store_code = s.store_code
SET e.region = s.region
WHERE (e.region IS NULL OR e.region = '')
  AND s.region IS NOT NULL;
```

### Fix: Missing ABM/RBM/CEE account
```sql
-- Add login account
INSERT INTO abm_login (username, password) 
VALUES ('username', 'password');
```

---

## 📊 What You're Checking

1. **Region Distribution**
   - Do all stores have a region?
   - Are regions correct per Google Sheets?

2. **Store Completeness**
   - Are all stores from Google Sheets in database?
   - Any duplicate store codes?

3. **Login Accounts**
   - Do all ABM/RBM/CEE accounts exist?
   - Can managers login?
   - Do regional codes work (North1, South2, etc.)?

4. **Events Data**
   - Do events have region populated?
   - Does it show correctly in reports?

---

## 📁 Files Overview

| File | Purpose | Use When |
|------|---------|----------|
| `VERIFY_DATABASE_EXPORT.bat` | Export to Excel | Want to compare with Google Sheets |
| `QUICK_DATABASE_SUMMARY.sql` | Fast overview | Want quick status check |
| `COMPLETE_DATABASE_VERIFICATION.sql` | Detailed report | Need comprehensive analysis |
| `COMPLETE_DATABASE_VERIFICATION_GUIDE.md` | Full instructions | Need step-by-step help |
| `fix_region_data.sql` | Fix null regions | Events/stores missing region |

---

## ✅ Success Checklist

After verification, you should have:

- [ ] Zero stores without region
- [ ] All stores from Google Sheets in database
- [ ] All ABM/RBM/CEE accounts exist and work
- [ ] All regional logins work (North1, South2, etc.)
- [ ] Zero events with null region
- [ ] Downloaded reports show proper region data

---

## 🆘 Need Help?

1. **Read the full guide:** `COMPLETE_DATABASE_VERIFICATION_GUIDE.md`
2. **Check previous fixes:** `REGION_FIX_SUMMARY.md`
3. **Run quick summary:** `QUICK_DATABASE_SUMMARY.sql`

---

## 🎯 Most Common Issues

### "Some stores missing from database"
→ You need to import from Excel/Google Sheets

### "Region showing null in reports"
→ Run `fix_region_data.sql` to fix existing events

### "Cannot login with North1, South2, etc."
→ Check `TanishqPageService.java` has all region codes

### "ABM/RBM/CEE cannot login"
→ Check if account exists in abm_login/rbm_login/cee_login tables

---

**🚀 START HERE:** Run `VERIFY_DATABASE_EXPORT.bat`

Then open the CSV files and compare with your Google Sheets!

---

**Version:** 1.0  
**Last Updated:** January 29, 2026  
**Status:** ✅ Ready to Use

