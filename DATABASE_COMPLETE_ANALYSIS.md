# 🔍 COMPLETE DATABASE SCHEMA ANALYSIS
**Tanishq Selfie App - Comprehensive Database Structure Report**

**Generated:** February 4, 2026  
**Database Type:** MySQL 8.x  
**Application:** Spring Boot + Hibernate JPA  
**Environment:** Production (`selfie_prod`) & Pre-Production (`selfie_preprod`)

---

## 📊 EXECUTIVE SUMMARY

### Database Statistics
- **Total Tables:** 15 Core Tables + Multiple Backup Tables
- **Total Entities:** 15 JPA Entity Classes
- **Primary Database:** `selfie_prod` (Production) / `selfie_preprod` (Pre-prod)
- **ORM Framework:** Hibernate with `ddl-auto=update`
- **Relationships:** Multiple One-to-Many, Many-to-One relationships

### Database Environments
```
┌─────────────┬──────────────────┬────────────┬─────────┐
│ Environment │ Host             │ Database   │ Port    │
├─────────────┼──────────────────┼────────────┼─────────┤
│ Production  │ localhost        │ selfie_prod│ 3001    │
│ Pre-Prod    │ localhost        │ selfie_preprod│ 3000 │
│ Local       │ localhost        │ tanishq    │ varies  │
└─────────────┴──────────────────┴────────────┴─────────┘
```

---

## 🗂️ CORE DATABASE TABLES (15 Tables)

### **CATEGORY 1: EVENT MANAGEMENT SYSTEM** (5 Tables)

#### 1. **`stores`** - Store Master Data
```
📍 Purpose: Central store information repository
🔑 Primary Key: storeCode (VARCHAR)
🔗 Relationships: One-to-Many with events
```

**Schema:**
```sql
CREATE TABLE stores (
    store_code VARCHAR(255) PRIMARY KEY,
    store_name VARCHAR(255),
    store_address VARCHAR(255),
    store_city VARCHAR(255),
    store_state VARCHAR(255),
    store_country VARCHAR(255),
    store_zip_code VARCHAR(255),
    store_phone_no_one VARCHAR(255),
    store_phone_no_two VARCHAR(255),
    store_email_id VARCHAR(255),
    store_latitude VARCHAR(255),
    store_longitude VARCHAR(255),
    store_date_of_opening VARCHAR(255),
    store_type VARCHAR(255),
    store_opening_time VARCHAR(255),
    store_closing_time VARCHAR(255),
    store_manager_name VARCHAR(255),
    store_manager_no VARCHAR(255),
    store_manager_email VARCHAR(255),
    store_location_link VARCHAR(255),
    languages VARCHAR(255),
    parking VARCHAR(255),
    payment VARCHAR(255),
    kakatiya_store VARCHAR(255),
    celeste_store VARCHAR(255),
    rating VARCHAR(255),
    number_of_ratings VARCHAR(255),
    is_collection VARCHAR(255),
    region VARCHAR(255),
    level VARCHAR(255),
    abm_username VARCHAR(255),
    rbm_username VARCHAR(255),
    cee_username VARCHAR(255)
);
```

**Business Logic:**
- Stores hierarchical management structure (ABM → RBM → CEE)
- Region-based organization
- Complete store operational details
- Manager contact information

---

#### 2. **`events`** - Event Master Table
```
📍 Purpose: Core event management and tracking
🔑 Primary Key: id (VARCHAR(255)) - Format: storeCode_uuid
🔗 Foreign Key: store_code → stores(store_code)
🔗 Relationships: Many-to-One with stores, One-to-Many with attendees, invitees
```

**Schema:**
```sql
CREATE TABLE events (
    id VARCHAR(255) PRIMARY KEY,
    created_at DATETIME,
    region VARCHAR(255),
    event_type VARCHAR(255),
    event_sub_type VARCHAR(255),
    event_name VARCHAR(255),
    rso VARCHAR(255),
    start_date VARCHAR(255),
    image VARCHAR(255),
    invitees INT,
    attendees INT,
    completed_events_drive_link VARCHAR(255),
    community VARCHAR(255),
    location VARCHAR(255),
    attendees_uploaded TINYINT(1),
    sale DOUBLE,
    advance DOUBLE,
    ghs_or_rga DOUBLE,
    gmb DOUBLE,
    diamond_awareness TINYINT(1),
    ghs_flag TINYINT(1),
    store_code VARCHAR(255),
    FOREIGN KEY (store_code) REFERENCES stores(store_code)
);
```

**Business Metrics Tracked:**
- **Sales Data:** `sale`, `advance` (Double)
- **Performance Metrics:** `ghs_or_rga`, `gmb`
- **Campaign Flags:** `diamond_awareness`, `ghs_flag`
- **Attendance:** Expected vs Actual (`invitees` vs `attendees`)

**Event Types Examples:**
- Wedding Events
- Festival Celebrations
- Product Launches
- Store Anniversaries
- Community Events

---

#### 3. **`attendees`** - Event Attendee Records
```
📍 Purpose: Track actual event participants
🔑 Primary Key: id (BIGINT AUTO_INCREMENT)
🔗 Foreign Key: event_id → events(id)
```

**Schema:**
```sql
CREATE TABLE attendees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    phone VARCHAR(255),
    `like` VARCHAR(255),
    first_time_at_tanishq TINYINT(1),
    created_at DATETIME,
    is_uploaded_from_excel TINYINT(1),
    rso_name VARCHAR(255),
    event_id VARCHAR(255),
    FOREIGN KEY (event_id) REFERENCES events(id)
);
```

**Key Features:**
- Tracks first-time Tanishq customers
- Supports bulk Excel uploads
- Records attendee preferences (`like` field)
- RSO (Regional Sales Officer) tracking

---

#### 4. **`invitees`** - Event Invitation List
```
📍 Purpose: Pre-event invitation management
🔑 Primary Key: id (BIGINT AUTO_INCREMENT)
🔗 Foreign Key: event_id → events(id)
```

**Schema:**
```sql
CREATE TABLE invitees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    contact VARCHAR(255),
    created_at DATETIME,
    event_id VARCHAR(255),
    FOREIGN KEY (event_id) REFERENCES events(id)
);
```

**Usage:**
- Pre-event planning
- Invitation list management
- Expected attendance calculation
- Follow-up tracking

---

#### 5. **`greetings`** - Video Greeting Cards
```
📍 Purpose: QR-based video greeting card system
🔑 Primary Key: id (BIGINT AUTO_INCREMENT)
🔑 Unique Key: unique_id (VARCHAR)
```

**Schema:**
```sql
CREATE TABLE greetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unique_id VARCHAR(255) UNIQUE,
    greeting_text VARCHAR(255),
    phone VARCHAR(255),
    message TEXT,
    qr_code_data LONGTEXT,
    drive_file_id VARCHAR(255),
    created_at DATETIME,
    uploaded TINYINT(1)
);
```

**Features:**
- QR code generation (Base64 encoded PNG)
- Video URL storage (S3 or local)
- Personalized messages
- Upload status tracking

**Flow:**
1. Customer creates greeting with name/message
2. System generates unique QR code
3. Customer uploads video
4. QR code links to video playback page

---

### **CATEGORY 2: USER & CUSTOMER DATA** (4 Tables)

#### 6. **`users`** - General User Accounts
```
📍 Purpose: System user authentication
🔑 Primary Key: id (BIGINT AUTO_INCREMENT)
```

**Schema:**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    username VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(255),
    email VARCHAR(255)
);
```

**Roles:**
- ABM (Area Business Manager)
- RBM (Regional Business Manager)
- CEE (Customer Engagement Executive)

---

#### 7. **`user_details`** - Customer Feedback Data
```
📍 Purpose: Customer purchase journey tracking
🔑 Primary Key: id (BIGINT AUTO_INCREMENT)
```

**Schema:**
```sql
CREATE TABLE user_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    reason VARCHAR(255),
    rso_name VARCHAR(255),
    store_code VARCHAR(255),
    date DATE,
    my_first_diamond VARCHAR(255)
);
```

**Purpose:**
- Track customer purchase occasions
- First-time diamond buyers
- Store-wise customer data
- RSO performance tracking

---

#### 8. **`bride_details`** - Bridal Customer Data
```
📍 Purpose: Rivaah (Bridal Collection) customer management
🔑 Primary Key: id (BIGINT AUTO_INCREMENT)
```

**Schema:**
```sql
CREATE TABLE bride_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bride_name VARCHAR(255),
    bride_event VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    date DATE,
    bride_type VARCHAR(255),
    zip_code VARCHAR(255)
);
```

**Bride Types:**
- Pre-wedding
- Wedding day
- Post-wedding
- Anniversary

---

#### 9. **`password_history`** - Password Change Tracking
```
📍 Purpose: Security audit trail for password changes
🔑 Primary Key: btq_code (VARCHAR)
```

**Schema:**
```sql
CREATE TABLE password_history (
    btq_code VARCHAR(255) PRIMARY KEY,
    old_password VARCHAR(255),
    new_password VARCHAR(255),
    changed_at DATETIME NOT NULL
);
```

---

### **CATEGORY 3: AUTHENTICATION SYSTEM** (3 Tables)

#### 10. **`abm_login`** - Area Business Manager Login
```sql
CREATE TABLE abm_login (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    abm_user_id VARCHAR(255) UNIQUE NOT NULL,
    abm_name VARCHAR(255),
    password VARCHAR(255),
    email VARCHAR(255),
    region VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME
);
```

#### 11. **`rbm_login`** - Regional Business Manager Login
```sql
CREATE TABLE rbm_login (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rbm_user_id VARCHAR(255) UNIQUE NOT NULL,
    rbm_name VARCHAR(255),
    password VARCHAR(255),
    email VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME
);
```

#### 12. **`cee_login`** - Customer Engagement Executive Login
```sql
CREATE TABLE cee_login (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cee_user_id VARCHAR(255) UNIQUE NOT NULL,
    cee_name VARCHAR(255),
    password VARCHAR(255),
    email VARCHAR(255),
    region VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME
);
```

**Hierarchy:**
```
RBM (Regional Business Manager)
  └─ ABM (Area Business Manager)
      └─ CEE (Customer Engagement Executive)
          └─ Store Manager
```

---

### **CATEGORY 4: RIVAAH (BRIDAL) SYSTEM** (3 Tables)

#### 13. **`rivaah`** - Rivaah Collection Master
```
📍 Purpose: Rivaah bridal collection campaigns
🔑 Primary Key: id (BIGINT AUTO_INCREMENT)
🔗 Relationships: One-to-Many with product_details, rivaah_users
```

**Schema:**
```sql
CREATE TABLE rivaah (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(255),
    bride VARCHAR(255),
    event VARCHAR(255),
    clothing_type VARCHAR(255),
    tags VARCHAR(255)
);
```

---

#### 14. **`rivaah_users`** - Rivaah Campaign Registrations
```sql
CREATE TABLE rivaah_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    contact VARCHAR(255),
    source VARCHAR(255),
    created_at DATETIME,
    rivaah_id BIGINT,
    FOREIGN KEY (rivaah_id) REFERENCES rivaah(id)
);
```

---

#### 15. **`product_details`** - Product Catalog
```sql
CREATE TABLE product_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(255),
    image_link VARCHAR(255),
    product_link VARCHAR(255),
    like_count INT,
    rivaah_id BIGINT,
    FOREIGN KEY (rivaah_id) REFERENCES rivaah(id)
);
```

---

## 🔄 BACKUP & STAGING TABLES (Observed in Screenshots)

Based on your MySQL Workbench screenshots, I can see several backup tables:

### **Backup Tables Identified:**
1. **`events_backup`** - Events table backup
2. **`events_backup_oct_nov_2025`** - Quarterly backup
3. **`attendees_backup_202501...`** - Monthly attendee backup
4. **`stores_backup`** - Store data backup
5. **`events_staging`** - Staging environment data
6. **`events_staging_oct_nov`** - Staging quarterly data
7. **`stores_before_recovery_202601...`** - Pre-recovery snapshot
8. **`events_before_recovery_202601...`** - Pre-recovery snapshot
9. **`events_test_backup`** - Test environment backup
10. **`product_details_backup`** - Product backup
11. **`events_event_name_back...`** - Specific field backup

### **Backup Strategy Pattern:**
```
Format: {table_name}_backup[_description][_YYYYMM]
Examples:
- events_backup
- attendees_backup_202501
- stores_before_recovery_20260115
```

---

## 🔗 DATABASE RELATIONSHIPS (ERD Summary)

```
┌─────────────────────────────────────────────────────────┐
│                  DATABASE RELATIONSHIP MAP               │
└─────────────────────────────────────────────────────────┘

         ┌──────────────┐
         │   STORES     │ ◄──── ABM, RBM, CEE managed
         │ (store_code) │
         └───────┬──────┘
                 │
                 │ 1:N
                 ▼
         ┌──────────────┐
         │    EVENTS    │
         │     (id)     │
         └───┬──────┬───┘
             │      │
        1:N  │      │ 1:N
             ▼      ▼
    ┌──────────┐ ┌──────────┐
    │ATTENDEES │ │ INVITEES │
    └──────────┘ └──────────┘

         ┌──────────────┐
         │   RIVAAH     │
         │     (id)     │
         └───┬──────┬───┘
             │      │
        1:N  │      │ 1:N
             ▼      ▼
    ┌──────────────┐ ┌──────────────┐
    │RIVAAH_USERS  │ │PRODUCT_DETAILS│
    └──────────────┘ └──────────────┘

    ┌──────────────┐
    │  GREETINGS   │ (Independent)
    └──────────────┘

    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
    │  ABM_LOGIN   │    │  RBM_LOGIN   │    │  CEE_LOGIN   │
    └──────────────┘    └──────────────┘    └──────────────┘
```

---

## ⚠️ ISSUES & CONCERNS IDENTIFIED

### **🔴 CRITICAL ISSUES**

#### 1. **Inconsistent Data Types**
```java
// In Event entity - start_date should be Date/LocalDate
@Column(name = "start_date")
private String startDate;  // ❌ Should be LocalDate
```

**Impact:** 
- Date comparisons are string-based
- Sorting issues
- Cannot use SQL date functions

**Recommendation:**
```java
@Column(name = "start_date")
private LocalDate startDate;  // ✅ Correct type
```

---

#### 2. **Missing Indexes**
Based on entity analysis, critical indexes are missing:

```sql
-- Missing indexes that should be added:
CREATE INDEX idx_events_store_code ON events(store_code);
CREATE INDEX idx_events_created_at ON events(created_at);
CREATE INDEX idx_events_start_date ON events(start_date);
CREATE INDEX idx_attendees_event_id ON attendees(event_id);
CREATE INDEX idx_attendees_phone ON attendees(phone);
CREATE INDEX idx_invitees_event_id ON invitees(event_id);
CREATE INDEX idx_stores_region ON stores(region);
CREATE INDEX idx_stores_abm_username ON stores(abm_username);
CREATE INDEX idx_greetings_unique_id ON greetings(unique_id);
CREATE INDEX idx_greetings_uploaded ON greetings(uploaded);
```

---

#### 3. **No Cascading Delete Strategy**
```java
@OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
private List<Attendee> attendeesEntities;  // ⚠️ Cascade ALL includes DELETE
```

**Risk:** Deleting an event will delete all attendees - potential data loss!

**Recommendation:**
```java
@OneToMany(mappedBy = "event", 
           cascade = {CascadeType.PERSIST, CascadeType.MERGE})
private List<Attendee> attendeesEntities;
```

---

#### 4. **Password Storage Security**
```java
// All login tables store plain text passwords
private String password;  // ❌ NO ENCRYPTION!
```

**CRITICAL SECURITY RISK:**
- Passwords stored in plain text
- No BCrypt/encryption
- Security audit failure

**Fix Required:**
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// In service layer
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode(rawPassword);
```

---

### **🟡 MODERATE ISSUES**

#### 5. **Column Name Keyword Conflict**
```java
@Column(name = "`like`")  // MySQL keyword escaped
private String like;
```

**Issue:** Using SQL keywords as column names

**Better approach:**
```java
@Column(name = "preference")  // ✅ Non-keyword name
private String preference;
```

---

#### 6. **Null Handling Not Defined**
```java
private String eventName;  // No @Column(nullable = false)
```

**Problem:** No database-level constraints

**Fix:**
```java
@Column(name = "event_name", nullable = false, length = 255)
private String eventName;
```

---

#### 7. **Redundant Data Storage**
```sql
-- In events table:
invitees INT,        -- Count of invitees
attendees INT,       -- Count of attendees

-- But also separate tables:
invitees (table)     -- Actual invitee records
attendees (table)    -- Actual attendee records
```

**Issue:** Data duplication risk - counts may not match reality

**Solution:** Use COUNT queries or triggers to maintain consistency

---

#### 8. **Missing Audit Columns**
Most tables lack:
- `updated_at` timestamp
- `updated_by` user tracking
- `is_deleted` soft delete flag

**Recommendation:**
```java
@Column(name = "updated_at")
private LocalDateTime updatedAt;

@Column(name = "is_active")
private Boolean isActive = true;
```

---

### **🟢 MINOR ISSUES**

#### 9. **Inconsistent Naming Conventions**
```
Mixing patterns:
- store_code (snake_case) ✓
- storeCode (camelCase in Java) ✓
- storeName vs store_name (inconsistent)
```

#### 10. **Large VARCHAR Lengths**
```java
@Id
@Column(length = 255, nullable = false)
private String id;  // Event ID probably doesn't need 255 chars
```

---

## 📈 PERFORMANCE CONSIDERATIONS

### **Query Performance Issues:**

1. **Missing Foreign Key Indexes** (Auto-created by Hibernate but verify):
```sql
SHOW INDEXES FROM attendees;
SHOW INDEXES FROM invitees;
```

2. **Large Text Columns Without Proper Indexing:**
```sql
-- qr_code_data is LONGTEXT - can't be indexed
-- Consider moving to file storage
```

3. **No Partitioning Strategy:**
```sql
-- Events table will grow large over time
-- Consider partitioning by created_at date
```

---

## 🔧 RECOMMENDED FIXES & OPTIMIZATIONS

### **Priority 1: Security**
```sql
-- 1. Hash all passwords immediately
UPDATE abm_login SET password = SHA2(password, 256) WHERE password NOT LIKE '$2a$%';
UPDATE rbm_login SET password = SHA2(password, 256) WHERE password NOT LIKE '$2a$%';
UPDATE cee_login SET password = SHA2(password, 256) WHERE password NOT LIKE '$2a$%';

-- 2. Add password policy columns
ALTER TABLE abm_login ADD COLUMN password_changed_at DATETIME;
ALTER TABLE abm_login ADD COLUMN must_change_password TINYINT(1) DEFAULT 0;
```

### **Priority 2: Data Integrity**
```sql
-- 1. Add NOT NULL constraints
ALTER TABLE events MODIFY event_name VARCHAR(255) NOT NULL;
ALTER TABLE events MODIFY store_code VARCHAR(255) NOT NULL;

-- 2. Add CHECK constraints (MySQL 8.0+)
ALTER TABLE events ADD CONSTRAINT chk_sale_positive CHECK (sale >= 0);
ALTER TABLE events ADD CONSTRAINT chk_advance_positive CHECK (advance >= 0);

-- 3. Add unique constraints where needed
ALTER TABLE greetings ADD UNIQUE INDEX uk_unique_id (unique_id);
```

### **Priority 3: Performance**
```sql
-- 1. Add composite indexes for common queries
CREATE INDEX idx_events_store_date ON events(store_code, start_date);
CREATE INDEX idx_attendees_event_created ON attendees(event_id, created_at);

-- 2. Add full-text search indexes if needed
ALTER TABLE events ADD FULLTEXT INDEX ft_event_name (event_name);
```

### **Priority 4: Auditing**
```sql
-- Add audit columns to all tables
ALTER TABLE events 
  ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN created_by VARCHAR(255),
  ADD COLUMN updated_by VARCHAR(255);
```

---

## 📋 DATABASE MAINTENANCE CHECKLIST

### **Weekly Tasks:**
- [ ] Check table sizes: `SELECT table_name, ROUND(((data_length + index_length) / 1024 / 1024), 2) AS "Size (MB)" FROM information_schema.TABLES WHERE table_schema = 'selfie_prod' ORDER BY (data_length + index_length) DESC;`
- [ ] Verify backup tables are current
- [ ] Check for orphaned records
- [ ] Monitor slow query log

### **Monthly Tasks:**
- [ ] Optimize tables: `OPTIMIZE TABLE events, attendees, invitees, stores;`
- [ ] Update table statistics: `ANALYZE TABLE events;`
- [ ] Review and archive old data
- [ ] Check foreign key integrity

### **Quarterly Tasks:**
- [ ] Create backup tables (as you're doing)
- [ ] Review and update indexes
- [ ] Performance audit
- [ ] Security audit

---

## 🎯 ACTION ITEMS SUMMARY

### **Immediate Actions Required:**

1. **SECURITY** (CRITICAL):
   - Implement BCrypt password hashing
   - Encrypt existing passwords
   - Add password expiry policy

2. **DATA INTEGRITY**:
   - Add missing NOT NULL constraints
   - Fix date column data types
   - Add foreign key constraints verification

3. **PERFORMANCE**:
   - Create missing indexes
   - Add composite indexes for common queries
   - Consider query optimization

4. **MAINTENANCE**:
   - Document backup strategy
   - Set up automated backups
   - Create data retention policy

5. **CODE IMPROVEMENTS**:
   - Add validation annotations in entities
   - Implement soft deletes instead of hard deletes
   - Add audit trail functionality

---

## 📞 NEXT STEPS

If you provide database access, I can help you with:

1. **Schema Analysis Queries:**
   - Generate full DDL scripts
   - Analyze table relationships
   - Check for data inconsistencies
   - Performance profiling

2. **Data Quality Checks:**
   - Orphaned record detection
   - Duplicate data identification
   - Data consistency validation

3. **Optimization Recommendations:**
   - Query performance tuning
   - Index optimization
   - Storage optimization

4. **Migration Scripts:**
   - Data type fixes
   - Index creation
   - Constraint additions

---

## 📚 RELATED DOCUMENTATION

- `DATABASE_SCHEMA_DOCUMENTATION.md` - Detailed schema reference
- `DATABASE_VISUAL_SCHEMA.md` - Visual ERD diagrams
- `GREETING_DATABASE_SCHEMA.sql` - Greeting module schema
- `DATABASE_VERIFICATION_GUIDE.md` - Verification procedures
- `COMPLETE_DATABASE_VERIFICATION.sql` - Verification queries

---

**Document Status:** ✅ Complete  
**Last Updated:** February 4, 2026  
**Author:** Database Analysis System  
**Review Required:** Security Team, DBA Team

