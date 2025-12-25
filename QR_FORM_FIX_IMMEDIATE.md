# IMMEDIATE FIX - QR Code Form Not Showing

## Quick Diagnosis

Run this in PowerShell to check what's wrong:

```powershell
# Check what JavaScript file the page is trying to load
$response = Invoke-WebRequest -Uri "https://celebrationsite-preprod.tanishq.co.in/events.html"
$response.Content | Select-String -Pattern "index-.*\.js"

# Check if the referenced JS file exists
# If you see "index-CLJQELnM.js", try:
Invoke-WebRequest -Method Head -Uri "https://celebrationsite-preprod.tanishq.co.in/static/assets/index-CLJQELnM.js"
# If you get 404, that's the problem!

# Check what JS files actually exist
Invoke-WebRequest -Uri "https://celebrationsite-preprod.tanishq.co.in/static/assets/" | Select-String -Pattern "index-.*\.js"
```

## The Problem

The `events.html` file is referencing a JavaScript file that doesn't exist on the server:
- **Looking for:** `index-CLJQELnM.js` ❌ 
- **Actual file:** `index-Bl1_SFlI.js` ✅

## Solution: Rebuild and Redeploy

### Step 1: Verify Local Fix
✅ Already done - `events.html` has been updated locally

### Step 2: Check if You Have Maven

```cmd
mvn --version
```

**If Maven is NOT installed:**
1. Download from: https://maven.apache.org/download.cgi
2. Extract to `C:\apache-maven-3.x.x`
3. Add to PATH: `C:\apache-maven-3.x.x\bin`
4. Restart terminal and try again

**OR use IntelliJ IDEA:**
1. Open project in IntelliJ IDEA
2. Right-click on `pom.xml`
3. Select: **Maven** → **Reload Project**
4. In Maven sidebar: **Lifecycle** → **clean**
5. Then: **Lifecycle** → **package**
6. Wait for build to complete
7. Find WAR file in: `target/tanishq-preprod-....war`

### Step 3: Upload and Deploy

**Option A: If you have WinSCP and SSH access**
1. Open WinSCP
2. Connect to: `10.160.128.94`
3. Navigate to: `/opt/tanishq/applications_preprod/`
4. Upload the new WAR file from `target/` folder
5. Use PuTTY or Windows Terminal to SSH:
   ```bash
   ssh root@10.160.128.94
   cd /opt/tanishq/applications_preprod
   sudo bash deploy-preprod.sh
   ```

**Option B: If someone else deploys**
Send them:
1. The new WAR file from `target/` folder
2. Instructions: "Please deploy this to preprod - it fixes the QR code form issue"

### Step 4: Verify

Open a new QR code URL in browser:
```
https://celebrationsite-preprod.tanishq.co.in/events/customer/TEST_[any-event-id]
```

You should see the form with:
- "I'm attending Tanishq Celebration"  
- Name, Phone, RSO fields
- Submit button

## Alternative: Quick Manual Fix on Server

If you can't rebuild, ask someone with server access to:

1. SSH to server
2. Find the deployed files:
   ```bash
   cd /opt/tanishq/applications_preprod
   find . -name "events.html"
   ```

3. Edit the file:
   ```bash
   nano ./[path-to-file]/events.html
   ```

4. Change line 17 from:
   ```html
   <script type="module" crossorigin src="/static/assets/index-CLJQELnM.js"></script>
   ```
   To:
   ```html
   <script type="module" crossorigin src="/static/assets/index-Bl1_SFlI.js"></script>
   ```

5. Change line 18 from:
   ```html
   <link rel="stylesheet" crossorigin href="/static/assets/index-CjU3bZCB.css">
   ```
   To:
   ```html
   <link rel="stylesheet" crossorigin href="/static/assets/index-DRK0HUpC.css">
   ```

6. Save and restart:
   ```bash
   ps -ef | grep tanishq
   kill [PID]
   cd /opt/tanishq/applications_preprod
   nohup java -jar -Dspring.profiles.active=preprod tanishq-*.war > app.log 2>&1 &
   ```

## Summary

✅ Local fix applied
📦 Need to: Rebuild WAR and redeploy to server
⏱️ Time: 5-15 minutes depending on deployment access


