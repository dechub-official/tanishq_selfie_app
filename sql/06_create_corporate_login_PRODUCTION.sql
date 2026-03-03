-- ============================================
-- Corporate Login - PRODUCTION DEPLOYMENT
-- ============================================
-- ✅ READY FOR DEPLOYMENT - Database: selfie_prod
-- ============================================

-- Production database name
USE selfie_prod;

-- ============================================
-- 1. Create Corporate Login Table
-- ============================================
CREATE TABLE IF NOT EXISTS corporate_login (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    corporate_user_id VARCHAR(255) UNIQUE NOT NULL COMMENT 'Unique username for Corporate user',
    corporate_name VARCHAR(255) COMMENT 'Full name of Corporate user',
    password VARCHAR(255) NOT NULL COMMENT 'Password (change after deployment!)',
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
-- Check if column exists first
SET @dbname = DATABASE();
SET @tablename = 'stores';
SET @columnname = 'corporate_username';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1', -- Column exists, do nothing
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(255) COMMENT ''Mapped Corporate username - highest level access'' AFTER cee_username')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Create index if it doesn't exist
SET @indexExists = (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = DATABASE()
    AND table_name = 'stores'
    AND index_name = 'idx_corporate_username'
);

SET @createIndexSQL = IF(@indexExists = 0,
    'CREATE INDEX idx_corporate_username ON stores(corporate_username)',
    'SELECT ''Index already exists'' AS info'
);

PREPARE stmt FROM @createIndexSQL;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 3. Insert Sample Corporate Logins
-- ============================================
-- Note: Change passwords after deployment!

-- National/All India Corporate Access
INSERT INTO corporate_login (corporate_user_id, corporate_name, password, email, region, organization, access_level)
VALUES
('CORP-NATIONAL-01', 'National Head - Events', 'Tanishq@Corp2026', 'national.head@tanishq.com', 'ALL', 'Tanishq Events Division', 'CORPORATE'),
('CORP-TITAN-01', 'Titan Corporate', 'Titan@Corp2026', 'corporate@titan.co.in', 'ALL', 'Titan Company', 'CORPORATE')
ON DUPLICATE KEY UPDATE
    corporate_name = VALUES(corporate_name),
    email = VALUES(email),
    region = VALUES(region),
    organization = VALUES(organization),
    updated_at = CURRENT_TIMESTAMP;

-- Regional Corporate Access
INSERT INTO corporate_login (corporate_user_id, corporate_name, password, email, region, organization, access_level)
VALUES
('CORP-NORTH', 'North Regional Head', 'Tanishq@Corp2026', 'north.corp@tanishq.com', 'NORTH', 'Tanishq North Division', 'REGIONAL_HEAD'),
('CORP-SOUTH', 'South Regional Head', 'Tanishq@Corp2026', 'south.corp@tanishq.com', 'SOUTH', 'Tanishq South Division', 'REGIONAL_HEAD'),
('CORP-EAST', 'East Regional Head', 'Tanishq@Corp2026', 'east.corp@tanishq.com', 'EAST', 'Tanishq East Division', 'REGIONAL_HEAD'),
('CORP-WEST', 'West Regional Head', 'Tanishq@Corp2026', 'west.corp@tanishq.com', 'WEST', 'Tanishq West Division', 'REGIONAL_HEAD')
ON DUPLICATE KEY UPDATE
    corporate_name = VALUES(corporate_name),
    email = VALUES(email),
    region = VALUES(region),
    organization = VALUES(organization),
    updated_at = CURRENT_TIMESTAMP;

-- ============================================
-- 4. Map All Stores to Corporate Users
-- ============================================
-- Map all stores to national corporate users
UPDATE stores
SET corporate_username = 'CORP-NATIONAL-01'
WHERE 1=1;

-- Additionally map regional stores to regional corporate heads
-- North region stores
UPDATE stores
SET corporate_username = 'CORP-NORTH'
WHERE (region LIKE '%NORTH%' OR region IN ('NORTH', 'NORTH1', 'NORTH2', 'NORTH3'))
AND corporate_username IS NOT NULL;

-- South region stores
UPDATE stores
SET corporate_username = 'CORP-SOUTH'
WHERE (region LIKE '%SOUTH%' OR region IN ('SOUTH', 'SOUTH1', 'SOUTH2', 'SOUTH3'))
AND corporate_username IS NOT NULL;

-- East region stores
UPDATE stores
SET corporate_username = 'CORP-EAST'
WHERE (region LIKE '%EAST%' OR region IN ('EAST', 'EAST1', 'EAST2', 'EAST3'))
AND corporate_username IS NOT NULL;

-- West region stores
UPDATE stores
SET corporate_username = 'CORP-WEST'
WHERE (region LIKE '%WEST%' OR region IN ('WEST', 'WEST1', 'WEST2', 'WEST3'))
AND corporate_username IS NOT NULL;

-- ============================================
-- 5. Verification Queries
-- ============================================
SELECT '=== VERIFICATION RESULTS ===' AS Status;

-- Check corporate login table
SELECT 'Corporate Users Created:' AS Step;
SELECT
    corporate_user_id,
    corporate_name,
    region,
    access_level
FROM corporate_login
ORDER BY access_level, region;

-- Count stores by corporate user
SELECT 'Stores Mapped to Corporate Users:' AS Step;
SELECT
    corporate_username,
    COUNT(*) as store_count,
    GROUP_CONCAT(DISTINCT region ORDER BY region) as regions
FROM stores
WHERE corporate_username IS NOT NULL
GROUP BY corporate_username;

-- Sample stores with full hierarchy
SELECT 'Sample Store Hierarchy (first 10):' AS Step;
SELECT
    store_code,
    store_name,
    store_city,
    region,
    corporate_username,
    cee_username
FROM stores
WHERE corporate_username IS NOT NULL
LIMIT 10;

SELECT '=== DEPLOYMENT COMPLETE ===' AS Status;
SELECT 'Corporate login system is now active!' AS Message;
SELECT 'IMPORTANT: Change default passwords before going live!' AS Warning;

-- ============================================
-- Post-Deployment TODO:
-- ============================================
-- 1. Change default passwords:
--    UPDATE corporate_login SET password = 'NEW_SECURE_PASSWORD' WHERE corporate_user_id = 'CORP-NATIONAL-01';
--
-- 2. Test login endpoint:
--    curl -X POST http://your-server/events/corporate_login \
--      -H "Content-Type: application/json" \
--      -d '{"username":"CORP-NATIONAL-01","password":"Tanishq@Corp2026"}'
--
-- 3. Verify stores are accessible:
--    curl "http://your-server/events/corporateStores?corporateUsername=CORP-NATIONAL-01"
-- ============================================

