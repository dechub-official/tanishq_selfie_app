# Fix for Mobile Greeting Upload Issue

## Problem
The greeting submission works fine on laptop but fails on mobile with "Failed to submit" error.

## Root Cause Analysis
The issue was caused by:
1. **Insufficient CORS headers** - Mobile browsers are stricter about CORS than desktop browsers
2. **Missing OPTIONS request handling** - Mobile browsers often send preflight OPTIONS requests
3. **Inadequate error logging** - No detailed logging to diagnose mobile-specific issues
4. **CORS header conflicts** - SecurityConfig had conflicting CORS settings

## Changes Made

### 1. Enhanced Greeting Upload Controller
**File**: `src/main/java/com/dechub/tanishq/controller/GreetingController.java`

Added comprehensive logging and validation:
- Log all incoming request parameters (uniqueId, name, message, file details)
- Validate video file is not null or empty
- Validate file size (max 100MB)
- Log warnings for non-video content types (but don't fail)
- Better error messages returned to frontend

### 2. Fixed CORS Configuration
**File**: `src/main/java/com/dechub/tanishq/config/WebMvcConfig.java`

Added:
- `allowCredentials(false)` - Required when using `allowedOrigins("*")`
- `exposedHeaders` - Expose necessary headers for mobile browsers

### 3. Fixed Security Configuration
**File**: `src/main/java/com/dechub/tanishq/config/SecurityConfig.java`

Fixed:
- Allow OPTIONS method for CORS preflight requests
- Explicitly permit `/greetings/**` endpoints
- Enable CORS support with `.cors()`
- Fixed `Access-Control-Allow-Methods` to include all needed methods
- Fixed `Access-Control-Allow-Headers` to include Content-Type
- Removed conflicting `Access-Control-Allow-Credentials: true` (conflicts with `allowedOrigins: *`)

## Testing Instructions

### 1. Build and Deploy
```bash
# Clean and build
mvn clean package -P preprod

# Deploy the WAR file
```

### 2. Test on Desktop (Should still work)
1. Open https://celebrationsite-preprod.tanishq.co.in/qr
2. Scan QR or manually create video
3. Record video message
4. Fill form with name and message
5. Submit - should work as before

### 3. Test on Mobile (Should now work)
1. Open https://celebrationsite-preprod.tanishq.co.in/qr on mobile
2. Allow camera permissions
3. Record video message (keep it short for testing - 5-10 seconds)
4. Fill form:
   - Name: [Your Name]
   - Message: [Your Message] (at least 10 characters)
5. Submit
6. Check server logs for detailed logging

### 4. Check Server Logs
When testing mobile upload, check the logs for:
```
Received upload request - uniqueId: xxx, name: xxx, message: xxx, videoFile: xxx, size: xxx, contentType: xxx
```

If upload fails, you'll see detailed error:
```
Failed to upload video for greeting: xxx - [error message]
```

## Common Mobile Issues & Solutions

### Issue: "Video file is required"
- **Cause**: Video blob not created properly on mobile
- **Check**: Browser console for JavaScript errors
- **Fix**: Ensure mobile browser supports MediaRecorder API

### Issue: "Video file size exceeds 100MB limit"
- **Cause**: Mobile recording creates very large files
- **Fix**: Record shorter videos or adjust max-file-size in application.properties

### Issue: Network timeout
- **Cause**: Slow mobile network uploading large video
- **Solution**: 
  1. Record shorter videos for testing
  2. Test on WiFi instead of cellular
  3. Consider adding connection timeout configuration

### Issue: CORS errors in browser console
- **Check**: Browser developer tools Network tab
- **Look for**: Preflight OPTIONS request status
- **Should see**: 200 OK for OPTIONS request

## Additional Diagnostics

### Enable Detailed Logging (if needed)
Add to `application-preprod.properties`:
```properties
logging.level.org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping=DEBUG
logging.level.org.springframework.web.cors=DEBUG
logging.level.org.springframework.security.web.access=DEBUG
```

### Test CORS Manually
```bash
# Test OPTIONS preflight request
curl -X OPTIONS https://celebrationsite-preprod.tanishq.co.in/greetings/test123/upload \
  -H "Origin: https://celebrationsite-preprod.tanishq.co.in" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v

# Should return 200 with CORS headers
```

## Backend Configuration Reference

### File Upload Limits
In `application-preprod.properties`:
```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

### CORS Configuration
In `application-preprod.properties`:
```properties
app.cors.allowedOrigins=*
```

## Mobile-Specific Considerations

### Browser Compatibility
- ✅ Chrome/Safari iOS 14+
- ✅ Chrome Android 80+
- ✅ Safari iOS 12+
- ⚠️ Older browsers may not support MediaRecorder

### Video Format Support
Mobile browsers may encode video differently:
- iOS: Usually H.264 in MP4 container
- Android: Usually VP8/VP9 in WebM container
- Backend accepts all video/* content types

### Network Considerations
- Mobile networks may have lower bandwidth
- Consider showing upload progress bar
- Add retry mechanism for failed uploads

## Rollback Plan
If issues persist:
1. Revert changes to SecurityConfig.java (restore from git)
2. Revert changes to WebMvcConfig.java  
3. Keep enhanced logging in GreetingController.java for debugging

## Next Steps
1. Deploy changes to preprod environment
2. Test thoroughly on both desktop and mobile
3. Monitor server logs during mobile testing
4. If successful, deploy to production
5. Consider adding upload progress indicator for better UX

## Support
If issues persist after these changes:
1. Check server logs for detailed error messages
2. Check browser console for JavaScript errors
3. Verify mobile browser version and compatibility
4. Test on different mobile devices/browsers
5. Check network connectivity and speed

