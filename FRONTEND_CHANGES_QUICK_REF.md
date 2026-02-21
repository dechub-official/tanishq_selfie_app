# Frontend Changes - Quick Reference

## 🎯 What You Need to Change

### ❌ OLD CODE (Broken)
```javascript
// This gets a URL that expires and causes 403 errors later
const response = await fetch(`/greetings/${greetingId}/view`);
const data = await response.json();
videoElement.src = data.videoPlaybackUrl; // DON'T USE THIS
```

### ✅ NEW CODE (Fixed)
```javascript
// Call this EVERY TIME the video page loads
const response = await fetch(`/greetings/${greetingId}/video-url`);
const data = await response.json();
videoElement.src = data.videoUrl; // USE THIS INSTEAD
```

---

## 🔧 Complete Implementation

### 1️⃣ Basic Implementation
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
```

### 2️⃣ With Loading & Error Handling
```javascript
async function loadVideo(greetingId) {
  const video = document.getElementById('greeting-video');
  const loader = document.getElementById('loader');
  const errorMsg = document.getElementById('error-message');
  
  try {
    // Show loading
    loader.style.display = 'block';
    video.style.display = 'none';
    errorMsg.style.display = 'none';
    
    // Fetch fresh URL
    const response = await fetch(`/greetings/${greetingId}/video-url`);
    
    if (response.status === 404) {
      throw new Error('Video not found');
    }
    
    if (!response.ok) {
      throw new Error('Failed to load video');
    }
    
    const data = await response.json();
    
    // Set video source
    video.src = data.videoUrl;
    
    // Wait for video to load
    video.onloadedmetadata = () => {
      loader.style.display = 'none';
      video.style.display = 'block';
      video.play();
    };
    
    // Handle video errors
    video.onerror = () => {
      loader.style.display = 'none';
      errorMsg.textContent = 'Failed to load video';
      errorMsg.style.display = 'block';
    };
    
  } catch (error) {
    loader.style.display = 'none';
    errorMsg.textContent = error.message;
    errorMsg.style.display = 'block';
    console.error('Error loading video:', error);
  }
}
```

### 3️⃣ React Component Example
```javascript
import React, { useState, useEffect, useRef } from 'react';

function GreetingVideo({ greetingId }) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [videoUrl, setVideoUrl] = useState(null);
  const videoRef = useRef(null);
  
  useEffect(() => {
    const fetchVideoUrl = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const response = await fetch(`/greetings/${greetingId}/video-url`);
        
        if (!response.ok) {
          if (response.status === 404) {
            throw new Error('Video not found');
          }
          throw new Error('Failed to load video');
        }
        
        const data = await response.json();
        setVideoUrl(data.videoUrl);
        
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    
    if (greetingId) {
      fetchVideoUrl();
    }
  }, [greetingId]);
  
  if (loading) {
    return <div className="loader">Loading video...</div>;
  }
  
  if (error) {
    return <div className="error">{error}</div>;
  }
  
  return (
    <video
      ref={videoRef}
      src={videoUrl}
      controls
      autoPlay
      className="greeting-video"
      onError={() => setError('Video playback failed')}
    >
      Your browser does not support video playback.
    </video>
  );
}

export default GreetingVideo;
```

---

## 🔍 Where to Find the Code

### Files to Search For
Look for files containing these patterns:

```bash
# Search in your frontend code
grep -r "videoPlaybackUrl" src/
grep -r "greeting.*video" src/
grep -r "/greetings/.*/view" src/
grep -r "video-message" src/
```

### Common File Locations
- `src/pages/VideoMessage.js`
- `src/components/GreetingVideo.jsx`
- `src/pages/QRScan.js`
- `src/components/VideoPlayer.js`
- `public/qr/video.html`
- `static/js/greeting-player.js`

---

## 📱 Mobile Considerations

### Auto-play on Mobile
```javascript
// Mobile browsers may block autoplay
video.addEventListener('loadedmetadata', () => {
  video.play().catch(err => {
    console.log('Autoplay blocked, showing play button');
    showPlayButton();
  });
});
```

### iOS Safari Specifics
```javascript
// iOS requires user interaction for fullscreen
video.setAttribute('playsinline', 'true');
video.setAttribute('webkit-playsinline', 'true');
```

---

## 🧪 Testing

### Browser Console Test
```javascript
// Paste this in browser console on the video page
fetch('/greetings/GREETING_1234567890/video-url')
  .then(r => r.json())
  .then(d => console.log('Video URL:', d.videoUrl))
  .catch(e => console.error('Error:', e));
```

### Check Network Tab
1. Open DevTools → Network tab
2. Load the video page
3. Look for request to `/video-url`
4. Response should be:
```json
{
  "videoUrl": "https://...amazonaws.com/...?X-Amz-Algorithm=..."
}
```

---

## ⚠️ Common Mistakes to Avoid

### ❌ DON'T DO THIS
```javascript
// ❌ Using old endpoint
fetch(`/greetings/${id}/view`)

// ❌ Caching the URL
const cachedUrl = localStorage.getItem('videoUrl');
video.src = cachedUrl;

// ❌ Reusing URL from state
const [videoUrl] = useState(previousUrl);

// ❌ Using videoPlaybackUrl from /view endpoint
video.src = greetingData.videoPlaybackUrl;
```

### ✅ DO THIS
```javascript
// ✅ Use new endpoint
fetch(`/greetings/${id}/video-url`)

// ✅ Fetch fresh on every page load
useEffect(() => {
  fetchVideoUrl();
}, [greetingId]);

// ✅ Don't cache, always fetch fresh
const freshUrl = await getVideoUrl();
```

---

## 📊 API Response Format

### Success Response
```json
{
  "videoUrl": "https://bucket.s3.region.amazonaws.com/greetings/ID/video.mp4?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...&X-Amz-Date=20260131T120000Z&X-Amz-Expires=600&X-Amz-Signature=..."
}
```

### Error Response (404)
```json
{
  "error": "Greeting not found"
}
```

### Error Response (500)
```json
{
  "error": "Error: Server error message"
}
```

---

## 🎨 UI/UX Recommendations

### Loading State
```html
<div id="loader" class="video-loader">
  <div class="spinner"></div>
  <p>Loading your video message...</p>
</div>
```

### Error State
```html
<div id="error" class="video-error">
  <p>Unable to load video</p>
  <button onclick="retryLoad()">Try Again</button>
</div>
```

### Video Container
```html
<div class="video-container">
  <video 
    id="greeting-video"
    controls
    playsinline
    poster="/images/video-placeholder.jpg"
  >
    Your browser doesn't support video playback.
  </video>
</div>
```

---

## 🔄 Migration Steps

### Step 1: Backup Current Code
```bash
git add .
git commit -m "Backup before video URL fix"
```

### Step 2: Update API Endpoint
```javascript
// Change from:
const url = `/greetings/${id}/view`;

// To:
const url = `/greetings/${id}/video-url`;
```

### Step 3: Update Response Parsing
```javascript
// Change from:
const videoSrc = response.videoPlaybackUrl;

// To:
const videoSrc = response.videoUrl;
```

### Step 4: Test
- Load page → Should work ✅
- Refresh → Should work ✅
- Close and reload → Should work ✅
- Scan QR again after 1 hour → Should work ✅

---

## 📞 Need Help?

### Debugging Checklist
- [ ] Network tab shows request to `/video-url`
- [ ] Response contains `videoUrl` field
- [ ] URL contains `X-Amz-Signature` parameter
- [ ] Video element's `src` is set correctly
- [ ] No 403 errors in console
- [ ] Video plays without errors

### Common Issues

**Issue:** Video still shows 403
- **Fix:** Make sure you're using `/video-url` endpoint, not `/view`

**Issue:** "Video not found"
- **Fix:** Check that video was actually uploaded

**Issue:** Video loads but won't play
- **Fix:** Check browser console for autoplay restrictions

**Issue:** Works first time, fails on reload
- **Fix:** Make sure you're fetching fresh URL on every load, not caching

---

## ✅ Checklist Before Deployment

- [ ] Updated all references to use `/video-url` endpoint
- [ ] Changed `videoPlaybackUrl` to `videoUrl`
- [ ] Added error handling for 404/500
- [ ] Removed any URL caching logic
- [ ] Tested on desktop browser
- [ ] Tested on mobile browser
- [ ] Tested QR scan flow
- [ ] Tested refresh/reload scenario
- [ ] Tested after closing tab

---

## 🎉 Result

After implementing these changes:
- ✅ Videos load on first scan
- ✅ Videos load on repeat scans
- ✅ Videos load after closing tab
- ✅ Videos load days/weeks later
- ✅ No more 403 Forbidden errors
- ✅ Secure private S3 access maintained

**That's it! Your greeting videos will now work perfectly every time! 🚀**

