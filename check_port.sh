#!/bin/bash

# Simple Port Detection Script for Preprod
# Run this on your server to find which port the application is using

echo "=================================================="
echo "PREPROD APPLICATION PORT CHECK"
echo "Server: $(hostname)"
echo "Date: $(date)"
echo "=================================================="
echo ""

# Check process arguments
echo "1. Checking Java process for port configuration..."
echo "─────────────────────────────────────────────────"
PORT=$(ps aux | grep "[j]ava.*tanishq" | grep -o '\-\-server.port=[0-9]*' | cut -d= -f2)
if [ -n "$PORT" ]; then
    echo "✅ Application configured to run on port: $PORT"
else
    echo "⚠️  Could not find port in process arguments"
    echo "   Checking default from config file..."
    PORT=3002
    echo "   Expected port from config: $PORT"
fi
echo ""

# Check if port is actually listening
echo "2. Checking if port $PORT is listening..."
echo "─────────────────────────────────────────────────"
if command -v netstat >/dev/null 2>&1; then
    if sudo netstat -tlnp 2>/dev/null | grep ":$PORT" | grep -q "java"; then
        echo "✅ Port $PORT is LISTENING (Java process)"
        sudo netstat -tlnp | grep ":$PORT" | grep java
    else
        # Try without sudo
        if netstat -tln 2>/dev/null | grep -q ":$PORT.*LISTEN"; then
            echo "✅ Port $PORT is LISTENING"
        else
            echo "❌ Port $PORT is NOT listening"
        fi
    fi
else
    echo "⚠️  netstat not available, trying ss command..."
    if command -v ss >/dev/null 2>&1; then
        if sudo ss -tlnp 2>/dev/null | grep ":$PORT" | grep -q "java"; then
            echo "✅ Port $PORT is LISTENING (Java process)"
        else
            echo "❌ Port $PORT is NOT listening"
        fi
    fi
fi
echo ""

# Test HTTP response
echo "3. Testing HTTP response on port $PORT..."
echo "─────────────────────────────────────────────────"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT --connect-timeout 5 2>/dev/null)
if [ "$RESPONSE" = "200" ] || [ "$RESPONSE" = "302" ] || [ "$RESPONSE" = "401" ]; then
    echo "✅ HTTP Response: $RESPONSE (Application is responding)"
else
    echo "⚠️  HTTP Response: $RESPONSE (Check if application is running)"
fi
echo ""

# Check all Java listening ports
echo "4. All ports Java is listening on..."
echo "─────────────────────────────────────────────────"
if command -v netstat >/dev/null 2>&1; then
    sudo netstat -tlnp 2>/dev/null | grep java | awk '{print $4}' | while read addr; do
        port=$(echo $addr | cut -d: -f2)
        echo "   Port: $port"
    done
else
    echo "   Run: sudo netstat -tlnp | grep java"
fi
echo ""

# Summary
echo "=================================================="
echo "SUMMARY"
echo "=================================================="
echo ""
echo "Application Port: $PORT"
echo ""
echo "Access URLs:"
echo "  • Local:  http://localhost:$PORT"
echo "  • Server: http://10.160.128.94:$PORT"
echo "  • Public: http://celebrationsite-preprod.tanishq.co.in"
echo ""
echo "To verify:"
echo "  curl -I http://localhost:$PORT"
echo ""
echo "=================================================="

