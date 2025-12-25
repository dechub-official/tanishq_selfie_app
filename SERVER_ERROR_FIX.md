### Step 3: Try Starting with Old WAR (Rollback)

The old WAR was working:
```bash
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-preprod-18-12-2025-1-0.0.1-SNAPSHOT.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

# Check if it starts
ps -ef | grep tanishq
tail -f application.log
```

If the old WAR works, then the issue is with the new build.

### Step 4: Manual Fix for events.html (Without Rebuilding)

If you want to fix the QR issue without rebuilding:

```bash
cd /opt/tanishq/applications_preprod

# 1. Extract the working WAR
mkdir temp_fix
cd temp_fix
jar -xvf ../tanishq-preprod-18-12-2025-1-0.0.1-SNAPSHOT.war

# 2. Edit events.html
nano BOOT-INF/classes/static/events.html

# Change line:
# FROM: src="/static/assets/index-CLJQELnM.js"
# TO:   src="/static/assets/index-Bl1_SFlI.js"

# FROM: href="/static/assets/index-CjU3bZCB.css"
# TO:   href="/static/assets/index-DRK0HUpC.css"

# Save (Ctrl+O, Enter, Ctrl+X)

# 3. Check what JS files actually exist
ls -la BOOT-INF/classes/static/assets/index-*.js

# 4. Repackage
jar -cvf ../tanishq-preprod-FIXED.war .
cd ..

# 5. Stop old app
ps -ef | grep tanishq
kill [PID]

# 6. Start fixed app
nohup java -jar tanishq-preprod-FIXED.war \
  --spring.datasource.url=jdbc:mysql://localhost:3306/selfie_preprod?useSSL=false\&serverTimezone=UTC \
  --spring.datasource.username=root \
  --spring.datasource.password=Dechub#2025 \
  --server.port=3000 \
  --spring.profiles.active=preprod \
  > application.log 2>&1 &

# 7. Verify
tail -f application.log
```

## Diagnosis Script

I've created a diagnosis script. Upload it to the server and run:

```bash
cd /opt/tanishq/applications_preprod
# Upload diagnose-server-error.sh using WinSCP
chmod +x diagnose-server-error.sh
./diagnose-server-error.sh > diagnosis.txt
cat diagnosis.txt
```

## What to Check in application.log

Send me the output of:
```bash
tail -100 application.log
```

Common errors to look for:
- ❌ `java.lang.ClassNotFoundException`
- ❌ `java.sql.SQLException`
- ❌ `Address already in use`
- ❌ `Failed to configure a DataSource`
- ❌ `Unable to start embedded Tomcat`

## Recommended Approach

**OPTION 1: Use Old WAR + Manual Fix (5 minutes)**
- Rollback to old WAR that works
- Extract and fix events.html manually
- Repackage and deploy
- ✅ QR code will work

**OPTION 2: Fix New Build Locally (15 minutes)**
- Find out why new WAR fails to start
- Fix the issue
- Rebuild
- Redeploy

**OPTION 3: Just Fix on Server (FASTEST - 2 minutes)**
- Keep old WAR running
- Fix events.html directly in deployed folder
- No restart needed (Spring Boot may hot-reload static files)

---

## Next Steps

1. **Check application.log** - This will tell us exactly what's wrong
2. **Send me the error** - I can help fix it
3. **Choose one of the 3 options above**

**For now, I recommend: Rollback to old WAR and use Manual Fix (Step 4 above)**

This will:
- ✅ Get the application running again
- ✅ Fix the QR code issue
- ✅ No rebuild needed
- ⏱️ Takes only 5 minutes

