# 🎥 Greeting Module - Quick Reference Card

## 🎯 What is it?
A **standalone video greeting system** where users can:
- Generate unique QR codes
- Upload video messages
- View greeting videos

## 📊 Database Storage

### ✅ Current Implementation (Your Branch)
```
MySQL Database Table: greetings
├── id (Primary Key)
├── unique_id (GREETING_1738318234567)
├── greeting_text (Sender name)
├── message (Personal message)
├── qr_code_data (Base64 PNG)
├── drive_file_id (Video URL - S3 or local)
├── created_at (Timestamp)
└── uploaded (Boolean flag)
```

**Database Name:**
- Local: `tanishq`
- Preprod: `selfie_preprod`
- Prod: `selfie_prod`

**Video Storage:**
- Local: `./storage/greetings/{id}/`
- Preprod/Prod: AWS S3 `s3://bucket/greetings/{id}/`

### ❌ Old Implementation (Main Branch - Google Sheets)
```
Google Sheets as Database
├── Column A: qrId (unique_id)
├── Column B: status (PENDING/UPLOADED)
├── Column C: videoFileId
├── Column D: videoPlaybackUrl
├── Column E: submissionTimestamp
├── Column F: name
└── Column G: message
```

**Video Storage:** Google Drive

## 🔄 How It Works

### Step 1: Generate Greeting
```
POST /greetings/generate
    ↓
Create MySQL record:
    unique_id = "GREETING_1738318234567"
    uploaded = false
    created_at = now()
    ↓
Return: "GREETING_1738318234567"
```

### Step 2: Generate QR Code
```
GET /greetings/{uniqueId}/qr
    ↓
Generate QR with URL:
    https://celebrationsite-preprod.tanishq.co.in/qr?id=GREETING_XXX
    ↓
Save Base64 QR to database
    ↓
Return: PNG image
```

### Step 3: Upload Video
```
POST /greetings/{uniqueId}/upload
    FormData:
        - video: file
        - name: "John Doe"
        - message: "Happy Birthday!"
    ↓
Upload to storage (S3 or local)
    ↓
Update MySQL:
    greeting_text = "John Doe"
    message = "Happy Birthday!"
    drive_file_id = "https://bucket.s3.region.amazonaws.com/..."
    uploaded = true
    ↓
Return: Video URL
```

### Step 4: View Greeting
```
GET /greetings/{uniqueId}/view
    ↓
Query MySQL by unique_id
    ↓
Return JSON:
{
    "hasVideo": true,
    "status": "completed",
    "driveFileId": "https://...",
    "videoPlaybackUrl": "https://...",
    "submissionTimestamp": "2026-01-31T10:30:00",
    "greetingText": "John Doe",
    "message": "Happy Birthday!"
}
```

## 📁 Key Files

| File | Purpose |
|------|---------|
| `entity/Greeting.java` | Database entity (JPA) |
| `repository/GreetingRepository.java` | Database queries |
| `service/GreetingService.java` | Business logic |
| `controller/GreetingController.java` | REST API endpoints |
| `service/storage/StorageService.java` | File upload interface |
| `service/storage/LocalFileStorageService.java` | Local dev storage |
| `service/storage/AwsS3StorageService.java` | Production S3 storage |

## 🌐 API Endpoints

| Method | Endpoint | Purpose | Response |
|--------|----------|---------|----------|
| POST | `/greetings/generate` | Create greeting | `"GREETING_XXX"` |
| GET | `/greetings/{id}/qr` | Get QR code | PNG image |
| POST | `/greetings/{id}/upload` | Upload video | `"Video uploaded..."` |
| GET | `/greetings/{id}/view` | Get greeting info | GreetingInfo JSON |
| GET | `/greetings/{id}/status` | Check upload status | `{"uploaded": true}` |
| DELETE | `/greetings/{id}` | Delete greeting | `"Greeting deleted"` |

## 🔧 Configuration Properties

### Database
```properties
# Local
spring.datasource.url=jdbc:mysql://localhost:3306/tanishq

# Preprod
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod
```

### Storage
```properties
# Local
local.storage.base.path=./storage
local.storage.base.url=http://localhost:3000/storage

# Preprod/Prod
aws.s3.bucket.name={bucket}
aws.s3.region={region}
```

### QR Code URLs
```properties
# Local
greeting.qr.base.url=http://localhost:3000/greetings/

# Preprod
greeting.qr.base.url=https://celebrationsite-preprod.tanishq.co.in/greetings/
```

## ⚙️ Technology Stack

- **Database:** MySQL 8
- **ORM:** Spring Data JPA + Hibernate
- **File Storage:** AWS S3 (prod) / Local File System (dev)
- **QR Code:** ZXing library
- **Video Upload:** MultipartFile (max 100MB)

## 🆚 Greeting vs Events Module

| Feature | Greeting Module | Events Module |
|---------|----------------|---------------|
| Database | MySQL only | MySQL + Google Sheets |
| Table | `greetings` | `events`, `attendees`, etc. |
| Data Import | No Excel import | Excel import required |
| File Storage | S3/Local (videos) | S3/Local + Google Drive |
| Purpose | Video greetings | Event management |
| Integration | Standalone | Part of main app |

## 🔍 Verification Commands

### Check table structure
```sql
USE selfie_preprod;
DESC greetings;
```

### View all greetings
```sql
SELECT unique_id, greeting_text, uploaded, created_at 
FROM greetings 
ORDER BY created_at DESC 
LIMIT 10;
```

### Count statistics
```sql
SELECT 
    uploaded,
    COUNT(*) as count
FROM greetings
GROUP BY uploaded;
```

## 🚨 Important Notes

1. **No Excel Dependency** - Unlike Events, this uses MySQL directly
2. **Auto-Schema** - Table created automatically by Hibernate on first run
3. **Profile-Based Storage** - Local dev uses file system, preprod/prod uses S3
4. **Misleading Name** - `drive_file_id` actually stores video URL, not Google Drive ID
5. **Separate System** - Completely independent from Events module
6. **Current vs Main** - Your working branch uses MySQL, main branch might still use Google Sheets

## 🔗 Related Documentation

- Full Architecture: `GREETING_MODULE_DOCUMENTATION.md`
- API Testing: Use Postman or curl with endpoints above
- Database Schema: Auto-generated from `Greeting.java` entity

---

**Quick Test:**
```bash
# 1. Generate greeting
curl -X POST http://localhost:3000/greetings/generate

# 2. Get QR code (replace {id} with result from step 1)
curl http://localhost:3000/greetings/{id}/qr --output qr.png

# 3. Check status
curl http://localhost:3000/greetings/{id}/status
```

---

Generated: January 31, 2026
Version: Current working branch (MySQL implementation)

