# 🎉 Greeting Project - Complete Guide & Bug Fix

## 📋 Overview
The Greeting project is a **separate mini-project** within the Tanishq Selfie App that allows users to:
1. Generate a unique QR code
2. Scan the QR code to open a video recording page
3. Record a video message with personal details
4. Store the video in Google Drive
5. View the greeting later

---

## 🗄️ Database Architecture

### **Technology Stack**
- **Database:** MySQL (NOT Excel like the main events project)
- **Storage:** Google Drive (videos)
- **Frontend:** React (deployed at `/qr/`)
- **Backend:** Spring Boot

### **Greeting Table Schema**
```sql
CREATE TABLE greeting (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unique_id VARCHAR(255) UNIQUE NOT NULL,
    uploaded BOOLEAN DEFAULT FALSE,
    drive_file_id VARCHAR(255),
    qr_code_data TEXT,
    greeting_text VARCHAR(500),  -- Name
    message TEXT,                -- Message
    created_at DATETIME,
    updated_at DATETIME
);
```

### **Key Fields**
- `unique_id`: Format `GREETING_{timestamp}` (e.g., `GREETING_1769837018311`)
- `uploaded`: Boolean flag indicating if video has been uploaded
- `drive_file_id`: Google Drive file ID for the uploaded video
- `qr_code_data`: Base64 encoded QR code image (cached)
- `greeting_text`: User's name
- `message`: Personal message

---

## 🔄 Complete Data Flow

### **1. Generate Greeting**
```
POST /greetings/generate
Response: "GREETING_1769837018311"
```
- Creates new record in MySQL with `uploaded=false`
- Returns unique ID

### **2. Generate QR Code**
```
GET /greetings/{uniqueId}/qr
Response: PNG image
```
- **QR Code Content:** `https://celebrations.tanishq.co.in/qr?id=GREETING_1769837018311`
- Saves Base64 QR code to database for caching
- Returns PNG image to download/print

### **3. Scan QR Code**
- User scans QR code with phone camera
- Opens: `https://celebrations.tanishq.co.in/qr?id=GREETING_1769837018311`
- React app at `/qr/` receives the URL

### **4. Record Video**
- React app extracts `id` parameter from URL
- User records video + enters details (name, phone, message)

### **5. Upload Video**
```
POST /greetings/{uniqueId}/upload
Content-Type: multipart/form-data
Body:
  - video: [video file]
  - name: "Nagaraj"
  - phone: "8152948407"
  - message: "Hi I am Nagaraj ajadar"
```
- Uploads video to Google Drive
- Updates MySQL: `uploaded=true`, `drive_file_id`, `greeting_text`, `message`

### **6. View Greeting**
```
GET /greetings/{uniqueId}/view
Response: {
  "hasVideo": true,
  "status": "completed",
  "driveFileId": "1xyz...",
  "videoPlaybackUrl": "https://drive.google.com/file/d/1xyz.../view",
  "submittedAt": "2026-01-31T10:28:39",
  "name": "Nagaraj",
  "message": "Hi I am Nagaraj ajadar"
}
```

---

## 🐛 THE BUG - 405 Method Not Allowed

### **Problem**
The form is submitting to the **wrong URL**:
```
❌ POST /greetings/https://celebrations.tanishq.co.in/qr?id=GREETING_1769837018311/upload
```

Instead of:
```
✅ POST /greetings/GREETING_1769837018311/upload
```

### **Root Cause**
The React app is using the **full URL** from `window.location.href` instead of extracting just the `id` query parameter.

### **Error Response**
```json
{
    "timestamp": "2026-01-31T05:28:39.577+00:00",
    "status": 405,
    "error": "Method Not Allowed",
    "path": "/greetings/https:/celebrations.tanishq.co.in/qr"
}
```

---

## 🔧 THE FIX

### **Location of Source Code**
```
C:\DECHUB\Tanishq-qr-scanner\tanishq-qr-scanner\
```

### **Files to Fix**

#### **1. Context/Provider File (VideoContext.tsx or similar)**
Find where `qrId` is being set and fix it:

**BEFORE (WRONG):**
```tsx
// Somewhere in the QR scanner callback or useEffect
const handleQrScan = (scannedData: string) => {
  // This sets the FULL URL as qrId
  setQrId(scannedData); // ❌ scannedData = "https://celebrations.tanishq.co.in/qr?id=GREETING_XXX"
}
```

**AFTER (CORRECT):**
```tsx
const handleQrScan = (scannedData: string) => {
  // Extract just the ID from the URL
  try {
    const url = new URL(scannedData);
    const id = url.searchParams.get('id');
    if (id) {
      setQrId(id); // ✅ Only "GREETING_1769837018311"
      console.log('Extracted greeting ID:', id);
    } else {
      console.error('No id parameter found in QR code');
    }
  } catch (error) {
    // If it's not a URL, maybe it's already just the ID
    console.log('Not a URL, using as-is:', scannedData);
    setQrId(scannedData);
  }
}
```

#### **2. Form Upload Component**
Find the upload function (probably in `FormPage.tsx` or similar):

**BEFORE (WRONG):**
```tsx
const uploadGreeting = async (formData, videoBlob) => {
  const form = new FormData();
  form.append('name', formData.name);
  form.append('phone', formData.phone);
  form.append('message', formData.message);
  if (videoBlob) {
    form.append('video', videoBlob, 'message.mp4');
  }

  // ❌ qrId contains full URL here
  const response = await axios.post(
    `https://celebrations.tanishq.co.in/greetings/${qrId}/upload`,
    form
  );
};
```

**AFTER (CORRECT):**
```tsx
const uploadGreeting = async (formData, videoBlob) => {
  // Extract ID if qrId is a full URL
  let greetingId = qrId;
  if (qrId.includes('http')) {
    try {
      const url = new URL(qrId);
      greetingId = url.searchParams.get('id') || qrId;
    } catch (e) {
      // Use as-is if URL parsing fails
    }
  }

  console.log('Uploading to greeting ID:', greetingId); // Should log: GREETING_1769837018311

  const form = new FormData();
  form.append('name', formData.name);
  form.append('phone', formData.phone);
  form.append('message', formData.message);
  if (videoBlob) {
    form.append('video', videoBlob, 'message.mp4');
  }

  // ✅ Use clean greeting ID
  const response = await axios.post(
    `https://celebrations.tanishq.co.in/greetings/${greetingId}/upload`,
    form
  );
};
```

#### **3. QR Scanner Page**
When the user opens the app via QR code, extract the ID from URL:

```tsx
// In the component that loads when QR is scanned (e.g., QRScannerPage.tsx)
useEffect(() => {
  // Get ID from URL query parameter when page loads
  const urlParams = new URLSearchParams(window.location.search);
  const greetingId = urlParams.get('id');
  
  if (greetingId) {
    console.log('Greeting ID from URL:', greetingId);
    setQrId(greetingId); // ✅ Set only the ID
    
    // Navigate to recording page
    navigate('/recording', { state: { qrData: greetingId } });
  }
}, []);
```

---

## 🔍 How to Test

### **1. Generate a Greeting**
```bash
curl -X POST https://celebrations.tanishq.co.in/greetings/generate
# Response: GREETING_1769837018311
```

### **2. Get QR Code**
```bash
curl https://celebrations.tanishq.co.in/greetings/GREETING_1769837018311/qr \
  -o qr_code.png
```

### **3. Scan QR Code**
- Scan with phone camera
- Should open: `https://celebrations.tanishq.co.in/qr?id=GREETING_1769837018311`

### **4. Fill Form and Submit**
- Enter name, phone, message
- Record video
- Submit form
- **Check Network Tab:** URL should be `/greetings/GREETING_1769837018311/upload`

### **5. Verify Upload**
```bash
curl https://celebrations.tanishq.co.in/greetings/GREETING_1769837018311/view
# Should return video details
```

---

## 📊 Database Queries for Debugging

### **Check if Greeting Exists**
```sql
SELECT * FROM greeting WHERE unique_id = 'GREETING_1769837018311';
```

### **Check Upload Status**
```sql
SELECT unique_id, uploaded, drive_file_id, greeting_text 
FROM greeting 
WHERE unique_id = 'GREETING_1769837018311';
```

### **List All Greetings**
```sql
SELECT unique_id, uploaded, greeting_text, created_at 
FROM greeting 
ORDER BY created_at DESC 
LIMIT 10;
```

### **Find Uploaded Greetings**
```sql
SELECT unique_id, greeting_text, message, drive_file_id 
FROM greeting 
WHERE uploaded = TRUE 
ORDER BY updated_at DESC;
```

---

## 🚀 Deployment Steps

### **1. Fix React Code**
```bash
cd C:\DECHUB\Tanishq-qr-scanner\tanishq-qr-scanner
# Make the fixes described above
```

### **2. Build React App**
```bash
npm run build
# or
yarn build
```

### **3. Copy Build to Spring Boot**
```bash
# Copy contents of build/ folder to:
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\static\qr\
```

### **4. Rebuild Spring Boot**
```bash
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package
```

### **5. Deploy WAR File**
```bash
# Deploy the generated WAR file:
target/tanishq-preprod-31-01-2026-2-0.0.1-SNAPSHOT.war
```

---

## 🔐 Backend Endpoints Reference

### **Base URL**
```
https://celebrations.tanishq.co.in/greetings
```

### **Endpoints**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/generate` | Create new greeting, returns ID |
| GET | `/{id}/qr` | Get QR code image (PNG) |
| POST | `/{id}/upload` | Upload video + details |
| GET | `/{id}/view` | Get greeting info + video URL |
| GET | `/{id}/status` | Check if video uploaded |
| DELETE | `/{id}` | Delete greeting (admin) |

---

## 📝 Summary

### **What Was Working**
- ✅ Greeting generation (MySQL)
- ✅ QR code generation
- ✅ QR code content encoding (correct URL format)
- ✅ Backend API endpoints
- ✅ Google Drive upload

### **What Was Broken**
- ❌ React app using full URL instead of extracting `id` parameter
- ❌ Form submission going to wrong endpoint

### **What Needs to be Fixed**
1. Extract `id` query parameter from URL when QR is scanned
2. Use only the greeting ID (not full URL) in API calls
3. Rebuild and redeploy React app

---

## 🎯 Key Takeaway

**The backend is working perfectly.** The issue is purely in the **React frontend** where it's not parsing the URL correctly. Once you extract just the `id` parameter from the QR code URL, everything will work as expected!

The QR code correctly contains: `https://celebrations.tanishq.co.in/qr?id=GREETING_XXX`

The React app just needs to extract `GREETING_XXX` and use that for API calls.

