#!/bin/bash
# PRODUCTION DEPLOYMENT - Server Commands
# Run these commands on the production server (10.10.63.97)
# Date: January 12, 2026

echo "=========================================="
echo "PRE-DEPLOYMENT CHECKS"
echo "=========================================="

echo -e "\n1. Checking currently running applications..."
ps aux | grep java | grep -v grep

echo -e "\n2. Checking ports in use..."
sudo netstat -tulpn | grep java || sudo ss -tulpn | grep java

echo -e "\n3. Checking current directory structure..."
ls -la /opt/tanishq/

echo -e "\n4. Checking existing WAR files..."
ls -lh /opt/tanishq/*.war 2>/dev/null || echo "No WAR files found yet"

echo -e "\n5. Checking databases..."
mysql -u root -pDechub#2025 -e "SHOW DATABASES;" | grep selfie

echo -e "\n6. Checking open firewall ports..."
sudo firewall-cmd --list-ports

echo "=========================================="
echo "PRE-DEPLOYMENT CHECKS COMPLETE"
echo "=========================================="

echo -e "\n\nNext Steps:"
echo "1. Upload tanishq-prod.war file"
echo "2. Run: sudo firewall-cmd --permanent --add-port=3001/tcp"
echo "3. Run: sudo firewall-cmd --reload"
echo "4. Test: java -jar -Dspring.profiles.active=prod /opt/tanishq/tanishq-prod.war"
echo "5. If successful, create systemd service for production"

