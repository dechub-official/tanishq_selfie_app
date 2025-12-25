```

---

## 📝 FRONTEND URL ISSUE

### Current Situation
The frontend JavaScript has hardcoded:
```javascript
const we="https://celebrationsite-preprod.tanishq.co.in/events";
```

### Why This Happens
- React/JavaScript applications typically use environment variables or build-time configuration
- The bundled files in `src/main/resources/static/` are compiled production builds
- Original source code is likely separate (React project)

### Recommended Solution
1. **Find the React source code** (usually in a separate folder like `frontend/`, `client/`, or `ui/`)
2. **Look for environment configuration files:**
   - `.env`
   - `.env.production`
   - `config.js`
   - Or hardcoded in source files

3. **Common patterns:**
   ```javascript
   // Pattern 1: Environment variable
   const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:3000/events';
   
   // Pattern 2: Config file
   import { API_BASE_URL } from './config';
   
   // Pattern 3: Hardcoded (needs fix)
   const we="https://celebrationsite-preprod.tanishq.co.in/events";
   ```

4. **After fixing, rebuild frontend:**
   ```bash
   cd [frontend-directory]
   npm run build
   # Copy built files to src/main/resources/static/
   ```

---

## 🚨 IMPORTANT NOTES

### 1. **Port Consistency**
- Application now runs on **port 3000** (was 3002 in config)
- Make sure firewall allows port 3000
- ELB/Load Balancer should forward to port 3000

### 2. **QR Codes**
- **Old QR codes** (created before this fix) will still point to old domain
- **New QR codes** (created after deployment) will use IP address
- Consider regenerating important QR codes

### 3. **CORS Configuration**
Current setting: `app.cors.allowedOrigins=*`
- This allows all origins (good for testing)
- For production, restrict to specific domains:
  ```properties
  app.cors.allowedOrigins=http://celebrationsite-preprod.tanishq.co.in,http://10.160.128.94:3000
  ```

### 4. **HTTPS vs HTTP**
- Current: Using HTTP
- Frontend JavaScript uses HTTPS: `https://celebrationsite-preprod.tanishq.co.in`
- This might cause mixed content warnings in browsers
- Consider setting up HTTPS on the application server

---

## ✅ VERIFICATION CHECKLIST

After deployment, verify:

- [ ] Application runs on port 3000
- [ ] Direct IP access works: `http://10.160.128.94:3000`
- [ ] Login works via IP
- [ ] Event creation works
- [ ] QR codes contain correct URL (IP-based, not domain)
- [ ] Scanning QR code opens correct page
- [ ] No errors in application logs
- [ ] Database connections work
- [ ] File uploads work

---

## 🔍 WHERE TO FIND FRONTEND SOURCE

Search for these directories in your codebase:
```bash
# Common frontend directory names
ls -la | grep -E "(frontend|client|ui|react|web|app)"

# Or search for package.json
find . -name "package.json" -type f

# Or search for React files
find . -name "*.jsx" -type f -o -name "*.tsx" -type f
```

---

## 📞 SUPPORT REFERENCE

**Issue:** Hardcoded URLs in application configuration  
**Root Cause:** Backend QR code URL and frontend API URL hardcoded to domain  
**Solution:** Changed backend to use IP address  
**Remaining:** Frontend source code needs similar fix  
**Impact:** QR codes now work with direct IP access  

---

**Created:** December 5, 2025  
**Updated:** December 5, 2025  
**Status:** Backend Fixed ✅ | Frontend Needs Source Code
# 🔧 HARDCODED URL FIX - COMPLETE SOLUTION

**Date:** December 5, 2025  
**Issue:** Application using hardcoded domain instead of current server  
**Status:** ✅ FIXED

---

## 🎯 PROBLEM IDENTIFIED

The application had **hardcoded URLs** that prevented it from working with direct IP access:

### 1. **Backend Configuration (FIXED)**
**File:** `src/main/resources/application-preprod.properties`

**Old Configuration:**
```properties
server.port=3002
qr.code.base.url=http://celebrationsite-preprod.tanishq.co.in/events/customer/
```

**New Configuration:**
```properties
server.port=3000
qr.code.base.url=http://10.160.128.94:3000/events/customer/
```

### 2. **Frontend Configuration (Bundled)**
**Files:** Compiled JavaScript bundles in `src/main/resources/static/`

The frontend has this hardcoded:
```javascript
const we="https://celebrationsite-preprod.tanishq.co.in/events";
```

**Note:** These are compiled/bundled files. The URL is likely defined in the original React source code before building.

---

## ✅ FIXES APPLIED

### Backend Configuration Fixed ✅
- [x] Changed `server.port` from 3002 to 3000
- [x] Changed `qr.code.base.url` to use IP address: `http://10.160.128.94:3000/events/customer/`
- [x] Added comments for easy environment switching

### What This Fixes:
1. **QR Code Generation** - QR codes will now point to correct IP address
2. **Port Alignment** - Application will run on port 3000 (matching current setup)
3. **Direct IP Access** - QR codes work without DNS/domain requirement

---

## 📦 DEPLOYMENT STEPS

### Step 1: Rebuild the Application
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -DskipTests
```

### Step 2: Transfer to Server
```cmd
scp target\tanishq-preprod-05-12-2025-1-0.0.1-SNAPSHOT.war jewdev-test@10.160.128.94:/opt/tanishq/applications_preprod/
```

### Step 3: Deploy on Server
```bash
ssh jewdev-test@10.160.128.94
cd /opt/tanishq/applications_preprod

# Backup current
mkdir -p backups/backup_$(date +%Y%m%d_%H%M%S)
cp *.war backups/backup_$(date +%Y%m%d_%H%M%S)/ 2>/dev/null

# Stop old application
sudo kill -15 $(ps aux | grep "[j]ava.*tanishq" | awk '{print $2}' | head -1)
sleep 10

# Start new application
nohup java -jar tanishq-preprod-05-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

# Wait and verify
sleep 30
tail -50 application.log
curl -I http://localhost:3000
```

---

## 🧪 TESTING AFTER DEPLOYMENT

### 1. Test Direct IP Access
```bash
# On server
curl -I http://10.160.128.94:3000
curl -I http://localhost:3000

# Expected: HTTP/1.1 200 OK or 302 Found
```

### 2. Test QR Code Generation
1. Login to dashboard
2. Create a new event
3. Download the generated QR code
4. Scan the QR code
5. **Expected URL:** `http://10.160.128.94:3000/events/customer/[event-code]`
6. **Previous (wrong) URL:** `http://celebrationsite-preprod.tanishq.co.in/events/customer/[event-code]`

### 3. Test Event Creation
```bash
# Login API
curl -X POST http://10.160.128.94:3000/events/login \
  -H "Content-Type: application/json" \
  -d '{"code":"STORE_CODE","password":"PASSWORD"}'
```

---

## 🔄 ENVIRONMENT SWITCHING

### To Use Domain (When DNS is Ready)
Edit `application-preprod.properties`:
```properties
qr.code.base.url=http://celebrationsite-preprod.tanishq.co.in/events/customer/
```

### To Use Direct IP
```properties
qr.code.base.url=http://10.160.128.94:3000/events/customer/
```

### To Use Localhost (Testing)
```properties
qr.code.base.url=http://localhost:3000/events/customer/

