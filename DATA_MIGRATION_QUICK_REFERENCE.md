# QUICK REFERENCE - DATA MIGRATION COMMANDS

## 🚀 FASTEST METHOD (Recommended)

### Prerequisites
✅ Pre-prod server has all data in MySQL
✅ Production server MySQL is installed and database created
✅ You have SSH access to both servers

---

## ON PRE-PROD SERVER

```bash
# 1. Export all data
mysqldump -u root -p selfie_preprod \
  --no-create-info \
  --complete-insert \
  --skip-triggers \
  events attendees invitees bride_details users stores product_details greetings rivaah rivaah_users abm_login cee_login rbm_login user_details password_history \
  > /tmp/preprod_data_export.sql

# 2. Transfer to production server (replace IP if different)
scp /tmp/preprod_data_export.sql root@10.10.63.97:/opt/tanishq/
```

---

## ON PRODUCTION SERVER (10.10.63.97)

```bash
# 3. Stop application
kill $(cat /opt/tanishq/tanishq-prod.pid)

# 4. Backup current database
mysqldump -u root -p selfie_prod > /opt/tanishq/backup_$(date +%Y%m%d_%H%M%S).sql

# 5. Import data
mysql -u root -p selfie_prod < /opt/tanishq/preprod_data_export.sql
# Password: Nagaraj@07

# 6. Verify data
mysql -u root -p -e "USE selfie_prod; 
SELECT 'events', COUNT(*) FROM events 
UNION ALL SELECT 'attendees', COUNT(*) FROM attendees 
UNION ALL SELECT 'bride_details', COUNT(*) FROM bride_details 
UNION ALL SELECT 'users', COUNT(*) FROM users;"
# Password: Nagaraj@07

# 7. Restart application
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > /opt/tanishq/logs/application.log 2>&1 &
echo $! > /opt/tanishq/tanishq-prod.pid

# 8. Check application
tail -f /opt/tanishq/logs/application.log
# Press Ctrl+C to exit

# 9. Test application
curl -I http://localhost:3000/
# Should return: HTTP/1.1 200
```

---

## TROUBLESHOOTING

### If you get "Table doesn't exist" error:

```bash
# Let Spring Boot create tables first
kill $(cat /opt/tanishq/tanishq-prod.pid)
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod --server.port=3000 \
  > /opt/tanishq/logs/application.log 2>&1 &
echo $! > /opt/tanishq/tanishq-prod.pid
sleep 30  # Wait for tables to be created

# Then import again
kill $(cat /opt/tanishq/tanishq-prod.pid)
mysql -u root -p selfie_prod < /opt/tanishq/preprod_data_export.sql
```

### If you get "Foreign key constraint" error:

```bash
# Import with foreign keys disabled
mysql -u root -p -e "
SET FOREIGN_KEY_CHECKS=0;
SOURCE /opt/tanishq/preprod_data_export.sql;
SET FOREIGN_KEY_CHECKS=1;
" selfie_prod
```

### If you get "Duplicate entry" error:

```bash
# Clear existing data first (CAUTION!)
mysql -u root -p -e "
USE selfie_prod;
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE attendees;
TRUNCATE TABLE invitees;
TRUNCATE TABLE greetings;
TRUNCATE TABLE events;
TRUNCATE TABLE bride_details;
SET FOREIGN_KEY_CHECKS=1;
"

# Then import again
mysql -u root -p selfie_prod < /opt/tanishq/preprod_data_export.sql
```

---

## VERIFY EVERYTHING IS WORKING

```bash
# On production server

# 1. Check if application is running
ps -p $(cat /opt/tanishq/tanishq-prod.pid)

# 2. Check if port 3000 is listening
ss -tlnp | grep 3000

# 3. Test HTTP response
curl -I http://localhost:3000/

# 4. Check database has data
mysql -u root -p -e "USE selfie_prod; SELECT COUNT(*) FROM events;"

# 5. Check recent logs
tail -100 /opt/tanishq/logs/application.log

# 6. Check for errors
grep -i "error\|exception" /opt/tanishq/logs/application.log | tail -20
```

---

## APPLICATION MANAGEMENT COMMANDS

```bash
# Start application
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod --server.port=3000 \
  > /opt/tanishq/logs/application.log 2>&1 &
echo $! > /opt/tanishq/tanishq-prod.pid

# Stop application
kill $(cat /opt/tanishq/tanishq-prod.pid)

# Restart application
kill $(cat /opt/tanishq/tanishq-prod.pid)
sleep 5
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod --server.port=3000 \
  > /opt/tanishq/logs/application.log 2>&1 &
echo $! > /opt/tanishq/tanishq-prod.pid

# View logs (real-time)
tail -f /opt/tanishq/logs/application.log

# View logs (last 100 lines)
tail -100 /opt/tanishq/logs/application.log

# Search for errors
grep -i "error" /opt/tanishq/logs/application.log | tail -20
```

---

## DATABASE MANAGEMENT COMMANDS

```bash
# Connect to database
mysql -u root -p selfie_prod

# Show all tables
mysql -u root -p -e "USE selfie_prod; SHOW TABLES;"

# Count records in all tables
mysql -u root -p -e "
USE selfie_prod;
SELECT 'events' as table_name, COUNT(*) as count FROM events
UNION ALL SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'stores', COUNT(*) FROM stores
UNION ALL SELECT 'product_details', COUNT(*) FROM product_details
UNION ALL SELECT 'greetings', COUNT(*) FROM greetings;
"

# Backup database
mysqldump -u root -p selfie_prod > /opt/tanishq/backup_$(date +%Y%m%d_%H%M%S).sql

# Restore database
mysql -u root -p selfie_prod < /opt/tanishq/backup_20260113_235959.sql
```

---

## PRODUCTION SERVER INFO

- **IP:** 10.10.63.97
- **MySQL User:** root
- **MySQL Password:** Nagaraj@07
- **Database:** selfie_prod
- **Application Port:** 3000
- **WAR File:** /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war
- **Logs:** /opt/tanishq/logs/application.log
- **PID File:** /opt/tanishq/tanishq-prod.pid
- **Storage:** /opt/tanishq/storage/

---

## COMPLETE MIGRATION IN ONE GO

```bash
# ON PRE-PROD SERVER (single command)
mysqldump -u root -p selfie_preprod --no-create-info --complete-insert --skip-triggers events attendees invitees bride_details users stores product_details greetings rivaah rivaah_users abm_login cee_login rbm_login user_details password_history > /tmp/preprod_data_export.sql && scp /tmp/preprod_data_export.sql root@10.10.63.97:/opt/tanishq/

# ON PRODUCTION SERVER (single command block)
kill $(cat /opt/tanishq/tanishq-prod.pid) 2>/dev/null ; \
mysqldump -u root -pNagaraj@07 selfie_prod > /opt/tanishq/backup_$(date +%Y%m%d_%H%M%S).sql ; \
mysql -u root -pNagaraj@07 selfie_prod < /opt/tanishq/preprod_data_export.sql ; \
mysql -u root -pNagaraj@07 -e "USE selfie_prod; SELECT 'events', COUNT(*) FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees UNION ALL SELECT 'users', COUNT(*) FROM users;" ; \
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > /opt/tanishq/logs/application.log 2>&1 & echo $! > /opt/tanishq/tanishq-prod.pid ; \
sleep 10 ; \
tail -50 /opt/tanishq/logs/application.log
```

---

## FINAL CHECKLIST

- [ ] Data exported from pre-prod
- [ ] Data transferred to production
- [ ] Production database backed up
- [ ] Data imported to production
- [ ] Data counts verified (> 0 records)
- [ ] Application restarted
- [ ] Application running (check with `ps`)
- [ ] Port 3000 listening (check with `ss -tlnp | grep 3000`)
- [ ] HTTP 200 response (check with `curl -I http://localhost:3000/`)
- [ ] Website accessible from browser
- [ ] Users can login
- [ ] 2 months of data visible

---

**DONE! 🎉**

