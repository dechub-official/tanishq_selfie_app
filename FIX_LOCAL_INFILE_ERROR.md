# 🔧 FIX: Loading Local Data is Disabled Error

## ❌ Your Error
```
ERROR 3948 (42000) at line 2: Loading local data is disabled; 
this must be enabled on both the client and server sides
```

## ✅ QUICK FIX (5 Minutes)

---

## STEP 1: Enable local-infile in MySQL

```bash
# Edit MySQL configuration
vi /etc/my.cnf
```

**Press `i` to enter insert mode, then add these lines:**

Find the `[mysqld]` section and add:
```ini
[mysqld]
local-infile=1
```

Find or create the `[mysql]` section and add:
```ini
[mysql]
local-infile=1
```

**Your /etc/my.cnf should look like this:**
```ini
[mysqld]
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock
log-error=/var/log/mysqld.log
pid-file=/var/run/mysqld/mysqld.pid
local-infile=1

[mysql]
local-infile=1
```

**Save and exit:**
- Press `ESC`
- Type `:wq`
- Press `ENTER`

---

## STEP 2: Restart MySQL

```bash
systemctl restart mysqld
```

**Verify MySQL is running:**
```bash
systemctl status mysqld
```

---

## STEP 3: Verify local-infile is Enabled

```bash
mysql -u root -pNagaraj@07 -e "SHOW VARIABLES LIKE 'local_infile';"
```

**Expected output:**
```
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| local_infile  | ON    |
+---------------+-------+
```

---

## STEP 4: Re-run the Import Command

```bash
cd /opt/tanishq

mysql --local-infile=1 -u root -pNagaraj@07 selfie_prod << 'EOF'
SET FOREIGN_KEY_CHECKS=0;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv' INTO TABLE events FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/attendees.csv' INTO TABLE attendees FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/invitees.csv' INTO TABLE invitees FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
SET FOREIGN_KEY_CHECKS=1;
EOF
```

---

## STEP 5: Verify Data Import

```bash
mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT 'events', COUNT(*) FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees UNION ALL SELECT 'invitees', COUNT(*) FROM invitees;"
```

**You should see increased counts!**

---

## STEP 6: Restart Application

```bash
cd /opt/tanishq

nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid

# Wait 10 seconds
sleep 10

# Check application
ps -p $(cat tanishq-prod.pid)

# Test
curl -I http://localhost:3000/
```

---

## ✅ COMPLETE COMMAND SEQUENCE (Copy-Paste This)

```bash
# 1. Edit MySQL config
vi /etc/my.cnf
# Add local-infile=1 under [mysqld] and [mysql] sections
# Press ESC, type :wq, press ENTER

# 2. Restart MySQL
systemctl restart mysqld

# 3. Verify setting
mysql -u root -pNagaraj@07 -e "SHOW VARIABLES LIKE 'local_infile';"

# 4. Re-run import
cd /opt/tanishq
mysql --local-infile=1 -u root -pNagaraj@07 selfie_prod << 'EOF'
SET FOREIGN_KEY_CHECKS=0;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv' INTO TABLE events FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/attendees.csv' INTO TABLE attendees FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/invitees.csv' INTO TABLE invitees FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
SET FOREIGN_KEY_CHECKS=1;
EOF

# 5. Verify counts
mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT 'events', COUNT(*) FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees UNION ALL SELECT 'invitees', COUNT(*) FROM invitees;"

# 6. Restart app
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > logs/application.log 2>&1 & echo $! > tanishq-prod.pid

# 7. Check status
sleep 10
ps -p $(cat tanishq-prod.pid)
curl -I http://localhost:3000/
tail -50 logs/application.log
```

---

## 🎯 TROUBLESHOOTING

### If vi editor is difficult, use nano:
```bash
nano /etc/my.cnf
# Add the lines
# Press Ctrl+X, then Y, then ENTER
```

### If MySQL won't restart:
```bash
# Check for errors
tail -50 /var/log/mysqld.log

# Check if syntax is correct
cat /etc/my.cnf
```

### If local-infile still shows OFF:
```bash
# Try this alternative location
vi /etc/mysql/my.cnf
# Or
vi /usr/my.cnf
```

---

## ✅ SUCCESS INDICATORS

After following these steps, you should see:

- ✅ `local_infile = ON` in MySQL
- ✅ No error during import
- ✅ Increased record counts
- ✅ Application running
- ✅ HTTP 200 response

---

**Total Time: 5 minutes**

**Follow the steps above and your CSV import will complete successfully!** 🚀

