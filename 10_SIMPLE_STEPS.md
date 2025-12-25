# 🎯 START HERE - 10 SIMPLE STEPS TO DEPLOY

**Time Needed:** 45 minutes  
**Difficulty:** Easy (just follow steps)

---

## ⚠️ BEFORE YOU START

### Do These 2 Things First:

#### 1️⃣ Update Database Password

**File:** `src\main\resources\application-preprod.properties`

**Find this line (around line 8):**
```
spring.datasource.password=CHANGE_THIS_TO_YOUR_MYSQL_ROOT_PASSWORD
```

**Change it to your actual MySQL root password:**
```
spring.datasource.password=YourActualPassword123
```

**💾 SAVE THE FILE!**

---

#### 2️⃣ Update Build Version

**File:** `pom.xml`

**Find `<artifactId>` (around line 11):**
```xml
<artifactId>tanishq-XX-XX-XXXX-1</artifactId>
```

**Change to today's date:**
```xml
<artifactId>tanishq-02-12-2025-1</artifactId>
```

**💾 SAVE THE FILE!**

---

## 🚀 NOW FOLLOW THESE 10 STEPS

### 📍 STEP 1: Build the Application (5 minutes)

**Open Command Prompt:**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean install
```

**Wait for:** `BUILD SUCCESS`

**You should see:** WAR file created in `target\` folder

---

### 📍 STEP 2: Connect to Server (2 minutes)

**Option 1: If VPN Required**
1. Open FortiClient VPN
2. Connect using your credentials
3. Wait for "Connected"

**Option 2: Direct Connection**
1. Skip if no VPN needed

**Open PuTTY:**
- Host: `10.160.128.94`
- Port: `22`
- Click "Open"
- Login as: `jewdev-test`
- Enter password

---

### 📍 STEP 3: Become Root User (30 seconds)

**In PuTTY, type:**
```bash
sudo su root
```

**Enter password if asked**

**Your prompt should change to:** `[root@...]`

---

### 📍 STEP 4: Create Directories (1 minute)

**In PuTTY, copy and paste ALL these commands:**

```bash
mkdir -p /opt/tanishq/applications_preprod
mkdir -p /opt/tanishq/storage/selfie_images
chmod -R 755 /opt/tanishq/storage
chmod -R 755 /opt/tanishq/applications_preprod
chown -R jewdev-test:jewdev-test /opt/tanishq
ls -la /opt/tanishq/
```

**You should see:**
```
drwxr-xr-x. applications_preprod
drwxr-xr-x. storage
```

✅ **Directories created!**

---

### 📍 STEP 5: Open WinSCP (1 minute)

**Launch WinSCP**

**Connection Settings:**
- File Protocol: `SCP`
- Host Name: `10.160.128.94`
- Port: `22`
- User Name: `jewdev-test`
- Password: (your password or use private key)

**Click:** Login

**You should see:** File browser with two panels

---

### 📍 STEP 6: Upload Google Key File (2 minutes)

**In WinSCP:**

**Left side (your computer):**
1. Navigate to: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\`
2. Find file: `tanishqgmb-5437243a8085.p12`

**Right side (server):**
1. Navigate to: `/opt/tanishq/`

**Drag and drop** the `.p12` file from left to right

**Wait for upload to complete**

**In PuTTY, set permissions:**
```bash
chmod 644 /opt/tanishq/tanishqgmb-5437243a8085.p12
ls -la /opt/tanishq/tanishqgmb-5437243a8085.p12
```

✅ **Google key uploaded!**

---

### 📍 STEP 7: Upload WAR File (3 minutes)

**In WinSCP:**

**Left side (your computer):**
1. Navigate to: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\target\`
2. Find file: `tanishq-02-12-2025-1-0.0.1-SNAPSHOT.war`

**Right side (server):**
1. Navigate to: `/opt/tanishq/applications_preprod/`

**Drag and drop** the WAR file from left to right

**Wait for upload** (may take 1-2 minutes depending on file size)

✅ **WAR file uploaded!**

---

### 📍 STEP 8: Start the Application (2 minutes)

**In PuTTY:**

```bash
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-02-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > app.log 2>&1 &
```

**You should see:** `[1] 12345` (some number = process ID)

✅ **Application starting!**

---

### 📍 STEP 9: Watch Application Start (3 minutes)

**In PuTTY:**

```bash
tail -f app.log
```

**Watch for these messages:**
- `Starting TanishqApplication...`
- `Tomcat initialized with port(s): 3002`
- `Started TanishqApplication in X seconds` ✅

**Press:** `Ctrl+C` to stop watching (application keeps running!)

---

### 📍 STEP 10: Verify Everything Works (5 minutes)

#### A. Check Tables Were Created

**In PuTTY:**
```bash
mysql -u root -p selfie_preprod -e "SHOW TABLES;"
```

**Enter MySQL root password**

**You should see tables like:**
- events
- users
- stores
- event_attendees
- etc.

✅ **Database tables created!**

---

#### B. Check Application is Running

**In PuTTY:**
```bash
ps -ef | grep tanishq
```

**You should see:** Process running

✅ **Application is running!**

---

#### C. Test in Browser

**Open your web browser and go to:**
```
http://10.160.128.94:3002
```

**Or if domain is configured:**
```
http://celebrations-preprod.tanishq.co.in
```

**You should see:** Your application homepage!

✅ **Application is accessible!**

---

## 🎉 CONGRATULATIONS!

### You're Done! Your Pre-Prod Environment is Live!

**What you accomplished:**
1. ✅ Built application WAR file
2. ✅ Created server directories
3. ✅ Uploaded all necessary files
4. ✅ Started application successfully
5. ✅ Database tables created automatically
6. ✅ Application is running and accessible

---

## 📝 IMPORTANT INFORMATION TO SAVE

**Write these down:**

**Process ID:** `_____________` (from step 8)

**WAR File:** `tanishq-02-12-2025-1-0.0.1-SNAPSHOT.war`

**Log File:** `/opt/tanishq/applications_preprod/app.log`

**Application URL:** `http://celebrations-preprod.tanishq.co.in`

**Port:** `3002`

---

## 🔧 USEFUL COMMANDS FOR LATER

### View Application Logs:
```bash
tail -f /opt/tanishq/applications_preprod/app.log
```

### Check if Application is Running:
```bash
ps -ef | grep tanishq
```

### Stop Application:
```bash
ps -ef | grep tanishq
kill <process_id>
```

### Restart Application:
```bash
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-02-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > app.log 2>&1 &
```

### Check Database:
```bash
mysql -u root -p selfie_preprod
```

### View Tables:
```bash
mysql -u root -p selfie_preprod -e "SHOW TABLES;"
```

---

## 🆘 IF SOMETHING GOES WRONG

### Application Won't Start?

**Check logs:**
```bash
tail -100 /opt/tanishq/applications_preprod/app.log
```

**Common issues:**
1. Wrong database password → Fix in `application-preprod.properties`
2. Port 3002 already in use → Kill old process
3. Google .p12 file missing → Upload it again

---

### Can't Access in Browser?

**Check:**
1. Application is running: `ps -ef | grep tanishq`
2. Port is open: `netstat -tulpn | grep 3002`
3. Contact network team about domain/firewall

---

### Database Connection Error?

**Check:**
1. Password in `application-preprod.properties` is correct
2. Database exists: `mysql -u root -p -e "SHOW DATABASES;"`
3. Can connect manually: `mysql -u root -p selfie_preprod`

---

## 🔄 FOR NEXT DEPLOYMENT

**When you need to deploy again (future updates):**

1. Update version in `pom.xml`: `tanishq-02-12-2025-2`
2. Build: `mvn clean install`
3. Upload new WAR to server via WinSCP
4. Stop old process: `kill <process_id>`
5. Start new process: `nohup java -jar <new-war-file>...`

**That's it! Much faster next time!**

---

## ✅ FINAL CHECKLIST

Make sure you did:

- [x] Updated database password in properties file
- [x] Updated pom.xml artifactId
- [x] Built WAR file successfully
- [x] Created directories on server
- [x] Uploaded Google .p12 file
- [x] Uploaded WAR file
- [x] Started application
- [x] Verified tables created
- [x] Tested in browser
- [x] Saved important information

---

**🎊 Well done! Your pre-prod environment is ready for testing! 🎊**

**Share the URL with your testing team:**
`http://celebrations-preprod.tanishq.co.in`

---

**Need help?** Check:
- `CURRENT_STATUS_AND_NEXT_STEPS.md` - Overview
- `YOUR_ACTION_PLAN_NOW.md` - Detailed steps
- `PREPROD_SETUP_FROM_SCRATCH.md` - Complete guide

**Everything is documented! Good luck! 🚀**

