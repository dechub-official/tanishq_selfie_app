# Pre-Production Deployment Guide
## Tanishq Celebrations Application - Pre-Prod Environment

**Server Details:**
- **Server IP:** 10.160.128.94
- **Environment:** Pre-Production
- **Domain:** http://celebrations-preprod.tanishq.co.in/
- **Server Port:** 3002

---

## Prerequisites

### 1. Server Setup (One-time setup on 10.160.128.94)

Connect to the pre-prod server and perform these initial setup steps:

```bash
# Connect to server via SSH/Putty
# Hostname: 10.160.128.94
# Username: nishal (or your provided username)

# Switch to root user
sudo su root

# Create application directory
mkdir -p /opt/tanishq/applications_preprod
cd /opt/tanishq/applications_preprod

# Create storage directories
mkdir -p /opt/tanishq/storage/selfie_images
chmod -R 755 /opt/tanishq/storage

# Set ownership
chown -R nishal:nishal /opt/tanishq

# Install Java 11 (if not already installed)
java -version  # Check if Java 11 is installed
# If not, install Java 11:
# For CentOS/RHEL:
# yum install java-11-openjdk
# For Ubuntu/Debian:
# apt-get install openjdk-11-jdk
```

### 2. Upload Required Files to Server (One-time)

Using WinSCP:
- **Hostname:** 10.160.128.94
- **Username:** nishal
- **Password:** (use private key from downloads)

Upload these files to `/opt/tanishq/`:
1. `tanishqgmb-5437243a8085.p12` (Google service account key)
2. `tanishq_selfie_app_store_data.xlsx` (Store details Excel)

### 3. Database Setup (One-time)

```sql
-- Connect to MySQL on pre-prod server
mysql -u root -p

-- Create database
CREATE DATABASE IF NOT EXISTS tanishq_preprod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'tanishq_preprod'@'localhost' IDENTIFIED BY 'YOUR_SECURE_PASSWORD';
CREATE USER 'tanishq_preprod'@'%' IDENTIFIED BY 'YOUR_SECURE_PASSWORD';

-- Grant privileges
GRANT ALL PRIVILEGES ON tanishq_preprod.* TO 'tanishq_preprod'@'localhost';
GRANT ALL PRIVILEGES ON tanishq_preprod.* TO 'tanishq_preprod'@'%';

-- Flush privileges
FLUSH PRIVILEGES;

-- Exit MySQL
EXIT;
```

**Important:** Update the password in `application-preprod.properties` file before building.

### 4. Nginx/Apache Configuration (One-time)

Configure reverse proxy for domain http://celebrations-preprod.tanishq.co.in/

**For Nginx:**
```nginx
server {
    listen 80;
    server_name celebrations-preprod.tanishq.co.in;

    location / {
        proxy_pass http://localhost:3002;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 600;
        proxy_send_timeout 600;
        proxy_read_timeout 600;
        send_timeout 600;
    }
}
```

---

## Deployment Procedure (For Each Deployment)

### Step 1: Prepare Frontend Files

1. **Build Frontend** (if you have a separate frontend project)
   - Navigate to your frontend project directory
   - Run build command (e.g., `npm run build`)
   - Copy all files from the `dist` or `build` folder

2. **Copy Frontend Static Files to Backend**
   ```
   Source: [frontend_build_folder]/static/*
   Destination: C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\src\main\resources\static\
   ```
   - Copy all files from frontend build/static folder
   - Paste into backend `src/main/resources/static/` folder
   - Overwrite existing files

3. **Update index.html**
   - If you have HTML changes, copy the HTML content from your reference txt file
   - Paste into: `src/main/resources/static/index.html`

### Step 2: Update Application Configuration

1. **Set Active Profile to Pre-Prod**
   ```properties
   # File: src/main/resources/application.properties
   spring.profiles.active=preprod
   ```

2. **Verify Pre-Prod Configuration**
   - Open: `src/main/resources/application-preprod.properties`
   - Ensure database password is correct
   - Verify all paths and configurations

### Step 3: Update artifactId in pom.xml

1. Open `pom.xml`
2. Update the `<artifactId>` with today's date format:
   ```xml
   <artifactId>tanishq-preprod-02-12-2025-1</artifactId>
   ```
   Format: `tanishq-preprod-DD-MM-YYYY-N` (N = deployment number for the day)

### Step 4: Build the WAR File

1. **Ensure Java 11 is Active**
   ```cmd
   java -version
   ```
   Should show Java 11. If not, set JAVA_HOME to Java 11 path.

2. **Clean and Build**
   ```cmd
   cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
   mvn clean install
   ```

3. **Verify Build Success**
   - Check for "BUILD SUCCESS" message
   - Locate WAR file in `target/` folder
   - Example: `target/tanishq-preprod-02-12-2025-1-0.0.1-SNAPSHOT.war`

### Step 5: Connect to Pre-Prod Server

1. **Connect FortiClient VPN**
   - Open FortiClient VPN
   - Setup connection using your credentials
   - Connect to VPN

2. **Transfer WAR File via WinSCP**
   - Open WinSCP
   - Connection Details:
     - **Hostname:** 10.160.128.94
     - **Username:** nishal
     - **Password/Key:** Use private key from downloads
   - Navigate to: `/opt/tanishq/applications_preprod/`
   - Upload the WAR file from `target/` folder

### Step 6: Deploy on Server

1. **Connect via PuTTY**
   - Hostname: 10.160.128.94
   - Username: nishal

2. **Switch to Root User**
   ```bash
   sudo su root
   ```

3. **Navigate to Application Directory**
   ```bash
   cd /opt/tanishq/applications_preprod
   ```

4. **Check Current Running Process**
   ```bash
   ps -ef | grep tanishq-preprod
   # OR
   ps -ef | grep 3002
   ```
   Note the process ID from the output.

5. **Kill Existing Process**
   ```bash
   kill [process_id]
   ```
   Replace `[process_id]` with the actual process ID from step 4.

6. **Start New Process**
   ```bash
   nohup java -jar tanishq-preprod-02-12-2025-1-0.0.1-SNAPSHOT.war > tanishq-preprod-02-12-2025-1-0.0.1-SNAPSHOT.log 2>&1 &
   ```
   Replace the filename with your actual WAR filename.

7. **Monitor Logs**
   ```bash
   tail -f tanishq-preprod-02-12-2025-1-0.0.1-SNAPSHOT.log
   ```
   - Watch for successful startup messages
   - Look for "Started TanishqSelfieApplication" message
   - Press `Ctrl+C` to exit log view (process continues running)

### Step 7: Verify Deployment

1. **Check Process Status**
   ```bash
   ps -ef | grep tanishq-preprod
   ```
   Should show the running process.

2. **Check Application Logs**
   ```bash
   tail -100 tanishq-preprod-02-12-2025-1-0.0.1-SNAPSHOT.log
   ```

3. **Test Application Access**
   - Open browser
   - Navigate to: http://celebrations-preprod.tanishq.co.in/
   - Verify application loads correctly

---

## Quick Deployment Checklist

- [ ] Copy frontend static files to backend resources/static folder
- [ ] Update index.html with latest HTML content (if needed)
- [ ] Set `spring.profiles.active=preprod` in application.properties
- [ ] Update artifactId in pom.xml with today's date (format: tanishq-preprod-DD-MM-YYYY-N)
- [ ] Verify Java 11 is active (`java -version`)
- [ ] Run `mvn clean install`
- [ ] Verify BUILD SUCCESS
- [ ] Connect to FortiClient VPN
- [ ] Transfer WAR file via WinSCP to `/opt/tanishq/applications_preprod/`
- [ ] Connect via PuTTY to 10.160.128.94
- [ ] Switch to root: `sudo su root`
- [ ] Navigate: `cd /opt/tanishq/applications_preprod`
- [ ] Check process: `ps -ef | grep tanishq-preprod`
- [ ] Kill old process: `kill [process_id]`
- [ ] Start new process: `nohup java -jar [war-filename].war > [war-filename].log 2>&1 &`
- [ ] Monitor logs: `tail -f [war-filename].log`
- [ ] Verify application is running: http://celebrations-preprod.tanishq.co.in/

---

## Troubleshooting

### Application Won't Start

1. Check logs for errors:
   ```bash
   tail -200 tanishq-preprod-[DATE]-[N]-0.0.1-SNAPSHOT.log | grep -i error
   ```

2. Verify Java version:
   ```bash
   java -version
   ```

3. Check port availability:
   ```bash
   netstat -tlnp | grep 3002
   ```

4. Verify database connection:
   ```bash
   mysql -u tanishq_preprod -p tanishq_preprod
   ```

### Cannot Access Website

1. Check if application is running:
   ```bash
   ps -ef | grep tanishq-preprod
   ```

2. Check nginx/apache status:
   ```bash
   systemctl status nginx
   # OR
   systemctl status httpd
   ```

3. Verify port is listening:
   ```bash
   netstat -tlnp | grep 3002
   ```

### Database Errors

1. Verify database credentials in application-preprod.properties
2. Check database exists:
   ```sql
   SHOW DATABASES LIKE 'tanishq_preprod';
   ```
3. Check user permissions:
   ```sql
   SHOW GRANTS FOR 'tanishq_preprod'@'localhost';
   ```

---

## Differences from Production

| Aspect | Production (10.10.63.97) | Pre-Production (10.160.128.94) |
|--------|-------------------------|--------------------------------|
| Domain | celebrations.tanishq.co.in | celebrations-preprod.tanishq.co.in |
| Port | 3001 | 3002 |
| Profile | prod | preprod |
| Database | tanishq | tanishq_preprod |
| Deploy Directory | applications_one | applications_preprod |
| War Naming | tanishq-DD-MM-YYYY-N | tanishq-preprod-DD-MM-YYYY-N |

---

## Important Notes

1. **Always use Java 11** for building and running
2. **Update artifactId before each build** with current date and deployment number
3. **Keep track of deployment numbers** for the same day (increment N)
4. **Backup old WAR files** before deleting (optional)
5. **Test thoroughly** in pre-prod before deploying to production
6. **Database password** must be updated in application-preprod.properties
7. **File paths** in pre-prod use Linux format (/) not Windows (\)
8. **system.isWindows** is set to "N" for Linux server

---

## Server Access Credentials Summary

### FortiClient VPN
- Use your provided VPN credentials

### WinSCP
- **Hostname:** 10.160.128.94
- **Username:** nishal
- **Authentication:** Private key from downloads folder

### PuTTY/SSH
- **Hostname:** 10.160.128.94
- **Username:** nishal
- **Switch to root:** `sudo su root`

### Database
- **Host:** 10.160.128.94
- **Port:** 3306
- **Database:** tanishq_preprod
- **Username:** tanishq_preprod
- **Password:** [Set during database setup]

---

## Contact & Support

For deployment issues, contact:
- Server Admin: [Your server admin contact]
- Database Admin: [Your DB admin contact]
- Application Support: [Your support contact]

---

**Document Version:** 1.0  
**Created:** December 2, 2025  
**Last Updated:** December 2, 2025

