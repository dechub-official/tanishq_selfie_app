# 🧪 QR Attendee Fix - Testing Guide

## Prerequisites

Before testing, ensure:
- ✅ MySQL database `selfie_preprod` is running
- ✅ Application is built with the latest changes
- ✅ At least one store exists in the `stores` table
- ✅ Server is running on port 3000

---

## 🔧 Step 1: Verify Database Schema

Run the verification SQL script:

```bash
mysql -u root -p"Dechub#2025" selfie_preprod < verify_qr_attendee_fix.sql
```

Or manually check:

```sql
USE selfie_preprod;

-- Check events table
DESC events;
-- Verify: id is VARCHAR(255)

-- Check attendees table
DESC attendees;
-- Verify: event_id is VARCHAR(255)

-- Check foreign key
SHOW CREATE TABLE attendees\G
-- Verify: CONSTRAINT includes FOREIGN KEY (event_id) REFERENCES events(id)
```

**Expected Result:** ✅ All tables have correct structure with matching VARCHAR(255) types

---

## 🔧 Step 2: Start Application

```bash
# Navigate to project directory
cd c:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Build the project
mvn clean package -DskipTests

# Run the application (or deploy WAR to Tomcat)
java -jar target/tanishq-*.war
```

**Expected Result:** ✅ Application starts without errors

Check logs for:
```
INFO: Started TanishqApplication
INFO: Tomcat started on port(s): 3000
```

---

## 🔧 Step 3: Test Event Creation

### Test Case 3.1: Create Event with Single Invitee

**Request:**
```bash
curl -X POST "http://localhost:3000/events/upload" \
  -F "code=STORE001" \
  -F "eventName=QR Test Event" \
  -F "eventType=Test" \
  -F "eventSubType=Verification" \
  -F "RSO=Test RSO" \
  -F "date=2025-12-20" \
  -F "time=10:00" \
  -F "location=Test Location" \
  -F "Community=General" \
  -F "region=North" \
  -F "sale=0" \
  -F "advance=0" \
  -F "ghsOrRga=0" \
  -F "gmb=0" \
  -F "diamondAwareness=false" \
  -F "ghsFlag=false"
```

**Expected Response:**
```json
{
  "status": true,
  "qrData": "data:image/png;base64,iVBORw0KG..."
}
```

**Verify in Database:**
```sql
SELECT id, event_name, store_code, attendees, invitees 
FROM events 
WHERE event_name = 'QR Test Event'
ORDER BY created_at DESC 
LIMIT 1;
```

**Expected:**
- ✅ 1 row returned
- ✅ `id` format: `STORE001_<uuid>`
- ✅ `attendees` = 0
- ✅ `invitees` = 0

**Save the Event ID for next steps!**
```
EVENT_ID = "STORE001_xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
```

---

## 🔧 Step 4: Test QR Code Download

**Request:**
```bash
curl -X GET "http://localhost:3000/events/dowload-qr/{EVENT_ID}"
```

Replace `{EVENT_ID}` with the actual ID from Step 3.

**Expected Response:**
```json
{
  "status": true,
  "qrData": "data:image/png;base64,iVBORw0KG..."
}
```

**Test QR Code URL:**
The QR code should contain: `https://celebrationsite-preprod.tanishq.co.in/events/customer/{EVENT_ID}`

---

## 🔧 Step 5: Test Attendee Form Submission (CRITICAL!)

### Test Case 5.1: Single Attendee Submission

**Request:**
```bash
curl -X POST "http://localhost:3000/events/attendees" \
  -F "eventId={EVENT_ID}" \
  -F "name=John Doe" \
  -F "phone=9876543210" \
  -F "like=Gold Jewelry" \
  -F "firstTimeAtTanishq=true" \
  -F "rsoName=Test RSO"
```

**Expected Response:**
```json
{
  "status": true,
  "message": "Attendee stored successfully",
  "result": 1
}
```

**Expected Logs:**
```
INFO: Received attendee submission - EventId: {EVENT_ID}, Name: John Doe, Phone: 9876543210
INFO: === START storeAttendeesData === EventId: {EVENT_ID}, Name: John Doe, Phone: 9876543210
INFO: Found event: {EVENT_ID} - Current attendee count: 0
INFO: Processing single attendee: John Doe - 9876543210
INFO: Saved attendee with ID: 1
INFO: Updated event {EVENT_ID} attendee count from 0 to 1
INFO: === SUCCESS storeAttendeesData === EventId: {EVENT_ID}, New Count: 1
```

**Verify in Database:**
```sql
-- Check attendee was saved
SELECT * FROM attendees WHERE event_id = '{EVENT_ID}';

-- Expected: 1 row
-- name = 'John Doe'
-- phone = '9876543210'
-- like = 'Gold Jewelry'
-- first_time_at_tanishq = 1 (true)
-- rso_name = 'Test RSO'

-- Check event count was updated
SELECT id, event_name, attendees FROM events WHERE id = '{EVENT_ID}';

-- Expected: attendees = 1
```

✅ **PASS CRITERIA:**
- Attendee saved to database
- Event attendee count incremented from 0 to 1
- No errors in logs
- Response status = true

---

### Test Case 5.2: Multiple Attendees (Same Event)

**Request (Attendee #2):**
```bash
curl -X POST "http://localhost:3000/events/attendees" \
  -F "eventId={EVENT_ID}" \
  -F "name=Jane Smith" \
  -F "phone=9876543211" \
  -F "like=Diamond Jewelry" \
  -F "firstTimeAtTanishq=false" \
  -F "rsoName=Sales Manager"
```

**Expected Response:**
```json
{
  "status": true,
  "message": "Attendee stored successfully",
  "result": 1
}
```

**Request (Attendee #3):**
```bash
curl -X POST "http://localhost:3000/events/attendees" \
  -F "eventId={EVENT_ID}" \
  -F "name=Bob Johnson" \
  -F "phone=9876543212" \
  -F "like=Platinum" \
  -F "firstTimeAtTanishq=true" \
  -F "rsoName=Store Manager"
```

**Verify in Database:**
```sql
-- Check all attendees
SELECT id, name, phone, event_id 
FROM attendees 
WHERE event_id = '{EVENT_ID}'
ORDER BY id;

-- Expected: 3 rows

-- Check event count
SELECT attendees FROM events WHERE id = '{EVENT_ID}';

-- Expected: attendees = 3
```

✅ **PASS CRITERIA:**
- 3 attendees in database
- Event count = 3
- All have correct event_id foreign key

---

### Test Case 5.3: Test with NULL Attendee Count

```sql
-- Manually set attendees to NULL to test null handling
UPDATE events SET attendees = NULL WHERE id = '{EVENT_ID}';

-- Verify
SELECT attendees FROM events WHERE id = '{EVENT_ID}';
-- Should show NULL
```

**Request:**
```bash
curl -X POST "http://localhost:3000/events/attendees" \
  -F "eventId={EVENT_ID}" \
  -F "name=Null Test User" \
  -F "phone=9999999999" \
  -F "like=Test" \
  -F "firstTimeAtTanishq=true" \
  -F "rsoName=Test"
```

**Expected Response:**
```json
{
  "status": true,
  "message": "Attendee stored successfully",
  "result": 1
}
```

**Expected Logs:**
```
INFO: Updated event {EVENT_ID} attendee count from null to 1
```

**Verify in Database:**
```sql
SELECT attendees FROM events WHERE id = '{EVENT_ID}';
-- Expected: attendees = 1 (not NULL, not error!)
```

✅ **PASS CRITERIA:**
- No NullPointerException
- Count set to 1 (not NULL + 1)
- Attendee saved successfully

---

## 🔧 Step 6: Test Error Scenarios

### Test Case 6.1: Invalid Event ID

**Request:**
```bash
curl -X POST "http://localhost:3000/events/attendees" \
  -F "eventId=INVALID_EVENT_123" \
  -F "name=Test User" \
  -F "phone=9999999999" \
  -F "like=Test" \
  -F "firstTimeAtTanishq=true"
```

**Expected Response:**
```json
{
  "status": false,
  "message": "Event not found"
}
```

**Expected Logs:**
```
ERROR: Event not found with ID: INVALID_EVENT_123
```

✅ **PASS CRITERIA:** Proper error handling, no crash

---

### Test Case 6.2: Missing Event ID

**Request:**
```bash
curl -X POST "http://localhost:3000/events/attendees" \
  -F "name=Test User" \
  -F "phone=9999999999"
```

**Expected Response:**
```json
{
  "status": false,
  "message": "Event ID is required"
}
```

**Expected Logs:**
```
ERROR: EventId is null or empty
```

✅ **PASS CRITERIA:** Validation catches missing field

---

## 🔧 Step 7: Test Foreign Key Constraints

### Test Case 7.1: Try to Delete Event with Attendees

```sql
-- This should fail due to FK constraint
DELETE FROM events WHERE id = '{EVENT_ID}';
```

**Expected:** ❌ Error - Cannot delete or update a parent row

**Why?** Foreign key constraint protects data integrity.

### Test Case 7.2: Delete Event (Cascade)

If you want to delete, first delete attendees:

```sql
-- Delete attendees first
DELETE FROM attendees WHERE event_id = '{EVENT_ID}';

-- Now delete event
DELETE FROM events WHERE id = '{EVENT_ID}';
```

**Expected:** ✅ Success

---

## 🔧 Step 8: Performance Test

### Test Case 8.1: Concurrent Attendee Submissions

Test multiple attendees being added simultaneously (simulates real-world QR scan scenario):

```bash
# Run 5 concurrent requests
for i in {1..5}; do
  curl -X POST "http://localhost:3000/events/attendees" \
    -F "eventId={EVENT_ID}" \
    -F "name=Concurrent User $i" \
    -F "phone=900000000$i" \
    -F "like=Test" \
    -F "firstTimeAtTanishq=true" \
    -F "rsoName=RSO $i" &
done
wait
```

**Verify in Database:**
```sql
SELECT COUNT(*) FROM attendees WHERE event_id = '{EVENT_ID}';
-- Expected: 5 (or current count + 5)

SELECT attendees FROM events WHERE id = '{EVENT_ID}';
-- Expected: Count should match actual attendees
```

✅ **PASS CRITERIA:**
- All 5 attendees saved
- No duplicate IDs
- Event count accurate
- No race conditions

---

## 🔧 Step 9: Integration Test (End-to-End)

### Full User Journey Simulation

1. **Create Event** (Manager action)
   ```bash
   # Create event with POST /events/upload
   # Get EVENT_ID from response
   ```

2. **Download QR Code** (Manager action)
   ```bash
   # GET /events/dowload-qr/{EVENT_ID}
   # Save QR image
   ```

3. **Scan QR Code** (Customer action)
   ```bash
   # Open URL: https://celebrationsite-preprod.tanishq.co.in/events/customer/{EVENT_ID}
   # React app should load with form
   ```

4. **Submit Attendee Form** (Customer action)
   ```bash
   # POST /events/attendees with form data
   # Should receive success response
   ```

5. **View Dashboard** (Manager action)
   ```bash
   # POST /events/getevents with store code
   # Should see event with attendees count = 1
   ```

6. **Submit Another Attendee**
   ```bash
   # Repeat step 4 with different data
   # Count should increment to 2
   ```

✅ **PASS CRITERIA:** Complete flow works without errors

---

## 📊 Test Results Summary

| Test Case | Status | Notes |
|-----------|--------|-------|
| Event Creation | ⏳ Pending | |
| QR Code Generation | ⏳ Pending | |
| Single Attendee Submission | ⏳ Pending | CRITICAL |
| Multiple Attendees | ⏳ Pending | |
| Null Handling | ⏳ Pending | CRITICAL |
| Invalid Event ID | ⏳ Pending | |
| Missing Event ID | ⏳ Pending | |
| Foreign Key Constraints | ⏳ Pending | |
| Concurrent Submissions | ⏳ Pending | |
| End-to-End Flow | ⏳ Pending | |

Update each test case as:
- ✅ **PASS** - Works as expected
- ❌ **FAIL** - Has issues
- ⚠️ **WARNING** - Works but with concerns
- ⏳ **PENDING** - Not tested yet

---

## 🐛 Troubleshooting

### Issue: "Event not found" error

**Solution:**
```sql
-- Check if event exists
SELECT * FROM events WHERE id = '{EVENT_ID}';

-- Check store exists
SELECT * FROM stores WHERE store_code = 'STORE001';
```

### Issue: Foreign key constraint violation

**Solution:**
```sql
-- Verify foreign key setup
SHOW CREATE TABLE attendees\G

-- Check event_id column type
DESC attendees;

-- Should match events.id type exactly
DESC events;
```

### Issue: NullPointerException in logs

**Check:** The fix for null handling might not be deployed. Verify:
1. Latest code is compiled
2. Server restarted with new WAR
3. No cached old classes

### Issue: Attendee count not incrementing

**Solution:**
```sql
-- Manually fix count
UPDATE events e
SET e.attendees = (
    SELECT COUNT(*) FROM attendees a WHERE a.event_id = e.id
)
WHERE e.id = '{EVENT_ID}';
```

---

## ✅ Sign-Off Checklist

Before considering this fix complete:

- [ ] All entity files compiled without errors
- [ ] Database schema verified (VARCHAR(255) matching)
- [ ] Foreign keys created successfully
- [ ] Single attendee submission works
- [ ] Multiple attendees work (count increments correctly)
- [ ] Null handling works (no NPE)
- [ ] Error scenarios handled gracefully
- [ ] Logs show detailed information
- [ ] End-to-end flow tested
- [ ] Performance acceptable (concurrent requests)
- [ ] Documentation updated
- [ ] Production deployment plan ready

---

**Test Completed By:** _______________
**Date:** _______________
**Overall Status:** ⏳ PENDING / ✅ PASS / ❌ FAIL

