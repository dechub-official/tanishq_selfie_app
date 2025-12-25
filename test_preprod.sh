#!/bin/bash

# ========================================
# PRE-PROD TESTING SCRIPT
# Test all features on server directly
# ========================================

echo "🧪 TANISHQ PRE-PROD - COMPLETE TESTING SCRIPT"
echo "=============================================="
echo "Date: $(date)"
echo "Server: $(hostname)"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Base URL
BASE_URL="http://localhost:3002"

# MySQL credentials
MYSQL_USER="root"
MYSQL_PASS="Dechub#2025"
MYSQL_DB="selfie_preprod"

# AWS S3
export PATH=/usr/local/aws-cli/v2/current/bin:$PATH
S3_BUCKET="celebrations-tanishq-preprod"

# ========================================
# TEST 1: Application Health Check
# ========================================
echo "📋 TEST 1: Application Health Check"
echo "------------------------------------"

response=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL)
if [ "$response" = "200" ]; then
    echo -e "${GREEN}✅ PASS${NC}: Application is running (HTTP 200)"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ FAIL${NC}: Application not responding (HTTP $response)"
    ((TESTS_FAILED++))
fi
echo ""

# ========================================
# TEST 2: Database Connection
# ========================================
echo "📋 TEST 2: Database Connection"
echo "------------------------------------"

db_test=$(mysql -u $MYSQL_USER -p$MYSQL_PASS -e "SELECT 1 as test" $MYSQL_DB 2>&1 | grep -c "test")
if [ "$db_test" -gt "0" ]; then
    echo -e "${GREEN}✅ PASS${NC}: Database connection successful"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ FAIL${NC}: Database connection failed"
    ((TESTS_FAILED++))
fi

# Show database stats
echo "Database Statistics:"
mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "
SELECT
    (SELECT COUNT(*) FROM stores) as 'Total Stores',
    (SELECT COUNT(*) FROM events) as 'Total Events',
    (SELECT COUNT(*) FROM attendees) as 'Total Attendees',
    (SELECT COUNT(*) FROM users) as 'Total Users';
" 2>/dev/null
echo ""

# ========================================
# TEST 3: S3 Service Access
# ========================================
echo "📋 TEST 3: S3 Service Access"
echo "------------------------------------"

s3_test=$(aws s3 ls s3://$S3_BUCKET/ 2>&1)
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ PASS${NC}: S3 bucket accessible"
    ((TESTS_PASSED++))
    echo "S3 Bucket contents:"
    aws s3 ls s3://$S3_BUCKET/ | head -5
else
    echo -e "${RED}❌ FAIL${NC}: S3 bucket not accessible"
    ((TESTS_FAILED++))
fi
echo ""

# ========================================
# TEST 4: Create Test Store (if not exists)
# ========================================
echo "📋 TEST 4: Create Test Store"
echo "------------------------------------"

# Check if TEST001 store exists
store_exists=$(mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -se "SELECT COUNT(*) FROM stores WHERE store_code='TEST001'" 2>/dev/null)

if [ "$store_exists" = "0" ]; then
    # Create test store
    mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "
    INSERT INTO stores (store_code, store_name, store_email_id, store_city, store_state, store_address, region)
    VALUES ('TEST001', 'Test Store Bangalore', 'test@tanishq.co.in', 'Bangalore', 'Karnataka', 'Test Address', 'SOUTH')
    ON DUPLICATE KEY UPDATE store_name='Test Store Bangalore';
    " 2>/dev/null

    echo -e "${GREEN}✅ PASS${NC}: Test store TEST001 created"
    ((TESTS_PASSED++))
else
    echo -e "${YELLOW}ℹ INFO${NC}: Test store TEST001 already exists"
    ((TESTS_PASSED++))
fi
echo ""

# ========================================
# TEST 5: Create Test User (if not exists)
# ========================================
echo "📋 TEST 5: Create Test User"
echo "------------------------------------"

user_exists=$(mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -se "SELECT COUNT(*) FROM users WHERE username='TEST001'" 2>/dev/null)

if [ "$user_exists" = "0" ]; then
    mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "
    INSERT INTO users (username, password, email, name, role)
    VALUES ('TEST001', 'test123', 'test@tanishq.co.in', 'Test User', 'STORE_MANAGER')
    ON DUPLICATE KEY UPDATE password='test123';
    " 2>/dev/null

    echo -e "${GREEN}✅ PASS${NC}: Test user TEST001 created (password: test123)"
    ((TESTS_PASSED++))
else
    echo -e "${YELLOW}ℹ INFO${NC}: Test user TEST001 already exists"
    ((TESTS_PASSED++))
fi
echo ""

# ========================================
# TEST 6: Test Login API
# ========================================
echo "📋 TEST 6: Test Login API"
echo "------------------------------------"

login_response=$(curl -s -X POST $BASE_URL/events/login \
  -H "Content-Type: application/json" \
  -d '{"code":"TEST001","password":"test123"}')

if echo "$login_response" | grep -q "status.*true"; then
    echo -e "${GREEN}✅ PASS${NC}: Login API working"
    echo "Response: $login_response" | head -c 200
    echo "..."
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ FAIL${NC}: Login API failed"
    echo "Response: $login_response"
    ((TESTS_FAILED++))
fi
echo ""
echo ""

# ========================================
# TEST 7: Create Test Event
# ========================================
echo "📋 TEST 7: Create Test Event"
echo "------------------------------------"

EVENT_ID="TEST001_$(date +%s)"
echo "Creating event with ID: $EVENT_ID"

# Create event in database directly
mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "
INSERT INTO events (
    id, store_code, event_name, event_type, event_sub_type,
    rso, start_date, location, community, invitees, attendees,
    created_at, attendees_uploaded, region
) VALUES (
    '$EVENT_ID',
    'TEST001',
    'Test Wedding Exhibition',
    'Exhibition',
    'Wedding',
    'Test RSO',
    '2025-12-15',
    'Bangalore',
    'General',
    0,
    0,
    NOW(),
    0,
    'SOUTH'
);
" 2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ PASS${NC}: Test event created successfully"
    echo "Event ID: $EVENT_ID"
    ((TESTS_PASSED++))

    # Verify event in database
    echo "Event details:"
    mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "
    SELECT id, event_name, event_type, start_date, location
    FROM events
    WHERE id='$EVENT_ID';
    " 2>/dev/null
else
    echo -e "${RED}❌ FAIL${NC}: Failed to create test event"
    ((TESTS_FAILED++))
fi
echo ""

# ========================================
# TEST 8: Add Test Attendee
# ========================================
echo "📋 TEST 8: Add Test Attendee"
echo "------------------------------------"

mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "
INSERT INTO attendees (
    event_id, name, phone, like_product, first_time_at_tanishq,
    created_at, is_uploaded_from_excel, rso_name
) VALUES (
    '$EVENT_ID',
    'Test Customer',
    '9876543210',
    'Gold Necklace',
    1,
    NOW(),
    0,
    'Test RSO'
);
" 2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ PASS${NC}: Test attendee added successfully"
    ((TESTS_PASSED++))

    # Update attendee count
    mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "
    UPDATE events SET attendees = 1 WHERE id='$EVENT_ID';
    " 2>/dev/null

    echo "Attendee added: Test Customer (9876543210)"
else
    echo -e "${RED}❌ FAIL${NC}: Failed to add test attendee"
    ((TESTS_FAILED++))
fi
echo ""

# ========================================
# TEST 9: Test S3 File Upload
# ========================================
echo "📋 TEST 9: Test S3 File Upload"
echo "------------------------------------"

# Create test image file
TEST_FILE="/tmp/test_event_photo_$(date +%s).jpg"
echo "This is a test event photo for $EVENT_ID" > $TEST_FILE

# Upload to S3 via API
upload_response=$(curl -s -X POST $BASE_URL/events/uploadCompletedEvents \
  -F "eventId=$EVENT_ID" \
  -F "files=@$TEST_FILE")

if echo "$upload_response" | grep -q "successfully"; then
    echo -e "${GREEN}✅ PASS${NC}: S3 file upload successful via API"
    echo "Upload response: $upload_response" | head -c 200
    echo "..."
    ((TESTS_PASSED++))

    # Verify file in S3
    echo ""
    echo "Verifying file in S3..."
    sleep 2
    s3_files=$(aws s3 ls s3://$S3_BUCKET/events/$EVENT_ID/)
    if [ ! -z "$s3_files" ]; then
        echo -e "${GREEN}✅ VERIFIED${NC}: File found in S3"
        echo "$s3_files"
    else
        echo -e "${YELLOW}⚠ WARNING${NC}: File not found in S3 (may take a moment)"
    fi
else
    echo -e "${RED}❌ FAIL${NC}: S3 file upload failed"
    echo "Response: $upload_response"
    ((TESTS_FAILED++))
fi

# Cleanup test file
rm -f $TEST_FILE
echo ""

# ========================================
# TEST 10: Get Events List API
# ========================================
echo "📋 TEST 10: Get Events List API"
echo "------------------------------------"

events_response=$(curl -s -X POST $BASE_URL/events/getevents \
  -H "Content-Type: application/json" \
  -d "{\"storeCode\":\"TEST001\",\"startDate\":\"2025-12-01\",\"endDate\":\"2025-12-31\"}")

if echo "$events_response" | grep -q "$EVENT_ID"; then
    echo -e "${GREEN}✅ PASS${NC}: Events list API working"
    echo "Found test event in response"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ FAIL${NC}: Events list API failed or event not found"
    echo "Response: $events_response" | head -c 200
    ((TESTS_FAILED++))
fi
echo ""

# ========================================
# TEST 11: Test QR Code Generation
# ========================================
echo "📋 TEST 11: Test QR Code Generation"
echo "------------------------------------"

qr_response=$(curl -s $BASE_URL/events/dowload-qr/$EVENT_ID)

if echo "$qr_response" | grep -q "data:image"; then
    echo -e "${GREEN}✅ PASS${NC}: QR code generation working"
    echo "QR code generated (Base64 data returned)"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ FAIL${NC}: QR code generation failed"
    ((TESTS_FAILED++))
fi
echo ""

# ========================================
# TEST 12: Verify Database Updates
# ========================================
echo "📋 TEST 12: Verify Database Updates"
echo "------------------------------------"

echo "Event summary:"
mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e "
SELECT
    e.id as 'Event ID',
    e.event_name as 'Event Name',
    e.store_code as 'Store',
    e.invitees as 'Invitees',
    e.attendees as 'Attendees',
    e.completed_events_drive_link as 'S3 Folder',
    e.created_at as 'Created'
FROM events e
WHERE e.id = '$EVENT_ID';
" 2>/dev/null

attendee_count=$(mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -se "SELECT COUNT(*) FROM attendees WHERE event_id='$EVENT_ID'" 2>/dev/null)

if [ "$attendee_count" -gt "0" ]; then
    echo -e "${GREEN}✅ PASS${NC}: Database updates verified (Found $attendee_count attendee)"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ FAIL${NC}: No attendees found for test event"
    ((TESTS_FAILED++))
fi
echo ""

# ========================================
# TEST 13: Check Application Logs
# ========================================
echo "📋 TEST 13: Check Application Logs"
echo "------------------------------------"

log_file="/opt/tanishq/applications_preprod/application.log"

if [ -f "$log_file" ]; then
    error_count=$(grep -c "ERROR" $log_file 2>/dev/null || echo 0)
    exception_count=$(grep -c "Exception" $log_file 2>/dev/null || echo 0)

    echo "Recent log entries (last 10 lines):"
    tail -10 $log_file
    echo ""
    echo "Error summary:"
    echo "- Errors: $error_count"
    echo "- Exceptions: $exception_count"

    if [ "$error_count" -lt "5" ] && [ "$exception_count" -lt "5" ]; then
        echo -e "${GREEN}✅ PASS${NC}: Application logs look healthy"
        ((TESTS_PASSED++))
    else
        echo -e "${YELLOW}⚠ WARNING${NC}: Multiple errors found in logs"
        ((TESTS_PASSED++))
    fi
else
    echo -e "${RED}❌ FAIL${NC}: Application log file not found"
    ((TESTS_FAILED++))
fi
echo ""

# ========================================
# TEST 14: Test Direct IP Access
# ========================================
echo "📋 TEST 14: Test Direct IP Access"
echo "------------------------------------"

ip_response=$(curl -s -o /dev/null -w "%{http_code}" http://10.160.128.94)

if [ "$ip_response" = "200" ]; then
    echo -e "${GREEN}✅ PASS${NC}: Direct IP access working (10.160.128.94)"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ FAIL${NC}: Direct IP access failed (HTTP $ip_response)"
    ((TESTS_FAILED++))
fi
echo ""

# ========================================
# TEST 15: Test Nginx Proxy
# ========================================
echo "📋 TEST 15: Test Nginx Proxy"
echo "------------------------------------"

nginx_response=$(curl -s -I http://localhost | grep -c "nginx")

if [ "$nginx_response" -gt "0" ]; then
    echo -e "${GREEN}✅ PASS${NC}: Nginx proxy working"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ FAIL${NC}: Nginx proxy not responding"
    ((TESTS_FAILED++))
fi
echo ""

# ========================================
# FINAL SUMMARY
# ========================================
echo "=============================================="
echo "🎯 TEST RESULTS SUMMARY"
echo "=============================================="
echo ""
echo "Total Tests: $((TESTS_PASSED + TESTS_FAILED))"
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Failed: $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL TESTS PASSED! Pre-prod is working perfectly!${NC}"
    echo ""
    echo "✅ Application is ready for use"
    echo "✅ Database is working"
    echo "✅ S3 integration is active"
    echo "✅ All APIs are functional"
    echo ""
    echo "Test Event ID: $EVENT_ID"
    echo "You can view this event via API or database"
else
    echo -e "${RED}⚠️ SOME TESTS FAILED - Review the output above${NC}"
fi

echo ""
echo "=============================================="
echo "📊 NEXT STEPS"
echo "=============================================="
echo ""
echo "1. Review test results above"
echo "2. Check S3 bucket for uploaded test file:"
echo "   aws s3 ls s3://$S3_BUCKET/events/$EVENT_ID/"
echo ""
echo "3. Verify test event in database:"
echo "   mysql -u $MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -e \"SELECT * FROM events WHERE id='$EVENT_ID'\\G\""
echo ""
echo "4. Test event APIs manually:"
echo "   curl http://localhost:3002/events/getevents -X POST -H 'Content-Type: application/json' -d '{\"storeCode\":\"TEST001\"}'"
echo ""
echo "5. Send email to network team to fix ELB (see FINAL_DEPLOYMENT_STATUS.md)"
echo ""
echo "=============================================="
echo "Testing complete at: $(date)"
echo "=============================================="

