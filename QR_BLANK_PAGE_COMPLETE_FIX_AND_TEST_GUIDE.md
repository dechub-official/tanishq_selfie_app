   - events.html: Should return 200 OK
   - index-CLJQELnM.js: Should return 200 OK
   - index-CjU3bZCB.css: Should return 200 OK
   ```

3. **Test Static Files Directly**
   ```
   http://localhost:8130/static/static/assets/index-CLJQELnM.js
   
   Should return JavaScript code (not 404)
   ```

**Fix:**
- If 404: Check `application.properties` for correct resource paths
- If CORS error: Check CORS configuration in `application-preprod.properties`

### Issue 2: Form Shows But Submit Fails

**Symptoms:** Form displays, but submission gives error

**Debug Steps:**

1. **Check Backend Logs**
   ```bash
   tail -f application.log | grep "Received attendee submission"
   ```

2. **Verify Event Exists**
   ```sql
   SELECT * FROM events WHERE id = 'PASTE_EVENT_ID_HERE';
   ```

3. **Check API Endpoint**
   ```bash
   curl -X POST http://localhost:8130/events/attendees \
     -F "eventId=STORE123_uuid" \
     -F "name=Test User" \
     -F "phone=9876543210"
   ```

**Fix:**
- If "Event not found": QR code has wrong event ID
- If validation error: Check required fields

### Issue 3: QR Code URL is Wrong

**Symptoms:** QR code contains localhost or wrong domain

**Debug Steps:**

1. **Check Configuration**
   ```properties
   # In application-preprod.properties
   events.qr.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/
   ```

2. **Regenerate QR Code**
   - Delete old event QR
   - Download new QR after fixing config

**Fix:**
Update `application-preprod.properties` with correct production URL

### Issue 4: React Router Not Working

**Symptoms:** Page refreshes instead of client-side routing

**Debug Steps:**

1. **Check events.html has base tag**
   ```html
   <base href="/">
   ```

2. **Verify EventsWebConfig is loaded**
   ```bash
   # In logs, look for:
   grep "EventsWebConfig" application.log
   ```

3. **Check React Router configuration**
   The basename should be `/events`:
   ```javascript
   <Router basename="/events">
   ```

---

## 📊 SUCCESS CRITERIA

The fix is working correctly when:

✅ **QR Code Generation Works**
- Manager can download QR code
- QR contains correct production URL
- QR code image is valid PNG

✅ **QR Code Scanning Works**
- Scanning QR opens browser
- Attendee form is displayed (not blank page)
- All form fields are visible

✅ **Form Submission Works**
- User can fill all fields
- Submit button works
- Success message shows
- Redirects to thank you page

✅ **Data is Saved**
- Attendee record created in database
- Event attendee count incremented
- Manager can see attendee in dashboard

✅ **Logs Show Activity**
```
INFO  - QR code scanned for event: STORE123_uuid
INFO  - Received attendee submission - EventId: STORE123_uuid, Name: Test User, Phone: 9876543210
INFO  - Saved attendee with ID: 123
INFO  - Updated event STORE123_uuid attendee count from 0 to 1
```

---

## 🔍 VERIFICATION CHECKLIST

Use this checklist to verify the fix:

### Before Deployment
- [ ] Code compiles without errors (`mvn clean package`)
- [ ] No critical warnings in IDE
- [ ] `EventsWebConfig.java` exists in `config` package
- [ ] `events.html` has `<base href="/">` tag
- [ ] Configuration files have correct URLs

### After Deployment
- [ ] Application starts successfully
- [ ] No errors in startup logs
- [ ] Manager can login to events dashboard
- [ ] Can create a new event
- [ ] Can download QR code
- [ ] QR code URL is correct (production domain)

### QR Code Flow
- [ ] Scanning QR opens browser
- [ ] Attendee form is displayed (NOT blank page)
- [ ] Form shows all fields:
  - [ ] Full Name input
  - [ ] Phone Number input
  - [ ] RSO Name input
  - [ ] First Time checkbox
  - [ ] Submit button
- [ ] Form styling looks correct (Tanishq branding)

### Form Submission
- [ ] Can fill all fields
- [ ] Phone validation works (only allows valid Indian numbers)
- [ ] Submit button shows loading spinner
- [ ] Success message appears
- [ ] Redirects to thank you page with checkmark

### Backend Verification
- [ ] Attendee record in database
- [ ] Event attendee count updated
- [ ] Manager dashboard shows new attendee
- [ ] Can view attendee details
- [ ] Logs show successful submission

### Edge Cases
- [ ] Invalid event ID shows error (not blank page)
- [ ] Duplicate phone number is handled
- [ ] Empty required fields show validation
- [ ] Network error shows proper message
- [ ] Works on mobile browsers
- [ ] Works on desktop browsers

---

## 📱 MOBILE TESTING

Test on different devices:

### iOS (Safari)
- [ ] QR scan works
- [ ] Form displays correctly
- [ ] All fields are tappable
- [ ] Keyboard appears for inputs
- [ ] Submit works

### Android (Chrome)
- [ ] QR scan works
- [ ] Form displays correctly
- [ ] All fields are tappable
- [ ] Keyboard appears for inputs
- [ ] Submit works

### Common Mobile Issues
- Form too wide → Check viewport meta tag
- Text too small → Check font sizes in CSS
- Buttons not tappable → Check button sizes (min 44x44px)
- Keyboard covers input → Check scroll behavior

---

## 🎯 PRODUCTION DEPLOYMENT CHECKLIST

Before deploying to production:

### Configuration
- [ ] Update `events.qr.base.url` to production URL
- [ ] Update database credentials
- [ ] Update AWS S3 credentials (if applicable)
- [ ] Update email configuration
- [ ] Set correct `spring.profiles.active=prod`

### Testing
- [ ] Test QR code generation in prod
- [ ] Test QR code scanning in prod
- [ ] Test form submission in prod
- [ ] Verify data in prod database
- [ ] Check prod application logs

### Backup
- [ ] Backup production database before deployment
- [ ] Keep previous WAR file as backup
- [ ] Document rollback procedure

### Monitoring
- [ ] Set up log monitoring
- [ ] Set up error alerts
- [ ] Monitor database connections
- [ ] Monitor API response times

---

## 📞 SUPPORT

If issues persist after following this guide:

1. **Collect Diagnostic Info:**
   ```bash
   # Backend logs
   tail -n 500 application.log > diagnostic_logs.txt
   
   # Database state
   mysql -u root -p selfie_preprod -e "SELECT COUNT(*) FROM events; SELECT COUNT(*) FROM attendees;" > db_state.txt
   
   # Application info
   curl http://localhost:8130/actuator/health > health.txt
   ```

2. **Browser Console Logs:**
   - Open DevTools (F12)
   - Go to Console tab
   - Screenshot any errors
   - Copy network requests

3. **Test Event ID:**
   - Note the exact Event ID from QR code
   - Verify it exists in database

4. **Environment Details:**
   - OS version
   - Java version (`java -version`)
   - Browser and version
   - Mobile device (if applicable)

---

## 📝 SUMMARY

**Files Changed:**
1. `src/main/resources/static/events.html` - Added base tag
2. `src/main/java/com/dechub/tanishq/config/EventsWebConfig.java` - NEW file for SPA routing
3. `src/main/java/com/dechub/tanishq/controller/EventsController.java` - Improved logging

**Key Configuration:**
```properties
# application-preprod.properties
events.qr.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/
```

**Expected Flow:**
```
1. Manager creates event
2. QR code generated with URL: /events/customer/{eventId}
3. Customer scans QR code
4. Spring Boot serves events.html
5. React Router renders attendee form (from URL path)
6. Customer fills form
7. Form submits to /events/attendees endpoint
8. Backend saves attendee and increments count
9. Customer sees thank you page
```

**Next Steps:**
1. Build and deploy the application
2. Test QR code flow end-to-end
3. Verify database updates
4. Monitor logs for any issues

---

**Last Updated:** December 18, 2025  
**Status:** ✅ Fix Applied and Ready for Testing
# 🔧 QR CODE BLANK PAGE - COMPLETE FIX GUIDE

## ✅ What Was Fixed

### Problem
When scanning the QR code, users saw a **blank white page** instead of the attendee registration form.

### Root Cause
The React Router was not properly handling the `/events/customer/{eventId}` route due to:
1. Static resource path issues
2. Missing base tag in HTML
3. No proper Spring MVC configuration for SPA routing

### Solution Applied

#### 1. **Updated events.html** ✅
Added base tag to help resolve paths correctly:
```html
<!-- Add base tag to help resolve relative paths -->
<base href="/">
```

#### 2. **Created EventsWebConfig.java** ✅
New Spring configuration to handle React Router:
```java
@Configuration
public class EventsWebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serves static files
        // Forwards /events/customer/* to events.html for React Router
    }
}
```

#### 3. **Improved EventsController** ✅
Added better logging and documentation:
```java
@GetMapping("/customer/{eventId}")
public String showAttendeeForm(@PathVariable("eventId") String eventId) {
    log.info("QR code scanned for event: {}", eventId);
    return "forward:/events.html";
}
```

---

## 🧪 TESTING INSTRUCTIONS

### Step 1: Rebuild the Application

**On Windows:**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -DskipTests
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Step 2: Deploy to Server (if applicable)

```bash
# Copy the WAR file to server
scp target/tanishq-*.war user@server:/opt/tanishq/applications_preprod/

# On server, restart the application
cd /opt/tanishq/applications_preprod
# Stop existing process
pkill -f tanishq-preprod

# Start new version
nohup java -jar tanishq-preprod-*.war --spring.profiles.active=preprod > application.log 2>&1 &

# Check logs
tail -f application.log
```

### Step 3: Test QR Code Flow

#### A. **Generate QR Code**

1. Login to Events Dashboard:
   ```
   http://localhost:8130/events
   OR
   https://celebrationsite-preprod.tanishq.co.in/events
   ```

2. Create a new event

3. Download QR code - it should contain URL like:
   ```
   https://celebrationsite-preprod.tanishq.co.in/events/customer/STORE123_uuid
   ```

#### B. **Test QR Code Scan**

**Option 1: Direct Browser Test**
```
1. Copy the QR code URL
2. Paste in browser
3. Should see: Attendee Registration Form with:
   - "I'm attending Tanishq Celebration" heading
   - Full Name field
   - Phone Number field
   - RSO Name field
   - "First time at Tanishq" checkbox
   - Submit button
```

**Option 2: Mobile QR Scan**
```
1. Use phone camera or QR scanner app
2. Scan the QR code
3. Should redirect to attendee form
4. Fill the form and submit
```

#### C. **Verify Backend Logs**

Check if the endpoint is being hit:
```bash
# On server
tail -f application.log | grep "QR code scanned"

# Expected log:
# INFO  - QR code scanned for event: STORE123_uuid
```

### Step 4: Test Form Submission

1. Fill in the form:
   - **Full Name:** Test User
   - **Phone Number:** 9876543210
   - **RSO Name:** Test RSO
   - **First Time:** Check/uncheck

2. Click **Submit**

3. **Expected Behavior:**
   - Success message appears
   - Redirected to "Thank You" page
   - Database updated with attendee info
   - Event attendee count incremented

4. **Verify in Database:**
   ```sql
   -- Check attendee was created
   SELECT * FROM attendees 
   WHERE event_id = 'STORE123_uuid' 
   ORDER BY created_at DESC LIMIT 1;
   
   -- Check event attendee count was incremented
   SELECT id, event_name, attendees 
   FROM events 
   WHERE id = 'STORE123_uuid';
   ```

---

## 🐛 TROUBLESHOOTING

### Issue 1: Still Seeing Blank Page

**Symptoms:** White/blank page after scanning QR

**Debug Steps:**

1. **Check Browser Console (F12)**
   ```
   Look for errors like:
   - "Failed to load resource" → Static files not loading
   - "Cannot GET /events/customer/..." → Routing issue
   - React errors → JS bundle issue
   ```

2. **Check Network Tab**
   ```

