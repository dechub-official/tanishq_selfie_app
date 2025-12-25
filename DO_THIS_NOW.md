# 🎯 SIMPLE FIX - DO THIS NOW

## The Problem
QR code form not showing because events.html references wrong JavaScript files.

## The Fix (Already Done Locally) ✅
The file `events.html` has been updated to reference the correct JavaScript files.

## What You Need To Do NOW

### Step 1: Rebuild the Application

**EASIEST WAY - Use IntelliJ IDEA:**

1. Open this project in IntelliJ IDEA (if not already open)
2. Click on "Maven" tab on the right side
3. Find "Lifecycle" folder and expand it
4. Double-click on "clean" → Wait for it to complete
5. Double-click on "package" → Wait for "BUILD SUCCESS"
6. Done! New WAR file is in the `target` folder

**Alternative - Use command line (if Maven is installed):**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -DskipTests
```

### Step 2: Deploy to Server

**Option A - You have server access:**
1. Open WinSCP → Connect to `10.160.128.94`
2. Go to `/opt/tanishq/applications_preprod/`
3. Upload the WAR file from `target` folder
4. SSH to server: `ssh root@10.160.128.94`
5. Run: `cd /opt/tanishq/applications_preprod && sudo bash deploy-preprod.sh`

**Option B - Someone else deploys:**
Send them the WAR file from the `target` folder with message:
"Please deploy this to preprod - fixes QR code form issue"

### Step 3: Test

1. Open Events Dashboard
2. Open any event
3. Download QR code
4. Scan with phone
5. **Form should appear!** ✅

---

## Current Status
✅ Local code fixed
⏳ Needs rebuild and deployment
⏱️ Time: 5-10 minutes

**Just do Step 1 above and you're 80% done!**

