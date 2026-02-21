-- =====================================================
-- MySQL Database Verification SQL Script
-- Run this directly in MySQL Workbench or command line
-- =====================================================

USE selfie_preprod;

-- Display current database
SELECT DATABASE() AS 'Current Database', NOW() AS 'Verification Time';

-- =====================================================
-- 1. VERIFY ALL TABLES EXIST
-- =====================================================
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '📋 TABLE EXISTENCE VERIFICATION' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

SHOW TABLES;

-- =====================================================
-- 2. TABLE RECORD COUNTS
-- =====================================================
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '📊 RECORD COUNTS BY TABLE' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

SELECT 'events' as table_name, COUNT(*) as record_count FROM events
UNION ALL
SELECT 'stores', COUNT(*) FROM stores
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'user_details', COUNT(*) FROM user_details
UNION ALL
SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL
SELECT 'greetings', COUNT(*) FROM greetings
UNION ALL
SELECT 'abm_login', COUNT(*) FROM abm_login
UNION ALL
SELECT 'rbm_login', COUNT(*) FROM rbm_login
UNION ALL
SELECT 'cee_login', COUNT(*) FROM cee_login
UNION ALL
SELECT 'password_history', COUNT(*) FROM password_history
UNION ALL
SELECT 'rivaah', COUNT(*) FROM rivaah
UNION ALL
SELECT 'rivaah_users', COUNT(*) FROM rivaah_users
UNION ALL
SELECT 'product_details', COUNT(*) FROM product_details
ORDER BY record_count DESC;

-- =====================================================
-- 3. VERIFY TABLE STRUCTURES
-- =====================================================
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '🏗️ EVENTS TABLE STRUCTURE' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

DESCRIBE events;

SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '🏗️ STORES TABLE STRUCTURE' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

DESCRIBE stores;

SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '🏗️ ATTENDEES TABLE STRUCTURE' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

DESCRIBE attendees;

-- =====================================================
-- 4. VERIFY FOREIGN KEY RELATIONSHIPS
-- =====================================================
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '🔗 FOREIGN KEY RELATIONSHIPS' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

SELECT
    TABLE_NAME AS 'Table',
    COLUMN_NAME AS 'Column',
    CONSTRAINT_NAME AS 'Constraint',
    CONCAT(REFERENCED_TABLE_NAME, '.', REFERENCED_COLUMN_NAME) AS 'References'
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'selfie_preprod'
AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME;

-- =====================================================
-- 5. CHECK DATA INTEGRITY
-- =====================================================
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '🔍 DATA INTEGRITY CHECKS' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

-- Check for orphaned attendees
SELECT 'Orphaned Attendees' AS 'Check',
       COUNT(*) AS 'Count',
       CASE WHEN COUNT(*) = 0 THEN '✓ PASS' ELSE '✗ FAIL' END AS 'Status'
FROM attendees
WHERE event_id NOT IN (SELECT id FROM events);

-- Check for orphaned invitees
SELECT 'Orphaned Invitees' AS 'Check',
       COUNT(*) AS 'Count',
       CASE WHEN COUNT(*) = 0 THEN '✓ PASS' ELSE '✗ FAIL' END AS 'Status'
FROM invitees
WHERE event_id NOT IN (SELECT id FROM events);

-- Check for events without valid stores
SELECT 'Events without Store' AS 'Check',
       COUNT(*) AS 'Count',
       CASE WHEN COUNT(*) = 0 THEN '✓ PASS' ELSE '✗ FAIL' END AS 'Status'
FROM events
WHERE store_code NOT IN (SELECT store_code FROM stores);

-- Check for product_details without rivaah
SELECT 'Products without Rivaah' AS 'Check',
       COUNT(*) AS 'Count',
       CASE WHEN COUNT(*) = 0 THEN '✓ PASS' ELSE '✗ FAIL' END AS 'Status'
FROM product_details
WHERE rivaah_id NOT IN (SELECT id FROM rivaah);

-- Check for rivaah_users without rivaah
SELECT 'Rivaah Users without Rivaah' AS 'Check',
       COUNT(*) AS 'Count',
       CASE WHEN COUNT(*) = 0 THEN '✓ PASS' ELSE '✗ FAIL' END AS 'Status'
FROM rivaah_users
WHERE rivaah_id NOT IN (SELECT id FROM rivaah);

-- =====================================================
-- 6. CHECK INDEXES
-- =====================================================
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '🔑 INDEX INFORMATION' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

SELECT
    TABLE_NAME AS 'Table',
    INDEX_NAME AS 'Index',
    COLUMN_NAME AS 'Column',
    NON_UNIQUE AS 'Non-Unique',
    INDEX_TYPE AS 'Type'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'selfie_preprod'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- =====================================================
-- 7. DATABASE SIZE INFORMATION
-- =====================================================
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '💾 TABLE SIZE INFORMATION' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

SELECT
    table_name AS 'Table',
    table_rows AS 'Rows',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)',
    ROUND((data_length / 1024 / 1024), 2) AS 'Data (MB)',
    ROUND((index_length / 1024 / 1024), 2) AS 'Index (MB)'
FROM information_schema.TABLES
WHERE table_schema = 'selfie_preprod'
ORDER BY (data_length + index_length) DESC;

-- =====================================================
-- 8. SAMPLE DATA VERIFICATION
-- =====================================================
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '📝 SAMPLE DATA - RECENT EVENTS' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

SELECT
    e.id,
    e.event_name,
    s.store_name,
    e.start_date,
    e.attendees,
    e.invitees,
    e.created_at
FROM events e
LEFT JOIN stores s ON e.store_code = s.store_code
ORDER BY e.created_at DESC
LIMIT 5;

SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '📝 SAMPLE DATA - STORES' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

SELECT
    store_code,
    store_name,
    store_city,
    region,
    abm_username,
    rbm_username
FROM stores
LIMIT 10;

-- =====================================================
-- 9. UNIQUE CONSTRAINTS VERIFICATION
-- =====================================================
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '⚡ UNIQUE CONSTRAINTS' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

-- Check for duplicate usernames in ABM login
SELECT 'ABM Login - Duplicate Users' AS 'Check',
       COUNT(*) - COUNT(DISTINCT abm_user_id) AS 'Duplicates',
       CASE WHEN COUNT(*) = COUNT(DISTINCT abm_user_id) THEN '✓ PASS' ELSE '✗ FAIL' END AS 'Status'
FROM abm_login;

-- Check for duplicate usernames in RBM login
SELECT 'RBM Login - Duplicate Users' AS 'Check',
       COUNT(*) - COUNT(DISTINCT rbm_user_id) AS 'Duplicates',
       CASE WHEN COUNT(*) = COUNT(DISTINCT rbm_user_id) THEN '✓ PASS' ELSE '✗ FAIL' END AS 'Status'
FROM rbm_login;

-- Check for duplicate usernames in CEE login
SELECT 'CEE Login - Duplicate Users' AS 'Check',
       COUNT(*) - COUNT(DISTINCT cee_user_id) AS 'Duplicates',
       CASE WHEN COUNT(*) = COUNT(DISTINCT cee_user_id) THEN '✓ PASS' ELSE '✗ FAIL' END AS 'Status'
FROM cee_login;

-- Check for duplicate store codes
SELECT 'Stores - Duplicate Codes' AS 'Check',
       COUNT(*) - COUNT(DISTINCT store_code) AS 'Duplicates',
       CASE WHEN COUNT(*) = COUNT(DISTINCT store_code) THEN '✓ PASS' ELSE '✗ FAIL' END AS 'Status'
FROM stores;

-- =====================================================
-- 10. SUMMARY REPORT
-- =====================================================
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '✅ VERIFICATION SUMMARY' AS '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

SELECT
    'Database Name' AS 'Item',
    DATABASE() AS 'Value'
UNION ALL
SELECT
    'Total Tables',
    CAST(COUNT(*) AS CHAR)
FROM information_schema.TABLES
WHERE table_schema = 'selfie_preprod'
UNION ALL
SELECT
    'Total Records',
    CAST((
        (SELECT COUNT(*) FROM events) +
        (SELECT COUNT(*) FROM stores) +
        (SELECT COUNT(*) FROM attendees) +
        (SELECT COUNT(*) FROM invitees) +
        (SELECT COUNT(*) FROM users) +
        (SELECT COUNT(*) FROM user_details) +
        (SELECT COUNT(*) FROM bride_details) +
        (SELECT COUNT(*) FROM greetings) +
        (SELECT COUNT(*) FROM abm_login) +
        (SELECT COUNT(*) FROM rbm_login) +
        (SELECT COUNT(*) FROM cee_login) +
        (SELECT COUNT(*) FROM password_history) +
        (SELECT COUNT(*) FROM rivaah) +
        (SELECT COUNT(*) FROM rivaah_users) +
        (SELECT COUNT(*) FROM product_details)
    ) AS CHAR)
UNION ALL
SELECT
    'Database Size (MB)',
    CAST(ROUND(SUM((data_length + index_length) / 1024 / 1024), 2) AS CHAR)
FROM information_schema.TABLES
WHERE table_schema = 'selfie_preprod';

SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT 'Verification completed successfully!' AS '';
SELECT NOW() AS 'Completed At';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

