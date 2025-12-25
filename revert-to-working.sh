#!/bin/bash

# Quick Revert to Last Working Version
# Reverts to: tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war

echo "=========================================="
echo "Reverting to Last Working Version"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "[ERROR] Please run as root: sudo bash revert-to-working.sh"
    exit 1
fi

cd /opt/tanishq/applications_preprod

# The last working version
WORKING_WAR="tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war"

echo "[1/4] Checking if $WORKING_WAR exists..."
if [ ! -f "$WORKING_WAR" ]; then
    echo "[ERROR] $WORKING_WAR not found!"
    echo "Available WAR files:"
    ls -lh *.war 2>/dev/null
    exit 1
fi

echo "[OK] Found: $WORKING_WAR"
echo "Size: $(ls -lh $WORKING_WAR | awk '{print $5}')"
echo "Date: $(ls -lh $WORKING_WAR | awk '{print $6, $7, $8}')"
echo ""

echo "[2/4] Stopping current application..."
EXISTING_PID=$(ps -ef | grep "tanishq.*\.war\|selfie.*\.war" | grep -v grep | awk '{print $2}')

if [ -n "$EXISTING_PID" ]; then
    echo "[INFO] Stopping process: $EXISTING_PID"
    kill -9 $EXISTING_PID
    sleep 2
    echo "[OK] Process stopped"
else
    echo "[INFO] No running process found"
fi

echo ""
echo "[3/4] Starting $WORKING_WAR..."

nohup java -jar \
  -Dserver.port=3000 \
  -Dspring.profiles.active=preprod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  $WORKING_WAR \
  > application.log 2>&1 &

echo "[OK] Application started"
sleep 3

echo ""
echo "[4/4] Checking application status..."

# Wait for startup
for i in {1..30}; do
    if grep -q "Started TanishqSelfieApplication" application.log 2>/dev/null; then
        echo ""
        echo "[SUCCESS] Application started successfully!"
        break
    fi
    echo -n "."
    sleep 1
done

echo ""
echo ""
echo "=========================================="
echo "Revert Complete!"
echo "=========================================="
echo ""

# Show process
echo "Running Process:"
ps -ef | grep "$WORKING_WAR" | grep -v grep

echo ""
echo "Recent Logs:"
tail -20 application.log

echo ""
echo "Checking for errors..."
ERRORS=$(tail -100 application.log | grep -i "error\|exception" | grep -v "INFO" | wc -l)
if [ $ERRORS -eq 0 ]; then
    echo "[SUCCESS] No errors found! ✓"
else
    echo "[WARNING] Found $ERRORS potential errors"
    tail -100 application.log | grep -i "error\|exception" | grep -v "INFO" | head -5
fi

echo ""
echo "Test URLs:"
echo "  - http://10.160.128.94:3000/"
echo "  - http://10.160.128.94:3000/events"
echo "  - http://10.160.128.94:3000/selfie"
echo ""
echo "Monitor logs: tail -f application.log"
echo ""

