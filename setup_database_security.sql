-- ================================================================
-- TANISHQ SELFIE APPLICATION - DATABASE SECURITY SETUP
-- ================================================================
-- Purpose: Implement role-based access control with least privilege
-- Date: January 16, 2026
-- Database: selfie_prod (Production)
-- MySQL Version: 8.4.7 Enterprise
-- ================================================================

-- ================================================================
-- SECTION 1: BACKUP EXISTING CONFIGURATION
-- ================================================================
-- Before making changes, document current state

SELECT
    '=== EXISTING USER ACCOUNTS ===' as info,
    User,
    Host,
    authentication_string as 'Has Password'
FROM mysql.user
WHERE User NOT IN ('mysql.sys', 'mysql.session', 'mysql.infoschema')
ORDER BY User;

-- ================================================================
-- SECTION 2: CREATE APPLICATION USER (Limited Privileges)
-- ================================================================
-- This user will be used by the Java application
-- Password MUST be set by CLIENT

DROP USER IF EXISTS 'tanishq_app'@'localhost';

CREATE USER 'tanishq_app'@'localhost'
IDENTIFIED BY 'ClientToSetSecurePassword2026!';

-- Grant minimal required privileges for application operations
-- SELECT, INSERT, UPDATE, DELETE only - NO DDL commands

GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.events TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.attendees TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.invitees TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.bride_details TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.users TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.stores TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.product_details TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.greetings TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.rivaah TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.rivaah_users TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.abm_login TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.cee_login TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.rbm_login TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.user_details TO 'tanishq_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON selfie_prod.password_history TO 'tanishq_app'@'localhost';

-- Application user CANNOT:
-- - CREATE or DROP tables
-- - ALTER table structure
-- - GRANT privileges to others
-- - Access other databases
-- - Execute administrative commands

-- ================================================================
-- SECTION 3: CREATE VENDOR MONITORING USER (Read-Only Metrics)
-- ================================================================
-- This user allows vendor to check health without viewing data

DROP USER IF EXISTS 'tanishq_vendor'@'localhost';

CREATE USER 'tanishq_vendor'@'localhost'
IDENTIFIED BY 'VendorMonitor2026!';

-- Grant ONLY ID column access (for counting records)
-- Vendor can count but cannot see actual customer data

GRANT SELECT (id) ON selfie_prod.events TO 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.attendees TO 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.invitees TO 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.bride_details TO 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.users TO 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.stores TO 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.product_details TO 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.greetings TO 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.rivaah TO 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.rivaah_users TO 'tanishq_vendor'@'localhost';

-- Allow viewing table metadata (for troubleshooting)
GRANT SHOW VIEW ON selfie_prod.* TO 'tanishq_vendor'@'localhost';

-- Vendor user CANNOT:
-- - View customer names, emails, phone numbers
-- - View any columns except 'id'
-- - Modify any data
-- - Export data
-- - Drop or alter tables

-- ================================================================
-- SECTION 4: CREATE BACKUP USER (Client-Controlled Backups)
-- ================================================================
-- This user is for CLIENT's backup operations

DROP USER IF EXISTS 'tanishq_backup'@'localhost';

CREATE USER 'tanishq_backup'@'localhost'
IDENTIFIED BY 'ClientBackupPassword2026!';

-- Full read access for backups (read-only)
GRANT SELECT, LOCK TABLES ON selfie_prod.* TO 'tanishq_backup'@'localhost';

-- Allow reload privilege for consistent backups
GRANT RELOAD ON *.* TO 'tanishq_backup'@'localhost';

-- Backup user CANNOT:
-- - Modify data (INSERT, UPDATE, DELETE)
-- - Drop or create tables
-- - Grant privileges to others

-- ================================================================
-- SECTION 5: APPLY ALL CHANGES
-- ================================================================

FLUSH PRIVILEGES;

-- ================================================================
-- SECTION 6: VERIFY USER CREATION
-- ================================================================

SELECT '=== NEW USER ACCOUNTS CREATED ===' as info;

SELECT
    User,
    Host,
    CASE
        WHEN User = 'tanishq_app' THEN 'Application Database User'
        WHEN User = 'tanishq_vendor' THEN 'Vendor Monitoring User'
        WHEN User = 'tanishq_backup' THEN 'Client Backup User'
        ELSE 'Other'
    END as 'Purpose'
FROM mysql.user
WHERE User LIKE 'tanishq%'
ORDER BY User;

-- ================================================================
-- SECTION 7: DISPLAY GRANTED PRIVILEGES
-- ================================================================

SELECT '=== PRIVILEGES FOR tanishq_app ===' as info;
SHOW GRANTS FOR 'tanishq_app'@'localhost';

SELECT '=== PRIVILEGES FOR tanishq_vendor ===' as info;
SHOW GRANTS FOR 'tanishq_vendor'@'localhost';

SELECT '=== PRIVILEGES FOR tanishq_backup ===' as info;
SHOW GRANTS FOR 'tanishq_backup'@'localhost';

-- ================================================================
-- SECTION 8: SECURITY VERIFICATION QUERIES
-- ================================================================

-- Check privilege distribution
SELECT '=== PRIVILEGE SUMMARY ===' as info;

SELECT
    User,
    Host,
    Select_priv as 'SELECT',
    Insert_priv as 'INSERT',
    Update_priv as 'UPDATE',
    Delete_priv as 'DELETE',
    Create_priv as 'CREATE',
    Drop_priv as 'DROP',
    Grant_priv as 'GRANT',
    Reload_priv as 'RELOAD'
FROM mysql.user
WHERE User LIKE 'tanishq%' OR User = 'root'
ORDER BY User;

-- ================================================================
-- SECTION 9: TEST QUERIES (For Verification)
-- ================================================================

-- Test 1: Vendor can count records but cannot view data
-- Run as tanishq_vendor user:
-- SELECT COUNT(id) FROM selfie_prod.events;  -- Should work
-- SELECT * FROM selfie_prod.events;          -- Should fail

-- Test 2: Application user can perform CRUD operations
-- Run as tanishq_app user:
-- SELECT * FROM selfie_prod.events;          -- Should work
-- INSERT INTO selfie_prod.events (...);      -- Should work
-- DROP TABLE selfie_prod.events;             -- Should fail

-- Test 3: Backup user can read all data
-- Run as tanishq_backup user:
-- SELECT * FROM selfie_prod.events;          -- Should work
-- DELETE FROM selfie_prod.events;            -- Should fail

-- ================================================================
-- SECTION 10: OPTIONAL - ENABLE AUDIT LOGGING
-- ================================================================
-- CLIENT can enable audit logging to track all database access

-- Check if audit plugin is available
SELECT PLUGIN_NAME, PLUGIN_STATUS
FROM INFORMATION_SCHEMA.PLUGINS
WHERE PLUGIN_NAME LIKE 'audit%';

-- To enable general query log (for auditing):
-- Add to /etc/my.cnf:
-- [mysqld]
-- general_log = 1
-- general_log_file = /var/log/mysql/audit.log

-- ================================================================
-- SECTION 11: PASSWORD CHANGE PROCEDURES
-- ================================================================

-- CLIENT can change passwords at any time:

-- Change root password (CLIENT only):
-- ALTER USER 'root'@'localhost' IDENTIFIED BY 'NewRootPassword';
-- FLUSH PRIVILEGES;

-- Change application password (CLIENT provides to vendor):
-- ALTER USER 'tanishq_app'@'localhost' IDENTIFIED BY 'NewAppPassword';
-- FLUSH PRIVILEGES;

-- Change vendor password:
-- ALTER USER 'tanishq_vendor'@'localhost' IDENTIFIED BY 'NewVendorPassword';
-- FLUSH PRIVILEGES;

-- ================================================================
-- SECTION 12: REVOKE ACCESS (If Needed)
-- ================================================================

-- If vendor access needs to be completely revoked:
-- REVOKE ALL PRIVILEGES ON selfie_prod.* FROM 'tanishq_vendor'@'localhost';
-- DROP USER 'tanishq_vendor'@'localhost';
-- FLUSH PRIVILEGES;

-- ================================================================
-- SECTION 13: GRANT TEMPORARY ELEVATED ACCESS
-- ================================================================

-- If vendor needs temporary full access for troubleshooting:
-- CLIENT executes:
-- GRANT ALL PRIVILEGES ON selfie_prod.* TO 'tanishq_vendor'@'localhost';
-- FLUSH PRIVILEGES;

-- After work is complete, CLIENT revokes:
-- REVOKE ALL PRIVILEGES ON selfie_prod.* FROM 'tanishq_vendor'@'localhost';
-- GRANT SELECT (id) ON selfie_prod.* TO 'tanishq_vendor'@'localhost';
-- FLUSH PRIVILEGES;

-- ================================================================
-- IMPLEMENTATION COMPLETE
-- ================================================================

SELECT '
=========================================
DATABASE SECURITY SETUP COMPLETED
=========================================

NEXT STEPS:

1. CLIENT: Change all default passwords
   - Root password
   - tanishq_app password
   - tanishq_vendor password
   - tanishq_backup password

2. CLIENT: Provide tanishq_app password to VENDOR securely

3. VENDOR: Update application-prod.properties with new credentials

4. CLIENT: Enable audit logging in MySQL configuration

5. CLIENT: Set up automated backup schedule

6. BOTH: Review and sign handover documents

=========================================
' as 'IMPLEMENTATION STATUS';

-- ================================================================
-- END OF SECURITY SETUP SCRIPT
-- ================================================================

