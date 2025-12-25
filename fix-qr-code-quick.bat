@echo off
REM =====================================================
REM QUICK FIX: QR Code Attendee Form - Use Direct IP (Windows)
REM =====================================================

echo.
echo 🚀 Fixing QR Code URL for immediate testing...
echo.

REM Backup original file
echo 📦 Creating backup...
copy src\main\resources\application-preprod.properties src\main\resources\application-preprod.properties.backup.%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%

REM Update QR code base URL
echo 🔧 Updating QR code URL to use direct IP...
powershell -Command "(Get-Content src\main\resources\application-preprod.properties) -replace 'qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/', 'qr.code.base.url=http://10.160.128.94:3000/events/customer/' | Set-Content src\main\resources\application-preprod.properties"

echo ✅ Configuration updated!
echo.
echo 📝 Changed:
echo    FROM: https://celebrationsite-preprod.tanishq.co.in/events/customer/
echo    TO:   http://10.160.128.94:3000/events/customer/
echo.

REM Rebuild WAR file
echo 🔨 Rebuilding WAR file...
call mvn clean package -Ppreprod -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Build successful!
    echo.
    echo 📦 WAR file location:
    echo    target\tanishq-preprod-10-12-2025-1-0.0.1-SNAPSHOT.war
    echo.
    echo 🚀 Next steps:
    echo    1. Deploy the new WAR file to server
    echo    2. Restart application
    echo    3. Create a new event
    echo    4. Download QR code
    echo    5. Scan with phone (must be on same network^)
    echo    6. Attendee form should now load!
    echo.
    echo ⚠️  Note: This only works on the same network as the server
    echo    For production, you need proper DNS + Nginx setup
    echo    See QR_CODE_ATTENDEE_FORM_FIX.md for details
) else (
    echo.
    echo ❌ Build failed! Check errors above.
)

echo.
pause
#!/bin/bash

# =====================================================
# QUICK FIX: QR Code Attendee Form - Use Direct IP
# =====================================================
# This script updates the QR code URL to use direct IP
# so you can test the attendee form immediately
# =====================================================

echo "🚀 Fixing QR Code URL for immediate testing..."
echo ""

# Backup original file
echo "📦 Creating backup..."
cp src/main/resources/application-preprod.properties src/main/resources/application-preprod.properties.backup.$(date +%Y%m%d_%H%M%S)

# Update QR code base URL to use IP
echo "🔧 Updating QR code URL to use direct IP..."
sed -i 's|qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/|qr.code.base.url=http://10.160.128.94:3000/events/customer/|g' src/main/resources/application-preprod.properties

echo "✅ Configuration updated!"
echo ""
echo "📝 Changed:"
echo "   FROM: https://celebrationsite-preprod.tanishq.co.in/events/customer/"
echo "   TO:   http://10.160.128.94:3000/events/customer/"
echo ""

# Rebuild WAR file
echo "🔨 Rebuilding WAR file..."
mvn clean package -Ppreprod -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "📦 WAR file location:"
    echo "   target/tanishq-preprod-10-12-2025-1-0.0.1-SNAPSHOT.war"
    echo ""
    echo "🚀 Next steps:"
    echo "   1. Deploy the new WAR file to server"
    echo "   2. Restart application"
    echo "   3. Create a new event"
    echo "   4. Download QR code"
    echo "   5. Scan with phone (must be on same network)"
    echo "   6. Attendee form should now load!"
    echo ""
    echo "⚠️  Note: This only works on the same network as the server"
    echo "   For production, you need proper DNS + Nginx setup"
    echo "   See QR_CODE_ATTENDEE_FORM_FIX.md for details"
else
    echo ""
    echo "❌ Build failed! Check errors above."
fi

