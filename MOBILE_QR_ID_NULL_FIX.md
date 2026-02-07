# Fix for Mobile QR ID Null Issue

## Problem
**Error on Mobile**: `Failed to submit message: 400 'Greeting not found: null'`
**Works on Desktop**: The same form submission works fine on desktop browsers

## Root Cause
The `qrId` is being passed as `null` or `undefined` from the frontend on mobile devices, causing the backend to look for a greeting with ID "null", which doesn't exist.

The error occurs because:
1. The frontend is sending the request to `/greetings/null/upload` or `/greetings/undefined/upload`
2. The backend receives "null" as a string in the path variable
3. The database lookup fails because no greeting exists with ID "null"

## Backend Fix Applied

### File: `src/main/java/com/dechub/tanishq/controller/GreetingController.java`

#### Change 1: Upload Endpoint Validation
Added validation to detect null/undefined IDs:

```java
@PostMapping(path = "/{uniqueId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<String> uploadVideo(
        @PathVariable String uniqueId,
        @RequestParam("video") MultipartFile videoFile,
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "message", required = false) String message) {

    log.info("=== GREETING UPLOAD REQUEST ===");
    log.info("UniqueId: {}", uniqueId);
    // ... other logs ...

    try {
        // NEW VALIDATION: Check for null/undefined string values
        if (uniqueId == null || uniqueId.trim().isEmpty() || 
            "null".equalsIgnoreCase(uniqueId.trim()) || 
            "undefined".equalsIgnoreCase(uniqueId.trim())) {
            
            log.error("Invalid uniqueId received: '{}' - Frontend qrId not set properly", uniqueId);
            return ResponseEntity.badRequest()
                .body("Invalid greeting ID. Please scan the QR code again or refresh the page.");
        }

        // ... rest of validation ...
    }
}
```

#### Change 2: View Endpoint Validation
Added the same validation to the view endpoint:

```java
@GetMapping("/{uniqueId}/view")
public ResponseEntity<?> getGreetingInfo(@PathVariable String uniqueId) {
    log.info("Received view request for greeting: {}", uniqueId);
    
    try {
        // NEW VALIDATION: Check for null/undefined string values
        if (uniqueId == null || uniqueId.trim().isEmpty() || 
            "null".equalsIgnoreCase(uniqueId.trim()) || 
            "undefined".equalsIgnoreCase(uniqueId.trim())) {
            
            log.error("Invalid uniqueId for view: '{}' - Frontend qrId not set", uniqueId);
            return ResponseEntity.badRequest()
                .body("Invalid greeting ID. Please scan the QR code again or refresh the page.");
        }

        // ... rest of the code ...
    }
}
```

## What This Fix Does

### Better Error Messages
Instead of the cryptic:
- ❌ `Failed to submit message: 400 'Greeting not found: null'`

Users will now see:
- ✅ `Invalid greeting ID. Please scan the QR code again or refresh the page.`

### Improved Logging
The backend now logs:
```
ERROR: Invalid uniqueId received: 'null' - This indicates a frontend issue where qrId is not being set properly
```

This helps identify that the problem is in the frontend state management, not the backend.

### Validation Checks
- Checks if `uniqueId` is null (Java null)
- Checks if `uniqueId` is empty string
- Checks if `uniqueId` is the string "null" (case-insensitive)
- Checks if `uniqueId` is the string "undefined" (case-insensitive)

## Frontend Issue (NOT FIXED - REQUIRES INVESTIGATION)

The real issue is in the frontend where `qrId` is not being properly set in the VideoContext/state on mobile browsers. 

### Likely Causes:
1. **State persistence issue**: Mobile browsers may clear context/state differently than desktop
2. **Navigation issue**: QR ID might not be passed correctly through navigation on mobile
3. **Session storage**: Mobile browsers might handle sessionStorage/localStorage differently
4. **Context Provider**: The VideoContext might not be wrapping the form component properly on mobile

### What to Check in Frontend:
1. Check if `qrId` is set in VideoContext when navigating to the form
2. Verify the QR scanning flow properly sets the `qrId`
3. Check browser console for any JavaScript errors on mobile
4. Verify navigation state is passed correctly
5. Check if context is being reset on mobile browser lifecycle events

## Testing Instructions

### 1. Build and Deploy
```bash
mvn clean package -P preprod
```

### 2. Test on Mobile (Error Should be Clearer Now)
1. Open the form on mobile without scanning QR (simulate the bug)
2. Try to submit
3. You should now see: "Invalid greeting ID. Please scan the QR code again or refresh the page."
4. Check server logs - should see: `ERROR: Invalid uniqueId received: 'null'`

### 3. Check Server Logs
Look for these log entries:
```
=== GREETING UPLOAD REQUEST ===
UniqueId: null
ERROR: Invalid uniqueId received: 'null' - This indicates a frontend issue where qrId is not being set properly
```

## Next Steps

### To Fully Fix (Frontend Changes Required):
The backend fix provides better error messages and logging, but the frontend still needs to be fixed to properly manage the `qrId` on mobile.

**Frontend files to investigate:**
- VideoContext provider
- QR scanning component
- Form component (`UserForm.tsx` or similar)
- Navigation/routing logic
- State management for `qrId`

## Status
✅ **Backend**: Fixed - Better validation and error messages
❌ **Frontend**: NOT FIXED - Still needs investigation for why qrId is null on mobile
⚠️ **Impact**: Desktop continues to work, mobile now gets clearer error message instead of confusing "Greeting not found: null"

## No Impact on Existing Functionality
- ✅ Desktop submission: Still works
- ✅ Valid QR scans: Still work
- ✅ Video upload: Still works when qrId is valid
- ✅ All other endpoints: Unaffected

