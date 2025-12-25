# 📋 BUILD & DEPLOY CHECKLIST - PRINT THIS!

**Project:** Tanishq Celebration App - Pre-Production  
**URL:** http://celebrationsite-preprod.tanishq.co.in  
**Server:** 10.160.128.94:3000  
**Date:** _________________

---

## ✅ PRE-DEPLOYMENT CHECKLIST

### **Before You Start:**
- [ ] Code changes completed and saved
- [ ] Java 11+ installed (`java -version`)
- [ ] Maven installed (`mvn -version`)
- [ ] WinSCP or FileZilla installed
- [ ] SSH client (PuTTY) installed
- [ ] Server credentials available

---

## 🏗️ BUILD PHASE (Windows)

### **Step 1: Navigate to Project**
```
Location: C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
```
- [ ] Opened Command Prompt in project folder

### **Step 2: Clean Build**
```cmd
mvn clean
```
- [ ] Clean successful (no errors)

### **Step 3: Build Application**
```cmd
mvn install -DskipTests
```
- [ ] Build shows "BUILD SUCCESS"
- [ ] Time taken: _______ minutes

### **Step 4: Verify WAR File**
```
Location: target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
```
- [ ] WAR file exists
- [ ] File size: _______ MB (should be 50-80 MB)

---

## 📤 TRANSFER PHASE

### **Step 5: Connect to Server**
```
WinSCP Details:
- Host: 10.160.128.94
- User: root
- Port: 22
- Password: [admin provided]
```
- [ ] WinSCP connection successful
- [ ] Can see `/opt/tanishq/applications_preprod/` folder

### **Step 6: Upload WAR File**
```
From: C:\JAVA\...\target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
To:   /opt/tanishq/applications_preprod/
```
- [ ] Upload started
- [ ] Upload progress: ________________
- [ ] Upload completed successfully
- [ ] File size on server matches local: _______ MB

---

## 🚀 DEPLOYMENT PHASE (Server)

### **Step 7: SSH to Server**
```
ssh root@10.160.128.94
Password: [enter when prompted]
```
- [ ] SSH connection successful
- [ ] Logged in as root

### **Step 8: Navigate to Deploy Directory**
```bash
cd /opt/tanishq/applications_preprod
pwd
```
- [ ] Current directory: `/opt/tanishq/applications_preprod`

### **Step 9: Stop Old Application**
```bash
pkill -9 -f tanishq-preprod
sleep 3
ps aux | grep java
```
- [ ] Old application stopped
- [ ] No Java processes running (or confirmed it's stopped)

### **Step 10: Backup (Optional but Recommended)**
```bash
mkdir -p backup_$(date +%Y%m%d)
cp tanishq-preprod*.war backup_$(date +%Y%m%d)/ 2>/dev/null || true
cp *.log backup_$(date +%Y%m%d)/ 2>/dev/null || true
```
- [ ] Backup created

### **Step 11: Deploy New Application**
```bash
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &
```
- [ ] Command executed
- [ ] PID shown: _________________

### **Step 12: Wait for Startup**
```bash
sleep 30
```
- [ ] Waited 30 seconds

---

## ✅ VERIFICATION PHASE

### **Step 13: Check Process**
```bash
ps aux | grep java | grep tanishq
```
- [ ] Process is running
- [ ] PID: _________________

### **Step 14: Check Port**
```bash
netstat -tlnp | grep 3000
```
- [ ] Port 3000 is listening

### **Step 15: Check Logs**
```bash
tail -50 application.log
```
- [ ] No errors in logs
- [ ] Logs show "Started TanishqSelfieApplication" or similar

### **Step 16: Test Localhost**
```bash
curl http://localhost:3000
```
- [ ] Returns HTML (status 200 or 302)

### **Step 17: Test IP**
```bash
curl http://10.160.128.94:3000
```
- [ ] Returns HTML (status 200 or 302)

### **Step 18: Wait for ELB Health Check**
```
Wait 60 seconds for AWS Load Balancer to detect healthy target
```
- [ ] Waited full 60 seconds
- [ ] Current time: _________________

### **Step 19: Test Domain**
```bash
curl http://celebrationsite-preprod.tanishq.co.in
```
- [ ] Returns HTML (not 502)
- [ ] HTTP Status: _________________

---

## 🌐 BROWSER TESTING

### **Step 20: Open in Browser**
```
URL: http://celebrationsite-preprod.tanishq.co.in
```
- [ ] Page loads successfully
- [ ] No console errors (F12)
- [ ] Page shows: "Let's Celebrate with Tanishq" or login page

### **Step 21: Test Login**
```
Test credentials: [Use actual test account]
```
- [ ] Login form visible
- [ ] Can enter credentials
- [ ] Login successful
- [ ] Redirected to correct page

### **Step 22: Test Key Features**
```
Test based on your changes:
```
- [ ] Feature 1: ___________________________ ✅ ❌
- [ ] Feature 2: ___________________________ ✅ ❌
- [ ] Feature 3: ___________________________ ✅ ❌
- [ ] Feature 4: ___________________________ ✅ ❌

---

## 📊 FINAL VERIFICATION

### **Step 23: Server Health**
```bash
# On server, run:
ps aux | grep java
netstat -tlnp | grep 3000
tail -20 application.log
```
- [ ] Process running: YES / NO
- [ ] Port listening: YES / NO
- [ ] Logs clean: YES / NO

### **Step 24: Database Check**
```bash
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores;"
```
- [ ] Database connected: YES / NO
- [ ] Store count: _________________

### **Step 25: Final Browser Check**
```
http://celebrationsite-preprod.tanishq.co.in
```
- [ ] Accessible: YES / NO
- [ ] HTTP Status: _________________
- [ ] All features working: YES / NO

---

## 📝 POST-DEPLOYMENT NOTES

**Deployment Date/Time:** _______________________________________

**Deployed By:** _______________________________________________

**Changes Made:**
- _________________________________________________________________
- _________________________________________________________________
- _________________________________________________________________

**Issues Encountered:**
- _________________________________________________________________
- _________________________________________________________________

**Resolution:**
- _________________________________________________________________
- _________________________________________________________________

**Testing Results:**
- _________________________________________________________________
- _________________________________________________________________

**Sign-off:** ___________________________________________________

---

## 🆘 ROLLBACK PROCEDURE (IF NEEDED)

If deployment fails and you need to rollback:

### **Emergency Rollback Steps:**

```bash
# 1. SSH to server
ssh root@10.160.128.94

# 2. Stop current application
pkill -9 -f tanishq-preprod

# 3. Navigate to deployment directory
cd /opt/tanishq/applications_preprod

# 4. Find latest backup
ls -lht backup_*/

# 5. Copy old WAR file back
cp backup_YYYYMMDD_HHMMSS/tanishq-preprod*.war .

# 6. Start old version
nohup java -jar tanishq-preprod*.war \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

# 7. Verify
sleep 30
curl http://localhost:3000
```

**Rollback Executed:** YES / NO  
**Rollback Time:** _________________  
**Reason:** _________________________________________________________________

---

## ✅ SUCCESS CRITERIA

**Deployment is successful when ALL of these are true:**

- ✅ Application process running on server
- ✅ Port 3000 listening
- ✅ `curl http://localhost:3000` returns HTML
- ✅ `curl http://10.160.128.94:3000` returns HTML
- ✅ `curl http://celebrationsite-preprod.tanishq.co.in` returns HTML (not 502)
- ✅ Browser can access domain
- ✅ Login works
- ✅ All key features tested and working
- ✅ No errors in application.log
- ✅ Database connected

**If ALL checked:** 🎉 **DEPLOYMENT SUCCESSFUL!** 🎉

---

## 📞 EMERGENCY CONTACTS

**AWS Team:** ___________________________________________________

**Database Admin:** _____________________________________________

**Network Team:** _______________________________________________

**Project Lead:** _______________________________________________

---

## 🔖 USEFUL COMMANDS

**Build:**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean install -DskipTests
```

**Deploy:**
```bash
cd /opt/tanishq/applications_preprod
pkill -9 -f tanishq-preprod
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war --server.port=3000 --spring.profiles.active=preprod > application.log 2>&1 &
```

**Check:**
```bash
ps aux | grep java
netstat -tlnp | grep 3000
tail -f application.log
curl http://celebrationsite-preprod.tanishq.co.in
```

---

**Print this checklist and use it for every deployment!**

**Version:** 1.0  
**Last Updated:** December 5, 2025

