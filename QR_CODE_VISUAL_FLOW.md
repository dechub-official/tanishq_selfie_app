# QR Code Flow - Visual Guide

## 🎯 Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         MANAGER CREATES EVENT                        │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Manager Dashboard: https://celebrations.tanishq.co.in/events/      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Event Form:                                                   │  │
│  │  • Event Name                                                  │  │
│  │  • Event Type (Diamond Awareness, GEP, etc.)                  │  │
│  │  • Date & Time                                                │  │
│  │  • Location                                                   │  │
│  │  • Customer List (Excel upload)                              │  │
│  │  [Submit Button]                                              │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Backend: POST /events/upload                                        │
│  • Saves event to database                                          │
│  • Generates unique Event ID: TEST_9d0c6280-76c7-40e8...           │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Dashboard shows "Download QR Code" button                          │
│  [📥 Download QR] [📊 View Attendees] [📝 Edit Event]              │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Manager clicks "Download QR Code"                                  │
│  GET /events/dowload-qr/TEST_9d0c6280-76c7-40e8-88e3-5efd2f2bde4a │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  QR Code Service Generates:                                         │
│  ┌─────────────────────────────────────────┐                       │
│  │  QR Code contains URL:                   │                       │
│  │  https://celebrations.tanishq.co.in/    │                       │
│  │  events/customer/                        │                       │
│  │  TEST_9d0c6280-76c7-40e8-88e3-5efd2f2bde4a                     │
│  │                                          │                       │
│  │  [QR CODE IMAGE]                         │                       │
│  │  █▀▀▀▀▀█ ▄▀▄█▄ █▀▀▀▀▀█                  │                       │
│  │  █ ███ █ ▀█▀██ █ ███ █                  │                       │
│  │  █ ▀▀▀ █ █▀ █▀ █ ▀▀▀ █                  │                       │
│  └─────────────────────────────────────────┘                       │
│  Returns: Base64 encoded PNG image                                 │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Manager downloads and prints/shares QR code                        │
│  QR code placed at event venue entrance                             │
└─────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════
                        ATTENDEE ARRIVES AT EVENT
═══════════════════════════════════════════════════════════════════════

                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  📱 ATTENDEE SCANS QR CODE WITH PHONE CAMERA                        │
│  ┌─────────────────────────────────────────┐                       │
│  │                                          │                       │
│  │      [Camera viewfinder]                 │                       │
│  │                                          │                       │
│  │         ┌──────────┐                     │                       │
│  │         │ QR CODE  │                     │                       │
│  │         │ DETECTED │                     │                       │
│  │         └──────────┘                     │                       │
│  │                                          │                       │
│  │  "Open in Chrome?"                       │                       │
│  │  [Open] [Cancel]                         │                       │
│  └─────────────────────────────────────────┘                       │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  📱 Phone opens URL in browser:                                     │
│  https://celebrations.tanishq.co.in/events/customer/               │
│  TEST_9d0c6280-76c7-40e8-88e3-5efd2f2bde4a                         │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Backend Endpoint: GET /events/customer/{eventId}                   │
│  EventsController.java:                                             │
│  @GetMapping("/customer/{eventId}")                                 │
│  public ModelAndView showAttendeeForm(                              │
│      @PathVariable("eventId") String eventId) {                     │
│      return new ModelAndView("forward:/qr/index.html");            │
│  }                                                                  │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  📱 Forwards to React App: /qr/index.html                          │
│  (Located: src/main/resources/static/qr/index.html)                │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  📱 ATTENDEE REGISTRATION FORM DISPLAYED                           │
│  ┌─────────────────────────────────────────┐                       │
│  │  🏪 Tanishq Celebrations                 │                       │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │                       │
│  │                                          │                       │
│  │  📝 Register for Event                   │                       │
│  │                                          │                       │
│  │  👤 Name: [________________]             │                       │
│  │                                          │                       │
│  │  📱 Phone: [________________]            │                       │
│  │                                          │                       │
│  │  ❤️  What do you like?                   │                       │
│  │     [ ] Gold Jewelry                     │                       │
│  │     [ ] Diamond Jewelry                  │                       │
│  │     [ ] Gemstone Jewelry                 │                       │
│  │                                          │                       │
│  │  ⭐ First time at Tanishq?               │                       │
│  │     ( ) Yes  ( ) No                      │                       │
│  │                                          │                       │
│  │  📸 Upload Photo (Optional)              │                       │
│  │     [Choose File] [No file chosen]       │                       │
│  │                                          │                       │
│  │  [✓ Submit Registration]                 │                       │
│  │                                          │                       │
│  └─────────────────────────────────────────┘                       │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Attendee fills form and taps "Submit Registration"                │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  React App sends POST request:                                      │
│  POST /events/attendees                                             │
│  Content-Type: multipart/form-data                                  │
│                                                                     │
│  Form Data:                                                         │
│  • eventId: TEST_9d0c6280-76c7-40e8-88e3-5efd2f2bde4a             │
│  • name: "John Doe"                                                │
│  • phone: "9876543210"                                             │
│  • like: "Gold Jewelry"                                            │
│  • firstTimeAtTanishq: true                                        │
│  • file: [photo.jpg] (if uploaded)                                │
│  • rsoName: "Store RSO Name"                                       │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Backend: EventsController.storeAttendeesData()                     │
│  1. Validates data                                                  │
│  2. Saves attendee info to database                                │
│  3. Links attendee to event via eventId                            │
│  4. Saves photo to storage (if provided)                           │
│  5. Returns success response                                        │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  📱 SUCCESS MESSAGE SHOWN TO ATTENDEE                              │
│  ┌─────────────────────────────────────────┐                       │
│  │  ✅ Registration Successful!             │                       │
│  │                                          │                       │
│  │  Thank you for registering!              │                       │
│  │  Enjoy your event at Tanishq.            │                       │
│  │                                          │                       │
│  │  [OK]                                    │                       │
│  └─────────────────────────────────────────┘                       │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Data stored in database:                                           │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ attendees_table                                              │  │
│  │ ───────────────────────────────────────────────────────────  │  │
│  │ id  event_id         name      phone       like    photo    │  │
│  │ 1   TEST_9d0c...    John Doe  9876543210  Gold    img.jpg   │  │
│  └─────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Manager can view attendees in dashboard:                          │
│  GET /events/getevents                                             │
│  • See all registered attendees                                    │
│  • View attendee photos                                            │
│  • Export attendee list                                            │
│  • Track event statistics                                          │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Key Points

### 1. **QR Code Generation**
- QR code is generated when manager clicks "Download QR Code"
- Contains URL: `https://celebrations.tanishq.co.in/events/customer/{eventId}`
- Event ID is a UUID that uniquely identifies the event

### 2. **QR Code Scanning**
- Attendee uses phone camera to scan QR code
- Phone reads URL from QR code
- Browser automatically opens the URL

### 3. **Backend Redirect**
- URL hits `/events/customer/{eventId}` endpoint
- Backend extracts event ID
- Forwards request to React app at `/qr/index.html`
- React app loads on attendee's phone

### 4. **Form Display**
- React app shows registration form
- Event ID is embedded in the page context
- Form fields match attendee needs

### 5. **Form Submission**
- Data sent to `/events/attendees` endpoint
- Backend validates and saves data
- Photo uploaded to storage (if provided)
- Success message shown to attendee

---

## 🔄 Data Flow Summary

```
Manager → Creates Event → Downloads QR Code
    ↓
QR Code contains: https://celebrations.tanishq.co.in/events/customer/{eventId}
    ↓
Attendee → Scans QR Code → Opens URL in phone browser
    ↓
Backend → Redirects to → /qr/index.html (React App)
    ↓
React App → Shows Form → Attendee fills details
    ↓
Form Submit → POST /events/attendees → Data saved to DB
    ↓
Success message → Attendee registered ✅
```

---

## 🎨 URL Structure

**Production URLs:**
```
Dashboard:        https://celebrations.tanishq.co.in/events/dashboard
QR Download:      https://celebrations.tanishq.co.in/events/dowload-qr/{eventId}
QR Scan URL:      https://celebrations.tanishq.co.in/events/customer/{eventId}
React App:        https://celebrations.tanishq.co.in/qr/index.html
Attendee Submit:  https://celebrations.tanishq.co.in/events/attendees
```

**Preprod URLs:**
```
Dashboard:        https://celebrationsite-preprod.tanishq.co.in/events/dashboard
QR Download:      https://celebrationsite-preprod.tanishq.co.in/events/dowload-qr/{eventId}
QR Scan URL:      https://celebrationsite-preprod.tanishq.co.in/events/customer/{eventId}
React App:        https://celebrationsite-preprod.tanishq.co.in/qr/index.html
Attendee Submit:  https://celebrationsite-preprod.tanishq.co.in/events/attendees
```

---

## 📱 Mobile User Experience

### Step-by-Step:

1. **Attendee arrives at event venue**
   - Sees QR code poster at entrance
   
2. **Opens phone camera**
   - Points camera at QR code
   - Camera detects QR code automatically
   
3. **Notification appears**
   - "Open celebrations.tanishq.co.in?"
   - Taps "Open"
   
4. **Browser loads**
   - Page loads with Tanishq branding
   - Registration form appears
   
5. **Fills form**
   - Enters name and phone
   - Selects preferences
   - Optionally uploads selfie
   
6. **Submits form**
   - Taps "Submit Registration"
   - Loading indicator shows
   
7. **Success message**
   - "Registration Successful!"
   - Can close browser or continue browsing

---

## 🔧 Technical Implementation

### Backend Controller:
```java
@GetMapping("/customer/{eventId}")
public ModelAndView showAttendeeForm(@PathVariable("eventId") String eventId) {
    return new ModelAndView("forward:/qr/index.html");
}
```

### QR Code Generation:
```java
@GetMapping("/dowload-qr/{id}")
private QrResponseDTO downloadQr(@PathVariable("id") String eventId) {
    String qrCodeBase64 = qrCodeService.generateEventQrCode(eventId);
    return new QrResponseDTO(true, "data:image/png;base64," + qrCodeBase64);
}
```

### Attendee Submission:
```java
@PostMapping("/attendees")
public ResponseDataDTO storeAttendeesData(
    @RequestParam("eventId") String eventId,
    @RequestParam("name") String name,
    @RequestParam("phone") String phone,
    @RequestParam("like") String like,
    @RequestParam("firstTimeAtTanishq") boolean firstTime,
    @RequestParam(value = "file", required = false) MultipartFile file
) {
    // Save attendee data
    return tanishqPageService.storeAttendeesData(attendeeDTO);
}
```

---

## ✅ Verification Checklist

**To verify the flow is working:**

- [ ] Manager can create events
- [ ] Manager can download QR codes
- [ ] QR code contains correct production URL
- [ ] Scanning QR code opens attendee form
- [ ] Form displays correctly on mobile
- [ ] Attendee can submit registration
- [ ] Data is saved to database
- [ ] Manager can view attendees in dashboard
- [ ] Photos are stored correctly (if uploaded)

---

**Last Updated:** December 10, 2025
**Purpose:** Visual guide to understand QR code flow from manager to attendee

