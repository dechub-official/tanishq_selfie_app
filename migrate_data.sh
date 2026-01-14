#!/bin/bash

#############################################
# DATA MIGRATION: Pre-Prod → Production
# Safe migration with automatic backups
#############################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BACKUP_DIR="/opt/tanishq/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
SOURCE_DB="selfie_preprod"
TARGET_DB="selfie_prod"
APP_WAR="tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war"
APP_PORT="3000"

echo -e "${BLUE}=========================================="
echo "🔄 DATA MIGRATION SCRIPT"
echo "Pre-Prod → Production"
echo -e "==========================================${NC}"
echo ""

# Create backup directory
echo -e "${YELLOW}📁 Creating backup directory...${NC}"
mkdir -p $BACKUP_DIR
echo -e "${GREEN}✅ Backup directory ready: $BACKUP_DIR${NC}"
echo ""

# Step 1: Backup Production Database
echo -e "${YELLOW}📦 Step 1/7: Backing up production database...${NC}"
echo "This may take a few minutes..."
mysqldump -u root -p $TARGET_DB > $BACKUP_DIR/${TARGET_DB}_before_migration_$TIMESTAMP.sql 2>/dev/null

if [ $? -eq 0 ]; then
    BACKUP_SIZE=$(ls -lh $BACKUP_DIR/${TARGET_DB}_before_migration_$TIMESTAMP.sql | awk '{print $5}')
    echo -e "${GREEN}✅ Production backup completed ($BACKUP_SIZE)${NC}"
    echo "   Saved to: $BACKUP_DIR/${TARGET_DB}_before_migration_$TIMESTAMP.sql"
else
    echo -e "${RED}❌ Production backup failed!${NC}"
    exit 1
fi
echo ""

# Step 2: Export Pre-Prod Data
echo -e "${YELLOW}📤 Step 2/7: Exporting pre-prod data...${NC}"
echo "This may take a few minutes..."
mysqldump -u root -p $SOURCE_DB > $BACKUP_DIR/${SOURCE_DB}_export_$TIMESTAMP.sql 2>/dev/null

if [ $? -eq 0 ]; then
    EXPORT_SIZE=$(ls -lh $BACKUP_DIR/${SOURCE_DB}_export_$TIMESTAMP.sql | awk '{print $5}')
    echo -e "${GREEN}✅ Pre-prod data exported ($EXPORT_SIZE)${NC}"
    echo "   Saved to: $BACKUP_DIR/${SOURCE_DB}_export_$TIMESTAMP.sql"
else
    echo -e "${RED}❌ Pre-prod export failed!${NC}"
    exit 1
fi
echo ""

# Step 3: Stop Production Application
echo -e "${YELLOW}🛑 Step 3/7: Stopping production application...${NC}"
if [ -f /opt/tanishq/tanishq-prod.pid ]; then
    PID=$(cat /opt/tanishq/tanishq-prod.pid)
    kill $PID 2>/dev/null
    sleep 3

    if ps -p $PID > /dev/null 2>&1; then
        kill -9 $PID 2>/dev/null
    fi
    echo -e "${GREEN}✅ Production application stopped${NC}"
else
    echo -e "${YELLOW}⚠️  No PID file found, skipping...${NC}"
fi
echo ""

# Step 4: Clear Production Database
echo -e "${YELLOW}🗑️  Step 4/7: Clearing production database...${NC}"
mysql -u root -p -e "DROP DATABASE IF EXISTS $TARGET_DB; CREATE DATABASE $TARGET_DB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Production database cleared and recreated${NC}"
else
    echo -e "${RED}❌ Failed to clear production database!${NC}"
    exit 1
fi
echo ""

# Step 5: Import Pre-Prod Data to Production
echo -e "${YELLOW}📥 Step 5/7: Importing pre-prod data to production...${NC}"
echo "This may take several minutes depending on data size..."
mysql -u root -p $TARGET_DB < $BACKUP_DIR/${SOURCE_DB}_export_$TIMESTAMP.sql 2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Data imported successfully${NC}"
else
    echo -e "${RED}❌ Import failed!${NC}"
    echo "Attempting to restore production backup..."
    mysql -u root -p $TARGET_DB < $BACKUP_DIR/${TARGET_DB}_before_migration_$TIMESTAMP.sql 2>/dev/null
    exit 1
fi
echo ""

# Step 6: Verify Data
echo -e "${YELLOW}🔍 Step 6/7: Verifying migrated data...${NC}"
mysql -u root -p -e "
USE $TARGET_DB;
SELECT '===================' as '';
SELECT 'Table Record Counts' as '';
SELECT '===================' as '';
SELECT 'events' as table_name, COUNT(*) as count FROM events
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'greetings', COUNT(*) FROM greetings
UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL
SELECT 'stores', COUNT(*) FROM stores
UNION ALL
SELECT 'product_details', COUNT(*) FROM product_details;
" 2>/dev/null
echo ""

# Step 7: Restart Production Application
echo -e "${YELLOW}🚀 Step 7/7: Restarting production application...${NC}"
cd /opt/tanishq
nohup java -jar $APP_WAR \
  --spring.profiles.active=prod \
  --server.port=$APP_PORT \
  > logs/application.log 2>&1 &

NEW_PID=$!
echo $NEW_PID > tanishq-prod.pid

echo "Waiting for application to start..."
sleep 10

if ps -p $NEW_PID > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Production application restarted (PID: $NEW_PID)${NC}"
else
    echo -e "${RED}⚠️  Application may not have started. Check logs.${NC}"
fi
echo ""

# Summary
echo -e "${BLUE}=========================================="
echo "✅ MIGRATION COMPLETED SUCCESSFULLY!"
echo -e "==========================================${NC}"
echo ""
echo -e "${GREEN}📊 Summary:${NC}"
echo "  📦 Production backup: ${TARGET_DB}_before_migration_$TIMESTAMP.sql"
echo "  📤 Pre-prod export:   ${SOURCE_DB}_export_$TIMESTAMP.sql"
echo "  🆔 Production PID:    $NEW_PID"
echo "  📁 Backup location:   $BACKUP_DIR"
echo ""
echo -e "${YELLOW}📝 Next Steps:${NC}"
echo "  1. Test production website: https://celebrations.tanishq.co.in/"
echo "  2. Verify user login works"
echo "  3. Check events are visible"
echo "  4. Test image uploads"
echo "  5. Monitor logs: tail -f /opt/tanishq/logs/application.log"
echo ""
echo -e "${BLUE}🔄 To rollback if needed:${NC}"
echo "  mysql -u root -p -e \"DROP DATABASE $TARGET_DB; CREATE DATABASE $TARGET_DB;\""
echo "  mysql -u root -p $TARGET_DB < $BACKUP_DIR/${TARGET_DB}_before_migration_$TIMESTAMP.sql"
echo ""
echo -e "${GREEN}Migration complete! Happy deploying! 🚀${NC}"

