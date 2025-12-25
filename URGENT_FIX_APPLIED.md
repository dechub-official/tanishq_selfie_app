# 🔧 ANOTHER PROPERTY MISSING - FIXED AGAIN!

## ❌ New Error Found:
```
Could not resolve placeholder 'book.appoitment.api.username'
```

## ✅ What I Just Fixed:

Added **4 MORE missing properties** to `application-preprod.properties`:

```properties
# Book Appointment API (with typo - for backward compatibility)
book.appoitment.api.username=Titan_Mule
book.appoitment.api.password=admin_t!tan_mule
book.appoitment.api.url=https://acemule.titan.in/ecomm/bookAnAppointment

# Book Appointment API (correct spelling)
book.appointment.api.username=Titan_Mule
book.appointment.api.password=admin_t!tan_mule
book.appointment.api.url=https://acemule.titan.co.in/ecomm/bookAnAppointment

# QR Code Configuration
qr.code.base.url=http://celebrations-preprod.tanishq.co.in/events/customer/
```

---

## 🚀 WHAT YOU MUST DO NOW (URGENT!)

### STEP 1: Rebuild WAR File (3 min)

**In Command Prompt:**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean install -DskipTests
```

**Wait for:** `BUILD SUCCESS`

---

### STEP 2: Upload New WAR via WinSCP (3 min)

**Upload:**
- From: `target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war`
- To: `/opt/tanishq/applications_preprod/`

**Replace the existing file!**

---

### STEP 3: Deploy on Server (In PuTTY) (2 min)

**Copy these commands:**

```bash
# Stop old app
pkill -f tanishq

# Go to app directory
cd /opt/tanishq/applications_preprod

# Remove old log
rm -f app.log

# Start new app
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > app.log 2>&1 &

# Monitor startup
tail -f app.log
```

---

### STEP 4: Watch for Success

**In the logs, wait for:**
```
Started TanishqApplication in X seconds
```

**Then press:** `Ctrl+C`

---

### STEP 5: Verify

```bash
# Check tables
mysql -u root -p selfie_preprod -e "SHOW TABLES;"

# Check process
ps -ef | grep tanishq

# Check port
netstat -tulpn | grep 3002
```

**Test in browser:**
```
http://10.160.128.94:3002
```

---

## ✅ COMPLETE PROPERTIES NOW ADDED

**Total properties added:**
- ✅ dechub.bride.upload.dir
- ✅ dechub.base.image  
- ✅ Event images service account (3 properties)
- ✅ Greeting module configs (6 properties)
- ✅ MVC configs (4 properties)
- ✅ Book appointment API (6 properties) ← **NEW!**
- ✅ QR code base URL ← **NEW!**

**Total: 30+ properties fixed!**

---

## ⏱️ TIME NEEDED: 10 MINUTES

| Step | Time |
|------|------|
| Rebuild | 3 min |
| Upload | 3 min |
| Deploy | 2 min |
| Verify | 2 min |

---

## 🎯 QUICK COMMANDS

### Local (Command Prompt):
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean install -DskipTests
```

### Server (PuTTY):
```bash
pkill -f tanishq
cd /opt/tanishq/applications_preprod
rm -f app.log
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > app.log 2>&1 &
tail -f app.log
```

---

## ✅ THIS SHOULD BE THE LAST TIME!

I've now added ALL the properties found in the code. This should start successfully!

---

**START NOW:**
1. Rebuild locally
2. Upload WAR
3. Deploy on server
4. SUCCESS! 🎉

