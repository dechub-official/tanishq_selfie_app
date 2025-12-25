# Pre-Production Environment Issues - Analysis and Fix

**Date:** December 8, 2025  
**Environment:** https://celebrationsite-preprod.tanishq.co.in/  
**Status:** ✅ Deployed and Working | ⚠️ Configuration Issues Identified

---

## 🔴 **CRITICAL ISSUES IDENTIFIED**

### **Issue #1: QR Code URL Configuration (Backend)**
**Severity:** HIGH  
**Impact:** Downloaded QR codes point to wrong URL, attendees form not accessible

**Current Configuration:**
```properties
# File: src/main/resources/application-preprod.properties
qr.code.base.url=http://10.160.128.94:3000/events/customer/
```

**Problem:**
- QR codes are being generated with internal IP address: `http://10.160.128.94:3000/events/customer/`
- This URL is NOT accessible from outside the network
- Attendees scanning the QR code cannot access the form

**✅ FIX REQUIRED:**
```properties
# Update to use the public domain
qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/
```

---

### **Issue #2: Frontend Navigation - Hardcoded Production URL**
**Severity:** HIGH  
**Impact:** "Create Event" button redirects to production instead of preprod

**Analysis:**
- The frontend React application is compiled (minified) in: `src/main/resources/static/static/js/main.69d68b31.js`
- This is a **build artifact** from a React application
- The source code for the React app is likely in a separate repository or was compiled before deployment

**Problem:**
- The React frontend has a hardcoded or environment-specific URL
- When clicking "Create Event", it navigates to production URL instead of preprod
- This is baked into the compiled JavaScript file

**Possible Causes:**
1. React app built with production API URL (`celebrationsite.tanishq.co.in`)
2. Missing environment variable configuration during build
3. Frontend not rebuilt for preprod environment

---

## 🔍 **DETAILED FINDINGS**

### **Backend Configuration**
| Property | Current Value | Required Value | Status |
|----------|--------------|----------------|--------|
| `qr.code.base.url` | `http://10.160.128.94:3000/events/customer/` | `https://celebrationsite-preprod.tanishq.co.in/events/customer/` | ❌ WRONG |
| `server.port` | `3000` | `3000` | ✅ OK |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/selfie_preprod` | - | ✅ OK |

### **Frontend Analysis**
- **Location:** `src/main/resources/static/` 
- **Main JS:** `static/js/main.69d68b31.js` (Minified React bundle)
- **Index:** `index.html`
- **Status:** Compiled/Built application (not source code)

---

## 🛠️ **IMMEDIATE FIXES REQUIRED**

### **FIX #1: Update QR Code Base URL (Backend)**

**File to Modify:**  
`src/main/resources/application-preprod.properties`

**Change:**
```properties
# OLD (Line 101)
qr.code.base.url=http://10.160.128.94:3000/events/customer/

# NEW  
qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/
```

**Impact:** All new QR codes generated will use the correct preprod URL

---

### **FIX #2: Frontend React Application Rebuild**

**This is MORE COMPLEX and requires the React source code!**

#### **Option A: If you have the React source code** 

1. **Locate React App Source:**
   - Usually in a folder like `frontend/`, `client/`, or `react-app/`
   - Look for `package.json`, `src/` folder with `.jsx` or `.tsx` files

2. **Update Environment Configuration:**
   ```javascript
   // Typically in .env.preprod or .env.production
   REACT_APP_API_URL=https://celebrationsite-preprod.tanishq.co.in
   REACT_APP_API_BASE_URL=https://celebrationsite-preprod.tanishq.co.in/api
   ```

3. **Rebuild for Preprod:**
   ```bash
   npm install
   npm run build:preprod
   # OR
   npm run build
   ```

4. **Copy build output:**
   ```bash
   # Copy from React build output to Spring Boot static folder
   cp -r frontend/build/* src/main/resources/static/
   ```

#### **Option B: If you DON'T have the React source**

**You need to:**
1. **Find the original React project** - Check with your development team
2. **Locate the repository** where the frontend was built
3. **Rebuild with preprod configuration**

**⚠️ WARNING:** Without the React source code, you CANNOT fix the navigation issue!

---

## 📋 **VERIFICATION CHECKLIST**

After applying fixes:

### **Backend (QR Code URL)**
- [ ] Updated `application-preprod.properties` with correct URL
- [ ] Rebuilt the WAR file: `mvn clean package -Ppreprod`
- [ ] Deployed new WAR to server
- [ ] Created a test event
- [ ] Downloaded QR code
- [ ] Scanned QR code → Should open: `https://celebrationsite-preprod.tanishq.co.in/events/customer/[EVENT_ID]`
- [ ] Verified attendee form loads correctly

### **Frontend (Navigation Fix)**
- [ ] Located React source code
- [ ] Updated environment configuration for preprod
- [ ] Rebuilt React app with preprod settings
- [ ] Copied build output to `src/main/resources/static/`
- [ ] Rebuilt WAR file
- [ ] Deployed to server
- [ ] Tested "Create Event" button → Should stay on `https://celebrationsite-preprod.tanishq.co.in/events`
- [ ] Verified all navigation works within preprod domain

---

## 🚀 **DEPLOYMENT STEPS**

### **Step 1: Fix QR Code URL (Can do NOW)**

```bash
# 1. Edit the file
nano src/main/resources/application-preprod.properties

# 2. Change line 101:
qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/

# 3. Rebuild
mvn clean package -Ppreprod

# 4. The WAR file will be in:
target/tanishq-preprod-[timestamp]-0.0.1-SNAPSHOT.war

# 5. Deploy to server (backup current first!)
# - Stop Tomcat
# - Backup current WAR
# - Copy new WAR
# - Start Tomcat
```

### **Step 2: Fix Frontend (Requires React Source)**

**CANNOT proceed without React source code!**

You need to find where the original React application code is located.

---

## 🔎 **WHERE IS THE REACT SOURCE CODE?**

The React app source is NOT in this repository. Look for:

1. **Separate Git Repository:**
   - Check for repos named like: `tanishq-selfie-frontend`, `celebration-ui`, etc.
   - Ask the development team

2. **Within Project Structure:**
   ```
   Possible locations:
   - /frontend/
   - /client/
   - /ui/
   - /react-app/
   - /webapp/src/
   ```

3. **Check Build Process:**
   - Look in `pom.xml` for frontend build plugins
   - Check for `maven-frontend-plugin` or similar

---

## 📊 **SUMMARY**

### **What's Working:**
✅ Application deployed successfully  
✅ Backend API functioning  
✅ Database connection working  
✅ Manual URL navigation works (`https://celebrationsite-preprod.tanishq.co.in/events`)  

### **What's Broken:**
❌ **QR Code URLs** → Points to internal IP, needs preprod domain  
❌ **Frontend Navigation** → Hardcoded to production URL, needs rebuild  

### **What You Need:**
1. ✅ **Backend Fix:** Can do immediately (update properties file)
2. ❌ **Frontend Fix:** Need React source code to rebuild

---

## 💡 **RECOMMENDATIONS**

1. **IMMEDIATE:** Apply Backend Fix #1 (QR Code URL) - This is CRITICAL
2. **URGENT:** Locate React source code for Frontend Fix #2
3. **IMPORTANT:** Set up proper CI/CD pipeline with environment-specific builds
4. **BEST PRACTICE:** Use environment variables for all URLs (don't hardcode)

---

## 📞 **NEXT STEPS - WHAT TO DO NOW**

### **For Backend Fix (You can do this now):**
1. I will update the `application-preprod.properties` file
2. You rebuild the WAR: `mvn clean package -Ppreprod`
3. Deploy the new WAR to server
4. Test QR code generation

### **For Frontend Fix (Need React source):**
1. Find the React application source code
2. Share the location with me
3. I'll guide you through the rebuild process
4. You'll deploy the updated static files

---

**Do you want me to:**
1. ✅ Apply the Backend Fix #1 now? (Update properties file)
2. 🔍 Help you search for the React source code?
3. 📝 Create step-by-step deployment scripts?

Please confirm and I'll proceed!

