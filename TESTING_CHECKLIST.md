# ✅ TESTING CHECKLIST - COPY & CHECK

**Data imported successfully! Now test everything step by step.** 📋

---

## 🎯 METHOD 1: SUPER QUICK (1 command, 2 minutes)

**See:** `QUICK_TEST_5MIN.md` - Copy ONE command, run it, see all results!

---

## 🎯 METHOD 2: STEP BY STEP (Follow this checklist)

### ☑️ **STEP 1: Verify Database** (30 seconds)

```bash
mysql -u root -pDechub#2025 tanishq -e "
SELECT 'Stores' as Item, COUNT(*) as Count FROM stores
UNION ALL SELECT 'Users', COUNT(*) FROM users
UNION ALL SELECT 'Events', COUNT(*) FROM events
UNION ALL SELECT 'Attendees', COUNT(*) FROM attendees;"
```

**Expected:** See actual numbers (not 0)
- [ ] Stores: ___ (should be ~450)
- [ ] Users: ___ (should be ~89)
- [ ] Events: ___ (should be ~16)
- [ ] Attendees: ___ (should be ~139)

---

### ☑️ **STEP 2: Get Test User** (10 seconds)

```bash
TEST_USER=$(mysql -u root -pDechub#2025 tanishq -e "SELECT code FROM users LIMIT 1;" -s -N)
TEST_PASS=$(mysql -u root -pDechub#2025 tanishq -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N)
echo "User: $TEST_USER, Password: $TEST_PASS"
```

**Write down:**
- [ ] User Code: ___________
- [ ] Password: ___________

---

### ☑️ **STEP 3: Test Login** (15 seconds)

```bash
curl -X POST http://localhost:3002/events/login \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}"
```

**Expected:** `{"status":true,"storeData":{...}}`
- [ ] Returns `"status":true`
- [ ] Contains store data

---

### ☑️ **STEP 4: Test Get Events** (15 seconds)

```bash
curl -X POST http://localhost:3002/events/getevents \
  -H "Content-Type: application/json" \
  -d "{\"storeCode\":\"$TEST_USER\",\"startDate\":\"2025-01-01\",\"endDate\":\"2025-12-31\"}"
```

**Expected:** Array of events or empty array
- [ ] Returns JSON array
- [ ] HTTP 200 status

---

### ☑️ **STEP 5: Test S3 Upload** (20 seconds)

```bash
TEST_EVENT=$(mysql -u root -pDechub#2025 tanishq -e "SELECT id FROM events LIMIT 1;" -s -N)
echo "test image" > /tmp/test.jpg

curl -X POST http://localhost:3002/events/uploadCompletedEvents \
  -F "eventId=$TEST_EVENT" \
  -F "files=@/tmp/test.jpg"
```

**Expected:** `{"status":true,"message":"All 1 files uploaded successfully to S3","result":[...]}`
- [ ] Returns `"status":true`
- [ ] Contains S3 URL starting with `https://celebrations-tanishq-preprod.s3`
- [ ] S3 URL is accessible

---

### ☑️ **STEP 6: Test QR Code** (10 seconds)

```bash
curl http://localhost:3002/events/dowload-qr/$TEST_EVENT
```

**Expected:** Base64 image data starting with `data:image/png;base64,...`
- [ ] Returns base64 string
- [ ] Starts with `data:image`

---

### ☑️ **STEP 7: Test Event Creation** (20 seconds)

```bash
STORE=$(mysql -u root -pDechub#2025 tanishq -e "SELECT storeCode FROM stores LIMIT 1;" -s -N)
REGION=$(mysql -u root -pDechub#2025 tanishq -e "SELECT region FROM stores WHERE storeCode='$STORE' LIMIT 1;" -s -N)

mysql -u root -pDechub#2025 tanishq -e "
INSERT INTO events (id, store_code, event_name, event_type, start_date, created_at, invitees, attendees, region)
VALUES ('TEST_CHECKLIST', '$STORE', 'Checklist Test Event', 'TEST', '2025-12-15', NOW(), 0, 0, '$REGION');"

# Verify
mysql -u root -pDechub#2025 tanishq -e "SELECT id, event_name FROM events WHERE id='TEST_CHECKLIST';"
```

**Expected:** Your new event shows up
- [ ] Event created successfully
- [ ] No foreign key errors

---

### ☑️ **STEP 8: Application Health** (10 seconds)

```bash
# Check if running
ps aux | grep tanishq_selfie_app | grep -v grep

# Check port
netstat -tlnp | grep 3002

# Check logs
tail -20 /opt/tanishq/applications_preprod/application.log
```

**Expected:**
- [ ] Application process is running
- [ ] Port 3002 is listening
- [ ] No errors in recent logs

---

## ✅ FINAL SCORE

**Count your checkmarks:**

- **8/8** ✅ = Perfect! Everything works! 🎉
- **6-7/8** ✅ = Good! Minor issues to fix
- **4-5/8** ✅ = Needs attention
- **0-3/8** ✅ = Major issues - check logs

---

## 🚀 QUICK COMMANDS SUMMARY

```bash
# Get everything ready
TEST_USER=$(mysql -u root -pDechub#2025 tanishq -e "SELECT code FROM users LIMIT 1;" -s -N)
TEST_PASS=$(mysql -u root -pDechub#2025 tanishq -e "SELECT password FROM users WHERE code='$TEST_USER' LIMIT 1;" -s -N)
TEST_EVENT=$(mysql -u root -pDechub#2025 tanishq -e "SELECT id FROM events LIMIT 1;" -s -N)

# Test all features
echo "1. Login:"
curl -s -X POST http://localhost:3002/events/login -H "Content-Type: application/json" -d "{\"code\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}" | grep -o '"status":[^,]*'

echo -e "\n2. Get Events:"
curl -s -X POST http://localhost:3002/events/getevents -H "Content-Type: application/json" -d "{\"storeCode\":\"$TEST_USER\",\"startDate\":\"2025-01-01\",\"endDate\":\"2025-12-31\"}" | head -c 100

echo -e "\n3. S3 Upload:"
echo "test" > /tmp/quick.jpg
curl -s -X POST http://localhost:3002/events/uploadCompletedEvents -F "eventId=$TEST_EVENT" -F "files=@/tmp/quick.jpg" | grep -o '"status":[^,]*'

echo -e "\n4. QR Code:"
curl -s http://localhost:3002/events/dowload-qr/$TEST_EVENT | head -c 50

echo -e "\n\n✅ Quick test complete!"
```

---

## 📚 MORE DETAILS

- **Super Quick Test (1 command):** See `QUICK_TEST_5MIN.md`
- **Complete Testing (all features):** See `COMPLETE_TESTING_GUIDE.md`
- **Troubleshooting:** Check application logs

---

**START TESTING NOW!** 🚀

Use METHOD 1 (quick) or METHOD 2 (step by step)!

