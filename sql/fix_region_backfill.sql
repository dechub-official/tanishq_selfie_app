-- ============================================================
-- Backfill: Populate `region` in existing events from stores
-- Run this ONCE on the database to fix events that were saved
-- before the region-population bug was fixed.
--
-- NOTE: This works for ALL store types (CEE, ABM, RBM, Store logins)
-- because region is stored on the `stores` table and all events
-- reference stores via store_code regardless of who created them.
-- ============================================================

-- Step 1: Disable safe update mode for this session only
SET SQL_SAFE_UPDATES = 0;

-- Step 2: Backfill region on all events that have empty/null region
UPDATE events e
JOIN stores s ON e.store_code = s.store_code
SET e.region = s.region
WHERE (e.region IS NULL OR e.region = '')
  AND s.region IS NOT NULL
  AND s.region != '';

-- Step 3: Re-enable safe update mode
SET SQL_SAFE_UPDATES = 1;

-- Step 4: Verify - check how many events were updated and any still missing region
SELECT
    COUNT(*) AS total_events,
    SUM(CASE WHEN e.region IS NOT NULL AND e.region != '' THEN 1 ELSE 0 END) AS events_with_region,
    SUM(CASE WHEN e.region IS NULL OR e.region = '' THEN 1 ELSE 0 END) AS events_still_missing_region
FROM events e;

-- Step 5: Show events still missing region (stores themselves may have no region set)
SELECT e.id, e.store_code, e.event_name, e.start_date, s.region AS store_region
FROM events e
JOIN stores s ON e.store_code = s.store_code
WHERE (e.region IS NULL OR e.region = '');
