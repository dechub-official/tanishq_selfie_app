# ✅ SUPER SIMPLE - Hide/Show Events Guide

## What You Want
- Show only January 2026 events (hide old ones)
- With ONE SQL query, hide events
- With ONE SQL query, show all events back
- Keep all data safe in database

## ✅ THE SOLUTION (3 Steps)

### Step 1: Add Column (One-Time Setup - 5 seconds)
```sql
ALTER TABLE selfie_prod.events 
ADD COLUMN is_visible BOOLEAN DEFAULT TRUE;
```

### Step 2: Hide Old Events (January 2026 onwards only)
```sql
UPDATE selfie_prod.events 
SET is_visible = FALSE 
WHERE STR_TO_DATE(start_date, '%Y-%m-%d') < '2026-01-01';
```

### Step 3: Build & Deploy
```bash
cd "C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app"
mvn clean package -DskipTests
```
Deploy the WAR file from `target/` folder.

---

## 🎯 QUICK COMMANDS

### Show Only January 2026
```sql
UPDATE selfie_prod.events SET is_visible = FALSE WHERE STR_TO_DATE(start_date, '%Y-%m-%d') < '2026-01-01';
```

### Show ALL Events (Undo)
```sql
UPDATE selfie_prod.events SET is_visible = TRUE;
```

### Show Only Current Month
```sql
UPDATE selfie_prod.events SET is_visible = FALSE;
UPDATE selfie_prod.events SET is_visible = TRUE WHERE start_date LIKE '2026-01%';
```

### Check What's Visible
```sql
SELECT is_visible, COUNT(*) as count FROM selfie_prod.events GROUP BY is_visible;
```

---

## 📊 How It Works

**Before (Without is_visible column):**
- Dashboard shows ALL events from all time
- No way to hide old events

**After (With is_visible column):**
- `is_visible = TRUE` → Event shows on dashboard ✅
- `is_visible = FALSE` → Event is hidden ❌
- All data remains in database, nothing deleted!

---

## 🔄 Monthly Maintenance

**At start of February 2026:**
```sql
-- Hide January, show only February
UPDATE selfie_prod.events SET is_visible = FALSE;
UPDATE selfie_prod.events SET is_visible = TRUE WHERE start_date LIKE '2026-02%';
```

**At start of March 2026:**
```sql
-- Hide all previous months, show only March
UPDATE selfie_prod.events SET is_visible = FALSE;
UPDATE selfie_prod.events SET is_visible = TRUE WHERE start_date LIKE '2026-03%';
```

**Or show last 3 months:**
```sql
UPDATE selfie_prod.events SET is_visible = FALSE;
UPDATE selfie_prod.events SET is_visible = TRUE 
WHERE STR_TO_DATE(start_date, '%Y-%m-%d') >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH);
```

---

## ⚙️ What Changed in Code

**Only 1 file changed:**
- `EventRepository.java` - Added `AND (is_visible IS NULL OR is_visible = TRUE)` to queries

That's it! No frontend changes, no complex logic.

---

## 🧪 Testing

1. **Before deployment:**
   ```sql
   -- Check current events
   SELECT COUNT(*) FROM selfie_prod.events;
   ```

2. **Add column and hide old events:**
   ```sql
   ALTER TABLE selfie_prod.events ADD COLUMN is_visible BOOLEAN DEFAULT TRUE;
   UPDATE selfie_prod.events SET is_visible = FALSE WHERE STR_TO_DATE(start_date, '%Y-%m-%d') < '2026-01-01';
   ```

3. **Check hidden vs visible:**
   ```sql
   SELECT is_visible, COUNT(*) FROM selfie_prod.events GROUP BY is_visible;
   ```

4. **Deploy application and verify dashboard shows only January 2026 events**

5. **To show all events again:**
   ```sql
   UPDATE selfie_prod.events SET is_visible = TRUE;
   ```

---

## 💡 Benefits

✅ **Simple** - Just 1 SQL query to hide, 1 query to show  
✅ **Safe** - Nothing is deleted, all data preserved  
✅ **Flexible** - Control any date range easily  
✅ **Fast** - Database-level filtering is efficient  
✅ **Reversible** - One query to undo everything  

---

## 🚨 Important Notes

1. **Backup first** (recommended but optional):
   ```sql
   CREATE TABLE selfie_prod.events_backup AS SELECT * FROM selfie_prod.events;
   ```

2. **The column defaults to TRUE** - All new events will be visible automatically

3. **NULL is treated as visible** - Backward compatible with existing data

4. **One-time code change** - After deployment, everything is controlled by SQL

---

## 📞 Quick Reference Card

| What You Want | SQL Command |
|---------------|-------------|
| Hide events before Jan 2026 | `UPDATE events SET is_visible = FALSE WHERE start_date < '2026-01-01'` |
| Show all events | `UPDATE events SET is_visible = TRUE` |
| Show only January | `UPDATE events SET is_visible = FALSE; UPDATE events SET is_visible = TRUE WHERE start_date LIKE '2026-01%'` |
| Check status | `SELECT is_visible, COUNT(*) FROM events GROUP BY is_visible` |

---

## ✅ CONFIRMATION

**YES**, you can:
- ✅ Hide old events with 1 query
- ✅ Show all events with 1 query  
- ✅ Keep all data safe (nothing deleted)
- ✅ Control from database only
- ✅ Very simple process!

**Total effort:**
- Database: 2 SQL queries (5 seconds)
- Code: 1 file change (already done)
- Deploy: Standard deployment process

That's it! 🎉

