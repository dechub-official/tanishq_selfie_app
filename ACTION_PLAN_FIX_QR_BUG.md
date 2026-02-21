# 🎯 ACTION PLAN - Fix Greeting QR Code Bug

## 📍 Your Frontend Project
```
C:\DECHUB\Tanishq-qr-scanner\tanishq-qr-scanner\
```

---

## 🚀 STEP-BY-STEP FIX

### ✅ Step 1: Understand the Problem
- **Current Bug:** Form submits to `/greetings/https://celebrations.tanishq.co.in/qr?id=GREETING_XXX/upload`
- **Expected:** Form should submit to `/greetings/GREETING_XXX/upload`
- **Root Cause:** React app is using the full QR code URL instead of extracting just the ID

### ✅ Step 2: Open Your React Project
```bash
cd C:\DECHUB\Tanishq-qr-scanner\tanishq-qr-scanner
code .
# or open in your IDE
```

### ✅ Step 3: Find These Files

Look for files that handle:
1. **QR Code Scanning** - probably `QRScannerPage.tsx` or similar
2. **Video Context/State** - probably `VideoContext.tsx` or `App.tsx`
3. **Form Upload** - probably `FormPage.tsx` or `UploadPage.tsx`

**Search for these keywords in your project:**
- `setQrId`
- `qrId`
- `/upload`
- `axios.post`

### ✅ Step 4: Add URL Parsing Function

**Add this utility function** (create a new file `src/utils/greetingUtils.ts` or add to existing utils):

```typescript
/**
 * Extract greeting ID from QR code URL
 * Handles both full URLs and direct IDs
 */
export const extractGreetingId = (data: string): string => {
  try {
    // If it contains http/https, it's a URL - extract the id parameter
    if (data.includes('http')) {
      const url = new URL(data);
      const id = url.searchParams.get('id');
      return id || data;
    }
    // Otherwise, it's already just the ID
    return data;
  } catch (error) {
    console.error('Error parsing greeting URL:', error);
    return data;
  }
};
```

### ✅ Step 5: Fix QR Scanner Callback

**Find the QR scanner callback** and update it:

```typescript
// BEFORE
const handleQrScan = (scannedData: string) => {
  setQrId(scannedData); // ❌ Sets full URL
};

// AFTER
import { extractGreetingId } from './utils/greetingUtils';

const handleQrScan = (scannedData: string) => {
  const greetingId = extractGreetingId(scannedData);
  console.log('Scanned QR:', scannedData);
  console.log('Extracted ID:', greetingId); // Should log: GREETING_XXX
  setQrId(greetingId); // ✅ Sets only the ID
};
```

### ✅ Step 6: Fix Initial URL Load

**In your main QR page component**, add this to handle direct URL opens:

```typescript
useEffect(() => {
  // When user scans QR and opens the URL directly
  const params = new URLSearchParams(window.location.search);
  const idFromUrl = params.get('id');
  
  if (idFromUrl) {
    console.log('Opened with ID from URL:', idFromUrl);
    setQrId(idFromUrl);
    // Navigate to recording or next step
    navigate('/recording', { state: { qrData: idFromUrl } });
  }
}, []);
```

### ✅ Step 7: Fix Upload Function

**In your upload/form component:**

```typescript
import { extractGreetingId } from './utils/greetingUtils';

const handleUpload = async (formData: FormData, videoBlob: Blob) => {
  // Clean the ID (in case it still has URL)
  const greetingId = extractGreetingId(qrId);
  
  console.log('Uploading to greeting:', greetingId);
  
  const form = new FormData();
  form.append('name', formData.name);
  form.append('phone', formData.phone);
  form.append('message', formData.message);
  form.append('video', videoBlob, 'message.mp4');
  
  try {
    const response = await axios.post(
      `https://celebrations.tanishq.co.in/greetings/${greetingId}/upload`,
      form,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    );
    
    console.log('Upload successful:', response.data);
    return response.data;
  } catch (error) {
    console.error('Upload failed:', error);
    throw error;
  }
};
```

---

## 🧪 Testing

### Test 1: Test URL Parser
```bash
# Open this test file in browser:
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\test_greeting_url_parser.html
```

### Test 2: Test in Dev Mode
```bash
cd C:\DECHUB\Tanishq-qr-scanner\tanishq-qr-scanner
npm run dev
```

Then:
1. Open browser console (F12)
2. Navigate to the app
3. Manually set a test URL:
```javascript
const testUrl = "https://celebrations.tanishq.co.in/qr?id=GREETING_TEST123";
// Paste your extractGreetingId function and test it
```

### Test 3: Test Form Submission
1. Fill out the form
2. Open Network tab (F12)
3. Submit the form
4. **Check the request URL** - it should be:
   ```
   POST /greetings/GREETING_XXXXXXXX/upload
   ```
   **NOT:**
   ```
   POST /greetings/https://celebrations.tanishq.co.in/qr?id=GREETING_XXX/upload
   ```

---

## 📦 Build & Deploy

### Build React App
```bash
cd C:\DECHUB\Tanishq-qr-scanner\tanishq-qr-scanner
npm run build
```

### Copy to Backend
```bash
# After build completes, copy contents from:
C:\DECHUB\Tanishq-qr-scanner\tanishq-qr-scanner\build\

# To:
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\static\qr\

# Note: Delete old files in qr\ folder first, then copy new build
```

### Rebuild Backend
```bash
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package
```

### Deploy
```bash
# Deploy the WAR file:
target/tanishq-preprod-31-01-2026-2-0.0.1-SNAPSHOT.war
```

---

## ✅ Verification Checklist

- [ ] QR code contains: `https://celebrations.tanishq.co.in/qr?id=GREETING_XXX`
- [ ] `extractGreetingId` function extracts only `GREETING_XXX`
- [ ] `qrId` state contains only `GREETING_XXX` (not full URL)
- [ ] Upload API call goes to `/greetings/GREETING_XXX/upload`
- [ ] Network tab shows 200 OK response
- [ ] Video uploads successfully to Google Drive
- [ ] Can view uploaded greeting via `/greetings/GREETING_XXX/view`

---

## 📚 Reference Documents Created

1. **Complete Guide:** `GREETING_PROJECT_FIX_GUIDE.md`
   - Full system architecture
   - Database schema
   - Data flow
   - Detailed fix instructions

2. **Quick Fix:** `QUICK_FIX_GREETING_QR.md`
   - Code snippets
   - Quick copy-paste fixes

3. **Test Tool:** `test_greeting_url_parser.html`
   - Interactive testing
   - Browser-based validation

---

## 🆘 Troubleshooting

### Issue: Still getting 405 error
**Check:**
- Open browser console - what's the actual `qrId` value?
- Check Network tab - what's the full request URL?
- Add `console.log('qrId before upload:', qrId)` before API call

### Issue: Can't find where qrId is set
**Search for:**
```bash
# In VS Code or your IDE:
- Search: "setQrId"
- Search: "useState" (look for qrId state)
- Search: "qrId:"
- Search: "/upload"
```

### Issue: Build fails
**Check:**
```bash
npm install
npm run build
# Check for TypeScript errors
```

---

## 🎯 Expected Result

After fixing:
1. ✅ Scan QR code
2. ✅ App opens at: `https://celebrations.tanishq.co.in/qr?id=GREETING_XXX`
3. ✅ React extracts ID: `GREETING_XXX`
4. ✅ User fills form and records video
5. ✅ Form submits to: `POST /greetings/GREETING_XXX/upload`
6. ✅ Backend responds: `200 OK`
7. ✅ Video stored in Google Drive
8. ✅ Database updated with video info
9. ✅ Success! 🎉

---

## 💡 Key Point

**The backend code is perfect!**  
**The database is working fine!**  
**Only the React frontend needs this one simple fix:**

```typescript
// Extract ID from URL instead of using the full URL
const greetingId = new URL(qrUrl).searchParams.get('id');
```

That's it! 🚀

