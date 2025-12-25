# QR Code Attendee Registration Fix

## Issue Identified
When users scanned the QR code after creating an event, they were being shown the **wrong application** - the "Tanishq - Heartfelt Video Messages" greetings app instead of the **Events Attendee Registration Form**.

### Root Cause
The `EventsController.java` had an incorrect forward path in the QR code scan handler:
- **Incorrect Path**: `forward:/qr/index.html` (Greetings/Video Messages app)
- **Correct Path**: `forward:/events.html` (Events Attendee Registration app)

## Files Modified

### 1. EventsController.java
**Location**: `src/main/java/com/dechub/tanishq/controller/EventsController.java`

**Changed Method**: `showAttendeeForm()`
```java
@GetMapping("/customer/{eventId}")
public ModelAndView showAttendeeForm(@PathVariable("eventId") String eventId) {
    // Forward to the attendee registration form (Events React app)
    return new ModelAndView("forward:/events.html");  // ✅ FIXED
}
```

**Previous Code** (INCORRECT):
```java
return new ModelAndView("forward:/qr/index.html");  // ❌ Wrong app
```

## Expected Flow Now

1. **Create Event** → Manager creates an event via the Events Management system
2. **Download QR Code** → URL like: `/events/dowload-qr/TEST_9d0c6280-76c7-40e8-88e3-5efd2f2bde4a`
3. **Scan QR Code** → QR code redirects to: `/events/customer/{eventId}`
4. **Show Attendee Form** → System now correctly shows the **Events Attendee Registration Form** from `/events.html`
5. **Submit Form** → Attendee data is posted to `/events/attendees` endpoint

## What Each HTML File Does

| File | Purpose | App Title |
|------|---------|-----------|
| `/qr/index.html` | Greetings/Video Messages app | "Tanishq - Heartfelt Video Messages" |
| `/events.html` | Events Attendee Registration | "Tanishq Events" |
| `/index.html` | Main application | - |

## Testing Checklist

- [ ] Build the application
- [ ] Deploy to pre-prod server
- [ ] Create a test event
- [ ] Download the QR code
- [ ] Scan the QR code with a mobile device
- [ ] Verify the **Events Attendee Registration Form** is displayed (not the greetings app)
- [ ] Fill in attendee details and submit
- [ ] Verify attendee is registered successfully

## Production Reference
Production URL pattern: `https://celebrations.tanishq.co.in/events/dowload-qr/TEST_9d0c6280-76c7-40e8-88e3-5efd2f2bde4a`

## Fix Date
December 10, 2025

## Status
✅ **FIXED** - Ready for build and deployment

