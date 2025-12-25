# Pre-Production Environment Setup - Summary

## Overview
This document provides a complete summary of the pre-production environment setup for the Tanishq Celebrations application.

---

## Environment Details

### Production Environment
- **Server IP:** 10.10.63.97
- **Domain:** http://celebrations.tanishq.co.in/
- **Port:** 3001
- **Deploy Directory:** /opt/tanishq/applications_one (assumed)
- **Profile:** prod
- **Database:** tanishq

### Pre-Production Environment
- **Server IP:** 10.160.128.94
- **Domain:** http://celebrations-preprod.tanishq.co.in/
- **Port:** 3002
- **Deploy Directory:** /opt/tanishq/applications_preprod
- **Profile:** preprod
- **Database:** tanishq_preprod

---

## Files Created/Modified

### Configuration Files Created:
1. **application-preprod.properties** - Pre-production environment configuration
   - Location: `src/main/resources/application-preprod.properties`
   - Contains database, port, file paths, and email settings for pre-prod

### Documentation Files Created:
1. **PREPROD_DEPLOYMENT_GUIDE.md** - Comprehensive deployment guide
   - Complete step-by-step instructions
   - One-time server setup procedures
   - Regular deployment procedures
   - Troubleshooting guide

2. **PREPROD_DEPLOYMENT_CHECKLIST.md** - Quick reference checklist
   - Pre-deployment checklist
   - Deployment checklist
   - Verification checklist
   - Quick commands reference
   - Troubleshooting quick fixes

### Scripts Created:
1. **build-preprod.bat** - Windows build script
   - Automated build process for Windows
   - Validation checks
   - Build verification

2. **deploy-preprod.sh** - Linux deployment script
   - Server-side deployment helper
   - Process management
   - Log monitoring

### Files Modified:
1. **application.properties** - Set preprod as active profile
   - Changed from: `spring.profiles.active=test`
   - Changed to: `spring.profiles.active=preprod`

2. **pom.xml** - Updated artifactId for preprod
   - Changed from: `selfie-29-10-2025-1`
   - Changed to: `tanishq-preprod-02-12-2025-1`

---

## Key Configuration Differences

### Database Configuration
**Production:**
```properties
# Not visible in provided files
```

**Pre-Production:**
```properties
spring.datasource.url=jdbc:mysql://10.160.128.94:3306/tanishq_preprod
spring.datasource.username=tanishq_preprod
spring.datasource.password=CHANGE_THIS_PASSWORD
```

### File Paths
**Production (Windows-based):**
```properties
selfie.upload.dir=C:\\Users\\91635\\Desktop\\Projects\\dechub tanishq\\storage\\selfie_images
system.isWindows=Y
```

**Pre-Production (Linux-based):**
```properties
selfie.upload.dir=/opt/tanishq/storage/selfie_images
system.isWindows=N
```

### Server Ports
- Production: 3001
- Pre-Production: 3002

---

## Deployment Workflow Comparison

### Production Deployment (Current)
1. Copy frontend static files to backend
2. Update index.html
3. Change artifactId to: `tanishq-DD-MM-YYYY-N`
4. Use Java 11
5. Run `mvn clean install`
6. Copy WAR to target folder
7. Connect FortiClient VPN
8. WinSCP to 10.10.63.97 (username: nishal)
9. Paste to applications_one folder
10. PuTTY to server
11. `sudo su root`
12. `cd applications_one`
13. `ps -ef | grep tanishq` or `ps -ef | grep selfie`
14. `kill [process_id]`
15. Start: `nohup java -jar tanishq-DD-MM-YYYY-N-0.0.1-SNAPSHOT.war > tanishq-DD-MM-YYYY-N-0.0.1-SNAPSHOT.log 2>&1 &`
16. Monitor: `tail -f tanishq-DD-MM-YYYY-N-0.0.1-SNAPSHOT.log`

### Pre-Production Deployment (New)
1. Copy frontend static files to backend
2. Update index.html
3. Change artifactId to: `tanishq-preprod-DD-MM-YYYY-N`
4. Use Java 11
5. Run `mvn clean install`
6. Copy WAR to target folder
7. Connect FortiClient VPN
8. WinSCP to 10.160.128.94 (username: nishal)
9. Paste to /opt/tanishq/applications_preprod folder
10. PuTTY to server
11. `sudo su root`
12. `cd /opt/tanishq/applications_preprod`
13. `ps -ef | grep tanishq-preprod`
14. `kill [process_id]`
15. Start: `nohup java -jar tanishq-preprod-DD-MM-YYYY-N-0.0.1-SNAPSHOT.war > tanishq-preprod-DD-MM-YYYY-N-0.0.1-SNAPSHOT.log 2>&1 &`
16. Monitor: `tail -f tanishq-preprod-DD-MM-YYYY-N-0.0.1-SNAPSHOT.log`

---

## Pre-Production Server Setup Requirements

### One-Time Server Configuration

#### 1. Directory Structure
```bash
sudo su root
mkdir -p /opt/tanishq/applications_preprod
mkdir -p /opt/tanishq/storage/selfie_images
chmod -R 755 /opt/tanishq/storage
chown -R nishal:nishal /opt/tanishq
```

#### 2. Java Installation
```bash
java -version  # Should be Java 11
# If not installed:
# CentOS/RHEL: yum install java-11-openjdk
# Ubuntu/Debian: apt-get install openjdk-11-jdk
```

#### 3. Database Setup
```sql
CREATE DATABASE IF NOT EXISTS tanishq_preprod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'tanishq_preprod'@'localhost' IDENTIFIED BY 'YOUR_SECURE_PASSWORD';
CREATE USER 'tanishq_preprod'@'%' IDENTIFIED BY 'YOUR_SECURE_PASSWORD';
GRANT ALL PRIVILEGES ON tanishq_preprod.* TO 'tanishq_preprod'@'localhost';
GRANT ALL PRIVILEGES ON tanishq_preprod.* TO 'tanishq_preprod'@'%';
FLUSH PRIVILEGES;
```

#### 4. Required Files Upload (via WinSCP)
Upload to `/opt/tanishq/`:
- tanishqgmb-5437243a8085.p12
- tanishq_selfie_app_store_data.xlsx

#### 5. Nginx/Apache Configuration
Configure reverse proxy for http://celebrations-preprod.tanishq.co.in/ to localhost:3002

**Nginx Example:**
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

## Important Configuration Notes

### application-preprod.properties Key Settings

1. **Database Password:** Must be updated before first deployment
   ```properties
   spring.datasource.password=CHANGE_THIS_PASSWORD
   ```

2. **File Paths:** All paths use Linux format (/)
   ```properties
   dechub.tanishq.key.filepath=/opt/tanishq/tanishqgmb-5437243a8085.p12
   selfie.upload.dir=/opt/tanishq/storage/selfie_images
   store.details.excel.sheet=/opt/tanishq/tanishq_selfie_app_store_data.xlsx
   ```

3. **System Flag:** Set to Linux
   ```properties
   system.isWindows=N
   ```

4. **Port:** Different from production
   ```properties
   server.port=3002
   ```

---

## Access Credentials

### VPN
- FortiClient VPN (use provided credentials)

### Server Access (Pre-Prod)
- **IP:** 10.160.128.94
- **Username:** nishal
- **Authentication:** Private key from downloads folder
- **Root Access:** `sudo su root`

### Database
- **Host:** 10.160.128.94
- **Port:** 3306
- **Database:** tanishq_preprod
- **Username:** tanishq_preprod
- **Password:** [To be set during setup]

---

## Next Steps

### Before First Deployment:

1. **Update Database Password**
   - Edit `src/main/resources/application-preprod.properties`
   - Replace `CHANGE_THIS_PASSWORD` with actual secure password
   - This password must match the one set during database creation

2. **Verify Server Setup**
   - Confirm server 10.160.128.94 has been configured
   - Verify directories exist
   - Verify Java 11 is installed
   - Verify database is created
   - Verify required files are uploaded
   - Verify nginx/apache is configured

3. **Build and Test Locally**
   - Run `build-preprod.bat` on Windows
   - Verify build succeeds
   - Check WAR file is created

4. **First Deployment**
   - Follow `PREPROD_DEPLOYMENT_GUIDE.md`
   - Use `PREPROD_DEPLOYMENT_CHECKLIST.md` for verification
   - Test all functionality after deployment

### For Regular Deployments:

1. **Quick Reference:** Use `PREPROD_DEPLOYMENT_CHECKLIST.md`
2. **Detailed Steps:** Refer to `PREPROD_DEPLOYMENT_GUIDE.md`
3. **Build Helper:** Run `build-preprod.bat` on Windows
4. **Deploy Helper:** Use `deploy-preprod.sh` on server

---

## Testing Strategy

### Pre-Production Testing Checklist:
1. **Functional Testing**
   - Store manager login
   - Event creation and management
   - Attendee registration
   - QR code generation and scanning
   - Selfie upload and processing
   - Reports generation
   - Email notifications

2. **Integration Testing**
   - Database connectivity
   - Google Sheets integration
   - Email service integration
   - File upload/download

3. **Performance Testing**
   - Page load times
   - Image processing
   - Report generation
   - Concurrent users

4. **Security Testing**
   - Authentication/Authorization
   - SQL injection prevention
   - XSS prevention
   - File upload security

---

## Troubleshooting Resources

### Quick Fixes Document
See `PREPROD_DEPLOYMENT_CHECKLIST.md` - Section: "Troubleshooting Quick Fix"

### Detailed Troubleshooting
See `PREPROD_DEPLOYMENT_GUIDE.md` - Section: "Troubleshooting"

### Common Issues:

1. **Application Won't Start**
   - Check Java version: `java -version`
   - Check logs for errors
   - Verify port availability: `netstat -tlnp | grep 3002`

2. **Database Connection Error**
   - Verify password in application-preprod.properties
   - Test MySQL connection manually
   - Check database exists and user has grants

3. **Website Not Accessible**
   - Verify application is running: `ps -ef | grep tanishq-preprod`
   - Check nginx/apache status
   - Verify domain DNS resolution

---

## Deployment Naming Convention

### WAR File Naming:
- **Format:** `tanishq-preprod-DD-MM-YYYY-N-0.0.1-SNAPSHOT.war`
- **Example:** `tanishq-preprod-02-12-2025-1-0.0.1-SNAPSHOT.war`
- **N:** Deployment number for the day (increment for multiple deployments)

### Log File Naming:
- **Format:** `tanishq-preprod-DD-MM-YYYY-N-0.0.1-SNAPSHOT.log`
- **Example:** `tanishq-preprod-02-12-2025-1-0.0.1-SNAPSHOT.log`

---

## Documentation References

| Document | Purpose | Use When |
|----------|---------|----------|
| PREPROD_DEPLOYMENT_GUIDE.md | Comprehensive deployment instructions | First time deployment or detailed reference |
| PREPROD_DEPLOYMENT_CHECKLIST.md | Quick reference and checklist | Regular deployments |
| build-preprod.bat | Automated build script | Building on Windows |
| deploy-preprod.sh | Server deployment helper | Deploying on Linux server |
| application-preprod.properties | Environment configuration | Configuration changes needed |

---

## Success Criteria

### Deployment is successful when:
- ✅ Build completes without errors
- ✅ WAR file is generated
- ✅ File is uploaded to server
- ✅ Application process starts
- ✅ No errors in startup logs
- ✅ "Started TanishqSelfieApplication" appears in logs
- ✅ Process is running: `ps -ef | grep tanishq-preprod`
- ✅ Port is listening: `netstat -tlnp | grep 3002`
- ✅ Website is accessible at http://celebrations-preprod.tanishq.co.in/
- ✅ Homepage loads correctly
- ✅ Login works
- ✅ Key features are functional

---

## Maintenance Notes

### Regular Tasks:
1. **Log Cleanup:** Periodically delete old log files
2. **WAR Cleanup:** Remove old WAR files after successful deployment
3. **Database Backup:** Regular backups of tanishq_preprod database
4. **Monitoring:** Check application health and logs regularly

### Log Management:
```bash
# Find old log files (older than 30 days)
find /opt/tanishq/applications_preprod -name "*.log" -mtime +30

# Delete old log files (older than 30 days)
find /opt/tanishq/applications_preprod -name "*.log" -mtime +30 -delete

# Find old WAR files (older than 30 days)
find /opt/tanishq/applications_preprod -name "*.war" -mtime +30

# Delete old WAR files (keeping current one)
find /opt/tanishq/applications_preprod -name "*.war" -mtime +30 -delete
```

---

## Support & Contacts

### For Deployment Issues:
- Review troubleshooting sections in documentation
- Check logs for specific error messages
- Verify all checklist items are completed

### For Server Access Issues:
- Contact server administrator
- Verify VPN connection
- Check credentials

### For Database Issues:
- Contact database administrator
- Verify credentials and grants
- Check database server status

---

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0 | Dec 2, 2025 | Initial pre-prod setup | System |

---

## Appendix

### A. File Locations

**Local (Windows):**
- Project: `C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app`
- Configuration: `src\main\resources\application-preprod.properties`
- Build Output: `target\`

**Server (Linux):**
- Application: `/opt/tanishq/applications_preprod/`
- Storage: `/opt/tanishq/storage/selfie_images/`
- Config Files: `/opt/tanishq/`

### B. Port Assignments
- Production: 3001
- Pre-Production: 3002
- Test/Local: 8130

### C. Profile Names
- Production: `prod`
- Pre-Production: `preprod`
- UAT: `uat`
- Test/Local: `test`

---

**Document Version:** 1.0  
**Created:** December 2, 2025  
**Last Updated:** December 2, 2025  
**Status:** Active

