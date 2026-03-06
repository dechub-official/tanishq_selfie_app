-- ================================================================
-- QUICK FIX: Add Missing Columns to stores Table
-- ================================================================
-- Copy and paste these commands into MySQL Workbench or command line
-- These commands are safe to run - they only ADD columns if missing

-- Step 1: Select the correct database
USE selfie_preprod;

-- Step 2: Add missing columns (safe - only adds if not exists)
ALTER TABLE stores ADD COLUMN IF NOT EXISTS region VARCHAR(100);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS level VARCHAR(50);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS abm_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS rbm_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS cee_username VARCHAR(255);
ALTER TABLE stores ADD COLUMN IF NOT EXISTS corporate_username VARCHAR(255);

-- Step 3: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_abm_username ON stores(abm_username);
CREATE INDEX IF NOT EXISTS idx_rbm_username ON stores(rbm_username);
CREATE INDEX IF NOT EXISTS idx_cee_username ON stores(cee_username);
CREATE INDEX IF NOT EXISTS idx_corporate_username ON stores(corporate_username);
CREATE INDEX IF NOT EXISTS idx_region ON stores(region);

-- Step 4: Verify the fix
DESCRIBE stores;

-- Step 5: Check TEST1 store exists
SELECT * FROM stores WHERE store_code = 'TEST1';

-- If TEST1 doesn't exist, create it:
-- INSERT INTO stores (store_code, store_name, store_email_id)
-- VALUES ('TEST1', 'Test Store 1', 'test1@titan.co.in');

SELECT 'DATABASE MIGRATION COMPLETE - RESTART YOUR APPLICATION NOW' AS Status;

