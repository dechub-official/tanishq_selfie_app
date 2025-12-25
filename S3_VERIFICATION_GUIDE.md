```

**Expected Output:**
```json
{
    "UserId": "AIDAXXXXXXXXXXXXXXXXX",
    "Account": "123456789012",
    "Arn": "arn:aws:iam::123456789012:user/tanishq-preprod"
}
```

**If error:** AWS credentials not configured (see below)

---

### Step 3: Test Bucket Access
```cmd
aws s3api head-bucket --bucket celebrations-tanishq-preprod --region ap-south-1
```

**Expected:** No output = Success ✅

**If error 403:** Permission denied - check IAM permissions  
**If error 404:** Bucket doesn't exist - check bucket name

---

### Step 4: Check Bucket Permissions
```cmd
aws s3api get-bucket-acl --bucket celebrations-tanishq-preprod --region ap-south-1
```

**Expected Output:**
```json
{
    "Owner": {
        "DisplayName": "tanishq",
        "ID": "..."
    },
    "Grants": [...]
}
```

---

### Step 5: Test File Upload (Write Access)
```cmd
REM Create test file
echo Test upload > test-s3-upload.txt

REM Upload to S3
aws s3 cp test-s3-upload.txt s3://celebrations-tanishq-preprod/test-upload.txt --region ap-south-1

REM Verify upload
aws s3 ls s3://celebrations-tanishq-preprod/test-upload.txt --region ap-south-1

REM Clean up
aws s3 rm s3://celebrations-tanishq-preprod/test-upload.txt --region ap-south-1
del test-s3-upload.txt
```

**If successful:** ✅ Write permissions working!

---

### Step 6: Test File Download (Read Access)
```cmd
REM List recent files
aws s3 ls s3://celebrations-tanishq-preprod/events/ --recursive --region ap-south-1 | findstr ".jpg" | more

REM Copy one file to test (replace with actual file path)
aws s3 cp s3://celebrations-tanishq-preprod/events/TEST_123/photo.jpg test-download.jpg --region ap-south-1

REM Verify downloaded file exists
dir test-download.jpg

REM Clean up
del test-download.jpg
```

**If successful:** ✅ Read permissions working!

---

## 🏥 APPLICATION-LEVEL VERIFICATION

### Check if Application is Using S3

#### On Server (SSH to 10.160.128.94):
```bash
# Check application logs for S3 activity
cd /opt/tanishq/applications_preprod
grep -i "s3\|aws" application.log | tail -20

# Check for upload success messages
grep "Successfully uploaded" application.log | tail -10

# Check for S3 errors
grep -i "s3.*error\|s3.*exception" application.log | tail -10
```

#### Expected Log Entries:
```
2025-12-08 10:30:45 INFO  S3Service - Successfully uploaded file to S3: events/TEST_123/event_20251208_103045.jpg
2025-12-08 10:30:45 INFO  S3Service - S3 URL: https://celebrations-tanishq-preprod.s3.ap-south-1.amazonaws.com/events/TEST_123/event_20251208_103045.jpg
```

---

### Check S3 Configuration in Application
```bash
cd /opt/tanishq/applications_preprod
unzip -p tanishq-preprod-08-12-2025-2-0.0.1-SNAPSHOT.war WEB-INF/classes/application-preprod.properties | grep s3
```

**Expected Output:**
```properties
aws.s3.bucket.name=celebrations-tanishq-preprod
aws.s3.region=ap-south-1
```

---

## 🧪 FUNCTIONAL TESTING

### Test 1: Upload Photo via Application
1. Login to application: http://10.160.128.94:3000
2. Create or select an event
3. Upload a customer photo
4. Note the event code (e.g., `STORE001_1733642400`)

### Test 2: Verify Photo in S3
```cmd
REM Replace EVENT_CODE with the event code from Test 1
aws s3 ls s3://celebrations-tanishq-preprod/events/EVENT_CODE/ --region ap-south-1
```

**Expected:** You should see the uploaded photo

### Test 3: Download and Verify Photo
```cmd
REM Get latest photo from event
aws s3 cp s3://celebrations-tanishq-preprod/events/EVENT_CODE/latest-photo.jpg downloaded-photo.jpg --region ap-south-1

REM Open the photo
start downloaded-photo.jpg
```

**Expected:** Photo opens successfully in image viewer

---

## 📊 S3 HEALTH CHECK SCRIPT

Create a quick health check batch file:

```cmd
@echo off
echo ========================================
echo S3 BUCKET HEALTH CHECK
echo ========================================
echo.

echo [1/5] Checking AWS CLI...
aws --version
if %errorlevel% neq 0 (
    echo ❌ AWS CLI not found
    exit /b 1
)
echo ✅ AWS CLI installed
echo.

echo [2/5] Checking AWS credentials...
aws sts get-caller-identity --region ap-south-1 >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ AWS credentials not configured
    exit /b 1
)
echo ✅ AWS credentials valid
echo.

echo [3/5] Checking bucket access...
aws s3api head-bucket --bucket celebrations-tanishq-preprod --region ap-south-1 2>nul
if %errorlevel% neq 0 (
    echo ❌ Cannot access bucket
    exit /b 1
)
echo ✅ Bucket accessible
echo.

echo [4/5] Listing bucket contents...
aws s3 ls s3://celebrations-tanishq-preprod/ --region ap-south-1
echo.

echo [5/5] Counting files...
for /f %%i in ('aws s3 ls s3://celebrations-tanishq-preprod/ --recursive --region ap-south-1 ^| find /c ".jpg"') do set COUNT=%%i
echo ✅ Total photos in bucket: %COUNT%
echo.

echo ========================================
echo ✅ S3 BUCKET IS WORKING!
echo ========================================
```

**Save as:** `check-s3-health.bat`

**Run with:**
```cmd
check-s3-health.bat
```

---

## ❌ TROUBLESHOOTING

### Error: "Unable to locate credentials"
**Problem:** AWS credentials not configured

**Solution:**
```cmd
REM Configure AWS credentials
aws configure

REM When prompted, enter:
AWS Access Key ID: [YOUR_ACCESS_KEY]
AWS Secret Access Key: [YOUR_SECRET_KEY]
Default region name: ap-south-1
Default output format: json
```

**Verify:**
```cmd
aws sts get-caller-identity
```

---

### Error: "The specified bucket does not exist"
**Problem:** Bucket name is incorrect or bucket doesn't exist

**Solution:**
```cmd
REM List all your S3 buckets
aws s3 ls --region ap-south-1

REM Create bucket if it doesn't exist
aws s3 mb s3://celebrations-tanishq-preprod --region ap-south-1
```

---

### Error: "Access Denied" or 403
**Problem:** IAM user doesn't have S3 permissions

**Solution:** Add this IAM policy to your AWS user:
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

---

### Error: Photos not appearing in S3
**Problem:** Application not uploading to S3

**Checklist:**
1. Check application logs for errors:
   ```bash
   grep -i "s3.*error" application.log
   ```

2. Verify S3 configuration in properties file:
   ```bash
   grep s3 /opt/tanishq/applications_preprod/WEB-INF/classes/application-preprod.properties
   ```

3. Verify AWS credentials on server:
   ```bash
   aws s3 ls s3://celebrations-tanishq-preprod/ --region ap-south-1
   ```

4. Check if S3Service is being used:
   ```bash
   grep "S3Service" application.log | tail -20
   ```

---

## 🌐 WEB BROWSER VERIFICATION

### Option 1: AWS Console
1. Open: https://s3.console.aws.amazon.com/s3/buckets/celebrations-tanishq-preprod?region=ap-south-1
2. Login with AWS credentials
3. Navigate to `events/` folder
4. You should see all event folders
5. Click on any event folder to see photos

### Option 2: Direct S3 URL (if public)
**Format:**
```
https://celebrations-tanishq-preprod.s3.ap-south-1.amazonaws.com/events/EVENT_CODE/photo.jpg
```

**Note:** Only works if bucket has public read access

---

## 📈 MONITORING S3 USAGE

### Check Bucket Size and Cost
```cmd
aws s3 ls s3://celebrations-tanishq-preprod/ --recursive --summarize --human-readable --region ap-south-1
```

### Monitor Recent Uploads
```cmd
REM Show files uploaded in last 24 hours (Windows)
aws s3 ls s3://celebrations-tanishq-preprod/events/ --recursive --region ap-south-1 | findstr "%date%"
```

### Export File List to CSV
```cmd
aws s3 ls s3://celebrations-tanishq-preprod/events/ --recursive --region ap-south-1 > s3-file-list.txt
```

---

## ✅ SUCCESS INDICATORS

Your S3 bucket is working correctly if:

- ✅ AWS CLI can list bucket contents
- ✅ You can upload test files
- ✅ You can download existing files
- ✅ Application logs show "Successfully uploaded to S3"
- ✅ Photos appear in S3 after uploading via application
- ✅ Photo count increases when events are created
- ✅ No S3 errors in application logs

---

## 📞 QUICK REFERENCE

### Your S3 Configuration
```
Bucket Name: celebrations-tanishq-preprod
Region: ap-south-1 (Mumbai)
Folder Structure: events/{EVENT_CODE}/{photo_files}
Application Config: src/main/resources/application-preprod.properties
```

### Most Useful Commands
```cmd
REM List all events
aws s3 ls s3://celebrations-tanishq-preprod/events/ --region ap-south-1

REM List specific event photos
aws s3 ls s3://celebrations-tanishq-preprod/events/EVENT_CODE/ --region ap-south-1

REM Count total photos
aws s3 ls s3://celebrations-tanishq-preprod/ --recursive --region ap-south-1 | find /c ".jpg"

REM Check bucket size
aws s3 ls s3://celebrations-tanishq-preprod/ --recursive --summarize --human-readable --region ap-south-1
```

---

**Created:** December 8, 2025  
**For:** Tanishq Celebrations S3 Storage Verification  
**Bucket:** celebrations-tanishq-preprod (ap-south-1)
# 🪣 S3 BUCKET VERIFICATION GUIDE

**Date:** December 8, 2025  
**Bucket:** `celebrations-tanishq-preprod`  
**Region:** `ap-south-1` (Mumbai)  

---

## 📋 QUICK VERIFICATION COMMANDS

### 1️⃣ Check if S3 Bucket is Accessible
```cmd
aws s3 ls s3://celebrations-tanishq-preprod/ --region ap-south-1
```

**Expected Output:**
```
                           PRE events/
```

**If you see this:** ✅ S3 bucket is working and accessible!

**If you see error:** ❌ Problem detected (see troubleshooting below)

---

### 2️⃣ Check What's Inside the Bucket
```cmd
aws s3 ls s3://celebrations-tanishq-preprod/events/ --region ap-south-1
```

**Expected Output:**
```
                           PRE EVENT_CODE_1234567890/
                           PRE STORE001_test123/
                           PRE TEST_123/
```

**This shows:** All event folders where photos are stored

---

### 3️⃣ Check Specific Event Photos
```cmd
REM Replace EVENT_CODE with actual event code
aws s3 ls s3://celebrations-tanishq-preprod/events/EVENT_CODE/ --region ap-south-1
```

**Expected Output:**
```
2025-12-08 10:30:45      45678 event_20251208_103045_1733641845123.jpg
2025-12-08 10:31:12      52341 event_20251208_103112_1733641872456.jpg
2025-12-08 10:32:05      48932 event_20251208_103205_1733641925789.jpg
```

**This shows:** Individual photos uploaded during events

---

### 4️⃣ Count Total Files in Bucket
```cmd
aws s3 ls s3://celebrations-tanishq-preprod/ --recursive --region ap-south-1 | find /c ".jpg"
```

**Expected Output:**
```
150
```

**This shows:** Total number of photos uploaded to S3

---

### 5️⃣ Check Total Storage Size
```cmd
aws s3 ls s3://celebrations-tanishq-preprod/ --recursive --summarize --human-readable --region ap-south-1
```

**Expected Output:**
```
Total Objects: 150
   Total Size: 7.2 MiB
```

---

## 🔍 DETAILED VERIFICATION STEPS

### Step 1: Verify AWS CLI is Installed
```cmd
aws --version
```

**Expected:**
```
aws-cli/2.x.x Python/3.x.x Windows/10 exe/AMD64
```

**If not installed:** Download from https://aws.amazon.com/cli/

---

### Step 2: Verify AWS Credentials
```cmd
aws sts get-caller-identity --region ap-south-1

