# 🚀 DEPLOY TO PRODUCTION - SIMPLE STEP-BY-STEP GUIDE

## Before You Start

**Two Important Facts:**
1. ✅ Pre-prod and production are **DIFFERENT servers** - no need to stop pre-prod!
2. ✅ Your code already supports MySQL - migration from Google Sheets is done!

---

## Step 1: Update MySQL Password (2 minutes)

**File to edit:** `src/main/resources/application-prod.properties`

**Find line 11:**
```properties
spring.datasource.password=YOUR_PRODUCTION_MYSQL_PASSWORD_HERE
```

**Change to your actual production MySQL password:**
```properties
spring.datasource.password=YourActualPassword
```

**Save the file!**

---

## Step 2: Build Production WAR (3 minutes)

Open PowerShell and run:

```powershell
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

mvn clean package -Pprod -DskipTests
```

**Wait for:** `BUILD SUCCESS`

**Verify file created:**
```powershell
dir target\*.war
```

You should see: `tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war`

---

## Step 3: Upload Files to Production Server (5 minutes)

**Option A - Using WinSCP (GUI):**
1. Open WinSCP
2. Connect to `10.10.63.97` as `root`
3. Navigate to `/opt/tanishq/`
4. Upload these files:
   - `target\tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war` → Rename to `tanishq-prod.war`
   - `src\main\resources\tanishqgmb-5437243a8085.p12`
   - `src\main\resources\event-images-469618-32e65f6d62b3.p12`
   - `setup_production_server.sh`
   - `setup_production_database.sql`

**Option B - Using pscp command:**
```powershell
pscp target\tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war root@10.10.63.97:/opt/tanishq/tanishq-prod.war

pscp src\main\resources\tanishqgmb-5437243a8085.p12 root@10.10.63.97:/opt/tanishq/

pscp src\main\resources\event-images-469618-32e65f6d62b3.p12 root@10.10.63.97:/opt/tanishq/

pscp setup_production_server.sh root@10.10.63.97:/opt/tanishq/

pscp setup_production_database.sql root@10.10.63.97:/opt/tanishq/
```

---

## Step 4: SSH to Production Server (1 minute)

```bash
ssh root@10.10.63.97
```

---

## Step 5: Create Database (2 minutes)

On production server, run:

```bash
cd /opt/tanishq

mysql -u root -p < setup_production_database.sql
```

Enter your MySQL root password when prompted.

**Verify database created:**
```bash
mysql -u root -p -e "SHOW DATABASES LIKE 'selfie_prod';"
```

You should see: `selfie_prod`

---

## Step 6: Setup Directories & Service (2 minutes)

```bash
cd /opt/tanishq

chmod +x setup_production_server.sh

./setup_production_server.sh
```

This creates:
- Directory structure (`/opt/tanishq/storage/...`)
- Systemd service file
- Log directories

---

## Step 7: Set File Permissions (1 minute)

```bash
chmod 644 /opt/tanishq/*.p12
chmod 644 /opt/tanishq/tanishq-prod.war
```

---

## Step 8: Start the Application (2 minutes)

```bash
systemctl daemon-reload

systemctl enable tanishq-prod

systemctl start tanishq-prod
```

---

## Step 9: Monitor Startup (3 minutes)

**Watch the logs:**
```bash
tail -f /opt/tanishq/logs/application.log
```

**Look for these success messages:**
- `Started TanishqSelfieApplication in X seconds`
- `Tomcat started on port(s): 3001`
- `HikariPool-1 - Start completed`

**Press Ctrl+C to stop watching logs**

---

## Step 10: Verify Everything Works (3 minutes)

### Check service status:
```bash
systemctl status tanishq-prod
```
Should show: `active (running)`

### Check port is listening:
```bash
ss -tlnp | grep 3001
```
Should show: `LISTEN` on port 3001

### Check database tables were created:
```bash
mysql -u root -p -e "USE selfie_prod; SHOW TABLES;"
```
Should show multiple tables: `event`, `attendee`, `invitee`, `bride_details`, etc.

### Test from browser:
Open: `https://celebrations.tanishq.co.in/`

---

## ✅ SUCCESS! You're Done!

Your production application is now running with:
- ✅ MySQL database (selfie_prod)
- ✅ AWS S3 storage (celebrations-tanishq-prod)
- ✅ Port 3001
- ✅ Production domain
- ✅ All tables auto-created

---

## 🔧 Troubleshooting

### Application won't start?

**Check logs for errors:**
```bash
tail -100 /opt/tanishq/logs/error.log
```

**Common issues:**

1. **MySQL connection failed**
   - Check password in application-prod.properties is correct
   - Verify MySQL is running: `systemctl status mysqld`
   - Test connection: `mysql -u root -p`

2. **Port already in use**
   - Check what's using port 3001: `ss -tlnp | grep 3001`
   - Stop conflicting service or change port

3. **Database doesn't exist**
   - Run: `mysql -u root -p < setup_production_database.sql` again

4. **Permission denied on .p12 files**
   - Run: `chmod 644 /opt/tanishq/*.p12`

### Check service logs:
```bash
journalctl -u tanishq-prod -n 50 --no-pager
```

### Restart service:
```bash
systemctl restart tanishq-prod
```

### Stop service:
```bash
systemctl stop tanishq-prod
```

---

## 📝 Post-Deployment Checklist

After deployment, verify:

- [ ] Application status shows "active (running)"
- [ ] Port 3001 is listening
- [ ] Database tables exist (10+ tables)
- [ ] Can access website from browser
- [ ] Can create a test event
- [ ] Can upload images (tests S3)
- [ ] QR codes generate correctly
- [ ] No errors in logs
- [ ] Pre-prod still running (didn't break anything!)

---

## 🎯 What Happened Automatically?

When Spring Boot started, it automatically:

1. ✅ **Read** `application-prod.properties` (because you built with `-Pprod`)
2. ✅ **Connected** to MySQL at `localhost:3306/selfie_prod`
3. ✅ **Created** all database tables from your JPA entities:
   - event
   - attendee
   - invitee
   - bride_details
   - greeting
   - rbm_login
   - cee_login
   - product_detail
   - rivaah
   - password_history
4. ✅ **Configured** AWS S3 connection to `celebrations-tanishq-prod`
5. ✅ **Started** web server on port 3001
6. ✅ **Ready** to handle requests!

---

## 📊 Architecture Summary

```
┌──────────────────────────────────────┐
│   Production Server (10.10.63.97)   │
│                                      │
│  ┌────────────────────────────────┐ │
│  │  Spring Boot Application       │ │
│  │  Port: 3001                    │ │
│  │  Profile: prod                 │ │
│  └────────────────────────────────┘ │
│              │                       │
│              ├──────────┬───────────┤
│              ↓          ↓           ↓
│  ┌──────────────┐ ┌─────────┐ ┌────────┐
│  │ MySQL DB     │ │ AWS S3  │ │ Google │
│  │ selfie_prod  │ │ Bucket  │ │ Sheets │
│  └──────────────┘ └─────────┘ └────────┘
└──────────────────────────────────────┘
```

---

## 🎉 Congratulations!

You've successfully deployed to production with:
- MySQL database (migrated from Google Sheets)
- AWS S3 for file storage
- Separate pre-prod and production environments
- Professional deployment setup

**Pre-prod is still running** for testing, and **production is live** for users!

---

## 📞 Quick Commands Reference

| Action | Command |
|--------|---------|
| **Check Status** | `systemctl status tanishq-prod` |
| **View Logs** | `tail -f /opt/tanishq/logs/application.log` |
| **Restart** | `systemctl restart tanishq-prod` |
| **Stop** | `systemctl stop tanishq-prod` |
| **Start** | `systemctl start tanishq-prod` |
| **Check Database** | `mysql -u root -p -e "USE selfie_prod; SHOW TABLES;"` |
| **Check Port** | `ss -tlnp | grep 3001` |

---

**Total Time: ~25 minutes**

**Difficulty: Easy (just follow the steps!)**

**Result: Production deployment complete!** 🚀

