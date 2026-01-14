#!/bin/bash
# PRODUCTION SERVER DIAGNOSTIC SCRIPT
# Run this on your production server to gather information
# Date: January 12, 2026

echo "=========================================="
echo "PRODUCTION SERVER DIAGNOSTIC"
echo "=========================================="
echo ""

echo "1. HOSTNAME AND IP INFORMATION"
echo "----------------------------"
hostname
hostname -I
echo ""

echo "2. CURRENT DIRECTORY: /applications_one"
echo "----------------------------"
cd /applications_one 2>/dev/null && pwd && ls -lah || echo "Directory /applications_one does not exist"
echo ""

echo "3. CURRENT DIRECTORY: /opt/tanishq"
echo "----------------------------"
cd /opt/tanishq 2>/dev/null && pwd && ls -lah || echo "Directory /opt/tanishq does not exist"
echo ""

echo "4. RUNNING JAVA PROCESSES"
echo "----------------------------"
ps -ef | grep java | grep -v grep
echo ""

echo "5. PORTS IN USE BY JAVA"
echo "----------------------------"
netstat -tulpn 2>/dev/null | grep java || ss -tulpn | grep java
echo ""

echo "6. MYSQL DATABASES"
echo "----------------------------"
mysql -u root -pDechub#2025 -e "SHOW DATABASES;" 2>/dev/null | grep selfie || echo "Could not connect to MySQL or no selfie databases found"
echo ""

echo "7. SYSTEMD SERVICES"
echo "----------------------------"
ls -la /etc/systemd/system/tanishq*.service 2>/dev/null || echo "No tanishq systemd services found"
echo ""

echo "8. DISK SPACE"
echo "----------------------------"
df -h | grep -E "Filesystem|/dev/"
echo ""

echo "9. MEMORY USAGE"
echo "----------------------------"
free -h
echo ""

echo "10. CURRENT USER"
echo "----------------------------"
whoami
echo ""

echo "=========================================="
echo "DIAGNOSTIC COMPLETE"
echo "=========================================="
echo ""
echo "Please share this output for analysis."

