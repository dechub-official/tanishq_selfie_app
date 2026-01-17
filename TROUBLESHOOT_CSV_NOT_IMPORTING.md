# 🔍 TROUBLESHOOTING: CSV Not Importing

## Issue
CSV import completes without error, but data counts don't increase. You have thousands of rows but only seeing small counts.

---

## DIAGNOSIS STEPS

### STEP 1: Check if CSV Files Exist

```bash
cd /opt/tanishq/csv_import
ls -lh *.csv
```

**Expected:** You should see your 3 CSV files with file sizes

---

### STEP 2: Check CSV File Content

```bash
# Check how many rows in each CSV
wc -l /opt/tanishq/csv_import/events.csv
wc -l /opt/tanishq/csv_import/attendees.csv
wc -l /opt/tanishq/csv_import/invitees.csv

# Preview first 5 rows of each file
echo "=== EVENTS.CSV ==="
head -5 /opt/tanishq/csv_import/events.csv

echo "=== ATTENDEES.CSV ==="
head -5 /opt/tanishq/csv_import/attendees.csv

echo "=== INVITEES.CSV ==="
head -5 /opt/tanishq/csv_import/invitees.csv
```

---

### STEP 3: Check for Import Errors

The import might have failed silently. Let's check MySQL error log:

```bash
# Check MySQL error log
tail -50 /var/log/mysqld.log
```

---

## COMMON ISSUES & FIXES

### Issue 1: CSV Files Not Uploaded

**Check:**
```bash
ls -lh /opt/tanishq/csv_import/
```

**If files are missing:**
- CSV files were never uploaded to the server
- Need to upload them first

**Fix:** Upload CSV files from your Windows PC:
```powershell
scp C:\path\to\events.csv root@10.10.63.97:/opt/tanishq/csv_import/
scp C:\path\to\attendees.csv root@10.10.63.97:/opt/tanishq/csv_import/
scp C:\path\to\invitees.csv root@10.10.63.97:/opt/tanishq/csv_import/
```

---

### Issue 2: Empty CSV Files

**Check:**
```bash
wc -l /opt/tanishq/csv_import/*.csv
```

**If showing 0 or 1 line:** Files are empty or only have headers

---

### Issue 3: Wrong CSV Format

**Common problems:**
- Line ending issues (Windows vs Unix)
- Encoding problems
- Wrong delimiter
- Missing quotes

**Check format:**
```bash
# Check file format
file /opt/tanishq/csv_import/events.csv

# Check for Windows line endings (CRLF)
cat -A /opt/tanishq/csv_import/events.csv | head -3
```

**If you see `^M$` at end of lines:** Windows line endings detected

**Fix:**
```bash
# Convert Windows to Unix line endings
dos2unix /opt/tanishq/csv_import/events.csv
dos2unix /opt/tanishq/csv_import/attendees.csv
dos2unix /opt/tanishq/csv_import/invitees.csv

# If dos2unix not installed:
sed -i 's/\r$//' /opt/tanishq/csv_import/events.csv
sed -i 's/\r$//' /opt/tanishq/csv_import/attendees.csv
sed -i 's/\r$//' /opt/tanishq/csv_import/invitees.csv
```

---

### Issue 4: Duplicate Primary Keys

**Your import might be skipping rows due to duplicate IDs**

**Check for errors with verbose output:**
```bash
mysql --local-infile=1 -u root -pNagaraj@07 selfie_prod --show-warnings << 'EOF'
SET FOREIGN_KEY_CHECKS=0;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv' 
INTO TABLE events 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY '\n' 
IGNORE 1 ROWS;
SHOW WARNINGS;
SET FOREIGN_KEY_CHECKS=1;
EOF
```

**If you see duplicate key warnings:**

**Option A: Skip duplicates (safer - keeps existing data):**
```bash
mysql --local-infile=1 -u root -pNagaraj@07 selfie_prod << 'EOF'
SET FOREIGN_KEY_CHECKS=0;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv' 
IGNORE INTO TABLE events 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY '\n' 
IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/attendees.csv' 
IGNORE INTO TABLE attendees 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY '\n' 
IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/invitees.csv' 
IGNORE INTO TABLE invitees 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY '\n' 
IGNORE 1 ROWS;
SET FOREIGN_KEY_CHECKS=1;
EOF
```

**Option B: Replace duplicates (overwrites existing):**
```bash
mysql --local-infile=1 -u root -pNagaraj@07 selfie_prod << 'EOF'
SET FOREIGN_KEY_CHECKS=0;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv' 
REPLACE INTO TABLE events 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY '\n' 
IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/attendees.csv' 
REPLACE INTO TABLE attendees 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY '\n' 
IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/invitees.csv' 
REPLACE INTO TABLE invitees 
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY '\n' 
IGNORE 1 ROWS;
SET FOREIGN_KEY_CHECKS=1;
EOF
```

---

### Issue 5: Column Mismatch

**CSV columns don't match database columns**

**Check database structure:**
```bash
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.events;" | head -30
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.attendees;" | head -30
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.invitees;" | head -30
```

**Check CSV headers:**
```bash
head -1 /opt/tanishq/csv_import/events.csv
head -1 /opt/tanishq/csv_import/attendees.csv
head -1 /opt/tanishq/csv_import/invitees.csv
```

**If columns don't match exactly:** You need to specify column mapping

---

## COMPLETE DIAGNOSTIC SCRIPT

**Run this to diagnose the issue:**

```bash
#!/bin/bash
echo "=================================="
echo "CSV IMPORT DIAGNOSTIC"
echo "=================================="
echo ""

echo "1. Checking if CSV files exist..."
ls -lh /opt/tanishq/csv_import/*.csv 2>/dev/null || echo "ERROR: CSV files not found!"
echo ""

echo "2. Counting rows in CSV files..."
echo "Events rows: $(wc -l < /opt/tanishq/csv_import/events.csv 2>/dev/null || echo '0')"
echo "Attendees rows: $(wc -l < /opt/tanishq/csv_import/attendees.csv 2>/dev/null || echo '0')"
echo "Invitees rows: $(wc -l < /opt/tanishq/csv_import/invitees.csv 2>/dev/null || echo '0')"
echo ""

echo "3. Checking file format..."
file /opt/tanishq/csv_import/events.csv 2>/dev/null
echo ""

echo "4. Preview of CSV files (first 3 rows)..."
echo "--- EVENTS ---"
head -3 /opt/tanishq/csv_import/events.csv 2>/dev/null
echo ""
echo "--- ATTENDEES ---"
head -3 /opt/tanishq/csv_import/attendees.csv 2>/dev/null
echo ""
echo "--- INVITEES ---"
head -3 /opt/tanishq/csv_import/invitees.csv 2>/dev/null
echo ""

echo "5. Current database counts..."
mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT 'events', COUNT(*) FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees UNION ALL SELECT 'invitees', COUNT(*) FROM invitees;" 2>/dev/null
echo ""

echo "6. Checking MySQL error log for recent errors..."
tail -20 /var/log/mysqld.log | grep -i error
echo ""

echo "=================================="
echo "DIAGNOSTIC COMPLETE"
echo "=================================="
```

**Save and run:**
```bash
# Save the script
cat > /opt/tanishq/diagnose_csv.sh << 'SCRIPT'
#!/bin/bash
echo "=================================="
echo "CSV IMPORT DIAGNOSTIC"
echo "=================================="
echo ""
echo "1. Checking if CSV files exist..."
ls -lh /opt/tanishq/csv_import/*.csv 2>/dev/null || echo "ERROR: CSV files not found!"
echo ""
echo "2. Counting rows in CSV files..."
echo "Events rows: $(wc -l < /opt/tanishq/csv_import/events.csv 2>/dev/null || echo '0')"
echo "Attendees rows: $(wc -l < /opt/tanishq/csv_import/attendees.csv 2>/dev/null || echo '0')"
echo "Invitees rows: $(wc -l < /opt/tanishq/csv_import/invitees.csv 2>/dev/null || echo '0')"
echo ""
echo "3. Checking file format..."
file /opt/tanishq/csv_import/events.csv 2>/dev/null
echo ""
echo "4. Preview of CSV files (first 3 rows)..."
echo "--- EVENTS ---"
head -3 /opt/tanishq/csv_import/events.csv 2>/dev/null
echo ""
echo "--- ATTENDEES ---"
head -3 /opt/tanishq/csv_import/attendees.csv 2>/dev/null
echo ""
echo "--- INVITEES ---"
head -3 /opt/tanishq/csv_import/invitees.csv 2>/dev/null
echo ""
echo "5. Current database counts..."
mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT 'events', COUNT(*) FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees UNION ALL SELECT 'invitees', COUNT(*) FROM invitees;" 2>/dev/null
echo ""
echo "6. Database structure (events table)..."
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.events;" 2>/dev/null | head -20
echo ""
echo "=================================="
echo "DIAGNOSTIC COMPLETE"
echo "=================================="
SCRIPT

chmod +x /opt/tanishq/diagnose_csv.sh
./diagnose_csv.sh
```

---

## MOST LIKELY ISSUE

**Based on your situation, the most likely issue is:**

1. **CSV files are NOT uploaded to the server** - Check with `ls -lh /opt/tanishq/csv_import/`
2. **Duplicate IDs** - Your CSV has same IDs as existing data, so rows are being rejected

---

## QUICK FIX TO TRY NOW

```bash
# 1. Check if CSV files exist and their size
ls -lh /opt/tanishq/csv_import/

# 2. If files exist, count rows
wc -l /opt/tanishq/csv_import/*.csv

# 3. If files exist with many rows, try with IGNORE keyword
mysql --local-infile=1 -u root -pNagaraj@07 selfie_prod << 'EOF'
SET FOREIGN_KEY_CHECKS=0;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv' IGNORE INTO TABLE events FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/attendees.csv' IGNORE INTO TABLE attendees FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/invitees.csv' IGNORE INTO TABLE invitees FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
SET FOREIGN_KEY_CHECKS=1;
EOF

# 4. Check counts again
mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT 'events', COUNT(*) FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees UNION ALL SELECT 'invitees', COUNT(*) FROM invitees;"
```

---

## NEXT STEP

**Run the diagnostic script above and share the output. It will tell us exactly what the problem is!**

