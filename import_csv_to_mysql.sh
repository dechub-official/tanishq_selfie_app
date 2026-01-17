#!/bin/bash

# =====================================================
# CSV TO MYSQL IMPORT SCRIPT
# =====================================================
# Imports CSV files for events, attendees, and invitees
# into production MySQL database safely
# =====================================================

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
MYSQL_USER="root"
MYSQL_PASS="Nagaraj@07"
MYSQL_DB="selfie_prod"
CSV_DIR="/opt/tanishq/csv_import"
BACKUP_DIR="/opt/tanishq/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# CSV files
EVENTS_CSV="${CSV_DIR}/events.csv"
ATTENDEES_CSV="${CSV_DIR}/attendees.csv"
INVITEES_CSV="${CSV_DIR}/invitees.csv"

# =====================================================
# Functions
# =====================================================

print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

check_file() {
    if [ -f "$1" ]; then
        print_success "Found: $1"
        return 0
    else
        print_error "Missing: $1"
        return 1
    fi
}

count_csv_rows() {
    local file=$1
    local count=$(tail -n +2 "$file" | wc -l)
    echo $count
}

# =====================================================
# Main Import Process
# =====================================================

print_header "CSV TO MYSQL IMPORT SCRIPT"

# Step 1: Verify directories exist
print_info "Creating directories..."
mkdir -p ${CSV_DIR}
mkdir -p ${BACKUP_DIR}
chmod 755 ${CSV_DIR}
chmod 755 ${BACKUP_DIR}
print_success "Directories ready"

# Step 2: Check CSV files exist
print_header "STEP 1: Verify CSV Files"

FILES_OK=true
check_file "${EVENTS_CSV}" || FILES_OK=false
check_file "${ATTENDEES_CSV}" || FILES_OK=false
check_file "${INVITEES_CSV}" || FILES_OK=false

if [ "$FILES_OK" = false ]; then
    print_error "Some CSV files are missing!"
    print_warning "Please upload CSV files to: ${CSV_DIR}/"
    print_info "Required files:"
    echo "  - events.csv"
    echo "  - attendees.csv"
    echo "  - invitees.csv"
    exit 1
fi

# Step 3: Show CSV file info
print_header "STEP 2: CSV File Information"

echo -e "${BLUE}File Details:${NC}"
ls -lh "${EVENTS_CSV}" "${ATTENDEES_CSV}" "${INVITEES_CSV}"
echo ""

echo -e "${BLUE}Row Counts (excluding header):${NC}"
EVENTS_COUNT=$(count_csv_rows "${EVENTS_CSV}")
ATTENDEES_COUNT=$(count_csv_rows "${ATTENDEES_CSV}")
INVITEES_COUNT=$(count_csv_rows "${INVITEES_CSV}")

echo "  Events:    ${EVENTS_COUNT} rows"
echo "  Attendees: ${ATTENDEES_COUNT} rows"
echo "  Invitees:  ${INVITEES_COUNT} rows"
echo ""

print_info "Previewing first 3 lines of each CSV file..."
echo ""
echo -e "${BLUE}Events CSV:${NC}"
head -3 "${EVENTS_CSV}"
echo ""
echo -e "${BLUE}Attendees CSV:${NC}"
head -3 "${ATTENDEES_CSV}"
echo ""
echo -e "${BLUE}Invitees CSV:${NC}"
head -3 "${INVITEES_CSV}"
echo ""

# Step 4: Get current database counts
print_header "STEP 3: Current Database Status"

print_info "Checking current record counts..."

CURRENT_EVENTS=$(mysql -u ${MYSQL_USER} -p${MYSQL_PASS} -N -e "SELECT COUNT(*) FROM events;" ${MYSQL_DB})
CURRENT_ATTENDEES=$(mysql -u ${MYSQL_USER} -p${MYSQL_PASS} -N -e "SELECT COUNT(*) FROM attendees;" ${MYSQL_DB})
CURRENT_INVITEES=$(mysql -u ${MYSQL_USER} -p${MYSQL_PASS} -N -e "SELECT COUNT(*) FROM invitees;" ${MYSQL_DB})

echo "Current counts in database:"
echo "  Events:    ${CURRENT_EVENTS}"
echo "  Attendees: ${CURRENT_ATTENDEES}"
echo "  Invitees:  ${CURRENT_INVITEES}"
echo ""

echo "After import (expected):"
echo "  Events:    $((CURRENT_EVENTS + EVENTS_COUNT))"
echo "  Attendees: $((CURRENT_ATTENDEES + ATTENDEES_COUNT))"
echo "  Invitees:  $((CURRENT_INVITEES + INVITEES_COUNT))"
echo ""

# Step 5: Confirmation
print_header "STEP 4: Confirmation"

print_warning "This will import CSV data into production database: ${MYSQL_DB}"
print_warning "A backup will be created before import"
echo ""
read -p "Do you want to continue? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    print_error "Import cancelled by user"
    exit 1
fi

# Step 6: Stop application
print_header "STEP 5: Stop Application"

print_info "Stopping application..."
if [ -f /opt/tanishq/tanishq-prod.pid ]; then
    PID=$(cat /opt/tanishq/tanishq-prod.pid)
    if ps -p $PID > /dev/null 2>&1; then
        kill $PID
        print_success "Application stopped (PID: ${PID})"
        sleep 3
    else
        print_warning "Application not running"
    fi
else
    print_warning "PID file not found, application may not be running"
fi

# Step 7: Backup database
print_header "STEP 6: Backup Database"

BACKUP_FILE="${BACKUP_DIR}/backup_before_csv_import_${TIMESTAMP}.sql"
print_info "Creating backup: ${BACKUP_FILE}"

mysqldump -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} > ${BACKUP_FILE}

if [ $? -eq 0 ]; then
    BACKUP_SIZE=$(du -h ${BACKUP_FILE} | cut -f1)
    print_success "Backup created successfully (${BACKUP_SIZE})"
else
    print_error "Backup failed!"
    exit 1
fi

# Step 8: Import CSV files
print_header "STEP 7: Import CSV Files to MySQL"

print_info "Starting import process..."
echo ""

# Create import SQL script
IMPORT_SQL="/tmp/import_csv_${TIMESTAMP}.sql"

cat > ${IMPORT_SQL} << 'EOF'
-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS=0;
SET AUTOCOMMIT=0;

-- Import events
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/events.csv'
INTO TABLE events
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

-- Import attendees
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/attendees.csv'
INTO TABLE attendees
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

-- Import invitees
LOAD DATA LOCAL INFILE '/opt/tanishq/csv_import/invitees.csv'
INTO TABLE invitees
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

COMMIT;
SET FOREIGN_KEY_CHECKS=1;
EOF

# Execute import
print_info "Importing data..."
mysql --local-infile=1 -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} < ${IMPORT_SQL}

if [ $? -eq 0 ]; then
    print_success "CSV data imported successfully!"
else
    print_error "Import failed!"
    print_warning "You can restore from backup: ${BACKUP_FILE}"
    exit 1
fi

# Clean up temp SQL file
rm -f ${IMPORT_SQL}

# Step 9: Verify import
print_header "STEP 8: Verify Import"

print_info "Counting records after import..."

NEW_EVENTS=$(mysql -u ${MYSQL_USER} -p${MYSQL_PASS} -N -e "SELECT COUNT(*) FROM events;" ${MYSQL_DB})
NEW_ATTENDEES=$(mysql -u ${MYSQL_USER} -p${MYSQL_PASS} -N -e "SELECT COUNT(*) FROM attendees;" ${MYSQL_DB})
NEW_INVITEES=$(mysql -u ${MYSQL_USER} -p${MYSQL_PASS} -N -e "SELECT COUNT(*) FROM invitees;" ${MYSQL_DB})

echo ""
echo -e "${BLUE}Import Results:${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "%-15s | %-10s | %-10s | %-10s\n" "Table" "Before" "After" "Imported"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "%-15s | %-10s | %-10s | %-10s\n" "Events" "$CURRENT_EVENTS" "$NEW_EVENTS" "$((NEW_EVENTS - CURRENT_EVENTS))"
printf "%-15s | %-10s | %-10s | %-10s\n" "Attendees" "$CURRENT_ATTENDEES" "$NEW_ATTENDEES" "$((NEW_ATTENDEES - CURRENT_ATTENDEES))"
printf "%-15s | %-10s | %-10s | %-10s\n" "Invitees" "$CURRENT_INVITEES" "$NEW_INVITEES" "$((NEW_INVITEES - CURRENT_INVITEES))"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Verify import counts match CSV rows
IMPORTED_EVENTS=$((NEW_EVENTS - CURRENT_EVENTS))
IMPORTED_ATTENDEES=$((NEW_ATTENDEES - CURRENT_ATTENDEES))
IMPORTED_INVITEES=$((NEW_INVITEES - CURRENT_INVITEES))

if [ $IMPORTED_EVENTS -ne $EVENTS_COUNT ] || [ $IMPORTED_ATTENDEES -ne $ATTENDEES_COUNT ] || [ $IMPORTED_INVITEES -ne $INVITEES_COUNT ]; then
    print_warning "Imported counts don't match CSV row counts!"
    print_warning "This may be due to duplicate keys or other constraints"
else
    print_success "All records imported successfully!"
fi

# Show sample of imported data
print_info "Sample of recently imported data:"
echo ""
mysql -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} -e "
SELECT id, event_name, start_date, store_code FROM events ORDER BY created_at DESC LIMIT 3;
"

# Step 10: Restart application
print_header "STEP 9: Restart Application"

print_info "Clearing logs..."
> /opt/tanishq/logs/application.log

print_info "Starting application..."
cd /opt/tanishq
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > logs/application.log 2>&1 &

echo $! > tanishq-prod.pid
NEW_PID=$(cat tanishq-prod.pid)
print_success "Application started (PID: ${NEW_PID})"

print_info "Waiting for application to start (30 seconds)..."
sleep 30

# Check if application is running
if ps -p $NEW_PID > /dev/null 2>&1; then
    print_success "Application is running!"
else
    print_error "Application failed to start! Check logs."
    exit 1
fi

# Step 11: Test application
print_header "STEP 10: Test Application"

print_info "Testing HTTP response..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/)

if [ "$HTTP_CODE" == "200" ]; then
    print_success "HTTP response: 200 OK"
else
    print_warning "HTTP response: ${HTTP_CODE}"
fi

print_info "Checking port 3000..."
if ss -tlnp | grep -q ":3000"; then
    print_success "Port 3000 is listening"
else
    print_error "Port 3000 is not listening!"
fi

# Summary
print_header "IMPORT COMPLETED SUCCESSFULLY!"

echo -e "${GREEN}✓ CSV files imported to production database${NC}"
echo -e "${GREEN}✓ Backup created: ${BACKUP_FILE}${NC}"
echo -e "${GREEN}✓ Application restarted${NC}"
echo ""

print_info "Summary:"
echo "  - Events imported:    ${IMPORTED_EVENTS}"
echo "  - Attendees imported: ${IMPORTED_ATTENDEES}"
echo "  - Invitees imported:  ${IMPORTED_INVITEES}"
echo ""

print_warning "Next Steps:"
echo "  1. Check application logs: tail -f /opt/tanishq/logs/application.log"
echo "  2. Test website in browser"
echo "  3. Verify imported data is visible"
echo "  4. Check for any errors"
echo ""

print_info "Backup location: ${BACKUP_FILE}"
print_info "To restore if needed: mysql -u root -p selfie_prod < ${BACKUP_FILE}"
echo ""

print_success "Import script completed!"

