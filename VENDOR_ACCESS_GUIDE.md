# VENDOR ACCESS & RESPONSIBILITIES DOCUMENT

## FOR: Dechub Technologies (Vendor/Support Team)

**Date:** January 16, 2026  
**Project:** Tanishq Selfie Application - Production Support  
**Client:** [Client Organization Name]

---

## 🎯 OVERVIEW: SECURE VENDOR ACCESS MODEL

As the vendor/support partner, your access has been intentionally **limited** to ensure client data security and privacy. This document outlines:

✅ What you CAN access  
❌ What you CANNOT access  
🔧 How to perform your support duties  
📋 Responsibilities and compliance

---

## 🔐 YOUR ACCESS CREDENTIALS

### 1. MySQL Monitoring Access (Read-Only)

**Username:** `tanishq_vendor`  
**Password:** `VendorMonitor2026!` *(Client will provide actual password)*  
**Database:** `selfie_prod`  
**Host:** `localhost` (on production server)

**Purpose:** Health monitoring and diagnostics only

---

### 2. SSH Server Access (Limited)

**Server:** 10.10.63.97  
**Username:** `tanishq_vendor` *(Client creates this limited user)*  
**Authentication:** Password or SSH key (client provides)

**Purpose:** Application management and log access only

---

## ✅ WHAT YOU CAN DO

### Database Monitoring Capabilities:

```sql
-- ✅ Count total records (monitoring database health)
SELECT COUNT(*) FROM events;
SELECT COUNT(*) FROM attendees;
SELECT COUNT(*) FROM invitees;

-- ✅ Check table structure (for troubleshooting schema issues)
DESCRIBE events;
SHOW TABLE STATUS FROM selfie_prod;

-- ✅ Monitor database performance
SHOW INDEX FROM events;
SHOW TABLE STATUS WHERE Name = 'events';

-- ✅ Check database size
SELECT 
    table_schema AS 'Database',
    SUM(data_length + index_length) / 1024 / 1024 AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'selfie_prod'
GROUP BY table_schema;

-- ✅ Verify table existence
SHOW TABLES FROM selfie_prod;

-- ✅ Get record count summary
SELECT 
    'events' as table_name, 
    COUNT(id) as count 
FROM events
UNION ALL
SELECT 'attendees', COUNT(id) FROM attendees
UNION ALL
SELECT 'invitees', COUNT(id) FROM invitees
UNION ALL
SELECT 'bride_details', COUNT(id) FROM bride_details
UNION ALL
SELECT 'users', COUNT(id) FROM users;
```

### Application Management:

```bash
# ✅ Check application status
systemctl status tanishq-app
ps aux | grep tanishq

# ✅ View application logs
tail -f /opt/tanishq/logs/application.log
grep ERROR /opt/tanishq/logs/application.log

# ✅ Check application process
cat /opt/tanishq/tanishq-prod.pid
ps -p $(cat /opt/tanishq/tanishq-prod.pid)

# ✅ Monitor disk space
df -h /opt/tanishq

# ✅ Check memory usage
free -h

# ✅ View Java process details
jps -v
```

### Service Management:

```bash
# ✅ Restart application (if sudo access granted)
sudo systemctl restart tanishq-app

# ✅ Stop application (if sudo access granted)
sudo systemctl stop tanishq-app

# ✅ Start application (if sudo access granted)
sudo systemctl start tanishq-app
```

---

## ❌ WHAT YOU CANNOT DO

### Database Restrictions:

```sql
-- ❌ CANNOT view customer personal data
SELECT * FROM attendees;  
-- ERROR: SELECT command denied

-- ❌ CANNOT view specific customer columns
SELECT name, email, phone FROM users;
-- ERROR: SELECT command denied for columns

-- ❌ CANNOT export data
SELECT * INTO OUTFILE '/tmp/data.csv' FROM events;
-- ERROR: Access denied

-- ❌ CANNOT modify any data
UPDATE events SET event_name = 'test';
-- ERROR: UPDATE command denied

DELETE FROM attendees;
-- ERROR: DELETE command denied

INSERT INTO users VALUES (...);
-- ERROR: INSERT command denied

-- ❌ CANNOT drop or alter tables
DROP TABLE events;
-- ERROR: DROP command denied

ALTER TABLE events ADD COLUMN test VARCHAR(50);
-- ERROR: ALTER command denied

-- ❌ CANNOT create new tables
CREATE TABLE test_table (id INT);
-- ERROR: CREATE command denied

-- ❌ CANNOT grant access to others
GRANT ALL ON selfie_prod.* TO 'another_user'@'localhost';
-- ERROR: GRANT command denied

-- ❌ CANNOT view other databases
USE mysql;
-- ERROR: Access denied to database 'mysql'
```

### Server/File Restrictions:

```bash
# ❌ CANNOT access customer uploaded files
ls /opt/tanishq/storage/bride_images/
# ERROR: Permission denied

cat /opt/tanishq/storage/selfie_images/customer_photo.jpg
# ERROR: Permission denied

# ❌ CANNOT access database backup files
ls /backup/
# ERROR: Permission denied

# ❌ CANNOT access configuration with passwords
cat /opt/tanishq/secure/database.properties
# ERROR: Permission denied

# ❌ CANNOT modify system configuration
sudo nano /etc/my.cnf
# ERROR: Permission denied (unless explicitly granted)

# ❌ CANNOT access MySQL data directory
ls /var/lib/mysql/
# ERROR: Permission denied
```

---

## 🔧 HOW TO PERFORM COMMON SUPPORT TASKS

### Task 1: Check Application Health

```bash
# Step 1: SSH to server
ssh tanishq_vendor@10.10.63.97

# Step 2: Check if application is running
ps aux | grep tanishq
systemctl status tanishq-app

# Step 3: Check recent errors
tail -50 /opt/tanishq/logs/application.log | grep ERROR

# Step 4: Check database connectivity
mysql -u tanishq_vendor -p -e "SELECT COUNT(*) as total_events FROM selfie_prod.events;"
```

---

### Task 2: Troubleshoot Application Errors

```bash
# Step 1: Review error logs
tail -100 /opt/tanishq/logs/application.log | grep -A 5 ERROR

# Step 2: Check for common issues
# - Database connection errors
# - File upload errors
# - Memory issues

# Step 3: Check database health
mysql -u tanishq_vendor -p -e "SHOW TABLE STATUS FROM selfie_prod;"

# Step 4: If issue requires data access
# → Escalate to client
# → Request temporary elevated access if necessary
```

---

### Task 3: Deploy Application Update

```bash
# Step 1: Upload new WAR file
scp tanishq-app-new-version.war tanishq_vendor@10.10.63.97:/tmp/

# Step 2: SSH to server
ssh tanishq_vendor@10.10.63.97

# Step 3: Stop application (requires sudo)
sudo systemctl stop tanishq-app

# Step 4: Backup current WAR
sudo cp /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
        /opt/tanishq/backup/tanishq-backup-$(date +%Y%m%d).war

# Step 5: Deploy new WAR
sudo cp /tmp/tanishq-app-new-version.war \
        /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war

# Step 6: Start application
sudo systemctl start tanishq-app

# Step 7: Verify startup
tail -f /opt/tanishq/logs/application.log
```

---

### Task 4: Monitor Database Performance

```bash
# Login to MySQL
mysql -u tanishq_vendor -p

# Check database size
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS "Size (MB)"
FROM information_schema.TABLES
WHERE table_schema = "selfie_prod"
ORDER BY (data_length + index_length) DESC;

# Check table counts
SELECT 'events' as table_name, COUNT(id) as count FROM selfie_prod.events
UNION ALL
SELECT 'attendees', COUNT(id) FROM selfie_prod.attendees
UNION ALL
SELECT 'invitees', COUNT(id) FROM selfie_prod.invitees;

# Check index usage
SHOW INDEX FROM selfie_prod.events;
```

---

### Task 5: Generate Health Report for Client

```bash
#!/bin/bash
# health_report.sh - Generate system health report

echo "==================================="
echo "TANISHQ APPLICATION HEALTH REPORT"
echo "Date: $(date)"
echo "==================================="
echo ""

echo "1. APPLICATION STATUS:"
systemctl status tanishq-app | head -3
echo ""

echo "2. DISK USAGE:"
df -h /opt/tanishq
echo ""

echo "3. MEMORY USAGE:"
free -h
echo ""

echo "4. APPLICATION PROCESS:"
ps aux | grep tanishq | head -1
echo ""

echo "5. RECENT ERRORS:"
grep ERROR /opt/tanishq/logs/application.log | tail -5
echo ""

echo "6. DATABASE RECORD COUNTS:"
mysql -u tanishq_vendor -p -e "
SELECT 'events' as table_name, COUNT(id) as count FROM selfie_prod.events
UNION ALL SELECT 'attendees', COUNT(id) FROM selfie_prod.attendees
UNION ALL SELECT 'invitees', COUNT(id) FROM selfie_prod.invitees
UNION ALL SELECT 'users', COUNT(id) FROM selfie_prod.users;
"

echo ""
echo "==================================="
echo "Report Generated Successfully"
echo "==================================="
```

---

## 📋 VENDOR RESPONSIBILITIES

### Security Compliance:

1. **Never attempt to access customer data**
   - Do not try to bypass access restrictions
   - Do not request root database credentials
   - Do not attempt to view uploaded customer images

2. **Use access only for legitimate support**
   - Monitoring application health
   - Troubleshooting technical issues
   - Deploying approved updates

3. **Report security concerns immediately**
   - If you discover a security vulnerability
   - If you suspect unauthorized access
   - If you notice unusual activity

4. **Maintain confidentiality**
   - Do not share access credentials
   - Do not discuss client deployment details publicly
   - Follow NDA agreements

---

### Support Best Practices:

1. **Document all actions**
   - Log when you access the system
   - Document changes made
   - Maintain change logs

2. **Request elevated access when needed**
   - If troubleshooting requires data access
   - If schema changes are needed
   - Document reason and duration

3. **Coordinate with client**
   - Schedule maintenance windows
   - Notify before making changes
   - Provide post-deployment reports

4. **Test before deploying**
   - Always test in pre-prod first
   - Validate WAR files before deployment
   - Have rollback plan ready

---

## 🆘 REQUESTING TEMPORARY ELEVATED ACCESS

### When You Might Need Full Access:

- Major database migration
- Schema modifications
- Complex data troubleshooting
- Emergency data recovery

### How to Request:

**Email Template to Client:**

```
Subject: Request for Temporary Database Access - [Reason]

Dear [Client Contact],

I am requesting temporary elevated database access for the following reason:

Reason: [e.g., Troubleshoot data inconsistency in events table]
Duration Needed: [e.g., 2 hours on January 16, 2026, 10 AM - 12 PM]
Access Required: [e.g., SELECT access to events and attendees tables]
Justification: [e.g., Need to verify data relationships for debugging]

I will:
- Only access data necessary for the task
- Document all queries executed
- Provide summary report after completion
- Return to limited access immediately after

Please let me know if you approve this request.

Best regards,
[Your Name]
Dechub Technologies
```

### Client Grants Temporary Access:

```sql
-- Client executes this
GRANT SELECT ON selfie_prod.events TO 'tanishq_vendor'@'localhost';
GRANT SELECT ON selfie_prod.attendees TO 'tanishq_vendor'@'localhost';
FLUSH PRIVILEGES;

-- After work is complete, client revokes
REVOKE SELECT ON selfie_prod.events FROM 'tanishq_vendor'@'localhost';
REVOKE SELECT ON selfie_prod.attendees FROM 'tanishq_vendor'@'localhost';
FLUSH PRIVILEGES;
```

---

## 📊 MONITORING DASHBOARD (What You Can See)

### Daily Health Checks:

| Metric | How to Check | Normal Range |
|--------|--------------|--------------|
| **Application Status** | `systemctl status tanishq-app` | Active (running) |
| **Memory Usage** | `free -h` | < 80% |
| **Disk Space** | `df -h /opt/tanishq` | < 80% |
| **Database Connection** | `mysql -u tanishq_vendor -p -e "SELECT 1"` | Success |
| **Total Events** | `SELECT COUNT(id) FROM events` | Growing daily |
| **Log Errors** | `grep ERROR logs/application.log` | < 5 per hour |
| **Response Time** | `curl -o /dev/null -s -w '%{time_total}' http://localhost:3000` | < 2 seconds |

---

## 🔍 AUDIT TRAIL

**All your activities are logged:**

- Database queries you execute
- Files you access
- Commands you run
- Login/logout times

**Client can review your activities at any time:**

```sql
-- Client checks your database activity
SELECT * FROM mysql.general_log 
WHERE user_host LIKE '%tanishq_vendor%';
```

**Be professional and responsible with your access.**

---

## 📞 ESCALATION PROCEDURES

### For Issues Beyond Your Access:

1. **Data-related issues** → Contact client database administrator
2. **Schema changes needed** → Submit change request to client
3. **Customer data inquiries** → Direct to client
4. **Security concerns** → Immediately notify client and your manager

### Contact Information:

- **Client IT Contact:** [Provided by client]
- **Dechub Manager:** [Your manager]
- **Emergency:** [Emergency contact]

---

## ✅ ACKNOWLEDGMENT & COMPLIANCE

### By using this access, you agree to:

- [ ] Only use access for legitimate support purposes
- [ ] Not attempt to access customer personal data
- [ ] Not share credentials with others
- [ ] Follow all security policies
- [ ] Document all actions taken
- [ ] Request elevated access when needed
- [ ] Maintain confidentiality
- [ ] Report security concerns immediately

**Vendor Representative:**  
Name: _________________________  
Date: _________________________  
Signature: _____________________

---

## 📎 QUICK REFERENCE COMMANDS

### Check Application:
```bash
systemctl status tanishq-app
tail -f /opt/tanishq/logs/application.log
```

### Check Database Health:
```bash
mysql -u tanishq_vendor -p -e "
SELECT 'events' as table, COUNT(id) as count FROM selfie_prod.events
UNION ALL
SELECT 'attendees', COUNT(id) FROM selfie_prod.attendees;"
```

### Restart Application:
```bash
sudo systemctl restart tanishq-app
```

### View Recent Errors:
```bash
tail -50 /opt/tanishq/logs/application.log | grep ERROR
```

---

## 📚 ADDITIONAL RESOURCES

- **Technical Documentation:** [Link to technical docs]
- **API Documentation:** [Link to API docs]
- **Deployment Guide:** See `COMPLETE_DEPLOYMENT_GUIDE.md`
- **Troubleshooting:** See `TROUBLESHOOTING.md`

---

**Document Version:** 1.0  
**Created:** January 16, 2026  
**Classification:** INTERNAL - VENDOR USE ONLY

---

## Remember: With limited access comes great responsibility!

Your role is to **support the application**, not access customer data.  
When in doubt, **ask the client** before proceeding.

