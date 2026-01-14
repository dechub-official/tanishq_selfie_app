# PRODUCTION DEPLOYMENT - YOUR ACTUAL PROCESS
## Step-by-Step Guide Based on Your Current Setup

**Date**: January 12, 2026
**Current artifactId**: `tanishq-preprod-07-01-2026-2`
**Next Production Build**: `tanishq-prod-12-01-2026-1` (or whatever number deployment today)

---

## ⚠️ CRITICAL CLARIFICATION NEEDED FIRST

Before proceeding, we need to understand:

### Questions to Answer:
1. **Is your production server the SAME as where pre-prod runs?**
   - Pre-prod: 10.10.63.97 (via direct SSH)
   - Production: ??? IP (via FortiClient VPN + WinSCP as 'nishal')

2. **What is the production server IP address?**

3. **Current production setup:**
   - Directory: `/applications_one/`
   - Port: ??? (need to check)
   - Database: ??? (need to check)

4. **The setup we did (Steps 1-3) - which server was that for?**
   - If same server: we need to reconcile `/opt/tanishq/` vs `/applications_one/`
   - If different server: we need to complete setup on correct server

---

## 🔍 DIAGNOSTIC STEPS - DO THIS FIRST

### Step 1: Check Your Current Production

Connect to production server via PuTTY:

```bash
# Switch to root
sudo su root

# Check what's in applications_one
cd /applications_one
ls -la

# Check running Java processes
ps -ef | grep java

# Check ports in use
netstat -tulpn | grep java

# Check if /opt/tanishq exists
ls -la /opt/tanishq 2>/dev/null || echo "Directory does not exist"

# Check databases
mysql -u root -p -e "SHOW DATABASES;" | grep selfie
```

**Copy and share the output**, then I'll provide exact next steps.

---

## 📋 YOUR STANDARD DEPLOYMENT PROCESS (Updated for Safety)

### PART A: PREPARE BUILD (On Your Local Machine)

#### Step A1: Update pom.xml for Production Build

```powershell
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
```

Open `pom.xml` and change artifactId:
```xml
<artifactId>tanishq-prod-12-01-2026-1</artifactId>
```

#### Step A2: Frontend Build (If needed)
If you have frontend changes:
1. Build frontend
2. Copy static files to `src/main/resources/static`
3. Update `index.html` with HTML tags from txt file

#### Step A3: Build WAR File
```powershell
# Make sure you're using Java 11
java -version

# Clean and build
mvn clean install

# Your WAR will be at:
# target\tanishq-prod-12-01-2026-1-0.0.1-SNAPSHOT.war
```

---

### PART B: CONNECT TO PRODUCTION

#### Step B1: Open FortiClient VPN
- Launch FortiClient VPN
- Connect using your credentials
- Wait for connection to establish

#### Step B2: Open WinSCP
- **Hostname**: [Your Production IP]
- **Username**: nishal
- **Port**: 22
- **Authentication**: Private key from Downloads folder
- Click "Login"

---

### PART C: UPLOAD WAR FILE

#### Step C1: Upload via WinSCP
1. Navigate to `/home/nishal/` in WinSCP (right panel)
2. Drag and drop your WAR file:
   - `tanishq-prod-12-01-2026-1-0.0.1-SNAPSHOT.war`
3. Wait for upload to complete

---

### PART D: DEPLOY ON SERVER

#### Step D1: Connect via PuTTY
- Open PuTTY
- Connect to your production server
- Login as: nishal
- Enter password/key

#### Step D2: Switch to Root
```bash
sudo su root
# Enter nishal's password if prompted
```

#### Step D3: Move WAR File
```bash
# Move to applications directory
cd /applications_one

# Move the uploaded WAR file
mv /home/nishal/tanishq-prod-12-01-2026-1-0.0.1-SNAPSHOT.war .

# Verify it's there
ls -la *.war
```

#### Step D4: Check Current Processes
```bash
# Check tanishq processes
ps -ef | grep tanishq

# Check selfie processes
ps -ef | grep selfie

# Note the process ID (second column)
```

#### Step D5: Kill Old Process
```bash
# Replace XXXX with actual process ID from step D4
kill XXXX

# Verify it's stopped
ps -ef | grep tanishq
```

#### Step D6: Start New Process

**CRITICAL**: Add Spring profile specification!

```bash
# Start with explicit production profile
nohup java -jar -Dspring.profiles.active=prod tanishq-prod-12-01-2026-1-0.0.1-SNAPSHOT.war > tanishq-prod-12-01-2026-1-0.0.1-SNAPSHOT.log 2>&1 &

# Check if it started
ps -ef | grep java

# Monitor the log
tail -f tanishq-prod-12-01-2026-1-0.0.1-SNAPSHOT.log
```

Press `Ctrl+C` to exit log monitoring.

#### Step D7: Verify Deployment
```bash
# Check if process is running
ps -ef | grep tanishq

# Check port is listening (verify which port it should be)
netstat -tulpn | grep java

# Check recent logs
tail -100 tanishq-prod-12-01-2026-1-0.0.1-SNAPSHOT.log

# Test the application (replace XXXX with actual port)
curl http://localhost:XXXX/
```

---

## ⚠️ CRITICAL ISSUES TO ADDRESS

### Issue 1: Spring Profile Not Specified
Your current command:
```bash
nohup java -jar tanishq-30-03-2025-1-0.0.1-SNAPSHOT.war > ...
```

**Problem**: No Spring profile specified, might use wrong config!

**Solution**: Always use:
```bash
nohup java -jar -Dspring.profiles.active=prod tanishq-30-03-2025-1-0.0.1-SNAPSHOT.war > ...
```

### Issue 2: Manual Process Management
**Current**: Using `nohup` and `kill`
**Better**: Use systemd service (more reliable, auto-restart)

Would you like to create a systemd service for production?

### Issue 3: Two Different Setups
- `/applications_one/` - Your current manual deployment
- `/opt/tanishq/` - The new setup from Steps 1-3

**Need to decide**: Which one to use going forward?

---

## 🔄 RECONCILING THE TWO APPROACHES

### Option 1: Keep Your Current Process (Safest for Now)
- Continue using `/applications_one/`
- Continue manual deployment with `nohup`
- **But add**: `-Dspring.profiles.active=prod` flag
- **But verify**: Using correct database and port

### Option 2: Migrate to New Setup
- Move to `/opt/tanishq/` directory
- Set up proper systemd service
- Better logging and management
- **Requires**: More setup and testing

### Option 3: Hybrid Approach
- Keep production in `/applications_one/` for now
- Use `/opt/tanishq/` for pre-prod/staging
- Migrate production later when tested

---

## 📊 WHAT WE NEED TO KNOW

Please run the diagnostic commands in **Step 1** above and provide:

1. **Output of `ls -la /applications_one/`**
2. **Output of `ps -ef | grep java`**
3. **Output of `netstat -tulpn | grep java`**
4. **Does `/opt/tanishq/` exist?**
5. **What is the production server IP?**
6. **Is it the same server as pre-prod (10.10.63.97)?**

---

## 🎯 RECOMMENDED IMMEDIATE ACTIONS

### For Today's Deployment:

1. **Use your familiar process** (PART A-D above)
2. **But ADD**: `-Dspring.profiles.active=prod` flag when starting
3. **Verify**: Check which database and port it connects to
4. **Document**: What you see in the logs

### After Deployment:

1. Share the diagnostic information
2. We'll create a better deployment strategy
3. Decide on `/applications_one/` vs `/opt/tanishq/`
4. Set up proper systemd service if needed

---

## 📝 QUICK CHECKLIST FOR YOUR DEPLOYMENT

- [ ] Frontend built and copied
- [ ] pom.xml artifactId updated to today's date
- [ ] Java 11 verified
- [ ] `mvn clean install` successful
- [ ] WAR file copied from target
- [ ] FortiClient VPN connected
- [ ] WinSCP opened and logged in
- [ ] WAR uploaded to server
- [ ] PuTTY connected
- [ ] Switched to root user
- [ ] Navigated to applications_one
- [ ] Checked current processes
- [ ] Killed old process
- [ ] Started new process **WITH** `-Dspring.profiles.active=prod`
- [ ] Verified process running
- [ ] Checked logs for errors
- [ ] Tested application URL

---

**Next Steps**: 
1. Run diagnostic commands
2. Share output
3. Then we'll provide precise guidance for your specific setup

**Status**: ⚠️ Awaiting server information before final deployment

