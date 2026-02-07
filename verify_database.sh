#!/bin/bash
# =====================================================
# MySQL Database Verification Script
# Verifies database structure and data integrity
# =====================================================

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║         MySQL Database Verification Tool                      ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Database connection details
DB_HOST="localhost"
DB_PORT="3306"
DB_USER="root"
DB_PASS="Dechub#2025"
DB_NAME="selfie_preprod"

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test database connection
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔌 Testing Database Connection..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS -e "SELECT 1;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Database connection successful${NC}"
else
    echo -e "${RED}✗ Database connection failed${NC}"
    echo "Please check your credentials and try again."
    exit 1
fi
echo ""

# Check if database exists
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🗄️  Verifying Database Existence..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS -e "SHOW DATABASES LIKE '$DB_NAME';" 2>/dev/null | grep -q $DB_NAME
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Database '$DB_NAME' exists${NC}"
else
    echo -e "${RED}✗ Database '$DB_NAME' not found${NC}"
    exit 1
fi
echo ""

# Verify all required tables exist
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 Verifying Tables..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

REQUIRED_TABLES=(
    "events"
    "stores"
    "attendees"
    "invitees"
    "users"
    "user_details"
    "bride_details"
    "greetings"
    "abm_login"
    "rbm_login"
    "cee_login"
    "password_history"
    "rivaah"
    "rivaah_users"
    "product_details"
)

MISSING_TABLES=0
for table in "${REQUIRED_TABLES[@]}"; do
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -e "SHOW TABLES LIKE '$table';" 2>/dev/null | grep -q $table
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓${NC} Table '$table' exists"
    else
        echo -e "${RED}✗${NC} Table '$table' MISSING"
        MISSING_TABLES=$((MISSING_TABLES + 1))
    fi
done

if [ $MISSING_TABLES -eq 0 ]; then
    echo -e "\n${GREEN}All 15 tables verified successfully!${NC}"
else
    echo -e "\n${RED}Warning: $MISSING_TABLES table(s) missing!${NC}"
fi
echo ""

# Check table record counts
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Table Record Counts..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME <<EOF 2>/dev/null
SELECT 'events' as table_name, COUNT(*) as record_count FROM events
UNION ALL
SELECT 'stores', COUNT(*) FROM stores
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'user_details', COUNT(*) FROM user_details
UNION ALL
SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL
SELECT 'greetings', COUNT(*) FROM greetings
UNION ALL
SELECT 'abm_login', COUNT(*) FROM abm_login
UNION ALL
SELECT 'rbm_login', COUNT(*) FROM rbm_login
UNION ALL
SELECT 'cee_login', COUNT(*) FROM cee_login
UNION ALL
SELECT 'password_history', COUNT(*) FROM password_history
UNION ALL
SELECT 'rivaah', COUNT(*) FROM rivaah
UNION ALL
SELECT 'rivaah_users', COUNT(*) FROM rivaah_users
UNION ALL
SELECT 'product_details', COUNT(*) FROM product_details;
EOF
echo ""

# Verify Foreign Key Relationships
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔗 Verifying Foreign Key Relationships..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME <<EOF 2>/dev/null
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = '$DB_NAME'
AND REFERENCED_TABLE_NAME IS NOT NULL;
EOF
echo ""

# Check for orphaned records
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔍 Checking Data Integrity..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check for orphaned attendees
ORPHANED_ATTENDEES=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -s -N -e "SELECT COUNT(*) FROM attendees WHERE event_id NOT IN (SELECT id FROM events);" 2>/dev/null)
if [ "$ORPHANED_ATTENDEES" -eq 0 ]; then
    echo -e "${GREEN}✓${NC} No orphaned attendees found"
else
    echo -e "${YELLOW}⚠${NC} Found $ORPHANED_ATTENDEES orphaned attendee records"
fi

# Check for orphaned invitees
ORPHANED_INVITEES=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -s -N -e "SELECT COUNT(*) FROM invitees WHERE event_id NOT IN (SELECT id FROM events);" 2>/dev/null)
if [ "$ORPHANED_INVITEES" -eq 0 ]; then
    echo -e "${GREEN}✓${NC} No orphaned invitees found"
else
    echo -e "${YELLOW}⚠${NC} Found $ORPHANED_INVITEES orphaned invitee records"
fi

# Check for events without stores
EVENTS_NO_STORE=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -s -N -e "SELECT COUNT(*) FROM events WHERE store_code NOT IN (SELECT store_code FROM stores);" 2>/dev/null)
if [ "$EVENTS_NO_STORE" -eq 0 ]; then
    echo -e "${GREEN}✓${NC} All events have valid store codes"
else
    echo -e "${YELLOW}⚠${NC} Found $EVENTS_NO_STORE events with invalid store codes"
fi
echo ""

# Check indexes
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔑 Verifying Indexes..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME <<EOF 2>/dev/null
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = '$DB_NAME'
GROUP BY TABLE_NAME, INDEX_NAME
ORDER BY TABLE_NAME, INDEX_NAME;
EOF
echo ""

# Database size
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "💾 Database Size Information..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME <<EOF 2>/dev/null
SELECT
    table_name AS 'Table',
    table_rows AS 'Rows',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.TABLES
WHERE table_schema = '$DB_NAME'
ORDER BY (data_length + index_length) DESC;
EOF
echo ""

# Summary
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📝 Verification Summary"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ $MISSING_TABLES -eq 0 ]; then
    echo -e "${GREEN}✓ Database Structure: VALID${NC}"
else
    echo -e "${RED}✗ Database Structure: INCOMPLETE${NC}"
fi

if [ "$ORPHANED_ATTENDEES" -eq 0 ] && [ "$ORPHANED_INVITEES" -eq 0 ] && [ "$EVENTS_NO_STORE" -eq 0 ]; then
    echo -e "${GREEN}✓ Data Integrity: GOOD${NC}"
else
    echo -e "${YELLOW}⚠ Data Integrity: ISSUES FOUND${NC}"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Verification completed at $(date)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

