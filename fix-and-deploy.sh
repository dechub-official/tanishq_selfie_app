#!/bin/bash

# Quick Fix Deployment Script for Static Assets Issue
# Run this on the server (10.160.128.94) after uploading the fixed source file

echo "=========================================="
echo "Static Assets Path Fix - Deployment"
echo "=========================================="
echo ""

# Configuration
APP_DIR="/opt/tanishq/applications_preprod"
SRC_DIR="$APP_DIR/tanishq_selfie_app"
JAVA_FILE="src/main/java/com/dechub/tanishq/config/ReactResourceResolver.java"

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "[ERROR] Please run as root (sudo su root)"
    exit 1
fi

echo "[1/5] Checking source directory..."
if [ ! -d "$SRC_DIR" ]; then
    echo "[ERROR] Source directory not found: $SRC_DIR"
    echo "Please upload the source code first"
    exit 1
fi

cd $SRC_DIR

echo "[2/5] Verifying the fix in source code..."
if grep -q 'requestPath.startsWith(REACT_STATIC_DIR + "/")' "$JAVA_FILE"; then
    echo "[OK] Fix is present in source code"
else
    echo "[ERROR] Fix not found in source code"
    echo "Please ensure the ReactResourceResolver.java file has been updated"
    exit 1
fi

echo ""
echo "[3/5] Stopping current application..."
EXISTING_PID=$(ps -ef | grep "tanishq.*\.war" | grep -v grep | awk '{print $2}')

if [ -n "$EXISTING_PID" ]; then
    echo "[INFO] Found running process: $EXISTING_PID"
    kill $EXISTING_PID
    sleep 3

    # Force kill if still running
    if ps -p $EXISTING_PID > /dev/null 2>&1; then
        echo "[WARNING] Force killing..."
        kill -9 $EXISTING_PID
        sleep 2
    fi
    echo "[OK] Application stopped"
else
    echo "[INFO] No running application found"
fi

echo ""
echo "[4/5] Building application..."
if command -v mvn &> /dev/null; then
    mvn clean package -DskipTests

    if [ $? -eq 0 ]; then
        echo "[OK] Build successful"
    else
        echo "[ERROR] Build failed"
        exit 1
    fi
else
    echo "[ERROR] Maven not found"
    echo "Install Maven or build on development machine"
    exit 1
fi

echo ""
echo "[5/5] Copying WAR file to deployment directory..."
WAR_FILE=$(ls -t target/*.war 2>/dev/null | grep -v "\.original" | head -1)

if [ -z "$WAR_FILE" ]; then
    echo "[ERROR] WAR file not found in target/"
    exit 1
fi

cp "$WAR_FILE" "$APP_DIR/"
DEPLOYED_WAR="$APP_DIR/$(basename $WAR_FILE)"
echo "[OK] WAR copied to: $DEPLOYED_WAR"

echo ""
echo "=========================================="
echo "Build Complete!"
echo "=========================================="
echo ""
echo "To start the application, run:"
echo "cd $APP_DIR"
echo "nohup java -jar -Dserver.port=3000 -Dspring.profiles.active=preprod $(basename $WAR_FILE) > application.log 2>&1 &"
echo ""
echo "Or use the deployment script:"
echo "./deploy-preprod.sh"
echo ""

