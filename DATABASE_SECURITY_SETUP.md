# DATABASE SECURITY SETUP GUIDE

## Overview
This guide implements a **secure multi-tier access control** system where:
- ✅ **CLIENT** controls the master database credentials (root access)
- ✅ **VENDOR** has limited application-only access (no data viewing)
- ✅ **APPLICATION** runs with minimal required permissions

---

## SECURITY PRINCIPLE: LEAST PRIVILEGE ACCESS

### Access Levels:

1. **CLIENT (Database Administrator)** - Full Control
   - Root MySQL access
   - Can view all data
   - Can backup/restore database
   - Can grant/revoke vendor access
   - Can audit all activities

2. **VENDOR (Support & Maintenance)** - Limited Access
   - Application management only
   - Cannot view sensitive customer data
   - Can check application logs
   - Can restart services
   - Can deploy updates

3. **APPLICATION USER** - Operational Access
   - Read/Write to specific tables only
   - Cannot drop tables or database
   - Cannot view other databases
   - Limited to application operations

---

## STEP 1: CREATE SECURE DATABASE USERS

### On Production Server (Client executes this)

```bash
# Login as root (CLIENT ONLY)
mysql -u root -p
```

**Enter the root password that CLIENT controls**

### Execute Security Setup SQL:

```sql
-- ================================================
-- DATABASE SECURITY CONFIGURATION
-- Execute Date: January 16, 2026
-- Purpose: Implement role-based access control
-- ================================================

USE selfie_prod;

-- ================================================
-- 1. CREATE APPLICATION USER (Limited Privileges)
-- ================================================
-- This user is used by the Java application only
-- Password: Generate a strong password for client

DROP USER IF EXISTS 'tanishq_app'@'localhost';
CREATE USER 'tanishq_app'@'localhost' 
IDENTIFIED BY 'ClientToSetSecurePassword2026!';

-- Grant ONLY necessary privileges for application operations
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

-- NO DROP, CREATE, or ALTER privileges - Application cannot modify schema
-- NO GRANT privilege - Application cannot give access to others

-- ================================================
-- 2. CREATE VENDOR MONITORING USER (Read-Only Stats)
-- ================================================
-- This user allows vendor to check health metrics only
-- NO access to actual customer data

DROP USER IF EXISTS 'tanishq_vendor'@'localhost';
CREATE USER 'tanishq_vendor'@'localhost' 
IDENTIFIED BY 'VendorMonitor2026!';

-- Grant only COUNT and table status checks (no data viewing)
GRANT SELECT (id) ON selfie_prod.events TO 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.attendees TO 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.invitees TO 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.users TO 'tanishq_vendor'@'localhost';

-- Allow checking table structure (for troubleshooting)
GRANT SHOW VIEW ON selfie_prod.* TO 'tanishq_vendor'@'localhost';

-- NO access to sensitive data columns
-- Can only count records and check database health

-- ================================================
-- 3. CREATE BACKUP USER (Client's Backup Operations)
-- ================================================
DROP USER IF EXISTS 'tanishq_backup'@'localhost';
CREATE USER 'tanishq_backup'@'localhost' 
IDENTIFIED BY 'ClientBackupPassword2026!';

-- Full read access for backup purposes
GRANT SELECT, LOCK TABLES ON selfie_prod.* TO 'tanishq_backup'@'localhost';
GRANT RELOAD ON *.* TO 'tanishq_backup'@'localhost';

-- CLIENT controls this user for data exports

-- ================================================
-- 4. APPLY CHANGES
-- ================================================
FLUSH PRIVILEGES;

-- ================================================
-- 5. VERIFY USER SETUP
-- ================================================
SELECT 
    User, 
    Host, 
    Select_priv, 
    Insert_priv, 
    Update_priv, 
    Delete_priv,
    Create_priv,
    Drop_priv,
    Grant_priv
FROM mysql.user 
WHERE User LIKE 'tanishq%';

-- ================================================
-- SECURITY VERIFICATION
-- ================================================
SHOW GRANTS FOR 'tanishq_app'@'localhost';
SHOW GRANTS FOR 'tanishq_vendor'@'localhost';
SHOW GRANTS FOR 'tanishq_backup'@'localhost';
```

---

## STEP 2: UPDATE APPLICATION CONFIGURATION

### Update application-prod.properties

**VENDOR ACTION:** Update the application to use the new limited user:

```properties
# PRODUCTION DATABASE - Application User (Limited Access)
# PASSWORD MUST BE PROVIDED BY CLIENT
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_prod
spring.datasource.username=tanishq_app
spring.datasource.password=${DB_PASSWORD}

# IMPORTANT: Password is provided by client via environment variable
# DO NOT hardcode client's password in source code
```

---

## STEP 3: SECURE PASSWORD MANAGEMENT

### Option A: Environment Variable (Recommended)

**On Production Server:**

```bash
# CLIENT creates a secure configuration file
sudo nano /opt/tanishq/db.config

# Add this line (CLIENT sets the password)
export DB_PASSWORD='ClientToSetSecurePassword2026!'

# Secure the file
sudo chmod 600 /opt/tanishq/db.config
sudo chown root:root /opt/tanishq/db.config
```

**Update application startup script:**

```bash
#!/bin/bash
# Start application with secure password

# Load database password (CLIENT controlled)
source /opt/tanishq/db.config

# Start application
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  --spring.datasource.password=${DB_PASSWORD} \
  > /opt/tanishq/logs/application.log 2>&1 &

echo $! > /opt/tanishq/tanishq-prod.pid
```

### Option B: External Properties File (Client Controlled)

```bash
# CLIENT creates secure properties file
sudo nano /opt/tanishq/secure/database.properties
```

**Content:**
```properties
spring.datasource.password=ClientToSetSecurePassword2026!
```

**Secure it:**
```bash
sudo chmod 600 /opt/tanishq/secure/database.properties
sudo chown root:root /opt/tanishq/secure/database.properties
```

**Update startup:**
```bash
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  --spring.config.additional-location=/opt/tanishq/secure/database.properties \
  > /opt/tanishq/logs/application.log 2>&1 &
```

---

## STEP 4: VENDOR MONITORING CAPABILITIES

### What Vendor CAN DO (With tanishq_vendor user):

```bash
# Login for monitoring
mysql -u tanishq_vendor -p
```

**Check database health (NO DATA VIEWING):**

```sql
USE selfie_prod;

-- Count total records (no data shown)
SELECT 
    'events' as table_name, 
    COUNT(id) as total_records 
FROM events
UNION ALL
SELECT 'attendees', COUNT(id) FROM attendees
UNION ALL
SELECT 'invitees', COUNT(id) FROM invitees
UNION ALL
SELECT 'bride_details', COUNT(id) FROM bride_details
UNION ALL
SELECT 'users', COUNT(id) FROM users;

-- Check table structure (for troubleshooting)
DESCRIBE events;
SHOW TABLE STATUS FROM selfie_prod;

-- Check indexes and performance
SHOW INDEX FROM events;
```

**What Vendor CANNOT DO:**
```sql
-- ❌ CANNOT view actual customer data
SELECT * FROM attendees;  -- PERMISSION DENIED

-- ❌ CANNOT see sensitive columns
SELECT name, email, phone FROM users;  -- PERMISSION DENIED

-- ❌ CANNOT delete data
DELETE FROM events;  -- PERMISSION DENIED

-- ❌ CANNOT modify schema
DROP TABLE events;  -- PERMISSION DENIED
ALTER TABLE events ADD COLUMN test VARCHAR(50);  -- PERMISSION DENIED

-- ❌ CANNOT grant access to others
GRANT ALL ON selfie_prod.* TO 'hacker'@'localhost';  -- PERMISSION DENIED
```

---

## STEP 5: CLIENT DATABASE ADMINISTRATION

### What CLIENT Controls (Root Access):

```bash
# CLIENT logs in with root credentials
mysql -u root -p
```

**Full control operations:**

```sql
-- View all data
SELECT * FROM selfie_prod.attendees;

-- Backup database
-- Run from bash:
mysqldump -u root -p selfie_prod > /backup/selfie_prod_$(date +%Y%m%d).sql

-- Restore database
mysql -u root -p selfie_prod < /backup/selfie_prod_20260116.sql

-- Audit vendor access
SHOW GRANTS FOR 'tanishq_vendor'@'localhost';

-- Revoke vendor access if needed
REVOKE ALL PRIVILEGES ON selfie_prod.* FROM 'tanishq_vendor'@'localhost';

-- Monitor connections
SHOW PROCESSLIST;

-- Check audit logs
SELECT * FROM mysql.general_log WHERE user_host LIKE '%tanishq%';
```

---

## STEP 6: ENABLE MYSQL AUDIT LOGGING

**CLIENT enables audit logging to track all database access:**

```bash
# Edit MySQL configuration
sudo nano /etc/my.cnf
```

**Add these lines:**
```ini
[mysqld]
# Enable general query log for auditing
general_log = 1
general_log_file = /var/log/mysql/audit.log

# Enable slow query log
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
```

**Restart MySQL:**
```bash
sudo systemctl restart mysqld
```

**CLIENT can now review all queries:**
```bash
tail -f /var/log/mysql/audit.log
```

---

## STEP 7: NETWORK SECURITY

### Restrict Database Access to Localhost Only

**CLIENT configures MySQL:**

```bash
sudo nano /etc/my.cnf
```

**Add:**
```ini
[mysqld]
bind-address = 127.0.0.1
skip-networking = 0
```

This ensures:
- Database only accessible from same server
- Vendor cannot connect remotely
- All access must be through application

---

## STEP 8: FILE SYSTEM PERMISSIONS

### Secure Application Files

**CLIENT sets proper ownership:**

```bash
# Application files owned by client
sudo chown -R root:root /opt/tanishq/

# Application can only read WAR file
sudo chmod 644 /opt/tanishq/*.war

# Logs directory writable by application
sudo chown -R tanishqapp:tanishqapp /opt/tanishq/logs/
sudo chmod 755 /opt/tanishq/logs/

# Storage directory for uploads
sudo chown -R tanishqapp:tanishqapp /opt/tanishq/storage/
sudo chmod 755 /opt/tanishq/storage/

# Database config only readable by root
sudo chmod 600 /opt/tanishq/db.config
```

---

## SECURITY CHECKLIST

### Client Verification:

- [ ] Root MySQL password known only to client
- [ ] Application user password stored securely (not in source code)
- [ ] Vendor user has read-only monitoring access
- [ ] Audit logging enabled
- [ ] Database bound to localhost only
- [ ] File permissions properly set
- [ ] Backup user created for client's use
- [ ] Application running with non-root OS user
- [ ] All passwords are strong (16+ characters)
- [ ] Regular backups scheduled

### Vendor Verification:

- [ ] Application updated to use tanishq_app user
- [ ] Password loaded from environment variable
- [ ] No hardcoded passwords in source code
- [ ] Can monitor database health metrics
- [ ] Cannot view customer data
- [ ] Documentation provided to client
- [ ] Handover process completed

---

## EMERGENCY ACCESS

### If Vendor Needs Temporary Full Access

**CLIENT can grant temporary access:**

```sql
-- Grant temporary full access (during migration/troubleshooting)
GRANT ALL PRIVILEGES ON selfie_prod.* TO 'tanishq_vendor'@'localhost';
FLUSH PRIVILEGES;

-- After work is complete, revoke
REVOKE ALL PRIVILEGES ON selfie_prod.* FROM 'tanishq_vendor'@'localhost';

-- Restore limited access
GRANT SELECT (id) ON selfie_prod.* TO 'tanishq_vendor'@'localhost';
FLUSH PRIVILEGES;
```

---

## PASSWORD ROTATION POLICY

### Regular Password Changes (Client Responsibility)

**Every 90 days, CLIENT should:**

```sql
-- Change application user password
ALTER USER 'tanishq_app'@'localhost' 
IDENTIFIED BY 'NewSecurePassword2026!';

FLUSH PRIVILEGES;
```

**Then update the secure config file:**
```bash
sudo nano /opt/tanishq/db.config
# Update DB_PASSWORD value

# Restart application
sudo systemctl restart tanishq-app
```

---

## MONITORING & ALERTS

### CLIENT can set up monitoring:

```sql
-- Create monitoring view (client only)
CREATE VIEW db_access_summary AS
SELECT 
    user,
    host,
    db,
    command,
    time,
    state,
    info
FROM information_schema.processlist
WHERE db = 'selfie_prod';

-- Check current connections
SELECT * FROM db_access_summary;
```

---

## COMPLIANCE NOTES

This setup ensures:
- ✅ **Data Privacy**: Vendor cannot view customer PII
- ✅ **Audit Trail**: All database access is logged
- ✅ **Least Privilege**: Each user has minimum required access
- ✅ **Client Control**: Client owns all master credentials
- ✅ **Accountability**: Clear separation of responsibilities
- ✅ **GDPR Compliance**: Data access is restricted and auditable
- ✅ **Industry Standards**: Follows security best practices

---

## SUMMARY

| User | Purpose | Can View Data | Can Modify Schema | Controlled By |
|------|---------|--------------|-------------------|---------------|
| `root` | Master Admin | ✅ Yes | ✅ Yes | **CLIENT** |
| `tanishq_app` | Application | ✅ Yes (limited) | ❌ No | CLIENT (password) |
| `tanishq_vendor` | Monitoring | ❌ No (only counts) | ❌ No | Vendor (monitoring) |
| `tanishq_backup` | Backups | ✅ Yes (read-only) | ❌ No | **CLIENT** |

---

## NEXT STEPS

1. **CLIENT** executes STEP 1 to create secure users
2. **CLIENT** provides `tanishq_app` password to vendor securely
3. **VENDOR** updates application configuration (STEP 2)
4. **VENDOR** removes all hardcoded passwords
5. **CLIENT** verifies access restrictions
6. **BOTH** sign off on security implementation

---

**Document Version:** 1.0  
**Last Updated:** January 16, 2026  
**Review Date:** April 16, 2026 (90 days)

