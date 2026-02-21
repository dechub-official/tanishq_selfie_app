-- ========================================
-- STEP 1: Add is_visible column to events table
-- ========================================
-- This column will control which events are shown in the dashboard
-- Default value is TRUE (1) for all new events

ALTER TABLE selfie_prod.events
ADD COLUMN is_visible BOOLEAN DEFAULT TRUE COMMENT 'Controls visibility in dashboard';

-- ========================================
-- STEP 2: Hide all events before January 2026
-- ========================================
-- Mark all events with start_date before 2026-01-01 as not visible

UPDATE selfie_prod.events
SET is_visible = FALSE
WHERE STR_TO_DATE(start_date, '%Y-%m-%d') < '2026-01-01';

-- ========================================
-- STEP 3: Verify the changes
-- ========================================
-- Check how many events are hidden vs visible

SELECT
    is_visible,
    COUNT(*) as event_count,
    MIN(start_date) as earliest_date,
    MAX(start_date) as latest_date
FROM selfie_prod.events
GROUP BY is_visible;

-- Show sample of hidden events
SELECT id, start_date, event_name, event_type, is_visible
FROM selfie_prod.events
WHERE is_visible = FALSE
ORDER BY start_date DESC
LIMIT 10;

-- Show sample of visible events
SELECT id, start_date, event_name, event_type, is_visible
FROM selfie_prod.events
WHERE is_visible = TRUE
ORDER BY start_date ASC
LIMIT 10;

-- ========================================
-- OPTIONAL: To show all events again (UNDO)
-- ========================================
-- If you want to revert and show all events, run this:
-- UPDATE selfie_prod.events SET is_visible = TRUE;

-- ========================================
-- OPTIONAL: Hide specific date range
-- ========================================
-- To hide events for a specific date range:
-- UPDATE selfie_prod.events
-- SET is_visible = FALSE
-- WHERE STR_TO_DATE(start_date, '%Y-%m-%d') BETWEEN '2025-01-01' AND '2025-12-31';

-- ========================================
-- OPTIONAL: Show only current month events
-- ========================================
-- To show only January 2026 events:
-- UPDATE selfie_prod.events SET is_visible = FALSE;
-- UPDATE selfie_prod.events
-- SET is_visible = TRUE
-- WHERE STR_TO_DATE(start_date, '%Y-%m-%d') >= '2026-01-01'
--   AND STR_TO_DATE(start_date, '%Y-%m-%d') < '2026-02-01';

