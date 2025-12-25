# 📸 IMAGE STORAGE IN EVENTS - COMPLETE ANALYSIS

**Date:** December 3, 2025  
**Topic:** Where are event-related images stored?

---

## 🎯 QUICK ANSWER

### **Event images are stored in TWO different places:**

| Image Type | Storage Location | Status |
|------------|-----------------|--------|
| **Event Cover Images** | ❌ **NOT stored** - Only URL saved in database | ✅ Working |
| **Selfie/User Images** | ✅ **Local Server** - `/opt/tanishq/storage/selfie_images/` | ✅ Working |
| **Completed Event Photos** | ⚠️ **Google Drive** (Currently DISABLED - Placeholder) | ❌ Not working |

---

## 📊 DETAILED BREAKDOWN

### 1️⃣ **Event Cover Images** (During Event Creation)

**Storage:** ❌ **NOT STORED ON SERVER OR GOOGLE DRIVE**

**How it works:**
```
Manager creates event → Provides image URL (not file upload)
           ↓
Frontend sends: image = "https://example.com/wedding.jpg"
           ↓
Backend receives image parameter as STRING (URL)
           ↓
Saves to database: event.image = "https://example.com/wedding.jpg"
           ↓
NO FILE UPLOAD - Just stores the URL!
```

**Code Evidence:**

**Controller:**
```java
@PostMapping(path = "/upload", produces = "application/json")
public QrResponseDTO storeEventsDetails(
    @RequestParam(value = "image", required = false) String image,  // ← String, not file!
    // ... other params
) {
    eventsDetailDTO.setImage(image);  // Just sets the URL string
    return tanishqPageService.storeEventsDetails(eventsDetailDTO);
}
```

**Service:**
```java
public QrResponseDTO storeEventsDetails(EventsDetailDTO eventsDetailDTO) {
    Event event = new Event();
    event.setImage(eventsDetailDTO.getImage());  // ← Saves URL string to DB
    eventRepository.save(event);
}
```

**Database:**
```sql
-- events table
CREATE TABLE events (
    image TEXT,  -- Stores URL like "https://..."
    -- NOT a file path or binary data!
);

-- Example data:
id: STORE001_abc123
image: "https://example.com/images/wedding.jpg"
```

**Summary:**
- ✅ Image URL is saved in database
- ❌ NO actual file is uploaded to server
- ❌ NO file is uploaded to Google Drive
- ✅ Frontend displays image by loading from external URL

---

### 2️⃣ **Selfie/Customer Images** (From Selfie Feature)

**Storage:** ✅ **LOCAL SERVER** - `/opt/tanishq/storage/selfie_images/`

**Configuration (application-preprod.properties):**
```properties
selfie.upload.dir=/opt/tanishq/storage/selfie_images
```

**How it works:**
```
User uploads selfie image
           ↓
Frontend calls: POST /saveImage
           ↓
Backend receives: MultipartFile (actual file data)
           ↓
Service processes:
  1. Generates unique filename (timestamp-based)
  2. Saves to: /opt/tanishq/storage/selfie_images/{division}/{filename}
  3. Returns filename to frontend
           ↓
File stored on LOCAL SERVER disk
           ↓
NO Google Drive upload
```

**Code:**
```java
@Value("${selfie.upload.dir}")
private String selfieDirectory;  // = "/opt/tanishq/storage/selfie_images"

public ResponseDataDTO saveImage(MultipartFile file, String storeCode) {
    String divisionName = this.getDivisionDirectory(storeCode);
    String newFileName = this.getNewFileName(file.getOriginalFilename());
    String sep = isWindows.equalsIgnoreCase("Y") ? "\\" : "/";
    
    // Local file path on server
    String filePath = selfieDirectory + sep + divisionName + sep + newFileName;
    //                 ↑ e.g., /opt/tanishq/storage/selfie_images/EAST/20251203123456789.jpg
    
    File targetFile = new File(filePath);
    file.transferTo(targetFile);  // Saves to server disk
    
    return newFileName;
}
```

**File Structure on Server:**
```
/opt/tanishq/storage/selfie_images/
├── EAST/
│   ├── 20251203101530123.jpg
│   ├── 20251203102045456.jpg
│   └── ...
├── WEST/
│   ├── 20251203103012789.jpg
│   └── ...
├── NORTH/
│   └── ...
└── SOUTH/
    └── ...
```

**Summary:**
- ✅ Files stored on local server disk
- ✅ Organized by division (EAST, WEST, NORTH, SOUTH)
- ✅ Unique filenames (timestamp-based)
- ❌ NOT stored in Google Drive
- ✅ Accessible via server file system

---

### 3️⃣ **Completed Event Photos/Videos** (After Event)

**Storage:** ⚠️ **GOOGLE DRIVE** (Currently DISABLED - Using Placeholder)

**Endpoint:** `POST /events/uploadCompletedEvents`

**Current Status:** ❌ **NOT WORKING - Service Disabled**

**How it SHOULD work:**
```
Manager uploads event photos/videos after event completion
           ↓
Frontend calls: POST /events/uploadCompletedEvents
  FormData:
  - files: [photo1.jpg, photo2.jpg, video1.mp4]
  - eventId: "STORE001_abc123"
           ↓
Backend receives: List<MultipartFile> files
           ↓
SHOULD DO (CURRENTLY DISABLED):
  1. Create temp files on server
  2. Upload each file to Google Drive
  3. Get Google Drive file IDs
  4. Create folder link
  5. Save folder link to database
           ↓
CURRENTLY DOES (PLACEHOLDER):
  1. Creates temp files
  2. ❌ COMMENTED OUT Google Drive upload
  3. Returns placeholder response
  4. Deletes temp files
```

**Code (Current Implementation):**
```java
@PostMapping("/uploadCompletedEvents")
public ResponseDataDTO uploadFiles(
    @RequestParam("files") List<MultipartFile> files,
    @RequestParam("eventId") String eventId
) {
    try {
        // ❌ DISABLED - Google Service commented out
        // String folderLink = googleServiceUtil.getFolderLinkForEvent(eventId);
        String folderLink = "https://drive.google.com/folder/placeholder";  // Placeholder!
        
        for (MultipartFile file : files) {
            // Create temp file
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);
            
            // ❌ DISABLED - Upload to Google Drive commented out
            // googleServiceUtil.uploadFileToDrive(tempFile, eventId, file.getContentType());
            
            tempFile.delete();  // Deletes file immediately
        }
        
        // Returns success but files are NOT actually stored anywhere!
        responseDataDTO.setStatus(true);
        responseDataDTO.setMessage("All files uploaded successfully.");
    }
}
```

**Why it's disabled:**
```java
// These services DON'T EXIST in current codebase:
// - googleServiceUtil
// - GoogleDriveService
// - GoogleServiceUtil

// Similar to Greeting Controller - service layer deleted during migration
```

**Summary:**
- ⚠️ **FEATURE DISABLED** (like Greeting Controller)
- ❌ Files are NOT uploaded to Google Drive
- ❌ Files are NOT saved to local server
- ❌ Files are immediately deleted after upload
- ✅ Returns "success" message but files are LOST
- ❌ Google Drive integration is COMMENTED OUT

---

## 🗄️ DATABASE STORAGE

### What's stored in database:

```sql
-- events table
CREATE TABLE events (
    id VARCHAR(255) PRIMARY KEY,
    image TEXT,                          -- ✅ Event cover image URL (not file)
    completed_events_drive_link TEXT,    -- ⚠️ Google Drive folder link (placeholder)
    -- ... other fields
);
```

**Example data:**
```
id: STORE001_abc123
image: "https://example.com/wedding.jpg"  ✅ External URL
completed_events_drive_link: "https://drive.google.com/folder/placeholder"  ⚠️ Placeholder
```

**No actual image files stored in database!**

---

## 📊 COMPARISON TABLE

| Feature | Image Type | Upload Method | Storage Location | Status |
|---------|-----------|---------------|------------------|--------|
| **Event Creation** | Cover Image | ❌ No upload (URL only) | Database (URL string) | ✅ Working |
| **Selfie Feature** | User Photos | ✅ File upload | `/opt/tanishq/storage/selfie_images/` | ✅ Working |
| **Completed Events** | Photos/Videos | ⚠️ File upload | ❌ **Google Drive (DISABLED)** | ❌ Not working |
| **QR Codes** | QR Images | Generated | Returned as Base64 (not stored) | ✅ Working |

---

## 🔄 COMPLETE WORKFLOW DIAGRAMS

### Workflow 1: Event Creation with Image

```
MANAGER creates event
           ↓
Fills form with event cover image URL:
  - image: "https://cdn.example.com/wedding.jpg"
           ↓
FRONTEND: POST /events/upload
  FormData:
  - image: "https://cdn.example.com/wedding.jpg"  ← URL string, not file!
           ↓
BACKEND:
  1. Receives image parameter as STRING
  2. Creates Event entity
  3. Sets event.image = "https://cdn.example.com/wedding.jpg"
  4. Saves to database
           ↓
DATABASE:
  INSERT INTO events (id, image, ...) 
  VALUES ('STORE001_abc', 'https://cdn.example.com/wedding.jpg', ...)
           ↓
RESULT:
  ✅ URL stored in database
  ❌ NO file stored anywhere
  ✅ Frontend displays image by loading from external URL
```

---

### Workflow 2: Selfie Upload

```
CUSTOMER uploads selfie
           ↓
FRONTEND: POST /saveImage
  FormData:
  - file: selfie.jpg (actual file data)
  - storeCode: "STORE001"
           ↓
BACKEND (TanishqPageService.saveImage):
  1. Receives MultipartFile (actual file)
  2. Gets division from store code
  3. Generates unique filename: "20251203123456789.jpg"
  4. Creates path: /opt/tanishq/storage/selfie_images/EAST/20251203123456789.jpg
  5. Saves file to server disk
  6. Returns filename
           ↓
SERVER DISK:
  /opt/tanishq/storage/selfie_images/
    └── EAST/
        └── 20251203123456789.jpg  ← File saved here!
           ↓
RESULT:
  ✅ File stored on local server
  ✅ Accessible via file path
  ❌ NOT in Google Drive
```

---

### Workflow 3: Completed Event Photos (DISABLED)

```
MANAGER uploads event photos after event
           ↓
FRONTEND: POST /events/uploadCompletedEvents
  FormData:
  - files: [photo1.jpg, photo2.jpg, video1.mp4]
  - eventId: "STORE001_abc123"
           ↓
BACKEND (EventsController.uploadFiles):
  1. Receives List<MultipartFile>
  2. For each file:
     a. Creates temp file on server
     b. Transfers uploaded file to temp
     c. ❌ SHOULD upload to Google Drive (DISABLED)
     d. Deletes temp file
  3. Returns "success" message
           ↓
GOOGLE DRIVE:
  ❌ NO upload happens (service commented out)
           ↓
SERVER DISK:
  ❌ Temp files deleted immediately
           ↓
RESULT:
  ❌ Files are LOST (not stored anywhere!)
  ⚠️ Returns success but files are gone
  ❌ Google Drive integration DISABLED
```

---

## 📁 FILE STORAGE PATHS

### Pre-Prod Environment (`application-preprod.properties`):

```properties
# Selfie images - LOCAL SERVER
selfie.upload.dir=/opt/tanishq/storage/selfie_images

# Bride images - LOCAL SERVER
dechub.bride.upload.dir=/opt/tanishq/storage/bride_uploads

# Event cover images - NOT STORED (URL only in DB)
# Completed event photos - SHOULD be Google Drive (currently disabled)
```

### Actual Paths on Server (10.160.128.94):

```
/opt/tanishq/
├── storage/
│   ├── selfie_images/
│   │   ├── EAST/
│   │   │   └── *.jpg
│   │   ├── WEST/
│   │   │   └── *.jpg
│   │   ├── NORTH/
│   │   │   └── *.jpg
│   │   └── SOUTH/
│   │       └── *.jpg
│   ├── bride_uploads/
│   │   └── *.jpg
│   └── base.jpg
├── tanishqgmb-5437243a8085.p12  ← Google service key
└── event-images-469618-32e65f6d62b3.p12  ← Event images service key
```

---

## 🔧 GOOGLE DRIVE INTEGRATION (DISABLED)

### Configuration (application-preprod.properties):

```properties
# Event Images Service Account Configuration
dechub.tanishq.google.service.account.event=event-images@event-images-469618.iam.gserviceaccount.com
dechub.tanishq.key.filepath.event=/opt/tanishq/event-images-469618-32e65f6d62b3.p12
dechub.tanishq.google.drive.parent-folder-id.event=1jE0rqkbPsPd2Y3lpa3-6MGhcU0UJbvfr
```

**Status:**
- ✅ Configuration exists
- ✅ Service account credentials present on server
- ❌ **But the service implementation is DELETED/COMMENTED OUT**

**Missing Services:**
```java
// These DON'T EXIST in current codebase:
- GoogleServiceUtil
- GoogleDriveService
- Drive upload methods
```

**Same issue as Greeting Controller!**

---

## ✅ WHAT'S WORKING vs NOT Working

### ✅ **WORKING:**

1. **Event Cover Images**
   - URL storage in database ✅
   - Frontend displays from external URLs ✅

2. **Selfie Images**
   - File upload to local server ✅
   - Organized by division ✅
   - Unique filename generation ✅
   - Accessible from server file system ✅

3. **Event Data**
   - All event metadata in database ✅
   - Event creation and management ✅

### ❌ **NOT WORKING:**

1. **Completed Event Photos/Videos**
   - Google Drive upload DISABLED ❌
   - Files are deleted after upload ❌
   - Only placeholder link saved ❌
   - Service layer DELETED ❌

2. **Google Drive Integration**
   - Upload service MISSING ❌
   - Folder creation MISSING ❌
   - File link generation MISSING ❌

---

## 🎯 SUMMARY

### **Current Image Storage:**

| Type | Location | Working? |
|------|----------|----------|
| Event cover images | Database (URL only) | ✅ YES |
| Selfie images | `/opt/tanishq/storage/selfie_images/` | ✅ YES |
| Completed event photos | ⚠️ Google Drive (DISABLED) | ❌ NO |

### **Key Points:**

1. ✅ **Event cover images:** NOT uploaded - just URL stored in database
2. ✅ **Selfie images:** Stored on LOCAL SERVER in `/opt/tanishq/storage/`
3. ❌ **Completed event photos:** SHOULD go to Google Drive but service is DISABLED
4. ⚠️ **Google Drive:** Configuration exists but implementation is DELETED

### **Same Pattern as Greeting Controller:**

```
Greeting Controller: ❌ Video upload to Google Drive - DISABLED
Events Controller:   ❌ Photo upload to Google Drive - DISABLED
                    ✅ But event creation works fine!
```

---

## 🔄 TO ENABLE GOOGLE DRIVE FOR COMPLETED EVENTS

### Would need to:

1. ✅ Restore/Create GoogleServiceUtil class
2. ✅ Implement uploadFileToDrive() method
3. ✅ Implement getFolderLinkForEvent() method
4. ✅ Uncomment code in EventsController.uploadCompletedEvents()
5. ✅ Test with Google Drive API
6. ✅ Verify service account permissions

**Until then:**
- ✅ Event creation works
- ✅ QR codes work
- ✅ Attendee registration works
- ❌ Completed event photo upload doesn't work (files are lost)

---

## 💡 RECOMMENDATION

### **For Pre-Prod Testing:**

**Option 1: Keep Current Setup (Recommended for now)**
- ✅ Event management works fine
- ✅ Selfie feature works fine
- ⚠️ Accept that completed event photos don't persist
- ✅ Focus on testing other features first

**Option 2: Enable Local Storage for Completed Events**
- Store completed event photos in `/opt/tanishq/storage/completed_events/`
- Similar to selfie storage
- No Google Drive dependency
- Easier to implement

**Option 3: Restore Google Drive Integration**
- More complex
- Requires service implementation
- Matches production behavior
- Better long-term solution

---

**BOTTOM LINE:**

✅ **Event images (cover):** URL only - stored in database  
✅ **Selfie images:** Local server - `/opt/tanishq/storage/selfie_images/`  
❌ **Completed event photos:** Google Drive DISABLED - files are LOST  

**Your event management features work fine - only post-event photo upload is disabled!** ✅


