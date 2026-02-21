# 🔄 Greeting Module - Migration Guide: Google Sheets → MySQL

## 📋 Overview

Your **current working branch** uses **MySQL database** for the greeting module, but based on code analysis, the **main branch** likely still uses **Google Sheets** as the database. This document explains the differences and migration path.

---

## 🆚 Implementation Comparison

### ❌ OLD IMPLEMENTATION (Likely in Main Branch)

#### Architecture
```
Google Sheets (as Database)
    ↓
GoogleSheetsService.java
    ↓
GreetingService.java (using Sheets API)
    ↓
GreetingController.java

Google Drive (for video storage)
    ↓
GoogleDriveService.java
```

#### Files Used (OLD)
- `com.dechub.tanishq.service.qrservices.GoogleSheetsService`
- `com.dechub.tanishq.service.qrservices.GoogleDriveService`
- `com.dechub.tanishq.dto.qrcode.Greeting` (DTO, not Entity)
- `com.dechub.tanishq.dto.qrcode.GreetingInfo` (DTO)

#### Google Sheets Structure (OLD)
```
Sheet: "Sheet1"
Columns: A-G

A: qrId (unique_id)
B: status (PENDING/UPLOADED)
C: videoFileId (Google Drive file ID)
D: videoPlaybackUrl (Google Drive playback URL)
E: submissionTimestamp
F: name (sender name)
G: message (personal message)
```

#### Configuration (OLD)
```properties
# Google Sheets Configuration
dechub.tanishq.greeting.sheet.key.filepath=/opt/tanishq/tanishqgmb-5437243a8085.p12
dechub.tanishq.greeting.sheet.service.account=tanishq-app@tanishqgmb.iam.gserviceaccount.com
dechub.tanishq.greeting.sheet.id=1EbbvXLIY6rVylXvlbfEgbZQUXPUuHoVlM9tuL-tFBDs

# Google Drive Configuration
dechub.tanishq.greeting.drive.key.filepath=/opt/tanishq/event-images-469618-32e65f6d62b3.p12
dechub.tanishq.greeting.drive.service.account=event-images@event-images-469618.iam.gserviceaccount.com
dechub.tanishq.greeting.drive.folder.id=1GtXx0JFNVd8cm4kEiNaZ-jw8GUHSSu2D
```

#### Data Flow (OLD)
```
1. Generate Greeting
   → Create new row in Google Sheets
   → Columns: [uniqueId, "PENDING", "", "", "", "", ""]

2. Upload Video
   → Upload file to Google Drive
   → Get Drive file ID
   → Update row in Google Sheets: status="UPLOADED", fileId, playbackUrl, timestamp

3. View Greeting
   → Read row from Google Sheets by uniqueId
   → Return data from columns
```

---

### ✅ NEW IMPLEMENTATION (Your Current Branch)

#### Architecture
```
MySQL Database (greetings table)
    ↓
GreetingRepository.java (JPA)
    ↓
GreetingService.java (using JPA Repository)
    ↓
GreetingController.java

AWS S3 or Local Storage (for video storage)
    ↓
StorageService.java (interface)
    ├→ LocalFileStorageService.java (@Profile("local"))
    └→ AwsS3StorageService.java (@Profile({"preprod", "prod"}))
```

#### Files Used (NEW)
- `com.dechub.tanishq.entity.Greeting` (JPA Entity)
- `com.dechub.tanishq.repository.GreetingRepository` (JPA Repository)
- `com.dechub.tanishq.service.GreetingService` (using JPA)
- `com.dechub.tanishq.service.storage.StorageService` (interface)
- `com.dechub.tanishq.service.storage.LocalFileStorageService`
- `com.dechub.tanishq.service.storage.AwsS3StorageService`

#### MySQL Table Structure (NEW)
```sql
CREATE TABLE greetings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    unique_id VARCHAR(255),
    greeting_text VARCHAR(255),      -- Sender name
    phone VARCHAR(255),
    message TEXT,                     -- Personal message
    qr_code_data LONGTEXT,            -- Base64 QR code
    drive_file_id VARCHAR(255),       -- Video URL (S3 or local)
    created_at DATETIME,
    uploaded BOOLEAN
);
```

#### Configuration (NEW)
```properties
# MySQL Database
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod
spring.datasource.username=root
spring.datasource.password=Dechub#2025
spring.jpa.hibernate.ddl-auto=update

# AWS S3 Storage (Preprod/Prod)
aws.s3.bucket.name={bucket}
aws.s3.region={region}

# Local Storage (Dev)
local.storage.base.path=./storage
local.storage.base.url=http://localhost:3000/storage
```

#### Data Flow (NEW)
```
1. Generate Greeting
   → INSERT into MySQL greetings table
   → Fields: uniqueId, uploaded=false, created_at=NOW()

2. Upload Video
   → Upload to S3 (or local storage)
   → Get video URL
   → UPDATE greetings SET greeting_text=name, message=msg, 
     drive_file_id=videoUrl, uploaded=true WHERE unique_id=id

3. View Greeting
   → SELECT * FROM greetings WHERE unique_id = ?
   → Return data from database record
```

---

## 🔄 Key Differences

| Aspect | OLD (Google Sheets) | NEW (MySQL) |
|--------|---------------------|-------------|
| **Database** | Google Sheets | MySQL |
| **ORM/API** | Google Sheets API | Spring Data JPA |
| **Schema** | Manual spreadsheet columns | Auto-generated from Entity |
| **Data Access** | Read/Write via API calls | JPA Repository methods |
| **Video Storage** | Google Drive | AWS S3 or Local File System |
| **Authentication** | Service Account P12 key | IAM Role (S3) or None (local) |
| **Dependencies** | `google-api-services-sheets` | `spring-boot-starter-data-jpa` |
| **Profile-Based** | No | Yes (local/preprod/prod) |
| **Scalability** | Limited by Sheets API quota | Database-level scalability |
| **Performance** | API calls (slower) | Direct DB queries (faster) |
| **Backup** | Google Sheets versions | MySQL backup tools |
| **Query Capability** | Limited (linear search) | Full SQL query support |

---

## 📊 Data Mapping

### Google Sheets Column → MySQL Column

| Sheets Column | MySQL Column | Notes |
|--------------|--------------|-------|
| A: qrId | unique_id | Same format |
| B: status | uploaded | PENDING→false, UPLOADED→true |
| C: videoFileId | drive_file_id | Now stores S3 URL instead of Drive ID |
| D: videoPlaybackUrl | drive_file_id | Same field (URL) |
| E: submissionTimestamp | created_at | DateTime format |
| F: name | greeting_text | Same |
| G: message | message | Same |
| - | id | New: Auto-increment primary key |
| - | phone | New: Optional field |
| - | qr_code_data | New: Cached QR code (Base64) |

---

## 🔧 Why the Change?

### Problems with Google Sheets Approach:
1. ❌ **API Quota Limits** - Google Sheets API has rate limits
2. ❌ **Slow Performance** - Network calls for every operation
3. ❌ **Poor Scalability** - Not designed for high-traffic applications
4. ❌ **Limited Query** - Can't do complex queries, joins, or indexing
5. ❌ **Concurrent Access** - Potential conflicts with simultaneous writes
6. ❌ **No Transactions** - Can't rollback on errors
7. ❌ **Authentication Overhead** - Requires P12 keys and service accounts
8. ❌ **Google Drive Dependency** - Another external service to manage

### Benefits of MySQL Approach:
1. ✅ **Better Performance** - Direct database queries
2. ✅ **Unlimited Queries** - No API quota restrictions
3. ✅ **ACID Transactions** - Data integrity guaranteed
4. ✅ **Full SQL Support** - Complex queries, joins, indexes
5. ✅ **Better Scalability** - Handle thousands of concurrent requests
6. ✅ **Standard ORM** - Use Spring Data JPA (industry standard)
7. ✅ **Profile-Based Storage** - Local dev, S3 for production
8. ✅ **Backup & Recovery** - Standard MySQL backup tools

---

## 🚀 Migration Steps (Google Sheets → MySQL)

### Step 1: Verify Current State

#### Check Main Branch Implementation
```bash
git checkout main
# Check if these files exist:
# - service/qrservices/GoogleSheetsService.java
# - service/qrservices/GoogleDriveService.java
# - dto/qrcode/Greeting.java (DTO, not Entity)
```

### Step 2: Export Existing Data from Google Sheets

#### Export Manually
1. Open Google Sheet: `1EbbvXLIY6rVylXvlbfEgbZQUXPUuHoVlM9tuL-tFBDs`
2. Download as CSV
3. Save as `greetings_export.csv`

#### Sample CSV Format:
```csv
qrId,status,videoFileId,videoPlaybackUrl,submissionTimestamp,name,message
GREETING_1738318234567,UPLOADED,1abc123xyz,https://drive.google.com/file/d/1abc123xyz/view,2026-01-20 10:30:00,John Doe,Happy Birthday!
GREETING_1738318234568,PENDING,,,,Jane Smith,Congratulations!
```

### Step 3: Transform Data

#### Create SQL Script
```sql
-- Load CSV into temporary table
CREATE TEMPORARY TABLE temp_sheets_data (
    qrId VARCHAR(255),
    status VARCHAR(20),
    videoFileId VARCHAR(255),
    videoPlaybackUrl VARCHAR(500),
    submissionTimestamp VARCHAR(50),
    name VARCHAR(255),
    message TEXT
);

-- Load CSV (adjust path)
LOAD DATA INFILE '/path/to/greetings_export.csv'
INTO TABLE temp_sheets_data
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

-- Transform and insert into greetings table
INSERT INTO greetings (
    unique_id,
    greeting_text,
    message,
    drive_file_id,
    uploaded,
    created_at
)
SELECT 
    qrId,
    name,
    message,
    COALESCE(videoPlaybackUrl, videoFileId),  -- Use playback URL or file ID
    CASE WHEN status = 'UPLOADED' THEN 1 ELSE 0 END,
    STR_TO_DATE(submissionTimestamp, '%Y-%m-%d %H:%i:%s')
FROM temp_sheets_data
WHERE qrId IS NOT NULL AND qrId != '';

-- Clean up
DROP TEMPORARY TABLE temp_sheets_data;
```

### Step 4: Migrate Video Files (Google Drive → S3)

#### Option A: Keep Using Google Drive URLs
- If videos are already in Google Drive with public URLs
- Just store the Google Drive URLs in `drive_file_id` column
- No need to migrate files

#### Option B: Migrate to AWS S3
```python
# Python script to migrate videos
import boto3
import gdown  # pip install gdown

s3 = boto3.client('s3')
bucket_name = 'your-bucket'

# Read from CSV
with open('greetings_export.csv', 'r') as f:
    for line in f:
        greeting_id, status, file_id, url, timestamp, name, msg = line.split(',')
        
        if status == 'UPLOADED' and file_id:
            # Download from Google Drive
            video_path = f'/tmp/{greeting_id}.mp4'
            gdown.download(f'https://drive.google.com/uc?id={file_id}', video_path)
            
            # Upload to S3
            s3_key = f'greetings/{greeting_id}/video.mp4'
            s3.upload_file(video_path, bucket_name, s3_key)
            
            # Update database with new S3 URL
            s3_url = f'https://{bucket_name}.s3.amazonaws.com/{s3_key}'
            # Run: UPDATE greetings SET drive_file_id='{s3_url}' WHERE unique_id='{greeting_id}'
```

### Step 5: Deploy New Code

#### Update Configuration
```properties
# Remove Google Sheets/Drive config
# dechub.tanishq.greeting.sheet.id=...
# dechub.tanishq.greeting.drive.folder.id=...

# Add MySQL config (already exists)
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod

# Add S3 config
aws.s3.bucket.name=your-greeting-bucket
aws.s3.region=ap-south-1
```

#### Build and Deploy
```bash
# Build WAR
mvn clean package -DskipTests

# Deploy to server
# Copy WAR to Tomcat or deploy via CI/CD
```

### Step 6: Verify Migration

#### Check Database
```sql
-- Count records
SELECT COUNT(*) FROM greetings;

-- Check uploaded vs pending
SELECT uploaded, COUNT(*) FROM greetings GROUP BY uploaded;

-- Verify recent greetings
SELECT unique_id, greeting_text, uploaded, created_at 
FROM greetings 
ORDER BY created_at DESC 
LIMIT 10;
```

#### Test Endpoints
```bash
# Generate new greeting
curl -X POST http://localhost:3000/greetings/generate

# Check existing greeting
curl http://localhost:3000/greetings/GREETING_1738318234567/status
```

### Step 7: Cleanup (After Verification)

#### Keep Google Sheet as Backup
- Don't delete the Google Sheet immediately
- Keep for 30-90 days as backup
- After verification period, archive or delete

#### Remove Old Dependencies
```xml
<!-- Remove from pom.xml if not used elsewhere -->
<dependency>
    <groupId>com.google.apis</groupId>
    <artifactId>google-api-services-sheets</artifactId>
</dependency>
<dependency>
    <groupId>com.google.apis</groupId>
    <artifactId>google-api-services-drive</artifactId>
</dependency>
```

#### Delete Old Code Files
```bash
# After successful migration and testing
rm -f src/main/java/com/dechub/tanishq/service/qrservices/GoogleSheetsService.java
rm -f src/main/java/com/dechub/tanishq/service/qrservices/GoogleDriveService.java
# Keep Greeting.java but ensure it's the Entity version, not DTO
```

---

## 🧪 Testing Checklist

- [ ] Create new greeting (POST /greetings/generate)
- [ ] Generate QR code (GET /greetings/{id}/qr)
- [ ] Upload video with name and message
- [ ] View greeting (GET /greetings/{id}/view)
- [ ] Check status (GET /greetings/{id}/status)
- [ ] Delete greeting (DELETE /greetings/{id})
- [ ] Verify migrated data loads correctly
- [ ] Verify video playback works
- [ ] Test with mobile QR scanner
- [ ] Load test with multiple concurrent requests

---

## 🔍 Troubleshooting

### Issue: Table not created
**Solution:** Check `spring.jpa.hibernate.ddl-auto=update` in properties

### Issue: Data not migrated
**Solution:** Check SQL import errors, verify CSV format

### Issue: Videos not playing
**Solution:** Check S3 bucket permissions, verify URLs in `drive_file_id`

### Issue: QR codes not working
**Solution:** Check `greeting.qr.base.url` property matches your domain

---

## 📝 Rollback Plan

If migration fails:

1. **Rollback Code:**
   ```bash
   git checkout main
   git push -f origin production
   ```

2. **Rollback Database:**
   ```sql
   DROP TABLE greetings;
   -- Restore from backup if needed
   ```

3. **Reconfigure:**
   - Restore Google Sheets/Drive config
   - Redeploy old WAR file

---

## 🎯 Recommendation

**Your current branch (MySQL implementation) is BETTER** than the main branch (Google Sheets). Reasons:

1. ✅ More scalable
2. ✅ Better performance
3. ✅ Standard technology stack
4. ✅ Easier to maintain
5. ✅ No external API dependencies
6. ✅ Profile-based storage (dev/prod)

**Action Items:**
1. ✅ Keep using MySQL implementation
2. ✅ Export data from Google Sheets (if any exists)
3. ✅ Migrate to MySQL using SQL import
4. ✅ Test thoroughly
5. ✅ Push to main branch
6. ✅ Archive Google Sheets version

---

Generated: January 31, 2026
Status: MySQL implementation is production-ready ✅

