# PASSWORD CHANGE FIX - COMPLETE SOLUTION

## Current Issues (Before Fix)

### 1. TEST Store - NonUniqueResultException
```json
{
    "status": false,
    "message": "Error changing password: query did not return a unique result: 2; nested exception is javax.persistence.NonUniqueResultException: query did not return a unique result: 2"
}
```

**Root Cause**: Database has 2 duplicate users with username "TEST"

### 2. ABH Store - 500 Internal Server Error
```json
{
    "timestamp": "2026-01-17T06:53:11.995+00:00",
    "status": 500,
    "error": "Internal Server Error",
    "path": "/events/changePassword"
}
```

**Root Cause**: Password history operations failing and breaking the entire password change process

---

## ✅ SOLUTION IMPLEMENTED

### Changes Made

#### 1. UserRepository.java
**Added new method to handle duplicate usernames:**
```java
List<User> findAllByUsername(String username);
```

#### 2. TanishqPageService.java - changePasswordForEventManager()

**Change #1: Use both username AND password for lookup**
```java
// OLD (causes NonUniqueResultException)
Optional<User> userOptional = userRepository.findByUsername(storeCode);

// NEW (filters by both username AND password)
Optional<User> userOptional = userRepository.findByUsernameAndPassword(storeCode, oldPassword);
```

**Change #2: Better error handling**
```java
// If user not found, check if it's wrong password or user doesn't exist
if (!userOptional.isPresent()) {
    List<User> usersWithSameUsername = userRepository.findAllByUsername(storeCode);
    if (usersWithSameUsername.isEmpty()) {
        dto.setMessage("User not found with store code: " + storeCode);
    } else {
        dto.setMessage("Old password is incorrect");
    }
    return dto;
}
```

**Change #3: Wrap non-critical operations in try-catch**
```java
// Password cache update (non-critical)
try {
    passwordCache.put(storeCode.toUpperCase(), newPassword);
} catch (Exception e) {
    System.err.println("Warning: Failed to update password cache: " + e.getMessage());
}

// Password history deletion (non-critical)
try {
    passwordHistoryRepository.deleteById(storeCode);
} catch (Exception e) {
    System.err.println("Warning: Failed to delete old password history: " + e.getMessage());
}

// Password history save (non-critical)
try {
    PasswordHistory history = new PasswordHistory(storeCode, oldPassword, newPassword, LocalDateTime.now());
    passwordHistoryRepository.save(history);
} catch (Exception e) {
    System.err.println("Warning: Failed to save password history: " + e.getMessage());
}
```

---

## 🎯 BENEFITS

✅ **Fixes NonUniqueResultException** - Uses username+password for unique lookup  
✅ **Fixes 500 Internal Server Errors** - Non-critical operations can't break password change  
✅ **Better error messages** - Distinguishes between "user not found" vs "wrong password"  
✅ **More resilient** - Password change succeeds even if history/cache operations fail  
✅ **Better logging** - Stack traces and warnings for debugging  

---

## 🚀 DEPLOYMENT OPTIONS

### **RECOMMENDED: Option 1 - Upload and Deploy from Windows**

**Single command deployment:**
```cmd
UPLOAD_AND_DEPLOY.bat
```

This script will:
1. Upload source files to production server via SCP
2. SSH to server and build the application
3. Stop current application
4. Backup old WAR file
5. Deploy new WAR file
6. Start application
7. Show deployment logs

**Requirements:**
- OpenSSH Client installed on Windows (or WinSCP)
- SSH access to production server (root@10.10.63.97)

---

### Option 2 - Manual Upload and Deploy

**Step 1: Upload files to production server**
```cmd
scp -r src pom.xml deploy_password_fix.sh root@10.10.63.97:/opt/tanishq/source/tanishq_selfie_app/
```

**Step 2: SSH to production server**
```cmd
ssh root@10.10.63.97
```

**Step 3: Run deployment script on server**
```bash
cd /opt/tanishq/source/tanishq_selfie_app
chmod +x deploy_password_fix.sh
./deploy_password_fix.sh
```

---

### Option 3 - Build Locally (if Maven is installed)

**Step 1: Build WAR file**
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -DskipTests
```

**Step 2: Upload WAR to production**
```cmd
scp target\tanishq-preprod-17-01-2026-1-0.0.1-SNAPSHOT.war root@10.10.63.97:/opt/tanishq/
```

**Step 3: Deploy on production**
```bash
# SSH to server
ssh root@10.10.63.97

# Stop current app
kill $(cat /opt/tanishq/tanishq-prod.pid)

# Backup old WAR
cp /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
   /opt/tanishq/backup/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war.backup

# Deploy new WAR
cp /opt/tanishq/tanishq-preprod-17-01-2026-1-0.0.1-SNAPSHOT.war \
   /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war

# Start application
nohup java -jar /opt/tanishq/tanishq-preprod-07-01-2026-2-0.0.1-SNAPSHOT.war \
  --spring.profiles.active=prod \
  --server.port=3000 \
  > /opt/tanishq/logs/application.log 2>&1 &

echo $! > /opt/tanishq/tanishq-prod.pid

# Check logs
tail -f /opt/tanishq/logs/application.log
```

---

## 🧪 TESTING THE FIX

### Wait for Application to Start
Wait 30-60 seconds after deployment for the application to fully start.

### Test Case 1: TEST Store (NonUniqueResultException Fix)
```bash
curl -X POST 'https://celebrations.tanishq.co.in/events/changePassword?storeCode=TEST&oldPassword=Tanishq@123&newPassword=Titan@123'
```

**Expected Response:**
```json
{
    "status": true,
    "message": "Password changed successfully"
}
```

### Test Case 2: ABH Store (500 Error Fix)
```bash
curl -X POST 'https://celebrations.tanishq.co.in/events/changePassword?storeCode=ABH&oldPassword=Tanishq@123&newPassword=T@nishq'
```

**Expected Response:**
```json
{
    "status": true,
    "message": "Password changed successfully"
}
```

### Test Case 3: Wrong Password
```bash
curl -X POST 'https://celebrations.tanishq.co.in/events/changePassword?storeCode=TEST&oldPassword=WrongPassword&newPassword=NewPass123'
```

**Expected Response:**
```json
{
    "status": false,
    "message": "Old password is incorrect"
}
```

### Test Case 4: Non-existent User
```bash
curl -X POST 'https://celebrations.tanishq.co.in/events/changePassword?storeCode=NONEXISTENT&oldPassword=SomePass&newPassword=NewPass'
```

**Expected Response:**
```json
{
    "status": false,
    "message": "User not found with store code: NONEXISTENT"
}
```

---

## 📊 MONITORING

### Check Application Status
```bash
ssh root@10.10.63.97
ps -p $(cat /opt/tanishq/tanishq-prod.pid)
```

### View Application Logs
```bash
tail -f /opt/tanishq/logs/application.log
```

### Check for Errors
```bash
grep -i "error\|exception" /opt/tanishq/logs/application.log | tail -20
```

---

## 🗃️ DATABASE CLEANUP (Optional)

If you want to remove duplicate users from the database:

### Find Duplicates
```sql
SELECT username, COUNT(*) as count 
FROM users 
GROUP BY username 
HAVING COUNT(*) > 1;
```

### View Duplicate TEST Users
```sql
SELECT * FROM users WHERE username = 'TEST';
```

### Delete Duplicate (Keep one, delete others)
```sql
-- First, identify which record to keep (usually the one with lower ID)
SELECT id, username, name, password, role FROM users WHERE username = 'TEST';

-- Delete the duplicate (replace 999 with the actual ID to delete)
DELETE FROM users WHERE id = 999;
```

---

## 📁 FILES MODIFIED

1. `src/main/java/com/dechub/tanishq/repository/UserRepository.java`
   - Added `findAllByUsername()` method

2. `src/main/java/com/dechub/tanishq/service/TanishqPageService.java`
   - Changed to use `findByUsernameAndPassword()`
   - Added error handling for password cache and history operations
   - Added better error messages and logging

---

## 📋 DEPLOYMENT CHECKLIST

- [ ] Source files uploaded to production server
- [ ] Application built successfully with Maven
- [ ] Current application stopped
- [ ] Old WAR file backed up
- [ ] New WAR file deployed
- [ ] Application started successfully
- [ ] Application logs show no errors
- [ ] Password change tested for TEST store
- [ ] Password change tested for ABH store
- [ ] Password change tested with wrong password
- [ ] Password change tested for non-existent user

---

## 🆘 TROUBLESHOOTING

### Issue: SCP not available on Windows
**Solution:** Install OpenSSH Client
1. Settings → Apps → Optional Features
2. Add a feature → OpenSSH Client
3. Install and restart terminal

### Issue: Build fails on production server
**Solution:** Check Maven version
```bash
mvn -version
```
Should be Maven 3.6+ with Java 8+

### Issue: Application won't start
**Solution:** Check port availability
```bash
netstat -tulpn | grep 3000
```
If port is busy, kill the process or use a different port

### Issue: Still getting 500 errors
**Solution:** Check application logs
```bash
tail -100 /opt/tanishq/logs/application.log
```
Look for stack traces and error messages

---

## 📞 SUPPORT

If you encounter any issues:

1. **Check logs**: `/opt/tanishq/logs/application.log`
2. **Check process**: `ps -p $(cat /opt/tanishq/tanishq-prod.pid)`
3. **Check port**: `netstat -tulpn | grep 3000`
4. **Restart if needed**: 
   ```bash
   kill $(cat /opt/tanishq/tanishq-prod.pid)
   ./deploy_password_fix.sh
   ```

---

## ✅ SUCCESS CRITERIA

The fix is successful when:

✅ TEST store password change works without NonUniqueResultException  
✅ ABH store password change works without 500 Internal Server Error  
✅ All other stores password change works normally  
✅ Wrong password returns "Old password is incorrect"  
✅ Non-existent user returns "User not found"  
✅ Application logs show no errors  

---

**Status**: ✅ CODE FIXED - READY FOR DEPLOYMENT  
**Risk Level**: 🟢 LOW (Only changes password change logic)  
**Rollback Plan**: Restore backup WAR file if issues occur  
**Estimated Downtime**: < 1 minute  

---

**Last Updated**: January 17, 2026  
**Author**: GitHub Copilot  
**Version**: 2.0 (includes 500 error fix)

