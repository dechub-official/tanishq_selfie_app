# 🚀 COMPLETE PRE-PROD DEPLOYMENT GUIDE - FROM SCRATCH

**Version:** 1.0  
**Date:** December 3, 2025  
**Environment:** Pre-Production  
**Domain:** http://celebrations-preprod.tanishq.co.in  
**Server IP:** 10.160.128.94

---

## 📋 TABLE OF CONTENTS

1. [Prerequisites](#1-prerequisites)
2. [Local Preparation](#2-local-preparation)
3. [Server Access Setup](#3-server-access-setup)
4. [Database Setup](#4-database-setup)
5. [Server Directory Structure](#5-server-directory-structure)
6. [File Uploads](#6-file-uploads)
7. [Application Configuration](#7-application-configuration)
8. [Build WAR File](#8-build-war-file)
9. [Deploy Application](#9-deploy-application)
10. [Nginx Configuration](#10-nginx-configuration)
11. [Security Configuration](#11-security-configuration)
12. [Start Application](#12-start-application)
13. [AWS Security Group](#13-aws-security-group)
14. [DNS Configuration](#14-dns-configuration)
15. [Verification](#15-verification)
16. [Troubleshooting](#16-troubleshooting)
17. [Maintenance](#17-maintenance)

---

## 1. PREREQUISITES

### 1.1 Required Software on Your Computer

**Install these on your Windows machine:**

```
✓ Java 11 (OpenJDK or Oracle JDK)
  Download: https://adoptium.net/
  
✓ Maven 3.6+
  Download: https://maven.apache.org/download.cgi
  
✓ Git (optional, if using version control)
  Download: https://git-scm.com/
  
✓ WinSCP (for file transfer)
  Download: https://winscp.net/
  
✓ PuTTY (for SSH access)
  Download: https://www.putty.org/
  
✓ FortiClient VPN (if required)
  Get from your IT team
```

**Verify Installation:**
```cmd
# Open Command Prompt and verify:
java -version     # Should show Java 11
mvn -version      # Should show Maven 3.x
```

### 1.2 Access Requirements

**You need access to:**
```
✓ Pre-prod server (10.160.128.94)
  - SSH access
  - Root/sudo privileges
  - User: jewdev-test
  
✓ VPN (if server not directly accessible)
  - FortiClient VPN credentials
  
✓ AWS Console or AWS Team contact
  - For Security Group modifications
  
✓ Network/DNS Team contact
  - For DNS configuration
  
✓ MySQL root password
  - For database setup
```

### 1.3 Project Files

**Ensure you have:**
```
✓ Complete project source code
  Location: C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\
  
✓ Frontend build (static files)
  Location: C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\static\
  
✓ Google Service Account Keys (.p12 files)
  - tanishqgmb-5437243a8085.p12
  - event-images-469618-32e65f6d62b3.p12
```

---

## 2. LOCAL PREPARATION

### 2.1 Configure Application Properties

**File:** `src\main\resources\application-preprod.properties`

**Open and configure these properties:**

```properties
# Server Configuration
server.port=3002

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod
spring.datasource.username=root
spring.datasource.password=Dechub#2025

# File Paths (Server paths - Linux)
dechub.tanishq.key.filepath=/opt/tanishq/tanishqgmb-5437243a8085.p12
dechub.tanishq.key.filepath.event=/opt/tanishq/event-images-469618-32e65f6d62b3.p12
selfie.upload.dir=/opt/tanishq/storage/selfie_images
dechub.bride.upload.dir=/opt/tanishq/storage/bride_uploads
dechub.base.image=/opt/tanishq/storage/base.jpg
store.details.excel.sheet=/opt/tanishq/tanishq_selfie_app_store_data.xlsx

# System Configuration
system.isWindows=N

# QR Code URL
qr.code.base.url=http://celebrations-preprod.tanishq.co.in/events/customer/

# All other properties (Google Sheets, Email, etc.) - keep as is
```

**File Location:**
```
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\application-preprod.properties
```

### 2.2 Update pom.xml Version

**File:** `pom.xml`

**Update the artifactId with today's date:**

```xml
<artifactId>tanishq-preprod-03-12-2025-1</artifactId>
```

**Change the date to current deployment date:**
- Format: tanishq-preprod-DD-MM-YYYY-N
- N = deployment number for that day (1, 2, 3, etc.)

**File Location:**
```
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\pom.xml
```

### 2.3 Verify Frontend Files

**Check static files exist:**

```
Location: src\main\resources\static\

Required files:
✓ index.html
✓ assets/ folder
✓ globalPage/ folder
✓ qr/ folder
✓ All CSS/JS files
✓ Images and fonts
```

**If missing:** Copy from frontend build to this location

---

## 3. SERVER ACCESS SETUP

### 3.1 Connect to VPN (if required)

**Using FortiClient VPN:**

1. Open FortiClient VPN
2. Enter connection details:
   ```
   Remote Gateway: [Your VPN Server]
   Username: [Your VPN Username]
   Password: [Your VPN Password]
   ```
3. Click "Connect"
4. Wait for "Connected" status

### 3.2 Test Server Connectivity

**Open Command Prompt:**

```cmd
# Test if server is reachable
ping 10.160.128.94

# Expected: Replies from 10.160.128.94
```

### 3.3 SSH to Server (PuTTY)

**Configure PuTTY:**

1. Open PuTTY
2. Enter settings:
   ```
   Host Name: 10.160.128.94
   Port: 22
   Connection Type: SSH
   ```
3. Click "Open"
4. Login:
   ```
   login as: jewdev-test
   Password: [Your SSH Password or use Private Key]
   ```

**If using Private Key:**
- Go to Connection > SSH > Auth
- Browse and select your private key (.ppk file)
- Then connect

**Success:** You should see prompt:
```
[jewdev-test@ip-10-160-128-94 ~]$
```

### 3.4 Switch to Root User

**In PuTTY terminal:**

```bash
sudo su
# Or: sudo su -
```

**Success:** Prompt changes to:
```
[root@ip-10-160-128-94 jewdev-test]#
```

---

## 4. DATABASE SETUP

### 4.1 Access MySQL

**In PuTTY (as root):**

```bash
mysql -u root -p
```

**Enter MySQL root password:** `Dechub#2025`

**Success:** You should see:
```
mysql>
```

### 4.2 Create Database

**In MySQL prompt:**

```sql
-- Create database
CREATE DATABASE IF NOT EXISTS selfie_preprod 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Verify database created
SHOW DATABASES;

-- Expected output should include:
-- selfie_preprod

-- Use the database
USE selfie_preprod;

-- Check tables (should be empty initially)
SHOW TABLES;

-- Exit MySQL
EXIT;
```

### 4.3 Verify Database Connection

**Test connection:**

```bash
mysql -u root -p selfie_preprod -e "SELECT DATABASE();"
```

**Expected output:**
```
+------------+
| DATABASE() |
+------------+
| selfie_preprod |
+------------+
```

---

## 5. SERVER DIRECTORY STRUCTURE

### 5.1 Create Main Directory Structure

**In PuTTY (as root):**

```bash
# Create main application directory
mkdir -p /opt/tanishq/applications_preprod

# Create storage directories
mkdir -p /opt/tanishq/storage/selfie_images
mkdir -p /opt/tanishq/storage/bride_uploads

# Create placeholder files
touch /opt/tanishq/storage/base.jpg

# Set ownership to jewdev-test
chown -R jewdev-test:jewdev-test /opt/tanishq/

# Set permissions
chmod -R 755 /opt/tanishq/

# Verify directory structure
ls -la /opt/tanishq/
```

**Expected output:**
```
drwxr-xr-x. applications_preprod
drwxr-xr-x. storage
```

### 5.2 Verify Storage Structure

```bash
ls -la /opt/tanishq/storage/
```

**Expected output:**
```
drwxr-xr-x. selfie_images
drwxr-xr-x. bride_uploads
-rwxr-xr-x. base.jpg
```

---

## 6. FILE UPLOADS

### 6.1 Configure WinSCP

**Open WinSCP:**

1. Click "New Site"
2. Enter connection details:
   ```
   File protocol: SFTP
   Host name: 10.160.128.94
   Port number: 22
   User name: jewdev-test
   Password: [Your password or use Private key]
   ```
3. Click "Login"

**If using Private Key:**
- Click "Advanced"
- Go to SSH > Authentication
- Select your private key file
- Click OK, then Login

**Success:** You should see two panels:
- Left: Your local computer
- Right: Server (/home/jewdev-test/)

### 6.2 Upload Google Service Account Keys

**In WinSCP:**

**Step 1: Upload tanishqgmb .p12 file**

1. **Left panel (local):** Navigate to:
   ```
   C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\
   ```

2. Find file: `tanishqgmb-5437243a8085.p12`

3. **Right panel (server):** Navigate to:
   ```
   /home/jewdev-test/
   ```

4. **Drag and drop** the .p12 file from left to right

5. **Wait** for upload to complete

**Step 2: Upload event-images .p12 file**

1. **Left panel:** Same location
2. Find file: `event-images-469618-32e65f6d62b3.p12`
3. **Right panel:** `/home/jewdev-test/`
4. **Drag and drop** from left to right
5. **Wait** for upload to complete

### 6.3 Move .p12 Files to Correct Location

**In PuTTY (as root):**

```bash
# Move files from home directory to /opt/tanishq/
mv /home/jewdev-test/tanishqgmb-5437243a8085.p12 /opt/tanishq/
mv /home/jewdev-test/event-images-469618-32e65f6d62b3.p12 /opt/tanishq/

# Set correct permissions
chmod 644 /opt/tanishq/tanishqgmb-5437243a8085.p12
chmod 644 /opt/tanishq/event-images-469618-32e65f6d62b3.p12

# Verify files are in place
ls -la /opt/tanishq/*.p12
```

**Expected output:**
```
-rw-r--r--. event-images-469618-32e65f6d62b3.p12
-rw-r--r--. tanishqgmb-5437243a8085.p12
```

---

## 7. APPLICATION CONFIGURATION

### 7.1 Final Configuration Check

**On your local machine:**

**Verify application-preprod.properties has:**

```properties
# All paths use Linux format (forward slashes)
# All paths start with /opt/tanishq/
# Database password is correct: Dechub#2025
# server.port=3002
# system.isWindows=N
# qr.code.base.url=http://celebrations-preprod.tanishq.co.in/events/customer/
```

**File to check:**
```
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\application-preprod.properties
```

### 7.2 Verify pom.xml

**Check:**
```xml
<artifactId>tanishq-preprod-03-12-2025-1</artifactId>
```

**File location:**
```
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\pom.xml
```

---

## 8. BUILD WAR FILE

### 8.1 Build Using Maven

**Open Command Prompt on your computer:**

```cmd
# Navigate to project directory
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Clean previous builds
mvn clean

# Build WAR file (skip tests for faster build)
mvn install -DskipTests

# OR build with tests
mvn install
```

**Wait for build to complete (3-5 minutes)**

### 8.2 Verify Build Success

**Look for:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX:XX min
```

**Check WAR file created:**
```cmd
dir target\*.war
```

**Expected file:**
```
tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
```

**File size:** ~170-180 MB

**File location:**
```
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\target\tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
```

---

## 9. DEPLOY APPLICATION

### 9.1 Upload WAR File to Server

**In WinSCP:**

1. **Left panel (local):** Navigate to:
   ```
   C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\target\
   ```

2. Find WAR file:
   ```
   tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war
   ```

3. **Right panel (server):** Navigate to:
   ```
   /opt/tanishq/applications_preprod/
   ```
   
   **Note:** You may need to switch to root or use sudo. If you can't access /opt/tanishq/ directly:
   - Upload to `/home/jewdev-test/`
   - Then move using PuTTY commands (see step 9.2)

4. **Drag and drop** WAR file from left to right

5. **Wait** for upload to complete (2-3 minutes for 170MB file)

### 9.2 Move WAR File (if uploaded to home directory)

**If you uploaded to /home/jewdev-test/ first:**

**In PuTTY (as root):**

```bash
# Move WAR file to application directory
mv /home/jewdev-test/tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war /opt/tanishq/applications_preprod/

# Set ownership
chown jewdev-test:jewdev-test /opt/tanishq/applications_preprod/*.war

# Set permissions
chmod 644 /opt/tanishq/applications_preprod/*.war
```

### 9.3 Verify WAR File

**In PuTTY:**

```bash
ls -lh /opt/tanishq/applications_preprod/
```

**Expected output:**
```
-rw-r--r--. tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war (170-180M)
```

---

## 10. NGINX CONFIGURATION

### 10.1 Install Nginx

**In PuTTY (as root):**

```bash
# Install Nginx
yum install nginx -y

# Verify installation
nginx -v
```

**Expected output:**
```
nginx version: nginx/X.X.X
```

### 10.2 Create Nginx Configuration

**Create configuration file:**

```bash
cat > /etc/nginx/conf.d/celebrations-preprod.conf << 'EOF'
server {
    listen 80;
    server_name celebrations-preprod.tanishq.co.in 10.160.128.94 _;
    
    # Maximum upload size
    client_max_body_size 10M;
    
    # Logging
    access_log /var/log/nginx/celebrations-preprod-access.log;
    error_log /var/log/nginx/celebrations-preprod-error.log;
    
    # Proxy to application
    location / {
        proxy_pass http://localhost:3002;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # Timeouts
        proxy_connect_timeout 600;
        proxy_send_timeout 600;
        proxy_read_timeout 600;
        send_timeout 600;
    }
}
EOF
```

### 10.3 Test Nginx Configuration

```bash
# Test configuration
nginx -t
```

**Expected output:**
```
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

**If you get errors:** Check the configuration file for typos

### 10.4 Enable and Start Nginx

```bash
# Enable Nginx to start on boot
systemctl enable nginx

# Start Nginx
systemctl start nginx

# Check status
systemctl status nginx
```

**Expected output:**
```
● nginx.service - The nginx HTTP and reverse proxy server
   Active: active (running)
```

**Press Ctrl+C** to exit status view

### 10.5 Verify Nginx is Listening

```bash
# Check port 80 is listening
netstat -tuln | grep :80
```

**Expected output:**
```
tcp  0  0  0.0.0.0:80  0.0.0.0:*  LISTEN
```

---

## 11. SECURITY CONFIGURATION

### 11.1 Configure SELinux

**Check SELinux status:**

```bash
getenforce
```

**If output is "Enforcing":**

```bash
# Allow Nginx to connect to backend application
setsebool -P httpd_can_network_connect 1

# Allow port 3002
semanage port -a -t http_port_t -p tcp 3002

# If semanage command not found, install it:
yum install policycoreutils-python-utils -y

# Then run the semanage command again
semanage port -a -t http_port_t -p tcp 3002

# Verify port is added
semanage port -l | grep http_port_t
```

**Expected output should include:**
```
http_port_t  tcp  3002, 80, 443, ...
```

**If output is "Permissive":**
- No action needed for now
- For production, you should set it back to Enforcing after testing

### 11.2 Firewall Configuration (if applicable)

**Check if firewalld is installed:**

```bash
systemctl status firewalld
```

**If firewalld is active:**

```bash
# Open port 3002
firewall-cmd --permanent --add-port=3002/tcp

# Open port 80
firewall-cmd --permanent --add-service=http

# Reload firewall
firewall-cmd --reload

# Verify
firewall-cmd --list-all
```

**If firewalld is not found:**
- No local firewall configured
- Security is managed by AWS Security Group (covered in Step 13)

---

## 12. START APPLICATION

### 12.1 Navigate to Application Directory

**In PuTTY (as root):**

```bash
cd /opt/tanishq/applications_preprod
```

### 12.2 Start Application

**Start the application in background:**

```bash
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > app.log 2>&1 &
```

**Expected output:**
```
[1] 12345    # Process ID (your number will be different)
```

**Note down the Process ID!**

### 12.3 Monitor Application Startup

**Watch the log file:**

```bash
tail -f app.log
```

**Watch for these messages (in order):**

```
Starting TanishqSelfieApplication...
✓ The following 1 profile is active: "preprod"
✓ Tomcat initialized with port(s): 3002 (http)
✓ HikariPool-1 - Start completed (Database connected)
✓ Initialized JPA EntityManagerFactory for persistence unit 'default'
✓ Tomcat started on port(s): 3002 (http)
✓ Started TanishqSelfieApplication in X seconds  ← MAIN SUCCESS!
⚠ WARNING: Could not load store details from Excel (This is OK!)
```

**When you see "Started TanishqSelfieApplication":**
- Press **Ctrl+C** to exit tail
- Application is now running in background!

**If you see errors:**
- Check the troubleshooting section (Section 16)

### 12.4 Verify Application is Running

**Check the process:**

```bash
ps -ef | grep tanishq
```

**Expected output:**
```
root  12345  ...  java -jar tanishq-preprod-03-12-2025-1...
```

**Check port is listening:**

```bash
netstat -tuln | grep 3002
```

**Expected output:**
```
tcp6  0  0  :::3002  :::*  LISTEN
```

### 12.5 Test Local Access

**Test application responds:**

```bash
curl http://localhost:3002
```

**Expected:** Should return HTML content (starts with `<!doctype html>`)

**Test Nginx proxy:**

```bash
curl http://localhost
```

**Expected:** Should return same HTML content

**Success!** Application is running and Nginx is proxying correctly!

---

## 13. AWS SECURITY GROUP

### 13.1 Identify Security Group

**Option A: Using AWS CLI (if installed on your computer)**

```bash
# Find instance details
aws ec2 describe-instances --filters "Name=private-ip-address,Values=10.160.128.94"
```

**Option B: Using AWS Console**

1. Login to AWS Console
2. Go to EC2 Dashboard
3. Click "Instances"
4. Search for: `10.160.128.94` or `ip-10-160-128-94`
5. Click on the instance
6. Go to "Security" tab
7. Note the Security Group ID (e.g., sg-xxxxxxxxx)

### 13.2 Add Inbound Rule for Port 80

**Option A: Using AWS Console**

1. Click on the Security Group name (it's a hyperlink)
2. Click "Edit inbound rules" button
3. Click "Add rule" button
4. Configure:
   ```
   Type: HTTP
   Protocol: TCP
   Port range: 80
   Source: 0.0.0.0/0
   Description: Tanishq Pre-Prod Application
   ```
5. Click "Save rules"

**Option B: Using AWS CLI**

```bash
# Add HTTP rule (replace sg-xxxxxxxxx with your Security Group ID)
aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxxxxxx \
    --protocol tcp \
    --port 80 \
    --cidr 0.0.0.0/0
```

### 13.3 Verify Security Group Rule

**In AWS Console:**
- Refresh the inbound rules page
- You should see the new rule for port 80

**Using AWS CLI:**

```bash
aws ec2 describe-security-groups --group-ids sg-xxxxxxxxx
```

### 13.4 Test External Access (Immediately)

**On your computer, open browser:**

```
http://10.160.128.94
```

**Expected:** Your application homepage should load! ✅

**If it doesn't load:**
- Wait 1-2 minutes and try again
- Check if you're still connected to VPN (if required)
- Verify Security Group rule was saved
- Check troubleshooting section

---

## 14. DNS CONFIGURATION

### 14.1 Contact Network Team

**Send email to Network/DNS team (use template from EMAIL_TEMPLATES.md):**

**Required DNS Record:**

```
Record Type: A
Hostname: celebrations-preprod.tanishq.co.in
IP Address: 10.160.128.94
TTL: 300
```

### 14.2 Wait for DNS Configuration

**Timeline:**
- Network team configuration: 10-60 minutes
- DNS propagation: 5-15 minutes
- **Total: 15-75 minutes**

### 14.3 Verify DNS Configuration

**After network team confirms, test DNS:**

**On your computer, open Command Prompt:**

```cmd
nslookup celebrations-preprod.tanishq.co.in
```

**Expected output:**
```
Server:  ...
Address:  ...

Name:    celebrations-preprod.tanishq.co.in
Address:  10.160.128.94
```

**Alternative test:**

```cmd
ping celebrations-preprod.tanishq.co.in
```

**Expected:**
```
Pinging celebrations-preprod.tanishq.co.in [10.160.128.94]...
```

### 14.4 Test Domain Access

**In browser:**

```
http://celebrations-preprod.tanishq.co.in
```

**Expected:** Your application homepage loads! ✅

---

## 15. VERIFICATION

### 15.1 Complete Functionality Test

**Test 1: Homepage**
```
URL: http://celebrations-preprod.tanishq.co.in
Expected: Homepage loads with "Let's Celebrate with Tanishq" banner
```

**Test 2: Wedding Checklist**
```
Click: "Wedding Checklist" button
Expected: Opens wedding checklist feature
```

**Test 3: Take Selfie**
```
Click: "Take Selfi" button
Expected: Opens selfie capture feature
```

**Test 4: Create Events**
```
Click: "Create Event" button
Expected: Opens event creation feature
```

**Test 5: Login Page**
```
Navigate to: http://celebrations-preprod.tanishq.co.in/events
Expected: Login page appears
```

### 15.2 Database Verification

**In PuTTY:**

```bash
# Check tables were created
mysql -u root -p selfie_preprod -e "SHOW TABLES;"
```

**Expected output (15 tables):**
```
+---------------------------+
| Tables_in_selfie_preprod  |
+---------------------------+
| abm_login                 |
| attendees                 |
| bride_details             |
| cee_login                 |
| events                    |
| greetings                 |
| invitees                  |
| password_history          |
| product_details           |
| rbm_login                 |
| rivaah                    |
| rivaah_users              |
| stores                    |
| user_details              |
| users                     |
+---------------------------+
```

### 15.3 Server Health Check

**Check application is running:**

```bash
ps -ef | grep tanishq
```

**Check ports:**

```bash
netstat -tuln | grep 3002  # Application
netstat -tuln | grep :80   # Nginx
```

**Check Nginx:**

```bash
systemctl status nginx
```

**Check logs for errors:**

```bash
# Application errors
grep ERROR /opt/tanishq/applications_preprod/app.log

# Nginx errors
tail -50 /var/log/nginx/celebrations-preprod-error.log
```

### 15.4 Performance Check

**Test response time:**

```bash
time curl -s http://localhost:3002 > /dev/null
```

**Expected:** Less than 2-3 seconds

### 15.5 Access from Multiple Devices

**Test access from:**
- ✓ Your computer
- ✓ Colleague's computer
- ✓ Mobile device
- ✓ Different network (if possible)

**All should be able to access:**
```
http://celebrations-preprod.tanishq.co.in
```

---

## 16. TROUBLESHOOTING

### 16.1 Application Won't Start

**Problem:** Application fails to start

**Check:**

```bash
# View full error
cat /opt/tanishq/applications_preprod/app.log | grep -A 20 "Caused by"

# Common issues:
# 1. Database connection failed
# 2. Missing .p12 file
# 3. Missing directory
# 4. Port already in use
```

**Solutions:**

**Database connection error:**
```bash
# Test MySQL connection
mysql -u root -p selfie_preprod

# Check password in application-preprod.properties
# Should be: Dechub#2025
```

**Missing .p12 file:**
```bash
# Verify files exist
ls -la /opt/tanishq/*.p12

# If missing, upload again (see Step 6.2)
```

**Missing directory:**
```bash
# Recreate directories
mkdir -p /opt/tanishq/storage/selfie_images
mkdir -p /opt/tanishq/storage/bride_uploads
chmod -R 755 /opt/tanishq/storage/
```

**Port already in use:**
```bash
# Find process using port 3002
netstat -tuln | grep 3002
ps -ef | grep 3002

# Kill old process
pkill -f tanishq

# Start application again
```

### 16.2 Cannot Access from Browser

**Problem:** Connection timeout when accessing from browser

**Check:**

```bash
# Application running?
ps -ef | grep tanishq

# Port listening?
netstat -tuln | grep 3002

# Nginx running?
systemctl status nginx

# Nginx listening on port 80?
netstat -tuln | grep :80

# Local access works?
curl http://localhost:3002
curl http://localhost
```

**Solutions:**

**If local access works but external doesn't:**
- AWS Security Group not configured (see Step 13)
- VPN required but not connected
- IP address blocked by corporate firewall

**If Nginx not running:**
```bash
systemctl start nginx
systemctl status nginx
```

**If application not running:**
```bash
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-preprod-*.war --spring.profiles.active=preprod > app.log 2>&1 &
```

### 16.3 DNS Not Resolving

**Problem:** Domain doesn't resolve to IP

**Check:**

```bash
# From your computer
nslookup celebrations-preprod.tanishq.co.in
```

**Solutions:**

**If no record found:**
- DNS not configured yet (contact Network team)
- DNS not propagated yet (wait 5-15 minutes)

**If shows different IP:**
- Wrong IP configured (contact Network team)

**If shows correct IP but can't access:**
- Clear browser cache
- Try incognito/private mode
- Try different browser
- Check AWS Security Group (Step 13)

### 16.4 502 Bad Gateway Error

**Problem:** Nginx shows 502 Bad Gateway

**This means:** Nginx is working but can't connect to application

**Check:**

```bash
# Is application running?
ps -ef | grep tanishq

# Is application on port 3002?
netstat -tuln | grep 3002

# Check application log
tail -100 /opt/tanishq/applications_preprod/app.log
```

**Solutions:**

**Application not running:**
```bash
# Start it
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-preprod-*.war --spring.profiles.active=preprod > app.log 2>&1 &
```

**Application on wrong port:**
- Check application-preprod.properties
- server.port should be 3002
- Rebuild and redeploy WAR file

**SELinux blocking:**
```bash
# Temporarily disable to test
setenforce 0

# If that fixes it, enable and configure properly:
setenforce 1
setsebool -P httpd_can_network_connect 1
```

### 16.5 Database Connection Errors

**Problem:** Application can't connect to database

**Check:**

```bash
# MySQL running?
systemctl status mysqld

# Can you connect?
mysql -u root -p selfie_preprod

# Database exists?
mysql -u root -p -e "SHOW DATABASES;"
```

**Solutions:**

**MySQL not running:**
```bash
systemctl start mysqld
systemctl enable mysqld
```

**Database doesn't exist:**
```bash
mysql -u root -p
CREATE DATABASE selfie_preprod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

**Wrong password:**
- Check application-preprod.properties
- Password should be: Dechub#2025
- Rebuild and redeploy if changed

### 16.6 Application Logs Show Errors

**Check specific errors:**

```bash
# View latest errors
tail -100 /opt/tanishq/applications_preprod/app.log

# Search for specific errors
grep -i "error" /opt/tanishq/applications_preprod/app.log
grep -i "exception" /opt/tanishq/applications_preprod/app.log

# View full startup log
less /opt/tanishq/applications_preprod/app.log
# Press 'q' to exit
```

**Common errors and solutions in the logs:**

**"Could not load Excel file":**
- This is OK! Not critical
- Application continues without Excel data

**"Could not resolve placeholder":**
- Missing property in application-preprod.properties
- Add the missing property and rebuild

**"Connection refused to database":**
- MySQL not running (see 16.5)
- Wrong database credentials

**"FileNotFoundException for .p12":**
- .p12 file missing or wrong path
- Upload file (see Step 6.2)
- Check path in application-preprod.properties

---

## 17. MAINTENANCE

### 17.1 View Application Logs

**Real-time log monitoring:**

```bash
tail -f /opt/tanishq/applications_preprod/app.log
```

**View last 100 lines:**

```bash
tail -100 /opt/tanishq/applications_preprod/app.log
```

**View Nginx access logs:**

```bash
tail -100 /var/log/nginx/celebrations-preprod-access.log
```

**View Nginx error logs:**

```bash
tail -100 /var/log/nginx/celebrations-preprod-error.log
```

### 17.2 Stop Application

**Find process ID:**

```bash
ps -ef | grep tanishq
```

**Kill by process ID:**

```bash
kill <process_id>
```

**Kill all tanishq processes:**

```bash
pkill -f tanishq
```

**Verify stopped:**

```bash
ps -ef | grep tanishq
# Should only show your grep command
```

### 17.3 Restart Application

**After stopping:**

```bash
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war --spring.profiles.active=preprod > app.log 2>&1 &
```

**Monitor startup:**

```bash
tail -f app.log
# Wait for "Started TanishqSelfieApplication"
# Press Ctrl+C
```

### 17.4 Deploy New Version

**Stop current version:**

```bash
pkill -f tanishq
```

**Upload new WAR file via WinSCP to:**
```
/opt/tanishq/applications_preprod/
```

**Start new version:**

```bash
cd /opt/tanishq/applications_preprod

# Remove old log (optional)
rm app.log

# Start new version
nohup java -jar tanishq-preprod-<new-date>-*.war --spring.profiles.active=preprod > app.log 2>&1 &

# Monitor
tail -f app.log
```

### 17.5 Nginx Management

**Restart Nginx:**

```bash
systemctl restart nginx
```

**Stop Nginx:**

```bash
systemctl stop nginx
```

**Start Nginx:**

```bash
systemctl start nginx
```

**Reload configuration (without restart):**

```bash
nginx -t        # Test config first
systemctl reload nginx
```

**View Nginx status:**

```bash
systemctl status nginx
```

### 17.6 Database Backup

**Backup database:**

```bash
# Create backup directory
mkdir -p /opt/tanishq/backups

# Backup database
mysqldump -u root -p selfie_preprod > /opt/tanishq/backups/selfie_preprod_$(date +%Y%m%d_%H%M%S).sql

# Verify backup created
ls -lh /opt/tanishq/backups/
```

**Restore database (if needed):**

```bash
mysql -u root -p selfie_preprod < /opt/tanishq/backups/selfie_preprod_YYYYMMDD_HHMMSS.sql
```

### 17.7 Disk Space Monitoring

**Check disk space:**

```bash
df -h
```

**Check directory sizes:**

```bash
du -sh /opt/tanishq/*
du -sh /var/log/nginx/*
```

**Clean old logs if needed:**

```bash
# Archive old logs
gzip /opt/tanishq/applications_preprod/app.log.old

# Remove old Nginx logs (be careful!)
find /var/log/nginx/ -name "*.log.*" -mtime +30 -delete
```

### 17.8 Security Updates

**Keep system updated:**

```bash
# Check for updates
yum check-update

# Update specific package
yum update nginx -y

# Update all (use with caution)
yum update -y
```

---

## 18. QUICK REFERENCE

### 18.1 Important Paths

```
Application Directory:
/opt/tanishq/applications_preprod/

WAR File:
/opt/tanishq/applications_preprod/tanishq-preprod-03-12-2025-1-0.0.1-SNAPSHOT.war

Application Log:
/opt/tanishq/applications_preprod/app.log

Storage Directories:
/opt/tanishq/storage/selfie_images/
/opt/tanishq/storage/bride_uploads/

Google Keys:
/opt/tanishq/tanishqgmb-5437243a8085.p12
/opt/tanishq/event-images-469618-32e65f6d62b3.p12

Nginx Configuration:
/etc/nginx/conf.d/celebrations-preprod.conf

Nginx Logs:
/var/log/nginx/celebrations-preprod-access.log
/var/log/nginx/celebrations-preprod-error.log
```

### 18.2 Important Commands

```bash
# Application Management
ps -ef | grep tanishq                    # Check if running
pkill -f tanishq                         # Stop application
tail -f app.log                          # View logs

# Nginx Management
systemctl status nginx                   # Check status
systemctl restart nginx                  # Restart
nginx -t                                 # Test config

# Database
mysql -u root -p selfie_preprod         # Connect to database
mysql -u root -p selfie_preprod -e "SHOW TABLES;"  # List tables

# Network
netstat -tuln | grep 3002               # Check app port
netstat -tuln | grep :80                # Check nginx port
curl http://localhost:3002              # Test app locally
curl http://localhost                   # Test nginx proxy
```

### 18.3 Important URLs

```
Application (Direct IP):
http://10.160.128.94

Application (Domain):
http://celebrations-preprod.tanishq.co.in

Features:
http://celebrations-preprod.tanishq.co.in/checklist
http://celebrations-preprod.tanishq.co.in/selfie
http://celebrations-preprod.tanishq.co.in/events
```

### 18.4 Important Credentials

```
Server SSH:
Host: 10.160.128.94
User: jewdev-test
Port: 22

MySQL Database:
Host: localhost
Port: 3306
Database: selfie_preprod
User: root
Password: Dechub#2025

Application:
Port: 3002 (internal)
Port: 80 (external via Nginx)
Profile: preprod
```

---

## 19. CHECKLIST

### 19.1 Pre-Deployment Checklist

- [ ] Java 11 installed on local machine
- [ ] Maven installed on local machine
- [ ] WinSCP installed
- [ ] PuTTY installed
- [ ] VPN access (if required)
- [ ] Server SSH credentials
- [ ] MySQL root password
- [ ] Project source code ready
- [ ] Frontend build files ready
- [ ] Google .p12 files ready

### 19.2 Deployment Checklist

- [ ] application-preprod.properties configured
- [ ] pom.xml version updated
- [ ] WAR file built successfully
- [ ] SSH connection to server working
- [ ] Root access obtained
- [ ] Database created
- [ ] Directory structure created
- [ ] .p12 files uploaded and moved
- [ ] WAR file uploaded
- [ ] Nginx installed and configured
- [ ] SELinux configured
- [ ] Application started successfully
- [ ] Application logs show "Started TanishqSelfieApplication"
- [ ] Nginx running
- [ ] Local access working (curl)
- [ ] AWS Security Group configured
- [ ] DNS configured
- [ ] External access working (browser)
- [ ] All features tested
- [ ] Database tables created (15 tables)

### 19.3 Post-Deployment Checklist

- [ ] Application accessible via domain
- [ ] All team members can access
- [ ] Homepage loads correctly
- [ ] Wedding Checklist feature works
- [ ] Selfie feature works
- [ ] Events feature works
- [ ] Login functionality works
- [ ] No errors in application logs
- [ ] No errors in Nginx logs
- [ ] Manager notified of successful deployment
- [ ] Testing team notified
- [ ] Documentation updated
- [ ] Credentials saved securely

---

## 20. CONTACT INFORMATION

### 20.1 Teams to Contact

**AWS/Cloud Team:**
- Purpose: Security Group configuration
- Request: Open port 80 for instance 10.160.128.94
- Timeline: 10-30 minutes

**Network/DNS Team:**
- Purpose: DNS configuration
- Request: A record for celebrations-preprod.tanishq.co.in → 10.160.128.94
- Timeline: 30-60 minutes

**Database Team (if separate):**
- Purpose: Database access/issues
- MySQL: localhost:3306

**Your Manager:**
- Purpose: Deployment status updates
- Notify: When deployment complete and accessible

### 20.2 Support Resources

**Server Access:**
- IT Helpdesk: [Your IT contact]
- VPN Issues: [VPN support contact]

**Application Issues:**
- Development Team: [Your team contact]
- Log Location: /opt/tanishq/applications_preprod/app.log

**Infrastructure:**
- AWS Support: [AWS team contact]
- Network Support: [Network team contact]

---

## APPENDIX

### A. File Structure Summary

```
Local Machine:
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\
├── src\main\
│   ├── java\...
│   └── resources\
│       ├── application-preprod.properties  ← Configure this
│       ├── static\...                      ← Frontend files
│       ├── tanishqgmb-5437243a8085.p12    ← Upload to server
│       └── event-images-469618-*.p12      ← Upload to server
├── target\
│   └── tanishq-preprod-03-12-2025-1-*.war ← Upload to server
└── pom.xml                                 ← Update version

Server (/opt/tanishq/):
├── applications_preprod/
│   ├── tanishq-preprod-03-12-2025-1-*.war ← Uploaded WAR
│   └── app.log                             ← Application logs
├── storage/
│   ├── selfie_images/                      ← User uploads
│   ├── bride_uploads/                      ← Form uploads
│   └── base.jpg                            ← Base image
├── tanishqgmb-5437243a8085.p12            ← Google key
└── event-images-469618-32e65f6d62b3.p12   ← Event images key
```

### B. Port Reference

```
Port 22   - SSH (for server access)
Port 80   - Nginx (HTTP, public facing)
Port 3002 - Application (internal, behind Nginx)
Port 3306 - MySQL (localhost only, not exposed)
```

### C. Environment Comparison

```
Development (Local):
- localhost:8130
- Windows paths
- Local MySQL
- Local files

Pre-Production (Server):
- 10.160.128.94:3002 (internal)
- celebrations-preprod.tanishq.co.in:80 (external)
- Linux paths
- Server MySQL
- Server storage

Production:
- 10.10.63.97
- celebrations.tanishq.co.in
- Similar setup to pre-prod
```

### D. Deployment Timeline

```
Estimated Total Time: 2-4 hours

Breakdown:
- Prerequisites check: 15 min
- Local configuration: 15 min
- Build WAR file: 5 min
- Server setup: 30 min
- File uploads: 15 min
- Application deployment: 20 min
- Nginx configuration: 15 min
- Security configuration: 15 min
- AWS Security Group: 30-60 min (waiting for team)
- DNS configuration: 30-90 min (waiting for team)
- Testing: 20 min
- Documentation: 10 min
```

---

## CONCLUSION

You now have a complete, step-by-step guide to deploy the Tanishq Celebrations application from scratch to pre-production server.

**Key Success Points:**
✅ Application deployed and running  
✅ Accessible via domain: http://celebrations-preprod.tanishq.co.in  
✅ Database connected and tables created  
✅ All features functional  
✅ Team can access for testing  

**For Support:**
- Refer to troubleshooting section for common issues
- Check logs for detailed error messages
- Contact respective teams for infrastructure access

**Good luck with your deployment!** 🚀

---

**Document Version:** 1.0  
**Last Updated:** December 3, 2025  
**Maintained By:** [Your Name]

---

