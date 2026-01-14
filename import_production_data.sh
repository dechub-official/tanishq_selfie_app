#!/bin/bash
# =============================================================================
# DATA IMPORT SCRIPT - Import to Production
# =============================================================================
#
# PURPOSE: Import migrated data into Production MySQL (selfie_prod)
#
# RUN THIS ON: Production Server (10.10.63.97)
# =============================================================================

set -e

echo "=============================================="
echo "TANISHQ DATA IMPORT - PRODUCTION"
echo "Date: $(date)"
echo "=============================================="

# Configuration
TARGET_DB="selfie_prod"
TARGET_USER="root"
TARGET_PASS="Nagaraj@07"

# Find the import file
IMPORT_FILE=$(ls -t /tmp/data_for_production_*.sql 2>/dev/null | head -1)

if [ -z "$IMPORT_FILE" ]; then
    # Try compressed file
    COMPRESSED_FILE=$(ls -t /tmp/data_for_production_*.sql.gz 2>/dev/null | head -1)
    if [ -n "$COMPRESSED_FILE" ]; then
        echo "Found compressed file: $COMPRESSED_FILE"
        echo "Decompressing..."
        gunzip -k "$COMPRESSED_FILE"
        IMPORT_FILE="${COMPRESSED_FILE%.gz}"
    else
        echo "ERROR: No import file found in /tmp/"
        echo "Please copy the export file from Pre-Production first."
        exit 1
    fi
fi

echo "Import file: $IMPORT_FILE"
echo "File size: $(du -h $IMPORT_FILE | cut -f1)"

echo ""
echo "Step 1: Verifying database connection..."
mysql -u $TARGET_USER -p"$TARGET_PASS" -e "SELECT 'Connected to MySQL' as Status;"

echo ""
echo "Step 2: Ensuring database exists..."
mysql -u $TARGET_USER -p"$TARGET_PASS" -e "
CREATE DATABASE IF NOT EXISTS $TARGET_DB
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
"
echo "Database $TARGET_DB ready."

echo ""
echo "Step 3: Getting table counts BEFORE import..."
mysql -u $TARGET_USER -p"$TARGET_PASS" $TARGET_DB -e "SHOW TABLES;" 2>/dev/null || echo "No tables yet"

echo ""
read -p "⚠️  WARNING: This will REPLACE existing data. Continue? (yes/no): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
    echo "Import cancelled."
    exit 0
fi

echo ""
echo "Step 4: Importing data..."
echo "This may take a few minutes..."

# Import with progress
pv $IMPORT_FILE 2>/dev/null | mysql -u $TARGET_USER -p"$TARGET_PASS" $TARGET_DB || \
mysql -u $TARGET_USER -p"$TARGET_PASS" $TARGET_DB < $IMPORT_FILE

echo ""
echo "Step 5: Verifying import - Table counts AFTER import..."
mysql -u $TARGET_USER -p"$TARGET_PASS" $TARGET_DB -e "
SELECT 'abm_login' as TableName, COUNT(*) as Records FROM abm_login UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees UNION ALL
SELECT 'bride_details', COUNT(*) FROM bride_details UNION ALL
SELECT 'cee_login', COUNT(*) FROM cee_login UNION ALL
SELECT 'events', COUNT(*) FROM events UNION ALL
SELECT 'greeting', COUNT(*) FROM greeting UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees UNION ALL
SELECT 'password_history', COUNT(*) FROM password_history UNION ALL
SELECT 'product_details', COUNT(*) FROM product_details UNION ALL
SELECT 'rbm_login', COUNT(*) FROM rbm_login UNION ALL
SELECT 'rivaah', COUNT(*) FROM rivaah UNION ALL
SELECT 'rivaah_user', COUNT(*) FROM rivaah_user UNION ALL
SELECT 'stores', COUNT(*) FROM stores UNION ALL
SELECT 'user_details', COUNT(*) FROM user_details UNION ALL
SELECT 'users', COUNT(*) FROM users
ORDER BY TableName;
" 2>/dev/null || echo "Note: Some tables might have different names"

echo ""
echo "=============================================="
echo "IMPORT COMPLETE"
echo "=============================================="
echo ""
echo "NEXT STEPS:"
echo "  1. Deploy the application WAR file"
echo "  2. Start the application with: systemctl start tanishq"
echo "  3. Verify application health"
echo "  4. Test all functionality"
echo ""
echo "=============================================="

