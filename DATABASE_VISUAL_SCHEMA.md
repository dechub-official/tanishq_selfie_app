# 🎨 Database Visual Schema - Tanishq Selfie App

## 📊 Complete Database Structure at a Glance

---

## 🗂️ All 15 Tables Overview

```
╔════════════════════════════════════════════════════════════════╗
║                    TANISHQ SELFIE APP DATABASE                 ║
║                        MySQL 8.x / Hibernate JPA                ║
╚════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────────────────────┐
│                    🏢 CORE EVENT MANAGEMENT                      │
└─────────────────────────────────────────────────────────────────┘

┏━━━━━━━━━━━━━━━━━━━┓
┃     STORES        ┃
┣━━━━━━━━━━━━━━━━━━━┫
┃ 🔑 store_code     ┃ ◄──────────────┐
┃    store_name     ┃                │
┃    store_address  ┃                │
┃    store_city     ┃                │
┃    store_state    ┃                │
┃    region         ┃                │
┃    abm_username   ┃                │
┃    rbm_username   ┃                │
┃    cee_username   ┃                │
┗━━━━━━━━━━━━━━━━━━━┛                │
                                     │
                                     │ 1:N
                                     │
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━┓        │
┃        EVENTS             ┃────────┘
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃ 🔑 id (VARCHAR(255))      ┃ ◄─────────┐
┃ 🔗 store_code (FK)        ┃           │
┃    event_name             ┃           │
┃    event_type             ┃           │
┃    start_date             ┃           │
┃    region                 ┃           │ 1:N
┃    sale (DOUBLE)          ┃           │
┃    advance (DOUBLE)       ┃           │
┃    attendees (INT)        ┃           │
┃    invitees (INT)         ┃           │
┃    created_at             ┃           │
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━┛           │
                                        │
        ┌───────────────────────────────┴───────────────┐
        │                                               │
        │                                               │
┏━━━━━━━━━━━━━━━━━━━━━┓                  ┏━━━━━━━━━━━━━━━━━━━━━┓
┃    ATTENDEES        ┃                  ┃     INVITEES        ┃
┣━━━━━━━━━━━━━━━━━━━━━┫                  ┣━━━━━━━━━━━━━━━━━━━━━┫
┃ 🔑 id (BIGINT)      ┃                  ┃ 🔑 id (BIGINT)      ┃
┃ 🔗 event_id (FK)    ┃                  ┃ 🔗 event_id (FK)    ┃
┃    name             ┃                  ┃    name             ┃
┃    phone            ┃                  ┃    contact          ┃
┃    like             ┃                  ┃    created_at       ┃
┃    first_time       ┃                  ┗━━━━━━━━━━━━━━━━━━━━━┛
┃    rso_name         ┃
┃    created_at       ┃
┗━━━━━━━━━━━━━━━━━━━━━┛

┏━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃       GREETINGS           ┃
┣━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
┃ 🔑 id (BIGINT)            ┃
┃    unique_id              ┃
┃    greeting_text          ┃
┃    phone                  ┃
┃    message                ┃
┃    qr_code_data (LONGTEXT)┃
┃    drive_file_id          ┃
┃    created_at             ┃
┃    uploaded (BOOLEAN)     ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━┛


┌─────────────────────────────────────────────────────────────────┐
│                    👥 USER MANAGEMENT                            │
└─────────────────────────────────────────────────────────────────┘

┏━━━━━━━━━━━━━━━━━━━┓    ┏━━━━━━━━━━━━━━━━━━━━━┓
┃      USERS        ┃    ┃   USER_DETAILS      ┃
┣━━━━━━━━━━━━━━━━━━━┫    ┣━━━━━━━━━━━━━━━━━━━━━┫
┃ 🔑 id (BIGINT)    ┃    ┃ 🔑 id (BIGINT)      ┃
┃    name           ┃    ┃    name             ┃
┃    username       ┃    ┃    reason           ┃
┃    password       ┃    ┃    rso_name         ┃
┃    role (ABM/RBM) ┃    ┃    store_code       ┃
┃    email          ┃    ┃    date             ┃
┗━━━━━━━━━━━━━━━━━━━┛    ┃    my_first_diamond ┃
                         ┗━━━━━━━━━━━━━━━━━━━━━┛

┏━━━━━━━━━━━━━━━━━━━━━━━┓    ┏━━━━━━━━━━━━━━━━━━━━━━━┓
┃   BRIDE_DETAILS       ┃    ┃  PASSWORD_HISTORY     ┃
┣━━━━━━━━━━━━━━━━━━━━━━━┫    ┣━━━━━━━━━━━━━━━━━━━━━━━┫
┃ 🔑 id (BIGINT)        ┃    ┃ 🔑 btq_code           ┃
┃    bride_name         ┃    ┃    old_password       ┃
┃    bride_event        ┃    ┃    new_password       ┃
┃    email              ┃    ┃    changed_at         ┃
┃    phone              ┃    ┗━━━━━━━━━━━━━━━━━━━━━━━┛
┃    date               ┃
┃    bride_type         ┃
┃    zip_code           ┃
┗━━━━━━━━━━━━━━━━━━━━━━━┛


┌─────────────────────────────────────────────────────────────────┐
│                    🔐 AUTHENTICATION SYSTEM                      │
└─────────────────────────────────────────────────────────────────┘

┏━━━━━━━━━━━━━━━━━━━┓    ┏━━━━━━━━━━━━━━━━━━━┓    ┏━━━━━━━━━━━━━━━━━━━┓
┃    ABM_LOGIN      ┃    ┃    RBM_LOGIN      ┃    ┃    CEE_LOGIN      ┃
┣━━━━━━━━━━━━━━━━━━━┫    ┣━━━━━━━━━━━━━━━━━━━┫    ┣━━━━━━━━━━━━━━━━━━━┫
┃ 🔑 id (BIGINT)    ┃    ┃ 🔑 id (BIGINT)    ┃    ┃ 🔑 id (BIGINT)    ┃
┃    abm_user_id ⚡ ┃    ┃    rbm_user_id ⚡ ┃    ┃    cee_user_id ⚡ ┃
┃    abm_name       ┃    ┃    rbm_name       ┃    ┃    cee_name       ┃
┃    password       ┃    ┃    password       ┃    ┃    password       ┃
┃    email          ┃    ┃    email          ┃    ┃    email          ┃
┃    region         ┃    ┃    created_at     ┃    ┃    region         ┃
┃    created_at     ┃    ┃    updated_at     ┃    ┃    created_at     ┃
┃    updated_at     ┃    ┗━━━━━━━━━━━━━━━━━━━┛    ┃    updated_at     ┃
┗━━━━━━━━━━━━━━━━━━━┛                              ┗━━━━━━━━━━━━━━━━━━━┛
 Area Business Mgr      Regional Business Mgr    Customer Engagement Exec


┌─────────────────────────────────────────────────────────────────┐
│                    💍 PRODUCT / RIVAAH SYSTEM                    │
└─────────────────────────────────────────────────────────────────┘

                    ┏━━━━━━━━━━━━━━━━━━━━━┓
                    ┃      RIVAAH         ┃
                    ┣━━━━━━━━━━━━━━━━━━━━━┫
                    ┃ 🔑 id (BIGINT)      ┃ ◄────────┐
                    ┃    code             ┃          │
                    ┃    bride            ┃          │
                    ┃    event            ┃          │
                    ┃    clothing_type    ┃          │
                    ┃    tags             ┃          │
                    ┗━━━━━━━━━━━━━━━━━━━━━┛          │
                              │                      │
                              │                      │
                    ┌─────────┴─────────┐            │
                    │                   │            │
                    │ 1:N               │ 1:N        │
                    ▼                   ▼            │
    ┏━━━━━━━━━━━━━━━━━━━━━━━┓    ┏━━━━━━━━━━━━━━━━━━━━━┓
    ┃  PRODUCT_DETAILS      ┃    ┃   RIVAAH_USERS      ┃
    ┣━━━━━━━━━━━━━━━━━━━━━━━┫    ┣━━━━━━━━━━━━━━━━━━━━━┫
    ┃ 🔑 id (BIGINT)        ┃    ┃ 🔑 id (BIGINT)      ┃
    ┃ 🔗 rivaah_id (FK)     ┃────┘ 🔗 rivaah_id (FK)    ┃
    ┃    category           ┃      ┃    name             ┃
    ┃    image_link         ┃      ┃    contact          ┃
    ┃    product_link       ┃      ┃    source           ┃
    ┃    like_count (INT)   ┃      ┃    created_at       ┃
    ┗━━━━━━━━━━━━━━━━━━━━━━━┛      ┗━━━━━━━━━━━━━━━━━━━━━┛
```

---

## 🔑 Legend

- `🔑` = Primary Key
- `🔗` = Foreign Key
- `⚡` = Unique Constraint
- `1:N` = One-to-Many Relationship
- `(BIGINT)` = Auto-incremented ID
- `(VARCHAR(255))` = String with custom length

---

## 📈 Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER JOURNEY                             │
└─────────────────────────────────────────────────────────────────┘

Step 1: AUTHENTICATION
   ┌──────────────┐
   │ Manager Login│
   │ (ABM/RBM/CEE)│
   └──────┬───────┘
          ↓
   ┌──────────────────┐
   │ Check Credentials│
   │ in login tables  │
   └──────┬───────────┘
          ↓
   [Access Granted]

Step 2: STORE & EVENT SETUP
   ┌──────────────┐
   │ Select Store │───► STORES table
   └──────┬───────┘
          ↓
   ┌──────────────┐
   │ Create Event │───► EVENTS table
   └──────┬───────┘
          ↓
   ┌──────────────┐
   │ Add Invitees │───► INVITEES table
   └──────────────┘

Step 3: CUSTOMER INTERACTION
   ┌─────────────────┐
   │ Customer Scans  │
   │ Event QR Code   │
   └────────┬────────┘
            ↓
   ┌─────────────────┐
   │ Register at     │───► ATTENDEES table
   │ Event           │
   └────────┬────────┘
            ↓
   ┌─────────────────┐
   │ Upload Selfie   │───► USER_DETAILS table
   │                 │      + File Storage
   └────────┬────────┘
            ↓
   ┌─────────────────┐
   │ Create Greeting │───► GREETINGS table
   │ Card with QR    │
   └─────────────────┘

Step 4: RIVAAH COLLECTION
   ┌─────────────────┐
   │ Bride Registers │───► BRIDE_DETAILS table
   └────────┬────────┘
            ↓
   ┌─────────────────┐
   │ Create Rivaah   │───► RIVAAH table
   │ Collection      │
   └────────┬────────┘
            ↓
   ┌─────────────────┐        ┌─────────────────┐
   │ Add Products    │────────┤Add Collection   │
   │                 │───►    │ Users           │
   └─────────────────┘        └─────────────────┘
            │                          │
            ↓                          ↓
   PRODUCT_DETAILS table      RIVAAH_USERS table

Step 5: ANALYTICS & REPORTING
   ┌─────────────────────────────────┐
   │ Managers View Dashboards        │
   │ - Event performance             │
   │ - Store analytics               │
   │ - Regional metrics              │
   │ - Sales reports                 │
   └─────────────────────────────────┘
            ↓
   ┌─────────────────────────────────┐
   │ Data Export to Google Sheets    │
   └─────────────────────────────────┘
```

---

## 🎯 Key Business Relationships

### Event Management Flow
```
STORE → Creates → EVENT → Has → INVITEES
                    ↓
                    ↓
              Records → ATTENDEES
                    ↓
              Tracks → SALES, METRICS
```

### User Authentication Hierarchy
```
CEE (Customer Engagement Executive)
  ↓
RBM (Regional Business Manager)
  ↓
ABM (Area Business Manager)
  ↓
STORES (Multiple stores per manager)
```

### Rivaah Collection Structure
```
BRIDE → Creates → RIVAAH COLLECTION
                      ↓
          ┌───────────┴───────────┐
          ↓                       ↓
    PRODUCT_DETAILS        RIVAAH_USERS
    (Catalog Items)        (Interested Users)
```

---

## 📊 Table Size Estimates

| Table | Typical Records | Growth Rate |
|-------|----------------|-------------|
| stores | ~500 | Slow (new stores) |
| events | ~10,000+ | High (daily events) |
| attendees | ~100,000+ | Very High (per event) |
| invitees | ~50,000+ | High (per event) |
| users | ~100 | Slow (staff only) |
| abm_login | ~20-50 | Slow |
| rbm_login | ~10-20 | Slow |
| cee_login | ~50-100 | Slow |
| greetings | ~50,000+ | High |
| bride_details | ~5,000+ | Medium |
| rivaah | ~1,000+ | Medium |
| product_details | ~10,000+ | Medium |
| rivaah_users | ~20,000+ | Medium |

---

## 🔧 Index Strategy

### Primary Indexes (Automatically Created)
- All `id` columns (BIGINT with AUTO_INCREMENT)
- `store_code` in STORES
- `btq_code` in PASSWORD_HISTORY

### Foreign Key Indexes (JPA Creates)
- `event_id` in ATTENDEES
- `event_id` in INVITEES
- `store_code` in EVENTS
- `rivaah_id` in PRODUCT_DETAILS
- `rivaah_id` in RIVAAH_USERS

### Recommended Additional Indexes
```sql
-- For faster event queries by date
CREATE INDEX idx_events_start_date ON events(start_date);

-- For phone number lookups
CREATE INDEX idx_attendees_phone ON attendees(phone);

-- For region-based queries
CREATE INDEX idx_events_region ON events(region);
CREATE INDEX idx_stores_region ON stores(region);

-- For authentication
CREATE INDEX idx_users_username ON users(username);
```

---

## 💾 Storage Considerations

### Large Data Columns
- `greetings.qr_code_data` - LONGTEXT (base64 images)
- Can grow very large, consider moving to file storage

### File Storage Locations
- Selfie Images: `/opt/tanishq/storage/selfie_images/`
- Bride Uploads: `/opt/tanishq/storage/bride_uploads/`
- Base Images: `/opt/tanishq/storage/base.jpg`

---

**Visual Schema Version:** 1.0  
**Last Updated:** January 24, 2026  
**Created By:** Database Analysis Tool

