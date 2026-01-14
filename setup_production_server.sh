#!/bin/bash
# =====================================================
# Production Server Setup Script
# Automates the creation of directories and basic setup
# =====================================================

set -e  # Exit on any error

echo "=========================================="
echo "Tanishq Production Server Setup"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Create main application directory
echo -e "${YELLOW}Step 1: Creating application directories...${NC}"
mkdir -p /opt/tanishq
mkdir -p /opt/tanishq/storage/selfie_images
mkdir -p /opt/tanishq/storage/bride_uploads
mkdir -p /opt/tanishq/storage/bride_images
mkdir -p /opt/tanishq/logs
mkdir -p /opt/tanishq/backups
echo -e "${GREEN}✓ Directories created${NC}"
echo ""

# Step 2: Set permissions
echo -e "${YELLOW}Step 2: Setting permissions...${NC}"
chown -R root:root /opt/tanishq
chmod -R 755 /opt/tanishq
echo -e "${GREEN}✓ Permissions set${NC}"
echo ""

# Step 3: Verify directory structure
echo -e "${YELLOW}Step 3: Verifying directory structure...${NC}"
ls -la /opt/tanishq/
echo ""

# Step 4: Create database setup SQL script
echo -e "${YELLOW}Step 4: Creating database setup script...${NC}"
cat > /opt/tanishq/setup_production_database.sql << 'EOF'
-- =====================================================
-- PRODUCTION DATABASE SETUP SCRIPT
-- =====================================================

-- Create the production database
CREATE DATABASE IF NOT EXISTS selfie_prod
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Verify database creation
SHOW DATABASES;

-- Select the production database
USE selfie_prod;

-- Verify it's empty
SHOW TABLES;

SELECT 'Production database selfie_prod created successfully!' as Status;
EOF
echo -e "${GREEN}✓ Database setup script created at /opt/tanishq/setup_production_database.sql${NC}"
echo ""

# Step 5: Create backup script
echo -e "${YELLOW}Step 5: Creating backup script...${NC}"
cat > /opt/tanishq/backup_production_database.sh << 'EOF'
#!/bin/bash
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/tanishq/backups"
DB_NAME="selfie_prod"
DB_USER="root"

# Prompt for password or use environment variable
if [ -z "$MYSQL_PASSWORD" ]; then
    echo "Enter MySQL root password:"
    read -s DB_PASS
else
    DB_PASS="$MYSQL_PASSWORD"
fi

mkdir -p $BACKUP_DIR

echo "Starting backup of $DB_NAME database..."

mysqldump -u $DB_USER -p$DB_PASS \
    --databases $DB_NAME \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    > $BACKUP_DIR/selfie_prod_backup_$TIMESTAMP.sql

if [ $? -eq 0 ]; then
    echo "✓ Backup completed: selfie_prod_backup_$TIMESTAMP.sql"

    # Keep only last 7 days of backups
    find $BACKUP_DIR -name "selfie_prod_backup_*.sql" -mtime +7 -delete
    echo "✓ Old backups cleaned up"
else
    echo "✗ Backup failed!"
    exit 1
fi
EOF

chmod +x /opt/tanishq/backup_production_database.sh
echo -e "${GREEN}✓ Backup script created at /opt/tanishq/backup_production_database.sh${NC}"
echo ""

# Step 6: Create systemd service file
echo -e "${YELLOW}Step 6: Creating systemd service file...${NC}"
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
echo -e "${GREEN}✓ Systemd service file created${NC}"
echo ""

# Step 7: Show next steps
echo -e "${GREEN}=========================================="
echo "Setup completed successfully!"
echo "==========================================${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Upload WAR file to: /opt/tanishq/tanishq-prod.war"
echo "2. Upload Google service account key: /opt/tanishq/tanishqgmb-5437243a8085.p12"
echo "3. Upload base.jpg to: /opt/tanishq/storage/base.jpg"
echo "4. Upload Excel file: /opt/tanishq/tanishq_selfie_app_store_data.xlsx"
echo "5. Setup database by running:"
echo "   mysql -u root -p < /opt/tanishq/setup_production_database.sql"
echo "6. Start application:"
echo "   systemctl daemon-reload"
echo "   systemctl enable tanishq-prod"
echo "   systemctl start tanishq-prod"
echo ""
echo -e "${YELLOW}To check status:${NC}"
echo "   systemctl status tanishq-prod"
echo "   tail -f /opt/tanishq/logs/application.log"
echo ""

