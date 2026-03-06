-- Comprehensive Database Verification Script
-- Run this to check EXACTLY what's in your database

USE selfie_preprod;

-- 1. Check if stores table exists
SELECT 'Checking if stores table exists...' AS Step;
SELECT COUNT(*) AS stores_table_exists
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'selfie_preprod'
  AND TABLE_NAME = 'stores';

-- 2. Show ALL columns in stores table
SELECT 'All columns in stores table:' AS Step;
SELECT
    ORDINAL_POSITION,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'selfie_preprod'
  AND TABLE_NAME = 'stores'
ORDER BY ORDINAL_POSITION;

-- 3. Specifically check for the 6 required columns
SELECT 'Checking for required columns:' AS Step;
SELECT
    CASE
        WHEN EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'selfie_preprod' AND TABLE_NAME = 'stores' AND COLUMN_NAME = 'region')
        THEN '✓ EXISTS'
        ELSE '✗ MISSING'
    END AS region_column,
    CASE
        WHEN EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'selfie_preprod' AND TABLE_NAME = 'stores' AND COLUMN_NAME = 'level')
        THEN '✓ EXISTS'
        ELSE '✗ MISSING'
    END AS level_column,
    CASE
        WHEN EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'selfie_preprod' AND TABLE_NAME = 'stores' AND COLUMN_NAME = 'abm_username')
        THEN '✓ EXISTS'
        ELSE '✗ MISSING'
    END AS abm_username_column,
    CASE
        WHEN EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'selfie_preprod' AND TABLE_NAME = 'stores' AND COLUMN_NAME = 'rbm_username')
        THEN '✓ EXISTS'
        ELSE '✗ MISSING'
    END AS rbm_username_column,
    CASE
        WHEN EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'selfie_preprod' AND TABLE_NAME = 'stores' AND COLUMN_NAME = 'cee_username')
        THEN '✓ EXISTS'
        ELSE '✗ MISSING'
    END AS cee_username_column,
    CASE
        WHEN EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'selfie_preprod' AND TABLE_NAME = 'stores' AND COLUMN_NAME = 'corporate_username')
        THEN '✓ EXISTS'
        ELSE '✗ MISSING'
    END AS corporate_username_column;

-- 4. Check if TEST1 store exists
SELECT 'Checking if TEST1 store exists:' AS Step;
SELECT * FROM stores WHERE store_code = 'TEST1';

-- 5. Count total stores
SELECT 'Total stores in database:' AS Step;
SELECT COUNT(*) AS total_stores FROM stores;

-- 6. Check events table for TEST1
SELECT 'Events for TEST1 store:' AS Step;
SELECT COUNT(*) AS test1_events FROM events WHERE store_code = 'TEST1';

-- 7. Show sample store data
SELECT 'Sample store data:' AS Step;
SELECT
    store_code,
    store_name,
    region,
    level,
    abm_username,
    rbm_username,
    cee_username,
    corporate_username
FROM stores
LIMIT 5;

