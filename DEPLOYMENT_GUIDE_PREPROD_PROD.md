# 🚀 DEPLOYMENT GUIDE - Greeting Feature (Preprod & Production)

## Date: December 20, 2025

---

## ✅ CURRENT SETUP CONFIRMED

### What's Already Implemented:

1. **S3 Bucket Structure** ✅
   ```
   celebrations-tanishq-preprod/
   ├── events/              (existing - for event images)
   │   └── {eventId}/
   └── greetings/           (NEW - for greeting videos)
       └── {greetingId}/
           └── greeting_video_*.mp4
   ```

2. **MySQL Table** ✅
   ```sql
   Table: greetings (auto-created by JPA)
   - id: BIGINT (primary key)
   - unique_id: VARCHAR(255) (unique)
   - greeting_text: VARCHAR(255) (sender name)
   - message: TEXT (personal message)
   - phone: VARCHAR(20)
   - qr_code_data: LONGTEXT (Base64 QR image)
   - drive_file_id: VARCHAR(500) (S3 URL)
   - uploaded: BOOLEAN (default false)
   - created_at: TIMESTAMP
   ```

3. **Code Implementation** ✅
   - GreetingService.java: Line 192 - Uses `greetings/{greetingId}/` path
   - QR Code Fix: Encodes only uniqueId
   - S3 upload: Direct to same bucket, separate folder
   - MySQL storage: Uses existing database

---

## 📋 PREPROD DEPLOYMENT (What You Have)

### Current Configuration:

**File:** `src/main/resources/application-preprod.properties`

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod
spring.datasource.username=root
spring.datasource.password=Dechub#2025

# S3 Bucket (SHARED with events)
aws.s3.bucket.name=celebrations-tanishq-preprod
aws.s3.region=ap-south-1

# ⚠️ NEEDS TO BE ADDED:
aws.access.key.id=YOUR_PREPROD_AWS_KEY
aws.secret.access.key=YOUR_PREPROD_AWS_SECRET
```

### S3 Bucket Setup:

**Bucket:** `celebrations-tanishq-preprod`
**Folder Structure:**
```
celebrations-tanishq-preprod/
├── events/
│   ├── EVT001/
│   ├── EVT002/
│   └── ...
└── greetings/              ← NEW FOLDER FOR GREETING VIDEOS
    ├── GREETING_1734700000000/
    │   └── greeting_video_20251220_100000_*.mp4
    ├── GREETING_1734700000001/
    │   └── greeting_video_20251220_100100_*.mp4
    └── ...
```

### What to Do:

#### Step 1: Add AWS Credentials (5 minutes)
```bash
# Edit properties file
nano src/main/resources/application-preprod.properties

# Add at line 108 (after aws.s3.region):
aws.access.key.id=YOUR_PREPROD_KEY
aws.secret.access.key=YOUR_PREPROD_SECRET
```

#### Step 2: Verify S3 Bucket & Create Folder (2 minutes)
```bash
# Check bucket exists
aws s3 ls s3://celebrations-tanishq-preprod/

# Create greetings folder (optional - auto-created on first upload)
aws s3api put-object --bucket celebrations-tanishq-preprod --key greetings/

# Verify folder created
aws s3 ls s3://celebrations-tanishq-preprod/
```

#### Step 3: Verify MySQL Database (2 minutes)
```bash
# Check database
mysql -u root -pDechub#2025 -e "SHOW DATABASES LIKE 'selfie_preprod';"

# If doesn't exist, create it
mysql -u root -pDechub#2025 -e "CREATE DATABASE selfie_preprod;"
```

#### Step 4: Build & Deploy (5 minutes)
```bash
# Navigate to project
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Build
mvn clean package

# Deploy (choose one):

# Option A: Run with Maven
mvn spring-boot:run

# Option B: Run JAR
java -jar target/tanishq-selfie-app-*.jar

# Option C: Deploy to Tomcat
cp target/tanishq-selfie-app-*.war /var/lib/tomcat/webapps/
```

#### Step 5: Test (5 minutes)
```bash
# Test greeting generation
curl -X POST http://localhost:3000/greetings/generate

# Save the greeting ID (e.g., GREETING_1734700000000)

# Test QR code generation
curl http://localhost:3000/greetings/GREETING_1734700000000/qr --output test.png

# Test status check
curl http://localhost:3000/greetings/GREETING_1734700000000/view

# Test video upload (with test video file)
curl -X POST http://localhost:3000/greetings/GREETING_1734700000000/upload \
  -F "video=@test.mp4" \
  -F "name=Test User" \
  -F "message=Test Message"
```

#### Step 6: Verify Data (3 minutes)
```bash
# Check MySQL
mysql -u root -pDechub#2025 selfie_preprod

SELECT * FROM greetings ORDER BY created_at DESC LIMIT 1;

# Check S3
aws s3 ls s3://celebrations-tanishq-preprod/greetings/ --recursive

# Should see:
# greetings/GREETING_1734700000000/greeting_video_*.mp4
```

---

## 🎯 PRODUCTION DEPLOYMENT (Step-by-Step)

### Step 1: Create Production Properties File

**File:** `src/main/resources/application-prod.properties`

```properties
#APPLICATION SETTINGS - PRODUCTION ENVIRONMENT
server.port=3000
app.cors.allowedOrigins=https://celebrations.tanishq.co.in

# MySQL Configuration for Production Server
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_prod
spring.datasource.username=root
spring.datasource.password=YOUR_PROD_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

# Logging Configuration
logging.level.org.springframework.web=INFO
logging.level.com.dechub.tanishq=INFO
logging.file.name=/var/log/tanishq/application.log

# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# AWS S3 Configuration (SAME BUCKET STRUCTURE AS PREPROD)
aws.s3.bucket.name=celebrations-tanishq-prod
aws.s3.region=ap-south-1

# AWS Credentials (Option 1: Properties - use IAM role instead)
# aws.access.key.id=YOUR_PROD_AWS_KEY
# aws.secret.access.key=YOUR_PROD_AWS_SECRET

# S3 Presigned URL Expiration (1 hour)
aws.s3.presigned.url.expiration=3600

# Connection Pool Settings
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# System Configuration
system.isWindows=N
```

### Step 2: Create Production S3 Bucket

```bash
# Create production bucket
aws s3 mb s3://celebrations-tanishq-prod --region ap-south-1

# Configure bucket
aws s3api put-public-access-block \
  --bucket celebrations-tanishq-prod \
  --public-access-block-configuration \
  "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"

# Enable encryption
aws s3api put-bucket-encryption \
  --bucket celebrations-tanishq-prod \
  --server-side-encryption-configuration \
  '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'

# Create folder structure
aws s3api put-object --bucket celebrations-tanishq-prod --key greetings/
aws s3api put-object --bucket celebrations-tanishq-prod --key events/

# Verify
aws s3 ls s3://celebrations-tanishq-prod/
```

### Step 3: Configure IAM Permissions (Production)

**Create IAM Policy:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "GreetingVideoManagement",
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject"
      ],
      "Resource": [
        "arn:aws:s3:::celebrations-tanishq-prod/greetings/*"
      ]
    },
    {
      "Sid": "EventImageManagement",
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject"
      ],
      "Resource": [
        "arn:aws:s3:::celebrations-tanishq-prod/events/*"
      ]
    },
    {
      "Sid": "BucketListing",
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::celebrations-tanishq-prod"
      ]
    }
  ]
}
```

**Attach to IAM Role:**
```bash
# If using EC2 instance, attach IAM role to instance
# No credentials needed in properties file

# If using user credentials:
aws.access.key.id=YOUR_PROD_KEY
aws.secret.access.key=YOUR_PROD_SECRET
```

### Step 4: Create Production MySQL Database

```bash
# Connect to production MySQL
mysql -u root -p

# Create database
CREATE DATABASE IF NOT EXISTS selfie_prod
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

# Verify
SHOW DATABASES LIKE 'selfie_prod';

# Exit
exit;
```

### Step 5: Build for Production

```bash
# Update application.properties to use prod profile
nano src/main/resources/application.properties

# Set:
spring.profiles.active=prod

# Build
mvn clean package -DskipTests

# Verify build
ls -lh target/tanishq-selfie-app-*.jar
```

### Step 6: Deploy to Production Server

```bash
# Option A: Copy to server
scp target/tanishq-selfie-app-*.jar user@prod-server:/opt/tanishq/

# Option B: Build on server
ssh user@prod-server
cd /opt/tanishq/tanishq_selfie_app
git pull
mvn clean package

# Create systemd service
sudo nano /etc/systemd/system/tanishq-greeting.service
```

**Service File Content:**
```ini
[Unit]
Description=Tanishq Celebration App
After=network.target mysql.service

[Service]
Type=simple
User=tanishq
WorkingDirectory=/opt/tanishq
ExecStart=/usr/bin/java -jar -Xms2G -Xmx4G -Dspring.profiles.active=prod /opt/tanishq/tanishq-selfie-app.jar
Restart=always
RestartSec=10
StandardOutput=append:/var/log/tanishq/application.log
StandardError=append:/var/log/tanishq/error.log

[Install]
WantedBy=multi-user.target
```

**Start Service:**
```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable service
sudo systemctl enable tanishq-greeting.service

# Start service
sudo systemctl start tanishq-greeting.service

# Check status
sudo systemctl status tanishq-greeting.service

# View logs
sudo journalctl -u tanishq-greeting.service -f
```

### Step 7: Production Testing

```bash
# Test from production server
curl -X POST http://localhost:3000/greetings/generate

# Test from external
curl -X POST https://celebrations.tanishq.co.in/greetings/generate

# Test video upload
curl -X POST https://celebrations.tanishq.co.in/greetings/GREETING_XXX/upload \
  -F "video=@test.mp4" \
  -F "name=Production Test" \
  -F "message=Testing production"
```

### Step 8: Production Verification

```sql
-- Check MySQL
mysql -u root -p selfie_prod

SELECT COUNT(*) FROM greetings;
SELECT * FROM greetings ORDER BY created_at DESC LIMIT 5;

-- Check uploaded videos
SELECT unique_id, uploaded, drive_file_id 
FROM greetings 
WHERE uploaded = true 
ORDER BY created_at DESC 
LIMIT 5;
```

```bash
# Check S3
aws s3 ls s3://celebrations-tanishq-prod/greetings/ --recursive

# Check bucket size
aws s3 ls s3://celebrations-tanishq-prod/greetings/ --recursive --summarize
```

---

## 📊 COMPARISON: Preprod vs Production

| Component | Preprod | Production |
|-----------|---------|------------|
| **Database** | `selfie_preprod` | `selfie_prod` |
| **S3 Bucket** | `celebrations-tanishq-preprod` | `celebrations-tanishq-prod` |
| **Folder** | `greetings/` | `greetings/` (same structure) |
| **Table** | `greetings` | `greetings` (same schema) |
| **Server Port** | 3000 | 3000 |
| **CORS** | `*` (all origins) | `https://celebrations.tanishq.co.in` |
| **Logging** | Console + DEBUG | File + INFO |
| **Credentials** | Properties file | IAM Role (recommended) |

---

## 🔄 MIGRATION FROM PREPROD TO PRODUCTION

If you want to migrate existing data:

### Option 1: Copy S3 Videos

```bash
# Copy entire greetings folder
aws s3 sync \
  s3://celebrations-tanishq-preprod/greetings/ \
  s3://celebrations-tanishq-prod/greetings/ \
  --region ap-south-1
```

### Option 2: Export/Import MySQL Data

```bash
# Export from preprod
mysqldump -u root -pDechub#2025 selfie_preprod greetings > greetings_preprod.sql

# Import to production
mysql -u root -p selfie_prod < greetings_preprod.sql

# Update S3 URLs in production database
mysql -u root -p selfie_prod

UPDATE greetings 
SET drive_file_id = REPLACE(drive_file_id, 
  'celebrations-tanishq-preprod', 
  'celebrations-tanishq-prod')
WHERE drive_file_id IS NOT NULL;
```

---

## ✅ POST-DEPLOYMENT CHECKLIST

### Preprod:
- [ ] AWS credentials added to application-preprod.properties
- [ ] S3 bucket accessible
- [ ] MySQL database created
- [ ] Application started successfully
- [ ] Greeting generation works
- [ ] QR code generation works
- [ ] Video upload works
- [ ] Video playback works
- [ ] Data visible in MySQL
- [ ] Videos visible in S3 under greetings/ folder

### Production:
- [ ] application-prod.properties created
- [ ] Production S3 bucket created with greetings/ folder
- [ ] Production MySQL database created
- [ ] IAM permissions configured
- [ ] Application deployed to server
- [ ] Systemd service configured
- [ ] HTTPS/SSL configured
- [ ] CORS restricted to production domain
- [ ] Monitoring/logging configured
- [ ] Backup strategy in place
- [ ] Load testing completed

---

## 📝 SUMMARY

### What You Already Have ✅
1. **Code**: Uses `greetings/` folder in S3 ✅
2. **MySQL**: Uses `greetings` table (auto-created) ✅
3. **S3 Structure**: Separate folders for events and greetings ✅
4. **QR Fix**: Encodes only uniqueId ✅

### What You Need to Do ⚠️

**Preprod (Immediate):**
1. Add AWS credentials to application-preprod.properties
2. Start application
3. Test greeting flow

**Production (When Ready):**
1. Create application-prod.properties (copy and modify preprod)
2. Create production S3 bucket with same structure
3. Create production MySQL database
4. Deploy application with prod profile
5. Test thoroughly

### Total Time Estimate:
- **Preprod Setup:** 15-20 minutes
- **Production Setup:** 1-2 hours (including testing)

---

**Everything is ready! The code already uses the separate greetings/ folder. Just add AWS credentials and deploy!** 🚀

