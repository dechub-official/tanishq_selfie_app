# 🔴 QR CODE FORM NOT SHOWING - ROOT CAUSE & FIX

## 📱 What You're Seeing

When users scan the QR code, they see:
- ✅ URL loads: `https://celebrationsite-preprod.tanishq.co.in/events/customer/TEST_...`
- ❌ **BLANK PAGE** - No form appears
- ❌ No "I'm attending Tanishq Celebration" heading
- ❌ No input fields

## 🔍 Root Cause (Technical)

The React app is not loading because `events.html` references **non-existent JavaScript files**:

```
Browser tries to load: index-CLJQELnM.js ❌ (404 Not Found)
Actual file on server: index-Bl1_SFlI.js ✅ (exists but not loaded)
```

**Why this happened:**
- React build creates new file names with random hashes (for caching)
- The `events.html` file wasn't updated to match the new file names
- When deployed, the HTML points to old files that don't exist

## ✅ The Fix (Local - Already Done)

File: `src/main/resources/static/events.html`

**Changed from:**
```html
<script type="module" crossorigin src="/static/assets/index-CLJQELnM.js"></script>
<link rel="stylesheet" crossorigin href="/static/assets/index-CjU3bZCB.css">
```

**Changed to:**
```html
<script type="module" crossorigin src="/static/assets/index-Bl1_SFlI.js"></script>
<link rel="stylesheet" crossorigin href="/static/assets/index-DRK0HUpC.css">
```

## 🚀 Deployment Options

### OPTION 1: Rebuild & Redeploy (Recommended)

**Step-by-Step:**

1. **Build the WAR file**
   ```cmd
   cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
   
   REM Run the rebuild script
   rebuild-qr-fix.bat
   ```
   
   OR manually with Maven:
   ```cmd
   mvn clean package -DskipTests
   ```
   
   OR using IntelliJ IDEA:
   - Open project in IntelliJ
   - View → Tool Windows → Maven
   - Lifecycle → clean (double-click)
   - Lifecycle → package (double-click)
   - Wait for "BUILD SUCCESS"

2. **Upload to Server**
   - Open WinSCP
   - Connect to: `10.160.128.94` (root)
   - Navigate to: `/opt/tanishq/applications_preprod/`
   - Upload: `target/tanishq-preprod-*.war`

3. **Deploy on Server**
   - Open PuTTY or Windows Terminal
   - SSH: `ssh root@10.160.128.94`
   - Commands:
     ```bash
     cd /opt/tanishq/applications_preprod
     sudo bash deploy-preprod.sh
     ```

4. **Verify**
   - Open Events Dashboard
   - Create or view any event
   - Download QR code
   - Scan with phone
   - **Form should appear!** ✅

### OPTION 2: Quick Server Fix (No Rebuild Needed)

If you have someone with SSH access to the server, they can fix it directly:

**On the Server:**
```bash
# 1. SSH to server
ssh root@10.160.128.94

# 2. Stop the application
ps -ef | grep tanishq
kill [PID_FROM_ABOVE]

# 3. Extract the WAR file
cd /opt/tanishq/applications_preprod
mkdir -p temp_extract
cd temp_extract
jar -xvf ../tanishq-preprod-*.war

# 4. Edit events.html
nano BOOT-INF/classes/static/events.html

# 5. Change these two lines:
#    FROM: src="/static/assets/index-CLJQELnM.js"
#    TO:   src="/static/assets/index-Bl1_SFlI.js"
#
#    FROM: href="/static/assets/index-CjU3bZCB.css"
#    TO:   href="/static/assets/index-DRK0HUpC.css"

# 6. Save and repackage
jar -cvf ../tanishq-preprod-FIXED.war .

# 7. Move and restart
cd ..
mv tanishq-preprod-FIXED.war tanishq-preprod-current.war
nohup java -jar -Dspring.profiles.active=preprod tanishq-preprod-current.war > app.log 2>&1 &

# 8. Verify
tail -f app.log
```

## 🧪 Testing Checklist

After deployment, verify:

1. **Check events.html loads:**
   ```
   https://celebrationsite-preprod.tanishq.co.in/events.html
   ```
   Should show a page (even if blank, not 404)

2. **Check JavaScript loads:**
   ```
   https://celebrationsite-preprod.tanishq.co.in/static/assets/index-Bl1_SFlI.js
   ```
   Should download a JavaScript file (not 404)

3. **Check customer form:**
   - Get any event ID from the Events Dashboard
   - Visit: `https://celebrationsite-preprod.tanishq.co.in/events/customer/[EVENT_ID]`
   - Should see form with:
     - ✅ "I'm attending Tanishq Celebration" heading
     - ✅ Full Name field
     - ✅ Your Phone Number field
     - ✅ RSO Name field
     - ✅ "This is my first time in Tanishq" checkbox
     - ✅ Submit button

4. **Test full flow:**
   - Fill form with test data
   - Click Submit
   - Should see success message
   - Check database: `events_attendees` table should have new record

## 📊 Impact

- **Severity:** 🔴 CRITICAL
- **Affected Feature:** QR Code Attendee Registration
- **Users Impacted:** All store managers creating events
- **Data Loss:** None (backend working, just frontend broken)
- **Fix Time:** 5-15 minutes

## 📝 Files Modified

### Local (Already Fixed) ✅
- `src/main/resources/static/events.html` - JavaScript/CSS references updated

### Server (Needs Deployment) ⏳
- WAR file needs to be rebuilt and redeployed with the fixed events.html

## 🎯 Quick Summary

**Problem:** Form not showing after scanning QR code
**Cause:** Wrong JavaScript file reference in events.html
**Fix:** Update events.html to reference correct JS file
**Status:** ✅ Fixed locally, ⏳ Needs deployment to server

---

**Need help?** Run `rebuild-qr-fix.bat` and follow the prompts!

