#!/bin/bash

###############################################################################
# Tanishq Pre-Production Deployment Script
# Server: ip-10-160-128-94
# Date: 2026-02-07
###############################################################################

# Configuration
APP_NAME="tanishq-preprod-11-02-2026-1-0.0.1-SNAPSHOT.war"
DEPLOY_DIR="/opt/tanishq/applications_preprod"
DB_URL="jdbc:mysql://localhost:3306/selfie_preprod"
DB_USER="root"
DB_PASS="Dechub#2025"
PORT="3000"
PROFILE="preprod"

echo "=========================================="
echo "Tanishq Pre-Production Deployment"
echo "=========================================="
echo "Application: $APP_NAME"
echo "Profile: $PROFILE"
echo "Port: $PORT"
echo "=========================================="

# Navigate to deployment directory
cd $DEPLOY_DIR || { echo "Failed to navigate to $DEPLOY_DIR"; exit 1; }

# Create logs directory if it doesn't exist
echo "Creating logs directory..."
mkdir -p logs

# Stop existing application
echo "Stopping existing application..."
OLD_PID=$(ps aux | grep "java -jar tanishq" | grep -v grep | awk '{print $2}')
if [ ! -z "$OLD_PID" ]; then
    echo "Found running process: $OLD_PID"
    kill $OLD_PID
    echo "Sent kill signal to process: $OLD_PID"

    # Wait for graceful shutdown
    echo "Waiting for graceful shutdown..."
    sleep 10

    # Check if still running
    STILL_RUNNING=$(ps aux | grep "java -jar tanishq" | grep -v grep | awk '{print $2}')
    if [ ! -z "$STILL_RUNNING" ]; then
        echo "Process still running. Force killing..."
        kill -9 $STILL_RUNNING
        sleep 2
    fi
    echo "✓ Old application stopped"
else
    echo "No running application found"
fi

# Verify stopped
VERIFY=$(ps aux | grep "java -jar tanishq" | grep -v grep)
if [ ! -z "$VERIFY" ]; then
    echo "✗ ERROR: Failed to stop old application"
    exit 1
fi

# Check if WAR file exists
if [ ! -f "$APP_NAME" ]; then
    echo "✗ ERROR: WAR file not found: $APP_NAME"
    echo "Available WAR files:"
    ls -lh *.war | tail -5
    exit 1
fi

# Backup old logs
if [ -f "logs/application.log" ]; then
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    echo "Backing up old logs to logs/application.log.$TIMESTAMP"
    mv logs/application.log logs/application.log.$TIMESTAMP
fi

# Start new application
echo "=========================================="
echo "Starting new application..."
echo "=========================================="

nohup java -jar $APP_NAME \
  --spring.profiles.active=$PROFILE \
  --spring.datasource.url=$DB_URL \
  --spring.datasource.username=$DB_USER \
  --spring.datasource.password=$DB_PASS \
  --server.port=$PORT \
  > logs/application.log 2>&1 &

NEW_PID=$!
echo "Application started with PID: $NEW_PID"

# Wait and verify startup
echo "Waiting for application to start..."
sleep 5

if ps -p $NEW_PID > /dev/null 2>&1; then
    echo "=========================================="
    echo "✓ Application is running (PID: $NEW_PID)"
    echo "=========================================="
    echo ""
    echo "Useful commands:"
    echo "  Monitor logs: tail -f $DEPLOY_DIR/logs/application.log"
    echo "  Check status: ps aux | grep tanishq"
    echo "  Stop app: kill $NEW_PID"
    echo "  Test endpoint: curl http://localhost:$PORT"
    echo ""
    echo "Showing last 20 lines of log:"
    echo "=========================================="
    tail -20 logs/application.log
else
    echo "=========================================="
    echo "✗ ERROR: Application failed to start"
    echo "=========================================="
    echo "Checking logs for errors..."
    tail -50 logs/application.log
    exit 1
fi

