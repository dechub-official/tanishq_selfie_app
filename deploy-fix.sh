#!/bin/bash

# Event Creation Fix - Deployment Script
# This script rebuilds and deploys the fixed application

echo "================================================"
echo "Event Creation Authorization Fix - Deployment"
echo "================================================"
echo ""

# Configuration
APP_DIR="/opt/tanishq/applications_preprod"
WAR_NAME="tanishq-preprod.war"
BACKUP_DIR="/opt/tanishq/backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo "Step 1: Creating backup of current deployment..."
mkdir -p "$BACKUP_DIR"
if [ -f "$APP_DIR/$WAR_NAME" ]; then
    cp "$APP_DIR/$WAR_NAME" "$BACKUP_DIR/${WAR_NAME}.backup_${TIMESTAMP}"
    echo "✓ Backup created: $BACKUP_DIR/${WAR_NAME}.backup_${TIMESTAMP}"
else
    echo "⚠ No existing WAR file found to backup"
fi

echo ""
echo "Step 2: Building updated application..."
cd /path/to/tanishq_selfie_app_clean
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "✗ Build failed! Please check Maven output for errors."
    exit 1
fi

echo "✓ Build successful!"

echo ""
echo "Step 3: Stopping application server..."
# Adjust this command based on your application server (Tomcat, Wildfly, etc.)
systemctl stop tomcat

echo "✓ Application server stopped"

echo ""
echo "Step 4: Deploying new WAR file..."
cp target/*.war "$APP_DIR/$WAR_NAME"

if [ $? -ne 0 ]; then
    echo "✗ Deployment failed! Restoring backup..."
    cp "$BACKUP_DIR/${WAR_NAME}.backup_${TIMESTAMP}" "$APP_DIR/$WAR_NAME"
    systemctl start tomcat
    exit 1
fi

echo "✓ WAR file deployed"

echo ""
echo "Step 5: Starting application server..."
systemctl start tomcat

echo "✓ Application server started"

echo ""
echo "Step 6: Waiting for application to start..."
sleep 10

echo ""
echo "Step 7: Checking application health..."
curl -s http://localhost:3000/events/health > /dev/null
if [ $? -eq 0 ]; then
    echo "✓ Application is responding"
else
    echo "⚠ Application may still be starting up. Check logs at: /opt/tanishq/applications_preprod/application.log"
fi

echo ""
echo "================================================"
echo "Deployment completed!"
echo "================================================"
echo ""
echo "Next steps:"
echo "1. Monitor logs: tail -f /opt/tanishq/applications_preprod/application.log"
echo "2. Test event creation by logging in and creating a new event"
echo "3. Verify logs show: 'Using authenticated user as store code for event creation'"
echo ""
echo "If issues occur, restore backup with:"
echo "  cp $BACKUP_DIR/${WAR_NAME}.backup_${TIMESTAMP} $APP_DIR/$WAR_NAME"
echo "  systemctl restart tomcat"
echo ""

