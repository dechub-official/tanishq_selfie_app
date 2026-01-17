# CSV IMPORT - QUICK START GUIDE

## 📋 YOU HAVE CSV FILES - HERE'S WHAT TO DO

### What You Need:
✅ **events.csv** - Events data  
✅ **attendees.csv** - Attendees data  
✅ **invitees.csv** - Invitees data

---

## 🚀 FASTEST METHOD - 3 STEPS

### **STEP 1: Upload CSV Files** (From Windows)

**Option A: Use the upload script**
```powershell
# Run this batch file
.\upload_csv_files.bat

# It will ask for CSV file locations, then upload them
```

**Option B: Manual upload**
```powershell
# From PowerShell on your Windows machine
scp C:\path\to\events.csv root@10.10.63.97:/opt/tanishq/csv_import/
scp C:\path\to\attendees.csv root@10.10.63.97:/opt/tanishq/csv_import/
scp C:\path\to\invitees.csv root@10.10.63.97:/opt/tanishq/csv_import/
```

---

### **STEP 2: Run Import Script** (On Production Server)

**Option A: Use automated script**
```bash
# SSH into production server
ssh root@10.10.63.97

# Run the import script
cd /opt/tanishq
chmod +x import_csv_to_mysql.sh
./import_csv_to_mysql.sh
```

**Option B: Manual import (single command)**
```bash
# SSH into production server
ssh root@10.10.63.97

# Run this complete command block
cd /opt/tanishq
mkdir -p csv_import backups
mysqldump -u root -pNagaraj@07 selfie_prod > backups/backup_$(date +%Y%m%d_%H%M%S).sql
kill $(cat tanishq-prod.pid) 2>/dev/null

mysql --local-infile=1 -u root -pNagaraj@07 selfie_prod << 'EOF'
SET FOREIGN_KEY_CHECKS=0;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv' INTO TABLE events FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/attendees.csv' INTO TABLE attendees FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/invitees.csv' INTO TABLE invitees FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
SET FOREIGN_KEY_CHECKS=1;
EOF

mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT 'events', COUNT(*) FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees UNION ALL SELECT 'invitees', COUNT(*) FROM invitees;"

nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid
```

---

### **STEP 3: Verify**

```bash
# Check data was imported
mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT 'events', COUNT(*) FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees UNION ALL SELECT 'invitees', COUNT(*) FROM invitees;"

# Check application is running
ps -p $(cat /opt/tanishq/tanishq-prod.pid)

# Test website
curl -I http://localhost:3000/
```

---

## 📝 CSV FILE FORMAT

### Required Format:

**First row = Column headers** (must match database columns exactly)

**events.csv example:**
```csv
id,created_at,region,event_type,event_sub_type,event_name,rso,start_date,image,invitees,attendees,store_code
STORE001_abc123,2025-11-15 10:30:00,North,Wedding,Bridal,Winter Show,John,2025-11-20,img.jpg,50,45,STORE001
```

**attendees.csv example:**
```csv
id,name,email,phone,event_id,selfie_image,created_at
1,Priya Sharma,priya@example.com,9876543210,STORE001_abc123,selfie1.jpg,2025-11-20 14:30:00
```

**invitees.csv example:**
```csv
id,name,email,phone,event_id,invited_date,status
1,Rahul Kumar,rahul@example.com,9876543211,STORE001_abc123,2025-11-15,invited
```

### Important Rules:
- ✅ First row must be column headers
- ✅ Comma separated (`,`)
- ✅ Text with commas should be quoted: `"text, with, commas"`
- ✅ Dates: `YYYY-MM-DD` or `YYYY-MM-DD HH:MM:SS`
- ✅ Boolean: `1` (true) or `0` (false)
- ✅ NULL: leave empty or use `\N`
- ✅ UTF-8 encoding

---

## 🔧 TROUBLESHOOTING

### Problem: Can't get stat of file

```bash
# Enable local-infile in MySQL
vi /etc/my.cnf

# Add these lines:
[mysqld]
local-infile=1

[mysql]
local-infile=1

# Restart MySQL
systemctl restart mysqld
```

### Problem: Duplicate entry error

```bash
# Use IGNORE to skip duplicates
mysql --local-infile=1 -u root -pNagaraj@07 selfie_prod << 'EOF'
SET FOREIGN_KEY_CHECKS=0;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv' IGNORE INTO TABLE events FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
# ... same for other files
SET FOREIGN_KEY_CHECKS=1;
EOF
```

### Problem: Foreign key constraint fails

**Solution:** Import events FIRST, then attendees and invitees
The command blocks above already do this in correct order.

### Problem: Wrong date format

**Fix your CSV dates to:**
- `2025-11-15` (date only)
- `2025-11-15 10:30:00` (date and time)

### Problem: Column mismatch

**Solution:** Ensure CSV headers exactly match database columns

```bash
# Check table structure
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.events;"
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.attendees;"
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.invitees;"
```

---

## ✅ VERIFICATION COMMANDS

```bash
# Count rows in database
mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT 'events', COUNT(*) FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees UNION ALL SELECT 'invitees', COUNT(*) FROM invitees;"

# View latest imported records
mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT * FROM events ORDER BY created_at DESC LIMIT 5;"

# Check application status
ps -p $(cat /opt/tanishq/tanishq-prod.pid)
ss -tlnp | grep 3000
curl -I http://localhost:3000/

# View application logs
tail -50 /opt/tanishq/logs/application.log

# Search for errors
grep -i "error" /opt/tanishq/logs/application.log | tail -20
```

---

## 🔄 ROLLBACK (If something goes wrong)

```bash
# Stop application
kill $(cat /opt/tanishq/tanishq-prod.pid)

# Restore from backup
mysql -u root -pNagaraj@07 selfie_prod < /opt/tanishq/backups/backup_TIMESTAMP.sql

# Restart application
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid
```

---

## 📊 WHAT HAPPENS DURING IMPORT

1. ✅ **Backup created** - Your current database is backed up
2. ✅ **Application stopped** - Prevents conflicts during import
3. ✅ **CSV data imported** - Events first, then attendees/invitees
4. ✅ **Data verified** - Counts checked before/after
5. ✅ **Application restarted** - Running on port 3000
6. ✅ **Website tested** - HTTP 200 response checked

---

## 💡 TIPS

1. **Test with 5-10 rows first** - Create small CSV files to test
2. **Check encoding** - Use UTF-8, not UTF-8 with BOM
3. **Remove empty rows** - No blank lines in CSV
4. **Quote special characters** - Text with commas should be quoted
5. **Match column order** - Not required, but makes it easier
6. **Backup always** - Script does this automatically
7. **Import order matters** - Events BEFORE attendees/invitees

---

## 📁 FILE LOCATIONS

- **CSV files:** `/opt/tanishq/csv_import/`
- **Backups:** `/opt/tanishq/backups/`
- **Logs:** `/opt/tanishq/logs/application.log`
- **Scripts:** `/opt/tanishq/import_csv_to_mysql.sh`

---

## 🎯 COMPLETE WORKFLOW

```
YOUR PC (Windows)           PRODUCTION SERVER
─────────────────          ──────────────────

CSV files                  
  ↓
upload_csv_files.bat  →    /opt/tanishq/csv_import/
                             ↓
                           import_csv_to_mysql.sh
                             ↓
                           MySQL Database
                             ↓
                           Application Restart
                             ↓
                           Website Live ✓
```

---

## 📞 DOCUMENTATION

- **Full Guide:** IMPORT_CSV_TO_MYSQL.md
- **Quick Reference:** This file
- **Upload Script:** upload_csv_files.bat (Windows)
- **Import Script:** import_csv_to_mysql.sh (Linux)

---

## ✅ FINAL CHECKLIST

- [ ] CSV files prepared with correct format
- [ ] First row is column headers
- [ ] Date format is YYYY-MM-DD
- [ ] Files uploaded to server
- [ ] Backup created
- [ ] Import completed without errors
- [ ] Record counts verified
- [ ] Application restarted
- [ ] Website accessible
- [ ] Data visible in application

---

**Ready? Start with STEP 1 - Upload your CSV files!** 🚀

