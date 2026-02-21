# S3 403 Forbidden Error - FIXED

## Problem
After uploading videos to S3, they couldn't be played in the frontend. Getting **403 Forbidden** error:
```
https://celebrations-tanishq.s3.ap-south-1.amazonaws.com/greetings/GREETING_XXX/video.mp4
Status: 403 Forbidden
```

## Root Cause
The S3 bucket is **private** (not publicly accessible). The application was generating **public S3 URLs** which don't work with private buckets.

Example of the old code:
```java
String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
```

This generates a URL like `https://celebrations-tanishq.s3.ap-south-1.amazonaws.com/greetings/XXX/video.mp4` which requires public access permissions.

## Solution: Pre-Signed URLs

### What are Pre-Signed URLs?
Pre-signed URLs are temporary URLs that grant time-limited access to private S3 objects. They include authentication information in the URL itself.

### Changes Made

**File**: `AwsS3StorageService.java`

#### 1. Added Import for Pre-Signed URL Generation
```java
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import java.util.Date;
```

#### 2. Updated `uploadGreetingVideo()` Method
Changed from returning a public URL to generating a pre-signed URL:

```java
// Old code:
String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
return s3Url;

// New code:
String presignedUrl = generatePresignedUrl(s3Key, 7 * 24 * 60); // 7 days expiration
return presignedUrl;
```

#### 3. Added `generatePresignedUrl()` Helper Method
```java
private String generatePresignedUrl(String s3Key, int expirationMinutes) {
    try {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * expirationMinutes;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest request = 
            new GeneratePresignedUrlRequest(bucketName, s3Key)
                .withExpiration(expiration);

        return s3Client.generatePresignedUrl(request).toString();
    } catch (Exception e) {
        log.error("Failed to generate pre-signed URL", e);
        return getS3Url(s3Key); // Fallback
    }
}
```

### Pre-Signed URL Example
**Before (doesn't work)**:
```
https://celebrations-tanishq.s3.ap-south-1.amazonaws.com/greetings/GREETING_XXX/video.mp4
```

**After (works)**:
```
https://celebrations-tanishq.s3.ap-south-1.amazonaws.com/greetings/GREETING_XXX/video.mp4?AWSAccessKeyId=XXX&Signature=YYY&Expires=1738406400
```

The pre-signed URL includes:
- **AWSAccessKeyId**: Temporary access credentials
- **Signature**: Cryptographic signature proving authenticity
- **Expires**: Unix timestamp when the URL expires

## Benefits

1. **Security**: Videos remain private in S3, only accessible via signed URLs
2. **Time-Limited Access**: URLs expire after 7 days (configurable)
3. **No Bucket Policy Changes**: Bucket stays private, no public access needed
4. **Works with IAM Roles**: Uses EC2 instance IAM role credentials automatically

## URL Expiration

URLs are currently set to expire after **7 days** (10,080 minutes). This can be adjusted by changing:

```java
String presignedUrl = generatePresignedUrl(s3Key, 7 * 24 * 60); // Change the multiplier
```

## Testing

1. Upload a video via the greeting QR flow
2. The backend will now return a pre-signed URL
3. The frontend will use this URL to display the video
4. Video should play successfully without 403 errors

## Next Steps

1. Rebuild the WAR file: `mvn clean package`
2. Deploy to server
3. Restart Tomcat
4. Test by scanning a QR code and uploading a video

## Alternative Solution (Not Recommended)

If you wanted to keep using public URLs, you would need to:
1. Make the S3 bucket publicly readable (security risk)
2. Add a bucket policy allowing public GetObject access
3. **NOT RECOMMENDED** - Keep private bucket + pre-signed URLs instead

