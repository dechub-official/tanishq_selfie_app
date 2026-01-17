# 📋 DATA MIGRATION - COMPLETE SOLUTION

## Your Question
> "I have 2 months of historical data but it's not structured like MySQL database. How can I add this data to production database so users can see their existing data?"

## ✅ ANSWER: Copy Data from Pre-Prod to Production

Your pre-prod server **already has** the 2 months of data properly structured in MySQL format. You don't need to restructure anything - just copy the data!

---

## 🎯 WHAT YOU NEED TO DO

### **SIMPLEST APPROACH - 3 STEPS:**

1. **Export data from Pre-Prod MySQL** → Creates SQL file
2. **Transfer SQL file to Production server** → Copy file
3. **Import SQL file to Production MySQL** → Load data

**Total Time:** 10-15 minutes

---

## 📚 DOCUMENTATION CREATED FOR YOU

I've created 4 comprehensive guides to help you:

### 1️⃣ **SIMPLE_DATA_MIGRATION_STEPS.md** ⭐ START HERE!
- Step-by-step manual process
- Easy to follow commands
- Copy-paste friendly
- **Best for:** Manual execution with full control

### 2️⃣ **DATA_MIGRATION_QUICK_REFERENCE.md** ⭐ QUICK COMMANDS!
- All commands in one place
- No explanations, just commands
- Troubleshooting commands
- **Best for:** Quick reference during migration

### 3️⃣ **LEGACY_DATA_MIGRATION_GUIDE.md**
- Detailed explanation of all approaches
- Background on Google Sheets migration
- Multiple migration options
- **Best for:** Understanding the complete picture

### 4️⃣ **migrate_data_windows.bat**
- Automated Windows script
- Runs from your local machine
- Need to update server IPs first
- **Best for:** Automated execution

---

## 🚀 RECOMMENDED PROCESS

### **FOLLOW THIS EXACT SEQUENCE:**

#### **ON PRE-PROD SERVER:**

```bash
# SSH into pre-prod server
ssh root@<PREPROD_SERVER_IP>

# Export all data from pre-prod database
mysqldump -u root -p selfie_preprod \
  --no-create-info \
  --complete-insert \
  --skip-triggers \
  events attendees invitees bride_details users stores product_details greetings \
  rivaah rivaah_users abm_login cee_login rbm_login user_details password_history \
  > /tmp/preprod_data_export.sql

# Transfer to production server
scp /tmp/preprod_data_export.sql root@10.10.63.97:/opt/tanishq/
```

#### **ON PRODUCTION SERVER (10.10.63.97):**

```bash
# SSH into production server
ssh root@10.10.63.97

# Stop the application
kill $(cat /opt/tanishq/tanishq-prod.pid)

# Backup current production database (safety!)
mysqldump -u root -p selfie_prod > /opt/tanishq/backup_before_migration.sql

# Import the data
mysql -u root -p selfie_prod < /opt/tanishq/preprod_data_export.sql
# Password: Nagaraj@07

# Verify data was imported
mysql -u root -p -e "
USE selfie_prod;
SELECT 'events' as table_name, COUNT(*) as count FROM events
UNION ALL SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL SELECT 'users', COUNT(*) FROM users;
"
# Password: Nagaraj@07

# Restart application
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > /opt/tanishq/logs/application.log 2>&1 &

echo $! > /opt/tanishq/tanishq-prod.pid

# Check application is running
ps -p $(cat /opt/tanishq/tanishq-prod.pid)

# Check logs
tail -f /opt/tanishq/logs/application.log
# Press Ctrl+C to stop viewing logs

# Test application
curl -I http://localhost:3000/
# Should return: HTTP/1.1 200
```

---

## ❓ COMMON QUESTIONS ANSWERED

### Q: "Will this overwrite existing production data?"
**A:** No, it will merge/add data. But we create a backup first just in case.

### Q: "What if I don't have access to pre-prod server?"
**A:** If you have a backup file from pre-prod on your local machine, you can upload it directly to production and import it.

### Q: "What about the Google Sheets data?"
**A:** Your pre-prod already migrated from Google Sheets to MySQL. You're copying that MySQL data, which is already properly structured.

### Q: "What if tables don't exist in production?"
**A:** 
```bash
# Let Spring Boot auto-create tables by starting the app once
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod --server.port=3000 \
  > /opt/tanishq/logs/application.log 2>&1 &
echo $! > /opt/tanishq/tanishq-prod.pid

# Wait 30 seconds for tables to be created
sleep 30

# Then stop and import data
kill $(cat /opt/tanishq/tanishq-prod.pid)
mysql -u root -p selfie_prod < /opt/tanishq/preprod_data_export.sql
```

### Q: "How do I verify users can see their data?"
**A:** 
1. Open production URL in browser
2. Login with an existing user from the last 2 months
3. Check if events/data from the last 2 months are visible

---

## 🔧 TROUBLESHOOTING

### Error: "Table doesn't exist"
```bash
# Run application once to auto-create tables, then import
```
See SIMPLE_DATA_MIGRATION_STEPS.md → Troubleshooting section

### Error: "Foreign key constraint fails"
```bash
# Import with foreign key checks disabled
mysql -u root -p -e "
SET FOREIGN_KEY_CHECKS=0;
SOURCE /opt/tanishq/preprod_data_export.sql;
SET FOREIGN_KEY_CHECKS=1;
" selfie_prod
```

### Error: "Duplicate entry"
```bash
# Clear existing data first, then import
# See DATA_MIGRATION_QUICK_REFERENCE.md for commands
```

### Application shows 502 Bad Gateway
1. Check if application is running: `ps -p $(cat /opt/tanishq/tanishq-prod.pid)`
2. Check if port 3000 is listening: `ss -tlnp | grep 3000`
3. Check Nginx configuration points to port 3000
4. Check logs: `tail -f /opt/tanishq/logs/application.log`

---

## ✅ VERIFICATION CHECKLIST

After migration, verify these:

- [ ] **Data Exported:** Check file size: `ls -lh /tmp/preprod_data_export.sql`
- [ ] **Data Transferred:** Check on prod: `ls -lh /opt/tanishq/preprod_data_export.sql`
- [ ] **Backup Created:** Check: `ls -lh /opt/tanishq/backup_*.sql`
- [ ] **Data Imported:** No errors during import
- [ ] **Data Counts:** All tables show count > 0
- [ ] **Application Running:** `ps` shows Java process
- [ ] **Port Listening:** `ss -tlnp | grep 3000` shows listener
- [ ] **HTTP Response:** `curl -I http://localhost:3000/` returns 200
- [ ] **Website Accessible:** Can open in browser
- [ ] **Login Works:** Existing users can login
- [ ] **Data Visible:** Users can see their 2 months of data

---

## 📊 WHAT GETS MIGRATED

These tables will be copied from pre-prod to production:

| Table | Description | Example Data |
|-------|-------------|--------------|
| `events` | All events from last 2 months | Store events, community events |
| `attendees` | People who attended events | Names, emails, photos |
| `invitees` | People invited to events | Contact information |
| `bride_details` | Bride registration data | Names, dates, contact info |
| `users` | System users | Login credentials, roles |
| `stores` | Store information | Store codes, locations |
| `product_details` | Product catalog | Product names, categories |
| `greetings` | Greeting messages | Messages sent to attendees |
| `rivaah` | Rivaah specific data | Rivaah events and info |
| `rivaah_users` | Rivaah user accounts | User credentials |
| `abm_login` | ABM login records | ABM user sessions |
| `cee_login` | CEE login records | CEE user sessions |
| `rbm_login` | RBM login records | RBM user sessions |
| `user_details` | Extended user information | Additional user data |
| `password_history` | Password change history | For security tracking |

---

## 🎯 NEXT STEPS AFTER SUCCESSFUL MIGRATION

1. **Test thoroughly:**
   - Login as different user types (ABM, CEE, RBM)
   - View events from the last 2 months
   - Check attendee lists
   - Verify bride details
   - Test all major features

2. **Update Nginx (if using):**
   - Ensure proxy points to port 3000
   - Restart Nginx if needed

3. **Monitor for a day:**
   - Watch application logs
   - Check for any errors
   - Monitor database connections
   - Verify no data loss

4. **Disable Google Sheets sync:**
   - Once confirmed MySQL is working
   - Remove or comment out Google Sheets scheduler
   - Clean up old Google Sheets code (optional)

5. **Set up regular backups:**
   ```bash
   # Create daily backup cron job
   crontab -e
   # Add: 0 2 * * * mysqldump -u root -pNagaraj@07 selfie_prod > /opt/tanishq/backups/backup_$(date +\%Y\%m\%d).sql
   ```

---

## 📞 NEED HELP?

1. **Start with:** SIMPLE_DATA_MIGRATION_STEPS.md
2. **Quick commands:** DATA_MIGRATION_QUICK_REFERENCE.md
3. **Troubleshooting:** Check the troubleshooting sections in both guides
4. **Verify:** Use the verification commands in DATA_MIGRATION_QUICK_REFERENCE.md

---

## 🎉 YOU'RE READY!

**Everything you need is in these 4 files:**

1. ✅ **SIMPLE_DATA_MIGRATION_STEPS.md** - Follow this first!
2. ✅ **DATA_MIGRATION_QUICK_REFERENCE.md** - Keep this open while migrating
3. ✅ **LEGACY_DATA_MIGRATION_GUIDE.md** - For deeper understanding
4. ✅ **migrate_data_windows.bat** - If you want automation

**Start the migration by opening SIMPLE_DATA_MIGRATION_STEPS.md and following STEP 1!**

---

**Good luck! The migration should take 10-15 minutes. 🚀**

