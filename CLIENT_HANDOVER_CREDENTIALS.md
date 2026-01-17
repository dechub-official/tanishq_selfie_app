# CLIENT HANDOVER DOCUMENT

## DATABASE & APPLICATION SECURITY CREDENTIALS

**Date:** January 16, 2026  
**Project:** Tanishq Selfie Application - Production Deployment  
**Client:** [Client Organization Name]  
**Vendor:** Dechub Technologies

---

## 📋 EXECUTIVE SUMMARY

This document outlines the complete handover of credentials and access controls for your Tanishq Selfie Application production environment. As the client, you maintain **full control** over all sensitive data and credentials, while the vendor has limited access only for application support and maintenance.

---

## 🔐 CREDENTIALS UNDER CLIENT CONTROL

### 1. MySQL Database - Master Administrator Access

**Purpose:** Complete database administration and data access

| Item | Value | Security Level |
|------|-------|----------------|
| **Host** | localhost (Production Server) | HIGH |
| **Port** | 3306 | HIGH |
| **Database Name** | `selfie_prod` | HIGH |
| **Username** | `root` | **CRITICAL** |
| **Current Password** | `Nagaraj@07` | **CRITICAL** |
| **Recommended Action** | ⚠️ **CHANGE IMMEDIATELY** | **CRITICAL** |

**What You Can Do:**
- ✅ View ALL customer data
- ✅ Export/backup entire database
- ✅ Create/modify/delete any table
- ✅ Grant/revoke access for vendor
- ✅ Audit all database activities
- ✅ Restore from backups

**How to Change Root Password:**
```sql
-- Login to MySQL
mysql -u root -p

-- Change password
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourNewSecurePassword2026!';
FLUSH PRIVILEGES;
```

---

### 2. MySQL Database - Application User

**Purpose:** Used by the Java application to access database

| Item | Value | Security Level |
|------|-------|----------------|
| **Username** | `tanishq_app` | HIGH |
| **Password** | To be set by CLIENT | HIGH |
| **Privileges** | SELECT, INSERT, UPDATE, DELETE only | HIGH |
| **Data Access** | Application tables only | HIGH |

**Recommended Password:** Generate a strong 20+ character password

**Example:** `TnShq@App#2026$Pr0d!Sec`

**How to Set Password:**
```sql
-- As root user
mysql -u root -p

-- Set application password
ALTER USER 'tanishq_app'@'localhost' 
IDENTIFIED BY 'YourSecureApplicationPassword2026!';

FLUSH PRIVILEGES;
```

**⚠️ IMPORTANT:** 
- Share this password with vendor SECURELY (encrypted email/secure portal)
- Do NOT share via plain text email or WhatsApp
- Vendor needs this to configure the application
- Change password every 90 days

---

### 3. MySQL Database - Backup User

**Purpose:** For automated backups and data exports

| Item | Value | Security Level |
|------|-------|----------------|
| **Username** | `tanishq_backup` | MEDIUM |
| **Password** | To be set by CLIENT | HIGH |
| **Privileges** | SELECT and LOCK TABLES only | MEDIUM |
| **Can Modify Data** | ❌ No (Read-only) | MEDIUM |

**How to Use for Backups:**
```bash
# Daily backup script
mysqldump -u tanishq_backup -p selfie_prod > /backup/selfie_prod_$(date +%Y%m%d).sql
```

---

### 4. Server SSH Access - Root/Admin

**Purpose:** Full server administration

| Item | Value | Security Level |
|------|-------|----------------|
| **Server IP** | 10.10.63.97 | **CRITICAL** |
| **SSH Port** | 22 (default) | HIGH |
| **Username** | `root` or client admin user | **CRITICAL** |
| **Password/Key** | Your server admin password | **CRITICAL** |

**What You Can Do:**
- ✅ Access all files and directories
- ✅ Start/stop application services
- ✅ View all logs
- ✅ Modify system configuration
- ✅ Grant/revoke vendor SSH access
- ✅ Monitor system resources

**Recommended:** Create a dedicated admin user instead of using root directly

---

### 5. Application Configuration Files (Client Owned)

**Location:** `/opt/tanishq/secure/database.properties`

**Content:**
```properties
# DATABASE PASSWORD - CLIENT CONTROLLED
spring.datasource.password=YourSecureApplicationPassword2026!
```

**File Permissions:**
```bash
# Only root can read this file
sudo chown root:root /opt/tanishq/secure/database.properties
sudo chmod 600 /opt/tanishq/secure/database.properties
```

---

### 6. Google Cloud Service Account

**Purpose:** Access to Google Sheets for data sync

| Item | Value | Security Level |
|------|-------|----------------|
| **Service Account** | tanishq-app@tanishqgmb.iam.gserviceaccount.com | HIGH |
| **Key File** | tanishqgmb-5437243a8085.p12 | **CRITICAL** |
| **Location** | /opt/tanishq/ | HIGH |

**Recommended Action:**
- ✅ Verify you have backup of this key file
- ✅ Store backup in secure location
- ✅ Restrict file permissions: `chmod 600`

---

### 7. Web Application Admin Accounts

**Purpose:** Application administrative access

| Role | Username | Action Required |
|------|----------|-----------------|
| **Super Admin** | (Current admin users) | Change passwords |
| **Store Managers** | (Store admin users) | Audit access |
| **Regional Managers** | (RBM/ABM users) | Verify active users |

**Recommended Actions:**
1. Change all admin passwords immediately
2. Enable two-factor authentication (if available)
3. Review and disable unused accounts
4. Document who has access to what

---

## 🔧 VENDOR ACCESS (LIMITED)

### What Vendor Can Access:

#### 1. MySQL Monitoring User (tanishq_vendor)

**Capabilities:**
- ✅ Count total records in tables
- ✅ Check database health metrics
- ✅ View table structure (no data)
- ✅ Monitor performance

**Restrictions:**
- ❌ **CANNOT** view customer names, emails, phone numbers
- ❌ **CANNOT** view event details
- ❌ **CANNOT** export any data
- ❌ **CANNOT** modify data
- ❌ **CANNOT** delete records
- ❌ **CANNOT** drop or alter tables

**Example Query (Vendor Can Do):**
```sql
-- Count records only
SELECT COUNT(id) FROM events;  -- Returns: 150 (no data shown)

-- Check structure
DESCRIBE events;  -- Shows columns only, no data
```

**Example Query (Vendor CANNOT Do):**
```sql
-- View actual data - PERMISSION DENIED
SELECT * FROM attendees;  -- ❌ ERROR: Access denied
SELECT name, email FROM users;  -- ❌ ERROR: Access denied
```

---

#### 2. Server SSH Access (Limited User)

**Recommendation:** Create a limited SSH user for vendor

```bash
# Create vendor user
sudo useradd -m -s /bin/bash tanishq_vendor
sudo passwd tanishq_vendor

# Grant limited sudo access
sudo visudo
# Add: tanishq_vendor ALL=(ALL) /bin/systemctl restart tanishq-app
# Add: tanishq_vendor ALL=(ALL) /bin/tail -f /opt/tanishq/logs/*
```

**Vendor Can:**
- ✅ Restart application service
- ✅ View application logs
- ✅ Check application status
- ✅ Deploy updated WAR files (to designated directory)

**Vendor Cannot:**
- ❌ Access database directly
- ❌ View uploaded images/files
- ❌ Modify system configuration
- ❌ Access other services

---

#### 3. Application Logs Access

**Vendor Can View:**
- Application error logs
- Performance metrics
- System status

**Vendor Cannot View:**
- Database query results with customer data
- Uploaded images/files
- Backup files
- Configuration with passwords

---

## 🔒 SECURITY IMPLEMENTATION CHECKLIST

### Immediate Actions (Within 24 Hours):

- [ ] **CRITICAL:** Change MySQL root password
- [ ] **CRITICAL:** Set password for tanishq_app user
- [ ] **CRITICAL:** Share tanishq_app password with vendor securely
- [ ] **HIGH:** Create secure database.properties file
- [ ] **HIGH:** Set proper file permissions
- [ ] **HIGH:** Change all web application admin passwords
- [ ] **MEDIUM:** Create limited SSH user for vendor
- [ ] **MEDIUM:** Set up automated database backups

### Short Term (Within 1 Week):

- [ ] Enable MySQL audit logging
- [ ] Test vendor monitoring access
- [ ] Verify vendor cannot view customer data
- [ ] Document emergency access procedures
- [ ] Set up backup verification process
- [ ] Create incident response plan

### Ongoing (Monthly):

- [ ] Review database access logs
- [ ] Verify backup integrity
- [ ] Check for unauthorized access attempts
- [ ] Update passwords (quarterly)
- [ ] Audit active user accounts

---

## 📊 ACCESS MATRIX

| Resource | Client Access | Vendor Access | Application Access |
|----------|--------------|---------------|-------------------|
| **MySQL Root** | ✅ Full | ❌ None | ❌ None |
| **Customer Data** | ✅ Full | ❌ None | ✅ Read/Write (limited) |
| **Database Schema** | ✅ Modify | ❌ View only | ❌ None |
| **Application Logs** | ✅ Full | ✅ Read | ✅ Write |
| **Uploaded Files** | ✅ Full | ❌ None | ✅ Read/Write |
| **Server SSH** | ✅ Root | ✅ Limited user | N/A |
| **Backups** | ✅ Full | ❌ None | ❌ None |
| **Google Sheets** | ✅ Owner | ❌ None | ✅ Read/Write |

---

## 🆘 EMERGENCY PROCEDURES

### If You Suspect Unauthorized Access:

1. **Immediately change all passwords:**
   ```sql
   ALTER USER 'root'@'localhost' IDENTIFIED BY 'NewEmergencyPassword!';
   ALTER USER 'tanishq_app'@'localhost' IDENTIFIED BY 'NewAppPassword!';
   FLUSH PRIVILEGES;
   ```

2. **Revoke vendor access:**
   ```sql
   REVOKE ALL PRIVILEGES ON selfie_prod.* FROM 'tanishq_vendor'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Check audit logs:**
   ```bash
   sudo tail -100 /var/log/mysql/audit.log
   ```

4. **Contact vendor immediately**

---

### If Vendor Needs Temporary Full Access:

**Only grant when necessary for major troubleshooting/migration**

```sql
-- Grant temporary access
GRANT ALL PRIVILEGES ON selfie_prod.* TO 'tanishq_vendor'@'localhost';
FLUSH PRIVILEGES;

-- After work is complete (CLIENT must revoke)
REVOKE ALL PRIVILEGES ON selfie_prod.* FROM 'tanishq_vendor'@'localhost';
GRANT SELECT (id) ON selfie_prod.* TO 'tanishq_vendor'@'localhost';
FLUSH PRIVILEGES;
```

**Document:** Date, reason, duration, what was accessed

---

## 📞 SUPPORT CONTACTS

### For Security Issues:
- **Client IT Security:** [Your security team contact]
- **Vendor Support:** [Dechub support contact]
- **Escalation:** [Management contacts]

### For Regular Support:
- **Vendor Helpdesk:** [Support email/phone]
- **Application Issues:** [Vendor technical contact]

---

## 📝 PASSWORD MANAGEMENT BEST PRACTICES

### Strong Password Requirements:
- Minimum 16 characters
- Mix of uppercase, lowercase, numbers, special characters
- No dictionary words
- Not based on personal information
- Unique (not reused from other systems)

### Password Storage:
- Use enterprise password manager
- Encrypt stored passwords
- Never share via email/chat
- Use secure transfer methods only

### Password Rotation:
- MySQL root: Every 90 days
- Application user: Every 90 days
- Admin accounts: Every 60 days
- Vendor access: Every 90 days or when person changes

---

## ✅ HANDOVER SIGN-OFF

### Client Acknowledgment:

I acknowledge that I have received and understand:
- All master credentials for the production system
- Security responsibilities as the data owner
- Procedures for managing vendor access
- Emergency response procedures

**Client Representative:**  
Name: _________________________  
Title: _________________________  
Date: _________________________  
Signature: _____________________

---

### Vendor Acknowledgment:

I acknowledge that:
- Client maintains full control of all master credentials
- Vendor access is limited to monitoring and support only
- Vendor cannot and will not access customer data without explicit authorization
- All access is logged and auditable

**Vendor Representative:**  
Name: _________________________  
Company: Dechub Technologies  
Date: _________________________  
Signature: _____________________

---

## 📎 APPENDIX: QUICK REFERENCE

### Change Root Password:
```sql
mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY 'NewPassword';
FLUSH PRIVILEGES;
```

### Create Database Backup:
```bash
mysqldump -u root -p selfie_prod > backup_$(date +%Y%m%d).sql
```

### Restore Database:
```bash
mysql -u root -p selfie_prod < backup_20260116.sql
```

### Check Active Connections:
```sql
SHOW PROCESSLIST;
```

### View Vendor Permissions:
```sql
SHOW GRANTS FOR 'tanishq_vendor'@'localhost';
```

---

**Document Version:** 1.0  
**Created:** January 16, 2026  
**Next Review:** April 16, 2026  
**Classification:** CONFIDENTIAL - CLIENT ONLY

---

## 📌 IMPORTANT REMINDERS

1. **Never share root password with vendor**
2. **Keep backups in secure location**
3. **Review access logs monthly**
4. **Change passwords quarterly**
5. **Test backups monthly**
6. **Document all changes**
7. **Maintain audit trail**

---

**This document should be stored securely and only accessible to authorized client personnel.**

