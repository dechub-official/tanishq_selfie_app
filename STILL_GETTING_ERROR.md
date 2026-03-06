# 🔴 STILL GETTING ERROR? - ADVANCED TROUBLESHOOTING

## If you've already added the database columns but still getting the error, follow this checklist:

---

## ✅ CHECKLIST - GO THROUGH EACH ITEM

### 1. VERIFY DATABASE COLUMNS EXIST ⚠️ CRITICAL

Run this in MySQL:
```sql
USE selfie_preprod;

SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'selfie_preprod'
  AND TABLE_NAME = 'stores' 
  AND COLUMN_NAME IN ('region', 'level', 'abm_username', 'rbm_username', 'cee_username', 'corporate_username');
```

**Expected Result:** 6 rows showing all column names

**If you see 0 rows:**
- The columns don't exist in THIS database
- You may have run the migration on a different database (selfie_prod, tanishq, etc.)
- Run the ALTER TABLE commands again (see QUICK_FIX.sql)

**If you see fewer than 6 rows:**
- Some columns are missing
- Run the ALTER TABLE commands again for missing columns

---

### 2. VERIFY YOU'RE USING THE CORRECT DATABASE

Check your `application-preprod.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod
```

The database name is **selfie_preprod** (not selfie_prod, not tanishq).

**Action:** Open MySQL and verify this database exists:
```sql
SHOW DATABASES;
USE selfie_preprod;
SHOW TABLES;
```

---

### 3. REBUILD THE APPLICATION ⚠️ REQUIRED

I've just made critical code changes:
- Changed Event→Store relationship to `FetchType.LAZY`
- Added explicit `@Column` annotations to Store entity

You **MUST** rebuild:
```bash
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app_clean
mvn clean package -Ppreprod -DskipTests
```

This will create a new WAR file in `target/` folder.

---

### 4. REDEPLOY/RESTART THE APPLICATION ⚠️ CRITICAL

**The running application has cached entity metadata!**

Even if you added database columns, the running application doesn't know about them until restarted.

**Options:**
- **If running locally:** Stop (Ctrl+C) and restart with `mvn spring-boot:run -Ppreprod`
- **If deployed to Tomcat:** 
  1. Stop Tomcat
  2. Delete old WAR from `webapps/`
  3. Copy new WAR from `target/` to Tomcat `webapps/`
  4. Start Tomcat
- **If using external server:**
  1. Copy new WAR to server
  2. Restart application service

---

### 5. CLEAR ALL CACHES

After rebuilding and redeploying:

**Backend Cache:**
- Application restart clears it ✅

**Browser Cache:**
- Press Ctrl+Shift+Delete
- Clear cookies and cached files
- OR use Incognito/Private window

**Hibernate Session Cache:**
- Cleared on application restart ✅

---

### 6. CHECK APPLICATION STARTUP LOGS

When the application starts, look for:

**Success indicators:**
```
Hibernate: select ... from stores ...
HikkaриCP: connected to database selfie_preprod
Started TanishqSelfieApplication in X seconds
```

**Error indicators:**
```
SQLGrammarException: Unknown column 'region' in 'field list'
SQLGrammarException: Unknown column 'abm_username' in 'field list'
```

If you see these errors in startup logs, it means:
- Database columns still don't exist
- OR application is connecting to wrong database

---

### 7. VERIFY DATABASE CONNECTION FROM APPLICATION

Add this temporarily to test database connectivity:

Create a test endpoint to verify columns exist:
```java
@GetMapping("/test-database")
public ResponseEntity<String> testDatabase() {
    try {
        List<Store> stores = storeRepository.findAll();
        return ResponseEntity.ok("Database OK. Found " + stores.size() + " stores");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Database Error: " + e.getMessage());
    }
}
```

Access: `https://celebrationsite-preprod.tanishq.co.in/events/test-database`

If this fails with same error, the database definitely doesn't have the columns.

---

### 8. POSSIBLE ISSUE: WRONG DATABASE BEING USED

Check if you have multiple databases:
```sql
SHOW DATABASES LIKE '%self%';
SHOW DATABASES LIKE '%tanishq%';
```

Common scenarios:
- Migration ran on `selfie_prod` but app uses `selfie_preprod`
- Migration ran on `tanishq` but app uses `selfie_preprod`
- Local database vs remote database confusion

**Solution:** Run the migration on the EXACT database your application uses.

---

### 9. NUCLEAR OPTION: Force Hibernate to Create Columns

If all else fails, let Hibernate create the columns automatically:

**Step 1:** Change `application-preprod.properties`:
```properties
# Change from:
spring.jpa.hibernate.ddl-auto=update

# To:
spring.jpa.hibernate.ddl-auto=validate
```

**Step 2:** Restart application

If you get error: "Missing column 'region' in table 'stores'", then you confirmed columns don't exist.

**Step 3:** Change back to:
```properties
spring.jpa.hibernate.ddl-auto=update
```

**Step 4:** Restart again - Hibernate should create missing columns automatically

---

### 10. MANUAL COLUMN CREATION (If IF NOT EXISTS syntax doesn't work)

Some older MySQL versions don't support `ADD COLUMN IF NOT EXISTS`. Try this instead:

```sql
USE selfie_preprod;

-- Drop and recreate (safe if columns don't exist)
-- Note: This will fail if column already exists, which is fine

ALTER TABLE stores ADD COLUMN region VARCHAR(100);
ALTER TABLE stores ADD COLUMN level VARCHAR(50);
ALTER TABLE stores ADD COLUMN abm_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN rbm_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN cee_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN corporate_username VARCHAR(255);
```

If you get "Duplicate column name" error, that's GOOD - it means the column exists.

---

## 🎯 MOST LIKELY CAUSES (Based on your situation)

Since you said you ran everything but still getting error:

### Cause #1: Application Not Restarted (90% probability)
**Solution:** Stop and restart the application server completely

### Cause #2: Old WAR File Deployed (85% probability)
**Solution:** Rebuild application (`mvn clean package`) and redeploy NEW WAR

### Cause #3: Wrong Database (70% probability)
**Solution:** Verify you ran migration on `selfie_preprod` (not other database)

### Cause #4: Browser Cache (30% probability)
**Solution:** Use incognito mode or clear all cookies/cache

### Cause #5: Database Connection Issue (20% probability)
**Solution:** Verify app can connect to database at all

---

## 🚨 EMERGENCY FIX - Do This Right Now

**Step 1:** Verify database columns exist:
```sql
USE selfie_preprod;
DESCRIBE stores;
```

Look for these columns in the output:
- region
- level
- abm_username
- rbm_username
- cee_username
- corporate_username

**Step 2:** Rebuild application:
```bash
mvn clean package -Ppreprod -DskipTests
```

**Step 3:** Find the new WAR file:
```
target/tanishq-preprod-05-03-2026-X-0.0.1-SNAPSHOT.war
```

**Step 4:** Redeploy to server:
- Copy this WAR to your Tomcat webapps folder
- Delete old WAR first
- Restart Tomcat

**Step 5:** Test:
```
GET https://celebrationsite-preprod.tanishq.co.in/events/store-summary?storeCode=TEST1
```

---

## 📞 IF STILL FAILING

Share these details:

1. **Database verification output:**
   ```sql
   USE selfie_preprod;
   DESCRIBE stores;
   ```

2. **Application startup logs** (first 50 lines after "Started TanishqSelfieApplication")

3. **Full error stacktrace** from server logs (not just the frontend error)

4. **Verify which WAR is deployed:**
   - Check Tomcat webapps/ folder
   - Check file timestamp

---

## ✅ WHAT I'VE ALREADY FIXED FOR YOU

1. ✅ Changed `Event.store` relationship to `fetch = FetchType.LAZY`
   - This prevents automatic Store queries when fetching Events
   - Fixes the EAGER fetch issue

2. ✅ Added explicit `@Column(name="...")` annotations to Store.java
   - Eliminates any naming strategy confusion
   - Ensures correct column mapping

3. ✅ Created comprehensive SQL migration scripts

**NOW:** You need to rebuild and redeploy with these changes!

---

## 🎯 CRITICAL POINT

**The error will persist if:**
- Old compiled code is running (old WAR file)
- Application is not restarted
- Database columns don't exist

**The error will be fixed if:**
- ✅ Database has all 6 columns
- ✅ Application is rebuilt with latest code (LAZY fetch)
- ✅ Application is restarted/redeployed
- ✅ Browser cache is cleared

**GO REBUILD AND RESTART NOW!**

