# Deployment Checklist - S3 Pre-Signed URL Fix

## ✅ Changes Completed

### Backend Changes
- [x] Fixed `AwsS3StorageService.java` to generate pre-signed URLs
- [x] Added `GeneratePresignedUrlRequest` import
- [x] Updated `uploadGreetingVideo()` method
- [x] Added `generatePresignedUrl()` helper method
- [x] URLs now expire after 7 days (configurable)

### Previous Fixes (Already Done)
- [x] Fixed `GreetingController.java` to return null for `driveFileId` 
- [x] Fixed controller to use `videoPlaybackUrl` field properly

## 📋 Deployment Steps

### 1. Rebuild the Project
```bash
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -P preprod
```

Expected output: `tanishq-preprod-31-01-2026-4-0.0.1-SNAPSHOT.war` in `target/` folder

### 2. Stop Tomcat
```bash
# SSH to server
sudo systemctl stop tomcat
# or
sudo service tomcat stop
```

### 3. Backup Old WAR (Optional but Recommended)
```bash
cd /opt/tomcat/webapps
sudo mv tanishq.war tanishq.war.backup.$(date +%Y%m%d_%H%M%S)
```

### 4. Deploy New WAR
```bash
# Upload new WAR to server
scp target/tanishq-preprod-31-01-2026-4-0.0.1-SNAPSHOT.war user@server:/tmp/

# SSH to server and deploy
sudo cp /tmp/tanishq-preprod-31-01-2026-4-0.0.1-SNAPSHOT.war /opt/tomcat/webapps/tanishq.war
sudo chown tomcat:tomcat /opt/tomcat/webapps/tanishq.war
```

### 5. Start Tomcat
```bash
sudo systemctl start tomcat
# Check logs
sudo tail -f /opt/tomcat/logs/catalina.out
```

### 6. Verify Deployment
Wait for log message:
```
AWS S3 Storage Service initialized successfully. Bucket: celebrations-tanishq, Region: ap-south-1
```

## 🧪 Testing Steps

### Test 1: Generate New QR Code
1. Go to admin panel or use API
2. Generate a new greeting: `POST /greetings/generate`
3. Get QR code: `GET /greetings/{uniqueId}/qr`
4. Expected: QR code image generated successfully

### Test 2: Scan QR & Upload Video
1. Scan the QR code with your phone
2. Record a video greeting
3. Fill in name, phone, message
4. Submit the form
5. Expected: "Video uploaded successfully" message

### Test 3: View Greeting
1. Scan the same QR code again (or navigate to view page)
2. Expected: Video displays and plays correctly
3. **NO 403 Forbidden error!**

### Test 4: Check Backend Logs
```bash
sudo grep "pre-signed URL" /opt/tomcat/logs/catalina.out
```
Expected output:
```
Generated pre-signed URL for greetings/GREETING_XXX/video.mp4 (expires in 10080 minutes)
Successfully uploaded greeting video to S3: https://celebrations-tanishq.s3...?AWSAccessKeyId=...
```

### Test 5: Verify URL Format
Check the database or API response for the `drive_file_id` field:
```sql
SELECT unique_id, drive_file_id FROM greetings WHERE uploaded = true LIMIT 1;
```

Expected format:
```
https://celebrations-tanishq.s3.ap-south-1.amazonaws.com/greetings/GREETING_XXX/video.mp4?AWSAccessKeyId=XXX&Signature=YYY&Expires=1738406400
```

The URL should include:
- ✅ `AWSAccessKeyId` parameter
- ✅ `Signature` parameter
- ✅ `Expires` parameter

## 🚨 Rollback Plan (If Issues Occur)

### Quick Rollback
```bash
sudo systemctl stop tomcat
sudo rm /opt/tomcat/webapps/tanishq.war
sudo rm -rf /opt/tomcat/webapps/tanishq
sudo cp tanishq.war.backup.YYYYMMDD_HHMMSS /opt/tomcat/webapps/tanishq.war
sudo systemctl start tomcat
```

## ⚠️ Important Notes

1. **IAM Permissions Required**: The EC2 instance IAM role must have:
   - `s3:PutObject` - To upload files
   - `s3:GetObject` - To generate pre-signed URLs
   - `s3:GeneratePresignedUrl` - Implicit with GetObject

2. **URL Expiration**: Pre-signed URLs expire after 7 days
   - Videos uploaded today will be accessible until: February 7, 2026
   - After expiration, users need to request a new URL (future enhancement)

3. **Database**: The `drive_file_id` column will now contain pre-signed URLs
   - These are LONG strings (~300-500 characters)
   - Ensure column size is adequate: `VARCHAR(1000)` or `TEXT`

4. **No Bucket Changes Needed**: S3 bucket stays private, no policy changes required

## 📊 Monitoring

After deployment, monitor:
- Application logs for S3 upload success/failures
- Error rate for greeting uploads
- User reports of "Video not playing"
- 403 errors should be eliminated

## ✨ Expected Outcome

- ✅ Videos upload to S3 successfully
- ✅ Pre-signed URLs generated automatically
- ✅ Videos play in frontend without 403 errors
- ✅ Security maintained (private S3 bucket)
- ✅ Time-limited access (7 days)

## 📞 Support

If issues persist after deployment:
1. Check Tomcat logs: `sudo tail -f /opt/tomcat/logs/catalina.out`
2. Verify IAM role permissions on EC2 instance
3. Test S3 connectivity: `aws s3 ls s3://celebrations-tanishq/` from server
4. Check application.properties for correct bucket/region configuration

