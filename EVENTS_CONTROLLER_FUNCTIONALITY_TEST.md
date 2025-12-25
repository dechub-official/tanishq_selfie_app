# 🧪 EVENTS CONTROLLER - COMPLETE FUNCTIONALITY TEST GUIDE
## Tanishq Celebrations - Pre-Production Verification

**Test Date:** December 18, 2025  
**Purpose:** Verify every feature before production deployment  
**Status:** Ready for Testing

---

## 📋 TEST OVERVIEW

This document provides **step-by-step tests** for every feature in the Events Controller. Each test includes:
- ✅ API endpoint details
- ✅ Sample request payloads
- ✅ Expected responses
- ✅ Database verification queries
- ✅ Success criteria

---

## 🎯 CRITICAL FEATURES TO TEST

### ✅ **Feature Checklist**

| # | Feature | API Endpoint | Status | Priority |
|---|---------|-------------|--------|----------|
| 1 | Manager Login | POST /events/login | ⏳ To Test | 🔴 Critical |
| 2 | Create Event (Single Invite) | POST /events/upload | ⏳ To Test | 🔴 Critical |
| 3 | Create Event (Bulk Invitees) | POST /events/upload | ⏳ To Test | 🔴 Critical |
| 4 | Generate QR Code | GET /events/dowload-qr/{id} | ⏳ To Test | 🔴 Critical |
| 5 | QR Code Scan (Show Form) | GET /events/customer/{id} | ⏳ To Test | 🔴 Critical |
| 6 | Store Attendee (Single) | POST /events/attendees | ⏳ To Test | 🔴 Critical |
| 7 | Store Attendees (Bulk) | POST /events/attendees | ⏳ To Test | 🔴 Critical |
| 8 | Get Completed Events | POST /events/getevents | ⏳ To Test | 🔴 Critical |
| 9 | Upload Event Photos (S3) | POST /events/uploadCompletedEvents | ⏳ To Test | 🔴 Critical |
| 10 | Update Sale Amount | POST /events/updateSaleOfAnEvent | ⏳ To Test | 🟡 Medium |
| 11 | Update Advance Amount | POST /events/updateAdvanceOfAnEvent | ⏳ To Test | 🟡 Medium |
| 12 | Update GHS/RGA | POST /events/updateGhsRgaOfAnEvent | ⏳ To Test | 🟡 Medium |
| 13 | Update GMB | POST /events/updateGmbOfAnEvent | ⏳ To Test | 🟡 Medium |
| 14 | Get Invited Members | POST /events/getinvitedmember | ⏳ To Test | 🟡 Medium |
| 15 | Get Stores by Region | GET /events/getStoresByRegion/{region} | ⏳ To Test | 🟢 Low |

---

## 🔐 TEST 1: MANAGER LOGIN

### Purpose
Verify store managers can log in and access their dashboard.

### API Endpoint
```
POST http://localhost:3000/events/login
Content-Type: application/json
```

### Test Case 1.1: Valid Store Login ✅
```json
{
  "code": "STORE001",
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "status": true,
  "message": null,
  "storeData": {
    "BtqCode": "STORE001",
    "BtqName": "Tanishq Bangalore Store",
    "BtqEmailid": "store001@tanishq.co.in",
    "storeAddress": "MG Road, Bangalore",
    "storeCity": "Bangalore",
    "storeState": "Karnataka"
  }
}
```

**Success Criteria:**
- ✅ status = true
- ✅ storeData contains BtqCode, BtqName, BtqEmailid
- ✅ Response time < 500ms

**Database Verification:**
```sql
-- Check if user exists
SELECT * FROM users WHERE username = 'STORE001';

-- Check if store exists
SELECT * FROM stores WHERE store_code = 'STORE001';
```

### Test Case 1.2: Invalid Credentials ❌
```json
{
  "code": "STORE001",
  "password": "wrongpassword"
}
```

**Expected Response:**
```json
{
  "status": false,
  "message": "Invalid credentials.",
  "storeData": null
}
```

### Test Case 1.3: Regional Manager Login (RBM) ✅
```json
{
  "code": "SOUTH1",
  "password": "rbm_password"
}
```

**Expected Response:**
```json
{
  "status": true,
  "message": null,
  "storeData": {
    "manager": "SOUTH1"
  }
}
```

### Test Case 1.4: User Not Found ❌
```json
{
  "code": "NONEXISTENT",
  "password": "anypassword"
}
```

**Expected Response:**
```json
{
  "status": false,
  "message": "User not found.",
  "storeData": null
}
```

---

## 🎉 TEST 2: CREATE EVENT (SINGLE INVITE)

### Purpose
Create an event with a single customer invitation (no Excel file).

### API Endpoint
```
POST http://localhost:3000/events/upload
Content-Type: multipart/form-data
```

### Test Case 2.1: Create Wedding Event ✅
```
Form Data:
- code: STORE001
- eventName: Kumar Wedding Reception
- eventType: Wedding
- eventSubType: Reception
- RSO: Rajesh Kumar
- date: 2025-12-25
- time: 18:00
- location: Grand Hotel, Bangalore
- Community: Hindu
- region: South
- customerName: Priya Kumar
- customerContact: 9876543210
- singalInvite: true
- sale: 150000
- advance: 50000
- ghsOrRga: 2
- gmb: 1
- diamondAwareness: true
- ghsFlag: false
```

**Expected Response:**
```json
{
  "status": true,
  "qrData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
}
```

**Success Criteria:**
- ✅ status = true
- ✅ qrData contains base64 encoded QR code
- ✅ Event created in database
- ✅ QR code is valid and scannable

**Database Verification:**
```sql
-- Check if event was created
SELECT * FROM events 
WHERE event_name = 'Kumar Wedding Reception' 
ORDER BY created_at DESC 
LIMIT 1;

-- Expected Result:
-- - id: STORE001_<uuid>
-- - event_type: Wedding
-- - event_sub_type: Reception
-- - rso: Rajesh Kumar
-- - start_date: 2025-12-25
-- - invitees: 0 (no Excel file)
-- - attendees: 0
-- - sale: 150000
-- - advance: 50000
-- - ghs_or_rga: 2
-- - gmb: 1
-- - diamond_awareness: 1
-- - ghs_flag: 0
-- - store_code: STORE001
```

**Visual Test:**
- ✅ Copy qrData (remove "data:image/png;base64," prefix)
- ✅ Decode base64 and save as image
- ✅ Scan QR code with phone
- ✅ Should redirect to: https://celebrationsite-preprod.tanishq.co.in/events/customer/STORE001_<uuid>

---

## 📤 TEST 3: CREATE EVENT (BULK INVITEES FROM EXCEL)

### Purpose
Create an event and upload multiple invitees from Excel file.

### API Endpoint
```
POST http://localhost:3000/events/upload
Content-Type: multipart/form-data
```

### Test Case 3.1: Upload Event with 50 Invitees ✅

**Prepare Excel File (invitees.xlsx):**
| Name | Contact |
|------|---------|
| Amit Sharma | 9876543211 |
| Priya Singh | 9876543212 |
| Raj Patel | 9876543213 |
| ... | ... |
| (50 rows total) | |

```
Form Data:
- code: STORE001
- file: invitees.xlsx
- eventName: Diwali Festival Celebration
- eventType: Festival
- eventSubType: Diwali
- RSO: Suresh Reddy
- date: 2025-10-24
- time: 10:00
- location: Store Premises
- Community: All
- region: South
- singalInvite: false
- sale: 0
- advance: 0
- ghsOrRga: 0
- gmb: 0
- diamondAwareness: false
- ghsFlag: false
```

**Expected Response:**
```json
{
  "status": true,
  "qrData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
}
```

**Success Criteria:**
- ✅ status = true
- ✅ Event created
- ✅ 50 invitees inserted into database
- ✅ QR code generated

**Database Verification:**
```sql
-- Get the latest event
SELECT * FROM events 
WHERE event_name = 'Diwali Festival Celebration' 
ORDER BY created_at DESC 
LIMIT 1;

-- Should show: invitees = 50

-- Check invitees table
SELECT COUNT(*) FROM invitees 
WHERE event_id = '<event_id_from_above>';

-- Should return: 50

-- View some invitees
SELECT * FROM invitees 
WHERE event_id = '<event_id>' 
LIMIT 10;

-- Expected: Name and Contact columns populated
```

---

## 📱 TEST 4: GENERATE QR CODE

### Purpose
Generate QR code for an existing event.

### API Endpoint
```
GET http://localhost:3000/events/dowload-qr/{eventId}
```

### Test Case 4.1: Generate QR for Existing Event ✅
```
GET http://localhost:3000/events/dowload-qr/STORE001_abc-123-def-456
```

**Expected Response:**
```json
{
  "status": true,
  "qrData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
}
```

**Success Criteria:**
- ✅ status = true
- ✅ qrData is valid base64 PNG
- ✅ QR code encodes: https://celebrationsite-preprod.tanishq.co.in/events/customer/STORE001_abc-123-def-456

### Test Case 4.2: Invalid Event ID ❌
```
GET http://localhost:3000/events/dowload-qr/INVALID_ID
```

**Expected Response:**
```json
{
  "status": false,
  "qrData": "Error generating QR code: Event not found"
}
```

---

## 👥 TEST 5: QR CODE SCAN (SHOW ATTENDEE FORM)

### Purpose
When customer scans QR code, they should see the registration form.

### API Endpoint
```
GET http://localhost:3000/events/customer/{eventId}
```

### Test Case 5.1: Valid QR Scan ✅
```
GET http://localhost:3000/events/customer/STORE001_abc-123-def-456
```

**Expected Response:**
- HTTP 200
- Content-Type: text/html
- Body: events.html (React app)

**Success Criteria:**
- ✅ Returns HTML page
- ✅ React app loads
- ✅ Shows event registration form
- ✅ Form has fields: Name, Phone, Like (what they liked), First Time at Tanishq

**Browser Test:**
1. Open URL in browser
2. Form should be visible
3. Fill out form and submit (next test)

---

## ✍️ TEST 6: STORE ATTENDEE (SINGLE)

### Purpose
Customer registers for event by submitting the form (single attendee).

### API Endpoint
```
POST http://localhost:3000/events/attendees
Content-Type: multipart/form-data
```

### Test Case 6.1: Register Single Attendee ✅
```
Form Data:
- eventId: STORE001_abc-123-def-456
- name: Ramesh Kumar
- phone: 9876543210
- like: Diamond Necklace
- firstTimeAtTanishq: true
- rsoName: Suresh (RSO who assisted)
```

**Expected Response:**
```json
{
  "status": true,
  "message": "Attendee stored successfully",
  "result": 1
}
```

**Success Criteria:**
- ✅ status = true
- ✅ message confirms success
- ✅ result = 1 (number of attendees inserted)

**Database Verification:**
```sql
-- Check attendee was saved
SELECT * FROM attendees 
WHERE event_id = 'STORE001_abc-123-def-456' 
AND name = 'Ramesh Kumar';

-- Expected Result:
-- - name: Ramesh Kumar
-- - phone: 9876543210
-- - like: Diamond Necklace
-- - first_time_at_tanishq: 1 (true)
-- - is_uploaded_from_excel: 0 (false)
-- - rso_name: Suresh
-- - created_at: <current timestamp>

-- Check event attendee count was updated
SELECT attendees FROM events 
WHERE id = 'STORE001_abc-123-def-456';

-- Should be incremented by 1
```

### Test Case 6.2: Register Multiple Attendees (Real-time) ✅
Register 5 attendees one by one and verify count updates:

**Attendee 1:**
```
name: John Doe, phone: 9876543211
```

**Attendee 2:**
```
name: Jane Smith, phone: 9876543212
```

... (repeat 3 more times)

**Database Verification:**
```sql
-- Check total attendees
SELECT COUNT(*) FROM attendees 
WHERE event_id = 'STORE001_abc-123-def-456';

-- Should return: 5

-- Check event attendee count
SELECT attendees FROM events 
WHERE id = 'STORE001_abc-123-def-456';

-- Should be: 5 (real-time update)
```

---

## 📋 TEST 7: STORE ATTENDEES (BULK UPLOAD FROM EXCEL)

### Purpose
Store manager uploads Excel file with attendees who already attended.

### API Endpoint
```
POST http://localhost:3000/events/attendees
Content-Type: multipart/form-data
```

### Test Case 7.1: Bulk Upload 100 Attendees ✅

**Prepare Excel File (attendees.xlsx):**
| Name | Phone | Like | FirstTime | RSO |
|------|-------|------|-----------|-----|
| Customer 1 | 9876543301 | Gold Ring | TRUE | RSO1 |
| Customer 2 | 9876543302 | Diamond Earrings | FALSE | RSO2 |
| Customer 3 | 9876543303 | Necklace Set | TRUE | RSO1 |
| ... | ... | ... | ... | ... |
| (100 rows) | | | | |

```
Form Data:
- eventId: STORE001_abc-123-def-456
- file: attendees.xlsx
```

**Expected Response:**
```json
{
  "status": true,
  "message": "100 attendees uploaded successfully",
  "result": 100
}
```

**Success Criteria:**
- ✅ status = true
- ✅ message shows count
- ✅ result = 100

**Database Verification:**
```sql
-- Check bulk attendees were saved
SELECT COUNT(*) FROM attendees 
WHERE event_id = 'STORE001_abc-123-def-456' 
AND is_uploaded_from_excel = 1;

-- Should return: 100

-- Check event attendee count
SELECT attendees FROM events 
WHERE id = 'STORE001_abc-123-def-456';

-- Should be: 100 (or 105 if you did Test 6.2 first)

-- Verify attendees_uploaded flag
SELECT attendees_uploaded FROM events 
WHERE id = 'STORE001_abc-123-def-456';

-- Should be: 1 (true)

-- View sample attendees
SELECT name, phone, like, rso_name, is_uploaded_from_excel 
FROM attendees 
WHERE event_id = 'STORE001_abc-123-def-456' 
LIMIT 10;

-- Verify data matches Excel
```

---

## 📊 TEST 8: GET COMPLETED EVENTS

### Purpose
Store manager views list of all events for their store.

### API Endpoint
```
POST http://localhost:3000/events/getevents
Content-Type: application/json
```

### Test Case 8.1: Get All Events for Store ✅
```json
{
  "storeCode": "STORE001",
  "startDate": "",
  "endDate": ""
}
```

**Expected Response:**
```json
{
  "status": true,
  "message": "Success",
  "list": [
    {
      "StoreCode": "STORE001",
      "Id": "STORE001_abc-123-def-456",
      "EventType": "Wedding",
      "EventSubType": "Reception",
      "EventName": "Kumar Wedding Reception",
      "RSO": "Rajesh Kumar",
      "StartDate": "2025-12-25",
      "Invitees": 50,
      "Attendees": 105,
      "CompletedEventsDriveLink": null,
      "Sale": 150000.0,
      "Advance": 50000.0,
      "GhsOrRga": 2.0,
      "Gmb": 1.0,
      "createdAt": "2025-12-18T10:30:00"
    },
    {
      "StoreCode": "STORE001",
      "Id": "STORE001_xyz-789-ghi-012",
      "EventType": "Festival",
      "EventSubType": "Diwali",
      "EventName": "Diwali Festival Celebration",
      "RSO": "Suresh Reddy",
      "StartDate": "2025-10-24",
      "Invitees": 50,
      "Attendees": 0,
      "CompletedEventsDriveLink": null,
      "Sale": 0.0,
      "Advance": 0.0,
      "GhsOrRga": 0.0,
      "Gmb": 0.0,
      "createdAt": "2025-12-18T11:00:00"
    }
  ]
}
```

**Success Criteria:**
- ✅ status = true
- ✅ list contains all events for STORE001
- ✅ Each event shows Invitees and Attendees counts (real-time)
- ✅ Sale, Advance, GhsOrRga, Gmb values present

### Test Case 8.2: Filter Events by Date Range ✅
```json
{
  "storeCode": "STORE001",
  "startDate": "2025-10-01",
  "endDate": "2025-10-31"
}
```

**Expected Response:**
```json
{
  "status": true,
  "message": "Success",
  "list": [
    {
      "EventName": "Diwali Festival Celebration",
      "StartDate": "2025-10-24",
      ...
    }
  ]
}
```

**Success Criteria:**
- ✅ Only events within date range returned
- ✅ Kumar Wedding (Dec 25) NOT in list
- ✅ Diwali Event (Oct 24) IN list

### Test Case 8.3: Regional Manager Gets All Stores' Events ✅
```json
{
  "storeCode": "SOUTH1",
  "startDate": "",
  "endDate": ""
}
```

**Expected Response:**
```json
{
  "status": true,
  "message": "Success",
  "list": [
    {
      "StoreCode": "STORE001",
      "EventName": "Kumar Wedding Reception",
      ...
    },
    {
      "StoreCode": "STORE002",
      "EventName": "Fashion Show Bangalore",
      ...
    },
    {
      "StoreCode": "STORE003",
      "EventName": "Gold Festival",
      ...
    }
    // All stores in SOUTH1 region
  ]
}
```

**Success Criteria:**
- ✅ Events from multiple stores in region
- ✅ All stores under SOUTH1 RBM included

**Database Verification:**
```sql
-- Check which stores are under SOUTH1
SELECT store_code, store_name FROM stores 
WHERE rbm_username = 'SOUTH1';

-- Verify events returned match these stores
SELECT e.id, e.event_name, s.store_code, s.rbm_username
FROM events e
JOIN stores s ON e.store_code = s.store_code
WHERE s.rbm_username = 'SOUTH1';
```

---

## 📸 TEST 9: UPLOAD EVENT PHOTOS (AWS S3)

### Purpose
Upload completed event photos to AWS S3 storage.

### API Endpoint
```
POST http://localhost:3000/events/uploadCompletedEvents
Content-Type: multipart/form-data
```

### Test Case 9.1: Upload 5 Event Photos ✅

**Prepare Files:**
- event_photo1.jpg (500 KB)
- event_photo2.jpg (750 KB)
- event_photo3.jpg (1.2 MB)
- event_photo4.jpg (900 KB)
- event_photo5.jpg (600 KB)

```
Form Data:
- eventId: STORE001_abc-123-def-456
- files: [event_photo1.jpg, event_photo2.jpg, event_photo3.jpg, event_photo4.jpg, event_photo5.jpg]
```

**Expected Response:**
```json
{
  "status": true,
  "message": "All 5 files uploaded successfully to S3",
  "result": [
    "https://celebrations-tanishq-preprod.s3.ap-south-1.amazonaws.com/events/STORE001_abc-123-def-456/event_20251218_103000_1734507000123.jpg",
    "https://celebrations-tanishq-preprod.s3.ap-south-1.amazonaws.com/events/STORE001_abc-123-def-456/event_20251218_103001_1734507001456.jpg",
    "https://celebrations-tanishq-preprod.s3.ap-south-1.amazonaws.com/events/STORE001_abc-123-def-456/event_20251218_103002_1734507002789.jpg",
    "https://celebrations-tanishq-preprod.s3.ap-south-1.amazonaws.com/events/STORE001_abc-123-def-456/event_20251218_103003_1734507003012.jpg",
    "https://celebrations-tanishq-preprod.s3.ap-south-1.amazonaws.com/events/STORE001_abc-123-def-456/event_20251218_103004_1734507004345.jpg"
  ]
}
```

**Success Criteria:**
- ✅ status = true
- ✅ message confirms 5 files uploaded
- ✅ result array contains 5 S3 URLs
- ✅ Each URL is accessible (returns image when opened)

**S3 Verification:**
```bash
# On server (or with AWS CLI)
aws s3 ls s3://celebrations-tanishq-preprod/events/STORE001_abc-123-def-456/ --region ap-south-1

# Expected Output:
# 2025-12-18 10:30:00   512000 event_20251218_103000_1734507000123.jpg
# 2025-12-18 10:30:01   768000 event_20251218_103001_1734507001456.jpg
# 2025-12-18 10:30:02  1228800 event_20251218_103002_1734507002789.jpg
# 2025-12-18 10:30:03   921600 event_20251218_103003_1734507003012.jpg
# 2025-12-18 10:30:04   614400 event_20251218_103004_1734507004345.jpg
```

**Database Verification:**
```sql
-- Check event completed link was updated
SELECT completed_events_drive_link FROM events 
WHERE id = 'STORE001_abc-123-def-456';

-- Expected Result:
-- completed_events_drive_link: https://s3.console.aws.amazon.com/s3/buckets/celebrations-tanishq-preprod?region=ap-south-1&prefix=events/STORE001_abc-123-def-456/
```

**Browser Test:**
1. Copy first S3 URL from response
2. Open in browser
3. Image should display (if bucket policy allows)

### Test Case 9.2: File Type Validation (Security) ❌
```
Form Data:
- eventId: STORE001_abc-123-def-456
- files: [malicious.php, script.jsp, hack.html]
```

**Expected Response:**
```json
{
  "status": false,
  "message": "No valid files to upload",
  "result": null
}
```

**Success Criteria:**
- ✅ Malicious files rejected
- ✅ No files uploaded to S3
- ✅ Application not compromised

---

## 💰 TEST 10: UPDATE SALE AMOUNT

### Purpose
Update sale amount for an event after it's completed.

### API Endpoint
```
POST http://localhost:3000/events/updateSaleOfAnEvent
Content-Type: application/x-www-form-urlencoded
```

### Test Case 10.1: Update Sale to 250000 ✅
```
Form Data:
- eventCode: STORE001_abc-123-def-456
- sale: 250000
```

**Expected Response:**
```json
{
  "status": true,
  "message": "Sale updated successfully",
  "result": null
}
```

**Success Criteria:**
- ✅ status = true
- ✅ message confirms update

**Database Verification:**
```sql
-- Check sale was updated
SELECT sale FROM events 
WHERE id = 'STORE001_abc-123-def-456';

-- Expected Result: 250000.0

-- Verify old value was 150000
-- New value should be 250000
```

---

## 💵 TEST 11: UPDATE ADVANCE AMOUNT

### Purpose
Update advance payment amount for an event.

### API Endpoint
```
POST http://localhost:3000/events/updateAdvanceOfAnEvent
Content-Type: application/x-www-form-urlencoded
```

### Test Case 11.1: Update Advance to 75000 ✅
```
Form Data:
- eventCode: STORE001_abc-123-def-456
- advance: 75000
```

**Expected Response:**
```json
{
  "status": true,
  "message": "Advance updated successfully",
  "result": null
}
```

**Database Verification:**
```sql
SELECT advance FROM events 
WHERE id = 'STORE001_abc-123-def-456';

-- Expected Result: 75000.0
```

---

## 🏆 TEST 12: UPDATE GHS/RGA

### Purpose
Update GHS (Golden Harvest Scheme) or RGA count.

### API Endpoint
```
POST http://localhost:3000/events/updateGhsRgaOfAnEvent
Content-Type: application/x-www-form-urlencoded
```

### Test Case 12.1: Update GHS/RGA to 5 ✅
```
Form Data:
- eventCode: STORE001_abc-123-def-456
- ghsRga: 5
```

**Expected Response:**
```json
{
  "status": true,
  "message": "GHS/RGA updated successfully",
  "result": null
}
```

**Database Verification:**
```sql
SELECT ghs_or_rga FROM events 
WHERE id = 'STORE001_abc-123-def-456';

-- Expected Result: 5.0
```

---

## 📍 TEST 13: UPDATE GMB (Google My Business)

### Purpose
Update GMB review count.

### API Endpoint
```
POST http://localhost:3000/events/updateGmbOfAnEvent
Content-Type: application/x-www-form-urlencoded
```

### Test Case 13.1: Update GMB to 3 ✅
```
Form Data:
- eventCode: STORE001_abc-123-def-456
- gmb: 3
```

**Expected Response:**
```json
{
  "status": true,
  "message": "GMB updated successfully",
  "result": null
}
```

**Database Verification:**
```sql
SELECT gmb FROM events 
WHERE id = 'STORE001_abc-123-def-456';

-- Expected Result: 3.0
```

---

## 📋 TEST 14: GET INVITED MEMBERS

### Purpose
View list of invitees for an event.

### API Endpoint
```
POST http://localhost:3000/events/getinvitedmember
Content-Type: application/x-www-form-urlencoded
```

### Test Case 14.1: Get Invitees List ✅
```
Form Data:
- eventCode: STORE001_abc-123-def-456
```

**Expected Response:**
```json
{
  "status": true,
  "message": null,
  "result": [
    {
      "id": 1,
      "name": "Amit Sharma",
      "contact": "9876543211",
      "createdAt": "2025-12-18T10:30:00"
    },
    {
      "id": 2,
      "name": "Priya Singh",
      "contact": "9876543212",
      "createdAt": "2025-12-18T10:30:00"
    },
    ... (50 total if you uploaded 50)
  ]
}
```

**Success Criteria:**
- ✅ status = true
- ✅ result is array of invitees
- ✅ Count matches invitees uploaded in Test 3

**Database Verification:**
```sql
SELECT id, name, contact, created_at 
FROM invitees 
WHERE event_id = 'STORE001_abc-123-def-456' 
ORDER BY id;

-- Should match result array
```

---

## 🌍 TEST 15: GET STORES BY REGION

### Purpose
Get list of stores in a specific region.

### API Endpoint
```
GET http://localhost:3000/events/getStoresByRegion/{region}
```

### Test Case 15.1: Get South Region Stores ✅
```
GET http://localhost:3000/events/getStoresByRegion/South
```

**Expected Response:**
```json
[
  {
    "storeCode": "STORE001",
    "storeName": "Tanishq Bangalore MG Road"
  },
  {
    "storeCode": "STORE002",
    "storeName": "Tanishq Chennai T Nagar"
  },
  {
    "storeCode": "STORE003",
    "storeName": "Tanishq Hyderabad Banjara Hills"
  }
  // All stores in South region
]
```

**Success Criteria:**
- ✅ Array of stores
- ✅ Only stores from South region
- ✅ Each store has storeCode and storeName

**Database Verification:**
```sql
SELECT store_code, store_name, region 
FROM stores 
WHERE region = 'South';

-- Should match returned list
```

---

## 🔄 REAL-TIME DATA FLOW TEST

### Purpose
Verify data updates in real-time across the workflow.

### Complete Workflow Test ✅

**Step 1: Create Event**
```
POST /events/upload
- Create "Test Wedding Event"
- Upload 10 invitees via Excel
```

**Verify:**
```sql
SELECT invitees, attendees FROM events WHERE event_name = 'Test Wedding Event';
-- Expected: invitees = 10, attendees = 0
```

---

**Step 2: Add Attendee #1**
```
POST /events/attendees
- name: Customer 1, phone: 9999999991
```

**Verify:**
```sql
SELECT attendees FROM events WHERE event_name = 'Test Wedding Event';
-- Expected: attendees = 1
```

---

**Step 3: Add Attendee #2**
```
POST /events/attendees
- name: Customer 2, phone: 9999999992
```

**Verify:**
```sql
SELECT attendees FROM events WHERE event_name = 'Test Wedding Event';
-- Expected: attendees = 2
```

---

**Step 4: Bulk Upload 20 Attendees**
```
POST /events/attendees
- file: 20_attendees.xlsx
```

**Verify:**
```sql
SELECT attendees FROM events WHERE event_name = 'Test Wedding Event';
-- Expected: attendees = 22 (2 + 20)
```

---

**Step 5: Get Events (Check Real-time Count)**
```
POST /events/getevents
- storeCode: <your_store>
```

**Verify Response:**
```json
{
  "EventName": "Test Wedding Event",
  "Invitees": 10,
  "Attendees": 22  ← Should match database
}
```

---

**Step 6: Update Sale**
```
POST /events/updateSaleOfAnEvent
- eventCode: <event_id>
- sale: 500000
```

**Verify:**
```sql
SELECT sale FROM events WHERE event_name = 'Test Wedding Event';
-- Expected: 500000.0
```

---

**Step 7: Upload Photos**
```
POST /events/uploadCompletedEvents
- eventId: <event_id>
- files: [photo1.jpg, photo2.jpg, photo3.jpg]
```

**Verify:**
```sql
SELECT completed_events_drive_link FROM events 
WHERE event_name = 'Test Wedding Event';
-- Expected: S3 folder URL
```

---

**Step 8: Get Events Again (Verify All Updates)**
```
POST /events/getevents
- storeCode: <your_store>
```

**Verify Response Contains:**
```json
{
  "EventName": "Test Wedding Event",
  "Invitees": 10,
  "Attendees": 22,
  "Sale": 500000.0,
  "CompletedEventsDriveLink": "https://s3.../<event_id>/"
}
```

**✅ ALL DATA UPDATED IN REAL-TIME!**

---

## 📊 SUMMARY CHECKLIST

Before production, ensure ALL these pass:

### ✅ Core Features
- [ ] Login works (store, RBM, ABM, CEE)
- [ ] Create event (single invite)
- [ ] Create event (bulk invitees from Excel)
- [ ] QR code generation works
- [ ] QR code scan shows form
- [ ] Single attendee registration
- [ ] Bulk attendee upload (Excel)
- [ ] Get events list
- [ ] Date filtering works
- [ ] Regional manager sees all stores' events

### ✅ Data Integrity
- [ ] Invitee count matches Excel rows
- [ ] Attendee count updates in real-time
- [ ] Sale/Advance/GHS/GMB update correctly
- [ ] Events table has all required fields populated
- [ ] Relationships (Event ↔ Store, Event ↔ Attendees) work

### ✅ File Uploads
- [ ] Excel invitees upload works
- [ ] Excel attendees upload works
- [ ] Event photos upload to S3
- [ ] S3 URLs are accessible
- [ ] File type validation prevents malicious files
- [ ] Large files (up to 10MB) work

### ✅ Security
- [ ] Invalid credentials rejected
- [ ] Non-existent users get error
- [ ] Malicious file types blocked
- [ ] SQL injection not possible (parameterized queries)

### ✅ Performance
- [ ] Login response < 500ms
- [ ] Event creation < 2 seconds
- [ ] Bulk upload 100 rows < 5 seconds
- [ ] Photo upload (5 files) < 10 seconds
- [ ] Get events list < 1 second

---

## 🚀 PRODUCTION READINESS

### ✅ READY IF:
- All 15 test cases pass
- Real-time data flow test passes
- Database queries return correct counts
- S3 uploads work
- QR codes are scannable
- No errors in application logs

### ⚠️ NOT READY IF:
- Any test case fails
- Data counts don't match
- Files not uploading to S3
- QR codes don't work
- Errors in logs

---

## 📝 TEST EXECUTION LOG

Use this template to track your testing:

| Test # | Feature | Status | Date Tested | Tester | Notes |
|--------|---------|--------|-------------|--------|-------|
| 1 | Manager Login | ⏳ | | | |
| 2 | Create Event (Single) | ⏳ | | | |
| 3 | Create Event (Bulk) | ⏳ | | | |
| 4 | Generate QR | ⏳ | | | |
| 5 | QR Scan | ⏳ | | | |
| 6 | Store Attendee (Single) | ⏳ | | | |
| 7 | Store Attendees (Bulk) | ⏳ | | | |
| 8 | Get Events | ⏳ | | | |
| 9 | Upload Photos (S3) | ⏳ | | | |
| 10 | Update Sale | ⏳ | | | |
| 11 | Update Advance | ⏳ | | | |
| 12 | Update GHS/RGA | ⏳ | | | |
| 13 | Update GMB | ⏳ | | | |
| 14 | Get Invitees | ⏳ | | | |
| 15 | Get Stores by Region | ⏳ | | | |

**Legend:**
- ⏳ Not Tested
- ✅ Passed
- ❌ Failed
- 🔄 Re-testing

---

## 🛠️ TOOLS FOR TESTING

### Postman Collection
Use the provided Postman collection to run all tests automatically.

### cURL Commands
All test cases include cURL equivalents for command-line testing.

### Database Scripts
SQL queries provided for verification after each test.

### Sample Files
- invitees_sample.xlsx
- attendees_sample.xlsx
- event_photos (5 sample images)

---

**Status:** 📋 Ready for Testing  
**Last Updated:** December 18, 2025  
**Next Step:** Execute tests and mark results in log table

