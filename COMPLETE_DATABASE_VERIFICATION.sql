-- =====================================================
-- 🔍 COMPLETE DATABASE VERIFICATION SCRIPT
-- Date: January 29, 2026
-- Purpose: Verify all region data, stores, and logins
-- =====================================================

USE selfie_preprod;

SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '🗄️  COMPLETE DATABASE VERIFICATION REPORT' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT DATABASE() AS 'Database Name', NOW() AS 'Verification Time';

-- =====================================================
-- 1️⃣  REGION-WISE STORE DISTRIBUTION
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '📊 SECTION 1: REGION-WISE STORE DISTRIBUTION' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

SELECT
    COALESCE(region, 'NULL/EMPTY') as Region,
    COUNT(*) as Total_Stores,
    COUNT(CASE WHEN store_name IS NOT NULL THEN 1 END) as Stores_With_Name,
    COUNT(CASE WHEN store_code IS NOT NULL THEN 1 END) as Stores_With_Code
FROM stores
GROUP BY region
ORDER BY
    CASE
        WHEN region LIKE 'North%' THEN 1
        WHEN region LIKE 'South%' THEN 2
        WHEN region LIKE 'East%' THEN 3
        WHEN region LIKE 'West%' THEN 4
        ELSE 5
    END,
    region;

-- =====================================================
-- 2️⃣  STORES WITHOUT REGION DATA
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '⚠️  SECTION 2: STORES WITHOUT REGION DATA' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

SELECT
    store_code,
    store_name,
    store_city,
    store_state,
    region as current_region,
    abm_username,
    rbm_username,
    cee_username
FROM stores
WHERE region IS NULL OR region = '' OR region = 'NULL'
ORDER BY store_state, store_city;

-- Count summary
SELECT
    COUNT(*) as Total_Stores_Without_Region,
    (SELECT COUNT(*) FROM stores) as Total_Stores_In_Database,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM stores), 2) as Percentage_Missing
FROM stores
WHERE region IS NULL OR region = '' OR region = 'NULL';

-- =====================================================
-- 3️⃣  DETAILED STORE LIST BY REGION
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '📍 SECTION 3: DETAILED STORE LIST BY REGION' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- North Region Stores
SELECT '──────────────────────────────────────────────────────────────' AS '';
SELECT '🔵 NORTH REGION STORES' AS '';
SELECT '──────────────────────────────────────────────────────────────' AS '';

SELECT
    region as Region,
    store_code as Store_Code,
    store_name as Store_Name,
    store_city as City,
    store_state as State,
    abm_username as ABM,
    rbm_username as RBM,
    cee_username as CEE
FROM stores
WHERE region LIKE 'North%'
ORDER BY region, store_code;

SELECT CONCAT('Total North Region Stores: ', COUNT(*)) as Summary
FROM stores WHERE region LIKE 'North%';

-- South Region Stores
SELECT '──────────────────────────────────────────────────────────────' AS '';
SELECT '🔴 SOUTH REGION STORES' AS '';
SELECT '──────────────────────────────────────────────────────────────' AS '';

SELECT
    region as Region,
    store_code as Store_Code,
    store_name as Store_Name,
    store_city as City,
    store_state as State,
    abm_username as ABM,
    rbm_username as RBM,
    cee_username as CEE
FROM stores
WHERE region LIKE 'South%'
ORDER BY region, store_code;

SELECT CONCAT('Total South Region Stores: ', COUNT(*)) as Summary
FROM stores WHERE region LIKE 'South%';

-- East Region Stores
SELECT '──────────────────────────────────────────────────────────────' AS '';
SELECT '🟢 EAST REGION STORES' AS '';
SELECT '──────────────────────────────────────────────────────────────' AS '';

SELECT
    region as Region,
    store_code as Store_Code,
    store_name as Store_Name,
    store_city as City,
    store_state as State,
    abm_username as ABM,
    rbm_username as RBM,
    cee_username as CEE
FROM stores
WHERE region LIKE 'East%'
ORDER BY region, store_code;

SELECT CONCAT('Total East Region Stores: ', COUNT(*)) as Summary
FROM stores WHERE region LIKE 'East%';

-- West Region Stores
SELECT '──────────────────────────────────────────────────────────────' AS '';
SELECT '🟡 WEST REGION STORES' AS '';
SELECT '──────────────────────────────────────────────────────────────' AS '';

SELECT
    region as Region,
    store_code as Store_Code,
    store_name as Store_Name,
    store_city as City,
    store_state as State,
    abm_username as ABM,
    rbm_username as RBM,
    cee_username as CEE
FROM stores
WHERE region LIKE 'West%'
ORDER BY region, store_code;

SELECT CONCAT('Total West Region Stores: ', COUNT(*)) as Summary
FROM stores WHERE region LIKE 'West%';

-- =====================================================
-- 4️⃣  ABM LOGIN VERIFICATION
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '👤 SECTION 4: ABM (Area Business Manager) LOGIN VERIFICATION' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- ABM Logins List
SELECT
    username as ABM_Username,
    password as ABM_Password,
    (SELECT COUNT(*) FROM stores WHERE abm_username = abm_login.username) as Stores_Assigned
FROM abm_login
ORDER BY username;

-- ABM Summary
SELECT
    COUNT(*) as Total_ABM_Accounts,
    COUNT(DISTINCT username) as Unique_ABM_Usernames,
    (SELECT COUNT(DISTINCT abm_username) FROM stores WHERE abm_username IS NOT NULL) as ABMs_Assigned_To_Stores
FROM abm_login;

-- Stores with ABM but no ABM account
SELECT '──────────────────────────────────────────────────────────────' AS '';
SELECT '⚠️  Stores with ABM assigned but no login account:' AS '';
SELECT '──────────────────────────────────────────────────────────────' AS '';

SELECT DISTINCT
    s.abm_username as ABM_Username,
    COUNT(s.store_code) as Stores_Count
FROM stores s
LEFT JOIN abm_login a ON s.abm_username = a.username
WHERE s.abm_username IS NOT NULL
  AND s.abm_username != ''
  AND a.username IS NULL
GROUP BY s.abm_username;

-- =====================================================
-- 5️⃣  RBM LOGIN VERIFICATION
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '👥 SECTION 5: RBM (Regional Business Manager) LOGIN VERIFICATION' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- RBM Logins List
SELECT
    username as RBM_Username,
    password as RBM_Password,
    (SELECT COUNT(*) FROM stores WHERE rbm_username = rbm_login.username) as Stores_Assigned
FROM rbm_login
ORDER BY username;

-- RBM Summary
SELECT
    COUNT(*) as Total_RBM_Accounts,
    COUNT(DISTINCT username) as Unique_RBM_Usernames,
    (SELECT COUNT(DISTINCT rbm_username) FROM stores WHERE rbm_username IS NOT NULL) as RBMs_Assigned_To_Stores
FROM rbm_login;

-- Stores with RBM but no RBM account
SELECT '──────────────────────────────────────────────────────────────' AS '';
SELECT '⚠️  Stores with RBM assigned but no login account:' AS '';
SELECT '──────────────────────────────────────────────────────────────' AS '';

SELECT DISTINCT
    s.rbm_username as RBM_Username,
    COUNT(s.store_code) as Stores_Count
FROM stores s
LEFT JOIN rbm_login r ON s.rbm_username = r.username
WHERE s.rbm_username IS NOT NULL
  AND s.rbm_username != ''
  AND r.username IS NULL
GROUP BY s.rbm_username;

-- =====================================================
-- 6️⃣  CEE LOGIN VERIFICATION
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '🎯 SECTION 6: CEE (Customer Engagement Executive) LOGIN VERIFICATION' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- CEE Logins List
SELECT
    username as CEE_Username,
    password as CEE_Password,
    (SELECT COUNT(*) FROM stores WHERE cee_username = cee_login.username) as Stores_Assigned
FROM cee_login
ORDER BY username;

-- CEE Summary
SELECT
    COUNT(*) as Total_CEE_Accounts,
    COUNT(DISTINCT username) as Unique_CEE_Usernames,
    (SELECT COUNT(DISTINCT cee_username) FROM stores WHERE cee_username IS NOT NULL) as CEEs_Assigned_To_Stores
FROM cee_login;

-- Stores with CEE but no CEE account
SELECT '──────────────────────────────────────────────────────────────' AS '';
SELECT '⚠️  Stores with CEE assigned but no login account:' AS '';
SELECT '──────────────────────────────────────────────────────────────' AS '';

SELECT DISTINCT
    s.cee_username as CEE_Username,
    COUNT(s.store_code) as Stores_Count
FROM stores s
LEFT JOIN cee_login c ON s.cee_username = c.username
WHERE s.cee_username IS NOT NULL
  AND s.cee_username != ''
  AND c.username IS NULL
GROUP BY s.cee_username;

-- =====================================================
-- 7️⃣  REGION-WISE MANAGER ASSIGNMENTS
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '🗂️  SECTION 7: REGION-WISE MANAGER ASSIGNMENTS' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

SELECT
    region as Region,
    COUNT(DISTINCT abm_username) as Unique_ABMs,
    COUNT(DISTINCT rbm_username) as Unique_RBMs,
    COUNT(DISTINCT cee_username) as Unique_CEEs,
    COUNT(*) as Total_Stores
FROM stores
WHERE region IS NOT NULL AND region != ''
GROUP BY region
ORDER BY
    CASE
        WHEN region LIKE 'North%' THEN 1
        WHEN region LIKE 'South%' THEN 2
        WHEN region LIKE 'East%' THEN 3
        WHEN region LIKE 'West%' THEN 4
        ELSE 5
    END,
    region;

-- =====================================================
-- 8️⃣  EVENTS REGION VERIFICATION
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '📅 SECTION 8: EVENTS REGION DATA VERIFICATION' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- Events with null region
SELECT
    COUNT(*) as Events_With_Null_Region,
    (SELECT COUNT(*) FROM events) as Total_Events,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM events), 2) as Percentage_Null
FROM events
WHERE region IS NULL OR region = '';

-- Events by region
SELECT
    COALESCE(region, 'NULL/EMPTY') as Region,
    COUNT(*) as Total_Events,
    COUNT(CASE WHEN attendees > 0 THEN 1 END) as Events_With_Attendees,
    SUM(COALESCE(attendees, 0)) as Total_Attendees,
    SUM(COALESCE(sale, 0)) as Total_Sales
FROM events
GROUP BY region
ORDER BY
    CASE
        WHEN region LIKE 'North%' THEN 1
        WHEN region LIKE 'South%' THEN 2
        WHEN region LIKE 'East%' THEN 3
        WHEN region LIKE 'West%' THEN 4
        ELSE 5
    END,
    region;

-- =====================================================
-- 9️⃣  DATA INTEGRITY CHECKS
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '✅ SECTION 9: DATA INTEGRITY CHECKS' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- Check 1: Stores without essential data
SELECT '──────────────────────────────────────────────────────────────' AS '';
SELECT 'Check 1: Stores missing essential information' AS '';
SELECT '──────────────────────────────────────────────────────────────' AS '';

SELECT
    store_code,
    store_name,
    CASE WHEN region IS NULL OR region = '' THEN '❌' ELSE '✅' END as Has_Region,
    CASE WHEN store_city IS NULL OR store_city = '' THEN '❌' ELSE '✅' END as Has_City,
    CASE WHEN store_state IS NULL OR store_state = '' THEN '❌' ELSE '✅' END as Has_State,
    CASE WHEN abm_username IS NULL OR abm_username = '' THEN '❌' ELSE '✅' END as Has_ABM,
    CASE WHEN rbm_username IS NULL OR rbm_username = '' THEN '❌' ELSE '✅' END as Has_RBM,
    CASE WHEN cee_username IS NULL OR cee_username = '' THEN '❌' ELSE '✅' END as Has_CEE
FROM stores
WHERE (region IS NULL OR region = '')
   OR (store_city IS NULL OR store_city = '')
   OR (store_state IS NULL OR store_state = '')
   OR (abm_username IS NULL OR abm_username = '')
   OR (rbm_username IS NULL OR rbm_username = '')
   OR (cee_username IS NULL OR cee_username = '')
ORDER BY store_code;

-- Check 2: Events without store linkage
SELECT '──────────────────────────────────────────────────────────────' AS '';
SELECT 'Check 2: Events without valid store linkage' AS '';
SELECT '──────────────────────────────────────────────────────────────' AS '';

SELECT
    e.id as Event_ID,
    e.event_name as Event_Name,
    e.store_code as Store_Code,
    e.region as Event_Region
FROM events e
LEFT JOIN stores s ON e.store_code = s.store_code
WHERE s.store_code IS NULL
ORDER BY e.created_at DESC;

-- Check 3: Duplicate store codes
SELECT '──────────────────────────────────────────────────────────────' AS '';
SELECT 'Check 3: Duplicate store codes (should be empty)' AS '';
SELECT '──────────────────────────────────────────────────────────────' AS '';

SELECT
    store_code,
    COUNT(*) as Duplicate_Count
FROM stores
GROUP BY store_code
HAVING COUNT(*) > 1;

-- =====================================================
-- 🔟  REGIONAL MANAGER LOGIN TEST
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '🔐 SECTION 10: REGIONAL MANAGER LOGIN CODES' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

SELECT '──────────────────────────────────────────────────────────────' AS '';
SELECT 'Regional Manager Codes that should work for login:' AS '';
SELECT '──────────────────────────────────────────────────────────────' AS '';

-- Show which region codes have stores
SELECT
    DISTINCT region as Region_Code,
    COUNT(*) as Stores_Count,
    CASE
        WHEN region IN ('east1','east2','north1','north1a','north1b','north2','north3','north4',
                       'south1','south2','south2a','south3','west1','west1a','west1b','west2','west3','test')
        THEN '✅ Login Enabled'
        ELSE '⚠️ Check Code'
    END as Login_Status
FROM stores
WHERE region IS NOT NULL AND region != ''
GROUP BY region
ORDER BY
    CASE
        WHEN region LIKE 'North%' THEN 1
        WHEN region LIKE 'South%' THEN 2
        WHEN region LIKE 'East%' THEN 3
        WHEN region LIKE 'West%' THEN 4
        ELSE 5
    END,
    region;

-- =====================================================
-- 📊 FINAL SUMMARY REPORT
-- =====================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '📊 FINAL SUMMARY REPORT' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

SELECT
    'Total Stores' as Metric,
    COUNT(*) as Count,
    '100%' as Percentage
FROM stores
UNION ALL
SELECT
    'Stores with Region',
    COUNT(*),
    CONCAT(ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM stores), 2), '%')
FROM stores WHERE region IS NOT NULL AND region != ''
UNION ALL
SELECT
    'Stores without Region',
    COUNT(*),
    CONCAT(ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM stores), 2), '%')
FROM stores WHERE region IS NULL OR region = ''
UNION ALL
SELECT
    'Total Events',
    COUNT(*),
    '100%'
FROM events
UNION ALL
SELECT
    'Events with Region',
    COUNT(*),
    CONCAT(ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM events), 2), '%')
FROM events WHERE region IS NOT NULL AND region != ''
UNION ALL
SELECT
    'Events without Region',
    COUNT(*),
    CONCAT(ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM events), 2), '%')
FROM events WHERE region IS NULL OR region = ''
UNION ALL
SELECT
    'Total ABM Accounts',
    COUNT(*),
    '-'
FROM abm_login
UNION ALL
SELECT
    'Total RBM Accounts',
    COUNT(*),
    '-'
FROM rbm_login
UNION ALL
SELECT
    'Total CEE Accounts',
    COUNT(*),
    '-'
FROM cee_login;

SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '✅ VERIFICATION COMPLETED' AS '';
SELECT NOW() as 'Report Generated At';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- =====================================================
-- END OF VERIFICATION SCRIPT
-- =====================================================

