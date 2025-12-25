# 📊 DATA IMPORT PROCESS - VISUAL GUIDE

```
┌─────────────────────────────────────────────────────────────────────┐
│                    DATA IMPORT FLOW DIAGRAM                         │
└─────────────────────────────────────────────────────────────────────┘

┌───────────────────┐
│  LOCAL WINDOWS PC │
│  (Your Machine)   │
└─────────┬─────────┘
          │
          │ STEP 1: Export Database
          │ Command: export_database_for_preprod.bat
          │
          ▼
┌─────────────────────────────────────────────────┐
│  File Created:                                  │
│  database_backup\tanishq_backup_20251203.sql   │
│  Size: ~5-10 MB (depends on your data)         │
└─────────┬───────────────────────────────────────┘
          │
          │ STEP 2: Upload File
          │ Tool: WinSCP (GUI) or SCP (command)
          │ Destination: 10.160.128.94:/tmp/
          │
          ▼
┌─────────────────────────────────────────────────┐
│  PREPROD SERVER                                 │
│  10.160.128.94:/tmp/tanishq_backup_*.sql       │
└─────────┬───────────────────────────────────────┘
          │
          │ STEP 3: Import to Database
          │ Command: mysql import with sed rename
          │
          ▼
┌─────────────────────────────────────────────────┐
│  MySQL Database: selfie_preprod                 │
│  ✅ All tables populated with data              │
└─────────┬───────────────────────────────────────┘
          │
          │ STEP 4: Verify & Test
          │
          ▼
┌─────────────────────────────────────────────────┐
│  ✅ Login API Works                             │
│  ✅ Events API Works                            │
│  ✅ Upload Works                                │
│  ✅ Application Fully Functional                │
└─────────────────────────────────────────────────┘
```

---

## 📋 DETAILED BREAKDOWN

### **🖥️ STEP 1: EXPORT (Local Windows)**

```
┌────────────────────────────────────────────────────────┐
│ Location: C:\JAVA\celebration-preprod-latest\...      │
│                                                        │
│ Command:                                               │
│   export_database_for_preprod.bat                     │
│                                                        │
│ What it does:                                          │
│   1. Connects to local MySQL (user: nagaraj_jadar)    │
│   2. Exports entire 'tanishq' database                 │
│   3. Creates timestamped backup file                   │
│   4. Saves to database_backup\ folder                  │
│                                                        │
│ Output:                                                │
│   ✅ tanishq_backup_20251203_143052.sql               │
│   📊 Contains ALL tables:                             │
│      - stores (structure + data)                       │
│      - users (structure + data)                        │
│      - events (structure + data)                       │
│      - attendees (structure + data)                    │
│      - invitees, greetings, etc.                       │
│                                                        │
│ Time: 30-60 seconds                                    │
└────────────────────────────────────────────────────────┘
```

---

### **📤 STEP 2: UPLOAD (Transfer to Server)**

```
┌────────────────────────────────────────────────────────┐
│ OPTION A: WinSCP (GUI - RECOMMENDED)                   │
│                                                        │
│ 1. Open WinSCP                                         │
│ 2. New Session:                                        │
│    ├─ Protocol: SFTP                                   │
│    ├─ Host: 10.160.128.94                             │
│    ├─ Port: 22                                         │
│    ├─ User: root                                       │
│    └─ Password: [your SSH password]                    │
│                                                        │
│ 3. Navigate:                                           │
│    ├─ Local (left):  database_backup\                  │
│    └─ Remote (right): /tmp/                            │
│                                                        │
│ 4. Drag & Drop:                                        │
│    tanishq_backup_*.sql → /tmp/                        │
│                                                        │
│ 5. Wait for upload (30-120 seconds)                    │
│                                                        │
│ Time: 1-3 minutes                                      │
└────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────┐
│ OPTION B: SCP Command (Advanced)                       │
│                                                        │
│ scp database_backup\tanishq_backup_*.sql \             │
│     root@10.160.128.94:/tmp/                          │
│                                                        │
│ Enter password when prompted                           │
│                                                        │
│ Time: 1-2 minutes                                      │
└────────────────────────────────────────────────────────┘
```

---

### **🚀 STEP 3: IMPORT (On Server)**

```
┌────────────────────────────────────────────────────────┐
│ SSH to: 10.160.128.94                                  │
│                                                        │
│ Command Breakdown:                                     │
│                                                        │
│ # Find the uploaded file                               │
│ BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
│                                                        │
│ # Rename database on-the-fly and import                │
│ sed 's/DATABASE `tanishq`/DATABASE `selfie_preprod`/g' $BACKUP_FILE | \
│ sed 's/USE `tanishq`/USE `selfie_preprod`/g' | \       │
│ mysql -u root -pDechub#2025                            │
│                                                        │
│ What happens:                                          │
│   1. sed reads the SQL file                            │
│   2. Replaces 'tanishq' with 'selfie_preprod'          │
│   3. Pipes to mysql                                    │
│   4. MySQL executes all INSERT statements              │
│   5. Database gets populated                           │
│                                                        │
│ During import:                                         │
│   - Creates tables (if not exist)                      │
│   - Inserts all rows                                   │
│   - Preserves relationships                            │
│   - Maintains data integrity                           │
│                                                        │
│ Time: 1-5 minutes (depends on data size)               │
└────────────────────────────────────────────────────────┘
```

---

### **✅ STEP 4: VERIFY (On Server)**

```
┌────────────────────────────────────────────────────────┐
│ Quick Verification Commands                            │
│                                                        │
│ # Check row counts                                     │
│ mysql -u root -pDechub#2025 selfie_preprod -e "       │
│   SELECT COUNT(*) FROM stores;                         │
│   SELECT COUNT(*) FROM users;                          │
│   SELECT COUNT(*) FROM events;                         │
│ "                                                      │
│                                                        │
│ Expected: Non-zero counts ✅                           │
│                                                        │
│ # View sample data                                     │
│ mysql -u root -pDechub#2025 selfie_preprod -e "       │
│   SELECT storeCode, storeName FROM stores LIMIT 5;     │
│ "                                                      │
│                                                        │
│ # Test API                                             │
│ curl -X POST http://localhost:3002/events/login \      │
│   -H "Content-Type: application/json" \                │
│   -d '{"code":"[real_code]","password":"[real_pass]"}' │
│                                                        │
│ Expected: {"status":true} ✅                           │
│                                                        │
│ Time: 1 minute                                         │
└────────────────────────────────────────────────────────┘
```

---

## 🎯 SUCCESS INDICATORS

```
✅ BEFORE Import:
   mysql> SELECT COUNT(*) FROM stores;
   +----------+
   | COUNT(*) |
   +----------+
   |        0 |  ← EMPTY
   +----------+

✅ AFTER Import:
   mysql> SELECT COUNT(*) FROM stores;
   +----------+
   | COUNT(*) |
   +----------+
   |       45 |  ← DATA IMPORTED!
   +----------+

✅ API Test:
   $ curl http://localhost:3002/events/login ...
   {"status":true,"storeData":{...}}  ← WORKING!

✅ Application Ready:
   - All tables populated
   - APIs functional
   - Ready for testing/production
```

---

## ⏱️ TIME ESTIMATE

```
┌──────────────────────┬──────────────┐
│ Step                 │ Time         │
├──────────────────────┼──────────────┤
│ Export (Windows)     │ 30-60 sec    │
│ Upload (WinSCP)      │ 1-3 min      │
│ Import (Server)      │ 1-5 min      │
│ Verify (Server)      │ 1 min        │
├──────────────────────┼──────────────┤
│ TOTAL                │ 5-10 min     │
└──────────────────────┴──────────────┘
```

---

## 🔄 DATA FLOW

```
Local MySQL (tanishq)
         ↓
    mysqldump
         ↓
   .sql file (5-10 MB)
         ↓
   WinSCP upload
         ↓
Server /tmp/ folder
         ↓
  sed (rename DB)
         ↓
   mysql import
         ↓
Server MySQL (selfie_preprod)
         ↓
   Application uses data
         ↓
    🎉 SUCCESS!
```

---

## 📁 FILE LOCATIONS

```
LOCAL WINDOWS:
  C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\
  └── database_backup\
      └── tanishq_backup_20251203_143052.sql  ← EXPORT HERE

SERVER:
  /tmp/
  └── tanishq_backup_20251203_143052.sql  ← UPLOAD HERE
  
  /var/lib/mysql/
  └── selfie_preprod/  ← IMPORT TO HERE
      ├── stores.ibd
      ├── users.ibd
      ├── events.ibd
      └── ... (all tables)
```

---

## 🛡️ SAFETY NOTES

1. **Backup existing data** (if any):
   ```bash
   mysqldump -u root -pDechub#2025 selfie_preprod > /tmp/backup_before_import.sql
   ```

2. **Test on sample first** (optional):
   ```bash
   # Import to test database first
   mysql -u root -pDechub#2025 -e "CREATE DATABASE test_import;"
   # Import to test_import instead of selfie_preprod
   ```

3. **Check for errors during import**:
   ```bash
   # Redirect to file to see errors
   sed 's/...' backup.sql | mysql ... 2> /tmp/import_errors.log
   cat /tmp/import_errors.log
   ```

---

## 🔧 ALTERNATIVE: Manual Table-by-Table

If full import fails, you can import specific tables:

```bash
# Export single table from local
mysqldump -u nagaraj_jadar -pNagaraj07 tanishq stores > stores.sql

# Upload stores.sql to server

# Import single table
mysql -u root -pDechub#2025 selfie_preprod < stores.sql
```

Repeat for: users, events, attendees, etc.

---

**See `IMPORT_QUICK_START.md` for copy-paste commands!** 🚀

