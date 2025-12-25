# 🚨 URGENT FIX - Events Page Showing Blank

## Problem
After clicking "Create Event", the page `/events` shows BLANK instead of the Events Dashboard.

## Root Cause
Same issue as QR code - `events.html` references wrong JavaScript files that don't exist.

## FASTEST FIX - Run These Commands on Server

### Copy this script to server and run it:

```bash
# SSH to server
ssh jewdev-test@10.160.128.94

# Navigate to app directory
cd /opt/tanishq/applications_preprod

# Create and run fix script
cat > quick-fix.sh << 'EOF'
#!/bin/bash
cd /opt/tanishq/applications_preprod

# Stop application
PID=$(ps -ef | grep tanishq-preprod | grep -v grep | awk '{print $2}')
[ -n "$PID" ] && kill $PID && sleep 3

# Extract WAR
rm -rf temp_fix
mkdir temp_fix && cd temp_fix
jar -xvf ../tanishq-preprod-18-12-2025-1-0.0.1-SNAPSHOT.war

# Find actual JS/CSS files
ACTUAL_JS=$(ls BOOT-INF/classes/static/assets/index-*.js 2>/dev/null | head -1 | xargs basename)
ACTUAL_CSS=$(ls BOOT-INF/classes/static/assets/index-*.css 2>/dev/null | head -1 | xargs basename)

echo "Found JS: $ACTUAL_JS"
echo "Found CSS: $ACTUAL_CSS"

# If files don't exist, use the ones we know work
if [ -z "$ACTUAL_JS" ]; then
    ACTUAL_JS="index-Bl1_SFlI.js"
    ACTUAL_CSS="index-DRK0HUpC.css"
fi

# Backup and fix events.html
cp BOOT-INF/classes/static/events.html BOOT-INF/classes/static/events.html.backup

# Replace JS reference
sed -i "s|src=\"/static/assets/index-[^\"]*\.js\"|src=\"/static/assets/$ACTUAL_JS\"|g" BOOT-INF/classes/static/events.html

# Replace CSS reference  
sed -i "s|href=\"/static/assets/index-[^\"]*\.css\"|href=\"/static/assets/$ACTUAL_CSS\"|g" BOOT-INF/classes/static/events.html

# Verify
echo ""
echo "Updated references:"
grep "index-" BOOT-INF/classes/static/events.html

# Repackage
cd ..
jar -cvf tanishq-preprod-FIXED.war -C temp_fix .

# Start fixed version
nohup java -jar tanishq-preprod-FIXED.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

sleep 5
echo ""
echo "Application started!"
ps -ef | grep tanishq | grep -v grep
EOF

chmod +x quick-fix.sh
./quick-fix.sh
```

### OR Manual Steps (If Script Fails):

```bash
cd /opt/tanishq/applications_preprod

# 1. Stop app
ps -ef | grep tanishq
kill [PID_FROM_ABOVE]

# 2. Extract
mkdir temp_fix && cd temp_fix
jar -xvf ../tanishq-preprod-18-12-2025-1-0.0.1-SNAPSHOT.war

# 3. Check what JS files exist
ls -la BOOT-INF/classes/static/assets/index-*.js
# Note the filename, e.g., index-2ipkaO8n.js

# 4. Edit events.html
nano BOOT-INF/classes/static/events.html

# Find this line (around line 17):
<script type="module" crossorigin src="/static/assets/index-XXXXXX.js"></script>

# Change XXXXXX to match the actual filename from step 3

# Also fix CSS line (around line 18):
<link rel="stylesheet" crossorigin href="/static/assets/index-YYYYYY.css">

# Change YYYYYY to match the actual CSS filename

# Save: Ctrl+O, Enter, Ctrl+X

# 5. Repackage
cd ..
jar -cvf tanishq-preprod-FIXED.war -C temp_fix .

# 6. Start
nohup java -jar tanishq-preprod-FIXED.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

# 7. Verify
sleep 10
ps -ef | grep tanishq
tail -f application.log
```

## Verification

1. **Check main page loads:**
   ```
   https://celebrationsite-preprod.tanishq.co.in/
   ```
   Should show: Wedding Checklist, Take Selfie, Create Event buttons ✅

2. **Check Events Dashboard loads:**
   ```
   https://celebrationsite-preprod.tanishq.co.in/events
   ```
   Should show: Events login/dashboard (NOT BLANK) ✅

3. **Check QR code form:**
   - Create an event
   - Download QR code
   - Scan with phone
   - Should show attendee form ✅

## If Still Blank After Fix

Check these:

```bash
# Check if JS file exists
curl -I https://celebrationsite-preprod.tanishq.co.in/static/assets/index-Bl1_SFlI.js
# Should return: 200 OK

# Check events.html content
curl https://celebrationsite-preprod.tanishq.co.in/events.html | grep "index-"
# Should show the correct JS filename

# Check application logs
tail -100 application.log
# Look for errors
```

## Alternative: Use Old Working Assets

If the current assets are corrupted, you can copy working assets from a backup:

```bash
cd /opt/tanishq/applications_preprod/temp_fix/BOOT-INF/classes/static

# Check if assets directory exists and has files
ls -la assets/

# If assets are missing or corrupted, you may need to:
# 1. Copy from a working backup
# 2. Or rebuild the frontend and redeploy
```

## Time Estimate
- Using script: 2-3 minutes
- Manual steps: 5-10 minutes

## Current Status
- ❌ Events page showing blank
- ❌ QR code form not working  
- ✅ Main landing page working
- ✅ Other features working

**ACTION NEEDED: Run the fix script on server NOW**

