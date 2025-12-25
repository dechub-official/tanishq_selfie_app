# Static Assets Path Fix - URGENT

## Issue Identified
The application is running but showing 404 errors for static assets (CSS, JS files).

**Error in logs:**
```
java.io.FileNotFoundException: class path resource [static/static/assets/index-BJPJAhhn.css] cannot be resolved to URL because it does not exist
```

## Root Cause
The `ReactResourceResolver.java` was creating a **double "static/" prefix** in the path:
- HTML file requests: `/static/assets/index-CjU3bZCB.css`
- Actual file location: `/static/static/assets/index-CjU3bZCB.css`
- Resolver was creating: `/static/` + `static/assets/...` = `/static/static/assets/...`

This was looking for `/static/static/static/assets/...` which doesn't exist.

## Fix Applied
**File:** `src/main/java/com/dechub/tanishq/config/ReactResourceResolver.java`

**Changed (around line 80):**
```java
// BEFORE:
} else if (rootStaticFiles.contains(requestPath)
        || requestPath.startsWith(REACT_STATIC_DIR)) {
    return new ClassPathResource(REACT_DIR + requestPath);
}

// AFTER:
} else if (rootStaticFiles.contains(requestPath)) {
    return new ClassPathResource(REACT_DIR + requestPath);
} else if (requestPath.startsWith(REACT_STATIC_DIR + "/")) {
    // Handle static/assets requests - the path already contains "static/"
    // so we need to prepend only "/static/" to get "/static/static/assets/..."
    return new ClassPathResource(REACT_DIR + requestPath);
}
```

The fix separates the handling of:
1. Root static files (manifest.json, etc.) 
2. Nested static directory assets (static/assets/...)

## How to Deploy the Fix

### Option 1: Build on Windows (if Maven is available)
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -DskipTests
```

The WAR file will be at: `target\selfie-08-12-2025-4-0.0.1-SNAPSHOT.war`

### Option 2: Build on Linux Server
If you have SSH access to a build server:

```bash
# Upload the source code to server
# Then run:
cd /path/to/tanishq_selfie_app
mvn clean package -DskipTests
```

### Option 3: Manual Class Compilation (Quick Fix)
If you have `javac` available, compile just the changed file:

```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

javac -cp "target\selfie-08-12-2025-4-0.0.1-SNAPSHOT\WEB-INF\lib\*;target\selfie-08-12-2025-4-0.0.1-SNAPSHOT\WEB-INF\classes" ^
  -d target\classes ^
  src\main\java\com\dechub\tanishq\config\ReactResourceResolver.java

# Then update the WAR file
cd target
jar uf selfie-08-12-2025-4-0.0.1-SNAPSHOT.war -C classes com/dechub/tanishq/config/ReactResourceResolver.class
```

## Deployment to Server (10.160.128.94)

1. **Upload the new WAR file** to `/opt/tanishq/applications_preprod/` via WinSCP

2. **SSH to server** and run:
```bash
sudo su root
cd /opt/tanishq/applications_preprod

# Stop current application
ps -ef | grep tanishq | grep -v grep
kill <PID>

# Or use the deployment script
./deploy-preprod.sh
```

3. **Start the application:**
```bash
nohup java -jar -Dserver.port=3000 -Dspring.profiles.active=preprod tanishq-*.war > application.log 2>&1 &
```

4. **Verify the fix:**
```bash
# Check if application started
tail -50 application.log

# Check for errors (should be gone)
tail -100 application.log | grep -i "filenotfound"

# Test in browser
# http://10.160.128.94:3000/events
```

## Expected Result
After deployment:
- ✅ No more 404 errors for static assets
- ✅ Events page loads with CSS styling
- ✅ All JavaScript files load correctly
- ✅ Application displays properly

## Files Changed
- `src/main/java/com/dechub/tanishq/config/ReactResourceResolver.java` (line ~77-82)

## Verification Commands
```bash
# On server after deployment
curl -I http://10.160.128.94:3000/static/assets/index-CjU3bZCB.css
# Should return: HTTP/1.1 200 (not 404)

# Check logs for errors
tail -100 application.log | grep -i "error\|exception"
# Should not show FileNotFoundException for static assets
```

---
**Status:** Fix applied to source code ✅  
**Next Step:** Build and deploy WAR file to server  
**Date:** December 8, 2025

