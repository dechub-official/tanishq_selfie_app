# 🚀 QUICK START - QR Code Fix

## Problem
❌ QR code scanning shows **blank page**

## Solution
✅ Added Spring MVC configuration for React Router

---

## 🔥 DEPLOY NOW

### 1. Build
```cmd
build-qr-fix.bat
```

### 2. Deploy
```bash
# Copy to server
scp target/tanishq-preprod-*.war user@server:/opt/tanishq/

# Restart on server
pkill -f tanishq-preprod
cd /opt/tanishq/applications_preprod
nohup java -jar tanishq-preprod-*.war --spring.profiles.active=preprod > app.log 2>&1 &
```

### 3. Test
```
1. Open: https://celebrationsite-preprod.tanishq.co.in/events
2. Create event → Download QR
3. Scan QR code
4. ✅ Should see attendee form (NOT blank page)
```

---

## 📁 Files Changed

### NEW FILE (Main Fix):
- ✅ `EventsWebConfig.java` - Configures React Router support

### MODIFIED:
- ✅ `events.html` - Added `<base href="/">`
- ✅ `EventsController.java` - Improved logging

---

## ✅ Success Check

After deployment, QR scanning should:
1. ✅ Open browser to attendee form
2. ✅ Show all form fields
3. ✅ Submit works
4. ✅ Data saves to database
5. ✅ Shows thank you page

---

## 🐛 If Still Blank

1. **Check logs:**
   ```bash
   tail -f application.log | grep "QR code scanned"
   ```

2. **Check browser console (F12):**
   - Look for 404 errors
   - Check network tab

3. **Verify config:**
   ```properties
   events.qr.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/
   ```

---

## 📚 Full Docs

- **Complete Guide:** `QR_BLANK_PAGE_COMPLETE_FIX_AND_TEST_GUIDE.md`
- **Summary:** `QR_BLANK_PAGE_FIX_SUMMARY.md`
- **Analysis:** `EVENT_QR_ATTENDEE_FEATURE_ANALYSIS.md`

---

**Status:** ✅ READY TO DEPLOY  
**Last Updated:** Dec 18, 2025

