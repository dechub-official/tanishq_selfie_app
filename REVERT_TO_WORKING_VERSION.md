# 🔄 REVERT TO LAST WORKING VERSION

## Current Situation
You want to revert to the last working version:
- **File:** `tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war`
- **Location:** `/opt/tanishq/applications_preprod/`
- **Status:** ✅ File exists on server (visible in your screenshot)

---

## ⚡ QUICK REVERT (Copy-Paste These Commands)

### Option 1: Manual Commands (2 minutes)

Copy-paste these into your SSH terminal:

```bash
# You're already in the right directory
cd /opt/tanishq/applications_preprod

# Stop current application
pkill -9 -f "tanishq.*\.war"
pkill -9 -f "selfie.*\.war"

# Verify the file exists
ls -lh tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war

# Start the last working version
nohup java -jar \
  -Dserver.port=3000 \
  -Dspring.profiles.active=preprod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war \
  > application.log 2>&1 &

# Monitor startup
tail -f application.log
```

Press `Ctrl+C` when you see `Started TanishqSelfieApplication`

Then verify:
```bash
# Check for errors
tail -100 application.log | grep -i "error\|exception"

# Check process is running
ps -ef | grep tanishq | grep -v grep
```

---

### Option 2: Automated Script

1. **Upload revert script** via WinSCP:
   ```
   Local:  C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\revert-to-working.sh
   Remote: /opt/tanishq/applications_preprod/revert-to-working.sh
   ```

2. **Run the script:**
   ```bash
   sudo bash /opt/tanishq/applications_preprod/revert-to-working.sh
   ```

---

## 📋 What This Version Contains

The `tanishq-preprod-08-12-2025-3` version:
- ✅ Has the main selfie app working
- ✅ Has events page working
- ⚠️ May have the path resolution issue (from this morning)
- ⚠️ pom.xml has frontend properties (might overwrite files on rebuild)

**Note:** This is the version from this morning, before the afternoon changes. It may have some of the issues we discovered, but if it was working for you last time, it should work now.

---

## 🔍 What's Different from Current Code

### This Morning's Version (What you're reverting to):
```xml
pom.xml:
<artifactId>tanishq-preprod-08-12-2025-3</artifactId>
<properties>
    <frontend.source.dir>...</frontend.source.dir>  ← Has frontend properties
</properties>
```

### Fixed Version (What we were working on):
```xml
pom.xml:
<artifactId>tanishq-preprod</artifactId>
<properties>
    <!-- Empty -->  ← Safer
</properties>

index.html: Restored from backup
ReactResourceResolver.java: Fixed path logic
```

---

## ⚠️ Important Notes

### After Reverting:
1. The application should start and run
2. BUT if you rebuild with Maven, the frontend properties might overwrite files again
3. The fixes we made (index.html, ReactResourceResolver) are NOT in this WAR

### If You Want the Fixes:
- Keep the current source code (with my fixes)
- Just use the old WAR temporarily
- Later, build new WAR with the fixes when Maven is available

---

## ✅ Verification After Revert

```bash
# 1. Check process is running
ps -ef | grep tanishq-preprod-08-12-2025-3 | grep -v grep

# 2. Check logs for startup
tail -50 application.log | grep "Started"

# 3. Check for errors
tail -100 application.log | grep -i "error\|exception"

# 4. Test URLs
curl -I http://10.160.128.94:3000/
curl -I http://10.160.128.94:3000/events
curl -I http://10.160.128.94:3000/selfie
```

All should return `HTTP/1.1 200`

---

## 🌐 Test in Browser

After starting, test these URLs:

1. **Landing:** http://10.160.128.94:3000/
2. **Events:** http://10.160.128.94:3000/events
3. **Selfie:** http://10.160.128.94:3000/selfie

If they all work, you're good! ✅

---

## 🎯 Next Steps (After Revert)

### Immediate:
1. ✅ Deploy old WAR (tanishq-preprod-08-12-2025-3)
2. ✅ Verify application works
3. ✅ Test all URLs in browser

### Later (When You Have Time):
1. Keep the source code with my fixes (pom.xml, index.html, ReactResourceResolver)
2. When Maven is available, build new WAR with fixes
3. Deploy the new fixed version
4. This will give you long-term stability

---

## 📊 File Comparison

| File | Size (on server) | Date |
|------|-----------------|------|
| tanishq-preprod-08-12-2025-3-*.war | (see ls output) | Dec 8, morning |
| tanishq-preprod-08-12-2025-2-*.war | (if exists) | Dec 8 |
| selfie-08-12-2025-6-*.war | (newer) | Dec 8, afternoon |

You're reverting to the **morning version** which was last working.

---

## 🚨 If Revert Fails

### Problem: File not found
```bash
# List all WAR files
ls -lh /opt/tanishq/applications_preprod/*.war

# Use exact filename from the list
```

### Problem: Application won't start
```bash
# Check logs
tail -200 application.log

# Check port is free
netstat -tlnp | grep 3000

# Kill any zombie processes
pkill -9 -f java
```

### Problem: Still getting errors
If the old version also has errors:
- Consider building the fixed version instead
- Or upload one of the other `tanishq-preprod-*` WAR files from server

---

## Summary

**What You're Doing:**
- Reverting to: `tanishq-preprod-08-12-2025-3-0.0.1-SNAPSHOT.war`
- This was your last working deployment
- It's on the server and ready to run

**How to Do It:**
1. Stop current app: `pkill -9 -f ".war"`
2. Start old version: `nohup java -jar tanishq-preprod-08-12-2025-3-*.war ...`
3. Monitor: `tail -f application.log`

**Time Required:** 2 minutes

**Risk:** LOW (you're going back to what worked)

---

**Ready to revert? Just copy-paste the commands from Option 1 above!** 🚀

