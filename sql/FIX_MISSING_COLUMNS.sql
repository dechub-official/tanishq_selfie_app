-- ============================================
-- FIX: Add Missing Columns to Stores Table
-- ============================================
-- This script adds the missing regional management columns to the stores table
-- Run this on your preprod database to fix the SQLGrammarException error

-- Change to your database
USE selfie_prod;  -- Or use: tanishq (depending on your database name)

-- ============================================
-- 1. Add Missing Columns (if they don't exist)
-- ============================================

-- Check current structure first (optional)
-- DESCRIBE stores;

-- Add region column if missing
ALTER TABLE stores
ADD COLUMN IF NOT EXISTS region VARCHAR(100)
COMMENT 'Region/Zone (e.g., North1, South2, East1, West1)'
AFTER is_collection;

-- Add level column if missing
ALTER TABLE stores
ADD COLUMN IF NOT EXISTS level VARCHAR(50)
COMMENT 'Store tier/level'
AFTER region;

-- Add abm_username column if missing
ALTER TABLE stores
ADD COLUMN IF NOT EXISTS abm_username VARCHAR(255)
COMMENT 'Mapped ABM (Area Business Manager) username'
AFTER level;

-- Add rbm_username column if missing
ALTER TABLE stores
ADD COLUMN IF NOT EXISTS rbm_username VARCHAR(255)
COMMENT 'Mapped RBM (Regional Business Manager) username'
AFTER abm_username;

-- Add cee_username column if missing
ALTER TABLE stores
ADD COLUMN IF NOT EXISTS cee_username VARCHAR(255)
COMMENT 'Mapped CEE (Customer Experience Executive) username'
AFTER rbm_username;

-- Add corporate_username column if missing
ALTER TABLE stores
ADD COLUMN IF NOT EXISTS corporate_username VARCHAR(255)
COMMENT 'Mapped Corporate username - highest level access'
AFTER cee_username;

-- ============================================
-- 2. Create Indexes for Performance
-- ============================================

-- Drop old indexes if they exist (to recreate them)
-- Note: MySQL doesn't have DROP INDEX IF EXISTS in all versions
-- So we use a safer approach

-- Create indexes (IF NOT EXISTS works in MySQL 5.7+)
CREATE INDEX IF NOT EXISTS idx_region ON stores(region);
CREATE INDEX IF NOT EXISTS idx_abm_username ON stores(abm_username);
CREATE INDEX IF NOT EXISTS idx_rbm_username ON stores(rbm_username);
CREATE INDEX IF NOT EXISTS idx_cee_username ON stores(cee_username);
CREATE INDEX IF NOT EXISTS idx_corporate_username ON stores(corporate_username);

-- ============================================
-- 3. Verification
-- ============================================

-- Show updated table structure
SELECT 'Updated table structure:' AS Info;
DESCRIBE stores;

-- Show column details for the new columns
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'stores'
AND COLUMN_NAME IN ('region', 'level', 'abm_username', 'rbm_username', 'cee_username', 'corporate_username')
ORDER BY ORDINAL_POSITION;

-- Show indexes on stores table
SELECT
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'stores'
AND INDEX_NAME IN ('idx_region', 'idx_abm_username', 'idx_rbm_username', 'idx_cee_username', 'idx_corporate_username')
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- Count stores with mappings
SELECT
    COUNT(*) as total_stores,
    COUNT(DISTINCT region) as unique_regions,
    COUNT(DISTINCT abm_username) as unique_abms,
    COUNT(DISTINCT rbm_username) as unique_rbms,
    COUNT(DISTINCT cee_username) as unique_cees,
    COUNT(DISTINCT corporate_username) as unique_corporate
FROM stores;

SELECT '=== FIX APPLIED SUCCESSFULLY ===' AS Status;
SELECT 'Next Steps: Run the login creation scripts (02_create_abm_logins.sql, 03_rbm_logins.sql, 04_cee_logins.sql, 06_create_corporate_login.sql)' AS Next_Steps;
SELECT 'Then run 05_map_stores.sql to map stores to users' AS Next_Steps_2;

