-- ============================================
-- SUPER SIMPLE SOLUTION: Hide/Show Events
-- ============================================
-- NO CODE CHANGES NEEDED!
-- Just run these SQL queries to control what shows on the dashboard

-- ============================================
-- OPTION 1: HIDE OLD EVENTS (Show only January 2026)
-- ============================================
-- This DELETES events before January 2026 from the database
-- WARNING: This is PERMANENT! Make backup first!

-- STEP 1: Create backup table first (IMPORTANT!)
CREATE TABLE IF NOT EXISTS selfie_prod.events_backup AS
SELECT * FROM selfie_prod.events;

-- STEP 2: Delete old events (before January 2026)
DELETE FROM selfie_prod.events
WHERE STR_TO_DATE(start_date, '%Y-%m-%d') < '2026-01-01';

-- STEP 3: Verify - check what's left
SELECT
    COUNT(*) as visible_events,
    MIN(start_date) as earliest_date,
    MAX(start_date) as latest_date
FROM selfie_prod.events;

-- ============================================
-- OPTION 2: RESTORE ALL EVENTS (Show Everything)
-- ============================================
-- Restore from backup to show all events again

-- STEP 1: Clear current events
TRUNCATE TABLE selfie_prod.events;

-- STEP 2: Restore from backup
INSERT INTO selfie_prod.events
SELECT * FROM selfie_prod.events_backup;

-- STEP 3: Verify - check all events are back
SELECT COUNT(*) as total_events FROM selfie_prod.events;

-- ============================================
-- BETTER OPTION: USE BACKUP TABLE METHOD
-- ============================================
-- Keep original data safe, work with a copy

-- STEP 1: Create backup (one-time only)
CREATE TABLE IF NOT EXISTS selfie_prod.events_all_data AS
SELECT * FROM selfie_prod.events;

-- STEP 2: To show only January 2026 onwards
DELETE FROM selfie_prod.events
WHERE STR_TO_DATE(start_date, '%Y-%m-%d') < '2026-01-01';

-- STEP 3: To restore all events
TRUNCATE TABLE selfie_prod.events;
INSERT INTO selfie_prod.events SELECT * FROM selfie_prod.events_all_data;

-- ============================================
-- SAFEST OPTION: Use is_visible Column (Recommended!)
-- ============================================
-- This doesn't delete anything, just marks as hidden

-- STEP 1: Add is_visible column (one-time setup)
ALTER TABLE selfie_prod.events
ADD COLUMN IF NOT EXISTS is_visible BOOLEAN DEFAULT TRUE;

-- STEP 2: Hide events before January 2026
UPDATE selfie_prod.events
SET is_visible = FALSE
WHERE STR_TO_DATE(start_date, '%Y-%m-%d') < '2026-01-01';

-- STEP 3: Show all events again
UPDATE selfie_prod.events
SET is_visible = TRUE;

-- STEP 4: Check what's visible/hidden
SELECT
    is_visible,
    COUNT(*) as count,
    MIN(start_date) as from_date,
    MAX(start_date) as to_date
FROM selfie_prod.events
GROUP BY is_visible;

-- ============================================
-- QUICK COMMANDS - Copy & Paste These!
-- ============================================

-- >> Hide old events (show only Jan 2026 onwards)
UPDATE selfie_prod.events SET is_visible = FALSE WHERE STR_TO_DATE(start_date, '%Y-%m-%d') < '2026-01-01';

-- >> Show all events
UPDATE selfie_prod.events SET is_visible = TRUE;

-- >> Hide all events from 2025
UPDATE selfie_prod.events SET is_visible = FALSE WHERE start_date LIKE '2025%';

-- >> Show only current month (January 2026)
UPDATE selfie_prod.events SET is_visible = FALSE;
UPDATE selfie_prod.events SET is_visible = TRUE WHERE start_date LIKE '2026-01%';

-- >> Check status
SELECT is_visible, COUNT(*) FROM selfie_prod.events GROUP BY is_visible;

