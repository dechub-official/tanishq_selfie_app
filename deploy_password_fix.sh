#!/bin/bash

# ========================================================
# PASSWORD CHANGE FIX - BUILD AND DEPLOY ON PRODUCTION
# ========================================================

echo ""
echo "========================================================"
echo "  PASSWORD CHANGE FIX - BUILD AND DEPLOY"
echo "========================================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
APP_DIR="/opt/tanishq"
SOURCE_DIR="/opt/tanishq/source/tanishq_selfie_app"
WAR_NAME="tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war"
NEW_WAR="target/tanishq-preprod-17-01-2026-1-0.0.1-SNAPSHOT.war"
PID_FILE="/opt/tanishq/tanishq-prod.pid"
LOG_FILE="/opt/tanishq/logs/application.log"

echo -e "${YELLOW}[1/8] Checking if source directory exists...${NC}"
if [ ! -d "$SOURCE_DIR" ]; then
    echo -e "${RED}   ✗ Source directory not found: $SOURCE_DIR${NC}"
    echo -e "${YELLOW}   Please upload the source files first using:${NC}"
    echo "   scp -r <local_path>/tanishq_selfie_app root@10.10.63.97:/opt/tanishq/source/"
    exit 1
fi
echo -e "${GREEN}   ✓ Source directory found${NC}"

echo ""
echo -e "${YELLOW}[2/8] Navigating to source directory...${NC}"
cd "$SOURCE_DIR" || exit 1
echo -e "${GREEN}   ✓ Changed to: $(pwd)${NC}"

echo ""
echo -e "${YELLOW}[3/8] Building application with Maven...${NC}"
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo -e "${RED}   ✗ Build failed!${NC}"
    exit 1
fi
echo -e "${GREEN}   ✓ Build successful${NC}"

echo ""
echo -e "${YELLOW}[4/8] Checking if WAR file was created...${NC}"
if [ ! -f "$NEW_WAR" ]; then
    echo -e "${RED}   ✗ WAR file not found: $NEW_WAR${NC}"
    exit 1
fi
echo -e "${GREEN}   ✓ WAR file created: $NEW_WAR${NC}"

echo ""
echo -e "${YELLOW}[5/8] Stopping current application...${NC}"
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        kill "$PID"
        echo -e "${GREEN}   ✓ Application stopped (PID: $PID)${NC}"
        sleep 3
    else
        echo -e "${YELLOW}   ! PID file exists but process not running${NC}"
    fi
else
    echo -e "${YELLOW}   ! PID file not found, application might not be running${NC}"
fi

echo ""
echo -e "${YELLOW}[6/8] Backing up old WAR file...${NC}"
if [ -f "$APP_DIR/$WAR_NAME" ]; then
    mkdir -p "$APP_DIR/backup"
    BACKUP_NAME="$APP_DIR/backup/${WAR_NAME}.backup.$(date +%Y%m%d_%H%M%S)"
    cp "$APP_DIR/$WAR_NAME" "$BACKUP_NAME"
    echo -e "${GREEN}   ✓ Backup created: $BACKUP_NAME${NC}"
else
    echo -e "${YELLOW}   ! Old WAR file not found, skipping backup${NC}"
fi

echo ""
echo -e "${YELLOW}[7/8] Deploying new WAR file...${NC}"
cp "$NEW_WAR" "$APP_DIR/$WAR_NAME"
if [ $? -ne 0 ]; then
    echo -e "${RED}   ✗ Failed to copy WAR file${NC}"
    exit 1
fi
echo -e "${GREEN}   ✓ New WAR file deployed${NC}"

echo ""
echo -e "${YELLOW}[8/8] Starting application...${NC}"
nohup java -jar "$APP_DIR/$WAR_NAME" \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > "$LOG_FILE" 2>&1 &

NEW_PID=$!
echo $NEW_PID > "$PID_FILE"
echo -e "${GREEN}   ✓ Application started (PID: $NEW_PID)${NC}"

echo ""
echo "========================================================"
echo -e "${GREEN}  DEPLOYMENT COMPLETE!${NC}"
echo "========================================================"
echo ""
echo "Application Details:"
echo "  - PID: $NEW_PID"
echo "  - WAR: $APP_DIR/$WAR_NAME"
echo "  - Log: $LOG_FILE"
echo ""
echo "To check application status:"
echo "  tail -f $LOG_FILE"
echo ""
echo "To check if application is running:"
echo "  ps -p $NEW_PID"
echo ""
echo "To stop application:"
echo "  kill $NEW_PID"
echo ""
echo "========================================================"
echo "  TESTING THE FIX"
echo "========================================================"
echo ""
echo "Wait 30-60 seconds for the application to start, then:"
echo ""
echo "1. For TEST store (duplicate username issue):"
echo "   curl -X POST 'https://celebrations.tanishq.co.in/events/changePassword?storeCode=TEST&oldPassword=Tanishq@123&newPassword=Titan@123'"
echo ""
echo "2. For ABH store (500 error issue):"
echo "   curl -X POST 'https://celebrations.tanishq.co.in/events/changePassword?storeCode=ABH&oldPassword=Tanishq@123&newPassword=T@nishq'"
echo ""
echo "Both should return: {\"status\":true,\"message\":\"Password changed successfully\"}"
echo ""
echo "Monitoring logs for errors..."
sleep 5
tail -20 "$LOG_FILE"

