-- View events in a clean table format
-- Use this with: mysql -u jewdev -p -h 10-160-128-94 applications_preprod < view-events-table.sql

-- Set better display options
SET @row_number = 0;

-- Show events with better formatting
SELECT
    event_id,
    SUBSTRING(event_name, 1, 30) as event_name,
    event_type,
    event_date,
    location,
    max_attendees,
    organizer_id
FROM events
ORDER BY event_date DESC
LIMIT 20;

