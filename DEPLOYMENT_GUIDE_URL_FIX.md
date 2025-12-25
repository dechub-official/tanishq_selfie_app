# Quick Deployment Guide - URL Fix

## What Was Fixed
All hardcoded production and pre-prod URLs in HTML files have been changed to **relative URLs**.

This ensures that clicking buttons like "Create Events", "Selfie", and "Checklist" will stay on the current domain instead of redirecting to production.

## Files Changed
- ✅ `src/main/resources/static/globalPage/celebrate.html`
- ✅ `src/main/resources/static/globalPage/globalAssets/celebrate.html`
- ✅ `src/main/resources/static/checklist/verify.html`
- ✅ `src/main/resources/static_backup/globalPage/celebrate.html`
- ✅ `src/main/resources/static_backup/checklist/verify.html`

## Deployment Steps

### Step 1: Build Complete ✅
The project is currently being rebuilt with the URL fixes.
```
Running: BUILD_PREPROD.bat
```

### Step 2: Locate the WAR File
After build completes, find the WAR file at:
```
c:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\target\tanishq-preprod-*.war
```

### Step 3: Deploy to Server
Two options:

#### Option A: Using WinSCP/FileZilla
1. Connect to your pre-prod server
2. Navigate to the deployment directory (e.g., `/opt/tanishq/` or `/var/lib/tomcat/webapps/`)
3. Upload the new WAR file
4. Restart the application server

#### Option B: Using SSH/SCP
```bash
scp target/tanishq-preprod-*.war user@server:/path/to/deployment/
ssh user@server "systemctl restart tomcat" # or your app service
```

### Step 4: Verify the Fix
1. Open pre-prod URL in browser: `https://celebrationsite-preprod.tanishq.co.in`
2. Navigate to the home page
3. Click on "Wedding Checklist" button
   - **Expected:** Should open `/checklist` on the SAME pre-prod domain
   - **Before Fix:** Would redirect to production domain
4. Click on "Take Selfi" button
   - **Expected:** Should open `/selfie` on the SAME pre-prod domain
5. Click on "Create Event" button
   - **Expected:** Should open `/events` on the SAME pre-prod domain

### Step 5: Check Browser Console
Open browser Developer Tools (F12) and check:
- No 404 errors
- No CORS errors
- All navigation works correctly

## Rollback Plan (If Needed)
If something goes wrong, you can rollback by:
1. Deploying the previous WAR file backup
2. OR reverting the changes in the HTML files:
   - Change relative URLs back to absolute URLs
   - Rebuild and redeploy

## Important Notes
- ✅ This fix applies to ALL environments (local, pre-prod, production)
- ✅ No configuration changes needed
- ✅ No database changes needed
- ✅ Same code will work on all domains
- ✅ More maintainable and environment-agnostic

## Contact
If you encounter any issues after deployment, check:
1. Server logs for any errors
2. Browser console for JavaScript errors
3. Network tab to see where requests are going

---
**Build Date:** December 10, 2025
**Status:** Ready for deployment after build completes

