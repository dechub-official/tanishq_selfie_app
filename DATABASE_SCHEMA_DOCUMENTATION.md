# 🗄️ Tanishq Selfie App - Complete Database Schema Documentation

**Date:** January 24, 2026  
**Database:** MySQL 8.x  
**Application:** Tanishq Selfie App (Spring Boot)  
**ORM:** Hibernate JPA

---

## 📊 Database Overview

### Database Environments

1. **Local Development**
   - Database: `tanishq`
   - Host: `localhost:3306`
   - User: `nagaraj_jadar`

2. **Pre-Production**
   - Database: `selfie_preprod`
   - Host: `localhost:3306`
   - User: `root`

3. **Production**
   - Database: `selfie_prod`
   - Host: `10.10.63.97:3306`
   - User: `root`

---

## 📑 Database Tables Summary

The application uses **15 tables** organized into the following categories:

### Core Event Management (5 tables)
1. `events` - Main event records
2. `attendees` - Event attendees/participants
3. `invitees` - Event invitations
4. `stores` - Store information
5. `greetings` - Greeting cards/messages

### User Management (4 tables)
6. `users` - General user accounts
7. `user_details` - Additional user information
8. `bride_details` - Bride-specific data
9. `password_history` - Password change tracking

### Authentication (3 tables)
10. `abm_login` - Area Business Manager logins
11. `rbm_login` - Regional Business Manager logins
12. `cee_login` - Customer Engagement Executive logins

### Product/Rivaah System (3 tables)
13. `rivaah` - Rivaah collection data
14. `rivaah_users` - Rivaah system users
15. `product_details` - Product catalog

---

## 🏗️ Detailed Table Schemas

---

### 1️⃣ **EVENTS Table**

**Purpose:** Stores all event information created by stores

**Table Name:** `events`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | VARCHAR(255) | PRIMARY KEY | Event ID (format: storeCode_uuid) |
| `created_at` | DATETIME | | Event creation timestamp |
| `region` | VARCHAR | | Geographic region |
| `event_type` | VARCHAR | | Type of event (Wedding, Festival, etc.) |
| `event_sub_type` | VARCHAR | | Sub-category of event |
| `event_name` | VARCHAR | | Display name of event |
| `rso` | VARCHAR | | Regional Sales Officer name |
| `start_date` | VARCHAR | | Event start date |
| `image` | VARCHAR | | Event promotional image path |
| `invitees` | INT | | Number of invited people |
| `attendees` | INT | | Number of actual attendees |
| `completed_events_drive_link` | VARCHAR | | Google Drive link for event files |
| `community` | VARCHAR | | Target community for event |
| `location` | VARCHAR | | Event location/venue |
| `attendees_uploaded` | BOOLEAN | | Flag if attendees list uploaded |
| `sale` | DOUBLE | | Total sales from event |
| `advance` | DOUBLE | | Advance payments received |
| `ghs_or_rga` | DOUBLE | | GHS/RGA metrics |
| `gmb` | DOUBLE | | GMB (Google My Business) metrics |
| `diamond_awareness` | BOOLEAN | | Diamond awareness campaign flag |
| `ghs_flag` | BOOLEAN | | GHS flag indicator |
| `store_code` | VARCHAR | FOREIGN KEY → stores(storeCode) | Associated store |

#### Relationships
- **Many-to-One:** with `stores` (event belongs to one store)
- **One-to-Many:** with `attendees` (event has many attendees)
- **One-to-Many:** with `invitees` (event has many invitees)

#### Indexes
- PRIMARY: `id`
- FOREIGN KEY: `store_code` → `stores(storeCode)`

---

### 2️⃣ **STORES Table**

**Purpose:** Master data for all Tanishq store locations

**Table Name:** `stores`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `store_code` | VARCHAR | PRIMARY KEY | Unique store identifier (BTQ code) |
| `store_name` | VARCHAR | | Store display name |
| `store_address` | VARCHAR | | Full street address |
| `store_city` | VARCHAR | | City name |
| `store_state` | VARCHAR | | State name |
| `store_country` | VARCHAR | | Country name |
| `store_zip_code` | VARCHAR | | Postal code |
| `store_phone_no_one` | VARCHAR | | Primary phone number |
| `store_phone_no_two` | VARCHAR | | Secondary phone number |
| `store_email_id` | VARCHAR | | Store email address |
| `store_latitude` | VARCHAR | | Geographic latitude |
| `store_longitude` | VARCHAR | | Geographic longitude |
| `store_date_of_opening` | VARCHAR | | Store opening date |
| `store_type` | VARCHAR | | Type/category of store |
| `store_opening_time` | VARCHAR | | Daily opening time |
| `store_closing_time` | VARCHAR | | Daily closing time |
| `store_manager_name` | VARCHAR | | Store manager name |
| `store_manager_no` | VARCHAR | | Manager phone number |
| `store_manager_email` | VARCHAR | | Manager email |
| `store_location_link` | VARCHAR | | Google Maps link |
| `languages` | VARCHAR | | Languages spoken (comma-separated) |
| `parking` | VARCHAR | | Parking facilities |
| `payment` | VARCHAR | | Payment methods accepted |
| `kakatiya_store` | VARCHAR | | Kakatiya store flag |
| `celeste_store` | VARCHAR | | Celeste collection flag |
| `rating` | VARCHAR | | Store rating |
| `number_of_ratings` | VARCHAR | | Total ratings count |
| `is_collection` | VARCHAR | | Collection store flag |
| `region` | VARCHAR | | Store region (North1, South2, etc.) |
| `level` | VARCHAR | | Store tier/level |
| `abm_username` | VARCHAR | | Area Business Manager username |
| `rbm_username` | VARCHAR | | Regional Business Manager username |
| `cee_username` | VARCHAR | | Customer Engagement Executive username |

#### Relationships
- **One-to-Many:** with `events` (store can have multiple events)

#### Business Rules
- Store code is the primary identifier used across the system
- Each store is assigned to regional managers (ABM, RBM, CEE)
- Stores can create and manage their own events

---

### 3️⃣ **ATTENDEES Table**

**Purpose:** Tracks people who attended events

**Table Name:** `attendees`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique attendee record ID |
| `name` | VARCHAR | | Attendee name |
| `phone` | VARCHAR | | Contact phone number |
| `like` | VARCHAR | | What they liked/preferred |
| `first_time_at_tanishq` | BOOLEAN | | First visit to Tanishq flag |
| `created_at` | DATETIME | | Registration timestamp |
| `is_uploaded_from_excel` | BOOLEAN | | Imported from Excel flag |
| `rso_name` | VARCHAR | | Regional Sales Officer name |
| `event_id` | VARCHAR(255) | FOREIGN KEY → events(id), NOT NULL | Associated event |

#### Relationships
- **Many-to-One:** with `events` (multiple attendees per event)

#### Indexes
- PRIMARY: `id`
- FOREIGN KEY: `event_id` → `events(id)`

---

### 4️⃣ **INVITEES Table**

**Purpose:** Stores event invitation lists

**Table Name:** `invitees`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique invitee record ID |
| `name` | VARCHAR | | Invitee name |
| `contact` | VARCHAR | | Contact information |
| `created_at` | DATETIME | | Invitation creation time |
| `event_id` | VARCHAR(255) | FOREIGN KEY → events(id), NOT NULL | Associated event |

#### Relationships
- **Many-to-One:** with `events` (multiple invitees per event)

#### Indexes
- PRIMARY: `id`
- FOREIGN KEY: `event_id` → `events(id)`

---

### 5️⃣ **USERS Table**

**Purpose:** General user authentication and roles

**Table Name:** `users`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique user ID |
| `name` | VARCHAR | | Full name |
| `username` | VARCHAR | | Login username |
| `password` | VARCHAR | | Encrypted password |
| `role` | VARCHAR | | User role (ABM, RBM, CEE) |
| `email` | VARCHAR | | Email address |

#### User Roles
- **ABM** - Area Business Manager
- **RBM** - Regional Business Manager  
- **CEE** - Customer Engagement Executive

---

### 6️⃣ **USER_DETAILS Table**

**Purpose:** Extended user information and selfie upload data

**Table Name:** `user_details`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique record ID |
| `name` | VARCHAR | | User name |
| `reason` | VARCHAR | | Reason for visit/upload |
| `rso_name` | VARCHAR | | Regional Sales Officer |
| `store_code` | VARCHAR | | Store code where visit occurred |
| `date` | DATE | | Visit/upload date |
| `my_first_diamond` | VARCHAR | | First diamond purchase flag |

---

### 7️⃣ **BRIDE_DETAILS Table**

**Purpose:** Stores bride-specific information for wedding events

**Table Name:** `bride_details`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique bride record ID |
| `bride_name` | VARCHAR | | Bride's name |
| `bride_event` | VARCHAR | | Wedding event type |
| `email` | VARCHAR | | Email address |
| `phone` | VARCHAR | | Contact number |
| `date` | DATE | | Wedding/event date |
| `bride_type` | VARCHAR | | Category/type of bride |
| `zip_code` | VARCHAR | | Postal code |

---

### 8️⃣ **GREETINGS Table**

**Purpose:** Stores personalized greeting cards with QR codes

**Table Name:** `greetings`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique greeting ID |
| `unique_id` | VARCHAR | | Unique identifier for greeting |
| `greeting_text` | VARCHAR | | Greeting message/name |
| `phone` | VARCHAR | | Recipient phone number |
| `message` | VARCHAR | | Custom message |
| `qr_code_data` | LONGTEXT | | QR code image data (base64) |
| `drive_file_id` | VARCHAR | | Google Drive file ID |
| `created_at` | DATETIME | | Creation timestamp |
| `uploaded` | BOOLEAN | | Upload status flag |

---

### 9️⃣ **ABM_LOGIN Table**

**Purpose:** Area Business Manager authentication

**Table Name:** `abm_login`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique record ID |
| `abm_user_id` | VARCHAR | UNIQUE, NOT NULL | ABM username |
| `abm_name` | VARCHAR | | Full name |
| `password` | VARCHAR | | Encrypted password |
| `email` | VARCHAR | | Email address |
| `region` | VARCHAR | | Assigned region |
| `created_at` | DATETIME | | Account creation time |
| `updated_at` | DATETIME | | Last update time |

---

### 🔟 **RBM_LOGIN Table**

**Purpose:** Regional Business Manager authentication

**Table Name:** `rbm_login`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique record ID |
| `rbm_user_id` | VARCHAR | UNIQUE, NOT NULL | RBM username |
| `rbm_name` | VARCHAR | | Full name |
| `password` | VARCHAR | | Encrypted password |
| `email` | VARCHAR | | Email address |
| `created_at` | DATETIME | | Account creation time |
| `updated_at` | DATETIME | | Last update time |

---

### 1️⃣1️⃣ **CEE_LOGIN Table**

**Purpose:** Customer Engagement Executive authentication

**Table Name:** `cee_login`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique record ID |
| `cee_user_id` | VARCHAR | UNIQUE, NOT NULL | CEE username |
| `cee_name` | VARCHAR | | Full name |
| `password` | VARCHAR | | Encrypted password |
| `email` | VARCHAR | | Email address |
| `region` | VARCHAR | | Assigned region |
| `created_at` | DATETIME | | Account creation time |
| `updated_at` | DATETIME | | Last update time |

---

### 1️⃣2️⃣ **PASSWORD_HISTORY Table**

**Purpose:** Tracks password changes for audit purposes

**Table Name:** `password_history`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `btq_code` | VARCHAR | PRIMARY KEY, NOT NULL | Store/user code |
| `old_password` | VARCHAR | | Previous password (encrypted) |
| `new_password` | VARCHAR | NOT NULL | New password (encrypted) |
| `changed_at` | DATETIME | NOT NULL | Change timestamp |

---

### 1️⃣3️⃣ **RIVAAH Table**

**Purpose:** Rivaah bridal collection management

**Table Name:** `rivaah`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique rivaah ID |
| `code` | VARCHAR | | 10-digit rivaah code |
| `bride` | VARCHAR | | Bride name |
| `event` | VARCHAR | | Event type |
| `clothing_type` | VARCHAR | | Clothing preference |
| `tags` | VARCHAR | | Tags (comma-separated) |

#### Relationships
- **One-to-Many:** with `product_details` (rivaah has multiple products)
- **One-to-Many:** with `rivaah_users` (rivaah has multiple users)

---

### 1️⃣4️⃣ **RIVAAH_USERS Table**

**Purpose:** Users associated with Rivaah collections

**Table Name:** `rivaah_users`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique record ID |
| `name` | VARCHAR | | User name |
| `contact` | VARCHAR | | Contact information |
| `source` | VARCHAR | | Lead source |
| `created_at` | DATETIME | | Registration time |
| `rivaah_id` | BIGINT | FOREIGN KEY → rivaah(id) | Associated rivaah collection |

#### Relationships
- **Many-to-One:** with `rivaah`

---

### 1️⃣5️⃣ **PRODUCT_DETAILS Table**

**Purpose:** Product catalog for Rivaah collections

**Table Name:** `product_details`

#### Columns

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique product ID |
| `category` | VARCHAR | | Product category |
| `image_link` | VARCHAR | | Product image URL |
| `product_link` | VARCHAR | | Product page URL |
| `like_count` | INT | | Number of likes |
| `rivaah_id` | BIGINT | FOREIGN KEY → rivaah(id) | Associated rivaah collection |

#### Relationships
- **Many-to-One:** with `rivaah`

---

## 🔗 Entity Relationship Diagram (ERD)

```
┌─────────────┐
│   STORES    │
│             │
│ store_code  │◄────┐
│ store_name  │     │
│ region      │     │ Many Events
│ abm_username│     │ per Store
│ rbm_username│     │
└─────────────┘     │
                    │
                    │
┌─────────────────┐ │
│     EVENTS      │─┘
│                 │
│ id (PK)         │◄───────┐
│ store_code (FK) │        │
│ event_name      │        │ Many Attendees
│ event_type      │        │ per Event
│ start_date      │        │
│ sale            │        │
└─────────────────┘        │
        │                  │
        │                  │
        │         ┌────────────────┐
        │         │   ATTENDEES    │
        │         │                │
        │         │ id (PK)        │
        │         │ event_id (FK)  │
        └─────────│ name           │
                  │ phone          │
        ┌─────────│ created_at     │
        │         └────────────────┘
        │
        │         ┌────────────────┐
        │         │   INVITEES     │
        │         │                │
        │         │ id (PK)        │
        └─────────│ event_id (FK)  │
                  │ name           │
                  │ contact        │
                  └────────────────┘

┌──────────────┐
│    RIVAAH    │
│              │
│ id (PK)      │◄────────┐
│ code         │         │
│ bride        │         │ Many Products
│ event        │         │ per Rivaah
└──────────────┘         │
        │                │
        │       ┌────────────────────┐
        │       │ PRODUCT_DETAILS    │
        │       │                    │
        │       │ id (PK)            │
        └───────│ rivaah_id (FK)     │
                │ category           │
        ┌───────│ image_link         │
        │       │ like_count         │
        │       └────────────────────┘
        │
        │       ┌────────────────────┐
        │       │  RIVAAH_USERS      │
        │       │                    │
        │       │ id (PK)            │
        └───────│ rivaah_id (FK)     │
                │ name               │
                │ contact            │
                └────────────────────┘

┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  ABM_LOGIN   │   │  RBM_LOGIN   │   │  CEE_LOGIN   │
│              │   │              │   │              │
│ abm_user_id  │   │ rbm_user_id  │   │ cee_user_id  │
│ password     │   │ password     │   │ password     │
│ region       │   │ email        │   │ region       │
└──────────────┘   └──────────────┘   └──────────────┘
```

---

## 🔄 Data Flow & Business Logic

### Event Creation Flow
1. Store manager logs in using ABM/RBM/CEE credentials
2. Creates event in `events` table with store_code
3. Adds invitees to `invitees` table
4. Event goes live with QR code for registration

### Attendee Registration Flow
1. Customer scans QR code at event
2. Fills registration form
3. Record created in `attendees` table
4. Linked to event via event_id
5. Data synced to Google Sheets

### Data Sync with Google Sheets
- Application periodically syncs data to/from Google Sheets
- Configured sheet IDs in `application-preprod.properties`
- Uses Google Service Account for authentication

---

## 📈 Database Statistics & Insights

### Key Metrics Tracked
- **Sales Data:** Per-event sales, advances, GMB metrics
- **Attendance:** Invitees vs actual attendees
- **Customer Data:** First-time visitors, preferences
- **Regional Performance:** Store-wise, region-wise analysis

### Reporting Capabilities
- Event-wise performance reports
- Store-wise sales analysis
- Regional manager dashboards
- Attendee demographics

---

## 🔒 Security Considerations

### Password Storage
- All passwords stored encrypted
- Password history tracked in `password_history`
- Separate login tables for different roles

### Data Access
- Role-based access control (ABM, RBM, CEE)
- Managers can only access their assigned regions/stores
- Event data linked to specific stores

---

## 🛠️ Database Maintenance

### Backup Strategy
```bash
# Backup pre-prod database
mysqldump -u root -pDechub#2025 selfie_preprod > backup_$(date +%Y%m%d).sql

# Backup production database
mysqldump -u root -pNagaraj@07 selfie_prod > backup_prod_$(date +%Y%m%d).sql
```

### Common Queries

#### Get all events for a store
```sql
SELECT * FROM events WHERE store_code = 'BTQ123';
```

#### Get event attendees
```sql
SELECT a.* FROM attendees a
JOIN events e ON a.event_id = e.id
WHERE e.id = 'BTQ123_uuid';
```

#### Store performance
```sql
SELECT s.store_name, COUNT(e.id) as total_events, SUM(e.sale) as total_sales
FROM stores s
LEFT JOIN events e ON s.store_code = e.store_code
GROUP BY s.store_code;
```

---

## 📞 Database Connection Information

### Connection via MySQL CLI

**Pre-Production:**
```bash
mysql -h localhost -P 3306 -u root -pDechub#2025 selfie_preprod
```

**Production:**
```bash
mysql -h 10.10.63.97 -P 3306 -u root -pNagaraj@07 selfie_prod
```

### JDBC Connection String

**Pre-Production:**
```
jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false&serverTimezone=UTC
```

**Production:**
```
jdbc:mysql://10.10.63.97:3306/selfie_prod?useSSL=false&serverTimezone=UTC
```

---

## 📝 Notes

- Database uses MySQL 8.x dialect
- Hibernate auto-generates tables based on JPA entities
- All timestamps stored in server timezone
- File uploads stored in `/opt/tanishq/storage/`
- QR codes stored as base64 LONGTEXT in database

---

**Document Version:** 1.0  
**Last Updated:** January 24, 2026  
**Maintained By:** Development Team

