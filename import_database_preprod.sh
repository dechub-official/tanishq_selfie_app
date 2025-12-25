#!/bin/bash
# ================================================
# Database Import Script for Pre-Production
# ================================================
# This script imports the database backup into
# the pre-production tanishq_preprod database
#
# Server: 10.160.128.94
# Database: tanishq_preprod

echo "================================================"
echo "Database Import for Pre-Production"
echo "================================================"
echo ""

# Configuration
DB_USER="tanishq_preprod"
DB_NAME="tanishq_preprod"
BACKUP_DIR="/tmp"

# Find the most recent backup file
echo "[Step 1/5] Looking for backup files in $BACKUP_DIR..."
LATEST_BACKUP=$(ls -t $BACKUP_DIR/tanishq_backup_*.sql 2>/dev/null | head -n 1)

if [ -z "$LATEST_BACKUP" ]; then
    echo "ERROR: No backup files found in $BACKUP_DIR"
    echo ""
    echo "Please upload the SQL backup file to $BACKUP_DIR first"
    echo "Expected filename pattern: tanishq_backup_*.sql"
    echo ""
    exit 1
fi

echo "Found backup file: $LATEST_BACKUP"
FILE_SIZE=$(du -h "$LATEST_BACKUP" | cut -f1)
echo "File size: $FILE_SIZE"
echo ""

# List all available backup files
echo "Available backup files:"
ls -lh $BACKUP_DIR/tanishq_backup_*.sql 2>/dev/null
echo ""

# Ask for confirmation
read -p "Do you want to import this file? (yes/no): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
    echo "Import cancelled by user"
    exit 0
fi
echo ""

# Check if database exists
echo "[Step 2/5] Checking if database exists..."
mysql -u $DB_USER -p -e "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='$DB_NAME';" 2>/dev/null | grep -q "$DB_NAME"

if [ $? -eq 0 ]; then
    echo "Database '$DB_NAME' exists"
    echo ""
    read -p "WARNING: Database exists. Do you want to drop and recreate it? (yes/no): " DROP_DB
    if [ "$DROP_DB" == "yes" ]; then
        echo "Dropping existing database..."
        mysql -u $DB_USER -p -e "DROP DATABASE $DB_NAME; CREATE DATABASE $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
        echo "Database recreated"
    fi
else
    echo "Database '$DB_NAME' does not exist. Creating..."
    mysql -u $DB_USER -p -e "CREATE DATABASE $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo "Database created"
fi
echo ""

# Check if backup contains database creation
echo "[Step 3/5] Analyzing backup file..."
if grep -q "CREATE DATABASE" "$LATEST_BACKUP"; then
    echo "Backup contains CREATE DATABASE statements"

    # Check if it creates the wrong database name
    if grep -q "CREATE DATABASE.*\`tanishq\`" "$LATEST_BACKUP"; then
        echo "WARNING: Backup creates 'tanishq' database, not 'tanishq_preprod'"
        echo ""
        echo "Options:"
        echo "1. Import and then rename database"
        echo "2. Edit the SQL file before import (recommended)"
        echo "3. Cancel and manually edit the file"
        echo ""
        read -p "Choose option (1/2/3): " OPTION

        if [ "$OPTION" == "2" ]; then
            echo "Creating modified backup file..."
            sed 's/CREATE DATABASE.*`tanishq`/CREATE DATABASE `tanishq_preprod`/g' "$LATEST_BACKUP" | \
            sed 's/USE `tanishq`/USE `tanishq_preprod`/g' > "${LATEST_BACKUP%.sql}_modified.sql"
            LATEST_BACKUP="${LATEST_BACKUP%.sql}_modified.sql"
            echo "Modified file created: $LATEST_BACKUP"
        elif [ "$OPTION" == "3" ]; then
            echo "Import cancelled. Please edit the file and run this script again."
            exit 0
        fi
    fi
else
    echo "Backup does not contain CREATE DATABASE (table dump only)"
fi
echo ""

# Import the database
echo "[Step 4/5] Importing database..."
echo "This may take several minutes depending on database size..."
echo "Please wait..."
echo ""

# Start time
START_TIME=$(date +%s)

# Import with progress
pv "$LATEST_BACKUP" | mysql -u $DB_USER -p $DB_NAME 2>&1

if [ $? -eq 0 ]; then
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))
    echo ""
    echo "Import completed successfully in $DURATION seconds!"
else
    echo ""
    echo "================================================"
    echo "ERROR: Import failed!"
    echo "================================================"
    echo ""
    echo "Common issues:"
    echo "- Wrong password"
    echo "- Insufficient permissions"
    echo "- Database already has conflicting data"
    echo ""
    exit 1
fi
echo ""

# Verify import
echo "[Step 5/5] Verifying import..."
echo ""
echo "Database tables:"
mysql -u $DB_USER -p $DB_NAME -e "SHOW TABLES;" 2>/dev/null
echo ""

echo "Table row counts:"
mysql -u $DB_USER -p $DB_NAME -e "
    SELECT
        TABLE_NAME,
        TABLE_ROWS
    FROM
        information_schema.TABLES
    WHERE
        TABLE_SCHEMA = '$DB_NAME'
    ORDER BY
        TABLE_NAME;
" 2>/dev/null
echo ""

echo "================================================"
echo "SUCCESS: Database imported successfully!"
echo "================================================"
echo ""
echo "Next Steps:"
echo "1. Verify the data is correct"
echo "2. Test application connection"
echo "3. Cleanup backup files:"
echo "   rm $LATEST_BACKUP"
echo ""
echo "To verify specific data, login to MySQL:"
echo "   mysql -u $DB_USER -p $DB_NAME"
echo ""

# Ask if user wants to cleanup
read -p "Do you want to delete the backup file from /tmp? (yes/no): " CLEANUP
if [ "$CLEANUP" == "yes" ]; then
    rm "$LATEST_BACKUP"
    echo "Backup file deleted: $LATEST_BACKUP"

    # Also delete modified file if exists
    if [ -f "${LATEST_BACKUP%.sql}_modified.sql" ]; then
        rm "${LATEST_BACKUP%.sql}_modified.sql"
        echo "Modified file deleted"
    fi
fi

echo ""
echo "Import script completed!"

