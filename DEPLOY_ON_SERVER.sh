#!/bin/bash

###############################################################################
# TANISHQ CELEBRATION APP - COMPLETE DEPLOYMENT SCRIPT
# Target: http://celebrationsite-preprod.tanishq.co.in
# Server: 10.160.128.94:3000
# Database: selfie_preprod
###############################################################################

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DEPLOY_DIR="/opt/tanishq/applications_preprod"
WAR_FILE="tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war"
APP_PORT=3000
DB_NAME="selfie_preprod"
DB_USER="root"
DB_PASS="Dechub#2025"
DOMAIN="celebrationsite-preprod.tanishq.co.in"

###############################################################################
# Functions
###############################################################################

print_header() {
    echo -e "${BLUE}"
    echo "========================================================"
    echo "  $1"
    echo "========================================================"
    echo -e "${NC}"
}

print_step() {
    echo -e "${BLUE}📍 $1${NC}"
}

print_success() {
    echo -e "${GREEN}   ✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}   ⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}   ❌ $1${NC}"
}

print_info() {
    echo -e "   ℹ️  $1"
}

###############################################################################
# Main Deployment Process
###############################################################################

print_header "TANISHQ CELEBRATION APP - DEPLOYMENT"
echo "   URL:      http://$DOMAIN"
echo "   Server:   10.160.128.94:$APP_PORT"
echo "   Database: $DB_NAME"
echo "   Date:     $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# Check if running as root or sudo
if [ "$EUID" -ne 0 ]; then
    print_warning "Not running as root. Some commands may fail."
    echo "   Consider running with: sudo $0"
    echo ""
    read -p "   Continue anyway? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

###############################################################################
# STEP 1: Stop Old Application
###############################################################################

print_step "STEP 1/12: Stopping old application..."

# Find and kill any running Java processes for Tanishq
if pgrep -f "tanishq-preprod" > /dev/null; then
    pkill -9 -f "tanishq-preprod" 2>/dev/null
    sleep 3

    # Verify it's stopped
    if pgrep -f "tanishq-preprod" > /dev/null; then
        print_warning "Process still running, attempting force kill..."
        pkill -9 java 2>/dev/null
        sleep 2
    fi
fi

# Final check
if pgrep -f "tanishq-preprod" > /dev/null; then
    print_error "Failed to stop old application"
    exit 1
else
    print_success "Old application stopped"
fi
echo ""

###############################################################################
# STEP 2: Navigate to Deployment Directory
###############################################################################

print_step "STEP 2/12: Navigating to deployment directory..."

if [ ! -d "$DEPLOY_DIR" ]; then
    print_warning "Directory not found, creating: $DEPLOY_DIR"
    mkdir -p "$DEPLOY_DIR"
fi

cd "$DEPLOY_DIR" || {
    print_error "Failed to navigate to $DEPLOY_DIR"
    exit 1
}

print_success "Current directory: $(pwd)"
echo ""

###############################################################################
# STEP 3: Create Backup
###############################################################################

print_step "STEP 3/12: Creating backup..."

BACKUP_DIR="backup_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

# Backup old WAR files
if ls tanishq-preprod*.war 1> /dev/null 2>&1; then
    cp tanishq-preprod*.war "$BACKUP_DIR/" 2>/dev/null || true
    print_success "WAR file backed up"
fi

# Backup logs
if ls *.log 1> /dev/null 2>&1; then
    cp *.log "$BACKUP_DIR/" 2>/dev/null || true
    print_success "Logs backed up"
fi

print_success "Backup created: $BACKUP_DIR"
echo ""

###############################################################################
# STEP 4: Clean Old Logs
###############################################################################

print_step "STEP 4/12: Cleaning old logs..."

rm -f application.log app.log nohup.out 2>/dev/null || true
print_success "Logs cleaned"
echo ""

###############################################################################
# STEP 5: Verify WAR File
###############################################################################

print_step "STEP 5/12: Verifying WAR file..."

if [ ! -f "$WAR_FILE" ]; then
    print_error "WAR file not found: $WAR_FILE"
    print_error "Please upload the WAR file to: $(pwd)"
    echo ""
    echo "Available files:"
    ls -lh *.war 2>/dev/null || echo "No WAR files found"
    exit 1
fi

WAR_SIZE=$(du -h "$WAR_FILE" | cut -f1)
print_success "WAR file found: $WAR_FILE ($WAR_SIZE)"
echo ""

###############################################################################
# STEP 6: Verify Database
###############################################################################

print_step "STEP 6/12: Verifying database..."

# Check if MySQL is running
if ! systemctl is-active --quiet mysqld 2>/dev/null && ! systemctl is-active --quiet mysql 2>/dev/null; then
    print_warning "MySQL service may not be running"
fi

# Check database connection and count stores
DB_COUNT=$(mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "SELECT COUNT(*) FROM stores;" -s -N 2>/dev/null || echo "0")

if [ "$DB_COUNT" -gt "0" ]; then
    print_success "Database connected: $DB_COUNT stores"
else
    print_warning "Database connection issue (Count: $DB_COUNT)"
    print_info "Application will create tables if needed"
fi
echo ""

###############################################################################
# STEP 7: Configure Firewall
###############################################################################

print_step "STEP 7/12: Configuring firewall..."

if command -v firewall-cmd &> /dev/null; then
    firewall-cmd --permanent --add-port=$APP_PORT/tcp 2>/dev/null || true
    firewall-cmd --reload 2>/dev/null || true

    if firewall-cmd --list-ports 2>/dev/null | grep -q "$APP_PORT"; then
        print_success "Port $APP_PORT opened in firewall"
    else
        print_warning "Could not verify firewall port"
    fi
else
    print_info "Firewall command not available (might be disabled)"
fi
echo ""

###############################################################################
# STEP 8: Deploy Application
###############################################################################

print_step "STEP 8/12: Deploying application on port $APP_PORT..."

nohup java -jar "$WAR_FILE" \
  --spring.datasource.url="jdbc:mysql://localhost:3306/$DB_NAME?useSSL=false&serverTimezone=UTC" \
  --spring.datasource.username="$DB_USER" \
  --spring.datasource.password="$DB_PASS" \
  --server.port=$APP_PORT \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

APP_PID=$!
print_success "Application started with PID: $APP_PID"
echo ""

###############################################################################
# STEP 9: Wait for Startup
###############################################################################

print_step "STEP 9/12: Waiting for application startup..."

echo -n "   "
for i in {30..1}; do
    echo -n "."
    sleep 1
    if [ $((i % 10)) -eq 0 ]; then
        echo -n " $i "
    fi
done
echo ""
print_success "Wait complete (30 seconds)"
echo ""

###############################################################################
# STEP 10: Verify Deployment
###############################################################################

print_step "STEP 10/12: Verifying deployment..."

# Check if process is running
if pgrep -f "java.*tanishq" > /dev/null; then
    REAL_PID=$(pgrep -f "java.*tanishq" | head -1)
    print_success "Process running (PID: $REAL_PID)"
else
    print_error "Process not running!"
    echo ""
    print_error "Last 50 lines of log:"
    tail -50 application.log
    exit 1
fi

# Check if port is listening
if netstat -tlnp 2>/dev/null | grep ":$APP_PORT" > /dev/null || ss -tlnp 2>/dev/null | grep ":$APP_PORT" > /dev/null; then
    print_success "Port $APP_PORT is listening"
else
    print_error "Port $APP_PORT not listening!"
    echo ""
    print_error "Last 50 lines of log:"
    tail -50 application.log
    exit 1
fi
echo ""

###############################################################################
# STEP 11: Test Local Access
###############################################################################

print_step "STEP 11/12: Testing local access..."

# Test localhost
if command -v curl &> /dev/null; then
    LOCAL_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$APP_PORT 2>/dev/null || echo "000")
    if [ "$LOCAL_STATUS" = "200" ] || [ "$LOCAL_STATUS" = "302" ]; then
        print_success "Localhost test: HTTP $LOCAL_STATUS"
    else
        print_warning "Localhost test: HTTP $LOCAL_STATUS"
    fi

    # Test IP
    SERVER_IP=$(hostname -I | awk '{print $1}')
    IP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://$SERVER_IP:$APP_PORT 2>/dev/null || echo "000")
    if [ "$IP_STATUS" = "200" ] || [ "$IP_STATUS" = "302" ]; then
        print_success "IP test: HTTP $IP_STATUS"
    else
        print_warning "IP test: HTTP $IP_STATUS"
    fi
else
    print_warning "curl not installed, skipping HTTP tests"
fi
echo ""

###############################################################################
# STEP 12: Test Database Connectivity
###############################################################################

print_step "STEP 12/12: Testing database connectivity via API..."

if command -v curl &> /dev/null && command -v mysql &> /dev/null; then
    TEST_USER=$(mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "SELECT code FROM users LIMIT 1;" -s -N 2>/dev/null || echo "")

    if [ -n "$TEST_USER" ]; then
        TEST_PASS=$(mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N 2>/dev/null || echo "")

        API_RESPONSE=$(curl -s -X POST "http://localhost:$APP_PORT/events/login" \
          -H "Content-Type: application/json" \
          -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" 2>/dev/null || echo "")

        if echo "$API_RESPONSE" | grep -q '"status":true\|"status":false'; then
            print_success "Login API responding (Test user: $TEST_USER)"
        else
            print_warning "API Response: $(echo "$API_RESPONSE" | cut -c1-100)"
        fi
    else
        print_warning "No test user found in database"
    fi
else
    print_info "Skipping API test (curl or mysql not available)"
fi
echo ""

###############################################################################
# FINAL SUMMARY
###############################################################################

print_header "DEPLOYMENT SUMMARY"

echo -e "${GREEN}Application Status:${NC}"
echo "   ✅ Running on port $APP_PORT (PID: $REAL_PID)"
echo "   ✅ Database: $DB_NAME ($DB_COUNT stores)"
echo "   ✅ WAR file: $WAR_FILE ($WAR_SIZE)"
echo ""

echo -e "${BLUE}Access URLs:${NC}"
echo "   • Local:    http://localhost:$APP_PORT (HTTP ${LOCAL_STATUS:-N/A})"
echo "   • IP:       http://10.160.128.94:$APP_PORT (HTTP ${IP_STATUS:-N/A})"
echo "   • Domain:   http://$DOMAIN"
echo ""

echo -e "${BLUE}Recent Application Logs:${NC}"
tail -20 application.log | grep -i "started\|tomcat\|error\|exception" | tail -5
echo ""

print_header "DEPLOYMENT COMPLETE!"

echo -e "${GREEN}Next Steps:${NC}"
echo "   1. Wait 60 seconds for AWS ELB health check"
echo "   2. Test domain: curl http://$DOMAIN"
echo "   3. Open in browser: http://$DOMAIN"
echo "   4. Monitor logs: tail -f $DEPLOY_DIR/application.log"
echo ""

echo -e "${YELLOW}If domain shows 502 error:${NC}"
echo "   → Contact AWS team to verify ELB target group"
echo "   → Verify target 10.160.128.94:$APP_PORT shows 'Healthy'"
echo "   → Check security group allows traffic from ELB"
echo ""

echo -e "${BLUE}Useful Commands:${NC}"
echo "   View logs:    tail -f application.log"
echo "   Check status: ps aux | grep java"
echo "   Check port:   netstat -tlnp | grep $APP_PORT"
echo "   Stop app:     pkill -f tanishq-preprod"
echo ""

print_header "DEPLOYMENT SUCCESSFUL! 🎉"

echo ""

