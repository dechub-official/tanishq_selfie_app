# MULTI-ENVIRONMENT SETUP GUIDE
## Tanishq Celebration Application - Production & Pre-Production

---

## 🎯 OVERVIEW

This application now supports **multiple environments** with easy switching between:
- **Pre-Production** (Testing Environment)
- **Production** (Live Environment)

Each environment has:
- ✅ Separate database
- ✅ Separate configuration
- ✅ Separate URLs
- ✅ Separate build scripts

---

## 📋 ENVIRONMENT COMPARISON

| Feature | Pre-Production | Production |
|---------|---------------|------------|
| **Database** | `selfie_preprod` | `selfie_prod` |
| **Port** | 3000 | 3001 |
| **URL** | celebrationsite-preprod.tanishq.co.in | celebrationsite.tanishq.co.in |
| **Profile** | `preprod` | `prod` |
| **Logging** | DEBUG (verbose) | INFO (minimal) |
| **SQL Logging** | ON | OFF |
| **S3 Bucket** | celebrations-tanishq-preprod | celebrations-tanishq-prod |

---

## 🚀 QUICK START

### Option 1: Use Environment Switcher (RECOMMENDED)
```batch
BUILD_ENVIRONMENT_SWITCHER.bat
```
This interactive menu will guide you through:
1. Building for Pre-Prod
2. Building for Production
3. Viewing settings
4. Testing database connections

### Option 2: Direct Build Commands

**For Pre-Production:**
```batch
BUILD_PREPROD.bat
```

**For Production:**
```batch
BUILD_PROD.bat
```

---

## 📦 WHAT EACH BUILD DOES

### Pre-Production Build (`BUILD_PREPROD.bat`)
1. ✅ Checks Maven and Java installation
2. ✅ Cleans previous builds
3. ✅ Builds with `preprod` profile
4. ✅ Creates WAR file configured for:
   - Database: `selfie_preprod`
   - URL: `https://celebrationsite-preprod.tanishq.co.in`
   - Port: 3000

### Production Build (`BUILD_PROD.bat`)
1. ✅ Checks Maven and Java installation
2. ✅ Cleans previous builds
3. ✅ Builds with `prod` profile using: `mvn clean package -Pprod -DskipTests`
4. ✅ Creates WAR file configured for:
   - Database: `selfie_prod`
   - URL: `https://celebrationsite.tanishq.co.in`
   - Port: 3001

---

## 🗄️ DATABASE SETUP

### Before Building: Set Up MySQL Databases

#### 1. For Pre-Production (Already Done)
Database: `selfie_preprod` is already set up.

#### 2. For Production (NEW - To Do Now)

**On Production Server, run:**
```bash
# Login to MySQL
mysql -u root -p

# Run the setup script
source /path/to/setup_production_database.sql
# OR
mysql -u root -p < setup_production_database.sql
```

**Or manually:**
```sql
CREATE DATABASE IF NOT EXISTS selfie_prod 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;
```

---

## 🔧 PRODUCTION SERVER SETUP STEPS

### Step 1: Install MySQL on Production Server
```bash
# Update system
sudo apt update

# Install MySQL Server 8.0
sudo apt install mysql-server -y

# Secure MySQL installation
sudo mysql_secure_installation

# Set root password to: Dechub#2025
```

### Step 2: Create Production Database
```bash
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE selfie_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Verify
SHOW DATABASES;
```

### Step 3: Create Required Directories
```bash
sudo mkdir -p /opt/tanishq/storage/selfie_images
sudo mkdir -p /opt/tanishq/storage/bride_uploads
sudo chmod -R 755 /opt/tanishq
```

### Step 4: Upload Required Files

Using WinSCP/FileZilla, upload:
1. **WAR file** → `/opt/tanishq/`
2. **Key files:**
   - `tanishqgmb-5437243a8085.p12` → `/opt/tanishq/`
   - `event-images-469618-32e65f6d62b3.p12` → `/opt/tanishq/`

### Step 5: Deploy to Tomcat
```bash
# Stop Tomcat
sudo systemctl stop tomcat

# Backup old deployment (if exists)
sudo mv /opt/tomcat/webapps/ROOT.war /opt/tomcat/webapps/ROOT.war.backup

# Deploy new WAR
sudo cp /opt/tanishq/[your-war-file].war /opt/tomcat/webapps/ROOT.war

# Start Tomcat
sudo systemctl start tomcat

# Monitor logs
tail -f /opt/tomcat/logs/catalina.out
```

---

## 🔄 WORKFLOW: How to Use Both Environments

### Typical Development & Deployment Flow:

#### 1️⃣ **Develop & Test in Pre-Prod**
```batch
# Build for pre-prod
BUILD_PREPROD.bat

# Deploy to pre-prod server
# Test thoroughly at: https://celebrationsite-preprod.tanishq.co.in
```

#### 2️⃣ **After Testing: Deploy to Production**
```batch
# Build for production
BUILD_PROD.bat

# Deploy to production server
# Go live at: https://celebrationsite.tanishq.co.in
```

#### 3️⃣ **Both Can Run Simultaneously!**
- Pre-prod on port 3000 → `selfie_preprod` database
- Production on port 3001 → `selfie_prod` database
- No conflicts! Completely isolated!

---

## 📝 CONFIGURATION FILES

### Main Configuration Files:

1. **`application.properties`** (Main selector)
   ```properties
   spring.profiles.active=preprod  # Change to 'prod' for production
   ```

2. **`application-preprod.properties`** (Pre-Prod settings)
   - Database: `selfie_preprod`
   - Port: 3000
   - URL: celebrationsite-preprod.tanishq.co.in

3. **`application-prod.properties`** (Production settings)
   - Database: `selfie_prod`
   - Port: 3001
   - URL: celebrationsite.tanishq.co.in

### ⚠️ IMPORTANT: The Build Scripts Handle Profile Selection!
You **DON'T** need to manually change `application.properties`!
- `BUILD_PREPROD.bat` automatically uses `-Ppreprod` profile
- `BUILD_PROD.bat` automatically uses `-Pprod` profile

---

## 🎯 MAVEN PROFILES IN POM.XML

The `pom.xml` already has profiles configured:

```xml
<profiles>
    <!-- PRE-PRODUCTION PROFILE -->
    <profile>
        <id>preprod</id>
        <properties>
            <spring.profiles.active>preprod</spring.profiles.active>
        </properties>
    </profile>

    <!-- PRODUCTION PROFILE -->
    <profile>
        <id>prod</id>
        <properties>
            <spring.profiles.active>prod</spring.profiles.active>
        </properties>
    </profile>
</profiles>
```

---

## ✅ VERIFICATION CHECKLIST

### Before Deploying to Production:

- [ ] MySQL 8.0 installed on production server
- [ ] Database `selfie_prod` created
- [ ] Root password is `Dechub#2025`
- [ ] Directories created: `/opt/tanishq/storage/`
- [ ] Key files uploaded to `/opt/tanishq/`
- [ ] Tomcat is installed and running
- [ ] Port 3001 is open in firewall
- [ ] DNS points to production server
- [ ] SSL certificate is installed (for HTTPS)

### After Deployment:

- [ ] Application starts without errors
- [ ] Database tables are created automatically
- [ ] Can access: https://celebrationsite.tanishq.co.in
- [ ] Can login to admin panel
- [ ] Events can be created
- [ ] QR codes are generated with correct URL
- [ ] File uploads work
- [ ] Google Sheets sync works
- [ ] Email notifications work

---

## 🆘 TROUBLESHOOTING

### Issue: "Database not found"
**Solution:**
```sql
CREATE DATABASE selfie_prod;
```

### Issue: "Access denied for user"
**Solution:**
```sql
GRANT ALL PRIVILEGES ON selfie_prod.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

### Issue: "Port already in use"
**Solution:**
- Pre-prod uses port 3000
- Production uses port 3001
- They don't conflict!

### Issue: "File upload fails"
**Solution:**
```bash
sudo mkdir -p /opt/tanishq/storage/selfie_images
sudo chmod -R 755 /opt/tanishq
```

---

## 🎉 SUCCESS INDICATORS

### Pre-Production Working:
✅ `https://celebrationsite-preprod.tanishq.co.in` is accessible
✅ Database `selfie_preprod` has tables
✅ QR codes point to preprod URL

### Production Working:
✅ `https://celebrationsite.tanishq.co.in` is accessible
✅ Database `selfie_prod` has tables
✅ QR codes point to production URL

---

## 📞 SUPPORT

If you encounter issues:
1. Check Tomcat logs: `tail -f /opt/tomcat/logs/catalina.out`
2. Check MySQL connection: `mysql -u root -p`
3. Verify WAR deployment: `ls -lh /opt/tomcat/webapps/`
4. Check ports: `netstat -tulpn | grep java`

---

## 🎯 SUMMARY

You now have:
✅ **Two completely separate environments**
✅ **Easy build scripts for each**
✅ **Interactive environment switcher**
✅ **Database setup scripts**
✅ **Complete isolation** - no conflicts!

**To build for Pre-Prod:** Run `BUILD_PREPROD.bat`
**To build for Production:** Run `BUILD_PROD.bat`
**To switch easily:** Run `BUILD_ENVIRONMENT_SWITCHER.bat`

That's it! You're ready to deploy to both environments! 🚀

