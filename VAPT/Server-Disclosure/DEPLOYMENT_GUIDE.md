# Server Information Disclosure Fix - Deployment Guide

**Date:** March 5, 2026  
**Status:** Ready for Deployment  
**Estimated Time:** 15-20 minutes

---

## 🎯 PRE-DEPLOYMENT CHECKLIST

- [x] Code implemented and tested locally
- [x] No compilation errors
- [x] Documentation complete
- [ ] Code reviewed
- [ ] Backup current WAR file
- [ ] Plan maintenance window (if needed)
- [ ] Notify stakeholders

---

## 📦 DEPLOYMENT STEPS

### Step 1: Build the Application (5 minutes)

```bash
# Navigate to project directory
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app_clean

# Clean and build
mvn clean package

# Verify WAR file created
ls target/*.war
```

**Expected Output:**
- `target/tanishq-preprod-03-03-2026-8-0.0.1-SNAPSHOT.war`

---

### Step 2: Backup Current Deployment (2 minutes)

**On Production Server:**
```bash
# Backup current WAR
cd /opt/tanishq
cp tanishq.war tanishq.war.backup-$(date +%Y%m%d-%H%M%S)

# Verify backup
ls -lh *.backup*
```

---

### Step 3: Deploy New WAR (3 minutes)

**Option A: Tomcat Deployment**
```bash
# Stop Tomcat
sudo systemctl stop tomcat

# Remove old deployment
rm -rf /opt/tomcat/webapps/tanishq*

# Copy new WAR
cp target/tanishq-preprod-03-03-2026-8-0.0.1-SNAPSHOT.war /opt/tomcat/webapps/tanishq.war

# Start Tomcat
sudo systemctl start tomcat
```

**Option B: Manual Deployment**
```bash
# Copy WAR to server
scp target/tanishq-preprod-03-03-2026-8-0.0.1-SNAPSHOT.war user@server:/opt/tanishq/

# SSH to server and deploy
ssh user@server
cd /opt/tanishq
# Follow your standard deployment procedure
```

---

### Step 4: Verify Deployment (5 minutes)

#### 4.1 Check Application Startup
```bash
# Check Tomcat logs
tail -f /opt/tomcat/logs/catalina.out

# Look for these lines:
# - "ServerHeaderFilter initialized"
# - "Server information disclosure protection enabled"
# - "Started TanishqSelfieApplication"
```

**Expected Log Output:**
```
INFO  ServerHeaderFilter - === ServerHeaderFilter initialized ===
INFO  ServerHeaderFilter - Server information disclosure protection enabled
INFO  RateLimitingFilter - === RateLimitingFilter initialized ===
INFO  TanishqSelfieApplication - Started TanishqSelfieApplication in X.XXX seconds
```

#### 4.2 Test Server Header Removal
```bash
# Test from command line
curl -I https://celebrations.tanishq.co.in/events/login

# Verify: NO Server header in output
# Should see other headers but NOT Server: ...
```

**Expected Output:**
```
HTTP/1.1 200 OK
Date: Wed, 05 Mar 2026 12:00:00 GMT
Content-Type: application/json
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
(No Server header)
```

#### 4.3 Test Existing Functionality
Test these critical features:
- [ ] Login (all user types)
- [ ] File upload
- [ ] Event creation
- [ ] QR code generation
- [ ] Greeting card features

---

### Step 5: Run Automated Tests (3 minutes)

**From your local machine:**
```powershell
cd VAPT\Server-Disclosure

# Edit script to use production URL
# Change: $baseUrl = "https://celebrations.tanishq.co.in"

.\test-server-disclosure.ps1
```

**Expected Result:**
```
✅ Server header: HIDDEN (PASS)
✅ X-Powered-By: HIDDEN (PASS)
✅ X-Amzn-Trace-Id: HIDDEN (PASS)
```

---

## 🔍 VERIFICATION CHECKLIST

After deployment, verify:

### Application Health
- [ ] Application started successfully
- [ ] No errors in logs
- [ ] All endpoints responding
- [ ] Database connection working

### Security Fix
- [ ] Server header is missing
- [ ] X-Powered-By header is missing
- [ ] Other security headers still present
- [ ] Test script shows all PASS

### Functionality
- [ ] User login works (all types)
- [ ] File upload works
- [ ] Events system works
- [ ] QR codes generate correctly
- [ ] Greetings system works

### Performance
- [ ] Response times normal
- [ ] No unusual CPU/memory usage
- [ ] Application responsive

---

## 🐛 ROLLBACK PROCEDURE

If something goes wrong:

### Quick Rollback
```bash
# Stop Tomcat
sudo systemctl stop tomcat

# Remove new deployment
rm -rf /opt/tomcat/webapps/tanishq*

# Restore backup
cp tanishq.war.backup-YYYYMMDD-HHMMSS /opt/tomcat/webapps/tanishq.war

# Start Tomcat
sudo systemctl start tomcat

# Verify application starts
tail -f /opt/tomcat/logs/catalina.out
```

---

## 📊 POST-DEPLOYMENT MONITORING

### First Hour
Monitor these for the first hour after deployment:
- Application logs for errors
- CPU and memory usage
- Response times
- Error rates
- User reports

### First 24 Hours
- Monitor error logs
- Check for any unusual patterns
- Verify VAPT scan results (if scheduled)
- Collect user feedback

---

## 📝 DEPLOYMENT LOG TEMPLATE

Fill this out during deployment:

```
Deployment Date: _______________
Deployed By: _______________
Environment: [ ] Local [ ] UAT [ ] Preprod [ ] Production

Pre-Deployment:
- Backup Created: [ ] Yes [ ] No - Location: _______________
- Build Successful: [ ] Yes [ ] No
- WAR File Size: _______________

Deployment:
- Start Time: _______________
- End Time: _______________
- Downtime: _______________
- Issues Encountered: _______________

Post-Deployment:
- Application Started: [ ] Yes [ ] No
- Filter Initialized: [ ] Yes [ ] No
- Server Header Removed: [ ] Yes [ ] No
- Functionality Tested: [ ] Yes [ ] No

Verification:
- Test Script Run: [ ] Yes [ ] No - Result: _______________
- Manual Testing: [ ] Pass [ ] Fail
- VAPT Scan: [ ] Pass [ ] Fail [ ] Scheduled

Sign-off:
- Deployed By: _______________
- Verified By: _______________
- Approved By: _______________
```

---

## 🎯 SUCCESS CRITERIA

Deployment is successful if:
- ✅ Application starts without errors
- ✅ "ServerHeaderFilter initialized" appears in logs
- ✅ Server header is missing in HTTP responses
- ✅ All existing functionality works
- ✅ No performance degradation
- ✅ Test script shows all PASS results

---

## 📞 SUPPORT CONTACTS

If you encounter issues:

**Technical Support:**
- Name: _______________
- Phone: _______________
- Email: _______________

**Security Team:**
- Name: _______________
- Phone: _______________
- Email: _______________

**Emergency Escalation:**
- Name: _______________
- Phone: _______________

---

## 📚 RELATED DOCUMENTS

- `README.md` - Overview and quick start
- `BACKEND_IMPLEMENTATION.md` - Technical details
- `FRONTEND_IMPACT.md` - Frontend team guide
- `test-server-disclosure.ps1` - Testing script

---

## ⚠️ IMPORTANT NOTES

### Server Configuration
If using AWS ELB or CloudFront, the application-level fix is complete. However:
- ELB may still add headers at the load balancer level
- CloudFront may add headers at the CDN level
- See `BACKEND_IMPLEMENTATION.md` for infrastructure-level configuration

### No Frontend Changes
This is a backend-only fix:
- Frontend code unchanged
- No frontend rebuild needed
- No frontend deployment needed
- Frontend should be tested to ensure nothing broke

### Compatibility
This fix is:
- ✅ Backward compatible
- ✅ Non-breaking
- ✅ Safe to deploy
- ✅ Minimal risk

---

## 📋 DEPLOYMENT TIMELINE

| Phase | Activity | Duration | Total |
|-------|----------|----------|-------|
| 1 | Build WAR | 5 min | 5 min |
| 2 | Backup | 2 min | 7 min |
| 3 | Deploy | 3 min | 10 min |
| 4 | Verify | 5 min | 15 min |
| 5 | Test | 3 min | 18 min |
| **Total** | | | **~20 min** |

**Recommended Deployment Window:** 30 minutes  
**Includes buffer for:** Unexpected issues, rollback if needed

---

**Deployment Status:** ⏳ READY  
**Risk Level:** 🟢 LOW  
**Impact:** Minimal  
**Rollback:** Easy

