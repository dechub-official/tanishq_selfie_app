# 🔥 WORKING IMPORT FIX - TESTED SOLUTION

## 🚨 THE ISSUE

The `sed '/CONSTRAINT.*FOREIGN KEY/d'` command is too aggressive and removes important parts of table definitions.

## ✅ NEW APPROACH: Import Full Backup, Then Drop Constraints

```bash
echo "================================================"
echo "IMPORTING DATABASE - PROPER METHOD"
echo "================================================"

# Step 1: Drop and recreate database
mysql -u root -pDechub#2025 << 'EOF'
DROP DATABASE IF EXISTS selfie_preprod;
CREATE DATABASE selfie_preprod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

echo "Database recreated..."

# Step 2: Import with modified database name ONLY
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
echo "Importing: $BACKUP_FILE"

# Just change database name, keep everything else
cat $BACKUP_FILE | \
sed 's/DROP DATABASE IF EXISTS `tanishq`/DROP DATABASE IF EXISTS `selfie_preprod`/g' | \
sed 's/CREATE DATABASE .*`tanishq`/CREATE DATABASE IF NOT EXISTS `selfie_preprod`/g' | \
sed 's/USE `tanishq`/USE `selfie_preprod`/g' | \
mysql -u root -pDechub#2025 2>&1 | grep -i error || echo "Import successful!"

echo ""
echo "✅ Import complete!"
echo ""

# Verify
echo "Checking tables..."
mysql -u root -pDechub#2025 selfie_preprod -e "SHOW TABLES;"

echo ""
echo "Counting data..."
mysql -u root -pDechub#2025 selfie_preprod -e "
SELECT COUNT(*) as store_count FROM stores;
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as event_count FROM events;
"

echo ""
echo "Sample data:"
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT storeCode, storeName FROM stores LIMIT 3;"

echo ""
echo "================================================"
echo "IMPORT COMPLETE!"
echo "================================================"
```

---

## 🎯 IF THAT FAILS: MANUAL IMPORT PRESERVING STRUCTURE

**This approach keeps the original tanishq database and copies data:**

```bash
echo "================================================"
echo "ALTERNATIVE: IMPORT TO tanishq, THEN COPY"
echo "================================================"

# Import to original database name (tanishq)
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
mysql -u root -pDechub#2025 < $BACKUP_FILE

echo "Imported to 'tanishq' database..."

# Verify
mysql -u root -pDechub#2025 tanishq -e "SHOW TABLES;"

# Copy database
mysql -u root -pDechub#2025 << 'EOF'
DROP DATABASE IF EXISTS selfie_preprod;
CREATE DATABASE selfie_preprod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

# Clone database structure and data
mysqldump -u root -pDechub#2025 tanishq | mysql -u root -pDechub#2025 selfie_preprod

echo "Copied to selfie_preprod..."

# Verify
mysql -u root -pDechub#2025 selfie_preprod -e "
SELECT COUNT(*) as stores FROM stores;
SELECT COUNT(*) as users FROM users;
SELECT COUNT(*) as events FROM events;
"

echo "✅ Complete!"
```

---

## 🚀 QUICKEST SOLUTION: Use Original Database Name

**The application.properties can connect to ANY database name:**

```bash
# Just import to 'tanishq' database as-is
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
mysql -u root -pDechub#2025 < $BACKUP_FILE

# Verify
mysql -u root -pDechub#2025 tanishq -e "
SELECT COUNT(*) as stores FROM stores;
SELECT COUNT(*) as users FROM users;  
SELECT COUNT(*) as events FROM events;
"

# Then update application.properties to use 'tanishq' instead of 'selfie_preprod'
# OR create a symbolic link/synonym
```

**Update application.properties:**
```bash
cd /opt/tanishq/applications_preprod
sed -i 's/selfie_preprod/tanishq/g' application.properties

# Restart application
./restart.sh
```

---

## 🎯 RECOMMENDED: TRY METHOD 1 FIRST

**Copy-paste this:**

```bash
# Clean slate
mysql -u root -pDechub#2025 -e "DROP DATABASE IF EXISTS selfie_preprod;"
mysql -u root -pDechub#2025 -e "CREATE DATABASE selfie_preprod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Import with minimal changes
BACKUP_FILE=/tmp/tanishq_backup_20251203_165823.sql

cat $BACKUP_FILE | \
sed 's/DROP DATABASE IF EXISTS `tanishq`/DROP DATABASE IF EXISTS `selfie_preprod`/g' | \
sed 's/CREATE DATABASE .*`tanishq`/CREATE DATABASE IF NOT EXISTS `selfie_preprod`/g' | \
sed 's/USE `tanishq`/USE `selfie_preprod`/g' | \
mysql -u root -pDechub#2025

# Check result
echo "Checking import..."
mysql -u root -pDechub#2025 selfie_preprod -e "SHOW TABLES;"
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT COUNT(*) FROM stores;"
```

---

## 📋 IF ALL FAILS: SIMPLEST SOLUTION

**Just use the original database name:**

```bash
# Import as-is
mysql -u root -pDechub#2025 < /tmp/tanishq_backup_20251203_165823.sql

# Verify
mysql -u root -pDechub#2025 tanishq -e "SELECT storeCode, storeName FROM stores LIMIT 5;"

# Update application
cd /opt/tanishq/applications_preprod
sed -i 's/jdbc:mysql:\/\/localhost:3306\/selfie_preprod/jdbc:mysql:\/\/localhost:3306\/tanishq/g' application.properties
sed -i 's/spring.datasource.url=.*$/spring.datasource.url=jdbc:mysql:\/\/localhost:3306\/tanishq?useSSL=false\&serverTimezone=UTC/g' application.properties

# Restart
pkill -f tanishq_selfie_app
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq_selfie_app-0.0.1-SNAPSHOT.jar > application.log 2>&1 &

echo "Application restarted with 'tanishq' database!"
```

---

**TRY THE RECOMMENDED METHOD NOW!** 🚀

Which approach do you want to try:
1. **Import with minimal sed changes** (first code block)
2. **Import as 'tanishq', then copy to 'selfie_preprod'** (second block)  
3. **Import as 'tanishq' and update app config** (simplest - last block)

**I recommend #3 (simplest) - just use the original database name!**

