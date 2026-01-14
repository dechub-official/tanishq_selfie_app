-- =====================================================
-- PRODUCTION DATABASE SETUP SCRIPT
-- MySQL 8.4.7 Enterprise (Commercial)
-- Server: 10.10.63.97 (Production)
-- Data Directory: /var/lib/mysql
-- Service: mysqld (RUNNING)
-- Database: selfie_prod
-- =====================================================

-- Step 1: Create the production database
CREATE DATABASE IF NOT EXISTS selfie_prod
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Step 2: Verify database creation
SHOW DATABASES;

-- Step 3: Select the production database
USE selfie_prod;

-- Step 4: Create application user (if different from root)
-- Uncomment and modify if you want a dedicated application user
-- CREATE USER IF NOT EXISTS 'tanishq_prod'@'localhost' IDENTIFIED BY 'YourStrongPassword#2025';
-- GRANT ALL PRIVILEGES ON selfie_prod.* TO 'tanishq_prod'@'localhost';
-- FLUSH PRIVILEGES;

-- Step 5: Verify current user privileges
SHOW GRANTS FOR CURRENT_USER();

-- Step 6: Set MySQL configuration for production
-- Check current settings
SHOW VARIABLES LIKE 'max_connections';
SHOW VARIABLES LIKE 'max_allowed_packet';
SHOW VARIABLES LIKE 'innodb_buffer_pool_size';

-- Note: The following settings should be added to /etc/my.cnf [mysqld] section
-- Then restart MySQL service
-- max_connections=200
-- max_allowed_packet=100M
-- innodb_buffer_pool_size=2G (adjust based on available RAM)
-- character-set-server=utf8mb4
-- collation-server=utf8mb4_unicode_ci

-- Step 7: Verify database is ready
SELECT
    SCHEMA_NAME as 'Database',
    DEFAULT_CHARACTER_SET_NAME as 'Charset',
    DEFAULT_COLLATION_NAME as 'Collation'
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = 'selfie_prod';

-- =====================================================
-- NOTES:
-- 1. Spring Boot will auto-create tables on first run
--    (spring.jpa.hibernate.ddl-auto=update)
-- 2. Ensure MySQL is configured for production workload
-- 3. Backup strategy should be implemented
-- 4. Monitor disk space in /var/lib/mysql
-- =====================================================

