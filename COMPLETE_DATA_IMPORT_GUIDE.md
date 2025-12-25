
echo ""
echo "2. Sample Data:"
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT storeCode, storeName FROM stores LIMIT 3;"

echo ""
echo "3. API Health Check:"
curl -s http://localhost:3002/actuator/health 2>/dev/null || echo "Application running on port 3002"

echo ""
echo "4. Application Logs (last 10 lines):"
tail -10 /opt/tanishq/applications_preprod/application.log

echo ""
echo "========================================="
echo "Verification Complete!"
echo "========================================="
```

---

## 🎯 QUICK REFERENCE - All Commands in Order

```bash
# ON WINDOWS (Local Machine):
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
export_database_for_preprod.bat

# Upload file using WinSCP to /tmp/ on server

# ON SERVER (SSH to 10.160.128.94):
# Verify file
ls -lh /tmp/tanishq_backup_*.sql

# Import with database rename
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
sed 's/DATABASE `tanishq`/DATABASE `selfie_preprod`/g' $BACKUP_FILE | \
sed 's/USE `tanishq`/USE `selfie_preprod`/g' | \
mysql -u root -pDechub#2025

# Verify import
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores; SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM events;"

# Test login
USER_CODE=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N)
USER_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$USER_CODE' LIMIT 1;" -s -N)
curl -X POST http://localhost:3002/events/login -H "Content-Type: application/json" -d "{\"code\":\"$USER_CODE\",\"password\":\"$USER_PASS\"}"

# Cleanup
rm /tmp/tanishq_backup_*.sql
```

---

## 🚀 START NOW!

**Step 1:** Run `export_database_for_preprod.bat` on your Windows machine  
**Step 2:** Upload the generated `.sql` file to server `/tmp/`  
**Step 3:** Run import command on server  
**Step 4:** Verify and test  

**You're ready to go!** 🎉
# 🚀 COMPLETE DATA IMPORT GUIDE - LOCAL TO PREPROD

**Goal:** Import ALL data from your local database to preprod server  
**Source:** Local MySQL (tanishq database)  
**Destination:** Preprod Server (selfie_preprod database)  

---

## 📋 STEP-BY-STEP PROCESS

### **PART 1: Export from Local Windows Machine** 🖥️

#### **Step 1: Open Command Prompt on Your Windows PC**

Press `Win + R`, type `cmd`, press Enter

#### **Step 2: Navigate to Project Folder**

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
```

#### **Step 3: Run Export Script**

```cmd
export_database_for_preprod.bat
```

**Expected Output:**
```
================================================
Database Export for Pre-Production Migration
================================================

Database: tanishq
User: nagaraj_jadar
Backup Directory: database_backup
Backup File: tanishq_backup_20251203_HHMMSS.sql

[Step 1/4] Creating backup directory...
[Step 2/4] Checking for mysqldump...
[Step 3/4] Exporting database...
Export completed successfully!
[Step 4/4] Verifying export file...

================================================
SUCCESS: Database exported successfully!
================================================

File Details:
- Location: C:\JAVA\...\database_backup\tanishq_backup_20251203_HHMMSS.sql
- Size: XXXXX bytes
```

✅ **File Created:** `database_backup\tanishq_backup_20251203_HHMMSS.sql`

---

### **PART 2: Upload to Preprod Server** 📤

#### **Method A: Using WinSCP (RECOMMENDED - EASY)**

1. **Open WinSCP** (if not installed, download from winscp.net)

2. **Connect to Server:**
   - **File Protocol:** SFTP
   - **Host name:** `10.160.128.94`
   - **Port:** 22
   - **User name:** `root` (or nishal if you have that access)
   - **Password/Key:** Your SSH credentials

3. **Upload File:**
   - Left panel: Navigate to `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\database_backup\`
   - Right panel: Navigate to `/tmp/`
   - Drag and drop the `tanishq_backup_*.sql` file to `/tmp/`
   - Wait for upload to complete ✅

4. **Verify Upload:**
   - Check file appears in `/tmp/` on server
   - Check file size matches local file

---

#### **Method B: Using SCP Command (Advanced)**

```cmd
scp C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\database_backup\tanishq_backup_*.sql root@10.160.128.94:/tmp/
```

Enter password when prompted.

---

### **PART 3: Import on Preprod Server** 🚀

#### **Step 1: Connect to Server via SSH**

Use PuTTY or any SSH client:
- **Host:** `10.160.128.94`
- **Username:** `root`
- **Port:** 22

#### **Step 2: Verify File Uploaded**

```bash
ls -lh /tmp/tanishq_backup_*.sql
```

**Expected:**
```
-rw-r--r-- 1 root root 5.2M Dec  3 12:30 /tmp/tanishq_backup_20251203_123045.sql
```

✅ File is there with proper size

---

#### **Step 3: Check Current Database Status**

```bash
mysql -u root -pDechub#2025 selfie_preprod -e "SHOW TABLES; SELECT COUNT(*) FROM stores;"
```

**Current Status:** Empty tables ❌

---

#### **Step 4: Import Database (THE MAIN STEP!)**

**Important:** Your local database is named `tanishq` but server database is `selfie_preprod`. We need to handle this AND disable foreign key checks to avoid compatibility errors.

**RECOMMENDED METHOD (Handles Foreign Key Issues):**

```bash
# Set the backup file variable
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
echo "Importing from: $BACKUP_FILE"

# Import with database rename AND disable foreign key checks
(echo "SET FOREIGN_KEY_CHECKS=0;"; \
 sed 's/DATABASE `tanishq`/DATABASE `selfie_preprod`/g' $BACKUP_FILE | \
 sed 's/USE `tanishq`/USE `selfie_preprod`/g'; \
 echo "SET FOREIGN_KEY_CHECKS=1;") | \
mysql -u root -pDechub#2025

echo "Import completed!"
```

**Why disable foreign key checks?**
- Handles MySQL version differences (8.0 vs 8.4)
- Avoids column type compatibility errors
- Safe because your data is already valid from local database

**Option B: If above doesn't work, manual steps:**

```bash
# Find your file
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
echo "Using file: $BACKUP_FILE"

# Create a modified version
sed 's/DATABASE `tanishq`/DATABASE `selfie_preprod`/g' $BACKUP_FILE | \
sed 's/USE `tanishq`/USE `selfie_preprod`/g' > /tmp/selfie_preprod_import.sql

# Import the modified file
mysql -u root -pDechub#2025 selfie_preprod < /tmp/selfie_preprod_import.sql

echo "Import completed!"
```

**Option C: Direct import if database names match:**

```bash
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
mysql -u root -pDechub#2025 < $BACKUP_FILE

# Then rename database
mysql -u root -pDechub#2025 -e "
CREATE DATABASE IF NOT EXISTS selfie_preprod;
USE selfie_preprod;
"
# Copy tables from tanishq to selfie_preprod
mysqldump -u root -pDechub#2025 tanishq | mysql -u root -pDechub#2025 selfie_preprod
```

---

#### **Step 5: Verify Import Success** ✅

```bash
echo "========================================="
echo "Verifying Data Import"
echo "========================================="

# Check stores
echo "Stores:"
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) as total_stores FROM stores;"
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT storeCode, storeName FROM stores LIMIT 5;"

# Check users
echo ""
echo "Users:"
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) as total_users FROM users;"
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code, role FROM users LIMIT 5;"

# Check events
echo ""
echo "Events:"
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) as total_events FROM events;"
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT id, event_name, store_code FROM events LIMIT 5;"

# Check attendees
echo ""
echo "Attendees:"
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) as total_attendees FROM attendees;"

# Check all tables
echo ""
echo "All Tables:"
mysql -u root -pDechub#2025 selfie_preprod -e "
SELECT 
    table_name, 
    table_rows 
FROM information_schema.tables 
WHERE table_schema = 'selfie_preprod' 
ORDER BY table_name;
"
```

**Expected Output:**
```
Stores:
+--------------+
| total_stores |
+--------------+
|           45 |  (or whatever you have locally)
+--------------+

Users:
+-------------+
| total_users |
+-------------+
|          45 |
+-------------+

Events:
+--------------+
| total_events |
+--------------+
|           67 |
+--------------+
```

✅ **SUCCESS!** You should see actual numbers, not 0!

---

### **PART 4: Test Application with Real Data** 🧪

#### **Test 1: Get a Real User from Database**

```bash
# Get any user code
USER_CODE=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -s -N)
echo "Testing with user: $USER_CODE"

# Get user's password (if stored in plain text)
USER_PASS=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT password FROM users WHERE code='$USER_CODE' LIMIT 1;" -s -N)
echo "Password: $USER_PASS"
```

---

#### **Test 2: Test Login API with Real User**

```bash
# Using the user from Test 1
curl -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$USER_CODE\",\"password\":\"$USER_PASS\"}"
```

**Expected:**
```json
{"status":true,"storeData":{...},"message":"Login successful"}
```

✅ **If you see `"status":true` → LOGIN WORKS!**

---

#### **Test 3: Get Events for a Real Store**

```bash
# Get any store code
STORE_CODE=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT storeCode FROM stores LIMIT 1;" -s -N)
echo "Testing with store: $STORE_CODE"

# Get events
curl -X POST http://localhost:3002/events/getevents \
  -H "Content-Type: application/json" \
  -d "{\"storeCode\":\"$STORE_CODE\",\"startDate\":\"2025-01-01\",\"endDate\":\"2025-12-31\"}"
```

**Expected:** JSON array with events for that store ✅

---

#### **Test 4: Create New Event with Real Store**

```bash
STORE_CODE=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT storeCode FROM stores LIMIT 1;" -s -N)

mysql -u root -pDechub#2025 selfie_preprod -e "
INSERT INTO events (id, store_code, event_name, event_type, start_date, created_at, invitees, attendees, region)
VALUES ('TEST_IMPORT_001', '$STORE_CODE', 'Post-Import Test Event', 'Exhibition', '2025-12-20', NOW(), 0, 0, 'SOUTH');
"

# Verify
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT id, event_name, store_code FROM events WHERE id='TEST_IMPORT_001';"
```

**Expected:** Your new event shows up ✅ (No foreign key error!)

---

#### **Test 5: Upload Photo**

```bash
echo "Test image content" > /tmp/test_after_import.jpg

curl -X POST http://localhost:3002/events/uploadCompletedEvents \
  -F "eventId=TEST_IMPORT_001" \
  -F "files=@/tmp/test_after_import.jpg"
```

**Expected:**
```json
{"status":true,"message":"All 1 files uploaded successfully to S3","result":[...]}
```

---

#### **Test 6: QR Code for Real Event**

```bash
# Get any event ID
EVENT_ID=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT id FROM events LIMIT 1;" -s -N)
echo "Generating QR for event: $EVENT_ID"

curl http://localhost:3002/events/dowload-qr/$EVENT_ID
```

**Expected:** Base64 QR code data ✅

---

### **PART 5: Cleanup** 🧹

#### **Remove Backup File from Server**

```bash
# List backup files
ls -lh /tmp/tanishq_backup_*.sql
ls -lh /tmp/selfie_preprod_import.sql

# Remove them
rm /tmp/tanishq_backup_*.sql
rm /tmp/selfie_preprod_import.sql 2>/dev/null

echo "Cleanup complete!"
```

---

## ✅ SUCCESS CHECKLIST

Mark each item when complete:

- [ ] **Export completed** - SQL file created locally
- [ ] **Upload completed** - File visible in `/tmp/` on server
- [ ] **Import completed** - No errors during import
- [ ] **Stores imported** - COUNT(*) shows real numbers (not 0)
- [ ] **Users imported** - COUNT(*) shows real numbers (not 0)
- [ ] **Events imported** - COUNT(*) shows real numbers (not 0)
- [ ] **Login works** - API returns `{"status":true}`
- [ ] **Get events works** - API returns event data
- [ ] **Upload works** - Files go to S3
- [ ] **QR code works** - Returns Base64 data
- [ ] **No errors in logs** - Application runs clean

---

## 🚨 TROUBLESHOOTING

### **Issue: Database name mismatch during import**

**Error:** `Database 'tanishq' doesn't exist`

**Solution:**
```bash
# Use the sed command to rename database in SQL file
sed 's/DATABASE `tanishq`/DATABASE `selfie_preprod`/g' /tmp/tanishq_backup_*.sql | \
sed 's/USE `tanishq`/USE `selfie_preprod`/g' | \
mysql -u root -pDechub#2025
```

---

### **Issue: Foreign key constraints during import**

**Solution:**
```bash
# Disable foreign key checks during import
mysql -u root -pDechub#2025 selfie_preprod << 'EOF'
SET FOREIGN_KEY_CHECKS=0;
SOURCE /tmp/tanishq_backup_20251203_XXXXXX.sql;
SET FOREIGN_KEY_CHECKS=1;
EOF
```

---

### **Issue: Duplicate entries**

**If database already has some data:**

```bash
# Clear existing data first
mysql -u root -pDechub#2025 selfie_preprod << 'EOF'
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE attendees;
TRUNCATE TABLE events;
TRUNCATE TABLE users;
TRUNCATE TABLE stores;
-- Add other tables as needed
SET FOREIGN_KEY_CHECKS=1;
EOF

# Then import
```

---

## 📊 FINAL VERIFICATION COMMANDS

**Run this complete verification:**

```bash
#!/bin/bash
echo "========================================="
echo "COMPLETE DATA IMPORT VERIFICATION"
echo "========================================="
echo ""

echo "1. Database Statistics:"
mysql -u root -pDechub#2025 selfie_preprod << 'EOF'
SELECT 
    'stores' as table_name, COUNT(*) as row_count FROM stores
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'events', COUNT(*) FROM events
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL
SELECT 'greetings', COUNT(*) FROM greetings
UNION ALL
SELECT 'product_details', COUNT(*) FROM product_details
UNION ALL
SELECT 'rivaah', COUNT(*) FROM rivaah
UNION ALL
SELECT 'bride_details', COUNT(*) FROM bride_details;
EOF

