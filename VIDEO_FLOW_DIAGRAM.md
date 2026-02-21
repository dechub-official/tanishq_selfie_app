# Video Playback Flow Diagram

## ❌ OLD FLOW (Broken - 403 Error)

```
┌─────────────┐
│  User Scans │
│  QR Code    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│ Frontend: /qr/video-message page    │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│ API Call: GET /greetings/{id}/view  │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│ Backend Returns:                    │
│ {                                   │
│   "videoPlaybackUrl": "https://     │
│   bucket.s3.amazonaws.com/video"    │
│ }                                   │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│ Frontend Sets:                      │
│ video.src = videoPlaybackUrl        │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│ Browser Tries to Load Video         │
└──────┬──────────────────────────────┘
       │
       ▼
     ❌ 403 FORBIDDEN
   (Private S3 bucket blocks access)
```

**Problem:** Direct S3 URL doesn't work for private buckets!

---

## ✅ NEW FLOW (Fixed - Works Every Time)

```
┌─────────────┐
│  User Scans │
│  QR Code    │
│  (Anytime!) │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│ Frontend: /qr/video-message page    │
│ (Loads fresh every time)            │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│ API Call: GET /greetings/{id}/      │
│           video-url                 │
│ (NEW ENDPOINT!)                     │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│ Backend:                            │
│ 1. Finds greeting in database       │
│ 2. Gets S3 URL                      │
│ 3. Generates FRESH pre-signed URL   │
│    (Valid for 10 minutes)           │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│ Backend Returns:                    │
│ {                                   │
│   "videoUrl": "https://bucket.s3.   │
│   amazonaws.com/video?              │
│   X-Amz-Signature=...&              │
│   X-Amz-Expires=600"                │
│ }                                   │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│ Frontend Sets:                      │
│ video.src = data.videoUrl           │
│ (Fresh signed URL!)                 │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│ Browser Loads Video                 │
└──────┬──────────────────────────────┘
       │
       ▼
     ✅ VIDEO PLAYS!
   (Pre-signed URL grants temp access)
```

**Solution:** Generate fresh pre-signed URL every time!

---

## 🔄 Comparison: First Scan vs. Rescan

### ❌ OLD System

```
FIRST SCAN:
User → QR → Page → /view API → videoPlaybackUrl → ✅ Works
                                  (Direct URL)

RESCAN (1 hour later):
User → QR → Page → /view API → videoPlaybackUrl → ❌ 403 Error
                                  (Same URL, still private)
```

### ✅ NEW System

```
FIRST SCAN:
User → QR → Page → /video-url API → Fresh Pre-signed URL → ✅ Works
                                     (Valid 10 min)

RESCAN (1 hour later):
User → QR → Page → /video-url API → NEW Fresh Pre-signed URL → ✅ Works
                                     (New signature, valid 10 min)

RESCAN (1 day later):
User → QR → Page → /video-url API → NEW Fresh Pre-signed URL → ✅ Works
                                     (New signature, valid 10 min)
```

**Every scan generates a NEW signed URL!**

---

## 🔐 Security Flow

```
┌──────────────────────────────────────────────┐
│           AWS S3 (PRIVATE BUCKET)            │
│  ┌────────────────────────────────────────┐  │
│  │  greetings/                            │  │
│  │    ├── GREETING_123/                   │  │
│  │    │     └── video.mp4  🔒 PRIVATE    │  │
│  │    ├── GREETING_456/                   │  │
│  │    │     └── video.mp4  🔒 PRIVATE    │  │
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
                    ▲
                    │
         ┌──────────┴──────────┐
         │  Pre-Signed URL     │
         │  (Temporary Key)    │
         │  Valid: 10 minutes  │
         └──────────┬──────────┘
                    │
         ┌──────────┴──────────┐
         │   Backend Server    │
         │   (Has AWS Creds)   │
         └──────────┬──────────┘
                    │
         ┌──────────┴──────────┐
         │   Frontend/Browser  │
         │   (No AWS Creds)    │
         └─────────────────────┘
```

**Flow:**
1. S3 bucket is PRIVATE (secure)
2. Backend has AWS credentials
3. Backend generates temporary signed URL
4. Frontend uses signed URL (no credentials needed)
5. URL expires after 10 minutes (security)

---

## 📊 URL Structure

### ❌ OLD (Direct URL)
```
https://bucket-name.s3.ap-south-1.amazonaws.com/greetings/GREETING_123/video.mp4
```
**Problem:** Browser blocked by S3 (bucket is private)

### ✅ NEW (Pre-Signed URL)
```
https://bucket-name.s3.ap-south-1.amazonaws.com/greetings/GREETING_123/video.mp4
  ?X-Amz-Algorithm=AWS4-HMAC-SHA256
  &X-Amz-Credential=ASIAXXX.../20260131/ap-south-1/s3/aws4_request
  &X-Amz-Date=20260131T120000Z
  &X-Amz-Expires=600
  &X-Amz-Security-Token=IQoJb3JpZ2luX2VjE...
  &X-Amz-Signature=abc123def456...
```
**Solution:** AWS validates signature and grants temporary access!

---

## 🎬 Timeline Visualization

```
Time: 0 minutes
└─ User scans QR
   └─ Backend generates signed URL (expires at +10 min)
      └─ Video plays ✅

Time: +5 minutes
└─ Video still playing (URL still valid) ✅

Time: +9 minutes
└─ Video can be replayed (URL still valid) ✅

Time: +11 minutes
└─ URL expired ❌
   └─ User refreshes page
      └─ Backend generates NEW signed URL (expires at +21 min)
         └─ Video plays ✅

Time: +24 hours
└─ User scans QR again
   └─ Backend generates NEW signed URL (expires at +10 min)
      └─ Video plays ✅
```

**Key Point:** Fresh URL generated on EVERY page load!

---

## 🔄 Frontend State Management

### ❌ DON'T DO THIS (Caching)
```javascript
// ❌ BAD - Stores URL in state/cache
const [videoUrl, setVideoUrl] = useState(cachedUrl);
localStorage.setItem('videoUrl', url);

// URL expires → Video fails later
```

### ✅ DO THIS (Fresh Every Time)
```javascript
// ✅ GOOD - Fetches fresh on every load
useEffect(() => {
  fetchFreshVideoUrl(greetingId)
    .then(url => setVideoUrl(url));
}, [greetingId]);

// Fresh URL every time → Always works
```

---

## 🧪 Test Scenarios

```
┌─────────────────────────────────────────────────────┐
│ Scenario 1: Normal Flow                             │
├─────────────────────────────────────────────────────┤
│ Action: Scan QR → Watch video → Close tab          │
│ Result: ✅ Video plays                              │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ Scenario 2: Rescan After Close                      │
├─────────────────────────────────────────────────────┤
│ Action: Close tab → Scan same QR 1 min later       │
│ Result: ✅ Video plays (new signed URL generated)  │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ Scenario 3: Rescan After Hours                      │
├─────────────────────────────────────────────────────┤
│ Action: Wait 3 hours → Scan same QR                │
│ Result: ✅ Video plays (new signed URL generated)  │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ Scenario 4: Multiple Devices                        │
├─────────────────────────────────────────────────────┤
│ Action: Scan QR on Phone 1 and Phone 2             │
│ Result: ✅ Both work (separate signed URLs)        │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ Scenario 5: Page Refresh                            │
├─────────────────────────────────────────────────────┤
│ Action: Load video → Refresh page                  │
│ Result: ✅ Video plays (new signed URL generated)  │
└─────────────────────────────────────────────────────┘
```

---

## 🎯 Key Takeaways

### The Problem
```
Private S3 → Direct URL → ❌ Browser Blocked → 403 Error
```

### The Solution
```
Private S3 → Pre-Signed URL → ✅ Temporary Access → Video Plays
```

### The Implementation
```
1. Backend: Generate fresh signed URL (10 min expiry)
2. Frontend: Request new URL on EVERY page load
3. Result: Videos work EVERY time, no matter when scanned
```

### The Rules
```
✅ DO: Fetch fresh URL on every page load
✅ DO: Use /video-url endpoint
✅ DO: Set video.src = data.videoUrl

❌ DON'T: Cache or reuse video URLs
❌ DON'T: Use videoPlaybackUrl from /view endpoint
❌ DON'T: Store URLs in localStorage/state
```

---

**That's the complete flow! Simple, secure, and works every time! 🎉**

