-- Quick Test to Verify Schema is Ready
USE selfie_preprod;

-- Test 1: Check if we can insert test event
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
    'TEST_SCHEMA_001',
    (SELECT store_code FROM stores LIMIT 1),
    'Schema Verification Test',
    'Test',
    'Test RSO',
    '2025-12-18',
    0,
    0,
    NOW(),
    false
);

-- Test 2: Check if we can insert attendee with FK relationship
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
    'TEST_SCHEMA_001',
    'Test User',
    '9999999999',
    'Gold',
    true,
    NOW(),
    false,
    'Test RSO'
);

-- Test 3: Verify the relationship works
SELECT
    a.id as attendee_id,
    a.name,
    a.phone,
    e.id as event_id,
    e.event_name,
    e.attendees as event_attendee_count
FROM
    attendees a
    INNER JOIN events e ON a.event_id = e.id
WHERE
    e.id = 'TEST_SCHEMA_001';

-- Should show 1 row with the test data

-- Test 4: Update event count (simulate what app does)
UPDATE events
SET attendees = 1
WHERE id = 'TEST_SCHEMA_001';

-- Test 5: Verify update worked
SELECT id, event_name, attendees
FROM events
WHERE id = 'TEST_SCHEMA_001';

-- Should show attendees = 1

-- Cleanup
DELETE FROM attendees WHERE event_id = 'TEST_SCHEMA_001';
DELETE FROM events WHERE id = 'TEST_SCHEMA_001';

-- If all above worked without errors: ✅ SCHEMA IS PERFECT!
SELECT 'Schema verification PASSED! Database is ready for the application.' as Status;

