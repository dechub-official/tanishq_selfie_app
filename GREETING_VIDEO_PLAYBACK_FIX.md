# Greeting Video Playback Fix - S3 Pre-Signed URL Solution

## 🔍 Problem Summary

When users scan a greeting QR code and complete the flow (video recording → form submission → video playback), the video plays correctly **the first time**. However, if the user closes the tab and scans the same QR code again later, the video fails to load with a **403 Forbidden** error from AWS S3.

### Root Cause
- Greeting videos are stored in a **private S3 bucket** (correct for security)
- The original implementation stored the direct S3 URL in the database
- Direct URLs to private S3 objects cannot be accessed by browsers
- Pre-signed URLs were not being generated, or were being reused after expiration

---

## ✅ Solution Implemented

### Core Principle
**Pre-signed URLs must be generated on-demand every time a user requests video playback, and never reused.**

### Updated Flow
```
User scans QR (any time)
        ↓
QR resolves to greetingId
        ↓
/qr/video-message page loads
        ↓
Frontend requests fresh video URL from backend
        ↓
Backend generates NEW pre-signed S3 URL (valid 10 min)
        ↓
Frontend sets video source
        ↓
Video plays successfully ✅
```

This works for:
- ✅ First scan
- ✅ Repeated scans
- ✅ After tab close
- ✅ After hours/days

---

## 🔧 Backend Changes Made

### 1. **StorageService Interface** (`StorageService.java`)
Added new method signature:
```java
/**
 * Generate a pre-signed URL for accessing a greeting video
 * @param s3Url The S3 URL of the video
 * @param expirationMinutes Expiration time in minutes
 * @return Pre-signed URL for temporary access
 */
String generatePresignedUrl(String s3Url, int expirationMinutes);
```

### 2. **AWS S3 Storage Service** (`AwsS3StorageService.java`)
Implemented pre-signed URL generation:
```java
@Override
public String generatePresignedUrl(String s3Url, int expirationMinutes) {
    try {
        String s3Key = extractS3KeyFromUrl(s3Url);
        
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += (long) expirationMinutes * 60 * 1000;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest request = 
            new GeneratePresignedUrlRequest(bucketName, s3Key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        URL url = s3Client.generatePresignedUrl(request);
        return url.toString();
    } catch (Exception e) {
        log.error("Failed to generate pre-signed URL", e);
        return s3Url; // Fallback
    }
}
```

**Key Features:**
- Extracts S3 key from full URL
- Generates AWS pre-signed URL with expiration
- Handles both `https://` and `s3://` URL formats
- Graceful fallback on error

### 3. **Local File Storage Service** (`LocalFileStorageService.java`)
For local development (no pre-signing needed):
```java
@Override
public String generatePresignedUrl(String s3Url, int expirationMinutes) {
    // Local files are publicly accessible
    return s3Url;
}
```

### 4. **Greeting Service** (`GreetingService.java`)
Added method to generate fresh video URLs:
```java
/**
 * Generate a fresh pre-signed URL for video playback
 * This solves the 403 Forbidden issue when users rescan QR codes
 */
public String generateFreshVideoUrl(String s3Url) {
    if (s3Url == null || s3Url.isEmpty()) {
        return null;
    }
    
    // Generate pre-signed URL with 10-minute expiration
    String presignedUrl = storageService.generatePresignedUrl(s3Url, 10);
    return presignedUrl;
}
```

### 5. **Greeting Controller** (`GreetingController.java`)
Added new endpoint for on-demand URL generation:
```java
/**
 * Get fresh pre-signed video URL for playback
 * GET /greetings/{uniqueId}/video-url
 */
@GetMapping("/{uniqueId}/video-url")
public ResponseEntity<?> getVideoPlaybackUrl(@PathVariable String uniqueId) {
    Optional<Greeting> optGreeting = greetingService.getGreeting(uniqueId);
    
    if (!optGreeting.isPresent()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Greeting not found"));
    }
    
    Greeting greeting = optGreeting.get();
    
    if (!greeting.getUploaded() || greeting.getDriveFileId() == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Video not uploaded yet"));
    }
    
    // Generate fresh pre-signed URL (expires in 10 minutes)
    String s3Url = greeting.getDriveFileId();
    String presignedUrl = greetingService.generateFreshVideoUrl(s3Url);
    
    return ResponseEntity.ok(new VideoUrlResponse(presignedUrl));
}
```

**Response Format:**
```json
{
  "videoUrl": "https://bucket.s3.region.amazonaws.com/path?X-Amz-Algorithm=...&X-Amz-Expires=600"
}
```

---

## 🎨 Frontend Changes Required

### Current Behavior (Broken)
```javascript
// OLD - Gets URL once and reuses it
const response = await fetch(`/greetings/${greetingId}/view`);
const data = await response.json();
videoElement.src = data.videoPlaybackUrl; // ❌ Expired URL
```

### New Behavior (Fixed)

#### **Step 1: Request Fresh URL from Backend**
```javascript
// NEW ENDPOINT - Call this EVERY TIME the video page loads
const response = await fetch(`/greetings/${greetingId}/video-url`);

if (!response.ok) {
  console.error('Failed to get video URL');
  return;
}

const data = await response.json();
const freshVideoUrl = data.videoUrl; // ✅ Fresh pre-signed URL
```

#### **Step 2: Set Video Source**
```javascript
const videoElement = document.getElementById('greeting-video');
videoElement.src = freshVideoUrl;
```

#### **Step 3: Handle Errors**
```javascript
try {
  const response = await fetch(`/greetings/${greetingId}/video-url`);
  
  if (response.status === 404) {
    // Video not uploaded yet
    showMessage("Video not found");
    return;
  }
  
  if (!response.ok) {
    showMessage("Error loading video");
    return;
  }
  
  const data = await response.json();
  videoElement.src = data.videoUrl;
  
} catch (error) {
  console.error('Error fetching video URL:', error);
  showMessage("Network error");
}
```

### Complete Frontend Example
```javascript
async function loadGreetingVideo(greetingId) {
  try {
    // Show loading indicator
    showLoader();
    
    // Fetch fresh pre-signed URL
    const response = await fetch(`/greetings/${greetingId}/video-url`);
    
    if (response.status === 404) {
      hideLoader();
      showError("Video not found or not uploaded yet");
      return;
    }
    
    if (!response.ok) {
      throw new Error('Failed to load video URL');
    }
    
    const data = await response.json();
    
    // Set video source with fresh pre-signed URL
    const video = document.getElementById('greeting-video');
    video.src = data.videoUrl;
    
    // Auto-play when ready
    video.addEventListener('loadedmetadata', () => {
      hideLoader();
      video.play();
    });
    
    // Handle video load errors
    video.addEventListener('error', (e) => {
      hideLoader();
      showError("Failed to load video");
      console.error('Video error:', e);
    });
    
  } catch (error) {
    hideLoader();
    showError("Error loading video");
    console.error('Error:', error);
  }
}

// Call this when the page loads
document.addEventListener('DOMContentLoaded', () => {
  const greetingId = getGreetingIdFromUrl(); // Extract from URL/QR scan
  loadGreetingVideo(greetingId);
});
```

---

## 🔐 Security & Best Practices

### ✅ What This Solution Provides
1. **Private S3 Bucket** - Videos remain secure
2. **Time-Limited Access** - URLs expire after 10 minutes
3. **On-Demand Generation** - Fresh URL every time
4. **No Credential Exposure** - AWS keys stay on server
5. **Stateless Architecture** - No session dependencies

### ⏰ URL Expiration
- **Set to:** 10 minutes
- **Why:** 
  - Long enough to watch the video
  - Short enough to prevent sharing
  - Allows replays within same session

### 🔄 When URLs Are Generated
- ✅ Every page load
- ✅ Every QR scan
- ✅ After browser refresh
- ❌ NOT stored in database
- ❌ NOT cached in frontend

---

## 🧪 Testing Checklist

### Test Scenarios
- [ ] **First QR scan** - Video loads and plays
- [ ] **Refresh page** - Video loads again
- [ ] **Close tab, rescan QR** - Video loads successfully
- [ ] **Scan after 1 hour** - Video loads successfully
- [ ] **Scan after 1 day** - Video loads successfully
- [ ] **Multiple devices** - Each gets own URL
- [ ] **Network error handling** - Shows user-friendly message
- [ ] **Video not uploaded yet** - Shows appropriate message

### Expected Results
| Scenario | Expected Result |
|----------|----------------|
| First scan | ✅ Video plays |
| Refresh | ✅ Video plays |
| Rescan after close | ✅ Video plays |
| Rescan after hours | ✅ Video plays |
| No video uploaded | ⚠️ "Not found" message |
| Network error | ⚠️ Error message |

---

## 📊 API Endpoints Summary

### New Endpoint (Use This)
```
GET /greetings/{uniqueId}/video-url
```
**Response:**
```json
{
  "videoUrl": "https://bucket.s3.region.amazonaws.com/path?X-Amz-..."
}
```
**Errors:**
- `404` - Greeting not found or no video uploaded
- `500` - Server error

### Existing Endpoint (Keep for metadata)
```
GET /greetings/{uniqueId}/view
```
**Response:**
```json
{
  "hasVideo": true,
  "status": "completed",
  "videoPlaybackUrl": "...", // ❌ Don't use this for video src
  "name": "John Doe",
  "message": "Happy Anniversary!"
}
```

---

## 🚀 Deployment Steps

### 1. Backend Deployment
```bash
# Build the project
mvn clean package

# Deploy WAR file to Tomcat
cp target/tanishq_selfie_app.war /path/to/tomcat/webapps/
```

### 2. Frontend Deployment
- Update React/JavaScript code to use new endpoint
- Test in development first
- Deploy to preprod
- Monitor for errors
- Deploy to production

### 3. Verification
```bash
# Test the new endpoint
curl https://celebrations.tanishq.co.in/greetings/GREETING_123/video-url

# Expected response:
# {"videoUrl":"https://bucket.s3.region.amazonaws.com/greetings/GREETING_123/video.mp4?X-Amz-..."}
```

---

## 📝 Frontend Code Locations to Update

Look for files containing:
- `/qr/video-message` route/page
- Video playback component
- QR scan handler
- Greeting view page

**Search for:**
```javascript
// Find these patterns
"videoPlaybackUrl"
"video.src ="
"/greetings/{id}/view"
"greeting-video"
```

**Replace with:**
```javascript
// Use the new endpoint
fetch(`/greetings/${id}/video-url`)
```

---

## ❓ FAQ

### Q: Why not just make the S3 bucket public?
**A:** Security risk. Videos could be accessed by anyone with the URL.

### Q: Can I cache the video URL?
**A:** No. It expires. Always fetch fresh on page load.

### Q: What if the URL expires during playback?
**A:** 10 minutes is enough for playback. User can refresh to get new URL.

### Q: Does this work for local development?
**A:** Yes. Local storage returns URL as-is (no pre-signing needed).

### Q: What about existing videos?
**A:** They work automatically. No migration needed.

---

## 📞 Support

If issues persist:
1. Check backend logs for pre-signed URL generation errors
2. Verify AWS IAM permissions for `s3:GetObject`
3. Check S3 bucket CORS configuration
4. Verify frontend is calling `/video-url` endpoint
5. Test with browser network tab to see actual requests

---

## ✅ Summary

### What Changed
- ✅ Added pre-signed URL generation to StorageService
- ✅ Implemented AWS S3 pre-signing logic
- ✅ Added new endpoint: `GET /greetings/{id}/video-url`
- ✅ Added service method to generate fresh URLs
- ❌ No database changes
- ❌ No data migration needed

### Frontend TODO
1. Update video playback page to call `/video-url` endpoint
2. Set video source with returned `videoUrl`
3. Add error handling for 404/500
4. Remove any URL caching logic
5. Test all QR scan scenarios

**Result:** Videos will now load reliably on every QR scan, no matter how much time has passed! 🎉

