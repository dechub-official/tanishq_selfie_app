# 🎯 QUICK START - Fix Greeting Video 403 Error

## Problem
Videos fail with **403 Forbidden** when users rescan QR codes.

## Root Cause
Direct S3 URLs don't work for private buckets. Need pre-signed URLs generated on-demand.

---

## ✅ Backend Changes (DONE)

### Files Modified
1. ✅ `StorageService.java` - Added `generatePresignedUrl()` interface
2. ✅ `AwsS3StorageService.java` - Implemented pre-signed URL generation
3. ✅ `LocalFileStorageService.java` - Added stub for local dev
4. ✅ `GreetingService.java` - Added `generateFreshVideoUrl()` method
5. ✅ `GreetingController.java` - Added `GET /greetings/{id}/video-url` endpoint

### New API Endpoint
```
GET /greetings/{greetingId}/video-url
```

**Response:**
```json
{
  "videoUrl": "https://bucket.s3.amazonaws.com/path?X-Amz-Signature=..."
}
```

---

## 🎨 Frontend Changes (YOU NEED TO DO THIS)

### Find This Code:
```javascript
// OLD - REMOVE THIS
fetch(`/greetings/${greetingId}/view`)
  .then(r => r.json())
  .then(data => {
    video.src = data.videoPlaybackUrl; // ❌ WRONG
  });
```

### Replace With:
```javascript
// NEW - USE THIS
fetch(`/greetings/${greetingId}/video-url`)
  .then(r => r.json())
  .then(data => {
    video.src = data.videoUrl; // ✅ CORRECT
  });
```

### Complete Example:
```javascript
async function loadVideo(greetingId) {
  try {
    const response = await fetch(`/greetings/${greetingId}/video-url`);
    
    if (!response.ok) {
      console.error('Failed to load video');
      return;
    }
    
    const data = await response.json();
    document.getElementById('greeting-video').src = data.videoUrl;
    
  } catch (error) {
    console.error('Error:', error);
  }
}

// Call on page load
document.addEventListener('DOMContentLoaded', () => {
  const greetingId = getGreetingIdFromUrl();
  loadVideo(greetingId);
});
```

---

## 📁 Where to Update Frontend Code

### Search for these patterns in your frontend:
```bash
# In your React/JavaScript files
- "videoPlaybackUrl"
- "/greetings/*/view"
- "greeting-video"
- "video-message"
```

### Common file locations:
- `src/pages/VideoMessage.jsx`
- `src/components/GreetingVideo.jsx`
- `public/qr/index.html`
- `static/js/greeting.js`

---

## 🧪 Testing

### Test 1: First Scan
1. Scan QR code
2. Video should load and play ✅

### Test 2: Rescan
1. Close the tab
2. Scan same QR code again
3. Video should load and play ✅

### Test 3: After Time
1. Wait 1+ hours
2. Scan same QR code
3. Video should load and play ✅

### Check Browser Console:
```
✅ Request to: /greetings/GREETING_XXX/video-url
✅ Response: {"videoUrl":"https://...?X-Amz-Signature=..."}
✅ No 403 errors
```

---

## 📋 Deployment Checklist

### Backend (Already Done)
- [x] Updated StorageService interface
- [x] Implemented pre-signed URL generation
- [x] Added new controller endpoint
- [x] Code compiles without errors

### Frontend (You Need to Do)
- [ ] Update video page to use `/video-url` endpoint
- [ ] Change `videoPlaybackUrl` to `videoUrl`
- [ ] Remove any URL caching
- [ ] Test on desktop
- [ ] Test on mobile
- [ ] Test QR scan flow

### Production Deployment
- [ ] Deploy backend WAR file
- [ ] Deploy updated frontend
- [ ] Test in production
- [ ] Monitor for 403 errors (should be none!)

---

## 🎉 Expected Results

| Scenario | Before Fix | After Fix |
|----------|-----------|-----------|
| First scan | ✅ Works | ✅ Works |
| Rescan after close | ❌ 403 Error | ✅ Works |
| Rescan after hours | ❌ 403 Error | ✅ Works |
| Refresh page | ❌ 403 Error | ✅ Works |

---

## 📚 Documentation

**Full details:** See `GREETING_VIDEO_PLAYBACK_FIX.md`
**Frontend guide:** See `FRONTEND_CHANGES_QUICK_REF.md`

---

## ❓ Questions?

**Q: Do I need to update existing videos?**
A: No! Works automatically.

**Q: Will this break anything?**
A: No! Backward compatible.

**Q: Do I need database changes?**
A: No! No DB changes needed.

**Q: What about local development?**
A: Works! Local storage returns URLs as-is.

---

## 🚀 Let's Fix This!

1. Update your frontend code (see above)
2. Test the changes
3. Deploy to production
4. Enjoy working videos! 🎉

**The backend is ready. Now it's your turn to update the frontend!**

