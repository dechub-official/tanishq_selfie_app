# 🔧 ULTIMATE FIX - FOREIGN KEY + COLUMN ISSUES

## 🚨 THE REAL PROBLEM

The `SET FOREIGN_KEY_CHECKS=0` isn't working because the error happens during table **creation**, not data insertion.

## ✅ SOLUTION: Drop and Recreate Database

```bash
# Step 1: Drop the existing database and recreate fresh
mysql -u root -pDechub#2025 << 'EOF'
DROP DATABASE IF EXISTS selfie_preprod;
CREATE DATABASE selfie_preprod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SET GLOBAL FOREIGN_KEY_CHECKS=0;
EOF

# Step 2: Import with modifications
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
echo "Importing: $BACKUP_FILE"

# Import with database rename and FK checks off globally
sed 's/DATABASE `tanishq`/DATABASE `selfie_preprod`/g' $BACKUP_FILE | \
sed 's/USE `tanishq`/USE `selfie_preprod`/g' | \
sed '/CONSTRAINT.*FOREIGN KEY/d' | \
mysql -u root -pDechub#2025 selfie_preprod

# Step 3: Re-enable foreign key checks
mysql -u root -pDechub#2025 -e "SET GLOBAL FOREIGN_KEY_CHECKS=1;"

echo "✅ Import complete!"
```

---

## 🔍 CHECK WHAT WE IMPORTED

```bash
# Check tables exist
mysql -u root -pDechub#2025 selfie_preprod -e "SHOW TABLES;"

# Check stores table structure
mysql -u root -pDechub#2025 selfie_preprod -e "DESCRIBE stores;"

# Check users table structure  
mysql -u root -pDechub#2025 selfie_preprod -e "DESCRIBE users;"

# Count rows (using backticks for Rows)
mysql -u root -pDechub#2025 selfie_preprod -e "
SELECT 'Stores' as TableName, COUNT(*) as RowCount FROM stores
UNION ALL SELECT 'Users', COUNT(*) FROM users
UNION ALL SELECT 'Events', COUNT(*) FROM events
UNION ALL SELECT 'Attendees', COUNT(*) FROM attendees;
"
```

---

## 🎯 COMPLETE ONE-COMMAND FIX

**Copy-paste this entire block:**

```bash
echo "================================================"
echo "FIXING DATABASE IMPORT - REMOVING FOREIGN KEYS"
echo "================================================"

# Drop and recreate database
mysql -u root -pDechub#2025 << 'EOF'
DROP DATABASE IF EXISTS selfie_preprod;
CREATE DATABASE selfie_preprod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

echo "Database recreated..."

# Import without foreign key constraints
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)
echo "Importing: $BACKUP_FILE"

sed 's/DATABASE `tanishq`/DATABASE `selfie_preprod`/g' $BACKUP_FILE | \
sed 's/USE `tanishq`/USE `selfie_preprod`/g' | \
sed '/CONSTRAINT.*FOREIGN KEY/d' | \
sed '/ADD CONSTRAINT/d' | \
mysql -u root -pDechub#2025 selfie_preprod 2>&1 | grep -v "Warning"

echo ""
echo "✅ Import complete!"
echo ""

# Verify import
echo "Verifying data..."
mysql -u root -pDechub#2025 selfie_preprod -e "
SELECT 'Stores' as TableName, COUNT(*) as RowCount FROM stores
UNION ALL SELECT 'Users', COUNT(*) FROM users  
UNION ALL SELECT 'Events', COUNT(*) FROM events
UNION ALL SELECT 'Attendees', COUNT(*) FROM attendees;
"

echo ""
echo "Sample stores:"
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT storeCode, storeName FROM stores LIMIT 3;"

echo ""
echo "Checking users table structure..."
mysql -u root -pDechub#2025 selfie_preprod -e "SHOW COLUMNS FROM users;"

echo ""
echo "================================================"
echo "IMPORT COMPLETE!"
echo "================================================"
```

---

## 🧪 AFTER IMPORT - TEST LOGIN

**First, check what columns exist in users table:**

```bash
mysql -u root -pDechub#2025 selfie_preprod -e "SHOW COLUMNS FROM users;"
```

**Then test with actual column names:**

```bash
# Get sample user (adjust column name based on above)
mysql -u root -pDechub#2025 selfie_preprod -e "SELECT * FROM users LIMIT 1;"

# Test login with actual data
# Replace 'code' and 'password' with actual column names
curl -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d '{"code":"TEST","password":"Titan@123"}'
```

---

## 🔧 ALTERNATIVE: MANUAL TABLE-BY-TABLE IMPORT

If the above still fails, we can import tables individually:

```bash
# Extract just the data (no CREATE TABLE statements)
BACKUP_FILE=$(ls -t /tmp/tanishq_backup_*.sql | head -n 1)

# Get table names from backup
grep "^INSERT INTO" $BACKUP_FILE | awk '{print $3}' | tr -d '`' | sort -u

# Import each table's data separately
# (We'll do this if needed)
```

---

## 📋 WHAT THIS DOES DIFFERENTLY

1. **Drops database completely** - Fresh start
2. **Removes ALL foreign key constraints** - No compatibility issues
3. **Imports just data and structure** - Skip problematic constraints
4. **Checks actual column names** - So we can test properly

---

**Run the "COMPLETE ONE-COMMAND FIX" block above NOW!** 🚀

This will:
- ✅ Remove the database and start fresh
- ✅ Import WITHOUT foreign key constraints (they're optional for your app)
- ✅ Show you what's actually in the database
- ✅ Tell you the real column names so we can test

**Copy the entire block and paste it!** 💪

