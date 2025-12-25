# ✅ YOUR ANSWER - IMPORT ALL LOCAL DATA TO PREPROD

**You asked:** "I have all the database data in my local, I want to import all to server"

**Answer:** YES! Here's exactly how to do it:

---

## 🎯 3 SIMPLE STEPS

### **STEP 1: Export (On Your Windows PC)**

Open Command Prompt:

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
export_database_for_preprod.bat
```

**Result:** File created in `database_backup\tanishq_backup_20251203_XXXXXX.sql` ✅

---

### **STEP 2: Upload (Use WinSCP)**

1. Open **WinSCP**
2. Connect to server: `10.160.128.94` (user: root)
3. Upload the `.sql` file from `database_backup\` folder to `/tmp/` on server

**Result:** File now on server at `/tmp/tanishq_backup_*.sql` ✅

---

### **STEP 3: Import (On Server via SSH)**

SSH to the server, then run:

```bash
# Import and rename database automatically
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
sed 's/DATABASE `tanishq`/DATABASE `selfie_preprod`/g' $BACKUP_FILE | \
sed 's/USE `tanishq`/USE `selfie_preprod`/g' | \
mysql -u root -pDechub#2025

echo "✅ Import complete!"
```

**Result:** All your local data is now on the server! ✅

---

## ✅ VERIFY IT WORKED

```bash
# Check data counts
mysql -u root -pDechub#2025 selfie_preprod -e "
SELECT 'Stores' as Table_Name, COUNT(*) as Row_Count FROM stores
UNION ALL SELECT 'Users', COUNT(*) FROM users
UNION ALL SELECT 'Events', COUNT(*) FROM events;
"
```

**Expected:**
```
+------------+-----------+
| Table_Name | Row_Count |
+------------+-----------+
| Stores     |        45 |  ← Your actual data count
| Users      |        45 |
| Events     |        67 |
+------------+-----------+
```

If you see your actual numbers (not 0), **SUCCESS!** 🎉

---

## 🧪 TEST THE APPLICATION

```bash
# Get a real user
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT code, password FROM users LIMIT 1;"

# Test login with that user's credentials
curl -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d '{"code":"BLR001","password":"YourActualPassword"}'
```

**Expected:** `{"status":true,"storeData":{...}}` ✅

**If you see this → Everything works! Your app is ready!** 🚀

---

## 📚 DETAILED GUIDES

- **Quick Start:** `IMPORT_QUICK_START.md` (1 page, copy-paste commands)
- **Complete Guide:** `COMPLETE_DATA_IMPORT_GUIDE.md` (detailed with troubleshooting)
- **Visual Guide:** `IMPORT_VISUAL_GUIDE.md` (diagrams and flow)

---

## ⏱️ HOW LONG WILL THIS TAKE?

- Export: **30 seconds**
- Upload: **1-2 minutes**
- Import: **2-3 minutes**
- Verify: **30 seconds**

**Total: 5-10 minutes** ⏱️

---

## 🔥 START NOW - COPY THESE COMMANDS

### **On Windows:**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
export_database_for_preprod.bat
```

Then upload via WinSCP to `/tmp/`

### **On Server:**
```bash
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
sed 's/DATABASE `tanishq`/DATABASE `selfie_preprod`/g' $BACKUP_FILE | sed 's/USE `tanishq`/USE `selfie_preprod`/g' | mysql -u root -pDechub#2025
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores; SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM events;"
```

---

## ✅ YOU'RE DONE WHEN:

- ✅ All tables show data (not empty)
- ✅ Login API returns `{"status":true}`
- ✅ Get events API returns your events
- ✅ Upload to S3 works
- ✅ QR code generation works

---

**That's it! Your local data will be on the server and working perfectly!** 🎉

**Open `IMPORT_QUICK_START.md` and follow the 3 steps!** 🚀

