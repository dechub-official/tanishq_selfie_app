#!/bin/bash

# Pre-Prod Deployment Script for Server
# Server: 10.160.128.94
# Run this script on the server after uploading WAR file

echo "========================================"
echo "Tanishq Pre-Prod Deployment Script"
echo "========================================"
echo ""

# Configuration
APP_DIR="/opt/tanishq/applications_preprod"
PROFILE="preprod"

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run as root (sudo su root)"
    exit 1
fi

cd $APP_DIR

# Find the latest WAR file
LATEST_WAR=$(ls -t tanishq-*.war 2>/dev/null | head -1)

if [ -z "$LATEST_WAR" ]; then
    echo "[ERROR] No WAR file found in $APP_DIR"
    echo "Please upload WAR file first using WinSCP"
    exit 1
fi

echo "[INFO] Found WAR file: $LATEST_WAR"
echo ""

# Find and stop existing process
echo "Step 1: Stopping existing process..."
EXISTING_PID=$(ps -ef | grep tanishq | grep -v grep | awk '{print $2}')

if [ -n "$EXISTING_PID" ]; then
    echo "[INFO] Found existing process: $EXISTING_PID"
    kill $EXISTING_PID
    sleep 3

    # Force kill if still running
    if ps -p $EXISTING_PID > /dev/null; then
        echo "[WARNING] Process still running, force killing..."
        kill -9 $EXISTING_PID
        sleep 2
    fi

    echo "[OK] Existing process stopped"
else
    echo "[INFO] No existing process found"
fi
echo ""

# Start new process
echo "Step 2: Starting new application..."
LOG_FILE="${LATEST_WAR%.war}.log"

nohup java -jar $LATEST_WAR --spring.profiles.active=$PROFILE > $LOG_FILE 2>&1 &
NEW_PID=$!

echo "[OK] Started new process with PID: $NEW_PID"
echo "[INFO] Log file: $LOG_FILE"
echo ""

# Wait for startup
echo "Step 3: Waiting for application to start..."
sleep 5

# Check if process is still running
if ps -p $NEW_PID > /dev/null; then
    echo "[OK] Process is running"
else
    echo "[ERROR] Process died! Check logs:"
    tail -50 $LOG_FILE
    exit 1
fi
echo ""

# Monitor logs for startup
echo "Step 4: Monitoring startup logs (30 seconds)..."
echo "Press Ctrl+C to stop monitoring (application will continue running)"
echo "----------------------------------------"

timeout 30 tail -f $LOG_FILE &
sleep 30

echo ""
echo "========================================"
echo "Deployment Status"
echo "========================================"

# Check if application started successfully
if ps -p $NEW_PID > /dev/null; then
    echo "[SUCCESS] Application is running!"
    echo ""
    echo "Process ID: $NEW_PID"
    echo "WAR File: $LATEST_WAR"
    echo "Log File: $LOG_FILE"
    echo ""
    echo "Commands:"
    echo "  View logs: tail -f $LOG_FILE"
    echo "  Check process: ps -ef | grep tanishq"
    echo "  Check port: netstat -tulpn | grep 3002"
    echo "  Stop app: kill $NEW_PID"
    echo ""
    echo "Test URLs:"
    echo "  Local: http://localhost:3002"
    echo "  Public: http://celebrations-preprod.tanishq.co.in"
else
    echo "[FAILED] Application failed to start!"
    echo "Check logs: tail -100 $LOG_FILE"
fi

echo "========================================"
echo ""

