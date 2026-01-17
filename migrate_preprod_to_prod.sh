#!/bin/bash

# =====================================================
# MIGRATE DATA FROM PRE-PROD TO PRODUCTION
# =====================================================
# This script exports data from pre-prod MySQL and
# imports it to production MySQL database
# =====================================================

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PREPROD_HOST="<PREPROD_SERVER_IP>"  # UPDATE THIS
PREPROD_USER="root"
PREPROD_DB="selfie_preprod"

PROD_HOST="localhost"
PROD_USER="root"
PROD_DB="selfie_prod"

BACKUP_DIR="/opt/tanishq/migration_backup"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
EXPORT_FILE="${BACKUP_DIR}/preprod_export_${TIMESTAMP}.sql"

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

# =====================================================
# Main Migration Process
# =====================================================

print_header "DATA MIGRATION: PRE-PROD → PRODUCTION"

# Step 1: Create backup directory
print_info "Creating backup directory..."
mkdir -p ${BACKUP_DIR}
print_success "Backup directory ready: ${BACKUP_DIR}"

# Step 2: Backup current production database
print_header "STEP 1: Backup Current Production Database"
print_info "Creating backup of current production data..."

mysqldump -u ${PROD_USER} -p ${PROD_DB} > ${BACKUP_DIR}/prod_backup_before_migration_${TIMESTAMP}.sql

if [ $? -eq 0 ]; then
    print_success "Production backup created: prod_backup_before_migration_${TIMESTAMP}.sql"
else
    print_error "Failed to create production backup!"
    exit 1
fi

# Step 3: Export data from Pre-Prod
print_header "STEP 2: Export Data from Pre-Prod"

# Check if running on pre-prod server or production server
if [ "${PREPROD_HOST}" == "localhost" ] || [ "${PREPROD_HOST}" == "127.0.0.1" ]; then
    # Running on pre-prod server - direct export
    print_info "Exporting data from pre-prod database..."

    mysqldump -u ${PREPROD_USER} -p ${PREPROD_DB} \
        --no-create-info \
        --complete-insert \
        --skip-triggers \
        --skip-lock-tables \
        --tables events attendees invitees bride_details users stores product_details greetings rivaah rivaah_users \
        > ${EXPORT_FILE}

    if [ $? -eq 0 ]; then
        print_success "Data exported successfully: ${EXPORT_FILE}"
    else
        print_error "Failed to export data from pre-prod!"
        exit 1
    fi
else
    # Running on prod server - need to fetch from remote
    print_warning "This script should be run on PRE-PROD server first, then transfer the file to production"
    print_info "Alternative: Use the manual steps in LEGACY_DATA_MIGRATION_GUIDE.md"
    exit 1
fi

# Step 4: Analyze export file
print_header "STEP 3: Analyze Exported Data"

EXPORT_SIZE=$(du -h ${EXPORT_FILE} | cut -f1)
EXPORT_LINES=$(wc -l < ${EXPORT_FILE})

print_info "Export file size: ${EXPORT_SIZE}"
print_info "Export file lines: ${EXPORT_LINES}"

# Count records per table
print_info "Counting records in export file..."
echo ""
echo "Table              | INSERT Count"
echo "-----------------------------------"
grep -c "INSERT INTO \`events\`" ${EXPORT_FILE} || echo "events             | 0"
grep -c "INSERT INTO \`attendees\`" ${EXPORT_FILE} || echo "attendees          | 0"
grep -c "INSERT INTO \`invitees\`" ${EXPORT_FILE} || echo "invitees           | 0"
grep -c "INSERT INTO \`bride_details\`" ${EXPORT_FILE} || echo "bride_details      | 0"
grep -c "INSERT INTO \`users\`" ${EXPORT_FILE} || echo "users              | 0"
grep -c "INSERT INTO \`stores\`" ${EXPORT_FILE} || echo "stores             | 0"
grep -c "INSERT INTO \`product_details\`" ${EXPORT_FILE} || echo "product_details    | 0"
echo ""

# Step 5: Ask for confirmation
print_header "STEP 4: Confirmation"
print_warning "This will import data into production database: ${PROD_DB}"
print_warning "Current production data will be merged with pre-prod data"
echo ""
read -p "Do you want to continue? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    print_error "Migration cancelled by user"
    exit 1
fi

# Step 6: Import to production
print_header "STEP 5: Import Data to Production"

print_info "Importing data to production database..."

# Disable foreign key checks temporarily
mysql -u ${PROD_USER} -p ${PROD_DB} -e "SET FOREIGN_KEY_CHECKS=0;"

# Import the data
mysql -u ${PROD_USER} -p ${PROD_DB} < ${EXPORT_FILE}

if [ $? -eq 0 ]; then
    print_success "Data imported successfully!"
else
    print_error "Failed to import data!"
    print_warning "You can restore from backup: ${BACKUP_DIR}/prod_backup_before_migration_${TIMESTAMP}.sql"
    exit 1
fi

# Re-enable foreign key checks
mysql -u ${PROD_USER} -p ${PROD_DB} -e "SET FOREIGN_KEY_CHECKS=1;"

# Step 7: Verify imported data
print_header "STEP 6: Verify Imported Data"

print_info "Counting records in production database..."

mysql -u ${PROD_USER} -p -e "
USE ${PROD_DB};
SELECT '========================================' as '';
SELECT 'PRODUCTION DATABASE - RECORD COUNTS' as '';
SELECT '========================================' as '';
SELECT 'events' as table_name, COUNT(*) as count FROM events
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL
SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'stores', COUNT(*) FROM stores
UNION ALL
SELECT 'product_details', COUNT(*) FROM product_details
UNION ALL
SELECT 'greetings', COUNT(*) FROM greetings;
"

# Step 8: Summary
print_header "MIGRATION COMPLETED SUCCESSFULLY!"

print_success "Data has been migrated from pre-prod to production"
print_info "Export file: ${EXPORT_FILE}"
print_info "Backup file: ${BACKUP_DIR}/prod_backup_before_migration_${TIMESTAMP}.sql"
echo ""
print_warning "Next Steps:"
echo "  1. Verify data in production database"
echo "  2. Test application functionality"
echo "  3. Restart application: kill \$(cat /opt/tanishq/tanishq-prod.pid) && nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war --spring.profiles.active=prod --server.port=3000 > /opt/tanishq/logs/application.log 2>&1 & echo \$! > /opt/tanishq/tanishq-prod.pid"
echo "  4. Check logs: tail -f /opt/tanishq/logs/application.log"
echo "  5. Test website functionality"
echo ""

print_success "Migration script completed!"

