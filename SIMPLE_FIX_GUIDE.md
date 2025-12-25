# 🎯 SIMPLE SOLUTION - Fix "Create Event" Redirection

**Problem:** When you click "Create Event", it redirects to PRODUCTION instead of PRE-PROD  
**Cause:** Frontend has hardcoded production URL  
**Solution:** Replace frontend with correct pre-prod build  

---

## ✅ THE SIMPLE FIX

### Step 1: Build Correct Frontend
```cmd
cd C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
npm run build:preprod
```

This creates frontend with pre-prod URLs in the `dist` folder.

---

### Step 2: Copy Frontend to Backend
```cmd
REM Backup old frontend
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources
move static static_backup_old

REM Create new static folder
mkdir static

REM Copy new frontend
xcopy /E /I /Y C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist\* static\
```

---

### Step 3: Rebuild Backend WAR
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -DskipTests
```

---

### Step 4: Deploy to Server
```cmd
REM Transfer WAR
scp target\tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war jewdev-test@10.160.128.94:/opt/tanishq/applications_preprod/
```

```bash
# On server (SSH)
ssh jewdev-test@10.160.128.94
cd /opt/tanishq/applications_preprod

# Stop old app
sudo kill -15 $(ps aux | grep "[j]ava.*tanishq" | awk '{print $2}' | head -1)
sleep 10

# Start new app
nohup java -jar tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

# Wait and check
sleep 30
tail -50 application.log
```

---

### Step 5: Test
1. Open: `http://celebrationsite-preprod.tanishq.co.in`
2. Login
3. Click "Create Event"
4. ✅ Should stay on pre-prod (not redirect to production)

---

## 📋 THAT'S IT!

**Time:** ~15 minutes  
**Complexity:** Simple  
**Permanent:** Until you need to rebuild  

---

## 🔄 FOR FUTURE BUILDS

### Pre-Production:
```cmd
cd C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
npm run build:preprod

cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
xcopy /E /I /Y C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist\* src\main\resources\static\
mvn clean package -DskipTests
```

### Production (when ready):
```cmd
cd C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events
npm run build:prod

cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
xcopy /E /I /Y C:\DECHUB_CELEBRATIONS\Event_Frontend_Preprod\Tanishq_Events\dist\* src\main\resources\static\
mvn clean package -DskipTests
```

---

## 💡 KEY POINTS

1. **Always build frontend first** with the correct environment (`npm run build:preprod` or `npm run build:prod`)
2. **Copy to backend** before building WAR
3. **WAR file includes frontend** - whatever is in `src/main/resources/static/` gets bundled
4. **For different environments** - just change which frontend build you copy

---

**That's all you need to know!** Simple and straightforward. 🎯

