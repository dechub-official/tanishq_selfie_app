# Pre-Production Deployment Guide

## Server Details
- **Server:** ip-10-160-128-94
- **User:** jewdev-test
- **Deployment Path:** /opt/tanishq/applications_preprod
- **Port:** 3000
- **Database:** selfie_preprod (localhost:3306)

## Deployment Steps

### 1. Build the WAR file locally
```bash
# From the project root directory
mvn clean package -Ppreprod
```

This will create: `target/tanishq-preprod-07-02-2026-9-0.0.1-SNAPSHOT.war`

### 2. Upload WAR to Server
```bash
# Use SCP or your preferred file transfer method
scp target/tanishq-preprod-07-02-2026-9-0.0.1-SNAPSHOT.war jewdev-test@ip-10-160-128-94:/tmp/
```

### 3. Deploy on Server

Connect to the server and run these commands:

```bash
# SSH to server
ssh jewdev-test@ip-10-160-128-94

# Switch to root
sudo su

# Navigate to deployment directory
cd /opt/tanishq/applications_preprod

# Create logs directory if it doesn't exist
mkdir -p logs

# Move the WAR file from /tmp
mv /tmp/tanishq-preprod-07-02-2026-9-0.0.1-SNAPSHOT.war .

# Find and kill the running application
ps aux | grep tanishq
# Note the PID and kill it
kill <PID>

# Verify it's stopped
ps aux | grep tanishq

# Start the new application
nohup java -jar tanishq-preprod-07-02-2026-9-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=preprod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  > logs/application.log 2>&1 &

# Check if it started
ps aux | grep tanishq

# Monitor logs
tail -f logs/application.log
```

## Quick Deploy Script (Run on Server)

```bash
#!/bin/bash

# Configuration
APP_NAME="tanishq-preprod-07-02-2026-9-0.0.1-SNAPSHOT.war"
DEPLOY_DIR="/opt/tanishq/applications_preprod"
DB_URL="jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false&serverTimezone=UTC"
DB_USER="root"
DB_PASS="Dechub#2025"
PORT="3000"
PROFILE="preprod"

cd $DEPLOY_DIR

# Create logs directory
mkdir -p logs

# Stop existing application
echo "Stopping existing application..."
OLD_PID=$(ps aux | grep "java -jar tanishq" | grep -v grep | awk '{print $2}')
if [ ! -z "$OLD_PID" ]; then
    kill $OLD_PID
    echo "Stopped process: $OLD_PID"
    sleep 5
else
    echo "No running application found"
fi

# Verify stopped
STILL_RUNNING=$(ps aux | grep "java -jar tanishq" | grep -v grep)
if [ ! -z "$STILL_RUNNING" ]; then
    echo "Force killing..."
    kill -9 $OLD_PID
    sleep 2
fi

# Start new application
echo "Starting application: $APP_NAME"
nohup java -jar $APP_NAME \
  --spring.profiles.active=$PROFILE \
  --spring.datasource.url=$DB_URL \
  --spring.datasource.username=$DB_USER \
  --spring.datasource.password=$DB_PASS \
  --server.port=$PORT \
  > logs/application.log 2>&1 &

NEW_PID=$!
echo "Application started with PID: $NEW_PID"

# Wait a moment and check
sleep 3
if ps -p $NEW_PID > /dev/null; then
    echo "✓ Application is running"
    echo "Monitor logs: tail -f logs/application.log"
else
    echo "✗ Application failed to start. Check logs:"
    tail -50 logs/application.log
fi
```

## Important Notes

### Profile Configuration
- Use `--spring.profiles.active=preprod` for pre-production
- Use `--spring.profiles.active=prod` for production
- Make sure the corresponding `application-preprod.properties` or `application-preprod.yml` exists

### Common Issues

1. **"No such file or directory" for logs**
   - Solution: Create logs directory: `mkdir -p logs`

2. **Port already in use**
   - Solution: Make sure old process is killed: `kill -9 <PID>`

3. **Database connection failed**
   - Verify MySQL is running: `systemctl status mysqld`
   - Check database exists: `mysql -u root -p -e "SHOW DATABASES;"`
   - Verify credentials

4. **Application won't start**
   - Check logs: `tail -100 logs/application.log`
   - Check Java version: `java -version` (should be Java 11)
   - Check WAR file integrity: `ls -lh *.war`

### Monitoring

```bash
# Check if application is running
ps aux | grep tanishq

# Monitor logs in real-time
tail -f logs/application.log

# Check application health (if health endpoint exists)
curl http://localhost:3000/actuator/health

# Check port is listening
netstat -tlnp | grep 3000
```

### Rollback

If the new deployment fails, rollback to previous version:

```bash
# Kill new process
kill $(ps aux | grep "tanishq-preprod-07-02-2026-9" | grep -v grep | awk '{print $2}')

# Start previous version (example)
nohup java -jar tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=preprod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  > logs/application.log 2>&1 &
```

## Fix Your Current Issue

Right now, run these commands on the server:

```bash
cd /opt/tanishq/applications_preprod
mkdir -p logs
nohup java -jar tanishq-preprod-07-02-2026-9-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=preprod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  > logs/application.log 2>&1 &
```

Note: Changed `prod` to `preprod` to match the server environment.

