# 🔧 DATABASE NAME MISMATCH - QUICK FIX

## ⚠️ THE PROBLEM

```
ERROR 1146 (42S02): Table 'tanishq.stores' doesn't exist
```

**What happened:**
- You imported data to the **`tanishq`** database
- But the **`tanishq`** database is EMPTY (no tables)
- Your data is probably in **`selfie_preprod`** database
- OR you need to check where the data actually went

---

## ✅ STEP 1: CHECK WHERE YOUR DATA IS

**Run this on the server:**

```bash
# Check which databases exist
mysql -u root -pDechub#2025 -e "SHOW DATABASES;"

# Check tanishq database
echo "Checking tanishq database:"
mysql -u root -pDechub#2025 -e "USE tanishq; SHOW TABLES;"

# Check selfie_preprod database
echo ""
echo "Checking selfie_preprod database:"
mysql -u root -pDechub#2025 -e "USE selfie_preprod; SHOW TABLES;"
```

---

## 🎯 SOLUTION DEPENDS ON WHERE DATA IS

### **CASE A: Data is in `selfie_preprod`**

**Update application to use `selfie_preprod`:**

```bash
cd /opt/tanishq/applications_preprod

# Backup original
cp application.properties application.properties.bak

# Update to use selfie_preprod
sed -i 's/jdbc:mysql:\/\/localhost:3306\/tanishq/jdbc:mysql:\/\/localhost:3306\/selfie_preprod/g' application.properties

# Verify change
echo "Updated database connection:"
grep "spring.datasource.url" application.properties

# Restart application
pkill -f tanishq_selfie_app
nohup java -jar tanishq_selfie_app-0.0.1-SNAPSHOT.jar > application.log 2>&1 &

echo "✅ Application restarted with selfie_preprod database"
```

---

### **CASE B: Data is in `tanishq` but tables missing**

**Re-import the backup properly:**

```bash
# Drop and recreate tanishq database
mysql -u root -pDechub#2025 << 'EOF'
DROP DATABASE IF EXISTS tanishq;
CREATE DATABASE tanishq CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

# Import backup
mysql -u root -pDechub#2025 < /tmp/tanishq_backup_20251203_165823.sql

# Verify
mysql -u root -pDechub#2025 tanishq -e "SHOW TABLES;"
mysql -u root -pDechub#2025 tanishq -e "SELECT COUNT(*) FROM stores;"
```

---

### **CASE C: Neither database has data**

**Import to whichever database your app uses:**

```bash
# Check what database app is configured for
grep "spring.datasource.url" /opt/tanishq/applications_preprod/application.properties

# If it shows selfie_preprod:
mysql -u root -pDechub#2025 << 'EOF'
DROP DATABASE IF EXISTS selfie_preprod;
CREATE DATABASE selfie_preprod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

cat /tmp/tanishq_backup_20251203_165823.sql | \
sed 's/DROP DATABASE IF EXISTS `tanishq`/DROP DATABASE IF EXISTS `selfie_preprod`/g' | \
sed 's/CREATE DATABASE.*`tanishq`/CREATE DATABASE IF NOT EXISTS `selfie_preprod`/g' | \
sed 's/USE `tanishq`/USE `selfie_preprod`/g' | \
mysql -u root -pDechub#2025

# Verify
mysql -u root -pDechub#2025 selfie_preprod -e "SHOW TABLES;"
```

---

## 🚀 COMPLETE ONE-COMMAND FIX

**This will automatically detect and fix the issue:**

```bash
bash << 'FIXSCRIPT'

echo "================================================"
echo "DATABASE NAME MISMATCH - AUTO FIX"
echo "================================================"

# Step 1: Check where data is
echo ""
echo "Checking databases..."
TANISHQ_TABLES=$(mysql -u root -pDechub#2025 tanishq -e "SHOW TABLES;" 2>/dev/null | wc -l)
SELFIE_TABLES=$(mysql -u root -pDechub#2025 selfie_preprod -e "SHOW TABLES;" 2>/dev/null | wc -l)

echo "tanishq database has $TANISHQ_TABLES tables"
echo "selfie_preprod database has $SELFIE_TABLES tables"

# Step 2: Check what app is configured for
APP_DB=$(grep "spring.datasource.url" /opt/tanishq/applications_preprod/application.properties | grep -o 'localhost:3306/[^?]*' | cut -d'/' -f2)
echo ""
echo "Application is configured for database: $APP_DB"

# Step 3: Fix based on situation
if [ "$TANISHQ_TABLES" -gt "1" ]; then
    echo ""
    echo "✅ Data found in 'tanishq' database"
    
    if [ "$APP_DB" != "tanishq" ]; then
        echo "Updating application to use 'tanishq' database..."
        cd /opt/tanishq/applications_preprod
        sed -i.bak "s/jdbc:mysql:\/\/localhost:3306\/$APP_DB/jdbc:mysql:\/\/localhost:3306\/tanishq/g" application.properties
        
        echo "Restarting application..."
        pkill -f tanishq_selfie_app
        nohup java -jar tanishq_selfie_app-0.0.1-SNAPSHOT.jar > application.log 2>&1 &
        
        echo "✅ Fixed! Application now uses 'tanishq' database"
    else
        echo "✅ Already correct! Application uses 'tanishq'"
    fi
    
elif [ "$SELFIE_TABLES" -gt "1" ]; then
    echo ""
    echo "✅ Data found in 'selfie_preprod' database"
    
    if [ "$APP_DB" != "selfie_preprod" ]; then
        echo "Updating application to use 'selfie_preprod' database..."
        cd /opt/tanishq/applications_preprod
        sed -i.bak "s/jdbc:mysql:\/\/localhost:3306\/$APP_DB/jdbc:mysql:\/\/localhost:3306\/selfie_preprod/g" application.properties
        
        echo "Restarting application..."
        pkill -f tanishq_selfie_app
        nohup java -jar tanishq_selfie_app-0.0.1-SNAPSHOT.jar > application.log 2>&1 &
        
        echo "✅ Fixed! Application now uses 'selfie_preprod' database"
    else
        echo "✅ Already correct! Application uses 'selfie_preprod'"
    fi
    
else
    echo ""
    echo "❌ No data found in either database!"
    echo "Need to re-import backup..."
    
    if [ "$APP_DB" == "selfie_preprod" ]; then
        TARGET_DB="selfie_preprod"
    else
        TARGET_DB="tanishq"
    fi
    
    echo "Importing to $TARGET_DB database..."
    
    mysql -u root -pDechub#2025 << EOF
DROP DATABASE IF EXISTS $TARGET_DB;
CREATE DATABASE $TARGET_DB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF
    
    if [ "$TARGET_DB" == "tanishq" ]; then
        mysql -u root -pDechub#2025 < /tmp/tanishq_backup_20251203_165823.sql
    else
        cat /tmp/tanishq_backup_20251203_165823.sql | \
        sed 's/DROP DATABASE IF EXISTS `tanishq`/DROP DATABASE IF EXISTS `selfie_preprod`/g' | \
        sed 's/CREATE DATABASE.*`tanishq`/CREATE DATABASE IF NOT EXISTS `selfie_preprod`/g' | \
        sed 's/USE `tanishq`/USE `selfie_preprod`/g' | \
        mysql -u root -pDechub#2025
    fi
    
    echo "✅ Data imported to $TARGET_DB"
fi

# Step 4: Verify fix
echo ""
echo "================================================"
echo "VERIFICATION"
echo "================================================"

# Check app config
echo "Application database: $(grep 'spring.datasource.url' /opt/tanishq/applications_preprod/application.properties | grep -o 'localhost:3306/[^?]*' | cut -d'/' -f2)"

# Check data
DB_TO_CHECK=$(grep 'spring.datasource.url' /opt/tanishq/applications_preprod/application.properties | grep -o 'localhost:3306/[^?]*' | cut -d'/' -f2)
echo ""
echo "Data in $DB_TO_CHECK database:"
mysql -u root -pDechub#2025 $DB_TO_CHECK -e "
SELECT 'stores' as tbl, COUNT(*) as cnt FROM stores
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'events', COUNT(*) FROM events;
"

echo ""
echo "================================================"
echo "✅ FIX COMPLETE!"
echo "================================================"

FIXSCRIPT
```

---

## ✅ AFTER RUNNING THE FIX

**Test with these commands:**

```bash
# Get the correct database name
APP_DB=$(grep "spring.datasource.url" /opt/tanishq/applications_preprod/application.properties | grep -o 'localhost:3306/[^?]*' | cut -d'/' -f2)

echo "Testing $APP_DB database..."

# Test database
mysql -u root -pDechub#2025 $APP_DB -e "SELECT storeCode, storeName FROM stores LIMIT 3;"

# Test API
sleep 3
TEST_USER=$(mysql -u root -pDechub#2025 $APP_DB -e "SELECT code FROM users LIMIT 1;" -s -N)
TEST_PASS=$(mysql -u root -pDechub#2025 $APP_DB -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N)

curl -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}"
```

---

## 📋 MANUAL CHECK

**If you want to check manually:**

```bash
# Check tanishq database
mysql -u root -pDechub#2025 tanishq -e "SHOW TABLES;" | wc -l

# Check selfie_preprod database  
mysql -u root -pDechub#2025 selfie_preprod -e "SHOW TABLES;" | wc -l

# Check app config
grep "datasource.url" /opt/tanishq/applications_preprod/application.properties
```

---

**RUN THE "COMPLETE ONE-COMMAND FIX" ABOVE!** 🚀

It will:
1. ✅ Detect where your data is
2. ✅ Update application.properties if needed
3. ✅ Restart the application
4. ✅ Verify everything works

**Copy the big bash script and run it!** 💪

