-- =====================================================
-- CREATE SEPARATE MANAGER TABLES
-- =====================================================
-- Creates separate tables for RBM, CEE, ABM logins
-- Removes manager accounts from users table
-- =====================================================

-- Step 1: Create RBM Login Table
CREATE TABLE IF NOT EXISTS rbm_login (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rbm_user_id VARCHAR(255) NOT NULL UNIQUE,
    rbm_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_rbm_user_id (rbm_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 2: Create CEE Login Table
CREATE TABLE IF NOT EXISTS cee_login (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cee_user_id VARCHAR(255) NOT NULL UNIQUE,
    cee_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    region VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cee_user_id (cee_user_id),
    INDEX idx_region (region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 3: Create ABM Login Table
CREATE TABLE IF NOT EXISTS abm_login (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    abm_user_id VARCHAR(255) NOT NULL UNIQUE,
    abm_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    region VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_abm_user_id (abm_user_id),
    INDEX idx_region (region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 4: Migrate RBM data from users table to rbm_login
INSERT INTO rbm_login (rbm_user_id, rbm_name, password, email)
SELECT username, name, password, email 
FROM users 
WHERE role = 'RBM'
ON DUPLICATE KEY UPDATE 
    rbm_name = VALUES(rbm_name),
    password = VALUES(password),
    email = VALUES(email);

-- Step 5: Migrate CEE data from users table to cee_login
INSERT INTO cee_login (cee_user_id, cee_name, password, email, region)
SELECT 
    username, 
    name, 
    password, 
    email,
    CASE 
        WHEN username LIKE 'EAST%' THEN SUBSTRING(username, 1, 5)
        WHEN username LIKE 'NORTH%' THEN SUBSTRING(username, 1, 6)
        WHEN username LIKE 'SOUTH%' THEN SUBSTRING(username, 1, 6)
        WHEN username LIKE 'WEST%' THEN SUBSTRING(username, 1, 5)
    END
FROM users 
WHERE role = 'CEE'
ON DUPLICATE KEY UPDATE 
    cee_name = VALUES(cee_name),
    password = VALUES(password),
    email = VALUES(email),
    region = VALUES(region);

-- Step 6: Migrate ABM data from users table to abm_login
INSERT INTO abm_login (abm_user_id, abm_name, password, email, region)
SELECT 
    username, 
    name, 
    password, 
    email,
    CASE 
        WHEN username LIKE 'EAST%' THEN SUBSTRING(username, 1, 5)
        WHEN username LIKE 'NORTH%' THEN SUBSTRING(username, 1, 6)
        WHEN username LIKE 'SOUTH%' THEN SUBSTRING(username, 1, 6)
        WHEN username LIKE 'WEST%' THEN SUBSTRING(username, 1, 5)
    END
FROM users 
WHERE role = 'ABM'
ON DUPLICATE KEY UPDATE 
    abm_name = VALUES(abm_name),
    password = VALUES(password),
    email = VALUES(email),
    region = VALUES(region);

-- Step 7: Delete manager accounts from users table (keep only Store role)
DELETE FROM users WHERE role IN ('RBM', 'CEE', 'ABM');

-- Step 8: Update users table structure (remove role column since it's only for stores now)
-- ALTER TABLE users DROP COLUMN IF EXISTS role;

-- Step 9: Add foreign key constraints to stores table for manager references
-- Note: This assumes abm_username, rbm_username, cee_username columns exist in stores table

-- Optional: Add indexes on stores table for better performance
ALTER TABLE stores ADD INDEX IF NOT EXISTS idx_abm_username (abm_username);
ALTER TABLE stores ADD INDEX IF NOT EXISTS idx_rbm_username (rbm_username);
ALTER TABLE stores ADD INDEX IF NOT EXISTS idx_cee_username (cee_username);
ALTER TABLE stores ADD INDEX IF NOT EXISTS idx_region (region);

-- Step 10: Verification
SELECT '=== TABLE CREATION SUMMARY ===' as status;
SELECT '' as blank;
SELECT 'RBM Login Table:' as info, COUNT(*) as count FROM rbm_login;
SELECT 'CEE Login Table:' as info, COUNT(*) as count FROM cee_login;
SELECT 'ABM Login Table:' as info, COUNT(*) as count FROM abm_login;
SELECT 'Users Table (Stores Only):' as info, COUNT(*) as count FROM users;
SELECT '' as blank;
SELECT 'Sample RBM Logins:' as info;
SELECT rbm_user_id, rbm_name FROM rbm_login LIMIT 5;
SELECT '' as blank;
SELECT 'Sample CEE Logins:' as info;
SELECT cee_user_id, cee_name, region FROM cee_login LIMIT 5;
SELECT '' as blank;
SELECT 'Sample ABM Logins:' as info;
SELECT abm_user_id, abm_name, region FROM abm_login LIMIT 5;
SELECT '' as blank;
SELECT '=== MIGRATION COMPLETED ===' as status;
