# Event Creation JSON API Fix

## Problem Summary

The frontend was sending event creation data as **JSON** in the request body:
```json
{
  "storeCode": "TEST",
  "eventName": "Onam",
  "eventType": "FESTIVAL CELEBRATION",
  "eventSubType": "Onam",
  "date": "2026-03-26",
  "time": "",
  "location": "In store",
  "image": "/static/assets/event2-B8cJ3Wja.png"
}
```

However, the backend `/events/upload` endpoint was expecting **form-data** with `@RequestParam` annotations, causing all parameters (including `eventName`) to be `null`, triggering the validation error: **"Event name is required"**.

## Root Causes

1. **Endpoint Mismatch**: Frontend sends JSON, backend expects form-data
2. **Empty Time Field**: Frontend sends `time: ""` but backend required it to be non-empty
3. **Parameter Mapping**: `@RequestParam` cannot map JSON body fields

## Solution Implemented

### 1. Created New DTO for JSON Requests

**File**: `EventCreationRequest.java`

- Accepts JSON payload from frontend
- Handles field name differences (e.g., `RSO`, `Community` with capitals)
- Validates `eventName` with `@NotBlank` annotation
- Makes `time` field optional
- Converts string booleans ("true"/"false") to actual booleans

### 2. Created New JSON Endpoint

**Endpoint**: `POST /events/upload-json`

**File**: `EventsController.java`

```java
@PostMapping(path = "/upload-json", produces = "application/json", consumes = "application/json")
public QrResponseDTO storeEventsDetailsJson(
        @Valid @RequestBody EventCreationRequest request,
        HttpSession session
)
```

**Features**:
- ✅ Accepts JSON payload with `@RequestBody`
- ✅ Validates `eventName` automatically via `@Valid` annotation
- ✅ Provides default time value `"00:00"` if empty
- ✅ Includes all security validations (authentication, authorization)
- ✅ Maps JSON request to internal `EventsDetailDTO`
- ✅ Properly handles the `eventName` sent by frontend

### 3. Made Time Field Optional

**Changes**:
1. Removed `@NotBlank` from `EventsDetailDTO.startTime` field
2. Added default value handling in both endpoints:
   ```java
   if (time == null || time.trim().isEmpty()) {
       time = "00:00"; // Default time
   }
   ```
3. Removed time validation from `validateUploadInputs()` method

### 4. Maintained Backward Compatibility

The original `/events/upload` endpoint still works for:
- Form-data requests
- File uploads (multipart/form-data)
- Legacy frontend code

## Frontend Change Required

**Update the API endpoint URL from**:
```javascript
// OLD - sends JSON to form-data endpoint
POST /events/upload
```

**TO**:
```javascript
// NEW - sends JSON to JSON endpoint
POST /events/upload-json
```

**Example**:
```javascript
const response = await fetch('/events/upload-json', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    storeCode: 'TEST',
    eventName: 'Onam',
    eventType: 'FESTIVAL CELEBRATION',
    eventSubType: 'Onam',
    date: '2026-03-26',
    time: '',  // Optional - will default to "00:00"
    location: 'In store',
    image: '/static/assets/event2-B8cJ3Wja.png'
  })
});
```

## Files Modified

1. ✅ `EventsController.java` - Added new `/upload-json` endpoint
2. ✅ `EventCreationRequest.java` - New DTO for JSON requests (CREATED)
3. ✅ `EventsDetailDTO.java` - Made `startTime` optional

## Validation Flow

### New JSON Endpoint (`/upload-json`)

1. **Authentication Check** - User must be logged in
2. **Store Access Validation** - User authorized for the store
3. **@Valid Annotation Check** - Validates:
   - `eventName` is not blank ✅
   - `eventType` is not blank ✅
   - `storeCode` is not blank ✅
   - `date` is not blank ✅
   - Field length limits
4. **Custom Validation** - Phone and name format validation
5. **Default Value Assignment** - Time defaults to "00:00" if empty
6. **Event Creation** - Passes to service layer

## Testing Checklist

- [ ] Test event creation with valid `eventName`
- [ ] Test with empty `time` field (should default to "00:00")
- [ ] Test with various event types (Festival, Special Day, etc.)
- [ ] Test authentication requirement
- [ ] Test store access authorization
- [ ] Verify event is saved in database with correct `eventName`
- [ ] Verify QR code is generated successfully

## Expected Behavior

**Before Fix**:
```json
{
  "status": false,
  "qrData": "Event name is required"
}
```

**After Fix**:
```json
{
  "status": true,
  "qrData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg..."
}
```

## Database Verification

After successful event creation, verify in the `events` table:

```sql
SELECT id, event_name, event_type, event_sub_type, start_date, start_time, store_code
FROM events
WHERE store_code = 'TEST'
ORDER BY created_at DESC
LIMIT 1;
```

Expected result:
- `event_name`: "Onam" ✅
- `event_type`: "FESTIVAL CELEBRATION" ✅
- `event_sub_type`: "Onam" ✅
- `start_time`: "00:00" (default) ✅

## Key Points

1. ✅ **eventName is now properly received** from frontend JSON payload
2. ✅ **No backend derivation** - accepts what frontend sends
3. ✅ **Time field is optional** - defaults to "00:00"
4. ✅ **Full security validation** maintained
5. ✅ **Backward compatible** - old endpoint still works

## Compilation

The changes have been implemented and are ready for compilation:

```bash
mvn clean compile -DskipTests
```

Or build the WAR file:

```bash
mvn clean package -DskipTests
```

## Deployment Notes

1. Deploy the updated WAR file
2. Update frontend to use `/events/upload-json` endpoint
3. Test thoroughly in preprod environment
4. Monitor logs for any validation errors

## Summary

The issue was a fundamental mismatch between how the frontend sends data (JSON) and how the backend expected it (form-data). By creating a new JSON-aware endpoint with proper validation and default values, the backend now correctly accepts and processes the `eventName` field sent by the frontend.

