# Fix: Mobile QR ID Null Issue - Complete Solution

## Problem Summary
**Error on Mobile**: `Failed to submit message: 400 "Invalid greeting ID. Please scan the QR code again or refresh the page."`
**Works on Desktop**: Same flow works correctly on desktop browsers

## Fixes Applied

### ✅ Backend Fix 1: QR URL Parameter Mismatch (DONE)

**Problem**: QR codes were generated with `?id=` but frontend looks for `?qrId=`

**File Changed**: `src/main/java/com/dechub/tanishq/service/GreetingService.java`

**Before**:
```java
String qrUrl = "https://celebrations.tanishq.co.in/qr/create-video?id=" + uniqueId;
```

**After**:
```java
String qrUrl = "https://celebrations.tanishq.co.in/qr/create-video?qrId=" + uniqueId + "&autoStart=true";
```

This fix ensures that when users scan QR codes directly with their phone camera (not the in-app scanner), the `qrId` parameter is correctly passed to the frontend.

**Note**: Existing QR codes with the old `?id=` format will still work IF scanned with the in-app QR scanner (which handles the conversion). New QR codes will work with both direct camera scan and in-app scanner.

### ✅ Backend Fix 2: Better Error Messages (DONE - Previous Fix)

The backend now returns clear error messages when `qrId` is null/undefined instead of confusing "Greeting not found: null".

## Root Cause Analysis

After analyzing the frontend code (bundled in `static/qr/assets/index-*.js`), the issue is:

### How the Flow Currently Works:
1. **QR Scan Page (`/`)**: User scans QR code → `qrId` extracted from QR data
2. **Create Video Page (`/create-video`)**: Receives `qrId` via URL params (`?qrId=...&autoStart=true`)
   - Sets `qrId` in React Context via `setQrId(i)`
3. **Recording Page (`/recording`)**: User records video
   - Navigates to `/form` **WITHOUT** passing qrId (relies on Context)
4. **Form Page (`/form`)**: Reads `qrId` from Context via `const { qrId } = xo()`
   - Submits to API: `/greetings/${qrId}/upload`

### The Bug:
- Navigation from `/recording` to `/form` does NOT pass `qrId`
- Mobile browsers may clear React Context during navigation due to:
  - Browser memory pressure
  - App backgrounding (switching to camera, etc.)
  - Different browser lifecycle management on mobile
  - Service worker interruptions

## Solution

### Frontend Changes Required (in `Event_Frontend_Preprod/Tanishq_Events/`)

#### Option 1: Persist qrId in URL Throughout Flow (Recommended)

**File: Recording.tsx (or similar recording component)**
```tsx
// Instead of:
const w = () => {
  navigate("/form");
};

// Change to:
const w = () => {
  const { qrId } = useVideo();
  navigate(`/form?qrId=${encodeURIComponent(qrId || '')}`);
};
```

**File: Form.tsx (or UserForm.tsx)**
```tsx
// Add URL parameter fallback:
const [searchParams] = useSearchParams();
const { qrId: contextQrId, setQrId } = useVideo();

// Use URL param as fallback
const urlQrId = searchParams.get('qrId');
const qrId = contextQrId || urlQrId;

// Sync URL param to context if context is empty
useEffect(() => {
  if (urlQrId && !contextQrId) {
    setQrId(urlQrId);
  }
}, [urlQrId, contextQrId, setQrId]);
```

#### Option 2: Use localStorage as Backup

**File: VideoContext.tsx**
```tsx
function VideoProvider({ children }) {
  const [qrId, setQrIdState] = useState<string | null>(() => {
    // Initialize from localStorage if available
    return localStorage.getItem('greeting_qrId');
  });

  const setQrId = (id: string | null) => {
    setQrIdState(id);
    if (id) {
      localStorage.setItem('greeting_qrId', id);
    } else {
      localStorage.removeItem('greeting_qrId');
    }
  };

  // Clear on successful submission or after timeout
  // ...
}
```

#### Option 3: Pass Through Navigation State (React Router)

**File: Recording.tsx**
```tsx
const handleStopRecording = () => {
  const { qrId } = useVideo();
  navigate('/form', { state: { qrId } });
};
```

**File: Form.tsx**
```tsx
const location = useLocation();
const { qrId: contextQrId } = useVideo();
const qrId = contextQrId || location.state?.qrId;
```

## Quick Fix (If You Can Only Modify Form.tsx)

The fastest fix that only requires changing the Form component:

```tsx
// In Form.tsx - add at the top of the component:

const [searchParams] = useSearchParams();
const { qrId: contextQrId, setQrId } = useVideo();

// Multi-source qrId recovery
const urlQrId = searchParams.get('qrId');
const storageQrId = sessionStorage.getItem('greeting_qrId');
const qrId = contextQrId || urlQrId || storageQrId;

useEffect(() => {
  // Sync found qrId back to context
  if (qrId && !contextQrId) {
    setQrId(qrId);
  }
  // Save to sessionStorage as backup
  if (qrId) {
    sessionStorage.setItem('greeting_qrId', qrId);
  }
}, [qrId, contextQrId, setQrId]);

// Add validation before API call
const handleSubmit = async (e) => {
  e.preventDefault();
  
  // Validate qrId before submission
  if (!qrId || qrId === 'null' || qrId === 'undefined') {
    setError('Session expired. Please scan the QR code again.');
    return;
  }
  
  // ... rest of submission logic
};
```

## Testing the Fix

### Test Steps:
1. Scan QR code on mobile
2. Navigate through create-video → recording → form
3. During recording, switch to another app briefly
4. Return and try to submit
5. Verify the submission works

### Debug Logging (Add Temporarily):
```tsx
console.log('qrId sources:', {
  context: contextQrId,
  url: urlQrId,
  storage: storageQrId,
  final: qrId
});
```

## Files to Modify

Based on the bundled code analysis, these are likely the source files:

```
Event_Frontend_Preprod/Tanishq_Events/src/
├── context/
│   └── VideoContext.tsx       # Add localStorage persistence
├── pages/
│   ├── CreateVideo.tsx        # Already passes qrId via URL ✓
│   ├── Recording.tsx          # FIX: Pass qrId when navigating to form
│   └── Form.tsx               # FIX: Add URL/storage fallback for qrId
└── components/
    └── ...
```

## Backend Status

✅ Backend is already fixed with proper validation and user-friendly error messages:
- Returns 400 with clear message instead of 404 "Greeting not found: null"
- Logs indicate frontend qrId issue for debugging

## Priority

**HIGH** - This is a production issue affecting mobile users

## Summary

| Component | Status | Action Needed |
|-----------|--------|---------------|
| Backend (QR URL) | ✅ Fixed | Changed `?id=` to `?qrId=` + added `autoStart=true` |
| Backend (Error Msgs) | ✅ Fixed | Clear error message implemented |
| Frontend (Recording) | ❌ Bug | Pass qrId when navigating to form |
| Frontend (Form) | ❌ Bug | Add fallback qrId sources |
| Frontend (Context) | ⚠️ Enhancement | Consider localStorage backup |

## What This Backend Fix Solves

1. **Direct phone camera scans**: When users scan QR with their phone camera (not in-app), the URL now has `?qrId=` which the frontend expects
2. **Auto-start recording**: Added `autoStart=true` so recording starts automatically after scanning

## What Still Needs Frontend Fix

1. **In-app scanner flow**: The in-app scanner extracts `id` and navigates to `?qrId=`, but the Recording page navigates to Form without passing `qrId`
2. **Context persistence**: React Context may lose `qrId` on mobile browsers during navigation/backgrounding

## Deployment Note

After deploying this backend fix:
- **New QR codes** will work correctly with both direct camera scan and in-app scanner
- **Old QR codes** with `?id=` format will only work with the in-app QR scanner (which handles conversion)
- Consider regenerating existing QR codes for customers if issues persist

