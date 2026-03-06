# IMMEDIATE FIX FOR STORE SUMMARY ERROR

## ⚠️ CRITICAL ISSUE IDENTIFIED

**Error:** `SQLGrammarException: could not extract ResultSet`  
**Cause:** Database table `stores` is missing 6 columns that exist in `Store.java` entity  
**Impact:** ANY query on the stores table will fail, including store-summary API

---

## 🔍 DIAGNOSIS COMPLETE

### The Problem
Your `Store.java` entity has these fields:
- `region` 
- `level`
- `abm_username`
- `rbm_username`
- `cee_username`
- `corporate_username`

But your database table `stores` does NOT have these columns.

When Hibernate/JPA tries to query the Store entity (even simple `findById`), it attempts to SELECT all columns. Since these 6 columns don't exist in the database, MySQL throws a `SQLGrammarException`.

### Why is this happening now?
You likely added these fields to `Store.java` recently (as part of VAPT fixes for role-based access control), but:
1. Either the database migration wasn't run
2. Or the application wasn't restarted after schema changes
3. Or `spring.jpa.hibernate.ddl-auto=update` failed silently

---

## ✅ SOLUTION - TWO OPTIONS

### OPTION 1: Run SQL Migration Script (RECOMMENDED)

**Step 1:** Connect to your preprod database
```bash
mysql -u root -p selfie_preprod
# When prompted, enter password: Dechub#2025
```

**Step 2:** Run the migration script
```bash
source C:/JAVA/celebration-preprod-latest/celeb/tanishq_selfie_app_clean/sql/FIX_MISSING_COLUMNS.sql
```

**OR** copy-paste the ALTER TABLE commands directly:

```sql
USE selfie_preprod;

ALTER TABLE stores ADD COLUMN IF NOT EXISTS region VARCHAR(100);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS level VARCHAR(50);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS abm_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS rbm_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS cee_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS corporate_username VARCHAR(255);

-- Verify
DESCRIBE stores;
```

**Step 3:** Restart your application
```bash
# Stop the application
# Restart it (deployment command or restart Tomcat/server)
```

### OPTION 2: Let Hibernate Create Columns Automatically

If you can't access the database directly:

**Step 1:** Temporarily change `application-preprod.properties`
```properties
# Change from:
spring.jpa.hibernate.ddl-auto=update

# To:
spring.jpa.hibernate.ddl-auto=create-drop  # ⚠️ WARNING: This will DELETE all data!
```

**DON'T USE THIS OPTION** unless you're okay with losing all data. Instead, use OPTION 1.

---

## 🚀 IMMEDIATE ACTIONS FOR YOU

### Action 1: Run the Database Migration (5 minutes)

1. Open MySQL Workbench or connect via command line:
   ```bash
   mysql -u root -p
   ```

2. Enter password: `Dechub#2025`

3. Select database:
   ```sql
   USE selfie_preprod;
   ```

4. Run these commands ONE BY ONE:
   ```sql
   ALTER TABLE stores ADD COLUMN IF NOT EXISTS region VARCHAR(100);
   ALTER TABLE stores ADD COLUMN IF NOT EXISTS level VARCHAR(50);
   ALTER TABLE stores ADD COLUMN IF NOT EXISTS abm_username VARCHAR(255);
   ALTER TABLE stores ADD COLUMN IF NOT EXISTS rbm_username VARCHAR(255);
   ALTER TABLE stores ADD COLUMN IF NOT EXISTS cee_username VARCHAR(255);
   ALTER TABLE stores ADD COLUMN IF NOT EXISTS corporate_username VARCHAR(255);
   ```

5. Verify columns were added:
   ```sql
   DESCRIBE stores;
   ```
   
   You should see all 6 new columns listed.

6. Create indexes:
   ```sql
   CREATE INDEX IF NOT EXISTS idx_abm_username ON stores(abm_username);
   CREATE INDEX IF NOT EXISTS idx_rbm_username ON stores(rbm_username);
   CREATE INDEX IF NOT EXISTS idx_cee_username ON stores(cee_username);
   CREATE INDEX IF NOT EXISTS idx_corporate_username ON stores(corporate_username);
   CREATE INDEX IF NOT EXISTS idx_region ON stores(region);
   ```

### Action 2: Restart the Application

After adding the columns, restart your Spring Boot application:
- If running locally: Stop and restart
- If deployed to Tomcat: Restart Tomcat
- If using Docker: Restart the container

### Action 3: Test the Fix

After restart, test the endpoint:
```
GET https://celebrationsite-preprod.tanishq.co.in/events/store-summary?storeCode=TEST1&startDate=&endDate=
```

You should now get a successful response instead of 500 error.

---

## 📊 EXPECTED RESULT

After fixing, the API should return:
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

(Values will be 0 if no events exist for TEST1 store)

---

## 🔧 TROUBLESHOOTING

### If error persists after migration:

1. **Verify columns exist:**
   ```sql
   SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_NAME = 'stores' 
   AND COLUMN_NAME IN ('region', 'level', 'abm_username', 'rbm_username', 'cee_username', 'corporate_username');
   ```

2. **Check Hibernate is using correct database:**
   Look at application startup logs for:
   ```
   Hibernate: select ... from stores where ...
   ```

3. **Verify TEST1 store exists:**
   ```sql
   SELECT * FROM stores WHERE store_code = 'TEST1';
   ```
   
   If it doesn't exist, create it:
   ```sql
   INSERT INTO stores (store_code, store_name, store_email_id) 
   VALUES ('TEST1', 'Test Store 1', 'test1@titan.co.in');
   ```

4. **Check application logs** for detailed error:
   ```bash
   tail -f /opt/tomcat/logs/catalina.out
   # Or wherever your logs are
   ```

---

## ✨ FINAL ANSWER TO YOUR QUESTIONS

### Q: Is this a backend error or frontend error?
**A: 100% BACKEND/DATABASE ERROR** ❌ Database schema issue

The frontend is working correctly. The error happens because:
- Frontend makes valid API call: `/events/store-summary?storeCode=TEST1`
- Backend receives the request correctly
- Backend tries to query the `stores` table using Hibernate
- Hibernate fails because 6 columns are missing from the database
- Backend returns 500 error to frontend

### Q: Do I need to change anything in the database?
**A: YES** - You must add 6 missing columns to the `stores` table

### Q: Do I need to change anything in the server?
**A: NO** - The backend code is correct. Just restart after DB migration.

### Q: Are backend and frontend perfectly aligned?
**A: YES** - Both are correct. Only the DATABASE schema is outdated.

**Frontend:** ✅ Correct  
**Backend Code:** ✅ Correct  
**Database Schema:** ❌ Missing columns - **FIX THIS**

---

## 🎯 NEXT STEPS AFTER FIX

Once the error is resolved and store-summary works:

1. **Populate user logins** (if not done):
   - Run `sql/02_create_abm_logins.sql` 
   - Run `sql/03_rbm_logins.sql`
   - Run `sql/04_cee_logins.sql`
   - Run `sql/06_create_corporate_login.sql`

2. **Map stores to users**:
   - Run `sql/05_map_stores.sql`
   - This will populate the new columns with actual usernames

3. **Test role-based access**:
   - Login as ABM/RBM/CEE user
   - Verify they see only their assigned stores

---

## 📝 SUMMARY

**Root Cause:** Database schema out of sync with JPA entity  
**Fix Required:** Run database migration to add 6 missing columns  
**Time to Fix:** ~5 minutes  
**Downtime Required:** Application restart only  

**Run this NOW:**
```sql
USE selfie_preprod;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS region VARCHAR(100);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS level VARCHAR(50);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS abm_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS rbm_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS cee_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS corporate_username VARCHAR(255);
```

Then restart your application. Problem solved! ✅

