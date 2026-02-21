# 🎯 QUICK REFERENCE: Data Re-Import

**Problem:** Old data has missing names, wrong dates, View Data not working  
**Solution:** Clean database and re-import correctly  
**Time:** 30-45 minutes  
**Guide:** CLEAN_DATA_IMPORT_GUIDE.md

---

## 🚀 QUICK STEPS

### 1️⃣ BACKUP (5 mins)
```powershell
mysqldump -u root -p selfie_preprod > backup_20260124.sql
```

### 2️⃣ DIAGNOSE (MySQL Workbench)
```sql
SELECT COUNT(*) FROM events WHERE event_name = '-' OR event_name IS NULL;
```

### 3️⃣ DELETE OLD DATA
```sql
-- Delete Oct/Nov/Dec/Jan events
DELETE FROM attendees WHERE event_id IN (SELECT id FROM events WHERE start_date LIKE '%/10/2025' OR start_date LIKE '%/11/2025');
DELETE FROM invitees WHERE event_id IN (SELECT id FROM events WHERE start_date LIKE '%/10/2025' OR start_date LIKE '%/11/2025');
DELETE FROM events WHERE start_date LIKE '%/10/2025' OR start_date LIKE '%/11/2025' OR start_date LIKE '%/12/2025' OR start_date LIKE '%/01/2026';
```

### 4️⃣ PREPARE EXCEL
**Column Order:**
1. store_code, 2. event_name, 3. event_type, 4. event_sub_type, 5. start_date
6. region, 7. rso, 8. invitees, 9. attendees, 10. location
11. community, 12. sale, 13. advance, 14. ghs_or_rga, 15. gmb

**Save as:** CSV (Comma delimited)

### 5️⃣ IMPORT
```sql
LOAD DATA LOCAL INFILE 'C:/path/to/events.csv'
INTO TABLE events
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(store_code, event_name, event_type, @event_sub_type, start_date, @region, @rso, @invitees, @attendees, @location, @community, @sale, @advance, @ghs_or_rga, @gmb)
SET id = CONCAT(store_code, '_', UUID()), created_at = NOW();
```

### 6️⃣ VERIFY
```sql
SELECT COUNT(*) FROM events WHERE event_name = '-' OR event_name IS NULL;
-- Should be 0
```

---

## ⚠️ CRITICAL EXCEL RULES

❌ **NEVER:**
- Use "-" for event name
- Leave event name blank
- Use wrong date format
- Use commas in numbers

✅ **ALWAYS:**
- Fill event names properly
- Use DD-MM-YYYY dates (15-10-2025)
- Verify store codes exist
- Use numbers without commas

---

## 📞 IF SOMETHING BREAKS

```bash
# Restore from backup
mysql -u root -p selfie_preprod < backup_20260124.sql
```

---

**Full Guide:** CLEAN_DATA_IMPORT_GUIDE.md  
**Created:** January 24, 2026

