# ✅ DEPLOYMENT CHECKLIST - Copy This!

**Date:** December 3, 2025  
**Error Fixed:** Missing `dechub.bride.upload.dir` and 20+ other properties  
**Status:** Ready to deploy  

---

## 📋 PRE-DEPLOYMENT CHECK

- [x] Configuration fixed in `application-preprod.properties`
- [x] Version updated in `pom.xml` to `03-12-2025-1`
- [ ] **YOU:** Rebuild WAR file
- [ ] **YOU:** Upload files to server
- [ ] **YOU:** Deploy and verify

---

## 1️⃣ BUILD (Your Computer)

**Open Command Prompt:**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean install -DskipTests
```

- [ ] Command executed
- [ ] Wait 3-5 minutes
- [ ] See `BUILD SUCCESS`
- [ ] WAR file created: `target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war`

---

## 2️⃣ UPLOAD EVENT P12 (WinSCP)

**File 1:**
- [ ] Open WinSCP
- [ ] Connect to 10.160.128.94
- [ ] Left panel: Navigate to `src\main\resources\`
- [ ] Find: `event-images-469618-32e65f6d62b3.p12`
- [ ] Right panel: Navigate to `/opt/tanishq/`
- [ ] Drag file from left to right
- [ ] Wait for upload complete

---

## 3️⃣ UPLOAD WAR FILE (WinSCP)

**File 2:**
- [ ] Left panel: Navigate to `target\`
- [ ] Find: `tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war`
- [ ] Right panel: Navigate to `/opt/tanishq/applications_preprod/`
- [ ] Drag file from left to right
- [ ] Wait for upload complete (2-3 minutes)

---

## 4️⃣ PREPARE SERVER (PuTTY)

**Copy-paste these commands:**

```bash
# Create bride uploads directory
mkdir -p /opt/tanishq/storage/bride_uploads

# Create base image placeholder
touch /opt/tanishq/storage/base.jpg

# Set permissions
chmod -R 755 /opt/tanishq/storage
chmod 644 /opt/tanishq/event-images-469618-32e65f6d62b3.p12
```

**Checklist:**
- [ ] All 4 commands executed
- [ ] No errors shown
- [ ] Verify: `ls -la /opt/tanishq/storage/` shows `bride_uploads/` and `base.jpg`

---

## 5️⃣ STOP OLD APP (PuTTY)

```bash
pkill -f tanishq
```

- [ ] Command executed
- [ ] Wait 2 seconds
- [ ] Verify stopped: `ps -ef | grep tanishq` (should show nothing)

---

## 6️⃣ START NEW APP (PuTTY)

```bash
cd /opt/tanishq/applications_preprod
rm -f app.log
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > app.log 2>&1 &
```

- [ ] Commands executed
- [ ] See `[1] <process_id>` (note the number: ________)
- [ ] Process ID saved

---

## 7️⃣ MONITOR STARTUP (PuTTY)

```bash
tail -f app.log
```

**Watch for these lines (in order):**

- [ ] `Starting TanishqApplication...`
- [ ] `Tomcat initialized with port(s): 3002`
- [ ] `Hibernate: create table events...`
- [ ] `Hibernate: create table users...`
- [ ] `Started TanishqApplication in X seconds` ← **MAIN SUCCESS!**

**When you see "Started":**
- [ ] Press `Ctrl+C` (app keeps running)

---

## 8️⃣ VERIFY DATABASE (PuTTY)

```bash
mysql -u root -p selfie_preprod -e "SHOW TABLES;"
```

- [ ] Enter MySQL root password
- [ ] Tables displayed:
  - [ ] events
  - [ ] users
  - [ ] stores
  - [ ] event_attendees
  - [ ] password_history
  - [ ] 10+ more tables

---

## 9️⃣ VERIFY PROCESS (PuTTY)

```bash
ps -ef | grep tanishq
```

- [ ] Java process shown
- [ ] Process includes WAR filename

```bash
netstat -tulpn | grep 3002
```

- [ ] Port 3002 shown as LISTEN
- [ ] Java process ID matches

---

## 🔟 TEST IN BROWSER

**Open browser and test:**

```
http://10.160.128.94:3002
```

- [ ] Homepage loads
- [ ] No 404 error
- [ ] Page displays correctly

**If domain configured:**
```
http://celebrations-preprod.tanishq.co.in
```

- [ ] Domain works
- [ ] Homepage loads

---

## ✅ FINAL VERIFICATION

- [ ] Application running: `ps -ef | grep tanishq`
- [ ] Port listening: `netstat -tulpn | grep 3002`
- [ ] No errors: `tail -100 app.log | grep ERROR` (should be empty)
- [ ] Database has tables: `SHOW TABLES;` works
- [ ] Website accessible in browser
- [ ] Can access login page
- [ ] Can login (if you have test credentials)

---

## 📝 DEPLOYMENT DETAILS (Fill this in)

**Date/Time Started:** _________________  
**Date/Time Completed:** _________________  
**Process ID:** _________________  
**WAR File:** tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war  
**Log File:** /opt/tanishq/applications_preprod/app.log  
**Any Issues:** _________________  
**Resolution:** _________________  

---

## 🆘 IF ERRORS OCCUR

### Build Fails:
```cmd
java -version    # Check Java 11
mvn -version     # Check Maven exists
```

### Upload Fails:
- Check VPN connected
- Check WinSCP connected
- Check server path correct

### App Won't Start:
```bash
cat app.log | grep -A 20 "Caused by"
```

**Common issues:**
- Missing p12 file → Check uploaded
- Wrong path → Check /opt/tanishq/ directory
- Permission issue → Run chmod commands again

### Database Error:
```bash
mysql -u root -p  # Test connection
```

---

## ✅ SUCCESS!

When all checkboxes are checked, you're done! 🎉

**Application Status:** [ ] Running  
**Database Status:** [ ] Tables Created  
**Website Status:** [ ] Accessible  

**Deployment:** [ ] COMPLETE ✅

---

## 📞 QUICK REFERENCE

**Check Logs:**
```bash
tail -f /opt/tanishq/applications_preprod/app.log
```

**Check Process:**
```bash
ps -ef | grep tanishq
```

**Stop App:**
```bash
pkill -f tanishq
```

**Restart App:**
```bash
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > app.log 2>&1 &
```

---

**Print this checklist and check off as you go!** ✅

**Total Time: 12-15 minutes** ⏱️

