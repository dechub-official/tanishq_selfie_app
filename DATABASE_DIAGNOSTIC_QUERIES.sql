-- ============================================================================
-- DATABASE ANALYSIS & DIAGNOSTIC QUERIES
-- Tanishq Selfie App - Complete Database Health Check
-- ============================================================================
-- Purpose: Comprehensive database analysis and issue detection
-- Run these queries to understand current database state
-- ============================================================================

-- ============================================================================
-- SECTION 1: DATABASE OVERVIEW
-- ============================================================================

-- 1.1 List all tables in the database
SELECT
    TABLE_NAME,
    TABLE_TYPE,
    ENGINE,
    TABLE_ROWS,
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'Size (MB)',
    ROUND((DATA_LENGTH / 1024 / 1024), 2) AS 'Data Size (MB)',
    ROUND((INDEX_LENGTH / 1024 / 1024), 2) AS 'Index Size (MB)',
    CREATE_TIME,
    UPDATE_TIME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;

-- 1.2 Identify backup tables
SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND (TABLE_NAME LIKE '%backup%'
       OR TABLE_NAME LIKE '%staging%'
       OR TABLE_NAME LIKE '%recovery%'
       OR TABLE_NAME LIKE '%test%'
       OR TABLE_NAME LIKE '%old%')
ORDER BY TABLE_NAME;

-- 1.3 Core tables summary
SELECT
    'stores' AS table_name, COUNT(*) AS record_count FROM stores
UNION ALL SELECT 'events', COUNT(*) FROM events
UNION ALL SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL SELECT 'greetings', COUNT(*) FROM greetings
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'user_details', COUNT(*) FROM user_details
UNION ALL SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL SELECT 'password_history', COUNT(*) FROM password_history
UNION ALL SELECT 'abm_login', COUNT(*) FROM abm_login
UNION ALL SELECT 'rbm_login', COUNT(*) FROM rbm_login
UNION ALL SELECT 'cee_login', COUNT(*) FROM cee_login
UNION ALL SELECT 'rivaah', COUNT(*) FROM rivaah
UNION ALL SELECT 'rivaah_users', COUNT(*) FROM rivaah_users
UNION ALL SELECT 'product_details', COUNT(*) FROM product_details;

-- ============================================================================
-- SECTION 2: RELATIONSHIP & INTEGRITY CHECKS
-- ============================================================================

-- 2.1 Check all foreign key constraints
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME;

-- 2.2 Find orphaned attendees (attendees without valid events)
SELECT
    a.id,
    a.name,
    a.event_id,
    'Orphaned Attendee' AS issue
FROM attendees a
LEFT JOIN events e ON a.event_id = e.id
WHERE e.id IS NULL;

-- 2.3 Find orphaned invitees
SELECT
    i.id,
    i.name,
    i.event_id,
    'Orphaned Invitee' AS issue
FROM invitees i
LEFT JOIN events e ON i.event_id = e.id
WHERE e.id IS NULL;

-- 2.4 Find events without valid store codes
SELECT
    e.id,
    e.event_name,
    e.store_code,
    'Invalid Store Code' AS issue
FROM events e
LEFT JOIN stores s ON e.store_code = s.store_code
WHERE s.store_code IS NULL;

-- 2.5 Check for events with mismatched attendee counts
SELECT
    e.id,
    e.event_name,
    e.attendees AS recorded_count,
    COUNT(a.id) AS actual_count,
    (e.attendees - COUNT(a.id)) AS difference
FROM events e
LEFT JOIN attendees a ON e.event_id = a.event_id
GROUP BY e.id, e.event_name, e.attendees
HAVING e.attendees != COUNT(a.id)
ORDER BY ABS(e.attendees - COUNT(a.id)) DESC;

-- 2.6 Check for events with mismatched invitee counts
SELECT
    e.id,
    e.event_name,
    e.invitees AS recorded_count,
    COUNT(i.id) AS actual_count,
    (e.invitees - COUNT(i.id)) AS difference
FROM events e
LEFT JOIN invitees i ON e.event_id = i.event_id
GROUP BY e.id, e.event_name, e.invitees
HAVING e.invitees != COUNT(i.id)
ORDER BY ABS(e.invitees - COUNT(i.id)) DESC;

-- ============================================================================
-- SECTION 3: DATA QUALITY CHECKS
-- ============================================================================

-- 3.1 Find NULL or empty critical fields in events
SELECT
    id,
    event_name,
    store_code,
    created_at,
    CASE
        WHEN event_name IS NULL OR event_name = '' THEN 'Missing Event Name'
        WHEN store_code IS NULL OR store_code = '' THEN 'Missing Store Code'
        WHEN created_at IS NULL THEN 'Missing Created Date'
        ELSE 'Other Issue'
    END AS issue
FROM events
WHERE event_name IS NULL OR event_name = ''
   OR store_code IS NULL OR store_code = ''
   OR created_at IS NULL;

-- 3.2 Find duplicate phone numbers in attendees
SELECT
    phone,
    COUNT(*) AS occurrence_count,
    GROUP_CONCAT(DISTINCT name SEPARATOR ', ') AS names
FROM attendees
WHERE phone IS NOT NULL AND phone != ''
GROUP BY phone
HAVING COUNT(*) > 10
ORDER BY occurrence_count DESC
LIMIT 50;

-- 3.3 Check for invalid phone numbers
SELECT
    id,
    name,
    phone,
    'Invalid Phone Format' AS issue
FROM attendees
WHERE phone IS NOT NULL
  AND phone != ''
  AND (LENGTH(phone) < 10 OR LENGTH(phone) > 15)
LIMIT 100;

-- 3.4 Find events with negative or unrealistic sales figures
SELECT
    id,
    event_name,
    sale,
    advance,
    CASE
        WHEN sale < 0 THEN 'Negative Sale'
        WHEN advance < 0 THEN 'Negative Advance'
        WHEN advance > sale THEN 'Advance > Sale'
        WHEN sale > 10000000 THEN 'Unrealistically High Sale'
        ELSE 'Other Issue'
    END AS issue
FROM events
WHERE sale < 0
   OR advance < 0
   OR advance > sale
   OR sale > 10000000;

-- 3.5 Check for greetings without videos
SELECT
    id,
    unique_id,
    greeting_text,
    uploaded,
    created_at,
    DATEDIFF(NOW(), created_at) AS days_old
FROM greetings
WHERE uploaded = 0
   OR uploaded IS NULL
   OR drive_file_id IS NULL
   OR drive_file_id = ''
ORDER BY created_at DESC
LIMIT 100;

-- ============================================================================
-- SECTION 4: INDEX ANALYSIS
-- ============================================================================

-- 4.1 Show all indexes on each table
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX SEPARATOR ', ') AS columns,
    INDEX_TYPE,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('events', 'attendees', 'invitees', 'stores', 'greetings')
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY TABLE_NAME, INDEX_NAME;

-- 4.2 Find tables without indexes (except PRIMARY)
SELECT DISTINCT
    TABLE_NAME
FROM information_schema.TABLES t
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_TYPE = 'BASE TABLE'
  AND NOT EXISTS (
    SELECT 1
    FROM information_schema.STATISTICS s
    WHERE s.TABLE_SCHEMA = t.TABLE_SCHEMA
      AND s.TABLE_NAME = t.TABLE_NAME
      AND s.INDEX_NAME != 'PRIMARY'
  )
ORDER BY TABLE_NAME;

-- 4.3 Check for missing foreign key indexes
SELECT
    kcu.TABLE_NAME,
    kcu.COLUMN_NAME,
    kcu.REFERENCED_TABLE_NAME,
    kcu.REFERENCED_COLUMN_NAME,
    CASE
        WHEN s.INDEX_NAME IS NULL THEN 'MISSING INDEX'
        ELSE 'Index Exists'
    END AS index_status
FROM information_schema.KEY_COLUMN_USAGE kcu
LEFT JOIN information_schema.STATISTICS s
    ON kcu.TABLE_SCHEMA = s.TABLE_SCHEMA
    AND kcu.TABLE_NAME = s.TABLE_NAME
    AND kcu.COLUMN_NAME = s.COLUMN_NAME
WHERE kcu.TABLE_SCHEMA = DATABASE()
  AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY kcu.TABLE_NAME;

-- ============================================================================
-- SECTION 5: SECURITY AUDIT
-- ============================================================================

-- 5.1 Check for plain text passwords (passwords not starting with hash prefix)
SELECT
    'abm_login' AS table_name,
    COUNT(*) AS plain_text_count
FROM abm_login
WHERE password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%'
UNION ALL
SELECT
    'rbm_login',
    COUNT(*)
FROM rbm_login
WHERE password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%'
UNION ALL
SELECT
    'cee_login',
    COUNT(*)
FROM cee_login
WHERE password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%';

-- 5.2 List all admin/privileged accounts
SELECT
    abm_user_id AS user_id,
    abm_name AS name,
    email,
    region,
    created_at,
    'ABM' AS role
FROM abm_login
UNION ALL
SELECT
    rbm_user_id,
    rbm_name,
    email,
    NULL AS region,
    created_at,
    'RBM' AS role
FROM rbm_login
UNION ALL
SELECT
    cee_user_id,
    cee_name,
    email,
    region,
    created_at,
    'CEE' AS role
FROM cee_login
ORDER BY created_at DESC;

-- 5.3 Check for duplicate usernames across all login tables
SELECT username, user_type, COUNT(*) as count
FROM (
    SELECT abm_user_id as username, 'ABM' as user_type FROM abm_login
    UNION ALL
    SELECT rbm_user_id, 'RBM' FROM rbm_login
    UNION ALL
    SELECT cee_user_id, 'CEE' FROM cee_login
) all_users
GROUP BY username
HAVING COUNT(*) > 1;

-- ============================================================================
-- SECTION 6: PERFORMANCE ANALYSIS
-- ============================================================================

-- 6.1 Find tables with high row counts but no indexes
SELECT
    t.TABLE_NAME,
    t.TABLE_ROWS,
    COUNT(DISTINCT s.INDEX_NAME) AS index_count
FROM information_schema.TABLES t
LEFT JOIN information_schema.STATISTICS s
    ON t.TABLE_SCHEMA = s.TABLE_SCHEMA
    AND t.TABLE_NAME = s.TABLE_NAME
    AND s.INDEX_NAME != 'PRIMARY'
WHERE t.TABLE_SCHEMA = DATABASE()
  AND t.TABLE_ROWS > 1000
GROUP BY t.TABLE_NAME, t.TABLE_ROWS
HAVING COUNT(DISTINCT s.INDEX_NAME) < 2
ORDER BY t.TABLE_ROWS DESC;

-- 6.2 Analyze events table growth over time
SELECT
    DATE_FORMAT(created_at, '%Y-%m') AS month,
    COUNT(*) AS events_count,
    COUNT(DISTINCT store_code) AS stores_involved,
    SUM(attendees) AS total_attendees,
    ROUND(AVG(sale), 2) AS avg_sale
FROM events
WHERE created_at IS NOT NULL
GROUP BY DATE_FORMAT(created_at, '%Y-%m')
ORDER BY month DESC
LIMIT 12;

-- 6.3 Find slowest growing tables (candidates for archival)
SELECT
    TABLE_NAME,
    TABLE_ROWS,
    UPDATE_TIME,
    DATEDIFF(NOW(), UPDATE_TIME) AS days_since_update
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND UPDATE_TIME IS NOT NULL
  AND TABLE_ROWS > 0
ORDER BY days_since_update DESC;

-- ============================================================================
-- SECTION 7: STORE & REGION ANALYSIS
-- ============================================================================

-- 7.1 Store activity summary
SELECT
    s.store_code,
    s.store_name,
    s.region,
    COUNT(e.id) AS total_events,
    COALESCE(SUM(e.attendees), 0) AS total_attendees,
    COALESCE(SUM(e.sale), 0) AS total_sales,
    MAX(e.created_at) AS last_event_date
FROM stores s
LEFT JOIN events e ON s.store_code = e.store_code
GROUP BY s.store_code, s.store_name, s.region
ORDER BY total_events DESC;

-- 7.2 Inactive stores (no events in last 90 days)
SELECT
    s.store_code,
    s.store_name,
    s.region,
    MAX(e.created_at) AS last_event_date,
    DATEDIFF(NOW(), MAX(e.created_at)) AS days_inactive
FROM stores s
LEFT JOIN events e ON s.store_code = e.store_code
GROUP BY s.store_code, s.store_name, s.region
HAVING MAX(e.created_at) IS NULL
    OR DATEDIFF(NOW(), MAX(e.created_at)) > 90
ORDER BY days_inactive DESC;

-- 7.3 Region-wise performance
SELECT
    s.region,
    COUNT(DISTINCT s.store_code) AS store_count,
    COUNT(e.id) AS total_events,
    SUM(e.attendees) AS total_attendees,
    ROUND(SUM(e.sale), 2) AS total_sales,
    ROUND(AVG(e.sale), 2) AS avg_sale_per_event
FROM stores s
LEFT JOIN events e ON s.store_code = e.store_code
WHERE s.region IS NOT NULL
GROUP BY s.region
ORDER BY total_sales DESC;

-- ============================================================================
-- SECTION 8: GREETING MODULE ANALYSIS
-- ============================================================================

-- 8.1 Greeting upload status
SELECT
    CASE
        WHEN uploaded = 1 THEN 'Uploaded'
        WHEN uploaded = 0 OR uploaded IS NULL THEN 'Pending'
        ELSE 'Unknown'
    END AS status,
    COUNT(*) AS count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM greetings), 2) AS percentage
FROM greetings
GROUP BY
    CASE
        WHEN uploaded = 1 THEN 'Uploaded'
        WHEN uploaded = 0 OR uploaded IS NULL THEN 'Pending'
        ELSE 'Unknown'
    END;

-- 8.2 Greetings created per day (last 30 days)
SELECT
    DATE(created_at) AS date,
    COUNT(*) AS greetings_created,
    SUM(CASE WHEN uploaded = 1 THEN 1 ELSE 0 END) AS uploaded,
    SUM(CASE WHEN uploaded = 0 OR uploaded IS NULL THEN 1 ELSE 0 END) AS pending
FROM greetings
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(created_at)
ORDER BY date DESC;

-- 8.3 Find duplicate unique_ids (should not exist)
SELECT
    unique_id,
    COUNT(*) AS occurrence
FROM greetings
WHERE unique_id IS NOT NULL
GROUP BY unique_id
HAVING COUNT(*) > 1;

-- ============================================================================
-- SECTION 9: COLUMN DATA TYPE ANALYSIS
-- ============================================================================

-- 9.1 Find all VARCHAR columns that might be dates
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND DATA_TYPE = 'varchar'
  AND (COLUMN_NAME LIKE '%date%'
       OR COLUMN_NAME LIKE '%time%'
       OR COLUMN_NAME LIKE '%created%'
       OR COLUMN_NAME LIKE '%updated%')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 9.2 Find all columns without NOT NULL constraint that probably should have it
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_KEY
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND IS_NULLABLE = 'YES'
  AND COLUMN_KEY IN ('PRI', 'MUL')  -- Primary or Foreign keys
ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- ============================================================================
-- SECTION 10: BACKUP TABLE ANALYSIS
-- ============================================================================

-- 10.1 Compare core table with backup tables (example for events)
-- NOTE: Adjust table names based on your actual backup tables
SELECT
    'events' AS table_name,
    COUNT(*) AS record_count,
    MIN(created_at) AS oldest_record,
    MAX(created_at) AS newest_record
FROM events
UNION ALL
SELECT
    'events_backup',
    COUNT(*),
    MIN(created_at),
    MAX(created_at)
FROM events_backup
WHERE EXISTS (SELECT 1 FROM information_schema.TABLES
              WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'events_backup');

-- 10.2 Find records in main table not in backup
-- NOTE: Run this only if you have events_backup table
-- SELECT e.id, e.event_name, e.created_at
-- FROM events e
-- LEFT JOIN events_backup eb ON e.id = eb.id
-- WHERE eb.id IS NULL
-- LIMIT 100;

-- ============================================================================
-- SECTION 11: RECOMMENDATIONS SUMMARY
-- ============================================================================

-- 11.1 Generate fix recommendations
SELECT 'RECOMMENDATION' AS type, 'Add indexes on foreign key columns' AS recommendation, 'HIGH' AS priority
UNION ALL SELECT 'RECOMMENDATION', 'Implement password hashing (BCrypt)', 'CRITICAL'
UNION ALL SELECT 'RECOMMENDATION', 'Add NOT NULL constraints on critical columns', 'HIGH'
UNION ALL SELECT 'RECOMMENDATION', 'Convert date VARCHAR columns to DATE/DATETIME', 'MEDIUM'
UNION ALL SELECT 'RECOMMENDATION', 'Add audit columns (created_by, updated_by, updated_at)', 'MEDIUM'
UNION ALL SELECT 'RECOMMENDATION', 'Implement soft delete strategy', 'MEDIUM'
UNION ALL SELECT 'RECOMMENDATION', 'Add CHECK constraints for numeric validations', 'LOW'
UNION ALL SELECT 'RECOMMENDATION', 'Review and optimize large LONGTEXT columns', 'LOW'
UNION ALL SELECT 'RECOMMENDATION', 'Implement data archival strategy for old events', 'MEDIUM'
UNION ALL SELECT 'RECOMMENDATION', 'Add composite indexes for common query patterns', 'HIGH';

-- ============================================================================
-- END OF DIAGNOSTIC QUERIES
-- ============================================================================

-- USAGE INSTRUCTIONS:
-- 1. Run each section independently
-- 2. Review results and identify issues
-- 3. Refer to DATABASE_COMPLETE_ANALYSIS.md for detailed explanations
-- 4. Execute fix scripts from DATABASE_FIX_SCRIPTS.sql
-- 5. Re-run diagnostics to verify fixes

-- SAFETY NOTE:
-- These are READ-ONLY queries (SELECT only)
-- Safe to run on production database
-- No data will be modified

