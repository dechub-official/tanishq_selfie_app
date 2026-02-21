if [ $? -eq 0 ]; then
    echo "[OK] Database 'tanishq_preprod' exists"
else
    echo "[WARNING] Database 'tanishq_preprod' not found"
    echo "Create it manually or let the application create tables"
fi
echo ""

echo "[7/10] Checking port 3002..."
if netstat -tuln | grep -q ":3002 "; then
    echo "[WARNING] Port 3002 is already in use"
    echo "Current process using port 3002:"
    netstat -tulpn | grep ":3002 "
else
    echo "[OK] Port 3002 is available"
fi
echo ""

echo "[8/10] Checking Nginx..."
if command -v nginx &> /dev/null; then
    echo "[OK] Nginx is installed"
    nginx -v 2>&1
    if [ -f "/etc/nginx/conf.d/celebrations-preprod.conf" ]; then
        echo "[OK] Nginx config for preprod exists"
    else
        echo "[WARNING] Nginx config not found: /etc/nginx/conf.d/celebrations-preprod.conf"
    fi
else
    echo "[INFO] Nginx not found (might not be needed)"
fi
echo ""

echo "[9/10] Checking disk space..."
df -h /opt/tanishq 2>/dev/null || df -h /opt
echo ""

echo "[10/10] Checking for running tanishq processes..."
ps -ef | grep tanishq | grep -v grep
if [ $? -eq 0 ]; then
    echo "[INFO] Found running tanishq processes"
else
    echo "[OK] No tanishq processes running"
fi
echo ""

echo "========================================"
echo "Summary"
echo "========================================"
if [ $ERROR_COUNT -eq 0 ]; then
    echo "[SUCCESS] Server is ready for pre-prod deployment!"
    echo ""
    echo "Next steps:"
    echo "1. Upload WAR file to /opt/tanishq/applications_preprod/"
    echo "2. Start application with nohup"
    echo "3. Monitor logs"
else
    echo "[FAILED] Found $ERROR_COUNT error(s)"
    echo "Please fix the errors above before deploying"
fi
echo "========================================"
echo ""

