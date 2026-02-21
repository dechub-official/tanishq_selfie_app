# 📊 DATABASE VISUAL QUICK REFERENCE
**Tanishq Selfie App - Database At-A-Glance**

---

## 🎯 QUICK NAVIGATION

| Section | Content | Priority |
|---------|---------|----------|
| [15 Core Tables](#15-core-tables) | Complete table list | ⭐⭐⭐ |
| [Entity Relationships](#entity-relationships-erd) | Visual ERD | ⭐⭐⭐ |
| [Critical Issues](#critical-issues-to-fix) | Problems found | ⭐⭐⭐ |
| [Data Flow](#data-flow-diagrams) | System workflows | ⭐⭐ |
| [Common Queries](#common-queries) | Frequently used SQL | ⭐⭐ |

---

## 📋 15 CORE TABLES

```
┌───────────────────────────────────────────────────────────────┐
│                     TABLE CATEGORIES                          │
└───────────────────────────────────────────────────────────────┘

🏢 EVENT MANAGEMENT (5)          👥 USER DATA (4)
├─ stores                        ├─ users
├─ events                        ├─ user_details
├─ attendees                     ├─ bride_details
├─ invitees                      └─ password_history
└─ greetings
                                 💍 RIVAAH SYSTEM (3)
🔐 AUTHENTICATION (3)            ├─ rivaah
├─ abm_login                     ├─ rivaah_users
├─ rbm_login                     └─ product_details
└─ cee_login
```

---

## 🗺️ ENTITY RELATIONSHIPS (ERD)

### **Primary Relationships**

```
                    ┌─────────────────┐
                    │     STORES      │
                    │  (store_code)   │
                    └────────┬────────┘
                             │
                             │ 1:N
                             ▼
                    ┌─────────────────┐
                    │     EVENTS      │
                    │      (id)       │
                    └────┬────────┬───┘
                         │        │
                    1:N  │        │ 1:N
                         ▼        ▼
                ┌──────────┐  ┌──────────┐
                │ATTENDEES │  │ INVITEES │
                └──────────┘  └──────────┘


            ┌────────────────┐
            │    RIVAAH      │
            │     (id)       │
            └────┬───────┬───┘
                 │       │
            1:N  │       │ 1:N
                 ▼       ▼
        ┌──────────┐  ┌──────────────┐
        │RIVAAH_   │  │PRODUCT_      │
        │USERS     │  │DETAILS       │
        └──────────┘  └──────────────┘


    ┌─────────────┐    INDEPENDENT TABLE
    │  GREETINGS  │    (No FK relationships)
    └─────────────┘
```

---

## 📊 TABLE DETAILS MATRIX

| Table | Primary Key | Foreign Keys | Relationships | Records (Est.) |
|-------|-------------|--------------|---------------|----------------|
| **stores** | store_code (VARCHAR) | - | → events (1:N) | ~500 |
| **events** | id (VARCHAR) | store_code | ← stores, → attendees, invitees | ~10,000 |
| **attendees** | id (BIGINT) | event_id | ← events | ~100,000 |
| **invitees** | id (BIGINT) | event_id | ← events | ~50,000 |
| **greetings** | id (BIGINT) | - | None | ~5,000 |
| **users** | id (BIGINT) | - | None | ~100 |
| **user_details** | id (BIGINT) | - | None | ~1,000 |
| **bride_details** | id (BIGINT) | - | None | ~2,000 |
| **password_history** | btq_code (VARCHAR) | - | None | ~500 |
| **abm_login** | id (BIGINT) | - | None | ~50 |
| **rbm_login** | id (BIGINT) | - | None | ~20 |
| **cee_login** | id (BIGINT) | - | None | ~100 |
| **rivaah** | id (BIGINT) | - | → rivaah_users, product_details | ~500 |
| **rivaah_users** | id (BIGINT) | rivaah_id | ← rivaah | ~2,000 |
| **product_details** | id (BIGINT) | rivaah_id | ← rivaah | ~5,000 |

---

## 🔴 CRITICAL ISSUES TO FIX

### **Priority 1: SECURITY** 🚨

```
❌ PLAIN TEXT PASSWORDS
Location: abm_login, rbm_login, cee_login
Risk: CRITICAL - Data breach exposure
Fix: Implement BCrypt hashing immediately

❌ NO PASSWORD POLICY
Missing: Expiry, complexity rules, history
Fix: Add password_changed_at, must_change_password columns
```

### **Priority 2: DATA INTEGRITY** ⚠️

```
❌ MISSING INDEXES
Tables: events, attendees, invitees, stores
Impact: Slow queries, poor performance
Fix: Add indexes on foreign keys and frequently queried columns

❌ INCONSISTENT DATA TYPES
Issue: start_date is VARCHAR instead of DATE
Impact: Cannot use date functions, sorting issues
Fix: Convert to proper DATE/DATETIME types

❌ NO NOT NULL CONSTRAINTS
Issue: Critical fields allow NULL values
Impact: Data quality issues
Fix: Add NOT NULL constraints on required fields
```

### **Priority 3: PERFORMANCE** 📈

```
❌ MISSING COMPOSITE INDEXES
Queries: Store + Date, Event + Type
Impact: Slow dashboard queries
Fix: Add composite indexes for common query patterns

❌ NO QUERY OPTIMIZATION
Issue: Full table scans on large tables
Impact: Slow response times
Fix: Optimize queries, add proper indexes
```

---

## 🔄 DATA FLOW DIAGRAMS

### **Event Creation Flow**

```
┌─────────────┐
│   USER      │
│  (Store)    │
└──────┬──────┘
       │ Creates
       ▼
┌─────────────────────┐
│   EVENT RECORD      │
│  (events table)     │
│  - event_name       │
│  - store_code  ─────┼──→ Links to STORES
│  - start_date       │
│  - event_type       │
└──────┬──────────────┘
       │
       │ Associated
       ├──────────────────┐
       ▼                  ▼
┌─────────────┐    ┌─────────────┐
│  INVITEES   │    │  ATTENDEES  │
│  (Pre-event)│    │  (Actual)   │
└─────────────┘    └─────────────┘
```

### **Greeting Card Flow**

```
┌─────────────┐
│  CUSTOMER   │
└──────┬──────┘
       │ Step 1: Creates greeting
       ▼
┌─────────────────────┐
│  GREETING RECORD    │
│  - unique_id        │
│  - greeting_text    │
│  - message          │
│  - uploaded = 0     │
└──────┬──────────────┘
       │ Step 2: QR Code generated
       ▼
┌─────────────────────┐
│  QR_CODE_DATA       │
│  (Base64 PNG)       │
└──────┬──────────────┘
       │ Step 3: Video upload
       ▼
┌─────────────────────┐
│  DRIVE_FILE_ID      │
│  (Video URL)        │
│  uploaded = 1       │
└─────────────────────┘
```

### **User Hierarchy**

```
        ┌────────────────────┐
        │       RBM          │
        │ (Regional Business │
        │     Manager)       │
        └─────────┬──────────┘
                  │
                  │ manages
                  ▼
        ┌────────────────────┐
        │       ABM          │
        │ (Area Business     │
        │     Manager)       │
        └─────────┬──────────┘
                  │
                  │ manages
                  ▼
        ┌────────────────────┐
        │       CEE          │
        │ (Customer Engage-  │
        │  ment Executive)   │
        └─────────┬──────────┘
                  │
                  │ manages
                  ▼
        ┌────────────────────┐
        │   STORE MANAGER    │
        │   (stores table)   │
        └────────────────────┘
```

---

## 💾 COLUMN REFERENCE GUIDE

### **stores** (Store Master)

```
📍 Primary Info
├─ store_code         [PK] Store identifier (BTQ code)
├─ store_name         Store display name
├─ store_address      Full address
├─ store_city         City location
├─ store_state        State/Province
└─ store_country      Country

📞 Contact
├─ store_phone_no_one    Primary phone
├─ store_phone_no_two    Secondary phone
└─ store_email_id        Email address

📍 Location
├─ store_latitude     GPS coordinate
├─ store_longitude    GPS coordinate
└─ store_location_link Bitly URL

👥 Management
├─ abm_username       ABM assigned
├─ rbm_username       RBM assigned
├─ cee_username       CEE assigned
└─ region             Region code

🏪 Operations
├─ store_opening_time Operating hours start
├─ store_closing_time Operating hours end
├─ languages          Spoken languages
├─ parking            Parking availability
└─ payment            Payment methods
```

### **events** (Event Records)

```
🆔 Identity
├─ id                 [PK] Format: {storeCode}_{uuid}
├─ event_name         Display name
├─ event_type         Category (Wedding, Festival, etc.)
└─ event_sub_type     Subcategory

📅 Scheduling
├─ created_at         Creation timestamp
└─ start_date         Event date (⚠️ VARCHAR, should be DATE)

📍 Location
├─ store_code         [FK] → stores
├─ region             Geographic region
└─ location           Venue address

👥 Participation
├─ invitees           Expected count
├─ attendees          Actual count
├─ community          Target community
└─ attendees_uploaded Upload status flag

💰 Business Metrics
├─ sale               Total sales (DOUBLE)
├─ advance            Advance payments (DOUBLE)
├─ ghs_or_rga         GHS/RGA metric
├─ gmb                GMB metric
├─ diamond_awareness  Campaign flag
└─ ghs_flag           GHS indicator

📎 Resources
├─ image              Event poster URL
├─ completed_events_drive_link Google Drive link
└─ rso                RSO name
```

### **attendees** (Event Participants)

```
🆔 Identity
├─ id                       [PK] Auto-increment
├─ event_id                 [FK] → events(id)
└─ name                     Attendee name

📞 Contact
└─ phone                    Phone number

🎯 Preferences
├─ like                     Product preference (⚠️ MySQL keyword)
└─ first_time_at_tanishq   First-time customer flag

📊 Tracking
├─ created_at               Registration time
├─ is_uploaded_from_excel   Bulk upload flag
└─ rso_name                 RSO assigned
```

### **greetings** (Video Cards)

```
🆔 Identity
├─ id                 [PK] Auto-increment
└─ unique_id          [UK] Format: GREETING_{timestamp}

📝 Content
├─ greeting_text      Sender name
├─ phone              Sender phone (optional)
└─ message            Personal message (TEXT)

📷 Media
├─ qr_code_data       Base64 PNG (LONGTEXT)
└─ drive_file_id      Video URL (⚠️ Misleading name)

📊 Status
├─ created_at         Creation time
└─ uploaded           Video upload status (0/1)
```

---

## 🔍 COMMON QUERIES

### **Find Events by Store**

```sql
SELECT 
    e.event_name,
    e.start_date,
    e.attendees,
    e.sale
FROM events e
WHERE e.store_code = 'YOUR_STORE_CODE'
ORDER BY e.created_at DESC;
```

### **Get Event Summary with Actual Counts**

```sql
SELECT 
    e.id,
    e.event_name,
    e.attendees AS expected,
    COUNT(a.id) AS actual,
    e.sale
FROM events e
LEFT JOIN attendees a ON e.id = a.event_id
WHERE e.store_code = 'YOUR_STORE_CODE'
GROUP BY e.id, e.event_name, e.attendees, e.sale;
```

### **Store Performance Report**

```sql
SELECT 
    s.store_name,
    s.region,
    COUNT(e.id) AS total_events,
    SUM(e.attendees) AS total_attendees,
    SUM(e.sale) AS total_sales,
    AVG(e.sale) AS avg_sale
FROM stores s
LEFT JOIN events e ON s.store_code = e.store_code
GROUP BY s.store_code, s.store_name, s.region
ORDER BY total_sales DESC;
```

### **Find First-Time Customers**

```sql
SELECT 
    a.name,
    a.phone,
    e.event_name,
    e.start_date
FROM attendees a
JOIN events e ON a.event_id = e.id
WHERE a.first_time_at_tanishq = 1
ORDER BY a.created_at DESC;
```

### **Greeting Upload Status**

```sql
SELECT 
    CASE WHEN uploaded = 1 THEN 'Completed' ELSE 'Pending' END AS status,
    COUNT(*) AS count
FROM greetings
GROUP BY uploaded;
```

---

## 🛠️ MAINTENANCE CHECKLIST

### **Daily Tasks**
```
☐ Check application error logs
☐ Monitor database connections
☐ Verify backup job completion
```

### **Weekly Tasks**
```
☐ Review slow query log
☐ Check table sizes
☐ Verify data integrity
☐ Clean up abandoned greetings
```

### **Monthly Tasks**
```
☐ ANALYZE tables
☐ OPTIMIZE tables
☐ Review and archive old data
☐ Update indexes if needed
☐ Security audit
```

### **Quarterly Tasks**
```
☐ Create backup tables
☐ Performance review
☐ Schema review
☐ Capacity planning
```

---

## 📈 GROWTH METRICS

### **Table Growth Patterns**

```
events          ▓▓▓▓▓▓▓▓░░  ~1,000/month
attendees       ▓▓▓▓▓▓▓▓▓▓  ~10,000/month
invitees        ▓▓▓▓▓░░░░░  ~5,000/month
greetings       ▓▓▓▓░░░░░░  ~500/month
stores          ▓░░░░░░░░░  ~10/month (stable)
```

### **Storage Requirements**

```
Current Total:   ~5-10 GB (estimated)
Growth Rate:     ~500 MB/month
Recommendation:  Monitor and archive data older than 2 years
```

---

## 🎯 QUICK ACTION ITEMS

### **⚠️ IMMEDIATE (This Week)**
```
1. Run DATABASE_DIAGNOSTIC_QUERIES.sql
2. Review critical issues
3. Backup all tables
4. Implement password hashing
```

### **📋 SHORT TERM (This Month)**
```
1. Add missing indexes
2. Fix data type issues
3. Add NOT NULL constraints
4. Clean up orphaned records
```

### **📅 MEDIUM TERM (This Quarter)**
```
1. Implement soft delete
2. Add audit columns
3. Create useful views
4. Set up automated monitoring
```

---

## 📚 FILE REFERENCE

```
📄 DATABASE_COMPLETE_ANALYSIS.md     ← Complete detailed analysis
📄 DATABASE_DIAGNOSTIC_QUERIES.sql   ← Run to find issues
📄 DATABASE_FIX_SCRIPTS.sql          ← Execute to fix issues
📄 DATABASE_VISUAL_QUICK_REF.md      ← This file (quick reference)
📄 DATABASE_SCHEMA_DOCUMENTATION.md  ← Original documentation
📄 GREETING_DATABASE_SCHEMA.sql      ← Greeting module details
```

---

## 🔗 USEFUL LINKS

- **Entity Classes:** `src/main/java/com/dechub/tanishq/entity/`
- **Application Properties:** `src/main/resources/application-*.properties`
- **Database Config:** Spring JPA with Hibernate DDL Auto-Update

---

## 💡 PRO TIPS

1. **Always backup before changes:** Use CREATE TABLE ... AS SELECT
2. **Test in pre-prod first:** Never run untested queries in production
3. **Use transactions:** BEGIN; ... COMMIT; or ROLLBACK;
4. **Monitor query performance:** Use EXPLAIN to analyze queries
5. **Regular maintenance:** ANALYZE and OPTIMIZE tables monthly
6. **Index wisely:** Too many indexes slow down INSERT/UPDATE
7. **Archive old data:** Events older than 2 years
8. **Document changes:** Update this file when schema changes

---

**Last Updated:** February 4, 2026  
**Database Version:** MySQL 8.x  
**Application:** Spring Boot + Hibernate JPA  
**Status:** ✅ Analysis Complete - Fixes Pending

