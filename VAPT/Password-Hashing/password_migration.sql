-- ============================================
-- Password Hashing Migration Script
-- ============================================
-- This script helps migrate plain text passwords to BCrypt hashed passwords
--
-- IMPORTANT: This script provides the structure only
-- The actual password hashing MUST be done by the Java application
-- DO NOT run password hashing in SQL - it's not secure
--
-- Security Fix: OWASP A02 - Cryptographic Failures
-- Vulnerability: Passwords stored in plain text
-- Solution: BCrypt hashing with strength 12
-- ============================================

USE tanishq;

-- ============================================
-- STEP 1: Backup Current Data (MANDATORY)
-- ============================================

-- Backup ABM logins
CREATE TABLE IF NOT EXISTS abm_login_backup_20260305 AS SELECT * FROM abm_login;

-- Backup RBM logins
CREATE TABLE IF NOT EXISTS rbm_login_backup_20260305 AS SELECT * FROM rbm_login;

-- Backup CEE logins
CREATE TABLE IF NOT EXISTS cee_login_backup_20260305 AS SELECT * FROM cee_login;

-- Backup Corporate logins
CREATE TABLE IF NOT EXISTS corporate_login_backup_20260305 AS SELECT * FROM corporate_login;

-- Backup Users table
CREATE TABLE IF NOT EXISTS users_backup_20260305 AS SELECT * FROM users;

-- ============================================
-- STEP 2: Update Password Column Size
-- ============================================
-- BCrypt passwords are 60 characters long
-- Ensure VARCHAR(255) is sufficient (it is)

ALTER TABLE abm_login MODIFY COLUMN password VARCHAR(255) NOT NULL
    COMMENT 'BCrypt hashed password ($2a$12$...)';

ALTER TABLE rbm_login MODIFY COLUMN password VARCHAR(255) NOT NULL
    COMMENT 'BCrypt hashed password ($2a$12$...)';

ALTER TABLE cee_login MODIFY COLUMN password VARCHAR(255) NOT NULL
    COMMENT 'BCrypt hashed password ($2a$12$...)';

ALTER TABLE corporate_login MODIFY COLUMN password VARCHAR(255) NOT NULL
    COMMENT 'BCrypt hashed password ($2a$12$...)';

ALTER TABLE users MODIFY COLUMN password VARCHAR(255)
    COMMENT 'BCrypt hashed password ($2a$12$...)';

-- ============================================
-- STEP 3: Password Migration Instructions
-- ============================================

-- OPTION A: Gradual Migration (RECOMMENDED)
-- ----------------------------------------
-- The application now supports BOTH plain text and BCrypt passwords
-- Plain text passwords will work during transition period
-- When users login successfully, their password is automatically hashed
-- When users change password, new password is hashed
-- This allows zero-downtime migration

-- OPTION B: Bulk Migration via Java Utility (if needed)
-- ----------------------------------------
-- Create a Java utility class to hash all passwords at once
-- See: PasswordMigrationUtility.java
-- This should be run during maintenance window if desired

-- ============================================
-- STEP 4: Verification Queries
-- ============================================

-- Check which passwords are already hashed (start with $2a$, $2b$, or $2y$)
SELECT
    'ABM' as user_type,
    COUNT(*) as total_users,
    SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END) as hashed_passwords,
    SUM(CASE WHEN password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%' THEN 1 ELSE 0 END) as plain_text_passwords
FROM abm_login

UNION ALL

SELECT
    'RBM' as user_type,
    COUNT(*) as total_users,
    SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END) as hashed_passwords,
    SUM(CASE WHEN password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%' THEN 1 ELSE 0 END) as plain_text_passwords
FROM rbm_login

UNION ALL

SELECT
    'CEE' as user_type,
    COUNT(*) as total_users,
    SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END) as hashed_passwords,
    SUM(CASE WHEN password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%' THEN 1 ELSE 0 END) as plain_text_passwords
FROM cee_login

UNION ALL

SELECT
    'Corporate' as user_type,
    COUNT(*) as total_users,
    SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END) as hashed_passwords,
    SUM(CASE WHEN password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%' THEN 1 ELSE 0 END) as plain_text_passwords
FROM corporate_login

UNION ALL

SELECT
    'Store Users' as user_type,
    COUNT(*) as total_users,
    SUM(CASE WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN 1 ELSE 0 END) as hashed_passwords,
    SUM(CASE WHEN password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%' THEN 1 ELSE 0 END) as plain_text_passwords
FROM users
WHERE password IS NOT NULL;

-- ============================================
-- STEP 5: Rollback Plan (Emergency Use Only)
-- ============================================

-- If something goes wrong, restore from backup:
-- TRUNCATE TABLE abm_login;
-- INSERT INTO abm_login SELECT * FROM abm_login_backup_20260305;

-- TRUNCATE TABLE rbm_login;
-- INSERT INTO rbm_login SELECT * FROM rbm_login_backup_20260305;

-- TRUNCATE TABLE cee_login;
-- INSERT INTO cee_login SELECT * FROM cee_login_backup_20260305;

-- TRUNCATE TABLE corporate_login;
-- INSERT INTO corporate_login SELECT * FROM corporate_login_backup_20260305;

-- TRUNCATE TABLE users;
-- INSERT INTO users SELECT * FROM users_backup_20260305;

-- ============================================
-- Notes:
-- ============================================
-- 1. BCrypt hashes always start with $2a$, $2b$, or $2y$
-- 2. BCrypt hashes are 60 characters long
-- 3. Each BCrypt hash has unique salt (automatic)
-- 4. Cannot reverse BCrypt hash to get plain text
-- 5. Must verify passwords using passwordEncoder.matches()

