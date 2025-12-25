# 🔄 QR CODE URL FLOW - VISUAL EXPLANATION

## THE COMPLETE FLOW

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    STEP 1: STORE MANAGER ACTION                         │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                    Manager clicks "Download QR Code"
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│              STEP 2: API CALL (EventsController.java)                   │
│                                                                           │
│  GET /events/dowload-qr/EVENT_12345                                     │
│                                                                           │
│  Controller receives request → Calls qrCodeService                      │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│            STEP 3: QR SERVICE (QrCodeServiceImpl.java)                  │
│                                                                           │
│  @Value("${qr.code.base.url:http://localhost:8130/events/customer/}")  │
│  private String QR_URL_BASE; ← READS FROM CONFIGURATION                │
│                                                                           │
│  generateEventQrCode(eventId) {                                         │
│      String qrUrl = QR_URL_BASE + eventId;  ← CONSTRUCTS FULL URL      │
│      return generateQrCodeBase64(qrUrl, 300, 300);                      │
│  }                                                                       │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                    🔍 WHERE DOES QR_URL_BASE COME FROM?
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│              STEP 4: SPRING BOOT PROPERTY LOADING                       │
│                                                                           │
│  Priority 1 (Lowest): Default in @Value annotation                      │
│     ❌ http://localhost:8130/events/customer/                           │
│                                                                           │
│  Priority 2: application-preprod.properties (inside WAR)                │
│     ✅ https://celebrationsite-preprod.tanishq.co.in/events/customer/   │
│                                                                           │
│  Priority 3 (Higher): External config file (if exists)                  │
│     ⚠️ /opt/tanishq/applications_preprod/config/application-preprod.properties │
│     ⚠️ Could override with OLD value!                                   │
│                                                                           │
│  Priority 4 (Highest): Environment variable                             │
│     ⚠️ QR_CODE_BASE_URL=...                                             │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    STEP 5: QR CODE GENERATED                            │
│                                                                           │
│  ❓ What URL gets embedded in QR code?                                  │
│                                                                           │
│  ❌ WRONG: http://10.160.128.94:3000/events/customer/EVENT_12345       │
│  ✅ RIGHT: https://celebrationsite-preprod.tanishq.co.in/events/customer/EVENT_12345 │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                  STEP 6: CUSTOMER SCANS QR CODE                         │
│                                                                           │
│  If URL is WRONG (internal IP):                                         │
│    ❌ Customer phone cannot reach 10.160.128.94                         │
│    ❌ Connection timeout                                                 │
│    ❌ Event registration fails                                           │
│                                                                           │
│  If URL is RIGHT (public domain):                                       │
│    ✅ Customer phone resolves celebrationsite-preprod.tanishq.co.in    │
│    ✅ Form loads successfully                                            │
│    ✅ Attendee can register                                              │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🔍 ROOT CAUSE SCENARIOS

### Scenario A: Old WAR File Deployed
```
┌──────────────────────┐     ┌──────────────────────┐     ┌──────────────────────┐
│   YOUR LOCAL CODE    │     │   COMPILED WAR FILE  │     │   SERVER RUNNING     │
│                      │     │                      │     │                      │
│  ✅ application-     │     │  ✅ application-     │     │  ❌ OLD WAR          │
│     preprod.         │     │     preprod.         │     │     (December 3)     │
│     properties       │────▶│     properties       │     │                      │
│                      │     │                      │     │  OLD CONFIG:         │
│  CORRECT URL:        │     │  CORRECT URL:        │     │  http://10.160...    │
│  celebrationsite-    │     │  celebrationsite-    │     │                      │
│  preprod...          │     │  preprod...          │     │                      │
│                      │     │                      │     │                      │
│  Updated: Dec 8      │     │  Built: Dec 10       │     │  Deployed: Dec 3     │
└──────────────────────┘     └──────────────────────┘     └──────────────────────┘
         ✅                           ✅                            ❌
    CODE IS FIXED              WAR IS CORRECT              DEPLOYMENT IS OLD
```

### Scenario B: External Config Override
```
┌──────────────────────────────────────────────────────────────────────────┐
│                           SERVER FILE SYSTEM                              │
│                                                                            │
│  /opt/tanishq/applications_preprod/                                       │
│  ├── tanishq-selfie.war  ← Contains CORRECT config                       │
│  │   └── WEB-INF/classes/application-preprod.properties                  │
│  │       qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in  │
│  │                                                                         │
│  └── config/  ← External config directory                                 │
│      └── application-preprod.properties  ← If this exists...              │
│          qr.code.base.url=http://10.160.128.94:3000  ← ...it OVERRIDES! │
│                                                                            │
│  Spring Boot loads external config AFTER internal config                 │
│  External config WINS! ❌                                                 │
└──────────────────────────────────────────────────────────────────────────┘
```

### Scenario C: Wrong Profile Active
```
┌──────────────────────────────────────────────────────────────────────────┐
│                      APPLICATION STARTUP                                  │
│                                                                            │
│  Expected:                                                                 │
│  java -jar -Dspring.profiles.active=preprod tanishq-selfie.war           │
│         ✅ Loads application-preprod.properties                           │
│                                                                            │
│  If Missing Profile:                                                      │
│  java -jar tanishq-selfie.war                                            │
│         ❌ Loads application.properties only                              │
│         ❌ Uses default value from @Value annotation                      │
│         ❌ http://localhost:8130/events/customer/                         │
│                                                                            │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## ✅ THE SOLUTION - DECISION TREE

```
                            Start Here
                                │
                                ▼
                    ┌───────────────────────┐
                    │ Can you access server?│
                    └───────────┬───────────┘
                                │
                ┌───────────────┴───────────────┐
                │ YES                           │ NO
                ▼                               ▼
    ┌─────────────────────┐         ┌──────────────────────┐
    │ Check what's running│         │ Ask someone who can  │
    │ on server            │         │ access the server    │
    └──────────┬──────────┘         └──────────────────────┘
               │
               ▼
    ┌─────────────────────────────┐
    │ Find currently deployed WAR │
    │ ls -lh *.war                │
    └──────────┬──────────────────┘
               │
               ▼
    ┌─────────────────────────────┐
    │ Check WAR file date/time    │
    └──────────┬──────────────────┘
               │
               ├─────────────────────────┬──────────────────────────┐
               │ Before Dec 10           │ Dec 10 or later          │
               ▼                         ▼                          │
    ┌─────────────────────┐   ┌─────────────────────┐            │
    │ OLD WAR - Solution: │   │ Check external config│            │
    │ Upload new WAR from │   │ files on server      │            │
    │ target/ directory   │   └──────────┬───────────┘            │
    └─────────────────────┘              │                        │
                                         ▼                         │
                          ┌──────────────────────────┐            │
                          │ find . -name "*.properties"│          │
                          └──────────┬─────────────────┘          │
                                     │                            │
                      ┌──────────────┴──────────────┐            │
                      │ Found external config?      │            │
                      └──────────┬──────────────────┘            │
                                 │                               │
                  ┌──────────────┴──────────────┐               │
                  │ YES                          │ NO            │
                  ▼                              ▼               │
      ┌────────────────────┐         ┌───────────────────────┐  │
      │ Check its contents │         │ Check profile setting │  │
      │ for qr.code.base.url│        │ in startup command    │  │
      └────────┬───────────┘         └───────────┬───────────┘  │
               │                                  │              │
               ▼                                  ▼              │
    ┌─────────────────────┐         ┌────────────────────────┐ │
    │ Has old internal IP?│         │ -Dspring.profiles.      │ │
    │                     │         │  active=preprod ?       │ │
    └────────┬────────────┘         └────────────┬───────────┘ │
             │ YES                                │             │
             ▼                                    ▼             │
  ┌──────────────────┐                ┌──────────────────────┐│
  │ Update or delete │                │ YES: Check logs for  ││
  │ external config  │                │ actual loaded value  ││
  │ file             │                │                      ││
  └──────────────────┘                │ NO: Add profile flag ││
                                      └──────────────────────┘│
                                                              │
                                                              ▼
                                                    ┌─────────────────┐
                                                    │ Restart app and │
                                                    │ test QR codes   │
                                                    └─────────────────┘
```

---

## 📝 QUICK DIAGNOSTIC CHECKLIST

Run these commands on the server to diagnose:

```bash
# 1. Check deployed WAR file date
ls -lh /opt/tanishq/applications_preprod/*.war
# Expected: Should be December 10 or later

# 2. Check for external config files
find /opt/tanishq/applications_preprod -name "*.properties"
# Should NOT find any external config OR they should have correct URL

# 3. Check running process
ps aux | grep tanishq | grep -v grep
# Should show: -Dspring.profiles.active=preprod

# 4. Check what URL is actually loaded
tail -100 /opt/tanishq/applications_preprod/logs/application.log | grep -i "qr"
# Look for log messages mentioning QR code URL

# 5. Test QR code generation
curl http://localhost:3000/events/dowload-qr/TEST123
# Decode the base64 QR code to see what URL is inside
```

---

## 🎯 MOST LIKELY CAUSE

Based on analysis, **Scenario A** is most likely:

**The server is running an OLD WAR file** that was deployed before the configuration fix was applied.

### Evidence:
- ✅ Source code has correct URL
- ✅ Compiled WAR (Dec 10) has correct URL  
- ⚠️ Server deployment status unknown
- 📅 Configuration was updated around Dec 8
- 📅 Current WAR was built Dec 10
- ❓ When was it deployed to server?

### Solution:
**Simply redeploy the existing WAR file** from:
```
C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\target\tanishq-preprod-10-12-2025-1-0.0.1-SNAPSHOT.war
```

**No rebuild needed!** The WAR already has the correct configuration.

---

**Created:** December 15, 2025  
**Purpose:** Visual explanation of QR code URL configuration flow  
**For:** Understanding and diagnosing the QR code URL issue

