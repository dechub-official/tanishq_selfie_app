2. Is EventQrCodeServiceImpl class found by Spring?
3. Check logs for errors

### Issue: Attendee form not showing after scan
**Check:**
1. Is `/events/customer/{eventId}` endpoint working?
2. Is `events.html` file present in webapp root?
3. Is React app built correctly?

### Issue: Attendee count not increasing
**Check:**
1. Is `/events/attendees` endpoint receiving requests?
2. Is event ID valid in database?
3. Check logs in `TanishqPageService.storeAttendeesData()`

---

## Summary

✅ **Created dedicated EventQrCodeService** - Separate from Greetings  
✅ **Updated EventsController** - Uses dedicated service  
✅ **Updated TanishqPageService** - Uses dedicated service  
✅ **Updated all properties files** - Separate configuration  
✅ **Preserved Google Sheets logic** - Exact same QR generation  
✅ **Maintained attendee flow** - Form → Submit → Count increment  
✅ **No mixing with Greetings** - Complete separation  

**Status:** Ready to build and deploy! 🎉

---

**Document Version:** 1.0  
**Last Updated:** December 18, 2025  
**Implementation:** Complete
# Events QR Code - Dedicated Implementation Guide

**Date:** December 18, 2025  
**Status:** ✅ COMPLETED - Separate from Greetings

---

## Overview

This document describes the **dedicated Events QR Code implementation** that is completely **separate** from the Greetings QR code functionality, just like it was in the Google Sheets implementation.

---

## Architecture - Complete Separation

### Events QR Code System (Dedicated)
```
Service:     EventQrCodeService / EventQrCodeServiceImpl
Controller:  EventsController
Purpose:     Customer attendee registration
Config:      events.qr.base.url
Status:      ✅ ACTIVE & SEPARATED
```

### Greetings QR Code System (Separate)
```
Service:     QrCodeService / QrCodeServiceImpl
Controller:  GreetingController
Purpose:     Video greeting messages
Config:      greeting.qr.base.url
Status:      ✅ SEPARATE (Not mixed)
```

---

## Files Created (New Dedicated Service)

### 1. EventQrCodeService.java
**Location:** `src/main/java/com/dechub/tanishq/service/events/EventQrCodeService.java`

**Purpose:** Interface for Events QR code generation ONLY

```java
package com.dechub.tanishq.service.events;

public interface EventQrCodeService {
    /**
     * Generate QR code for event attendee registration
     * @param eventId The event ID
     * @return Base64-encoded PNG image string
     */
    String generateEventQrCode(String eventId) throws Exception;
}
```

### 2. EventQrCodeServiceImpl.java
**Location:** `src/main/java/com/dechub/tanishq/service/events/EventQrCodeServiceImpl.java`

**Purpose:** Implementation of Events QR code generation - exact replica of Google Sheets logic

**Key Features:**
- ✅ Uses ZXing library (same as Google Sheets)
- ✅ QR size: 300x300 pixels (same as Google Sheets)
- ✅ Output: Base64-encoded PNG (same as Google Sheets)
- ✅ URL format: `{baseUrl}/events/customer/{eventId}` (same as Google Sheets)

**Configuration:**
```java
@Value("${events.qr.base.url:http://localhost:8130/events/customer/}")
private String eventsQrBaseUrl;
```

---

## Files Updated

### 1. EventsController.java
**Changed:**
- ❌ Removed: `import com.dechub.tanishq.service.qr.QrCodeService;`
- ✅ Added: `import com.dechub.tanishq.service.events.EventQrCodeService;`
- ❌ Removed: `@Autowired private QrCodeService qrCodeService;`
- ✅ Added: `@Autowired private EventQrCodeService eventQrCodeService;`

**Updated Endpoint:**
```java
@GetMapping("/dowload-qr/{id}")
private QrResponseDTO downloadQr(@PathVariable("id") String eventId){
    QrResponseDTO qrResponseDTO = new QrResponseDTO();
    try {
        // Use dedicated Event QR Code Service (NOT Greeting QR service)
        String qrCodeBase64 = eventQrCodeService.generateEventQrCode(eventId);
        qrResponseDTO.setStatus(true);
        qrResponseDTO.setQrData("data:image/png;base64," + qrCodeBase64);
    } catch (Exception e) {
        qrResponseDTO.setStatus(false);
        qrResponseDTO.setQrData("Error generating QR code: " + e.getMessage());
    }
    return qrResponseDTO;
}
```

### 2. TanishqPageService.java
**Changed:**
- ❌ Removed: `private com.dechub.tanishq.service.qr.QrCodeService qrCodeService;`
- ✅ Added: `private com.dechub.tanishq.service.events.EventQrCodeService eventQrCodeService;`

**Updated Method:**
```java
// Generate QR code using dedicated Event QR Code Service
// This service is separate from Greeting QR functionality
String qrCode;
try {
    qrCode = eventQrCodeService.generateEventQrCode(eventId);
} catch (Exception qrError) {
    log.error("Failed to generate QR code for event {}: {}", eventId, qrError.getMessage());
    response.setQrData("Error generating QR code: " + qrError.getMessage());
    return response;
}
```

### 3. Application Properties Files

#### application-preprod.properties
```properties
# Events QR Code Configuration (Dedicated for Events - Customer Registration)
# URL Format: https://celebrationsite-preprod.tanishq.co.in/events/customer/{eventId}
events.qr.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/

# Greeting QR Code Configuration (Separate - for Video Greetings)
greeting.qr.base.url=https://celebrationsite-preprod.tanishq.co.in/greetings/

# Legacy QR Code Config (DEPRECATED - Use events.qr.base.url for Events)
qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/
```

#### application-prod.properties
```properties
# Events QR Code Configuration (Dedicated for Events - Customer Registration)
events.qr.base.url=https://celebrationsite.tanishq.co.in/events/customer/

# Greeting QR Code Configuration (Separate - for Video Greetings)
greeting.qr.base.url=https://celebrationsite.tanishq.co.in/greetings/

# Legacy QR Code Config (DEPRECATED - Use events.qr.base.url for Events)
qr.code.base.url=https://celebrationsite.tanishq.co.in/events/customer/
```

#### application-test.properties
```properties
# Events QR Code Configuration (Dedicated for Events - Customer Registration)
events.qr.base.url=http://localhost:8130/events/customer/

# Legacy QR Code Config (DEPRECATED - Use events.qr.base.url for Events)
qr.code.base.url=http://localhost:8130/events/customer/
```

---

## Complete Event Flow (With Attendee Count)

### Step 1: Manager Creates Event
**Endpoint:** `POST /events/upload`

**What Happens:**
1. Manager fills event form (event type, date, invitees Excel, etc.)
2. Backend creates Event record in `events` table
3. Generates unique Event ID: `{storeCode}_{UUID}`
4. **EventQrCodeService** generates QR code
5. QR URL: `https://celebrationsite-preprod.tanishq.co.in/events/customer/{eventId}`
6. Returns Base64 QR code to frontend
7. Frontend auto-downloads QR code as PNG

**Database:**
```sql
INSERT INTO events (
    id,                                    -- STORE123_uuid
    store_code,                            -- STORE123
    event_type,                            -- Birthday
    start_date,                            -- 2025-12-20
    invitees,                              -- 50 (from Excel)
    attendees,                             -- 0 (initial)
    ...
);
```

### Step 2: Customer Scans QR Code
**What Customer Does:**
1. Opens camera/QR scanner app
2. Scans QR code printed by manager
3. Phone opens browser with URL

**URL Opened:**
```
https://celebrationsite-preprod.tanishq.co.in/events/customer/STORE123_9d0c6280-76c7-40e8-88e3-5efd2f2bde4a
```

### Step 3: Spring Boot Handles Request
**Endpoint:** `GET /events/customer/{eventId}`

```java
@GetMapping("/customer/{eventId}")
public ModelAndView showAttendeeForm(@PathVariable("eventId") String eventId) {
    // Forward to the attendee registration form (Events React app)
    return new ModelAndView("forward:/events.html");
}
```

**What Happens:**
1. Spring Boot receives request
2. Extracts `eventId` from URL path
3. Forwards to `/events.html` (React app)
4. React app loads with `eventId` in URL

### Step 4: React App Shows Attendee Form
**React Route:** `/events/customer/:id`

**Form Fields:**
- Name (required)
- Phone (required)
- What did you like? (text)
- First time at Tanishq? (checkbox)
- RSO Name (optional)
- Selfie upload (optional)

**Frontend Code:**
```javascript
// React component reads eventId from URL
const { id: eventId } = useParams();

// When user submits form
const submitAttendee = async (formData) => {
    const response = await fetch('/events/attendees', {
        method: 'POST',
        body: formData  // Contains eventId + attendee data
    });
};
```

### Step 5: Customer Submits Form
**Endpoint:** `POST /events/attendees`

**Parameters:**
- `eventId` (from URL)
- `name` (user input)
- `phone` (user input)
- `like` (user input)
- `firstTimeAtTanishq` (boolean)
- `file` (optional selfie)
- `rsoName` (optional)

**Backend Processing:**
```java
@PostMapping("/attendees")
public ResponseDataDTO storeAttendeesData(...) {
    // Create attendee DTO
    AttendeesDetailDTO attendeesDetailDTO = new AttendeesDetailDTO();
    attendeesDetailDTO.setId(eventId);
    attendeesDetailDTO.setName(name);
    attendeesDetailDTO.setPhone(phone);
    // ... other fields
    
    // Save to database
    return tanishqPageService.storeAttendeesData(attendeesDetailDTO);
}
```

### Step 6: Save Attendee & Update Count
**Service Method:** `TanishqPageService.storeAttendeesData()`

**What Happens:**
1. Find Event by `eventId`
2. Create new Attendee record
3. Save to `attendees` table
4. **Increment attendee count** in `events` table
5. Upload selfie to S3 (if provided)
6. Return success response

**Database Operations:**
```sql
-- 1. Insert attendee record
INSERT INTO attendees (
    event_id,                  -- Links to events.id
    name,
    phone,
    like_text,
    first_time_at_tanishq,
    rso_name,
    created_at
) VALUES (...);

-- 2. Update event attendee count (AUTOMATIC)
UPDATE events 
SET attendees = attendees + 1 
WHERE id = 'STORE123_uuid';
```

**Result:**
- ✅ Attendee saved to database
- ✅ Attendee count incremented automatically
- ✅ Manager sees updated count in dashboard
- ✅ Customer sees success message

---

## QR Code URL Pattern

### Format:
```
{events.qr.base.url}{eventId}
```

### Example - Preprod:
```
Base URL:  https://celebrationsite-preprod.tanishq.co.in/events/customer/
Event ID:  STORE123_9d0c6280-76c7-40e8-88e3-5efd2f2bde4a
QR URL:    https://celebrationsite-preprod.tanishq.co.in/events/customer/STORE123_9d0c6280-76c7-40e8-88e3-5efd2f2bde4a
```

### Example - Production:
```
Base URL:  https://celebrationsite.tanishq.co.in/events/customer/
Event ID:  STORE123_9d0c6280-76c7-40e8-88e3-5efd2f2bde4a
QR URL:    https://celebrationsite.tanishq.co.in/events/customer/STORE123_9d0c6280-76c7-40e8-88e3-5efd2f2bde4a
```

---

## Attendee Count Logic

### Automatic Increment
```java
// In TanishqPageService.storeAttendeesData()

// 1. Find event
Optional<Event> eventOpt = eventRepository.findById(eventId);
Event event = eventOpt.get();

// 2. Create attendee
Attendee attendee = new Attendee();
attendee.setEvent(event);  // Links to event
attendee.setName(name);
// ... other fields

// 3. Save attendee
attendeeRepository.save(attendee);

// 4. Update event count
int currentCount = event.getAttendees() != null ? event.getAttendees() : 0;
event.setAttendees(currentCount + 1);
eventRepository.save(event);

// Done! Attendee count is now updated
```

### Manager Dashboard
- Manager logs in
- Views "Completed Events" table
- Sees:
  - Event Name
  - Date
  - **Invitees:** 50 (from Excel)
  - **Attendees:** 12 (auto-updated as customers register)
  - Attendance %: 24%

---

## Comparison: Google Sheets vs Database

| Feature | Google Sheets (Old) | Database (New - Separated) |
|---------|---------------------|----------------------------|
| **QR Generation Service** | `GSheetUserDetailsUtil.generateQrCode()` | `EventQrCodeService.generateEventQrCode()` |
| **Service Location** | Inline in util class | Dedicated service (`service/events/`) |
| **Mixed with Greetings?** | ❌ No (separate util) | ❌ No (separate service) ✅ |
| **URL Format** | `{url}/events/customer/{id}` | `{events.qr.base.url}{id}` ✅ |
| **QR Library** | ZXing (QRCodeWriter) | ZXing (QRCodeWriter) ✅ |
| **QR Size** | 300x300 px | 300x300 px ✅ |
| **Output Format** | PNG → Base64 | PNG → Base64 ✅ |
| **Controller Prefix** | `data:image/png;base64,` | `data:image/png;base64,` ✅ |
| **Attendee Storage** | Google Sheets row | MySQL `attendees` table ✅ |
| **Count Update** | Manual / Script | Automatic (JPA) ✅ |

**Result:** ✅ **Perfect Match - Logic Preserved, Separation Maintained**

---

## Key Benefits of Separation

### 1. **Clear Responsibility**
- EventQrCodeService → Only handles Events
- QrCodeService → Only handles Greetings
- No confusion, no mixing

### 2. **Independent Configuration**
```properties
events.qr.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/
greeting.qr.base.url=https://celebrationsite-preprod.tanishq.co.in/greetings/
```

### 3. **Independent Evolution**
- Events QR can change without affecting Greetings
- Greetings QR can change without affecting Events

### 4. **Easier Testing**
- Test Events QR independently
- Test Greetings QR independently

### 5. **Matches Google Sheets Architecture**
- Google Sheets had separate utils
- Now we have separate services
- Same separation principle ✅

---

## Testing Guide

### 1. Build the Application
```bash
cd c:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -Ppreprod
```

### 2. Deploy WAR File
```bash
# Copy to Tomcat
copy target\tanishq-preprod-*.war c:\path\to\tomcat\webapps\
```

### 3. Test QR Generation
```bash
# Create event
POST http://localhost:3000/events/upload
# Returns QR code with base64 data

# Download QR later
GET http://localhost:3000/events/dowload-qr/{eventId}
# Returns same QR code
```

### 4. Test QR Scanning Flow
```
1. Generate QR code for event
2. Print QR code or display on screen
3. Scan with mobile phone camera
4. Should open: http://localhost:3000/events/customer/{eventId}
5. Should show: Attendee registration form
6. Fill form and submit
7. Should see: Success message
8. Check database: New row in attendees table
9. Check event: Attendee count increased by 1
```

### 5. Verify Separation
```bash
# Events uses EventQrCodeService
curl http://localhost:3000/events/dowload-qr/TEST_123

# Greetings uses QrCodeService (different service)
curl http://localhost:3000/greetings/GREETING_456/qr

# Both work independently ✅
```

---

## Troubleshooting

### Issue: QR code not generating
**Check:**
1. Is `events.qr.base.url` configured in properties file?

