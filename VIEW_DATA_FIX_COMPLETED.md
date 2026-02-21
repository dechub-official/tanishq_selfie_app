# Fix Summary for View Data Issue

## Problem Identified
The "View Data" feature was not displaying **Customer Phone** and **Like** columns even though the backend was returning the data.

## Root Cause
There was a **field name mismatch** between backend and frontend:
- **Backend** was sending the field as `"contact"` 
- **Frontend** was expecting the field as `"phone"`

## Fix Applied
Changed the backend service method `getInvitedMember()` in:
**File:** `src/main/java/com/dechub/tanishq/service/TanishqPageService.java`

**Changed from:**
```java
map.put("contact", attendee.getPhone());
```

**Changed to:**
```java
map.put("phone", attendee.getPhone());
```

## API Response Format (AFTER FIX)
```json
{
    "status": true,
    "result": [
        {
            "name": "Sudhir",
            "phone": "1234567890",
            "like": "",
            "firstTimeAtTanishq": true
        }
    ]
}
```

## Frontend Display
The modal will now correctly show all 4 columns:
1. **Sno** - Serial number
2. **Customer Name** - from `name` field
3. **Customer Phone** - from `phone` field ✅ (NOW FIXED)
4. **Like** - from `like` field ✅ (NOW FIXED)
5. **First Time At Tanishq** - from `firstTimeAtTanishq` field

## Build and Deploy Instructions

### To rebuild the application:

**Option 1: Using Maven (if installed)**
```bash
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
mvn clean package -DskipTests -P preprod
```

**Option 2: Using Maven Wrapper (if available)**
```bash
cd C:\JAVA\celebration-preprod-latest\celeb\tanishq_selfie_app
.\mvnw.cmd clean package -DskipTests -P preprod
```

**Option 3: Using IntelliJ IDEA**
1. Open the project in IntelliJ
2. Right-click on `pom.xml`
3. Select "Maven" → "Reload Project"
4. Then: "Maven" → "tanishq [clean]"
5. Then: "Maven" → "tanishq [package]" (with profile: preprod)

### After successful build:
The WAR file will be generated at:
```
target\tanishq-preprod-30-01-2026-2-0.0.1-SNAPSHOT.war
```

### To deploy:
1. Stop the running Spring Boot application
2. Copy the new WAR file to your deployment directory
3. Restart the application
4. Clear browser cache (Ctrl+F5) to reload the frontend JavaScript
5. Test the "View Data" feature

## Verification Steps
After deployment:
1. Login to the dashboard
2. Click on "View Data" button for any event
3. Verify that **Customer Phone** column shows the phone numbers
4. Verify that **Like** column is displayed
5. Confirm all data matches what's in the database

## Files Modified
- `src/main/java/com/dechub/tanishq/service/TanishqPageService.java` (Line 685)

## No Database Changes Required
This is purely a code-level fix. No database migration or schema changes are needed.

