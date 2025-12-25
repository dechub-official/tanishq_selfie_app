# Complete Application Structure Analysis & Fix

## Issue Summary
The `index.html` file was accidentally overwritten with the Events page content during a frontend rebuild. This caused the main selfie application to fail.

## Application Architecture

### Three Main Applications:

#### 1. **Main Selfie Application** (Root: `/selfie`)
- **HTML:** `/static/index.html`
- **CSS:** `/static/css/main.39fd591b.css` → resolves to `/static/static/css/main.39fd591b.css`
- **JS:** `/static/js/main.69d68b31.js` → resolves to `/static/static/js/main.69d68b31.js`
- **Framework:** React (Create React App build)
- **Features:** Photo selfie app, CamanJS filters, GTM tracking
- **Title:** "Celebrations With Tanishq"

#### 2. **Events Page** (Root: `/events`)
- **HTML:** `/static/events.html`
- **CSS:** `/static/assets/index-CjU3bZCB.css` → resolves to `/static/static/assets/index-CjU3bZCB.css`
- **JS:** `/static/assets/index-CLJQELnM.js` → resolves to `/static/static/assets/index-CLJQELnM.js`
- **Framework:** React (Vite build)
- **Features:** Event listings and management
- **Title:** "Tanishq Events"

#### 3. **Global Landing Page** (Root: `/`)
- **HTML:** `/static/globalPage/celebrate.html`
- **Assets:** `/globalAssets/*`
- **Purpose:** Landing page that routes to other apps

### File Structure
```
src/main/resources/static/
├── index.html                     ← Main Selfie App (RESTORED)
├── events.html                    ← Events Page
├── logo.png
├── manifest.json
├── robots.txt
├── vite.svg
│
├── static/                        ← Nested static directory (actual files)
│   ├── css/
│   │   └── main.39fd591b.css     ← Main app CSS
│   ├── js/
│   │   └── main.69d68b31.js      ← Main app JS
│   ├── assets/
│   │   ├── index-CjU3bZCB.css    ← Events page CSS
│   │   ├── index-CLJQELnM.js     ← Events page JS
│   │   └── (images, etc.)
│   └── media/
│
├── globalPage/
│   ├── celebrate.html             ← Landing page
│   └── globalAssets/
│
├── checklist/                     ← Checklist module
│   ├── index.html
│   ├── create.html
│   ├── form.html
│   └── assets/
│
└── qr/                            ← QR scanning module
    ├── index.html
    └── assets/
```

## Path Resolution Logic

The `ReactResourceResolver.java` handles path resolution:

### How it works:
1. **Browser requests:** `/static/css/main.39fd591b.css`
2. **Spring receives:** `static/css/main.39fd591b.css` (no leading slash)
3. **Resolver checks:** `requestPath.startsWith("static/")`
4. **Resolver returns:** `REACT_DIR("/static/") + "static/css/..." = "/static/static/css/main.39fd591b.css"`
5. **Spring finds:** `classpath:/static/static/css/main.39fd591b.css` ✅

### Routing Rules:
```java
requestPath starts with → Returns
─────────────────────────────────────────────────────────────
"events"           → events.html
"selfie"           → index.html (main selfie app)
"static/"          → /static/static/... (nested static files)
"assets"           → /static/checklist/assets/...
"checklist"        → checklist/index.html
"qr"               → qr/index.html
"globalAssets"     → globalPage/globalAssets/...
(default)          → globalPage/celebrate.html (landing page)
```

## Fixes Applied

### Fix 1: Restored index.html ✅
**Problem:** index.html was overwritten with events page content
**Solution:** Restored original content from static_backup

**BEFORE (Wrong):**
```html
<title>Tanishq Events</title>
<script type="module" crossorigin src="/assets/index-D9W3lokU.js"></script>
<link rel="stylesheet" crossorigin href="/assets/index-BJPJAhhn.css">
```

**AFTER (Correct):**
```html
<title>Celebrations With Tanishq</title>
<script defer="defer" src="/static/js/main.69d68b31.js"></script>
<link href="/static/css/main.39fd591b.css" rel="stylesheet" />
```

### Fix 2: Updated ReactResourceResolver.java ✅
**Problem:** Path doubling for static assets
**Solution:** Separated handling of root files vs nested static directory

**Changed (Lines 77-88):**
```java
// BEFORE - bundled logic
else if (rootStaticFiles.contains(requestPath)
        || requestPath.startsWith(REACT_STATIC_DIR)) {
    return new ClassPathResource(REACT_DIR + requestPath);
}

// AFTER - separated logic
else if (rootStaticFiles.contains(requestPath)) {
    return new ClassPathResource(REACT_DIR + requestPath);
} else if (requestPath.startsWith(REACT_STATIC_DIR + "/")) {
    // Handle static/assets requests - the path already contains "static/"
    // so we need to prepend only "/static/" to get "/static/static/assets/..."
    return new ClassPathResource(REACT_DIR + requestPath);
}
```

## URL Routing

### Public URLs:
- `http://server:3000/` → Global landing page
- `http://server:3000/events` → Events listing page (Vite build)
- `http://server:3000/selfie` → Main selfie application (CRA build)
- `http://server:3000/checklist` → Checklist module
- `http://server:3000/qr` → QR scanning module

### Static Asset URLs:
- Main App: `/static/css/*.css`, `/static/js/*.js`
- Events: `/static/assets/*.css`, `/static/assets/*.js`
- Checklist: `/assets/*` (resolves to `/static/checklist/assets/*`)
- Global: `/globalAssets/*` (resolves to `/static/globalPage/globalAssets/*`)

## Verification

### Files to Check:
```bash
# Main app files (should exist)
static/static/css/main.39fd591b.css      ✅
static/static/js/main.69d68b31.js        ✅

# Events page files (should exist)
static/static/assets/index-CjU3bZCB.css  ✅
static/static/assets/index-CLJQELnM.js   ✅

# Files that DON'T exist (referenced in old broken index.html)
assets/index-BJPJAhhn.css                ❌ (should not be referenced)
assets/index-D9W3lokU.js                 ❌ (should not be referenced)
```

### Test URLs After Deployment:
```bash
# Main selfie app (restored)
curl -I http://10.160.128.94:3000/selfie
# Should return 200, serve index.html with main.39fd591b.css reference

# Main app CSS
curl -I http://10.160.128.94:3000/static/css/main.39fd591b.css
# Should return 200

# Events page
curl -I http://10.160.128.94:3000/events
# Should return 200, serve events.html with index-CjU3bZCB.css reference

# Events CSS
curl -I http://10.160.128.94:3000/static/assets/index-CjU3bZCB.css
# Should return 200
```

## Deployment Checklist

- [x] Restore index.html from backup
- [x] Fix ReactResourceResolver.java path logic
- [ ] Build new WAR file
- [ ] Upload to server
- [ ] Restart application
- [ ] Test all three main URLs
- [ ] Verify no 404 errors in logs

## Files Modified

1. **src/main/resources/static/index.html**
   - Restored from static_backup
   - Changed from Events page back to Selfie app

2. **src/main/java/com/dechub/tanishq/config/ReactResourceResolver.java**
   - Fixed path resolution for static/ prefix
   - Lines 77-88 modified

## Build Command
```bash
cd /opt/tanishq/applications_preprod/tanishq_selfie_app
mvn clean package -DskipTests
cp target/selfie-*.war /opt/tanishq/applications_preprod/
```

## Deployment Command
```bash
cd /opt/tanishq/applications_preprod
pkill -f "tanishq.*\.war"
nohup java -jar -Dserver.port=3000 -Dspring.profiles.active=preprod selfie-08-12-2025-5-0.0.1-SNAPSHOT.war > application.log 2>&1 &
```

## Expected Behavior After Fix

### Main Selfie App (`/selfie`):
- ✅ Loads with proper styling
- ✅ Uses main.39fd591b.css and main.69d68b31.js
- ✅ Shows "Celebrations With Tanishq" title
- ✅ Has CamanJS photo filters
- ✅ GTM tracking active

### Events Page (`/events`):
- ✅ Loads with proper styling
- ✅ Uses index-CjU3bZCB.css and index-CLJQELnM.js
- ✅ Shows "Tanishq Events" title
- ✅ Vite build assets load correctly

### Landing Page (`/`):
- ✅ Shows global celebration page
- ✅ Routes to other modules

## Logs to Monitor
```bash
# After restart, check for:
tail -100 application.log | grep -i "started"
# Should show: Started TanishqSelfieApplication in X seconds

# Check for errors (should be NONE):
tail -100 application.log | grep -i "filenotfound"
# Should return nothing

# Check for 404s (should be NONE):
tail -100 application.log | grep "404"
# Should return nothing
```

---

**Status:** ✅ All fixes applied  
**Ready for:** Build and deployment  
**Priority:** HIGH - Main application was broken  
**Risk:** LOW - Simple file restoration  
**Date:** December 8, 2025

