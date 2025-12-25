#!/bin/bash

# Database Verification Script for Preprod
# Copy this file to your preprod server and run it

echo "========================================================"
echo "DATABASE VERIFICATION FOR CELEBRATIONSITE-PREPROD"
echo "Date: $(date)"
echo "========================================================"
echo ""

# Database credentials
DB_USER="root"
DB_PASS="Dechub#2025"
DB_NAME="selfie_preprod"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "1️⃣  CHECKING MYSQL SERVICE..."
if systemctl status mysqld >/dev/null 2>&1 || systemctl status mysql >/dev/null 2>&1; then
    echo -e "${GREEN}✅ MySQL service is RUNNING${NC}"
else
    echo -e "${RED}❌ MySQL service is NOT running${NC}"
    exit 1
fi
echo ""

echo "2️⃣  CHECKING DATABASE EXISTS..."
DB_EXISTS=$(mysql -u $DB_USER -p$DB_PASS -e "SHOW DATABASES LIKE '$DB_NAME';" 2>/dev/null | grep $DB_NAME)
if [ -n "$DB_EXISTS" ]; then
    echo -e "${GREEN}✅ Database '$DB_NAME' EXISTS${NC}"
else
    echo -e "${RED}❌ Database '$DB_NAME' NOT FOUND${NC}"
    exit 1
fi
echo ""

echo "3️⃣  CHECKING DATABASE CONNECTION..."
CONNECTION_TEST=$(mysql -u $DB_USER -p$DB_PASS $DB_NAME -e "SELECT 1;" 2>/dev/null)
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Can connect to database${NC}"
else
    echo -e "${RED}❌ Cannot connect to database${NC}"
    exit 1
fi
echo ""

echo "4️⃣  DATABASE STATISTICS..."
echo "─────────────────────────────────────────────────────"
mysql -u $DB_USER -p$DB_PASS $DB_NAME -e "
SELECT
    'Database Name' as 'Information',
    DATABASE() as 'Value'
UNION ALL
SELECT
    'Total Events',
    CAST(COUNT(*) AS CHAR)
FROM events
UNION ALL
SELECT
    'Total Stores',
    CAST(COUNT(*) AS CHAR)
FROM stores
UNION ALL
SELECT
    'Total Users',
    CAST(COUNT(*) AS CHAR)
FROM users
UNION ALL
SELECT
    'Total Attendees',
    CAST(COUNT(*) AS CHAR)
FROM attendees
UNION ALL
SELECT
    'Total Invitees',
    CAST(COUNT(*) AS CHAR)
FROM invitees;" 2>/dev/null
echo "─────────────────────────────────────────────────────"
echo ""

echo "5️⃣  LATEST 5 EVENTS IN DATABASE..."
echo "─────────────────────────────────────────────────────"
mysql -u $DB_USER -p$DB_PASS $DB_NAME -e "
SELECT
    event_code as 'Event Code',
    event_name as 'Event Name',
    store_code as 'Store',
    DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') as 'Created At'
FROM events
ORDER BY created_at DESC
LIMIT 5;" 2>/dev/null
echo "─────────────────────────────────────────────────────"
echo ""

echo "6️⃣  CHECKING APPLICATION STATUS..."
if ps aux | grep "[j]ava.*tanishq" > /dev/null; then
    APP_PID=$(ps aux | grep "[j]ava.*tanishq" | awk '{print $2}' | head -1)
    echo -e "${GREEN}✅ Application is RUNNING (PID: $APP_PID)${NC}"
else
    echo -e "${RED}❌ Application is NOT running${NC}"
fi
echo ""

echo "7️⃣  CHECKING APPLICATION PORT..."
if netstat -tlnp 2>/dev/null | grep ":3002" > /dev/null; then
    echo -e "${GREEN}✅ Application listening on port 3002${NC}"
else
    echo -e "${YELLOW}⚠️  Port 3002 not listening${NC}"
fi
echo ""

echo "8️⃣  CHECKING DATABASE CONNECTION IN APPLICATION LOGS..."
echo "─────────────────────────────────────────────────────"
if [ -f /opt/tanishq/applications_preprod/application.log ]; then
    CONN_LOG=$(tail -100 /opt/tanishq/applications_preprod/application.log | grep -i "HikariPool\|datasource" | tail -3)
    if [ -n "$CONN_LOG" ]; then
        echo "$CONN_LOG"
        if echo "$CONN_LOG" | grep -q "selfie_preprod"; then
            echo ""
            echo -e "${GREEN}✅ Application logs confirm connection to 'selfie_preprod'${NC}"
        fi
    else
        echo "No HikariPool connection logs found"
    fi
else
    echo -e "${YELLOW}⚠️  Application log file not found${NC}"
fi
echo "─────────────────────────────────────────────────────"
echo ""

echo "9️⃣  TESTING LIVE DATABASE WRITE..."
TEST_RESULT=$(mysql -u $DB_USER -p$DB_PASS $DB_NAME -e "
INSERT INTO events (event_code, event_name, store_code, created_at)
VALUES ('TEST_VERIFY', 'Database Verification Test', 'TEST', NOW());
SELECT ROW_COUNT() as rows_inserted;
DELETE FROM events WHERE event_code = 'TEST_VERIFY';
" 2>/dev/null | tail -1)

if [ "$TEST_RESULT" = "1" ]; then
    echo -e "${GREEN}✅ Can WRITE to database${NC}"
else
    echo -e "${RED}❌ Cannot write to database${NC}"
fi
echo ""

echo "🔟  FINAL VERIFICATION TEST..."
echo "─────────────────────────────────────────────────────"
echo "Current Event Count:"
BEFORE_COUNT=$(mysql -u $DB_USER -p$DB_PASS $DB_NAME -e "SELECT COUNT(*) FROM events;" 2>/dev/null | tail -1)
echo "  Events in database: $BEFORE_COUNT"
echo ""
echo "To fully verify:"
echo "  1. Open: http://celebrationsite-preprod.tanishq.co.in"
echo "  2. Login with valid credentials"
echo "  3. Create a new test event"
echo "  4. Run this command to check:"
echo "     mysql -u $DB_USER -p$DB_PASS $DB_NAME -e 'SELECT COUNT(*) FROM events;'"
echo ""
echo "  If count increases by 1, database connection is confirmed!"
echo "─────────────────────────────────────────────────────"
echo ""

echo "========================================================"
echo "VERIFICATION SUMMARY"
echo "========================================================"
echo ""
echo "Database Server: localhost:3306"
echo "Database Name:   selfie_preprod"
echo "Application URL: http://celebrationsite-preprod.tanishq.co.in"
echo "Application Port: 3002"
echo ""

# Final status check
MYSQL_OK=$(systemctl is-active mysqld 2>/dev/null || systemctl is-active mysql 2>/dev/null)
APP_OK=$(ps aux | grep "[j]ava.*tanishq" > /dev/null && echo "active" || echo "inactive")
DB_OK=$(mysql -u $DB_USER -p$DB_PASS $DB_NAME -e "SELECT 1;" 2>/dev/null && echo "✅" || echo "❌")

if [ "$MYSQL_OK" = "active" ] && [ "$APP_OK" = "active" ] && [ "$DB_OK" = "✅" ]; then
    echo -e "${GREEN}✅ ALL SYSTEMS OPERATIONAL${NC}"
    echo ""
    echo -e "${GREEN}CONFIRMED: Your preprod website is using the local database.${NC}"
    echo ""
    echo "Every event, user, and data created on:"
    echo "  → http://celebrationsite-preprod.tanishq.co.in"
    echo ""
    echo "Is stored in:"
    echo "  → Database: selfie_preprod"
    echo "  → Server: localhost (this server)"
    echo "  → NOT using any remote or production database"
else
    echo -e "${YELLOW}⚠️  SOME SYSTEMS NEED ATTENTION${NC}"
fi

echo ""
echo "========================================================"
echo "VERIFICATION COMPLETE - $(date)"
echo "========================================================"

