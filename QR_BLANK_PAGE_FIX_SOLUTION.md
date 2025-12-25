# 🚨 QR CODE SCANNING SHOWS BLANK PAGE - ROOT CAUSE & FIX

## Problem Description

**Issue:** When scanning the QR code generated for an event, users see a **BLANK PAGE** instead of the attendee registration form.

**QR Code URL Format:** 
```
https://celebrationsite-preprod.tanishq.co.in/events/customer/{eventId}
```

**Expected:** Attendee registration form with fields for name, phone, RSO name, etc.  
**Actual:** Blank white page

---

## 🔍 Root Cause Analysis

### 1. **Backend Controller is Working ✅**
```java
@GetMapping("/customer/{eventId}")
public ModelAndView showAttendeeForm(@PathVariable("eventId") String eventId) {
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("forward:/events.html");
    return modelAndView;
}
```
- Controller correctly forwards to `/events.html`
- No errors here

### 2. **React Router is Configured ✅**
From the JavaScript bundle analysis:
```javascript
// React Router with basename
<Router basename="/events">
  <Route path="/customer/:id" element={<AttendeeForm />} />
</Router>
```
- Route exists for `/customer/:id`
- Attendee form component (`m0` function) is defined

### 3. **THE ACTUAL PROBLEM ❌**

The issue is a **BASE URL MISMATCH** between:
- **QR Code URL:** `/events/customer/{eventId}` 
- **React Router Expectation:** `/events` (base) + `/customer/:id` (route)

When Spring Boot forwards to `/events.html`, React Router tries to match the URL path, but there's likely an issue with:

1. **Static Resource Serving**: The `events.html` file may not be loading the React JavaScript bundle correctly
2. **Router Base Path**: React Router basename might not be aligning with the actual URL
3. **JavaScript Bundle Path**: The JS file paths in `events.html` might be incorrect

---

## 🔧 SOLUTION

### **Quick Fix Option 1: Check events.html JavaScript Paths**

The `events.html` file has these script/link tags:
```html
<script type="module" crossorigin src="/static/static/assets/index-CLJQELnM.js"></script>
<link rel="stylesheet" crossorigin href="/static/static/assets/index-CjU3bZCB.css">
```

**Problem**: The paths start with `/static/static/` which might not resolve correctly when accessed via `/events/customer/{id}`.

**Fix**: Update the paths to be absolute or use correct context path:


