# Testing Guide - Greeting Video Pre-Signed URL Fix

## 🧪 Pre-Deployment Testing

### 1. Backend API Testing

#### Test New Endpoint Exists
```bash
# Test with curl (replace with actual greeting ID)
curl -X GET "http://localhost:8080/tanishq_selfie_app/greetings/GREETING_1234567890/video-url"

# Expected Response:
# {"videoUrl":"https://bucket.s3.region.amazonaws.com/greetings/GREETING_XXX/video.mp4?X-Amz-..."}
```

#### Test Error Cases
```bash
# Test with non-existent greeting ID
curl -X GET "http://localhost:8080/tanishq_selfie_app/greetings/INVALID_ID/video-url"

# Expected Response (404):
# {"error":"Greeting not found"}

# Test with greeting that has no video uploaded
curl -X GET "http://localhost:8080/tanishq_selfie_app/greetings/GREETING_NO_VIDEO/video-url"

# Expected Response (404):
# {"error":"Video not uploaded yet"}
```

#### Verify Pre-Signed URL Structure
```javascript
// The returned URL should contain these query parameters:
- X-Amz-Algorithm
- X-Amz-Credential
- X-Amz-Date
- X-Amz-Expires (should be 600 seconds = 10 minutes)
- X-Amz-Signature
- X-Amz-Security-Token (if using IAM role)
```

---

### 2. Frontend Integration Testing

#### Test in Browser Console
```javascript
// Open browser DevTools console on video page
async function testVideoUrl(greetingId) {
  try {
    console.log('Testing greeting:', greetingId);
    
    const response = await fetch(`/greetings/${greetingId}/video-url`);
    console.log('Response status:', response.status);
    
    if (!response.ok) {
      const error = await response.json();
      console.error('Error:', error);
      return;
    }
    
    const data = await response.json();
    console.log('Success! Video URL:', data.videoUrl);
    
    // Check URL structure
    if (data.videoUrl.includes('X-Amz-Signature')) {
      console.log('✅ Pre-signed URL confirmed');
    } else {
      console.warn('⚠️ URL may not be pre-signed');
    }
    
    return data.videoUrl;
    
  } catch (error) {
    console.error('Test failed:', error);
  }
}

// Run test (replace with actual greeting ID)
testVideoUrl('GREETING_1234567890');
```

#### Test Video Playback
```javascript
// After getting URL, test actual video loading
async function testVideoPlayback(greetingId) {
  const url = await testVideoUrl(greetingId);
  
  if (!url) {
    console.error('Failed to get URL');
    return;
  }
  
  const video = document.createElement('video');
  video.src = url;
  
  video.onloadeddata = () => {
    console.log('✅ Video loaded successfully');
    console.log('Duration:', video.duration, 'seconds');
  };
  
  video.onerror = (e) => {
    console.error('❌ Video failed to load:', e);
  };
  
  document.body.appendChild(video);
}

testVideoPlayback('GREETING_1234567890');
```

---

### 3. QR Code Flow Testing

#### Scenario 1: First Time Scan
```
Steps:
1. Generate a test greeting
2. Upload a video
3. Scan QR code
4. Verify video loads and plays

Expected Result: ✅ Video plays

Check:
- [ ] QR scan redirects to correct page
- [ ] API call to /video-url succeeds
- [ ] Video element receives URL
- [ ] Video loads and plays
- [ ] No 403 errors in console
```

#### Scenario 2: Rescan After Close
```
Steps:
1. Complete Scenario 1
2. Close the browser tab
3. Scan the same QR code again
4. Verify video loads and plays

Expected Result: ✅ Video plays (with NEW pre-signed URL)

Check:
- [ ] New API call to /video-url
- [ ] New pre-signed URL generated
- [ ] Video loads and plays
- [ ] No cached URL used
```

#### Scenario 3: Rescan After Delay
```
Steps:
1. Complete Scenario 1
2. Wait 15+ minutes (URL should expire)
3. Refresh the page OR scan QR again
4. Verify video loads and plays

Expected Result: ✅ Video plays (expired URL replaced with fresh one)

Check:
- [ ] Fresh URL requested from backend
- [ ] New signature generated
- [ ] Video loads successfully
```

#### Scenario 4: Multiple Concurrent Users
```
Steps:
1. Scan same QR code on 2+ devices simultaneously
2. Verify video plays on all devices

Expected Result: ✅ Each device gets its own pre-signed URL

Check:
- [ ] Multiple API calls to /video-url
- [ ] Different URLs returned (different signatures)
- [ ] All videos play independently
```

---

## 🔍 Debugging Checklist

### Network Tab Inspection
Open Chrome DevTools → Network Tab:

```
1. Clear network log
2. Load video page
3. Look for request to /video-url

✅ Expected:
   Request URL: /greetings/GREETING_XXX/video-url
   Status: 200 OK
   Response: {"videoUrl":"https://...?X-Amz-..."}

❌ If you see:
   - 404: Greeting not found or video not uploaded
   - 500: Server error (check backend logs)
   - No request: Frontend not calling new endpoint
```

### Console Inspection
```javascript
// No errors expected
✅ No 403 Forbidden errors
✅ No CORS errors
✅ Video element has src set
✅ Video loadeddata event fires

// Check video element
const video = document.querySelector('video');
console.log('Video src:', video.src);
console.log('Has X-Amz-Signature:', video.src.includes('X-Amz-Signature'));
```

### Backend Logs Inspection
```
# Check Tomcat/Spring logs for:

✅ Expected logs:
   "Generated pre-signed URL for greetings/XXX/video.mp4 (expires in 10 minutes)"
   "Generated fresh video URL for greeting: GREETING_XXX"

❌ Error logs to watch for:
   "Failed to generate pre-signed URL"
   "Failed to extract S3 key from URL"
   "S3 availability check failed"
```

---

## 📊 Test Matrix

| Test Case | Expected Result | Pass/Fail |
|-----------|----------------|-----------|
| **Backend Tests** |
| GET /video-url with valid ID | 200 + signed URL | [ ] |
| GET /video-url with invalid ID | 404 + error | [ ] |
| GET /video-url with no video | 404 + error | [ ] |
| Signed URL contains X-Amz params | Yes | [ ] |
| Signed URL expires in 10 min | Yes | [ ] |
| **Frontend Tests** |
| Video page loads | Success | [ ] |
| API call to /video-url | Success | [ ] |
| Video element src set | Yes | [ ] |
| Video plays | Yes | [ ] |
| No 403 errors | No errors | [ ] |
| **QR Flow Tests** |
| First QR scan | Video plays | [ ] |
| Rescan after close | Video plays | [ ] |
| Rescan after 1 hour | Video plays | [ ] |
| Multiple devices | All play | [ ] |
| Page refresh | Video plays | [ ] |
| **Mobile Tests** |
| iOS Safari | Works | [ ] |
| Android Chrome | Works | [ ] |
| Mobile QR scanner | Works | [ ] |

---

## 🚨 Common Issues & Solutions

### Issue 1: Still Getting 403 Errors
**Symptoms:** Video fails to load, 403 in console

**Causes:**
- Frontend still using old `/view` endpoint
- Frontend using `videoPlaybackUrl` instead of `videoUrl`
- URL being cached/reused

**Solution:**
```javascript
// Check your code for these patterns:
❌ fetch('/greetings/${id}/view')
❌ video.src = data.videoPlaybackUrl
❌ localStorage.getItem('videoUrl')

// Should be:
✅ fetch('/greetings/${id}/video-url')
✅ video.src = data.videoUrl
✅ Fresh fetch on every load
```

---

### Issue 2: Video Loads First Time, Fails on Reload
**Symptoms:** Works once, then breaks

**Cause:** URL being cached

**Solution:**
```javascript
// Remove any caching:
❌ const [url] = useState(previousUrl);
❌ if (cachedUrl) return cachedUrl;

// Always fetch fresh:
✅ useEffect(() => fetchVideoUrl(), [greetingId]);
```

---

### Issue 3: Backend Returns Direct URL (Not Pre-Signed)
**Symptoms:** URL doesn't contain `X-Amz-Signature`

**Causes:**
- StorageService not generating pre-signed URL
- Using LocalFileStorageService instead of AwsS3StorageService
- AWS credentials missing

**Solution:**
```bash
# Check active Spring profile
# Should be 'preprod' or 'prod', not 'local'

# Check application.properties
spring.profiles.active=preprod

# Verify AWS credentials are available
# (IAM role on EC2 instance)
```

---

### Issue 4: Pre-Signed URL Generated but Video Won't Play
**Symptoms:** URL looks correct but video fails

**Causes:**
- S3 bucket CORS not configured
- S3 bucket policy blocking access
- Wrong S3 region

**Solution:**
```json
// Check S3 bucket CORS configuration
[
  {
    "AllowedOrigins": ["https://celebrations.tanishq.co.in"],
    "AllowedMethods": ["GET", "HEAD"],
    "AllowedHeaders": ["*"],
    "ExposeHeaders": [],
    "MaxAgeSeconds": 3000
  }
]

// Check S3 bucket policy allows GetObject with IAM role
```

---

## 📱 Mobile-Specific Testing

### iOS Safari
```
Test:
1. Open in Safari on iPhone
2. Scan QR code
3. Video should play inline (not fullscreen)

Check:
- [ ] Video has playsinline attribute
- [ ] Video loads without errors
- [ ] Autoplay works (may require user interaction)
- [ ] Video doesn't force fullscreen
```

### Android Chrome
```
Test:
1. Open in Chrome on Android
2. Scan QR code
3. Video should play

Check:
- [ ] Video loads
- [ ] Controls work
- [ ] No CORS errors
- [ ] Pre-signed URL works on mobile network
```

---

## 🎯 Performance Testing

### URL Generation Speed
```javascript
// Test how fast URLs are generated
console.time('videoUrlGeneration');
await fetch(`/greetings/${id}/video-url`);
console.timeEnd('videoUrlGeneration');

// Expected: < 500ms
```

### Video Load Time
```javascript
// Test video loading performance
const video = document.querySelector('video');
const startTime = Date.now();

video.onloadeddata = () => {
  const loadTime = Date.now() - startTime;
  console.log('Video loaded in', loadTime, 'ms');
  // Expected: < 3000ms (depends on video size/network)
};
```

---

## ✅ Sign-Off Checklist

Before deploying to production:

### Backend
- [ ] Code compiles without errors
- [ ] Unit tests pass (if any)
- [ ] `/video-url` endpoint responds correctly
- [ ] Pre-signed URLs generated with 10-min expiry
- [ ] Error handling works (404, 500)
- [ ] Logs show correct messages

### Frontend
- [ ] Updated to use `/video-url` endpoint
- [ ] Removed URL caching logic
- [ ] Error handling implemented
- [ ] Loading states work
- [ ] No console errors

### Testing
- [ ] First scan works
- [ ] Rescan works
- [ ] Refresh works
- [ ] Multiple devices work
- [ ] Mobile iOS works
- [ ] Mobile Android works
- [ ] No 403 errors
- [ ] Performance acceptable

### Production
- [ ] Deployed to preprod
- [ ] Tested in preprod
- [ ] No issues found
- [ ] Ready for production deployment

---

## 🔬 Advanced Testing (Optional)

### Test URL Expiration
```javascript
// Get a signed URL
const response = await fetch('/greetings/XXX/video-url');
const data = await response.json();
const url = data.videoUrl;

// Extract expiry from URL
const urlObj = new URL(url);
const expires = urlObj.searchParams.get('X-Amz-Expires');
console.log('URL expires in', expires, 'seconds');
// Should be 600 (10 minutes)

// Try using URL after 11 minutes
// Should fail (URL expired)
```

### Load Testing
```javascript
// Simulate multiple concurrent requests
async function loadTest(greetingId, concurrent = 10) {
  const promises = [];
  
  for (let i = 0; i < concurrent; i++) {
    promises.push(
      fetch(`/greetings/${greetingId}/video-url`)
    );
  }
  
  const results = await Promise.all(promises);
  const successful = results.filter(r => r.ok).length;
  
  console.log(`${successful}/${concurrent} requests succeeded`);
}

loadTest('GREETING_XXX', 50);
```

---

## 📞 If Tests Fail

### 1. Check Backend Logs
```bash
tail -f /path/to/tomcat/logs/catalina.out | grep -i greeting
```

### 2. Check Network Tab
- Is the request being made?
- What's the response status?
- What's the response body?

### 3. Check Console
- Any JavaScript errors?
- Is video.src being set?
- Any 403/CORS errors?

### 4. Check Code
- Using correct endpoint?
- Using correct response field?
- No URL caching?

### 5. Verify Environment
- Correct Spring profile?
- AWS credentials available?
- S3 bucket accessible?

---

## 🎉 Success Criteria

Tests are successful when:
- ✅ Videos load on first scan
- ✅ Videos load on repeat scans
- ✅ Videos load after hours/days
- ✅ No 403 Forbidden errors
- ✅ Works on desktop browsers
- ✅ Works on mobile devices
- ✅ Multiple users can watch simultaneously
- ✅ Page refresh works
- ✅ QR code flow works end-to-end

**When all checkboxes are ticked, you're ready to deploy! 🚀**

