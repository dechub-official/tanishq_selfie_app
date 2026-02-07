# QR ID NULL Issue - Diagnosis and Solution

## Problem
User is getting error: **"Failed to submit message: 400 'Greeting not found: null'"**

## Root Cause Analysis

### 1. URL Mismatch
The QR code generates URL:
```
https://celebrations.tanishq.co.in/qr/create-video?id=GREETING_XXX
```

But screenshot shows different domain:
```
https://orations.tanishq.co.in/...
```

### 2. QR ID is NULL
- Frontend is sending `null` as the qrId to backend
- This means the frontend cannot extract the ID from the URL properly
- Either:
  - Wrong URL format (missing `?id=` parameter)
  - Frontend routing not configured correctly
  - User accessed wrong URL

## Solution Steps

### Step 1: Verify the Actual URL Being Used
1. Check what URL is in the QR code
2. Check what URL the user is accessing
3. Ensure they match the expected format: `https://celebrations.tanishq.co.in/qr/create-video?id=GREETING_XXX`

### Step 2: Test the Frontend
1. Open browser and go to: `https://celebrations.tanishq.co.in/qr/create-video?id=TEST_123`
2. Open Developer Console (F12)
3. Check if qrId is being extracted: Look for console logs or check the state
4. Try submitting form and check Network tab for the POST request
5. Verify the URL path in the POST request includes the qrId

### Step 3: Verify Backend is Receiving Correct Data
Check the backend logs when submitting. It should show:
```
=== GREETING UPLOAD REQUEST ===
UniqueId: GREETING_XXX  <-- Should NOT be "null"
```

## Quick Test Commands

### Test 1: Check if Frontend is Deployed Correctly
```bash
curl https://celebrations.tanishq.co.in/qr/index.html
```
Should return the React app HTML.

### Test 2: Check if Backend Endpoint Works
```bash
# Replace ACTUAL_GREETING_ID with a real greeting ID from database
curl -X GET "https://celebrations.tanishq.co.in/api/greeting/ACTUAL_GREETING_ID/view"
```
Should return greeting data.

### Test 3: Test Upload Endpoint Directly
```bash
# This will fail with validation error but confirms endpoint is reachable
curl -X POST "https://celebrations.tanishq.co.in/api/greeting/TEST_ID/upload" \
  -H "Content-Type: multipart/form-data"
```

## Common Issues and Fixes

### Issue 1: Domain Mismatch
**If QR points to wrong domain:**
- Option A: Generate new QR codes with correct domain
- Option B: Set up redirect from old domain to new domain

### Issue 2: Frontend Not Extracting ID
**If frontend can't read URL parameter:**
- Verify the frontend is built correctly
- Check if there's a routing configuration issue
- Ensure the frontend code handles `?id=XXX` parameter

### Issue 3: NULL Being Sent
**If frontend explicitly sends null:**
- This means URL parameter extraction failed
- Need to debug frontend code to see why `qrId` state is not being set

## What to Check Right Now

1. **Get the actual URL from the QR code:**
   - Scan the QR code with your phone
   - Check what URL it opens
   - Share that URL

2. **Check backend logs:**
   ```bash
   # On server, check recent logs
   tail -f /path/to/logs/application.log
   ```
   
3. **Test with browser:**
   - Open: `https://celebrations.tanishq.co.in/qr/create-video?id=GREETING_123456`
   - Open Developer Tools (F12)
   - Try to submit the form
   - Check the Network tab for the POST request
   - Share the request URL and payload

## Expected Frontend Behavior

The frontend should:
1. Extract `id` from URL query parameter
2. Store it in state as `qrId`
3. When submitting, POST to: `/api/greeting/{qrId}/upload`
4. Include video file, name, and message in form data

## Quick Fix Attempt

If the issue is just the frontend not reading the URL parameter, you can:

1. **Verify URL format** - Ensure QR codes use: `?id=XXX` format
2. **Test manually** - Navigate to the correct URL with a valid greeting ID
3. **Check form submission** - Ensure the POST URL includes the greeting ID

## Files to Check

1. Frontend: `/src/main/resources/static/qr/assets/index-*.js` (minified)
2. Backend Controller: `GreetingController.java` line 83-100
3. Backend Service: `GreetingService.java` line 73-74
4. QR Generation: `GreetingService.java` line 83

## Next Steps

Please provide:
1. The actual URL from a QR code (scan one and copy the URL)
2. Screenshot of browser Developer Tools Network tab showing the failed POST request
3. Backend logs from the time of the error

This will help identify exactly where the disconnect is happening.

