#!/bin/bash

# Tanishq Celebration App - Startup Script
# Run this on the server to start the application

APP_DIR="/opt/tanishq/applications_preprod"
LOG_FILE="$APP_DIR/application.log"
PID_FILE="$APP_DIR/app.pid"

echo "========================================"
echo "Starting Tanishq Celebration Application"
echo "========================================"

# Check if already running
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p $OLD_PID > /dev/null 2>&1; then
        echo "⚠️  Application already running (PID: $OLD_PID)"
        echo "   To restart, first stop it: kill $OLD_PID"
        exit 1
    else
        echo "Removing stale PID file..."
        rm -f "$PID_FILE"
    fi
fi

# Go to app directory
cd "$APP_DIR" || {
    echo "❌ Directory not found: $APP_DIR"
    exit 1
}

# Find the WAR file
WAR_FILE=$(ls -t tanishq-preprod-*.war 2>/dev/null | head -1)

if [ -z "$WAR_FILE" ]; then
    echo "❌ No WAR file found in $APP_DIR"
    echo "   Expected: tanishq-preprod-*.war"
    exit 1
fi

echo "Found WAR file: $WAR_FILE"
echo "Log file: $LOG_FILE"
echo ""

# Start the application
echo "Starting application..."
nohup java -jar "$WAR_FILE" \
    --spring.profiles.active=preprod \
    --server.port=3000 \
    > "$LOG_FILE" 2>&1 &

APP_PID=$!
echo $APP_PID > "$PID_FILE"

echo "✅ Application started!"
echo "   PID: $APP_PID"
echo "   Log: $LOG_FILE"
echo ""

# Wait a moment and check if still running
sleep 3

if ps -p $APP_PID > /dev/null 2>&1; then
    echo "✅ Process is running"
    echo ""
    echo "Waiting for startup (this may take 30-60 seconds)..."
    echo "Watching logs..."
    echo ""

    # Watch logs for startup completion
    timeout 60 tail -f "$LOG_FILE" &
    TAIL_PID=$!

    # Wait for startup message
    for i in {1..60}; do
        if grep -q "Started TanishqApplication" "$LOG_FILE" 2>/dev/null; then
            kill $TAIL_PID 2>/dev/null
            echo ""
            echo "========================================"
            echo "✅ APPLICATION STARTED SUCCESSFULLY!"
            echo "========================================"
            echo ""
            echo "Testing..."

            # Test the endpoint
            sleep 2
            response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/events.html)
            if [ "$response" = "200" ]; then
                echo "✅ Application responds: HTTP $response"
                echo ""
                echo "🎉 Ready to serve requests!"
                echo ""
                echo "URLs:"
                echo "  - http://localhost:3000/events.html"
                echo "  - https://celebrationsite-preprod.tanishq.co.in/events/customer/{eventId}"
            else
                echo "⚠️  Application started but HTTP response: $response"
                echo "   Check logs for issues"
            fi

            echo ""
            echo "To view logs:"
            echo "  tail -f $LOG_FILE"
            echo ""
            echo "To stop:"
            echo "  kill $APP_PID"
            echo "  rm $PID_FILE"

            exit 0
        fi
        sleep 1
    done

    kill $TAIL_PID 2>/dev/null
    echo ""
    echo "⚠️  Application started but startup not confirmed"
    echo "   Check logs: tail -f $LOG_FILE"

else
    echo "❌ Process died immediately!"
    echo "   Check logs: tail -20 $LOG_FILE"
    rm -f "$PID_FILE"
    exit 1
fi

