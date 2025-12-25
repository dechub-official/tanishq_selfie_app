# 🎯 YOUR IMMEDIATE ACTION PLAN - Pre-Prod Setup

**Current Status:**
✅ Database created: `selfie_preprod` on server 10.160.128.94  
✅ Database is empty (no tables yet)  
✅ Server user: `jewdev-test`  
✅ All data is on localhost only  

---

## 📍 DECISION POINT: Do You Need to Migrate Local Data?

### Option A: Start Fresh (Recommended for Pre-Prod)
**Choose this if:**
- You want a clean pre-prod environment
- Local data is just test data
- You'll create new data for testing

**Advantages:**
- Simpler and faster
- No risk of data migration issues
- Application will create all tables automatically

### Option B: Migrate Local Data
**Choose this if:**
- You have important data on localhost
- You need existing events/users/stores in pre-prod
- You want pre-prod to have same data as local

**Time:** Additional 30-60 minutes

---

## 🚀 OPTION A: START FRESH (RECOMMENDED)

### What You Need to Do RIGHT NOW:

#### 1. Update Database Password (2 minutes)

**File:** `src\main\resources\application-preprod.properties`

Find this line:
```
spring.datasource.password=CHANGE_THIS_TO_YOUR_MYSQL_ROOT_PASSWORD
```

Replace with your actual MySQL root password that you use on the server.

**Example:**
```
spring.datasource.password=YourActualMySQLPassword123
```

**Save the file!**

---

#### 2. Update pom.xml (1 minute)

**File:** `pom.xml`

Find the `<artifactId>` line and update to today's date:

```xml
<artifactId>tanishq-02-12-2025-1</artifactId>
```

**Save the file!**

---

#### 3. Copy Frontend Build Files (5 minutes)

If you have a separate frontend project:

1. Go to your frontend folder
2. Run build: `npm run build`
3. Copy ALL files from `build/` or `dist/` folder
4. Paste into: `src\main\resources\static\`

**Or if frontend is already in static folder, skip this step!**

---

#### 4. Build the WAR File (5-10 minutes)

Open Command Prompt in your project folder:

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
```

Run the build:

```cmd
mvn clean install
```

**Wait for:** `BUILD SUCCESS`

**Verify:** Check `target\` folder for WAR file  
**Example:** `tanishq-02-12-2025-1-0.0.1-SNAPSHOT.war`

---

#### 5. Server Setup - Create Directories (10 minutes)

**In PuTTY (connected as jewdev-test):**

```bash
# Switch to root
sudo su root

# Create directories
mkdir -p /opt/tanishq/applications_preprod
mkdir -p /opt/tanishq/storage/selfie_images

# Set permissions
chmod -R 755 /opt/tanishq/storage
chmod -R 755 /opt/tanishq/applications_preprod

# Set ownership to your user
chown -R jewdev-test:jewdev-test /opt/tanishq

# Verify
ls -la /opt/tanishq/
```

**You should see:**
```
drwxr-xr-x. applications_preprod
drwxr-xr-x. storage
```

---

#### 6. Upload Google Service Key File (5 minutes)

**Using WinSCP:**

1. Connect to: `10.160.128.94`
2. Username: `jewdev-test`
3. Navigate to: `/opt/tanishq/`
4. Upload file: `tanishqgmb-5437243a8085.p12`
   - From local: `src\main\resources\tanishqgmb-5437243a8085.p12`

**In PuTTY:**
```bash
sudo chmod 644 /opt/tanishq/tanishqgmb-5437243a8085.p12
ls -la /opt/tanishq/tanishqgmb-5437243a8085.p12
```

---

#### 7. Upload WAR File (5 minutes)

**Using WinSCP:**

1. Navigate to: `/opt/tanishq/applications_preprod/`
2. Upload: `tanishq-02-12-2025-1-0.0.1-SNAPSHOT.war`
   - From: `target\tanishq-02-12-2025-1-0.0.1-SNAPSHOT.war`

---

#### 8. Start Application (5 minutes)

**In PuTTY:**

```bash
cd /opt/tanishq/applications_preprod

# Start the application
nohup java -jar tanishq-02-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > tanishq-02-12-2025-1.log 2>&1 &

# Note the process ID shown
```

**Monitor startup:**
```bash
tail -f tanishq-02-12-2025-1.log
```

**Wait for:**
- `Started TanishqApplication in X seconds`
- `Tomcat started on port(s): 3002`

**Press Ctrl+C** (application keeps running in background)

---

#### 9. Verify Tables Created (2 minutes)

**In PuTTY:**

```bash
mysql -u root -p selfie_preprod -e "SHOW TABLES;"
```

**You should see tables like:**
- events
- users
- stores
- event_attendees
- etc.

**If tables are there = SUCCESS!** ✅

---

#### 10. Test Application (5 minutes)

**In your browser:**

```
http://10.160.128.94:3002
```

**Or if domain is configured:**
```
http://celebrations-preprod.tanishq.co.in
```

**Test:**
- Homepage loads
- Can access login page
- Try logging in

---

## 📊 OPTION B: MIGRATE LOCAL DATA

### Additional Steps if You Want to Copy Local Data:

#### Step 1: Export Local Database (5 minutes)

**On your local machine:**

```cmd
mysqldump -u root -p tanishq_local > C:\temp\tanishq_preprod_data.sql
```

**Replace `tanishq_local` with your actual local database name!**

---

#### Step 2: Upload SQL File to Server (3 minutes)

**Using WinSCP:**

1. Upload `tanishq_preprod_data.sql` to `/tmp/` on server

---

#### Step 3: Import to Server Database (5 minutes)

**In PuTTY:**

```bash
mysql -u root -p selfie_preprod < /tmp/tanishq_preprod_data.sql
```

Enter MySQL root password when prompted.

**Verify:**
```bash
mysql -u root -p selfie_preprod -e "SHOW TABLES;"
mysql -u root -p selfie_preprod -e "SELECT COUNT(*) FROM events;"
```

**Then continue with OPTION A steps 1-10!**

---

## ✅ QUICK CHECKLIST

### Before Starting:
- [ ] Know your MySQL root password on server
- [ ] Have WinSCP installed
- [ ] Have PuTTY connected to server
- [ ] Have FortiClient VPN connected (if required)

### Option A - Fresh Start:
- [ ] 1. Update database password in application-preprod.properties
- [ ] 2. Update pom.xml artifactId
- [ ] 3. Copy frontend build files (if needed)
- [ ] 4. Run: mvn clean install
- [ ] 5. Create directories on server
- [ ] 6. Upload Google .p12 file
- [ ] 7. Upload WAR file
- [ ] 8. Start application
- [ ] 9. Verify tables created
- [ ] 10. Test in browser

### Option B - Migrate Data (in addition to above):
- [ ] Export local database
- [ ] Upload SQL file to server
- [ ] Import to selfie_preprod database

---

## 🆘 COMMON ISSUES

### Issue: "Access denied for user 'root'@'localhost'"
**Solution:** Check password in `application-preprod.properties` matches your MySQL root password

### Issue: Application won't start
**Solution:** 
```bash
tail -100 tanishq-02-12-2025-1.log
```
Look for error messages and fix accordingly

### Issue: Can't create directories
**Solution:** Make sure you're root: `sudo su root`

### Issue: Port 3002 already in use
**Solution:**
```bash
netstat -tulpn | grep 3002
kill <process_id>
```

---

## 📞 IMPORTANT COMMANDS

### Check if application is running:
```bash
ps -ef | grep tanishq
```

### Check application logs:
```bash
tail -f /opt/tanishq/applications_preprod/tanishq-02-12-2025-1.log
```

### Stop application:
```bash
ps -ef | grep tanishq
kill <process_id>
```

### Restart application:
```bash
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-02-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > tanishq-02-12-2025-1.log 2>&1 &
```

### Check database:
```bash
mysql -u root -p selfie_preprod
```

---

## ⏰ ESTIMATED TIME

**Option A (Fresh Start):** 45-60 minutes total  
**Option B (With Data Migration):** 90-120 minutes total

---

## 🎯 YOUR NEXT STEP RIGHT NOW:

1. **Decide:** Option A (fresh start) or Option B (migrate data)?

2. **If Option A:** Go to step 1 and start!

3. **If Option B:** Export database first, then follow all steps!

---

**Start with updating the password in `application-preprod.properties` NOW!**

**Then follow the numbered steps one by one!**

**Good luck! 🚀**

