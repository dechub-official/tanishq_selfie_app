# CSV TO MYSQL IMPORT GUIDE

## Your Situation
✅ You have CSV files for: events, attendees, invitees
✅ CSV structure matches database table structure
✅ Need to import without messing up existing data

---

## 📁 PREPARE YOUR CSV FILES

### Required CSV Files:
1. **events.csv** - Event records
2. **attendees.csv** - Attendee records  
3. **invitees.csv** - Invitee records

### CSV Format Requirements:
- First row should be column headers (matching database column names)
- Use comma (`,`) as delimiter
- Text fields should be quoted if they contain commas
- Date format: `YYYY-MM-DD` or `YYYY-MM-DD HH:MM:SS`
- NULL values: leave empty or use `\N`

### Example CSV Structure:

**events.csv:**
```csv
id,created_at,region,event_type,event_sub_type,event_name,rso,start_date,image,invitees,attendees,completed_events_drive_link,community,location,attendees_uploaded,sale,advance,ghs_or_rga,gmb,diamond_awareness,ghs_flag,store_code
STORE001_uuid123,2025-11-15 10:30:00,North,Wedding,Bridal Show,Winter Collection,John Doe,2025-11-20,/images/event1.jpg,50,45,https://drive.google.com/...,Hindu,Mumbai,1,150000.00,50000.00,25000.00,10000.00,1,0,STORE001
```

**attendees.csv:**
```csv
id,name,email,phone,event_id,selfie_image,created_at
1,Priya Sharma,priya@example.com,9876543210,STORE001_uuid123,/storage/selfie1.jpg,2025-11-20 14:30:00
```

**invitees.csv:**
```csv
id,name,email,phone,event_id,invited_date,status
1,Rahul Kumar,rahul@example.com,9876543211,STORE001_uuid123,2025-11-15,invited
```

---

## 🚀 STEP-BY-STEP IMPORT PROCESS

### STEP 1: Upload CSV Files to Production Server

**On Your Windows Machine (PowerShell):**

```powershell
# Upload CSV files to production server
scp C:\path\to\events.csv root@10.10.63.97:/opt/tanishq/csv_import/
scp C:\path\to\attendees.csv root@10.10.63.97:/opt/tanishq/csv_import/
scp C:\path\to\invitees.csv root@10.10.63.97:/opt/tanishq/csv_import/
```

**Replace:** `C:\path\to\` with your actual CSV file location

---

### STEP 2: Prepare Import on Production Server

**SSH into Production Server:**

```bash
ssh root@10.10.63.97
```

**Create import directory and set permissions:**

```bash
# Create directory for CSV files
mkdir -p /opt/tanishq/csv_import

# Create backup directory
mkdir -p /opt/tanishq/backups

# Set permissions
chmod 755 /opt/tanishq/csv_import
```

---

### STEP 3: Verify CSV Files

```bash
# Check files are uploaded
ls -lh /opt/tanishq/csv_import/

# Preview first few lines of each file
head -5 /opt/tanishq/csv_import/events.csv
head -5 /opt/tanishq/csv_import/attendees.csv
head -5 /opt/tanishq/csv_import/invitees.csv

# Count rows (subtract 1 for header row)
wc -l /opt/tanishq/csv_import/*.csv
```

---

### STEP 4: Backup Current Database

**IMPORTANT: Always backup before importing!**

```bash
# Create backup with timestamp
mysqldump -u root -p selfie_prod > /opt/tanishq/backups/backup_before_csv_import_$(date +%Y%m%d_%H%M%S).sql
```

**Password:** `Nagaraj@07`

---

### STEP 5: Stop Application (Optional but Recommended)

```bash
# Stop application to prevent conflicts during import
kill $(cat /opt/tanishq/tanishq-prod.pid)
```

---

### STEP 6: Import CSV Files to MySQL

#### Method 1: Using LOAD DATA INFILE (Fastest)

```bash
# Import events
mysql -u root -p selfie_prod -e "
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv'
INTO TABLE events
FIELDS TERMINATED BY ','
ENCLOSED BY '\"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;
"

# Import attendees
mysql -u root -p selfie_prod -e "
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/attendees.csv'
INTO TABLE attendees
FIELDS TERMINATED BY ','
ENCLOSED BY '\"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;
"

# Import invitees
mysql -u root -p selfie_prod -e "
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/invitees.csv'
INTO TABLE invitees
FIELDS TERMINATED BY ','
ENCLOSED BY '\"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;
"
```

**Password:** `Nagaraj@07`

#### Method 2: Using mysqlimport (Alternative)

```bash
# Rename CSV files to match table names (if needed)
cp /opt/tanishq/csv_import/events.csv /opt/tanishq/csv_import/events.txt
cp /opt/tanishq/csv_import/attendees.csv /opt/tanishq/csv_import/attendees.txt
cp /opt/tanishq/csv_import/invitees.csv /opt/tanishq/csv_import/invitees.txt

# Import using mysqlimport
mysqlimport --local \
  --fields-terminated-by=',' \
  --fields-enclosed-by='"' \
  --lines-terminated-by='\n' \
  --ignore-lines=1 \
  -u root -p selfie_prod \
  /opt/tanishq/csv_import/events.txt \
  /opt/tanishq/csv_import/attendees.txt \
  /opt/tanishq/csv_import/invitees.txt
```

---

### STEP 7: Verify Data Import

```bash
# Count records in each table
mysql -u root -p -e "
USE selfie_prod;
SELECT 'events' as table_name, COUNT(*) as count FROM events
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees;
"
```

**Password:** `Nagaraj@07`

**Expected:** Count should increase by the number of rows in your CSV files

```bash
# Check latest records
mysql -u root -p -e "
USE selfie_prod;
SELECT * FROM events ORDER BY created_at DESC LIMIT 5;
"

mysql -u root -p -e "
USE selfie_prod;
SELECT * FROM attendees ORDER BY id DESC LIMIT 5;
"
```

---

### STEP 8: Restart Application

```bash
# Clear logs
> /opt/tanishq/logs/application.log

# Start application
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > /opt/tanishq/logs/application.log 2>&1 &

echo $! > /opt/tanishq/tanishq-prod.pid

# Wait for startup
sleep 30

# Check application is running
ps -p $(cat /opt/tanishq/tanishq-prod.pid)

# Check logs
tail -50 /opt/tanishq/logs/application.log
```

---

### STEP 9: Test Application

```bash
# Test HTTP response
curl -I http://localhost:3000/

# Check if port is listening
ss -tlnp | grep 3000
```

**Expected:** HTTP/1.1 200

**Test in Browser:**
- Open your production URL
- Login
- Verify you can see the imported data

---

## 🔧 TROUBLESHOOTING

### Error: "Can't get stat of file"

**Solution:** Enable local-infile option

```bash
# Edit MySQL config
vi /etc/my.cnf

# Add under [mysqld] section:
[mysqld]
local-infile=1

# Add under [mysql] section:
[mysql]
local-infile=1

# Restart MySQL
systemctl restart mysqld

# Then retry import with --local-infile option
mysql --local-infile=1 -u root -p selfie_prod
```

---

### Error: "Duplicate entry for key PRIMARY"

**Cause:** IDs in CSV already exist in database

**Solution 1: Skip duplicates**
```bash
mysql -u root -p selfie_prod -e "
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv'
IGNORE INTO TABLE events
FIELDS TERMINATED BY ','
ENCLOSED BY '\"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;
"
```

**Solution 2: Update on duplicate (replace existing)**
```bash
mysql -u root -p selfie_prod -e "
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv'
REPLACE INTO TABLE events
FIELDS TERMINATED BY ','
ENCLOSED BY '\"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;
"
```

---

### Error: "Foreign key constraint fails"

**Cause:** Attendees/Invitees reference event_id that doesn't exist yet

**Solution:** Import in correct order and disable foreign key checks

```bash
mysql -u root -p selfie_prod << EOF
SET FOREIGN_KEY_CHECKS=0;

LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv'
INTO TABLE events
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/attendees.csv'
INTO TABLE attendees
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/invitees.csv'
INTO TABLE invitees
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

SET FOREIGN_KEY_CHECKS=1;
EOF
```

---

### Error: "Data truncated" or "Incorrect datetime value"

**Cause:** Date format mismatch

**Solution:** Check and fix date format in CSV

```bash
# Dates should be: YYYY-MM-DD HH:MM:SS
# Example: 2025-11-15 10:30:00

# Or just date: YYYY-MM-DD
# Example: 2025-11-15
```

---

### CSV has Different Column Order

**Solution:** Specify column mapping

```bash
mysql -u root -p selfie_prod -e "
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv'
INTO TABLE events
FIELDS TERMINATED BY ','
ENCLOSED BY '\"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(id, event_name, start_date, store_code, region, ...);
"
```

---

## 📊 IMPORT SCRIPT (All-in-One)

See the file: `import_csv_to_mysql.sh` created separately.

---

## ✅ VERIFICATION CHECKLIST

After import, verify:

- [ ] CSV files uploaded to server
- [ ] Backup created before import
- [ ] Import completed without errors
- [ ] Record counts increased correctly
- [ ] Sample data looks correct (check latest records)
- [ ] No duplicate entries
- [ ] Foreign keys are valid (attendees/invitees linked to events)
- [ ] Application restarted successfully
- [ ] Website accessible
- [ ] Data visible in application

---

## 🎯 QUICK IMPORT COMMANDS

```bash
# Complete import sequence (copy-paste this entire block)
cd /opt/tanishq
mkdir -p csv_import backups
chmod 755 csv_import
mysqldump -u root -pNagaraj@07 selfie_prod > backups/backup_$(date +%Y%m%d_%H%M%S).sql
kill $(cat tanishq-prod.pid) 2>/dev/null

mysql --local-infile=1 -u root -pNagaraj@07 selfie_prod << EOF
SET FOREIGN_KEY_CHECKS=0;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv' INTO TABLE events FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/attendees.csv' INTO TABLE attendees FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/invitees.csv' INTO TABLE invitees FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
SET FOREIGN_KEY_CHECKS=1;
EOF

mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT 'events', COUNT(*) FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees UNION ALL SELECT 'invitees', COUNT(*) FROM invitees;"

nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid

sleep 10
tail -50 logs/application.log
```

---

## 💡 TIPS

1. **Test with small CSV first:** Import 10-20 rows first to verify format
2. **Check CSV encoding:** Should be UTF-8
3. **Remove BOM:** Some Excel exports add BOM character - remove it
4. **Line endings:** Unix (LF) is better than Windows (CRLF)
5. **Column headers:** Must exactly match database column names (case-sensitive)
6. **NULL values:** Use `\N` or leave empty between commas: `,,`
7. **Boolean values:** Use 1 for true, 0 for false
8. **Order matters:** Import events first, then attendees/invitees

---

## 🔄 ROLLBACK (If Something Goes Wrong)

```bash
# Stop application
kill $(cat /opt/tanishq/tanishq-prod.pid)

# Restore from backup
mysql -u root -p selfie_prod < /opt/tanishq/backups/backup_YYYYMMDD_HHMMSS.sql

# Restart application
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > /opt/tanishq/logs/application.log 2>&1 & echo $! > /opt/tanishq/tanishq-prod.pid
```

---

**You're ready to import CSV files! Start with STEP 1.** 🚀

