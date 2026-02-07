-- =====================================================
-- CHECK AND FIX LOGIN TABLES STRUCTURE
-- Date: January 29, 2026
-- Database: selfie_prod
-- Issue: Queries failing with "Unknown column 'username'"
-- =====================================================

USE selfie_prod;

SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'LOGIN TABLES STRUCTURE CHECK' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- Check ABM Login Table Structure
SELECT 'ABM_LOGIN TABLE STRUCTURE:' AS '';
DESCRIBE abm_login;

SELECT '' AS '';
SELECT 'RBM_LOGIN TABLE STRUCTURE:' AS '';
DESCRIBE rbm_login;

SELECT '' AS '';
SELECT 'CEE_LOGIN TABLE STRUCTURE:' AS '';
DESCRIBE cee_login;

-- Show sample data
SELECT '' AS '';
SELECT 'ABM_LOGIN SAMPLE DATA:' AS '';
SELECT * FROM abm_login LIMIT 5;

SELECT '' AS '';
SELECT 'RBM_LOGIN SAMPLE DATA:' AS '';
SELECT * FROM rbm_login LIMIT 5;

SELECT '' AS '';
SELECT 'CEE_LOGIN SAMPLE DATA:' AS '';
SELECT * FROM cee_login LIMIT 5;

-- Count records
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'LOGIN ACCOUNTS COUNT:' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

SELECT
    'ABM Accounts' as account_type,
    COUNT(*) as total_accounts
FROM abm_login
UNION ALL
SELECT
    'RBM Accounts',
    COUNT(*)
FROM rbm_login
UNION ALL
SELECT
    'CEE Accounts',
    COUNT(*)
FROM cee_login;

-- =====================================================
-- BASED ON YOUR TABLE STRUCTURE, USE ONE OF THESE:
-- =====================================================

/*
IF YOUR COLUMNS ARE NAMED DIFFERENTLY, RUN THE APPROPRIATE QUERY BELOW:

-- If columns are: user_name, user_password
SELECT
    user_name as ABM_Username,
    user_password as ABM_Password,
    (SELECT COUNT(*) FROM stores WHERE abm_username = abm_login.user_name) as Stores_Assigned
FROM abm_login
ORDER BY user_name;

-- If columns are: login, password
SELECT
    login as ABM_Username,
    password as ABM_Password,
    (SELECT COUNT(*) FROM stores WHERE abm_username = abm_login.login) as Stores_Assigned
FROM abm_login
ORDER BY login;

-- If columns are: id, name, password
SELECT
    name as ABM_Username,
    password as ABM_Password,
    (SELECT COUNT(*) FROM stores WHERE abm_username = abm_login.name) as Stores_Assigned
FROM abm_login
ORDER BY name;
*/

-- =====================================================
-- CHECK WHICH MANAGERS ARE REFERENCED BUT DON'T EXIST
-- =====================================================

SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'CHECKING FOR ORPHANED MANAGER REFERENCES' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- ABM usernames in stores
SELECT 'ABM USERNAMES IN STORES:' AS '';
SELECT DISTINCT
    abm_username,
    COUNT(*) as store_count
FROM stores
WHERE abm_username IS NOT NULL
GROUP BY abm_username
ORDER BY abm_username;

SELECT '' AS '';
SELECT 'RBM USERNAMES IN STORES:' AS '';
SELECT DISTINCT
    rbm_username,
    COUNT(*) as store_count
FROM stores
WHERE rbm_username IS NOT NULL
GROUP BY rbm_username
ORDER BY rbm_username;

SELECT '' AS '';
SELECT 'CEE USERNAMES IN STORES:' AS '';
SELECT DISTINCT
    cee_username,
    COUNT(*) as store_count
FROM stores
WHERE cee_username IS NOT NULL
GROUP BY cee_username
ORDER BY cee_username;

-- =====================================================
-- SUGGESTED FIX: Create proper login accounts
-- =====================================================

/*
AFTER YOU RUN THE DESCRIBE COMMANDS ABOVE AND KNOW THE COLUMN NAMES,
USE THIS TEMPLATE TO ADD MISSING ACCOUNTS:

-- For ABM (adjust column names as needed)
INSERT INTO abm_login (username_column, password_column)
VALUES ('NORTH1-ABM-01', 'default_password');

-- For RBM (adjust column names as needed)
INSERT INTO rbm_login (username_column, password_column)
VALUES ('NORTH1', 'default_password');

-- For CEE (adjust column names as needed)
INSERT INTO cee_login (username_column, password_column)
VALUES ('NORTH1-CEE-01', 'default_password');

REPEAT FOR ALL MISSING ACCOUNTS SHOWN IN THE OUTPUT ABOVE.
*/

SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '✅ CHECK COMPLETED' AS '';
SELECT 'Review the output above to understand your table structure' AS '';
SELECT 'Then use the appropriate INSERT statements to add missing accounts' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';

-- =====================================================
-- END OF CHECK SCRIPT
-- =====================================================

