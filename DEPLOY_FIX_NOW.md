# URGENT FIX - Deployment Steps

## The Problem
Application is running but static files (CSS/JS) are not loading due to path doubling issue:
- Expected: `/static/static/assets/index-CjU3bZCB.css`
- Looking for: `/static/static/static/assets/...` ❌

## The Fix
✅ Fixed `ReactResourceResolver.java` to correctly handle static asset paths

## Deploy NOW - Choose Your Method

### METHOD 1: Upload Fixed File + Build on Server (RECOMMENDED)
**Time: ~5 minutes**

1. **Upload the fixed file to server** via WinSCP:
   ```
   Local:  C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\java\com\dechub\tanishq\config\ReactResourceResolver.java
   Server: /opt/tanishq/applications_preprod/tanishq_selfie_app/src/main/java/com/dechub/tanishq/config/ReactResourceResolver.java
   ```

2. **SSH to server and run:**
   ```bash
   ssh jewdev-test@10.160.128.94
   sudo su root
   cd /opt/tanishq/applications_preprod/tanishq_selfie_app
   
   # Verify the fix is present
   grep -A 2 'REACT_STATIC_DIR + "/"' src/main/java/com/dechub/tanishq/config/ReactResourceResolver.java
   
   # Build
   mvn clean package -DskipTests
   
   # Copy WAR to deployment directory
   cp target/selfie-*.war /opt/tanishq/applications_preprod/
   
   # Deploy
   cd /opt/tanishq/applications_preprod
   ./deploy-preprod.sh
   ```

3. **Verify:**
   ```bash
   tail -f application.log
   # Press Ctrl+C after seeing "Started TanishqSelfieApplication"
   
   # Test (should return 200, not 404)
   curl -I http://10.160.128.94:3000/static/assets/index-CjU3bZCB.css
   ```

---

### METHOD 2: Build on Windows + Upload WAR (if Maven available)
**Time: ~10 minutes**

1. **Build on Windows:**
   ```cmd
   cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
   
   REM Find Maven (adjust path as needed)
   C:\apache-maven-3.x.x\bin\mvn clean package -DskipTests
   
   REM WAR will be at: target\selfie-08-12-2025-4-0.0.1-SNAPSHOT.war
   ```

2. **Upload WAR via WinSCP:**
   ```
   Local:  C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\target\selfie-08-12-2025-4-0.0.1-SNAPSHOT.war
   Server: /opt/tanishq/applications_preprod/selfie-08-12-2025-4-0.0.1-SNAPSHOT.war
   ```

3. **Deploy on server:**
   ```bash
   ssh jewdev-test@10.160.128.94
   sudo su root
   cd /opt/tanishq/applications_preprod
   ./deploy-preprod.sh
   ```

---

### METHOD 3: Quick Automated Script
**Time: ~5 minutes**

1. **Upload fix-and-deploy.sh** via WinSCP:
   ```
   Local:  C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\fix-and-deploy.sh
   Server: /opt/tanishq/applications_preprod/fix-and-deploy.sh
   ```

2. **Also upload the fixed source file** (as in Method 1, step 1)

3. **Run the script:**
   ```bash
   ssh jewdev-test@10.160.128.94
   sudo su root
   cd /opt/tanishq/applications_preprod
   chmod +x fix-and-deploy.sh
   ./fix-and-deploy.sh
   ```

---

## Verification After Deployment

### Check Application Started:
```bash
tail -50 /opt/tanishq/applications_preprod/application.log
```
Look for: `Started TanishqSelfieApplication in X seconds`

### Check No More Errors:
```bash
tail -100 /opt/tanishq/applications_preprod/application.log | grep -i "filenotfound"
```
Should return: **nothing** (no FileNotFoundException)

### Test Static Assets:
```bash
curl -I http://10.160.128.94:3000/static/assets/index-CjU3bZCB.css
```
Should return: `HTTP/1.1 200`

### Test in Browser:
Open: http://10.160.128.94:3000/events

Should see:
- ✅ Properly styled page
- ✅ No console errors
- ✅ All CSS/JS loaded

---

## What Changed?

**File:** `ReactResourceResolver.java`  
**Lines:** ~77-82

```java
// BEFORE - caused double static/ prefix
else if (rootStaticFiles.contains(requestPath)
        || requestPath.startsWith(REACT_STATIC_DIR)) {
    return new ClassPathResource(REACT_DIR + requestPath);
}

// AFTER - handles nested static correctly
else if (rootStaticFiles.contains(requestPath)) {
    return new ClassPathResource(REACT_DIR + requestPath);
} else if (requestPath.startsWith(REACT_STATIC_DIR + "/")) {
    return new ClassPathResource(REACT_DIR + requestPath);
}
```

---

## Rollback (if needed)

If something goes wrong:
```bash
cd /opt/tanishq/applications_preprod
# Stop new version
ps -ef | grep tanishq | grep -v grep
kill <PID>

# Start old version (if you kept a backup)
nohup java -jar -Dserver.port=3000 -Dspring.profiles.active=preprod OLD_WAR_NAME.war > application.log 2>&1 &
```

---

**Status:** ✅ Source code fixed  
**Next:** Deploy to server  
**Priority:** HIGH - Application is running but UI not loading properly  
**Date:** December 8, 2025

