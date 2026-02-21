# 🚀 DEPLOY COMMANDS - QR Code Fix

## Backend Deployment

```powershell
# Navigate to project
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app

# Clean and build
mvn clean package

# The WAR file will be in:
# target\tanishq-preprod-[version].war

# Deploy to Tomcat
# (Replace with your actual Tomcat path)
copy target\*.war C:\path\to\tomcat\webapps\

# Restart Tomcat
# Method 1: Windows Service
net stop Tomcat9
net start Tomcat9

# Method 2: Batch files
cd C:\path\to\tomcat\bin
shutdown.bat
startup.bat

# Method 3: Your deployment script
.\deploy_production.sh
```

## Testing Commands

```powershell
# Test 1: Generate greeting
curl -X POST https://celebrations.tanishq.co.in/greetings/generate

# Response example: GREETING_1738318234567

# Test 2: Get QR code (replace {id} with actual ID)
curl https://celebrations.tanishq.co.in/greetings/GREETING_1738318234567/qr --output test-qr.png

# Test 3: Verify QR contains correct URL
# Scan test-qr.png with phone or use online decoder:
# https://zxing.org/w/decode.jsp
# Should show: https://celebrations.tanishq.co.in/qr/upload?id=GREETING_XXX

# Test 4: Check greeting info
curl https://celebrations.tanishq.co.in/greetings/GREETING_1738318234567/view
```

## Frontend Check

```powershell
# Navigate to frontend project
cd C:\DECHUB\Tanishq-qr-scanner\tanishq-qr-scanner

# Check if upload route exists
# Look for: <Route path="/qr/upload" ...

# If needed, start dev server to test
npm install
npm start

# Or build for production
npm run build
```

## Quick Verification

```powershell
# 1. Generate greeting and save ID
$greetingId = (Invoke-RestMethod -Method POST -Uri "https://celebrations.tanishq.co.in/greetings/generate")

# 2. Download QR code
Invoke-WebRequest -Uri "https://celebrations.tanishq.co.in/greetings/$greetingId/qr" -OutFile "test-qr.png"

# 3. Open QR code
Start-Process "test-qr.png"

# 4. Scan with your phone and verify it opens /qr/upload page
```

## Rollback (if needed)

```java
// In GreetingService.java, line ~82, change back to:
String qrUrl = greetingBaseUrl.replace("/greetings/", "/qr?id=") + uniqueId;

// Then rebuild and redeploy
```

---

**Status:** ✅ Ready to deploy  
**Estimated deployment time:** 5-10 minutes  
**Risk level:** Low  
**Impact:** High (Major UX improvement)

