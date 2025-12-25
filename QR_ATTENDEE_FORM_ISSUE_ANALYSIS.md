# 🚨 QR Code Attendee Form - Blank Page Issue Analysis

## Date: December 18, 2025

## Problem Summary

**Issue:** When users scan the QR code for an event, they see a **BLANK PAGE** instead of the attendee registration form.

**QR Code URL:** `https://celebrationsite-preprod.tanishq.co.in/events/customer/{eventId}`

**Expected:** Attendee form with fields (name, phone, RSO, etc.)  
**Actual:** Blank white page

---

## 🔍 Root Cause Analysis

### Current Flow

```
1. User scans QR code
   ↓
2. Browser opens: /events/customer/EVENT_12345
   ↓
3. Spring Boot EventsController.showAttendeeForm()
   - Forwards to: /events.html
   ↓
4. Browser loads: /events.html
   - Contains: <script src="/static/assets/index-CLJQELnM.js">
   - Contains: <link href="/static/assets/index-CjU3bZCB.css">
   ↓
5. React app loads
   - Router basename: "/events"
   - Route: /customer/:id → AttendeeForm component
   ↓
6. ❌ PROBLEM: React Router cannot match the path correctly
```

### Technical Issues Identified

#### Issue 1: React Router Path Mismatch

**Current Setup:**
- Spring Boot forwards `/events/customer/123` to `/events.html`
- React Router has basename `/events`
- React Router tries to match `/customer/123` within its routes

**Problem:**
When the browser URL is `/events/customer/123` and the page loads `/events.html`, React Router with basename `/events` expects to find routes relative to `/events`.

However, checking the minified JavaScript bundle shows the React Router configuration:
```javascript
// From index-BPoj6p4i.js (line 8000+)
<Router basename="/events">
  <Route path="/customer/:id" element={<AttendeeForm />} />
</Router>
```

This SHOULD work, but there might be an issue with:
1. The JavaScript bundle not loading correctly
2. The asset paths being incorrect
3. Browser caching old JavaScript

#### Issue 2: Static Asset Path Issues

**Current events.html:**
```html
<script type="module" crossorigin src="/static/assets/index-CLJQELnM.js"></script>
<link rel="stylesheet" crossorigin href="/static/assets/index-CjU3bZCB.css">
```

**Potential Problem:**
- When accessed via `/events/customer/123`, the relative path resolution might fail
- The browser might look for assets at incorrect locations

#### Issue 3: ReactResourceResolver Configuration

**Current Code** (ReactResourceResolver.java line 86):
```java
if (requestPath.startsWith("events")) {
    return eventsIndex;
}
```

**Issue:** This catches ALL paths starting with "events", which means:
- `/events/customer/123` → Returns events.html ✅
- `/events.html` → Returns events.html ✅
- `/events` (without .html) → Returns events.html ✅

BUT the asset loading might be affected when the URL path is deep (e.g., `/events/customer/123`).

#### Issue 4: Missing Asset Files

Checked the asset directory structure:
```
src/main/resources/static/assets/
├── index-2ipkaO8n.js (exists)
├── index-Bl1_SFlI.js (exists)
├── index-BvKtF2Jc.css (exists)
├── index-DRK0HUpC.css (exists)
└── ... (various images)
```

**Problem:** The events.html references:
- `index-CLJQELnM.js` (NOT in the assets folder!)
- `index-CjU3bZCB.css` (NOT in the assets folder!)

**This is the PRIMARY ISSUE!** The HTML file references JavaScript/CSS files that don't exist in the assets folder.

---

## 🎯 ROOT CAUSE

**The blank page is caused by:**

1. **Missing JavaScript Bundle:** The events.html file references `index-CLJQELnM.js` which doesn't exist in the assets folder
2. **Missing CSS File:** Similarly, `index-CjU3bZCB.css` is missing
3. **Build Mismatch:** The frontend React app was built, but the generated bundle files were not properly copied to the static assets folder

---

## 🔧 Solutions

### Solution 1: Rebuild and Deploy the Correct Assets (RECOMMENDED)

**Steps:**

1. **Navigate to the frontend project directory:**
   ```bash
   cd frontend
   ```

2. **Clean previous build:**
   ```bash
   npm run clean
   # or
   rm -rf dist/
   ```

3. **Install dependencies (if needed):**
   ```bash
   npm install
   ```

4. **Build the project:**
   ```bash
   npm run build
   ```

5. **Copy the built files to Spring Boot static folder:**
   ```bash
   # The dist folder should contain the built React app
   cp -r dist/* ../src/main/resources/static/
   ```

6. **Verify the files are copied:**
   ```bash
   ls -la ../src/main/resources/static/assets/
   # Should contain index-CLJQELnM.js and index-CjU3bZCB.css
   ```

7. **Rebuild the Spring Boot application:**
   ```bash
   cd ..
   mvn clean package -DskipTests
   ```

8. **Deploy the new WAR file to the server**

---

### Solution 2: Update events.html to Use Correct Asset Names

**If you cannot rebuild the frontend**, update events.html to reference the CORRECT JavaScript files that exist:

**File:** `src/main/resources/static/events.html`

**Change from:**
```html
<script type="module" crossorigin src="/static/assets/index-CLJQELnM.js"></script>
<link rel="stylesheet" crossorigin href="/static/assets/index-CjU3bZCB.css">
```

**Change to:**
```html
<script type="module" crossorigin src="/static/assets/index-2ipkaO8n.js"></script>
<link rel="stylesheet" crossorigin href="/static/assets/index-BvKtF2Jc.css">
```

**Then rebuild and redeploy:**
```bash
mvn clean package -DskipTests
```

---

### Solution 3: Fix Asset Path Resolution (If above doesn't work)

**Issue:** Relative paths might not work when URL is deep (e.g., `/events/customer/123`)

**Fix:** Use absolute paths with base tag

**Update events.html:**
```html
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <base href="/"> <!-- ADD THIS LINE -->
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    ...
```

This ensures all asset paths are resolved from the root, not relative to the current URL path.

---

## 📝 Verification Steps

After applying the fix, verify:

1. **Check if JavaScript loads:**
   - Open browser DevTools (F12)
   - Go to Network tab
   - Clear cache (Ctrl+Shift+Del)
   - Scan the QR code
   - Check if `/static/assets/index-XXX.js` loads successfully (should be 200, not 404)

2. **Check if React app initializes:**
   - In Console tab, look for React-specific messages
   - Should see: "React Router loaded" or similar

3. **Check if AttendeeForm component renders:**
   - The page should show the registration form with fields

---

## 🔍 Debugging Commands

### Check which files exist in WAR:
```bash
jar -tf target/tanishq-0.0.1-SNAPSHOT.war | grep "WEB-INF/classes/static/assets"
```

### Check if files are in the right location:
```bash
ls -la src/main/resources/static/assets/index*.js
ls -la src/main/resources/static/assets/index*.css
```

### Check events.html references:
```bash
grep "index-" src/main/resources/static/events.html
```

---

## 🎯 Quick Fix (If in Production Emergency)

If you need an IMMEDIATE fix and cannot rebuild:

1. Copy an existing working JavaScript file to the missing name:
   ```bash
   cd src/main/resources/static/assets/
   cp index-2ipkaO8n.js index-CLJQELnM.js
   cp index-BvKtF2Jc.css index-CjU3bZCB.css
   ```

2. Rebuild WAR:
   ```bash
   mvn clean package -DskipTests
   ```

3. Deploy immediately

**Note:** This is a temporary workaround. The proper fix is to rebuild the frontend with correct asset names.

---

## 📌 Preventive Measures

1. **Frontend Build Script:** Create a script that:
   - Builds the React app
   - Automatically copies files to Spring Boot static folder
   - Updates the HTML file references

2. **Version Check:** Add a version number to asset files and log it on page load

3. **Build Verification:** Add a Maven phase that verifies all referenced assets exist before packaging

---

## 📋 Summary

| Issue | Root Cause | Solution |
|-------|-----------|----------|
| Blank page after QR scan | Missing JavaScript bundle (index-CLJQELnM.js) | Rebuild frontend and copy assets |
| Assets not loading | Incorrect file references in events.html | Update HTML to use correct filenames |
| Deep URL path issues | Relative path resolution | Add `<base href="/">` tag |

**Priority Fix:** Rebuild the React frontend and ensure the generated bundle files are correctly copied to the Spring Boot static assets folder.

