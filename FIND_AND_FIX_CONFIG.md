# ✅ SOLUTION FOUND!

## 🎯 THE SITUATION:

```
✅ Data is in: selfie_preprod (16 tables)
❌ tanishq database: EMPTY
❌ application.properties: NOT in /opt/tanishq/applications_preprod
```

---

## 🔍 STEP 1: FIND application.properties

**Run this on the server:**

```bash
# Find the application.properties file
echo "Searching for application.properties..."
find /opt -name "application.properties" 2>/dev/null

# Also check current directory
ls -la /opt/tanishq/applications_preprod/
```

---

## 🎯 STEP 2: ONCE FOUND, UPDATE IT

**After you find where application.properties is, run:**

```bash
# Replace with actual path
PROP_FILE="/path/to/application.properties"

# Backup
cp $PROP_FILE ${PROP_FILE}.bak

# Update database name
sed -i 's/jdbc:mysql:\/\/localhost:3306\/tanishq/jdbc:mysql:\/\/localhost:3306\/selfie_preprod/g' $PROP_FILE

# Verify
echo "Updated configuration:"
grep "spring.datasource.url" $PROP_FILE
```

---

## ⚡ QUICK SOLUTION - RUN THIS NOW:

```bash
echo "================================================"
echo "FINDING AND FIXING APPLICATION.PROPERTIES"
echo "================================================"

# Find application.properties
echo "Searching for application.properties..."
PROP_FILE=$(find /opt -name "application.properties" 2>/dev/null | head -1)

if [ -z "$PROP_FILE" ]; then
    echo "❌ application.properties not found in /opt"
    echo ""
    echo "Checking current directory and subdirectories..."
    PROP_FILE=$(find . -name "application.properties" 2>/dev/null | head -1)
fi

if [ -n "$PROP_FILE" ]; then
    echo "✅ Found: $PROP_FILE"
    echo ""
    
    # Show current database config
    echo "Current database configuration:"
    grep -i "datasource" "$PROP_FILE" | grep -v "#"
    
    # Check if it needs updating
    if grep -q "selfie_preprod" "$PROP_FILE"; then
        echo ""
        echo "✅ Already configured for selfie_preprod!"
    elif grep -q "tanishq" "$PROP_FILE"; then
        echo ""
        echo "Updating to use selfie_preprod..."
        
        # Backup
        cp "$PROP_FILE" "${PROP_FILE}.bak_$(date +%Y%m%d_%H%M%S)"
        
        # Update
        sed -i 's/jdbc:mysql:\/\/localhost:3306\/tanishq/jdbc:mysql:\/\/localhost:3306\/selfie_preprod/g' "$PROP_FILE"
        
        echo "✅ Updated!"
        echo ""
        echo "New configuration:"
        grep -i "datasource" "$PROP_FILE" | grep -v "#"
        
        # Restart application
        echo ""
        echo "Restarting application..."
        pkill -f tanishq_selfie_app
        
        APP_DIR=$(dirname "$PROP_FILE")
        cd "$APP_DIR"
        
        JAR_FILE=$(ls -1 *.jar 2>/dev/null | head -1)
        if [ -n "$JAR_FILE" ]; then
            nohup java -jar "$JAR_FILE" > application.log 2>&1 &
            echo "✅ Application restarted!"
        else
            echo "❌ No JAR file found in $APP_DIR"
        fi
    fi
else
    echo "❌ application.properties not found!"
    echo ""
    echo "Please tell me where your application files are located."
    echo "Try: ls -la /opt/tanishq/"
fi

echo ""
echo "================================================"
echo "VERIFICATION"
echo "================================================"

# Test database
echo "Testing selfie_preprod database:"
mysql -u root -pDechub#2025 selfie_preprod -e "
SELECT 'stores' as tbl, COUNT(*) as cnt FROM stores
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'events', COUNT(*) FROM events;
" 2>&1 | grep -v "Warning"

# Check if app is running
echo ""
echo "Application status:"
ps aux | grep -c "[t]anishq_selfie_app" && echo "✅ Running" || echo "❌ Not running"

echo ""
echo "================================================"
```

---

## 🔍 OR MANUALLY FIND THE FILE:

```bash
# Check common locations
ls -la /opt/tanishq/applications_preprod/
ls -la /root/applications_preprod/
ls -la ~/applications_preprod/

# Find anywhere
find / -name "application.properties" 2>/dev/null | grep -i tanishq

# Check where JAR file is
find /opt -name "*.jar" 2>/dev/null | grep tanishq
```

---

## 📋 ALTERNATIVE: CREATE application.properties

**If the file doesn't exist, you might be using application.yml or running from JAR directly:**

```bash
# Check for application.yml
find /opt -name "application.yml" 2>/dev/null

# Check how application is currently running
ps aux | grep tanishq_selfie_app

# Get the full command that's running
ps aux | grep java | grep tanishq
```

---

## ✅ AFTER YOU FIND IT:

**Tell me the output of:**

```bash
find /opt -name "application.properties" 2>/dev/null
```

**OR**

```bash
ps aux | grep java | grep tanishq
```

**And I'll give you the exact commands to fix it!**

---

**RUN THE QUICK SOLUTION SCRIPT ABOVE FIRST!** 🚀

It will:
1. ✅ Find application.properties automatically
2. ✅ Update it to use selfie_preprod
3. ✅ Restart the application
4. ✅ Verify everything works

