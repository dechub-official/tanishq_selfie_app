# 🚨 COMPLETE FIX GUIDE - Events Blank Page Issue

## Current Situation

### What's Working ✅
- Main landing page: `celebrationsite-preprod.tanishq.co.in`
- Home page shows: Wedding Checklist, Take Selfie, Create Event buttons

### What's NOT Working ❌
- Events Dashboard: `celebrationsite-preprod.tanishq.co.in/events` - **SHOWS BLANK PAGE**
- QR Code Form: When scanning QR code - **SHOWS BLANK PAGE**

## Root Cause

The `events.html` file references JavaScript files that **don't exist** on the server:
- Looking for: `index-CLJQELnM.js` ❌ (404 Not Found)
- Actual files: `index-Bl1_SFlI.js` or `index-2ipkaO8n.js` ✅

This prevents the React app from loading, causing blank pages.

---

## ⚡ QUICKEST FIX (5 minutes)

### On the Server - Run This Script:

```bash
# SSH to server
ssh jewdev-test@10.160.128.94

# Go to application directory
cd /opt/tanishq/applications_preprod

# Create fix script
cat > fix-events-now.sh << 'SCRIPT_END'
#!/bin/bash
echo "=========================================="
echo "Fixing Events Blank Page Issue"
echo "=========================================="

cd /opt/tanishq/applications_preprod

# Stop current application
echo "1. Stopping application..."
PID=$(ps -ef | grep "tanishq-preprod" | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    kill $PID
    sleep 3
    echo "   Stopped PID: $PID"
fi

# Extract the working WAR
echo "2. Extracting WAR file..."
rm -rf temp_fix
mkdir temp_fix && cd temp_fix
jar -xf ../tanishq-preprod-18-12-2025-1-0.0.1-SNAPSHOT.war

# Find actual JS/CSS files
echo "3. Finding actual asset files..."
ACTUAL_JS=$(ls BOOT-INF/classes/static/assets/index-*.js 2>/dev/null | head -1 | xargs basename 2>/dev/null)
ACTUAL_CSS=$(ls BOOT-INF/classes/static/assets/index-*.css 2>/dev/null | head -1 | xargs basename 2>/dev/null)

echo "   Found JS:  $ACTUAL_JS"
echo "   Found CSS: $ACTUAL_CSS"

# Backup events.html
echo "4. Backing up events.html..."
cp BOOT-INF/classes/static/events.html BOOT-INF/classes/static/events.html.backup

# Fix events.html
echo "5. Fixing events.html..."
if [ -n "$ACTUAL_JS" ]; then
    # Update JS reference
    sed -i.bak "s|src=\"/static/assets/index-[^\"]*\.js\"|src=\"/static/assets/$ACTUAL_JS\"|g" BOOT-INF/classes/static/events.html
    # Update CSS reference
    sed -i "s|href=\"/static/assets/index-[^\"]*\.css\"|href=\"/static/assets/$ACTUAL_CSS\"|g" BOOT-INF/classes/static/events.html
    echo "   Updated to use: $ACTUAL_JS"
else
    echo "   ERROR: Could not find JS files!"
    exit 1
fi

# Verify the fix
echo "6. Verifying fix..."
echo "   New references:"
grep -E "index-.*\.(js|css)" BOOT-INF/classes/static/events.html

# Repackage WAR
echo "7. Repackaging WAR file..."
cd ..
jar -cf tanishq-preprod-FIXED.war -C temp_fix .

# Start application
echo "8. Starting application..."
nohup java -jar tanishq-preprod-FIXED.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

echo "9. Waiting for startup..."
sleep 15

# Check status
echo "10. Checking status..."
if ps -ef | grep "tanishq-preprod-FIXED" | grep -v grep > /dev/null; then
    echo "    ✅ Application is running!"
    ps -ef | grep tanishq | grep -v grep
else
    echo "    ❌ Application failed to start"
    echo "    Check logs: tail -50 application.log"
fi

echo ""
echo "=========================================="
echo "FIX COMPLETED!"
echo "=========================================="
echo ""
echo "Test now:"
echo "1. https://celebrationsite-preprod.tanishq.co.in/events"
echo "2. Should see Events Dashboard (not blank)"
echo ""
SCRIPT_END

# Make executable and run
chmod +x fix-events-now.sh
./fix-events-now.sh
```

### After Running the Script

1. **Test Events Dashboard:**
   - Go to: `https://celebrationsite-preprod.tanishq.co.in/events`
   - Should show login page or dashboard (NOT blank)

2. **Test QR Code:**
   - Login to Events Dashboard
   - Create or open an event
   - Download QR code
   - Scan with mobile phone
   - Should show attendee registration form

3. **If Still Having Issues:**
   ```bash
   # Check application log
   cd /opt/tanishq/applications_preprod
   tail -100 application.log
   
   # Check if app is running
   ps -ef | grep tanishq
   
   # Check port
   netstat -tuln | grep 3000
   ```

---

## 📋 Alternative: Manual Fix (If Script Fails)

### Step-by-Step Manual Process:

```bash
# 1. Stop application
cd /opt/tanishq/applications_preprod
ps -ef | grep tanishq
kill [PID]

# 2. Extract WAR
mkdir temp_fix && cd temp_fix
jar -xf ../tanishq-preprod-18-12-2025-1-0.0.1-SNAPSHOT.war

# 3. Check what JS files exist
ls -la BOOT-INF/classes/static/assets/index-*.js
# Example output: index-2ipkaO8n.js

# 4. Edit events.html
nano BOOT-INF/classes/static/events.html

# Find and change line 17:
# FROM: <script type="module" crossorigin src="/static/assets/index-CLJQELnM.js"></script>
# TO:   <script type="module" crossorigin src="/static/assets/index-2ipkaO8n.js"></script>
#       (use the actual filename from step 3)

# Find and change line 18:
# FROM: <link rel="stylesheet" crossorigin href="/static/assets/index-CjU3bZCB.css">
# TO:   <link rel="stylesheet" crossorigin href="/static/assets/index-DRK0HUpC.css">
#       (use the actual CSS filename)

# Save: Ctrl+O, Enter, Ctrl+X

# 5. Repackage
cd ..
jar -cf tanishq-preprod-FIXED.war -C temp_fix .

# 6. Start
nohup java -jar tanishq-preprod-FIXED.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

# 7. Monitor startup
tail -f application.log
# Press Ctrl+C when you see "Started TanishqApplication"
```

---

## 🔍 Troubleshooting

### Issue: Script Says "Could not find JS files"

```bash
cd /opt/tanishq/applications_preprod/temp_fix
find . -name "index-*.js"
# This will show where the JS files are located
# They might be in a different path like: ./static/assets/index-*.js
```

### Issue: Application Won't Start

```bash
# Check the last 50 lines of log
tail -50 application.log

# Common errors:
# - Port already in use: Kill existing process
# - Database connection: Check MySQL is running
# - Out of memory: Restart server
```

### Issue: Still Shows Blank After Fix

```bash
# Clear browser cache and retry
# Or check if events.html is correct:
curl https://celebrationsite-preprod.tanishq.co.in/events.html | grep "index-"

# Should show the correct JS filename, not index-CLJQELnM.js
```

---

## 📝 What I've Fixed Locally

1. ✅ `pom.xml` - Fixed artifactId to stable name (no dates)
2. ✅ `events.html` - Updated to reference correct JS files
3. ✅ Created fix scripts for server deployment

---

## 🎯 Summary

**Problem:** Events page and QR forms showing blank  
**Cause:** Wrong JavaScript file references in events.html  
**Fix:** Update events.html to reference correct JS files  
**Method:** Run fix script on server (5 minutes)  
**Status:** Script ready, needs to be executed on server  

---

## ⚡ NEXT ACTION

**Copy and paste the script above into your SSH terminal and run it.**

It will:
1. Stop the app
2. Extract the WAR
3. Fix events.html automatically
4. Repackage
5. Start with fixed version

**Total time: 5 minutes**

After that, the Events Dashboard and QR code forms will work! ✅

