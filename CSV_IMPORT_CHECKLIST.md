# ✅ CSV IMPORT - COMPLETE CHECKLIST

## 📋 PREPARATION PHASE

### On Your Windows PC:

- [ ] CSV files created:
  - [ ] events.csv
  - [ ] attendees.csv  
  - [ ] invitees.csv

- [ ] CSV format verified:
  - [ ] First row is column headers
  - [ ] Headers match database column names
  - [ ] Date format is YYYY-MM-DD
  - [ ] No empty rows
  - [ ] UTF-8 encoding (no BOM)

- [ ] Check database structure (run once):
```bash
ssh root@10.10.63.97
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.events;"
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.attendees;"
mysql -u root -pNagaraj@07 -e "DESCRIBE selfie_prod.invitees;"
```

- [ ] CSV headers match database columns ✓

---

## 🚀 EXECUTION PHASE

### Step 1: Upload Scripts (One-time setup)

- [ ] Upload import scripts to server:
```powershell
.\setup_scripts_on_server.bat
```

### Step 2: Upload CSV Files

- [ ] Upload CSV files:
```powershell
.\upload_csv_files.bat
```

**OR manually:**
```powershell
scp C:\path\to\events.csv root@10.10.63.97:/opt/tanishq/csv_import/
scp C:\path\to\attendees.csv root@10.10.63.97:/opt/tanishq/csv_import/
scp C:\path\to\invitees.csv root@10.10.63.97:/opt/tanishq/csv_import/
```

### Step 3: Validate CSV (Optional but Recommended)

- [ ] SSH to server:
```bash
ssh root@10.10.63.97
```

- [ ] Validate CSV files:
```bash
cd /opt/tanishq
./validate_csv_files.sh
```

- [ ] Fix any issues found ✓

### Step 4: Run Import

- [ ] Run automated import:
```bash
cd /opt/tanishq
./import_csv_to_mysql.sh
```

**OR manual import:**
```bash
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

## ✅ VERIFICATION PHASE

### On Production Server:

- [ ] Check backup was created:
```bash
ls -lh /opt/tanishq/backups/
```

- [ ] Verify data counts:
```bash
mysql -u root -pNagaraj@07 -e "
USE selfie_prod;
SELECT 'events' as table_name, COUNT(*) as count FROM events
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees;
"
```

Expected: Counts should increase by number of CSV rows

- [ ] View sample imported data:
```bash
mysql -u root -pNagaraj@07 -e "
USE selfie_prod;
SELECT * FROM events ORDER BY created_at DESC LIMIT 5;
"
```

- [ ] Check application is running:
```bash
ps -p $(cat /opt/tanishq/tanishq-prod.pid)
```

- [ ] Check port 3000 is listening:
```bash
ss -tlnp | grep 3000
```

- [ ] Test HTTP response:
```bash
curl -I http://localhost:3000/
```

Expected: HTTP/1.1 200

- [ ] Check application logs:
```bash
tail -50 /opt/tanishq/logs/application.log
```

- [ ] Check for errors:
```bash
grep -i "error" /opt/tanishq/logs/application.log | tail -20
```

### In Web Browser:

- [ ] Open production URL
- [ ] Login with existing user
- [ ] Verify events are visible
- [ ] Check attendee data is linked to events
- [ ] Verify invitee data is linked to events
- [ ] Test all major features
- [ ] Check if 2 months of data is visible

---

## 🎯 POST-IMPORT TASKS

- [ ] Monitor logs for 10-15 minutes:
```bash
tail -f /opt/tanishq/logs/application.log
```

- [ ] Test different user types:
  - [ ] ABM login
  - [ ] CEE login
  - [ ] RBM login
  - [ ] Regular user login

- [ ] Verify data integrity:
  - [ ] Events have correct dates
  - [ ] Attendees linked to correct events
  - [ ] Invitees linked to correct events
  - [ ] All relationships intact

- [ ] Performance check:
  - [ ] Website loads quickly
  - [ ] No database errors
  - [ ] Queries are fast

---

## 📊 SUCCESS CRITERIA

✅ All CSV files uploaded  
✅ Backup created successfully  
✅ Import completed without errors  
✅ Record counts increased correctly  
✅ Application restarted successfully  
✅ Port 3000 listening  
✅ HTTP 200 response  
✅ Website accessible  
✅ Data visible in application  
✅ No errors in logs  
✅ All features working  

---

## 🔄 ROLLBACK PROCEDURE (If Needed)

If something goes wrong:

- [ ] Stop application:
```bash
kill $(cat /opt/tanishq/tanishq-prod.pid)
```

- [ ] Restore from backup:
```bash
mysql -u root -pNagaraj@07 selfie_prod < /opt/tanishq/backups/backup_TIMESTAMP.sql
```

- [ ] Restart application:
```bash
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid
```

- [ ] Verify rollback:
```bash
mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT COUNT(*) FROM events;"
curl -I http://localhost:3000/
```

---

## 📞 TROUBLESHOOTING

### If import fails:

1. Check CSV file format:
```bash
./validate_csv_files.sh
```

2. Check MySQL local-infile setting:
```bash
mysql -u root -pNagaraj@07 -e "SHOW VARIABLES LIKE 'local_infile';"
```

3. Enable if disabled:
```bash
vi /etc/my.cnf
# Add: local-infile=1 under [mysqld] and [mysql]
systemctl restart mysqld
```

### If application won't start:

1. Check logs:
```bash
tail -100 /opt/tanishq/logs/application.log
```

2. Check if port is already in use:
```bash
ss -tlnp | grep 3000
```

3. Kill existing process if needed:
```bash
kill -9 $(lsof -t -i:3000)
```

### If data not visible:

1. Verify data in database:
```bash
mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT * FROM events LIMIT 5;"
```

2. Check foreign key relationships:
```bash
mysql -u root -pNagaraj@07 -e "
USE selfie_prod;
SELECT e.id, e.event_name, COUNT(a.id) as attendee_count
FROM events e
LEFT JOIN attendees a ON e.id = a.event_id
GROUP BY e.id
LIMIT 5;
"
```

3. Clear browser cache and try again

---

## 🎉 COMPLETION

Once all checkboxes are ticked:

✅ **CSV Import Completed Successfully!**

Your 2 months of historical data is now in production and users can access it!

---

## 📝 NOTES

**Import Date:** _______________  
**Records Imported:**
- Events: _______
- Attendees: _______
- Invitees: _______

**Backup File:** _______________________________

**Issues Encountered:** _______________________________

**Resolution:** _______________________________

**Verified By:** _______________________________

---

**Keep this checklist for your records!** 📋

