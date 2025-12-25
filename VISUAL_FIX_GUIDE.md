# 🔄 QR CODE FIX - VISUAL WORKFLOW

```
┌─────────────────────────────────────────────────────────────────┐
│                    THE PROBLEM                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  User scans QR code                                            │
│       ↓                                                         │
│  Browser opens: /events/customer/TEST_xxx                      │
│       ↓                                                         │
│  Server forwards to: /events.html                              │
│       ↓                                                         │
│  events.html tries to load: index-CLJQELnM.js ❌               │
│       ↓                                                         │
│  404 Not Found - File doesn't exist!                           │
│       ↓                                                         │
│  React app doesn't load                                        │
│       ↓                                                         │
│  BLANK PAGE - No form shows! ❌                                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    THE FIX                                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  STEP 1: Update events.html (✅ DONE)                          │
│  ───────────────────────────────────────────                   │
│  Change: index-CLJQELnM.js → index-Bl1_SFlI.js                 │
│  Location: src/main/resources/static/events.html               │
│  Status: ✅ Already fixed locally                              │
│                                                                 │
│  STEP 2: Rebuild Application (⏳ YOUR ACTION)                   │
│  ───────────────────────────────────────────                   │
│  Method 1: IntelliJ IDEA ⭐                                     │
│    Maven → Lifecycle → clean → package                         │
│                                                                 │
│  Method 2: Command Line                                        │
│    mvn clean package -DskipTests                               │
│                                                                 │
│  Method 3: Run Script                                          │
│    rebuild-qr-fix.bat                                          │
│                                                                 │
│  Result: ✅ New WAR file in target/ folder                     │
│                                                                 │
│  STEP 3: Upload to Server (⏳ YOUR ACTION)                      │
│  ───────────────────────────────────────────                   │
│  Tool: WinSCP                                                  │
│  From: C:\JAVA\...\target\tanishq-preprod-*.war                │
│  To: 10.160.128.94:/opt/tanishq/applications_preprod/         │
│                                                                 │
│  STEP 4: Deploy on Server (⏳ YOUR ACTION)                      │
│  ───────────────────────────────────────────                   │
│  SSH: ssh root@10.160.128.94                                   │
│  Run: cd /opt/tanishq/applications_preprod                     │
│       sudo bash deploy-preprod.sh                              │
│                                                                 │
│  STEP 5: Test (⏳ YOUR ACTION)                                  │
│  ───────────────────────────────────────────                   │
│  1. Open Events Dashboard                                      │
│  2. Get QR code from any event                                 │
│  3. Scan with phone                                            │
│  4. Verify: Form appears ✅                                     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                 AFTER THE FIX                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  User scans QR code                                            │
│       ↓                                                         │
│  Browser opens: /events/customer/TEST_xxx                      │
│       ↓                                                         │
│  Server forwards to: /events.html                              │
│       ↓                                                         │
│  events.html tries to load: index-Bl1_SFlI.js ✅               │
│       ↓                                                         │
│  200 OK - File exists and loads!                               │
│       ↓                                                         │
│  React app loads successfully                                  │
│       ↓                                                         │
│  FORM APPEARS! ✅                                               │
│       ↓                                                         │
│  User fills: Name, Phone, RSO                                  │
│       ↓                                                         │
│  Clicks Submit                                                 │
│       ↓                                                         │
│  POST /events/attendees                                        │
│       ↓                                                         │
│  Data saved to database ✅                                      │
│       ↓                                                         │
│  Success message shown! 🎉                                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                   QUICK CHECKLIST                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ✅ Problem identified: Wrong JS file reference                │
│  ✅ events.html updated locally                                │
│  ⏳ Rebuild WAR file                    ← YOU ARE HERE          │
│  ⏳ Upload to server                                            │
│  ⏳ Deploy on server                                            │
│  ⏳ Test QR code scan                                           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

═══════════════════════════════════════════════════════════════════
                     NEXT ACTION FOR YOU
═══════════════════════════════════════════════════════════════════

👉 OPEN INTELLIJ IDEA
👉 MAVEN → LIFECYCLE → CLEAN
👉 MAVEN → LIFECYCLE → PACKAGE
👉 WAIT FOR "BUILD SUCCESS"
👉 THEN DEPLOY TO SERVER

═══════════════════════════════════════════════════════════════════
                         TIME ESTIMATE
═══════════════════════════════════════════════════════════════════

Rebuild:     2-5 minutes
Upload:      1-2 minutes
Deploy:      1-2 minutes
Test:        1 minute
───────────────────────
TOTAL:       5-10 minutes ⏱️

═══════════════════════════════════════════════════════════════════

