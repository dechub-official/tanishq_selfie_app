-- ============================================
-- Corporate Login Testing Script
-- ============================================
-- Run this script to verify the corporate login implementation

USE selfie_prod;

-- ============================================
-- 1. Verify corporate_login table exists
-- ============================================
SELECT 'Checking corporate_login table...' AS Step;
DESCRIBE corporate_login;

-- ============================================
-- 2. Check all corporate users
-- ============================================
SELECT 'Corporate Users:' AS Step;
SELECT
    corporate_user_id,
    corporate_name,
    region,
    organization,
    access_level,
    email
FROM corporate_login
ORDER BY access_level, region;

-- ============================================
-- 3. Verify corporate_username column in stores
-- ============================================
SELECT 'Checking stores table structure...' AS Step;
SHOW COLUMNS FROM stores LIKE 'corporate_username';

-- ============================================
-- 4. Count stores mapped to each corporate user
-- ============================================
SELECT 'Stores mapped to corporate users:' AS Step;
SELECT
    corporate_username,
    COUNT(*) as total_stores,
    COUNT(DISTINCT region) as unique_regions,
    GROUP_CONCAT(DISTINCT region ORDER BY region SEPARATOR ', ') as regions
FROM stores
WHERE corporate_username IS NOT NULL
GROUP BY corporate_username
ORDER BY total_stores DESC;

-- ============================================
-- 5. Sample stores with corporate mapping
-- ============================================
SELECT 'Sample store mappings (first 20):' AS Step;
SELECT
    store_code,
    store_name,
    store_city,
    region,
    corporate_username,
    cee_username,
    abm_username
FROM stores
WHERE corporate_username IS NOT NULL
LIMIT 20;

-- ============================================
-- 6. Test CORP-NATIONAL-01 access
-- ============================================
SELECT 'CORP-NATIONAL-01 Store Access:' AS Step;
SELECT
    COUNT(*) as total_stores,
    COUNT(DISTINCT store_state) as states,
    COUNT(DISTINCT region) as regions
FROM stores
WHERE corporate_username = 'CORP-NATIONAL-01';

-- ============================================
-- 7. Test Regional Corporate Access
-- ============================================
SELECT 'Regional Corporate Access:' AS Step;
SELECT
    corporate_username,
    COUNT(*) as stores,
    GROUP_CONCAT(DISTINCT region ORDER BY region) as covered_regions
FROM stores
WHERE corporate_username IN ('CORP-NORTH', 'CORP-SOUTH', 'CORP-EAST', 'CORP-WEST')
GROUP BY corporate_username;

-- ============================================
-- 8. Check for unmapped stores
-- ============================================
SELECT 'Unmapped stores (no corporate user):' AS Step;
SELECT COUNT(*) as unmapped_store_count
FROM stores
WHERE corporate_username IS NULL;

-- If count > 0, you may want to assign them:
-- Sample stores without corporate mapping
SELECT
    store_code,
    store_name,
    store_city,
    region,
    cee_username
FROM stores
WHERE corporate_username IS NULL
LIMIT 10;

-- ============================================
-- 9. Verify Login Credentials
-- ============================================
SELECT 'Testing login credentials:' AS Step;
SELECT
    corporate_user_id,
    corporate_name,
    CASE
        WHEN password = 'Tanishq@Corp2026' THEN '✓ Password Correct'
        WHEN password = 'Titan@Corp2026' THEN '✓ Password Correct'
        ELSE '✗ Unexpected Password'
    END as password_status
FROM corporate_login;

-- ============================================
-- 10. Full Hierarchy View
-- ============================================
SELECT 'Full hierarchy for sample stores:' AS Step;
SELECT
    s.store_code,
    s.store_name,
    s.region,
    s.corporate_username,
    c.corporate_name,
    s.abm_username,
    s.rbm_username,
    s.cee_username
FROM stores s
LEFT JOIN corporate_login c ON s.corporate_username = c.corporate_user_id
WHERE s.corporate_username IS NOT NULL
LIMIT 15;

-- ============================================
-- 11. Event Statistics by Corporate User
-- ============================================
SELECT 'Event statistics by corporate user:' AS Step;
SELECT
    s.corporate_username,
    c.corporate_name,
    COUNT(DISTINCT s.store_code) as total_stores,
    COUNT(e.id) as total_events,
    COUNT(DISTINCT e.event_type) as event_types
FROM stores s
LEFT JOIN corporate_login c ON s.corporate_username = c.corporate_user_id
LEFT JOIN events e ON s.store_code = e.store_code
WHERE s.corporate_username IS NOT NULL
GROUP BY s.corporate_username, c.corporate_name
ORDER BY total_events DESC;

-- ============================================
-- 12. Verify All Access Levels Work
-- ============================================
SELECT 'Testing store access for each corporate user:' AS Step;

-- National Access
SELECT 'CORP-NATIONAL-01' as user_id, COUNT(*) as accessible_stores
FROM stores WHERE corporate_username = 'CORP-NATIONAL-01'
UNION ALL
-- Regional Access
SELECT 'CORP-NORTH', COUNT(*) FROM stores WHERE corporate_username = 'CORP-NORTH'
UNION ALL
SELECT 'CORP-SOUTH', COUNT(*) FROM stores WHERE corporate_username = 'CORP-SOUTH'
UNION ALL
SELECT 'CORP-EAST', COUNT(*) FROM stores WHERE corporate_username = 'CORP-EAST'
UNION ALL
SELECT 'CORP-WEST', COUNT(*) FROM stores WHERE corporate_username = 'CORP-WEST';

-- ============================================
-- EXPECTED RESULTS SUMMARY
-- ============================================
/*
EXPECTED RESULTS:

1. corporate_login table should exist with 6 users
2. stores table should have corporate_username column
3. CORP-NATIONAL-01 should have access to ALL stores
4. Regional corporate users should have stores in their regions
5. All passwords should be either "Tanishq@Corp2026" or "Titan@Corp2026"
6. Hierarchy: CORPORATE -> ABM -> RBM -> CEE -> STORE should be visible
7. Events should be queryable by corporate user

If any step fails, refer to CORPORATE_LOGIN_IMPLEMENTATION_GUIDE.md
*/

SELECT '=== TESTING COMPLETE ===' AS Status;
SELECT 'If all queries ran successfully, corporate login is properly configured!' AS Message;

