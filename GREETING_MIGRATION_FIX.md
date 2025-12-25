# Greeting Feature Migration Fix

## Date: December 20, 2025

## Context
Migrated greeting feature from Google Sheets + Google Drive to MySQL + AWS S3.
The new implementation deviated from the old behavior in QR code generation.

## Issue Found
❌ **QR code was encoding a full URL** (`greetingBaseUrl + uniqueId + "/upload"`)
✅ **Should encode ONLY the uniqueId** (frontend decides navigation)

## Fix Applied

### File Changed: `GreetingService.java`
**Location:** Line 75-89 in `generateQrCode()` method

**Before:**
```java
// Create URL for upload page
String uploadUrl = greetingBaseUrl + uniqueId + "/upload";
log.debug("Generating QR code for URL: {}", uploadUrl);

// Generate QR code
byte[] qrCodeImage = qrCodeService.generateQrCodeImage(uploadUrl, 300, 300);
```

**After:**
```java
// Encode ONLY the uniqueId (old behavior - frontend decides navigation)
log.debug("Generating QR code for uniqueId: {}", uniqueId);

// Generate QR code with just the uniqueId
byte[] qrCodeImage = qrCodeService.generateQrCodeImage(uniqueId, 300, 300);
```

## Verification Checklist

### ✅ Behavior Preserved
1. **QR Code Generation**
   - ✅ QR encodes only `uniqueId` (not full URL)
   - ✅ Frontend decides navigation after QR scan
   - ✅ Endpoint remains: `GET /greetings/{uniqueId}/qr`

2. **Greeting State Management**
   - ✅ MySQL stores: uniqueId, uploaded, driveFileId (now S3 URL), name, message
   - ✅ Same field structure as Google Sheets
   - ✅ `driveFileId` repurposed to store S3 URL

3. **View Endpoint Behavior** (`GET /greetings/{uniqueId}/view`)
   - ✅ Returns `pending` when `uploaded == false`
   - ✅ Returns `completed` with video URL when `uploaded == true`
   - ✅ Response structure unchanged

4. **Upload Endpoint**
   - ✅ Endpoint remains: `POST /greetings/{uniqueId}/upload`
   - ✅ Accepts video file, name, message
   - ✅ Uploads to S3 (replaces Google Drive)
   - ✅ Sets `uploaded = true` after successful upload

### ✅ No Breaking Changes
- ❌ No endpoint URLs changed
- ❌ No HTTP methods changed
- ❌ No request/response structures changed
- ❌ No frontend routing logic affected
- ❌ No unrelated code refactored

## Migration Mapping

| Component | Old (Google) | New (MySQL + S3) | Status |
|-----------|--------------|------------------|--------|
| Greeting ID | Sheet Row ID | MySQL uniqueId | ✅ Same |
| Video Storage | Google Drive | AWS S3 | ✅ Works |
| Video Reference | Drive File ID | S3 URL in driveFileId field | ✅ Compatible |
| QR Code Content | uniqueId only | uniqueId only | ✅ **Fixed** |
| Upload Status | Sheet Column | MySQL uploaded boolean | ✅ Same |
| View Behavior | pending/completed | pending/completed | ✅ Same |

## Testing Steps

1. **Create Greeting**
   ```bash
   curl -X POST http://localhost:8080/greetings/generate
   # Expected: Returns uniqueId (e.g., "GREETING_1734700000000")
   ```

2. **Generate QR Code**
   ```bash
   curl -X GET http://localhost:8080/greetings/{uniqueId}/qr --output qr.png
   # Expected: PNG file with QR encoding ONLY the uniqueId
   ```

3. **Check View (Before Upload)**
   ```bash
   curl -X GET http://localhost:8080/greetings/{uniqueId}/view
   # Expected: {"hasVideo": false, "status": "pending", ...}
   ```

4. **Upload Video**
   ```bash
   curl -X POST http://localhost:8080/greetings/{uniqueId}/upload \
     -F "video=@test.mp4" \
     -F "name=John Doe" \
     -F "message=Happy Birthday!"
   # Expected: Success message with S3 URL
   ```

5. **Check View (After Upload)**
   ```bash
   curl -X GET http://localhost:8080/greetings/{uniqueId}/view
   # Expected: {"hasVideo": true, "status": "completed", "videoPlaybackUrl": "https://...", ...}
   ```

## Summary

**✅ MINIMAL CHANGE APPLIED**
- Only 1 file modified: `GreetingService.java`
- Only 1 method affected: `generateQrCode()`
- Only 5 lines changed (removed full URL construction, kept uniqueId only)

**✅ OLD BEHAVIOR RESTORED**
- QR codes now encode only uniqueId (matching Google Sheets version)
- Frontend decides navigation (no backend URL hardcoding)
- All endpoints and behaviors unchanged

**✅ MIGRATION COMPLETE**
- Google Sheets → MySQL ✅
- Google Drive → AWS S3 ✅
- QR behavior → Restored ✅

## Notes
- The `greetingBaseUrl` field is now unused (safe to remove in future cleanup)
- No database schema changes required
- No frontend changes required
- Backend is now 100% compatible with old frontend expectations

