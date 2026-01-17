# 📊 DATA FLOW ARCHITECTURE - Tanishq Celebrations

## 🎯 System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         TANISHQ CELEBRATIONS SYSTEM                       │
│                    (Spring Boot Application - Port 3000/3001)             │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌──────────────┐          ┌──────────────────┐        ┌────────────────┐
│   FRONTEND   │          │   REST API       │        │  SCHEDULERS    │
│  (React SPA) │          │  (Controllers)   │        │  (Background)  │
└──────────────┘          └──────────────────┘        └────────────────┘
        │                           │                           │
        └───────────────────────────┼───────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────┐
                    │    SERVICE LAYER          │
                    │  - TanishqPageService     │
                    │  - GreetingService        │
                    │  - StoreServices          │
                    │  - S3Service              │
                    └───────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌──────────────┐          ┌──────────────────┐        ┌────────────────┐
│  JPA REPOS   │          │  CACHE LAYER     │        │ EXTERNAL APIs  │
│ (15 Repos)   │          │ StoreSummaryCache│        │ Google/AWS/Mail│
└──────────────┘          └──────────────────┘        └────────────────┘
        │                           │                           │
        └───────────────────────────┼───────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
                    ▼                               ▼
        ┌────────────────────┐          ┌──────────────────────┐
        │   MYSQL DATABASE   │          │   FILE STORAGE       │
        │   (15 Tables)      │          │  Local/S3/Drive      │
        └────────────────────┘          └──────────────────────┘
```

---

## 🔄 DATA FLOW PATTERNS

### 1. **USER CREATES EVENT** (Write Operation)

```
┌──────────┐
│  User    │ Creates new event via web form
└────┬─────┘
     │ POST /events/create
     ▼
┌──────────────────────┐
│ EventsController     │ Receives HTTP request
└────┬─────────────────┘
     │ eventDTO
     ▼
┌──────────────────────┐
│ TanishqPageService   │ Business logic validation
└────┬─────────────────┘
     │ Event entity
     ▼
┌──────────────────────┐
│ EventRepository      │ JPA save()
└────┬─────────────────┘
     │ INSERT INTO events
     ▼
┌──────────────────────┐
│ MySQL: events table  │ Data persisted
└──────────────────────┘
     │
     ├─→ Generate QR Code (events.qr.base.url + eventId)
     ├─→ Send Email Notifications (if configured)
     └─→ Update Cache (invalidate/refresh)
```

### 2. **USER UPLOADS EVENT IMAGES** (File Upload)

```
┌──────────┐
│  User    │ Uploads images/videos
└────┬─────┘
     │ POST /events/upload (multipart/form-data)
     ▼
┌──────────────────────┐
│ EventsController     │ Receives file upload
└────┬─────────────────┘
     │ MultipartFile
     ▼
┌──────────────────────┐
│ TanishqPageService   │ Determine storage strategy
└────┬─────────────────┘
     │
     ├─→ LOCAL DEVELOPMENT
     │   └─→ ./storage/selfie_images/event_timestamp.jpg
     │
     └─→ PRODUCTION
         └─→ S3Service.uploadEventFile()
             │
             ▼
         ┌──────────────────────┐
         │ AWS S3               │
         │ celebrations-tanishq │
         │ /events/{eventId}/   │
         └──────────────────────┘
                 │
                 │ Returns S3 URL
                 ▼
         ┌──────────────────────┐
         │ Update Event entity  │
         │ completedEventsDrive │
         │ Link = S3 URL        │
         └──────────────────────┘
                 │
                 ▼
         ┌──────────────────────┐
         │ MySQL: events table  │
         │ UPDATE completed_    │
         │ events_drive_link    │
         └──────────────────────┘
```

### 3. **VIEWING STORE SUMMARIES** (Read with Cache)

```
┌──────────┐
│  RBM     │ Views dashboard (Regional Business Manager)
└────┬─────┘
     │ GET /stores/summary?rbm={username}
     ▼
┌──────────────────────┐
│ TanishqPageController│
└────┬─────────────────┘
     │
     ▼
┌──────────────────────┐
│ TanishqPageService   │ Check cache first
└────┬─────────────────┘
     │
     ▼
┌──────────────────────┐
│ StoreSummaryCache    │ In-memory cache
└────┬─────────────────┘
     │
     ├─→ CACHE HIT ──────────────┐
     │   └─→ Return cached data  │
     │                            │
     └─→ CACHE MISS               │
         │                        │
         ▼                        │
    ┌────────────────┐           │
    │ Query Database │           │
    │                │           │
    │ 1. StoreRepo   │           │
    │ 2. EventRepo   │           │
    │ 3. AttendeeRepo│           │
    │                │           │
    └────┬───────────┘           │
         │                        │
         ├─→ Aggregate data       │
         ├─→ Calculate metrics    │
         └─→ Update cache         │
             │                    │
             └────────────────────┘
                      │
                      ▼
              ┌──────────────┐
              │ Return JSON  │
              │ to Frontend  │
              └──────────────┘
```

### 4. **SCHEDULED CACHE REFRESH** (Background Job)

```
┌──────────────────────┐
│ Spring Scheduler     │ Runs every 6 hours
└────┬─────────────────┘
     │ @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
     ▼
┌──────────────────────┐
│StoreSummaryScheduler │
└────┬─────────────────┘
     │
     ▼
For each cached RBM username:
     │
     ├─→ TanishqPageService.fetchStoreSummariesByRbmParallel()
     │   │
     │   ├─→ Query all stores for RBM
     │   ├─→ Query all events per store
     │   ├─→ Count attendees/invitees
     │   └─→ Aggregate statistics
     │
     └─→ StoreSummaryCache.put(rbm, summary)
         │
         └─→ Cache refreshed (reduces DB load)
```

---

## 🗄️ DATABASE SCHEMA RELATIONSHIPS

```
┌─────────────────┐
│     STORES      │ (PK: storeCode)
├─────────────────┤
│ storeCode       │◄──────┐
│ storeName       │       │
│ region          │       │
│ abmUsername     │       │ @ManyToOne
│ rbmUsername     │       │
│ ceeUsername     │       │
└─────────────────┘       │
                          │
                          │
┌─────────────────┐       │
│     EVENTS      │ (PK: id = storeCode_uuid)
├─────────────────┤       │
│ id              │       │
│ storeCode       │───────┘
│ eventName       │
│ eventType       │◄──────┐
│ startDate       │       │
│ region          │       │ @ManyToOne
│ invitees        │       │
│ attendees       │       │
│ completed...Link│       │
└─────────────────┘       │
        ▲                 │
        │                 │
        ├─────────────────┤
        │                 │
┌───────┴────────┐  ┌─────┴──────┐
│   ATTENDEES    │  │  INVITEES  │
├────────────────┤  ├────────────┤
│ id             │  │ id         │
│ eventId        │──┘│ eventId    │──┘
│ name           │   │ name       │
│ phone          │   │ email      │
│ registeredAt   │   │ invitedAt  │
└────────────────┘   └────────────┘


┌─────────────────┐
│     USERS       │ (System Users)
├─────────────────┤
│ id              │
│ username        │
│ password        │◄─────┐
│ role            │      │ Track history
│ email           │      │
└─────────────────┘      │
                         │
                  ┌──────┴─────────┐
                  │ PASSWORD_      │
                  │ HISTORY        │
                  ├────────────────┤
                  │ userId         │
                  │ oldPassword    │
                  │ changedAt      │
                  └────────────────┘

┌─────────────────┐  ┌─────────────┐  ┌─────────────┐
│   ABM_LOGIN     │  │  RBM_LOGIN  │  │  CEE_LOGIN  │
├─────────────────┤  ├─────────────┤  ├─────────────┤
│ username        │  │ username    │  │ username    │
│ password        │  │ password    │  │ password    │
│ region          │  │ region      │  │ storeCode   │
└─────────────────┘  └─────────────┘  └─────────────┘
    (Area)           (Regional)         (Store)


┌─────────────────┐
│ BRIDE_DETAILS   │
├─────────────────┤
│ id              │
│ brideName       │
│ email           │
│ phone           │
│ date            │
│ brideType       │
└─────────────────┘


┌─────────────────┐       ┌─────────────────┐
│    RIVAAH       │       │  RIVAAH_USER    │
├─────────────────┤       ├─────────────────┤
│ id              │       │ id              │
│ collectionName  │       │ userName        │
│ details         │       │ preferences     │
└─────────────────┘       └─────────────────┘


┌─────────────────┐
│   GREETINGS     │
├─────────────────┤
│ id              │
│ uniqueId        │ (for QR code)
│ greetingText    │
│ phone           │
│ qrCodeData      │ (LONGTEXT - base64 image)
│ driveFileId     │ (video URL)
│ createdAt       │
│ uploaded        │
└─────────────────┘


┌─────────────────┐
│PRODUCT_DETAIL   │
├─────────────────┤
│ id              │
│ productName     │
│ category        │
│ checklistType   │
└─────────────────┘
```

---

## 🔗 EXTERNAL INTEGRATIONS

### 1. **Google Sheets Integration**

```
┌──────────────────────┐
│ Application          │
└────┬─────────────────┘
     │ Reads configuration/reference data
     ▼
┌──────────────────────────────────────┐
│ Google Sheets API                    │
│ Service Account Authentication       │
│ tanishq-app@tanishqgmb.iam...       │
└────┬─────────────────────────────────┘
     │
     ├─→ User Details Sheet (1vSG8T8rRm5jge_j-...)
     ├─→ Bride Details Sheet (13C0M-v8tZQp...)
     ├─→ Store Details Sheet (1Y3ieu2Fz0E...)
     ├─→ Events Details Sheet (1ZKb4rqIon5...)
     ├─→ Checklist Products Sheet
     ├─→ Rivaah Details Sheet
     └─→ Greetings Sheet

Purpose:
- Master data reference
- Configuration management
- External data sync (not primary storage)
```

### 2. **AWS S3 Storage**

```
┌──────────────────────┐
│ Application (Prod)   │
│ S3Service.java       │
└────┬─────────────────┘
     │ IAM Role Authentication
     │ (EC2 Instance Profile)
     ▼
┌──────────────────────────────────────┐
│ AWS S3                               │
│ Bucket: celebrations-tanishq         │
│ Region: ap-south-1 (Mumbai)          │
└────┬─────────────────────────────────┘
     │
     └─→ events/
         ├─→ {eventId}/
         │   ├─→ event_20260114_*.jpg
         │   └─→ event_20260114_*.mp4
         └─→ ...

Returns: https://celebrations-tanishq.s3.ap-south-1.amazonaws.com/...
```

### 3. **Email Service (SMTP)**

```
┌──────────────────────┐
│ Application          │
│ Spring Mail          │
└────┬─────────────────┘
     │ STARTTLS Connection
     ▼
┌──────────────────────────────────────┐
│ Microsoft Office365 SMTP             │
│ smtp.office365.com:587               │
│ tanishqcelebrations@titan.co.in      │
└──────────────────────────────────────┘
     │
     └─→ Send notifications
         - Event invitations
         - Registration confirmations
         - Appointment bookings
```

### 4. **Titan Book Appointment API**

```
┌──────────────────────┐
│ Application          │
└────┬─────────────────┘
     │ HTTPS POST
     │ Basic Auth (Titan_Mule)
     ▼
┌──────────────────────────────────────┐
│ Titan ACE Mule API                   │
│ https://acemule.titan.co.in/ecomm/   │
│ bookAnAppointment                    │
└──────────────────────────────────────┘
     │
     └─→ Books customer appointments
         at Tanishq stores
```

---

## 📊 DATA PERSISTENCE STRATEGIES

### Environment-Specific Storage

```
┌─────────────────────────────────────────────────────────┐
│                    LOCAL DEVELOPMENT                     │
├─────────────────────────────────────────────────────────┤
│ Database: localhost:3306/tanishq                        │
│ Storage:  ./storage/                                    │
│           - selfie_images/                              │
│           - bride_uploads/                              │
│ Sheets:   Test Google Sheets                            │
│ S3:       Not configured                                │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                      PRE-PRODUCTION                      │
├─────────────────────────────────────────────────────────┤
│ Database: localhost:3306/selfie_preprod                 │
│ Storage:  /opt/tanishq/storage/                         │
│ Sheets:   Pre-prod Google Sheets                        │
│ S3:       celebrations-tanishq (enabled)                │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                       PRODUCTION                         │
├─────────────────────────────────────────────────────────┤
│ Database: localhost:3306/selfie_prod                    │
│ Storage:  /opt/tanishq/storage/                         │
│ Sheets:   Production Google Sheets                      │
│ S3:       celebrations-tanishq (enabled)                │
│ Email:    tanishqcelebrations@titan.co.in (enabled)     │
│ APIs:     Titan appointment API (enabled)               │
└─────────────────────────────────────────────────────────┘
```

---

## 🔄 MIGRATION DATA FLOW

### Pre-Prod to Production Migration

```
┌─────────────────────────┐
│ PRE-PROD DATABASE       │
│ selfie_preprod          │
└────┬────────────────────┘
     │ mysqldump
     ▼
┌─────────────────────────┐
│ Export File             │
│ preprod_export.sql      │
└────┬────────────────────┘
     │
     ├─→ Backup
     │   └─→ /opt/tanishq/backups/preprod_export_20260114.sql
     │
     └─→ Import
         │ mysql selfie_prod < preprod_export.sql
         ▼
     ┌─────────────────────────┐
     │ PRODUCTION DATABASE     │
     │ selfie_prod             │
     └─────────────────────────┘
             │
             ├─→ Verify row counts
             ├─→ Test queries
             └─→ Restart application
```

### CSV Import Flow

```
┌─────────────────────────┐
│ CSV Files               │
│ - events.csv            │
│ - attendees.csv         │
│ - invitees.csv          │
└────┬────────────────────┘
     │ Upload to server
     ▼
┌─────────────────────────┐
│ /opt/tanishq/csv/       │
└────┬────────────────────┘
     │
     ├─→ Validate format
     ├─→ Check headers
     └─→ LOAD DATA LOCAL INFILE
         │
         ▼
     ┌─────────────────────────┐
     │ MySQL Database          │
     │ INSERT INTO events...   │
     │ INSERT INTO attendees..│
     │ INSERT INTO invitees... │
     └─────────────────────────┘
             │
             └─→ Verify import
                 - Row counts
                 - Sample queries
                 - Data integrity
```

---

## 🔐 AUTHENTICATION & AUTHORIZATION FLOW

```
┌──────────┐
│  User    │ Enters credentials
└────┬─────┘
     │ POST /login
     ▼
┌──────────────────────┐
│ AuthController       │
└────┬─────────────────┘
     │
     ▼
┌──────────────────────┐
│ Check User Type      │
└────┬─────────────────┘
     │
     ├─→ ABM (Area Business Manager)
     │   └─→ Query: abm_login table
     │
     ├─→ RBM (Regional Business Manager)
     │   └─→ Query: rbm_login table
     │
     └─→ CEE (Customer Engagement Executive)
         └─→ Query: cee_login table
                 │
                 ▼
         ┌──────────────────┐
         │ Validate Password│
         └────┬─────────────┘
              │
              ├─→ Success
              │   ├─→ Create session
              │   ├─→ Set role-based permissions
              │   └─→ Return token/session
              │
              └─→ Failure
                  └─→ Return 401 Unauthorized
```

---

## 📈 PERFORMANCE OPTIMIZATION

### Caching Strategy

```
┌──────────────────────────────────────┐
│         FIRST REQUEST                │
└────┬─────────────────────────────────┘
     │ GET /stores/summary?rbm=USER1
     ▼
Cache MISS
     │
     ├─→ Query Database (expensive)
     │   - Join stores, events, attendees
     │   - Aggregate calculations
     │   - Format response
     │
     └─→ Store in Cache
         └─→ StoreSummaryCache.put("USER1", data)

┌──────────────────────────────────────┐
│       SUBSEQUENT REQUESTS            │
└────┬─────────────────────────────────┘
     │ GET /stores/summary?rbm=USER1
     ▼
Cache HIT (instant response)
     │
     └─→ Return cached data
         (No database query needed)

┌──────────────────────────────────────┐
│    BACKGROUND REFRESH (Every 6hrs)   │
└────┬─────────────────────────────────┘
     │ @Scheduled
     ▼
StoreSummaryScheduler
     │
     ├─→ For each cached RBM
     │   └─→ Re-query database
     │       └─→ Update cache
     │
     └─→ Fresh data maintained automatically
```

---

## 🎯 QR CODE GENERATION & SCANNING

```
┌──────────────────────────────────────┐
│    EVENT CREATION                    │
└────┬─────────────────────────────────┘
     │ Create Event with ID: STORE123_uuid
     ▼
┌──────────────────────────────────────┐
│ Generate QR Code                     │
│ URL: http://server:3001/events/      │
│      customer/STORE123_uuid          │
└────┬─────────────────────────────────┘
     │
     ├─→ Generate QR image (base64)
     └─→ Store in database
         └─→ events.qr_code_data

┌──────────────────────────────────────┐
│    CUSTOMER SCANS QR                 │
└────┬─────────────────────────────────┘
     │ Scan QR → Opens URL
     ▼
┌──────────────────────────────────────┐
│ GET /events/customer/STORE123_uuid   │
└────┬─────────────────────────────────┘
     │
     ├─→ Fetch event details
     ├─→ Show registration form
     └─→ Customer registers
         │
         ▼
     ┌─────────────────────────┐
     │ INSERT INTO attendees   │
     │ eventId = STORE123_uuid │
     └─────────────────────────┘
```

---

## 📊 KEY METRICS & MONITORING

```
┌─────────────────────────────────────────┐
│         LOGGED INFORMATION              │
├─────────────────────────────────────────┤
│ ✓ SQL Queries (show-sql=true)          │
│ ✓ Service layer operations              │
│ ✓ File uploads (success/failure)        │
│ ✓ Cache hit/miss rates                  │
│ ✓ Scheduled job execution               │
│ ✓ External API calls                    │
└─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│         LOG FILES                       │
├─────────────────────────────────────────┤
│ Level: DEBUG (local/preprod)            │
│ Level: INFO (production)                │
│                                         │
│ Packages:                               │
│ - org.springframework.web: INFO         │
│ - com.dechub.tanishq: DEBUG/INFO        │
└─────────────────────────────────────────┘
```

---

*This diagram represents the complete data management architecture of the Tanishq Celebrations Event Management System.*

*Last Updated: January 14, 2026*

