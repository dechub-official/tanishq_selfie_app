-- =====================================================
-- FIX PRODUCTION DATABASE ISSUES
-- Date: January 29, 2026
-- Database: selfie_prod
-- Issues: 88 stores without region, 30,857 events without region
-- =====================================================

USE selfie_prod;

-- =====================================================
-- STEP 1: BACKUP CURRENT DATA (IMPORTANT!)
-- =====================================================
SELECT 'Creating backup tables...' AS 'Step 1';

CREATE TABLE IF NOT EXISTS stores_backup_20260129 AS SELECT * FROM stores;
CREATE TABLE IF NOT EXISTS events_backup_20260129 AS SELECT * FROM events;

SELECT 'Backup created successfully!' AS 'Status';
SELECT COUNT(*) AS 'Stores Backed Up' FROM stores_backup_20260129;
SELECT COUNT(*) AS 'Events Backed Up' FROM events_backup_20260129;

-- =====================================================
-- STEP 2: STANDARDIZE REGION NAMES (Fix Case Issues)
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'STEP 2: Standardizing Region Names' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- Standardize North regions
UPDATE stores SET region = 'north1' WHERE region IN ('NORTH 1', 'North 1', 'north1');
UPDATE stores SET region = 'north2' WHERE region IN ('NORTH 2', 'North 2', 'north2');
UPDATE stores SET region = 'north3' WHERE region IN ('NORTH 3', 'North 3', 'north3');
UPDATE stores SET region = 'north4' WHERE region IN ('NORTH 4', 'North 4', 'north4');

-- Standardize South regions
UPDATE stores SET region = 'south1' WHERE region IN ('SOUTH 1', 'South 1', 'south1', 'South1');
UPDATE stores SET region = 'south2' WHERE region IN ('SOUTH 2', 'South 2', 'south2', 'South-2');
UPDATE stores SET region = 'south3' WHERE region IN ('SOUTH 3', 'South 3', 'south3');

-- Standardize East regions
UPDATE stores SET region = 'east1' WHERE region IN ('EAST 1', 'East 1', 'east1', 'East1');
UPDATE stores SET region = 'east2' WHERE region IN ('EAST 2', 'East 2', 'east2');

-- Standardize West regions
UPDATE stores SET region = 'west1' WHERE region IN ('WEST 1', 'West 1', 'west1', 'West-1');
UPDATE stores SET region = 'west2' WHERE region IN ('WEST 2', 'West 2', 'west2', 'West-2');
UPDATE stores SET region = 'west3' WHERE region IN ('WEST 3', 'West 3', 'west3', 'West3');
UPDATE stores SET region = 'west' WHERE region = 'WEST';

SELECT 'Region names standardized!' AS 'Status';

-- Show current state
SELECT
    region,
    COUNT(*) as store_count
FROM stores
GROUP BY region
ORDER BY region;

-- =====================================================
-- STEP 3: FIX STORES WITHOUT REGION (Manual Assignment Needed)
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'STEP 3: Assigning Regions to Stores Without Region' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- IMPORTANT: You need to manually map these stores to regions
-- Based on state/city, here are suggested mappings:

-- Andhra Pradesh -> south1
UPDATE stores SET region = 'south1'
WHERE (region IS NULL OR region = '')
  AND store_state IN ('Andhra Pradesh', 'Tamilnadu', 'Tamil Nadu');

-- Telangana -> south3
UPDATE stores SET region = 'south3'
WHERE (region IS NULL OR region = '')
  AND store_state IN ('Telangana', 'TELANGANA');

-- Karnataka, Kerala, Goa -> south2
UPDATE stores SET region = 'south2'
WHERE (region IS NULL OR region = '')
  AND store_state IN ('Karnataka', 'KARNATAKA', 'Karanataka', 'Kerala', 'Goa');

-- West Bengal, Odisha, Assam, etc. -> east1
UPDATE stores SET region = 'east1'
WHERE (region IS NULL OR region = '')
  AND store_state IN ('West Bengal', 'WEST BENGAL', 'Odisha', 'Orrisa', 'Assam',
                      'ARUNCHAL PRADESH', 'Tripura', 'Manipur', 'Nagaland',
                      'Andaman and Nicobar');

-- Bihar, Jharkhand -> east2
UPDATE stores SET region = 'east2'
WHERE (region IS NULL OR region = '')
  AND store_state IN ('Bihar', 'Jharkhand');

-- Delhi, Haryana, Rajasthan -> north1
UPDATE stores SET region = 'north1'
WHERE (region IS NULL OR region = '')
  AND store_state IN ('Delhi', 'DELHI', 'Haryana', 'HARYANA', 'Rajasthan');

-- UP (except eastern UP) -> north2
UPDATE stores SET region = 'north2'
WHERE (region IS NULL OR region = '')
  AND store_state IN ('Uttar Pradesh', 'UTTAR PRADESH', 'Uttar Pardesh', 'Uttarakhand');

-- Punjab, Himachal, J&K -> north3
UPDATE stores SET region = 'north3'
WHERE (region IS NULL OR region = '')
  AND store_state IN ('Punjab', 'Himachal Pradesh', 'Jammu & Kashmir');

-- Gujarat, Maharashtra (except Mumbai) -> west1
UPDATE stores SET region = 'west1'
WHERE (region IS NULL OR region = '')
  AND store_state IN ('Gujarat', 'Madhya Pradesh');

-- Maharashtra (Mumbai region) -> west2
UPDATE stores SET region = 'west2'
WHERE (region IS NULL OR region = '')
  AND store_state IN ('Maharashtra', 'Maharastra');

-- Special cases - stores with NULL state or unknown
UPDATE stores SET region = 'test'
WHERE store_code IN ('KRN', 'UNK', 'HKL', 'ABH', 'TBM', 'AFD')
  AND (region IS NULL OR region = '');

SELECT 'Region assignment completed!' AS 'Status';

-- Check how many stores still don't have region
SELECT
    COUNT(*) as stores_still_without_region
FROM stores
WHERE region IS NULL OR region = '';

-- Show stores that still need manual assignment
SELECT
    store_code,
    store_name,
    store_city,
    store_state,
    region
FROM stores
WHERE region IS NULL OR region = ''
ORDER BY store_state, store_city;

-- =====================================================
-- STEP 4: FIX EVENTS WITHOUT REGION
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'STEP 4: Fixing Events Without Region (This may take a minute...)' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- Update events to get region from their store
UPDATE events e
JOIN stores s ON e.store_code = s.store_code
SET e.region = s.region
WHERE (e.region IS NULL OR e.region = '')
  AND s.region IS NOT NULL
  AND s.region != '';

SELECT 'Events region update completed!' AS 'Status';

-- Check how many events still don't have region
SELECT
    COUNT(*) as events_without_region,
    (SELECT COUNT(*) FROM events) as total_events
FROM events
WHERE region IS NULL OR region = '';

-- =====================================================
-- STEP 5: STANDARDIZE EMPTY STRINGS TO NULL
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'STEP 5: Standardizing Empty Strings to NULL' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- For stores
UPDATE stores SET region = NULL WHERE region = '';
UPDATE stores SET abm_username = NULL WHERE abm_username = '';
UPDATE stores SET rbm_username = NULL WHERE rbm_username = '';
UPDATE stores SET cee_username = NULL WHERE cee_username = '';

-- For events
UPDATE events SET region = NULL WHERE region = '';

SELECT 'Empty strings standardized!' AS 'Status';

-- =====================================================
-- STEP 6: VERIFICATION
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'STEP 6: FINAL VERIFICATION' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- Stores verification
SELECT 'STORES SUMMARY:' AS '';
SELECT
    'Total Stores' as metric,
    COUNT(*) as count
FROM stores
UNION ALL
SELECT
    'Stores with Region',
    COUNT(*)
FROM stores WHERE region IS NOT NULL
UNION ALL
SELECT
    'Stores without Region',
    COUNT(*)
FROM stores WHERE region IS NULL;

-- Region distribution
SELECT '' AS '';
SELECT 'REGION DISTRIBUTION:' AS '';
SELECT
    COALESCE(region, 'NULL') as region,
    COUNT(*) as store_count
FROM stores
GROUP BY region
ORDER BY region;

-- Events verification
SELECT '' AS '';
SELECT 'EVENTS SUMMARY:' AS '';
SELECT
    'Total Events' as metric,
    COUNT(*) as count
FROM events
UNION ALL
SELECT
    'Events with Region',
    COUNT(*)
FROM events WHERE region IS NOT NULL
UNION ALL
SELECT
    'Events without Region',
    COUNT(*)
FROM events WHERE region IS NULL;

-- =====================================================
-- STEP 7: CREATE REPORT OF CHANGES
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'CHANGES MADE REPORT' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

SELECT
    'Stores Before Fix' as description,
    (SELECT COUNT(*) FROM stores_backup_20260129 WHERE region IS NULL OR region = '') as count
UNION ALL
SELECT
    'Stores After Fix',
    (SELECT COUNT(*) FROM stores WHERE region IS NULL) as count
UNION ALL
SELECT
    'Stores Fixed',
    (SELECT COUNT(*) FROM stores_backup_20260129 WHERE region IS NULL OR region = '') -
    (SELECT COUNT(*) FROM stores WHERE region IS NULL) as count
UNION ALL
SELECT
    '---',
    NULL
UNION ALL
SELECT
    'Events Before Fix',
    (SELECT COUNT(*) FROM events_backup_20260129 WHERE region IS NULL OR region = '')
UNION ALL
SELECT
    'Events After Fix',
    (SELECT COUNT(*) FROM events WHERE region IS NULL)
UNION ALL
SELECT
    'Events Fixed',
    (SELECT COUNT(*) FROM events_backup_20260129 WHERE region IS NULL OR region = '') -
    (SELECT COUNT(*) FROM events WHERE region IS NULL);

SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '✅ FIX COMPLETED!' AS '';
SELECT NOW() as 'Completed At';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- =====================================================
-- NOTES FOR MANUAL VERIFICATION
-- =====================================================
/*
NEXT STEPS:

1. Verify in Application:
   - Login with regional codes (north1, south2, etc.)
   - Download event reports - check region column shows data
   - Test ABM/RBM/CEE logins

2. If some stores still don't have region:
   - Check the output of "stores that still need manual assignment"
   - Use this query to update them:
     UPDATE stores SET region = 'regionX' WHERE store_code = 'CODE';

3. To rollback if needed:
   DROP TABLE stores;
   CREATE TABLE stores AS SELECT * FROM stores_backup_20260129;
   DROP TABLE events;
   CREATE TABLE events AS SELECT * FROM events_backup_20260129;

4. Login Table Issues:
   Your abm_login, rbm_login, cee_login tables seem to have wrong column names.
   Check the actual column names with:
   DESCRIBE abm_login;
   DESCRIBE rbm_login;
   DESCRIBE cee_login;
*/

-- =====================================================
-- END OF FIX SCRIPT
-- =====================================================

