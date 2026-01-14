#!/bin/bash
# =============================================================================
# PRODUCTION PRE-DEPLOYMENT VERIFICATION SCRIPT
# Run this on Production Server (10.10.63.97) BEFORE deploying
# =============================================================================

echo "=============================================="
echo "TANISHQ PRODUCTION PRE-DEPLOYMENT VERIFICATION"
echo "Date: $(date)"
echo "Server: $(hostname)"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0
WARNINGS=0

check_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
}

check_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((ERRORS++))
}

check_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
    ((WARNINGS++))
}

echo ""
echo "=== 1. SYSTEM CHECKS ==="

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    check_pass "Running as root"
else
    check_fail "Not running as root. Please run with sudo"
fi

# Check disk space
DISK_USAGE=$(df -h / | awk 'NR==2 {print $5}' | sed 's/%//')
if [ "$DISK_USAGE" -lt 80 ]; then
    check_pass "Disk space OK ($DISK_USAGE% used)"
else
    check_warn "Disk space is high ($DISK_USAGE% used)"
fi

# Check memory
FREE_MEM=$(free -m | awk 'NR==2 {print $7}')
if [ "$FREE_MEM" -gt 1024 ]; then
    check_pass "Available memory: ${FREE_MEM}MB"
else
    check_warn "Low available memory: ${FREE_MEM}MB (recommended > 1GB)"
fi

echo ""
echo "=== 2. MYSQL CHECKS ==="

# Check MySQL service
if systemctl is-active --quiet mysqld; then
    check_pass "MySQL service is running"
else
    check_fail "MySQL service is NOT running"
fi

# Check MySQL version
MYSQL_VERSION=$(mysql --version 2>/dev/null | awk '{print $3}')
if [ -n "$MYSQL_VERSION" ]; then
    check_pass "MySQL version: $MYSQL_VERSION"
else
    check_fail "Cannot determine MySQL version"
fi

# Check MySQL connection
if mysql -u root -p'Nagaraj@07' -e "SELECT 1" &>/dev/null; then
    check_pass "MySQL root connection successful"
else
    check_fail "Cannot connect to MySQL as root"
fi

# Check if database exists
DB_EXISTS=$(mysql -u root -p'Nagaraj@07' -e "SHOW DATABASES LIKE 'selfie_prod'" 2>/dev/null | grep selfie_prod)
if [ -n "$DB_EXISTS" ]; then
    check_pass "Database 'selfie_prod' exists"

    # Check table count
    TABLE_COUNT=$(mysql -u root -p'Nagaraj@07' -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='selfie_prod'" 2>/dev/null | tail -1)
    if [ "$TABLE_COUNT" -gt 0 ]; then
        check_pass "Database has $TABLE_COUNT tables"
    else
        check_warn "Database 'selfie_prod' has no tables (will be created on first run)"
    fi
else
    check_warn "Database 'selfie_prod' does not exist (will need to create)"
fi

echo ""
echo "=== 3. JAVA CHECKS ==="

# Check Java installation
if command -v java &>/dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1)
    check_pass "Java installed: $JAVA_VERSION"
else
    check_fail "Java is NOT installed"
fi

# Check Java version is 11 or higher
JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VER" -ge 11 ]; then
    check_pass "Java version is 11 or higher"
else
    check_warn "Java version might be too old (need Java 11+)"
fi

echo ""
echo "=== 4. DIRECTORY CHECKS ==="

# Check /opt/tanishq directory
if [ -d "/opt/tanishq" ]; then
    check_pass "/opt/tanishq directory exists"
else
    check_fail "/opt/tanishq directory does NOT exist"
fi

# Check subdirectories
DIRS=(
    "/opt/tanishq/storage"
    "/opt/tanishq/storage/selfie_images"
    "/opt/tanishq/storage/bride_uploads"
    "/opt/tanishq/storage/bride_images"
    "/opt/tanishq/logs"
    "/opt/tanishq/backups"
)

for DIR in "${DIRS[@]}"; do
    if [ -d "$DIR" ]; then
        check_pass "$DIR exists"
    else
        check_fail "$DIR does NOT exist"
    fi
done

echo ""
echo "=== 5. FILE CHECKS ==="

# Check required files
FILES=(
    "/opt/tanishq/tanishqgmb-5437243a8085.p12:Google Service Account Key 1"
    "/opt/tanishq/event-images-469618-32e65f6d62b3.p12:Google Service Account Key 2"
    "/opt/tanishq/tanishq_selfie_app_store_data.xlsx:Store Data Excel"
)

for FILE_ENTRY in "${FILES[@]}"; do
    FILE_PATH=$(echo $FILE_ENTRY | cut -d':' -f1)
    FILE_DESC=$(echo $FILE_ENTRY | cut -d':' -f2)
    if [ -f "$FILE_PATH" ]; then
        check_pass "$FILE_DESC exists"
    else
        check_warn "$FILE_DESC NOT found at $FILE_PATH"
    fi
done

# Check for WAR file
WAR_FILE=$(ls /opt/tanishq/*.war 2>/dev/null | head -1)
if [ -n "$WAR_FILE" ]; then
    WAR_SIZE=$(du -h "$WAR_FILE" | cut -f1)
    check_pass "WAR file found: $(basename $WAR_FILE) ($WAR_SIZE)"
else
    check_warn "No WAR file found in /opt/tanishq/"
fi

echo ""
echo "=== 6. PORT CHECKS ==="

# Check if port 3001 is available
if netstat -tulpn | grep -q ":3001 "; then
    check_warn "Port 3001 is already in use"
    netstat -tulpn | grep ":3001 "
else
    check_pass "Port 3001 is available"
fi

# Check if port 3306 (MySQL) is listening
if netstat -tulpn | grep -q ":3306 "; then
    check_pass "MySQL port 3306 is listening"
else
    check_fail "MySQL port 3306 is NOT listening"
fi

echo ""
echo "=== 7. NETWORK CHECKS ==="

# Check if we can reach external services
if ping -c 1 smtp.office365.com &>/dev/null; then
    check_pass "Can reach email server (smtp.office365.com)"
else
    check_warn "Cannot reach email server (smtp.office365.com)"
fi

echo ""
echo "=== 8. EXISTING APPLICATION CHECK ==="

# Check if any tanishq application is running
TANISHQ_PROC=$(ps -ef | grep -E "tanishq.*\.war" | grep -v grep)
if [ -n "$TANISHQ_PROC" ]; then
    check_warn "Existing Tanishq application is running:"
    echo "$TANISHQ_PROC"
else
    check_pass "No existing Tanishq application running"
fi

echo ""
echo "=============================================="
echo "VERIFICATION SUMMARY"
echo "=============================================="
echo -e "Errors:   ${RED}$ERRORS${NC}"
echo -e "Warnings: ${YELLOW}$WARNINGS${NC}"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ System is ready for deployment!${NC}"
    if [ $WARNINGS -gt 0 ]; then
        echo -e "${YELLOW}⚠️  Please review warnings before proceeding.${NC}"
    fi
else
    echo -e "${RED}❌ Please fix $ERRORS error(s) before deployment.${NC}"
fi

echo ""
echo "=============================================="

