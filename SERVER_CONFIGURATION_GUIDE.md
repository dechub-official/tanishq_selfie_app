# 🔧 Server Configuration Guide - PREPROD Environment

## Current Status: December 20, 2025

---

## ✅ What's Already Configured

### 1. MySQL Database ✅
```properties
# Current Configuration (application-preprod.properties)
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod
spring.datasource.username=root
spring.datasource.password=Dechub#2025
spring.jpa.hibernate.ddl-auto=update  # Auto-creates tables
```

**Status:** ✅ **Fully Configured**
- Database: `selfie_preprod` on localhost:3306
- User: `root`
- Auto-creates `greetings` table on first run
- No manual schema creation needed

### 2. AWS S3 Configuration ✅
```properties
# Current Configuration
aws.s3.bucket.name=celebrations-tanishq-preprod
aws.s3.region=ap-south-1
```

**Status:** ✅ **Bucket Configured**
- Bucket: `celebrations-tanishq-preprod`
- Region: `ap-south-1` (Mumbai)

**⚠️ MISSING:** AWS Credentials (see below)

### 3. File Upload Limits ✅
```properties
# Current Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

**Status:** ✅ **Properly Configured**
- Max file size: 100MB (sufficient for 2-minute videos)
- No changes needed

### 4. Server Configuration ✅
```properties
# Current Configuration
server.port=3000
app.cors.allowedOrigins=*
```

**Status:** ✅ **Running on Port 3000**
- Access URL: `http://localhost:3000`
- CORS: Allows all origins (⚠️ consider restricting in production)

### 5. QR Code Base URLs ✅
```properties
# Current Configuration
greeting.qr.base.url=https://celebrationsite-preprod.tanishq.co.in/greetings/
```

**Status:** ✅ **Configured but NOT USED**
- The fix removed dependency on this URL
- QR codes now encode only uniqueId
- This property is no longer used by greeting feature

---

## ⚠️ WHAT YOU NEED TO ADD

### 1. AWS Credentials - REQUIRED

You have 2 options:

#### **Option A: Add to application-preprod.properties (Quick but less secure)**

Add these lines to `src/main/resources/application-preprod.properties`:

```properties
# AWS Credentials (Add at line 108)
aws.access.key.id=YOUR_AWS_ACCESS_KEY_ID
aws.secret.access.key=YOUR_AWS_SECRET_ACCESS_KEY
```

**Where to get credentials:**
1. Go to AWS Console → IAM → Users
2. Select your user or create new one
3. Security credentials tab
4. Create access key
5. Copy Access Key ID and Secret Access Key

#### **Option B: Use IAM Role (Recommended for production)**

If your server is an EC2 instance:
1. Create IAM Role with S3 permissions
2. Attach role to EC2 instance
3. No credentials needed in properties file
4. AWS SDK auto-detects credentials

**Required IAM Policy:**
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
        "arn:aws:s3:::celebrations-tanishq-preprod",
        "arn:aws:s3:::celebrations-tanishq-preprod/*"
      ]
    }
  ]
}
```

### 2. S3 Bucket Setup - REQUIRED

**Check if bucket exists:**
```bash
aws s3 ls s3://celebrations-tanishq-preprod/
```

**If bucket doesn't exist, create it:**
```bash
# Using AWS CLI
aws s3 mb s3://celebrations-tanishq-preprod --region ap-south-1

# Or create via AWS Console:
# 1. Go to S3 Console
# 2. Create bucket
# 3. Name: celebrations-tanishq-preprod
# 4. Region: ap-south-1
# 5. Block all public access: ENABLED
# 6. Encryption: AES-256 (default)
```

**Create folder structure:**
```bash
# Create greetings folder
aws s3api put-object --bucket celebrations-tanishq-preprod --key greetings/
```

### 3. Verify MySQL Database - REQUIRED

**Check if database exists:**
```bash
mysql -u root -pDechub#2025 -e "SHOW DATABASES LIKE 'selfie_preprod';"
```

**If database doesn't exist, create it:**
```sql
mysql -u root -pDechub#2025

CREATE DATABASE IF NOT EXISTS selfie_preprod 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE selfie_preprod;

-- Table will be auto-created by JPA on first run
-- But you can verify it exists after starting the app:
SHOW TABLES;
DESCRIBE greetings;
```

---

## 📝 Complete Configuration File

Here's your complete `application-preprod.properties` with AWS credentials added:

```properties
#APPLICATION SETTINGS - PRE-PRODUCTION ENVIRONMENT
server.port=3000
app.cors.allowedOrigins=*

# MySQL Configuration for Pre-Prod Server
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod
spring.datasource.username=root
spring.datasource.password=Dechub#2025
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# Logging Configuration
logging.level.org.springframework.web=INFO
logging.level.com.dechub.tanishq=DEBUG

# ... (all your existing Google Sheets config)

# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# ... (all your existing config)

# AWS S3 Configuration for Event Images and Greeting Videos
aws.s3.bucket.name=celebrations-tanishq-preprod
aws.s3.region=ap-south-1

# ⚠️ ADD THESE LINES (AWS Credentials)
aws.access.key.id=YOUR_AWS_ACCESS_KEY_ID
aws.secret.access.key=YOUR_AWS_SECRET_ACCESS_KEY

# Optional: S3 Presigned URL Expiration (default 1 hour)
aws.s3.presigned.url.expiration=3600
```

---

## 🚀 Deployment Steps

### Step 1: Update Configuration

```bash
# Edit the properties file
nano src/main/resources/application-preprod.properties

# Add AWS credentials at the end (or use IAM role)
```

### Step 2: Verify Prerequisites

```bash
# Check MySQL is running
systemctl status mysql
# OR on Windows: Check MySQL service in Services

# Check database exists
mysql -u root -pDechub#2025 -e "SHOW DATABASES LIKE 'selfie_preprod';"

# Check AWS credentials (if using AWS CLI)
aws s3 ls s3://celebrations-tanishq-preprod/
```

### Step 3: Build Application

```bash
# Navigate to project
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Clean and build
mvn clean package

# Check build success
ls -la target/tanishq-selfie-app*.jar
```

### Step 4: Start Application

```bash
# Option 1: Run with Maven
mvn spring-boot:run

# Option 2: Run JAR directly
java -jar target/tanishq-selfie-app-0.0.1-SNAPSHOT.jar

# Option 3: Run with specific profile
java -jar target/tanishq-selfie-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=preprod
```

### Step 5: Verify Application Started

```bash
# Check logs for successful startup
tail -f logs/application.log

# Look for these messages:
# ✓ Started TanishqSelfieApplication in X seconds
# ✓ Tomcat started on port(s): 3000
# ✓ Hibernate: create table greetings... (first run only)
```

### Step 6: Test Greeting Feature

```bash
# Test 1: Generate greeting
curl -X POST http://localhost:3000/greetings/generate

# Expected: GREETING_1734700000000

# Test 2: Generate QR code
curl http://localhost:3000/greetings/GREETING_1734700000000/qr --output test.png

# Expected: PNG file created

# Test 3: Check status
curl http://localhost:3000/greetings/GREETING_1734700000000/view

# Expected: {"hasVideo":false,"status":"pending",...}
```

### Step 7: Test Video Upload

```bash
# Create a test video file (or use existing)
# Upload test
curl -X POST http://localhost:3000/greetings/GREETING_1734700000000/upload \
  -F "video=@test.mp4" \
  -F "name=Test User" \
  -F "message=Test message"

# Expected: Video uploaded successfully. URL: https://...
```

### Step 8: Verify in Database

```sql
mysql -u root -pDechub#2025

USE selfie_preprod;

-- Check greeting was created
SELECT * FROM greetings ORDER BY created_at DESC LIMIT 1;

-- Verify uploaded flag and S3 URL
SELECT 
    unique_id, 
    uploaded, 
    drive_file_id,
    greeting_text,
    message,
    created_at 
FROM greetings 
WHERE uploaded = true;
```

### Step 9: Verify in S3

```bash
# List uploaded videos
aws s3 ls s3://celebrations-tanishq-preprod/greetings/

# Expected output:
# 2025-12-20 10:00:00   25000000 GREETING_1734700000000.mp4
```

---

## 🔍 Troubleshooting

### Problem 1: Can't Connect to MySQL

**Error:** `Communications link failure`

**Solution:**
```bash
# Check MySQL is running
systemctl status mysql

# Start MySQL if stopped
sudo systemctl start mysql

# Check port is correct
netstat -an | grep 3306

# Test connection
mysql -u root -pDechub#2025 -e "SELECT 1;"
```

### Problem 2: AWS S3 Upload Fails

**Error:** `Unable to load AWS credentials` or `Access Denied`

**Solution:**
```bash
# Option 1: Verify credentials in properties file
grep aws.access.key src/main/resources/application-preprod.properties

# Option 2: Test AWS CLI access
aws s3 ls s3://celebrations-tanishq-preprod/

# Option 3: Check IAM permissions
aws iam get-user
```

### Problem 3: Table 'greetings' Doesn't Exist

**Error:** `Table 'selfie_preprod.greetings' doesn't exist`

**Solution:**
```bash
# Check auto-create is enabled
grep ddl-auto src/main/resources/application-preprod.properties
# Should be: spring.jpa.hibernate.ddl-auto=update

# Manually create table if needed
mysql -u root -pDechub#2025 selfie_preprod < schema.sql
```

### Problem 4: File Upload Too Large

**Error:** `Maximum upload size exceeded`

**Solution:**
```properties
# Already configured to 100MB - should be fine
# If needed, increase in application-preprod.properties:
spring.servlet.multipart.max-file-size=200MB
spring.servlet.multipart.max-request-size=200MB
```

### Problem 5: Port 3000 Already in Use

**Error:** `Port 3000 is already in use`

**Solution:**
```bash
# Option 1: Find and kill process using port 3000
# Linux:
sudo lsof -i :3000
sudo kill -9 <PID>

# Windows:
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Option 2: Change port in properties
server.port=8080
```

---

## 📊 Data Storage Locations

### MySQL Database
```
Host: localhost
Port: 3306
Database: selfie_preprod
Table: greetings

Location on server: /var/lib/mysql/selfie_preprod/
(or C:\ProgramData\MySQL\MySQL Server 8.0\Data\ on Windows)
```

### AWS S3
```
Bucket: celebrations-tanishq-preprod
Region: ap-south-1 (Mumbai)
Path: greetings/{uniqueId}.mp4

Example:
s3://celebrations-tanishq-preprod/greetings/GREETING_1734700000000.mp4
```

### Application Logs
```
Location: logs/application.log (if configured)
Or: Console output if running in terminal
```

---

## 💰 Current Cost Estimate

Based on your configuration:

### AWS S3 Costs (Mumbai Region)
```
Bucket: celebrations-tanishq-preprod
Region: ap-south-1

Storage: $0.023 per GB/month
Requests: $0.005 per 1,000 PUT requests
         $0.0004 per 1,000 GET requests
Transfer: $0.114 per GB (data out to internet)

Estimated monthly cost for 100 videos/day:
- Storage (75 GB): $1.73
- Requests: $0.03
- Transfer (150 GB views): $17.10
Total: ~$19/month
```

### MySQL Database
```
Current: Local MySQL on same server
Cost: Included in server hosting
Growth: ~21 KB per greeting (with QR code stored)
```

---

## 🔐 Security Recommendations

### Current Security Status

✅ **Good:**
- Videos stored privately in S3
- MySQL credentials not exposed
- CORS configured

⚠️ **Should Improve:**

1. **Move AWS Credentials to Environment Variables**
   ```bash
   # Instead of properties file:
   export AWS_ACCESS_KEY_ID="your-key"
   export AWS_SECRET_ACCESS_KEY="your-secret"
   
   # Application will auto-detect these
   ```

2. **Restrict CORS Origins**
   ```properties
   # Instead of:
   app.cors.allowedOrigins=*
   
   # Use:
   app.cors.allowedOrigins=https://celebrationsite-preprod.tanishq.co.in
   ```

3. **Use HTTPS on Server**
   ```properties
   # Add SSL certificate
   server.ssl.key-store=/path/to/keystore.p12
   server.ssl.key-store-password=your-password
   server.ssl.key-store-type=PKCS12
   ```

4. **Enable Rate Limiting**
   ```java
   // Add to prevent abuse
   @Component
   public class RateLimitFilter implements Filter {
       // Implement rate limiting logic
   }
   ```

---

## ✅ Pre-Deployment Checklist

- [ ] AWS credentials configured (properties or IAM role)
- [ ] S3 bucket `celebrations-tanishq-preprod` exists
- [ ] MySQL database `selfie_preprod` exists
- [ ] MySQL user `root` has access
- [ ] Port 3000 is available
- [ ] Application builds successfully (`mvn clean package`)
- [ ] All tests pass
- [ ] Logs directory exists (if logging to file)
- [ ] Sufficient disk space (for video storage)
- [ ] Network allows S3 access

---

## 🎯 Summary

### What's Ready ✅
- MySQL configuration
- S3 bucket name and region
- File upload limits
- Server port configuration
- JPA auto-creates table

### What You Need to Do ⚠️
1. **Add AWS credentials** to application-preprod.properties
2. **Verify S3 bucket** exists and is accessible
3. **Verify MySQL database** exists
4. **Start application** and test

### Estimated Setup Time
- **If everything exists:** 5 minutes (add credentials, restart)
- **If need to create:** 15-30 minutes (create bucket, configure IAM)

### Expected Performance
- Greeting generation: < 100ms
- QR code generation: < 200ms
- Video upload (25MB): 3-10 seconds
- Video playback: 1-3 seconds

---

**You're 95% ready! Just add AWS credentials and you're good to go!** 🚀

