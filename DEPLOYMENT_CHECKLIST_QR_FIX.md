  -F "name=Test User" \
  -F "phone=9999999999" \
  -F "like=Gold" \
  -F "firstTimeAtTanishq=true"
```

**Expected:** `{"status":true, "message":"Attendee stored successfully"}`

**4. Verify in Database:**
```sql
-- Check if test data was saved
SELECT * FROM attendees WHERE phone = '9999999999';

-- Should return 1 row
```

---

## 📊 Monitoring

### Key Metrics to Watch (First 24 Hours)

1. **Error Rate:**
   ```bash
   # Check for errors in logs
   grep -i "error\|exception\|failed" /opt/tomcat/logs/catalina.out | tail -50
   ```

2. **Attendee Submissions:**
   ```sql
   -- Check recent attendee submissions
   SELECT 
       DATE(created_at) as date,
       COUNT(*) as submissions
   FROM attendees
   WHERE created_at >= CURDATE()
   GROUP BY DATE(created_at);
   ```

3. **Event Attendee Count Accuracy:**
   ```sql
   -- Find events with mismatched counts
   SELECT 
       e.id,
       e.event_name,
       e.attendees as recorded_count,
       COUNT(a.id) as actual_count
   FROM events e
   LEFT JOIN attendees a ON e.id = a.event_id
   GROUP BY e.id, e.event_name, e.attendees
   HAVING e.attendees != COUNT(a.id);
   ```
   
   **Expected:** 0 rows (all counts should match)

4. **Application Logs:**
   ```bash
   # Watch for successful attendee submissions
   grep "=== SUCCESS storeAttendeesData ===" /opt/tomcat/logs/catalina.out

   # Watch for failures
   grep "=== FAILED storeAttendeesData ===" /opt/tomcat/logs/catalina.out
   ```

---

## 🚨 Rollback Plan

### If Issues Occur:

**1. Stop Application:**
```bash
sudo systemctl stop tomcat
```

**2. Restore Previous WAR:**
```bash
# Find latest backup
ls -ltr /opt/tanishq/app/backup_*.war | tail -1

# Restore it
cp /opt/tanishq/app/backup_YYYYMMDD_HHMMSS.war /opt/tanishq/app/tanishq.war
```

**3. Restore Database (If Schema Changed):**
```bash
# Find latest backup
ls -ltr /opt/tanishq/backups/selfie_preprod_*.sql | tail -1

# Restore
mysql -u root -p selfie_preprod < /opt/tanishq/backups/selfie_preprod_YYYYMMDD_HHMMSS.sql
```

**4. Restart Application:**
```bash
sudo systemctl start tomcat
```

**5. Verify Rollback:**
```bash
curl http://localhost:3000/events/getStoresByRegion/North
```

---

## 📝 Deployment Log

| Item | Status | Notes | Time |
|------|--------|-------|------|
| Code Review | ⏳ | | |
| Build Successful | ⏳ | | |
| Backup Created | ⏳ | | |
| Application Stopped | ⏳ | | |
| WAR Deployed | ⏳ | | |
| Application Started | ⏳ | | |
| Health Check Passed | ⏳ | | |
| Smoke Test Passed | ⏳ | | |
| Database Verified | ⏳ | | |

**Status Legend:**
- ⏳ Pending
- ✅ Complete
- ❌ Failed
- ⚠️ Warning

---

## 🔐 Security Checklist

- [ ] Database credentials secure (not in logs)
- [ ] Backup files have restricted permissions
- [ ] Application logs don't expose sensitive data
- [ ] HTTPS configured (if applicable)
- [ ] Input validation working (tested in Step 5)
- [ ] SQL injection prevention (using JPA, safe)

---

## 📞 Support Contacts

**If deployment fails:**
1. Check logs first: `/opt/tomcat/logs/catalina.out`
2. Review this checklist for troubleshooting
3. Rollback if critical

**Common Issues:**

| Issue | Solution |
|-------|----------|
| Port 3000 in use | Kill existing process or change port |
| Database connection failed | Check MySQL service, credentials |
| Foreign key constraint errors | Run schema update SQL manually |
| OutOfMemory errors | Increase JVM heap: `-Xmx2g` |

---

## ✅ Deployment Sign-Off

**Deployed By:** _______________  
**Date:** _______________  
**Time:** _______________  
**Build Version:** _______________  
**Deployment Status:** ⏳ PENDING / ✅ SUCCESS / ❌ FAILED  

**Verification:**
- [ ] Application started successfully
- [ ] Health checks passed
- [ ] Smoke tests passed
- [ ] Database connections working
- [ ] Logs showing no errors
- [ ] Attendee submission tested and working

**Notes:**
________________________________
________________________________
________________________________

---

**READY FOR PRODUCTION!** 🚀
# 🚀 QR Attendee Fix - Deployment Checklist

## 📋 Pre-Deployment Verification

### 1. Code Changes Review
- [x] `Event.java` - Added explicit VARCHAR(255) column definition
- [x] `Attendee.java` - Fixed foreign key with columnDefinition
- [x] `Invitee.java` - Fixed foreign key with columnDefinition  
- [x] `TanishqPageService.java` - Fixed null pointer exception + added logging
- [x] `EventsController.java` - Added validation + logging

### 2. Compile & Build
```bash
cd c:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Clean and compile
mvn clean compile

# Run tests (if any)
mvn test

# Package WAR file
mvn package -DskipTests
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

**WAR Location:** `target/tanishq-preprod-*.war`

### 3. Backup Current Deployment
```bash
# Backup current WAR file on server
cp /opt/tanishq/app/current.war /opt/tanishq/app/backup_$(date +%Y%m%d_%H%M%S).war

# Backup database
mysqldump -u root -p selfie_preprod > /opt/tanishq/backups/selfie_preprod_$(date +%Y%m%d_%H%M%S).sql
```

---

## 🔧 Database Migration (If Needed)

### Check Current Schema

Connect to MySQL:
```bash
mysql -u root -p selfie_preprod
```

Run these checks:
```sql
-- Check events.id column
SELECT 
    COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH 
FROM 
    information_schema.COLUMNS 
WHERE 
    TABLE_SCHEMA = 'selfie_preprod' 
    AND TABLE_NAME = 'events' 
    AND COLUMN_NAME = 'id';

-- Check attendees.event_id column
SELECT 
    COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH 
FROM 
    information_schema.COLUMNS 
WHERE 
    TABLE_SCHEMA = 'selfie_preprod' 
    AND TABLE_NAME = 'attendees' 
    AND COLUMN_NAME = 'event_id';
```

### If Schema Needs Update

**Option A: Let Hibernate Auto-Update (Recommended)**
- With `spring.jpa.hibernate.ddl-auto=update` in properties
- Hibernate will automatically alter tables on app startup
- Safer for minor changes like adding columnDefinition

**Option B: Manual Schema Update (If Needed)**
```sql
-- Only run if Hibernate doesn't update automatically

-- Drop existing foreign keys
ALTER TABLE attendees DROP FOREIGN KEY IF EXISTS FK_attendees_event_id;
ALTER TABLE invitees DROP FOREIGN KEY IF EXISTS FK_invitees_event_id;

-- Ensure columns are VARCHAR(255)
ALTER TABLE events MODIFY COLUMN id VARCHAR(255) NOT NULL;
ALTER TABLE attendees MODIFY COLUMN event_id VARCHAR(255) NOT NULL;
ALTER TABLE invitees MODIFY COLUMN event_id VARCHAR(255) NOT NULL;

-- Recreate foreign keys
ALTER TABLE attendees 
ADD CONSTRAINT FK_attendees_event_id 
FOREIGN KEY (event_id) REFERENCES events(id) 
ON DELETE CASCADE;

ALTER TABLE invitees 
ADD CONSTRAINT FK_invitees_event_id 
FOREIGN KEY (event_id) REFERENCES events(id) 
ON DELETE CASCADE;
```

---

## 🚀 Deployment Steps

### Step 1: Stop Current Application

**If using Tomcat:**
```bash
sudo systemctl stop tomcat
```

**If using standalone Java:**
```bash
# Find process
ps aux | grep tanishq

# Kill process
kill -9 <PID>
```

### Step 2: Deploy New WAR File

```bash
# Copy new WAR to deployment location
cp target/tanishq-preprod-*.war /opt/tanishq/app/tanishq.war

# Or if using Tomcat webapps
cp target/tanishq-preprod-*.war /opt/tomcat/webapps/tanishq.war
```

### Step 3: Start Application

**If using Tomcat:**
```bash
sudo systemctl start tomcat

# Check status
sudo systemctl status tomcat

# Watch logs
tail -f /opt/tomcat/logs/catalina.out
```

**If using standalone Java:**
```bash
cd /opt/tanishq/app
nohup java -jar tanishq.war > application.log 2>&1 &

# Watch logs
tail -f application.log
```

### Step 4: Verify Startup

**Check logs for:**
```
✅ Started TanishqApplication
✅ Tomcat started on port(s): 3000
✅ Hibernate: create table if not exists... (if first time)
✅ JPA repositories initialized
```

**Check for errors:**
```
❌ Error creating bean
❌ Failed to initialize
❌ Port already in use
❌ Database connection failed
```

### Step 5: Health Check

```bash
# Check if application responds
curl http://localhost:3000/events/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"code":"STORE001","password":"test123"}'

# Should return 200 OK (even if login fails, endpoint should respond)
```

---

## 🧪 Post-Deployment Testing

### Quick Smoke Test

**1. Database Connection Test:**
```bash
curl http://localhost:3000/events/getStoresByRegion/North
```

**Expected:** List of stores returned

**2. Create Test Event:**
```bash
curl -X POST "http://localhost:3000/events/upload" \
  -F "code=STORE001" \
  -F "eventName=Deployment Test Event" \
  -F "eventType=Test" \
  -F "RSO=Test RSO" \
  -F "date=2025-12-18" \
  -F "time=10:00" \
  -F "location=Test" \
  -F "Community=General"
```

**Expected:** `{"status":true, "qrData":"data:image/png;base64,..."}`

**3. Test Attendee Submission (CRITICAL!):**

```bash
# Use EVENT_ID from previous response
curl -X POST "http://localhost:3000/events/attendees" \
  -F "eventId=STORE001_xxxxx" \

