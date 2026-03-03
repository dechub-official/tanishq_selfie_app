-- ============================================
-- Corporate Login - Top-level Access Layer
-- ============================================
-- This script creates corporate login table and updates stores table
-- Corporate users can access all stores under their management (above CEE level)

-- ✅ Database: selfie_prod (Production Ready)
USE selfie_prod;

-- ============================================
-- 1. Create Corporate Login Table
-- ============================================
CREATE TABLE IF NOT EXISTS corporate_login (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    corporate_user_id VARCHAR(255) UNIQUE NOT NULL COMMENT 'Unique username for Corporate user',
    corporate_name VARCHAR(255) COMMENT 'Full name of Corporate user',
    password VARCHAR(255) NOT NULL COMMENT 'Password (plain text - consider hashing!)',
    email VARCHAR(255) COMMENT 'Email address',
    region VARCHAR(255) COMMENT 'Region managed by Corporate user (e.g., All, North, South, East, West)',
    organization VARCHAR(255) COMMENT 'Organization/Division name',
    access_level VARCHAR(50) DEFAULT 'CORPORATE' COMMENT 'Access level (CORPORATE, REGIONAL_HEAD, etc.)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_corporate_user_id (corporate_user_id),
    INDEX idx_region (region),
    INDEX idx_access_level (access_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Stores Corporate-level login credentials - top hierarchy for client access';

-- ============================================
-- 2. Add Corporate Username Column to Stores
-- ============================================
ALTER TABLE stores
ADD COLUMN IF NOT EXISTS corporate_username VARCHAR(255)
COMMENT 'Mapped Corporate username - highest level access'
AFTER cee_username;

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_corporate_username ON stores(corporate_username);

-- ============================================
-- 3. Insert Sample Corporate Logins
-- ============================================
-- National/All India Corporate Access
INSERT INTO corporate_login (corporate_user_id, corporate_name, password, email, region, organization, access_level)
VALUES
('CORP-NATIONAL-01', 'National Head - Events', 'Tanishq@Corp2026', 'national.head@tanishq.com', 'ALL', 'Tanishq Events Division', 'CORPORATE'),
('CORP-TITAN-01', 'Titan Corporate', 'Titan@Corp2026', 'corporate@titan.co.in', 'ALL', 'Titan Company', 'CORPORATE');

-- Regional Corporate Access
INSERT INTO corporate_login (corporate_user_id, corporate_name, password, email, region, organization, access_level)
VALUES
('CORP-NORTH', 'North Regional Head', 'Tanishq@Corp2026', 'north.corp@tanishq.com', 'NORTH', 'Tanishq North Division', 'REGIONAL_HEAD'),
('CORP-SOUTH', 'South Regional Head', 'Tanishq@Corp2026', 'south.corp@tanishq.com', 'SOUTH', 'Tanishq South Division', 'REGIONAL_HEAD'),
('CORP-EAST', 'East Regional Head', 'Tanishq@Corp2026', 'east.corp@tanishq.com', 'EAST', 'Tanishq East Division', 'REGIONAL_HEAD'),
('CORP-WEST', 'West Regional Head', 'Tanishq@Corp2026', 'west.corp@tanishq.com', 'WEST', 'Tanishq West Division', 'REGIONAL_HEAD');

-- ============================================
-- 4. Map All Stores to Corporate Users
-- ============================================
-- National corporate users can see ALL stores
-- Regional corporate users see stores in their region

-- Map all stores to national corporate users
UPDATE stores
SET corporate_username = 'CORP-NATIONAL-01'
WHERE 1=1;

-- Additionally map regional stores to regional corporate heads
-- North region stores
UPDATE stores
SET corporate_username = 'CORP-NORTH'
WHERE region LIKE '%NORTH%'
   OR region IN ('NORTH', 'NORTH1', 'NORTH2', 'NORTH3');

-- South region stores
UPDATE stores
SET corporate_username = 'CORP-SOUTH'
WHERE region LIKE '%SOUTH%'
   OR region IN ('SOUTH', 'SOUTH1', 'SOUTH2', 'SOUTH3');

-- East region stores
UPDATE stores
SET corporate_username = 'CORP-EAST'
WHERE region LIKE '%EAST%'
   OR region IN ('EAST', 'EAST1', 'EAST2', 'EAST3');

-- West region stores
UPDATE stores
SET corporate_username = 'CORP-WEST'
WHERE region LIKE '%WEST%'
   OR region IN ('WEST', 'WEST1', 'WEST2', 'WEST3');

-- ============================================
-- 5. Verification Queries
-- ============================================
-- Check corporate login table
SELECT * FROM corporate_login;

-- Count stores by corporate user
SELECT
    corporate_username,
    COUNT(*) as store_count,
    GROUP_CONCAT(DISTINCT region ORDER BY region) as regions
FROM stores
WHERE corporate_username IS NOT NULL
GROUP BY corporate_username;

-- Sample stores with full hierarchy
SELECT
    store_code,
    store_name,
    store_city,
    region,
    corporate_username,
    abm_username,
    rbm_username,
    cee_username
FROM stores
WHERE corporate_username IS NOT NULL
LIMIT 20;

-- ============================================
-- Access Hierarchy Summary:
-- ============================================
-- CORPORATE (Top) -> ABM -> RBM -> CEE -> STORE (Bottom)
--
-- CORP-NATIONAL-01: Can access ALL stores nationwide
-- CORP-NORTH/SOUTH/EAST/WEST: Can access stores in their respective regions
--
-- This allows corporate clients to download and view data from all stores
-- under their management, just like CEE users can see their stores.
-- ============================================

SELECT '=== CORPORATE LOGIN SETUP COMPLETE ===' AS Status;

