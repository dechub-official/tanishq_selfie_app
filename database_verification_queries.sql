-- =====================================================
-- EVENTS CONTROLLER - DATABASE VERIFICATION QUERIES
-- Run these after each test to verify functionality
-- =====================================================

-- =====================================================
-- 1. CHECK LATEST EVENT CREATED
-- =====================================================
SELECT
    id AS event_id,
    event_name,
    event_type,
    rso,
    start_date,
    invitees AS invitee_count,
    attendees AS attendee_count,
    sale,
    advance,
    ghs_or_rga,
    gmb,
    diamond_awareness,
    ghs_flag,
    completed_events_drive_link AS s3_url,
    created_at,
    store_code
FROM events
ORDER BY created_at DESC
LIMIT 5;

-- =====================================================
-- 2. CHECK INVITEES FOR AN EVENT
-- =====================================================
-- Replace '<EVENT_ID>' with your actual event ID
SELECT
    id,
    name,
    contact,
    created_at
FROM invitees
WHERE event_id = '<EVENT_ID>'
ORDER BY id;

-- Count invitees
SELECT COUNT(*) AS total_invitees
FROM invitees
WHERE event_id = '<EVENT_ID>';

-- =====================================================
-- 3. CHECK ATTENDEES FOR AN EVENT
-- =====================================================
-- Replace '<EVENT_ID>' with your actual event ID
SELECT
    id,
    name,
    phone,
    `like` AS liked_product,
    first_time_at_tanishq,
    is_uploaded_from_excel,
    rso_name,
    created_at
FROM attendees
WHERE event_id = '<EVENT_ID>'
ORDER BY created_at DESC;

-- Count attendees
SELECT COUNT(*) AS total_attendees
FROM attendees
WHERE event_id = '<EVENT_ID>';

-- Count by upload method
SELECT
    is_uploaded_from_excel,
    COUNT(*) AS count
FROM attendees
WHERE event_id = '<EVENT_ID>'
GROUP BY is_uploaded_from_excel;

-- =====================================================
-- 4. VERIFY REAL-TIME COUNTS MATCH
-- =====================================================
-- This should show if counts in events table match actual records
SELECT
    e.id AS event_id,
    e.event_name,
    e.invitees AS invitees_in_event_table,
    COUNT(DISTINCT i.id) AS actual_invitees_count,
    e.attendees AS attendees_in_event_table,
    COUNT(DISTINCT a.id) AS actual_attendees_count,
    CASE
        WHEN e.invitees = COUNT(DISTINCT i.id) THEN '✅ MATCH'
        ELSE '❌ MISMATCH'
    END AS invitees_match,
    CASE
        WHEN e.attendees = COUNT(DISTINCT a.id) THEN '✅ MATCH'
        ELSE '❌ MISMATCH'
    END AS attendees_match
FROM events e
LEFT JOIN invitees i ON e.id = i.event_id
LEFT JOIN attendees a ON e.id = a.event_id
WHERE e.id = '<EVENT_ID>'
GROUP BY e.id, e.event_name, e.invitees, e.attendees;

-- =====================================================
-- 5. CHECK ALL EVENTS FOR A STORE
-- =====================================================
-- Replace 'STORE001' with your store code
SELECT
    id AS event_id,
    event_name,
    event_type,
    start_date,
    invitees,
    attendees,
    sale,
    advance,
    CASE
        WHEN completed_events_drive_link IS NOT NULL THEN '✅ Has Photos'
        ELSE '❌ No Photos'
    END AS photos_status,
    created_at
FROM events
WHERE store_code = 'STORE001'
ORDER BY created_at DESC;

-- =====================================================
-- 6. CHECK STORES TABLE
-- =====================================================
SELECT
    store_code,
    store_name,
    store_city,
    store_state,
    region,
    rbm_username,
    abm_username,
    cee_username
FROM stores
WHERE store_code = 'STORE001';

-- =====================================================
-- 7. CHECK USER LOGIN
-- =====================================================
-- Replace 'STORE001' with your username
SELECT
    username,
    password,
    created_at
FROM users
WHERE username = 'STORE001';

-- =====================================================
-- 8. CHECK ALL EVENTS BY REGION
-- =====================================================
-- Get all events for stores in a region
SELECT
    e.id AS event_id,
    e.event_name,
    s.store_code,
    s.store_name,
    s.region,
    e.invitees,
    e.attendees,
    e.sale,
    e.created_at
FROM events e
JOIN stores s ON e.store_code = s.store_code
WHERE s.region = 'South'
ORDER BY e.created_at DESC;

-- =====================================================
-- 9. CHECK S3 UPLOAD STATUS
-- =====================================================
-- Events that have photos uploaded to S3
SELECT
    id AS event_id,
    event_name,
    store_code,
    completed_events_drive_link AS s3_url,
    attendees,
    created_at
FROM events
WHERE completed_events_drive_link IS NOT NULL
ORDER BY created_at DESC;

-- Events without photos
SELECT
    id AS event_id,
    event_name,
    store_code,
    attendees,
    created_at
FROM events
WHERE completed_events_drive_link IS NULL
ORDER BY created_at DESC;

-- =====================================================
-- 10. STATISTICS & SUMMARY
-- =====================================================

-- Total events by store
SELECT
    store_code,
    COUNT(*) AS total_events,
    SUM(invitees) AS total_invitees,
    SUM(attendees) AS total_attendees,
    SUM(sale) AS total_sales,
    SUM(advance) AS total_advances
FROM events
GROUP BY store_code
ORDER BY total_events DESC;

-- Events by type
SELECT
    event_type,
    COUNT(*) AS count,
    SUM(attendees) AS total_attendees
FROM events
GROUP BY event_type
ORDER BY count DESC;

-- Events by date
SELECT
    DATE(created_at) AS event_date,
    COUNT(*) AS events_created
FROM events
GROUP BY DATE(created_at)
ORDER BY event_date DESC;

-- =====================================================
-- 11. FIND EVENTS WITH DATA ISSUES (Quality Check)
-- =====================================================

-- Events with 0 attendees (may need attention)
SELECT
    id AS event_id,
    event_name,
    store_code,
    start_date,
    invitees,
    attendees,
    DATEDIFF(CURRENT_DATE, DATE(start_date)) AS days_since_event
FROM events
WHERE attendees = 0
AND DATE(start_date) < CURRENT_DATE
ORDER BY start_date DESC;

-- Events with attendees but no sale recorded
SELECT
    id AS event_id,
    event_name,
    store_code,
    attendees,
    sale,
    advance
FROM events
WHERE attendees > 0
AND (sale IS NULL OR sale = 0)
ORDER BY created_at DESC;

-- Events with invitees but no attendees yet
SELECT
    id AS event_id,
    event_name,
    store_code,
    invitees,
    attendees,
    start_date
FROM events
WHERE invitees > 0
AND attendees = 0
ORDER BY created_at DESC;

-- =====================================================
-- 12. PERFORMANCE CHECK
-- =====================================================

-- Count all records
SELECT
    'events' AS table_name,
    COUNT(*) AS record_count
FROM events
UNION ALL
SELECT
    'invitees' AS table_name,
    COUNT(*) AS record_count
FROM invitees
UNION ALL
SELECT
    'attendees' AS table_name,
    COUNT(*) AS record_count
FROM attendees
UNION ALL
SELECT
    'stores' AS table_name,
    COUNT(*) AS record_count
FROM stores
UNION ALL
SELECT
    'users' AS table_name,
    COUNT(*) AS record_count
FROM users;

-- =====================================================
-- 13. TEST DATA CLEANUP (Use with caution!)
-- =====================================================

-- DELETE TEST EVENT AND RELATED DATA
-- ⚠️ WARNING: This will permanently delete data!
-- Uncomment and replace <EVENT_ID> to use

-- DELETE FROM attendees WHERE event_id = '<EVENT_ID>';
-- DELETE FROM invitees WHERE event_id = '<EVENT_ID>';
-- DELETE FROM events WHERE id = '<EVENT_ID>';

-- =====================================================
-- 14. REGIONAL MANAGER QUERIES
-- =====================================================

-- Get all stores under RBM
SELECT
    store_code,
    store_name,
    store_city,
    rbm_username
FROM stores
WHERE rbm_username = 'SOUTH1'
ORDER BY store_code;

-- Get all events for RBM's stores
SELECT
    e.id AS event_id,
    e.event_name,
    s.store_code,
    s.store_name,
    e.attendees,
    e.sale,
    e.created_at
FROM events e
JOIN stores s ON e.store_code = s.store_code
WHERE s.rbm_username = 'SOUTH1'
ORDER BY e.created_at DESC;

-- =====================================================
-- 15. QUICK VERIFICATION AFTER EACH TEST
-- =====================================================

-- Run this after EVERY test to verify changes
-- Replace <EVENT_ID> with your test event ID

SELECT
    'EVENT INFO' AS section,
    id,
    event_name,
    invitees,
    attendees,
    sale,
    advance,
    ghs_or_rga,
    gmb,
    completed_events_drive_link
FROM events
WHERE id = '<EVENT_ID>'

UNION ALL

SELECT
    'INVITEES COUNT' AS section,
    '<EVENT_ID>' AS id,
    NULL AS event_name,
    COUNT(*) AS invitees,
    NULL AS attendees,
    NULL AS sale,
    NULL AS advance,
    NULL AS ghs_or_rga,
    NULL AS gmb,
    NULL AS completed_events_drive_link
FROM invitees
WHERE event_id = '<EVENT_ID>'

UNION ALL

SELECT
    'ATTENDEES COUNT' AS section,
    '<EVENT_ID>' AS id,
    NULL AS event_name,
    NULL AS invitees,
    COUNT(*) AS attendees,
    NULL AS sale,
    NULL AS advance,
    NULL AS ghs_or_rga,
    NULL AS gmb,
    NULL AS completed_events_drive_link
FROM attendees
WHERE event_id = '<EVENT_ID>';

-- =====================================================
-- END OF VERIFICATION QUERIES
-- =====================================================

-- USAGE INSTRUCTIONS:
-- 1. Replace '<EVENT_ID>' with your actual event ID (e.g., 'STORE001_abc-123-def-456')
-- 2. Replace 'STORE001' with your actual store code
-- 3. Run queries after each API test
-- 4. Compare results with API responses
-- 5. Verify counts match between events table and related tables
-- =====================================================

