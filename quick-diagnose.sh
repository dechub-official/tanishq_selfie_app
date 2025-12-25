#!/bin/bash

# Quick Diagnostic Script for Tanishq Celebration Site Issue
# Run this on the server: bash quick-diagnose.sh

echo "========================================"
echo "TANISHQ CELEBRATION SITE - QUICK DIAGNOSTIC"
echo "Date: $(date)"
echo "========================================"

echo ""
echo "1. Checking if Java application is running..."
echo "----------------------------------------"
if ps aux | grep -v grep | grep "tanishq.*\.war" > /dev/null; then
    echo "✅ Application process found:"
    ps aux | grep -v grep | grep "tanishq.*\.war" | awk '{print "   PID: "$2" | Command: "$11" "$12" "$13}'
else
    echo "❌ NO APPLICATION PROCESS RUNNING!"
    echo "   Action: Need to start the application"
fi

echo ""
echo "2. Checking if port 3000 is listening..."
echo "----------------------------------------"
if netstat -tuln | grep ":3000 " > /dev/null 2>&1 || ss -tuln | grep ":3000 " > /dev/null 2>&1; then
    echo "✅ Port 3000 is listening"
    netstat -tuln 2>/dev/null | grep ":3000 " || ss -tuln | grep ":3000 "
else
    echo "❌ PORT 3000 IS NOT LISTENING!"
    echo "   Action: Application may not be running or using different port"
fi

echo ""
echo "3. Testing localhost access..."
echo "----------------------------------------"
response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/events.html 2>&1)
if [ "$response" = "200" ]; then
    echo "✅ Application responds on localhost (HTTP 200)"
elif [ "$response" = "302" ] || [ "$response" = "301" ]; then
    echo "⚠️  Application redirects (HTTP $response)"
else
    echo "❌ Application not responding on localhost (HTTP $response)"
    echo "   Action: Check application logs"
fi

echo ""
echo "4. Testing events customer endpoint..."
echo "----------------------------------------"
response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/events/customer/TEST 2>&1)
if [ "$response" = "200" ]; then
    echo "✅ Customer endpoint responds (HTTP 200)"
else
    echo "❌ Customer endpoint issue (HTTP $response)"
fi

echo ""
echo "5. Checking Nginx status..."
echo "----------------------------------------"
if systemctl is-active --quiet nginx 2>/dev/null; then
    echo "✅ Nginx is running"
    nginx -v 2>&1 | head -1
elif systemctl is-active --quiet httpd 2>/dev/null; then
    echo "✅ Apache is running"
else
    echo "⚠️  No web server found (nginx/apache)"
fi

echo ""
echo "6. Checking Nginx configuration..."
echo "----------------------------------------"
if command -v nginx > /dev/null; then
    if nginx -t 2>&1 | grep -q "successful"; then
        echo "✅ Nginx configuration is valid"
    else
        echo "❌ Nginx configuration has errors:"
        nginx -t 2>&1
    fi

    # Check if celebration site is configured
    if find /etc/nginx -name "*celebration*" 2>/dev/null | grep -q .; then
        echo "✅ Celebration site config found:"
        find /etc/nginx -name "*celebration*" 2>/dev/null
    else
        echo "⚠️  No celebration site config found in /etc/nginx/"
    fi
else
    echo "⚠️  Nginx not found"
fi

echo ""
echo "7. Checking DNS resolution..."
echo "----------------------------------------"
if command -v nslookup > /dev/null; then
    echo "Resolving: celebrationsite-preprod.tanishq.co.in"
    nslookup celebrationsite-preprod.tanishq.co.in 2>&1 | grep -A2 "Name:" || echo "❌ DNS not resolving"
else
    echo "⚠️  nslookup not available"
fi

echo ""
echo "8. Checking firewall (iptables)..."
echo "----------------------------------------"
if command -v iptables > /dev/null; then
    if iptables -L -n 2>/dev/null | grep -q "3000"; then
        echo "✅ Firewall rule found for port 3000"
    else
        echo "⚠️  No specific firewall rule for port 3000 (may use default allow)"
    fi
else
    echo "⚠️  iptables not available"
fi

echo ""
echo "9. Checking application logs (last 10 lines)..."
echo "----------------------------------------"
if [ -f "/opt/tanishq/applications_preprod/application.log" ]; then
    echo "Last 10 lines of application.log:"
    tail -10 /opt/tanishq/applications_preprod/application.log
elif [ -f "application.log" ]; then
    echo "Last 10 lines of application.log:"
    tail -10 application.log
else
    echo "⚠️  Log file not found at /opt/tanishq/applications_preprod/application.log"
    echo "Searching for log files..."
    find /opt/tanishq -name "*.log" 2>/dev/null | head -5
fi

echo ""
echo "10. Summary & Recommended Actions..."
echo "----------------------------------------"

# Check critical issues
if ! ps aux | grep -v grep | grep "tanishq.*\.war" > /dev/null; then
    echo "🚨 CRITICAL: Application is NOT RUNNING"
    echo "   → Start application: cd /opt/tanishq/applications_preprod && nohup java -jar tanishq-*.war &"
fi

if ! netstat -tuln 2>/dev/null | grep ":3000 " > /dev/null && ! ss -tuln 2>/dev/null | grep ":3000 " > /dev/null; then
    echo "🚨 CRITICAL: Port 3000 is NOT LISTENING"
    echo "   → Check if application started successfully"
    echo "   → Check logs for errors"
fi

response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/events.html 2>&1)
if [ "$response" != "200" ]; then
    echo "🚨 CRITICAL: Application not responding locally"
    echo "   → Check application health"
    echo "   → Review startup logs"
fi

if ! systemctl is-active --quiet nginx 2>/dev/null && ! systemctl is-active --quiet httpd 2>/dev/null; then
    echo "⚠️  WARNING: No reverse proxy detected"
    echo "   → Need nginx or apache to serve HTTPS"
fi

echo ""
echo "========================================"
echo "Diagnostic Complete!"
echo "========================================"
echo ""
echo "Next steps:"
echo "1. Fix any CRITICAL issues above"
echo "2. If app not running, start it"
echo "3. If app running but not accessible, check nginx config"
echo "4. Share this output if you need help"

