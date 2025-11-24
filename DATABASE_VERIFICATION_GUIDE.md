# How to Check if Data is Going to Database

## Current Status
- **Database:** tanishq_app
- **Host:** localhost:3306
- **Username:** root
- **Password:** Root@12345

---

## Method 1: Using PowerShell Script (Easiest)

Run the provided PowerShell script:

```powershell
.\check-database.ps1
```

This will show you counts and latest records from all tables.

---

## Method 2: Direct MySQL Commands

### Check Total Records in Each Table:

```powershell
# Events
mysql -u root -pRoot@12345 -e "SELECT COUNT(*) as total FROM tanishq_app.events;"

# Attendees
mysql -u root -pRoot@12345 -e "SELECT COUNT(*) as total FROM tanishq_app.attendees;"

# Invitees
mysql -u root -pRoot@12345 -e "SELECT COUNT(*) as total FROM tanishq_app.invitees;"

# Stores
mysql -u root -pRoot@12345 -e "SELECT COUNT(*) as total FROM tanishq_app.stores;"
```

### View Latest Records:

```powershell
# Latest 10 events
mysql -u root -pRoot@12345 -e "SELECT * FROM tanishq_app.events ORDER BY created_at DESC LIMIT 10;"

# Latest 10 attendees
mysql -u root -pRoot@12345 -e "SELECT * FROM tanishq_app.attendees ORDER BY created_at DESC LIMIT 10;"
```

### Check Specific Event:

```powershell
# Replace EVENT_ID with actual event id
mysql -u root -pRoot@12345 -e "SELECT * FROM tanishq_app.events WHERE id='EVENT_ID';"
```

---

## Method 3: Using MySQL Workbench or DBeaver

1. Connect to MySQL server:
   - Host: localhost
   - Port: 3306
   - Username: root
   - Password: Root@12345
   - Database: tanishq_app

2. Run queries:
   ```sql
   SELECT COUNT(*) FROM events;
   SELECT COUNT(*) FROM attendees;
   SELECT COUNT(*) FROM invitees;
   
   -- View latest records
   SELECT * FROM events ORDER BY created_at DESC LIMIT 10;
   SELECT * FROM attendees ORDER BY created_at DESC LIMIT 10;
   ```

---

## Method 4: Check Application Logs

Your application has `spring.jpa.show-sql=true` enabled, so watch the console logs for SQL INSERT statements:

```
Hibernate: insert into events (attendees, ...) values (?, ...)
```

If you see these statements, data is being written to the database.

---

## Method 5: Test Insert Through API

### Create a Test Event:

```powershell
# Using Invoke-WebRequest (PowerShell)
$body = @{
    code = "TEST001"
    eventName = "Test Event"
    eventType = "Wedding"
    region = "North"
    RSO = "Test RSO"
    date = "2025-11-20"
    location = "Test Location"
    Community = "Test Community"
}

Invoke-WebRequest -Uri "http://localhost:8130/events/upload" -Method POST -Body $body
```

After creating, check database:
```powershell
.\check-database.ps1
```

---

## Method 6: Monitor in Real-Time

Open two terminals:

**Terminal 1** - Run the application:
```powershell
mvn spring-boot:run
```

**Terminal 2** - Watch database changes:
```powershell
# Run this in a loop
while ($true) {
    Clear-Host
    Write-Host "Database Status at $(Get-Date)" -ForegroundColor Cyan
    mysql -u root -pRoot@12345 -e "SELECT COUNT(*) as events FROM tanishq_app.events; SELECT COUNT(*) as attendees FROM tanishq_app.attendees;" 2>$null
    Start-Sleep -Seconds 5
}
```

---

## Troubleshooting

### If data is NOT appearing in database:

1. **Check transaction is committed:**
   - Verify `@Transactional` annotation is on service methods ✅ (Already added)

2. **Check for errors in logs:**
   ```powershell
   # Look for errors like:
   # - ConstraintViolationException
   # - DataIntegrityViolationException
   # - TransactionException
   ```

3. **Verify database connection:**
   ```powershell
   mysql -u root -pRoot@12345 -e "SELECT 1;"
   ```

4. **Check if tables exist:**
   ```powershell
   mysql -u root -pRoot@12345 -e "SHOW TABLES FROM tanishq_app;"
   ```

5. **Check table structure:**
   ```powershell
   mysql -u root -pRoot@12345 -e "DESCRIBE tanishq_app.events;"
   ```

---

## Quick Test Checklist

- [ ] Application is running on port 8130
- [ ] Database connection is successful (check startup logs)
- [ ] Tables exist in database
- [ ] No errors in application logs
- [ ] API endpoint is accessible
- [ ] Test data insert via API
- [ ] Verify data appears in database using script

---

## Current Database Status

Run this to see current status:
```powershell
.\check-database.ps1
```

**Current counts:**
- Events: 0
- Attendees: 0
- Invitees: 0

This means the database is ready but no data has been inserted yet. Try creating an event through the application UI or API to test.
