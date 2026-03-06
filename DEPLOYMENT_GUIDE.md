# Quick Deployment Guide - Event Creation Fix

## 📋 Pre-Deployment Checklist

- [ ] Backup current WAR file on server
- [ ] Notify users of brief downtime (2-3 minutes)
- [ ] Have rollback plan ready

---

## 🔨 Build the Application

### On Windows (Your Current System):

```powershell
# Navigate to project directory
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app_clean

# Run the build script
.\build-and-package.ps1

# OR manually:
mvn clean package -DskipTests
```

**Expected output**: WAR file created in `target\` directory

---

## 📤 Upload to Server

```powershell
# Find the WAR file
$warFile = Get-ChildItem -Path target -Filter "*.war" | Select-Object -First 1

# Upload using SCP (adjust server details)
scp $warFile.FullName root@your-server-ip:/tmp/tanishq-preprod-new.war

# OR use your preferred file transfer method (WinSCP, FileZilla, etc.)
```

---

## 🚀 Deploy on Server

```bash
# SSH into the server
ssh root@your-server-ip

# 1. Backup current WAR file
cd /opt/tanishq/applications_preprod
cp tanishq-preprod.war tanishq-preprod.war.backup_$(date +%Y%m%d_%H%M%S)

# 2. Stop application server
systemctl stop tomcat
# OR if using different server:
# systemctl stop wildfly
# systemctl stop jetty

# 3. Replace WAR file
mv /tmp/tanishq-preprod-new.war /opt/tanishq/applications_preprod/tanishq-preprod.war

# 4. Clear temp/cache directories (optional but recommended)
rm -rf /opt/tomcat/work/*
rm -rf /opt/tomcat/temp/*

# 5. Start application server
systemctl start tomcat

# 6. Wait for startup
echo "Waiting for application to start..."
sleep 15
```

---

## ✅ Verify Deployment

```bash
# 1. Check application logs
tail -50 /opt/tanishq/applications_preprod/application.log

# Look for:
# - "Application started" or similar
# - No ERROR or EXCEPTION messages

# 2. Check if server is responding
curl -I http://localhost:3000/events/

# Expected: HTTP 200 or 302 (redirect)

# 3. Check log for the fix
grep "Using authenticated user" /opt/tanishq/applications_preprod/application.log

# This will show log entries once users start creating events
```

---

## 🧪 Test Event Creation

### From Browser:

1. Open: https://celebrationsite-preprod.tanishq.co.in/events/dashboard
2. Log in with test credentials (e.g., user: TEST, password: ***)
3. Click "Create Event"
4. Fill in event details:
   - Event Name: "Deployment Test Event"
   - Event Type: "HOME VISITS AND REACH OUTS"
   - Event Sub Type: Select any
   - Date: Select today's date
   - Time: Current time
   - Customer Name: "Test Customer"
   - Customer Contact: "9876543210"
5. Click "Create Event"

**✅ Expected Result**: 
- Success message displayed
- No "Access denied" error
- Event appears in dashboard

### Verify in Logs:

```bash
# Watch logs in real-time
tail -f /opt/tanishq/applications_preprod/application.log

# You should see:
# INFO: Using authenticated user 'TEST' as store code for event creation
# DEBUG: Event created successfully
```

---

## 🔧 Troubleshooting

### Problem: Application won't start

```bash
# Check for port conflicts
netstat -tlnp | grep 3000

# Check logs for errors
tail -100 /opt/tanishq/applications_preprod/application.log | grep -i error

# Verify Java process
ps aux | grep java

# Check system resources
free -h
df -h
```

### Problem: Still getting "Access denied"

```bash
# 1. Verify fix was deployed
strings /opt/tanishq/applications_preprod/tanishq-preprod.war | grep "Using authenticated user"

# Should show the log message from our fix

# 2. Check session
grep "authenticated" /opt/tanishq/applications_preprod/application.log | tail -10

# Verify user logged in successfully

# 3. Clear browser cache
# In browser: Ctrl+Shift+Delete → Clear cache and cookies
```

### Problem: Other errors

```bash
# Check database connectivity
mysql -u tanishq_user -p tanishq_db -e "SELECT COUNT(*) FROM users;"

# Check file permissions
ls -l /opt/tanishq/applications_preprod/tanishq-preprod.war

# Check disk space
df -h

# Full log review
tail -200 /opt/tanishq/applications_preprod/application.log
```

---

## ↩️ Rollback (If Needed)

```bash
# 1. Stop application
systemctl stop tomcat

# 2. Restore backup
cp /opt/tanishq/applications_preprod/tanishq-preprod.war.backup_YYYYMMDD_HHMMSS \
   /opt/tanishq/applications_preprod/tanishq-preprod.war

# 3. Start application
systemctl start tomcat

# 4. Verify
tail -f /opt/tanishq/applications_preprod/application.log
```

---

## 📞 Post-Deployment

### Immediate (within 1 hour):
- [ ] Monitor logs for errors
- [ ] Test event creation with multiple users
- [ ] Verify existing events still load correctly
- [ ] Check database for new event entries

### Short-term (within 24 hours):
- [ ] Monitor user reports
- [ ] Check system performance
- [ ] Verify all event types work
- [ ] Test with ABM/RBM/CEE users (if applicable)

### Follow-up (within 1 week):
- [ ] Review security logs
- [ ] Verify no authorization bypasses
- [ ] Check for any unexpected behavior
- [ ] Remove old backup files (keep 1-2 most recent)

---

## ✅ Success Criteria

The deployment is successful when:

✅ Users can create events without "Access denied" errors  
✅ Logs show: "Using authenticated user 'XXX' as store code for event creation"  
✅ Events are created with correct store codes  
✅ Store users cannot create events for other stores  
✅ ABM/RBM/CEE users can still manage multiple stores  
✅ No new errors in application logs  
✅ System performance is normal  

---

## 📝 Documentation

After successful deployment, update:

- [ ] Deployment log with date/time
- [ ] Change management system (if applicable)
- [ ] Team communication (notify of successful deployment)
- [ ] User documentation (if needed)

---

## 🎯 Quick Commands Reference

```bash
# View logs
tail -f /opt/tanishq/applications_preprod/application.log

# Restart application
systemctl restart tomcat

# Check status
systemctl status tomcat

# Filter errors
grep -i error /opt/tanishq/applications_preprod/application.log | tail -20

# Check database
mysql -u tanishq_user -p tanishq_db

# View recent events
mysql -u tanishq_user -p tanishq_db -e "SELECT id, store_code, event_name, created_at FROM events ORDER BY created_at DESC LIMIT 10;"
```

---

**Deployment Completed By**: ________________

**Date/Time**: ________________

**Version Deployed**: ________________

**Status**: 
- [ ] ✅ Success
- [ ] ⚠️ Success with minor issues (document below)
- [ ] ❌ Failed (rolled back)

**Notes**:
```
_________________________________________________
_________________________________________________
_________________________________________________
```

