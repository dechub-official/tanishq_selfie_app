#!/bin/bash
# =============================================================================
# DATA MIGRATION SCRIPT - Export from Pre-Prod to Production
# =============================================================================
#
# PURPOSE: Export all data from Pre-Production MySQL (selfie_preprod) and
#          prepare it for import into Production MySQL (selfie_prod)
#
# RUN THIS ON: Pre-Production Server (10.160.128.94)
# =============================================================================

set -e

echo "=============================================="
echo "TANISHQ DATA EXPORT - PRE-PRODUCTION"
echo "Date: $(date)"
echo "=============================================="

# Configuration
SOURCE_DB="selfie_preprod"
SOURCE_USER="root"
SOURCE_PASS="Dechub#2025"
TARGET_DB="selfie_prod"
BACKUP_DIR="/tmp/tanishq_migration"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
EXPORT_FILE="$BACKUP_DIR/data_export_${TIMESTAMP}.sql"

# Create backup directory
mkdir -p $BACKUP_DIR

echo ""
echo "Step 1: Checking source database..."
mysql -u $SOURCE_USER -p"$SOURCE_PASS" -e "USE $SOURCE_DB; SELECT 'Connected to $SOURCE_DB' as Status;"

echo ""
echo "Step 2: Getting table counts before export..."
mysql -u $SOURCE_USER -p"$SOURCE_PASS" $SOURCE_DB -e "
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
" 2>/dev/null || echo "Note: Some tables might not exist yet"

echo ""
echo "Step 3: Exporting database..."
mysqldump -u $SOURCE_USER -p"$SOURCE_PASS" \
    --single-transaction \
    --routines \
    --triggers \
    --add-drop-table \
    --complete-insert \
    --extended-insert \
    $SOURCE_DB > $EXPORT_FILE

echo "Export completed: $EXPORT_FILE"
echo "File size: $(du -h $EXPORT_FILE | cut -f1)"

echo ""
echo "Step 4: Creating production-ready version..."
# Replace database name in the export file
PROD_FILE="$BACKUP_DIR/data_for_production_${TIMESTAMP}.sql"

# Add header and modify for production
cat > $PROD_FILE << EOF
-- =============================================================================
-- TANISHQ PRODUCTION DATABASE IMPORT FILE
-- Exported from: Pre-Production ($SOURCE_DB)
-- Export Date: $(date)
-- Target Database: $TARGET_DB
-- =============================================================================

-- Disable foreign key checks during import
SET FOREIGN_KEY_CHECKS = 0;
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';

-- Select production database
USE $TARGET_DB;

EOF

# Remove database creation/selection from original and append data
grep -v "^CREATE DATABASE" $EXPORT_FILE | \
grep -v "^USE \`$SOURCE_DB\`" | \
grep -v "^DROP DATABASE" >> $PROD_FILE

# Add footer
cat >> $PROD_FILE << EOF

-- Re-enable checks
SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_MODE=@OLD_SQL_MODE;

-- =============================================================================
-- IMPORT COMPLETE
-- =============================================================================
EOF

echo "Production-ready file created: $PROD_FILE"
echo "File size: $(du -h $PROD_FILE | cut -f1)"

echo ""
echo "Step 5: Creating compressed archive..."
gzip -c $PROD_FILE > "${PROD_FILE}.gz"
echo "Compressed file: ${PROD_FILE}.gz"
echo "Compressed size: $(du -h ${PROD_FILE}.gz | cut -f1)"

echo ""
echo "=============================================="
echo "EXPORT COMPLETE"
echo "=============================================="
echo ""
echo "Files created:"
echo "  1. Original export: $EXPORT_FILE"
echo "  2. Production-ready: $PROD_FILE"
echo "  3. Compressed: ${PROD_FILE}.gz"
echo ""
echo "NEXT STEPS:"
echo "  1. Copy ${PROD_FILE}.gz to Production Server (10.10.63.97)"
echo "     scp ${PROD_FILE}.gz root@10.10.63.97:/tmp/"
echo ""
echo "  2. On Production Server, run:"
echo "     gunzip /tmp/$(basename ${PROD_FILE}).gz"
echo "     mysql -u root -p'Nagaraj@07' < /tmp/$(basename ${PROD_FILE})"
echo ""
echo "=============================================="

