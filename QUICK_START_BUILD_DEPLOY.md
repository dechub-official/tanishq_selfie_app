# 🚀 QUICK START - BUILD & DEPLOY IN 5 MINUTES

**Last Updated:** December 5, 2025  
**Target:** http://celebrationsite-preprod.tanishq.co.in

---

## 📋 TL;DR - COMPLETE WORKFLOW

```
1. Make changes to code
2. Double-click BUILD_PREPROD.bat
3. Upload WAR file to server using WinSCP
4. SSH to server and run deployment script
5. Test in browser
```

**Total Time:** 5-10 minutes

---

## 🎯 STEP-BY-STEP GUIDE

### **STEP 1: Make Your Code Changes** ⏱️ 5-30 min

**Edit files in:**
- `src/main/java/com/dechub/tanishq/controller/` - For API changes
- `src/main/resources/templates/` - For HTML changes
- `src/main/resources/application-preprod.properties` - For config changes

**Save all files.**

---

### **STEP 2: Build the Application** ⏱️ 2-3 min

**Option A: Easy Way (Double-Click)**
```
1. Go to: C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\
2. Double-click: BUILD_PREPROD.bat
3. Wait for "BUILD SUCCESSFUL"
4. Note the WAR file location shown
```

**Option B: Command Line**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean install -DskipTests
```

**Result:**
```
✅ WAR file created in: target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
```

---

### **STEP 3: Upload to Server** ⏱️ 1-2 min

**Using WinSCP:**

1. **Open WinSCP**

2. **Connect:**
   ```
   Host:     10.160.128.94
   User:     root
   Password: [Ask admin]
   Port:     22
   ```

3. **Transfer:**
   - Left panel: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\target\`
   - Right panel: `/opt/tanishq/applications_preprod/`
   - Drag: `tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war` from left to right

4. **Wait:** Until transfer shows 100% complete

---

### **STEP 4: Deploy on Server** ⏱️ 1-2 min

**Method A: Using Deployment Script (Recommended)**

```bash
# 1. SSH to server
ssh root@10.160.128.94

# 2. Navigate to deployment directory
cd /opt/tanishq/applications_preprod

# 3. Download the deployment script (if not already there)
# Upload DEPLOY_ON_SERVER.sh from your project to server using WinSCP

# 4. Make it executable
chmod +x DEPLOY_ON_SERVER.sh

# 5. Run it
./DEPLOY_ON_SERVER.sh
```

**Method B: Quick Manual Deploy**

```bash
# SSH to server
ssh root@10.160.128.94

# Quick deploy (copy entire block)
cd /opt/tanishq/applications_preprod
pkill -9 -f tanishq-preprod
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

# Wait 30 seconds
sleep 30

# Check status
ps aux | grep java | grep tanishq
tail -50 application.log
```

---

### **STEP 5: Test** ⏱️ 1 min

**On Server:**
```bash
# Test locally
curl http://localhost:3000

# Should return HTML
```

**In Browser:**
```
1. Wait 60 seconds for AWS ELB health check
2. Open: http://celebrationsite-preprod.tanishq.co.in
3. Test login and features
4. Done! ✅
```

---

## 🔥 COMMON SCENARIOS

### **Scenario 1: Changed Login API**

```
1. Edit: src/main/java/com/dechub/tanishq/controller/EventsController.java
2. Run: BUILD_PREPROD.bat
3. Upload WAR via WinSCP
4. Deploy on server
5. Test: http://celebrationsite-preprod.tanishq.co.in/events/login
```

### **Scenario 2: Changed Frontend Page**

```
1. Edit: src/main/resources/templates/your-page.html
2. Run: BUILD_PREPROD.bat
3. Upload WAR via WinSCP
4. Deploy on server
5. Test in browser
```

### **Scenario 3: Changed Database Settings**

```
1. Edit: src/main/resources/application-preprod.properties
2. Run: BUILD_PREPROD.bat
3. Upload WAR via WinSCP
4. Deploy on server
5. Check logs: tail -f application.log
```

---

## 📁 IMPORTANT FILES

| File | What It Does |
|------|-------------|
| `BUILD_PREPROD.bat` | Builds the application on Windows |
| `DEPLOY_ON_SERVER.sh` | Deploys the application on server |
| `COMPLETE_BUILD_AND_DEPLOY_GUIDE.md` | Full detailed documentation |
| `pom.xml` | Maven build configuration |
| `src/main/resources/application-preprod.properties` | Pre-prod configuration |

---

## 🆘 TROUBLESHOOTING

### **Build Failed**

```cmd
# Clean Maven cache
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean
mvn install -DskipTests -X
```

### **Upload Failed**

```
- Check server is reachable: ping 10.160.128.94
- Check credentials are correct
- Try different tool (FileZilla instead of WinSCP)
```

### **Deployment Failed**

```bash
# Check logs
tail -100 application.log

# Check port
netstat -tlnp | grep 3000

# Restart
pkill -f tanishq-preprod
./DEPLOY_ON_SERVER.sh
```

### **Domain Shows 502**

```bash
# Check app is running locally
curl http://localhost:3000

# If works locally, wait 2 minutes for ELB
# Then contact AWS team if still failing
```

---

## ✅ SUCCESS CHECKLIST

**After deployment, verify:**

- [ ] `ps aux | grep java` shows process running
- [ ] `netstat -tlnp | grep 3000` shows port listening
- [ ] `curl http://localhost:3000` returns HTML
- [ ] `curl http://10.160.128.94:3000` returns HTML
- [ ] Browser shows: http://celebrationsite-preprod.tanishq.co.in
- [ ] Login works
- [ ] All features tested

---

## 🎯 QUICK REFERENCE

### **Build on Windows**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean install -DskipTests
```

### **Deploy on Server**
```bash
cd /opt/tanishq/applications_preprod
pkill -9 -f tanishq-preprod
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war --server.port=3000 --spring.profiles.active=preprod > application.log 2>&1 &
```

### **Check Status**
```bash
ps aux | grep java
netstat -tlnp | grep 3000
tail -f application.log
```

### **Test**
```bash
curl http://localhost:3000
curl http://celebrationsite-preprod.tanishq.co.in
```

---

## 📞 NEED HELP?

1. **Check logs:** `tail -f /opt/tanishq/applications_preprod/application.log`
2. **Read full guide:** `COMPLETE_BUILD_AND_DEPLOY_GUIDE.md`
3. **Search for error:** Look in troubleshooting section

---

## 🎉 YOU'RE DONE!

**If you see:**
- ✅ Application running
- ✅ Domain accessible
- ✅ Login working
- ✅ No errors in logs

**Then:** 🎊 **DEPLOYMENT SUCCESSFUL!** 🎊

---

**Last Updated:** December 5, 2025  
**Questions?** Check `COMPLETE_BUILD_AND_DEPLOY_GUIDE.md` for detailed help.

