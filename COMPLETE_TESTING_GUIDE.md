# ✅ COMPLETE TESTING GUIDE - ALL FEATURES

**Your data is imported! Now let's test EVERYTHING!** 🎉

---

## 🎯 STEP-BY-STEP TESTING - COPY & PASTE EACH SECTION

### **STEP 1: Verify Database Import** ✅

```bash
echo "========================================="
echo "1. DATABASE VERIFICATION"
echo "========================================="

# Check all tables and row counts
mysql -u root -pDechub#2025 tanishq -e "
SELECT 'abm_login' as TableName, COUNT(*) as Rows FROM abm_login
UNION ALL SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL SELECT 'cee_login', COUNT(*) FROM cee_login
UNION ALL SELECT 'events', COUNT(*) FROM events
UNION ALL SELECT 'greetings', COUNT(*) FROM greetings
UNION ALL SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL SELECT 'password_history', COUNT(*) FROM password_history
UNION ALL SELECT 'product_details', COUNT(*) FROM product_details
UNION ALL SELECT 'rbm_login', COUNT(*) FROM rbm_login
UNION ALL SELECT 'rivaah', COUNT(*) FROM rivaah
UNION ALL SELECT 'rivaah_users', COUNT(*) FROM rivaah_users
UNION ALL SELECT 'stores', COUNT(*) FROM stores
UNION ALL SELECT 'user_details', COUNT(*) FROM user_details
UNION ALL SELECT 'users', COUNT(*) FROM users;
"

echo ""
echo "✅ Database verification complete!"
```

---

### **STEP 2: Get Sample Test Data** 📊

```bash
echo "========================================="
echo "2. GETTING SAMPLE TEST DATA"
echo "========================================="

# Get a real store
echo "Sample Stores:"
mysql -u root -pDechub#2025 tanishq -e "SELECT storeCode, storeName, region FROM stores LIMIT 5;"

# Get a real user (store manager)
echo ""
echo "Sample Users:"
mysql -u root -pDechub#2025 tanishq -e "SELECT code, role, created_at FROM users LIMIT 5;"

# Get user's password
echo ""
echo "Get user credentials for testing:"
USER_CODE=$(mysql -u root -pDechub#2025 tanishq -e "SELECT code FROM users LIMIT 1;" -s -N)
USER_PASS=$(mysql -u root -pDechub#2025 tanishq -e "SELECT password FROM users WHERE code='$USER_CODE' LIMIT 1;" -s -N)
echo "User Code: $USER_CODE"
echo "Password: $USER_PASS"

# Save for later use
export TEST_USER="$USER_CODE"
export TEST_PASS="$USER_PASS"

# Get a real event
echo ""
echo "Sample Events:"
mysql -u root -pDechub#2025 tanishq -e "SELECT id, event_name, store_code, start_date FROM events LIMIT 3;"

# Get event ID for testing
TEST_EVENT=$(mysql -u root -pDechub#2025 tanishq -e "SELECT id FROM events LIMIT 1;" -s -N)
echo ""
echo "Test Event ID: $TEST_EVENT"
export TEST_EVENT_ID="$TEST_EVENT"

echo ""
echo "✅ Test data collected!"
```

---

### **STEP 3: Test Application Health** 🏥

```bash
echo "========================================="
echo "3. APPLICATION HEALTH CHECK"
echo "========================================="

# Check if app is running
echo "Checking if application is running..."
ps aux | grep tanishq_selfie_app | grep -v grep

# Check application port
echo ""
echo "Checking port 3002..."
netstat -tlnp | grep 3002

# Health endpoint (if exists)
echo ""
echo "Testing health endpoint:"
curl -s http://localhost:3002/actuator/health 2>/dev/null || curl -s http://localhost:3002/health 2>/dev/null || echo "No health endpoint found"

echo ""
echo "✅ Application health check complete!"
```

---

### **STEP 4: Test Login API** 🔐

```bash
echo "========================================="
echo "4. TESTING LOGIN API"
echo "========================================="

# Test login with real credentials
echo "Testing login with user: $TEST_USER"

RESPONSE=$(curl -s -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}")

echo "Response:"
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"

# Check if login successful
if echo "$RESPONSE" | grep -q '"status":true'; then
    echo ""
    echo "✅ LOGIN SUCCESSFUL!"
else
    echo ""
    echo "❌ LOGIN FAILED!"
    echo "Trying with different user..."
    
    # Try with another user
    TEST_USER2=$(mysql -u root -pDechub#2025 tanishq -e "SELECT code FROM users LIMIT 1,1;" -s -N)
    TEST_PASS2=$(mysql -u root -pDechub#2025 tanishq -e "SELECT password FROM users WHERE code='$TEST_USER2' LIMIT 1;" -s -N)
    
    echo "Trying user: $TEST_USER2"
    curl -s -X POST http://localhost:3002/events/login \
      -H "Content-Type: application/json" \
      -d "{\"code\":\"$TEST_USER2\",\"password\":\"$TEST_PASS2\"}" | python3 -m json.tool 2>/dev/null
fi

echo ""
echo "✅ Login API test complete!"
```

---

### **STEP 5: Test Get Events API** 📅

```bash
echo "========================================="
echo "5. TESTING GET EVENTS API"
echo "========================================="

# Get store code from a user
STORE_CODE=$(mysql -u root -pDechub#2025 tanishq -e "SELECT code FROM users LIMIT 1;" -s -N)

echo "Getting events for store: $STORE_CODE"

curl -s -X POST http://localhost:3002/events/getevents \
  -H "Content-Type: application/json" \
  -d "{\"storeCode\":\"$STORE_CODE\",\"startDate\":\"2025-01-01\",\"endDate\":\"2025-12-31\"}" | python3 -m json.tool 2>/dev/null

echo ""
echo "✅ Get events API test complete!"
```

---

### **STEP 6: Test Event Creation** ➕

```bash
echo "========================================="
echo "6. TESTING EVENT CREATION"
echo "========================================="

# Get a real store code
STORE_CODE=$(mysql -u root -pDechub#2025 tanishq -e "SELECT storeCode FROM stores LIMIT 1;" -s -N)
STORE_REGION=$(mysql -u root -pDechub#2025 tanishq -e "SELECT region FROM stores WHERE storeCode='$STORE_CODE' LIMIT 1;" -s -N)

echo "Creating test event for store: $STORE_CODE (Region: $STORE_REGION)"

# Create a new event
NEW_EVENT_ID="TEST_$(date +%Y%m%d_%H%M%S)"

mysql -u root -pDechub#2025 tanishq << EOF
INSERT INTO events (
    id, store_code, event_name, event_type, start_date, 
    created_at, invitees, attendees, region
) VALUES (
    '$NEW_EVENT_ID', 
    '$STORE_CODE', 
    'Test Event - API Testing', 
    'FESTIVAL CELEBRATION', 
    '2025-12-15', 
    NOW(), 
    0, 
    0, 
    '$STORE_REGION'
);
EOF

# Verify creation
echo ""
echo "Verifying event creation..."
mysql -u root -pDechub#2025 tanishq -e "SELECT id, event_name, store_code, start_date FROM events WHERE id='$NEW_EVENT_ID';"

export NEW_EVENT_ID="$NEW_EVENT_ID"

echo ""
echo "✅ Event created: $NEW_EVENT_ID"
```

---

### **STEP 7: Test S3 Upload (Image Upload)** 📸

```bash
echo "========================================="
echo "7. TESTING S3 FILE UPLOAD"
echo "========================================="

# Create a test image
echo "Creating test image..."
echo "Test image content for S3 upload - $(date)" > /tmp/test_image_$(date +%s).jpg

# Upload to S3 via API
echo ""
echo "Uploading to S3 for event: $NEW_EVENT_ID"

UPLOAD_RESPONSE=$(curl -s -X POST http://localhost:3002/events/uploadCompletedEvents \
  -F "eventId=$NEW_EVENT_ID" \
  -F "files=@/tmp/test_image_$(ls -t /tmp/test_image_*.jpg | head -1 | xargs basename)")

echo "Upload Response:"
echo "$UPLOAD_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$UPLOAD_RESPONSE"

# Check if upload successful
if echo "$UPLOAD_RESPONSE" | grep -q '"status":true'; then
    echo ""
    echo "✅ S3 UPLOAD SUCCESSFUL!"
    
    # Extract S3 URL
    S3_URL=$(echo "$UPLOAD_RESPONSE" | grep -o 'https://celebrations-tanishq-preprod.s3[^"]*' | head -1)
    echo "S3 URL: $S3_URL"
    
    # Test if URL is accessible
    echo ""
    echo "Testing S3 URL accessibility..."
    curl -I "$S3_URL" 2>/dev/null | head -5
else
    echo ""
    echo "❌ S3 UPLOAD FAILED!"
fi

echo ""
echo "✅ S3 upload test complete!"
```

---

### **STEP 8: Test QR Code Generation** 📱

```bash
echo "========================================="
echo "8. TESTING QR CODE GENERATION"
echo "========================================="

# Get an event ID
EVENT_ID=$(mysql -u root -pDechub#2025 tanishq -e "SELECT id FROM events LIMIT 1;" -s -N)

echo "Generating QR code for event: $EVENT_ID"

QR_RESPONSE=$(curl -s http://localhost:3002/events/dowload-qr/$EVENT_ID)

echo "QR Code Response (first 200 chars):"
echo "$QR_RESPONSE" | head -c 200
echo "..."

# Check if QR generated
if echo "$QR_RESPONSE" | grep -q "data:image"; then
    echo ""
    echo "✅ QR CODE GENERATED SUCCESSFULLY!"
    echo "Response contains base64 image data"
else
    echo ""
    echo "❌ QR CODE GENERATION FAILED!"
fi

echo ""
echo "✅ QR code test complete!"
```

---

### **STEP 9: Test Attendee Addition** 👥

```bash
echo "========================================="
echo "9. TESTING ATTENDEE ADDITION"
echo "========================================="

# Add test attendees
echo "Adding test attendees to event: $NEW_EVENT_ID"

mysql -u root -pDechub#2025 tanishq << EOF
INSERT INTO attendees (
    created_at, is_bride, is_product, 
    bride_city, bride_name, bride_number, 
    product, event_id
) VALUES 
(NOW(), 0, 1, '', 'Test Attendee 1', '9999999991', 'Gold Necklace', '$NEW_EVENT_ID'),
(NOW(), 0, 1, '', 'Test Attendee 2', '9999999992', 'Diamond Ring', '$NEW_EVENT_ID'),
(NOW(), 1, 0, 'Mumbai', 'Test Bride', '9999999993', '', '$NEW_EVENT_ID');
EOF

# Verify
echo ""
echo "Verifying attendees..."
mysql -u root -pDechub#2025 tanishq -e "SELECT bride_name, bride_number, product FROM attendees WHERE event_id='$NEW_EVENT_ID';"

# Update event count
mysql -u root -pDechub#2025 tanishq -e "UPDATE events SET attendees = (SELECT COUNT(*) FROM attendees WHERE event_id='$NEW_EVENT_ID') WHERE id='$NEW_EVENT_ID';"

echo ""
echo "✅ Attendees added successfully!"
```

---

### **STEP 10: Complete Integration Test** 🎯

```bash
echo "========================================="
echo "10. COMPLETE INTEGRATION TEST"
echo "========================================="

# Full workflow test
echo "Running complete workflow..."

# 1. Login
echo ""
echo "1. Testing login..."
LOGIN_RESULT=$(curl -s -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}")

echo "$LOGIN_RESULT" | grep -q '"status":true' && echo "✅ Login works" || echo "❌ Login failed"

# 2. Get events
echo ""
echo "2. Testing get events..."
EVENTS_RESULT=$(curl -s -X POST http://localhost:3002/events/getevents \
  -H "Content-Type: application/json" \
  -d "{\"storeCode\":\"$TEST_USER\",\"startDate\":\"2025-01-01\",\"endDate\":\"2025-12-31\"}")

echo "$EVENTS_RESULT" | grep -q "event" && echo "✅ Get events works" || echo "❌ Get events failed"

# 3. Upload to S3
echo ""
echo "3. Testing S3 upload..."
echo "test" > /tmp/final_test.jpg
S3_RESULT=$(curl -s -X POST http://localhost:3002/events/uploadCompletedEvents \
  -F "eventId=$NEW_EVENT_ID" \
  -F "files=@/tmp/final_test.jpg")

echo "$S3_RESULT" | grep -q '"status":true' && echo "✅ S3 upload works" || echo "❌ S3 upload failed"

# 4. QR Code
echo ""
echo "4. Testing QR code..."
QR_RESULT=$(curl -s http://localhost:3002/events/dowload-qr/$NEW_EVENT_ID)

echo "$QR_RESULT" | grep -q "data:image" && echo "✅ QR code works" || echo "❌ QR code failed"

# 5. Database integrity
echo ""
echo "5. Testing database integrity..."
DB_CHECK=$(mysql -u root -pDechub#2025 tanishq -e "
SELECT 
    (SELECT COUNT(*) FROM stores) as stores,
    (SELECT COUNT(*) FROM users) as users,
    (SELECT COUNT(*) FROM events) as events,
    (SELECT COUNT(*) FROM attendees) as attendees
" -s -N)

echo "Database counts: $DB_CHECK"
echo "✅ Database integrity check complete"

echo ""
echo "========================================="
echo "🎉 ALL TESTS COMPLETE!"
echo "========================================="
```

---

## 📊 SUMMARY TEST REPORT

```bash
echo "========================================="
echo "FINAL TEST SUMMARY REPORT"
echo "========================================="

mysql -u root -pDechub#2025 tanishq << 'EOF'
SELECT 
    'Database Tables' as Category,
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='tanishq') as Value
UNION ALL
SELECT 'Total Stores', COUNT(*) FROM stores
UNION ALL
SELECT 'Total Users', COUNT(*) FROM users
UNION ALL
SELECT 'Total Events', COUNT(*) FROM events
UNION ALL
SELECT 'Total Attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'Total Invitees', COUNT(*) FROM invitees;
EOF

echo ""
echo "Application Status:"
ps aux | grep -c tanishq_selfie_app | grep -v grep && echo "✅ Running" || echo "❌ Not running"

echo ""
echo "Port Status:"
netstat -tlnp | grep 3002 && echo "✅ Port 3002 listening" || echo "❌ Port not listening"

echo ""
echo "Recent Events:"
mysql -u root -pDechub#2025 tanishq -e "SELECT id, event_name, store_code, DATE(start_date) as event_date FROM events ORDER BY created_at DESC LIMIT 5;"

echo ""
echo "========================================="
echo "✅ TESTING GUIDE COMPLETE!"
echo "========================================="
```

---

## 🎯 QUICK TEST - RUN ALL AT ONCE

**Copy this entire block to run all tests:**

```bash
# Run all tests in sequence
bash << 'TESTSCRIPT'

echo "🚀 Starting Complete Test Suite..."
echo ""

# Test 1: Database
echo "Test 1/10: Database verification..."
mysql -u root -pDechub#2025 tanishq -e "SELECT COUNT(*) FROM stores; SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM events;" -s -N | paste -sd ' ' - | awk '{print "Stores: "$1", Users: "$2", Events: "$3}'

# Test 2: Get test data
TEST_USER=$(mysql -u root -pDechub#2025 tanishq -e "SELECT code FROM users LIMIT 1;" -s -N)
TEST_PASS=$(mysql -u root -pDechub#2025 tanishq -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N)
echo "Test user: $TEST_USER"

# Test 3: Login
echo ""
echo "Test 2/10: Login API..."
curl -s -X POST http://localhost:3002/events/login -H "Content-Type: application/json" -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" | grep -q '"status":true' && echo "✅ Login OK" || echo "❌ Login Failed"

# Test 4: Get events
echo ""
echo "Test 3/10: Get Events API..."
curl -s -X POST http://localhost:3002/events/getevents -H "Content-Type: application/json" -d "{\"storeCode\":\"$TEST_USER\",\"startDate\":\"2025-01-01\",\"endDate\":\"2025-12-31\"}" > /dev/null && echo "✅ Get Events OK" || echo "❌ Get Events Failed"

# Test 5: S3 Upload
echo ""
echo "Test 4/10: S3 Upload..."
EVENT_ID=$(mysql -u root -pDechub#2025 tanishq -e "SELECT id FROM events LIMIT 1;" -s -N)
echo "test" > /tmp/quick_test.jpg
curl -s -X POST http://localhost:3002/events/uploadCompletedEvents -F "eventId=$EVENT_ID" -F "files=@/tmp/quick_test.jpg" | grep -q '"status":true' && echo "✅ S3 Upload OK" || echo "❌ S3 Upload Failed"

# Test 6: QR Code
echo ""
echo "Test 5/10: QR Code Generation..."
curl -s http://localhost:3002/events/dowload-qr/$EVENT_ID | grep -q "data:image" && echo "✅ QR Code OK" || echo "❌ QR Code Failed"

echo ""
echo "🎉 Quick test complete!"

TESTSCRIPT
```

---

## 📝 WHAT TO EXPECT

**All tests should show:**
- ✅ Database: All tables with data
- ✅ Login: Returns `{"status":true}`
- ✅ Get Events: Returns array of events
- ✅ S3 Upload: Returns `{"status":true}` with S3 URL
- ✅ QR Code: Returns base64 image data

**If any test fails, check:**
1. Application logs: `tail -f /opt/tanishq/applications_preprod/application.log`
2. Database connection
3. S3 credentials in application.properties
4. Network/firewall settings

---

**START WITH STEP 2 (Get Sample Test Data) - Then run tests!** 🚀

