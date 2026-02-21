#!/bin/bash
# =====================================================
# Quick Production Deployment Script
# Run this on Production Server (10.10.63.97) after uploading files
# =====================================================

set -e  # Exit on error

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=========================================="
echo "Tanishq Production Deployment"
echo "Server: 10.10.63.97"
echo "MySQL: 8.4.7 Enterprise"
echo "==========================================${NC}"
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}Please run as root${NC}"
    exit 1
fi

# Step 1: Verify MySQL is running
echo -e "${YELLOW}[1/7] Checking MySQL Server...${NC}"
if systemctl is-active --quiet mysqld; then
    echo -e "${GREEN}✓ MySQL is running${NC}"
    mysql --version
else
    echo -e "${RED}✗ MySQL is not running!${NC}"
    echo "Start MySQL: systemctl start mysqld"
    exit 1
fi
echo ""

# Step 2: Check if WAR file exists
echo -e "${YELLOW}[2/7] Checking application files...${NC}"
if [ ! -f "/opt/tanishq/tanishq-prod.war" ]; then
    echo -e "${RED}✗ WAR file not found at /opt/tanishq/tanishq-prod.war${NC}"
    echo "Please upload the WAR file first"
    exit 1
fi
echo -e "${GREEN}✓ WAR file found${NC}"

# Check for P12 files
if [ -f "/opt/tanishq/tanishqgmb-5437243a8085.p12" ]; then
    echo -e "${GREEN}✓ tanishqgmb P12 file found${NC}"
else
    echo -e "${YELLOW}⚠ tanishqgmb P12 file not found${NC}"
fi

if [ -f "/opt/tanishq/event-images-469618-32e65f6d62b3.p12" ]; then
    echo -e "${GREEN}✓ event-images P12 file found${NC}"
else
    echo -e "${YELLOW}⚠ event-images P12 file not found${NC}"
fi
echo ""

# Step 3: Setup directory structure
echo -e "${YELLOW}[3/7] Setting up directory structure...${NC}"
mkdir -p /opt/tanishq/storage/selfie_images
mkdir -p /opt/tanishq/storage/bride_uploads
mkdir -p /opt/tanishq/storage/bride_images
mkdir -p /opt/tanishq/logs
mkdir -p /opt/tanishq/backups
echo -e "${GREEN}✓ Directories created${NC}"
echo ""

# Step 4: Setup database
echo -e "${YELLOW}[4/7] Setting up database...${NC}"
echo "This will create the selfie_prod database if it doesn't exist"
echo -e "${BLUE}Please enter MySQL root password:${NC}"

if [ -f "/opt/tanishq/setup_production_database.sql" ]; then
    mysql -u root -p < /opt/tanishq/setup_production_database.sql
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Database setup completed${NC}"
    else
        echo -e "${RED}✗ Database setup failed${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}⚠ Database script not found, creating database manually...${NC}"
    mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo -e "${GREEN}✓ Database created${NC}"
fi
echo ""

# Step 5: Create/Update systemd service
echo -e "${YELLOW}[5/7] Creating systemd service...${NC}"
cat > /etc/systemd/system/tanishq-prod.service << 'EOF'
[Unit]
Description=Tanishq Production Application
After=network.target mysqld.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/tanishq
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/tanishq/tanishq-prod.war
Restart=always
RestartSec=10
StandardOutput=append:/opt/tanishq/logs/application.log
StandardError=append:/opt/tanishq/logs/error.log

[Install]
WantedBy=multi-user.target
EOF
echo -e "${GREEN}✓ Systemd service created${NC}"
echo ""

# Step 6: Set permissions
echo -e "${YELLOW}[6/7] Setting file permissions...${NC}"
chmod 644 /opt/tanishq/tanishq-prod.war
chmod 644 /opt/tanishq/*.p12 2>/dev/null || true
chmod 755 /opt/tanishq/storage
chmod 755 /opt/tanishq/logs
chmod 755 /opt/tanishq/backups
echo -e "${GREEN}✓ Permissions set${NC}"
echo ""

# Step 7: Start application
echo -e "${YELLOW}[7/7] Starting application...${NC}"

# Reload systemd
systemctl daemon-reload

# Enable service
systemctl enable tanishq-prod

# Stop if already running
if systemctl is-active --quiet tanishq-prod; then
    echo "Stopping existing application..."
    systemctl stop tanishq-prod
    sleep 3
fi

# Start application
systemctl start tanishq-prod
sleep 5

# Check status
if systemctl is-active --quiet tanishq-prod; then
    echo -e "${GREEN}✓ Application started successfully!${NC}"
    echo ""

    # Show status
    systemctl status tanishq-prod --no-pager -l | head -20

    echo ""
    echo -e "${GREEN}==========================================${NC}"
    echo -e "${GREEN}Deployment Completed Successfully!${NC}"
    echo -e "${GREEN}==========================================${NC}"
    echo ""
    echo -e "${BLUE}Next Steps:${NC}"
    echo "1. Monitor logs: tail -f /opt/tanishq/logs/application.log"
    echo "2. Check if port 3001 is listening: ss -tlnp | grep 3001"
    echo "3. Verify database tables: mysql -u root -p -e 'USE selfie_prod; SHOW TABLES;'"
    echo "4. Test application: curl http://localhost:3001/"
    echo ""
    echo -e "${BLUE}Useful Commands:${NC}"
    echo "• Check status: systemctl status tanishq-prod"
    echo "• View logs: journalctl -u tanishq-prod -f"
    echo "• Restart app: systemctl restart tanishq-prod"
    echo "• Stop app: systemctl stop tanishq-prod"
    echo ""
else
    echo -e "${RED}✗ Application failed to start!${NC}"
    echo ""
    echo "Check logs for errors:"
    echo "• tail -100 /opt/tanishq/logs/error.log"
    echo "• journalctl -u tanishq-prod -n 50 --no-pager"
    echo ""
    exit 1
fi

