# 🚨 QR CODE FORM FIX - START HERE

## 📱 Problem
When users scan the QR code for event registration, they see a **blank page** instead of the attendee registration form.

## ✅ Solution Status
- **Root cause identified:** events.html references non-existent JavaScript files
- **Fix applied:** events.html updated locally
- **Action needed:** Rebuild and redeploy to server

---

## 🎯 QUICK START (Do This Now)

### 1️⃣ Rebuild the Application

**EASIEST METHOD - Use IntelliJ IDEA:**

```
1. Open IntelliJ IDEA
2. View → Tool Windows → Maven
3. Expand "Lifecycle" folder
4. Double-click "clean"
5. Double-click "package"
6. Wait for "BUILD SUCCESS"
```

Result: New WAR file created in `target/` folder

### 2️⃣ Deploy to Server

**If you have server access:**
- Use WinSCP to upload WAR to `10.160.128.94:/opt/tanishq/applications_preprod/`
- SSH and run: `sudo bash deploy-preprod.sh`

**If someone else deploys:**
- Send them the WAR file from `target/` folder

### 3️⃣ Test

- Scan any event QR code
- Form should appear with all fields ✅

---

## 📚 Documentation Files

| File | Purpose |
|------|---------|
| **`FIX_SUMMARY.md`** | Complete summary with all steps |
| **`DO_THIS_NOW.md`** | Simplest instructions |
| **`VISUAL_FIX_GUIDE.md`** | Visual flowchart |
| **`QR_FORM_FIX_COMPLETE_GUIDE.md`** | Detailed technical guide |
| **`rebuild-qr-fix.bat`** | Automated rebuild script |
| **`verify-fix.bat`** | Verify fix before rebuild |

---

## 🔍 What Was Changed

**File:** `src/main/resources/static/events.html`

**Before:**
```html
<script type="module" crossorigin src="/static/assets/index-CLJQELnM.js"></script>
```

**After:**
```html
<script type="module" crossorigin src="/static/assets/index-Bl1_SFlI.js"></script>
```

---

## ⏱️ Time Required

- Rebuild: 2-5 minutes
- Deploy: 2-3 minutes
- **Total: 5-10 minutes**

---

## 💡 Quick Reference

**Problem:** Blank page after scanning QR code
**Cause:** Wrong JavaScript file reference
**Fix:** Update events.html (✅ done) → Rebuild → Deploy
**Status:** Ready to deploy

---

## 🆘 Need Help?

1. **Can't rebuild?** → See `DO_THIS_NOW.md` Step 1
2. **Can't deploy?** → See `FIX_SUMMARY.md` Step 2
3. **Want details?** → See `QR_FORM_FIX_COMPLETE_GUIDE.md`
4. **Want visual?** → See `VISUAL_FIX_GUIDE.md`

---

**👉 Next Action:** Open IntelliJ → Maven → Lifecycle → clean + package

