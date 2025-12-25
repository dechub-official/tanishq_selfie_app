# 🔍 COMPLETE ANALYSIS: What Went Wrong This Afternoon

## Timeline of Changes

### BEFORE (Working - December 5, 2025)
**WAR File Name:** `tanishq-preprod-05-12-2025-1-0.0.1-SNAPSHOT.war`

**pom.xml Configuration:**
```xml
<artifactId>tanishq-preprod-05-12-2025-1</artifactId>
<version>0.0.1-SNAPSHOT</version>
```

**Status:** ✅ Application working fine

---

### TODAY AFTERNOON (Multiple Changes)

#### Change 1: pom.xml artifactId Changed
**From:** `tanishq-preprod-05-12-2025-1`  
**To:** `selfie-08-12-2025-6`

**Current pom.xml (Line 13):**
```xml
<artifactId>selfie-08-12-2025-6</artifactId>
```

**Result:** WAR file name changed from `tanishq-preprod-*.war` to `selfie-*.war`

#### Change 2: Frontend Rebuild
The Events frontend was rebuilt using Vite, which:
- Overwrote `index.html` with Events page content
- Created new hashed asset filenames (index-BJPJAhhn.css, etc.)
- These new files were referenced in the overwritten index.html
- **BUT** these files don't exist in the actual build!

#### Change 3: Database Migration
Some database changes were made (you mentioned "before db migrating")

---

## Root Causes of Current Issues

### Issue 1: Wrong WAR File Name ❌
**Problem:** pom.xml was modified to change artifact name

**Evidence:**
```bash
# Server expects:
tanishq-preprod-*.war

# Now builds as:
selfie-08-12-2025-6-0.0.1-SNAPSHOT.war
```

**Impact:**
- Deployment scripts looking for `tanishq-preprod-*.war` won't find it
- Confusing for operations team
- Breaks existing deployment automation

---

### Issue 2: index.html Overwritten ❌
**Problem:** Frontend build process overwrote the main app's index.html

**Evidence:**
```html
<!-- BEFORE (Correct - Main Selfie App) -->
<title>Celebrations With Tanishq</title>
<script defer="defer" src="/static/js/main.69d68b31.js"></script>
<link href="/static/css/main.39fd591b.css" rel="stylesheet" />

<!-- AFTER (Wrong - Events Page) -->
<title>Tanishq Events</title>
<script type="module" crossorigin src="/assets/index-D9W3lokU.js"></script>
<link rel="stylesheet" crossorigin href="/assets/index-BJPJAhhn.css">
```

**Impact:**
- Main selfie app completely broken
- References non-existent files
- Application crashes on startup

---

### Issue 3: Missing Asset Files ❌
**Problem:** Vite build created references to files that don't exist

**Missing Files:**
- `/assets/index-BJPJAhhn.css` ❌
- `/assets/index-D9W3lokU.js` ❌

**Actual Files:**
- `/static/static/css/main.39fd591b.css` ✅
- `/static/static/js/main.69d68b31.js` ✅
- `/static/static/assets/index-CjU3bZCB.css` ✅
- `/static/static/assets/index-CLJQELnM.js` ✅

---

## What Should Have Happened

### Correct Process:
1. **Events frontend build** → Copy ONLY to `events.html`
2. **Main app (selfie)** → Keep `index.html` unchanged
3. **pom.xml** → Keep artifactId as `tanishq-preprod`
4. **Build** → Creates `tanishq-preprod-DATE-0.0.1-SNAPSHOT.war`

### What Actually Happened:
1. ❌ Events build copied to BOTH `index.html` AND `events.html`
2. ❌ Someone changed pom.xml artifactId
3. ❌ Build created `selfie-*.war` instead of `tanishq-preprod-*.war`
4. ❌ Application crashes because wrong files referenced

---

## File Comparison

### pom.xml Changes

**BACKUP (pom.xml.backup) - ALSO WRONG:**
```xml
<artifactId>tanishq-preprod-08-12-2025-3</artifactId>
```

**CURRENT (pom.xml) - WRONG:**
```xml
<artifactId>selfie-08-12-2025-6</artifactId>
```

**SHOULD BE:**
```xml
<artifactId>tanishq-preprod</artifactId>
```

Notice: Even the backup has a dated artifactId! This suggests someone has been incrementing versions manually.

---

## The Correct Configuration

### 1. Fix pom.xml
```xml
<groupId>com.dechub</groupId>
<artifactId>tanishq-preprod</artifactId>
<version>0.0.1-SNAPSHOT</version>
<name>tanishq</name>
<description>Tanishq Celebrations Application</description>
<packaging>war</packaging>
```

This will create: `tanishq-preprod-0.0.1-SNAPSHOT.war`

### 2. Fix index.html
Restore from `static_backup/index.html`:
```html
<title>Celebrations With Tanishq</title>
<script defer="defer" src="/static/js/main.69d68b31.js"></script>
<link href="/static/css/main.39fd591b.css" rel="stylesheet" />
```

### 3. Keep events.html Separate
```html
<title>Tanishq Events</title>
<script type="module" crossorigin src="/static/assets/index-CLJQELnM.js"></script>
<link rel="stylesheet" crossorigin href="/static/assets/index-CjU3bZCB.css">
```

---

## Complete Fix Steps

### Fix 1: Restore pom.xml
```xml
<!-- Change line 13 from: -->
<artifactId>selfie-08-12-2025-6</artifactId>

<!-- To: -->
<artifactId>tanishq-preprod</artifactId>
```

### Fix 2: Restore index.html
```bash
cp src/main/resources/static_backup/index.html src/main/resources/static/index.html
```

### Fix 3: Fix ReactResourceResolver.java
Already fixed in your current code! ✅

### Fix 4: Rebuild
```bash
mvn clean package -DskipTests
```

This will create: `target/tanishq-preprod-0.0.1-SNAPSHOT.war`

### Fix 5: Deploy
```bash
# Upload tanishq-preprod-0.0.1-SNAPSHOT.war to server
# Start with:
nohup java -jar \
  -Dserver.port=3000 \
  -Dspring.profiles.active=preprod \
  tanishq-preprod-0.0.1-SNAPSHOT.war \
  > application.log 2>&1 &
```

---

## Why This Naming Convention Matters

### Bad Practice (Current):
```xml
<artifactId>selfie-08-12-2025-6</artifactId>
<artifactId>tanishq-preprod-08-12-2025-3</artifactId>
<artifactId>tanishq-preprod-05-12-2025-1</artifactId>
```

**Problems:**
- ❌ Version number in artifactId (should be in `<version>`)
- ❌ Date in artifactId (unnecessary)
- ❌ Manual incrementing (error-prone)
- ❌ Inconsistent naming (selfie vs tanishq)
- ❌ Breaks deployment automation

### Best Practice:
```xml
<artifactId>tanishq-preprod</artifactId>
<version>1.0.0-SNAPSHOT</version>
```

**Benefits:**
- ✅ Clean, consistent name
- ✅ Version in proper Maven field
- ✅ Deployment scripts always work
- ✅ Can use version numbers: 1.0.0, 1.0.1, 1.1.0, etc.

---

## Server Deployment Scripts Impact

Your server's `deploy-preprod.sh` likely looks for:
```bash
LATEST_WAR=$(ls -t tanishq-*.war 2>/dev/null | head -1)
```

When you build `selfie-*.war`, this script won't find it!

---

## Summary of What Went Wrong

| Component | Before | After | Impact |
|-----------|--------|-------|---------|
| pom.xml artifactId | `tanishq-preprod-05-12-2025-1` | `selfie-08-12-2025-6` | ❌ Wrong WAR name |
| WAR filename | `tanishq-preprod-*.war` | `selfie-*.war` | ❌ Scripts break |
| index.html | Selfie app | Events page | ❌ App crashes |
| Asset files | Existed | Don't exist | ❌ 404 errors |

---

## Recommended Fixes (Priority Order)

### HIGH PRIORITY (Fix Immediately):
1. ✅ **Fix pom.xml** - Change artifactId back to `tanishq-preprod`
2. ✅ **Fix index.html** - Restore from backup
3. ✅ **Rebuild** - Create correct WAR file
4. ✅ **Deploy** - Upload and restart

### MEDIUM PRIORITY (After System Running):
5. Document the frontend build process
6. Create separate build scripts for events vs main app
7. Add validation to prevent index.html overwrite

### LOW PRIORITY (Long-term):
8. Implement proper CI/CD
9. Version control for all configuration changes
10. Automated testing before deployment

---

## Files to Fix

1. **pom.xml** - Line 13 - Change artifactId
2. **index.html** - Restore from static_backup
3. **ReactResourceResolver.java** - Already fixed! ✅

---

**Next Action:** Fix pom.xml and rebuild to get the correct WAR filename back!

