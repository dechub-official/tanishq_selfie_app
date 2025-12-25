#!/bin/bash

# EMERGENCY FIX SCRIPT
# Run this on the server: sudo bash emergency-fix.sh

echo "========================================"
echo "Emergency Fix for Tanishq Application"
echo "========================================"
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "[ERROR] Please run as root: sudo bash emergency-fix.sh"
    exit 1
fi

APP_DIR="/opt/tanishq/applications_preprod"
SRC_DIR="$APP_DIR/tanishq_selfie_app"

echo "[Step 1/6] Checking source directory..."
if [ ! -d "$SRC_DIR" ]; then
    echo "[ERROR] Source directory not found: $SRC_DIR"
    echo "Please ensure source code is uploaded to server"
    exit 1
fi

cd $SRC_DIR

echo ""
echo "[Step 2/6] Fixing index.html..."

# Backup current index.html
cp src/main/resources/static/index.html src/main/resources/static/index.html.broken.bak

# Restore from backup if available
if [ -f "src/main/resources/static_backup/index.html" ]; then
    cp src/main/resources/static_backup/index.html src/main/resources/static/index.html
    echo "[OK] Restored index.html from backup"
else
    # Create correct index.html manually
    cat > src/main/resources/static/index.html << 'INDEXEOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <link rel="icon" href="/logo.png" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <meta name="theme-color" content="#000000" />
    <meta name="description" content="Web site created using create-react-app" />
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100;0,300;0,600;0,700;1,300&family=Nunito:ital,wght@0,200;0,300;0,400;0,500;0,600;0,700;1,200;1,300;1,700&family=Open+Sans:wght@300;500;600;700&family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;1,100;1,300;1,400;1,500;1,700&family=Sacramento&display=swap" rel="stylesheet" />
    <link rel="apple-touch-icon" href="/logo.png" />
    <script src="https://cdnjs.cloudflare.com/ajax/libs/camanjs/4.1.2/caman.full.min.js" integrity="sha512-JjFeUD2H//RHt+DjVf1BTuy1X5ZPtMl0svQ3RopX641DWoSilJ89LsFGq4Sw/6BSBfULqUW/CfnVopV5CfvRXA==" crossorigin="anonymous"></script>
    <link rel="manifest" href="/manifest.json" />
    <title>Celebrations With Tanishq</title>
    <script defer="defer" src="/static/js/main.69d68b31.js"></script>
    <link href="/static/css/main.39fd591b.css" rel="stylesheet" />
    <script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
    new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
    j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
    'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
    })(window,document,'script','dataLayer','GTM-NW3KTR3');</script>
</head>
<body>
<noscript>You need to enable JavaScript to run this app.</noscript>
<div id="root"></div>
<noscript>
    <iframe src=https://www.googletagmanager.com/ns.html?id=GTM-NW3KTR3 height="0" width="0" style="display:none;visibility:hidden"></iframe>
</noscript>
</body>
</html>
INDEXEOF
    echo "[OK] Created correct index.html"
fi

# Verify the fix
if grep -q "Celebrations With Tanishq" src/main/resources/static/index.html; then
    echo "[OK] index.html has correct title"
else
    echo "[ERROR] index.html still incorrect!"
    exit 1
fi

echo ""
echo "[Step 3/6] Fixing ReactResourceResolver.java..."

# Check if file needs fixing
if grep -q 'requestPath.startsWith(REACT_STATIC_DIR + "/")' src/main/java/com/dechub/tanishq/config/ReactResourceResolver.java; then
    echo "[OK] ReactResourceResolver.java already fixed"
else
    echo "[WARNING] ReactResourceResolver.java needs manual fix"
    echo "Please upload the fixed version from Windows machine"
fi

echo ""
echo "[Step 4/6] Building application..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "[ERROR] Build failed! Check Maven output above"
    exit 1
fi

echo ""
echo "[Step 5/6] Deploying WAR file..."

# Copy WAR to deployment directory
WAR_FILE=$(ls -t target/*.war 2>/dev/null | grep -v "\.original" | head -1)
if [ -z "$WAR_FILE" ]; then
    echo "[ERROR] WAR file not found in target/"
    exit 1
fi

cp "$WAR_FILE" "$APP_DIR/"
NEW_WAR="$APP_DIR/$(basename $WAR_FILE)"
echo "[OK] Copied: $NEW_WAR"

echo ""
echo "[Step 6/6] Starting application..."

cd $APP_DIR

# Stop any existing process
EXISTING_PID=$(ps -ef | grep "tanishq.*\.war" | grep -v grep | awk '{print $2}')
if [ -n "$EXISTING_PID" ]; then
    echo "[INFO] Stopping existing process: $EXISTING_PID"
    kill -9 $EXISTING_PID
    sleep 2
fi

# Start new version
WAR_NAME=$(basename $NEW_WAR)
nohup java -jar \
    -Dserver.port=3000 \
    -Dspring.profiles.active=preprod \
    "$WAR_NAME" \
    > application.log 2>&1 &

echo "[OK] Application started"
echo ""

# Wait for startup
echo "Waiting for application to start..."
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
echo "========================================"
echo "Deployment Complete!"
echo "========================================"
echo ""

# Show status
echo "Application Status:"
ps -ef | grep "tanishq.*\.war" | grep -v grep

echo ""
echo "Recent Logs:"
tail -20 application.log

echo ""
echo "Checking for errors..."
ERRORS=$(tail -100 application.log | grep -i "filenotfound" | wc -l)
if [ $ERRORS -eq 0 ]; then
    echo "[SUCCESS] No FileNotFound errors! ✓"
else
    echo "[WARNING] Found $ERRORS FileNotFound errors"
    tail -100 application.log | grep -i "filenotfound" | head -5
fi

echo ""
echo "Test URLs:"
echo "  - Main App:    http://10.160.128.94:3000/selfie"
echo "  - Events:      http://10.160.128.94:3000/events"
echo "  - Landing:     http://10.160.128.94:3000/"
echo ""
echo "Monitor logs:  tail -f application.log"
echo ""

