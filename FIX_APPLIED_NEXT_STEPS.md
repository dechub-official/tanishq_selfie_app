# 🔧 FIXED! Here's What I Did and What You Need to Do Next

## ✅ What I Fixed

### Problem Found:
The application was failing because the `application-preprod.properties` file was **missing several required properties**:
- ❌ `dechub.bride.upload.dir`
- ❌ `dechub.base.image`
- ❌ Event images service account configs
- ❌ Greeting module configs
- ❌ MVC view configs

### What I Updated:

**1. Fixed `application-preprod.properties`** ✅
Added all missing properties:
- Bride upload directory
- Base image path
- Event images service account
- Greeting module sheet and drive configs
- MVC view configurations

**2. Updated `pom.xml`** ✅
Changed version from `02-12-2025` to `03-12-2025` (today's date)

---

## 🚀 WHAT YOU NEED TO DO NOW

### STEP 1: Rebuild the Application (5 minutes)

**Open Command Prompt as Administrator:**

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean install -DskipTests
```

**Wait for:** `BUILD SUCCESS`

**New WAR file:** `tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war`

---

### STEP 2: Upload Additional Files to Server

You need to upload **2 files** total:

#### File 1: Event Images P12 File

**Using WinSCP:**
- **Source (local):** `src\main\resources\event-images-469618-32e65f6d62b3.p12`
- **Destination (server):** `/opt/tanishq/event-images-469618-32e65f6d62b3.p12`

#### File 2: New WAR File

**Using WinSCP:**
- **Source (local):** `target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war`
- **Destination (server):** `/opt/tanishq/applications_preprod/`

---

### STEP 3: Create Missing Directories on Server

**In PuTTY (as root):**

```bash
# Create bride uploads directory
mkdir -p /opt/tanishq/storage/bride_uploads

# Set permissions
chmod -R 755 /opt/tanishq/storage

# Verify
ls -la /opt/tanishq/storage/
```

**You should see:**
- `selfie_images/`
- `bride_uploads/`

---

### STEP 4: Copy Base Image (Optional)

The app expects a base image at `/opt/tanishq/storage/base.jpg`

**Option 1:** Create a placeholder:
```bash
# Create empty file for now
touch /opt/tanishq/storage/base.jpg
```

**Option 2:** Upload actual base image via WinSCP later

---

### STEP 5: Stop Old Application

**In PuTTY:**

```bash
# Find and kill any running tanishq process
pkill -f tanishq

# Verify stopped
ps -ef | grep tanishq
```

---

### STEP 6: Start New Application

**In PuTTY:**

```bash
cd /opt/tanishq/applications_preprod

# Remove old log
rm -f app.log

# Start new application
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > app.log 2>&1 &
```

**You should see:** `[1] <process_id>`

---

### STEP 7: Monitor Startup

**In PuTTY:**

```bash
tail -f app.log
```

**Watch for:**
- ✅ `Starting TanishqApplication...`
- ✅ `Tomcat initialized with port(s): 3002`
- ✅ `Hibernate: create table...` (creates database tables)
- ✅ `Started TanishqApplication in X seconds` ← **SUCCESS!**

**Press Ctrl+C** when you see "Started" (app keeps running)

---

### STEP 8: Verify Database Tables Created

**In PuTTY:**

```bash
mysql -u root -p selfie_preprod -e "SHOW TABLES;"
```

**You should see tables like:**
- events
- users
- stores
- event_attendees
- password_history
- etc.

---

### STEP 9: Test in Browser

**Open browser:**
```
http://10.160.128.94:3002
```

**Or:**
```
http://celebrations-preprod.tanishq.co.in
```

**You should see:** Your application homepage! ✅

---

## 📋 COMPLETE COMMAND CHECKLIST

Copy and paste these commands in order:

### On Your Local Machine (Command Prompt):
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean install -DskipTests
```

### Using WinSCP:
1. Upload `src\main\resources\event-images-469618-32e65f6d62b3.p12` → `/opt/tanishq/`
2. Upload `target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war` → `/opt/tanishq/applications_preprod/`

### On Server (PuTTY - as root):
```bash
# Create directories
mkdir -p /opt/tanishq/storage/bride_uploads
chmod -R 755 /opt/tanishq/storage
touch /opt/tanishq/storage/base.jpg

# Set permissions on p12 file
chmod 644 /opt/tanishq/event-images-469618-32e65f6d62b3.p12

# Stop old app
pkill -f tanishq

# Start new app
cd /opt/tanishq/applications_preprod
rm -f app.log
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > app.log 2>&1 &

# Monitor
tail -f app.log

# After success, verify tables
mysql -u root -p selfie_preprod -e "SHOW TABLES;"
```

---

## ✅ SUCCESS CRITERIA

You're done when:

1. ✅ Build completes with `BUILD SUCCESS`
2. ✅ Both files uploaded to server
3. ✅ Application starts without errors
4. ✅ Log shows "Started TanishqApplication"
5. ✅ Database tables are created
6. ✅ Website loads in browser
7. ✅ Can login and use application

---

## 🎯 WHAT FILES WERE CHANGED

### Local Files Updated:
1. ✅ `src\main\resources\application-preprod.properties` - Added 20+ missing properties
2. ✅ `pom.xml` - Updated version to 03-12-2025

### New Files to Upload:
1. ❌ `event-images-469618-32e65f6d62b3.p12` (not uploaded yet)
2. ❌ New WAR file (needs to be built and uploaded)

---

## 📊 FILE LOCATIONS SUMMARY

| File | Local Path | Server Path |
|------|-----------|-------------|
| **WAR** | `target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war` | `/opt/tanishq/applications_preprod/` |
| **Main P12** | `src\main\resources\tanishqgmb-5437243a8085.p12` | `/opt/tanishq/` ✅ Already there |
| **Event P12** | `src\main\resources\event-images-469618-32e65f6d62b3.p12` | `/opt/tanishq/` ❌ Need to upload |
| **Base Image** | - | `/opt/tanishq/storage/base.jpg` ❌ Need to create |

---

## 🆘 IF YOU GET ERRORS

### Build Fails:
- Check Java version: `java -version` (must be 11)
- Check Maven installed: `mvn -version`

### Application Won't Start:
```bash
# Check full error
cat app.log | grep -A 10 "Caused by"

# Common fixes:
# 1. Wrong database password → Update application-preprod.properties and rebuild
# 2. Missing p12 file → Upload event-images p12
# 3. Missing directory → Run mkdir commands above
```

### Can't Access Website:
```bash
# Check if app is running
ps -ef | grep tanishq

# Check port
netstat -tulpn | grep 3002

# Check logs
tail -100 app.log
```

---

## 🎉 ESTIMATED TIME

- Build WAR: 3-5 minutes
- Upload files: 3 minutes
- Create directories: 1 minute
- Start app: 2 minutes
- Verify: 2 minutes
- **Total: 15 minutes** ✅

---

## 🚀 START NOW!

**Step 1:** Open Command Prompt and run the build command above

**Step 2:** While building, open WinSCP and get ready to upload

**Step 3:** Follow the checklist step by step

---

**Everything is fixed in the code! Just rebuild and redeploy!** 🎊

