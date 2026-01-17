# Legacy Data Migration Guide - Google Sheets to MySQL

## Overview
This guide helps you migrate your **2 months of historical data** from Google Sheets to the MySQL production database so users can see their existing data after deployment.

---

## Current Situation

### ✅ What's Working:
- **Pre-Prod Server**: MySQL database with new structure - WORKING
- **Production Server**: 
  - MySQL installed and configured
  - Application running on port 3000
  - Database: `selfie_prod` created
  - Tables: Partially created (some missing like `attendees`)

### ❌ Problem:
- Historical data (last 2 months) is still in Google Sheets
- Data structure in Google Sheets is different from MySQL tables
- Need to migrate and transform this data to MySQL format

---

## Google Sheets Data Sources

Based on your `application-prod.properties`, you have data in these Google Sheets:

1. **Events Data**: `1ZKb4rqIon5HSdXNnwnYPNZA75Rh1vSBQoup7GCmaWcQ`
2. **Events Attendees**: `1rXq_zS0dj0pofs_wzlDfpl5rXVYDIN0fs9Qb9TgMXYU`
3. **Events Invitees**: `1D4R7minvW2rke4LQfO70PemRDQZYI92x63EYFf1p9b0`
4. **Bride Details**: `13C0M-v8tZQpDCXg09pufQ1f6kz2sQYE0wEIRB1-yIpk`
5. **User Details**: `1vSG8T8rRm5jge_j-exRRvglRO6DEVBXH8UjqMaRQ_5w`
6. **Store Details**: `1Y3ieu2Fz0ELcixqNaJa1KTvBOCP65B0lbbmP_edF_oQ`
7. **Product Details**: `1ZM3YEDlRI-Kbbt1CVN3qZlnMx2QizXJWTMiUO6Rgd-Y`
8. **Rivaah Details**: `1tjb2cF6Ye0uIj51jtVHUKgNITJRey5i13Ew0GSwncVY`
9. **Rivaah Users**: `186XwrPKGhaaFMmN5q7doT5lXxbdKVrk8zI_Bs45N0sg`

---

## Migration Strategy

### Option 1: Export from Google Sheets → Import to MySQL (Recommended)
**Best for:** When you have access to Google Sheets and can export data

### Option 2: Let Spring Boot Sync on Startup
**Best for:** If your app already has Google Sheets reading logic

### Option 3: Use Pre-Prod Database as Source
**Best for:** If pre-prod already has the migrated data

---

## OPTION 1: Manual Export & Import (Step-by-Step)

### Step 1: Export Data from Google Sheets

#### On Your Local Machine (Windows):

1. **Open each Google Sheet** using the IDs from config
2. **Download as CSV** for each sheet:
   - Events → `events_legacy.csv`
   - Attendees → `attendees_legacy.csv`
   - Invitees → `invitees_legacy.csv`
   - Bride Details → `bride_details_legacy.csv`
   - Users → `users_legacy.csv`
   - Stores → `stores_legacy.csv`
   - Product Details → `products_legacy.csv`

3. **Save all CSV files** to: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\legacy_data\`

### Step 2: Upload CSV Files to Production Server

```bash
# On your local Windows machine (PowerShell)
# Replace 10.10.63.97 with your production server IP
scp -r C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\legacy_data\*.csv root@10.10.63.97:/opt/tanishq/legacy_data/
```

### Step 3: Run the Migration Script on Production Server

See the `import_legacy_data.sh` script created below.

---

## OPTION 2: Copy from Pre-Prod Database (Easiest)

### If Your Pre-Prod Already Has the Data:

#### Step 1: Export from Pre-Prod Server

```bash
# On Pre-Prod Server
mysqldump -u root -p selfie_preprod \
  --no-create-info \
  --complete-insert \
  --skip-triggers \
  events attendees invitees bride_details users stores product_details greetings \
  > /tmp/legacy_data_export.sql
```

#### Step 2: Transfer to Production Server

```bash
# On Pre-Prod Server
scp /tmp/legacy_data_export.sql root@10.10.63.97:/opt/tanishq/legacy_data_export.sql
```

#### Step 3: Import to Production Database

```bash
# On Production Server
mysql -u root -p selfie_prod < /opt/tanishq/legacy_data_export.sql
```

---

## OPTION 3: Application-Based Migration

### Let Your Application Import from Google Sheets on First Run

This option uses your existing Google Sheets integration code to read and migrate data.

#### Step 1: Create Migration Controller/Service

You'll need to create a one-time migration endpoint that:
1. Reads data from Google Sheets
2. Transforms it to MySQL entity format
3. Saves to MySQL database

#### Step 2: Run Migration Endpoint

```bash
# Call the migration endpoint once
curl -X POST http://localhost:3000/api/admin/migrate-legacy-data
```

---

## Table Mapping Guide

### MySQL Tables vs Google Sheets Structure

| MySQL Table | Google Sheet | Key Fields to Map |
|-------------|--------------|-------------------|
| `events` | Events Details | event_id, event_name, start_date, store_code |
| `attendees` | Events Attendees | name, email, phone, event_id |
| `invitees` | Events Invitees | name, email, phone, event_id |
| `bride_details` | Bride Details | brideName, email, phone, date, brideType |
| `users` | User Details | username, email, name, password, role |
| `stores` | Store Details | storeCode, storeName, region, city |
| `product_details` | Product Details | productName, category, price |

---

## Fix Missing Tables Issue

You got error: `Table 'selfie_prod.attendees' doesn't exist`

### Solution: Let Spring Boot Auto-Create Tables

```bash
# On Production Server
# Stop the application
kill $(cat /opt/tanishq/tanishq-prod.pid)

# Clear logs
> /opt/tanishq/logs/application.log

# Start application (it will auto-create missing tables)
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > /opt/tanishq/logs/application.log 2>&1 &

echo $! > /opt/tanishq/tanishq-prod.pid

# Wait 30 seconds for app to start
sleep 30

# Verify all tables are created
mysql -u root -p -e "USE selfie_prod; SHOW TABLES;"
```

---

## Recommended Approach for You

Based on your situation, I recommend **OPTION 2** (Copy from Pre-Prod):

### Why?
1. ✅ Pre-prod is already working with MySQL
2. ✅ Data structure is already correct
3. ✅ Fastest and safest method
4. ✅ No need to handle CSV transformations

### Steps:
1. Export data from pre-prod MySQL
2. Transfer SQL file to production
3. Import to production MySQL
4. Verify data
5. Restart application

---

## Verification After Migration

```bash
# On Production Server
mysql -u root -p -e "
USE selfie_prod;
SELECT 'events' as table_name, COUNT(*) as count FROM events
UNION ALL
SELECT 'attendees', COUNT(*) FROM attendees
UNION ALL
SELECT 'invitees', COUNT(*) FROM invitees
UNION ALL
SELECT 'bride_details', COUNT(*) FROM bride_details
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'stores', COUNT(*) FROM stores
UNION ALL
SELECT 'product_details', COUNT(*) FROM product_details
UNION ALL
SELECT 'greetings', COUNT(*) FROM greetings;
"
```

---

## Important Notes

### 1. Backup First!
```bash
# Before any migration, backup current production database
mysqldump -u root -p selfie_prod > /opt/tanishq/backup_before_migration_$(date +%Y%m%d_%H%M%S).sql
```

### 2. Stop Application During Migration
```bash
# Stop app to prevent data conflicts
kill $(cat /opt/tanishq/tanishq-prod.pid)
```

### 3. File Uploads / Images
If you have images stored with Google Sheets data:
- Check if image paths need to be updated
- Images might be in Google Drive or local storage
- Update paths to point to `/opt/tanishq/storage/`

### 4. Data Validation
After migration, test:
- User login works
- Events are displayed correctly
- Attendee/Invitee data is linked to events
- Bride details are accessible

---

## Troubleshooting

### Problem: Foreign Key Constraints
If you get foreign key errors during import:

```sql
SET FOREIGN_KEY_CHECKS=0;
-- Run your import
SET FOREIGN_KEY_CHECKS=1;
```

### Problem: Duplicate Data
If some data is already in production:

```sql
-- Delete existing data before import
TRUNCATE TABLE attendees;
TRUNCATE TABLE invitees;
TRUNCATE TABLE events;
-- etc.
```

### Problem: Character Encoding Issues
If you see weird characters:

```sql
-- Ensure UTF-8 encoding
ALTER DATABASE selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## Next Steps After Migration

1. ✅ Verify all data is imported
2. ✅ Test application functionality
3. ✅ Update Nginx/proxy configuration
4. ✅ Point domain to new database-backed application
5. ✅ Monitor logs for any issues
6. ✅ Disable Google Sheets sync (if any)

---

## Need Help?

Run the automated migration script (see `import_legacy_data.sh`) or follow OPTION 2 for the safest approach.

