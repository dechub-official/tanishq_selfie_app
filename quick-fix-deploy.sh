#!/bin/bash

# Quick Fix Deployment for Static Files Path Issue
# This rebuilds and redeploys the WAR with the corrected events.html

echo "=========================================="
echo "QUICK FIX: Static Files Path Correction"
echo "=========================================="
echo ""

# Navigate to project directory
cd c:/JAVA/celebration-preprod-latest/celeb/tanishq_selfie_app || exit 1

echo "1. Building the application (this will take a moment)..."
echo "   Command: mvn clean package -DskipTests"
echo ""

mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""

    # Find the new WAR file
    WAR_FILE=$(ls -t target/tanishq-preprod-*.war 2>/dev/null | head -1)

    if [ -n "$WAR_FILE" ]; then
        echo "✅ WAR file created: $WAR_FILE"
        echo ""
        echo "=========================================="
        echo "DEPLOYMENT INSTRUCTIONS:"
        echo "=========================================="
        echo ""
        echo "2. Copy the new WAR to your server:"
        echo "   scp $WAR_FILE user@10.160.128.94:/opt/tanishq/applications_preprod/"
        echo ""
        echo "3. On the server, stop the current application:"
        echo "   kill 355542  # (or whatever PID is currently running)"
        echo ""
        echo "4. Start the new version:"
        echo "   cd /opt/tanishq/applications_preprod"
        echo "   nohup java -jar tanishq-preprod-18-12-2025-*.war \\"
        echo "     --spring.profiles.active=preprod \\"
        echo "     --server.port=3000 \\"
        echo "     > application.log 2>&1 &"
        echo ""
        echo "5. Wait 30 seconds, then test:"
        echo "   curl http://localhost:3000/static/static/assets/index-CLJQELnM.js"
        echo "   # Should return JavaScript code (not 404)"
        echo ""
        echo "6. Test in browser:"
        echo "   https://celebrationsite-preprod.tanishq.co.in/events/customer/TEST_25302712-2ea6-4706-bb30-d8c118693608"
        echo "   # Should now show the attendee form (not blank page)!"
        echo ""
        echo "=========================================="
    else
        echo "❌ WAR file not found in target directory"
        exit 1
    fi
else
    echo ""
    echo "❌ Build failed!"
    echo "Check the error messages above"
    exit 1
fi

