# 🔧 GREETING QR CODE BUG - QUICK FIX

## 🐛 The Problem
Form submits to: `/greetings/https://celebrations.tanishq.co.in/qr?id=GREETING_XXX/upload`
Should submit to: `/greetings/GREETING_XXX/upload`

## 📍 Frontend Project Location
```
C:\DECHUB\Tanishq-qr-scanner\tanishq-qr-scanner\
```

---

## ✅ CODE FIXES

### Fix 1: Extract ID from Scanned QR Code

**File:** Look for QR scanner callback (VideoContext.tsx, QRScannerPage.tsx, or App.tsx)

**Find this pattern:**
```typescript
setQrId(scannedData); // Sets full URL
```

**Replace with:**
```typescript
// Extract ID from QR code URL
const extractGreetingId = (data: string): string => {
  try {
    // If it's a URL, extract the id parameter
    if (data.includes('http')) {
      const url = new URL(data);
      const id = url.searchParams.get('id');
      return id || data;
    }
    return data;
  } catch {
    return data;
  }
};

const greetingId = extractGreetingId(scannedData);
setQrId(greetingId);
console.log('Greeting ID:', greetingId); // Should log: GREETING_1769837018311
```

---

### Fix 2: Parse URL on Initial Load

**File:** Main QR page component (index.tsx, QRScannerPage.tsx)

**Add this useEffect:**
```typescript
useEffect(() => {
  // Extract ID from URL query when user scans QR and opens the page
  const params = new URLSearchParams(window.location.search);
  const greetingId = params.get('id');
  
  if (greetingId) {
    console.log('Loaded with greeting ID:', greetingId);
    setQrId(greetingId); // Store only GREETING_XXX
    
    // Navigate to recording page
    navigate('/recording', { state: { qrData: greetingId } });
  }
}, []);
```

---

### Fix 3: Clean ID Before Upload

**File:** Form submission component (FormPage.tsx, UploadPage.tsx)

**Find the upload function:**
```typescript
const response = await axios.post(
  `https://celebrations.tanishq.co.in/greetings/${qrId}/upload`,
  formData
);
```

**Add ID cleaning before the API call:**
```typescript
// Clean the greeting ID (remove URL if present)
const cleanGreetingId = (id: string): string => {
  if (!id) return id;
  
  try {
    if (id.includes('http')) {
      const url = new URL(id);
      return url.searchParams.get('id') || id;
    }
  } catch {
    // Not a valid URL, use as-is
  }
  
  return id;
};

const greetingId = cleanGreetingId(qrId);
console.log('Uploading to greeting:', greetingId);

const response = await axios.post(
  `https://celebrations.tanishq.co.in/greetings/${greetingId}/upload`,
  formData
);
```

---

## 🧪 Testing Steps

### 1. Test the Fix Locally
```bash
cd C:\DECHUB\Tanishq-qr-scanner\tanishq-qr-scanner
npm run dev
```

### 2. Test URL Parsing
Open browser console and test:
```javascript
const testUrl = "https://celebrations.tanishq.co.in/qr?id=GREETING_1769837018311";
const url = new URL(testUrl);
console.log(url.searchParams.get('id')); // Should log: GREETING_1769837018311
```

### 3. Check Form Submission
- Fill form with test data
- Open Network tab (F12)
- Submit form
- Verify URL is: `/greetings/GREETING_XXXXXXXX/upload` (no https://)

### 4. Build for Production
```bash
npm run build
# or
yarn build
```

---

## 📦 Deploy to Backend

### 1. Copy Build Files
```bash
# After building React app
# Copy from: C:\DECHUB\Tanishq-qr-scanner\tanishq-qr-scanner\build\
# To: C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\static\qr\
```

### 2. Rebuild Spring Boot
```bash
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package
```

### 3. Verify WAR
```bash
# Check file exists:
target/tanishq-preprod-31-01-2026-2-0.0.1-SNAPSHOT.war
```

---

## 🔍 Quick Debug Checklist

- [ ] QR code contains: `https://celebrations.tanishq.co.in/qr?id=GREETING_XXX`
- [ ] React app extracts ID from URL query parameter
- [ ] Form submission URL is: `/greetings/GREETING_XXX/upload` (no https)
- [ ] Network tab shows 200 OK (not 405 Method Not Allowed)
- [ ] Backend receives correct greeting ID

---

## 🎯 Expected Behavior

**QR Code URL:**
```
https://celebrations.tanishq.co.in/qr?id=GREETING_1769837018311
```

**React State (qrId):**
```
GREETING_1769837018311
```

**API Call:**
```
POST /greetings/GREETING_1769837018311/upload
```

**Response:**
```json
{
  "status": 200,
  "message": "Video uploaded successfully"
}
```

---

## 📞 Need Help?

If the fix doesn't work:
1. Check browser console for errors
2. Check Network tab for actual request URL
3. Verify qrId state value using React DevTools
4. Add console.logs to trace the ID value through the app

---

## ✨ Key Point

**The backend is working fine!**
**The issue is purely in the React frontend URL parsing.**
**Just extract the `id` parameter from the URL query string.**

