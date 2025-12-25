# 🧪 PRE-PROD TESTING GUIDE

**Quick guide to test all functionality on server**

---

## 🚀 QUICK START

### **Upload and Run Test Script:**

```bash
# 1. Upload test_preprod.sh to server using WinSCP
# Source: C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\test_preprod.sh
# Destination: /opt/tanishq/applications_preprod/test_preprod.sh

# 2. Make it executable
chmod +x /opt/tanishq/applications_preprod/test_preprod.sh

# 3. Run the test
cd /opt/tanishq/applications_preprod
./test_preprod.sh

# Or run with output saved to file
./test_preprod.sh | tee test_results.txt
```

---

## ✅ WHAT THE SCRIPT TESTS

The script performs **15 comprehensive tests:**

1. ✅ **Application Health Check** - Verifies app is running
2. ✅ **Database Connection** - Tests MySQL connectivity
3. ✅ **S3 Service Access** - Verifies AWS S3 bucket access
4. ✅ **Create Test Store** - Creates TEST001 store (if not exists)
5. ✅ **Create Test User** - Creates test login credentials
6. ✅ **Test Login API** - Verifies authentication works
7. ✅ **Create Test Event** - Creates a sample event in database
8. ✅ **Add Test Attendee** - Adds sample attendee to event
9. ✅ **Test S3 File Upload** - Uploads test image to S3 via API
10. ✅ **Get Events List API** - Tests event retrieval
11. ✅ **Test QR Code Generation** - Verifies QR code creation
12. ✅ **Verify Database Updates** - Checks all data saved correctly
13. ✅ **Check Application Logs** - Reviews for errors
14. ✅ **Test Direct IP Access** - Tests http://10.160.128.94
15. ✅ **Test Nginx Proxy** - Verifies nginx is working

---

## 📊 EXPECTED OUTPUT

```
🧪 TANISHQ PRE-PROD - COMPLETE TESTING SCRIPT
==============================================
Date: Tue Dec  3 11:30:00 UTC 2025
Server: ip-10-160-128-94.ap-south-1.compute.internal

📋 TEST 1: Application Health Check
------------------------------------
✅ PASS: Application is running (HTTP 200)

📋 TEST 2: Database Connection
------------------------------------
✅ PASS: Database connection successful
Database Statistics:
+--------------+--------------+-----------------+-------------+
| Total Stores | Total Events | Total Attendees | Total Users |
+--------------+--------------+-----------------+-------------+
|           45 |           12 |              85 |          25 |
+--------------+--------------+-----------------+-------------+

📋 TEST 3: S3 Service Access
------------------------------------
✅ PASS: S3 bucket accessible
S3 Bucket contents:
                           PRE Test/

... (continues for all 15 tests)

==============================================
🎯 TEST RESULTS SUMMARY
==============================================

Total Tests: 15
Passed: 15
Failed: 0

🎉 ALL TESTS PASSED! Pre-prod is working perfectly!

✅ Application is ready for use
✅ Database is working
✅ S3 integration is active
✅ All APIs are functional

Test Event ID: TEST001_1733229600
You can view this event via API or database
```

---

## 🔧 MANUAL TESTS (Alternative)

If you prefer to test manually, here are the key commands:

### **Test 1: Check Application**
```bash
curl -I http://localhost:3002
# Expected: HTTP/1.1 200 OK
```

### **Test 2: Test Login API**
```bash
curl -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d '{"code":"TEST001","password":"test123"}'
# Expected: {"status":true,...}
```

### **Test 3: Create Event Manually**
```bash
mysql -u root -pDechub#2025 selfie_preprod -e "
INSERT INTO events (id, store_code, event_name, event_type, start_date, created_at, invitees, attendees, region)
VALUES ('TEST_MANUAL_001', 'TEST001', 'Manual Test Event', 'Exhibition', '2025-12-20', NOW(), 0, 0, 'SOUTH');
"
```

### **Test 4: Upload Test File to S3**
```bash
# Create test file
echo "Test image content" > /tmp/test.jpg

# Upload via API
curl -X POST http://localhost:3002/events/uploadCompletedEvents \
  -F "eventId=TEST_MANUAL_001" \
  -F "files=@/tmp/test.jpg"

# Verify in S3
export PATH=/usr/local/aws-cli/v2/current/bin:$PATH
aws s3 ls s3://celebrations-tanishq-preprod/events/TEST_MANUAL_001/
```

### **Test 5: Get Events**
```bash
curl -X POST http://localhost:3002/events/getevents \
  -H "Content-Type: application/json" \
  -d '{"storeCode":"TEST001","startDate":"2025-12-01","endDate":"2025-12-31"}'
```

### **Test 6: Generate QR Code**
```bash
curl http://localhost:3002/events/dowload-qr/TEST_MANUAL_001 > /tmp/qr.json
cat /tmp/qr.json
# Should contain Base64 QR code data
```

---

## 📁 TEST DATA CREATED

The script creates these test records:

### **Test Store:**
```
Store Code: TEST001
Store Name: Test Store Bangalore
City: Bangalore
Region: SOUTH
```

### **Test User:**
```
Username: TEST001
Password: test123
Role: STORE_MANAGER
```

### **Test Event:**
```
Event ID: TEST001_{timestamp}
Event Name: Test Wedding Exhibition
Type: Exhibition / Wedding
Date: 2025-12-15
Location: Bangalore
```

### **Test Attendee:**
```
Name: Test Customer
Phone: 9876543210
Interest: Gold Necklace
First Time: Yes
```

---

## 🗄️ VERIFY DATA IN DATABASE

```bash
# Show test store
mysql -u root -pDechub#2025 selfie_preprod -e "
SELECT * FROM stores WHERE store_code='TEST001'\G
"

# Show test events
mysql -u root -pDechub#2025 selfie_preprod -e "
SELECT id, event_name, event_type, start_date, attendees 
FROM events 
WHERE store_code='TEST001';
"

# Show test attendees
mysql -u root -pDechub#2025 selfie_preprod -e "
SELECT a.name, a.phone, a.like_product, e.event_name
FROM attendees a
JOIN events e ON a.event_id = e.id
WHERE e.store_code='TEST001';
"

# Show S3 links
mysql -u root -pDechub#2025 selfie_preprod -e "
SELECT id, completed_events_drive_link 
FROM events 
WHERE completed_events_drive_link IS NOT NULL;
"
```

---

## 📤 VERIFY S3 UPLOADS

```bash
# Set AWS CLI path
export PATH=/usr/local/aws-cli/v2/current/bin:$PATH

# List all event folders
aws s3 ls s3://celebrations-tanishq-preprod/events/

# List files for specific event
aws s3 ls s3://celebrations-tanishq-preprod/events/TEST001_1733229600/

# Download a test file
aws s3 cp s3://celebrations-tanishq-preprod/events/TEST001_1733229600/event_20251203_113000_1733229600123.jpg /tmp/

# View file
file /tmp/event_20251203_113000_1733229600123.jpg
```

---

## 🧹 CLEANUP TEST DATA

```bash
# Delete test event
mysql -u root -pDechub#2025 selfie_preprod -e "
DELETE FROM attendees WHERE event_id LIKE 'TEST001_%';
DELETE FROM events WHERE id LIKE 'TEST001_%';
"

# Delete S3 test files
aws s3 rm s3://celebrations-tanishq-preprod/events/TEST001_1733229600/ --recursive

# Keep test store and user for future tests
# (or delete them if you want clean state)
```

---

## 🎯 SUCCESS CRITERIA

All tests should pass if:
- ✅ Application returns HTTP 200
- ✅ Database is accessible
- ✅ S3 bucket is accessible
- ✅ APIs return valid JSON
- ✅ Files are uploaded to S3
- ✅ Data is saved to database
- ✅ QR codes are generated
- ✅ No critical errors in logs

---

## 🚨 TROUBLESHOOTING

### **If tests fail:**

1. **Check application is running:**
   ```bash
   ps -ef | grep tanishq | grep -v grep
   ```

2. **Check application logs:**
   ```bash
   tail -50 /opt/tanishq/applications_preprod/application.log
   ```

3. **Check database connection:**
   ```bash
   mysql -u root -pDechub#2025 -e "SHOW DATABASES;"
   ```

4. **Check S3 access:**
   ```bash
   export PATH=/usr/local/aws-cli/v2/current/bin:$PATH
   aws sts get-caller-identity
   aws s3 ls s3://celebrations-tanishq-preprod/
   ```

5. **Check nginx:**
   ```bash
   systemctl status nginx
   curl -I http://localhost
   ```

---

## 📊 AFTER TESTING

Once tests pass:

1. ✅ **Document results** - Save test output
2. ✅ **Keep test data** - For future reference
3. ✅ **Share with team** - Show everything works
4. 📧 **Email network team** - To fix ELB (see FINAL_DEPLOYMENT_STATUS.md)
5. ⏳ **Wait for ELB fix** - Domain will work after
6. 🎉 **Go live!** - Pre-prod is ready

---

## 🎊 SUMMARY

**To test everything:**

```bash
# Upload test_preprod.sh to server
# Then run:
cd /opt/tanishq/applications_preprod
chmod +x test_preprod.sh
./test_preprod.sh | tee test_results.txt
cat test_results.txt
```

**Expected result:** ✅ All 15 tests pass

**This proves:**
- Application works ✅
- Database works ✅
- S3 works ✅
- APIs work ✅
- Everything ready! ✅

---

**Upload the script and run it now!** 🚀

