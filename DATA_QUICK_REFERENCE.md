# 📋 DATA MANAGEMENT QUICK REFERENCE

## 🎯 TL;DR - How Data is Managed

**This project uses a 4-layer data management system:**

1. **MySQL Database** (Primary) - All transactional data
2. **Google Sheets** (Reference) - Configuration & master data
3. **File Storage** (Media) - Local folders or AWS S3
4. **In-Memory Cache** (Performance) - Frequently accessed summaries

---

## 📊 WHERE IS YOUR DATA?

### Production Environment

```
┌─────────────────────────────────────────┐
│ MySQL Database: selfie_prod             │
├─────────────────────────────────────────┤
│ ✓ Events (weddings, celebrations)       │
│ ✓ Attendees (customers registered)      │
│ ✓ Invitees (people invited)             │
│ ✓ Stores (locations & managers)         │
│ ✓ Users (ABM, RBM, CEE logins)          │
│ ✓ Bride details                         │
│ ✓ Greetings                             │
│ ✓ Rivaah collection data                │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ File Storage: /opt/tanishq/storage/     │
├─────────────────────────────────────────┤
│ ✓ Selfie images                         │
│ ✓ Bride uploads                         │
│ ✓ Event photos                          │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ AWS S3: celebrations-tanishq            │
├─────────────────────────────────────────┤
│ ✓ Event images/videos (organized by     │
│   event ID in folders)                  │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ Google Sheets: Multiple sheets          │
├─────────────────────────────────────────┤
│ ✓ Store master data                     │
│ ✓ Configuration settings                │
│ ✓ Reference data                        │
└─────────────────────────────────────────┘
```

---

## 🔄 HOW DATA FLOWS

### Writing Data (Creating/Updating)

```
User Input → REST API → Service → Repository → MySQL Database
                                              ↓
                                        Also saves to:
                                        - File system (images)
                                        - S3 (production media)
```

### Reading Data (Viewing)

```
Request → Check Cache → If cached: Return immediately
                     → If not: Query MySQL → Update cache → Return
```

---

## 💾 DATABASE QUICK ACCESS

### Connect to Database
```bash
# Production
mysql -u root -p
# Password: Nagaraj@07

# Pre-production
mysql -u root -p
# Password: Dechub#2025

# Local
mysql -u nagaraj_jadar -p
# Password: Nagaraj07
```

### Check Data
```sql
-- Use the right database
USE selfie_prod;  -- or selfie_preprod, or tanishq

-- See all tables
SHOW TABLES;

-- Count records in each table
SELECT 'events' as table_name, COUNT(*) as count FROM events
UNION ALL SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL SELECT 'stores', COUNT(*) FROM stores
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL SELECT 'greetings', COUNT(*) FROM greetings;

-- View recent events
SELECT id, event_name, start_date, region FROM events 
ORDER BY created_at DESC LIMIT 10;

-- View store details
SELECT store_code, store_name, region, rbm_username FROM stores 
LIMIT 10;
```

---

## 🗂️ DATABASE TABLES (15 Total)

### Core Tables
1. **events** - Event master data
2. **attendees** - Event participants
3. **invitees** - Event invitations
4. **stores** - Store locations

### User Management
5. **users** - System users
6. **abm_login** - Area Business Managers
7. **rbm_login** - Regional Business Managers
8. **cee_login** - Customer Engagement Executives
9. **password_history** - Password changes

### Customer Data
10. **bride_details** - Bride registrations
11. **user_details** - Customer information

### Collections
12. **rivaah** - Rivaah collection
13. **rivaah_user** - Rivaah customers

### Others
14. **greetings** - Personalized greetings
15. **product_detail** - Product checklists

---

## 📁 FILE STORAGE LOCATIONS

### Local Development
```
./storage/
├── selfie_images/      # Customer selfies
├── bride_images/       # Bride photos
└── bride_uploads/      # Bride content
    └── bride_images/
```

### Production Server
```
/opt/tanishq/storage/
├── selfie_images/
├── bride_uploads/
└── base.jpg           # Base template image
```

### AWS S3 (Production Only)
```
celebrations-tanishq/
└── events/
    └── {eventId}/
        ├── event_20260114_120000_1736849400123.jpg
        ├── event_20260114_120030_1736849430456.mp4
        └── ...
```

**S3 URL Format:**
```
https://celebrations-tanishq.s3.ap-south-1.amazonaws.com/events/{eventId}/{filename}
```

---

## 🔗 GOOGLE SHEETS REFERENCES

### Key Sheets (Production)

| Purpose | Sheet ID |
|---------|----------|
| User Details | 1vSG8T8rRm5jge_j-exRRvglRO6DEVBXH8UjqMaRQ_5w |
| Bride Details | 13C0M-v8tZQpDCXg09pufQ1f6kz2sQYE0wEIRB1-yIpk |
| Store Details | 1Y3ieu2Fz0ELcixqNaJa1KTvBOCP65B0lbbmP_edF_oQ |
| Events Details | 1ZKb4rqIon5HSdXNnwnYPNZA75Rh1vSBQoup7GCmaWcQ |
| Checklist Products | 1ZM3YEDlRI-Kbbt1CVN3qZlnMx2QizXJWTMiUO6Rgd-Y |
| Rivaah Details | 1tjb2cF6Ye0uIj51jtVHUKgNITJRey5i13Ew0GSwncVY |
| Greetings | 1EbbvXLIY6rVylXvlbfEgbZQUXPUuHoVlM9tuL-tFBDs |

**Access:** These are read for reference data, not primary storage

---

## 🔄 DATA MIGRATION COMMANDS

### Export Database
```bash
# Export production
mysqldump -u root -p selfie_prod > prod_backup_$(date +%Y%m%d_%H%M%S).sql

# Export pre-prod
mysqldump -u root -p selfie_preprod > preprod_backup_$(date +%Y%m%d_%H%M%S).sql
```

### Import Database
```bash
# Import to production
mysql -u root -p selfie_prod < backup_file.sql

# Import to pre-prod
mysql -u root -p selfie_preprod < backup_file.sql
```

### Copy Pre-Prod to Production
```bash
# One-command migration
mysqldump -u root -p selfie_preprod | mysql -u root -p selfie_prod
```

### Import CSV Files
```bash
# Upload CSV files to server first, then:
cd /opt/tanishq/csv/
bash import_csv_to_mysql.sh
```

---

## ⚙️ CONFIGURATION BY ENVIRONMENT

### Local Development
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tanishq
spring.datasource.username=nagaraj_jadar
spring.datasource.password=Nagaraj07
server.port=3000
# File storage: ./storage/
# S3: Not configured
```

### Pre-Production
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod
spring.datasource.username=root
spring.datasource.password=Dechub#2025
server.port=3000
# File storage: /opt/tanishq/storage/
# S3: celebrations-tanishq (enabled)
```

### Production
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_prod
spring.datasource.username=root
spring.datasource.password=Nagaraj@07
server.port=3001
# File storage: /opt/tanishq/storage/
# S3: celebrations-tanishq (enabled)
# Email: Enabled
# APIs: Titan appointment booking enabled
```

---

## 🎯 COMMON TASKS

### 1. View All Events
```sql
SELECT id, event_name, event_type, start_date, region, 
       invitees, attendees, completed_events_drive_link
FROM events 
ORDER BY start_date DESC;
```

### 2. Count Attendees for an Event
```sql
SELECT e.event_name, COUNT(a.id) as attendee_count
FROM events e
LEFT JOIN attendees a ON e.id = a.event_id
WHERE e.id = 'STORE123_uuid'
GROUP BY e.id;
```

### 3. List Stores by Region
```sql
SELECT store_code, store_name, region, rbm_username, cee_username
FROM stores
WHERE region = 'North1'
ORDER BY store_name;
```

### 4. Recent Bride Registrations
```sql
SELECT bride_name, email, phone, date, bride_type
FROM bride_details
ORDER BY date DESC
LIMIT 20;
```

### 5. User Activity (Login Types)
```sql
-- ABM users
SELECT username, region FROM abm_login;

-- RBM users
SELECT username, region FROM rbm_login;

-- CEE users  
SELECT username, store_code FROM cee_login;
```

---

## 🔍 TROUBLESHOOTING DATA ISSUES

### Issue: No data showing in UI
**Check:**
1. Database connection (credentials in application-{env}.properties)
2. Active profile (spring.profiles.active in application.properties)
3. Table existence (`SHOW TABLES;`)
4. Data presence (`SELECT COUNT(*) FROM events;`)

### Issue: File upload fails
**Check:**
1. Storage directory exists and is writable
2. File size within limit (100MB)
3. S3 credentials (production only)
4. Logs for error messages

### Issue: Slow performance
**Check:**
1. Cache status (StoreSummaryCache)
2. Database query logs (show-sql=true)
3. Number of records in tables
4. Scheduled job status

---

## 📊 DATA VOLUME LIMITS

```properties
# File uploads
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Database
# No hard limits set - MySQL can handle millions of records
# Current architecture supports typical event management scale

# Cache
# In-memory - size depends on number of RBM users
# Refreshes every 6 hours automatically
```

---

## 🔐 SECURITY NOTES

### Database Access
- **Production:** Only root user, password protected
- **Network:** Localhost only (not exposed externally)
- **Backups:** Regular backups recommended

### File Storage
- **Local:** Server file system permissions
- **S3:** IAM role-based access (no credentials in code)
- **Access:** Application has full read/write

### Google Sheets
- **Authentication:** Service account with P12 key file
- **Access:** Read-only for most sheets
- **Scope:** Limited to specific sheet IDs

---

## 📚 DOCUMENTATION REFERENCE

**Start here:**
- `DATA_MANAGEMENT_OVERVIEW.md` - Complete overview (this file's parent)
- `DATA_FLOW_DIAGRAM.md` - Visual architecture

**Migration guides:**
- `DATA_MIGRATION_GUIDE.md` - Database migration
- `CSV_IMPORT_QUICK_START.md` - CSV import
- `PRODUCTION_MIGRATION_GUIDE.md` - Production deployment

**Troubleshooting:**
- `FIX_TABLE_NOT_EXIST.md` - Table issues
- `TROUBLESHOOT_CSV_NOT_IMPORTING.md` - CSV problems
- `FIX_LOCAL_INFILE_ERROR.md` - Import errors

---

## 🚀 QUICK HEALTH CHECK

```bash
# Database connectivity
mysql -u root -p -e "SELECT VERSION();"

# Check application is running
curl http://localhost:3001/health  # Production
curl http://localhost:3000/health  # Pre-prod/Local

# Check file storage
ls -la /opt/tanishq/storage/  # Server
ls -la ./storage/             # Local

# Check recent logs
tail -f /opt/tanishq/logs/application.log  # Production
# or check console output if running in foreground

# Verify S3 access (production)
aws s3 ls s3://celebrations-tanishq/events/
```

---

## ⚡ PERFORMANCE TIPS

1. **Use the cache** - First request slow, subsequent fast
2. **Scheduled refresh** - Cache updates every 6 hours automatically
3. **Query optimization** - MySQL indexes on primary/foreign keys
4. **Hibernate** - Auto-generates optimized SQL
5. **S3 for media** - Offloads file serving from app server

---

## 🎯 KEY TAKEAWAYS

✅ **MySQL is the source of truth** for transactional data
✅ **Google Sheets** for configuration/reference only
✅ **S3** stores event media files (production)
✅ **Cache** improves performance dramatically
✅ **Migration scripts** available for data transfer
✅ **Environment-specific** configurations keep data separated

---

*For detailed information, see:*
- Full documentation: `DATA_MANAGEMENT_OVERVIEW.md`
- Architecture diagrams: `DATA_FLOW_DIAGRAM.md`

*Last Updated: January 14, 2026*

