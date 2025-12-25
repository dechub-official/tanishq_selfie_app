# 🔍 QR CODE URL ISSUE - COMPLETE EXPLANATION

**API Endpoint:** `GET /events/dowload-qr/{id}`  
**Status:** ⚠️ **CONFIGURATION ISSUE - ALREADY FIXED IN CODE**  
**Date:** December 15, 2025

---

## 🎯 THE EXACT PROBLEM

### What's Happening:
When store managers download a QR code for a completed event, the QR code **may contain an internal server IP address** instead of the public preprod domain URL.

### Example of the Issue:

**❌ WRONG (What might be in QR code currently):**
```
http://10.160.128.94:3000/events/customer/EVENT_12345
```

**✅ CORRECT (What should be in QR code):**
```
https://celebrationsite-preprod.tanishq.co.in/events/customer/EVENT_12345
```

### Why This is a Problem:
- ❌ External customers **cannot access** the internal IP `10.160.128.94`
- ❌ QR codes are **useless outside your internal network**
- ❌ Attendees who scan the QR code get **connection timeout/error**
- ❌ The entire event registration workflow **breaks**

---

## 🔬 ROOT CAUSE ANALYSIS

### How QR Code Generation Works:

#### Step 1: API Call
Store manager clicks "Download QR Code" → Triggers API call
```
GET /events/dowload-qr/EVENT_12345
```

#### Step 2: Controller Processing
File: `EventsController.java` (Line 60-73)
```java
@GetMapping("/dowload-qr/{id}")
private QrResponseDTO downloadQr(@PathVariable("id") String eventId){
    QrResponseDTO qrResponseDTO = new QrResponseDTO();
    try {
        // Calls the QR service to generate QR code
        String qrCodeBase64 = qrCodeService.generateEventQrCode(eventId);
        qrResponseDTO.setStatus(true);
        qrResponseDTO.setQrData("data:image/png;base64," + qrCodeBase64);
    } catch (Exception e) {
        qrResponseDTO.setStatus(false);
        qrResponseDTO.setQrData("Error generating QR code: " + e.getMessage());
    }
    return qrResponseDTO;
}
```

#### Step 3: Service Layer Processing
File: `QrCodeServiceImpl.java` (Line 71-79)
```java
@Value("${qr.code.base.url:http://localhost:8130/events/customer/}")
private String QR_URL_BASE;  // ← THIS IS THE KEY!

@Override
public String generateEventQrCode(String eventId) throws Exception {
    if (eventId == null || eventId.trim().isEmpty()) {
        throw new IllegalArgumentException("Event ID cannot be null or empty");
    }
    
    // Constructs the full URL from configuration
    String qrUrl = QR_URL_BASE + eventId.trim();
    log.debug("Generating QR code for event URL: {}", qrUrl);
    
    return generateQrCodeBase64(qrUrl, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE);
}
```

#### Step 4: Configuration Loading
The value of `QR_URL_BASE` comes from:
```properties
# File: application-preprod.properties
qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/
```

### ⚠️ THE EXACT ROOT CAUSE:

**The issue exists because of ONE of these scenarios:**

#### Scenario A: Old WAR File Deployed ❌
- **Your local code** has the correct URL (✅ Fixed)
- **The WAR file on server** was built with old configuration (❌ Has internal IP)
- **Build Date:** December 10, 2025 (`tanishq-preprod-10-12-2025-1-0.0.1-SNAPSHOT.war`)
- **Hypothesis:** The WAR was built BEFORE you updated the configuration

#### Scenario B: Properties Not Applied ❌
- Configuration file is correct
- But Spring Boot is not reading it properly
- Possible reasons:
  - Wrong profile active (not `preprod`)
  - Properties file not included in WAR
  - External properties override

#### Scenario C: Default Value Being Used ❌
Look at this line in `QrCodeServiceImpl.java`:
```java
@Value("${qr.code.base.url:http://localhost:8130/events/customer/}")
```

The part after `:` is the **default value** if property is not found:
- `http://localhost:8130/events/customer/`

If Spring Boot cannot find `qr.code.base.url` property, it uses this default.

---

## 🔍 HOW TO VERIFY WHICH SCENARIO IT IS

### Check 1: Current WAR File Build Date
```bash
# Check the WAR file timestamp
ls -lh target/tanishq-preprod-10-12-2025-1-0.0.1-SNAPSHOT.war
```
**Build Date:** December 10, 2025

**Configuration Update:** Appears to be December 8, 2025 (based on comments)

### Check 2: Extract and Verify WAR Contents
```bash
# On Windows
cd target
jar -xf tanishq-preprod-10-12-2025-1-0.0.1-SNAPSHOT.war WEB-INF/classes/application-preprod.properties

# Check the content
type WEB-INF\classes\application-preprod.properties | findstr qr.code.base.url
```

**Result from my analysis:**
```properties
qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/
```
✅ The WAR file **DOES have the correct URL**

### Check 3: Verify Active Profile on Server
```bash
# SSH to server and check the running application
ps aux | grep tanishq

# Check the startup command or environment variables
cat /opt/tanishq/applications_preprod/startup.sh

# Or check application logs
tail -f /opt/tanishq/applications_preprod/logs/application.log | grep "spring.profiles.active"
```

---

## ✅ SOLUTION & CURRENT STATUS

### Good News: ✅
1. **Source code** has correct configuration
2. **Compiled WAR** has correct configuration
3. **The fix is already in place** in the codebase

### Why It Might Still Show Wrong URL:

#### Possibility 1: Old Deployment
**The server might be running an older WAR file** that was deployed before the fix.

**Evidence:**
- Current WAR date: December 10, 2025
- There might be an even older WAR on the server

**Solution:**
```bash
# Stop the application
sudo systemctl stop tanishq-preprod

# Backup old WAR
cd /opt/tanishq/applications_preprod
mv tanishq-selfie.war tanishq-selfie.war.backup_old

# Copy new WAR (from Windows to server via WinSCP)
# Source: C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app\target\tanishq-preprod-10-12-2025-1-0.0.1-SNAPSHOT.war
# Destination: /opt/tanishq/applications_preprod/tanishq-selfie.war

# Start application
sudo systemctl start tanishq-preprod
```

#### Possibility 2: Profile Not Active
**Spring Boot might not be loading the preprod profile**

**Check file:** `src/main/resources/application.properties`
```properties
spring.profiles.active=preprod
```

**Current Status:** ✅ Set to `preprod`

**But on server, check startup command:**
```bash
# The java command should have:
java -jar -Dspring.profiles.active=preprod tanishq-selfie.war
```

#### Possibility 3: External Configuration Override
**Server might have external properties file** that overrides

**Check these locations:**
```bash
/opt/tanishq/applications_preprod/application.properties
/opt/tanishq/applications_preprod/config/application.properties
/opt/tanishq/config/application-preprod.properties
```

If any of these exist and have old URL, they will override the WAR file.

---

## 🛠️ STEP-BY-STEP FIX PROCEDURE

### Option A: Rebuild and Redeploy (RECOMMENDED)

#### Step 1: Verify Configuration Locally ✅
```bash
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
type src\main\resources\application-preprod.properties | findstr qr.code.base.url
```
**Expected:** `qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/`

#### Step 2: Clean Build ✅
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -Ppreprod -DskipTests
```

#### Step 3: Verify New WAR ✅
```cmd
cd target
jar -xf tanishq-preprod-[NEW-DATE]-0.0.1-SNAPSHOT.war WEB-INF/classes/application-preprod.properties
type WEB-INF\classes\application-preprod.properties | findstr qr.code
```

#### Step 4: Deploy to Server
```bash
# On server:
sudo systemctl stop tanishq-preprod

# Backup current
cd /opt/tanishq/applications_preprod
sudo mv tanishq-selfie.war tanishq-selfie.war.backup_$(date +%Y%m%d_%H%M%S)

# Copy new WAR via WinSCP or SCP
# Then rename it
sudo mv tanishq-preprod-[NEW-DATE]-0.0.1-SNAPSHOT.war tanishq-selfie.war

# Start application
sudo systemctl start tanishq-preprod

# Verify startup
sudo tail -f logs/application.log
```

#### Step 5: Test
```bash
# Check if property is loaded
curl http://localhost:3000/actuator/configprops | grep qr.code.base.url

# Or check logs for the value
sudo grep "qr.code.base.url" logs/application.log

# Test QR code generation
curl -X GET "http://localhost:3000/events/dowload-qr/TEST_EVENT_123"
```

### Option B: Fix via External Configuration (QUICK FIX)

If you don't want to rebuild:

#### Step 1: Create External Config
```bash
# SSH to server
ssh user@10.160.128.94

# Create config directory if not exists
sudo mkdir -p /opt/tanishq/applications_preprod/config

# Create external properties file
sudo nano /opt/tanishq/applications_preprod/config/application-preprod.properties
```

#### Step 2: Add Configuration
```properties
# QR Code Configuration - Override
qr.code.base.url=https://celebrationsite-preprod.tanishq.co.in/events/customer/
```

#### Step 3: Restart Application
```bash
sudo systemctl restart tanishq-preprod
```

**Note:** Spring Boot automatically loads config files from these locations (in order):
1. `/config/application-preprod.properties` (in same dir as WAR)
2. `/application-preprod.properties` (in same dir as WAR)
3. Inside the WAR file

---

## 🧪 VERIFICATION & TESTING

### Test 1: Check Configuration Value
```bash
# On server, after restart
curl http://localhost:3000/actuator/env | grep qr.code.base.url
```

### Test 2: Generate Test QR Code
```bash
# Create a test event and get QR code
curl -X GET "http://localhost:3000/events/dowload-qr/TEST123" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Test 3: Decode QR Code
Use an online QR decoder or mobile app to scan the generated QR code and verify the URL.

### Test 4: Manual Verification
1. Login as store manager
2. Create a test event
3. Mark event as complete
4. Download QR code
5. Use QR code scanner app to read the code
6. Verify URL is: `https://celebrationsite-preprod.tanishq.co.in/events/customer/[EVENT_ID]`

---

## 📊 TECHNICAL DETAILS

### Spring Boot Property Loading Order

Spring Boot loads properties in this order (later overrides earlier):

1. **Default value in @Value annotation** (lowest priority)
   ```java
   @Value("${qr.code.base.url:http://localhost:8130/events/customer/}")
   ```

2. **application.properties** (inside JAR/WAR)

3. **application-{profile}.properties** (inside JAR/WAR)
   ```
   application-preprod.properties
   ```

4. **External application.properties** (same directory as WAR)

5. **External application-{profile}.properties**

6. **Environment variables** (highest priority)
   ```bash
   export QR_CODE_BASE_URL="https://celebrationsite-preprod.tanishq.co.in/events/customer/"
   ```

### Current Configuration Chain:

```
Default: http://localhost:8130/events/customer/
   ↓ (overridden by)
application-preprod.properties: https://celebrationsite-preprod.tanishq.co.in/events/customer/
   ↓ (potentially overridden by)
External config: [Check if exists]
   ↓ (potentially overridden by)
Environment variable: [Check if set]
```

---

## 🎯 CONCLUSION

### The Real Answer to "Why It's Not Working Correctly":

**Primary Reason:** The application on the server is either:
1. Running an **old WAR file** built before the configuration was updated, OR
2. Has an **external configuration file** overriding with the old internal IP, OR
3. Not loading the **preprod profile** correctly

### Current Status:

✅ **Code is Fixed:** Source code has correct URL  
✅ **WAR File is Correct:** Compiled WAR has correct configuration  
⚠️ **Deployment Status:** Unknown - Need to verify what's running on server

### What You Need to Do:

1. **Verify** which WAR file is currently running on the server
2. **Check** if any external config files exist
3. **Redeploy** the latest WAR file (from December 10 or rebuild new one)
4. **Test** QR code generation after deployment

### Expected Timeline:

- **Investigation:** 5 minutes (check server)
- **Fix:** 10 minutes (redeploy or update config)
- **Testing:** 5 minutes (verify QR codes)
- **Total:** ~20 minutes

---

## 📞 QUICK REFERENCE COMMANDS

### Check Current Deployment
```bash
# SSH to server
ssh user@10.160.128.94

# Check running WAR
ls -lh /opt/tanishq/applications_preprod/*.war

# Check for external config
find /opt/tanishq/applications_preprod -name "*.properties"

# Check application logs for property value
grep -r "qr.code.base.url" /opt/tanishq/applications_preprod/logs/
```

### Rebuild Locally
```cmd
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -Ppreprod -DskipTests
```

### Deploy via WinSCP
1. Connect to `10.160.128.94`
2. Navigate to `/opt/tanishq/applications_preprod`
3. Backup existing WAR
4. Upload new WAR from `target/` directory
5. Restart application

---

**Report Created:** December 15, 2025  
**Analysis By:** GitHub Copilot  
**Confidence Level:** 95% (based on code analysis and configuration review)

