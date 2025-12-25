-- ============================================
-- QR ATTENDEE FIX VERIFICATION SCRIPT
-- ============================================
-- Run this script to verify the database schema is correct
-- after the MySQL migration fixes

USE selfie_preprod;

-- ============================================
-- 1. CHECK EVENTS TABLE STRUCTURE
-- ============================================
SHOW CREATE TABLE events\G

-- Expected: id VARCHAR(255) NOT NULL PRIMARY KEY

-- ============================================
-- 2. CHECK ATTENDEES TABLE STRUCTURE
-- ============================================
SHOW CREATE TABLE attendees\G

-- Expected:
--   event_id VARCHAR(255) NOT NULL
--   FOREIGN KEY (event_id) REFERENCES events(id)

-- ============================================
-- 3. CHECK INVITEES TABLE STRUCTURE
-- ============================================
SHOW CREATE TABLE invitees\G

-- Expected:
--   event_id VARCHAR(255) NOT NULL
--   FOREIGN KEY (event_id) REFERENCES events(id)

-- ============================================
-- 4. VERIFY FOREIGN KEY CONSTRAINTS
-- ============================================
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    information_schema.KEY_COLUMN_USAGE
WHERE
    TABLE_SCHEMA = 'selfie_preprod'
    AND TABLE_NAME IN ('attendees', 'invitees')
    AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Expected output:
-- attendees | event_id | FK_xxx | events | id
-- invitees  | event_id | FK_xxx | events | id

-- ============================================
-- 5. CHECK COLUMN TYPES MATCH
-- ============================================
-- Check events.id column type
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM
    information_schema.COLUMNS
WHERE
    TABLE_SCHEMA = 'selfie_preprod'
    AND TABLE_NAME = 'events'
    AND COLUMN_NAME = 'id';

-- Check attendees.event_id column type
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM
    information_schema.COLUMNS
WHERE
    TABLE_SCHEMA = 'selfie_preprod'
    AND TABLE_NAME = 'attendees'
    AND COLUMN_NAME = 'event_id';

-- Check invitees.event_id column type
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM
    information_schema.COLUMNS
WHERE
    TABLE_SCHEMA = 'selfie_preprod'
    AND TABLE_NAME = 'invitees'
    AND COLUMN_NAME = 'event_id';

-- ✅ All three should show:
--    DATA_TYPE: varchar
--    CHARACTER_MAXIMUM_LENGTH: 255
--    IS_NULLABLE: NO

-- ============================================
-- 6. TEST SAMPLE DATA INSERTION
-- ============================================
-- Create a test event
INSERT INTO events (
    id,
    store_code,
    event_name,
    event_type,
    rso,
    start_date,
    invitees,
    attendees,
    created_at,
    attendees_uploaded
) VALUES (
    'TEST001_verification_test',
    'STORE001',
    'QR Fix Test Event',
    'Test',
    'Test RSO',
    '2025-12-18',
    0,
    0,
    NOW(),
    false
);

-- Insert test attendee (THIS SHOULD WORK NOW!)
INSERT INTO attendees (
    event_id,
    name,
    phone,
    `like`,
    first_time_at_tanishq,
    created_at,
    is_uploaded_from_excel,
    rso_name
) VALUES (
    'TEST001_verification_test',
    'Test User',
    '9999999999',
    'Gold Jewelry',
    true,
    NOW(),
    false,
    'Test RSO'
);

-- Verify the insert worked
SELECT
    a.id,
    a.event_id,
    a.name,
    a.phone,
    e.event_name,
    e.attendees
FROM
    attendees a
    INNER JOIN events e ON a.event_id = e.id
WHERE
    a.event_id = 'TEST001_verification_test';

-- ✅ Should return 1 row showing the attendee linked to the event

-- Update event attendee count (simulating what the app does)
UPDATE events
SET attendees = 1
WHERE id = 'TEST001_verification_test';

-- Verify update
SELECT id, event_name, attendees
FROM events
WHERE id = 'TEST001_verification_test';

-- ✅ Should show attendees = 1

-- ============================================
-- 7. CLEANUP TEST DATA
-- ============================================
DELETE FROM attendees WHERE event_id = 'TEST001_verification_test';
DELETE FROM events WHERE id = 'TEST001_verification_test';

-- ============================================
-- 8. CHECK EXISTING DATA INTEGRITY
-- ============================================
-- Count events
SELECT COUNT(*) as total_events FROM events;

-- Count attendees
SELECT COUNT(*) as total_attendees FROM attendees;

-- Count invitees
SELECT COUNT(*) as total_invitees FROM invitees;

-- Check for any orphaned attendees (attendees without valid event)
SELECT
    a.id,
    a.event_id,
    a.name
FROM
    attendees a
    LEFT JOIN events e ON a.event_id = e.id
WHERE
    e.id IS NULL;

-- ✅ Should return 0 rows (no orphaned attendees)

-- Check for any orphaned invitees
SELECT
    i.id,
    i.event_id,
    i.name
FROM
    invitees i
    LEFT JOIN events e ON i.event_id = e.id
WHERE
    e.id IS NULL;

-- ✅ Should return 0 rows (no orphaned invitees)

-- ============================================
-- 9. SUMMARY REPORT
-- ============================================
SELECT
    'Events' as table_name,
    COUNT(*) as row_count,
    SUM(attendees) as total_attendees_sum
FROM events

UNION ALL

SELECT
    'Attendees' as table_name,
    COUNT(*) as row_count,
    NULL as total_attendees_sum
FROM attendees

UNION ALL

SELECT
    'Invitees' as table_name,
    COUNT(*) as row_count,
    NULL as total_attendees_sum
FROM invitees;

-- ============================================
-- 10. EVENT-ATTENDEE RELATIONSHIP VERIFICATION
-- ============================================
SELECT
    e.id,
    e.event_name,
    e.attendees as recorded_count,
    COUNT(a.id) as actual_count,
    CASE
        WHEN e.attendees = COUNT(a.id) THEN '✅ Match'
        ELSE '❌ Mismatch'
    END as status
FROM
    events e
    LEFT JOIN attendees a ON e.id = a.event_id
GROUP BY
    e.id, e.event_name, e.attendees
HAVING
    e.attendees != COUNT(a.id)
ORDER BY
    e.created_at DESC;

-- ✅ Should return 0 rows (all counts should match)
-- If rows are returned, those events have incorrect attendee counts

-- ============================================
-- VERIFICATION COMPLETE!
-- ============================================
-- If all queries executed successfully:
-- ✅ Foreign keys are properly configured
-- ✅ Data types match correctly
-- ✅ Test insertion works
-- ✅ No data integrity issues
-- ✅ Ready for production!

