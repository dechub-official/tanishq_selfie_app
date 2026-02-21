-- =====================================================
-- SQL Script to Fix Region Data in Existing Events
-- =====================================================
-- Date: January 28, 2026
-- Purpose: Update null/empty region values in events table
--          by copying from associated stores table
-- =====================================================

-- Step 1: Backup existing events table (RECOMMENDED)
-- Uncomment the line below if you want to create a backup
-- CREATE TABLE events_backup_20260128 AS SELECT * FROM events;

-- Step 2: Check how many events have missing region data
SELECT
    COUNT(*) as total_events,
    SUM(CASE WHEN region IS NULL OR region = '' THEN 1 ELSE 0 END) as events_with_null_region,
    SUM(CASE WHEN region IS NOT NULL AND region != '' THEN 1 ELSE 0 END) as events_with_region
FROM events;

-- Step 3: Preview which events will be updated
SELECT
    e.id as event_id,
    e.store_code,
    e.region as current_region,
    s.region as store_region,
    e.event_name,
    e.created_at
FROM events e
JOIN stores s ON e.store_code = s.store_code
WHERE e.region IS NULL OR e.region = ''
ORDER BY e.created_at DESC
LIMIT 20;

-- Step 4: Update events with region from stores
-- This updates all events where region is null or empty
UPDATE events e
JOIN stores s ON e.store_code = s.store_code
SET e.region = s.region
WHERE (e.region IS NULL OR e.region = '')
  AND s.region IS NOT NULL
  AND s.region != '';

-- Step 5: Verify the update
SELECT
    COUNT(*) as total_events,
    SUM(CASE WHEN region IS NULL OR region = '' THEN 1 ELSE 0 END) as events_with_null_region,
    SUM(CASE WHEN region IS NOT NULL AND region != '' THEN 1 ELSE 0 END) as events_with_region
FROM events;

-- Step 6: Check if any stores are missing region data
SELECT
    store_code,
    store_name,
    region,
    abm_username,
    rbm_username,
    cee_username
FROM stores
WHERE region IS NULL OR region = ''
ORDER BY store_code;

-- Step 7: List region distribution after update
SELECT
    region,
    COUNT(*) as event_count
FROM events
WHERE region IS NOT NULL AND region != ''
GROUP BY region
ORDER BY region;

-- =====================================================
-- If you need to manually set region for specific stores
-- =====================================================
-- Example: Update region for a specific store
-- UPDATE stores SET region = 'North1' WHERE store_code = 'STORE123';

-- Then re-run Step 4 to update associated events
-- =====================================================

