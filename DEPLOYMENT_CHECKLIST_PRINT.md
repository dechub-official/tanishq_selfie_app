# ✅ Pre-Prod Deployment Checklist

**Print this and check off as you go!**

---

## 🟦 PHASE 1: LOCAL SETUP (Your Computer)

### Verification
- [ ] Run `verify_local_readiness.bat`
- [ ] Java 11 verified: `java -version`
- [ ] Maven verified: `mvn -version`

### Configuration
- [ ] Open `src\main\resources\application-preprod.properties`
- [ ] Update line: `spring.datasource.password=YOUR_ACTUAL_PASSWORD`
- [ ] Save file

### Frontend Files
- [ ] Frontend built (npm run build or similar)
- [ ] All files from `frontend/build` or `dist` copied to:
  - `src\main\resources\static\`
- [ ] Verify `index.html` exists in static folder

### Build Configuration
- [ ] Open `pom.xml`
- [ ] Update `<artifactId>` to: `tanishq-02-12-2025-1`
  - Format: `tanishq-DD-MM-YYYY-N` (N = deployment number)
- [ ] Save file

### Build Application
- [ ] Run: `build-preprod.bat` (or `mvn clean install`)
- [ ] Wait for "BUILD SUCCESS"
- [ ] Verify WAR file created in `target\` folder
- [ ] Note WAR filename: `_______________________________`

### Database Export (Optional)
- [ ] If migrating data: Run `export_database_for_preprod.bat`
- [ ] Verify .sql file created
- [ ] Or: Skip if database is empty/new

---

## 🟨 PHASE 2: SERVER SETUP (One-Time Only!)

### Connect to Server
- [ ] Open FortiClient VPN
- [ ] Connect to VPN (wait for "Connected")
- [ ] Open PuTTY
- [ ] Connect to: `10.160.128.94`
- [ ] Login with username: `nishal`
- [ ] Switch to root: `sudo su root`

### Create Directories
```bash
mkdir -p /opt/tanishq/applications_preprod
mkdir -p /opt/tanishq/storage/selfie_images
chmod -R 755 /opt/tanishq/storage
chmod -R 755 /opt/tanishq/applications_preprod
chown -R nishal:nishal /opt/tanishq
```

- [ ] Run commands above
- [ ] Verify: `ls -la /opt/tanishq/`

### Verify Java
- [ ] Run: `java -version`
- [ ] Confirm Java 11.x.x
- [ ] If not installed:
  ```bash
  yum install java-11-openjdk -y
  # OR
  apt-get install openjdk-11-jdk -y
  ```

### Database Verification
- [ ] Run: `mysql -u root -p`
- [ ] Run: `SHOW DATABASES;`
- [ ] Confirm `tanishq_preprod` exists
- [ ] If not, create it:
  ```sql
  CREATE DATABASE tanishq_preprod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  CREATE USER 'tanishq_preprod'@'localhost' IDENTIFIED BY 'YourPassword';
  GRANT ALL PRIVILEGES ON tanishq_preprod.* TO 'tanishq_preprod'@'localhost';
  FLUSH PRIVILEGES;
  EXIT;
  ```
- [ ] Note password used: `_______________________________`

### Upload Google Service Key
- [ ] Open WinSCP
- [ ] Connect to: `10.160.128.94` (user: nishal)
- [ ] Navigate to: `/opt/tanishq/`
- [ ] Upload file: `tanishqgmb-5437243a8085.p12`
  - From: `src\main\resources\tanishqgmb-5437243a8085.p12`
- [ ] In PuTTY, set permissions:
  ```bash
  chmod 644 /opt/tanishq/tanishqgmb-5437243a8085.p12
  chown nishal:nishal /opt/tanishq/tanishqgmb-5437243a8085.p12
  ```
- [ ] Verify: `ls -la /opt/tanishq/tanishqgmb-5437243a8085.p12`

### Upload Verification Script (Optional)
- [ ] In WinSCP, upload `verify_server_readiness.sh` to `/tmp/`
- [ ] In PuTTY:
  ```bash
  chmod +x /tmp/verify_server_readiness.sh
  /tmp/verify_server_readiness.sh
  ```
- [ ] Review output, fix any errors

### Import Database (If you have data)
- [ ] Upload .sql file to `/tmp/` via WinSCP
- [ ] In PuTTY:
  ```bash
  mysql -u tanishq_preprod -p tanishq_preprod < /tmp/your-backup-file.sql
  ```
- [ ] Verify tables:
  ```bash
  mysql -u tanishq_preprod -p tanishq_preprod -e "SHOW TABLES;"
  ```

---

## 🟩 PHASE 3: DEPLOYMENT

### Upload WAR File
- [ ] Open WinSCP (if not already open)
- [ ] Navigate to: `/opt/tanishq/applications_preprod/`
- [ ] Upload WAR file from: `target\tanishq-02-12-2025-1-0.0.1-SNAPSHOT.war`
- [ ] Wait for upload to complete

### Upload Deployment Script (Optional)
- [ ] Upload `deploy-preprod.sh` to `/opt/tanishq/applications_preprod/`
- [ ] In PuTTY:
  ```bash
  cd /opt/tanishq/applications_preprod
  chmod +x deploy-preprod.sh
  ```

### Start Application

**Option A: Automated (using script)**
- [ ] Run: `./deploy-preprod.sh`
- [ ] Watch logs for startup

**Option B: Manual**
- [ ] In PuTTY, navigate:
  ```bash
  cd /opt/tanishq/applications_preprod
  ```
- [ ] Check for old processes:
  ```bash
  ps -ef | grep tanishq
  ps -ef | grep selfie
  ```
- [ ] Kill old process if exists:
  ```bash
  kill <process_id>
  ```
- [ ] Start new application:
  ```bash
  nohup java -jar tanishq-02-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > tanishq-02-12-2025-1-0.0.1-SNAPSHOT.log 2>&1 &
  ```
- [ ] Note process ID: `_______________________________`

### Monitor Startup
- [ ] Run: `tail -f tanishq-02-12-2025-1-0.0.1-SNAPSHOT.log`
- [ ] Wait for: "Started TanishqApplication in X seconds"
- [ ] Wait for: "Tomcat started on port(s): 3002"
- [ ] Press Ctrl+C (application keeps running)

### Verify Running
- [ ] Check process: `ps -ef | grep tanishq`
- [ ] Check port: `netstat -tulpn | grep 3002`
- [ ] Test locally: `curl http://localhost:3002`

---

## 🟪 PHASE 4: VERIFICATION & TESTING

### Basic Connectivity
- [ ] From server: `curl http://localhost:3002`
- [ ] From your browser: `http://10.160.128.94:3002`
- [ ] From your browser: `http://celebrations-preprod.tanishq.co.in`

### Application Testing
- [ ] Homepage loads
- [ ] Can access login page
- [ ] Can login with valid credentials
- [ ] Can navigate through pages

### Database Testing
- [ ] Create a test entry in application
- [ ] In PuTTY, verify data saved:
  ```bash
  mysql -u tanishq_preprod -p tanishq_preprod -e "SELECT * FROM your_table LIMIT 5;"
  ```

### Image Upload Testing
- [ ] Upload a test image in application
- [ ] In PuTTY, verify file saved:
  ```bash
  ls -la /opt/tanishq/storage/selfie_images/
  ```

### Google Sheets Testing (if applicable)
- [ ] Perform action that updates Google Sheets
- [ ] Check Google Sheet to verify data appeared
- [ ] Check logs for any Google API errors

### Log Review
- [ ] Review logs for errors:
  ```bash
  tail -100 tanishq-02-12-2025-1-0.0.1-SNAPSHOT.log
  ```
- [ ] No critical errors (ERROR, FATAL)

---

## 🎯 FINAL CHECKS

### Functionality
- [ ] Login works
- [ ] All main features work
- [ ] Images upload successfully
- [ ] Data saves to database
- [ ] Google Sheets integration works (if used)

### Performance
- [ ] Pages load reasonably fast
- [ ] No obvious performance issues

### Logs
- [ ] No critical errors in logs
- [ ] Application logs show successful operations

### Documentation
- [ ] Document any server-specific changes made
- [ ] Note any issues encountered and solutions
- [ ] Save credentials/passwords securely

---

## 📝 POST-DEPLOYMENT

### Share Information
- [ ] Share pre-prod URL with testing team
- [ ] Share any test credentials
- [ ] Share known issues/limitations

### Monitoring Setup
- [ ] Know how to check logs
- [ ] Know how to restart application
- [ ] Know how to check if process is running

### Future Deployments
- [ ] Bookmark this checklist for next time
- [ ] Note: Future deployments only need Phase 3 & 4

---

## 🆘 TROUBLESHOOTING REFERENCE

### Application Won't Start
1. Check logs: `tail -100 <log-file>`
2. Check port in use: `netstat -tulpn | grep 3002`
3. Check database password in properties file
4. Check Google .p12 file exists and has correct permissions

### Can't Access from Browser
1. Check Nginx running: `systemctl status nginx`
2. Check firewall: `firewall-cmd --list-all`
3. Contact network team about DNS configuration

### Database Connection Error
1. Verify database exists: `SHOW DATABASES;`
2. Verify user permissions: `SHOW GRANTS FOR 'tanishq_preprod'@'localhost';`
3. Check password in application-preprod.properties

### Images Not Uploading
1. Check directory exists: `ls -la /opt/tanishq/storage/selfie_images`
2. Check permissions: `chmod -R 755 /opt/tanishq/storage`
3. Check disk space: `df -h`

---

## ✅ DEPLOYMENT COMPLETE!

**Congratulations! Your pre-prod environment is live!**

**Website:** http://celebrations-preprod.tanishq.co.in

**Important Commands to Remember:**

```bash
# View logs
tail -f /opt/tanishq/applications_preprod/<log-file>

# Check process
ps -ef | grep tanishq

# Restart application
cd /opt/tanishq/applications_preprod
./deploy-preprod.sh

# Check database
mysql -u tanishq_preprod -p tanishq_preprod
```

---

**Date Completed:** _______________  
**Deployed By:** _______________  
**WAR File:** _______________  
**Process ID:** _______________

---

**For future deployments, only follow Phase 3 & 4!**

