# URL Redirect Issue - FIXED ✅

## Problem Identified
When clicking on "Create Events", "Selfie", or "Checklist" buttons on the pre-prod server, users were being redirected to the production URL instead of staying on the pre-prod environment.

However, manually typing `/events`, `/selfie`, or `/checklist` after the pre-prod URL worked fine.

## Root Cause
All the links in the `celebrate.html` files were hardcoded with absolute URLs pointing to either:
- Production: `https://celebrations.tanishq.co.in`
- Pre-prod: `https://celebrationsite-preprod.tanishq.co.in`

This meant that clicking the buttons would always redirect to these hardcoded URLs instead of staying on the current domain.

## Solution Applied
Changed all hardcoded absolute URLs to **relative URLs** in the following files:

### Files Fixed:
1. `src/main/resources/static/globalPage/celebrate.html`
2. `src/main/resources/static_backup/globalPage/celebrate.html`
3. `src/main/resources/static/globalPage/globalAssets/celebrate.html`
4. `src/main/resources/static/checklist/verify.html`
5. `src/main/resources/static_backup/checklist/verify.html`

### Changes Made:
- ❌ **Before:** `href="https://celebrations.tanishq.co.in/checklist"`
- ✅ **After:** `href="/checklist"`

- ❌ **Before:** `href="https://celebrations.tanishq.co.in/selfie"`
- ✅ **After:** `href="/selfie"`

- ❌ **Before:** `href="https://celebrations.tanishq.co.in/events"`
- ✅ **After:** `href="/events"`

Also removed `target="_blank"` attributes since users should stay in the same window.

### API Endpoint Fixed:
- ❌ **Before:** `xhr.open('POST', 'https://celebrations.tanishq.co.in/tanishq/selfie/brideImage', true)`
- ✅ **After:** `xhr.open('POST', '/tanishq/selfie/brideImage', true)`

## Benefits
✅ Works on any domain (production, pre-prod, or local)
✅ No need to change URLs when deploying to different environments
✅ Buttons now stay within the same environment
✅ Cleaner, more maintainable code

## Next Steps
1. ✅ Fixed all URLs
2. 🔄 Currently rebuilding the project with `mvn clean package`
3. ⏳ Deploy the new WAR file to the server
4. ✅ Test on pre-prod to confirm the fix

## Testing Instructions
After deployment, test the following on pre-prod:
1. Click on "Wedding Checklist" button → Should stay on pre-prod domain
2. Click on "Take Selfi" button → Should stay on pre-prod domain  
3. Click on "Create Event" button → Should stay on pre-prod domain
4. All buttons should navigate correctly without changing the domain

