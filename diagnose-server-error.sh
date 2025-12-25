#!/bin/bash
# Server Diagnosis Script - Run this on the server to check the error

echo "=========================================="
echo "Application Startup Error Diagnosis"
echo "=========================================="
echo ""

echo "1. Checking application.log for errors..."
echo "=========================================="
if [ -f "application.log" ]; then
    echo "Last 50 lines of application.log:"
    echo ""
    tail -50 application.log
    echo ""
    echo "=========================================="
    echo ""

    echo "Looking for ERROR messages:"
    echo ""
    grep -i "error" application.log | tail -20
    echo ""
    echo "=========================================="
    echo ""

    echo "Looking for Exception messages:"
    echo ""
    grep -i "exception" application.log | tail -20
    echo ""
else
    echo "[ERROR] application.log not found!"
fi

echo "=========================================="
echo "2. Checking WAR file..."
echo "=========================================="
ls -lh *.war
echo ""

echo "=========================================="
echo "3. Checking Java version..."
echo "=========================================="
java -version
echo ""

echo "=========================================="
echo "4. Checking database connection..."
echo "=========================================="
mysql -u root -p'Dechub#2025' -e "USE selfie_preprod; SHOW TABLES;" 2>&1 | head -10
echo ""

echo "=========================================="
echo "5. Checking port 3000..."
echo "=========================================="
netstat -tuln | grep 3000
echo ""

echo "=========================================="
echo "6. Checking if events.html is in the WAR..."
echo "=========================================="
jar -tf tanishq-preprod-18-12-2025-2-0.0.1-SNAPSHOT.war | grep events.html
echo ""

echo "=========================================="
echo "7. Extracting events.html to check content..."
echo "=========================================="
unzip -p tanishq-preprod-18-12-2025-2-0.0.1-SNAPSHOT.war BOOT-INF/classes/static/events.html 2>/dev/null | grep -o 'index-[^"]*\.js'
echo ""

echo "=========================================="
echo "Diagnosis complete!"
echo "=========================================="

