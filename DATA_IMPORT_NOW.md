# 🚨 URGENT: IMPORT DATA TO PREPROD

**Current Situation:** Your preprod database is EMPTY! ❌
- ❌ No stores
- ❌ No users  
- ❌ No events

**Solution:** Export from local → Upload to server → Import to preprod

---

## 📋 STEP-BY-STEP DATA MIGRATION

### **🖥️ PART 1: Export from LOCAL Windows Machine**

**Run on your Windows machine (where local database is):**

#### **Option A: Use Existing Export Script (EASIEST)**

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
export_database_for_preprod.bat
```

**Expected Output:**
```
Database Export for Pre-Production Migration
Database: tanishq
User: nagaraj_jadar
Backup File: tanishq_backup_20251203_HHMMSS.sql
✅ Export completed successfully!
```

**Backup file location:**
```
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\database_backup\tanishq_backup_20251203_HHMMSS.sql
```

---

#### **Option B: Manual Export (If script fails)**

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" ^
  -u nagaraj_jadar -pNagaraj07 ^
  --databases tanishq ^
  --single-transaction ^
  --routines ^
  --triggers ^
  --events ^
  --result-file=tanishq_backup_manual.sql

echo Export complete! File: tanishq_backup_manual.sql
```

---

### **📤 PART 2: Upload SQL File to Server**

**You have 3 options:**

#### **Option A: WinSCP (GUI - EASIEST)**

1. Open WinSCP
2. Connect to: `10.160.128.94`
3. Username: `root`
4. Upload your `.sql` file to: `/tmp/`

#### **Option B: SCP Command (Windows)**

```cmd
scp C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\database_backup\tanishq_backup_*.sql root@10.160.128.94:/tmp/
```

#### **Option C: Copy-Paste Small Tables (If file is small)**

If your database is small, you can export individual tables and paste SQL directly on server.

---

### **🚀 PART 3: Import on SERVER**

**Run these commands on the preprod server (SSH):**

#### **Step 1: Verify Upload**

```bash
ls -lh /tmp/tanishq_backup_*.sql
```

**Expected:** File should be listed with size

---

#### **Step 2: Check Current Database**

```bash
mysql -u root -pDechub#2025 selfie_preprod -e "SHOW TABLES;"
```

**Expected:** Empty tables (as you saw)

---

#### **Step 3: Import Database**

**Import the full backup:**

```bash
# Find your uploaded file
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
echo "Importing: $BACKUP_FILE"

# Import (this will take a few seconds)
mysql -u root -pDechub#2025 selfie_preprod < $BACKUP_FILE

echo "Import complete!"
```

**OR if the file creates the database (has 'CREATE DATABASE tanishq'):**

```bash
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)

# Import and rename database from 'tanishq' to 'selfie_preprod'
sed 's/CREATE DATABASE.*tanishq/CREATE DATABASE IF NOT EXISTS selfie_preprod/g' $BACKUP_FILE | \
sed 's/USE.*tanishq/USE selfie_preprod/g' | \
mysql -u root -pDechub#2025
```

---

#### **Step 4: Verify Import**

```bash
# Check stores
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) as store_count FROM stores;"

# Check users
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) as user_count FROM users;"

# Check events
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) as event_count FROM events;"

# View sample data
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT store_code, store_name FROM stores LIMIT 5;"
```

**Expected:** You should see counts > 0 and actual data! ✅

---

## 🎯 QUICK REFERENCE - What Data You Need

Based on your local database structure, you should have:

| Table | Expected Records |
|-------|-----------------|
| **stores** | Multiple stores (BLR001, MUM001, etc.) |
| **users** | Store manager accounts |
| **events** | Past/upcoming events |
| **attendees** | Event attendees |
| **product_details** | Product catalog |
| **rivaah** | Wedding collection data |
| **greetings** | Greeting cards |

---

## 🔥 AFTER IMPORT IS COMPLETE

Once you see data in the tables, **NOW you can test:**

### **Test 1: Check Stores**

```bash
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT store_code, store_name FROM stores LIMIT 10;"
```

### **Test 2: Check Users**

```bash
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code, role FROM users LIMIT 10;"
```

### **Test 3: Try Login with Real User**

```bash
# Get a real user from database
USER_CODE=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code FROM users LIMIT 1;" -N)
echo "Testing login with user: $USER_CODE"

# Test login (assuming password is 'Password123' or similar)
curl -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$USER_CODE\",\"password\":\"Password123\"}"
```

### **Test 4: Create Event with Real Store**

```bash
# Get a real store
STORE_CODE=$(mysql -u root -pDechub#2025 selfie_preprod -e "SELECT store_code FROM stores LIMIT 1;" -N)

# Create test event
mysql -u root -pDechub#2025 selfie_preprod -e "
INSERT INTO events (id, store_code, event_name, event_type, start_date, created_at, invitees, attendees, region)
VALUES ('TEST_001', '$STORE_CODE', 'Test Event', 'Exhibition', '2025-12-20', NOW(), 0, 0, 'SOUTH');
"

# Verify
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT id, store_code, event_name FROM events WHERE id='TEST_001';"
```

---

## 🚨 IF YOU DON'T HAVE LOCAL DATA

If you don't have production data to import, you can create sample data:

### **Create Sample Stores**

```bash
mysql -u root -pDechub#2025 selfie_preprod << 'EOF'
INSERT INTO stores (store_code, store_name, store_region, created_at) VALUES
('BLR001', 'Bangalore MG Road', 'SOUTH', NOW()),
('MUM001', 'Mumbai Fort', 'WEST', NOW()),
('DEL001', 'Delhi Connaught Place', 'NORTH', NOW()),
('CHE001', 'Chennai T Nagar', 'SOUTH', NOW()),
('HYD001', 'Hyderabad Banjara Hills', 'SOUTH', NOW());
EOF
```

### **Create Sample Users**

```bash
mysql -u root -pDechub#2025 selfie_preprod << 'EOF'
INSERT INTO users (code, password, role, created_at) VALUES
('BLR001', 'Password123', 'STORE_MANAGER', NOW()),
('MUM001', 'Password123', 'STORE_MANAGER', NOW()),
('DEL001', 'Password123', 'STORE_MANAGER', NOW()),
('CHE001', 'Password123', 'STORE_MANAGER', NOW()),
('HYD001', 'Password123', 'STORE_MANAGER', NOW());
EOF
```

### **Create Sample Events**

```bash
mysql -u root -pDechub#2025 selfie_preprod << 'EOF'
INSERT INTO events (id, store_code, event_name, event_type, start_date, created_at, invitees, attendees, region) VALUES
('EVT001', 'BLR001', 'Diwali Gold Festival', 'Festival', '2025-11-10', NOW(), 50, 35, 'SOUTH'),
('EVT002', 'MUM001', 'Wedding Collection Launch', 'Launch', '2025-11-15', NOW(), 30, 28, 'WEST'),
('EVT003', 'CHE001', 'Diamond Exhibition', 'Exhibition', '2025-12-05', NOW(), 40, 0, 'SOUTH');
EOF
```

---

## ✅ SUCCESS CHECKLIST

After import, verify:

- [ ] `stores` table has data (not empty)
- [ ] `users` table has data (not empty)
- [ ] Login API works with real user
- [ ] Can create events with real store codes
- [ ] Can query events via API
- [ ] Application logs show no errors

---

## 🎯 WHAT TO DO RIGHT NOW

### **ON YOUR WINDOWS MACHINE:**

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
export_database_for_preprod.bat
```

### **THEN UPLOAD FILE:**

Use WinSCP or SCP to upload the `.sql` file to `/tmp/` on server

### **THEN ON SERVER:**

```bash
# Import
mysql -u root -pDechub#2025 selfie_preprod < /tmp/tanishq_backup_*.sql

# Verify
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores; SELECT COUNT(*) FROM users;"
```

---

## 📞 NEXT STEPS AFTER DATA IS IMPORTED

Once you have data:
1. ✅ Go back to **SIMPLE_TEST_NOW.md**
2. ✅ Run the tests with REAL store codes
3. ✅ Everything should work now!

---

**Start with exporting your local database NOW!** 🚀

