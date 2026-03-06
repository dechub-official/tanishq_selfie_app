# STORE SUMMARY ERROR - DIAGNOSIS AND FIX

## Problem Summary
**Error:** `SQLGrammarException: could not extract ResultSet`
**Endpoint:** `GET /events/store-summary?storeCode=TEST1&startDate=&endDate=`
**Status:** 500 Internal Server Error

## Root Cause Analysis

### ✅ This is a **BACKEND/DATABASE** issue, NOT a frontend issue.

The error occurs because:

1. **Missing Database Columns**: Your `stores` table is missing critical columns that the JPA entity `Store.java` expects:
   - `region`
   - `level`
   - `abm_username`
   - `rbm_username`
   - `cee_username`
   - `corporate_username`

2. **How the error happens**:
   - Frontend calls: `/events/store-summary?storeCode=TEST1`
   - Backend routes to appropriate service based on store code
   - Service methods (`getRbmSummary`, `getCeeSummary`, `getAbmSummary`, `getCorporateSummary`) call repository methods
   - Repository tries to query columns that don't exist in database
   - Hibernate throws `SQLGrammarException`

## Solution Steps

### Step 1: Run the Database Migration Script

I've created `FIX_MISSING_COLUMNS.sql` which will add all missing columns to your `stores` table.

**Action Required:**
1. Connect to your preprod database
2. Run the SQL script: `sql/FIX_MISSING_COLUMNS.sql`
3. This will add:
   - `region` column
   - `level` column
   - `abm_username` column
   - `rbm_username` column
   - `cee_username` column
   - `corporate_username` column
4. Create necessary indexes for performance

### Step 2: Populate the New Columns

After adding the columns, you need to:

1. **Create user logins** (if not already done):
   - Run `sql/02_create_abm_logins.sql` or `sql/02_abm_logins.sql`
   - Run `sql/03_rbm_logins.sql`
   - Run `sql/04_cee_logins.sql`
   - Run `sql/06_create_corporate_login.sql` or `sql/06_create_corporate_login_PRODUCTION.sql`

2. **Map stores to users**:
   - Run `sql/05_map_stores.sql`
   - Or manually update stores with appropriate usernames

### Step 3: Verify the Fix

Run these SQL queries to verify:

```sql
-- Check if columns exist
DESCRIBE stores;

-- Check if data is populated
SELECT 
    store_code,
    region,
    abm_username,
    rbm_username,
    cee_username,
    corporate_username
FROM stores
LIMIT 10;
```

## Frontend vs Backend Analysis

### Frontend Code (CORRECT ✅)
The frontend is making the correct API call:
```javascript
const V = await me.get(`/store-summary?storeCode=${e}&startDate=${F}&endDate=${B}`);
```

When dates are empty, the URL becomes:
`/events/store-summary?storeCode=TEST1&startDate=&endDate=`

This is **valid** - the backend handles optional parameters with `@RequestParam(required = false)`

### Backend Code (CORRECT ✅)
The controller and service layer are correctly implemented:
```java
@GetMapping("/store-summary")
public ResponseEntity<ApiResponse<StoreEventSummaryDTO>> getStoreSummary(
        @RequestParam String storeCode,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate) {
    // ... routes to appropriate service
}
```

### Database Schema (INCORRECT ❌)
The `stores` table structure in your preprod database doesn't match the JPA entity.

**Expected (from Store.java)**:
- All standard store fields PLUS:
- `region` VARCHAR(100)
- `level` VARCHAR(50)
- `abm_username` VARCHAR(255)
- `rbm_username` VARCHAR(255)
- `cee_username` VARCHAR(255)
- `corporate_username` VARCHAR(255)

**Actual (in database)**:
- Missing the above 6 columns

## Quick Test After Fix

After running the migration script, test with:

```bash
# Test the API endpoint
curl -X GET "https://celebrationsite-preprod.tanishq.co.in/events/store-summary?storeCode=TEST1&startDate=&endDate=" \
  -H "Cookie: JSESSIONID=your-session-id"
```

Or test from frontend after logging in.

## Additional Notes

### Empty startDate/endDate Parameters
The backend correctly handles empty date parameters:
```java
@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate
```

When `startDate` and `endDate` are empty strings `""`, Spring converts them to `null`, which is correct behavior.

### Store Code TEST1
Make sure `TEST1` exists in your `stores` table after the migration:
```sql
SELECT * FROM stores WHERE store_code = 'TEST1';
```

If it doesn't exist, you need to insert it:
```sql
INSERT INTO stores (store_code, store_name, region, abm_username, rbm_username, cee_username, corporate_username)
VALUES ('TEST1', 'Test Store 1', 'TEST_REGION', NULL, NULL, NULL, NULL);
```

## Summary

✅ **Frontend**: Working correctly  
✅ **Backend Code**: Working correctly  
❌ **Database Schema**: Missing columns - **THIS IS THE ISSUE**  

**Action Required**: Run `sql/FIX_MISSING_COLUMNS.sql` on your preprod database.

