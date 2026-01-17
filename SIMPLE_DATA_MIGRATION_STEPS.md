# SIMPLE STEP-BY-STEP DATA MIGRATION PROCESS

## Your Situation
- ✅ Pre-prod server: Has 2 months of data in MySQL - WORKING
- ❌ Production server: Empty MySQL database - NEEDS DATA
- 🎯 Goal: Copy all data from pre-prod to production

---

## THE SIMPLEST APPROACH - 3 STEPS ONLY!

### STEP 1: Export Data from Pre-Prod Server

**On Pre-Prod Server (SSH into pre-prod):**

```bash
# Export all data from pre-prod
mysqldump -u root -p selfie_preprod \
  --no-create-info \
  --complete-insert \
  --skip-triggers \
  events attendees invitees bride_details users stores product_details greetings rivaah rivaah_users abm_login cee_login rbm_login user_details password_history \
  > /tmp/preprod_data_export.sql

# Check if export was successful
ls -lh /tmp/preprod_data_export.sql

# You should see the file size (e.g., 2.3M)
```

**Enter password when prompted:** (your pre-prod MySQL root password)

---

### STEP 2: Transfer File to Production Server

**On Pre-Prod Server:**

```bash
# Transfer the exported data to production server
# Replace 10.10.63.97 with your actual production server IP
scp /tmp/preprod_data_export.sql root@10.10.63.97:/opt/tanishq/preprod_data_export.sql
```

**Enter production server password when prompted**

---

### STEP 3: Import Data to Production Database

**On Production Server (SSH into production - 10.10.63.97):**

```bash
# IMPORTANT: Stop the application first
kill $(cat /opt/tanishq/tanishq-prod.pid)

# Backup current production database (just in case)
mysqldump -u root -p selfie_prod > /opt/tanishq/backup_before_import_$(date +%Y%m%d_%H%M%S).sql

# Import the data
mysql -u root -p selfie_prod < /opt/tanishq/preprod_data_export.sql
```

**Enter password:** `Nagaraj@07`

---

### STEP 4: Verify Data

**On Production Server:**

```bash
# Check if data was imported
mysql -u root -p -e "
USE selfie_prod;
SELECT '========================' as '';
SELECT 'DATA VERIFICATION' as '';
SELECT '========================' as '';
SELECT 'events' as table_name, COUNT(*) as count FROM events
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL
SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'stores', COUNT(*) FROM stores
UNION ALL
SELECT 'product_details', COUNT(*) FROM product_details
UNION ALL
SELECT 'greetings', COUNT(*) FROM greetings;
"
```

**Enter password:** `Nagaraj@07`

You should see counts > 0 for tables with data!

---

### STEP 5: Restart Application

**On Production Server:**

```bash
# Clear logs
> /opt/tanishq/logs/application.log

# Start application on port 3000
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > /opt/tanishq/logs/application.log 2>&1 &

# Save the process ID
echo $! > /opt/tanishq/tanishq-prod.pid

# Wait 30 seconds
sleep 30

# Check if application is running
ps -p $(cat /opt/tanishq/tanishq-prod.pid)

# Check logs
tail -f /opt/tanishq/logs/application.log
```

Press `Ctrl+C` to stop tailing logs

---

### STEP 6: Test Application

**On Production Server:**

```bash
# Test if application is responding
curl -I http://localhost:3000/

# Should return HTTP/1.1 200
```

**On Your Browser:**
- Open your production URL
- Login with existing credentials
- Verify you can see the 2 months of data

---

## TROUBLESHOOTING

### Problem 1: Table doesn't exist error

**Solution:**
```bash
# Let Spring Boot create missing tables first
# In application-prod.properties, ensure this is set:
# spring.jpa.hibernate.ddl-auto=update

# Restart application to auto-create tables
kill $(cat /opt/tanishq/tanishq-prod.pid)

nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > /opt/tanishq/logs/application.log 2>&1 &

echo $! > /opt/tanishq/tanishq-prod.pid

# Wait for tables to be created (30 seconds)
sleep 30

# Then import data again
mysql -u root -p selfie_prod < /opt/tanishq/preprod_data_export.sql
```

---

### Problem 2: Foreign key constraint errors

**Solution:**
```bash
# Import with foreign key checks disabled
mysql -u root -p -e "
SET FOREIGN_KEY_CHECKS=0;
SOURCE /opt/tanishq/preprod_data_export.sql;
SET FOREIGN_KEY_CHECKS=1;
" selfie_prod
```

---

### Problem 3: Duplicate entry errors

**Solution:**
```bash
# Clear existing data first (CAUTION: This deletes current data!)
mysql -u root -p -e "
USE selfie_prod;
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE attendees;
TRUNCATE TABLE invitees;
TRUNCATE TABLE greetings;
TRUNCATE TABLE events;
TRUNCATE TABLE bride_details;
TRUNCATE TABLE product_details;
SET FOREIGN_KEY_CHECKS=1;
" 

# Then import again
mysql -u root -p selfie_prod < /opt/tanishq/preprod_data_export.sql
```

---

### Problem 4: Can't connect to pre-prod server

**Alternative: Export from your local backup**

If you have a database backup from pre-prod on your local machine:

```powershell
# On Windows (PowerShell)
scp C:\path\to\backup.sql root@10.10.63.97:/opt/tanishq/preprod_data_export.sql
```

Then continue with STEP 3 above.

---

## VERIFICATION CHECKLIST

After migration, verify these:

- [ ] Login works with existing credentials
- [ ] Events are displayed (should show 2 months of events)
- [ ] Attendee data is linked to events
- [ ] Invitee data is linked to events
- [ ] Bride details are accessible
- [ ] Store information is correct
- [ ] Product details are loaded
- [ ] No errors in application logs

---

## COMPLETE COMMAND SEQUENCE

### ON PRE-PROD SERVER:
```bash
mysqldump -u root -p selfie_preprod --no-create-info --complete-insert --skip-triggers events attendees invitees bride_details users stores product_details greetings rivaah rivaah_users abm_login cee_login rbm_login user_details password_history > /tmp/preprod_data_export.sql

scp /tmp/preprod_data_export.sql root@10.10.63.97:/opt/tanishq/preprod_data_export.sql
```

### ON PRODUCTION SERVER:
```bash
kill $(cat /opt/tanishq/tanishq-prod.pid)

mysqldump -u root -p selfie_prod > /opt/tanishq/backup_before_import_$(date +%Y%m%d_%H%M%S).sql

mysql -u root -p selfie_prod < /opt/tanishq/preprod_data_export.sql

mysql -u root -p -e "USE selfie_prod; SELECT 'events' as table_name, COUNT(*) as count FROM events UNION ALL SELECT 'attendees', COUNT(*) FROM attendees UNION ALL SELECT 'bride_details', COUNT(*) FROM bride_details UNION ALL SELECT 'users', COUNT(*) FROM users;"

nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > /opt/tanishq/logs/application.log 2>&1 &

echo $! > /opt/tanishq/tanishq-prod.pid

tail -f /opt/tanishq/logs/application.log
```

---

## SUMMARY

1. **Export** from pre-prod MySQL
2. **Transfer** file to production
3. **Import** to production MySQL
4. **Verify** data counts
5. **Restart** application
6. **Test** website

**Total Time:** 10-15 minutes

**Risk Level:** Low (you have backups!)

---

## QUESTIONS?

- Can't access pre-prod server? → Use local backup file
- Error during import? → Check troubleshooting section above
- Application won't start? → Check logs: `tail -f /opt/tanishq/logs/application.log`
- Bad Gateway error? → Check Nginx configuration, ensure port 3000 is used

---

**Ready to migrate? Follow STEP 1 above! 🚀**

