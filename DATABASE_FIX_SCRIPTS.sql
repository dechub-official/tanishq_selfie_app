-- ============================================================================
-- DATABASE FIX & OPTIMIZATION SCRIPTS
-- Tanishq Selfie App - Database Improvements
-- ============================================================================
-- ⚠️ WARNING: These scripts modify database structure and data
-- ⚠️ ALWAYS backup database before running these scripts
-- ⚠️ Test in pre-production environment first
-- ============================================================================

-- ============================================================================
-- BACKUP COMMANDS (Run these FIRST!)
-- ============================================================================

-- Create backup tables before making changes
CREATE TABLE IF NOT EXISTS events_backup_before_fixes_20260204 AS SELECT * FROM events;
CREATE TABLE IF NOT EXISTS attendees_backup_before_fixes_20260204 AS SELECT * FROM attendees;
CREATE TABLE IF NOT EXISTS stores_backup_before_fixes_20260204 AS SELECT * FROM stores;
CREATE TABLE IF NOT EXISTS greetings_backup_before_fixes_20260204 AS SELECT * FROM greetings;

-- Verify backups
SELECT 'events' AS original, COUNT(*) FROM events
UNION ALL
SELECT 'events_backup', COUNT(*) FROM events_backup_before_fixes_20260204;

-- ============================================================================
-- SECTION 1: ADD MISSING INDEXES (HIGH PRIORITY)
-- ============================================================================

-- 1.1 Events table indexes
ALTER TABLE events ADD INDEX idx_events_store_code (store_code);
ALTER TABLE events ADD INDEX idx_events_created_at (created_at);
ALTER TABLE events ADD INDEX idx_events_start_date (start_date);
ALTER TABLE events ADD INDEX idx_events_region (region);
ALTER TABLE events ADD INDEX idx_events_event_type (event_type);
-- Composite index for common queries
ALTER TABLE events ADD INDEX idx_events_store_date (store_code, created_at);
ALTER TABLE events ADD INDEX idx_events_store_type (store_code, event_type);

-- 1.2 Attendees table indexes
ALTER TABLE attendees ADD INDEX idx_attendees_event_id (event_id);
ALTER TABLE attendees ADD INDEX idx_attendees_phone (phone);
ALTER TABLE attendees ADD INDEX idx_attendees_created_at (created_at);
ALTER TABLE attendees ADD INDEX idx_attendees_first_time (first_time_at_tanishq);
-- Composite index
ALTER TABLE attendees ADD INDEX idx_attendees_event_created (event_id, created_at);

-- 1.3 Invitees table indexes
ALTER TABLE invitees ADD INDEX idx_invitees_event_id (event_id);
ALTER TABLE invitees ADD INDEX idx_invitees_contact (contact);
ALTER TABLE invitees ADD INDEX idx_invitees_created_at (created_at);

-- 1.4 Stores table indexes
ALTER TABLE stores ADD INDEX idx_stores_region (region);
ALTER TABLE stores ADD INDEX idx_stores_abm_username (abm_username);
ALTER TABLE stores ADD INDEX idx_stores_rbm_username (rbm_username);
ALTER TABLE stores ADD INDEX idx_stores_cee_username (cee_username);
ALTER TABLE stores ADD INDEX idx_stores_city (store_city);

-- 1.5 Greetings table indexes
ALTER TABLE greetings ADD UNIQUE INDEX uk_greetings_unique_id (unique_id);
ALTER TABLE greetings ADD INDEX idx_greetings_uploaded (uploaded);
ALTER TABLE greetings ADD INDEX idx_greetings_created_at (created_at);

-- 1.6 Login tables indexes
ALTER TABLE abm_login ADD INDEX idx_abm_region (region);
ALTER TABLE abm_login ADD INDEX idx_abm_email (email);
ALTER TABLE rbm_login ADD INDEX idx_rbm_email (email);
ALTER TABLE cee_login ADD INDEX idx_cee_region (region);
ALTER TABLE cee_login ADD INDEX idx_cee_email (email);

-- 1.7 Rivaah tables indexes
ALTER TABLE rivaah ADD INDEX idx_rivaah_code (code);
ALTER TABLE rivaah_users ADD INDEX idx_rivaah_users_contact (contact);
ALTER TABLE product_details ADD INDEX idx_product_category (category);

-- Verify indexes were created
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND INDEX_NAME LIKE 'idx_%'
GROUP BY TABLE_NAME, INDEX_NAME
ORDER BY TABLE_NAME;

-- ============================================================================
-- SECTION 2: ADD AUDIT COLUMNS (MEDIUM PRIORITY)
-- ============================================================================

-- 2.1 Add updated_at columns with auto-update trigger
ALTER TABLE events
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD COLUMN created_by VARCHAR(255),
    ADD COLUMN updated_by VARCHAR(255);

ALTER TABLE attendees
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE invitees
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE stores
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE greetings
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 2.2 Add soft delete columns
ALTER TABLE events ADD COLUMN is_active TINYINT(1) DEFAULT 1;
ALTER TABLE attendees ADD COLUMN is_active TINYINT(1) DEFAULT 1;
ALTER TABLE invitees ADD COLUMN is_active TINYINT(1) DEFAULT 1;
ALTER TABLE stores ADD COLUMN is_active TINYINT(1) DEFAULT 1;

-- Update existing records to active
UPDATE events SET is_active = 1 WHERE is_active IS NULL;
UPDATE attendees SET is_active = 1 WHERE is_active IS NULL;
UPDATE invitees SET is_active = 1 WHERE is_active IS NULL;
UPDATE stores SET is_active = 1 WHERE is_active IS NULL;

-- ============================================================================
-- SECTION 3: ADD NOT NULL CONSTRAINTS (MEDIUM PRIORITY)
-- ============================================================================

-- 3.1 Events table constraints
-- First, clean up any NULL values
UPDATE events SET event_name = 'Unnamed Event' WHERE event_name IS NULL OR event_name = '';
UPDATE events SET event_type = 'General' WHERE event_type IS NULL OR event_type = '';
UPDATE events SET region = 'Unknown' WHERE region IS NULL OR region = '';

-- Then add constraints
ALTER TABLE events MODIFY COLUMN event_name VARCHAR(255) NOT NULL;
ALTER TABLE events MODIFY COLUMN store_code VARCHAR(255) NOT NULL;
ALTER TABLE events MODIFY COLUMN created_at DATETIME NOT NULL;

-- 3.2 Attendees constraints
UPDATE attendees SET name = 'Anonymous' WHERE name IS NULL OR name = '';
ALTER TABLE attendees MODIFY COLUMN name VARCHAR(255) NOT NULL;
ALTER TABLE attendees MODIFY COLUMN event_id VARCHAR(255) NOT NULL;

-- 3.3 Invitees constraints
UPDATE invitees SET name = 'Anonymous' WHERE name IS NULL OR name = '';
ALTER TABLE invitees MODIFY COLUMN name VARCHAR(255) NOT NULL;
ALTER TABLE invitees MODIFY COLUMN event_id VARCHAR(255) NOT NULL;

-- 3.4 Stores constraints
ALTER TABLE stores MODIFY COLUMN store_name VARCHAR(255) NOT NULL;

-- ============================================================================
-- SECTION 4: ADD CHECK CONSTRAINTS (MySQL 8.0+)
-- ============================================================================

-- 4.1 Sales validation constraints
ALTER TABLE events
    ADD CONSTRAINT chk_events_sale_positive
    CHECK (sale IS NULL OR sale >= 0);

ALTER TABLE events
    ADD CONSTRAINT chk_events_advance_positive
    CHECK (advance IS NULL OR advance >= 0);

ALTER TABLE events
    ADD CONSTRAINT chk_events_advance_not_exceed_sale
    CHECK (advance IS NULL OR sale IS NULL OR advance <= sale);

-- 4.2 Count validation constraints
ALTER TABLE events
    ADD CONSTRAINT chk_events_attendees_positive
    CHECK (attendees IS NULL OR attendees >= 0);

ALTER TABLE events
    ADD CONSTRAINT chk_events_invitees_positive
    CHECK (invitees IS NULL OR invitees >= 0);

-- 4.3 Greeting validation
ALTER TABLE greetings
    ADD CONSTRAINT chk_greetings_uploaded_valid
    CHECK (uploaded IN (0, 1));

-- ============================================================================
-- SECTION 5: DATA CLEANUP
-- ============================================================================

-- 5.1 Remove orphaned attendees (backup first!)
-- Uncomment after reviewing orphaned records
-- DELETE FROM attendees
-- WHERE event_id NOT IN (SELECT id FROM events);

-- 5.2 Remove orphaned invitees
-- DELETE FROM invitees
-- WHERE event_id NOT IN (SELECT id FROM events);

-- 5.3 Clean up duplicate phone numbers (keep most recent)
-- This is a complex operation - review carefully before running
/*
DELETE a1 FROM attendees a1
INNER JOIN attendees a2
WHERE a1.id < a2.id
  AND a1.phone = a2.phone
  AND a1.event_id = a2.event_id
  AND a1.phone IS NOT NULL
  AND a1.phone != '';
*/

-- 5.4 Fix inconsistent attendee/invitee counts
UPDATE events e
SET e.attendees = (
    SELECT COUNT(*)
    FROM attendees a
    WHERE a.event_id = e.id
)
WHERE e.id IN (SELECT DISTINCT event_id FROM attendees);

UPDATE events e
SET e.invitees = (
    SELECT COUNT(*)
    FROM invitees i
    WHERE i.event_id = e.id
)
WHERE e.id IN (SELECT DISTINCT event_id FROM invitees);

-- ============================================================================
-- SECTION 6: SECURITY IMPROVEMENTS (CRITICAL)
-- ============================================================================

-- 6.1 Add password policy columns
ALTER TABLE abm_login
    ADD COLUMN password_changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN must_change_password TINYINT(1) DEFAULT 0,
    ADD COLUMN last_login_at DATETIME,
    ADD COLUMN failed_login_attempts INT DEFAULT 0;

ALTER TABLE rbm_login
    ADD COLUMN password_changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN must_change_password TINYINT(1) DEFAULT 0,
    ADD COLUMN last_login_at DATETIME,
    ADD COLUMN failed_login_attempts INT DEFAULT 0;

ALTER TABLE cee_login
    ADD COLUMN password_changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN must_change_password TINYINT(1) DEFAULT 0,
    ADD COLUMN last_login_at DATETIME,
    ADD COLUMN failed_login_attempts INT DEFAULT 0;

-- 6.2 ⚠️ PASSWORD HASHING NOTE:
-- Passwords MUST be hashed in the application layer using BCrypt
-- DO NOT hash passwords in SQL - use Spring Security BCryptPasswordEncoder
-- Sample Java code to use:
/*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode(plainPassword);
*/

-- ============================================================================
-- SECTION 7: ADD FOREIGN KEY CONSTRAINTS (If not exist)
-- ============================================================================

-- 7.1 Check existing foreign keys first
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 7.2 Add foreign keys if they don't exist
-- Note: Hibernate may have already created these

-- Events -> Stores foreign key
ALTER TABLE events
    ADD CONSTRAINT fk_events_store
    FOREIGN KEY (store_code)
    REFERENCES stores(store_code)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

-- Attendees -> Events foreign key
ALTER TABLE attendees
    ADD CONSTRAINT fk_attendees_event
    FOREIGN KEY (event_id)
    REFERENCES events(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- Invitees -> Events foreign key
ALTER TABLE invitees
    ADD CONSTRAINT fk_invitees_event
    FOREIGN KEY (event_id)
    REFERENCES events(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- Rivaah Users -> Rivaah foreign key
ALTER TABLE rivaah_users
    ADD CONSTRAINT fk_rivaah_users_rivaah
    FOREIGN KEY (rivaah_id)
    REFERENCES rivaah(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- Product Details -> Rivaah foreign key
ALTER TABLE product_details
    ADD CONSTRAINT fk_product_details_rivaah
    FOREIGN KEY (rivaah_id)
    REFERENCES rivaah(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- ============================================================================
-- SECTION 8: OPTIMIZE TABLES
-- ============================================================================

-- 8.1 Analyze tables to update statistics
ANALYZE TABLE events;
ANALYZE TABLE attendees;
ANALYZE TABLE invitees;
ANALYZE TABLE stores;
ANALYZE TABLE greetings;
ANALYZE TABLE users;
ANALYZE TABLE user_details;
ANALYZE TABLE bride_details;
ANALYZE TABLE abm_login;
ANALYZE TABLE rbm_login;
ANALYZE TABLE cee_login;
ANALYZE TABLE rivaah;
ANALYZE TABLE rivaah_users;
ANALYZE TABLE product_details;

-- 8.2 Optimize tables to reclaim space
OPTIMIZE TABLE events;
OPTIMIZE TABLE attendees;
OPTIMIZE TABLE invitees;
OPTIMIZE TABLE stores;
OPTIMIZE TABLE greetings;

-- ============================================================================
-- SECTION 9: CREATE USEFUL VIEWS
-- ============================================================================

-- 9.1 View for event summary with actual counts
CREATE OR REPLACE VIEW v_event_summary AS
SELECT
    e.id,
    e.event_name,
    e.event_type,
    e.store_code,
    s.store_name,
    s.region,
    e.created_at,
    e.start_date,
    e.invitees AS expected_invitees,
    COUNT(DISTINCT i.id) AS actual_invitees,
    e.attendees AS expected_attendees,
    COUNT(DISTINCT a.id) AS actual_attendees,
    e.sale,
    e.advance,
    e.is_active
FROM events e
LEFT JOIN stores s ON e.store_code = s.store_code
LEFT JOIN attendees a ON e.id = a.event_id
LEFT JOIN invitees i ON e.id = i.event_id
GROUP BY e.id, e.event_name, e.event_type, e.store_code, s.store_name,
         s.region, e.created_at, e.start_date, e.invitees, e.attendees,
         e.sale, e.advance, e.is_active;

-- 9.2 View for store performance
CREATE OR REPLACE VIEW v_store_performance AS
SELECT
    s.store_code,
    s.store_name,
    s.region,
    s.store_city,
    COUNT(DISTINCT e.id) AS total_events,
    COALESCE(SUM(e.attendees), 0) AS total_attendees,
    COALESCE(SUM(e.sale), 0) AS total_sales,
    COALESCE(AVG(e.sale), 0) AS avg_sale_per_event,
    MAX(e.created_at) AS last_event_date,
    DATEDIFF(NOW(), MAX(e.created_at)) AS days_since_last_event
FROM stores s
LEFT JOIN events e ON s.store_code = e.store_code AND e.is_active = 1
WHERE s.is_active = 1
GROUP BY s.store_code, s.store_name, s.region, s.store_city;

-- 9.3 View for greeting status
CREATE OR REPLACE VIEW v_greeting_status AS
SELECT
    g.id,
    g.unique_id,
    g.greeting_text,
    g.phone,
    g.created_at,
    g.uploaded,
    CASE
        WHEN g.uploaded = 1 THEN 'Completed'
        WHEN g.uploaded = 0 AND DATEDIFF(NOW(), g.created_at) > 7 THEN 'Abandoned'
        WHEN g.uploaded = 0 THEN 'Pending'
        ELSE 'Unknown'
    END AS status,
    DATEDIFF(NOW(), g.created_at) AS days_old
FROM greetings g;

-- 9.4 View for region performance
CREATE OR REPLACE VIEW v_region_performance AS
SELECT
    s.region,
    COUNT(DISTINCT s.store_code) AS store_count,
    COUNT(DISTINCT e.id) AS total_events,
    COALESCE(SUM(e.attendees), 0) AS total_attendees,
    COALESCE(SUM(e.sale), 0) AS total_sales,
    COALESCE(AVG(e.sale), 0) AS avg_sale_per_event,
    COALESCE(SUM(e.advance), 0) AS total_advance
FROM stores s
LEFT JOIN events e ON s.store_code = e.store_code AND e.is_active = 1
WHERE s.region IS NOT NULL AND s.is_active = 1
GROUP BY s.region;

-- ============================================================================
-- SECTION 10: VERIFICATION QUERIES
-- ============================================================================

-- 10.1 Verify indexes were created
SELECT
    TABLE_NAME,
    COUNT(DISTINCT INDEX_NAME) AS index_count
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('events', 'attendees', 'invitees', 'stores', 'greetings')
GROUP BY TABLE_NAME
ORDER BY TABLE_NAME;

-- 10.2 Verify constraints were added
SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE
FROM information_schema.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = DATABASE()
  AND CONSTRAINT_TYPE IN ('CHECK', 'FOREIGN KEY')
ORDER BY TABLE_NAME, CONSTRAINT_TYPE;

-- 10.3 Verify audit columns were added
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND COLUMN_NAME IN ('updated_at', 'created_by', 'updated_by', 'is_active')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 10.4 Verify views were created
SHOW FULL TABLES WHERE Table_type = 'VIEW';

-- ============================================================================
-- SECTION 11: POST-DEPLOYMENT VALIDATION
-- ============================================================================

-- Run these queries after applying fixes to ensure everything works

-- 11.1 Test view queries
SELECT * FROM v_event_summary LIMIT 10;
SELECT * FROM v_store_performance LIMIT 10;
SELECT * FROM v_greeting_status LIMIT 10;
SELECT * FROM v_region_performance;

-- 11.2 Check for any remaining orphaned records
SELECT COUNT(*) AS orphaned_attendees
FROM attendees a
LEFT JOIN events e ON a.event_id = e.id
WHERE e.id IS NULL;

SELECT COUNT(*) AS orphaned_invitees
FROM invitees i
LEFT JOIN events e ON i.event_id = e.id
WHERE e.id IS NULL;

-- 11.3 Verify data integrity
SELECT
    'Events with mismatched counts' AS check_type,
    COUNT(*) AS issues_found
FROM events e
WHERE e.attendees != (SELECT COUNT(*) FROM attendees WHERE event_id = e.id)
   OR e.invitees != (SELECT COUNT(*) FROM invitees WHERE event_id = e.id);

-- ============================================================================
-- ROLLBACK PROCEDURE (If something goes wrong)
-- ============================================================================

-- If you need to rollback changes:
/*
-- Drop added indexes
ALTER TABLE events DROP INDEX idx_events_store_code;
-- (repeat for all added indexes)

-- Drop added columns
ALTER TABLE events DROP COLUMN updated_at;
ALTER TABLE events DROP COLUMN created_by;
ALTER TABLE events DROP COLUMN updated_by;
ALTER TABLE events DROP COLUMN is_active;

-- Restore from backup
TRUNCATE TABLE events;
INSERT INTO events SELECT * FROM events_backup_before_fixes_20260204;
*/

-- ============================================================================
-- END OF FIX SCRIPTS
-- ============================================================================

-- EXECUTION SUMMARY:
-- 1. ✅ Created backups
-- 2. ✅ Added indexes for performance
-- 3. ✅ Added audit columns
-- 4. ✅ Added NOT NULL constraints
-- 5. ✅ Added CHECK constraints
-- 6. ✅ Cleaned up data
-- 7. ✅ Added security columns
-- 8. ✅ Verified foreign keys
-- 9. ✅ Optimized tables
-- 10. ✅ Created useful views
-- 11. ✅ Verified changes

-- NEXT STEPS:
-- 1. Update application code to use new columns (is_active, updated_at, etc.)
-- 2. Implement BCrypt password hashing in Spring Security
-- 3. Update queries to use new views
-- 4. Monitor query performance
-- 5. Set up regular maintenance schedule

