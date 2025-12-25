#!/bin/bash

# IMMEDIATE FIX SCRIPT - Run this ON THE SERVER
# This will verify and fix the static files path issue

echo "=========================================="
echo "QR CODE BLANK PAGE - IMMEDIATE FIX"
echo "=========================================="
echo ""

cd /opt/tanishq/applications_preprod || exit 1

echo "1. Checking current deployment..."
echo "----------------------------------------"

# Check what's in the deployed WAR
WAR_FILE=$(ls -t tanishq-preprod-*.war 2>/dev/null | head -1)

if [ -z "$WAR_FILE" ]; then
    echo "❌ No WAR file found!"
    exit 1
fi

echo "Found: $WAR_FILE"
echo ""

echo "2. Checking events.html paths in deployed WAR..."
echo "----------------------------------------"

unzip -p "$WAR_FILE" WEB-INF/classes/static/events.html | grep -A2 "script.*index-" | head -5

echo ""
echo "3. Checking if static files exist..."
echo "----------------------------------------"

# Check both possible locations
if unzip -l "$WAR_FILE" | grep -q "static/static/assets/index-CLJQELnM.js"; then
    echo "✅ Files found at: static/static/assets/"
    CORRECT_PATH="/static/static/assets/"
elif unzip -l "$WAR_FILE" | grep -q "static/assets/index-CLJQELnM.js"; then
    echo "✅ Files found at: static/assets/"
    CORRECT_PATH="/static/assets/"
else
    echo "❌ JavaScript files NOT FOUND in WAR!"
    echo "Listing static directory contents:"
    unzip -l "$WAR_FILE" | grep "static.*index.*\.js"
fi

echo ""
echo "4. Testing if application is responding..."
echo "----------------------------------------"

response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/events.html 2>&1)
if [ "$response" = "200" ]; then
    echo "✅ Application responds: HTTP 200"
else
    echo "❌ Application issue: HTTP $response"
fi

echo ""
echo "5. Testing static file access..."
echo "----------------------------------------"

# Test both paths
echo "Testing: /static/assets/index-CLJQELnM.js"
response1=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/static/assets/index-CLJQELnM.js 2>&1)
echo "Result: HTTP $response1"

echo "Testing: /static/static/assets/index-CLJQELnM.js"
response2=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/static/static/assets/index-CLJQELnM.js 2>&1)
echo "Result: HTTP $response2"

echo ""
echo "=========================================="
echo "DIAGNOSIS:"
echo "=========================================="

if [ "$response2" = "200" ]; then
    echo "✅ Files ARE accessible at: /static/static/assets/"
    echo "⚠️  BUT events.html might reference: /static/assets/"
    echo ""
    echo "SOLUTION:"
    echo "The WAR needs to be rebuilt with corrected events.html"
    echo ""
    echo "On your Windows machine, run:"
    echo "  cd c:\\JAVA\\celebration-preprod-latest\\celeb\\tanishq_selfie_app"
    echo "  mvn clean package -DskipTests"
    echo ""
    echo "Then upload the new WAR and restart."
elif [ "$response1" = "200" ]; then
    echo "✅ Files ARE accessible at: /static/assets/"
    echo "⚠️  events.html might reference: /static/static/assets/"
    echo ""
    echo "SOLUTION:"
    echo "Revert the events.html change and rebuild"
else
    echo "❌ Files NOT accessible at either location!"
    echo ""
    echo "Possible causes:"
    echo "1. Files not included in WAR build"
    echo "2. Wrong file names in WAR"
    echo "3. Static resource handling issue"
    echo ""
    echo "Check what's actually in the WAR:"
    unzip -l "$WAR_FILE" | grep "index.*\.js$" | tail -10
fi

echo ""
echo "=========================================="
echo "QUICK TEST FROM BROWSER:"
echo "=========================================="
echo ""
echo "Open browser console (F12) and go to:"
echo "https://celebrationsite-preprod.tanishq.co.in/events/customer/TEST_25302712-2ea6-4706-bb30-d8c118693608"
echo ""
echo "Check Network tab for:"
echo "- Which .js file is being requested"
echo "- What HTTP status code it returns (404 = not found)"
echo ""
echo "Share the exact URL that's failing and the HTTP status."

