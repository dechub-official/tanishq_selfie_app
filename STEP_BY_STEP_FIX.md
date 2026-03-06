# 🚨 CRITICAL: Fix Store Summary SQL Error - STEP BY STEP GUIDE

## PROBLEM
- **Error:** `SQLGrammarException: could not extract ResultSet`
- **API:** `GET /events/store-summary?storeCode=TEST1`
- **Status:** 500 Internal Server Error
- **Cause:** Database missing 6 columns that Java code expects

---

## 🎯 ROOT CAUSE (100% Confirmed)

Your `Store.java` entity class defines these fields:
```java
private String region;
private String level;
private String abmUsername;
private String rbmUsername;
private String ceeUsername;
private String corporateUsername;
```

But your `selfie_preprod` database table `stores` does NOT have these columns.

When Hibernate/JPA tries to query the Store entity (which happens either directly or through Event→Store relationship), it generates SQL like:
```sql
SELECT store_code, store_name, ..., region, level, abm_username, rbm_username, cee_username, corporate_username
FROM stores
WHERE ...
```

Since those 6 columns don't exist, MySQL returns an error, and Hibernate wraps it as `SQLGrammarException`.

---

## ✅ SOLUTION - EXACT STEPS TO FIX

### Step 1: Access Your Database

**Option A - Using MySQL Workbench (Recommended):**
1. Open MySQL Workbench
2. Connect to localhost:3306
3. Username: `root`
4. Password: `Dechub#2025`
5. Select database: `selfie_preprod`

**Option B - Using MySQL Command Line:**
```bash
mysql -u root -p
# Enter password when prompted: Dechub#2025
```

### Step 2: Run These SQL Commands

Copy and paste these commands **ONE BY ONE** (or all at once):

```sql
-- Select your database
USE selfie_preprod;

-- Add the 6 missing columns
ALTER TABLE stores ADD COLUMN IF NOT EXISTS region VARCHAR(100);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS level VARCHAR(50);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS abm_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS rbm_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS cee_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS corporate_username VARCHAR(255);

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_abm_username ON stores(abm_username);
CREATE INDEX IF NOT EXISTS idx_rbm_username ON stores(rbm_username);
CREATE INDEX IF NOT EXISTS idx_cee_username ON stores(cee_username);
CREATE INDEX IF NOT EXISTS idx_corporate_username ON stores(corporate_username);
CREATE INDEX IF NOT EXISTS idx_region ON stores(region);

-- Verify columns were added
DESCRIBE stores;
```

### Step 3: Verify the Columns Were Added

After running the ALTER TABLE commands, check the output of `DESCRIBE stores;`

You should see these new columns listed:
- `region` - varchar(100)
- `level` - varchar(50)
- `abm_username` - varchar(255)
- `rbm_username` - varchar(255)
- `cee_username` - varchar(255)
- `corporate_username` - varchar(255)

### Step 4: Verify TEST1 Store Exists

Run this query:
```sql
SELECT * FROM stores WHERE store_code = 'TEST1';
```

**If TEST1 doesn't exist**, create it:
```sql
INSERT INTO stores (store_code, store_name, store_email_id) 
VALUES ('TEST1', 'Test Store 1', 'test1@titan.co.in');
```

### Step 5: Restart Your Spring Boot Application

**CRITICAL:** You MUST restart the application after schema changes!

If running locally:
```bash
# Stop the application (Ctrl+C)
# Then start it again
mvn spring-boot:run
```

If deployed:
- Restart Tomcat/application server
- Or redeploy the WAR file

### Step 6: Test the Fix

After restart, try the API again:
```
GET https://celebrationsite-preprod.tanishq.co.in/events/store-summary?storeCode=TEST1&startDate=&endDate=
```

**Expected Response (Success):**
```json
{
    "status": 200,
    "message": "Store summary fetched successfully",
    "data": {
        "storeCode": "TEST1",
        "totalEvents": 0,
        "totalInvitees": 0,
        "totalAttendees": 0,
        "totalAdvance": 0.0,
        "totalGhsOrRga": 0.0,
        "totalSale": 0.0
    }
}
```

---

## 🔍 WHY THIS HAPPENED

You added role-based access control (VAPT security fixes) which required:
1. New user login tables (ABM, RBM, CEE, Corporate)
2. New columns in stores table to map stores to users
3. New repository methods to query by username

You updated:
- ✅ Java entity (`Store.java`) - added 6 new fields
- ✅ Repository (`StoreRepository.java`) - added new query methods
- ✅ Service layer (`TanishqPageService.java`) - added summary methods
- ✅ Controller (`EventsController.java`) - added endpoints
- ❌ **Database schema** - FORGOT to add columns ← **THIS IS THE ISSUE**

---

## 📋 CHECKLIST

- [ ] Connected to MySQL database
- [ ] Selected `selfie_preprod` database
- [ ] Ran 6 ALTER TABLE commands
- [ ] Verified columns exist with DESCRIBE stores
- [ ] Verified TEST1 store exists
- [ ] Restarted Spring Boot application
- [ ] Tested API endpoint - got 200 OK response
- [ ] Frontend dashboard loads without error

---

## ⚠️ IMPORTANT NOTES

1. **Safe to run:** The `ADD COLUMN IF NOT EXISTS` syntax ensures columns are only added if missing
2. **No data loss:** ALTER TABLE only adds columns, doesn't delete anything
3. **NULL values OK:** New columns will be NULL for existing stores - that's fine
4. **Must restart app:** Spring Boot caches entity metadata, so restart is required
5. **Frontend is fine:** No frontend changes needed - error is 100% backend/database

---

## 🆘 IF STILL HAVING ISSUES

### Check application logs:
```bash
# Look for the actual SQL error
tail -f catalina.out
# Or check your IDE console logs
```

### Enable SQL logging:
In `application-preprod.properties`, verify:
```properties
spring.jpa.show-sql=true
logging.level.org.springframework.web=DEBUG
```

### Check Hibernate is using correct database:
Look for this in startup logs:
```
Hibernate: select ... from stores ...
```

### Manual column check:
```sql
SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'selfie_preprod' 
  AND TABLE_NAME = 'stores' 
  AND COLUMN_NAME IN ('region', 'level', 'abm_username', 'rbm_username', 'cee_username', 'corporate_username');
```

Should return: `6`

---

## 🎉 AFTER FIX WORKS

Once the store-summary API works, you can populate the user management:

1. Run `sql/02_create_abm_logins.sql` - Create ABM users
2. Run `sql/03_rbm_logins.sql` - Create RBM users
3. Run `sql/04_cee_logins.sql` - Create CEE users
4. Run `sql/06_create_corporate_login.sql` - Create Corporate users
5. Run `sql/05_map_stores.sql` - Map stores to users

This will enable full role-based access control.

---

## FINAL ANSWER

**Q: Is this a frontend or backend error?**  
**A: Backend/Database error - the database schema is missing columns**

**Q: Is frontend and backend aligned?**  
**A: YES, they are aligned. Database is NOT aligned with backend code.**

**Q: What needs to be changed?**  
**A: Only the database - add 6 columns to stores table using QUICK_FIX.sql**

**Now go run the SQL migration! That's all you need to do.** ✅

