}
```

**Purpose:** 
- Data Transfer Object for API responses
- Combines database data with computed values (like video URL)
- Makes JSON responses clean and structured

---

### Greeting.java (DTO - different from Entity!)

```java
package com.dechub.tanishq.dto.qrcode;

public class Greeting {
    private String uniqueId = UUID.randomUUID().toString();
    private String googleDriveFileId;
    
    // Getters and Setters
}
```

**Purpose:**
- Lightweight DTO for some greeting operations
- Different from `com.dechub.tanishq.entity.Greeting` (database entity)
- Used for transferring data between layers

---

## 🔄 COMPLETE DATA FLOW (WORKING VERSION)

### When Service is Restored, Here's the Complete Flow:

```
┌─────────────────────────────────────────────────────────────────┐
│ STEP 1: USER CREATES GREETING                                   │
└─────────────────────────────────────────────────────────────────┘

User → Frontend → POST /greetings/generate
                         ↓
                 GreetingController.generateGreetingLink()
                         ↓
                 Generate ID: "GREETING_" + timestamp
                         ↓
                 Create new Greeting entity
                         ↓
                 greetingRepository.save(greeting)
                         ↓
                 MySQL: INSERT INTO greetings (unique_id, uploaded, created_at)
                        VALUES ('GREETING_1733213456789', false, NOW())
                         ↓
                 Return ID to user

┌─────────────────────────────────────────────────────────────────┐
│ STEP 2: GENERATE QR CODE                                        │
└─────────────────────────────────────────────────────────────────┘

User → Frontend → GET /greetings/GREETING_1733213456789/qr
                         ↓
                 GreetingController.generateQrCode(uniqueId)
                         ↓
                 greetingRepository.findByUniqueId(uniqueId)
                         ↓
                 MySQL: SELECT * FROM greetings WHERE unique_id = ?
                         ↓
                 Create QR URL: "https://...tanishq.co.in/greetings/.../upload"
                         ↓
                 qrCodeService.generateQRCode(url)
                         ↓
                 Convert to Base64
                         ↓
                 greeting.setQrCodeData(base64)
                         ↓
                 greetingRepository.save(greeting)
                         ↓
                 MySQL: UPDATE greetings SET qr_code_data = ? WHERE id = ?
                         ↓
                 Return PNG image

┌─────────────────────────────────────────────────────────────────┐
│ STEP 3: RECIPIENT SCANS QR & UPLOADS VIDEO                      │
└─────────────────────────────────────────────────────────────────┘

Recipient scans QR → Opens upload page
                         ↓
                 Fills form (name, message, video file)
                         ↓
                 POST /greetings/GREETING_1733213456789/upload
                         ↓
                 GreetingController.uploadVideo(uniqueId, video, name, msg)
                         ↓
                 greetingRepository.findByUniqueId(uniqueId)
                         ↓
                 MySQL: SELECT * FROM greetings WHERE unique_id = ?
                         ↓
                 Validate video file (size, format)
                         ↓
                 googleDriveService.uploadVideo(file)
                         ↓
                 Google Drive API: Upload video → Returns file ID
                         ↓
                 greeting.setGreetingText(name)
                 greeting.setMessage(message)
                 greeting.setDriveFileId(driveFileId)
                 greeting.setUploaded(true)
                         ↓
                 greetingRepository.save(greeting)
                         ↓
                 MySQL: UPDATE greetings 
                        SET greeting_text = ?, 
                            message = ?, 
                            drive_file_id = ?, 
                            uploaded = true 
                        WHERE id = ?
                         ↓
                 Return success message

┌─────────────────────────────────────────────────────────────────┐
│ STEP 4: USER VIEWS GREETING                                     │
└─────────────────────────────────────────────────────────────────┘

User → Frontend → GET /greetings/GREETING_1733213456789/view
                         ↓
                 GreetingController.getGreetingInfo(uniqueId)
                         ↓
                 greetingRepository.findByUniqueId(uniqueId)
                         ↓
                 MySQL: SELECT * FROM greetings WHERE unique_id = ?
                         ↓
                 Check if uploaded = true
                         ↓
                 If yes:
                     - Get driveFileId
                     - Generate playback URL
                     - Create GreetingInfo DTO
                 If no:
                     - Return "No video yet"
                         ↓
                 Return JSON response
                         ↓
                 Frontend displays video player
```

---

## 🎯 SUMMARY

### What the Greeting Controller Does:

1. **Creates unique greeting links**
   - Generates timestamp-based IDs
   - Stores in database with `uploaded = false`

2. **Generates QR codes** (when service active)
   - Creates QR code pointing to upload URL
   - Saves QR image data in database

3. **Handles video uploads** (when service active)
   - Accepts multipart form data (video file)
   - Uploads to Google Drive
   - Updates database with file ID and metadata
   - Marks as `uploaded = true`

4. **Provides viewing interface**
   - Returns greeting info (name, message)
   - Provides Google Drive video playback URL
   - Checks upload status

### Database Operations:

```sql
-- CREATE
INSERT INTO greetings (unique_id, uploaded, created_at) 
VALUES ('GREETING_XXX', false, NOW());

-- READ
SELECT * FROM greetings WHERE unique_id = 'GREETING_XXX';

-- UPDATE (after QR generation)
UPDATE greetings SET qr_code_data = 'base64data' WHERE unique_id = 'GREETING_XXX';

-- UPDATE (after video upload)
UPDATE greetings 
SET greeting_text = 'John', 
    message = 'Happy Birthday', 
    drive_file_id = '1ABC', 
    uploaded = true 
WHERE unique_id = 'GREETING_XXX';
```

### Current Status:

- ✅ **Database structure** exists and ready
- ✅ **Repository layer** works
- ✅ **Entity class** defined
- ✅ **Controller endpoints** defined
- ❌ **Service layer** deleted/migrated
- ❌ **QR code generation** disabled
- ❌ **Video upload** disabled
- ❌ **Google Drive integration** disabled

### To Restore Feature:

1. ✅ Create `GreetingService` class
2. ✅ Implement QR code generation logic
3. ✅ Implement Google Drive upload service
4. ✅ Update controller to call services instead of returning stubs
5. ✅ Test complete workflow
6. ✅ Deploy to pre-prod environment

---

## 🎓 KEY LEARNINGS

### Architecture Pattern Used:

```
Controller → Service → Repository → Database
    ↓          ↓           ↓
  API      Business    Data Access
 Layer      Logic       Layer
```

### RESTful Design:

- `POST /greetings/generate` → Create resource (greeting ID)
- `GET /greetings/{id}/qr` → Read resource (QR code)
- `POST /greetings/{id}/upload` → Update resource (add video)
- `GET /greetings/{id}/view` → Read resource (greeting info)

### Database Design:

- **Primary Key:** Auto-increment ID
- **Unique Identifier:** `unique_id` (business key)
- **Status Flag:** `uploaded` (boolean)
- **External Reference:** `drive_file_id` (Google Drive)
- **Metadata:** `created_at`, `message`, `name`

---

**This is exactly how your Greeting Controller works (or will work when service is restored)!** ✅

# 🎥 GREETING CONTROLLER - COMPLETE WORKFLOW EXPLANATION

**Date:** December 3, 2025  
**Feature:** Video Greeting Upload & QR Code Generation System

---

## 📋 TABLE OF CONTENTS

1. [Overview](#overview)
2. [Database Structure](#database-structure)
3. [API Endpoints](#api-endpoints)
4. [Complete Workflow](#complete-workflow)
5. [Data Flow Diagrams](#data-flow-diagrams)
6. [Code Explanation](#code-explanation)

---

## 🎯 OVERVIEW

### What is the Greeting Feature?

The Greeting Controller manages a **video greeting card system** where:
1. Users can **generate a unique link** for a greeting card
2. System creates a **QR code** for that link
3. Recipients can **upload a video** using the link/QR code
4. Users can **view the uploaded video** via the link

### ⚠️ CURRENT STATUS

```
🚨 FEATURE IS CURRENTLY DISABLED (STUB IMPLEMENTATION)
```

**Why?** The controller shows:
- Service layer was deleted during system migration
- QR code service is temporarily unavailable
- Video upload functionality is disabled
- All endpoints return error/placeholder responses

**However, the database structure and entity are still intact!**

---

## 🗄️ DATABASE STRUCTURE

### Table: `greetings`

```sql
CREATE TABLE greetings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    unique_id VARCHAR(255),           -- Unique identifier for the greeting
    greeting_text VARCHAR(255),       -- Name or greeting text
    phone VARCHAR(20),                -- Phone number (optional)
    message TEXT,                     -- Personal message
    qr_code_data TEXT,                -- QR code image data (Base64)
    drive_file_id VARCHAR(255),       -- Google Drive file ID for uploaded video
    created_at DATETIME,              -- Timestamp when created
    uploaded BOOLEAN                  -- Flag: video uploaded or not
);
```

### Entity Class: `Greeting.java`

```java
@Entity
@Table(name = "greetings")
public class Greeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // Auto-increment primary key
    
    private String uniqueId;            // E.g., "GREETING_1733213456789"
    private String greetingText;        // Name or greeting message
    private String phone;               // Contact number
    private String message;             // Personal message
    private String qrCodeData;          // Base64 encoded QR code image
    private String driveFileId;         // Google Drive video file ID
    private LocalDateTime createdAt;    // When greeting was created
    private Boolean uploaded;           // True if video uploaded
}
```

---

## 🌐 API ENDPOINTS

### 1. **POST /greetings/generate**
**Purpose:** Generate a unique greeting ID

**Request:**
```http
POST /greetings/generate
Content-Type: application/json
```

**Response:**
```json
"GREETING_1733213456789"
```

**What It Does:**
- Creates a unique ID based on current timestamp
- Returns the ID to the user
- User can share this ID or generate QR code for it

---

### 2. **GET /greetings/{uniqueId}/qr**
**Purpose:** Generate QR code for the greeting link

**Request:**
```http
GET /greetings/GREETING_1733213456789/qr
```

**Response:**
```
Image/PNG (QR Code image)
```

**Current Status:** ❌ **Disabled**
```
Throws: "QR service temporarily disabled - migration in progress"
```

---

### 3. **POST /greetings/{uniqueId}/upload**
**Purpose:** Upload video for a greeting

**Request:**
```http
POST /greetings/GREETING_1733213456789/upload
Content-Type: multipart/form-data

Parameters:
- video: MultipartFile (video file)
- name: String (optional - sender name)
- message: String (optional - personal message)
```

**Response:**
```json
{
  "message": "Video upload disabled during system upgrade"
}
```

**Current Status:** ❌ **Disabled**

---

### 4. **GET /greetings/{uniqueId}/view**
**Purpose:** View greeting info (check if video uploaded)

**Request:**
```http
GET /greetings/GREETING_1733213456789/view
```

**Response:**
```json
{
  "error": "Greeting feature temporarily unavailable - system upgrade in progress",
  "status": 404
}
```

**Current Status:** ❌ **Disabled**

---

## 🔄 COMPLETE WORKFLOW (How It SHOULD Work)

### Phase 1: Generation (User Creates Greeting)

```
USER ACTION: Clicks "Create Greeting Card"
           ↓
APP CALLS: POST /greetings/generate
           ↓
BACKEND:
    1. Generates unique ID: "GREETING_" + timestamp
    2. Creates Greeting entity in database:
       - uniqueId: "GREETING_1733213456789"
       - uploaded: false
       - createdAt: current timestamp
    3. Saves to database
           ↓
RESPONSE: Returns unique ID
           ↓
FRONTEND: Displays greeting link and option to get QR code
```

**Database After Phase 1:**
```
greetings table:
+----+---------------------------+----------+-------+---------+--------------+---------------+---------------------+----------+
| id | unique_id                 | greeting | phone | message | qr_code_data | drive_file_id | created_at          | uploaded |
+----+---------------------------+----------+-------+---------+--------------+---------------+---------------------+----------+
| 1  | GREETING_1733213456789   | NULL     | NULL  | NULL    | NULL         | NULL          | 2025-12-03 10:30:00 | false    |
+----+---------------------------+----------+-------+---------+--------------+---------------+---------------------+----------+
```

---

### Phase 2: QR Code Generation

```
USER ACTION: Clicks "Get QR Code"
           ↓
APP CALLS: GET /greetings/GREETING_1733213456789/qr
           ↓
BACKEND:
    1. Receives unique ID
    2. Creates QR code containing URL:
       "https://celebrationsite-preprod.tanishq.co.in/greetings/GREETING_1733213456789/upload"
    3. Generates QR code image (PNG)
    4. Converts to Base64
    5. Saves QR code data to database
    6. Returns QR code image
           ↓
RESPONSE: PNG image (QR code)
           ↓
FRONTEND: Displays QR code for user to print/share
```

**Database After Phase 2:**
```
greetings table:
+----+---------------------------+----------+-------+---------+------------------------+---------------+---------------------+----------+
| id | unique_id                 | greeting | phone | message | qr_code_data           | drive_file_id | created_at          | uploaded |
+----+---------------------------+----------+-------+---------+------------------------+---------------+---------------------+----------+
| 1  | GREETING_1733213456789   | NULL     | NULL  | NULL    | iVBORw0KGgoAAAANSUh... | NULL          | 2025-12-03 10:30:00 | false    |
+----+---------------------------+----------+-------+---------+------------------------+---------------+---------------------+----------+
```

---

### Phase 3: Video Upload (Recipient Uploads Video)

```
RECIPIENT: Scans QR code with phone
           ↓
PHONE OPENS: https://celebrationsite-preprod.tanishq.co.in/greetings/GREETING_1733213456789/upload
           ↓
RECIPIENT: Fills form:
    - Name: "John Doe"
    - Message: "Happy Birthday!"
    - Uploads video file (e.g., birthday_wish.mp4)
           ↓
APP CALLS: POST /greetings/GREETING_1733213456789/upload
    FormData:
    - video: birthday_wish.mp4
    - name: "John Doe"
    - message: "Happy Birthday!"
           ↓
BACKEND:
    1. Receives unique ID and form data
    2. Validates video file (size, format)
    3. Uploads video to Google Drive
    4. Gets Drive file ID: "1ABcd2EFgh3IJkl4MNop"
    5. Updates database:
       - greetingText: "John Doe"
       - message: "Happy Birthday!"
       - driveFileId: "1ABcd2EFgh3IJkl4MNop"
       - uploaded: true
           ↓
RESPONSE: "Video uploaded successfully"
           ↓
FRONTEND: Shows success message
```

**Database After Phase 3:**
```
greetings table:
+----+---------------------------+----------+-------+------------------+------------------------+----------------------+---------------------+----------+
| id | unique_id                 | greeting | phone | message          | qr_code_data           | drive_file_id        | created_at          | uploaded |
+----+---------------------------+----------+-------+------------------+------------------------+----------------------+---------------------+----------+
| 1  | GREETING_1733213456789   | John Doe | NULL  | Happy Birthday!  | iVBORw0KGgoAAAANSUh... | 1ABcd2EFgh3IJkl4MNop | 2025-12-03 10:30:00 | true     |
+----+---------------------------+----------+-------+------------------+------------------------+----------------------+---------------------+----------+
```

---

### Phase 4: View Greeting (User Views Uploaded Video)

```
USER ACTION: Opens greeting link
           ↓
APP CALLS: GET /greetings/GREETING_1733213456789/view
           ↓
BACKEND:
    1. Looks up greeting by unique ID in database
    2. Checks if video uploaded (uploaded = true)
    3. If uploaded:
       - Gets Drive file ID
       - Generates video playback URL from Drive
       - Returns greeting info
    4. If not uploaded:
       - Returns "No video uploaded yet"
           ↓
RESPONSE (if uploaded):
{
  "hasVideo": true,
  "status": "uploaded",
  "driveFileId": "1ABcd2EFgh3IJkl4MNop",
  "videoPlaybackUrl": "https://drive.google.com/file/d/1ABcd2EFgh3IJkl4MNop/preview",
  "submissionTimestamp": "2025-12-03T10:45:00",
  "name": "John Doe",
  "message": "Happy Birthday!"
}
           ↓
FRONTEND: 
    - Displays sender name: "John Doe"
    - Shows message: "Happy Birthday!"
    - Plays video from Google Drive
```

---

## 📊 DATA FLOW DIAGRAMS

### Complete System Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                        USER / FRONTEND                        │
│  (Web Browser / Mobile App)                                  │
└──────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP Requests
                              ▼
┌──────────────────────────────────────────────────────────────┐
│               NGINX (Port 80)                                 │
│  Proxies to: localhost:3002                                  │
└──────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────┐
│         SPRING BOOT APPLICATION (Port 3002)                   │
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │        GreetingController                          │     │
│  │  @RequestMapping("/greetings")                     │     │
│  │                                                     │     │
│  │  Endpoints:                                        │     │
│  │  - POST /generate                                  │     │
│  │  - GET /{uniqueId}/qr                              │     │
│  │  - POST /{uniqueId}/upload                         │     │
│  │  - GET /{uniqueId}/view                            │     │
│  └────────────────────────────────────────────────────┘     │
│                              │                               │
│                              ▼                               │
│  ┌────────────────────────────────────────────────────┐     │
│  │        GreetingService (MISSING/DELETED)           │     │
│  │  Business Logic:                                   │     │
│  │  - Generate unique IDs                             │     │
│  │  - Create QR codes                                 │     │
│  │  - Upload videos to Google Drive                  │     │
│  │  - Retrieve video playback URLs                   │     │
│  └────────────────────────────────────────────────────┘     │
│                              │                               │
│                              ▼                               │
│  ┌────────────────────────────────────────────────────┐     │
│  │        GreetingRepository (JPA)                    │     │
│  │  extends JpaRepository<Greeting, Long>             │     │
│  │                                                     │     │
│  │  Methods:                                          │     │
│  │  - save(Greeting)                                  │     │
│  │  - findByUniqueId(String)                          │     │
│  │  - findById(Long)                                  │     │
│  └────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────┐
│            MySQL DATABASE (selfie_preprod)                   │
│                                                              │
│  Table: greetings                                            │
│  - id (PK)                                                   │
│  - unique_id                                                 │
│  - greeting_text                                             │
│  - phone                                                     │
│  - message                                                   │
│  - qr_code_data                                              │
│  - drive_file_id                                             │
│  - created_at                                                │
│  - uploaded                                                  │
└──────────────────────────────────────────────────────────────┘
                              │
                              │ (When service is active)
                              ▼
┌──────────────────────────────────────────────────────────────┐
│            GOOGLE DRIVE API                                   │
│  - Upload videos                                             │
│  - Store video files                                         │
│  - Provide playback URLs                                     │
└──────────────────────────────────────────────────────────────┘
```

---

## 💻 CODE EXPLANATION (Line by Line)

### 1. GreetingController.java

```java
@RestController
@RequestMapping("/greetings")
public class GreetingController {
```
**What it does:**
- `@RestController`: Marks this as a REST API controller
- `@RequestMapping("/greetings")`: All endpoints start with `/greetings`

---

### Endpoint 1: Generate Greeting ID

```java
@PostMapping("/generate")
public ResponseEntity<String> generateGreetingLink() {
    // Generate simple unique ID - in production could use proper UUID
    String uniqueId = "GREETING_" + System.currentTimeMillis();
    return ResponseEntity.ok(uniqueId);
}
```

**Breakdown:**
1. `@PostMapping("/generate")` → Creates endpoint: `POST /greetings/generate`
2. `System.currentTimeMillis()` → Gets current time in milliseconds since 1970
   - Example: `1733213456789`
3. `"GREETING_" + timestamp` → Creates unique ID
   - Result: `"GREETING_1733213456789"`
4. `ResponseEntity.ok(uniqueId)` → Returns HTTP 200 with the ID

**Example:**
```
Request:  POST /greetings/generate
Response: "GREETING_1733213456789"
```

---

### Endpoint 2: Generate QR Code (DISABLED)

```java
@GetMapping(value = "/{uniqueId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
public byte[] generateQrCode(@PathVariable String uniqueId) throws Exception {
    // Return placeholder QR - service was migrated
    throw new RuntimeException("QR service temporarily disabled - migration in progress");
}
```

**Breakdown:**
1. `@GetMapping` → Creates GET endpoint
2. `"/{uniqueId}/qr"` → URL has a path variable
   - Example: `/greetings/GREETING_1733213456789/qr`
3. `produces = MediaType.IMAGE_PNG_VALUE` → Returns PNG image
4. `@PathVariable String uniqueId` → Extracts `uniqueId` from URL
5. `throw new RuntimeException(...)` → **Currently throws error** (service disabled)

**What it SHOULD do (when service is restored):**
```java
public byte[] generateQrCode(@PathVariable String uniqueId) throws Exception {
    // 1. Find greeting in database
    Optional<Greeting> greeting = greetingRepository.findByUniqueId(uniqueId);
    
    // 2. Create QR code URL
    String qrUrl = "https://celebrationsite-preprod.tanishq.co.in/greetings/" + uniqueId + "/upload";
    
    // 3. Generate QR code image
    byte[] qrCodeImage = qrCodeService.generateQRCode(qrUrl, 300, 300);
    
    // 4. Save QR code to database (as Base64)
    String base64QR = Base64.getEncoder().encodeToString(qrCodeImage);
    greeting.get().setQrCodeData(base64QR);
    greetingRepository.save(greeting.get());
    
    // 5. Return QR code image
    return qrCodeImage;
}
```

---

### Endpoint 3: Upload Video (DISABLED)

```java
@PostMapping(path = "/{uniqueId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<String> uploadVideo(
        @PathVariable String uniqueId,
        @RequestParam("video") MultipartFile videoFile,
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "message", required = false) String message) {
    // Stub implementation - video upload feature disabled during migration
    return ResponseEntity.ok("Video upload disabled during system upgrade");
}
```

**Breakdown:**
1. `@PostMapping` → Creates POST endpoint
2. `path = "/{uniqueId}/upload"` → URL pattern
3. `consumes = MediaType.MULTIPART_FORM_DATA_VALUE` → Accepts file uploads
4. `@PathVariable String uniqueId` → Gets greeting ID from URL
5. `@RequestParam("video") MultipartFile videoFile` → Gets uploaded video file
6. `@RequestParam(value = "name", required = false)` → Optional name parameter
7. Currently returns stub message

**What it SHOULD do (when service is restored):**
```java
public ResponseEntity<String> uploadVideo(...) {
    // 1. Find greeting in database
    Optional<Greeting> greetingOpt = greetingRepository.findByUniqueId(uniqueId);
    if (!greetingOpt.isPresent()) {
        return ResponseEntity.notFound().build();
    }
    Greeting greeting = greetingOpt.get();
    
    // 2. Validate video file
    if (videoFile.isEmpty()) {
        return ResponseEntity.badRequest().body("Video file is required");
    }
    if (videoFile.getSize() > 100_000_000) { // 100MB limit
        return ResponseEntity.badRequest().body("Video file too large");
    }
    
    // 3. Upload video to Google Drive
    String driveFileId = googleDriveService.uploadVideo(videoFile, uniqueId);
    
    // 4. Update database
    greeting.setGreetingText(name);
    greeting.setMessage(message);
    greeting.setDriveFileId(driveFileId);
    greeting.setUploaded(true);
    greetingRepository.save(greeting);
    
    // 5. Return success
    return ResponseEntity.ok("Video uploaded successfully");
}
```

**Example Request:**
```http
POST /greetings/GREETING_1733213456789/upload
Content-Type: multipart/form-data

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="video"; filename="birthday.mp4"
Content-Type: video/mp4

[binary video data]
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="name"

John Doe
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="message"

Happy Birthday!
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

---

### Endpoint 4: View Greeting Info (DISABLED)

```java
@GetMapping("/{uniqueId}/view")
public ResponseEntity<?> getGreetingInfo(@PathVariable String uniqueId) {
    // Stub implementation - greeting feature disabled during migration
    return ResponseEntity.status(404).body("Greeting feature temporarily unavailable - system upgrade in progress");
}
```

**What it SHOULD do (when service is restored):**
```java
public ResponseEntity<?> getGreetingInfo(@PathVariable String uniqueId) {
    // 1. Find greeting in database
    Optional<Greeting> greetingOpt = greetingRepository.findByUniqueId(uniqueId);
    if (!greetingOpt.isPresent()) {
        return ResponseEntity.notFound().build();
    }
    Greeting greeting = greetingOpt.get();
    
    // 2. Check if video uploaded
    boolean hasVideo = greeting.getUploaded() != null && greeting.getUploaded();
    String videoUrl = null;
    
    if (hasVideo && greeting.getDriveFileId() != null) {
        // 3. Generate Google Drive playback URL
        videoUrl = "https://drive.google.com/file/d/" + 
                   greeting.getDriveFileId() + "/preview";
    }
    
    // 4. Create response object
    GreetingInfo info = new GreetingInfo(
        hasVideo,
        hasVideo ? "uploaded" : "pending",
        greeting.getDriveFileId(),
        videoUrl,
        greeting.getCreatedAt() != null ? greeting.getCreatedAt().toString() : "",
        greeting.getGreetingText(),
        greeting.getMessage()
    );
    
    // 5. Return response
    return ResponseEntity.ok(info);
}
```

**Example Response (when video uploaded):**
```json
{
  "hasVideo": true,
  "status": "uploaded",
  "driveFileId": "1ABcd2EFgh3IJkl4MNop",
  "videoPlaybackUrl": "https://drive.google.com/file/d/1ABcd2EFgh3IJkl4MNop/preview",
  "submissionTimestamp": "2025-12-03T10:45:00",
  "name": "John Doe",
  "message": "Happy Birthday!"
}
```

---

## 🗄️ REPOSITORY LAYER

### GreetingRepository.java

```java
@Repository
public interface GreetingRepository extends JpaRepository<Greeting, Long> {
    Optional<Greeting> findByUniqueId(String uniqueId);
}
```

**What it does:**

1. `extends JpaRepository<Greeting, Long>`
   - Provides standard database operations:
     - `save(Greeting)` → INSERT or UPDATE
     - `findById(Long)` → SELECT by ID
     - `findAll()` → SELECT all
     - `delete(Greeting)` → DELETE
     - `count()` → COUNT rows

2. `Optional<Greeting> findByUniqueId(String uniqueId)`
   - **Custom query method**
   - Spring automatically generates SQL:
     ```sql
     SELECT * FROM greetings WHERE unique_id = ?
     ```
   - Returns `Optional<Greeting>` (empty if not found)

**Usage Example:**
```java
// Save new greeting
Greeting greeting = new Greeting();
greeting.setUniqueId("GREETING_1733213456789");
greeting.setUploaded(false);
greeting.setCreatedAt(LocalDateTime.now());
greetingRepository.save(greeting);

// Find by unique ID
Optional<Greeting> found = greetingRepository.findByUniqueId("GREETING_1733213456789");
if (found.isPresent()) {
    Greeting g = found.get();
    System.out.println("Found greeting: " + g.getUniqueId());
}
```

---

## 📦 DTO CLASSES

### GreetingInfo.java (Response DTO)

```java
public class GreetingInfo {
    private boolean hasVideo;           // Is video uploaded?
    private String status;              // "pending" or "uploaded"
    private String driveFileId;         // Google Drive file ID
    private String videoPlaybackUrl;    // URL to play video
    private String submissionTimestamp; // When video was uploaded
    private String name;                // Sender name
    private String message;             // Personal message
    
    // Constructor, Getters

