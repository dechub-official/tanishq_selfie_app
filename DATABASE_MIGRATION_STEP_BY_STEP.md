# Database Migration - Step by Step

## Simple Guide to Move Your Database from Localhost to Pre-Prod Server

---

## What You Need

- ✅ Windows machine with MySQL installed (your local development)
- ✅ Access to pre-prod server (10.160.128.94)
- ✅ FortiClient VPN credentials
- ✅ WinSCP installed
- ✅ PuTTY installed
- ✅ Pre-prod database password

---

## Step 1: Export Your Local Database

### On Your Windows Machine:

1. Open File Explorer
2. Navigate to: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app`
3. Double-click: `export_database_for_preprod.bat`
4. Wait for the export to complete (you'll see "SUCCESS" message)
5. The file will be created in: `database_backup\tanishq_backup_20251202_HHMMSS.sql`

**That's it for Step 1!**

---

## Step 2: Connect to VPN

1. Open **FortiClient VPN**
2. Enter your credentials
3. Click **Connect**
4. Wait until connected

**Keep VPN connected for Steps 3 and 4!**

---

## Step 3: Upload SQL File to Server

### Using WinSCP:

1. **Open WinSCP**

2. **Create/Select Connection:**
   - File Protocol: SFTP
   - Hostname: `10.160.128.94`
   - Port: 22
   - Username: `nishal`
   - Password: Leave empty (use private key)
   - Private key: Browse to your private key file

3. **Click Login**

4. **Navigate:**
   - Left panel (your computer): Go to `database_backup\` folder
   - Right panel (server): Go to `/tmp/` folder

5. **Upload:**
   - Drag and drop the SQL file from left to right
   - Or select file and click "Upload" button
   - Wait for upload to complete

6. **Verify:**
   - You should see the file in `/tmp/` on the server side
   - Note the filename (you'll need it in Step 4)

**Keep WinSCP open (you might need it)**

---

## Step 4: Import Database on Server

### Using PuTTY:

1. **Open PuTTY**

2. **Connect to Server:**
   - Hostname: `10.160.128.94`
   - Port: 22
   - Click **Open**
   - Login as: `nishal`
   - Enter your password or use private key

3. **Navigate to temp directory:**
   ```bash
   cd /tmp
   ```

4. **List files to verify your upload:**
   ```bash
   ls -lh tanishq_backup_*.sql
   ```
   You should see your SQL file listed.

5. **Import the database:**
   ```bash
   mysql -u tanishq_preprod -p tanishq_preprod < tanishq_backup_20251202_143022.sql
   ```
   Replace `tanishq_backup_20251202_143022.sql` with your actual filename.

6. **Enter password when prompted:**
   Type your pre-prod database password and press Enter.

7. **Wait for import to complete:**
   This may take a few seconds to several minutes depending on data size.
   When you see the command prompt again, import is complete.

---

## Step 5: Verify the Import

### Still in PuTTY:

1. **Login to MySQL:**
   ```bash
   mysql -u tanishq_preprod -p tanishq_preprod
   ```
   Enter password when prompted.

2. **Check tables:**
   ```sql
   SHOW TABLES;
   ```
   You should see all your tables listed.

3. **Check some data:**
   ```sql
   SELECT COUNT(*) FROM stores;
   SELECT COUNT(*) FROM events;
   SELECT COUNT(*) FROM attendees;
   ```
   The counts should match your local database.

4. **Exit MySQL:**
   ```sql
   EXIT;
   ```

---

## Step 6: Cleanup

### Still in PuTTY:

1. **Remove the SQL file from server:**
   ```bash
   rm /tmp/tanishq_backup_*.sql
   ```

2. **Verify it's deleted:**
   ```bash
   ls /tmp/tanishq_backup_*.sql
   ```
   You should see "No such file or directory"

3. **Exit PuTTY:**
   ```bash
   exit
   ```

**Done!**

---

## Common Issues and Solutions

### Issue 1: Export script says "mysqldump not found"

**Solution:**
1. Find where MySQL is installed (usually `C:\Program Files\MySQL\MySQL Server 8.0\`)
2. Edit `export_database_for_preprod.bat`
3. Add this line at the top:
   ```cmd
   set PATH=%PATH%;C:\Program Files\MySQL\MySQL Server 8.0\bin
   ```

---

### Issue 2: Import fails with "Access denied"

**Solution:**
1. Check you're using the correct password
2. On server, verify user has permissions:
   ```bash
   mysql -u root -p
   ```
   ```sql
   GRANT ALL PRIVILEGES ON tanishq_preprod.* TO 'tanishq_preprod'@'localhost';
   FLUSH PRIVILEGES;
   EXIT;
   ```

---

### Issue 3: Database name mismatch error

**Solution:**
The SQL file creates database named `tanishq` but you need `tanishq_preprod`.

**Before uploading in Step 3:**
1. Open the SQL file in Notepad++
2. Find: `CREATE DATABASE` and `tanishq`
3. Replace: `CREATE DATABASE `tanishq`` with `CREATE DATABASE `tanishq_preprod``
4. Find: `USE `tanishq``
5. Replace with: `USE `tanishq_preprod``
6. Save the file
7. Then upload and import

---

### Issue 4: Can't connect to VPN

**Solution:**
1. Check your VPN credentials
2. Check internet connection
3. Contact your network administrator

---

### Issue 5: WinSCP can't connect

**Solution:**
1. Make sure VPN is connected
2. Check hostname: `10.160.128.94`
3. Check username: `nishal`
4. Make sure you're using the correct private key
5. Try with password if key doesn't work

---

## Verification Checklist

After completing all steps, verify:

- [ ] SQL file was created in `database_backup\` folder
- [ ] File was uploaded to server `/tmp/` folder
- [ ] Import command completed without errors
- [ ] `SHOW TABLES;` displays all tables
- [ ] Row counts match your local database
- [ ] SQL file deleted from server `/tmp/`
- [ ] VPN disconnected (if not needed)

---

## What to Do Next

After successful database migration:

1. **Update application configuration:**
   - Edit `src\main\resources\application-preprod.properties`
   - Set the database password:
     ```properties
     spring.datasource.password=YOUR_ACTUAL_PASSWORD
     ```

2. **Deploy application:**
   - Follow `PREPROD_DEPLOYMENT_CHECKLIST.md`
   - Build and deploy your application

3. **Test:**
   - Access: http://celebrations-preprod.tanishq.co.in/
   - Login and test features
   - Verify data is correct

---

## Quick Command Reference

### Export (Windows):
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
export_database_for_preprod.bat
```

### Import (Linux Server):
```bash
cd /tmp
mysql -u tanishq_preprod -p tanishq_preprod < tanishq_backup_*.sql
```

### Verify (Linux Server):
```bash
mysql -u tanishq_preprod -p tanishq_preprod
SHOW TABLES;
SELECT COUNT(*) FROM stores;
EXIT;
```

### Cleanup (Linux Server):
```bash
rm /tmp/tanishq_backup_*.sql
```

---

## Time Estimate

- **Step 1 (Export):** 2-5 minutes
- **Step 2 (VPN):** 1 minute
- **Step 3 (Upload):** 2-10 minutes (depends on file size and network)
- **Step 4 (Import):** 2-10 minutes (depends on data size)
- **Step 5 (Verify):** 2 minutes
- **Step 6 (Cleanup):** 1 minute

**Total:** 10-30 minutes

---

## Need More Help?

- **Quick Reference:** `DATABASE_MIGRATION_QUICK_REF.md`
- **Complete Guide:** `DATABASE_MIGRATION_GUIDE.md`
- **Overall Setup:** `README_PREPROD.md`

---

**Guide Version:** 1.0  
**Created:** December 2, 2025  
**For:** Database Migration - Localhost to Pre-Production

---

**Remember:** Keep your local database backup safe. Don't delete it until you've verified the pre-prod database is working correctly!

