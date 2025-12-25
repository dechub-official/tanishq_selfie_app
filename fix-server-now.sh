#!/bin/bash
# QUICK FIX - Run this on the server to fix events.html without rebuilding

echo "=========================================="
echo "QR Code Form Fix - Direct Server Fix"
echo "=========================================="
echo ""

cd /opt/tanishq/applications_preprod

echo "Step 1: Stop the application..."
PID=$(ps -ef | grep tanishq-preprod | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "Stopping process: $PID"
    kill $PID
    sleep 3
fi

echo ""
echo "Step 2: Extract the WAR file..."
mkdir -p temp_fix
cd temp_fix
jar -xvf ../tanishq-preprod-18-12-2025-1-0.0.1-SNAPSHOT.war

echo ""
echo "Step 3: Check what JavaScript files exist..."
echo "Available JS files:"
ls -la BOOT-INF/classes/static/assets/index-*.js

echo ""
echo "Step 4: Check current events.html references..."
grep -o 'index-[^"]*\.js' BOOT-INF/classes/static/events.html

echo ""
echo "Step 5: Creating backup..."
cp BOOT-INF/classes/static/events.html BOOT-INF/classes/static/events.html.backup

echo ""
echo "Step 6: Fixing events.html..."

# Get the actual JS filename
ACTUAL_JS=$(ls BOOT-INF/classes/static/assets/index-*.js | head -1 | xargs basename)
ACTUAL_CSS=$(ls BOOT-INF/classes/static/assets/index-*.css | head -1 | xargs basename)

echo "Found JS file: $ACTUAL_JS"
echo "Found CSS file: $ACTUAL_CSS"

# Update events.html with correct file references
sed -i "s|index-[^\"]*\.js|$ACTUAL_JS|g" BOOT-INF/classes/static/events.html
sed -i "s|index-[^\"]*\.css|$ACTUAL_CSS|g" BOOT-INF/classes/static/events.html

echo ""
echo "Step 7: Verify the fix..."
echo "New references:"
grep -o 'index-[^"]*\.js' BOOT-INF/classes/static/events.html
grep -o 'index-[^"]*\.css' BOOT-INF/classes/static/events.html

echo ""
echo "Step 8: Repackage the WAR file..."
jar -cvf ../tanishq-preprod-FIXED-$(date +%Y%m%d_%H%M%S).war .

cd ..
echo ""
echo "Step 9: Create symbolic link to fixed WAR..."
ln -sf tanishq-preprod-FIXED-*.war tanishq-preprod-current.war

echo ""
echo "Step 10: Start the application..."
nohup java -jar tanishq-preprod-current.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

echo ""
echo "Step 11: Wait for startup..."
sleep 10

echo ""
echo "Step 12: Check if application started..."
ps -ef | grep tanishq | grep -v grep

echo ""
echo "Step 13: Check application log..."
tail -50 application.log

echo ""
echo "=========================================="
echo "FIX COMPLETE!"
echo "=========================================="
echo ""
echo "Test now:"
echo "1. Go to: https://celebrationsite-preprod.tanishq.co.in/events"
echo "2. Should see the Events Dashboard (not blank)"
echo "3. Create an event and scan QR code"
echo "4. Form should appear!"
echo ""
echo "If there are errors, check: tail -f application.log"
echo ""

