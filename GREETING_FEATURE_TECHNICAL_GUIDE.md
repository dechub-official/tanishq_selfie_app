# 🎥 Greeting Feature - Complete Technical Guide

## Date: December 20, 2025

---

## 📐 Architecture Overview

```
┌─────────────┐      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Frontend  │─────▶│  Spring Boot │─────▶│    MySQL     │      │   AWS S3     │
│   (React)   │◀─────│  Controller  │◀─────│   Database   │      │   Storage    │
└─────────────┘      └──────────────┘      └──────────────┘      └──────────────┘
      │                     │                      │                      │
      │                     │                      │                      │
   QR Scan              REST API            Store Metadata         Store Videos
   uniqueId             Endpoints          greeting_text          .mp4 files
```

---

## 🔄 Complete Data Flow

### **Step 1: Generate Greeting**
```
User Action: Click "Generate Greeting"
    ↓
Frontend: POST /greetings/generate
    ↓
Backend: GreetingService.createGreeting()
    ↓
Generate: uniqueId = "GREETING_" + timestamp
    ↓
MySQL: INSERT INTO greetings (unique_id, uploaded=false, created_at=NOW())
    ↓
Response: Return uniqueId to frontend
```

**MySQL Record Created:**
```sql
{
  id: 1,
  unique_id: "GREETING_1734700000000",
  greeting_text: NULL,
  phone: NULL,
  message: NULL,
  qr_code_data: NULL,
  drive_file_id: NULL,
  uploaded: false,
  created_at: "2025-12-20 10:00:00"
}
```

---

### **Step 2: Generate QR Code**
```
User Action: View QR Code
    ↓
Frontend: GET /greetings/{uniqueId}/qr
    ↓
Backend: GreetingService.generateQrCode(uniqueId)
    ↓
MySQL: SELECT * FROM greetings WHERE unique_id = ?
    ↓
Generate: QR Code PNG encoding ONLY "GREETING_1734700000000"
    ↓
Convert: PNG → Base64 string
    ↓
MySQL: UPDATE greetings SET qr_code_data = ? WHERE unique_id = ?
    ↓
Response: Return PNG byte array
```

**MySQL Record Updated:**
```sql
{
  id: 1,
  unique_id: "GREETING_1734700000000",
  qr_code_data: "iVBORw0KGgoAAAANSUhEUgAA..." (Base64),
  uploaded: false,
  ...
}
```

**QR Code Contents:** `GREETING_1734700000000` (plain text, NOT a URL)

---

### **Step 3: Scan QR & Upload Video**
```
Recipient Action: Scan QR Code
    ↓
QR Scanner: Reads "GREETING_1734700000000"
    ↓
Frontend: Navigate to /recording with qrId
    ↓
User Records: Video (max 2 minutes, ~20-50 MB)
    ↓
User Fills Form: name, phone, message
    ↓
Frontend: POST /greetings/{uniqueId}/upload
    ├─ video: MultipartFile (video.mp4)
    ├─ name: "John Doe"
    ├─ phone: "+1234567890"
    └─ message: "Happy Birthday!"
    ↓
Backend: GreetingService.uploadVideo()
    ↓
MySQL: SELECT * FROM greetings WHERE unique_id = ?
    ↓
Validate: Greeting exists and not already uploaded
    ↓
AWS S3: Upload video to s3://bucket-name/greetings/{uniqueId}.mp4
    ├─ Bucket: tanishq-celebration-videos
    ├─ Key: greetings/GREETING_1734700000000.mp4
    ├─ Size: 25 MB (approx)
    └─ Time: 2-5 seconds (depending on internet speed)
    ↓
Get S3 URL: https://bucket.s3.region.amazonaws.com/greetings/{uniqueId}.mp4
    ↓
MySQL: UPDATE greetings SET 
       drive_file_id = S3_URL,
       greeting_text = "John Doe",
       phone = "+1234567890",
       message = "Happy Birthday!",
       uploaded = true
       WHERE unique_id = ?
    ↓
Response: Success message with S3 URL
```

**MySQL Record After Upload:**
```sql
{
  id: 1,
  unique_id: "GREETING_1734700000000",
  greeting_text: "John Doe",
  phone: "+1234567890",
  message: "Happy Birthday!",
  qr_code_data: "iVBORw0KGgoAAAANSUhEUgAA...",
  drive_file_id: "https://tanishq-celebration-videos.s3.ap-south-1.amazonaws.com/greetings/GREETING_1734700000000.mp4",
  uploaded: true,
  created_at: "2025-12-20 10:00:00"
}
```

**AWS S3 File:**
```
Location: s3://tanishq-celebration-videos/greetings/GREETING_1734700000000.mp4
Size: 25 MB
Type: video/mp4
Access: Private (presigned URL required)
```

---

### **Step 4: View Greeting**
```
Recipient Action: Open greeting link
    ↓
Frontend: GET /greetings/{uniqueId}/view
    ↓
Backend: GreetingService.getGreeting(uniqueId)
    ↓
MySQL: SELECT * FROM greetings WHERE unique_id = ?
    ↓
Check: if (uploaded == false)
    ↓ YES (No video yet)
    Response: {
      hasVideo: false,
      status: "pending",
      driveFileId: null,
      videoPlaybackUrl: null
    }
    ↓ NO (Video uploaded)
    Generate Presigned URL: Valid for 1 hour
    S3URL: getVideoPlaybackUrl(driveFileId)
    ↓
    Response: {
      hasVideo: true,
      status: "completed",
      driveFileId: "https://...",
      videoPlaybackUrl: "https://...?X-Amz-Signature=...",
      submissionTimestamp: "2025-12-20T10:00:00",
      name: "John Doe",
      message: "Happy Birthday!"
    }
```

---

## 💾 Data Storage Details

### **MySQL Database**
```sql
Database: celebration_db (or your configured name)
Table: greetings

Storage per record:
- unique_id: ~30 bytes
- greeting_text: ~100 bytes (avg)
- phone: ~20 bytes
- message: ~500 bytes (avg)
- qr_code_data: ~20 KB (Base64 PNG)
- drive_file_id: ~150 bytes (S3 URL)
- uploaded: 1 byte
- created_at: 8 bytes
- id: 8 bytes

Total per greeting: ~21 KB (with QR code stored)
```

**Estimated Database Growth:**
- 100 greetings/day = 2.1 MB/day = 63 MB/month
- 1,000 greetings/day = 21 MB/day = 630 MB/month

### **AWS S3 Storage**
```
Bucket: tanishq-celebration-videos
Region: ap-south-1 (Mumbai) or your configured region
Path: greetings/{uniqueId}.mp4

Video file sizes:
- 30 second video: ~5-10 MB
- 1 minute video: ~10-20 MB
- 2 minute video (max): ~20-50 MB
Average: ~25 MB per video
```

**Estimated S3 Growth:**
- 100 videos/day = 2.5 GB/day = 75 GB/month
- 1,000 videos/day = 25 GB/day = 750 GB/month

---

## ⏱️ Performance & Timing

### **Operation Timings**

| Operation | Typical Time | Depends On |
|-----------|-------------|------------|
| Generate Greeting | 50-100 ms | MySQL write speed |
| Generate QR Code | 100-200 ms | QR generation + MySQL update |
| Check View Status | 30-50 ms | MySQL read speed |
| Upload Video (25MB) | 3-10 seconds | Internet upload speed |
| S3 Upload Processing | 1-2 seconds | AWS S3 processing |
| Get Presigned URL | 10-20 ms | AWS SDK call |
| Video Playback Start | 1-3 seconds | CDN + buffering |

### **Total User Experience Timings**

**Greeting Creator Journey:**
1. Generate greeting: **~100 ms**
2. Display QR code: **~200 ms**
3. Download QR as image: **~50 ms**

**Total: ~350 ms (instant)**

**Video Uploader Journey:**
1. Scan QR code: **User action**
2. Record video (2 min): **User action (120 seconds)**
3. Fill form: **User action (30 seconds)**
4. Upload video (25 MB): **3-10 seconds**
5. Process S3: **1-2 seconds**

**Total upload time: 4-12 seconds** (plus user recording time)

**Video Viewer Journey:**
1. Check greeting status: **~50 ms**
2. Get presigned URL: **~20 ms**
3. Load video player: **~500 ms**
4. Start video playback: **1-3 seconds**

**Total: 1.5-3.5 seconds** until video starts playing

---

## 🔧 Server Configuration Required

### **1. MySQL Configuration**

**Minimum Requirements:**
```properties
# application.properties or application.yml

# Database Connection
spring.datasource.url=jdbc:mysql://localhost:3306/celebration_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# Connection Pool (for production)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

**Database Schema (Auto-created by JPA):**
```sql
CREATE TABLE greetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unique_id VARCHAR(255) NOT NULL UNIQUE,
    greeting_text VARCHAR(255),
    phone VARCHAR(20),
    message TEXT,
    qr_code_data LONGTEXT,  -- Base64 encoded PNG
    drive_file_id VARCHAR(500),  -- S3 URL
    uploaded BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_unique_id (unique_id),
    INDEX idx_created_at (created_at)
);
```

### **2. AWS S3 Configuration**

**Required Configuration:**
```properties
# application.properties

# AWS Credentials (Option 1: Direct in properties - NOT recommended for production)
aws.access.key.id=YOUR_ACCESS_KEY_ID
aws.secret.access.key=YOUR_SECRET_ACCESS_KEY
aws.region=ap-south-1

# S3 Bucket Configuration
aws.s3.bucket.name=tanishq-celebration-videos
aws.s3.presigned.url.expiration=3600  # 1 hour in seconds
```

**Better: Use AWS IAM Role (Production Recommended):**
```
1. Create IAM Role with S3 permissions
2. Attach role to EC2 instance
3. Remove credentials from properties file
4. AWS SDK will auto-detect credentials
```

**Required IAM Permissions:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::tanishq-celebration-videos",
        "arn:aws:s3:::tanishq-celebration-videos/*"
      ]
    }
  ]
}
```

**S3 Bucket Settings:**
```
Bucket Name: tanishq-celebration-videos
Region: ap-south-1 (Mumbai)
Block Public Access: ON (keep videos private)
Versioning: OFF (unless needed)
Encryption: AES-256 (default)
Lifecycle Rules: Optional (delete old videos after 90 days)
```

### **3. File Upload Configuration**

```properties
# application.properties

# Max file upload size (50 MB for 2-minute videos)
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Temporary upload directory
spring.servlet.multipart.location=/tmp

# Enable multipart uploads
spring.servlet.multipart.enabled=true
```

### **4. Application Server Configuration**

**Tomcat (Embedded - Spring Boot):**
```properties
# application.properties

# Server Port
server.port=8080

# Max HTTP Header Size (for large requests)
server.max-http-header-size=20KB

# Connection timeout
server.connection-timeout=30s

# Thread pool (for handling concurrent uploads)
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
```

**Memory Requirements:**
```
Minimum: 2 GB RAM
Recommended: 4-8 GB RAM (for production)
JVM Options: -Xms2G -Xmx4G
```

### **5. QR Code Service Configuration**

**Already Configured in Code:**
```java
// GreetingService.java
byte[] qrCodeImage = qrCodeService.generateQrCodeImage(uniqueId, 300, 300);
// Size: 300x300 pixels (adjustable)
// Format: PNG
// Library: ZXing (Google)
```

**No additional configuration needed.**

---

## 🚀 Server Setup Checklist

### **Before First Deployment**

- [ ] **MySQL Server Running**
  - Database created: `celebration_db`
  - User credentials configured
  - Port 3306 accessible

- [ ] **AWS S3 Bucket Created**
  - Bucket name: `tanishq-celebration-videos`
  - Region: `ap-south-1` (or your preferred region)
  - Block public access enabled
  - IAM permissions configured

- [ ] **Application Properties Updated**
  ```
  ✓ Database connection URL
  ✓ Database credentials
  ✓ AWS credentials or IAM role
  ✓ S3 bucket name
  ✓ File upload size limits
  ```

- [ ] **Network Configuration**
  - Port 8080 open (or your configured port)
  - Firewall allows MySQL connection
  - Internet access for AWS S3

### **First Run Commands**

```bash
# 1. Navigate to project directory
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# 2. Build the application
mvn clean package

# 3. Run the application
java -jar target/tanishq-selfie-app.jar

# OR run with Maven
mvn spring-boot:run
```

### **Verify Setup**

```bash
# 1. Test greeting creation
curl -X POST http://localhost:8080/greetings/generate

# 2. Test QR generation (replace ID)
curl http://localhost:8080/greetings/GREETING_123456789/qr --output test.png

# 3. Check MySQL
mysql -u root -p
USE celebration_db;
SELECT * FROM greetings;

# 4. Verify AWS S3 access
aws s3 ls s3://tanishq-celebration-videos/greetings/
```

---

## 💰 Cost Estimation (AWS S3)

### **S3 Storage Costs (Mumbai Region)**
```
Standard Storage: $0.023 per GB/month

Scenarios:
- 100 videos/day (2.5 GB/day, 75 GB/month): $1.73/month
- 1,000 videos/day (25 GB/day, 750 GB/month): $17.25/month
- 10,000 videos/day (250 GB/day, 7.5 TB/month): $172.50/month
```

### **S3 Request Costs**
```
PUT requests: $0.005 per 1,000 requests
GET requests: $0.0004 per 1,000 requests

Scenarios:
- 100 videos/day:
  - Uploads: 100 × 30 = 3,000/month → $0.015
  - Views (10× per video): 1,000 × 30 = 30,000/month → $0.012
  - Total: $0.027/month

- 1,000 videos/day:
  - Uploads: 1,000 × 30 = 30,000/month → $0.15
  - Views: 10,000 × 30 = 300,000/month → $0.12
  - Total: $0.27/month
```

### **Data Transfer Costs**
```
Data Transfer OUT (to internet): $0.114 per GB (first 10 TB)

Scenarios (assuming 2× views per video = 2× downloads):
- 100 videos/day (25 MB avg):
  - Storage: 75 GB/month
  - Transfer OUT: 150 GB/month → $17.10/month

- 1,000 videos/day:
  - Storage: 750 GB/month
  - Transfer OUT: 1,500 GB/month → $171/month
```

### **Total Monthly Cost Estimate**

| Videos/Day | Storage | Requests | Transfer OUT | Total/Month |
|------------|---------|----------|--------------|-------------|
| 100 | $1.73 | $0.03 | $17.10 | **$18.86** |
| 1,000 | $17.25 | $0.27 | $171.00 | **$188.52** |
| 10,000 | $172.50 | $2.70 | $1,710.00 | **$1,885.20** |

**Note:** Costs can be reduced by:
- Using S3 Lifecycle policies (delete old videos)
- Using CloudFront CDN (cheaper data transfer)
- Optimizing video compression

---

## 🔐 Security Considerations

### **Current Implementation**

✅ **What's Secure:**
- Videos stored privately in S3
- Presigned URLs with expiration (1 hour)
- QR codes encode only uniqueId (no sensitive data)
- MySQL prepared statements (SQL injection protected)
- Multipart file upload validation

⚠️ **What Could Be Improved:**

1. **Add Authentication**
   ```java
   // Optional: Add JWT or session-based auth for management endpoints
   @DeleteMapping("/{uniqueId}")
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<String> deleteGreeting(@PathVariable String uniqueId)
   ```

2. **Add Rate Limiting**
   ```java
   // Prevent abuse of greeting generation
   @RateLimiter(limit = 10, window = "1m")
   @PostMapping("/generate")
   ```

3. **Add File Type Validation**
   ```java
   // Already present, but could be stricter
   if (!videoFile.getContentType().equals("video/mp4")) {
       throw new IllegalArgumentException("Only MP4 videos allowed");
   }
   ```

4. **Add CORS Configuration**
   ```java
   @Configuration
   public class CorsConfig {
       @Bean
       public WebMvcConfigurer corsConfigurer() {
           return new WebMvcConfigurer() {
               @Override
               public void addCorsMappings(CorsRegistry registry) {
                   registry.addMapping("/greetings/**")
                       .allowedOrigins("https://yourdomain.com")
                       .allowedMethods("GET", "POST", "DELETE");
               }
           };
       }
   }
   ```

---

## 📊 Monitoring & Maintenance

### **What to Monitor**

1. **MySQL Database**
   - Connection pool usage
   - Query performance
   - Table size growth
   - Failed queries

2. **AWS S3**
   - Bucket size
   - Upload success rate
   - Presigned URL generation failures
   - Monthly costs

3. **Application Metrics**
   - Response times per endpoint
   - Error rates
   - Concurrent uploads
   - Memory usage

### **Recommended Tools**

- **Spring Boot Actuator** (built-in metrics)
- **AWS CloudWatch** (S3 monitoring)
- **MySQL Performance Schema** (query monitoring)
- **Prometheus + Grafana** (comprehensive monitoring)

### **Maintenance Tasks**

**Weekly:**
- [ ] Check error logs
- [ ] Monitor S3 storage growth
- [ ] Verify backup status

**Monthly:**
- [ ] Review AWS S3 costs
- [ ] Clean up old test data
- [ ] Update dependencies
- [ ] Review security patches

**Quarterly:**
- [ ] Optimize database indexes
- [ ] Review S3 lifecycle policies
- [ ] Performance testing
- [ ] Security audit

---

## 🎯 Summary

### **How It Works**
1. User generates greeting → MySQL stores metadata
2. QR code generated → Encodes uniqueId only
3. Recipient scans QR → Frontend gets uniqueId
4. Video uploaded → Stored in AWS S3
5. MySQL updated → Links S3 URL to greeting
6. Viewer accesses → Presigned S3 URL generated
7. Video plays → Streamed from S3

### **What's Stored Where**
- **MySQL**: Metadata (uniqueId, name, message, S3 URL)
- **AWS S3**: Video files (MP4)
- **Frontend**: Nothing permanent (state only)

### **Performance**
- Fast: Database operations (< 100ms)
- Medium: QR generation (< 200ms)
- Slow: Video upload (3-10 seconds for 25MB)
- Fast: Video playback start (1-3 seconds)

### **Server Changes Needed**
✅ **Already Configured:**
- Spring Boot application
- JPA/Hibernate
- AWS SDK
- File upload handling

❌ **Need to Configure:**
- MySQL database connection
- AWS credentials or IAM role
- S3 bucket name
- File size limits (if different)

### **Cost (100 videos/day)**
- AWS S3: ~$19/month
- MySQL: Minimal (< $5/month if hosted)
- Server: Depends on your hosting

### **Next Steps**
1. Review and update `application.properties`
2. Create MySQL database
3. Create S3 bucket
4. Configure IAM permissions
5. Deploy application
6. Test complete flow
7. Monitor and optimize

---

**Everything is ready to deploy! Just configure the server settings and you're good to go.** 🚀

