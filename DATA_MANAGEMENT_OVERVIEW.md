# 📊 DATA MANAGEMENT OVERVIEW - Tanishq Selfie Application

## 🎯 Executive Summary

This **Tanishq Celebrations Event Management System** manages data through a **hybrid architecture** combining:
- **MySQL Database** (Primary storage)
- **Google Sheets Integration** (Reference data & configuration)
- **Local/Cloud File Storage** (Images & media files)
- **AWS S3** (Production media storage)
- **Scheduled Data Synchronization** (Caching for performance)

---

## 🗄️ DATABASE ARCHITECTURE

### Primary Database: MySQL 8

**Database Names by Environment:**
- **Local:** `tanishq` (localhost:3306)
- **Pre-Production:** `selfie_preprod` (localhost:3306)
- **Production:** `selfie_prod` (localhost:3306)

### Database Tables & Entities

#### 1. **Events Management**
- `events` - Event master data (weddings, celebrations, store events)
- `attendees` - Event participants/customers
- `invitees` - Event invitations sent

#### 2. **User Management**
- `users` - System users (ABM, RBM, CEE roles)
- `abm_login` - Area Business Manager credentials
- `rbm_login` - Regional Business Manager credentials
- `cee_login` - Customer Engagement Executive credentials
- `password_history` - Password change tracking

#### 3. **Store Management**
- `stores` - Tanishq store details (locations, managers, regions)

#### 4. **Customer Data**
- `bride_details` - Bride registration & preferences
- `user_details` - Customer information

#### 5. **Rivaah (Wedding Collection)**
- `rivaah` - Rivaah collection details
- `rivaah_user` - Rivaah customer data

#### 6. **Greetings Module**
- `greetings` - Personalized greetings with QR codes

#### 7. **Product Information**
- `product_detail` - Checklist products

---

## 🔄 DATA FLOW & SOURCES

### 1. **MySQL Database** (Primary Storage)
**Purpose:** Transactional data, user-generated content

**Data Flow:**
```
User Input (Web/API)
    ↓
Spring Boot Controllers
    ↓
JPA Repositories
    ↓
MySQL Database (CRUD Operations)
    ↓
Hibernate ORM (ddl-auto=update)
```

**Configuration:**
```properties
# Auto-creates/updates tables based on Entity classes
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 2. **Google Sheets Integration** (Reference Data)
**Purpose:** Configuration data, external references, master data sync

**Active Google Sheets:**
```properties
# User Details
google.sheet.dechub.user.details.id=1vSG8T8rRm5jge_j-exRRvglRO6DEVBXH8UjqMaRQ_5w

# Bride Details (varies by environment)
google.sheet.dechub.bride.details.id=13C0M-v8tZQpDCXg09pufQ1f6kz2sQYE0wEIRB1-yIpk

# Store Details
google.sheet.dechub.store.details.id=1Y3ieu2Fz0ELcixqNaJa1KTvBOCP65B0lbbmP_edF_oQ

# Events Configuration
google.sheet.dechub.events.details.id=1ZKb4rqIon5HSdXNnwnYPNZA75Rh1vSBQoup7GCmaWcQ
google.sheet.dechub.events_attendees.details.id=1rXq_zS0dj0pofs_wzlDfpl5rXVYDIN0fs9Qb9TgMXYU
google.sheet.dechub.events_invitees.details.id=1D4R7minvW2rke4LQfO70PemRDQZYI92x63EYFf1p9b0

# Products & Checklist
google.sheet.dechub.checklist_products.id=1ZM3YEDlRI-Kbbt1CVN3qZlnMx2QizXJWTMiUO6Rgd-Y

# Rivaah Collection
google.sheet.dechub.rivaah.details.id=1tjb2cF6Ye0uIj51jtVHUKgNITJRey5i13Ew0GSwncVY
google.sheet.dechub.rivaah.user.details.id=186XwrPKGhaaFMmN5q7doT5lXxbdKVrk8zI_Bs45N0sg

# Credentials & Authentication
google.sheet.dechub.events.credential.sheet.id=16auf7HZxT6RZmi3pShvds_-FttAg2vyKLjb9esnrLRw

# Greetings
dechub.tanishq.greeting.sheet.id=1EbbvXLIY6rVylXvlbfEgbZQUXPUuHoVlM9tuL-tFBDs
```

**Authentication:**
- Service Account: `tanishq-app@tanishqgmb.iam.gserviceaccount.com`
- Key File: `tanishqgmb-5437243a8085.p12`

### 3. **File Storage** (Images & Media)

#### **Local Environment (Development)**
```properties
local.storage.base.path=./storage
local.storage.base.url=http://localhost:3000/storage

# Directories
selfie.upload.dir=./storage/selfie_images
dechub.bride.upload.dir=./storage/bride_uploads
dechub.base.image=./storage/base.jpg
```

**Local Storage Structure:**
```
storage/
├── selfie_images/          # Customer selfies
├── bride_images/           # Bride photos
└── bride_uploads/          # Uploaded bride content
    └── bride_images/
```

#### **Production Environment (Server)**
```properties
# Server paths
selfie.upload.dir=/opt/tanishq/storage/selfie_images
dechub.bride.upload.dir=/opt/tanishq/storage/bride_uploads
```

#### **AWS S3 (Production Cloud Storage)**
```properties
aws.s3.bucket.name=celebrations-tanishq
aws.s3.region=ap-south-1
```

**S3 Folder Structure:**
```
celebrations-tanishq/
└── events/
    └── {eventId}/
        ├── event_20260114_120000_*.jpg
        ├── event_20260114_120030_*.mp4
        └── ...
```

**S3 Integration:**
- Service: `S3Service.java`
- Authentication: IAM Role (EC2 Instance Profile)
- Upload Method: `uploadEventFile(MultipartFile file, String eventId)`

### 4. **Google Drive Integration** (Legacy/Optional)
```properties
# Event Images Drive
dechub.tanishq.key.filepath.event=/opt/tanishq/event-images-469618-32e65f6d62b3.p12
dechub.tanishq.google.service.account.event=event-images@event-images-469618.iam.gserviceaccount.com
dechub.tanishq.google.drive.parent-folder-id.event=1jE0rqkbPsPd2Y3lpa3-6MGhcU0UJbvfr

# Greeting Module Drive
dechub.tanishq.greeting.drive.key.filepath=/opt/tanishq/event-images-469618-32e65f6d62b3.p12
dechub.tanishq.greeting.drive.service.account=event-images@event-images-469618.iam.gserviceaccount.com
dechub.tanishq.greeting.drive.folder.id=1GtXx0JFNVd8cm4kEiNaZ-jw8GUHSSu2D
```

---

## ⚡ PERFORMANCE OPTIMIZATION

### 1. **In-Memory Caching**
**Class:** `StoreSummaryCache.java`

**Purpose:** Cache store summary data to reduce database queries

**Scheduled Refresh:**
```java
@Scheduled(fixedRate = 6 * 60 * 60 * 1000) // Every 6 hours
public void refreshCachedSummaries()
```

**What's Cached:**
- Store summaries by RBM (Regional Business Manager)
- Event aggregations
- Performance metrics

### 2. **Scheduler Configuration**
```properties
dechub.scheduler.fixedDelay=1800000  # 30 minutes
```

---

## 🔐 DATA SECURITY & CONFIGURATION

### 1. **Database Credentials**
**Local:**
```properties
spring.datasource.username=nagaraj_jadar
spring.datasource.password=Nagaraj07
```

**Pre-Production:**
```properties
spring.datasource.username=root
spring.datasource.password=Dechub#2025
```

**Production:**
```properties
spring.datasource.username=root
spring.datasource.password=Nagaraj@07
```

### 2. **Email Configuration (Production)**
```properties
spring.mail.host=smtp.office365.com
spring.mail.port=587
spring.mail.username=tanishqcelebrations@titan.co.in
spring.mail.password=Titan@2024
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 3. **External API Integration**
**Book Appointment API (Titan):**
```properties
book.appointment.api.username=Titan_Mule
book.appointment.api.password=admin_t!tan_mule
book.appointment.api.url=https://acemule.titan.co.in/ecomm/bookAnAppointment
```

---

## 📥 DATA IMPORT/EXPORT

### 1. **CSV Import** (Bulk Data Migration)
**Supported Files:**
- `events.csv` - Event data
- `attendees.csv` - Attendee records
- `invitees.csv` - Invitee records
- `stores.csv` - Store information

**Import Scripts:**
- `import_csv_to_mysql.sh` - Linux/Server import
- `migrate_data_windows.bat` - Windows import
- `setup_scripts_on_server.bat` - Upload scripts to server

**Documentation:**
- `CSV_IMPORT_QUICK_START.md` - Quick guide
- `IMPORT_CSV_TO_MYSQL.md` - Detailed guide
- `CSV_IMPORT_CHECKLIST.md` - Step-by-step checklist

### 2. **Database Migration**
**Pre-Prod to Production:**
```bash
# Export from pre-prod
mysqldump -u root -p selfie_preprod > preprod_export.sql

# Import to production
mysql -u root -p selfie_prod < preprod_export.sql
```

**Scripts:**
- `migrate_preprod_to_prod.sh` - Full migration script
- `export_preprod_data.sh` - Export script
- `import_production_data.sh` - Import script

**Documentation:**
- `DATA_MIGRATION_GUIDE.md` - Complete migration guide
- `PRODUCTION_MIGRATION_GUIDE.md` - Production-specific guide

### 3. **Database Backup**
```bash
# Backup directory
/opt/tanishq/backups/

# Backup example
mysqldump -u root -p selfie_prod > tanishq_backup_$(date +%Y%m%d_%H%M%S).sql
```

**Existing Backup:**
- `database_backup/tanishq_backup_20251203_165823.sql`

---

## 🔀 DATA SYNCHRONIZATION STRATEGY

### Read/Write Pattern

#### **Write Operations** (User Input)
```
User Action
    ↓
Controller (REST API)
    ↓
Service Layer
    ↓
JPA Repository
    ↓
MySQL Database (WRITE)
```

#### **Read Operations** (Display Data)
```
Request
    ↓
Check Cache (StoreSummaryCache)
    ├── Cache Hit → Return cached data
    └── Cache Miss
            ↓
        Query MySQL
            ↓
        Update Cache
            ↓
        Return data
```

### Google Sheets Sync (Reference Data Only)
- **Not** used for transactional data
- Used for configuration and master data
- Manually synced or scheduled import
- Acts as external data source

---

## 🎯 ENTITY RELATIONSHIPS

### Key Relationships (JPA Mappings)

```java
Store
    ↓ @OneToMany
    Event
        ↓ @ManyToOne (reverse)
        Store

Event
    ↓ @OneToMany
    Attendee
        ↓ @ManyToOne (reverse)
        Event

Event
    ↓ @OneToMany
    Invitee
        ↓ @ManyToOne (reverse)
        Event
```

### Data Model Highlights

**Store Entity:**
- Primary Key: `storeCode` (String)
- Includes: region, level, manager usernames (abm, rbm, cee)
- Related to: Events

**Event Entity:**
- Primary Key: `id` (String, format: storeCode_uuid)
- Includes: eventType, startDate, region, invitees/attendees counts
- Links to: Store, Drive folder (completedEventsDriveLink)
- Related to: Attendees, Invitees

**User Entity:**
- Role-based: ABM, RBM, CEE
- Separate login tables: abm_login, rbm_login, cee_login
- Password history tracking

---

## 📊 DATA VOLUME & SCALABILITY

### File Upload Limits
```properties
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

### Database Configuration
- **Dialect:** MySQL8Dialect
- **DDL Mode:** Update (auto-creates/updates schema)
- **SQL Logging:** Enabled (show-sql=true)
- **Format SQL:** Enabled (format_sql=true)

---

## 🚀 DEPLOYMENT DATA CONSIDERATIONS

### Environment Profiles
1. **local** - Development with local MySQL
2. **test** - Testing environment
3. **uat** - User acceptance testing
4. **preprod** - Pre-production (current: selfie_preprod)
5. **prod** - Production (current: selfie_prod)

**Active Profile:** Set in `application.properties`
```properties
spring.profiles.active=prod
```

### Current Deployment State
- **Pre-Prod:** Running with data
- **Production:** Running (may need data migration)
- **Migration:** Scripts available for data transfer

---

## 📝 DATA MANAGEMENT BEST PRACTICES

### 1. **Backup Before Changes**
Always create database backups before:
- Data migration
- Schema updates
- Production deployments

### 2. **Environment Separation**
- Different databases per environment
- Different Google Sheets for test vs prod
- Separate storage paths

### 3. **Data Validation**
- CSV imports validated before loading
- File uploads checked for size/type
- User input sanitized

### 4. **Performance Monitoring**
- SQL queries logged (in debug mode)
- Cache hit/miss monitoring
- Scheduled refresh of cached data

---

## 🛠️ KEY SERVICES & COMPONENTS

### Data Access Layer
- **Repositories:** 15 JPA repositories (AttendeeRepository, EventRepository, etc.)
- **Entities:** 15 JPA entities (@Entity classes)
- **ORM:** Hibernate with MySQL8Dialect

### Business Logic Layer
- **TanishqPageService:** Main business logic
- **GreetingService:** Greetings module
- **StoreServices:** Store operations
- **StoreSummaryScheduler:** Scheduled cache refresh

### Storage Services
- **S3Service:** AWS S3 uploads (preprod/prod only)
- **Local file storage:** Development environment

---

## 📚 DOCUMENTATION INDEX

### Data Migration
- `DATA_MIGRATION_GUIDE.md` - General migration guide
- `PRODUCTION_MIGRATION_GUIDE.md` - Production-specific
- `LEGACY_DATA_MIGRATION_GUIDE.md` - Google Sheets → MySQL

### CSV Import
- `CSV_IMPORT_QUICK_START.md` - Quick start guide
- `IMPORT_CSV_TO_MYSQL.md` - Detailed CSV import
- `ALL_CSV_FILES_SUMMARY.md` - CSV files overview

### Deployment
- `COMPLETE_DEPLOYMENT_GUIDE.md` - Full deployment guide
- `PRODUCTION_DEPLOYMENT_CHECKLIST.md` - Deployment checklist
- `DEPLOY_PRODUCTION_NOW.md` - Production deployment

### Troubleshooting
- `FIX_TABLE_NOT_EXIST.md` - Table creation issues
- `FIX_LOCAL_INFILE_ERROR.md` - CSV import errors
- `TROUBLESHOOT_CSV_NOT_IMPORTING.md` - Import debugging

---

## 🎓 SUMMARY

**This project uses a sophisticated multi-tier data management approach:**

1. **MySQL** - Primary transactional database
2. **Google Sheets** - Configuration & reference data
3. **Local Storage** - Development file storage
4. **AWS S3** - Production cloud storage
5. **Caching** - In-memory performance optimization
6. **Scheduled Jobs** - Automated data refresh

**Data flows through:**
- REST APIs → Service Layer → JPA Repositories → MySQL
- Google Sheets integration for master data
- File uploads to local storage or S3
- Cached summaries for performance

**Migration capabilities:**
- CSV bulk import/export
- Database dump/restore
- Environment-to-environment transfer
- Google Sheets to MySQL conversion

This architecture ensures **scalability, performance, and data integrity** across all environments.

---

## 📞 Quick Reference

**Environment Check:**
```bash
# Check which profile is active
grep "spring.profiles.active" src/main/resources/application.properties
```

**Database Check:**
```bash
# Connect to database
mysql -u root -p

# Show databases
SHOW DATABASES;

# Use specific database
USE selfie_prod;

# Show tables
SHOW TABLES;

# Count records
SELECT COUNT(*) FROM events;
```

**Storage Check:**
```bash
# Local
ls -la ./storage/

# Production
ls -la /opt/tanishq/storage/
```

---

*Last Updated: January 14, 2026*
*Version: 1.0*

