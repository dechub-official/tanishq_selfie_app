# 🎥 Greeting Module - Complete Architecture Documentation

## 📋 Overview
The Greeting Module is a **standalone mini-project** within the Tanishq Selfie App that allows users to:
1. Generate unique QR codes for video greetings
2. Upload video greetings
3. View and retrieve video greetings

**Important:** This module is **completely separate** from the Events module and uses its **own database table**.

---

## 🗄️ Database Architecture

### Database: MySQL
- **Local Environment:** `tanishq` database
- **Preprod Environment:** `selfie_preprod` database  
- **Production Environment:** `selfie_prod` database

### Table: `greetings`

The greeting data is stored in a **MySQL database table**, NOT in Excel sheets like the Events module.

#### Table Structure:
```sql
CREATE TABLE greetings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    unique_id VARCHAR(255),
    greeting_text VARCHAR(255),           -- Sender's name
    phone VARCHAR(255),
    message TEXT,                          -- Personal message
    qr_code_data LONGTEXT,                 -- Base64 encoded QR code image
    drive_file_id VARCHAR(255),            -- Video URL (S3 or local path)
    created_at DATETIME,
    uploaded BOOLEAN DEFAULT FALSE
);
```

#### Column Details:
| Column | Type | Purpose |
|--------|------|---------|
| `id` | BIGINT | Primary key (auto-increment) |
| `unique_id` | VARCHAR | Unique identifier (e.g., "GREETING_1738318234567") |
| `greeting_text` | VARCHAR | Sender's name |
| `phone` | VARCHAR | Sender's phone (optional) |
| `message` | TEXT | Personal message from sender |
| `qr_code_data` | LONGTEXT | Base64 encoded QR code PNG image |
| `drive_file_id` | VARCHAR | Video file URL (S3 URL or local path) |
| `created_at` | DATETIME | Timestamp when greeting was created |
| `uploaded` | BOOLEAN | Flag indicating if video has been uploaded |

**Note:** The column name `drive_file_id` is misleading - it actually stores the video URL (S3 or local file path), not a Google Drive file ID.

---

## 🏗️ Architecture Components

### 1. Entity Layer
**File:** `src/main/java/com/dechub/tanishq/entity/Greeting.java`

```java
@Entity
@Table(name = "greetings")
public class Greeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String uniqueId;
    private String greetingText;
    private String phone;
    private String message;
    
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String qrCodeData;
    
    private String driveFileId;  // Actually stores video URL
    private LocalDateTime createdAt;
    private Boolean uploaded;
}
```

### 2. Repository Layer
**File:** `src/main/java/com/dechub/tanishq/repository/GreetingRepository.java`

```java
@Repository
public interface GreetingRepository extends JpaRepository<Greeting, Long> {
    Optional<Greeting> findByUniqueId(String uniqueId);
}
```

Uses **Spring Data JPA** for database operations:
- Save greeting records
- Find greeting by unique ID
- Update greeting records
- Delete greeting records

### 3. Service Layer
**File:** `src/main/java/com/dechub/tanishq/service/GreetingService.java`

**Key Methods:**

#### `createGreeting()`
- Generates unique ID: `GREETING_{timestamp}`
- Creates new database record with `uploaded=false`
- Returns unique ID

#### `generateQrCode(String uniqueId)`
- Validates greeting exists
- Generates QR code with URL: `https://celebrationsite-preprod.tanishq.co.in/qr?id={uniqueId}`
- Saves Base64 QR code to database
- Returns PNG byte array

#### `uploadVideo(String uniqueId, MultipartFile videoFile, String name, String message)`
- Validates greeting exists
- Validates video file (max 100MB, video content type)
- Uploads video using StorageService (S3 or local)
- Updates database:
  - `greetingText = name`
  - `message = message`
  - `driveFileId = videoUrl`
  - `uploaded = true`
- Returns video URL

#### `getGreeting(String uniqueId)`
- Retrieves greeting record from database
- Returns Optional<Greeting>

#### `deleteGreeting(String uniqueId)`
- Deletes greeting record from database

#### `hasVideoUploaded(String uniqueId)`
- Checks if video has been uploaded
- Returns boolean

### 4. Controller Layer
**File:** `src/main/java/com/dechub/tanishq/controller/GreetingController.java`

**REST API Endpoints:**

| Method | Endpoint | Purpose | Response |
|--------|----------|---------|----------|
| POST | `/greetings/generate` | Generate new greeting | Unique ID |
| GET | `/greetings/{uniqueId}/qr` | Get QR code image | PNG image |
| POST | `/greetings/{uniqueId}/upload` | Upload video + metadata | Success message |
| GET | `/greetings/{uniqueId}/view` | View greeting info | GreetingInfo JSON |
| GET | `/greetings/{uniqueId}/status` | Check upload status | {"uploaded": true/false} |
| DELETE | `/greetings/{uniqueId}` | Delete greeting | Success message |

### 5. Storage Layer
**Interface:** `src/main/java/com/dechub/tanishq/service/storage/StorageService.java`

**Implementations:**

#### Local File Storage (`@Profile("local")`)
**File:** `LocalFileStorageService.java`
- Used in **local development**
- Stores videos in: `./storage/greetings/{greetingId}/`
- Returns URL: `http://localhost:3000/storage/greetings/{greetingId}/{filename}`

#### AWS S3 Storage (`@Profile({"preprod", "prod"})`)
**File:** `AwsS3StorageService.java`
- Used in **preprod and production**
- Stores videos in S3: `s3://{bucket}/greetings/{greetingId}/`
- Returns URL: `https://{bucket}.s3.{region}.amazonaws.com/greetings/{greetingId}/{filename}`
- Uses IAM role credentials (no access keys in code)

---

## 🔄 Data Flow Diagram

### Flow 1: Generate Greeting QR Code
```
1. User requests QR code generation
   ↓
2. POST /greetings/generate
   ↓
3. GreetingService.createGreeting()
   ↓
4. Generate unique ID: GREETING_{timestamp}
   ↓
5. Save to MySQL database:
   - unique_id = GREETING_1738318234567
   - uploaded = false
   - created_at = now()
   ↓
6. Return unique ID to user
   ↓
7. GET /greetings/{uniqueId}/qr
   ↓
8. QrCodeService generates QR code PNG
   ↓
9. Save Base64 QR code to database
   ↓
10. Return PNG image to user
```

### Flow 2: Upload Video Greeting
```
1. User uploads video + name + message
   ↓
2. POST /greetings/{uniqueId}/upload
   ↓
3. Validate greeting exists in database
   ↓
4. Validate video file (size, content type)
   ↓
5. Upload to storage:
   - Local: ./storage/greetings/{id}/video_{timestamp}.mp4
   - S3: s3://bucket/greetings/{id}/video_{timestamp}.mp4
   ↓
6. Update MySQL database:
   - greeting_text = "John Doe"
   - message = "Happy birthday!"
   - drive_file_id = "https://bucket.s3.region.amazonaws.com/..."
   - uploaded = true
   ↓
7. Return video URL to user
```

### Flow 3: View Greeting
```
1. User requests greeting info
   ↓
2. GET /greetings/{uniqueId}/view
   ↓
3. Query MySQL database by unique_id
   ↓
4. If not found → 404 Not Found
   ↓
5. If found but uploaded=false:
   {
     "hasVideo": false,
     "status": "pending",
     "driveFileId": null,
     "videoPlaybackUrl": null
   }
   ↓
6. If uploaded=true:
   {
     "hasVideo": true,
     "status": "completed",
     "driveFileId": "https://...",
     "videoPlaybackUrl": "https://...",
     "submissionTimestamp": "2026-01-31T10:30:00",
     "greetingText": "John Doe",
     "message": "Happy birthday!"
   }
```

---

## ⚙️ Configuration

### Database Configuration

#### Local Environment (`application-local.properties`)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tanishq
spring.datasource.username=nagaraj_jadar
spring.datasource.password=Nagaraj07
spring.jpa.hibernate.ddl-auto=update
```

#### Preprod Environment (`application-preprod.properties`)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod
spring.datasource.username=root
spring.datasource.password=Dechub#2025
spring.jpa.hibernate.ddl-auto=update
```

#### Production Environment (`application-prod.properties`)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_prod
spring.datasource.username=root
spring.datasource.password=Nagaraj@07
spring.jpa.hibernate.ddl-auto=update
```

### Storage Configuration

#### Local
```properties
local.storage.base.path=./storage
local.storage.base.url=http://localhost:3000/storage
```

#### Preprod/Prod (AWS S3)
```properties
aws.s3.bucket.name={bucket-name}
aws.s3.region={region}
```

### QR Code Configuration
```properties
# Local
greeting.qr.base.url=http://localhost:3000/greetings/

# Preprod
greeting.qr.base.url=https://celebrationsite-preprod.tanishq.co.in/greetings/
```

---

## 🆚 Comparison: Greeting Module vs Events Module

| Aspect | Greeting Module | Events Module |
|--------|----------------|---------------|
| **Database** | MySQL (`greetings` table) | MySQL + Google Sheets |
| **Data Storage** | JPA Entity + Repository | Excel import → MySQL |
| **Purpose** | Video greeting cards | Event management |
| **File Storage** | S3/Local (videos) | S3/Local + Google Drive |
| **QR Code URL** | `/greetings/` or `/qr?id=` | `/events/customer/` |
| **Dependencies** | Standalone | Integrated with event system |

---

## 🔍 Important Notes

### 1. **Database Schema Auto-Creation**
- Using `spring.jpa.hibernate.ddl-auto=update`
- The `greetings` table is **automatically created** by Hibernate on first run
- No manual SQL scripts needed
- Schema updates automatically when entity changes

### 2. **No Excel Dependency**
- Unlike the Events module, this does **NOT** use Excel sheets
- All data stored directly in MySQL
- No Google Sheets integration for greetings

### 3. **Misleading Field Name**
- `drive_file_id` column name suggests Google Drive
- Actually stores **video URL** (S3 or local file path)
- Historical naming - not using Google Drive

### 4. **Storage Implementation**
- **Local:** Videos in `./storage/greetings/`
- **Preprod/Prod:** Videos in AWS S3
- Automatically selected based on Spring profile

### 5. **Migration from Main Branch**
Based on semantic search results, there was a **previous version** that used:
- Google Sheets for data storage
- Google Drive for video storage
- Different service classes (`GoogleSheetsService`, `GoogleDriveService`)

**Current implementation (not pushed to main):**
- MySQL database for data
- S3/Local for videos
- Standard JPA/Hibernate

---

## 📊 Database Queries

### Check if greeting exists
```sql
SELECT * FROM greetings WHERE unique_id = 'GREETING_1738318234567';
```

### View all greetings
```sql
SELECT unique_id, greeting_text, message, uploaded, created_at 
FROM greetings 
ORDER BY created_at DESC;
```

### Count uploaded vs pending
```sql
SELECT 
    uploaded,
    COUNT(*) as count
FROM greetings
GROUP BY uploaded;
```

### Delete old greetings (older than 30 days)
```sql
DELETE FROM greetings 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

---

## 🔧 How to Verify the Setup

### 1. Check Database Connection
```bash
mysql -u root -p
USE selfie_preprod;
SHOW TABLES;
DESC greetings;
```

### 2. Check Table Structure
```sql
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'selfie_preprod' 
  AND TABLE_NAME = 'greetings';
```

### 3. Test API Endpoints
```bash
# Generate greeting
curl -X POST http://localhost:3000/greetings/generate

# Get QR code
curl http://localhost:3000/greetings/GREETING_1738318234567/qr --output qr.png

# Check status
curl http://localhost:3000/greetings/GREETING_1738318234567/status
```

---

## 🚀 Deployment Notes

### When deploying to production:
1. ✅ Database automatically creates `greetings` table
2. ✅ No Excel files needed
3. ✅ Configure AWS S3 bucket for video storage
4. ✅ Set correct `greeting.qr.base.url` in properties
5. ✅ IAM role should have S3 permissions

### Migration from Google Sheets version (if needed):
If you need to migrate data from the old Google Sheets version:
1. Export data from Google Sheets
2. Transform to SQL INSERT statements
3. Import into MySQL `greetings` table
4. Video files may need to be migrated from Google Drive to S3

---

## 📝 Summary

**The Greeting Module:**
- ✅ Uses **MySQL database** (not Excel)
- ✅ Table: `greetings` (auto-created by Hibernate)
- ✅ Videos stored in **AWS S3** (preprod/prod) or **local file system** (dev)
- ✅ Completely **separate from Events module**
- ✅ Uses standard **Spring Data JPA** for database operations
- ✅ Profile-based storage selection (local vs S3)
- ✅ REST API for all operations
- ✅ QR code generation with custom URLs

**This is the current implementation in your working branch (not pushed to main).**

The main branch might still have the old Google Sheets-based implementation based on the semantic search results.

---

Generated: January 31, 2026

